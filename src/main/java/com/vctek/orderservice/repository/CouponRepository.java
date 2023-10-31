package com.vctek.orderservice.repository;

import com.vctek.orderservice.couponservice.model.CouponModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CouponRepository extends JpaRepository<CouponModel, Long> {

    @Query(value = "SELECT  * FROM coupon WHERE company_id = ?1 AND promotion_source_rule_id is null", nativeQuery = true)
    List<CouponModel> findAllForQualifyingByCompanyId(Long companyId);

    @Query(value = "SELECT  * FROM coupon WHERE (company_id = ?1 AND promotion_source_rule_id is null) OR promotion_source_rule_id = ?2", nativeQuery = true)
    List<CouponModel> findAllForQualifyingByCompanyIdOrSourceRule(Long companyId, Long sourceRuleId);

    Page<CouponModel> findAllByCompanyId(Long companyId, Pageable pageable);

    Page<CouponModel> findAllByCompanyIdAndNameLike(Long companyId, String name, Pageable pageable);

    CouponModel findByIdAndCompanyId(Long couponId, Long companyId);
}
