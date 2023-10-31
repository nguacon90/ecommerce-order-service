package com.vctek.orderservice.converter.populator.loyaltyRewardRate;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.ProductLoyaltyRewardRateData;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import org.springframework.stereotype.Component;

@Component
public class ProductLoyaltyRewardRatePopulator implements Populator<ProductLoyaltyRewardRateModel, ProductLoyaltyRewardRateData> {

    @Override
    public void populate(ProductLoyaltyRewardRateModel source, ProductLoyaltyRewardRateData target) {
        target.setRewardRate(source.getRewardRate());
        target.setProductId(source.getProductId());
    }
}
