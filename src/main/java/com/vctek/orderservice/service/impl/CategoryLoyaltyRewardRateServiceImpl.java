package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateRequest;
import com.vctek.orderservice.model.CategoryLoyaltyRewardRateModel;
import com.vctek.orderservice.repository.CategoryLoyaltyRewardRateRepository;
import com.vctek.orderservice.service.CategoryLoyaltyRewardRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryLoyaltyRewardRateServiceImpl implements CategoryLoyaltyRewardRateService {
    private CategoryLoyaltyRewardRateRepository repository;
    @Override
    public List<CategoryLoyaltyRewardRateModel> findAllByCompanyId(Long companyId) {
        return repository.findAllByCompanyId(companyId);
    }

    @Override
    public List<CategoryLoyaltyRewardRateModel> createOrUpdate(LoyaltyRewardRateRequest loyaltyRewardRateRequest) {
        Long companyId = loyaltyRewardRateRequest.getCompanyId();
        List<CategoryLoyaltyRewardRateModel> saveList = new ArrayList<>();
        for (LoyaltyRewardRateDetailRequest request : loyaltyRewardRateRequest.getDetails()) {
            CategoryLoyaltyRewardRateModel oldModel = repository.findByCompanyIdAndCategoryId(companyId, request.getCategoryId());
            oldModel = oldModel != null ? oldModel : new CategoryLoyaltyRewardRateModel();
            oldModel.setCompanyId(companyId);
            oldModel.setCategoryId(request.getCategoryId());
            oldModel.setRewardRate(request.getRewardRate());
            saveList.add(oldModel);
        }
        return repository.saveAll(saveList);
    }

    @Override
    public void delete(LoyaltyRewardRateDetailRequest request) {
        CategoryLoyaltyRewardRateModel oldModel = repository.findByCompanyIdAndCategoryId(request.getCompanyId(), request.getCategoryId());
        if (oldModel != null) {
            repository.delete(oldModel);
        }
    }

    @Autowired
    public void setRepository(CategoryLoyaltyRewardRateRepository repository) {
        this.repository = repository;
    }
}
