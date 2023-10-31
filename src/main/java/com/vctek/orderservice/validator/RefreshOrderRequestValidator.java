package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.RefreshCartRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.validate.Validator;
import org.springframework.stereotype.Component;

@Component
public class RefreshOrderRequestValidator extends AbstractCartValidator implements Validator<RefreshCartRequest> {

    @Override
    public void validate(RefreshCartRequest refreshCartRequest) throws ServiceException {
        if(refreshCartRequest.getOldCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(refreshCartRequest.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(refreshCartRequest.getWarehouseId() != null) {
            checkUserManageWarehouse(refreshCartRequest.getCompanyId(), refreshCartRequest.getWarehouseId());
            validateWarehouseStatus(refreshCartRequest.getWarehouseId(), refreshCartRequest.getCompanyId());
        }
    }
}
