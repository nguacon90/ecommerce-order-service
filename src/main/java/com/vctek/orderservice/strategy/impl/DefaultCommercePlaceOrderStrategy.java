package com.vctek.orderservice.strategy.impl;

import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.loyalty.TransactionData;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.AddressRequest;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.dto.request.storefront.ShippingFeeData;
import com.vctek.orderservice.dto.request.storefront.StoreFrontCheckoutRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.feignclient.dto.OrderBillRequest;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionOrderEntryConsumedModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionOrderEntryConsumedRepository;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionResultRepository;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import com.vctek.orderservice.strategy.CommercePlaceOrderStrategy;
import com.vctek.orderservice.util.EventType;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import com.vctek.util.PaymentMethodType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DefaultCommercePlaceOrderStrategy implements CommercePlaceOrderStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCommercePlaceOrderStrategy.class);
    private OrderService orderService;
    private CartService cartService;
    private ApplicationEventPublisher eventPublisher;
    private BillService billService;
    private GenerateCartCodeService generateCartCodeService;
    private OrderSourceService orderSourceService;
    private CouponService couponService;
    private CalculationService calculationService;
    private CustomerService customerService;
    private InvoiceService invoiceService;
    private LoyaltyService loyaltyService;
    private FinanceService financeService;
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;
    private OrderSettingCustomerOptionService orderSettingCustomerOptionService;
    private OrderEntryRepository entryRepository;

    private EntryRepository abstractEntryRepository;
    private PaymentTransactionService paymentTransactionService;

    private PromotionResultRepository promotionResultRepository;

    private PromotionOrderEntryConsumedRepository promotionOrderEntryConsumedRepository;

    @Override
    @Transactional
    public CommerceOrderResult placeOrder(CommerceCheckoutParameter parameter) {
        final CartModel cartModel = parameter.getCart();
        final CommerceOrderResult result = new CommerceOrderResult();
        double paidAmount = 0d;
        double redeemAmount = 0d;
        OrderModel orderModel = orderService.createOrderFromCart(cartModel);
        populateBasicOrderModel(parameter, orderModel);

        populateVat(parameter, orderModel);
        Set<PaymentTransactionModel> paymentTransactions = parameter.getPaymentTransactions();
        if (CollectionUtils.isNotEmpty(paymentTransactions)) {
            PaymentMethodData paymentMethodData = financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code());
            for (PaymentTransactionModel payment : paymentTransactions) {
                payment.setOrderModel(orderModel);
                payment.setOrderCode(orderModel.getCode());
                payment.setWarehouseId(orderModel.getWarehouseId());
                if (paymentMethodData.getId().equals(payment.getPaymentMethodId())) {
                    LoyaltyCardData loyaltyCardData = loyaltyService.findByCardNumber(orderModel.getCardNumber(),
                            orderModel.getCompanyId());
                    payment.setConversionRate(loyaltyCardData.getConversionRate());
                    redeemAmount = payment.getAmount();
                }
                if (OrderType.ONLINE.name().equals(orderModel.getType())) {
                    paidAmount += CommonUtils.readValue(payment.getAmount());
                }
            }

            orderModel.setPaymentTransactions(paymentTransactions);
            orderModel.setPaidAmount(paidAmount);
        }

        cartService.delete(cartModel);
        OrderModel savedOrder = orderService.save(orderModel);

        createRedeemForOrder(savedOrder, redeemAmount);

        this.commerceCartCalculationStrategy.splitOrderPromotionToEntries(savedOrder);

        if (OrderType.ONLINE.toString().equals(savedOrder.getType()) && !cartModel.isExchange()) {
            savedOrder.setOrderStatus(OrderStatus.NEW.code());
            calculationService.calculateTotals(savedOrder, true);
        }
        couponService.createCouponRedemption(savedOrder);
        if (!OrderType.ONLINE.toString().equals(savedOrder.getType()) || cartModel.isExchange()) {
            Long billId = billService.createBillForOrder(savedOrder);
            savedOrder.setBillId(billId);
        }

        updateCustomerAndCreateInvoiceForCustomer(savedOrder, parameter.getCustomerRequest(), false);
        assignLoyaltyCardToCustomer(parameter.getCardNumber(), parameter.getCustomerRequest(),
                savedOrder.isExchange(), savedOrder.getWarehouseId());
        if (!OrderType.ONLINE.toString().equals(savedOrder.getType()) || savedOrder.isExchange()) {
            commerceCartCalculationStrategy.calculateLoyaltyRewardOrder(savedOrder);
        }
        orderService.save(savedOrder);
        result.setOrderModel(savedOrder);
        OrderEvent orderEvent = populateCreateOrderEvent(orderModel);
        eventPublisher.publishEvent(orderEvent);
        LOGGER.info("FINISHED DO PLACE ORDER: {'orderCode': {}, 'companyId': {}, 'customerId': {}, 'createdBy': {}}",
                orderModel.getCode(), orderModel.getCompanyId(), orderModel.getCustomerId(), orderModel.getCreateByUser());
        return result;
    }

    protected OrderEvent populateCreateOrderEvent(OrderModel orderModel) {
        OrderEvent orderEvent = new OrderEvent(orderModel);
        orderEvent.setEventType(EventType.CREATE);

        if (OrderType.ONLINE.toString().equals(orderModel.getType()) && orderModel.isExchange()) {
            orderEvent.setEventType(EventType.CHANGE_COMPLETED_ONLINE);
        }

        return orderEvent;
    }

    private void createRedeemForOrder(OrderModel orderModel, double redeemAmount) {
        if (redeemAmount > 0) {
            TransactionData transactionData;
            if (OrderType.ONLINE.toString().equals(orderModel.getType()) && !orderModel.isExchange()) {
                transactionData = loyaltyService.createRedeemPending(orderModel, redeemAmount);
            } else {
                transactionData = loyaltyService.redeem(orderModel, redeemAmount);
            }
            orderModel.setRedeemAmount(transactionData.getRedeemAmount());
        }
    }

    private void updateOrderSettingCustomerOptions(CommerceCheckoutParameter parameter, OrderModel orderModel) {
        Set<OrderSettingCustomerOptionModel> orderSettingCustomerOptionModels = new HashSet<>();
        if (CollectionUtils.isNotEmpty(parameter.getSettingCustomerOptionIds())) {
            for (Long settingCustomerOptionId : parameter.getSettingCustomerOptionIds()) {
                OrderSettingCustomerOptionModel orderSettingCustomerOptionModel = orderSettingCustomerOptionService.findByIdAndCompanyId(settingCustomerOptionId, orderModel.getCompanyId());
                if (orderSettingCustomerOptionModel != null) {
                    orderSettingCustomerOptionModels.add(orderSettingCustomerOptionModel);
                }
            }
        }
        orderModel.setOrderSettingCustomerOptionModels(orderSettingCustomerOptionModels);
    }

    private void populateBasicOrderModel(CommerceCheckoutParameter parameter, OrderModel orderModel) {
        orderModel.setCustomerNote(parameter.getCustomerNote());
        orderModel.setCustomerSupportNote(parameter.getCustomerSupportNote());
        orderModel.setDeliveryDate(parameter.getDeliveryDate());
        orderModel.setShippingCompanyId(parameter.getShippingCompanyId());
        orderModel.setDeliveryCost(parameter.getDeliveryCost());
        orderModel.setCompanyShippingFee(parameter.getCompanyShippingFee());
        orderModel.setCollaboratorShippingFee(parameter.getCollaboratorShippingFee());
        orderModel.setAge(parameter.getAge());
        orderModel.setGender(parameter.getGender());
        orderModel.setCardNumber(parameter.getCardNumber());
        orderModel.setEmployeeId(parameter.getEmployeeId());
        if (parameter.getOrderSourceId() != null) {
            populateOrderSource(parameter.getOrderSourceId(), orderModel);
        }

        if (parameter.getConfirmDiscountBy() != null) {
            orderModel.setConfirmDiscountBy(parameter.getConfirmDiscountBy());
        }

        updateOrderSettingCustomerOptions(parameter, orderModel);
    }

    private void assignLoyaltyCardToCustomer(String cardNumber, CustomerRequest customerRequest, boolean isExchange,
                                             Long warehouseId) {
        loyaltyService.assignCardToCustomerIfNeed(cardNumber, customerRequest, isExchange, warehouseId);
    }

    private void populateVat(CommerceCheckoutParameter parameter, OrderModel orderModel) {
        orderModel.setVatNumber(parameter.getVatNumber());
        orderModel.setVatDate(parameter.getVatDate());
    }

    private void populateOrderSource(Long orderSourceId, OrderModel orderModel) {
        OrderSourceModel orderSourceModel = orderSourceService.findByIdAndCompanyId(orderSourceId, orderModel.getCompanyId());
        if (orderSourceModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_SOURCE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        orderModel.setOrderSourceModel(orderSourceModel);
    }

    @Override
    public void updateProductInReturnBillWithOrder(OrderModel order, CommerceCartModification commerceCartModification) {
        billService.updateProductInReturnBillWithOrder(order, commerceCartModification);
        orderService.save(order);
    }

    @Override
    public void deleteProductInReturnBillWithOrder(OrderModel order, CommerceCartModification commerceCartModification) {
        billService.deleteProductInReturnBillWithOrder(order, commerceCartModification);
    }

    @Override
    @Transactional
    public void deleteProductOfComboInReturnBillWithOrder(OrderModel order, OrderEntryModel entryModel, SubOrderEntryModel subOrderEntryModel) {
        boolean isUpdateBill = billService.shouldUpdateBillOf(order);
        if (isUpdateBill) {
            billService.deleteProductOfComboInReturnBillWithOrder(order, subOrderEntryModel);
        }

        entryModel.getSubOrderEntries().remove(subOrderEntryModel);
        orderService.saveEntry(entryModel);
        eventPublisher.publishEvent(new OrderEvent(order));
    }

    @Override
    @Transactional
    public CommerceOrderResult updateCustomerInfoInOnlineOrder(UpdateOrderParameter updateOrderParameter) {
        final CommerceOrderResult result = new CommerceOrderResult();
        OrderModel orderToUpdate = populateUpdateCustomerInfoInOrder(updateOrderParameter);
        CustomerRequest customerRequest = updateOrderParameter.getCustomerRequest();
        OrderStatus currentStatus = OrderStatus.findByCode(orderToUpdate.getOrderStatus());
        if (currentStatus.value() < OrderStatus.CONFIRMED.value() && customerRequest != null) {
            orderToUpdate.setCustomerId(customerRequest.getId());
            AddressRequest shippingAddress = customerRequest.getShippingAddress();
            if(shippingAddress != null) {
                orderToUpdate.setShippingCustomerName(shippingAddress.getCustomerName());
                orderToUpdate.setShippingCustomerPhone(shippingAddress.getPhone1());
                orderToUpdate.setShippingAddressId(shippingAddress.getId());
                orderToUpdate.setShippingProvinceId(shippingAddress.getProvinceId());
                orderToUpdate.setShippingDistrictId(shippingAddress.getDistrictId());
                orderToUpdate.setShippingWardId(shippingAddress.getWardId());
                orderToUpdate.setShippingAddressDetail(shippingAddress.getAddressDetail());
            }
        }
        OrderModel savedOrder = orderService.save(orderToUpdate);
        result.setOrderModel(savedOrder);
        eventPublisher.publishEvent(new OrderEvent(savedOrder));
        return result;
    }

    private OrderModel populateUpdateCustomerInfoInOrder(UpdateOrderParameter updateOrderParameter) {
        OrderModel orderToUpdate = updateOrderParameter.getOrder();
        orderToUpdate.setShippingCompanyId(updateOrderParameter.getShippingCompanyId());
        orderToUpdate.setCustomerNote(updateOrderParameter.getCustomerNote());
        orderToUpdate.setCustomerSupportNote(updateOrderParameter.getCustomerSupportNote());
        orderToUpdate.setVatDate(updateOrderParameter.getVatDate());
        orderToUpdate.setVatNumber(updateOrderParameter.getVatNumber());
        orderToUpdate.setAge(updateOrderParameter.getAge());
        orderToUpdate.setGender(updateOrderParameter.getGender());
        orderToUpdate.setCardNumber(updateOrderParameter.getCardNumber());
        orderToUpdate.setDeliveryDate(updateOrderParameter.getDeliveryDate());
        if (updateOrderParameter.getOrderSourceId() != null) {
            populateOrderSource(updateOrderParameter.getOrderSourceId(), orderToUpdate);
        }

        return orderToUpdate;
    }

    @Override
    @Transactional
    public CommerceOrderResult updateOrder(UpdateOrderParameter updateOrderParameter) {
        final CommerceOrderResult result = new CommerceOrderResult();
        OrderModel orderToUpdate = populateUpdateCustomerInfoInOrder(updateOrderParameter);
        orderToUpdate.setDeliveryCost(updateOrderParameter.getDeliveryCost());
        orderToUpdate.setCompanyShippingFee(updateOrderParameter.getCompanyShippingFee());
        orderToUpdate.setCollaboratorShippingFee(updateOrderParameter.getCollaboratorShippingFee());
        this.calculationService.recalculate(orderToUpdate);
        this.commerceCartCalculationStrategy.splitOrderPromotionToEntries(orderToUpdate);

        loyaltyService.updateRewardRedeemForOrder(orderToUpdate);
        OrderModel savedOrder = orderService.save(orderToUpdate);
        this.updatePriceAndDiscountBillOf(orderToUpdate);
        result.setOrderModel(savedOrder);
        updateCustomerAndCreateInvoiceForCustomer(savedOrder, updateOrderParameter.getCustomerRequest(), true);
        assignLoyaltyCardToCustomer(updateOrderParameter.getCardNumber(), updateOrderParameter.getCustomerRequest(),
                orderToUpdate.isExchange(), orderToUpdate.getWarehouseId());
        eventPublisher.publishEvent(new OrderEvent(savedOrder));
        return result;
    }

    private void updateCustomerAndCreateInvoiceForCustomer(OrderModel order, CustomerRequest customerRequest, boolean isUpdate) {
        try {
            if (order.isExchange()) {
                Long customerId;
                if (isUpdate) {
                    ReturnOrderModel returnOrder = order.getReturnOrder();
                    OrderModel originOrder = returnOrder.getOriginOrder();
                    customerId = originOrder.getCustomerId();
                } else {
                    customerId = customerRequest.getId();
                }

                invoiceService.saveInvoices(order, customerId);
                return;
            }
            CustomerData data = customerService.update(order, customerRequest);
            Long customerId = data != null ? data.getId() : null;
            if (customerRequest != null) {
                customerRequest.setId(customerId);
            }
            invoiceService.saveInvoices(order, customerId);
        } catch (RuntimeException e) {
            LOGGER.error("ERROR create invoice for orderCode: {}", order.getCode());
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void updatePriceAndDiscountBillOf(OrderModel order) {
        try {
            billService.updatePriceAndDiscountBillOf(order);
            //Ignore rollback order when update price and discount fail.
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void updateComboInReturnBillWithOrder(OrderModel order, AbstractOrderEntryModel entry) {
        billService.updateComboInReturnBillWithOrder(order, entry);
        orderService.save(order);
    }

    @Override
    public void updateOrDeleteToppingInReturnBillWithOrder(OrderModel order, List<OrderBillRequest> orderBillRequests) {
        billService.updateOrDeleteToppingInReturnBillWithOrder(order, orderBillRequests);

    }

    @Override
    public void addProductToReturnBill(OrderModel order, AbstractOrderEntryModel abstractOrderEntry) {
        billService.addProductToReturnBill(order, abstractOrderEntry);
    }

    @Override
    public void revertComboSaleQuantity(Long companyId, OrderEntryModel entryModel) {
        AbstractOrderModel order = entryModel.getOrder();
        if (order instanceof OrderModel && billService.shouldUpdateBillOf((OrderModel) order)) {
            billService.revertComboSaleQuantity(companyId, entryModel);
        }
    }

    @Override
    @Transactional
    public OrderModel changeBillToRetail(OrderModel order) {
        OrderModel cloneRetailOrderModel = SerializationUtils.clone(order);
        cloneRetailOrderModel.setId(null);
        cloneRetailOrderModel.setType(OrderType.RETAIL.toString());
        cloneRetailOrderModel.setReturnOrders(new HashSet<>());
        cloneRetailOrderModel.setCouldFirePromotions(new HashSet<>());
        cloneRetailOrderModel.setOrderSourceModel(null);
        cloneRetailOrderModel.setOrderStatus(OrderStatus.CHANGE_TO_RETAIL.code());
        order.setOrderStatus(OrderStatus.CHANGE_TO_RETAIL.code());
        cloneRetailOrderModel.setOrderHistory(Collections.emptyList());
        List<AbstractOrderEntryModel> entryModelList = new ArrayList<>();
        for (AbstractOrderEntryModel entryModel : order.getEntries()) {
            OrderEntryModel clonedOrderEntryModel = orderService.cloneOrderEntry(cloneRetailOrderModel, entryModel);
            entryModelList.add(clonedOrderEntryModel);
            orderService.cloneSubOrderEntries(entryModel, clonedOrderEntryModel);
            orderService.cloneToppingOptions(entryModel, clonedOrderEntryModel);
        }
        cloneRetailOrderModel.setEntries(entryModelList);
        cloneRetailOrderModel.setCouponRedemptionModels(new HashSet<>());
        cloneRetailOrderModel.setTags(new HashSet<>());

        Set<PaymentTransactionModel> newPaymentList = new HashSet<>();
        for (PaymentTransactionModel paymentTransactionModel : order.getPaymentTransactions()) {
            PaymentTransactionModel transactionModel = SerializationUtils.clone(paymentTransactionModel);
            transactionModel.setId(null);
            transactionModel.setOrderModel(cloneRetailOrderModel);
            newPaymentList.add(transactionModel);
        }
        cloneRetailOrderModel.setPaymentTransactions(newPaymentList);
        Set<OrderSettingCustomerOptionModel> cloneSettings = new HashSet<>();
        order.getOrderSettingCustomerOptionModels().forEach(opt -> cloneSettings.add(opt));
        cloneRetailOrderModel.setOrderSettingCustomerOptionModels(cloneSettings);
        orderService.transferPromotionsToOrder(order.getPromotionResults(), cloneRetailOrderModel);
        orderService.transferCouponCodeToOrder(order.getOrderHasCouponCodeModels(), cloneRetailOrderModel);
        OrderModel savedRetailOrder = orderService.save(cloneRetailOrderModel);
        calculationService.calculateTotals(savedRetailOrder, true);
        String retailOrderCode = generateCartCodeService.generateCartCode(savedRetailOrder);
        savedRetailOrder.setCode(retailOrderCode);
        savedRetailOrder.getEntries().forEach(e -> e.setOrderCode(retailOrderCode));
        savedRetailOrder.getPaymentTransactions().forEach(p -> p.setOrderCode(retailOrderCode));
        savedRetailOrder.getPromotionResults().forEach(pr -> pr.setOrderCode(retailOrderCode));

        order.setOrderRetailCode(retailOrderCode);
        orderService.save(savedRetailOrder);
        orderService.save(order);
        eventPublisher.publishEvent(new OrderEvent(order));
        return savedRetailOrder;
    }

    @Override
    @Transactional
    public OrderModel cancelRedeem(OrderModel model) {
        loyaltyService.cancelPendingRedeem(model);
        Double paidAmount = CommonUtils.readValue(model.getPaidAmount());
        paidAmount -= model.getRedeemAmount();
        model.setPaidAmount(paidAmount);
        model.setRedeemAmount(null);
        OrderModel savedModel = orderService.save(model);
        eventPublisher.publishEvent(new OrderEvent(savedModel));
        return savedModel;
    }

    @Override
    @Transactional
    public double updateRedeemOnline(OrderModel model, PaymentTransactionRequest request) {
        TransactionData transactionData;
        if (OrderStatus.COMPLETED.toString().equals(model.getOrderStatus())) {
            PaymentMethodData paymentMethodData = financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code());
            Set<PaymentTransactionModel> paymentTransactions = model.getPaymentTransactions();
            for (PaymentTransactionModel payment : paymentTransactions) {
                if (!payment.isDeleted() && paymentMethodData.getId().equals(payment.getPaymentMethodId())) {
                    payment.setAmount(request.getAmount());
                    break;
                }
            }
            transactionData = loyaltyService.updateRedeem(model);
            invoiceService.saveInvoices(model, model.getCustomerId());
        } else {
            transactionData = loyaltyService.updatePendingRedeem(model, request);
        }

        Double paidAmount = CommonUtils.readValue(model.getPaidAmount());
        paidAmount += transactionData.getRedeemAmount() - CommonUtils.readValue(model.getRedeemAmount());
        model.setRedeemAmount(transactionData.getRedeemAmount());
        model.setPaidAmount(paidAmount);
        orderService.save(model);
        eventPublisher.publishEvent(new OrderEvent(model));
        return transactionData.getPoint();
    }

    @Override
    @Transactional
    public double createRedeemOnline(OrderModel model, PaymentTransactionRequest request) {
        TransactionData transactionData = loyaltyService.createRedeemPending(model, request.getAmount());
        PaymentMethodData paymentMethodData = financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code());
        Set<PaymentTransactionModel> payments = model.getPaymentTransactions();
        PaymentTransactionModel payment = new PaymentTransactionModel();
        payment.setAmount(transactionData.getRedeemAmount());
        payment.setConversionRate(transactionData.getConversionRate());
        payment.setPaymentMethodId(paymentMethodData.getId());
        payment.setMoneySourceId(request.getMoneySourceId());
        payment.setOrderModel(model);
        payment.setMoneySourceType(request.getType());
        payment.setOrderCode(model.getCode());
        payment.setWarehouseId(model.getWarehouseId());
        payments.add(payment);

        model.setRedeemAmount(transactionData.getRedeemAmount());
        Double paidAmount = CommonUtils.readValue(model.getPaidAmount());
        paidAmount += model.getRedeemAmount();
        model.setPaidAmount(paidAmount);
        orderService.save(model);
        if (OrderStatus.COMPLETED.code().equals(model.getOrderStatus())) {
            invoiceService.saveRedeemLoyaltyForOnlineOrChangeToRetailOrder(model);
            loyaltyService.completeRedeemLoyaltyForOnline(model);
        }
        eventPublisher.publishEvent(new OrderEvent(model));
        return transactionData.getPoint();
    }

    @Override
    @Transactional
    public OrderModel changeBillToRetailForKafkaImportOrderStatus(OrderModel order) {
        OrderModel cloneRetailOrderModel = cloneOrderModelChangeBillToRetail(order);
        List<AbstractOrderEntryModel> entryModelList = new ArrayList<>();
        List<OrderEntryModel> entries = entryRepository.findAllByOrderCode(order.getCode());
        for (AbstractOrderEntryModel entryModel : entries) {
            OrderEntryModel clonedOrderEntryModel = orderService.cloneOrderEntry(cloneRetailOrderModel, entryModel);
            entryModelList.add(clonedOrderEntryModel);
            orderService.cloneSubOrderEntriesForKafkaImportOrderStatus(entryModel, clonedOrderEntryModel);
            orderService.cloneToppingOptionsForKafkaImportOrderStatus(entryModel, clonedOrderEntryModel);
        }
        cloneRetailOrderModel.setEntries(entryModelList);
        cloneRetailOrderModel.setCouponRedemptionModels(new HashSet<>());

        Set<PaymentTransactionModel> newPaymentList = new HashSet<>();
        List<PaymentTransactionModel> payments = paymentTransactionService.findAllByOrderCode(order.getCode());
        for (PaymentTransactionModel paymentTransactionModel : payments) {
            PaymentTransactionModel transactionModel = SerializationUtils.clone(paymentTransactionModel);
            transactionModel.setId(null);
            transactionModel.setOrderModel(cloneRetailOrderModel);
            newPaymentList.add(transactionModel);
        }
        cloneRetailOrderModel.setPaymentTransactions(newPaymentList);
        orderService.cloneSettingCustomerOption(order, cloneRetailOrderModel);
        orderService.transferPromotionsToOrderForKafkaImportOrderStatus(order, cloneRetailOrderModel);
        orderService.transferCouponCodeToOrderForKafkaImportOrderStatus(order, cloneRetailOrderModel);

        OrderModel savedRetailOrder = orderService.save(cloneRetailOrderModel);
        calculationService.calculateTotals(savedRetailOrder, true);
        String retailOrderCode = generateCartCodeService.generateCartCode(savedRetailOrder);
        order.setOrderRetailCode(retailOrderCode);
        order.setOrderStatus(OrderStatus.CHANGE_TO_RETAIL.code());

        savedRetailOrder.setCode(retailOrderCode);
        savedRetailOrder.getEntries().forEach(e -> e.setOrderCode(retailOrderCode));
        savedRetailOrder.getPaymentTransactions().forEach(p -> p.setOrderCode(retailOrderCode));
        savedRetailOrder.getPromotionResults().forEach(pr -> pr.setOrderCode(retailOrderCode));
        OrderModel saveModel = orderService.save(savedRetailOrder);
        Double totalReward = commerceCartCalculationStrategy.calculateTotalRewardAmount(saveModel);
        saveModel.setTotalRewardAmount(totalReward);
        orderService.save(saveModel);
        return saveModel;
    }

    private OrderModel cloneOrderModelChangeBillToRetail(OrderModel order) {
        OrderModel cloneRetailOrderModel = orderService.cloneOrderFormModel(order);
        cloneRetailOrderModel.setType(OrderType.RETAIL.toString());
        cloneRetailOrderModel.setRedeemAmount(order.getRedeemAmount());
        cloneRetailOrderModel.setTotalRewardAmount(order.getTotalRewardAmount());
        cloneRetailOrderModel.setCustomerNote(order.getCustomerNote());
        cloneRetailOrderModel.setCustomerSupportNote(order.getCustomerSupportNote());
        cloneRetailOrderModel.setEmployeeId(order.getEmployeeId());
        cloneRetailOrderModel.setDeliveryDate(order.getDeliveryDate());
        cloneRetailOrderModel.setCardNumber(order.getCardNumber());
        cloneRetailOrderModel.setShippingCompanyId(order.getShippingCompanyId());
        cloneRetailOrderModel.setCustomerPhone(order.getCustomerPhone());
        cloneRetailOrderModel.setShippingAddressDetail(order.getShippingAddressDetail());
        cloneRetailOrderModel.setShippingCustomerName(order.getShippingCustomerName());
        cloneRetailOrderModel.setShippingCustomerPhone(order.getShippingCustomerPhone());
        cloneRetailOrderModel.setShippingAddressId(order.getShippingAddressId());
        cloneRetailOrderModel.setShippingProvinceId(order.getShippingProvinceId());
        cloneRetailOrderModel.setShippingDistrictId(order.getShippingDistrictId());
        cloneRetailOrderModel.setShippingWardId(order.getShippingWardId());
        cloneRetailOrderModel.setAge(order.getAge());
        cloneRetailOrderModel.setGender(order.getGender());
        cloneRetailOrderModel.setConfirmDiscountBy(order.getConfirmDiscountBy());
        cloneRetailOrderModel.setReturnOrders(new HashSet<>());
        cloneRetailOrderModel.setCouldFirePromotions(new HashSet<>());
        cloneRetailOrderModel.setOrderSourceModel(null);
        cloneRetailOrderModel.setOrderStatus(OrderStatus.CHANGE_TO_RETAIL.code());
        cloneRetailOrderModel.setOrderHistory(Collections.emptyList());
        return cloneRetailOrderModel;
    }

    @Override
    @Transactional
    public OrderModel updateSettingCustomerToOrder(OrderModel orderModel, List<Long> settingCustomerOptionIds) {
        List<OrderSettingCustomerOptionModel> optionModels = orderSettingCustomerOptionService.findAllByCompanyIdAndIdIn(orderModel.getCompanyId(), settingCustomerOptionIds);
        if (CollectionUtils.isEmpty(optionModels)) {
            orderModel.setOrderSettingCustomerOptionModels(new HashSet<>());
        } else {
            orderModel.setOrderSettingCustomerOptionModels(optionModels.stream().collect(Collectors.toSet()));
        }
        OrderModel saveModel = orderService.save(orderModel);
        eventPublisher.publishEvent(new OrderEvent(saveModel));
        return saveModel;
    }

    @Override
    @Transactional
    public OrderModel storefrontPlaceOrder(CommerceCheckoutParameter parameter) {
        final CartModel cartModel = parameter.getCart();
        OrderModel orderModel = orderService.createOrderFromCart(cartModel);
        orderModel.setCustomerNote(parameter.getCustomerNote());
        orderModel.setCustomerSupportNote(parameter.getCustomerSupportNote());
        orderModel.setShippingCompanyId(parameter.getShippingCompanyId());
        orderModel.setShippingFeeSettingId(parameter.getShippingFeeSettingId());
        orderModel.setDeliveryCost(parameter.getDeliveryCost());
        orderModel.setCompanyShippingFee(parameter.getCompanyShippingFee());
        orderModel.setOrderStatus(OrderStatus.CONFIRMED.code());
        orderModel.setCreateByUser(parameter.getCreatedByUser());
        populateOrderSource(parameter.getOrderSourceId(), orderModel);
        populateWeightOrderEntries(orderModel, parameter.getProductWeight());
        cartService.delete(cartModel);
        OrderModel savedOrder = orderService.save(orderModel);
        commerceCartCalculationStrategy.splitOrderPromotionToEntries(savedOrder);
        calculationService.calculateTotals(savedOrder, true);
        couponService.createCouponRedemption(savedOrder);
        updateCustomerAndCreateInvoiceForCustomer(savedOrder, parameter.getCustomerRequest(), false);
        orderService.save(savedOrder);
        OrderEvent orderEvent = populateCreateOrderEvent(savedOrder);
        orderEvent.setEcommerceOrder(true);
        eventPublisher.publishEvent(orderEvent);
        LOGGER.info("FINISHED DO STOREFRONT PLACE ORDER: {'orderCode': {}, 'companyId': {}, 'customerId': {}, 'createdBy': {}}",
                savedOrder.getCode(), savedOrder.getCompanyId(), savedOrder.getCustomerId(), savedOrder.getCreateByUser());
        return savedOrder;
    }

    private void populateWeightOrderEntries(OrderModel orderModel, Map<Long, Double> productWeight) {
        if (CollectionUtils.isEmpty(orderModel.getEntries()) || MapUtils.isEmpty(productWeight)) return;
        for (AbstractOrderEntryModel entry : orderModel.getEntries()) {
            if (CollectionUtils.isEmpty(entry.getSubOrderEntries())) {
                entry.setWeight(productWeight.get(entry.getProductId()));
                continue;
            }
            double weight = 0;
            for (SubOrderEntryModel subOrderEntry : entry.getSubOrderEntries()) {
                weight += productWeight.get(subOrderEntry.getProductId());
            }
            entry.setWeight(weight);
        }
    }

    @Override
    @Transactional
    public OrderModel updateAddressShipping(OrderModel orderModel, ShippingFeeData shippingFeeData, StoreFrontCheckoutRequest request) {
        orderModel.setShippingCompanyId(shippingFeeData.getShippingCompanyId());
        orderModel.setDeliveryCost(shippingFeeData.getShippingFee());
        orderModel.setCompanyShippingFee(shippingFeeData.getShippingFeeDiscount());
        orderModel.setShippingFeeSettingId(shippingFeeData.getShippingFeeSettingId());
        orderModel.setCalculated(false);
        calculationService.calculate(orderModel);
        OrderModel orderSaved = orderService.save(orderModel);
        customerService.update(orderSaved, request.getCustomer());
        OrderEvent orderEvent = populateCreateOrderEvent(orderSaved);
        orderEvent.setEventType(EventType.UPDATE);
        eventPublisher.publishEvent(orderEvent);
        return orderSaved;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    @Autowired
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Autowired
    public void setBillService(BillService billService) {
        this.billService = billService;
    }

    @Autowired
    public void setGenerateCartCodeService(GenerateCartCodeService generateCartCodeService) {
        this.generateCartCodeService = generateCartCodeService;
    }

    @Autowired
    public void setOrderSourceService(OrderSourceService orderSourceService) {
        this.orderSourceService = orderSourceService;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }

    @Autowired
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
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
    public void setFinanceService(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Autowired
    public void setCommerceCartCalculationStrategy(CommerceCartCalculationStrategy commerceCartCalculationStrategy) {
        this.commerceCartCalculationStrategy = commerceCartCalculationStrategy;
    }

    @Autowired
    public void setOrderSettingCustomerOptionService(OrderSettingCustomerOptionService orderSettingCustomerOptionService) {
        this.orderSettingCustomerOptionService = orderSettingCustomerOptionService;
    }

    @Autowired
    public void setEntryRepository(OrderEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Autowired
    public void setPaymentTransactionService(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @Autowired
    public void setPromotionResultRepository(PromotionResultRepository promotionResultRepository) {
        this.promotionResultRepository = promotionResultRepository;
    }

    @Autowired
    public void setPromotionOrderEntryConsumedRepository(PromotionOrderEntryConsumedRepository promotionOrderEntryConsumedRepository) {
        this.promotionOrderEntryConsumedRepository = promotionOrderEntryConsumedRepository;
    }

    @Autowired
    public void setAbstractEntryRepository(EntryRepository abstractEntryRepository) {
        this.abstractEntryRepository = abstractEntryRepository;
    }
}