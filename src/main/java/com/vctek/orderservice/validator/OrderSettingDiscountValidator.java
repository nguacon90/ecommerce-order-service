package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderSettingDiscountData;
import com.vctek.orderservice.dto.request.OrderSettingRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.validate.Validator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderSettingDiscountValidator implements Validator<OrderSettingRequest> {
    @Override
    public void validate(OrderSettingRequest request) throws ServiceException {
        if (request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        for (OrderSettingDiscountData setting : request.getSettingDiscountData()) {
            if (setting.getDiscount() == null || setting.getDiscount() < 0) {
                ErrorCodes err = ErrorCodes.INVALID_DISCOUNT;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            CurrencyType discountType = CurrencyType.findByCode(setting.getDiscountType());
            if(discountType == null) {
                ErrorCodes err = ErrorCodes.INVALID_DISCOUNT_TYPE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }
}
