package com.vctek.orderservice.service.event.listener;

import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.service.OrderElasticSearchService;
import com.vctek.orderservice.kafka.producer.OrderProducerService;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.LoyaltyService;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.orderservice.util.EventType;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import com.vctek.util.PriceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Component
public class OrderIndexListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderIndexListener.class);
    private OrderElasticSearchService orderElasticSearchService;
    private OrderProducerService producerService;
    private LoyaltyService loyaltyService;

    public OrderIndexListener(OrderElasticSearchService orderElasticSearchService) {
        this.orderElasticSearchService = orderElasticSearchService;
    }

    @TransactionalEventListener
    public void handleOrderEventToIndex(OrderEvent orderEvent) {
        OrderModel orderModel = orderEvent.getOrderModel();
        if (orderModel == null) {
            return;
        }

        if (EventType.DELETE.equals(orderEvent.getEventType())) {
            Optional<OrderSearchModel> optional = orderElasticSearchService.findById(orderModel.getCode());
            if (optional.isPresent()) {
                OrderSearchModel orderSearchModel = optional.get();
                orderSearchModel.setDeleted(true);
                orderElasticSearchService.save(orderSearchModel);
                producerService.sendOrderKafka(orderEvent);
            }

            return;
        }

        if (EventType.UPDATE_RETURN_ORDER.equals(orderEvent.getEventType())) {
            orderElasticSearchService.indexReturnOrderIds(orderModel);
            return;
        }

        try {
            if(OrderType.ONLINE.toString().equals(orderModel.getType())
                    && OrderStatus.COMPLETED.code().equals(orderModel.getOrderStatus())
                    && PriceType.RETAIL_PRICE.name().equals(orderModel.getPriceType())
                    && (EventType.CHANGE_COMPLETED_ONLINE.equals(orderEvent.getEventType()) || EventType.IMPORT_CHANGE_STATUS_ONLINE.equals(orderEvent.getEventType())
            )) {
                loyaltyService.reward(orderModel);
            } else if (!OrderType.ONLINE.name().equals(orderModel.getType())
                    && (EventType.CREATE.equals(orderEvent.getEventType()) || EventType.IMPORT_CHANGE_STATUS_ONLINE.equals(orderEvent.getEventType()))) {
                loyaltyService.reward(orderModel);
            }

        } catch (RuntimeException e) {
            LOGGER.error("ERROR REWARD: " + e.getMessage(), e);
        }
        orderElasticSearchService.indexOrder(orderModel);
        producerService.sendOrderKafka(orderEvent);
    }

    @Autowired
    public void setProducerService(OrderProducerService producerService) {
        this.producerService = producerService;
    }

    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }
}
