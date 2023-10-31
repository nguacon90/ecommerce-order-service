package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.LoyaltyRewardRateModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface LoyaltyRewardRateRepository<T extends LoyaltyRewardRateModel> extends JpaRepository<T, Long>, JpaSpecificationExecutor {

}
