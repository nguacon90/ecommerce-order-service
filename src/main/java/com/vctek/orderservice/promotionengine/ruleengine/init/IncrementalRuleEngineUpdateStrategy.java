package com.vctek.orderservice.promotionengine.ruleengine.init;

import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import org.kie.api.builder.ReleaseId;

import java.util.Collection;

public interface IncrementalRuleEngineUpdateStrategy {

    boolean shouldUpdateIncrementally(ReleaseId releaseId, String moduleName,
                                      Collection<DroolsRuleModel> rulesToAdd, Collection<DroolsRuleModel> rulesToRemove);
}
