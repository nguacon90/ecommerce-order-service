package com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.task;

import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;
import com.vctek.orderservice.promotionengine.ruleengine.init.task.PostRulesModuleSwappingTask;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateRulesStatusPostRulesModuleSwappingTask implements PostRulesModuleSwappingTask {
    private PromotionSourceRuleService promotionSourceRuleService;

    @Override
    public boolean execute(RuleEngineActionResult result) {
        if (!result.isActionFailed()) {
            this.promotionSourceRuleService.updateAllActiveRuleStatus(result.getModuleName(), RuleStatus.PUBLISHED);
            this.promotionSourceRuleService.updateAllExpiredRuleToInActive(result.getModuleName());
            return true;
        }

        return false;
    }

    @Autowired
    public void setPromotionSourceRuleService(PromotionSourceRuleService promotionSourceRuleService) {
        this.promotionSourceRuleService = promotionSourceRuleService;
    }

}
