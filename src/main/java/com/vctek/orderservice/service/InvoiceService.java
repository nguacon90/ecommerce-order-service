package com.vctek.orderservice.service;

import com.vctek.kafka.data.PaymentInvoiceData;
import com.vctek.orderservice.event.ReturnOrderEvent;
import com.vctek.orderservice.feignclient.dto.InvoiceData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;

import java.util.List;

public interface InvoiceService {
    void saveInvoices(OrderModel order, Long customerId);

    void createInvoiceForReturnOrder(ReturnOrderEvent returnOrderEvent);

    void unverifyInvoices(OrderModel order);

    void updateRefundInvoice(ReturnOrderModel returnOrderModel);

    void reverseVerifyInvoiceWithOnline(OrderModel order);

    List<InvoiceData> findAllOrderInvoices(Long companyId, String orderCode, Long returnOrderId, String orderType);

    void saveRedeemLoyaltyForOnlineOrChangeToRetailOrder(OrderModel order);

    void cancelLoyaltyRedeemInvoice(OrderModel order);

    void cancelLoyaltyRewardInvoice(OrderModel order);

    void mapInvoiceToPaymentTransaction(PaymentInvoiceData paymentInvoiceData);
}
