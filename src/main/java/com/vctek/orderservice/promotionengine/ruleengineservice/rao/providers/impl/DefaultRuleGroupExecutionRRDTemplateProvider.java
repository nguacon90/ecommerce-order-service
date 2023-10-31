package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleGroupExecutionRRD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component("ruleGroupExecutionRRDTemplateProvider")
public class DefaultRuleGroupExecutionRRDTemplateProvider implements RAOProvider {
    private Converter<RuleGroupExecutionRRD, RuleGroupExecutionRRD> ruleGroupExecutionRrdTemplateConverter;

    @Override
    public Set expandFactModel(Object factTemplate) {
        return factTemplate instanceof RuleGroupExecutionRRD ? Collections.singleton(this.createRAO((RuleGroupExecutionRRD)factTemplate)) : Collections.emptySet();
    }

    protected RuleGroupExecutionRRD createRAO(RuleGroupExecutionRRD factTemplate) {
        return this.getRuleGroupExecutionRrdTemplateConverter().convert(factTemplate);
    }

    protected Converter<RuleGroupExecutionRRD, RuleGroupExecutionRRD> getRuleGroupExecutionRrdTemplateConverter() {
        return this.ruleGroupExecutionRrdTemplateConverter;
    }

    @Autowired
    public void setRuleGroupExecutionRrdTemplateConverter(Converter<RuleGroupExecutionRRD, RuleGroupExecutionRRD> ruleGroupExecutionRrdTemplateConverter) {
        this.ruleGroupExecutionRrdTemplateConverter = ruleGroupExecutionRrdTemplateConverter;
    }
}
