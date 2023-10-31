package com.vctek.orderservice.promotionengine.ruleengine.init;

import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.init.impl.RulePublishingFuture;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;

import java.util.List;

public interface RulePublishingSpliterator {
    RulePublishingFuture publishRulesAsync(KieModuleModel kieModuleModel, ReleaseId containerReleaseId,
                                           List<String> ruleUuids, KIEModuleCacheBuilder cache);
}
