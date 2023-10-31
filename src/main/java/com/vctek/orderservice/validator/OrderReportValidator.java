package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.OrderReportRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.validate.Validator;
import org.springframework.stereotype.Component;

@Component("orderReportValidator")
public class OrderReportValidator implements Validator<OrderReportRequest> {
    @Override
    public void validate(OrderReportRequest orderReportRequest) throws ServiceException {
        if(orderReportRequest.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if(orderReportRequest.getFromDate() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_FROM_DATE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
