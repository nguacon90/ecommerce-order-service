package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateRequest;
import com.vctek.orderservice.model.CategoryLoyaltyRewardRateModel;

import java.util.List;

public interface CategoryLoyaltyRewardRateService {
    List<CategoryLoyaltyRewardRateModel> findAllByCompanyId(Long companyId);

    List<CategoryLoyaltyRewardRateModel> createOrUpdate(LoyaltyRewardRateRequest loyaltyRewardRateRequest);

    void delete(LoyaltyRewardRateDetailRequest loyaltyRewardRateDetailRequest);
}
