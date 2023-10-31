package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.kafka.data.order.OrderProcessData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.OrderProcessKafkaOutStream;
import com.vctek.orderservice.kafka.producer.OrderProcessProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessProducerServiceImpl implements OrderProcessProducerService {
    private KafkaProducerService kafkaProducerService;
    private OrderProcessKafkaOutStream outStream;

    @Autowired
    public OrderProcessProducerServiceImpl(KafkaProducerService kafkaProducerService,
                                           OrderProcessKafkaOutStream outStream) {
        this.kafkaProducerService = kafkaProducerService;
        this.outStream = outStream;
    }

    @Override
    public void sendOrderStatusImportKafka(OrderProcessData orderProcessData) {
        KafkaMessage<OrderProcessData> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setContent(orderProcessData);
        kafkaMessage.setType(KafkaMessageType.IMPORT_CHANGE_ORDER_STATUS);

        kafkaProducerService.send(kafkaMessage, outStream.produceOrderTopic());
    }
}
