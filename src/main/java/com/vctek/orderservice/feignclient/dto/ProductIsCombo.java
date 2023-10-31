package com.vctek.orderservice.feignclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductIsCombo implements Serializable {
    private static final long serialVersionUID = 22356159883129461L;
    private Long id;
    private Long companyId;
    private boolean isCombo;
    private String comboType;
    private Double price;
    private Double wholesalePrice;
    private int comboAvaiableStock;
    private List<BasicProductData> comboProducts = new ArrayList<>();

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

    public boolean isCombo() {
        return isCombo;
    }

    public void setCombo(boolean combo) {
        isCombo = combo;
    }

    public String getComboType() {
        return comboType;
    }

    public void setComboType(String comboType) {
        this.comboType = comboType;
    }

    public List<BasicProductData> getComboProducts() {
        return comboProducts;
    }

    public void setComboProducts(List<BasicProductData> comboProducts) {
        this.comboProducts = comboProducts;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getWholesalePrice() {
        return wholesalePrice;
    }

    public void setWholesalePrice(Double wholesalePrice) {
        this.wholesalePrice = wholesalePrice;
    }

    public int getComboAvaiableStock() {
        return comboAvaiableStock;
    }

    public void setComboAvaiableStock(int comboAvaiableStock) {
        this.comboAvaiableStock = comboAvaiableStock;
    }
}
