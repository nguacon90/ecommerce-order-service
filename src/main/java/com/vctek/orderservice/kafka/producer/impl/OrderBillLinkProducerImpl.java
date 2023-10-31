package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.converter.Converter;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.MigrateOrderBillLinkOutStream;
import com.vctek.migration.dto.OrderBillLinkDTO;
import com.vctek.orderservice.kafka.producer.OrderBillLinkProducer;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderBillLinkProducerImpl implements OrderBillLinkProducer {
    private Converter<OrderModel, OrderBillLinkDTO> orderBillLinkDTOConverter;
    private KafkaProducerService kafkaProducerService;
    private MigrateOrderBillLinkOutStream migrateOrderBillLinkOutStream;

    public OrderBillLinkProducerImpl(MigrateOrderBillLinkOutStream migrateOrderBillLinkOutStream) {
        this.migrateOrderBillLinkOutStream = migrateOrderBillLinkOutStream;
    }

    @Override
    public void produce(List<OrderModel> orders) {
        List<OrderModel> syncOrders = new ArrayList<>();
        for(OrderModel orderModel : orders) {
            String orderStatus = orderModel.getOrderStatus();
            String type = orderModel.getType();
            if(isIgnoreOrder(orderStatus, type)) {
                continue;
            }
            syncOrders.add(orderModel);
        }
        List<OrderBillLinkDTO> orderBillLinkDTOS = orderBillLinkDTOConverter.convertAll(syncOrders);
        KafkaMessage<List<OrderBillLinkDTO>> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setContent(orderBillLinkDTOS);
        kafkaMessage.setType(KafkaMessageType.LINK_ORDER_TO_BILL);
        kafkaProducerService.send(kafkaMessage, migrateOrderBillLinkOutStream.produceTopic());
    }

    private boolean isIgnoreOrder(String orderStatus, String type) {
        if(OrderType.ONLINE.toString().equals(type) &&
                !OrderStatus.CHANGE_TO_RETAIL.code().equals(orderStatus) &&
                !OrderStatus.COMPLETED.code().equals(orderStatus) &&
                !OrderStatus.SHIPPING.code().equals(orderStatus)) {
            return true;
        }

        if(OrderType.RETAIL.toString().equals(type) && OrderStatus.CHANGE_TO_RETAIL.code().equals(orderStatus)) {
            return true;
        }

        return false;
    }

    @Autowired
    public void setOrderBillLinkDTOConverter(Converter<OrderModel, OrderBillLinkDTO> orderBillLinkDTOConverter) {
        this.orderBillLinkDTOConverter = orderBillLinkDTOConverter;
    }

    @Autowired
    public void setKafkaProducerService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }
}
