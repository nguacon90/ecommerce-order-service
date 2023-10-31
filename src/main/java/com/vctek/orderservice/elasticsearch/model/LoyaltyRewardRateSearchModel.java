package com.vctek.orderservice.elasticsearch.model;

import com.vctek.orderservice.util.ElasticSearchIndex;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = ElasticSearchIndex.LOYALTY_REWARD_RATE_INDEX)
public class LoyaltyRewardRateSearchModel extends ElasticItemModel {
    private Long companyId;
    private Long productId;
    private String productName;
    private String productStringName;
    private String productSku;
    private Double rewardRate;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductStringName() {
        return productStringName;
    }

    public void setProductStringName(String productStringName) {
        this.productStringName = productStringName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public Double getRewardRate() {
        return rewardRate;
    }

    public void setRewardRate(Double rewardRate) {
        this.rewardRate = rewardRate;
    }
}
