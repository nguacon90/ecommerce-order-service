package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.dto.CartInfoParameter;
import com.vctek.orderservice.dto.MiniCartData;
import com.vctek.orderservice.facade.MiniCartFacade;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.util.SellSignal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MiniCartFacadeImpl implements MiniCartFacade {
    private AuthService authService;
    private CartService cartService;
    private Converter<CartModel, MiniCartData> miniCartDataConverter;

    public MiniCartFacadeImpl(AuthService authService, CartService cartService,
                              @Qualifier("miniCartDataConverter") Converter<CartModel, MiniCartData> miniCartDataConverter) {
        this.authService = authService;
        this.cartService = cartService;
        this.miniCartDataConverter = miniCartDataConverter;
    }

    @Override
    public List<MiniCartData> findAllByUser(CartInfoParameter cartInfoParameter) {
        cartInfoParameter.setUserId(authService.getCurrentUserId());
        cartInfoParameter.setSellSignal(SellSignal.WEB.toString());
        List<CartModel> allCarts = cartService.findAllOrCreateNewByCreatedByUser(cartInfoParameter);
        return miniCartDataConverter.convertAll(allCarts);
    }
}
