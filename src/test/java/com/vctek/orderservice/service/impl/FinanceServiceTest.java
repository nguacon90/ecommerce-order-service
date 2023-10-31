package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.feignclient.FinanceClient;
import com.vctek.orderservice.service.FinanceService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class FinanceServiceTest {
    @Mock
    private FinanceClient financeClient;
    private FinanceService financeService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        financeService = new FinanceServiceImpl(financeClient);
    }

    @Test
    public void getPaymentMethod() {
        financeService.getPaymentMethod(1l);
        verify(financeClient).getPaymentMethodData(1l);
    }

    @Test
    public void getPaymentMethodByCode() {
        financeService.getPaymentMethodByCode("code");
        verify(financeClient).getPaymentMethodDataByCode("code");
    }
}
