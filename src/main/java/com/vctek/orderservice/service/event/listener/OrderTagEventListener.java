package com.vctek.orderservice.service.event.listener;

import com.vctek.orderservice.elasticsearch.service.OrderElasticSearchService;
import com.vctek.orderservice.kafka.producer.OrderProducerService;
import com.vctek.orderservice.service.event.OrderTagEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderTagEventListener {
    private OrderElasticSearchService orderElasticSearchService;
    private OrderProducerService orderProducerService;
    public OrderTagEventListener(OrderElasticSearchService orderElasticSearchService) {
        this.orderElasticSearchService = orderElasticSearchService;
    }

    @TransactionalEventListener
    public void handleAddTagToOrder(OrderTagEvent event) {
        orderElasticSearchService.indexTags(event.getOrderModel());
        orderProducerService.produceUpdateOrderTag(event.getOrderModel());
    }

    @Autowired
    public void setOrderProducerService(OrderProducerService orderProducerService) {
        this.orderProducerService = orderProducerService;
    }
}
