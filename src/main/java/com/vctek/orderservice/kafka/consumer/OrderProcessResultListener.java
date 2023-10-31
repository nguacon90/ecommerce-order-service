package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.data.OrderProcessResultData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.OrderProcessResultInStream;
import com.vctek.orderservice.service.OrderStatusImportDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
public class OrderProcessResultListener implements KafkaListener<OrderProcessResultData>  {
    private OrderStatusImportDetailService service;

    @Override
    @StreamListener(OrderProcessResultInStream.ORDER_PROCESS_RESULT_IN)
    public void handleMessage(KafkaMessage<OrderProcessResultData> kafkaMessage) {
        service.updateStatusAndUnlockOrder(kafkaMessage.getContent());
    }

    @Autowired
    public void setService(OrderStatusImportDetailService service) {
        this.service = service;
    }
}
