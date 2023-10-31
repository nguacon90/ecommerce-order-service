package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleGroupExecutionRRD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RuleGroupExecutionRrdTemplateConverter
        extends AbstractPopulatingConverter<RuleGroupExecutionRRD, RuleGroupExecutionRRD> {
    @Autowired
    private Populator<RuleGroupExecutionRRD, RuleGroupExecutionRRD> ruleGroupExecutionRrdTemplatePopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(RuleGroupExecutionRRD.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(ruleGroupExecutionRrdTemplatePopulator);
    }
}
