package com.vctek.orderservice.dto.request.storefront;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.feignclient.dto.BasicProductData;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductPromotionRequest {
    private Long companyId;
    private List<ProductPromotion> productList;
    private List<ProductSearchModel> productSearchModels;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public List<ProductSearchModel> getProductSearchModels() {
        return productSearchModels;
    }

    public void setProductSearchModels(List<ProductSearchModel> productSearchModels) {
        this.productSearchModels = productSearchModels;
    }

    public List<ProductPromotion> getProductList() {
        return productList;
    }

    public void setProductList(List<ProductPromotion> productList) {
        this.productList = productList;
    }
}
