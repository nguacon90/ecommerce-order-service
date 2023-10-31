package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.kafka.data.OrderData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderSourceModel;
import org.springframework.stereotype.Component;

@Component("originOrderPopulator")
public class OriginOrderPopulator implements Populator<OrderModel, OrderData> {

    @Override
    public void populate(OrderModel source, OrderData target) {
        target.setCompanyId(source.getCompanyId());
        target.setOrderCode(source.getCode());
        target.setWarehouseId(source.getWarehouseId());
        target.setOrderStatus(source.getOrderStatus());
        target.setTotalPrice(source.getTotalPrice());
        target.setFinalPrice(source.getFinalPrice());
        target.setOrderType(source.getType());
        target.setVat(source.getTotalTax());
        target.setShippingCompanyId(source.getShippingCompanyId());
        OrderSourceModel orderSourceModel = source.getOrderSourceModel();
        if (orderSourceModel != null) {
            target.setOrderSourceId(orderSourceModel.getId());
            target.setOrderSourceName(orderSourceModel.getName());
        }
        target.setPriceType(source.getPriceType());
        target.setExchange(source.isExchange());
        target.setDeliveryDate(source.getDeliveryDate());
    }

}
