package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.kafka.data.MigratePaymentMethodDto;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.MigratePaymentMethodKafkaOutStream;
import com.vctek.orderservice.kafka.producer.MigratePaymentMethodProducer;
import com.vctek.orderservice.model.PaymentTransactionModel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MigratePaymentMethodProducerImpl implements MigratePaymentMethodProducer {
    private KafkaProducerService kafkaProducerService;
    private MigratePaymentMethodKafkaOutStream migratePaymentMethodKafkaOutStream;

    public MigratePaymentMethodProducerImpl(KafkaProducerService kafkaProducerService, MigratePaymentMethodKafkaOutStream migratePaymentMethodKafkaOutStream) {
        this.kafkaProducerService = kafkaProducerService;
        this.migratePaymentMethodKafkaOutStream = migratePaymentMethodKafkaOutStream;
    }

    @Override
    public void sendMigratePaymentMethodMessage(List<PaymentTransactionModel> paymentTransactionModelList) {
        List<MigratePaymentMethodDto> migratePaymentMethodDtos = new ArrayList<>();
        for (PaymentTransactionModel paymentTransactionModel : paymentTransactionModelList) {
            MigratePaymentMethodDto migratePaymentMethodDto = new MigratePaymentMethodDto();
            migratePaymentMethodDto.setInvoiceId(paymentTransactionModel.getInvoiceId());
            migratePaymentMethodDto.setMoneySourceType(paymentTransactionModel.getMoneySourceType());
            migratePaymentMethodDto.setPaymentMethodId(paymentTransactionModel.getPaymentMethodId());
            migratePaymentMethodDtos.add(migratePaymentMethodDto);
        }
        if (CollectionUtils.isNotEmpty(migratePaymentMethodDtos)) {
            KafkaMessage<List<MigratePaymentMethodDto>> kafkaMessage = new KafkaMessage<>();
            kafkaMessage.setContent(migratePaymentMethodDtos);
            kafkaProducerService.send(kafkaMessage, migratePaymentMethodKafkaOutStream.produceTopic());
        }
    }
}
