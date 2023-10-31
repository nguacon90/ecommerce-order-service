package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.request.CheckTotalSellingOfProductRequest;

import java.util.Map;

public interface CheckProductSellingFacade {
    Map<Long, Long> checkTotalSellingOfProduct(CheckTotalSellingOfProductRequest request);
}
