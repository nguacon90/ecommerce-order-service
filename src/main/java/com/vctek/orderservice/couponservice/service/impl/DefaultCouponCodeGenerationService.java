package com.vctek.orderservice.couponservice.service.impl;

import com.vctek.orderservice.couponservice.couponcodegeneration.CouponCodesGenerator;
import com.vctek.orderservice.couponservice.couponcodegeneration.dto.CouponCodeConfiguration;
import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.service.CouponCodeGenerationService;
import com.vctek.orderservice.repository.CouponCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class DefaultCouponCodeGenerationService implements CouponCodeGenerationService {
    public static final int EXTEND_QUANTITY_TIMES = 100;
    private CouponCodesGenerator couponCodesGenerator;
    private CouponCodeRepository couponCodeRepository;

    @Override
    public Set<String> generateCodes(CouponCodeConfiguration configuration) {
        Set<String> codes = new HashSet<>();
        int quantity = configuration.getQuantity();
        for (int i = 0; i < quantity * EXTEND_QUANTITY_TIMES; i++) {
            if(codes.size() == quantity) {
                break;
            }
            String couponCode = couponCodesGenerator.generateNextCouponCode(configuration);
            CouponCodeModel model = couponCodeRepository.findOneBy(couponCode, configuration.getCompanyId());
            if(model == null) {
                codes.add(couponCode);
            }
        }

        return codes;
    }

    @Autowired
    public void setCouponCodesGenerator(CouponCodesGenerator couponCodesGenerator) {
        this.couponCodesGenerator = couponCodesGenerator;
    }

    @Autowired
    public void setCouponCodeRepository(CouponCodeRepository couponCodeRepository) {
        this.couponCodeRepository = couponCodeRepository;
    }
}
