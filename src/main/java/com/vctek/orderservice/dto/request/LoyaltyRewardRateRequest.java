package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoyaltyRewardRateRequest {
    private Long companyId;
    private String dtype;
    private List<LoyaltyRewardRateDetailRequest> details;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public List<LoyaltyRewardRateDetailRequest> getDetails() {
        return details;
    }

    public void setDetails(List<LoyaltyRewardRateDetailRequest> details) {
        this.details = details;
    }
}
