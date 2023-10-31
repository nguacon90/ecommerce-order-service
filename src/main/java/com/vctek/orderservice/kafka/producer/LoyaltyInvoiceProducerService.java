package com.vctek.orderservice.kafka.producer;

import com.vctek.kafka.data.loyalty.LoyaltyInvoiceData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.util.InvoiceType;

public interface LoyaltyInvoiceProducerService {

    void createOrUpdateLoyaltyImbursementInvoice(OrderModel orderModel);

    void createOrUpdateLoyaltyReceiptInvoice(ReturnOrderModel returnOrderModel);

    void produceCancelLoyaltyRewardInvoice(LoyaltyInvoiceData loyaltyInvoiceData);

    void produceCancelLoyaltyRedeemInvoice(LoyaltyInvoiceData loyaltyInvoiceData);
}
