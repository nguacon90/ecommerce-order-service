package com.vctek.orderservice.dto;

public class ReturnOrderVatData {
    private Double originOrderVat;
    private Double returnOrderVat;

    public Double getOriginOrderVat() {
        return originOrderVat;
    }

    public void setOriginOrderVat(Double originOrderVat) {
        this.originOrderVat = originOrderVat;
    }

    public Double getReturnOrderVat() {
        return returnOrderVat;
    }

    public void setReturnOrderVat(Double returnOrderVat) {
        this.returnOrderVat = returnOrderVat;
    }
}
