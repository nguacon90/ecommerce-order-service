package com.vctek.orderservice.promotionengine.promotionservice.strategy;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.FreeProductRAO;

import java.util.List;
import java.util.Set;

public interface EcommerceWebFreeGiftFilterStrategy {
    void filterNotSupportFreeGiftComboProduct(Set<PromotionSourceRuleModel> freeGiftPromotionSourceRules, List<FreeProductRAO> freeGiftActions, AbstractOrderModel order);

    AbstractRuleActionRAO filterFreeGiftAppliedAction(Set<PromotionSourceRuleModel> freeGiftPromotionSourceRules, List<FreeProductRAO> freeGiftActions, AbstractOrderModel order);
}
