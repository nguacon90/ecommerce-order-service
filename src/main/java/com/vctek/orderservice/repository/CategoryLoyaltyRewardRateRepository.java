package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.CategoryLoyaltyRewardRateModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CategoryLoyaltyRewardRateRepository extends LoyaltyRewardRateRepository<CategoryLoyaltyRewardRateModel>, JpaRepository<CategoryLoyaltyRewardRateModel, Long> {
    List<CategoryLoyaltyRewardRateModel> findAllByCompanyId(Long companyId);

    CategoryLoyaltyRewardRateModel findByCompanyIdAndCategoryId(Long companyId, Long categoryId);

    List<CategoryLoyaltyRewardRateModel> findAllByCategoryIdInAndCompanyId(Collection<Long> categoryId, Long companyId);
}
