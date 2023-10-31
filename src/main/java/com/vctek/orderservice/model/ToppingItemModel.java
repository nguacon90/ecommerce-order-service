package com.vctek.orderservice.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "topping_item")
public class ToppingItemModel extends ItemModel{

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "base_price")
    private Double basePrice;

    @Column(name = "discount_order_to_item")
    private Double discountOrderToItem;

    @Column(name = "discount")
    private Double discount;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "reward_amount")
    private Double rewardAmount;

    @Column(name = "vat")
    private Double vat;

    @Column(name = "vat_type")
    private String vatType;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "topping_option_id", referencedColumnName = "id")
    private ToppingOptionModel toppingOptionModel;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Double getDiscountOrderToItem() {
        return discountOrderToItem;
    }

    public void setDiscountOrderToItem(Double discountOrderToItem) {
        this.discountOrderToItem = discountOrderToItem;
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

    public ToppingOptionModel getToppingOptionModel() {
        return toppingOptionModel;
    }

    public void setToppingOptionModel(ToppingOptionModel toppingOptionModel) {
        this.toppingOptionModel = toppingOptionModel;
    }

    public Double getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(Double rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToppingItemModel that = (ToppingItemModel) o;
        if(this.getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
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
}
