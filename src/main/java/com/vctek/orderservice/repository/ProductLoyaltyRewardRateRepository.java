package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductLoyaltyRewardRateRepository extends LoyaltyRewardRateRepository<ProductLoyaltyRewardRateModel>, JpaRepository<ProductLoyaltyRewardRateModel, Long> {
    List<ProductLoyaltyRewardRateModel> findAllByCompanyId(Long companyId);

    ProductLoyaltyRewardRateModel findByCompanyIdAndProductId(Long companyId, Long productId);

    List<ProductLoyaltyRewardRateModel> findAllByCompanyIdAndProductIdIn(Long companyId, Collection<Long> productIds);
}
