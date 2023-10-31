package com.vctek.orderservice.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "order_setting_customer_option")
public class OrderSettingCustomerOptionModel extends AuditModel {

    @Column(name = "name")
    private String name;

    @Column(name = "deleted")
    private boolean deleted;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "order_setting_customer_id", unique = true)
    private OrderSettingCustomerModel orderSettingCustomerModel;

    @ManyToMany(mappedBy = "orderSettingCustomerOptionModels")
    private Set<AbstractOrderModel> abstractOrderModels = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public OrderSettingCustomerModel getOrderSettingCustomerModel() {
        return orderSettingCustomerModel;
    }

    public void setOrderSettingCustomerModel(OrderSettingCustomerModel orderSettingCustomerModel) {
        this.orderSettingCustomerModel = orderSettingCustomerModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderSettingCustomerOptionModel)) return false;
        OrderSettingCustomerOptionModel that = (OrderSettingCustomerOptionModel) o;
        if(this.getId() == null && that.getId() ==  null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public Set<AbstractOrderModel> getAbstractOrderModels() {
        return abstractOrderModels;
    }

    public void setAbstractOrderModels(Set<AbstractOrderModel> abstractOrderModels) {
        this.abstractOrderModels = abstractOrderModels;
    }
}
