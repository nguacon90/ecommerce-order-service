package com.vctek.orderservice.converter.promotion;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.PromotionSourceRuleData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PromotionSourceRuleDataConverter extends AbstractPopulatingConverter<PromotionSourceRuleDTO, PromotionSourceRuleData> {

    @Autowired
    private Populator<PromotionSourceRuleDTO, PromotionSourceRuleData> promotionSourceRuleDataPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(PromotionSourceRuleData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(promotionSourceRuleDataPopulator);
    }
}
