package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.MigrateOrderBillLinkInStream;
import com.vctek.migration.dto.OrderBillLinkDTO;
import com.vctek.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderBillLinkListener implements KafkaListener<List<OrderBillLinkDTO>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderBillLinkListener.class);
    private OrderService orderService;

    @Override
    @StreamListener(MigrateOrderBillLinkInStream.MIGRATE_ORDER_BILL_LINK)
    public void handleMessage(KafkaMessage<List<OrderBillLinkDTO>> kafkaMessage) {
        if(KafkaMessageType.LINK_ORDER_TO_BILL.equals(kafkaMessage.getType())) {
            return;
        }

        orderService.linkBillToOrder(kafkaMessage.getContent());
        LOGGER.info("FINISHED LINK BILL TO ORDER");
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
