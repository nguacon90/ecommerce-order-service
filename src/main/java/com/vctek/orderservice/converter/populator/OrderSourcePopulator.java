package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderSourceData;
import com.vctek.orderservice.model.OrderSourceModel;
import org.springframework.stereotype.Component;

@Component
public class OrderSourcePopulator implements Populator<OrderSourceModel, OrderSourceData> {

    @Override
    public void populate(OrderSourceModel source, OrderSourceData target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setCreatedTime(source.getCreatedTime());
        target.setModifiedTime(source.getModifiedTime());
        target.setCreatedBy(source.getCreatedBy());
        target.setCompanyId(source.getCompanyId());
        target.setDescription(source.getDescription());
        target.setTransactionName(source.getTransactionName());
        target.setOrder(source.getOrder());
    }
}
