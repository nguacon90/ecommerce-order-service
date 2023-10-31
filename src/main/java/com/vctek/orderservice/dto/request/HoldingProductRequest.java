package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.dto.HoldingData;

import java.util.ArrayList;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldingProductRequest {
    private String orderCode;
    private Long companyId;
    private List<HoldingData> holdingDataList = new ArrayList<>();

    public List<HoldingData> getHoldingDataList() {
        return holdingDataList;
    }

    public void setHoldingDataList(List<HoldingData> holdingDataList) {
        this.holdingDataList = holdingDataList;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}
