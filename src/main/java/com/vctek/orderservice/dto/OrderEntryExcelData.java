package com.vctek.orderservice.dto;

import java.util.HashMap;
import java.util.Map;

public class OrderEntryExcelData {
    private String createdTime;
    private String type;
    private String id;
    private String createdName;
    private String companyName;
    private String warehouseName;
    private String customerName;
    private String customerPhone;
    private String address;
    private String barcode;
    private Long productId;
    private String sku;
    private String name;
    private String supplierProductName;
    private String comboSku;
    private String comboName;
    private Double price;
    private Long quantity;
    private Double totalPrice;
    private Double discount;
    private String discountType;
    private Double finalDiscount;
    private Double totalTax;
    private Double deliveryCost;
    private Double collaboratorShippingFee;
    private Double companyShippingFee;
    private Double shippingFee;
    private Double finalPrice;
    private Double totalPriceOrder;
    private String orderSource;
    private String orderStatus;
    private Double customerCash;
    private Double cash;
    private String originOrder;
    private String note;
    private String completeDate;
    private String originOrderSourceName;
    private Double vatExchange;
    private Double vat;
    private Double orderValue;
    private Double recommendedRetailPrice;
    private String cancelReason;
    private Map<Long, Double> paymentMethodAmount = new HashMap<>();
    private String customerNote;
    private String shippingCompanyName;
    private String unitName;
    private Double redeemAmount;
    private String tags;
    private String finishedProductImages;
    private String productVat;

    public String getCustomerNote() {
        return customerNote;
    }

    public void setCustomerNote(String customerNote) {
        this.customerNote = customerNote;
    }

    public String getShippingCompanyName() {
        return shippingCompanyName;
    }

    public void setShippingCompanyName(String shippingCompanyName) {
        this.shippingCompanyName = shippingCompanyName;
    }

    public Double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedName() {
        return createdName;
    }

    public void setCreatedName(String createdName) {
        this.createdName = createdName;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComboSku() {
        return comboSku;
    }

    public void setComboSku(String comboSku) {
        this.comboSku = comboSku;
    }

    public String getComboName() {
        return comboName;
    }

    public void setComboName(String comboName) {
        this.comboName = comboName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getFinalDiscount() {
        return finalDiscount;
    }

    public void setFinalDiscount(Double finalDiscount) {
        this.finalDiscount = finalDiscount;
    }

    public Double getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(Double totalTax) {
        this.totalTax = totalTax;
    }

    public Double getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(Double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Double getTotalPriceOrder() {
        return totalPriceOrder;
    }

    public void setTotalPriceOrder(Double totalPriceOrder) {
        this.totalPriceOrder = totalPriceOrder;
    }

    public String getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Double getCash() {
        return cash;
    }

    public void setCash(Double cash) {
        this.cash = cash;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getOriginOrder() {
        return originOrder;
    }

    public void setOriginOrder(String originOrder) {
        this.originOrder = originOrder;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getSupplierProductName() {
        return supplierProductName;
    }

    public void setSupplierProductName(String supplierProductName) {
        this.supplierProductName = supplierProductName;
    }

    public String getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(String completeDate) {
        this.completeDate = completeDate;
    }

    public Double getCollaboratorShippingFee() {
        return collaboratorShippingFee;
    }

    public void setCollaboratorShippingFee(Double collaboratorShippingFee) {
        this.collaboratorShippingFee = collaboratorShippingFee;
    }

    public Double getCompanyShippingFee() {
        return companyShippingFee;
    }

    public void setCompanyShippingFee(Double companyShippingFee) {
        this.companyShippingFee = companyShippingFee;
    }

    public Double getCustomerCash() {
        return customerCash;
    }

    public void setCustomerCash(Double customerCash) {
        this.customerCash = customerCash;
    }

    public String getOriginOrderSourceName() {
        return originOrderSourceName;
    }

    public void setOriginOrderSourceName(String originOrderSourceName) {
        this.originOrderSourceName = originOrderSourceName;
    }

    public Double getVatExchange() {
        return vatExchange;
    }

    public void setVatExchange(Double vatExchange) {
        this.vatExchange = vatExchange;
    }

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public Double getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(Double orderValue) {
        this.orderValue = orderValue;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Map<Long, Double> getPaymentMethodAmount() {
        return paymentMethodAmount;
    }

    public void setPaymentMethodAmount(Map<Long, Double> paymentMethodAmount) {
        this.paymentMethodAmount = paymentMethodAmount;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Double getRecommendedRetailPrice() {
        return recommendedRetailPrice;
    }

    public void setRecommendedRetailPrice(Double recommendedRetailPrice) {
        this.recommendedRetailPrice = recommendedRetailPrice;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public Double getRedeemAmount() {
        return redeemAmount;
    }

    public void setRedeemAmount(Double redeemAmount) {
        this.redeemAmount = redeemAmount;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getFinishedProductImages() {
        return finishedProductImages;
    }

    public void setFinishedProductImages(String finishedProductImages) {
        this.finishedProductImages = finishedProductImages;
    }

    public String getProductVat() {
        return productVat;
    }

    public void setProductVat(String productVat) {
        this.productVat = productVat;
    }
}
