package com.vctek.orderservice.feignclient.dto;

import java.util.List;

public class ProductVatRequest {
    private List<Long> productIds;

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}
