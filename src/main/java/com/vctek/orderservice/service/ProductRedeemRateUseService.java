package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.request.RedeemRateRequest;
import com.vctek.orderservice.model.ProductRedeemRateModel;

import java.util.List;
import java.util.Map;

public interface ProductRedeemRateUseService {
    List<ProductRedeemRateModel> createOrUpdate(RedeemRateRequest request);

    List<ProductRedeemRateModel> findByCompanyId(Long companyId);

    ProductRedeemRateModel findByProductIdAndCompanyId(Long productId, Long companyId);

    void delete(ProductRedeemRateModel model);

    Map<Long, Boolean> productCanRedeem(Long companyId, List<Long> products);
}
