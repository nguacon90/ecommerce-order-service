package com.vctek.orderservice.converter.coupon;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.dto.CouponCodeData;
import com.vctek.orderservice.dto.request.CouponRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.service.CouponRedemptionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class CouponModelPopulatorTest {
    private CouponModelPopulator populator;
    @Mock
    private CouponRedemptionService couponRedemptionService;
    @Mock
    private CouponRequest requestMock;
    private CouponModel couponModel;
    @Mock
    private CouponCodeData codeMock1;
    @Mock
    private CouponCodeData codeMock2;
    @Mock
    private CouponCodeData codeMock3;
    private String CODE_1 = "code1";
    private String CODE_2 = "code2";
    private String CODE_3 = "code3";
    @Mock
    private CouponCodeModel codeModelMock1;
    @Mock
    private CouponCodeModel codeModelMock2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        couponModel = new CouponModel();
        populator = new CouponModelPopulator();
        populator.setCouponRedemptionService(couponRedemptionService);
        when(codeMock1.getCode()).thenReturn(CODE_1);
        when(codeMock2.getCode()).thenReturn(CODE_2);
        when(codeMock3.getCode()).thenReturn(CODE_3);
        when(codeModelMock1.getCode()).thenReturn(CODE_1);
        when(codeModelMock2.getCode()).thenReturn(CODE_2);
    }

    @Test
    public void populateCouponCodes_CreateNew() {
        when(requestMock.getCodes()).thenReturn(Arrays.asList(codeMock1, codeMock2));
        populator.populateCouponCodes(requestMock, couponModel);
        verify(couponRedemptionService, times(0)).countBy(any());
        List<String> couponCodes = couponModel.getCouponCodes().stream()
                .map(CouponCodeModel::getCode).collect(Collectors.toList());

        assertEquals(2, couponCodes.size());
        assertTrue(couponCodes.contains(CODE_1));
        assertTrue(couponCodes.contains(CODE_2));
    }

    @Test
    public void populateCouponCodes_UpdateOldCode_HasNotRedeem_ShouldBeValid() {
        couponModel.setCouponCodes(new LinkedHashSet<>(Arrays.asList(codeModelMock1, codeModelMock2)));
        when(couponRedemptionService.countBy(codeModelMock1)).thenReturn(0l);
        when(couponRedemptionService.countBy(codeModelMock2)).thenReturn(0l);
        when(requestMock.getCodes()).thenReturn(Arrays.asList(codeMock1, codeMock2));
        populator.populateCouponCodes(requestMock, couponModel);
        verify(couponRedemptionService, times(2)).countBy(any());
        List<String> couponCodes = couponModel.getCouponCodes().stream()
                .map(CouponCodeModel::getCode).collect(Collectors.toList());

        assertEquals(2, couponCodes.size());
        assertTrue(couponCodes.contains(CODE_1));
        assertTrue(couponCodes.contains(CODE_2));
    }

    @Test
    public void populateCouponCodes_UpdateOldCode_HasRedeemed_ShouldBeValid() {
        couponModel.setCouponCodes(new LinkedHashSet<>(Arrays.asList(codeModelMock1, codeModelMock2)));
        when(couponRedemptionService.countBy(codeModelMock1)).thenReturn(10l);
        when(couponRedemptionService.countBy(codeModelMock2)).thenReturn(0l);
        when(requestMock.getCodes()).thenReturn(Arrays.asList(codeMock1, codeMock2));
        populator.populateCouponCodes(requestMock, couponModel);
        verify(couponRedemptionService, times(2)).countBy(any());
        List<String> couponCodes = couponModel.getCouponCodes().stream()
                .map(CouponCodeModel::getCode).collect(Collectors.toList());

        assertEquals(2, couponCodes.size());
        assertTrue(couponCodes.contains(CODE_1));
        assertTrue(couponCodes.contains(CODE_2));
    }

    @Test
    public void populateCouponCodes_HasNewCode_LosedOneOldCode_ShouldThrowException() {
        couponModel.setCouponCodes(new LinkedHashSet<>(Arrays.asList(codeModelMock1, codeModelMock2)));
        when(couponRedemptionService.countBy(codeModelMock1)).thenReturn(10l);
        when(couponRedemptionService.countBy(codeModelMock2)).thenReturn(0l);
        when(requestMock.getCodes()).thenReturn(Arrays.asList(codeMock1, codeMock3));
        try {
            populator.populateCouponCodes(requestMock, couponModel);
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CANNOT_UPDATE_REDEMPTION_COUPON_CODE.code(), e.getCode());
        }
    }

    @Test
    public void populateCouponCodes_HasNewCode_THeSameOldCode_ShouldValid() {
        couponModel.setCouponCodes(new LinkedHashSet<>(Arrays.asList(codeModelMock1, codeModelMock2)));
        when(couponRedemptionService.countBy(codeModelMock1)).thenReturn(10l);
        when(couponRedemptionService.countBy(codeModelMock2)).thenReturn(0l);
        when(requestMock.getCodes()).thenReturn(Arrays.asList(codeMock1, codeMock2, codeMock3));
        populator.populateCouponCodes(requestMock, couponModel);
        verify(couponRedemptionService, times(2)).countBy(any());
        List<String> couponCodes = couponModel.getCouponCodes().stream()
                .map(CouponCodeModel::getCode).collect(Collectors.toList());

        assertEquals(3, couponCodes.size());
        assertTrue(couponCodes.contains(CODE_1));
        assertTrue(couponCodes.contains(CODE_2));
        assertTrue(couponCodes.contains(CODE_3));
    }
}
