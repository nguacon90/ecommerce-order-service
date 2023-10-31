package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleConfigurationRRD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component("ruleConfigurationRRDProvider")
public class RuleConfigurationRRDProvider implements RAOProvider {
    private Converter<AbstractRuleEngineRuleModel, RuleConfigurationRRD> ruleConfigurationRrdConverter;


    public Set expandFactModel(Object modelFact) {
        return modelFact instanceof AbstractRuleEngineRuleModel ?
                Collections.singleton(this.createRAO((AbstractRuleEngineRuleModel)modelFact)) : Collections.emptySet();
    }

    protected RuleConfigurationRRD createRAO(AbstractRuleEngineRuleModel source) {
        return this.getRuleConfigurationRrdConverter().convert(source);
    }

    protected Converter<AbstractRuleEngineRuleModel, RuleConfigurationRRD> getRuleConfigurationRrdConverter() {
        return this.ruleConfigurationRrdConverter;
    }

    @Autowired
    public void setRuleConfigurationRrdConverter(Converter<AbstractRuleEngineRuleModel, RuleConfigurationRRD> ruleConfigurationConverter) {
        this.ruleConfigurationRrdConverter = ruleConfigurationConverter;
    }
}
