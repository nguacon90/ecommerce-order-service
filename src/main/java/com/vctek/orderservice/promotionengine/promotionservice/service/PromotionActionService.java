package com.vctek.orderservice.promotionengine.promotionservice.service;


import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.DiscountRAO;

import java.util.List;

public interface PromotionActionService {

    PromotionResultModel createPromotionResult(AbstractRuleActionRAO action);

    DroolsRuleModel getRule(AbstractRuleActionRAO action);

    void createDiscountValue(DiscountRAO discountRao, String guid, AbstractOrderModel order);

    void recalculateTotals(AbstractOrderModel order);

    List<ItemModel> removeDiscountValue(String guid, AbstractOrderModel order);

    AbstractOrderEntryModel getOrderEntry(AbstractRuleActionRAO action);

    void createDiscountValue(DiscountRAO discountRAO, String code, AbstractOrderEntryModel entry);

    void removeDiscountValueBy(List<String> actionGuids, AbstractOrderModel order);

    AbstractOrderModel getOrder(AbstractRuleActionRAO action);
}
