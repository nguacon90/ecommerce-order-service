package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.data.order.OrderProcessData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.OrderProcessInStream;
import com.vctek.orderservice.service.OrderStatusImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
public class OrderProcessListener implements KafkaListener<OrderProcessData>  {
    private OrderStatusImportService service;

    @Override
    @StreamListener(OrderProcessInStream.ORDER_PROCESS_IN)
    public void handleMessage(KafkaMessage<OrderProcessData> kafkaMessage) {

        if (!KafkaMessageType.IMPORT_CHANGE_ORDER_STATUS.equals(kafkaMessage.getType())) return;
        service.changeStatusMultipleOrder(kafkaMessage.getContent());
    }

    @Autowired
    public void setService(OrderStatusImportService service) {
        this.service = service;
    }
}
