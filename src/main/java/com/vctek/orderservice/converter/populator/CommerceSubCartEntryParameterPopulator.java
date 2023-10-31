package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.request.AddSubOrderEntryRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("commerceSubCartEntryParameterPopulator")
public class CommerceSubCartEntryParameterPopulator implements Populator<AddSubOrderEntryRequest, CommerceAbstractOrderParameter> {
    private CartService cartService;

    @Override
    public void populate(AddSubOrderEntryRequest source, CommerceAbstractOrderParameter target) {
        CartModel cart = cartService.findByCodeAndCompanyId(source.getOrderCode(), source.getCompanyId());
        if (cart == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        target.setCompanyId(cart.getCompanyId());
        target.setOrder(cart);
        target.setWarehouseId(cart.getWarehouseId());
        target.setProductId(source.getProductId());
        target.setEntryId(source.getEntryId());
        target.setComboId(source.getComboId());
        target.setComboGroupNumber(source.getComboGroupNumber());
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    public CartService getCartService() {
        return cartService;
    }
}
