package com.vctek.orderservice.facade.impl;

import com.vctek.orderservice.dto.request.AvailablePointAmountRequest;
import com.vctek.orderservice.service.LoyaltyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoyaltyFacadeImplTest {

    private LoyaltyFacadeImpl facade;

    @Mock
    private LoyaltyService loyaltyService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        facade = new LoyaltyFacadeImpl();
        facade.setLoyaltyService(loyaltyService);
    }

    @Test
    public void computeAvailablePointAmountOfOrder() {
        when(loyaltyService.computeAvailablePointAmountOf(any(AvailablePointAmountRequest.class))).thenReturn(null);
        facade.computeAvailablePointAmountOfOrder(new AvailablePointAmountRequest());
        verify(loyaltyService).computeAvailablePointAmountOf(any(AvailablePointAmountRequest.class));
    }
}
