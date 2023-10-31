package com.vctek.orderservice.feignclient.dto;

public class OrderBillRequest {
    private Long billId;
    private Long companyId;
    private String orderCode;
    private Long warehouseId;
    private Long productId;
    private String productName;
    private String productBarcode;
    private String productSku;
    private Integer quantity;
    private Double discount;
    private String discountType;
    private Double price;
    private Long comboId;
    private Long subOrderEntryId;
    private Long orderEntryId;
    private Long toppingOptionId;
    private boolean saleOff;
    private String orderStatus;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductBarcode() {
        return productBarcode;
    }

    public void setProductBarcode(String productBarcode) {
        this.productBarcode = productBarcode;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getComboId() {
        return comboId;
    }

    public void setComboId(Long comboId) {
        this.comboId = comboId;
    }

    public Long getSubOrderEntryId() {
        return subOrderEntryId;
    }

    public void setSubOrderEntryId(Long subOrderEntryId) {
        this.subOrderEntryId = subOrderEntryId;
    }

    public Long getOrderEntryId() {
        return orderEntryId;
    }

    public void setOrderEntryId(Long orderEntryId) {
        this.orderEntryId = orderEntryId;
    }

    public Long getToppingOptionId() {
        return toppingOptionId;
    }

    public void setToppingOptionId(Long toppingOptionId) {
        this.toppingOptionId = toppingOptionId;
    }

    public boolean isSaleOff() {
        return saleOff;
    }

    public void setSaleOff(boolean saleOff) {
        this.saleOff = saleOff;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
