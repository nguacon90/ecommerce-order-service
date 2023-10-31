package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.dto.CommerceCheckoutParameter;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.OrderRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.dto.request.VatRequest;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.CartService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class CommerceCheckoutPopulatorTest {
    @Mock
    private CartService cartService;
    @Mock
    private  AuthService authService;

    private CommerceCheckoutPopulator populator;
    @Mock
    private OrderRequest orderRequest;
    private CommerceCheckoutParameter param = new CommerceCheckoutParameter();
    private CartModel cart = new CartModel();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new CommerceCheckoutPopulator(cartService, authService);
        when(authService.getCurrentUserId()).thenReturn(1l);
        when(orderRequest.getCode()).thenReturn("code");
    }

    @Test
    public void populate() {
        CustomerRequest customer = new CustomerRequest();
        customer.setPhone("09782905");
        customer.setGender("FEMALE");
        customer.setAge("MIDDLE_AGE");
        List<Long> settingCustomerOptionIds = new ArrayList<>();
        settingCustomerOptionIds.add(1l);
        settingCustomerOptionIds.add(2l);
        when(orderRequest.getSettingCustomerOptionIds()).thenReturn(settingCustomerOptionIds);
        when(orderRequest.getCustomer()).thenReturn(customer);
        when(orderRequest.getPayments()).thenReturn(Arrays.asList(new PaymentTransactionRequest()));
        when(orderRequest.getVatInfo()).thenReturn(new VatRequest());
        when(orderRequest.getAge()).thenReturn("CHILDREN");
        when(orderRequest.getGender()).thenReturn("MALE");
        when(cartService.findByCodeAndUserIdAndCompanyId(anyString(), anyLong(), anyLong())).thenReturn(cart);

        populator.populate(orderRequest, param);
        assertEquals("09782905", param.getCustomerRequest().getPhone());
        assertEquals("09782905", param.getCustomerRequest().getName());
        assertEquals("09782905", param.getCustomerRequest().getPhone1());
        assertEquals("FEMALE", param.getCustomerRequest().getGender());
        assertEquals("CHILDREN", param.getCustomerRequest().getAge());

        assertEquals("CHILDREN", param.getAge());
        assertEquals("MALE", param.getGender());
        assertEquals(settingCustomerOptionIds, param.getSettingCustomerOptionIds());

    }
}
