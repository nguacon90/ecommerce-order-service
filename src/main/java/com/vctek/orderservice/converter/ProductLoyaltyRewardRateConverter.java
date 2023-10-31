package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.ProductLoyaltyRewardRateData;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductLoyaltyRewardRateConverter extends AbstractPopulatingConverter<ProductLoyaltyRewardRateModel, ProductLoyaltyRewardRateData> {

    @Autowired
    private Populator<ProductLoyaltyRewardRateModel, ProductLoyaltyRewardRateData> productLoyaltyRewardRatePopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(ProductLoyaltyRewardRateData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(productLoyaltyRewardRatePopulator);
    }
}
