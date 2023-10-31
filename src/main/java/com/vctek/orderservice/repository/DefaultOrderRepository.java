package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.AbstractOrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefaultOrderRepository extends AbstractOrderRepository<AbstractOrderModel>, JpaRepository<AbstractOrderModel, Long> {
    @Query(nativeQuery = true, value = "select * from orders where code = ?1 and company_id = ?2")
    AbstractOrderModel findByOrderCodeAndCompanyId(String code, Long companyId);

    List<AbstractOrderModel> findAllByImagesIsNotNull();
}
