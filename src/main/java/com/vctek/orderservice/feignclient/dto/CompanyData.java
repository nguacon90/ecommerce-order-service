package com.vctek.orderservice.feignclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyData implements Serializable {
    private static final long serialVersionUID = 5419424127912032810L;
    private Long id;
    private String name;
    private String phone;
    private String email;
    private Long addressId;
    private boolean sellLessZero;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public boolean isSellLessZero() {
        return sellLessZero;
    }

    public void setSellLessZero(boolean sellLessZero) {
        this.sellLessZero = sellLessZero;
    }
}
