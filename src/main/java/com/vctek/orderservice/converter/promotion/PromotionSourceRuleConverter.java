package com.vctek.orderservice.converter.promotion;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.PromotionSourceRuleData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PromotionSourceRuleConverter extends AbstractPopulatingConverter<PromotionSourceRuleData, PromotionSourceRuleModel> {

    @Autowired
    private Populator<PromotionSourceRuleData, PromotionSourceRuleModel> promotionSourceRulePopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(PromotionSourceRuleModel.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(promotionSourceRulePopulator);
    }
}
