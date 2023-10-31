package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.RedeemRateRequest;
import com.vctek.orderservice.model.CategoryRedeemRateModel;
import com.vctek.orderservice.repository.CategoryRedeemRateRepository;
import com.vctek.orderservice.service.CategoryRedeemRateService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CategoryRedeemRateServiceImpl implements CategoryRedeemRateService {
    private CategoryRedeemRateRepository rewardRateUseRepository;

    public CategoryRedeemRateServiceImpl(CategoryRedeemRateRepository rewardRateUseRepository) {
        this.rewardRateUseRepository = rewardRateUseRepository;
    }

    @Override
    public List<CategoryRedeemRateModel> createOrUpdate(RedeemRateRequest loyaltyRewardRateRequest) {
        Long companyId = loyaltyRewardRateRequest.getCompanyId();
        List<CategoryRedeemRateModel> saveList = new ArrayList<>();
        for (Long categoryId : loyaltyRewardRateRequest.getListId()) {
            CategoryRedeemRateModel oldModel = rewardRateUseRepository.findByCompanyIdAndCategoryId(companyId, categoryId);
            oldModel = oldModel != null ? oldModel : new CategoryRedeemRateModel();
            oldModel.setCompanyId(companyId);
            oldModel.setCategoryId(categoryId);
            saveList.add(oldModel);
        }
        return rewardRateUseRepository.saveAll(saveList);
    }

    @Override
    public List<CategoryRedeemRateModel> findByCompanyId(Long companyId) {
        return rewardRateUseRepository.findAllByCompanyId(companyId);
    }

    @Override
    public CategoryRedeemRateModel findByCategoryIdAndCompanyId(Long categoryId, Long companyId) {
        return rewardRateUseRepository.findByCompanyIdAndCategoryId(companyId, categoryId);
    }

    @Override
    public void delete(CategoryRedeemRateModel model) {
        rewardRateUseRepository.delete(model);
    }

    @Override
    public void deleteAll(Collection<CategoryRedeemRateModel> models) {
        rewardRateUseRepository.deleteAll(models);
    }


    @Override
    public List<CategoryRedeemRateModel> findByCategoryInAndCompanyId(List<Long> categoryId, Long companyId) {
        return rewardRateUseRepository.findAllByCategoryIdInAndCompanyId(categoryId, companyId);
    }
}
