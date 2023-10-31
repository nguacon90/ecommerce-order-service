package com.vctek.orderservice.dto;

public class CreateCartParam {
    private Long companyId;
    private String oldCartGuid;
    private String sellSignal;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getOldCartGuid() {
        return oldCartGuid;
    }

    public void setOldCartGuid(String oldCartGuid) {
        this.oldCartGuid = oldCartGuid;
    }

    public String getSellSignal() {
        return sellSignal;
    }

    public void setSellSignal(String sellSignal) {
        this.sellSignal = sellSignal;
    }
}
