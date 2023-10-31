package com.vctek.orderservice.strategy.impl;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.service.ModelService;
import com.vctek.orderservice.strategy.CommerceRemoveEntriesStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultCommerceRemoveEntriesStrategy implements CommerceRemoveEntriesStrategy {
    private ModelService modelService;

    public DefaultCommerceRemoveEntriesStrategy(ModelService modelService) {
        this.modelService = modelService;
    }

    @Override
    public void removeAllEntries(CommerceAbstractOrderParameter parameter) {
        AbstractOrderModel abstractOrderModel = parameter.getOrder();
        List<AbstractOrderEntryModel> entries = abstractOrderModel.getEntries();
        abstractOrderModel.getEntries().removeAll(entries);
        modelService.save(abstractOrderModel);
        abstractOrderModel.setCalculated(false);
    }
}
