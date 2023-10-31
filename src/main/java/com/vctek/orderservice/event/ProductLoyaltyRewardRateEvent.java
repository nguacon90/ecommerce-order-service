package com.vctek.orderservice.event;

import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;

public class ProductLoyaltyRewardRateEvent {
    private ProductLoyaltyRewardRateModel productLoyaltyRewardRateModel;
    private String type;

    public ProductLoyaltyRewardRateEvent(ProductLoyaltyRewardRateModel productLoyaltyRewardRateModel, String type) {
        this.productLoyaltyRewardRateModel = productLoyaltyRewardRateModel;
        this.type = type;
    }

    public ProductLoyaltyRewardRateModel getProductLoyaltyRewardRateModel() {
        return productLoyaltyRewardRateModel;
    }

    public void setProductLoyaltyRewardRateModel(ProductLoyaltyRewardRateModel productLoyaltyRewardRateModel) {
        this.productLoyaltyRewardRateModel = productLoyaltyRewardRateModel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
