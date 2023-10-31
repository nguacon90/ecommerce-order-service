package com.vctek.orderservice.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderSettingData {
    private Long id;
    private Long companyId;
    private String type;
    private Double amount;
    private String amountType;
    private String orderTypes;
    private String orderStatus;
    private String note;
    private List<OrderSettingDiscountData> settingDiscountData = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getAmountType() {
        return amountType;
    }

    public void setAmountType(String amountType) {
        this.amountType = amountType;
    }

    public String getOrderTypes() {
        return orderTypes;
    }

    public void setOrderTypes(String orderTypes) {
        this.orderTypes = orderTypes;
    }

    public List<OrderSettingDiscountData> getSettingDiscountData() {
        return settingDiscountData;
    }

    public void setSettingDiscountData(List<OrderSettingDiscountData> settingDiscountData) {
        this.settingDiscountData = settingDiscountData;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
