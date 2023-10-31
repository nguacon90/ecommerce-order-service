package com.vctek.orderservice.service.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.couponservice.model.CouponRedemptionModel;
import com.vctek.orderservice.dto.CommerceRedeemCouponParameter;
import com.vctek.orderservice.dto.CouponCodeData;
import com.vctek.orderservice.dto.RedeemableCouponCodeData;
import com.vctek.orderservice.dto.ValidCouponCodeData;
import com.vctek.orderservice.event.CouponCRUEvent;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderHasCouponCodeModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionResultRepository;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionEngineService;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.repository.CouponCodeRepository;
import com.vctek.orderservice.repository.CouponRepository;
import com.vctek.orderservice.repository.OrderHasCouponRepository;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.CouponRedemptionService;
import com.vctek.orderservice.service.ModelService;
import com.vctek.orderservice.service.ValidateCouponService;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

public class CouponServiceTest {
    private CouponServiceImpl service;
    @Mock
    private CouponRepository repositoryMock;
    @Mock
    private CouponCodeRepository couponCodeRepository;
    @Mock
    private CouponRedemptionService couponRedemptionServiceMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private PromotionEngineService promotionEngineServiceMock;
    @Mock
    private CouponModel couponMock;
    @Mock
    private PromotionSourceRuleModel ruleMock;
    private Set<CouponModel> coupons = new HashSet<>();
    private Set<CouponCodeModel> couponCodes = new HashSet<>();
    @Mock
    private CouponCodeModel couponCodeMock;
    private CommerceRedeemCouponParameter parameter;
    @Mock
    private AbstractOrderModel orderMock;
    private String couponCode = "SUMMER";
    @Mock
    private CouponCodeModel existedCouponCodeMock;
    @Mock
    private CouponModel existedCouponMock;
    @Mock
    private OrderHasCouponCodeModel orderHasCouponMock;
    @Mock
    private PromotionResultService promotionResultService;
    @Mock
    private OrderHasCouponCodeModel orderHasCouponMock2;
    @Mock
    private CouponCodeModel couponCodeMock2;
    @Mock
    private CouponModel couponMock2;
    @Mock
    private PromotionSourceRuleModel ruleMock2;
    @Mock
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private OrderHasCouponRepository orderHasCouponRepository;
    @Mock
    private PromotionResultRepository promotionResultRepository;
    @Mock
    private CalculationService calculationService;
    @Mock
    private ValidateCouponService validateCouponService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        parameter = new CommerceRedeemCouponParameter(orderMock, couponCode);
        service = new CouponServiceImpl();
        service.setCouponRepository(repositoryMock);
        service.setCouponCodeRepository(couponCodeRepository);
        service.setCouponRedemptionService(couponRedemptionServiceMock);
        service.setModelService(modelServiceMock);
        service.setPromotionEngineService(promotionEngineServiceMock);
        service.setPromotionResultService(promotionResultService);
        service.setCommerceCartCalculationStrategy(commerceCartCalculationStrategy);
        service.setApplicationEventPublisher(applicationEventPublisher);
        service.setOrderHasCouponRepository(orderHasCouponRepository);
        service.setPromotionResultRepository(promotionResultRepository);
        service.setCalculationService(calculationService);
        service.setValidateCouponService(validateCouponService);
        coupons.add(couponMock);
        couponCodes.add(couponCodeMock);
        when(couponMock.getCouponCodes()).thenReturn(couponCodes);
        when(couponCodeMock.getCoupon()).thenReturn(couponMock);
        when(ruleMock.getCoupons()).thenReturn(coupons);
        when(ruleMock.getId()).thenReturn(1l);
        RedeemableCouponCodeData couponCodeData = new RedeemableCouponCodeData();
        couponCodeData.setCanRedeem(true);
        when(validateCouponService.getValidateRedemptionQuantityCouponCode(any(CouponCodeModel.class), anyInt())).thenReturn(couponCodeData);
    }

    @Test
    public void save() {
        service.save(couponMock);
        verify(repositoryMock).save(couponMock);
        verify(applicationEventPublisher).publishEvent(any(CouponCRUEvent.class));
    }

    @Test
    public void findAllForQualifyingByCompanyId() {
        service.findAllForQualifyingByCompanyId(1l);
        verify(repositoryMock).findAllForQualifyingByCompanyId(1l);
    }

    @Test
    public void updateUseForPromotion_EmptyRule() {
        service.updateUseForPromotion(Arrays.asList(1l), null);
        verify(repositoryMock, times(0)).saveAll(anyCollection());
    }

    @Test
    public void updateUseForPromotion_ExistedCouponCodeHadRedeemed_ShouldAllow() {
        when(couponRedemptionServiceMock.countBy(couponCodeMock)).thenReturn(1l);
        CouponModel coupon = new CouponModel();
        coupon.setPromotionSourceRule(ruleMock);
        when(repositoryMock.findById(1l)).thenReturn(Optional.of(coupon));

        service.updateUseForPromotion(Arrays.asList(1l), ruleMock);
        verify(couponMock).setPromotionSourceRule(null);
        verify(repositoryMock, times(1)).saveAll(anyCollection());

    }

    @Test
    public void updateUseForPromotion_success() {
        when(couponRedemptionServiceMock.countBy(couponCodeMock)).thenReturn(0l);
        CouponModel coupon = new CouponModel();
        when(repositoryMock.findById(1l)).thenReturn(Optional.of(coupon));
        service.updateUseForPromotion(Arrays.asList(1l), ruleMock);

        verify(couponMock).setPromotionSourceRule(null);
        verify(repositoryMock, times(1)).saveAll(anyCollection());
    }

    @Test
    public void findAllBy() {
        service.findAllBy(1l, "", PageRequest.of(10, 10));
        verify(repositoryMock).findAllByCompanyId(anyLong(), any(Pageable.class));
    }

    @Test
    public void findAllByName() {
        service.findAllBy(1l, "aa", PageRequest.of(10, 10));
        verify(repositoryMock).findAllByCompanyIdAndNameLike(anyLong(), anyString(), any(Pageable.class));
    }

    @Test
    public void findById() {
        service.findById(1l, 2l);
        verify(repositoryMock).findByIdAndCompanyId(anyLong(), anyLong());
    }

    @Test
    public void delete() {
        service.delete(couponMock);
        verify(repositoryMock).delete(couponMock);
    }

    @Test
    public void findAllForQualifyingByCompanyIdOrSourceRule() {
        service.findAllForQualifyingByCompanyIdOrSourceRule(1l, 11l);
        verify(repositoryMock).findAllForQualifyingByCompanyIdOrSourceRule(anyLong(), anyLong());
    }

    @Test
    public void redeemCoupon_orderContainsCoupon() {
        try {
            when(orderMock.getOrderHasCouponCodeModels()).thenReturn(new HashSet<>(Arrays.asList(orderHasCouponMock)));
            when(orderHasCouponMock.getCouponCode()).thenReturn(couponCodeMock);
            when(couponCodeMock.getCode()).thenReturn(couponCode);

            service.redeemCoupon(parameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EXISTED_COUPON_IN_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void redeemCoupon_CouponNotExisted() {
        try {
            when(orderMock.getOrderHasCouponCodeModels()).thenReturn(Collections.emptySet());
            when(couponCodeRepository.findValidCoupon(anyString(), anyLong(), any(Date.class)))
                    .thenReturn(Collections.emptyList());
            service.redeemCoupon(parameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_COUPON_CODE.code(), e.getCode());
        }
    }

    @Test
    public void redeemCoupon_ExistedButOverMaxRedemption() {
        try {
            when(orderMock.getOrderHasCouponCodeModels()).thenReturn(Collections.emptySet());
            when(couponCodeRepository.findValidCoupon(anyString(), anyLong(), any(Date.class)))
                    .thenReturn(new ArrayList<>(couponCodes));
            when(couponCodeMock.getCoupon()).thenReturn(couponMock);
            when(couponMock.getMaxTotalRedemption()).thenReturn(1);
            when(couponRedemptionServiceMock.countBy(couponCodeMock)).thenReturn(1l);

            service.redeemCoupon(parameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.COUPON_OVER_MAX_REDEMPTION.code(), e.getCode());
        }
    }

    @Test
    public void redeemCoupon_OrderHasNotAppliedCoupon_Valid() {
        when(orderMock.getOrderHasCouponCodeModels()).thenReturn(Collections.emptySet());
        when(couponCodeRepository.findValidCoupon(anyString(), anyLong(), any(Date.class)))
                .thenReturn(new ArrayList<>(couponCodes));
        when(couponCodeMock.getCoupon()).thenReturn(couponMock);
        when(couponMock.getMaxTotalRedemption()).thenReturn(3);
        when(couponRedemptionServiceMock.countBy(couponCodeMock)).thenReturn(2l);
        when(orderMock.getOrderHasCouponCodeModels()).thenReturn(new HashSet<>());
        parameter.setRedemptionQuantity(1);

        service.redeemCoupon(parameter);
        verify(modelServiceMock).saveAll(orderMock, couponCodeMock);
        verify(promotionEngineServiceMock).updatePromotions(anyCollection(), eq(orderMock));
        verify(calculationService).calculateVatByProductOf(orderMock, true);
    }

    @Test
    public void redeemCoupon_OrderHasAppliedCouponNotAcceptMultiple() {
        when(orderMock.getOrderHasCouponCodeModels()).thenReturn(Collections.emptySet());
        when(couponCodeRepository.findValidCoupon(anyString(), anyLong(), any(Date.class)))
                .thenReturn(new ArrayList<>(couponCodes));
        when(couponCodeMock.getCoupon()).thenReturn(couponMock);
        when(couponMock.getMaxTotalRedemption()).thenReturn(3);
        when(couponRedemptionServiceMock.countBy(couponCodeMock)).thenReturn(2l);
        when(orderMock.getOrderHasCouponCodeModels()).thenReturn(new HashSet<>(Arrays.asList(orderHasCouponMock)));
        when(orderHasCouponMock.getCouponCode()).thenReturn(existedCouponCodeMock);
        when(existedCouponCodeMock.getCoupon()).thenReturn(existedCouponMock);
        when(existedCouponMock.isAllowRedemptionMultipleCoupon()).thenReturn(false);

        try {
            service.redeemCoupon(parameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_ALLOW_REDEMPTION_MULTIPLE_COUPON_IN_ORDER.code(), e.getCode());
        }
    }

    @Test
    public void redeemCoupon_OrderHasAppliedCouponAcceptMultiple_Valid() {
        when(orderMock.getOrderHasCouponCodeModels()).thenReturn(Collections.emptySet());
        when(couponCodeRepository.findValidCoupon(anyString(), anyLong(), any(Date.class)))
                .thenReturn(new ArrayList<>(couponCodes));
        when(couponCodeMock.getCoupon()).thenReturn(couponMock);
        when(couponMock.isAllowRedemptionMultipleCoupon()).thenReturn(true);
        when(couponMock.getMaxTotalRedemption()).thenReturn(3);
        when(couponRedemptionServiceMock.countBy(couponCodeMock)).thenReturn(2l);
        when(orderMock.getOrderHasCouponCodeModels()).thenReturn(new HashSet<>(Arrays.asList(orderHasCouponMock)));
        when(orderHasCouponMock.getCouponCode()).thenReturn(couponCodeMock);
        when(existedCouponCodeMock.getCoupon()).thenReturn(existedCouponMock);
        when(existedCouponMock.isAllowRedemptionMultipleCoupon()).thenReturn(true);
        parameter.setRedemptionQuantity(1);

        service.redeemCoupon(parameter);
        verify(modelServiceMock).saveAll(orderMock, couponCodeMock);
        verify(promotionEngineServiceMock).updatePromotions(anyCollection(), eq(orderMock));
        verify(calculationService).calculateVatByProductOf(orderMock, true);
    }

    @Test
    public void releaseCoupon_NotExisted() {
        try {
            when(orderMock.getOrderHasCouponCodeModels()).thenReturn(new HashSet<>(Arrays.asList(orderHasCouponMock)));
            when(orderHasCouponMock.getCouponCode()).thenReturn(couponCodeMock);
            when(couponCodeMock.getCode()).thenReturn("SPRING");

            service.releaseCoupon(parameter);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_COUPON_CODE.code(), e.getCode());
        }
    }

    @Test
    public void releaseCoupon_Valid() {
        when(orderMock.getOrderHasCouponCodeModels()).thenReturn(new HashSet<>(Arrays.asList(orderHasCouponMock)));
        when(orderHasCouponMock.getCouponCode()).thenReturn(couponCodeMock);
        when(couponCodeMock.getCode()).thenReturn(couponCode);
        service.releaseCoupon(parameter);
        verify(modelServiceMock).saveAll(orderMock);
        verify(modelServiceMock).remove(orderHasCouponMock);
        verify(promotionEngineServiceMock).updatePromotions(anyCollection(), eq(orderMock));
        verify(calculationService).calculateVatByProductOf(orderMock, true);
    }

    @Test
    public void createCouponRedemption_EmptyAppliedCoupon() {
        OrderModel order = new OrderModel();
        order.setOrderHasCouponCodeModels(new HashSet<>());

        service.createCouponRedemption(order);
        verify(couponRedemptionServiceMock, times(0)).saveAll(anyList());
    }

    @Test
    public void createCouponRedemption() {
        OrderModel order = new OrderModel();
        order.setOrderHasCouponCodeModels(new HashSet<>(Arrays.asList(orderHasCouponMock)));
        when(orderHasCouponMock.getCouponCode()).thenReturn(couponCodeMock);

        service.createCouponRedemption(order);
        verify(couponRedemptionServiceMock, times(1)).saveAll(anyList());
    }

    @Test
    public void getValidatedCouponCode_OrderHasNotCoupon() {
        when(orderMock.getOrderHasCouponCodeModels()).thenReturn(new HashSet<>());
        ValidCouponCodeData validatedCouponCode = service.getValidatedCouponCode(orderMock);
        assertTrue(validatedCouponCode.isValid());
        assertEquals(0, validatedCouponCode.getCouponData().size());
    }

    @Test
    public void getValidatedCouponCode_OrderHasCoupon_OfPotentialPromotion_ShouldTrue() {
        when(orderMock.getOrderHasCouponCodeModels()).thenReturn(new HashSet<>(Arrays.asList(orderHasCouponMock)));
        when(orderHasCouponMock.getCouponCode()).thenReturn(couponCodeMock);
        when(couponCodeMock.getCoupon()).thenReturn(couponMock);
        when(couponCodeMock.getCode()).thenReturn("ABC");
        when(orderHasCouponMock.getRedemptionQuantity()).thenReturn(2);
        when(couponMock.getPromotionSourceRule()).thenReturn(ruleMock);
        when(ruleMock.getId()).thenReturn(12l);

        when(promotionResultService.findAllPromotionSourceRulesByOrder(orderMock)).thenReturn(new HashSet<>(Arrays.asList(ruleMock)));

        ValidCouponCodeData validatedCouponCode = service.getValidatedCouponCode(orderMock);
        assertTrue(validatedCouponCode.isValid());
        assertEquals(1, validatedCouponCode.getCouponData().size());
        assertEquals(true, validatedCouponCode.getCouponData().get(0).isValid());
        assertEquals("ABC", validatedCouponCode.getCouponData().get(0).getCode());
        assertEquals(2, validatedCouponCode.getCouponData().get(0).getTotalRedemption(), 0);
        assertEquals(12l, validatedCouponCode.getCouponData().get(0).getPromotionId(), 0);
    }

    @Test
    public void getValidatedCouponCode_OrderHasCoupon_OfCouldFiredPromotion_ShouldTrue() {
        when(orderMock.getOrderHasCouponCodeModels()).thenReturn(new HashSet<>(Arrays.asList(orderHasCouponMock)));
        when(orderHasCouponMock.getCouponCode()).thenReturn(couponCodeMock);
        when(couponCodeMock.getCoupon()).thenReturn(couponMock);
        when(couponCodeMock.getCode()).thenReturn("ABC");
        when(orderHasCouponMock.getRedemptionQuantity()).thenReturn(2);
        when(couponMock.getPromotionSourceRule()).thenReturn(ruleMock);
        when(ruleMock.getId()).thenReturn(12l);

        when(promotionResultService.findAllPromotionSourceRulesByOrder(orderMock)).thenReturn(new HashSet<>());
        when(orderMock.getCouldFirePromotions()).thenReturn(new HashSet<>(Arrays.asList(ruleMock)));
        ValidCouponCodeData validatedCouponCode = service.getValidatedCouponCode(orderMock);
        assertTrue(validatedCouponCode.isValid());
        assertEquals(1, validatedCouponCode.getCouponData().size());
        assertEquals(true, validatedCouponCode.getCouponData().get(0).isValid());
        assertEquals("ABC", validatedCouponCode.getCouponData().get(0).getCode());
        assertEquals(2, validatedCouponCode.getCouponData().get(0).getTotalRedemption(), 0);
        assertEquals(12l, validatedCouponCode.getCouponData().get(0).getPromotionId(), 0);
    }

    @Test
    public void getValidatedCouponCode_OrderHasCoupon_ThatNotBelongToPromotion_ShouldFalse() {
        when(orderMock.getOrderHasCouponCodeModels()).thenReturn(new HashSet<>(Arrays.asList(orderHasCouponMock)));
        when(orderHasCouponMock.getCouponCode()).thenReturn(couponCodeMock);
        when(couponCodeMock.getCoupon()).thenReturn(couponMock);
        when(couponCodeMock.getCode()).thenReturn("ABC");
        when(orderHasCouponMock.getRedemptionQuantity()).thenReturn(2);
        when(couponMock.getPromotionSourceRule()).thenReturn(null);

        ValidCouponCodeData validatedCouponCode = service.getValidatedCouponCode(orderMock);
        assertFalse(validatedCouponCode.isValid());
        assertEquals(1, validatedCouponCode.getCouponData().size());
        assertEquals(false, validatedCouponCode.getCouponData().get(0).isValid());
        assertEquals("ABC", validatedCouponCode.getCouponData().get(0).getCode());
        assertEquals(2, validatedCouponCode.getCouponData().get(0).getTotalRedemption(), 0);
        assertNull(validatedCouponCode.getCouponData().get(0).getPromotionId());
    }

    @Test
    public void getValidatedCouponCode_OrderHasCoupons_OfPotentialPromotion_ExistedInvalidCoupon_ShouldFalse() {
        when(orderMock.getOrderHasCouponCodeModels()).thenReturn(new HashSet<>(Arrays.asList(orderHasCouponMock, orderHasCouponMock2)));
        when(orderHasCouponMock.getCouponCode()).thenReturn(couponCodeMock);
        when(orderHasCouponMock2.getCouponCode()).thenReturn(couponCodeMock2);
        when(couponCodeMock.getCoupon()).thenReturn(couponMock);
        when(couponCodeMock2.getCoupon()).thenReturn(couponMock2);

        when(couponCodeMock.getCode()).thenReturn("ABC");
        when(orderHasCouponMock.getRedemptionQuantity()).thenReturn(2);
        when(couponMock.getPromotionSourceRule()).thenReturn(ruleMock);
        when(couponMock2.getPromotionSourceRule()).thenReturn(ruleMock2);
        when(ruleMock.getId()).thenReturn(12l);
        when(ruleMock2.getId()).thenReturn(22l);

        when(promotionResultService.findAllPromotionSourceRulesByOrder(orderMock)).thenReturn(new HashSet<>(Arrays.asList(ruleMock)));

        ValidCouponCodeData validatedCouponCode = service.getValidatedCouponCode(orderMock);
        assertFalse(validatedCouponCode.isValid());
        assertEquals(2, validatedCouponCode.getCouponData().size());
        Optional<CouponCodeData> first = validatedCouponCode.getCouponData().stream().filter(c -> c.getPromotionId().equals(12l)).findFirst();
        Optional<CouponCodeData> second = validatedCouponCode.getCouponData().stream().filter(c -> c.getPromotionId().equals(22l)).findFirst();

        assertEquals("ABC", first.get().getCode());
        assertEquals(true, first.get().isValid());
        assertEquals(2, first.get().getTotalRedemption(), 0);

        assertEquals(false, second.get().isValid());
    }

    @Test
    public void findAllCouponCodeBy() {
        service.findAllCouponCodeBy(new CouponModel());
        verify(couponCodeRepository).findAllByCoupon(any(CouponModel.class));
    }

    @Test
    public void findAllByCompanyId() {
        service.findAllByCompanyId(2L, PageRequest.of(0, 20));
        verify(repositoryMock).findAllByCompanyId(anyLong(), any());
    }

    @Test
    public void revertAllCouponToOrder_emptyCounpon() {
        OrderModel orderModel = new OrderModel();
        orderModel.setId(2L);
        when(orderHasCouponRepository.findAllByOrderId(anyLong())).thenReturn(Collections.emptyList());
        service.revertAllCouponToOrder(orderModel);
        verify(orderHasCouponRepository, times(1)).findAllByOrderId(anyLong());
        verify(modelServiceMock, times(0)).removeAll(anyCollection());
        verify(modelServiceMock, times(0)).save(any(OrderModel.class));
        verify(promotionEngineServiceMock, times(0)).updatePromotions(anyCollection(), any(OrderModel.class));
        verify(commerceCartCalculationStrategy, times(0)).splitOrderPromotionToEntries(any(OrderModel.class));
        verify(couponRedemptionServiceMock, times(0)).findAllBy(any(OrderModel.class));
        verify(calculationService, times(0)).calculateVatByProductOf(any(OrderModel.class), anyBoolean());
    }

    @Test
    public void revertAllCouponToOrder_removeCounpon() {
        OrderHasCouponCodeModel model = new OrderHasCouponCodeModel();
        CouponCodeModel couponCode = new CouponCodeModel();
        couponCode.setCoupon(new CouponModel());
        model.setCouponCode(couponCode);
        CouponRedemptionModel redemptionModel = new CouponRedemptionModel();
        OrderModel orderModel = new OrderModel();
        orderModel.setId(2L);
        orderModel.setOrderHasCouponCodeModels(new HashSet<>(Arrays.asList(model)));
        orderModel.setCouponRedemptionModels(new HashSet<>(Arrays.asList(redemptionModel)));
        when(orderHasCouponRepository.findAllByOrderId(anyLong())).thenReturn(Arrays.asList(model));
        when(couponRedemptionServiceMock.findAllBy(any())).thenReturn(Arrays.asList(redemptionModel));
        service.revertAllCouponToOrder(orderModel);
        verify(orderHasCouponRepository, times(1)).findAllByOrderId(anyLong());
        verify(modelServiceMock, times(1)).removeAll(anyCollection());
        verify(modelServiceMock, times(1)).save(any(OrderModel.class));
        verify(calculationService, times(0)).calculateVatByProductOf(any(OrderModel.class), anyBoolean());
    }
}
