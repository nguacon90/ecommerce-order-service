package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionCategoryData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionCategoryModel;
import org.springframework.stereotype.Component;

@Component
public class RuleConditionDefinitionCategoryPopulator
        implements Populator<RuleConditionDefinitionCategoryModel, RuleConditionDefinitionCategoryData> {

    @Override
    public void populate(RuleConditionDefinitionCategoryModel source, RuleConditionDefinitionCategoryData target) {
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setName(source.getName());
        target.setPriority(source.getPriority());
    }
}
