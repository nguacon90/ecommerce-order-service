package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateRequest;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductLoyaltyRewardRateService {

    List<ProductLoyaltyRewardRateModel> findAllByCompanyId(Long companyId);

    List<ProductLoyaltyRewardRateModel> createOrUpdate(LoyaltyRewardRateRequest loyaltyRewardRateRequest);

    void delete(LoyaltyRewardRateDetailRequest loyaltyRewardRateDetailRequest);

    Map<Long, Double> getRewardRateByProductIds(Set<Long> productIds, Long companyId, boolean isCombo);

    Page<ProductLoyaltyRewardRateModel> findAll(Pageable pageable);

    List<ProductLoyaltyRewardRateModel> findAllByCompanyIdAndProductIds(Long companyId, List<Long> productIds);
}
