package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.LoyaltyRedeemRateData;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.dto.request.RedeemRateRequest;

import java.util.List;

public interface RedeemRateFacade {
    List<Long> createOrUpdateProduct(RedeemRateRequest request);

    List<Long> createOrUpdateCategory(RedeemRateRequest request);

    LoyaltyRedeemRateData findBy(Long companyId);

    void deleteCategory(RedeemRateRequest request);

    void deleteProduct(LoyaltyRewardRateDetailRequest request);
}
