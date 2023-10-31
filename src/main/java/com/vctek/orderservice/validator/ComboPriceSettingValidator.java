package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.OrderSettingRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.validate.Validator;
import org.springframework.stereotype.Component;

@Component("comboPriceSettingValidator")
public class ComboPriceSettingValidator implements Validator<OrderSettingRequest> {

    @Override
    public void validate(OrderSettingRequest orderSettingRequest) {
        if(orderSettingRequest.getAmount() == null || orderSettingRequest.getAmount() < 0) {
            ErrorCodes err = ErrorCodes.INVALID_AMOUNT_OF_COMBO_PRICE_SETTING;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

}
