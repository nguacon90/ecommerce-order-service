package com.vctek.orderservice.promotionengine.promotionservice.service;


import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;

import java.util.List;
import java.util.Set;

public interface PromotionResultService {
    String getDescription(PromotionResultModel promoResult);

    List<PromotionResultModel> findAllByOrder(AbstractOrderModel order);

    Set<PromotionSourceRuleModel> findAllPromotionSourceRulesByOrder(AbstractOrderModel orderModel);

    Set<PromotionSourceRuleModel> findAllPromotionSourceRulesAppliedToOrder(AbstractOrderModel orderModel);

    Set<PromotionSourceRuleModel> findAllPromotionSourceRulesAppliedToOrderEntry(AbstractOrderEntryModel orderEntryModel);

    Long getTotalAppliedQuantityOf(AbstractOrderEntryModel orderEntryModel);
}
