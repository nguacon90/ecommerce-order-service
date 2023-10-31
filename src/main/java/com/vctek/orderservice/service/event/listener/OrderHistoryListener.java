package com.vctek.orderservice.service.event.listener;

import com.vctek.orderservice.kafka.producer.OrderProducerService;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.service.event.OrderHistoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderHistoryListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderHistoryListener.class);
    private OrderProducerService producerService;

    public OrderHistoryListener(OrderProducerService producerService) {
        this.producerService = producerService;
    }

    @EventListener
    public void handleOrderEventToIndex(OrderHistoryEvent orderHistoryEvent) {
        OrderHistoryModel orderHistoryModel = orderHistoryEvent.getOrderHistoryModel();
        LOGGER.debug("Change status orderHistory {}", orderHistoryModel);
        if (orderHistoryModel == null) {
            return;
        }
        try {
            producerService.sendChangeStatusKafka(orderHistoryModel);
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
