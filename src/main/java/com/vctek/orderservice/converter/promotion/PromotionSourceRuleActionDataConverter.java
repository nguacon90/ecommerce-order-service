package com.vctek.orderservice.converter.promotion;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.dto.promotion.ActionDTO;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PromotionSourceRuleActionDataConverter extends AbstractPopulatingConverter<ActionDTO, RuleActionData> {

    @Autowired
    private Populator<ActionDTO, RuleActionData> promotionSourceRuleActionDataPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(RuleActionData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(promotionSourceRuleActionDataPopulator);
    }
}
