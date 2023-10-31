package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.ToppingOptionModel;

public class ToppingItemParameter {
    private Long toppingItemId;
    private AbstractOrderModel abstractOrderModel;
    private AbstractOrderEntryModel abstractOrderEntryModel;
    private ToppingOptionModel toppingOptionModel;
    private Long productId;
    private Integer quantity;
    private Double price;
    private Double discount;
    private String discountType;

    public Long getToppingItemId() {
        return toppingItemId;
    }

    public void setToppingItemId(Long toppingItemId) {
        this.toppingItemId = toppingItemId;
    }

    public AbstractOrderModel getAbstractOrderModel() {
        return abstractOrderModel;
    }

    public void setAbstractOrderModel(AbstractOrderModel abstractOrderModel) {
        this.abstractOrderModel = abstractOrderModel;
    }

    public AbstractOrderEntryModel getAbstractOrderEntryModel() {
        return abstractOrderEntryModel;
    }

    public void setAbstractOrderEntryModel(AbstractOrderEntryModel abstractOrderEntryModel) {
        this.abstractOrderEntryModel = abstractOrderEntryModel;
    }

    public ToppingOptionModel getToppingOptionModel() {
        return toppingOptionModel;
    }

    public void setToppingOptionModel(ToppingOptionModel toppingOptionModel) {
        this.toppingOptionModel = toppingOptionModel;
    }

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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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
}
