package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.data.loyalty.TransactionData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.loyalty.LoyaltyTransactionResponseInStream;
import com.vctek.orderservice.model.LoyaltyTransactionModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.LoyaltyService;
import com.vctek.orderservice.service.LoyaltyTransactionService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.util.OrderType;
import com.vctek.util.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class LoyaltyTransactionResponseListener implements KafkaListener<TransactionData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoyaltyTransactionResponseListener.class);
    private LoyaltyService loyaltyService;
    private LoyaltyTransactionService loyaltyTransactionService;
    private OrderService orderService;

    @Override
    @StreamListener(LoyaltyTransactionResponseInStream.LOYALTY_TRANSACTION_RESPONSE_IN)
    public void handleMessage(@Payload KafkaMessage<TransactionData> kafkaMessage) {
        TransactionData transactionData = kafkaMessage.getContent();
        if (TransactionType.REDEEM.name().equals(transactionData.getType())) {
            loyaltyService.createLoyaltyTransaction(transactionData.getReferCode(), transactionData.getInvoiceNumber(),
                    TransactionType.REDEEM.toString(), transactionData.getConversionRate(), null);
            return;
        }
        if (TransactionType.REVERT.name().equals(transactionData.getType())) {
            revertOnlineOrderReward(transactionData);
            return;
        }
        if (TransactionType.REFUND.name().equals(transactionData.getType())) {
            refundOnlineOrder(transactionData);
        }
    }

    private void refundOnlineOrder(TransactionData transactionData) {
        if (validateOrderOnline(transactionData)) return;
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(transactionData.getReferCode(),
                Arrays.asList(TransactionType.REDEEM.name(), TransactionType.REFUND.name()));
        if (loyaltyTransactionModel == null) {
            LOGGER.warn("Not found award transaction: orderCode: {}, awardAmount: {}", transactionData.getReferCode(), transactionData.getRevertAmount());
            return;
        }
        loyaltyService.createLoyaltyTransaction(transactionData.getReferCode(), transactionData.getInvoiceNumber(),
                TransactionType.REFUND.toString(), transactionData.getConversionRate(), null);
    }

    private void revertOnlineOrderReward(TransactionData transactionData) {
        if (validateOrderOnline(transactionData)) return;

        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(transactionData.getReferCode(),
                Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()));
        if (loyaltyTransactionModel == null) {
            LOGGER.warn("Not found award transaction: orderCode: {}, awardAmount: {}", transactionData.getReferCode(), transactionData.getRevertAmount());
            return;
        }
        loyaltyService.createLoyaltyTransaction(transactionData.getReferCode(), transactionData.getInvoiceNumber(),
                TransactionType.REVERT.toString(), transactionData.getConversionRate(), null);
    }

    private boolean validateOrderOnline(TransactionData transactionData) {
        OrderModel model = orderService.findByCodeAndCompanyId(transactionData.getReferCode(), transactionData.getCompanyId());
        if (model == null || !OrderType.ONLINE.toString().equals(model.getType())) {
            LOGGER.warn("Cannot revert for order type ONLINE: orderCode: {}", transactionData.getReferCode());
            return true;
        }
        return false;
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
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
