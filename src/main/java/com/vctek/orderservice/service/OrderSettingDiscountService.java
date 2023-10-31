package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.OrderDiscountSettingMapper;
import com.vctek.orderservice.model.OrderSettingDiscountModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderSettingDiscountService {
    OrderSettingDiscountModel save(OrderSettingDiscountModel model);

    List<OrderSettingDiscountModel> saveAll(List<OrderSettingDiscountModel> models);

    List<OrderSettingDiscountModel> findAllByCompanyIdAndProductIdAndDeleted(Long companyId, List<Long> productIds);

    List<OrderSettingDiscountModel> findAllByCompanyIdAndCategoryCodeAndDeleted(Long companyId, List<String> categoryCodes);

    OrderSettingDiscountModel findOneByIdAndCompanyId(Long companyId, Long id);

    OrderSettingDiscountModel findByCompanyIdAndProductIdAndDeleted(Long companyId, Long productId);

    Page<OrderSettingDiscountModel> findAllProductSetting(Long companyId, String product, Pageable pageableRequest);

    List<OrderSettingDiscountModel> findAllCatgorySetting(Long companyId);

    List<OrderDiscountSettingMapper> findAllByProductId(Long companyId, Pageable pageable);
}
