package com.vctek.orderservice.event.listener;

import com.vctek.orderservice.event.OrderStatusImportEvent;
import com.vctek.orderservice.model.OrderStatusImportModel;
import com.vctek.orderservice.service.OrderStatusImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusImportListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStatusImportListener.class);
    private OrderStatusImportService orderStatusImportService;

    @EventListener
    public void handleOrderEventToIndexExchangeOrder(OrderStatusImportEvent event) {
        OrderStatusImportModel model = event.getOrderStatusImportModel();
        if(model == null) {
            return;
        }
        try {
            orderStatusImportService.handleSendKafkaChangeOrderStatus(model);
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Autowired
    public void setOrderStatusImportService(OrderStatusImportService orderStatusImportService) {
        this.orderStatusImportService = orderStatusImportService;
    }
}
