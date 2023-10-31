package com.vctek.orderservice.kafka.producer;

import com.vctek.orderservice.model.OrderModel;
import com.vctek.util.TransactionType;

public interface LoyaltyTransactionProducerService {

    void updateRevertTransactionKafka(OrderModel orderModel, double revertAmount);

    void sendLoyaltyTransactionKafka(OrderModel orderModel, double amount, TransactionType transactionType);
}
