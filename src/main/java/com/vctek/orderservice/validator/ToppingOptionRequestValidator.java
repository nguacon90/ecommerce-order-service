package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.ToppingOptionRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.validate.Validator;
import org.springframework.stereotype.Component;

@Component
public class ToppingOptionRequestValidator extends AbstractToppingValidator implements Validator<ToppingOptionRequest> {

    @Override
    public void validate(ToppingOptionRequest request) throws ServiceException {
        validateQuantity(request.getQuantity());
        validateSugarAndIce(request.getSugar(), request.getIce());
        if (request.getEntryId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_ENTRY_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        validateCompany(request.getCompanyId());
    }
}
