package com.vctek.orderservice.feignclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.dto.ToppingOptionData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BillDetailData implements Serializable {
    private static final long serialVersionUID = -2978179606167440823L;
    private Long id;
    private Long productId;
    private Integer quantity;
    private Double discount;
    private Double price;
    private Integer originQuantity;
    private Double originFinalDiscount;
    private Double totalPrice;
    private Double discountValue;
    private Double finalPrice;
    private Long toppingOptionId;
    private Long orderEntryId;
    private Long comboId;
    private Integer comboQuantity;
    private Long subOrderEntryId;
    private Double originBasePrice;
    private boolean giveAway;
    private List<ToppingOptionData> toppingOptions = new ArrayList<>();
    private List<BillDetailData> subOrderEntries = new ArrayList<>();
    private boolean saleOff;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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

    public Double getFinalDiscount() {
        return this.discount;
    }

    public Integer getOriginQuantity() {
        return originQuantity;
    }

    public void setOriginQuantity(Integer originQuantity) {
        this.originQuantity = originQuantity;
    }

    public Double getOriginFinalDiscount() {
        return originFinalDiscount;
    }

    public void setOriginFinalDiscount(Double originFinalDiscount) {
        this.originFinalDiscount = originFinalDiscount;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(Double discountValue) {
        this.discountValue = discountValue;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public List<ToppingOptionData> getToppingOptions() {
        return toppingOptions;
    }

    public void setToppingOptions(List<ToppingOptionData> toppingOptions) {
        this.toppingOptions = toppingOptions;
    }

    public Long getToppingOptionId() {
        return toppingOptionId;
    }

    public void setToppingOptionId(Long toppingOptionId) {
        this.toppingOptionId = toppingOptionId;
    }

    public Long getOrderEntryId() {
        return orderEntryId;
    }

    public void setOrderEntryId(Long orderEntryId) {
        this.orderEntryId = orderEntryId;
    }

    public Long getComboId() {
        return comboId;
    }

    public void setComboId(Long comboId) {
        this.comboId = comboId;
    }

    public Integer getComboQuantity() {
        return comboQuantity;
    }

    public void setComboQuantity(Integer comboQuantity) {
        this.comboQuantity = comboQuantity;
    }

    public Long getSubOrderEntryId() {
        return subOrderEntryId;
    }

    public void setSubOrderEntryId(Long subOrderEntryId) {
        this.subOrderEntryId = subOrderEntryId;
    }

    public List<BillDetailData> getSubOrderEntries() {
        return subOrderEntries;
    }

    public void setSubOrderEntries(List<BillDetailData> subOrderEntries) {
        this.subOrderEntries = subOrderEntries;
    }

    public Double getOriginBasePrice() {
        return originBasePrice;
    }

    public void setOriginBasePrice(Double originBasePrice) {
        this.originBasePrice = originBasePrice;
    }

    public boolean isGiveAway() {
        return giveAway;
    }

    public void setGiveAway(boolean giveAway) {
        this.giveAway = giveAway;
    }

    public boolean isSaleOff() {
        return saleOff;
    }

    public void setSaleOff(boolean saleOff) {
        this.saleOff = saleOff;
    }
}
