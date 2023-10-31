package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleConfigurationRRD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RuleConfigurationRrdTemplateConverter extends AbstractPopulatingConverter<RuleConfigurationRRD, RuleConfigurationRRD> {

    @Autowired
    private Populator<RuleConfigurationRRD, RuleConfigurationRRD> ruleConfigurationRrdTemplatePopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(RuleConfigurationRRD.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(ruleConfigurationRrdTemplatePopulator);
    }
}
