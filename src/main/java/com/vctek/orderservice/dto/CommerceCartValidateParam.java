package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.CartModel;

public class CommerceCartValidateParam {
    private CartModel cartModel;
    private boolean validateCoupon;

    public CommerceCartValidateParam(CartModel cartModel) {
        this.cartModel = cartModel;
    }

    public CartModel getCartModel() {
        return cartModel;
    }

    public void setCartModel(CartModel cartModel) {
        this.cartModel = cartModel;
    }

    public boolean isValidateCoupon() {
        return validateCoupon;
    }

    public void setValidateCoupon(boolean validateCoupon) {
        this.validateCoupon = validateCoupon;
    }
}
