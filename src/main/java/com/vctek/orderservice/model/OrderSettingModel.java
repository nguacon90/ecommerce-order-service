package com.vctek.orderservice.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_setting")
public class OrderSettingModel extends ItemModel {

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "type")
    private String type;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "amount_type")
    private String amountType;

    @Column(name = "order_types")
    private String orderTypes;

    @Column(name = "order_status")
    private String orderStatus;

    @Column(name = "note")
    private String note;

    @OneToMany(mappedBy = "orderSetting", cascade = CascadeType.ALL)
    private List<OrderSettingDiscountModel> settingDiscountModel = new ArrayList<>();

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

    public List<OrderSettingDiscountModel> getSettingDiscountModel() {
        return settingDiscountModel;
    }

    public void setSettingDiscountModel(List<OrderSettingDiscountModel> settingDiscountModel) {
        this.settingDiscountModel = settingDiscountModel;
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
