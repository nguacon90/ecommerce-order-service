package com.vctek.orderservice.promotionengine.promotionservice.service.impl;

import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionSourceRuleRepository;
import com.vctek.util.DateUtil;
import com.vctek.util.OrderType;
import com.vctek.util.PriceType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Calendar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class DefaultPromotionSourceRuleServiceTest {
    private DefaultPromotionSourceRuleService service;

    @Mock
    private PromotionSourceRuleRepository promotionSourceRuleRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PromotionSourceRuleModel sourceRuleModel;
    @Mock
    private CartModel cart;
    @Mock
    private OrderSourceModel orderSource;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new DefaultPromotionSourceRuleService(promotionSourceRuleRepository);
        service.setApplicationEventPublisher(applicationEventPublisher);
    }

    @Test
    public void validatePromotion_Inactive() {
        when(sourceRuleModel.isActive()).thenReturn(false);
        boolean isValid = service.isValidToAppliedForCart(sourceRuleModel, cart);
        assertFalse(isValid);
    }

    @Test
    public void validatePromotion_Expired() {
        when(sourceRuleModel.isActive()).thenReturn(true);
        when(sourceRuleModel.getEndDate()).thenReturn(DateUtil.parseDate("2022-01-01", "yyyy-MM-dd"));
        boolean isValid = service.isValidToAppliedForCart(sourceRuleModel, cart);
        assertFalse(isValid);
    }

    @Test
    public void validatePromotion_InvalidOrderTypes() {
        when(sourceRuleModel.isActive()).thenReturn(true);
        when(sourceRuleModel.getEndDate()).thenReturn(DateUtil.addDate(Calendar.getInstance().getTime(), 2));
        when(sourceRuleModel.getAppliedOrderTypes()).thenReturn("RETAIL");
        when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
        boolean isValid = service.isValidToAppliedForCart(sourceRuleModel, cart);
        assertFalse(isValid);
    }

    @Test
    public void validatePromotion_InvalidPriceTypes() {
        when(sourceRuleModel.isActive()).thenReturn(true);
        when(sourceRuleModel.getEndDate()).thenReturn(DateUtil.addDate(Calendar.getInstance().getTime(), 2));
        when(sourceRuleModel.getAppliedPriceTypes()).thenReturn("WHOLESALE_PRICE");

        when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
        when(cart.getPriceType()).thenReturn(PriceType.RETAIL_PRICE.toString());

        boolean isValid = service.isValidToAppliedForCart(sourceRuleModel, cart);
        assertFalse(isValid);
    }

    @Test
    public void validatePromotion_InvalidWarehouses() {
        when(sourceRuleModel.isActive()).thenReturn(true);
        when(sourceRuleModel.getEndDate()).thenReturn(DateUtil.addDate(Calendar.getInstance().getTime(), 2));
        when(sourceRuleModel.getAppliedWarehouseIds()).thenReturn("1");

        when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
        when(cart.getPriceType()).thenReturn(PriceType.RETAIL_PRICE.toString());
        when(cart.getWarehouseId()).thenReturn(2l);

        boolean isValid = service.isValidToAppliedForCart(sourceRuleModel, cart);
        assertFalse(isValid);
    }

    @Test
    public void validatePromotion_InvalidOrderSource() {
        when(sourceRuleModel.isActive()).thenReturn(true);
        when(sourceRuleModel.getEndDate()).thenReturn(DateUtil.addDate(Calendar.getInstance().getTime(), 2));
        when(sourceRuleModel.getExcludeOrderSources()).thenReturn("2");
        when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
        when(cart.getPriceType()).thenReturn(PriceType.RETAIL_PRICE.toString());
        when(cart.getWarehouseId()).thenReturn(2l);
        when(cart.getOrderSourceModel()).thenReturn(orderSource);
        when(orderSource.getId()).thenReturn(2l);

        boolean isValid = service.isValidToAppliedForCart(sourceRuleModel, cart);
        assertFalse(isValid);
    }

    @Test
    public void validatePromotion_valid_Case1() {
        when(sourceRuleModel.isActive()).thenReturn(true);
        when(sourceRuleModel.getEndDate()).thenReturn(DateUtil.addDate(Calendar.getInstance().getTime(), 2));
        when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
        when(cart.getPriceType()).thenReturn(PriceType.RETAIL_PRICE.toString());
        when(cart.getWarehouseId()).thenReturn(2l);
        when(cart.getOrderSourceModel()).thenReturn(orderSource);
        when(orderSource.getId()).thenReturn(2l);

        boolean isValid = service.isValidToAppliedForCart(sourceRuleModel, cart);
        assertTrue(isValid);
    }

    @Test
    public void validatePromotion_valid_Case2() {
        when(sourceRuleModel.isActive()).thenReturn(true);
        when(sourceRuleModel.getEndDate()).thenReturn(DateUtil.addDate(Calendar.getInstance().getTime(), 2));
        when(sourceRuleModel.getAppliedOrderTypes()).thenReturn("ONLINE");
        when(sourceRuleModel.getAppliedPriceTypes()).thenReturn("RETAIL_PRICE");
        when(sourceRuleModel.getAppliedWarehouseIds()).thenReturn("2");
        when(sourceRuleModel.getExcludeOrderSources()).thenReturn("222");

        when(cart.getType()).thenReturn(OrderType.ONLINE.toString());
        when(cart.getPriceType()).thenReturn(PriceType.RETAIL_PRICE.toString());
        when(cart.getWarehouseId()).thenReturn(2l);
        when(cart.getOrderSourceModel()).thenReturn(orderSource);
        when(orderSource.getId()).thenReturn(2l);

        boolean isValid = service.isValidToAppliedForCart(sourceRuleModel, cart);
        assertTrue(isValid);
    }
}
