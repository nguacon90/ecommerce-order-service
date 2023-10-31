package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntrySaleOffRequest {
    private Long companyId;
    private String orderCode;
    private Long entryId;
    private boolean saleOff;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public boolean isSaleOff() {
        return saleOff;
    }

    public void setSaleOff(boolean saleOff) {
        this.saleOff = saleOff;
    }
}
