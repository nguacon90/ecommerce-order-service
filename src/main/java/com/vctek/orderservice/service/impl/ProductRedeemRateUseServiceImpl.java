package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.RedeemRateRequest;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.CategoryRedeemRateModel;
import com.vctek.orderservice.model.ProductRedeemRateModel;
import com.vctek.orderservice.repository.CategoryRedeemRateRepository;
import com.vctek.orderservice.repository.ProductRedeemRateRepository;
import com.vctek.orderservice.service.ProductRedeemRateUseService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.redis.elastic.ProductSearchData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductRedeemRateUseServiceImpl implements ProductRedeemRateUseService {
    
    private ProductRedeemRateRepository rewardRateUseRepository;
    private CategoryRedeemRateRepository categoryRedeemRateRepository;
    private ProductService productService;

    public ProductRedeemRateUseServiceImpl(ProductRedeemRateRepository rewardRateUseRepository, CategoryRedeemRateRepository categoryRedeemRateRepository) {
        this.rewardRateUseRepository = rewardRateUseRepository;
        this.categoryRedeemRateRepository = categoryRedeemRateRepository;
    }

    @Override
    public List<ProductRedeemRateModel> createOrUpdate(RedeemRateRequest loyaltyRewardRateRequest) {
        Long companyId = loyaltyRewardRateRequest.getCompanyId();
        List<ProductRedeemRateModel> saveList = new ArrayList<>();
        for (Long productId : loyaltyRewardRateRequest.getListId()) {
            ProductRedeemRateModel oldModel = rewardRateUseRepository.findByCompanyIdAndProductId(companyId, productId);
            oldModel = oldModel != null ? oldModel : new ProductRedeemRateModel();
            oldModel.setCompanyId(companyId);
            oldModel.setProductId(productId);
            saveList.add(oldModel);
        }
        return rewardRateUseRepository.saveAll(saveList);
    }

    @Override
    public List<ProductRedeemRateModel> findByCompanyId(Long companyId) {
        return rewardRateUseRepository.findAllByCompanyId(companyId);
    }

    @Override
    public void delete(ProductRedeemRateModel model) {
        rewardRateUseRepository.delete(model);
    }

    @Override
    public ProductRedeemRateModel findByProductIdAndCompanyId(Long productId, Long companyId) {
        return rewardRateUseRepository.findByCompanyIdAndProductId(companyId, productId);
    }

    @Override
    public Map<Long, Boolean> productCanRedeem(Long companyId, List<Long> products) {
        Map<Long, Boolean> results = new HashMap<>();
        List<ProductRedeemRateModel> productRedeemRateModels = rewardRateUseRepository.findAllByCompanyIdAndProductIdIn(companyId, products);
        if (CollectionUtils.isNotEmpty(productRedeemRateModels)) {
            productRedeemRateModels.forEach(model -> {
                if (products.contains(model.getProductId())) {
                    results.put(model.getProductId(), false);
                }
            });
        }
        List<Long> productListSearchInCategory = products.stream().filter(p -> !results.keySet().contains(p)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(productListSearchInCategory)) {
            return results;
        }
        searchInCategory(companyId, productListSearchInCategory, products, results);
        return results;
    }

    private void searchInCategory(Long companyId, List<Long> productListSearchInCategory, List<Long> products, Map<Long, Boolean> results) {
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setCompanyId(companyId);
        searchRequest.setIds(StringUtils.join(productListSearchInCategory, ","));
        searchRequest.setPageSize(CollectionUtils.size(productListSearchInCategory));
        List<ProductSearchData> productSearchData = productService.search(searchRequest);
        if (CollectionUtils.isNotEmpty(productSearchData)) {
            for (ProductSearchData product : productSearchData) {
                Optional<CategoryRedeemRateModel> categoryRedeemRateModel = categoryRedeemRateRepository.findTopByCategoryIdAndAndCompanyId(product.getMainCategoryId(), companyId);
                results.put(product.getId(), !categoryRedeemRateModel.isPresent());
            }
        }
        products.forEach(p -> {
            if (!results.containsKey(p)) results.put(p, true);
        });
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
