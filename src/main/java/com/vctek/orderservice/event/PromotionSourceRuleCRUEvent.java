package com.vctek.orderservice.event;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;

public class PromotionSourceRuleCRUEvent {
    private PromotionSourceRuleModel sourceRuleModel;

    public PromotionSourceRuleCRUEvent(PromotionSourceRuleModel sourceRuleModel) {
        this.sourceRuleModel = sourceRuleModel;
    }

    public PromotionSourceRuleModel getSourceRuleModel() {
        return sourceRuleModel;
    }

    public void setSourceRuleModel(PromotionSourceRuleModel sourceRuleModel) {
        this.sourceRuleModel = sourceRuleModel;
    }
}
