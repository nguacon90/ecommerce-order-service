package com.vctek.orderservice.feignclient.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RewardSettingData {
    private Long id;
    private Long companyId;
    private double conversionRate;
    private Long rewardTime;
    private String rewardTimeUnit;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRewardTime() {
        return rewardTime;
    }

    public void setRewardTime(Long rewardTime) {
        this.rewardTime = rewardTime;
    }

    public String getRewardTimeUnit() {
        return rewardTimeUnit;
    }

    public void setRewardTimeUnit(String rewardTimeUnit) {
        this.rewardTimeUnit = rewardTimeUnit;
    }

    public double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(double conversionRate) {
        this.conversionRate = conversionRate;
    }
}
