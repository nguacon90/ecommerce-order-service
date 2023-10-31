package com.vctek.orderservice.model;

import javax.persistence.*;

@Entity
@Table(name = "order_type_setting_customer")
public class OrderTypeSettingCustomerModel extends ItemModel {

    @Column(name = "order_type")
    private String orderType;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "order_setting_customer_id", unique = true)
    private OrderSettingCustomerModel orderTypeSettingCustomers;

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public OrderSettingCustomerModel getOrderTypeSettingCustomers() {
        return orderTypeSettingCustomers;
    }

    public void setOrderTypeSettingCustomers(OrderSettingCustomerModel orderTypeSettingCustomers) {
        this.orderTypeSettingCustomers = orderTypeSettingCustomers;
    }
}
