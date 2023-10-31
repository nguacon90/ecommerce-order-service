package com.vctek.orderservice.facade;

import com.vctek.orderservice.couponservice.couponcodegeneration.dto.CouponCodeConfiguration;
import com.vctek.orderservice.dto.CouponCodeData;
import com.vctek.orderservice.dto.CouponData;
import com.vctek.orderservice.dto.request.CouponRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CouponFacade {
    List<CouponCodeData> generateCouponCodes(CouponCodeConfiguration configuration);

    CouponData create(CouponRequest request);

    List<CouponData> findAllForQualifying(Long companyId, Long sourceRuleId);

    Page<CouponData> findAllBy(Long companyId, String name, Pageable pageable);

    CouponData getDetail(Long couponId, Long companyId);

    CouponData update(CouponRequest request);

    void remove(Long couponId, Long companyId);

    byte[] exportExcel(Long companyId);
}
