package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.UpdatePaidAmountOrderInStream;
import com.vctek.migration.dto.PaidAmountOrderData;
import com.vctek.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CalculatePaidAmountOrderListener implements KafkaListener<List<PaidAmountOrderData >> {
    private OrderService orderService;

    @Override
    @StreamListener(UpdatePaidAmountOrderInStream.UPDATE_PAID_AMOUNT_ORDER)
    public void handleMessage(KafkaMessage<List<PaidAmountOrderData>> kafkaMessage) {
        if (KafkaMessageType.UPDATE_PAID_AMOUNT_ORDER.equals(kafkaMessage.getType())) {
            orderService.updatePaidAmountOrder(kafkaMessage.getContent());
        }
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
