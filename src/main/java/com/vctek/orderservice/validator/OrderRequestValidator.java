package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.OrderRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.CartService;
import com.vctek.util.OrderType;
import org.springframework.stereotype.Component;

@Component("orderRequestValidator")
public class OrderRequestValidator extends AbstractOrderRequestValidator {
    private CartService cartService;


    public OrderRequestValidator(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    protected AbstractOrderModel getOrder(OrderRequest orderRequest) {
        return cartService.findByCodeAndUserIdAndCompanyId(orderRequest.getCode(), authService.getCurrentUserId(),
                orderRequest.getCompanyId());
    }

    @Override
    public void validate(OrderRequest orderRequest) {
        super.validate(orderRequest);
        if (OrderType.ONLINE.toString().equals(orderRequest.getOrderType())) {
            validateOnlineOrderRequest(orderRequest);
        }
    }

    private void validateOnlineOrderRequest(OrderRequest orderRequest) {
        if(orderRequest.getShippingCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_SHIPPING_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(orderRequest.getOrderSourceId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_SOURCE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public void validateEntry(AbstractOrderModel abstractOrderModel) {
        super.validateEntry(abstractOrderModel);
    }


}
