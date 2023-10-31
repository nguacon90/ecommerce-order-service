package com.vctek.orderservice.service;

import com.vctek.orderservice.model.OrderSettingModel;

public interface OrderSettingService {
    OrderSettingModel save(OrderSettingModel model);

    OrderSettingModel findByTypeAndCompanyId(String type, Long companyId);

    OrderSettingModel findCreateNotificationChangeStatus(Long companyId);
}
