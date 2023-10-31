package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderStorefrontSetupModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStorefrontSetupRepository extends JpaRepository<OrderStorefrontSetupModel, Long> {
    OrderStorefrontSetupModel findByCompanyId(Long companyId);
}
