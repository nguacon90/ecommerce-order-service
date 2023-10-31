package com.vctek.orderservice.service;

import com.vctek.orderservice.model.OrderTypeSettingCustomerModel;

import java.util.List;

public interface OrderTypeSettingCustomerService {
    void deleteAll(List<OrderTypeSettingCustomerModel> models);
}
