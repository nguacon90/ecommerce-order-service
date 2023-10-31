package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderTypeSettingCustomerModel;
import com.vctek.orderservice.repository.OrderTypeSettingCustomerRepository;
import com.vctek.orderservice.service.OrderTypeSettingCustomerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderTypeSettingCustomerServiceImpl implements OrderTypeSettingCustomerService {
    private OrderTypeSettingCustomerRepository repository;

    public OrderTypeSettingCustomerServiceImpl(OrderTypeSettingCustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public void deleteAll(List<OrderTypeSettingCustomerModel> models) {
        repository.deleteAll(models);
    }
}
