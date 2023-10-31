package com.vctek.orderservice.validator.storefront;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.AddressRequest;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.storefront.StoreFrontCheckoutRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.service.CommerceCartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Pattern;

public abstract class AbstractStorefrontShippingAddressValidator {
    protected String phoneRegex;

    protected void validateCartCode(StoreFrontCheckoutRequest request) {
        if(StringUtils.isBlank(request.getCode())) {
            ErrorCodes err = ErrorCodes.EMPTY_CART_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void validateCustomerInfo(StoreFrontCheckoutRequest request) {
        CustomerRequest customerRequest = request.getCustomer();
        if(customerRequest == null) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if (StringUtils.isBlank(customerRequest.getName())) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (customerRequest.getShippingAddress() == null) {
            ErrorCodes err = ErrorCodes.INVALID_SHIPPING_PROVINCE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        AddressRequest addressRequest = customerRequest.getShippingAddress();
        if (addressRequest.getProvinceId() == null) {
            ErrorCodes err = ErrorCodes.INVALID_SHIPPING_PROVINCE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if (addressRequest.getDistrictId() == null) {
            ErrorCodes err = ErrorCodes.INVALID_SHIPPING_DISTRICT_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if (StringUtils.isBlank(addressRequest.getAddressDetail())) {
            ErrorCodes err = ErrorCodes.EMPTY_SHIPPING_ADDRESS_DETAIL;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (StringUtils.isBlank(addressRequest.getPhone1())) {
            ErrorCodes err = ErrorCodes.EMPTY_SHIPPING_PHONE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if (!Pattern.compile(phoneRegex).matcher(addressRequest.getPhone1()).matches()) {
            ErrorCodes err = ErrorCodes.INVALID_SHIPPING_PHONE_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Value("${vctek.config.phoneRegex:^(09|03|07|08|05)+([0-9]{8})$}")
    public void setPhoneRegex(String phoneRegex) {
        this.phoneRegex = phoneRegex;
    }
}
