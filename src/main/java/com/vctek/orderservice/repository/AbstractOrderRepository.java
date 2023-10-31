package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.AbstractOrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AbstractOrderRepository<T extends AbstractOrderModel> extends JpaRepository<T, Long>, JpaSpecificationExecutor {

}
