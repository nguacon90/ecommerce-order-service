package com.vctek.orderservice.facade.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.LoyaltyRedeemRateData;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.dto.request.RedeemRateRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.RedeemRateFacade;
import com.vctek.orderservice.model.CategoryRedeemRateModel;
import com.vctek.orderservice.model.LoyaltyRedeemRateUseModel;
import com.vctek.orderservice.model.ProductRedeemRateModel;
import com.vctek.orderservice.service.CategoryRedeemRateService;
import com.vctek.orderservice.service.ProductRedeemRateUseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RedeemRateFacadeImpl implements RedeemRateFacade {

    private ProductRedeemRateUseService productRedeemRateUseService;
    private CategoryRedeemRateService categoryRedeemRateService;

    public RedeemRateFacadeImpl(ProductRedeemRateUseService productRedeemRateUseService, CategoryRedeemRateService categoryRedeemRateService) {
        this.productRedeemRateUseService = productRedeemRateUseService;
        this.categoryRedeemRateService = categoryRedeemRateService;
    }

    @Override
    public List<Long> createOrUpdateProduct(RedeemRateRequest request) {
        List<ProductRedeemRateModel> list = productRedeemRateUseService.createOrUpdate(request);
        List<Long> productId = list.stream().map(p -> p.getProductId()).collect(Collectors.toList());
        return productId;
    }

    @Override
    public List<Long> createOrUpdateCategory(RedeemRateRequest request) {
        List<CategoryRedeemRateModel> list = categoryRedeemRateService.createOrUpdate(request);
        List<Long> listId = list.stream().map(p -> p.getCategoryId()).collect(Collectors.toList());
        return listId;
    }

    @Override
    public LoyaltyRedeemRateData findBy(Long companyId) {
        LoyaltyRedeemRateData results = new LoyaltyRedeemRateData();
        results.setCompanyId(companyId);
        List<ProductRedeemRateModel> productRedeemRateModelList = productRedeemRateUseService.findByCompanyId(companyId);
        if (CollectionUtils.isNotEmpty(productRedeemRateModelList)) {
            List<Long> listId = productRedeemRateModelList.stream()
                    .map(i -> i.getProductId()).collect(Collectors.toList());
            results.setProductList(listId);
        }
        List<CategoryRedeemRateModel> categoryRedeemRateModels = categoryRedeemRateService.findByCompanyId(companyId);
        if (CollectionUtils.isNotEmpty(categoryRedeemRateModels)) {
            List<Long> listId = categoryRedeemRateModels.stream()
                    .map(i -> i.getCategoryId()).collect(Collectors.toList());
            results.setCategoryList(listId);
        }
        return results;
    }

    @Override
    public void deleteCategory(RedeemRateRequest request) {
        List<CategoryRedeemRateModel> model = categoryRedeemRateService.findByCategoryInAndCompanyId(request.getListId(), request.getCompanyId());
        if (CollectionUtils.isNotEmpty(model)) {
            categoryRedeemRateService.deleteAll(model);
        }
    }

    @Override
    public void deleteProduct(LoyaltyRewardRateDetailRequest request) {
        ProductRedeemRateModel model = productRedeemRateUseService.findByProductIdAndCompanyId(request.getProductId(), request.getCompanyId());
        validateModel(model);
        productRedeemRateUseService.delete(model);
    }

    private void validateModel(LoyaltyRedeemRateUseModel model) {
        if (model == null) {
            ErrorCodes err = ErrorCodes.NOT_FOUND_DATA;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
