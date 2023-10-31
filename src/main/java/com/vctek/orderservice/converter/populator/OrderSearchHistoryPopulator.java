package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.model.OrderModel;
import org.springframework.stereotype.Component;

@Component("orderSearchModelHistoryPopulator")
public class OrderSearchHistoryPopulator extends AbstractOrderSearchModelPopulator implements Populator<OrderModel, OrderSearchModel> {

    @Override
    public void populate(OrderModel source, OrderSearchModel target) {
        target.setOrderStatus(source.getOrderStatus());
        populateProduct(target, source);
        populateOrderHistory(target, source);
    }
}
