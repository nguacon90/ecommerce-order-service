package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.feignclient.AddressClient;
import com.vctek.orderservice.service.CustomerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

public class CRMServiceTest {
    @Mock
    private CustomerService customerService;
    @Mock
    private AddressClient addressClient;
    private CRMServiceImpl crmService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        crmService = new CRMServiceImpl(customerService);
        crmService.setAddressClient(addressClient);
    }

    @Test
    public void getCustomer() {
        crmService.getCustomer(1l, 1l);
        verify(customerService).getCustomerById( 1l, 1l);
    }

    @Test
    public void getAddress() {
        crmService.getAddress( 1l);
        verify(addressClient).getAddress(1l);
    }

    @Test
    public void getPrintSettingById() {
        crmService.getPrintSettingById( 1l, 2l);
        verify(addressClient).getPrintSettingById(1l, 2L);
    }

    @Test
    public void getBasicCustomerInfo() {
        crmService.getBasicCustomerInfo( 1l, 2l);
        verify(customerService).getBasicCustomerInfo(1l, 2L);
    }

    @Test
    public void getProvinceById() {
        crmService.getProvinceById( 1l);
        verify(addressClient).getProvinceById(anyLong());
    }

    @Test
    public void getDistrictById() {
        crmService.getDistrictById( 1l);
        verify(addressClient).getDistrictById(anyLong());
    }

    @Test
    public void getWardById() {
        crmService.getWardById( 1l);
        verify(addressClient).getWardById(anyLong());
    }
}
