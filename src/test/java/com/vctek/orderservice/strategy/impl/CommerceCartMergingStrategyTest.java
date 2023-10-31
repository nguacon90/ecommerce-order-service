package com.vctek.orderservice.strategy.impl;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.CommerceCartService;
import com.vctek.orderservice.service.ModelService;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import com.vctek.orderservice.strategy.EntryMergeStrategy;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CommerceCartMergingStrategyTest {
    private DefaultCommerceCartMergingStrategy strategy;

    @Mock
    private CartService cartService;
    @Mock
    private UserService userService;
    @Mock
    private EntryMergeStrategy entryMergeStrategy;
    @Mock
    private CommerceCartService commerceCartService;
    @Mock
    private ModelService modelService;
    @Mock
    private CartModel fromCart;
    @Mock
    private CartModel toCart;
    @Mock
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;
    @Mock
    private CartEntryModel fromEntry1;
    @Mock
    private CartEntryModel toEntry1;
    @Mock
    private CartEntryModel cloneFromEntry1;
    private List<AbstractOrderEntryModel> toEntries;
    private ArgumentCaptor<CommerceAbstractOrderParameter> captor;
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        strategy = new DefaultCommerceCartMergingStrategy();
        captor = ArgumentCaptor.forClass(CommerceAbstractOrderParameter.class);
        strategy.setCartService(cartService);
        strategy.setCommerceCartService(commerceCartService);
        strategy.setModelService(modelService);
        strategy.setUserService(userService);
        strategy.setEntryMergeStrategy(entryMergeStrategy);
        strategy.setCommerceCartCalculationStrategy(commerceCartCalculationStrategy);
        when(userService.getCurrentUserId()).thenReturn(1l);
        when(fromCart.getId()).thenReturn(11l);
        when(fromCart.getCode()).thenReturn("fromCart");
        when(fromCart.getSellSignal()).thenReturn(SellSignal.ECOMMERCE_WEB.toString());
        when(fromCart.getCompanyId()).thenReturn(2l);
        when(fromCart.getCreateByUser()).thenReturn(null);
        when(fromCart.getEntries()).thenReturn(Arrays.asList(fromEntry1));

        when(toCart.getId()).thenReturn(22l);
        when(toCart.getCode()).thenReturn("toCart");
        when(toCart.getSellSignal()).thenReturn(SellSignal.ECOMMERCE_WEB.toString());
        when(toCart.getCompanyId()).thenReturn(2l);
        toEntries = new ArrayList<>();
        toEntries.add(toEntry1);
        when(toCart.getEntries()).thenReturn(toEntries);
    }

    @Test
    public void mergeCarts_hasNotFromCart() {
        strategy.mergeCarts(null, toCart);
        verify(cartService, times(0)).delete(fromCart);
        verify(cartService, times(0)).save(toCart);
        verify(commerceCartCalculationStrategy, times(0)).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void mergeCarts_hasNotToCart() {
        strategy.mergeCarts(fromCart, null);
        verify(cartService, times(0)).delete(fromCart);
        verify(cartService, times(0)).save(toCart);
        verify(commerceCartCalculationStrategy, times(0)).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void mergeCarts_FromCartNotCommerceSellSignal() {
        when(fromCart.getSellSignal()).thenReturn(SellSignal.WEB.toString());
        strategy.mergeCarts(fromCart, toCart);
        verify(cartService, times(0)).delete(fromCart);
        verify(cartService, times(0)).save(toCart);
        verify(commerceCartCalculationStrategy, times(0)).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void mergeCarts_toCartNotCommerceSellSignal() {
        when(toCart.getSellSignal()).thenReturn(SellSignal.WEB.toString());
        strategy.mergeCarts(fromCart, toCart);
        verify(cartService, times(0)).delete(fromCart);
        verify(cartService, times(0)).save(toCart);
        verify(commerceCartCalculationStrategy, times(0)).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void mergeCarts_fromCartTheSameToCart() {
        when(fromCart.getId()).thenReturn(1l);
        when(toCart.getId()).thenReturn(1l);
        strategy.mergeCarts(fromCart, toCart);
        verify(cartService, times(0)).delete(fromCart);
        verify(cartService, times(0)).save(toCart);
        verify(commerceCartCalculationStrategy, times(0)).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void mergeCarts_NotAcceptForAnonymousUser() {
        when(userService.getCurrentUserId()).thenReturn(null);
        strategy.mergeCarts(fromCart, toCart);
        verify(cartService, times(0)).delete(fromCart);
        verify(cartService, times(0)).save(toCart);
        verify(commerceCartCalculationStrategy, times(0)).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void mergeCarts_NotAcceptForDiffCompanyId() {
        when(fromCart.getCompanyId()).thenReturn(1l);
        when(toCart.getCompanyId()).thenReturn(3l);
        strategy.mergeCarts(fromCart, toCart);
        verify(cartService, times(0)).delete(fromCart);
        verify(cartService, times(0)).save(toCart);
        verify(commerceCartCalculationStrategy, times(0)).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void mergeCarts_NotAcceptForUserFromCart() {
        when(fromCart.getCreateByUser()).thenReturn(1l);
        strategy.mergeCarts(fromCart, toCart);
        verify(cartService, times(0)).delete(fromCart);
        verify(cartService, times(0)).save(toCart);
        verify(commerceCartCalculationStrategy, times(0)).recalculateCart(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void mergeCarts_NotExistedCandidateEntry() {
        when(entryMergeStrategy.getEntryToMerge(anyList(), eq(fromEntry1))).thenReturn(null);
        when(cartService.cloneEntry(fromEntry1, toCart)).thenReturn(cloneFromEntry1);

        strategy.mergeCarts(fromCart, toCart);
        assertEquals(2, toEntries.size());
        verify(cartService).cloneSubOrderEntries(fromEntry1, cloneFromEntry1);
        verify(modelService).save(cloneFromEntry1);
        verify(cartService, times(1)).delete(fromCart);
        verify(cartService, times(1)).save(toCart);
        verify(commerceCartCalculationStrategy, times(1)).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(commerceCartService, times(0)).updateQuantityForCartEntry(any(CommerceAbstractOrderParameter.class));
    }

    @Test
    public void mergeCarts_ExistedCandidateEntry() {
        when(entryMergeStrategy.getEntryToMerge(anyList(), eq(fromEntry1))).thenReturn(toEntry1);
        when(cartService.cloneEntry(fromEntry1, toCart)).thenReturn(cloneFromEntry1);
        when(fromEntry1.getQuantity()).thenReturn(1l);
        when(toEntry1.getQuantity()).thenReturn(2l);

        strategy.mergeCarts(fromCart, toCart);
        verify(cartService, times(0)).cloneSubOrderEntries(fromEntry1, cloneFromEntry1);
        verify(modelService, times(0)).save(cloneFromEntry1);
        verify(cartService, times(1)).delete(fromCart);
        verify(cartService, times(1)).save(toCart);
        verify(commerceCartCalculationStrategy, times(1)).recalculateCart(any(CommerceAbstractOrderParameter.class));
        verify(commerceCartService, times(1)).updateQuantityForCartEntry(captor.capture());
    }
}
