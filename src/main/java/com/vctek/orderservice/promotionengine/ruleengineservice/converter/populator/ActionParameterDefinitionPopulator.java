package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionParameterModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import org.springframework.stereotype.Component;

@Component
public class ActionParameterDefinitionPopulator extends AbstractDefinitionPopulator
        implements Populator<RuleActionDefinitionParameterModel, RuleParameterDefinitionData> {

    public ActionParameterDefinitionPopulator(RuleParameterValueConverter ruleParameterValueConverter) {
        super(ruleParameterValueConverter);
    }

    @Override
    public void populate(RuleActionDefinitionParameterModel source, RuleParameterDefinitionData target) {
        super.populateParameters(source, target);
    }
}
