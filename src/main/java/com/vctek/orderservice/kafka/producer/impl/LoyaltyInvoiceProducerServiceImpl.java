package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.kafka.data.loyalty.LoyaltyInvoiceData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.loyalty.LoyaltyInvoiceOutStream;
import com.vctek.orderservice.kafka.producer.LoyaltyInvoiceProducerService;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.OrderHistoryService;
import com.vctek.util.OrderType;
import com.vctek.util.ReferType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class LoyaltyInvoiceProducerServiceImpl implements LoyaltyInvoiceProducerService {
    private KafkaProducerService kafkaProducerService;
    private LoyaltyInvoiceOutStream loyaltyInvoiceOutStream;
    private OrderHistoryService orderHistoryService;

    public LoyaltyInvoiceProducerServiceImpl(KafkaProducerService kafkaProducerService, LoyaltyInvoiceOutStream loyaltyInvoiceOutStream) {
        this.kafkaProducerService = kafkaProducerService;
        this.loyaltyInvoiceOutStream = loyaltyInvoiceOutStream;
    }

    @Override
    public void createOrUpdateLoyaltyImbursementInvoice(OrderModel orderModel) {
        Double totalRewardAmount = orderModel.getTotalRewardAmount();
        if (totalRewardAmount != null) {
            LoyaltyInvoiceData loyaltyInvoiceData = new LoyaltyInvoiceData();
            loyaltyInvoiceData.setCompanyId(orderModel.getCompanyId());
            loyaltyInvoiceData.setWarehouseId(orderModel.getWarehouseId());
            loyaltyInvoiceData.setCustomerId(orderModel.getCustomerId());
            loyaltyInvoiceData.setAmount(totalRewardAmount);
            populateTransactionDate(orderModel, loyaltyInvoiceData);
            loyaltyInvoiceData.setCurrentUserId(orderModel.getCreateByUser());
            loyaltyInvoiceData.setModifiedDate(orderModel.getModifiedTime());
            loyaltyInvoiceData.setModifiedBy(orderModel.getModifiedBy());
            loyaltyInvoiceData.setReferId(orderModel.getCode());
            loyaltyInvoiceData.setReferType(orderModel.getType());
            loyaltyInvoiceData.setOrderType(orderModel.getType());
            KafkaMessage<LoyaltyInvoiceData> kafkaMessage = new KafkaMessage<>();
            kafkaMessage.setType(KafkaMessageType.LOYALTY_IMBURSEMENT_INVOICE_FOR_ORDER);
            kafkaMessage.setContent(loyaltyInvoiceData);
            kafkaProducerService.send(kafkaMessage, loyaltyInvoiceOutStream.produce());
        }
    }

    private void populateTransactionDate(OrderModel orderModel, LoyaltyInvoiceData loyaltyInvoiceData) {
        if(OrderType.ONLINE.toString().equals(orderModel.getType()) && !orderModel.isExchange()) {
            Date completedDate = orderHistoryService.getLastCompletedDateOf(orderModel);
            loyaltyInvoiceData.setTransactionDate(completedDate);
            return;
        }

        loyaltyInvoiceData.setTransactionDate(orderModel.getCreatedTime());
    }

    @Override
    @Transactional
    public void createOrUpdateLoyaltyReceiptInvoice(ReturnOrderModel returnOrderModel) {
        Double revertAmount = returnOrderModel.getRevertAmount();
        OrderModel originOrder = returnOrderModel.getOriginOrder();
        if (revertAmount != null) {
            LoyaltyInvoiceData loyaltyInvoiceData = new LoyaltyInvoiceData();
            loyaltyInvoiceData.setCompanyId(originOrder.getCompanyId());
            loyaltyInvoiceData.setWarehouseId(originOrder.getWarehouseId());
            loyaltyInvoiceData.setCustomerId(originOrder.getCustomerId());
            loyaltyInvoiceData.setAmount(revertAmount);
            loyaltyInvoiceData.setTransactionDate(returnOrderModel.getModifiedTime());
            loyaltyInvoiceData.setCurrentUserId(returnOrderModel.getCreatedBy());
            loyaltyInvoiceData.setModifiedDate(returnOrderModel.getModifiedTime());
            loyaltyInvoiceData.setModifiedBy(returnOrderModel.getModifiedBy());
            loyaltyInvoiceData.setReferId(returnOrderModel.getId().toString());
            loyaltyInvoiceData.setReferType(ReferType.RETURN_ORDER.code());
            loyaltyInvoiceData.setOrderType(originOrder.getType());
            KafkaMessage<LoyaltyInvoiceData> kafkaMessage = new KafkaMessage<>();
            kafkaMessage.setType(KafkaMessageType.LOYALTY_RECEIPT_INVOICE_FOR_RETURN_ORDER);
            kafkaMessage.setContent(loyaltyInvoiceData);
            kafkaProducerService.send(kafkaMessage, loyaltyInvoiceOutStream.produce());
        }
    }


    @Override
    public void produceCancelLoyaltyRewardInvoice(LoyaltyInvoiceData loyaltyInvoiceData) {
        KafkaMessage<LoyaltyInvoiceData> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setType(KafkaMessageType.CANCEL_LOYALTY_IMBURSEMENT_INVOICE);
        kafkaMessage.setContent(loyaltyInvoiceData);
        kafkaProducerService.send(kafkaMessage, loyaltyInvoiceOutStream.produce());
    }

    @Override
    public void produceCancelLoyaltyRedeemInvoice(LoyaltyInvoiceData loyaltyInvoiceData) {
        KafkaMessage<LoyaltyInvoiceData> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setType(KafkaMessageType.CANCEL_LOYALTY_RECEIPT_INVOICE);
        kafkaMessage.setContent(loyaltyInvoiceData);
        kafkaProducerService.send(kafkaMessage, loyaltyInvoiceOutStream.produce());
    }

    @Autowired
    public void setOrderHistoryService(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }
}
