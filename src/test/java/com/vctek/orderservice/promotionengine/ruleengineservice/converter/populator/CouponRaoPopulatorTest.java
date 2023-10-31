package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.dto.RedeemableCouponCodeData;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderHasCouponCodeModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.service.ValidateCouponService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class CouponRaoPopulatorTest {
    private CouponRaoPopulator populator;
    @Mock
    private AbstractOrderModel cartMock;
    private CartRAO cartRao = new CartRAO();
    private Set<CouponCodeModel> appliedCouponCodes = new HashSet<>();
    @Mock
    private CouponCodeModel couponCodeMock;
    @Mock
    private CouponModel couponMock;
    @Mock
    private OrderHasCouponCodeModel orderHasCouponMock;
    @Mock
    private ValidateCouponService validateCouponService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new CouponRaoPopulator();
        populator.setValidateCouponService(validateCouponService);
        appliedCouponCodes.add(couponCodeMock);
    }

    @Test
    public void populate() {
        when(cartMock.getOrderHasCouponCodeModels()).thenReturn(new HashSet<>(Arrays.asList(orderHasCouponMock)));
        when(orderHasCouponMock.getCouponCode()).thenReturn(couponCodeMock);
        when(couponCodeMock.getCode()).thenReturn("code");
        when(couponCodeMock.getId()).thenReturn(1l);
        when(couponCodeMock.getCoupon()).thenReturn(couponMock);
        when(couponMock.getId()).thenReturn(12l);
        when(orderHasCouponMock.getRedemptionQuantity()).thenReturn(1);
        RedeemableCouponCodeData value = new RedeemableCouponCodeData();
        value.setCanRedeem(true);
        when(validateCouponService.getValidateRedemptionQuantityCouponCode(couponCodeMock, 1)).thenReturn(value);
        populator.populate(cartMock, cartRao);
        assertEquals(1, cartRao.getCoupons().size());
        assertEquals(12l, cartRao.getCoupons().get(0).getCouponId(), 0);
        assertEquals("code", cartRao.getCoupons().get(0).getCouponCode());

    }
}
