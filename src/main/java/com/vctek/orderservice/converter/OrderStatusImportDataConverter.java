package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderStatusImportData;
import com.vctek.orderservice.model.OrderStatusImportModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusImportDataConverter extends AbstractPopulatingConverter<OrderStatusImportModel, OrderStatusImportData> {
    @Autowired
    private Populator<OrderStatusImportModel, OrderStatusImportData> populator;

    @Override
    public void setTargetClass() { setTargetClass(OrderStatusImportData.class); }

    @Override
    public void setPopulators() { setPopulators(populator); }
}
