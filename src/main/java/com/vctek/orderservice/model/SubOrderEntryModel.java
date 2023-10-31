package com.vctek.orderservice.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "sub_order_entry")
public class SubOrderEntryModel extends ItemModel{

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "origin_price")
    private Double originPrice;

    @Column(name = "price")
    private Double price;

    @Column(name = "discount_value")
    private Double discountValue;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "final_price")
    private Double finalPrice;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name ="reward_amount")
    private Double rewardAmount;

    @Column(name ="combo_group_number")
    private Integer comboGroupNumber;

    @Column(name = "vat")
    private Double vat;

    @Column(name = "vat_type")
    private String vatType;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "order_entry_id", referencedColumnName = "id")
    private AbstractOrderEntryModel orderEntry;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Double getOriginPrice() {
        return originPrice;
    }

    public void setOriginPrice(Double originPrice) {
        this.originPrice = originPrice;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(Double discountValue) {
        this.discountValue = discountValue;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public AbstractOrderEntryModel getOrderEntry() {
        return orderEntry;
    }

    public void setOrderEntry(AbstractOrderEntryModel orderEntry) {
        this.orderEntry = orderEntry;
    }

    public Double getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(Double rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    public Integer getComboGroupNumber() {
        return comboGroupNumber;
    }

    public void setComboGroupNumber(Integer comboGroupNumber) {
        this.comboGroupNumber = comboGroupNumber;
    }

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public String getVatType() {
        return vatType;
    }

    public void setVatType(String vatType) {
        this.vatType = vatType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubOrderEntryModel)) return false;
        SubOrderEntryModel that = (SubOrderEntryModel) o;
        if(this.getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
