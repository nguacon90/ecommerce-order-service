package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleGroupExecutionRRD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RuleGroupExecutionRRDConverter extends
        AbstractPopulatingConverter<AbstractRuleEngineRuleModel, RuleGroupExecutionRRD> {
    @Autowired
    private Populator<AbstractRuleEngineRuleModel, RuleGroupExecutionRRD> ruleGroupExecutionRRDPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(RuleGroupExecutionRRD.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(ruleGroupExecutionRRDPopulator);
    }
}
