package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.OrderSettingData;
import com.vctek.orderservice.dto.request.OrderSettingRequest;

public interface OrderSettingFacade {
    OrderSettingData createOrUpdateComboPriceSetting(OrderSettingRequest orderSettingRequest);

    OrderSettingData getComboPriceSetting(Long companyId);

    OrderSettingData getOrderMaximumDiscount(Long companyId);

    OrderSettingData createOrUpdateSettingNotificationChangeStatus(OrderSettingRequest request);

    OrderSettingData getSettingNotificationChangeStatus(Long companyId);
}
