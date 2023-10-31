package com.vctek.orderservice.promotionengine.ruleengine.init;

import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;

public interface ContentMatchRulesFilter {
    Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> apply(List<DroolsRuleModel> rules);

    Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> apply(List<DroolsRuleModel> rules, Long newModuleVersion);
}
