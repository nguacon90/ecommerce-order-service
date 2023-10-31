package com.vctek.orderservice.service;

import com.vctek.kafka.data.ReturnOrderBillDTO;
import com.vctek.orderservice.dto.CommerceCartModification;
import com.vctek.orderservice.dto.request.LinkReturnOrderforbillRequest;
import com.vctek.orderservice.feignclient.dto.BillRequest;
import com.vctek.orderservice.feignclient.dto.OrderBillRequest;
import com.vctek.orderservice.feignclient.dto.ReturnOrderBillData;
import com.vctek.orderservice.feignclient.dto.UpdateReturnOrderBillRequest;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.OrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.util.OrderStatus;

import java.util.List;

public interface BillService {
    Long createBillForOrder(OrderModel orderModel);

    void revertBillForOrder(String orderCode, Long companyId);

    void updateProductInReturnBillWithOrder(OrderModel order, CommerceCartModification commerceCartModification);

    void deleteProductInReturnBillWithOrder(OrderModel order, CommerceCartModification commerceCartModification);

    void deleteProductOfComboInReturnBillWithOrder(OrderModel order, SubOrderEntryModel subOrderEntryModel);

    Long createBillForReturnOrder(BillRequest billRequest);

    ReturnOrderBillData getReturnOrderBill(Long billId, Long companyId, Long returnOrderId);

    Long createReturnBillWithOrderOnline(OrderModel orderModel);

    void subtractShippingStockOf(OrderModel order);

    void changeOrderStatusToOrderReturn(OrderModel orderModel);

    void addShippingStockOf(OrderModel order);

    BillRequest populateBillRequest(OrderModel order);

    void updatePriceAndDiscountBillOf(OrderModel order);

    void updateComboInReturnBillWithOrder(OrderModel order, AbstractOrderEntryModel entry);

    void updateOrDeleteToppingInReturnBillWithOrder(OrderModel order, List<OrderBillRequest> orderBillRequests);

    ReturnOrderBillDTO getBillWithReturnOrder(Long billId, Long companyId, Long returnOrderId);

    void addProductToReturnBill(OrderModel order, AbstractOrderEntryModel abstractOrderEntry);

    boolean shouldUpdateBillOf(OrderModel orderModel);

    void updateOriginOrderCode(UpdateReturnOrderBillRequest returnOrderBillRequest);

    void revertComboSaleQuantity(Long companyId, OrderEntryModel entryModel);

    Long linkReturnOrderforbill(LinkReturnOrderforbillRequest request);

    void cancelOnlineOrder(OrderModel order);

    void revertOnlineBillWhenError(OrderStatus oldStatus, OrderStatus newStatus, OrderModel order);
}
