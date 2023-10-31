package com.vctek.orderservice.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_setting_customer")
public class OrderSettingCustomerModel extends AuditModel {

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "name")
    private String name;

    @Column(name = "is_default")
    private boolean isDefault;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "deleted")
    private boolean deleted;

    @OneToMany(mappedBy = "orderSettingCustomerModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderSettingCustomerOptionModel> optionModels = new ArrayList<>();

    @OneToMany(mappedBy = "orderTypeSettingCustomers", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderTypeSettingCustomerModel> orderTypeSettingCustomerModels = new ArrayList<>();

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public List<OrderSettingCustomerOptionModel> getOptionModels() {
        return optionModels;
    }

    public void setOptionModels(List<OrderSettingCustomerOptionModel> optionModels) {
        this.optionModels = optionModels;
    }

    public List<OrderTypeSettingCustomerModel> getOrderTypeSettingCustomerModels() {
        return orderTypeSettingCustomerModels;
    }

    public void setOrderTypeSettingCustomerModels(List<OrderTypeSettingCustomerModel> orderTypeSettingCustomerModels) {
        this.orderTypeSettingCustomerModels = orderTypeSettingCustomerModels;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
