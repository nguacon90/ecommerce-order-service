package com.vctek.orderservice.service.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.AddressRequest;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.storefront.ShippingFeeData;
import com.vctek.orderservice.dto.request.storefront.ShippingFeeRequest;
import com.vctek.orderservice.dto.request.storefront.StoreFrontCheckoutRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.service.LogisticService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CommerceCartShippingFeeServiceImplTest {
    private CommerceCartShippingFeeServiceImpl service;
    private CartModel model;
    private StoreFrontCheckoutRequest request;
    @Mock
    private LogisticService logisticService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new CommerceCartShippingFeeServiceImpl();
        service.setLogisticService(logisticService);
        request = new StoreFrontCheckoutRequest();
        request.setDeliveryCost(2000d);
        CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setPhone("090909878");
        customerRequest.setExternalId(2L);
        customerRequest.setName("name");
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setProvinceId(2L);
        addressRequest.setDistrictId(2L);
        addressRequest.setWardId(2L);
        customerRequest.setShippingAddress(addressRequest);
        request.setCustomer(customerRequest);
        List<AbstractOrderEntryModel> entryModels = new ArrayList<>();
        AbstractOrderEntryModel entry = new AbstractOrderEntryModel();
        entry.setProductId(2L);
        entry.setQuantity(2L);
        AbstractOrderEntryModel entry2 = new AbstractOrderEntryModel();
        Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
        SubOrderEntryModel sub = new SubOrderEntryModel();
        sub.setProductId(2L);
        sub.setQuantity(3);
        subOrderEntryModels.add(sub);
        entry2.setSubOrderEntries(subOrderEntryModels);
        entryModels.add(entry);
        entryModels.add(entry2);
        model = new CartModel();
        model.setId(1L);
        model.setCompanyId(2L);
        model.setEntries(entryModels);
    }

    @Test
    public void getValidateShippingFee_null() {
        when(logisticService.getStoreFrontShippingFee(any(ShippingFeeRequest.class))).thenReturn(new ArrayList<>());
        ShippingFeeData data = service.getValidateShippingFee(model, request);
        assertNull(data);
    }

    @Test
    public void getValidateShippingFee_ShippingSettingIdNotNull_NotEmptyShippingFeeData() {
        try {
            request.setShippingFeeSettingId(2L);
            when(logisticService.getStoreFrontShippingFee(any(ShippingFeeRequest.class))).thenReturn(new ArrayList<>());
            service.getValidateShippingFee(model, request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.DELIVERY_COST_DIFFERENT_SETTING_SHIPPING_FEE.message(), e.getMessage());
        }
    }

    @Test
    public void getValidateShippingFee_ShippingSettingIdNull_EmptyShippingFeeData() {
        try {
            when(logisticService.getStoreFrontShippingFee(any(ShippingFeeRequest.class))).thenReturn(Arrays.asList(new ShippingFeeData()));
            service.getValidateShippingFee(model, request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.DELIVERY_COST_DIFFERENT_SETTING_SHIPPING_FEE.message(), e.getMessage());
        }
    }

    @Test
    public void getValidateShippingFee_notExistShippingFeeData() {
        try {
            List<ShippingFeeData> dataList = new ArrayList<>();
            ShippingFeeData data = new ShippingFeeData();
            data.setShippingFeeSettingId(2L);
            dataList.add(data);
            request.setShippingFeeSettingId(3L);
            when(logisticService.getStoreFrontShippingFee(any(ShippingFeeRequest.class))).thenReturn(dataList);
            service.getValidateShippingFee(model, request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.DELIVERY_COST_DIFFERENT_SETTING_SHIPPING_FEE.message(), e.getMessage());
        }
    }

    @Test
    public void getValidateShippingFee_DeliveryCost_NotEquals_ShippingFee() {
        try {
            List<ShippingFeeData> dataList = new ArrayList<>();
            ShippingFeeData data = new ShippingFeeData();
            data.setShippingFeeSettingId(2L);
            data.setShippingFee(20000d);
            dataList.add(data);
            request.setShippingFeeSettingId(2L);
            request.setDeliveryCost(15000d);
            when(logisticService.getStoreFrontShippingFee(any(ShippingFeeRequest.class))).thenReturn(dataList);
            service.getValidateShippingFee(model, request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.DELIVERY_COST_DIFFERENT_SETTING_SHIPPING_FEE.message(), e.getMessage());
        }
    }

    @Test
    public void getValidateShippingFee() {
        List<ShippingFeeData> dataList = new ArrayList<>();
        ShippingFeeData data = new ShippingFeeData();
        data.setShippingFeeSettingId(2L);
        data.setShippingFee(20000d);
        dataList.add(data);
        request.setShippingFeeSettingId(2L);
        request.setDeliveryCost(20000d);
        when(logisticService.getStoreFrontShippingFee(any(ShippingFeeRequest.class))).thenReturn(dataList);
        ShippingFeeData shippingFeeData = service.getValidateShippingFee(model, request);
        assertEquals(20000d, shippingFeeData.getShippingFee(), 0);
    }
}
