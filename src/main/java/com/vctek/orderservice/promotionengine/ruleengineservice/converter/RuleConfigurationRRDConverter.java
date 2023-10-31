package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleConfigurationRRD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RuleConfigurationRRDConverter extends
        AbstractPopulatingConverter<AbstractRuleEngineRuleModel, RuleConfigurationRRD> {
    @Autowired
    private Populator<AbstractRuleEngineRuleModel, RuleConfigurationRRD> ruleConfigurationRRDPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(RuleConfigurationRRD.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(ruleConfigurationRRDPopulator);
    }
}
