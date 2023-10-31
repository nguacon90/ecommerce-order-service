package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AbstractOrderEntryRepository<T extends AbstractOrderEntryModel> extends JpaRepository<T, Long>, JpaSpecificationExecutor {

}
