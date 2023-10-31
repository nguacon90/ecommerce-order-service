package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionParameterModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import org.springframework.stereotype.Component;

@Component
public class RuleParameterDefinitionPopulator extends AbstractDefinitionPopulator
        implements Populator<RuleConditionDefinitionParameterModel, RuleParameterDefinitionData> {

    public RuleParameterDefinitionPopulator(RuleParameterValueConverter ruleParameterValueConverter) {
        super(ruleParameterValueConverter);
    }

    @Override
    public void populate(RuleConditionDefinitionParameterModel source, RuleParameterDefinitionData target) {
        super.populateParameters(source, target);
    }
}
