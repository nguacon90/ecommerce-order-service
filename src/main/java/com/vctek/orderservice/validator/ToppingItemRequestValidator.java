package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.ToppingItemRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.validate.Validator;
import org.springframework.stereotype.Component;

@Component
public class ToppingItemRequestValidator extends AbstractToppingValidator implements Validator<ToppingItemRequest> {

    @Override
    public void validate(ToppingItemRequest request) throws ServiceException {
        validateCompany(request.getCompanyId());
        if (request.getId() == null && request.getProductId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_PRODUCT_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        validateQuantity(request.getQuantity());
        if (request.getId() == null && request.getQuantity() < 1) {
            ErrorCodes err = ErrorCodes.INVALID_QUANTITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
