package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.RedeemRateRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class RedeemRateRequestValidator implements Validator<RedeemRateRequest> {
    @Override
    public void validate(RedeemRateRequest request) throws ServiceException {
        if(request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(CollectionUtils.isEmpty(request.getListId())) {
            ErrorCodes err = ErrorCodes.EMPTY_PRODUCT_OR_CATEGORY_DATA;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
