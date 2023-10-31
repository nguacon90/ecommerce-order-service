package com.vctek.orderservice.service.impl;

import com.vctek.kafka.data.CustomerCouponDto;
import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.model.CustomerCouponModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.repository.CouponCodeRepository;
import com.vctek.orderservice.repository.CustomerCouponRepository;
import com.vctek.orderservice.service.CouponService;
import com.vctek.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class CustomerCouponServiceImplTest {
    @Mock
    private CustomerCouponRepository repository;
    @Mock
    private CouponCodeRepository couponCodeRepository;
    @Mock
    private CouponService couponService;
    @Mock
    private UserService userService;
    private CustomerCouponServiceImpl service;
    private CustomerCouponDto dto;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new CustomerCouponServiceImpl(repository);
        service.setCouponCodeRepository(couponCodeRepository);
        service.setCouponService(couponService);
        service.setUserService(userService);
        dto = new CustomerCouponDto();
        dto.setCouponId(2L);
        dto.setCompanyId(2L);
        dto.setCustomerId(2L);
    }

    @Test
    public void saveCustomerCoupon_emptyCoupon() {
        when(couponService.findById(anyLong(), anyLong())).thenReturn(null);
        service.saveCustomerCoupon(dto);
        verify(couponCodeRepository, times(0)).findAllByCouponIdAndUnAssignUser(anyLong());
        verify(repository, times(0)).save(any());
    }

    @Test
    public void saveCustomerCoupon_existModel() {
        when(couponService.findById(anyLong(), anyLong())).thenReturn(new CouponModel());
        service.saveCustomerCoupon(dto);
        verify(couponCodeRepository, times(0)).findAllByCouponIdAndUnAssignUser(anyLong());
        verify(repository, times(0)).save(any());
    }

    @Test
    public void saveCustomerCoupon_emptyCouponCode() {
        CouponModel couponModel = new CouponModel();
        PromotionSourceRuleModel promotionSourceRuleModel = new PromotionSourceRuleModel();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 9000);
        promotionSourceRuleModel.setEndDate(calendar.getTime());
        couponModel.setPromotionSourceRule(promotionSourceRuleModel);
        when(couponService.findById(anyLong(), anyLong())).thenReturn(couponModel);
        when(couponCodeRepository.findAllByCouponIdAndUnAssignUser(anyLong())).thenReturn(new ArrayList<>());
        service.saveCustomerCoupon(dto);
        verify(couponCodeRepository, times(1)).findAllByCouponIdAndUnAssignUser(anyLong());
        verify(repository, times(0)).save(any());
    }

    @Test
    public void saveCustomerCoupon() {
        CouponCodeModel couponCodeModel = new CouponCodeModel();
        couponCodeModel.setCode("code");
        CouponModel couponModel = new CouponModel();
        PromotionSourceRuleModel promotionSourceRuleModel = new PromotionSourceRuleModel();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 9000);
        promotionSourceRuleModel.setEndDate(calendar.getTime());
        couponModel.setPromotionSourceRule(promotionSourceRuleModel);
        when(couponService.findById(anyLong(), anyLong())).thenReturn(couponModel);
        when(couponCodeRepository.findAllByCouponIdAndUnAssignUser(anyLong())).thenReturn(Arrays.asList(couponCodeModel));
        when(couponService.findValidatedCouponCode(anyString(), anyLong())).thenReturn(couponCodeModel);
        service.saveCustomerCoupon(dto);
        verify(couponCodeRepository, times(1)).findAllByCouponIdAndUnAssignUser(anyLong());
        verify(repository, times(1)).save(any());
    }

    @Test
    public void getCouponByUser_anonymous() {
        when(userService.getCurrentUserId()).thenReturn(null);
        service.getCouponByUser(2L);
        verify(repository, times(0)).findAllByUserId(anyLong());
        verify(couponService, times(0)).findValidatedCouponCode(anyString(), anyLong());
    }

    @Test
    public void getCouponByUser_emptyCouponAssignUser() {
        when(userService.getCurrentUserId()).thenReturn(2L);
        when(repository.findAllByUserId(anyLong())).thenReturn(new ArrayList<>());
        service.getCouponByUser(2L);
        verify(repository, times(1)).findAllByUserId(anyLong());
        verify(couponService, times(0)).findValidatedCouponCode(anyString(), anyLong());
    }

    @Test
    public void getCouponByUser() {
        CustomerCouponModel customerCouponModel = new CustomerCouponModel();
        CouponCodeModel couponCodeModel = new CouponCodeModel();
        couponCodeModel.setCode("code");
        CouponModel couponModel = new CouponModel();
        couponModel.setName("name");
        couponCodeModel.setCoupon(couponModel);
        customerCouponModel.setCouponCodeModel(couponCodeModel);
        when(userService.getCurrentUserId()).thenReturn(2L);
        when(repository.findAllByUserId(anyLong())).thenReturn(Arrays.asList(customerCouponModel));
        when(couponService.findValidatedCouponCode(anyString(), anyLong())).thenReturn(couponCodeModel);
        service.getCouponByUser(2L);
        verify(repository, times(1)).findAllByUserId(anyLong());
        verify(couponService, times(1)).findValidatedCouponCode(anyString(), anyLong());
    }

}

