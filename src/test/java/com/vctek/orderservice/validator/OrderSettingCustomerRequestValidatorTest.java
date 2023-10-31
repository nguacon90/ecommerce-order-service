package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.OrderSettingCustomerRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.service.OrderSettingCustomerService;
import com.vctek.util.OrderType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OrderSettingCustomerRequestValidatorTest {
    private OrderSettingCustomerRequestValidator validator;
    @Mock
    private OrderSettingCustomerService service;

    private OrderSettingCustomerRequest request = new OrderSettingCustomerRequest();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new OrderSettingCustomerRequestValidator();
        validator.setService(service);

    }

    @Test
    public void validateCompanyId_null() {
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.EMPTY_COMPANY_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validatePriority_null() {
        try {
            request.setCompanyId(2l);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.EMPTY_ORDER_SETTING_CUSTOMER_PRIORITY.message(), e.getMessage());
        }
    }

    @Test
    public void validatePriority_invalid() {
        try {
            request.setCompanyId(2l);
            request.setPriority(-1);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.INVALID_ORDER_SETTING_CUSTOMER_PRIORITY.message(), e.getMessage());
        }
    }

    @Test
    public void validateName_empty() {
        try {
            request.setPriority(12);
            request.setCompanyId(2l);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.EMPTY_ORDER_SETTING_CUSTOMER_NAME.message(), e.getMessage());
        }
    }

    @Test
    public void validateName_max_length_100() {
        try {
            request.setPriority(12);
            request.setCompanyId(2l);
            request.setName("Độ tuổi sẽ coi là cài đặt mặc định. Có thể cài đặt ẩn hoặc hiển thị độ tuổi cho các loại hoá đơn. loại hoá đơn.");
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.OVER_MAX_LENGTH_100.message(), e.getMessage());
        }
    }

    @Test
    public void validateInvalidOrderType() {
        try {
            request.setPriority(12);
            request.setCompanyId(2l);
            request.setName("Độ tuổi sẽ");
            request.setOrderTypes(Arrays.asList("type"));
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.INVALID_ORDER_TYPE.message(), e.getMessage());
        }
    }

    @Test
    public void validateInvalid_uniqueName() {
        try {
            request.setPriority(12);
            request.setCompanyId(2l);
            request.setName("Độ tuổi sẽ");
            request.setOrderTypes(Arrays.asList(OrderType.ONLINE.toString()));
            OrderSettingCustomerModel settingCustomerModel = new OrderSettingCustomerModel();
            when(service.findAllByNameAndCompanyId(anyString(), anyLong())).thenReturn(Arrays.asList(settingCustomerModel));
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.UNIQUE_ORDER_SETTING_CUSTOMER_NAME.message(), e.getMessage());
        }
    }

    @Test
    public void validateInvalid_model() {
        try {
            request.setId(12L);
            request.setPriority(12);
            request.setCompanyId(2l);
            request.setName("Độ tuổi sẽ");
            request.setOrderTypes(Arrays.asList(OrderType.ONLINE.toString()));
            OrderSettingCustomerModel settingCustomerModel = new OrderSettingCustomerModel();
            settingCustomerModel.setId(12L);
            when(service.findAllByNameAndCompanyId(anyString(), anyLong())).thenReturn(Arrays.asList(settingCustomerModel));
            when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.INVALID_ORDER_SETTING_CUSTOMER.message(), e.getMessage());
        }
    }

    @Test
    public void validateInvalid_emptyOptions() {
        try {
            request.setId(12L);
            request.setPriority(12);
            request.setCompanyId(2l);
            request.setName("Độ tuổi sẽ");
            request.setOrderTypes(Arrays.asList(OrderType.ONLINE.toString()));
            OrderSettingCustomerModel settingCustomerModel = new OrderSettingCustomerModel();
            when(service.findAllByNameAndCompanyId(anyString(), anyLong())).thenReturn(new ArrayList<>());
            when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(settingCustomerModel);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.CANNOT_EMPTY_ORDER_SETTING_CUSTOMER_OPTIONS.message(), e.getMessage());
        }
    }

    @Test
    public void validateInvalid_emptyOption_name() {
        try {
            request.setId(12L);
            request.setPriority(12);
            request.setCompanyId(2l);
            request.setName("Độ tuổi sẽ");
            request.setOrderTypes(Arrays.asList(OrderType.ONLINE.toString()));
            List<OrderSettingCustomerRequest> optionRequests = new ArrayList<>();
            OrderSettingCustomerRequest optionRequest = new OrderSettingCustomerRequest();
            optionRequests.add(optionRequest);
            request.setOptions(optionRequests);
            OrderSettingCustomerModel settingCustomerModel = new OrderSettingCustomerModel();
            when(service.findAllByNameAndCompanyId(anyString(), anyLong())).thenReturn(new ArrayList<>());
            when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(settingCustomerModel);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.EMPTY_ORDER_SETTING_CUSTOMER_OPTION_NAME.message(), e.getMessage());
        }
    }

    @Test
    public void validateInvalid_uniqueOptionName_create() {
        try {
            request.setId(12L);
            request.setPriority(12);
            request.setCompanyId(2l);
            request.setName("Độ tuổi sẽ");
            request.setOrderTypes(Arrays.asList(OrderType.ONLINE.toString()));
            List<OrderSettingCustomerRequest> optionRequests = new ArrayList<>();
            OrderSettingCustomerRequest optionRequest = new OrderSettingCustomerRequest();
            optionRequest.setName("option name");
            optionRequests.add(optionRequest);
            OrderSettingCustomerRequest optionRequest2 = new OrderSettingCustomerRequest();
            optionRequest2.setName("option name");
            optionRequests.add(optionRequest2);
            request.setOptions(optionRequests);
            OrderSettingCustomerModel settingCustomerModel = new OrderSettingCustomerModel();
            when(service.findAllByNameAndCompanyId(anyString(), anyLong())).thenReturn(new ArrayList<>());
            when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(settingCustomerModel);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.UNIQUE_ORDER_SETTING_CUSTOMER_OPTION_NAME.message(), e.getMessage());
        }
    }

    @Test
    public void validate() {
        request.setId(12L);
        request.setPriority(12);
        request.setCompanyId(2l);
        request.setName("Độ tuổi sẽ");
        request.setOrderTypes(Arrays.asList(OrderType.ONLINE.toString()));
        List<OrderSettingCustomerRequest> optionRequests = new ArrayList<>();
        OrderSettingCustomerRequest optionRequest = new OrderSettingCustomerRequest();
        optionRequest.setName("option name");
        optionRequests.add(optionRequest);
        request.setOptions(optionRequests);
        OrderSettingCustomerModel settingCustomerModel = new OrderSettingCustomerModel();
        when(service.findAllByNameAndCompanyId(anyString(), anyLong())).thenReturn(new ArrayList<>());
        when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(settingCustomerModel);
        validator.validate(request);
        verify(service, times(1)).findAllByNameAndCompanyId(anyString(), anyLong());
        verify(service, times(1)).findByIdAndCompanyId(anyLong(), anyLong());
    }
}
