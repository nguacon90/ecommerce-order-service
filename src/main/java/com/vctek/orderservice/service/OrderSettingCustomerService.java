package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.request.OrderSettingCustomerSearchRequest;
import com.vctek.orderservice.model.OrderSettingCustomerModel;

import java.util.List;

public interface OrderSettingCustomerService {
    OrderSettingCustomerModel save(OrderSettingCustomerModel model);

    OrderSettingCustomerModel findByIdAndCompanyId(Long id, Long companyId);

    List<OrderSettingCustomerModel> findAllByNameAndCompanyId(String name, Long companyId);

    List<OrderSettingCustomerModel> findAllBy(OrderSettingCustomerSearchRequest request);

    OrderSettingCustomerModel findByCompanyIdAndDefault(Long companyId);

    List<OrderSettingCustomerModel> findAllByCompanyIdAndOrderType(Long companyId, String orderType);
}
