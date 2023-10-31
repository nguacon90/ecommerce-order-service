package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionParameterModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RuleConditionDefinitionParameterConverter
        extends AbstractPopulatingConverter<RuleConditionDefinitionParameterModel, RuleParameterDefinitionData> {

    @Autowired
    private Populator<RuleConditionDefinitionParameterModel, RuleParameterDefinitionData> ruleParameterDefinitionPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(RuleParameterDefinitionData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(ruleParameterDefinitionPopulator);
    }
}
