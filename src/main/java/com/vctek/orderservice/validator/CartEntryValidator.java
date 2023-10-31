package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderEntryDTO;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.util.OrderType;
import com.vctek.validate.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component("cartEntryValidator")
public class CartEntryValidator extends AbstractCartValidator implements Validator<OrderEntryDTO> {

    @Override
    public void validate(OrderEntryDTO orderEntryDTO) throws ServiceException {
        validateCommonProperties(orderEntryDTO);

        if (orderEntryDTO.getDiscount() != null) {
            if (orderEntryDTO.getDiscount() < 0) {
                ErrorCodes err = ErrorCodes.INVALID_DISCOUNT;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            CurrencyType currencyType = CurrencyType.findByCode(orderEntryDTO.getDiscountType());
            if (currencyType == null) {
                ErrorCodes err = ErrorCodes.INVALID_DISCOUNT_TYPE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }

        validateOrderType(orderEntryDTO);
    }

    protected void validateOrderType(OrderEntryDTO orderEntryDTO) {
        OrderType orderType = OrderType.findByCode(orderEntryDTO.getOrderType());
        if (orderType == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_TYPE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
