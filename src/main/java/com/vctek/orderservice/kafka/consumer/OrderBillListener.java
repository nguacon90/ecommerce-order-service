package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.data.BillDto;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.BillInStream;
import com.vctek.kafka.stream.MigrateOrderBillLinkInStream;
import com.vctek.migration.dto.OrderBillLinkDTO;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.util.OrderType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderBillListener implements KafkaListener<BillDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderBillLinkListener.class);
    private OrderService orderService;

    @Override
    @StreamListener(BillInStream.BILL_IN)
    public void handleMessage(KafkaMessage<BillDto> kafkaMessage) {
        BillDto billDto = kafkaMessage.getContent();
        orderService.updateOrderBill(billDto);
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
