package com.vctek.orderservice.converter.populator.loyaltyRewardRate;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.DefaultLoyaltyRewardRateData;
import com.vctek.orderservice.model.DefaultLoyaltyRewardRateModel;
import org.springframework.stereotype.Component;

@Component
public class DefaultLoyaltyRewardRatePopulator implements Populator<DefaultLoyaltyRewardRateModel, DefaultLoyaltyRewardRateData> {

    @Override
    public void populate(DefaultLoyaltyRewardRateModel source, DefaultLoyaltyRewardRateData target) {
        target.setRewardRate(source.getRewardRate());
    }
}
