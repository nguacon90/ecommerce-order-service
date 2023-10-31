package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderSettingDiscountData;
import com.vctek.orderservice.dto.request.OrderSettingRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

public class OrderSettingDiscountValidatorTest {
    private OrderSettingDiscountValidator validator;
    private OrderSettingRequest request = new OrderSettingRequest();
    OrderSettingDiscountData data = new OrderSettingDiscountData();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new OrderSettingDiscountValidator();
        data = new OrderSettingDiscountData();
        data.setCompanyId(2l);
        data.setProductId(12l);
        List<OrderSettingDiscountData> dataList = new ArrayList<>();
        dataList.add(data);
        request.setSettingDiscountData(dataList);
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
    public void validateDiscount_null() {
        try {
            request.setCompanyId(2l);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.INVALID_DISCOUNT.message(), e.getMessage());
        }
    }

    @Test
    public void validate_Invalid_DiscountType() {
        try {
            data.setDiscount(12d);
            request.setCompanyId(2l);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCodes.INVALID_DISCOUNT_TYPE.message(), e.getMessage());
        }
    }
}
