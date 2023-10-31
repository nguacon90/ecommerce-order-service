package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.kafka.data.MigratePaymentMethodDto;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.MigratePaymentMethodKafkaOutStream;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.util.MoneySourceType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class MigratePaymentMethodProducerTest {

    private MigratePaymentMethodProducerImpl producer;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private MigratePaymentMethodKafkaOutStream migratePaymentMethodKafkaOutStream;
    private ArgumentCaptor<KafkaMessage<List<MigratePaymentMethodDto>>> captor = ArgumentCaptor.forClass(KafkaMessage.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        producer = new MigratePaymentMethodProducerImpl(kafkaProducerService, migratePaymentMethodKafkaOutStream);
    }

    @Test
    public void sendMigratePaymentMethodMessage() {
        List<PaymentTransactionModel> paymentTransactionModelList = new ArrayList<>();
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setMoneySourceType(MoneySourceType.CASH.name());
        paymentTransactionModel.setInvoiceId(1L);
        paymentTransactionModelList.add(paymentTransactionModel);

        PaymentTransactionModel model2 = new PaymentTransactionModel();
        model2.setMoneySourceType(MoneySourceType.BANK_ACCOUNT.name());
        model2.setInvoiceId(2L);
        model2.setPaymentMethodId(2L);
        paymentTransactionModelList.add(model2);

        producer.sendMigratePaymentMethodMessage(paymentTransactionModelList);
        verify(kafkaProducerService).send(captor.capture(), any());
        KafkaMessage<List<MigratePaymentMethodDto>> actualMessage = captor.getValue();
        List<MigratePaymentMethodDto> message = actualMessage.getContent();
        assertEquals(1L, message.get(0).getInvoiceId(), 0);
        assertEquals(2L, message.get(1).getInvoiceId(), 0);
        assertEquals(2L, message.get(1).getPaymentMethodId(), 0);
    }
}
