package com.vctek.orderservice.converter.coupon;

import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.dto.CouponCodeData;
import com.vctek.orderservice.dto.request.CouponRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.service.CouponRedemptionService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CouponModelPopulator implements Populator<CouponRequest, CouponModel> {
    private CouponRedemptionService couponRedemptionService;

    @Override
    public void populate(CouponRequest couponRequest, CouponModel couponModel) {
        couponModel.setCompanyId(couponRequest.getCompanyId());
        couponModel.setActive(true);
        couponModel.setLength(couponRequest.getLength());
        couponModel.setPrefix(couponRequest.getPrefix());
        couponModel.setSuffix(couponRequest.getSuffix());
        couponModel.setName(couponRequest.getName());
        couponModel.setQuantity(couponRequest.getQuantity());
        couponModel.setMaxTotalRedemption(couponRequest.getMaxTotalRedemption());
        if(couponRequest.getMaxRedemptionPerCustomer() != null) {
            couponModel.setMaxRedemptionPerCustomer(couponRequest.getMaxRedemptionPerCustomer());
        }
        if(couponRequest.getAllowRedemptionMultipleCoupon() != null) {
            couponModel.setAllowRedemptionMultipleCoupon(couponRequest.getAllowRedemptionMultipleCoupon());
        }
        populateCouponCodes(couponRequest, couponModel);
    }

    protected void populateCouponCodes(CouponRequest couponRequest, CouponModel couponModel) {
        Set<CouponCodeModel> couponCodes = couponModel.getCouponCodes();
        List<CouponCodeData> codes = couponRequest.getCodes();
        Set<String> updatedCouponCodes = codes.stream().map(CouponCodeData::getCode).collect(Collectors.toSet());

        if(CollectionUtils.isNotEmpty(couponCodes)) {
            for(CouponCodeModel codeModel : couponCodes) {
                Long totalRedemption = couponRedemptionService.countBy(codeModel);
                if(totalRedemption != null && totalRedemption > 0
                        && !updatedCouponCodes.contains(codeModel.getCode())) {
                    ErrorCodes err = ErrorCodes.CANNOT_UPDATE_REDEMPTION_COUPON_CODE;
                    throw new ServiceException(err.code(), err.message(), err.httpStatus());
                } else {
                    updatedCouponCodes.remove(codeModel.getCode());
                }

            }
        }

        Set<CouponCodeModel> couponCodeModels = new HashSet<>();
        CouponCodeModel couponCodeModel;
        for(String code : updatedCouponCodes) {
            couponCodeModel = new CouponCodeModel();
            couponCodeModel.setCode(code);
            couponCodeModel.setCoupon(couponModel);
            couponCodeModels.add(couponCodeModel);
        }

        couponModel.getCouponCodes().addAll(couponCodeModels);
    }

    @Autowired
    public void setCouponRedemptionService(CouponRedemptionService couponRedemptionService) {
        this.couponRedemptionService = couponRedemptionService;
    }
}
