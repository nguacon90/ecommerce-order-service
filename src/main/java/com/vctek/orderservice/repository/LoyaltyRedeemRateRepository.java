package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.LoyaltyRedeemRateUseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface LoyaltyRedeemRateRepository<T extends LoyaltyRedeemRateUseModel> extends JpaRepository<T, Long>, JpaSpecificationExecutor {
    List<T> findAllByCompanyId(Long companyId);
}
