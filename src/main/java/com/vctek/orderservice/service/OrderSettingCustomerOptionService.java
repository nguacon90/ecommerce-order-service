package com.vctek.orderservice.service;

import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.model.OrderSettingCustomerOptionModel;

import java.util.List;

public interface OrderSettingCustomerOptionService {
    List<OrderSettingCustomerOptionModel> findByOrderSettingCustomerModel(OrderSettingCustomerModel orderSettingCustomerModel);

    OrderSettingCustomerOptionModel findByIdAndCompanyId(Long id, Long companyId);

    List<OrderSettingCustomerOptionModel> findAllByOrderId(Long orderId);

    List<OrderSettingCustomerOptionModel> findAllByCompanyNotHasOrder(Long companyId);

    List<OrderSettingCustomerOptionModel> findAllByCompanyIdAndIdIn(Long companyId, List<Long> ids);
}
