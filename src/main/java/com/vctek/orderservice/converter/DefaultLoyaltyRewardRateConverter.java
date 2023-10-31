package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.DefaultLoyaltyRewardRateData;
import com.vctek.orderservice.model.DefaultLoyaltyRewardRateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultLoyaltyRewardRateConverter extends AbstractPopulatingConverter<DefaultLoyaltyRewardRateModel, DefaultLoyaltyRewardRateData> {

    @Autowired
    private Populator<DefaultLoyaltyRewardRateModel, DefaultLoyaltyRewardRateData> defaultLoyaltyRewardRatePopulator;

    @Override
    public void setTargetClass() {
        setTargetClass(DefaultLoyaltyRewardRateData.class);
    }

    @Override
    public void setPopulators() {
        setPopulators(defaultLoyaltyRewardRatePopulator);
    }
}
