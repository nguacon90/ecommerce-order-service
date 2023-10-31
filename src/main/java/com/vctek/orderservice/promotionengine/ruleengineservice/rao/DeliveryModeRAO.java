package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.io.Serializable;
import java.math.BigDecimal;

public class DeliveryModeRAO implements Serializable {
    private String code;
    private BigDecimal cost;
    private String currencyIsoCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getCurrencyIsoCode() {
        return currencyIsoCode;
    }

    public void setCurrencyIsoCode(String currencyIsoCode) {
        this.currencyIsoCode = currencyIsoCode;
    }
}
