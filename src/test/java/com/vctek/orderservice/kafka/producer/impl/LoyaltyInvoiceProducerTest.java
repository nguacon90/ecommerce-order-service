package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.kafka.data.loyalty.LoyaltyInvoiceData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.loyalty.LoyaltyInvoiceOutStream;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class LoyaltyInvoiceProducerTest {
    private LoyaltyInvoiceProducerServiceImpl loyaltyInvoiceProducerService;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private LoyaltyInvoiceOutStream loyaltyInvoiceOutStream;
    @Mock
    private LoyaltyInvoiceData dataMock;
    private ArgumentCaptor<KafkaMessage<LoyaltyInvoiceData>> captor;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        captor = ArgumentCaptor.forClass(KafkaMessage.class);
        loyaltyInvoiceProducerService = new LoyaltyInvoiceProducerServiceImpl(kafkaProducerService, loyaltyInvoiceOutStream);
    }

    @Test
    public void createOrUpdateLoyaltyInvoice() {
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setPaymentMethodId(1L);
        paymentTransactionModel.setMoneySourceId(2L);

        OrderModel orderModel = new OrderModel();
        orderModel.setId(11L);
        orderModel.setTotalRewardAmount(10000d);
        orderModel.setRewardPoint(10d);
        orderModel.setPaymentTransactions(Collections.singleton(paymentTransactionModel));
        loyaltyInvoiceProducerService.createOrUpdateLoyaltyImbursementInvoice(orderModel);
        verify(kafkaProducerService).send(any(), any());
    }

    @Test
    public void createOrUpdateLoyaltyReceiptInvoice() {

        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setPaymentMethodId(1L);
        paymentTransactionModel.setMoneySourceId(2L);

        OrderModel orderModel = new OrderModel();
        orderModel.setId(11L);
        orderModel.setTotalRewardAmount(10000d);
        orderModel.setPaymentTransactions(Collections.singleton(paymentTransactionModel));

        ReturnOrderModel returnOrderModel = new ReturnOrderModel();
        returnOrderModel.setId(1L);
        returnOrderModel.setRevertAmount(10000d);
        returnOrderModel.setOriginOrder(orderModel);

        loyaltyInvoiceProducerService.createOrUpdateLoyaltyReceiptInvoice(returnOrderModel);
        verify(kafkaProducerService).send(any(), any());
    }

    @Test
    public void produceCancelLoyaltyRewardInvoice() {
        loyaltyInvoiceProducerService.produceCancelLoyaltyRewardInvoice(dataMock);
        verify(kafkaProducerService).send(captor.capture(), any());
        KafkaMessage<LoyaltyInvoiceData> message = captor.getValue();
        assertEquals(KafkaMessageType.CANCEL_LOYALTY_IMBURSEMENT_INVOICE, message.getType());
    }

    @Test
    public void produceCancelLoyaltyRedeemInvoice() {
        loyaltyInvoiceProducerService.produceCancelLoyaltyRedeemInvoice(dataMock);
        verify(kafkaProducerService).send(captor.capture(), any());
        KafkaMessage<LoyaltyInvoiceData> message = captor.getValue();
        assertEquals(KafkaMessageType.CANCEL_LOYALTY_RECEIPT_INVOICE, message.getType());
    }
}
