package com.vctek.orderservice.dto;

public class AvailablePointAmountData {
    private Long companyId;
    private Double pointAmount;
    private Double availableAmount;
    private Double originAvailableAmount;
    private Double conversionRate;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Double getPointAmount() {
        return pointAmount;
    }

    public void setPointAmount(Double pointAmount) {
        this.pointAmount = pointAmount;
    }

    public Double getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(Double availableAmount) {
        this.availableAmount = availableAmount;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public Double getOriginAvailableAmount() {
        return originAvailableAmount;
    }

    public void setOriginAvailableAmount(Double originAvailableAmount) {
        this.originAvailableAmount = originAvailableAmount;
    }
}
