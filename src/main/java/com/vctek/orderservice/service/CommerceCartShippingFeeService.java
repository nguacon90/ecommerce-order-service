package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.request.storefront.ShippingFeeData;
import com.vctek.orderservice.dto.request.storefront.StoreFrontCheckoutRequest;
import com.vctek.orderservice.model.AbstractOrderModel;

public interface CommerceCartShippingFeeService {
    ShippingFeeData getValidateShippingFee(AbstractOrderModel model, StoreFrontCheckoutRequest request);
}
