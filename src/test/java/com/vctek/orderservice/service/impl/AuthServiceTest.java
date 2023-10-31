package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.feignclient.CompanyClient;
import com.vctek.orderservice.feignclient.UserClient;
import com.vctek.service.TokenStoreService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthServiceTest {
    @Mock
    private UserClient userClient;
    @Mock
    private CompanyClient companyClient;
    private AuthServiceImpl authService;
    @Mock
    private TokenStoreService tokenStoreService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        authService = new AuthServiceImpl(userClient, tokenStoreService);
        authService.setCompanyClient(companyClient);
    }

    @Test
    public void getUserById() {
        authService.getUserById(1l);
        verify(userClient).getUserById( 1l);
    }

    @Test
    public void getCurrentUser() {
        authService.getCurrentUserId();
        verify(tokenStoreService).getCurrentUserId();
    }

    @Test
    public void getAllWarehouseOfCurrentUser() {
        when(tokenStoreService.getCurrentUserId()).thenReturn(2L);
        authService.getAllWarehouseOfCurrentUser(2L);
        verify(userClient).getAllWarehouses(anyLong(), anyLong());
    }

    @Test
    public void getUserWarehouses() {
        authService.getUserWarehouses(2L, 2L);
        verify(userClient).getAllWarehouseByUser(anyLong(), anyLong());
    }

    @Test
    public void isAdminCompanyUser() {
        authService.isAdminCompanyUser();
        verify(userClient).isAdminCompanyUser();
    }

    @Test
    public void isCheckDeliveryDate() {
        authService.isCheckDeliveryDate(2L);
        verify(companyClient).getCompulsoryDeliveryDate(anyLong());
    }
}
