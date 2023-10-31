package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CouponCodeData {
    private String code;
    private Integer totalRedemption;
    private boolean isValid;

    private boolean overTotalRedemption;
    private Long promotionId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getTotalRedemption() {
        return totalRedemption;
    }

    public void setTotalRedemption(Integer totalRedemption) {
        this.totalRedemption = totalRedemption;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public Long getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Long promotionId) {
        this.promotionId = promotionId;
    }

    public boolean isOverTotalRedemption() {
        return overTotalRedemption;
    }

    public void setOverTotalRedemption(boolean overTotalRedemption) {
        this.overTotalRedemption = overTotalRedemption;
    }
}
