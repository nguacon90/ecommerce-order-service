package com.vctek.orderservice.repository;

import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface CouponCodeRepository extends JpaRepository<CouponCodeModel, Long> {

    @Query(value = "SELECT * FROM coupon_code AS cc " +
            "JOIN coupon AS cp ON cc.coupon_id = cp.id " +
            "JOIN promotion_source_rule as psr ON cp.promotion_source_rule_id = psr.id " +
            "WHERE  UPPER(cc.code) = UPPER(?1) AND cp.company_id = ?2 AND psr.active = 1 " +
            "AND (psr.end_date is null or psr.end_date >= ?3);", nativeQuery = true)
    List<CouponCodeModel> findValidCoupon(String couponCode, Long companyId, Date date);

    List<CouponCodeModel> findAllByCoupon(CouponModel source);

    @Query(value = "SELECT * FROM coupon_code AS cc " +
            "JOIN coupon AS cp ON cc.coupon_id = cp.id " +
            "WHERE  UPPER(cc.code) = UPPER(?1) AND cp.company_id = ?2 LIMIT 1", nativeQuery = true)
    CouponCodeModel findOneBy(String couponCode, Long companyId);

    @Query(value = "SELECT * FROM coupon_code AS cc " +
            "JOIN coupon AS cp ON cc.coupon_id = cp.id " +
            "WHERE cc.id = ?1 AND cp.company_id = ?2 LIMIT 1", nativeQuery = true)
    CouponCodeModel findByIdAndCompanyId(Long id, Long companyId);

    @Query(value = "SELECT * FROM coupon_code AS cc LEFT JOIN customer_coupon AS cuc ON cuc.coupon_code_id = cc.id WHERE cc.coupon_id = ?1 AND cuc.coupon_code_id IS NULL ", nativeQuery = true)
    List<CouponCodeModel> findAllByCouponIdAndUnAssignUser(Long couponId);
}
