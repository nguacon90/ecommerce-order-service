package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.ReturnOrderService;
import com.vctek.validate.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("updateReturnOrderRequestValidator")
public class UpdateReturnOrderRequestValidator extends ReturnOrderRequestValidator implements Validator<ReturnOrderRequest> {
    private ReturnOrderService returnOrderService;

    @Override
    public void validate(ReturnOrderRequest returnOrderRequest) throws ServiceException {
        if(returnOrderRequest.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if(returnOrderRequest.getId() == null) {
            ErrorCodes err = ErrorCodes.INVALID_RETURN_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        ReturnOrderModel returnOrderModel = returnOrderService.findByIdAndCompanyId(returnOrderRequest.getId(),
                returnOrderRequest.getCompanyId());
        if (returnOrderModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_RETURN_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if ((returnOrderRequest.getShippingFee() != null && returnOrderRequest.getShippingFee() < 0)
                || (returnOrderRequest.getCompanyShippingFee() != null && returnOrderRequest.getCompanyShippingFee() < 0)
                || (returnOrderRequest.getCollaboratorShippingFee() != null && returnOrderRequest.getCollaboratorShippingFee() < 0)) {
            ErrorCodes err = ErrorCodes.SHIPPING_FEE_MUST_BE_LARGE_ZERO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(StringUtils.isNotBlank(returnOrderRequest.getNote()) &&
                returnOrderRequest.getNote().length() > AbstractOrderRequestValidator.MAXIMUM_NOTE_LENGTH) {
            ErrorCodes err = ErrorCodes.NOTE_OVER_MAX_LENGTH;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (returnOrderModel.getExchangeOrder() != null) {
            validateComboExchange(returnOrderModel.getExchangeOrder());
        }
    }

    @Autowired
    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }
}
