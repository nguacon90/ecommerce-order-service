package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.CategoryRedeemRateModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRedeemRateRepository extends LoyaltyRedeemRateRepository<CategoryRedeemRateModel>, JpaRepository<CategoryRedeemRateModel, Long> {
    CategoryRedeemRateModel findByCompanyIdAndCategoryId(Long companyId, Long categoryId);

    List<CategoryRedeemRateModel> findAllByCategoryIdInAndCompanyId(Collection<Long> categoryId, Long companyId);

    Optional<CategoryRedeemRateModel> findTopByCategoryIdAndAndCompanyId(Long categoryId, Long companyId);
}
