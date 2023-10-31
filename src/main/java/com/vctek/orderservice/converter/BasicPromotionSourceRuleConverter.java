package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("basicPromotionSourceRuleConverter")
public class BasicPromotionSourceRuleConverter extends AbstractPopulatingConverter<PromotionSourceRuleModel,
        PromotionSourceRuleDTO> {
    @Autowired
    private Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> basicPromotionSourceRulePopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(PromotionSourceRuleDTO.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(basicPromotionSourceRulePopulator);
    }
}
