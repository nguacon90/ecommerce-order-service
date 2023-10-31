package com.vctek.orderservice.dto.excel;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductLoyaltyRewardRateDTO {
    private String productId;
    private String productSku;
    private String productName;
    private String rewardRate;
    private String oldRewardRate;
    private String error;
    private Integer rowExcel;
    private List<ProductLoyaltyRewardRateDTO> rewardRateData = new ArrayList<>();
    private List<ProductLoyaltyRewardRateDTO> errors = new ArrayList<>();

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getRewardRate() {
        return rewardRate;
    }

    public void setRewardRate(String rewardRate) {
        this.rewardRate = rewardRate;
    }

    public String getOldRewardRate() {
        return oldRewardRate;
    }

    public void setOldRewardRate(String oldRewardRate) {
        this.oldRewardRate = oldRewardRate;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getRowExcel() {
        return rowExcel;
    }

    public void setRowExcel(Integer rowExcel) {
        this.rowExcel = rowExcel;
    }

    public List<ProductLoyaltyRewardRateDTO> getRewardRateData() {
        return rewardRateData;
    }

    public void setRewardRateData(List<ProductLoyaltyRewardRateDTO> rewardRateData) {
        this.rewardRateData = rewardRateData;
    }

    public List<ProductLoyaltyRewardRateDTO> getErrors() {
        return errors;
    }

    public void setErrors(List<ProductLoyaltyRewardRateDTO> errors) {
        this.errors = errors;
    }

    public boolean isNotEmpty() {
        if(StringUtils.isNotBlank(productId)) {
            return true;
        }
        if(StringUtils.isNotBlank(productSku)) {
            return true;
        }
        if(StringUtils.isNotBlank(rewardRate)) {
            return true;
        }
        if(StringUtils.isNotBlank(productName)) {
            return true;
        }
        return false;
    }
}
