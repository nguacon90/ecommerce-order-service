package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.model.DefaultLoyaltyRewardRateModel;

public interface DefaultLoyaltyRewardRateService {

    DefaultLoyaltyRewardRateModel findByCompanyId(Long companyId);

    DefaultLoyaltyRewardRateModel createOrUpdate(LoyaltyRewardRateDetailRequest loyaltyRewardRateDetailRequest);
}
