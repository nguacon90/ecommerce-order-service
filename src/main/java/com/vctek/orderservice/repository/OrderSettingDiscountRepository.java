package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.OrderSettingDiscountModel;
import com.vctek.orderservice.model.OrderSettingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderSettingDiscountRepository extends JpaRepository<OrderSettingDiscountModel, Long> {

    List<OrderSettingDiscountModel> findAllByCompanyIdAndProductIdInAndAndDeleted(Long companyId, List<Long> productIds, boolean deleted);

    List<OrderSettingDiscountModel> findAllByCompanyIdAndCategoryCodeInAndDeleted(Long companyId, List<String> categoryCodes, boolean deleted);

    OrderSettingDiscountModel findByCompanyIdAndId(Long companyId, Long id);

    OrderSettingDiscountModel findByCompanyIdAndProductIdAndDeleted(Long companyId, Long productId, boolean deleted);

    List<OrderSettingDiscountModel> findAllByCompanyIdAndCategoryCodeIsNotNullAndDeleted(Long companyId, boolean deleted);
}
