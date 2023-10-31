package com.vctek.orderservice.service.impl;

import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.ReturnOrderBillDTO;
import com.vctek.orderservice.dto.CommerceCartModification;
import com.vctek.orderservice.dto.request.ComboOrToppingOrderRequest;
import com.vctek.orderservice.dto.request.LinkReturnOrderforbillRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.BillClient;
import com.vctek.orderservice.feignclient.ProductClient;
import com.vctek.orderservice.feignclient.dto.*;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import com.vctek.orderservice.service.BillService;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.util.BillStatus;
import com.vctek.orderservice.util.CurrencyUtils;
import com.vctek.orderservice.util.DiscountType;
import com.vctek.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class BillServiceImpl implements BillService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BillServiceImpl.class);
    private BillClient billClient;
    private CalculationService calculationService;
    private OrderService orderService;
    private ProductClient productClient;

    public BillServiceImpl(BillClient billClient, CalculationService calculationService) {
        this.billClient = billClient;
        this.calculationService = calculationService;
    }

    @Override
    public Long createBillForOrder(OrderModel orderModel) {
        this.revertBillForOrder(orderModel.getCode(), orderModel.getCompanyId());
        BillRequest billRequest = this.populateBillRequest(orderModel);
        return billClient.createReturnBillWithOrder(billRequest);
    }

    @Override
    public void revertBillForOrder(String orderCode, Long companyId) {
        try {
            OrderBillRequest revertOrderBillReq = new OrderBillRequest();
            revertOrderBillReq.setCompanyId(companyId);
            revertOrderBillReq.setOrderCode(orderCode);
            billClient.revertReturnBillWithOrder(revertOrderBillReq);
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void updateProductInReturnBillWithOrder(OrderModel order, CommerceCartModification commerceCartModification) {
        Long billId = order.getBillId();
        AbstractOrderEntryModel entry = commerceCartModification.getEntry();
        if (billId != null) {
            if (orderService.isComboEntry(entry)) {
                this.updateComboInReturnBillWithOrder(order, commerceCartModification.getEntry());
                return;
            }
            OrderBillRequest orderBillRequest = new OrderBillRequest();
            orderBillRequest.setOrderCode(order.getCode());
            orderBillRequest.setCompanyId(order.getCompanyId());
            orderBillRequest.setBillId(order.getBillId());
            orderBillRequest.setDiscount(calculationService.calculateFinalDiscountOfEntry(entry));
            orderBillRequest.setDiscountType(DiscountType.CASH.toString());
            orderBillRequest.setProductId(entry.getProductId());
            orderBillRequest.setPrice(entry.getBasePrice());
            orderBillRequest.setQuantity(entry.getQuantity() == null ? 0 : entry.getQuantity().intValue());
            orderBillRequest.setWarehouseId(order.getWarehouseId());
            orderBillRequest.setOrderEntryId(entry.getId());
            orderBillRequest.setSaleOff(entry.isSaleOff());
            orderBillRequest.setOrderStatus(order.getOrderStatus());
            billClient.updateProductInReturnBillWithOrder(orderBillRequest);
            return;
        }

        billId = billClient.getBillOfOrder(order.getCode(), order.getCompanyId(), BillType.RECEIPT_BILL.code(),
                BillStatus.VERIFIED.code());

        if (billId == null && !isOrderHasOnlyDynamicComboEntry(order)) {
            LOGGER.info("Create bill for empty bill id of order: {}", order.getCode());
            Long billIdForOrder = this.createBillForOrder(order);
            order.setBillId(billIdForOrder);
        }
    }

    protected boolean isOrderHasOnlyDynamicComboEntry(OrderModel order) {
        List<AbstractOrderEntryModel> entries = order.getEntries();
        if(CollectionUtils.isEmpty(entries)) {
            return false;
        }

        Optional<AbstractOrderEntryModel> validEntryOption = entries.stream()
                .filter(e -> StringUtils.isBlank(e.getComboType()) || ComboType.FIXED_COMBO.toString().equals(e.getComboType()))
                .findFirst();
        if(validEntryOption.isPresent()) {
            return false;
        }

        List<AbstractOrderEntryModel> comboEntries = entries.stream()
                .filter(e -> ComboType.MULTI_GROUP.toString().equals(e.getComboType()) ||
                ComboType.ONE_GROUP.toString().equals(e.getComboType())).collect(Collectors.toList());

        for(AbstractOrderEntryModel entryModel : comboEntries) {
            if(CollectionUtils.isNotEmpty(entryModel.getSubOrderEntries())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void updateComboInReturnBillWithOrder(OrderModel order, AbstractOrderEntryModel entry) {
        if(order.getBillId() == null) {
            Long billIdForOrder = this.createBillForOrder(order);
            order.setBillId(billIdForOrder);
            return;
        }
        ComboOrToppingOrderRequest comboOrToppingOrderRequest = populateComboOrderRequest(order, entry);
        billClient.updateComBoInReturnBillWithOrder(comboOrToppingOrderRequest);
    }

    @Override
    public void updateOrDeleteToppingInReturnBillWithOrder(OrderModel order, List<OrderBillRequest> orderBillRequests) {
        ComboOrToppingOrderRequest comboOrToppingOrderRequest = new ComboOrToppingOrderRequest();
        comboOrToppingOrderRequest.setCompanyId(order.getCompanyId());
        comboOrToppingOrderRequest.setBillId(order.getBillId());
        comboOrToppingOrderRequest.setOrderCode(order.getCode());
        comboOrToppingOrderRequest.setOrderRequestList(orderBillRequests);
        billClient.updateOrDeleteToppingInReturnBillWithOrder(comboOrToppingOrderRequest);
    }

    @Override
    public ReturnOrderBillDTO getBillWithReturnOrder(Long billId, Long companyId, Long returnOrderId) {
        return billClient.getBillWithReturnOrder(billId, returnOrderId, companyId);
    }

    @Override
    public void addProductToReturnBill(OrderModel order, AbstractOrderEntryModel abstractOrderEntry) {
        AddProductBillRequest request = new AddProductBillRequest();
        request.setBillId(order.getBillId());
        request.setOrderCode(order.getCode());
        request.setCompanyId(order.getCompanyId());
        request.setWarehouseId(order.getWarehouseId());
        List<BillDetailRequest> billDetails = new ArrayList<>();
        BillDetailRequest billDetail = populateBillDetailRequest(abstractOrderEntry);
        if (abstractOrderEntry.isGiveAway()) {
            billDetail.setDiscount(abstractOrderEntry.getDiscount());
            billDetail.setDiscountType(abstractOrderEntry.getDiscountType());
        }
        billDetail.setOrderStatus(order.getOrderStatus());
        billDetails.add(billDetail);
        request.setBillDetails(billDetails);
        billClient.addProductToReturnBillWithOrder(request);
    }

    @Override
    public boolean shouldUpdateBillOf(OrderModel orderModel) {
        if (!OrderType.ONLINE.toString().equals(orderModel.getType())) {
            return true;
        }

        OrderStatus status = OrderStatus.findByCode(orderModel.getOrderStatus());
        if (status != null && (status.value() < OrderStatus.SHIPPING.value())) {
            return false;
        }
        return true;
    }

    @Override
    public void updateOriginOrderCode(UpdateReturnOrderBillRequest returnOrderBillRequest) {
        billClient.updateOriginOrderCode(returnOrderBillRequest.getBillId(), returnOrderBillRequest.getReturnOrderId(), returnOrderBillRequest);
    }

    @Override
    public void revertComboSaleQuantity(Long companyId, OrderEntryModel entryModel) {
        ComboRequest comboRequest = new ComboRequest();
        comboRequest.setId(entryModel.getProductId());
        comboRequest.setCompanyId(companyId);
        if(entryModel.getQuantity() == null) {
            return;
        }
        comboRequest.setSaleQuantity(-entryModel.getQuantity().intValue());
        productClient.updateSaleQuantity(Collections.singletonList(comboRequest));
    }

    @Override
    public Long linkReturnOrderforbill(LinkReturnOrderforbillRequest request) {
        return billClient.linkReturnOrderforbill(request);
    }

    @Override
    public void cancelOnlineOrder(OrderModel order) {
        OrderBillRequest cancelOnlineOrderReq = new OrderBillRequest();
        cancelOnlineOrderReq.setCompanyId(order.getCompanyId());
        cancelOnlineOrderReq.setOrderCode(order.getCode());
        cancelOnlineOrderReq.setBillId(order.getBillId());
        billClient.cancelOnlineOrder(cancelOnlineOrderReq);
    }

    @Override
    public void revertOnlineBillWhenError(OrderStatus oldStatus, OrderStatus newStatus, OrderModel order) {
        if(!OrderStatus.SHIPPING.equals(newStatus)) {
            return;
        }

        if(oldStatus.value() >= OrderStatus.SHIPPING.value()) {
            return;
        }
        OrderBillRequest revertOrderBillReq = new OrderBillRequest();
        revertOrderBillReq.setCompanyId(order.getCompanyId());
        revertOrderBillReq.setOrderCode(order.getCode());
        billClient.revertReturnBillWithOnlineOrder(revertOrderBillReq);
        Long billId = order.getBillId();
        order.setBillId(null);
        orderService.save(order);
        LOGGER.info("REVERT ONLINE RETURN BILL SUCCESS: orderCode: {}, billId: {}", order.getCode(), billId);
    }

    @Override
    public void deleteProductInReturnBillWithOrder(OrderModel order, CommerceCartModification commerceCartModification) {
        AbstractOrderEntryModel entry = commerceCartModification.getEntry();
        if (orderService.isComboEntry(entry)) {
            deleteComboInReturnBillWithOrder(order, entry);
            return;
        }
        OrderBillRequest orderBillRequest = setOrderBillRequest(order, entry, commerceCartModification.getProductId(), null, null);
        try {
            billClient.deleteProductInReturnBillWithOrder(orderBillRequest);
        } catch (RuntimeException e) {
            LOGGER.error("REVERT ALL INVENTORY FAIL of productId: billId: {}, orderCode: {}, productId: {}",
                    order.getBillId(), order.getCode(), commerceCartModification.getProductId());
            throw e;
        }
    }

    @Override
    public void deleteProductOfComboInReturnBillWithOrder(OrderModel order, SubOrderEntryModel subOrderEntryModel) {
        AbstractOrderEntryModel orderEntry = subOrderEntryModel.getOrderEntry();
        OrderBillRequest orderBillRequest = setOrderBillRequest(order, orderEntry, subOrderEntryModel.getProductId(),
                subOrderEntryModel, orderEntry.getProductId());
        try {
            billClient.deleteProductInReturnBillWithOrder(orderBillRequest);
        } catch (RuntimeException e) {
            LOGGER.error("REVERT ALL INVENTORY FAIL of productId: billId: {}, orderCode: {}, productId: {}",
                    order.getBillId(), order.getCode(), subOrderEntryModel.getProductId());
            throw e;
        }
    }

    private void deleteComboInReturnBillWithOrder(OrderModel order, AbstractOrderEntryModel entry) {
        ComboOrToppingOrderRequest comboOrToppingOrderRequest = populateComboOrderRequest(order, entry);
        try {
            billClient.deleteComboInReturnBillWithOrder(comboOrToppingOrderRequest);
        } catch (RuntimeException e) {
            LOGGER.error("REVERT ALL INVENTORY FAIL of productId: billId: {}, orderCode: {}, comboId: {}",
                    order.getBillId(), order.getCode(), entry.getProductId());
            throw e;
        }
    }

    private ComboOrToppingOrderRequest populateComboOrderRequest(OrderModel order, AbstractOrderEntryModel entry) {
        ComboOrToppingOrderRequest comboOrToppingOrderRequest = new ComboOrToppingOrderRequest();
        comboOrToppingOrderRequest.setComboId(entry.getProductId());
        comboOrToppingOrderRequest.setOrderEntryId(entry.getId());
        comboOrToppingOrderRequest.setCompanyId(order.getCompanyId());
        comboOrToppingOrderRequest.setBillId(order.getBillId());
        comboOrToppingOrderRequest.setOrderCode(order.getCode());
        comboOrToppingOrderRequest.setQuantity(entry.getQuantity().intValue());

        List<OrderBillRequest> orderBillRequests = new ArrayList<>();
        for (SubOrderEntryModel subOrderEntryModel : entry.getSubOrderEntries()) {
            OrderBillRequest orderBillRequest = setOrderBillRequest(order, entry, subOrderEntryModel.getProductId(),
                    subOrderEntryModel, entry.getProductId());
            orderBillRequests.add(orderBillRequest);
        }
        comboOrToppingOrderRequest.setOrderRequestList(orderBillRequests);
        return comboOrToppingOrderRequest;
    }

    private OrderBillRequest setOrderBillRequest(OrderModel order, AbstractOrderEntryModel entry,
                                                 Long productId, SubOrderEntryModel subOrderEntryModel, Long comboId) {
        OrderBillRequest orderBillRequest = new OrderBillRequest();
        orderBillRequest.setOrderCode(order.getCode());
        orderBillRequest.setCompanyId(order.getCompanyId());
        orderBillRequest.setBillId(order.getBillId());
        orderBillRequest.setProductId(productId);
        orderBillRequest.setComboId(comboId);
        orderBillRequest.setOrderEntryId(entry.getId());
        orderBillRequest.setSaleOff(entry.isSaleOff());
        orderBillRequest.setOrderStatus(order.getOrderStatus());
        if (subOrderEntryModel != null) {
            orderBillRequest.setDiscount(subOrderEntryModel.getDiscountValue());
            orderBillRequest.setDiscountType(DiscountType.CASH.toString());
            orderBillRequest.setPrice(subOrderEntryModel.getPrice());
            orderBillRequest.setQuantity(subOrderEntryModel.getQuantity() == null ? 0 : subOrderEntryModel.getQuantity());
            orderBillRequest.setWarehouseId(order.getWarehouseId());
            orderBillRequest.setSubOrderEntryId(subOrderEntryModel.getId());
        }
        return orderBillRequest;
    }

    @Override
    public Long createBillForReturnOrder(BillRequest billRequest) {
        return billClient.createReceiptBillForOrder(billRequest);
    }

    @Override
    public ReturnOrderBillData getReturnOrderBill(Long billId, Long companyId, Long returnOrderId) {
        return billClient.getReturnOrderBill(billId, returnOrderId, companyId);
    }

    @Override
    public Long createReturnBillWithOrderOnline(OrderModel orderModel) {
        if(orderModel.getBillId() != null) {
            ErrorCodes err = ErrorCodes.CAN_NOT_CREATE_ONLINE_ORDER_HAS_RETURN_BILL;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        BillRequest billRequest = this.populateBillRequest(orderModel);
        return billClient.createReturnBillWithOrderOnline(billRequest);
    }

    @Override
    public void subtractShippingStockOf(OrderModel order) {
        billClient.subtractStockOfInventoryStatusBy(order.getBillId(), InventoryStatus.SHIPPING.code(), order.getCompanyId());
    }

    @Override
    public void addShippingStockOf(OrderModel order) {
        billClient.addStockOfInventoryStatusBy(order.getBillId(), InventoryStatus.SHIPPING.code(), order.getCompanyId());
    }

    @Override
    public void changeOrderStatusToOrderReturn(OrderModel orderModel) {
        billClient.changeOrderStatusToOrderReturn(orderModel.getCode(), orderModel.getBillId(), orderModel.getCompanyId());
    }

    private String getReasonExportBillOf(OrderModel orderModel) {

        if (OrderType.ONLINE.toString().equals(orderModel.getType())) {
            return ReceiptDeliveryReason.RETURN_BILL_ONLINE_REASON.code();
        }

        if (OrderType.WHOLESALE.toString().equals(orderModel.getType())) {
            return ReceiptDeliveryReason.RETURN_BILL_WHOLESALE_REASON.code();
        }

        return ReceiptDeliveryReason.RETURN_BILL_RETAIL_REASON.code();
    }

    @Override
    public BillRequest populateBillRequest(OrderModel order) {
        BillRequest request = new BillRequest();
        request.setCompanyId(order.getCompanyId());
        request.setDiscount(order.getDiscount());
        request.setDiscountType(order.getDiscountType());
        request.setWarehouseId(order.getWarehouseId());
        request.setTransactionDate(order.getCreatedTime());
        request.setOrderType(order.getType());
        request.setReasonCode(this.getReasonExportBillOf(order));
        request.setExchange(order.isExchange());
        List<AbstractOrderEntryModel> entries = order.getEntries();
        Set<BillDetailRequest> billDetails = new HashSet<>();
        for (AbstractOrderEntryModel entry : entries) {
            if (orderService.isComboEntry(entry)) {
                Set<SubOrderEntryModel> subOrderEntryModels = entry.getSubOrderEntries();
                List<SubOrderEntryModel> list = new ArrayList<>(subOrderEntryModels);
                populateBillDetailWithCombo(list, billDetails, entry);
                continue;
            }
            if (CollectionUtils.isNotEmpty(entry.getToppingOptionModels())) {
                List<BillDetailRequest> details = populateBillDetailRequestWithTopping(entry);
                billDetails.addAll(details);
            }

            BillDetailRequest billDetail = populateBillDetailRequest(entry);
            billDetails.add(billDetail);
        }
        request.setOrderCode(order.getCode());
        request.setBillDetails(billDetails);
        return request;
    }

    private List<BillDetailRequest> populateBillDetailRequestWithTopping(AbstractOrderEntryModel entry) {
        List<BillDetailRequest> billDetailRequests = new ArrayList<>();
        for (ToppingOptionModel toppingOptionModel : entry.getToppingOptionModels()) {
            int optionQty = CommonUtils.getIntValue(toppingOptionModel.getQuantity());
            for (ToppingItemModel toppingItemModel : toppingOptionModel.getToppingItemModels()) {
                BillDetailRequest billDetail = new BillDetailRequest();
                int itemQty = CommonUtils.getIntValue(toppingItemModel.getQuantity());
                int totalItemQuantity = CommonUtils.getIntValue(toppingItemModel.getQuantity()) * optionQty;
                double totalPriceWithoutPromotion = toppingItemModel.getBasePrice() * totalItemQuantity;
                double totalDiscount = CurrencyUtils.computeValue(toppingItemModel.getDiscount(), toppingItemModel.getDiscountType(), totalPriceWithoutPromotion);
                double discountOrderToItem = CommonUtils.getDoubleValue(toppingItemModel.getDiscountOrderToItem());
                billDetail.setDiscount(totalDiscount + discountOrderToItem);
                billDetail.setDiscountType(DiscountType.CASH.toString());
                billDetail.setProductId(toppingItemModel.getProductId());
                billDetail.setPrice(CommonUtils.getDoubleValue(toppingItemModel.getBasePrice()));
                billDetail.setQuantity(itemQty * optionQty);
                billDetail.setToppingOptionId(toppingOptionModel.getId());
                billDetail.setOrderEntryId(entry.getId());
                billDetailRequests.add(billDetail);
            }
        }
        return billDetailRequests;
    }

    private BillDetailRequest populateBillDetailRequest(AbstractOrderEntryModel entry) {
        BillDetailRequest billDetail = new BillDetailRequest();
        billDetail.setDiscount(calculationService.calculateFinalDiscountOfEntry(entry));
        billDetail.setDiscountType(DiscountType.CASH.toString());
        billDetail.setPrice(entry.getBasePrice());
        billDetail.setProductId(entry.getProductId());
        billDetail.setQuantity(entry.getQuantity() == null ? 0 : entry.getQuantity().intValue());
        billDetail.setOrderEntryId(entry.getId());
        billDetail.setSaleOff(entry.isSaleOff());
        return billDetail;
    }

    @Override
    public void updatePriceAndDiscountBillOf(OrderModel order) {
        BillRequest billRequest = this.populateBillRequest(order);
        billRequest.setId(order.getBillId());
        billClient.updateDiscountPriceInReturnBillWithOrder(billRequest);
    }

    private void populateBillDetailWithCombo(List<SubOrderEntryModel> subOrderEntryModels,
                                             Set<BillDetailRequest> billDetails, AbstractOrderEntryModel entry) {
        for (SubOrderEntryModel subOrderEntryModel : subOrderEntryModels) {
            BillDetailRequest billDetail = new BillDetailRequest();
            billDetail.setComboId(entry.getProductId());
            int comboQuantity = entry.getQuantity() == null ? 0 : entry.getQuantity().intValue();
            billDetail.setComboQuantity(comboQuantity);
            billDetail.setDiscount(subOrderEntryModel.getDiscountValue());
            billDetail.setDiscountType(DiscountType.CASH.toString());
            billDetail.setProductId(subOrderEntryModel.getProductId());
            billDetail.setPrice(subOrderEntryModel.getPrice());
            billDetail.setQuantity(subOrderEntryModel.getQuantity() == null ? 0 : subOrderEntryModel.getQuantity());
            billDetail.setSubOrderEntryId(subOrderEntryModel.getId());
            billDetail.setOrderEntryId(entry.getId());
            billDetails.add(billDetail);
        }
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setProductClient(ProductClient productClient) {
        this.productClient = productClient;
    }
}