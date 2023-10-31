package com.vctek.orderservice.strategy.impl;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.service.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class DefaultCommerceRemoveEntriesStrategyTest {
    private DefaultCommerceRemoveEntriesStrategy strategy;

    @Mock
    private ModelService modelService;
    private CommerceAbstractOrderParameter param = new CommerceAbstractOrderParameter();
    private CartModel cart = new CartModel();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        strategy = new DefaultCommerceRemoveEntriesStrategy(modelService);
        param.setOrder(cart);
    }

    @Test
    public void removeAllEntries() {
        strategy.removeAllEntries(param);
        verify(modelService).save(any(CartModel.class));
    }
}
