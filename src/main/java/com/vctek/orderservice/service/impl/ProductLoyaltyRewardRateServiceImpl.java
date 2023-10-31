package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateRequest;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.event.ProductLoyaltyRewardRateEvent;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.CategoryLoyaltyRewardRateModel;
import com.vctek.orderservice.model.DefaultLoyaltyRewardRateModel;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import com.vctek.orderservice.repository.CategoryLoyaltyRewardRateRepository;
import com.vctek.orderservice.repository.DefaultLoyaltyRewardRateRepository;
import com.vctek.orderservice.repository.ProductLoyaltyRewardRateRepository;
import com.vctek.orderservice.service.ProductLoyaltyRewardRateService;
import com.vctek.orderservice.util.EventType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductLoyaltyRewardRateServiceImpl implements ProductLoyaltyRewardRateService {

    private ProductLoyaltyRewardRateRepository repository;
    private DefaultLoyaltyRewardRateRepository defaultRepository;
    private CategoryLoyaltyRewardRateRepository categoryLoyaltyRewardRateRepository;
    private ApplicationEventPublisher applicationEventPublisher;
    private ProductSearchService productSearchService;

    @Override
    public List<ProductLoyaltyRewardRateModel> findAllByCompanyId(Long companyId) {
        return repository.findAllByCompanyId(companyId);
    }

    @Override
    public List<ProductLoyaltyRewardRateModel> createOrUpdate(LoyaltyRewardRateRequest loyaltyRewardRateRequest) {
        Long companyId = loyaltyRewardRateRequest.getCompanyId();
        Map<Long, ProductLoyaltyRewardRateModel> saveMap = new HashMap<>();
        for (LoyaltyRewardRateDetailRequest request : loyaltyRewardRateRequest.getDetails()) {
            ProductLoyaltyRewardRateModel oldModel = repository.findByCompanyIdAndProductId(companyId, request.getProductId());
            oldModel = oldModel != null ? oldModel : new ProductLoyaltyRewardRateModel();
            oldModel.setCompanyId(companyId);
            oldModel.setProductId(request.getProductId());
            oldModel.setRewardRate(request.getRewardRate());
            saveMap.put(request.getProductId(), oldModel);
        }
        List<ProductLoyaltyRewardRateModel> loyaltyRewardRateModels = repository.saveAll(saveMap.values());
        for (ProductLoyaltyRewardRateModel model : loyaltyRewardRateModels) {
            applicationEventPublisher.publishEvent(new ProductLoyaltyRewardRateEvent(model, EventType.CREATE.toString()));
        }
        return loyaltyRewardRateModels;
    }

    @Override
    public void delete(LoyaltyRewardRateDetailRequest request) {
        ProductLoyaltyRewardRateModel oldModel = repository.findByCompanyIdAndProductId(request.getCompanyId(), request.getProductId());
        if (oldModel != null) {
            repository.delete(oldModel);
            applicationEventPublisher.publishEvent(new ProductLoyaltyRewardRateEvent(oldModel, EventType.DELETE.toString()));
        }
    }

    @Override
    public Map<Long, Double> getRewardRateByProductIds(Set<Long> productIds, Long companyId, boolean isCombo) {
        Map<Long, Double> rewardRate = new HashMap<>();
        List<ProductLoyaltyRewardRateModel> models = repository.findAllByCompanyIdAndProductIdIn(companyId, productIds);
        if (CollectionUtils.isNotEmpty(models)) {
            models.forEach(model -> {
                if (productIds.contains(model.getProductId())) {
                    rewardRate.put(model.getProductId(), model.getRewardRate());
                }
            });
        }

        List<Long> productIdsWithCategory = productIds.stream().filter(p -> !rewardRate.keySet().contains(p)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(productIdsWithCategory) || isCombo) {
            return rewardRate;
        }
        populateCategoryRewardRate(productIdsWithCategory, companyId, rewardRate);

        List<Long> productIdsDefault = productIds.stream().filter(p -> !rewardRate.keySet().contains(p)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(productIdsDefault)) {
            return rewardRate;
        }
        populateDefaultRewardRate(productIdsDefault, companyId, rewardRate);

        return rewardRate;
    }

    @Override
    public Page<ProductLoyaltyRewardRateModel> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public List<ProductLoyaltyRewardRateModel> findAllByCompanyIdAndProductIds(Long companyId, List<Long> productIds) {
        return repository.findAllByCompanyIdAndProductIdIn(companyId, productIds);
    }

    private void populateDefaultRewardRate(List<Long> productIdsDefault, Long companyId, Map<Long, Double> rewardRate) {
        DefaultLoyaltyRewardRateModel rewardRateModel = defaultRepository.findByCompanyId(companyId);
        double rate = rewardRateModel == null ? 0 : rewardRateModel.getRewardRate();
        for (Long id : productIdsDefault) {
            rewardRate.put(id, rate);
        }
    }

    private void populateCategoryRewardRate(List<Long> productIdsWithCategory, Long companyId, Map<Long, Double> rewardRate) {
        Set<Long> categoryIds = new HashSet<>();
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setCompanyId(companyId);
        String product = StringUtils.join(productIdsWithCategory, ",");
        searchRequest.setIds(product);
        searchRequest.setPageSize(CollectionUtils.size(productIdsWithCategory));
        List<ProductSearchModel> productDataList = productSearchService.findAllByCompanyId(searchRequest);
        for (ProductSearchModel data : productDataList) {
            categoryIds.addAll(data.getFullCategoryIds());
        }
        List<CategoryLoyaltyRewardRateModel> models = categoryLoyaltyRewardRateRepository.findAllByCategoryIdInAndCompanyId(categoryIds, companyId);
        if (CollectionUtils.isNotEmpty(models)) {
            Map<Long, Double> categoryRate = models.stream().collect(Collectors.toMap(CategoryLoyaltyRewardRateModel::getCategoryId, CategoryLoyaltyRewardRateModel::getRewardRate));
            for (ProductSearchModel data : productDataList) {
                for (Long category : data.getFullCategoryIds()) {
                    if (categoryRate.containsKey(category)) {
                        rewardRate.put(data.getId(), categoryRate.get(category));
                        break;
                    }
                }
            }
        }
    }

    @Autowired
    public void setRepository(ProductLoyaltyRewardRateRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setCategoryLoyaltyRewardRateRepository(CategoryLoyaltyRewardRateRepository categoryLoyaltyRewardRateRepository) {
        this.categoryLoyaltyRewardRateRepository = categoryLoyaltyRewardRateRepository;
    }

    @Autowired
    public void setDefaultRepository(DefaultLoyaltyRewardRateRepository defaultRepository) {
        this.defaultRepository = defaultRepository;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setProductSearchService(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }
}
