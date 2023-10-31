package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.impl.DefaultDroolsRuleTargetCodeGenerator;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleConfigurationRRD;
import com.vctek.orderservice.promotionengine.util.RuleEngineServiceConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class RuleConfigurationRRDPopulator implements Populator<AbstractRuleEngineRuleModel, RuleConfigurationRRD> {

    @Override
    public void populate(AbstractRuleEngineRuleModel source, RuleConfigurationRRD target) {
        target.setRuleCode(source.getCode());
        target.setCurrentRuns(0);
        if (source.getMaxAllowedRuns() != null && source.getMaxAllowedRuns() != 0) {
            target.setMaxAllowedRuns(source.getMaxAllowedRuns());
        } else {
            target.setMaxAllowedRuns(RuleEngineServiceConstants.DEFAULT_MAX_ALLOWED_RUNS);
        }

        if (StringUtils.isNotBlank(source.getRuleGroupCode())) {
            target.setRuleGroupCode(source.getRuleGroupCode());
        } else {
            target.setRuleGroupCode(DefaultDroolsRuleTargetCodeGenerator.DEFAULT_RULE_GROUP_CODE);
        }
    }
}
