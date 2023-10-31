package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.request.OrderSettingRequest;
import com.vctek.orderservice.model.OrderSettingModel;

public interface ComboPriceSettingService {
    OrderSettingModel save(OrderSettingRequest orderSettingRequest);

    OrderSettingModel findByTypeAndCompanyId(String type, Long companyId);
}
