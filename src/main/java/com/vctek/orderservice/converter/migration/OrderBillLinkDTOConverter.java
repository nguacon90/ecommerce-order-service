package com.vctek.orderservice.converter.migration;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.migration.dto.OrderBillLinkDTO;
import com.vctek.orderservice.model.OrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("orderBillLinkDTOConverter")
public class OrderBillLinkDTOConverter extends AbstractPopulatingConverter<OrderModel, OrderBillLinkDTO> {

    @Autowired
    private Populator<OrderModel, OrderBillLinkDTO> orderBillLinkDTOPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(OrderBillLinkDTO.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(orderBillLinkDTOPopulator);
    }
}
