package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleConfigurationRRD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component("ruleConfigurationRRDTemplateProvider")
public class DefaultRuleConfigurationRRDTemplateProvider implements RAOProvider {
    private Converter<RuleConfigurationRRD, RuleConfigurationRRD> ruleConfigurationRrdTemplateConverter;

    @Override
    public Set expandFactModel(Object factTemplate) {
        return factTemplate instanceof RuleConfigurationRRD ?
                Collections.singleton(this.createRAO((RuleConfigurationRRD)factTemplate)) : Collections.emptySet();
    }

    protected RuleConfigurationRRD createRAO(RuleConfigurationRRD template) {
        return this.getRuleConfigurationRrdTemplateConverter().convert(template);
    }

    protected Converter<RuleConfigurationRRD, RuleConfigurationRRD> getRuleConfigurationRrdTemplateConverter() {
        return this.ruleConfigurationRrdTemplateConverter;
    }

    @Autowired
    public void setRuleConfigurationRrdTemplateConverter(Converter<RuleConfigurationRRD, RuleConfigurationRRD> ruleConfigurationConverter) {
        this.ruleConfigurationRrdTemplateConverter = ruleConfigurationConverter;
    }
}