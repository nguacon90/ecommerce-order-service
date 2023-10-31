package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.UpdatePaidAmountOrderOutStream;
import com.vctek.migration.dto.PaidAmountOrderData;
import com.vctek.orderservice.kafka.producer.CalculatePaidAmountOrderProducer;
import com.vctek.orderservice.model.OrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CalculatePaidAmountOrderProducerImpl implements CalculatePaidAmountOrderProducer {
    private KafkaProducerService kafkaProducerService;
    private UpdatePaidAmountOrderOutStream updatePaidAmountOrderOutStream;

    public CalculatePaidAmountOrderProducerImpl(UpdatePaidAmountOrderOutStream updatePaidAmountOrderOutStream) {
        this.updatePaidAmountOrderOutStream = updatePaidAmountOrderOutStream;
    }


    @Override
    public void produce(List<OrderModel> orders) {
        List<PaidAmountOrderData> amountOrderData = new ArrayList<>();
        for (OrderModel orderModel : orders) {
            PaidAmountOrderData data = new PaidAmountOrderData();
            data.setCompanyId(orderModel.getCompanyId());
            data.setOrderCode(orderModel.getCode());
            amountOrderData.add(data);
        }

        KafkaMessage<List<PaidAmountOrderData>> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setContent(amountOrderData);
        kafkaMessage.setType(KafkaMessageType.CALCULATE_PAID_AMOUNT_ORDER);
        kafkaProducerService.send(kafkaMessage, updatePaidAmountOrderOutStream.producePaidAmountTopic());
    }

    @Autowired
    public void setKafkaProducerService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }
}
