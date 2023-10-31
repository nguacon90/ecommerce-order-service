package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CategoryLoyaltyRewardRateData;
import com.vctek.orderservice.model.CategoryLoyaltyRewardRateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CategoryLoyaltyRewardRateConverter extends AbstractPopulatingConverter<CategoryLoyaltyRewardRateModel, CategoryLoyaltyRewardRateData> {

    @Autowired
    private Populator<CategoryLoyaltyRewardRateModel, CategoryLoyaltyRewardRateData> categoryLoyaltyRewardRatePopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(CategoryLoyaltyRewardRateData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(categoryLoyaltyRewardRatePopulator);
    }
}
