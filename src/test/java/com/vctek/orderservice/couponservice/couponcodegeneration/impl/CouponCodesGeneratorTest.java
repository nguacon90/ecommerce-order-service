package com.vctek.orderservice.couponservice.couponcodegeneration.impl;

import com.vctek.orderservice.couponservice.couponcodegeneration.dto.CouponCodeConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CouponCodesGeneratorTest {
    private DefaultCouponCodesGenerator generator;
    private CouponCodeConfiguration configuration;

    @Before
    public void setUp() {
        configuration = new CouponCodeConfiguration();
        generator = new DefaultCouponCodesGenerator();
    }

    @Test
    public void generateWithPreFix_Length1() {
        configuration.setPrefix("SUMMER");
        configuration.setLength(1);
        String code = generator.generateNextCouponCode(configuration);
        assertEquals(7, code.length());
        assertTrue(code.startsWith("SUMMER"));
    }

    @Test
    public void generateWithPreFix_Length10() {
        configuration.setPrefix("SUMMER");
        configuration.setLength(10);
        String code = generator.generateNextCouponCode(configuration);
        assertEquals(16, code.length());
        assertTrue(code.startsWith("SUMMER"));
    }

    @Test
    public void generateWithPreFixAndSuffix_Length10() {
        configuration.setPrefix("SUMMER");
        configuration.setSuffix("COMBO");
        configuration.setLength(4);
        String code = generator.generateNextCouponCode(configuration);
        assertEquals(15, code.length());
        assertTrue(code.startsWith("SUMMER"));
        assertTrue(code.endsWith("COMBO"));
    }
}
