package com.vctek.orderservice.event;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;

public class PublishPromotionSourceRuleEvent {
    private PromotionSourceRuleModel promotionSourceRuleModel;
    private DroolsRuleModel droolsRuleModel;
    private DroolsKIEModuleModel kieModule;

    public PublishPromotionSourceRuleEvent(PromotionSourceRuleModel sourceRuleModel, DroolsRuleModel droolsRuleModel,
                                           DroolsKIEModuleModel kieModule) {
        this.promotionSourceRuleModel = sourceRuleModel;
        this.droolsRuleModel = droolsRuleModel;
        this.kieModule = kieModule;
    }

    public PromotionSourceRuleModel getPromotionSourceRuleModel() {
        return promotionSourceRuleModel;
    }

    public DroolsRuleModel getDroolsRuleModel() {
        return droolsRuleModel;
    }

    public DroolsKIEModuleModel getKieModule() {
        return kieModule;
    }
}
