package com.vctek.orderservice.converter.coupon;

import com.vctek.converter.Populator;
import com.vctek.dto.promotion.CouponCodeDTO;
import com.vctek.dto.promotion.CouponDTO;
import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.service.CouponService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CouponDTOPopulator implements Populator<CouponModel, CouponDTO> {
    private CouponService couponService;

    @Override
    public void populate(CouponModel source, CouponDTO target) {
        target.setId(source.getId());
        target.setCompanyId(source.getCompanyId());
        target.setActive(source.isActive());
        target.setAllowRedemptionMultipleCoupon(source.isAllowRedemptionMultipleCoupon());
        target.setLength(source.getLength());
        target.setSuffix(source.getSuffix());
        target.setPrefix(source.getPrefix());
        target.setMaxRedemptionPerCustomer(source.getMaxRedemptionPerCustomer());
        target.setName(source.getName());
        target.setMaxTotalRedemption(source.getMaxTotalRedemption());
        target.setQuantity(source.getQuantity());
        if(source.getPromotionSourceRule() != null) {
            target.setSourceRuleId(source.getPromotionSourceRule().getId());
        }
        List<CouponCodeModel> couponCodeModelList = couponService.findAllCouponCodeBy(source);
        if(CollectionUtils.isNotEmpty(couponCodeModelList)) {
            List<CouponCodeDTO> couponCodes = couponCodeModelList.stream()
                    .map(cc -> {
                        CouponCodeDTO dto = new CouponCodeDTO();
                        dto.setCode(cc.getCode());
                        return dto;
                    }).collect(Collectors.toList());
            target.setCodes(couponCodes);
        }
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }
}
