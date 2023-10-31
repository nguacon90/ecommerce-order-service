package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.elasticsearch.model.LoyaltyRewardRateSearchModel;
import com.vctek.orderservice.model.LoyaltyRewardRateModel;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("loyaltyRewardRateSearchConverter")
public class LoyaltyRewardRateElasticConverter extends AbstractPopulatingConverter<ProductLoyaltyRewardRateModel, LoyaltyRewardRateSearchModel> {

    @Autowired
    @Qualifier("loyaltyRewardRateSearchPopulator")
    private Populator<ProductLoyaltyRewardRateModel, LoyaltyRewardRateSearchModel> loyaltyRewardRateSearchPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(LoyaltyRewardRateSearchModel.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(loyaltyRewardRateSearchPopulator);
    }
}
