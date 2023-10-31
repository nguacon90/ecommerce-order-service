package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.CustomerGroupData;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.model.OrderModel;

import java.util.List;

public interface CustomerService {
    CustomerData update(OrderModel order, CustomerRequest customerRequest);

    CustomerData getCustomerById(Long customerId, Long companyId);

    CustomerData getBasicCustomerInfo(Long customerId, Long companyId);

    boolean limitedApplyPromotionAndReward(Long customerId, Long companyId);

    List<CustomerGroupData> getCustomerGroups(Long customerId);
}
