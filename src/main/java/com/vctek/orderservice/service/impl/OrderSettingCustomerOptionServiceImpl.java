package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.model.OrderSettingCustomerOptionModel;
import com.vctek.orderservice.repository.OrderSettingCustomerOptionRepository;
import com.vctek.orderservice.service.OrderSettingCustomerOptionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderSettingCustomerOptionServiceImpl implements OrderSettingCustomerOptionService {
    private OrderSettingCustomerOptionRepository repository;

    public OrderSettingCustomerOptionServiceImpl(OrderSettingCustomerOptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<OrderSettingCustomerOptionModel> findByOrderSettingCustomerModel(OrderSettingCustomerModel settingCustomerModel) {
        return repository.findByOrderSettingCustomerModelAndDeleted(settingCustomerModel, false);
    }

    @Override
    public OrderSettingCustomerOptionModel findByIdAndCompanyId(Long id, Long companyId) {
        return repository.findByIdAndCompanyIdAndDeleted(id, companyId, false);
    }

    @Override
    public List<OrderSettingCustomerOptionModel> findAllByOrderId(Long orderId) {
        return repository.findAllByOrderId(orderId);
    }

    @Override
    public List<OrderSettingCustomerOptionModel> findAllByCompanyNotHasOrder(Long companyId) {
        return repository.findAllByCompanyNotHasOrder(companyId);
    }

    @Override
    public List<OrderSettingCustomerOptionModel> findAllByCompanyIdAndIdIn(Long companyId, List<Long> ids) {
        return repository.findAllByCompanyIdAndIdIn(companyId, ids);
    }

}
