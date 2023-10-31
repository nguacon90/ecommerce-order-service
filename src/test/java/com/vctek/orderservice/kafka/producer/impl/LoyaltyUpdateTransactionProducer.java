package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.converter.Populator;
import com.vctek.kafka.data.loyalty.TransactionRequest;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.loyalty.LoyaltyTransactionOutStream;
import com.vctek.orderservice.kafka.producer.LoyaltyTransactionProducerService;
import com.vctek.orderservice.model.LoyaltyTransactionModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.LoyaltyTransactionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LoyaltyUpdateTransactionProducer {

    private LoyaltyTransactionProducerService service;

    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private LoyaltyTransactionOutStream loyaltyTransactionOutStream;
    @Mock
    private LoyaltyTransactionService loyaltyTransactionService;
    @Mock
    private Populator<OrderModel, TransactionRequest> transactionRequestPopulator;

    private OrderModel orderModel;

    private ArgumentCaptor<KafkaMessage<TransactionRequest>> captor = ArgumentCaptor.forClass(KafkaMessage.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new LoyaltyTransactionProducerServiceImpl(kafkaProducerService, loyaltyTransactionOutStream);
        ((LoyaltyTransactionProducerServiceImpl) service).setLoyaltyTransactionService(loyaltyTransactionService);
        ((LoyaltyTransactionProducerServiceImpl) service).setTransactionRequestPopulator(transactionRequestPopulator);

        orderModel = new OrderModel();
        orderModel.setId(10l);
        orderModel.setCompanyId(1l);
        orderModel.setCode("154HETPHONGTOA");
    }

    @Test
    public void updateRevertTransactionKafka_nullOrderModel() {
        service.updateRevertTransactionKafka(null, 0);
        verify(loyaltyTransactionService, times(0)).findLastByOrderCodeAndListType(anyString(), anyList());
    }

    @Test
    public void updateRevertTransactionKafka_notFoundTransaction() {
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(null);
        service.updateRevertTransactionKafka(orderModel, 0);
        verify(loyaltyTransactionService).findLastByOrderCodeAndListType(anyString(), anyList());
        verify(transactionRequestPopulator, times(0)).populate(any(OrderModel.class), any(TransactionRequest.class));
    }

    @Test
    public void updateRevertTransactionKafka() {
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setOrderCode(orderModel.getCode());
        loyaltyTransactionModel.setId(10l);
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(loyaltyTransactionModel);
        service.updateRevertTransactionKafka(orderModel, 2000);
        verify(loyaltyTransactionService).findLastByOrderCodeAndListType(anyString(), anyList());
        verify(transactionRequestPopulator).populate(any(OrderModel.class), any(TransactionRequest.class));
        verify(kafkaProducerService).send(captor.capture(), any());

        KafkaMessage<TransactionRequest> actualMessage = captor.getValue();
        TransactionRequest transactionRequest = actualMessage.getContent();
        assertEquals(2000, transactionRequest.getAmount(), 0);
    }
}
