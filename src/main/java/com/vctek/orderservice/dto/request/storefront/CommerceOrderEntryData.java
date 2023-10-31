package com.vctek.orderservice.dto.request.storefront;


import com.vctek.orderservice.elasticsearch.model.OrderEntryData;

public class CommerceOrderEntryData extends OrderEntryData {
    private Long parentProductId;
    private String parentProductName;

    public Long getParentProductId() {
        return parentProductId;
    }

    public void setParentProductId(Long parentProductId) {
        this.parentProductId = parentProductId;
    }

    public String getParentProductName() {
        return parentProductName;
    }

    public void setParentProductName(String parentProductName) {
        this.parentProductName = parentProductName;
    }
}
