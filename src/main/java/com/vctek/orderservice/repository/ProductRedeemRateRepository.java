package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.ProductRedeemRateModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRedeemRateRepository extends LoyaltyRedeemRateRepository<ProductRedeemRateModel>, JpaRepository<ProductRedeemRateModel, Long> {
    ProductRedeemRateModel findByCompanyIdAndProductId(Long companyId, Long productId);

    List<ProductRedeemRateModel> findAllByCompanyIdAndProductIdIn(Long companyId, List<Long> productId);
}
