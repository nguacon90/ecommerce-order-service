package com.vctek.orderservice.facade.impl;

import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.kafka.producer.PromotionProducer;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.service.CouponService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class SyncReportFacadeTest {
    private SyncReportFacadeImpl syncReportFacade;
    @Mock
    private PromotionSourceRuleService promotionSourceRuleService;
    @Mock
    private CouponService couponService;
    @Mock
    private PromotionProducer promotionProducer;
    @Mock
    private Page<PromotionSourceRuleModel> pageMock;
    @Mock
    private Page<PromotionSourceRuleModel> pageMockEmpty;
    @Mock
    private Page<CouponModel> pageCouponMock;
    @Mock
    private Page<CouponModel> pageCouponMockEmpty;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        syncReportFacade = new SyncReportFacadeImpl();
        syncReportFacade.setCouponService(couponService);
        syncReportFacade.setPromotionProducer(promotionProducer);
        syncReportFacade.setPromotionSourceRuleService(promotionSourceRuleService);
        when(pageMockEmpty.getContent()).thenReturn(Collections.emptyList());
        when(pageCouponMockEmpty.getContent()).thenReturn(Collections.emptyList());
    }

    @Test
    public void syncPromotionReport() throws InterruptedException {
        when(promotionSourceRuleService.findAllByCompanyId(anyLong(), any()))
                .thenReturn(pageMock, pageMockEmpty);
        PromotionSourceRuleModel sourceRuleModel = new PromotionSourceRuleModel();
        when(pageMock.getContent()).thenReturn(Arrays.asList(sourceRuleModel));
        syncReportFacade.syncPromotion(1l, "PROMOTION");
        Thread.sleep(200);
        verify(promotionProducer).sendPromotionToKafka(sourceRuleModel);
        verify(promotionProducer, times(0)).sendCouponToKafka(any());
    }

    @Test
    public void synCouponReport() throws InterruptedException {
        when(couponService.findAllByCompanyId(anyLong(), any()))
                .thenReturn(pageCouponMock, pageCouponMockEmpty);
        CouponModel couponModel = new CouponModel();
        when(pageCouponMock.getContent()).thenReturn(Arrays.asList(couponModel));
        syncReportFacade.syncPromotion(1l, "COUPON");
        Thread.sleep(200);
        verify(promotionProducer).sendCouponToKafka(couponModel);
        verify(promotionProducer, times(0)).sendPromotionToKafka(any());
    }
}
