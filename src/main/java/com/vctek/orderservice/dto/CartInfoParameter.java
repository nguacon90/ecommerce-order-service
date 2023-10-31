package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.util.SellSignal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CartInfoParameter {
    private String code;
    private Long userId;
    private Long customerId;
    private Long companyId;
    private String orderType;
    private String priceType;
    private Long warehouseId;
    private boolean isExchangeCart;
    private String cardNumber;
    private String sellSignal = SellSignal.WEB.toString();
    private Long externalId;
    private String externalCode;
    private Long distributorId;
    private String guid;
    private Long orderSourceId;
    private OrderSourceModel orderSourceModel;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isExchangeCart() {
        return isExchangeCart;
    }

    public void setExchangeCart(boolean exchangeCart) {
        isExchangeCart = exchangeCart;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public String getSellSignal() {
        return sellSignal;
    }

    public void setSellSignal(String sellSignal) {
        this.sellSignal = sellSignal;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public String getExternalCode() {
        return externalCode;
    }

    public void setExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }

    public Long getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(Long distributorId) {
        this.distributorId = distributorId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Long getOrderSourceId() {
        return orderSourceId;
    }

    public void setOrderSourceId(Long orderSourceId) {
        this.orderSourceId = orderSourceId;
    }

    public OrderSourceModel getOrderSourceModel() {
        return orderSourceModel;
    }

    public void setOrderSourceModel(OrderSourceModel orderSourceModel) {
        this.orderSourceModel = orderSourceModel;
    }
}
