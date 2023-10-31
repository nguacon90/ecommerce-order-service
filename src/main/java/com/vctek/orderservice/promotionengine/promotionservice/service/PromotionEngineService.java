package com.vctek.orderservice.promotionengine.promotionservice.service;


import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionGroupModel;
import com.vctek.orderservice.promotionengine.promotionservice.result.PromotionOrderResults;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationResult;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;

import java.util.Collection;
import java.util.Date;

public interface PromotionEngineService {
    PromotionOrderResults updatePromotions(Collection<PromotionGroupModel> promotionGroups, AbstractOrderModel order);

    RuleEvaluationResult evaluate(AbstractOrderModel order, Collection<PromotionGroupModel> groups, Date date);

    RuleEngineResultRAO doEvaluateCartTemp(CartModel cartModel);
}
