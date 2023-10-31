package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RuleConditionDefinitionConverter extends
        AbstractPopulatingConverter<RuleConditionDefinitionModel, RuleConditionDefinitionData> {

    @Autowired
    private Populator<RuleConditionDefinitionModel, RuleConditionDefinitionData> ruleConditionDefinitionDataPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(RuleConditionDefinitionData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(ruleConditionDefinitionDataPopulator);
    }
}
