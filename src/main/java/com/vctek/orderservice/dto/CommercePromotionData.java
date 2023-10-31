package com.vctek.orderservice.dto;

import com.vctek.dto.promotion.PromotionSourceRuleDTO;

import java.util.ArrayList;
import java.util.List;

public class CommercePromotionData extends PromotionSourceRuleDTO {
    private List<Long> conditionProducts = new ArrayList<>();
    private List<Long> conditionCategories = new ArrayList<>();

    public List<Long> getConditionProducts() {
        return conditionProducts;
    }

    public void setConditionProducts(List<Long> conditionProducts) {
        this.conditionProducts = conditionProducts;
    }

    public List<Long> getConditionCategories() {
        return conditionCategories;
    }

    public void setConditionCategories(List<Long> conditionCategories) {
        this.conditionCategories = conditionCategories;
    }
}
