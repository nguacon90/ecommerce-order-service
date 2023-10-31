package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.ReturnOrderUpdateParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.ReturnOrderService;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReturnOrderUpdateParameterValidator implements Validator<ReturnOrderUpdateParameter> {
    private ReturnOrderService returnOrderService;

    @Override
    public void validate(ReturnOrderUpdateParameter parameter) throws ServiceException {
        Long companyId = parameter.getCompanyId();
        if(companyId == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        Long warehouseId = parameter.getWarehouseId();
        if(warehouseId == null) {
            ErrorCodes err = ErrorCodes.EMPTY_WAREHOUSE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }


        String exchangeOrderCode = parameter.getExchangeOrderCode();
        Long returnOrderId = parameter.getReturnOrderId();
        ReturnOrderModel returnOrder = returnOrderService.findByIdAndCompanyId(returnOrderId, companyId);
        if(returnOrder == null) {
            ErrorCodes err = ErrorCodes.INVALID_RETURN_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        OrderModel exchangeOrder = returnOrder.getExchangeOrder();
        if(exchangeOrder == null || exchangeOrder.getCode() == null || !exchangeOrder.getCode().equals(exchangeOrderCode)) {
            ErrorCodes err = ErrorCodes.INVALID_EXCHANGE_ORDER_CODE_IN_RETURN_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Autowired
    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }
}
