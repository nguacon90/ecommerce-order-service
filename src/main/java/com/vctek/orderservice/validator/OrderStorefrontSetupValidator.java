package com.vctek.orderservice.validator;

import com.vctek.dto.redis.OrderStorefrontSetupData;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.service.OrderSourceService;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderStorefrontSetupValidator implements Validator<OrderStorefrontSetupData> {
    private OrderSourceService orderSourceService;

    @Override
    public void validate(OrderStorefrontSetupData request) throws ServiceException {
        if (request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (request.getWarehouseId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_WAREHOUSE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        validateOrderSource(request);
    }

    private void validateOrderSource(OrderStorefrontSetupData request) {
        if (request.getOrderSourceId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_SOURCE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        OrderSourceModel orderSourceModel = orderSourceService.findByIdAndCompanyId(request.getOrderSourceId(), request.getCompanyId());
        if (orderSourceModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_SOURCE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Autowired
    public void setOrderSourceService(OrderSourceService orderSourceService) {
        this.orderSourceService = orderSourceService;
    }
}
