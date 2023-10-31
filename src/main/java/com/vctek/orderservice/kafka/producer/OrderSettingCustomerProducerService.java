package com.vctek.orderservice.kafka.producer;

import com.vctek.orderservice.model.OrderSettingCustomerModel;

public interface OrderSettingCustomerProducerService {

    void createOrUpdateOrderSettingCustomer(OrderSettingCustomerModel model);
}
