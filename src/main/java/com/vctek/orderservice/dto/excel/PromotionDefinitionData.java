package com.vctek.orderservice.dto.excel;

import java.util.HashSet;
import java.util.Set;

public class PromotionDefinitionData {
    private String definitionId;
    private Set<Long> productIds = new HashSet<>();
    private Set<Long> categoryIds = new HashSet<>();

    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    public Set<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(Set<Long> productIds) {
        this.productIds = productIds;
    }

    public Set<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(Set<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }
}
