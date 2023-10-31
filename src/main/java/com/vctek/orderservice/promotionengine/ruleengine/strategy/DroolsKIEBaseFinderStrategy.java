package com.vctek.orderservice.promotionengine.ruleengine.strategy;


import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;

public interface DroolsKIEBaseFinderStrategy {
    DroolsKIEBaseModel getKIEBaseForKIEModule(DroolsKIEModuleModel rulesModule);
}
