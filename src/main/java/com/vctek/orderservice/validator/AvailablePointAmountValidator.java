package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.AvailablePointAmountRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.validate.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class AvailablePointAmountValidator implements Validator<AvailablePointAmountRequest> {

    @Override
    public void validate(AvailablePointAmountRequest request) throws ServiceException {
        if (StringUtils.isEmpty(request.getCardNumber())) {
            ErrorCodes err = ErrorCodes.EMPTY_LOYALTY_CARD_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if (StringUtils.isEmpty(request.getOrderCode())) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
