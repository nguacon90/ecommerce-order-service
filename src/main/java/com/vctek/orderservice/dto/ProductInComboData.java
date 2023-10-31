package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductInComboData {
    private Long id;
    private String name;
    private String sku;
    private String barcode;
    private Double price;
    private Integer comboGroupNumber;
    private Integer quantity;
    private boolean updateQuantity = false;

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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getComboGroupNumber() {
        return comboGroupNumber;
    }

    public void setComboGroupNumber(Integer comboGroupNumber) {
        this.comboGroupNumber = comboGroupNumber;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public boolean isUpdateQuantity() {
        return updateQuantity;
    }

    public void setUpdateQuantity(boolean updateQuantity) {
        this.updateQuantity = updateQuantity;
    }
}
