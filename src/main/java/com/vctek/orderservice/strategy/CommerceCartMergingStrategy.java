package com.vctek.orderservice.strategy;

import com.vctek.orderservice.model.CartModel;

public interface CommerceCartMergingStrategy {

    void mergeCarts(CartModel fromCart, CartModel toCart);
}
