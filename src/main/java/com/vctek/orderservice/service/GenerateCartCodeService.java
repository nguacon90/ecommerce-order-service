package com.vctek.orderservice.service;

import com.vctek.orderservice.model.AbstractOrderModel;

public interface GenerateCartCodeService {
    String generateCartCode(AbstractOrderModel newCart);
}
