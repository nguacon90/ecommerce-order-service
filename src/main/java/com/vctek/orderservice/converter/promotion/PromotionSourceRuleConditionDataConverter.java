package com.vctek.orderservice.converter.promotion;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.dto.promotion.ConditionDTO;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PromotionSourceRuleConditionDataConverter extends AbstractPopulatingConverter<ConditionDTO, RuleConditionData> {

    @Autowired
    private Populator<ConditionDTO, RuleConditionData> promotionSourceRuleConditionDataPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(RuleConditionData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(promotionSourceRuleConditionDataPopulator);
    }
}
