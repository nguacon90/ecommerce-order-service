package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderSettingModel;
import com.vctek.orderservice.repository.OrderSettingRepository;
import com.vctek.orderservice.service.OrderSettingService;
import com.vctek.orderservice.util.OrderSettingType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class OrderSettingServiceImpl implements OrderSettingService {
    private OrderSettingRepository repository;

    public OrderSettingServiceImpl(OrderSettingRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderSettingModel save(OrderSettingModel model) {
        return repository.save(model);
    }

    @Override
    public OrderSettingModel findByTypeAndCompanyId(String type, Long companyId) {
        return repository.findByTypeAndCompanyId(type, companyId);
    }

    @Override
    @Cacheable(unless="#result == null", value = "create_notification_change_order_status", key = "#companyId", cacheManager = "microServiceCacheManager")
    public OrderSettingModel findCreateNotificationChangeStatus(Long companyId) {
        return repository.findByTypeAndCompanyId(OrderSettingType.CREATE_NOTIFICATION_CHANGE_ORDER_STATUS.code(), companyId);
    }
}
