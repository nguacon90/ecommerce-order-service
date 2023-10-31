package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("toppingItemOrderParameterPopulator")
public class ToppingItemOrderParameterPopulator extends AbstractToppingItemParameterPopulator {
    private OrderService orderService;

    @Override
    protected AbstractOrderEntryModel getEntry(AbstractOrderModel abstractOrderModel, Long entryId) {
        return orderService.findEntryBy(entryId, (OrderModel) abstractOrderModel);
    }

    @Override
    protected AbstractOrderModel getOrderModel(String code, Long companyId) {
        return orderService.findByCodeAndCompanyIdAndDeleted(code, companyId, false);
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
