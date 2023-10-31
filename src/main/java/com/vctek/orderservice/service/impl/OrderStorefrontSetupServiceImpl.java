package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderStorefrontSetupModel;
import com.vctek.orderservice.repository.OrderStorefrontSetupRepository;
import com.vctek.orderservice.service.OrderStorefrontSetupService;
import org.springframework.stereotype.Service;

@Service
public class OrderStorefrontSetupServiceImpl implements OrderStorefrontSetupService {
    private OrderStorefrontSetupRepository repository;

    public OrderStorefrontSetupServiceImpl(OrderStorefrontSetupRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderStorefrontSetupModel findByCompanyId(Long companyId) {
        return repository.findByCompanyId(companyId);
    }

    @Override
    public OrderStorefrontSetupModel save(OrderStorefrontSetupModel model) {
        return repository.save(model);
    }
}
