package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.model.DefaultLoyaltyRewardRateModel;
import com.vctek.orderservice.repository.DefaultLoyaltyRewardRateRepository;
import com.vctek.orderservice.service.DefaultLoyaltyRewardRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultLoyaltyRewardRateServiceImpl implements DefaultLoyaltyRewardRateService {

    private DefaultLoyaltyRewardRateRepository repository;

    @Override
    public DefaultLoyaltyRewardRateModel findByCompanyId(Long companyId) {
        return repository.findByCompanyId(companyId);
    }

    @Override
    public DefaultLoyaltyRewardRateModel createOrUpdate(LoyaltyRewardRateDetailRequest loyaltyRewardRateDetailRequest) {
        DefaultLoyaltyRewardRateModel model = repository.findByCompanyId(loyaltyRewardRateDetailRequest.getCompanyId());
        model = model != null ? model : new DefaultLoyaltyRewardRateModel();
        model.setCompanyId(loyaltyRewardRateDetailRequest.getCompanyId());
        model.setRewardRate(loyaltyRewardRateDetailRequest.getRewardRate());
        return repository.save(model);
    }

    @Autowired
    public void setRepository(DefaultLoyaltyRewardRateRepository repository) {
        this.repository = repository;
    }
}
