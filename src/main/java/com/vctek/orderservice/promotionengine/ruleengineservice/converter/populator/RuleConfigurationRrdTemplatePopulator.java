package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleConfigurationRRD;
import org.springframework.stereotype.Component;

@Component
public class RuleConfigurationRrdTemplatePopulator implements Populator<RuleConfigurationRRD, RuleConfigurationRRD> {

    @Override
    public void populate(RuleConfigurationRRD source, RuleConfigurationRRD target) {
        target.setRuleCode(source.getRuleCode());
        target.setCurrentRuns(source.getCurrentRuns());
        target.setMaxAllowedRuns(source.getMaxAllowedRuns());
        target.setRuleGroupCode(source.getRuleGroupCode());
    }
}
