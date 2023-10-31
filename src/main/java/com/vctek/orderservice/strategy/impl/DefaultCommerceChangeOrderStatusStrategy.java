package com.vctek.orderservice.strategy.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceChangeOrderStatusModification;
import com.vctek.orderservice.dto.CommerceChangeOrderStatusParameter;
import com.vctek.orderservice.dto.OrderEntryData;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.orderservice.service.event.OrderHistoryEvent;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import com.vctek.orderservice.strategy.CommerceChangeOrderStatusStrategy;
import com.vctek.orderservice.strategy.CommercePlaceOrderStrategy;
import com.vctek.orderservice.util.EventType;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import com.vctek.util.ProductType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultCommerceChangeOrderStatusStrategy implements CommerceChangeOrderStatusStrategy {
    private InventoryService inventoryService;
    private BillService billService;
    private CommercePlaceOrderStrategy commercePlaceOrderStrategy;
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;
    private List<String> shippingStatusList;
    private List<String> holdingStatusList;
    private List<String> newStatusList;
    private List<String> cancelStatusList;
    private OrderService orderService;
    private AuthService authService;
    private ApplicationEventPublisher applicationEventPublisher;
    private OrderHistoryService orderHistoryService;
    private InvoiceService invoiceService;
    private LoyaltyService loyaltyService;
    private LoyaltyTransactionService loyaltyTransactionService;
    private PaymentTransactionService paymentTransactionService;
    private ProductSearchService productSearchService;
    private OrderEntryRepository orderEntryRepository;
    private Converter<AbstractOrderEntryModel, OrderEntryData> entryDataConverter;
    private CouponService couponService;
    private WebSocketService webSocketService;

    @PostConstruct
    public void init() {
        shippingStatusList = new ArrayList<>();
        shippingStatusList.add(OrderStatus.SHIPPING.code());
        shippingStatusList.add(OrderStatus.RETURNING.code());

        holdingStatusList = new ArrayList<>();
        holdingStatusList.add(OrderStatus.CONFIRMED.code());
        holdingStatusList.add(OrderStatus.PACKING.code());
        holdingStatusList.add(OrderStatus.PACKAGED.code());

        newStatusList = new ArrayList<>();
        newStatusList.add(OrderStatus.NEW.code());
        newStatusList.add(OrderStatus.CONFIRMING.code());
        newStatusList.add(OrderStatus.CONFIRMING_CHANGE.code());

        cancelStatusList = new ArrayList<>();
        cancelStatusList.add(OrderStatus.ORDER_RETURN.code());
        cancelStatusList.add(OrderStatus.CUSTOMER_CANCEL.code());
        cancelStatusList.add(OrderStatus.SYSTEM_CANCEL.code());
    }

    @Override
    @Transactional
    public CommerceChangeOrderStatusModification changeToHigherStatus(CommerceChangeOrderStatusParameter parameter) {
        CommerceChangeOrderStatusModification modification = new CommerceChangeOrderStatusModification(parameter.getOrder());
        OrderStatus oldStatus = parameter.getOldStatus();
        OrderStatus newStatus = parameter.getNewStatus();

        validateChangingStatus(oldStatus, newStatus);

        OrderModel order = parameter.getOrder();
        if (OrderStatus.PRE_ORDER.equals(newStatus)) {
            return modification;
        }

        if (OrderStatus.CONFIRMED.equals(newStatus)) {
            inventoryService.holdingAllQuantityOf(order);
            inventoryService.subtractPreOrder(order);
            orderService.resetPreAndHoldingStockOf(order);
            return modification;
        }

        if (OrderStatus.SHIPPING.equals(newStatus)) {
            Long billId = billService.createReturnBillWithOrderOnline(order);
            order.setBillId(billId);
            return modification;
        }

        if (OrderStatus.COMPLETED.equals(newStatus)) {
            handleChangeLowerStatusToCompleted(oldStatus, order);
            return modification;
        }

        if (OrderStatus.RETURNING.equals(newStatus) && oldStatus.value() < OrderStatus.SHIPPING.value()) {
            ErrorCodes err = ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (OrderStatus.ORDER_RETURN.equals(newStatus)) {
            handleChangeLowerStatusToOrderReturn(oldStatus, order);
            return modification;
        }

        if (OrderStatus.CUSTOMER_CANCEL.equals(newStatus)
                || OrderStatus.SYSTEM_CANCEL.equals(newStatus)) {
            handleChangeLowerStatusToCancelOrder(oldStatus, order);
            return modification;
        }

        if (OrderStatus.CHANGE_TO_RETAIL.equals(newStatus)) {
            String retailOrderCode = handleChangeLowerStatusToChangeToRetail(oldStatus, order);
            modification.setRetailOrderCode(retailOrderCode);
            return modification;
        }


        return modification;
    }

    @Override
    @Transactional
    public CommerceChangeOrderStatusModification changeToLowerStatus(CommerceChangeOrderStatusParameter parameter) {
        OrderModel order = parameter.getOrder();
        CommerceChangeOrderStatusModification modification = new CommerceChangeOrderStatusModification(order);
        OrderStatus oldStatus = parameter.getOldStatus();
        OrderStatus newStatus = parameter.getNewStatus();
        validateChangingStatus(oldStatus, newStatus);

        if (isNotChangingStock(oldStatus, newStatus)) {
            return modification;
        }

        if (oldStatus.value() >= OrderStatus.ORDER_RETURN.value()) {
            handleHigherOrderReturnToLowerStatus(order, newStatus);
            return modification;
        }

        if (OrderStatus.COMPLETED.equals(oldStatus)) {
            handleChangeCompletedToLowerStatus(order, newStatus);
            return modification;
        }

        if (OrderStatus.SHIPPING.equals(oldStatus) || OrderStatus.RETURNING.equals(oldStatus)) {
            billService.subtractShippingStockOf(order);
            billService.cancelOnlineOrder(order);
            order.setBillId(null);

            if (newStatus.value() >= OrderStatus.CONFIRMED.value()) {
                inventoryService.holdingAllQuantityOf(order);
                inventoryService.subtractPreOrder(order);
            }
            return modification;
        }

        if (oldStatus.value() >= OrderStatus.CONFIRMED.value()) {
            inventoryService.changeAllHoldingToAvailableOf(order);
            orderService.resetPreAndHoldingStockOf(order);
            return modification;
        }

        if (OrderStatus.PRE_ORDER.equals(oldStatus)) {
            inventoryService.subtractPreOrder(order);
            inventoryService.resetHoldingStockOf(order);
            orderService.resetPreAndHoldingStockOf(order);
            return modification;
        }

        return modification;
    }

    @Override
    @Transactional
    public void changeStatusOrder(CommerceChangeOrderStatusParameter parameter) {
        OrderStatus newStatus = parameter.getNewStatus();
        OrderStatus oldStatus = parameter.getOldStatus();
        OrderModel order = parameter.getOrder();
        if ((OrderStatus.SYSTEM_CANCEL.equals(newStatus) || OrderStatus.CUSTOMER_CANCEL.equals(newStatus))
                && StringUtils.isBlank(parameter.getCancelText())) {
            ErrorCodes err = ErrorCodes.EMPTY_CANCEL_TEXT_ORDER_ONLINE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (oldStatus.value() < newStatus.value()) {
            this.changeToHigherStatus(parameter);
        } else {
            this.changeToLowerStatus(parameter);
        }

        revertAllCouponToOrder(order, newStatus);

        if (OrderType.ONLINE.toString().equals(order.getType())) {
            boolean hasRefund = refundOnlineOrder(order, oldStatus, newStatus);
            boolean hasRevert = revertOnlineOrder(order, oldStatus, newStatus);
            cancelLoyaltyInvoiceOfOnlineOrder(order, oldStatus, newStatus, hasRefund, hasRevert);
        }

        populateConfirmDiscount(parameter, order, newStatus, oldStatus);

        order.setOrderStatus(newStatus.code());
        if(OrderStatus.COMPLETED.equals(newStatus)) {
            this.commerceCartCalculationStrategy.calculateLoyaltyRewardOrder(order);
            invoiceService.saveRedeemLoyaltyForOnlineOrChangeToRetailOrder(order);
            loyaltyService.completeRedeemLoyaltyForOnline(order);
        }

        OrderModel savedOrderModel = orderService.save(order);
        OrderEvent orderEvent = new OrderEvent(savedOrderModel);
        if(OrderStatus.COMPLETED.equals(parameter.getNewStatus())) {
            orderEvent.setEventType(EventType.CHANGE_COMPLETED_ONLINE);
        }
        publishOrderEventAndCreateHistoryOrder(orderEvent, parameter, null);
        webSocketService.sendNotificationChangeOrderStatus(order);
    }

    private void revertAllCouponToOrder(OrderModel order, OrderStatus newStatus) {
        if (!OrderType.ONLINE.toString().equals(order.getType()) || !cancelStatusList.contains(newStatus.code())) return;
        couponService.revertAllCouponToOrder(order);
    }

    private void publishOrderEventAndCreateHistoryOrder(OrderEvent orderEvent, CommerceChangeOrderStatusParameter parameter, Long modifiedBy) {
        applicationEventPublisher.publishEvent(orderEvent);
        OrderHistoryModel orderHistoryModel = new OrderHistoryModel();
        orderHistoryModel.setPreviousStatus(parameter.getOldStatus().code());
        orderHistoryModel.setCurrentStatus(parameter.getNewStatus().code());
        orderHistoryModel.setOrder(orderEvent.getOrderModel());
        orderHistoryModel.setExtraData(parameter.getCancelText());
        if (modifiedBy != null) {
            orderHistoryModel.setModifiedBy(modifiedBy);
        }
        OrderHistoryModel saveOrderHistoryModel = orderHistoryService.save(orderHistoryModel);
        applicationEventPublisher.publishEvent(new OrderHistoryEvent(saveOrderHistoryModel));
    }

    private void updatePaidAmountWhenChangeSatus(OrderModel orderModel, OrderStatus oldStatus) {
        if (orderModel.getRedeemAmount() == null || orderModel.getRedeemAmount() == 0
                || isOrderStatusBetweenCompletedAndChangeToRetail(oldStatus)) return;
        double oldPaidAmount = CommonUtils.readValue(orderModel.getPaidAmount());
        double newPaidAmount = oldPaidAmount - CommonUtils.readValue(orderModel.getRedeemAmount());
        orderModel.setPaidAmount(newPaidAmount);
    }

    private void cancelLoyaltyInvoiceOfOnlineOrder(OrderModel order, OrderStatus oldStatus, OrderStatus newStatus, boolean hasRefund, boolean hasRevert) {
        if (OrderStatus.COMPLETED.equals(oldStatus) && !OrderStatus.CHANGE_TO_RETAIL.equals(newStatus)) {
            if(hasRefund) {
                invoiceService.cancelLoyaltyRedeemInvoice(order);
            }

            if(hasRevert) {
                invoiceService.cancelLoyaltyRewardInvoice(order);
            }
        }
    }

    private boolean revertOnlineOrder(OrderModel order, OrderStatus oldStatus, OrderStatus newStatus) {
        if (CommonUtils.readValue(order.getTotalRewardAmount()) == 0) {
            return false;
        }

        if (OrderStatus.COMPLETED.equals(oldStatus) && !OrderStatus.CHANGE_TO_RETAIL.equals(newStatus)) {
            loyaltyService.revertOnlineOrderReward(order, order.getTotalRewardAmount());
            order.setTotalRewardAmount(null);
            order.setRewardPoint(null);
            return true;
        }

        return false;
    }

    private boolean refundOnlineOrder(OrderModel order, OrderStatus oldStatus, OrderStatus newStatus) {
        if (CommonUtils.readValue(order.getRedeemAmount()) == 0) {
            return false;
        }

        if (isOrderStatusBetweenCompletedAndChangeToRetail(oldStatus)) {
            updatePaidAmountWhenChangeSatus(order, oldStatus);
            refundOnlineOrderForStatusBetweenCompletedAndChangeToRetail(newStatus, order);
            return false;
        }

        if (OrderStatus.CHANGE_TO_RETAIL.equals(newStatus)) {
            return false;
        }

        if (OrderStatus.COMPLETED.equals(oldStatus)) {
            loyaltyService.refund(order, null, order.getRedeemAmount());
            updatePaidAmountWhenChangeSatus(order, oldStatus);
            if (newStatus.value() < OrderStatus.COMPLETED.value()) {
                order.setRedeemAmount(null);
            }
            return true;
        }

        if ((oldStatus.value() >= OrderStatus.CONFIRMED.value() && oldStatus.value() < OrderStatus.COMPLETED.value()
                && isOrderStatusBetweenCompletedAndChangeToRetail(newStatus))) {
            loyaltyService.cancelPendingRedeemForCancelOrder(order);
            updatePaidAmountWhenChangeSatus(order, oldStatus);
            return false;
        }

        return false;
    }

    private void refundOnlineOrderForStatusBetweenCompletedAndChangeToRetail(OrderStatus newStatus, OrderModel order) {
        if (!isOrderStatusBetweenCompletedAndChangeToRetail(newStatus)) {
            order.setRedeemAmount(null);
            PaymentTransactionModel paymentTransactionModel = paymentTransactionService.findLoyaltyRedeem(order);
            if (paymentTransactionModel != null) {
                paymentTransactionModel.setDeleted(true);
                paymentTransactionService.save(paymentTransactionModel);
            }
        }
    }

    private boolean isOrderStatusBetweenCompletedAndChangeToRetail(OrderStatus orderStatus) {
        return orderStatus.value() > OrderStatus.COMPLETED.value() && orderStatus.value() < OrderStatus.CHANGE_TO_RETAIL.value();
    }

    private void populateConfirmDiscount(CommerceChangeOrderStatusParameter parameter, OrderModel order, OrderStatus newStatus, OrderStatus oldStatus) {
        if (order.getConfirmDiscountBy() != null && oldStatus.value() >= OrderStatus.CONFIRMED.value()
                && newStatus.value() < OrderStatus.CONFIRMED.value()) {
            order.setConfirmDiscountBy(null);
            return;
        }

        if (parameter.isConfirmDiscount() && order.getConfirmDiscountBy() == null
                && OrderStatus.CONFIRMED.code().equals(newStatus.code())) {
            order.setConfirmDiscountBy(authService.getCurrentUserId());
            return;
        }
    }

    private boolean isNotChangingStock(OrderStatus oldStatus, OrderStatus newStatus) {
        if (shippingStatusList.contains(oldStatus.code()) && shippingStatusList.contains(newStatus.code())) {
            return true;
        }

        if (holdingStatusList.contains(oldStatus.code()) && holdingStatusList.contains(newStatus.code())) {
            return true;
        }

        if (newStatusList.contains(oldStatus.code()) && newStatusList.contains(newStatus.code())) {
            return true;
        }

        if (cancelStatusList.contains(oldStatus.code()) && cancelStatusList.contains(newStatus.code())) {
            return true;
        }

        return false;
    }

    private void handleChangeCompletedToLowerStatus(OrderModel order, OrderStatus newStatus) {
        billService.cancelOnlineOrder(order);
        order.setBillId(null);

        if (newStatus.value() < OrderStatus.CONFIRMED.value()) {
            return;
        }

        if (newStatus.value() <= OrderStatus.RETURNING.value()) {
            inventoryService.holdingAllQuantityOf(order);
            inventoryService.subtractPreOrder(order);
            if (newStatus.value() >= OrderStatus.SHIPPING.value()) {
                Long billId = billService.createReturnBillWithOrderOnline(order);
                order.setBillId(billId);
            }
        }
    }

    private void handleHigherOrderReturnToLowerStatus(OrderModel order, OrderStatus newStatus) {
        if (newStatus.value() < OrderStatus.CONFIRMED.value()) {
            return;
        }

        if (OrderStatus.COMPLETED.equals(newStatus)) {
            inventoryService.holdingAllQuantityOf(order);
            inventoryService.subtractPreOrder(order);
            Long billId = billService.createReturnBillWithOrderOnline(order);
            order.setBillId(billId);
            billService.subtractShippingStockOf(order);
            return;
        }

        if (newStatus.value() <= OrderStatus.RETURNING.value()) {
            inventoryService.holdingAllQuantityOf(order);
            inventoryService.subtractPreOrder(order);

            if (newStatus.value() >= OrderStatus.SHIPPING.value()) {
                Long billId = billService.createReturnBillWithOrderOnline(order);
                order.setBillId(billId);
            }

            return;
        }
    }

    private void handleChangeLowerStatusToCompleted(OrderStatus oldStatus, OrderModel order) {
        if (oldStatus.value() < OrderStatus.SHIPPING.value()) {
            ErrorCodes err = ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        billService.subtractShippingStockOf(order);
    }

    private void handleChangeLowerStatusToOrderReturn(OrderStatus oldStatus, OrderModel order) {
        if (oldStatus.value() < OrderStatus.SHIPPING.value()) {
            ErrorCodes err = ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (oldStatus.equals(OrderStatus.COMPLETED)) {
            billService.addShippingStockOf(order);
        }
        billService.changeOrderStatusToOrderReturn(order);
        order.setBillId(null);
    }

    private void handleChangeLowerStatusToCancelOrder(OrderStatus oldStatus, OrderModel order) {
        if (OrderStatus.ORDER_RETURN.equals(oldStatus) ||
                OrderStatus.CUSTOMER_CANCEL.equals(oldStatus) ||
                OrderStatus.SYSTEM_CANCEL.equals(oldStatus)) {
            return;
        }

        if (oldStatus.value() < OrderStatus.SHIPPING.value()) {
            inventoryService.changeAllHoldingToAvailableOf(order);
            return;
        }

        if (OrderStatus.SHIPPING.equals(oldStatus) || OrderStatus.RETURNING.equals(oldStatus)) {
            billService.subtractShippingStockOf(order);
        }

        billService.cancelOnlineOrder(order);
        order.setBillId(null);
    }

    private String handleChangeLowerStatusToChangeToRetail(OrderStatus oldStatus, OrderModel order) {
        if (OrderStatus.SHIPPING.equals(oldStatus) || OrderStatus.RETURNING.equals(oldStatus)) {
            this.commerceCartCalculationStrategy.calculateLoyaltyRewardOrder(order);
            billService.subtractShippingStockOf(order);
            OrderModel retailOrder = commercePlaceOrderStrategy.changeBillToRetail(order);
            invoiceService.saveRedeemLoyaltyForOnlineOrChangeToRetailOrder(retailOrder);
            loyaltyService.cancelPendingRedeemForCancelOrder(order);
            loyaltyService.redeem(retailOrder, CommonUtils.readValue(retailOrder.getRedeemAmount()));
            OrderEvent orderEvent = new OrderEvent(retailOrder);
            orderEvent.setEventType(EventType.CREATE);
            applicationEventPublisher.publishEvent(orderEvent);
            return retailOrder.getCode();
        }

        if (OrderStatus.COMPLETED.equals(oldStatus)) {
            OrderModel retailOrder = commercePlaceOrderStrategy.changeBillToRetail(order);
            loyaltyTransactionService.cloneAwardRedeemLoyaltyTransaction(order.getCode(), retailOrder.getCode());
            OrderEvent orderEvent = new OrderEvent(retailOrder);
            applicationEventPublisher.publishEvent(orderEvent);
            return retailOrder.getCode();
        }

        if (isOrderStatusBetweenCompletedAndChangeToRetail(oldStatus)) {
            order.setRedeemAmount(null);
            PaymentTransactionModel paymentTransactionModel = paymentTransactionService.findLoyaltyRedeem(order);
            if (paymentTransactionModel != null) {
                paymentTransactionModel.setDeleted(true);
                paymentTransactionService.save(paymentTransactionModel);
            }
        }

        if (oldStatus.value() >= OrderStatus.CONFIRMED.value()
                && oldStatus.value() < OrderStatus.SHIPPING.value()) {
            inventoryService.changeAllHoldingToAvailableOf(order);
            orderService.resetPreAndHoldingStockOf(order);
        } else if (OrderStatus.PRE_ORDER.equals(oldStatus)) {
            inventoryService.subtractPreOrder(order);
            inventoryService.resetHoldingStockOf(order);
            orderService.resetPreAndHoldingStockOf(order);
        }
        this.commerceCartCalculationStrategy.calculateLoyaltyRewardOrder(order);
        OrderModel retailOrder = commercePlaceOrderStrategy.changeBillToRetail(order);
        loyaltyService.cancelPendingRedeemForCancelOrder(order);
        loyaltyService.redeem(retailOrder, CommonUtils.readValue(retailOrder.getRedeemAmount()));
        Long billId = billService.createBillForOrder(retailOrder);
        retailOrder.setBillId(billId);
        orderService.save(retailOrder);
        invoiceService.saveRedeemLoyaltyForOnlineOrChangeToRetailOrder(retailOrder);
        OrderEvent orderEvent = new OrderEvent(retailOrder);
        orderEvent.setEventType(EventType.CREATE);
        applicationEventPublisher.publishEvent(orderEvent);
        return retailOrder.getCode();
    }

    private void validateChangingStatus(OrderStatus oldStatus, OrderStatus newStatus) {
        if (oldStatus.value() <= OrderStatus.PRE_ORDER.value()
                && newStatus.value() > OrderStatus.CONFIRMED.value()) {
            ErrorCodes err = ErrorCodes.CANNOT_CHANGE_STATUS_OVER_CONFIRMED;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (oldStatus.equals(OrderStatus.CHANGE_TO_RETAIL)) {
            ErrorCodes err = ErrorCodes.CAN_NOT_CHANGE_STATUS_THIS_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(),
                    new Object[]{oldStatus, newStatus});
        }
    }


    @Override
    @Transactional
    public void importChangeStatusOrder(CommerceChangeOrderStatusParameter parameter, Long modifiedBy) {
        OrderStatus newStatus = parameter.getNewStatus();
        OrderStatus oldStatus = parameter.getOldStatus();
        OrderModel order = parameter.getOrder();
        List<OrderEntryModel> entries = orderEntryRepository.findAllByOrderCode(order.getCode());
        List<OrderEntryData> oldEntries = entryDataConverter.convertAll(entries);
        validateChangeStatusOrder(order.getCompanyId(), entries, oldStatus, newStatus);
        OrderModel retailOrder = null;
        if (oldStatus.value() < newStatus.value()) {
            retailOrder = importChangeToHigherStatus(parameter, entries);
        } else {
            importChangeToLowerStatus(order, oldStatus, newStatus, entries);
        }

        if ((oldStatus.value() >= OrderStatus.CONFIRMED.value() && oldStatus.value() < OrderStatus.COMPLETED.value()
                && isOrderStatusBetweenCompletedAndChangeToRetail(newStatus))) {
            updatePaidAmountWhenChangeSatus(order, oldStatus);
        }

        if (OrderStatus.COMPLETED.equals(newStatus)) {
            Double totalReward = commerceCartCalculationStrategy.calculateTotalRewardAmount(order);
            order.setTotalRewardAmount(totalReward);
        }

        revertAllCouponToOrder(order, newStatus);
        populateConfirmDiscount(parameter, order, newStatus, oldStatus);
        order.setOrderStatus(newStatus.code());

        orderEntryRepository.saveAll(entries);
        OrderModel savedOrderModel = orderService.save(order);
        OrderEvent orderEvent = new OrderEvent(savedOrderModel);
        orderEvent.setOldOrderStatus(oldStatus);
        orderEvent.setCurrentUserId(modifiedBy);
        orderEvent.setOldEntries(oldEntries);
        orderEvent.setImportDetailId(parameter.getImportDetailId());
        orderEvent.setEventType(EventType.IMPORT_CHANGE_STATUS_ONLINE);
        publishOrderEventAndCreateHistoryOrder(orderEvent, parameter, modifiedBy);

        if (retailOrder != null) {
            OrderEvent orderRetailEvent = new OrderEvent(retailOrder);
            orderRetailEvent.setEventType(EventType.IMPORT_CHANGE_STATUS_ONLINE);
            orderRetailEvent.setCurrentUserId(modifiedBy);
            orderRetailEvent.setOldOrderStatus(oldStatus);
            applicationEventPublisher.publishEvent(orderRetailEvent);
        }
    }

    private OrderModel importChangeToHigherStatus(CommerceChangeOrderStatusParameter parameter, List<OrderEntryModel> entries) {
        OrderStatus newStatus = parameter.getNewStatus();
        OrderStatus oldStatus = parameter.getOldStatus();
        OrderModel order = parameter.getOrder();
        validateChangingStatus(oldStatus, newStatus);
        if (OrderStatus.PRE_ORDER.equals(newStatus)) {
            orderService.holdingStockAndResetPreStockOf(entries);
            return null;
        }

        if (OrderStatus.CONFIRMED.equals(newStatus)) {
            orderService.resetPreAndHoldingStockOfEntries(entries);
            return null;
        }

        if (OrderStatus.ORDER_RETURN.equals(newStatus)) {
            order.setBillId(null);
            return null;
        }

        if (OrderStatus.CUSTOMER_CANCEL.equals(newStatus) || OrderStatus.SYSTEM_CANCEL.equals(newStatus)) {
            if (oldStatus.value() < OrderStatus.SHIPPING.value()) {
                orderService.resetPreAndHoldingStockOfEntries(entries);
                return null;
            }
            order.setBillId(null);
            return null;
        }

        if (OrderStatus.CHANGE_TO_RETAIL.equals(newStatus)) {
            return changeToRetailForImportChangeOrderStatus(order, entries, oldStatus);
        }
        return null;
    }

    private OrderModel changeToRetailForImportChangeOrderStatus(OrderModel order, List<OrderEntryModel> entries, OrderStatus oldStatus) {
        if (oldStatus.value() >= OrderStatus.CONFIRMED.value() && oldStatus.value() < OrderStatus.SHIPPING.value()) {
            orderService.resetPreAndHoldingStockOfEntries(entries);
        }

        OrderModel retailOrder = commercePlaceOrderStrategy.changeBillToRetailForKafkaImportOrderStatus(order);
        paymentTransactionService.resetPaymentForLoyaltyRedeem(order);
        return retailOrder;
    }

    private void validateChangeStatusOrder(Long companyId, List<OrderEntryModel> entries, OrderStatus oldStatus, OrderStatus newStatus) {
        List<OrderStatus> ignoreStatus = new ArrayList<>();
        ignoreStatus.add(OrderStatus.COMPLETED);
        ignoreStatus.add(OrderStatus.ORDER_RETURN);
        ignoreStatus.add(OrderStatus.CUSTOMER_CANCEL);
        ignoreStatus.add(OrderStatus.SYSTEM_CANCEL);
        ignoreStatus.add(OrderStatus.CHANGE_TO_RETAIL);
        if (ignoreStatus.contains(oldStatus)) {
            ErrorCodes err = ErrorCodes.CANNOT_CHANGE_WITH_CURRENT_STATUS_OVER_COMPLETED;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if ((OrderStatus.COMPLETED.equals(newStatus) || OrderStatus.RETURNING.equals(newStatus) || OrderStatus.ORDER_RETURN.equals(newStatus))
                && oldStatus.value() < OrderStatus.SHIPPING.value()) {
            ErrorCodes err = ErrorCodes.NOT_ACCEPT_CHANGE_STATUS_BEFORE_SHIPPING;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (!OrderStatus.PRE_ORDER.equals(newStatus)) return;

        String productIds = entries.stream().map(i -> i.getProductId().toString()).collect(Collectors.joining(","));
        ProductSearchRequest request = new ProductSearchRequest();
        request.setCompanyId(companyId);
        request.setIds(productIds);
        request.setPageSize(entries.size());
        List<ProductSearchModel> productSearchData = productSearchService.findAllByCompanyId(request);
        for (ProductSearchModel product : productSearchData) {
            if (ProductType.FOOD.code().equals(product.getProductType()) || ProductType.BEVERAGE.code().equals(product.getProductType())) {
                ErrorCodes err = ErrorCodes.CANNOT_PRE_ORDER_CONTAIN_FOOD_BEVERAGE_ENTRY;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }

    private void importChangeToLowerStatus(OrderModel order, OrderStatus oldStatus, OrderStatus newStatus, List<OrderEntryModel> entries) {
        validateChangingStatus(oldStatus, newStatus);

        if (isNotChangingStock(oldStatus, newStatus)) {
            return;
        }

        if (OrderStatus.SHIPPING.equals(oldStatus) || OrderStatus.RETURNING.equals(oldStatus)) {
            order.setBillId(null);
            if (OrderStatus.PRE_ORDER.equals(newStatus)) {
                orderService.holdingStockAndResetPreStockOf(entries);
            }
            return;
        }

        if (oldStatus.value() >= OrderStatus.CONFIRMED.value()) {
            if (OrderStatus.PRE_ORDER.equals(newStatus)) {
                orderService.holdingStockAndResetPreStockOf(entries);
                return;
            }
            orderService.resetPreAndHoldingStockOfEntries(entries);
            return;
        }

        if (OrderStatus.PRE_ORDER.equals(oldStatus)) {
            orderService.resetPreAndHoldingStockOfEntries(entries);
        }
    }

    @Autowired
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Autowired
    public void setBillService(BillService billService) {
        this.billService = billService;
    }

    @Autowired
    public void setCommercePlaceOrderStrategy(CommercePlaceOrderStrategy commercePlaceOrderStrategy) {
        this.commercePlaceOrderStrategy = commercePlaceOrderStrategy;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setOrderHistoryService(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @Autowired
    public void setInvoiceService(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @Autowired
    public void setLoyaltyTransactionService(LoyaltyTransactionService loyaltyTransactionService) {
        this.loyaltyTransactionService = loyaltyTransactionService;
    }

    @Autowired
    public void setCommerceCartCalculationStrategy(CommerceCartCalculationStrategy commerceCartCalculationStrategy) {
        this.commerceCartCalculationStrategy = commerceCartCalculationStrategy;
    }

    @Autowired
    public void setPaymentTransactionService(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @Autowired
    public void setProductSearchService(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @Autowired
    public void setOrderEntryRepository(OrderEntryRepository orderEntryRepository) {
        this.orderEntryRepository = orderEntryRepository;
    }

    @Autowired
    public void setEntryDataConverter(Converter<AbstractOrderEntryModel, OrderEntryData> entryDataConverter) {
        this.entryDataConverter = entryDataConverter;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }

    @Autowired
    public void setWebSocketService(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }
}
