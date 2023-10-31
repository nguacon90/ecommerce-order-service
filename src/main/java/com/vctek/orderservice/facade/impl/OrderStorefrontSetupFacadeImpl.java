package com.vctek.orderservice.facade.impl;

import com.vctek.dto.redis.OrderStorefrontSetupData;
import com.vctek.orderservice.facade.OrderStorefrontSetupFacade;
import com.vctek.orderservice.model.OrderStorefrontSetupModel;
import com.vctek.orderservice.service.OrderStorefrontSetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class OrderStorefrontSetupFacadeImpl implements OrderStorefrontSetupFacade {
    private OrderStorefrontSetupService service;

    @Override
    @CacheEvict(value = "orderStorefrontSetupData", key = "#request.companyId")
    public OrderStorefrontSetupData createOrUpdate(OrderStorefrontSetupData request) {
        OrderStorefrontSetupModel model = service.findByCompanyId(request.getCompanyId());
        if (model == null) {
            model = new OrderStorefrontSetupModel();
            model.setCompanyId(request.getCompanyId());
        }
        model.setWarehouseId(request.getWarehouseId());
        model.setOrderSourceId(request.getOrderSourceId());
        OrderStorefrontSetupModel savedModel = service.save(model);
        return convertModel(savedModel);
    }

    @Override
    @Cacheable(unless="#result == null", value = "orderStorefrontSetupData", key = "#companyId", cacheManager = "microServiceCacheManager")
    public OrderStorefrontSetupData findByCompanyId(Long companyId) {
        OrderStorefrontSetupModel model = service.findByCompanyId(companyId);
        if (model == null) {
            return null;
        }
        return convertModel(model);
    }
    private OrderStorefrontSetupData convertModel(OrderStorefrontSetupModel model) {
        OrderStorefrontSetupData data = new OrderStorefrontSetupData();
        data.setId(model.getId());
        data.setCompanyId(model.getCompanyId());
        data.setOrderSourceId(model.getOrderSourceId());
        data.setWarehouseId(model.getWarehouseId());
        return data;
    }

    @Autowired
    public void setOrderStorefrontSetupService(OrderStorefrontSetupService service) {
        this.service = service;
    }

}
