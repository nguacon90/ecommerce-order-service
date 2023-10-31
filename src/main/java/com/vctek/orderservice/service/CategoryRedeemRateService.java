package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.request.RedeemRateRequest;
import com.vctek.orderservice.model.CategoryRedeemRateModel;

import java.util.Collection;
import java.util.List;

public interface CategoryRedeemRateService {
    List<CategoryRedeemRateModel> createOrUpdate(RedeemRateRequest request);

    List<CategoryRedeemRateModel> findByCompanyId(Long companyId);

    CategoryRedeemRateModel findByCategoryIdAndCompanyId(Long categoryId, Long companyId);

    void delete(CategoryRedeemRateModel model);

    void deleteAll(Collection<CategoryRedeemRateModel> models);

    List<CategoryRedeemRateModel> findByCategoryInAndCompanyId(List<Long> categoryId, Long companyId);
}
