package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActionDefinitionConverter extends
        AbstractPopulatingConverter<RuleActionDefinitionModel, RuleActionDefinitionData> {

    @Autowired
    private Populator<RuleActionDefinitionModel, RuleActionDefinitionData> actionDefinitionDataPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(RuleActionDefinitionData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(actionDefinitionDataPopulator);
    }
}
