package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.OrderSettingCustomerData;
import com.vctek.orderservice.dto.SettingCustomerData;
import com.vctek.orderservice.dto.request.OrderSettingCustomerRequest;
import com.vctek.orderservice.dto.request.OrderSettingCustomerSearchRequest;

import java.util.List;

public interface OrderSettingCustomerFacade {

    OrderSettingCustomerData createSetting(OrderSettingCustomerRequest request);

    OrderSettingCustomerData updateSetting(OrderSettingCustomerRequest request);

    OrderSettingCustomerData findOneBy(Long settingId, Long companyId);

    List<OrderSettingCustomerData> findAllBy(OrderSettingCustomerSearchRequest request);

    OrderSettingCustomerData createOrUpdateDefault(OrderSettingCustomerRequest request);

    OrderSettingCustomerData getSettingDefault(Long companyId);

    void deletedSetting(Long id, Long companyId);

    void deletedSettingOption(Long settingId, Long optionId, Long companyId);

    SettingCustomerData findSettingByOrder(Long companyId, String orderType);
}
