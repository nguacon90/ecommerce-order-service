package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.converter.Populator;
import com.vctek.kafka.data.loyalty.TransactionRequest;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.loyalty.LoyaltyTransactionOutStream;
import com.vctek.orderservice.kafka.producer.LoyaltyTransactionProducerService;
import com.vctek.orderservice.model.LoyaltyTransactionModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.LoyaltyService;
import com.vctek.orderservice.service.LoyaltyTransactionService;
import com.vctek.util.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class LoyaltyTransactionProducerServiceImpl implements LoyaltyTransactionProducerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoyaltyTransactionProducerServiceImpl.class);
    private KafkaProducerService kafkaProducerService;
    private LoyaltyTransactionOutStream loyaltyTransactionOutStream;
    private LoyaltyTransactionService loyaltyTransactionService;
    private LoyaltyService loyaltyService;
    private Populator<OrderModel, TransactionRequest> transactionRequestPopulator;

    public LoyaltyTransactionProducerServiceImpl(KafkaProducerService kafkaProducerService, LoyaltyTransactionOutStream loyaltyTransactionOutStream) {
        this.kafkaProducerService = kafkaProducerService;
        this.loyaltyTransactionOutStream = loyaltyTransactionOutStream;
    }

    @Override
    public void updateRevertTransactionKafka(OrderModel orderModel, double revertAmount) {
        if (orderModel == null) return;
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(orderModel.getCode(),
                Arrays.asList(TransactionType.REVERT.name()));
        if (loyaltyTransactionModel == null) {
            LOGGER.warn("Has not existed refer invoice for revert: orderId: {}", orderModel.getId());
            return;
        }
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequestPopulator.populate(orderModel, transactionRequest);
        transactionRequest.setAmount(revertAmount);
        transactionRequest.setInvoiceNumber(loyaltyTransactionModel.getInvoiceNumber());
        transactionRequest.setType(TransactionType.REVERT.name());


        KafkaMessage<TransactionRequest> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setContent(transactionRequest);
        kafkaProducerService.send(kafkaMessage, loyaltyTransactionOutStream.produceBill());
    }

    @Override
    public void sendLoyaltyTransactionKafka(OrderModel orderModel, double amount, TransactionType transactionType) {
        TransactionRequest transactionRequest;
        KafkaMessage<TransactionRequest> kafkaMessage = new KafkaMessage<>();
        switch (transactionType) {
            case REDEEM:
                transactionRequest = loyaltyService.populateRedeemKafkaForOnlineOrder(orderModel, amount);
                kafkaMessage.setType(KafkaMessageType.REDEEM);
                break;
            case REVERT:
                transactionRequest = loyaltyService.populateRevertKafkaForOnlineOrder(orderModel, amount);
                kafkaMessage.setType(KafkaMessageType.REVERT);
                break;
            case REFUND:
                transactionRequest = loyaltyService.populateRefundKafkaForOnlineOrder(orderModel, amount);
                kafkaMessage.setType(KafkaMessageType.REFUND);
                break;
            case CANCEL_PENDING_REDEEM:
                transactionRequest = loyaltyService.populateCancelRedeemKafkaForOnlineOrder(orderModel);
                kafkaMessage.setType(KafkaMessageType.REDEEM);
                break;
            case COMPLETE_PENDING_REDEEM:
                transactionRequest = loyaltyService.populateCompleteRedeemKafkaForOnlineOrder(orderModel);
                kafkaMessage.setType(KafkaMessageType.REDEEM);
                break;
            default:
                return;
        }
        if (transactionRequest == null) {
            return;
        }
        transactionRequest.setType(transactionType.toString());
        kafkaMessage.setContent(transactionRequest);
        kafkaProducerService.send(kafkaMessage, loyaltyTransactionOutStream.produceBill());
    }

    @Autowired
    public void setLoyaltyTransactionService(LoyaltyTransactionService loyaltyTransactionService) {
        this.loyaltyTransactionService = loyaltyTransactionService;
    }

    @Autowired
    public void setTransactionRequestPopulator(Populator<OrderModel, TransactionRequest> transactionRequestPopulator) {
        this.transactionRequestPopulator = transactionRequestPopulator;
    }

    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }
}
