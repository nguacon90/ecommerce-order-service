package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.ComboData;
import com.vctek.orderservice.dto.request.AddSubOrderEntryRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.service.ProductService;
import com.vctek.util.OrderType;
import com.vctek.validate.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddSubOrderEntryRequestValidator implements Validator<AddSubOrderEntryRequest> {
    private ProductService productService;

    @Autowired
    public AddSubOrderEntryRequestValidator(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void validate(AddSubOrderEntryRequest request) throws ServiceException {
        if(request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if(request.getProductId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_PRODUCT_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if(request.getComboId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMBO_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        } else {
            ComboData comboData = productService.getCombo(request.getComboId(), request.getCompanyId());
            if (comboData == null) {
                ErrorCodes err = ErrorCodes.INVALID_COMBO_ID;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }

        if(StringUtils.isBlank(request.getOrderCode())) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(request.getEntryId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_ENTRY_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (request.getComboGroupNumber() == null && OrderType.ONLINE.name().equals(request.getOrderType())) {
            ErrorCodes err = ErrorCodes.EMPTY_COMBO_GROUP_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
