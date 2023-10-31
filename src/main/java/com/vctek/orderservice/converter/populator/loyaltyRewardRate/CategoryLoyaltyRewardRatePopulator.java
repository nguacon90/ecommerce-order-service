package com.vctek.orderservice.converter.populator.loyaltyRewardRate;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CategoryLoyaltyRewardRateData;
import com.vctek.orderservice.model.CategoryLoyaltyRewardRateModel;
import org.springframework.stereotype.Component;

@Component
public class CategoryLoyaltyRewardRatePopulator implements Populator<CategoryLoyaltyRewardRateModel, CategoryLoyaltyRewardRateData> {

    @Override
    public void populate(CategoryLoyaltyRewardRateModel source, CategoryLoyaltyRewardRateData target) {
        target.setRewardRate(source.getRewardRate());
        target.setCategoryId(source.getCategoryId());
    }
}
