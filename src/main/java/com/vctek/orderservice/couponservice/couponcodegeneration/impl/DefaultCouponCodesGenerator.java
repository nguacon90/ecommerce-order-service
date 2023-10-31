package com.vctek.orderservice.couponservice.couponcodegeneration.impl;

import com.vctek.orderservice.couponservice.couponcodegeneration.CouponCodesGenerator;
import com.vctek.orderservice.couponservice.couponcodegeneration.dto.CouponCodeConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class DefaultCouponCodesGenerator implements CouponCodesGenerator {
    private SecureRandom secureRandom = new SecureRandom();
    private static final String NUMBER_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTYVWXYZ";

    @Override
    public String generateNextCouponCode(CouponCodeConfiguration configuration) {
        StringBuilder sb = new StringBuilder();
        char[] chars = NUMBER_ALPHABET.toCharArray();
        if(StringUtils.isNotBlank(configuration.getPrefix())) {
            sb.append(configuration.getPrefix());
        }

        for (int i = 0; i < configuration.getLength(); i++) {
            sb.append(chars[secureRandom.nextInt(chars.length)]);
        }

        if(StringUtils.isNotBlank(configuration.getSuffix())) {
            sb.append(configuration.getSuffix());
        }
        return sb.toString();
    }
}
