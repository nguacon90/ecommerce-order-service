package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.OrderStatusImportRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusImportRequestValidator implements Validator<OrderStatusImportRequest> {
    private int maxOrderSize;

    @Override
    public void validate(OrderStatusImportRequest request) throws ServiceException {
        Long companyId = request.getCompanyId();
        if (companyId == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (request.getOrderStatus() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_STATUS;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (CollectionUtils.isEmpty(request.getOrderCodes())) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(request.getOrderCodes().size() > maxOrderSize) {
            ErrorCodes err = ErrorCodes.OVER_MAX_SUPPORTED_ORDER_SIZE_FOR_CHANGE_STATUS;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{maxOrderSize});
        }
    }

    @Value("${vctek.config.maxOrderSize:200}")
    public void setMaxOrderSize(int maxOrderSize) {
        this.maxOrderSize = maxOrderSize;
    }
}
