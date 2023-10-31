package com.vctek.orderservice.facade.impl;

import com.vctek.orderservice.dto.AvailablePointAmountData;
import com.vctek.orderservice.dto.request.AvailablePointAmountRequest;
import com.vctek.orderservice.facade.LoyaltyFacade;
import com.vctek.orderservice.service.LoyaltyService;
import com.vctek.orderservice.service.ProductRedeemRateUseService;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LoyaltyFacadeImpl implements LoyaltyFacade {
    private LoyaltyService loyaltyService;
    private ProductRedeemRateUseService productRedeemRateUseService;


    @Override
    public AvailablePointAmountData computeAvailablePointAmountOfOrder(AvailablePointAmountRequest request) {
        return loyaltyService.computeAvailablePointAmountOf(request);
    }

    @Override
    public List<Long> checkRedeemOfProduct(Long companyId, String products) {
        Map<Long, Boolean> redeemOfProductMap = productRedeemRateUseService.productCanRedeem(companyId, CommonUtils.parseLongStringByComma(products));
        List<Long> productList = new ArrayList<>();
        if (MapUtils.isEmpty(redeemOfProductMap)) {
            return productList;
        }
        redeemOfProductMap.forEach((productId, canRedeem) -> {
            if (canRedeem) productList.add(productId);
        });
        return productList;
    }

    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @Autowired
    public void setProductRedeemRateUseService(ProductRedeemRateUseService productRedeemRateUseService) {
        this.productRedeemRateUseService = productRedeemRateUseService;
    }
}
