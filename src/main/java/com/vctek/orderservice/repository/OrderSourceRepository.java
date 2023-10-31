package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderSourceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderSourceRepository extends JpaRepository<OrderSourceModel, Long> {
    Optional<OrderSourceModel> findByIdAndCompanyId(Long orderSourceId, Long companyId);

    List<OrderSourceModel> findAllByCompanyIdOrderByOrderAsc(Long companyId);

    List<OrderSourceModel> findAllByIdIn(List<Long> ids);

}
