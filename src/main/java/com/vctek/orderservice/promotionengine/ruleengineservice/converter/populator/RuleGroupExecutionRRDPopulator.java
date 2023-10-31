package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.impl.DefaultDroolsRuleTargetCodeGenerator;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleGroupExecutionRRD;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class RuleGroupExecutionRRDPopulator implements Populator<AbstractRuleEngineRuleModel, RuleGroupExecutionRRD> {


    @Override
    public void populate(AbstractRuleEngineRuleModel source, RuleGroupExecutionRRD target) {
        String ruleGroupCode = StringUtils.isNotBlank(source.getRuleGroupCode()) ? source.getRuleGroupCode() :
                DefaultDroolsRuleTargetCodeGenerator.DEFAULT_RULE_GROUP_CODE;
        target.setCode(ruleGroupCode);
    }
}
