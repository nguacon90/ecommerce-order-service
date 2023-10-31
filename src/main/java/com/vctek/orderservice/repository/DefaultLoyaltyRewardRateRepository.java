package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.DefaultLoyaltyRewardRateModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DefaultLoyaltyRewardRateRepository extends LoyaltyRewardRateRepository<DefaultLoyaltyRewardRateModel>, JpaRepository<DefaultLoyaltyRewardRateModel, Long> {
    DefaultLoyaltyRewardRateModel findByCompanyId(Long companyId);
}
