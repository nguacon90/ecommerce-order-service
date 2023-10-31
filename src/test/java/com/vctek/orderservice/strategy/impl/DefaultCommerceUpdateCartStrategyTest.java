package com.vctek.orderservice.strategy.impl;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.ModelService;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultCommerceUpdateCartStrategyTest {
    private DefaultCommerceUpdateCartStrategy strategy;

    @Mock
    private CalculationService calculationService;
    @Mock
    private ModelService modelService;
    @Mock
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;
    private CommerceAbstractOrderParameter param = new CommerceAbstractOrderParameter();
    @Mock
    private CartModel cart;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        strategy = new DefaultCommerceUpdateCartStrategy();
        strategy.setCalculationService(calculationService);
        strategy.setModelService(modelService);
        strategy.setCommerceCartCalculationStrategy(commerceCartCalculationStrategy);
        param.setOrder(cart);
    }

    @Test
    public void updateCartDiscount() {
        when(modelService.save(cart)).thenReturn(cart);
        strategy.updateCartDiscount(param);
        verify(commerceCartCalculationStrategy).calculateCart(any());
    }

    @Test
    public void updateVat() {
        when(modelService.save(cart)).thenReturn(cart);
        strategy.updateVat(param);
        verify(calculationService).calculateVat(cart);
    }
}
