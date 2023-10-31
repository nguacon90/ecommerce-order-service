package com.vctek.orderservice.validator.storefront;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.storefront.StoreFrontCheckoutRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("storefrontShippingAddressValidator")
public class StorefrontShippingAddressValidator extends AbstractStorefrontShippingAddressValidator implements Validator<StoreFrontCheckoutRequest> {
    private OrderService orderService;

    @Override
    public void validate(StoreFrontCheckoutRequest request) throws ServiceException {
        validateCartCode(request);
        OrderModel model = orderService.findByCodeAndCompanyId(request.getCode(), request.getCompanyId());;
        if(model == null) {
            ErrorCodes err = ErrorCodes.NOT_EXISTED_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        validateCustomerInfo(request);
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
