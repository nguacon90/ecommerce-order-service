package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnOrderUpdateParameter {
    private Long returnOrderId;
    private Long companyId;
    private Long warehouseId;
    private String exchangeOrderCode;
    private String exchangeLoyaltyCard;
    private String priceType;

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

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getExchangeOrderCode() {
        return exchangeOrderCode;
    }

    public void setExchangeOrderCode(String exchangeOrderCode) {
        this.exchangeOrderCode = exchangeOrderCode;
    }

    public String getExchangeLoyaltyCard() {
        return exchangeLoyaltyCard;
    }

    public void setExchangeLoyaltyCard(String exchangeLoyaltyCard) {
        this.exchangeLoyaltyCard = exchangeLoyaltyCard;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }
}
