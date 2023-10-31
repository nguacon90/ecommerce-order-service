package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionCategoryData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionCategoryModel;
import org.springframework.stereotype.Component;

@Component
public class ActionDefinitionCategoryPopulator
        implements Populator<RuleActionDefinitionCategoryModel, RuleActionDefinitionCategoryData> {

    @Override
    public void populate(RuleActionDefinitionCategoryModel source, RuleActionDefinitionCategoryData target) {
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setName(source.getName());
        target.setPriority(source.getPriority());
    }
}
