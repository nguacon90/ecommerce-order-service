package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.util.SugarAndIce;

public abstract class AbstractToppingValidator {

    protected void validateCompany(Long companyId) {
        if (companyId == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 0) {
            ErrorCodes err = ErrorCodes.INVALID_QUANTITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void validateSugarAndIce(Integer sugar, Integer ice) {
        if (sugar == null) {
            ErrorCodes err = ErrorCodes.EMPTY_PERCENT_SUGAR;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        } else {
            SugarAndIce percentSugar = SugarAndIce.findByValue(sugar);
            if (percentSugar == null) {
                ErrorCodes err = ErrorCodes.INVALID_PERCENT_SUGAR;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }

        if (ice == null) {
            ErrorCodes err = ErrorCodes.EMPTY_PERCENT_ICE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        } else {
            SugarAndIce percentIce = SugarAndIce.findByValue(ice);
            if (percentIce == null) {
                ErrorCodes err = ErrorCodes.INVALID_PERCENT_ICE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }
}
