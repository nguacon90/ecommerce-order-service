package com.vctek.orderservice.kafka.producer;

import com.vctek.orderservice.model.PaymentTransactionModel;

import java.util.List;

public interface MigratePaymentMethodProducer {

    void sendMigratePaymentMethodMessage(List<PaymentTransactionModel> paymentTransactionModelList);
}
