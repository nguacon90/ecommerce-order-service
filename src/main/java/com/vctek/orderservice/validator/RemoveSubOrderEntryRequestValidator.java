package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.RemoveSubOrderEntryRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.validate.Validator;
import org.springframework.stereotype.Component;

@Component
public class RemoveSubOrderEntryRequestValidator implements Validator<RemoveSubOrderEntryRequest> {

    @Override
    public void validate(RemoveSubOrderEntryRequest request) throws ServiceException {
        if(request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if(request.getSubEntryId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_SUB_ORDER_ENTRY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
