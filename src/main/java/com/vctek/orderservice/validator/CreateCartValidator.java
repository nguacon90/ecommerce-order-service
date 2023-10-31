package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CartInfoParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.util.PriceType;
import com.vctek.util.OrderType;
import com.vctek.validate.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CreateCartValidator extends AbstractCartValidator implements Validator<CartInfoParameter> {

    @Override
    public void validate(CartInfoParameter cartInfoParameter) throws ServiceException {
        if(cartInfoParameter.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(cartInfoParameter.getWarehouseId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_WAREHOUSE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        checkUserManageWarehouse(cartInfoParameter.getCompanyId(), cartInfoParameter.getWarehouseId());
        validateWarehouseStatus(cartInfoParameter.getWarehouseId(), cartInfoParameter.getCompanyId());
        OrderType orderType = OrderType.findByCode(cartInfoParameter.getOrderType());
        if(orderType == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_TYPE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (OrderType.ONLINE.toString().equals(cartInfoParameter.getOrderType())) {
            if (StringUtils.isEmpty(cartInfoParameter.getPriceType())) {
                ErrorCodes err = ErrorCodes.EMPTY_PRICE_TYPE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            PriceType type = PriceType.findByCode(cartInfoParameter.getPriceType());
            if(type == null) {
                ErrorCodes err = ErrorCodes.INVALID_PRICE_TYPE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }
}
