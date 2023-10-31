package com.vctek.orderservice.repository.dao;

import com.vctek.orderservice.dto.OrderDiscountSettingMapper;
import com.vctek.orderservice.model.OrderSettingDiscountModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderSettingDiscountDAO {
    List<OrderDiscountSettingMapper> findAllBy(Long companyId, Pageable pageable);

    Page<OrderSettingDiscountModel> findAllProductSetting(Long companyId, String product, Pageable pageRequest);
}
