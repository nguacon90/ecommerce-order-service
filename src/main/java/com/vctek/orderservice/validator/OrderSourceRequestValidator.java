package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.OrderSourceRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.service.OrderSourceService;
import com.vctek.validate.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderSourceRequestValidator implements Validator<OrderSourceRequest> {
    private OrderSourceService service;

    @Autowired
    public OrderSourceRequestValidator(OrderSourceService service) {
        this.service = service;
    }


    @Override
    public void validate(OrderSourceRequest orderSourceRequest) {

        if(orderSourceRequest.getId() != null) {
            OrderSourceModel orderSourceModel = service.findById(orderSourceRequest.getId());
            if(orderSourceModel == null) {
                ErrorCodes err = ErrorCodes.INVALID_ORDER_SOURCE_ID;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
        if(StringUtils.isBlank(orderSourceRequest.getName())) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_SOURCE_NAME;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(orderSourceRequest.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
