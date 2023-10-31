package com.vctek.orderservice.elasticsearch.model.returnorder;

import java.io.Serializable;

public class ReturnOrderEntry implements Serializable {
    private static final long serialVersionUID = -1303981974504565328L;
    private Long productId;
    private String name;
    private String productName;
    private String productSku;
    private String supplierProductName;
    private Double productVat;
    private String productVatType;
    private Long quantity;
    private Double discount;
    private Double price;
    private String dType;
    private Long comboId;
    private String comboName;
    private String comboSku;
    private Double vat;
    private Double vatExchange;
    private Double orderValue;
    private Double shippingTransport;

    public Double getShippingTransport() {
        return shippingTransport;
    }

    public void setShippingTransport(Double shippingTransport) {
        this.shippingTransport = shippingTransport;
    }

    public Double getOrderValue() {
        return orderValue;
    }
    public void setOrderValue(Double orderValue) {
        this.orderValue = orderValue;
    }

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public Double getVatExchange() {
        return vatExchange;
    }

    public void setVatExchange(Double vatExchange) {
        this.vatExchange = vatExchange;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getdType() {
        return dType;
    }

    public void setdType(String dType) {
        this.dType = dType;
    }

    public Long getComboId() {
        return comboId;
    }

    public void setComboId(Long comboId) {
        this.comboId = comboId;
    }

    public String getComboName() {
        return comboName;
    }

    public void setComboName(String comboName) {
        this.comboName = comboName;
    }

    public String getComboSku() {
        return comboSku;
    }

    public void setComboSku(String comboSku) {
        this.comboSku = comboSku;
    }

    public String getSupplierProductName() {
        return supplierProductName;
    }

    public void setSupplierProductName(String supplierProductName) {
        this.supplierProductName = supplierProductName;
    }

    public Double getProductVat() {
        return productVat;
    }

    public void setProductVat(Double productVat) {
        this.productVat = productVat;
    }

    public String getProductVatType() {
        return productVatType;
    }

    public void setProductVatType(String productVatType) {
        this.productVatType = productVatType;
    }
}
