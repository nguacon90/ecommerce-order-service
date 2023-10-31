package com.vctek.orderservice.converter.storefront;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CommercePromotionData;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommercePromotionDataConverter extends AbstractPopulatingConverter<PromotionSourceRuleModel, CommercePromotionData> {

    @Autowired
    private Populator<PromotionSourceRuleModel, CommercePromotionData> commercePromotionDataPopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(CommercePromotionData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(commercePromotionDataPopulator);
    }
}
