package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.request.CheckTotalSellingOfProductRequest;

public interface CheckProductSellingService {
    Long countTotalInWholeSaleAndRetail(CheckTotalSellingOfProductRequest request, Long productId);

    Long countTotalInOnline(CheckTotalSellingOfProductRequest request, Long productId);
}
