package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.AvailablePointAmountData;
import com.vctek.orderservice.dto.request.AvailablePointAmountRequest;

import java.util.List;

public interface LoyaltyFacade {
    AvailablePointAmountData computeAvailablePointAmountOfOrder(AvailablePointAmountRequest request);

    List<Long> checkRedeemOfProduct(Long companyId, String products);
}
