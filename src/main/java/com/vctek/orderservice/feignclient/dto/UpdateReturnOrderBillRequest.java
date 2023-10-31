package com.vctek.orderservice.feignclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateReturnOrderBillRequest {
    private Long billId;
    private Long returnOrderId;
    private Long companyId;
    private String originOrderCode;

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Long getReturnOrderId() {
        return returnOrderId;
    }

    public void setReturnOrderId(Long returnOrderId) {
        this.returnOrderId = returnOrderId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getOriginOrderCode() {
        return originOrderCode;
    }

    public void setOriginOrderCode(String originOrderCode) {
        this.originOrderCode = originOrderCode;
    }
}
