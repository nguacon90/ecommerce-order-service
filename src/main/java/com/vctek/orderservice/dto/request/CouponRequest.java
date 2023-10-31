package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.dto.CouponCodeData;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CouponRequest {
    private Long id;
    private Long companyId;
    private String name;
    private Integer maxTotalRedemption;
    private Integer maxRedemptionPerCustomer;
    private String prefix;
    private String suffix;
    private Integer length;
    private Integer quantity;
    private Boolean allowRedemptionMultipleCoupon;
    private List<CouponCodeData> codes;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxTotalRedemption() {
        return maxTotalRedemption;
    }

    public void setMaxTotalRedemption(Integer maxTotalRedemption) {
        this.maxTotalRedemption = maxTotalRedemption;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getMaxRedemptionPerCustomer() {
        return maxRedemptionPerCustomer;
    }

    public void setMaxRedemptionPerCustomer(Integer maxRedemptionPerCustomer) {
        this.maxRedemptionPerCustomer = maxRedemptionPerCustomer;
    }

    public List<CouponCodeData> getCodes() {
        return codes;
    }

    public void setCodes(List<CouponCodeData> codes) {
        this.codes = codes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getAllowRedemptionMultipleCoupon() {
        return allowRedemptionMultipleCoupon;
    }

    public void setAllowRedemptionMultipleCoupon(Boolean allowRedemptionMultipleCoupon) {
        this.allowRedemptionMultipleCoupon = allowRedemptionMultipleCoupon;
    }
}
