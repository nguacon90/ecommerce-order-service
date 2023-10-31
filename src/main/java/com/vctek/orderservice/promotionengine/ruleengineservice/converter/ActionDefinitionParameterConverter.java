package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionParameterModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActionDefinitionParameterConverter
        extends AbstractPopulatingConverter<RuleActionDefinitionParameterModel, RuleParameterDefinitionData> {

    @Autowired
    private Populator<RuleActionDefinitionParameterModel, RuleParameterDefinitionData> actionParameterDefinitionPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(RuleParameterDefinitionData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(actionParameterDefinitionPopulator);
    }
}
