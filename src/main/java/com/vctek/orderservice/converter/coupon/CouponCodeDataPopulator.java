package com.vctek.orderservice.converter.coupon;

import com.vctek.converter.Populator;
import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.dto.CouponCodeData;
import com.vctek.orderservice.dto.CouponData;
import com.vctek.orderservice.service.CouponRedemptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component("couponCodeDataPopulator")
public class CouponCodeDataPopulator implements Populator<CouponModel, CouponData> {
    private CouponRedemptionService couponRedemptionService;

    @Override
    public void populate(CouponModel couponModel, CouponData couponData) {
        List<CouponCodeModel> couponCodes = couponModel.getCouponCodes().stream().collect(Collectors.toList());
        Collections.sort(couponCodes, Comparator.comparing(CouponCodeModel::getId));

        List<CouponCodeData> codeDataList = new ArrayList<>();
        CouponCodeData codeData;
        for(CouponCodeModel codeModel : couponCodes) {
            codeData = new CouponCodeData();
            codeData.setCode(codeModel.getCode());
            Long totalRedemption = couponRedemptionService.countBy(codeModel);
            if(totalRedemption != null && totalRedemption > 0) {
                couponData.setRedemptCoupon(true);
            }
            codeData.setTotalRedemption(totalRedemption == null ? 0 : totalRedemption.intValue());
            codeDataList.add(codeData);
        }

        couponData.setCodes(codeDataList);
    }

    @Autowired
    public void setCouponRedemptionService(CouponRedemptionService couponRedemptionService) {
        this.couponRedemptionService = couponRedemptionService;
    }
}
