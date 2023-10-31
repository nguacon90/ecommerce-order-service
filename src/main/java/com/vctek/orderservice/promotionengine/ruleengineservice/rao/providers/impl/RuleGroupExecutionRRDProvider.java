package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleGroupExecutionRRD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component("ruleGroupExecutionRRDProvider")
public class RuleGroupExecutionRRDProvider implements RAOProvider {
    private Converter<AbstractRuleEngineRuleModel, RuleGroupExecutionRRD> ruleGroupExecutionRrdConverter;

    public Set expandFactModel(Object modelFact) {
        return modelFact instanceof AbstractRuleEngineRuleModel ?
                Collections.singleton(this.createRAO((AbstractRuleEngineRuleModel)modelFact)) : Collections.emptySet();
    }

    protected RuleGroupExecutionRRD createRAO(AbstractRuleEngineRuleModel source) {
        return this.getRuleGroupExecutionRrdConverter().convert(source);
    }

    protected Converter<AbstractRuleEngineRuleModel, RuleGroupExecutionRRD> getRuleGroupExecutionRrdConverter() {
        return this.ruleGroupExecutionRrdConverter;
    }

    @Autowired
    public void setRuleGroupExecutionRrdConverter(Converter<AbstractRuleEngineRuleModel, RuleGroupExecutionRRD> ruleGroupExecutionRrdConverter) {
        this.ruleGroupExecutionRrdConverter = ruleGroupExecutionRrdConverter;
    }
}
