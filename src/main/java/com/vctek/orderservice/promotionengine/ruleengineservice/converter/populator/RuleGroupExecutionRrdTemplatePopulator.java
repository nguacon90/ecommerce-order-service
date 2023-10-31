package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleGroupExecutionRRD;
import org.springframework.stereotype.Component;

@Component
public class RuleGroupExecutionRrdTemplatePopulator implements Populator<RuleGroupExecutionRRD, RuleGroupExecutionRRD> {

    public void populate(RuleGroupExecutionRRD source, RuleGroupExecutionRRD target) {
        target.setCode(source.getCode());
        target.setExclusive(source.isExclusive());
    }
}
