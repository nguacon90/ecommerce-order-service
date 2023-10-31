package com.vctek.orderservice.couponservice.service.impl;

import com.vctek.orderservice.couponservice.couponcodegeneration.dto.CouponCodeConfiguration;
import com.vctek.orderservice.couponservice.couponcodegeneration.impl.DefaultCouponCodesGenerator;
import com.vctek.orderservice.repository.CouponCodeRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class CouponCodeGenerationServiceTest {
    private DefaultCouponCodeGenerationService service;
    private DefaultCouponCodesGenerator generator;
    private CouponCodeConfiguration configuration;
    @Mock
    private CouponCodeRepository couponCodeRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        configuration = new CouponCodeConfiguration();
        generator = new DefaultCouponCodesGenerator();
        service = new DefaultCouponCodeGenerationService();
        service.setCouponCodesGenerator(generator);
        service.setCouponCodeRepository(couponCodeRepository);
        when(couponCodeRepository.findOneBy(anyString(), anyLong())).thenReturn(null);
    }

    @Test
    public void generateCodes_Length2WithQty1000() {
        configuration.setLength(3);
        configuration.setQuantity(10);
        configuration.setCompanyId(1l);
        Set<String> codes = service.generateCodes(configuration);
        assertEquals(10, codes.size());
    }
}
