package com.vctek.orderservice.promotionengine.ruleengine.init.impl;

import com.vctek.orderservice.promotionengine.ruleengine.init.IncrementalRuleEngineUpdateStrategy;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsRuleService;
import org.kie.api.builder.ReleaseId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class DefaultIncrementalRuleEngineUpdateStrategy implements IncrementalRuleEngineUpdateStrategy {
    private DroolsRuleService droolsRuleService;
    private int totalNumOfRulesThreshold;
    private float fractionOfRulesThreshold;

    public DefaultIncrementalRuleEngineUpdateStrategy(DroolsRuleService droolsRuleService) {
        this.droolsRuleService = droolsRuleService;
    }

    public boolean shouldUpdateIncrementally(ReleaseId releaseId, String moduleName, Collection<DroolsRuleModel> rulesToAdd, Collection<DroolsRuleModel> rulesToRemove) {
        long totalNumberOfDeployedRules = droolsRuleService.countDeployedEngineRulesForModule(moduleName);
        int numberOfRulesToUpdate = rulesToAdd.size() + rulesToRemove.size();
        boolean updateIncrementally = Math.sqrt((double) totalNumberOfDeployedRules * (double) totalNumberOfDeployedRules + (double) (numberOfRulesToUpdate * numberOfRulesToUpdate)) > (double) this.totalNumOfRulesThreshold;
        if (updateIncrementally && numberOfRulesToUpdate > 0) {
            updateIncrementally = totalNumberOfDeployedRules > 0L && (double) numberOfRulesToUpdate / (double) totalNumberOfDeployedRules < (double) this.fractionOfRulesThreshold;
        }

        return updateIncrementally;
    }

    protected int getTotalNumOfRulesThreshold() {
        return this.totalNumOfRulesThreshold;
    }

    @Value("${vctek.config.ruleengine.incremental.update.total.threshold:100}")
    public void setTotalNumOfRulesThreshold(int totalNumOfRulesThreshold) {
        this.totalNumOfRulesThreshold = totalNumOfRulesThreshold;
    }

    protected float getFractionOfRulesThreshold() {
        return this.fractionOfRulesThreshold;
    }

    @Value("${vctek.config.ruleengine.incremental.update.fraction.threshold:0.5}")
    public void setFractionOfRulesThreshold(float fractionOfRulesThreshold) {
        this.fractionOfRulesThreshold = fractionOfRulesThreshold;
    }
}