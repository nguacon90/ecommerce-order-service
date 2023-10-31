package com.vctek.orderservice.promotionengine.promotionservice.model;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.ItemModel;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "promotion_order_entry_consume")
public class PromotionOrderEntryConsumedModel extends ItemModel {

    @Column(name = "code")
    private String code;

    @Column(name = "quantity")
    private Long quantity = 0l;

    @Column(name = "applied_quantity")
    private Long appliedQuantity = 0l;

    @Column(name = "adjust_unit_price")
    private Double adjustedUnitPrice = 0d;

    @Column(name = "order_entry_number")
    private Integer orderEntryNumber;


    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "promotion_result_id")
    private PromotionResultModel promotionResult;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, optional = false)
    @JoinColumn(name = "order_entry_id")
    private AbstractOrderEntryModel orderEntry;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Double getAdjustedUnitPrice() {
        return adjustedUnitPrice;
    }

    public void setAdjustedUnitPrice(Double adjustedUnitPrice) {
        this.adjustedUnitPrice = adjustedUnitPrice;
    }

    public Integer getOrderEntryNumber() {
        return orderEntryNumber;
    }

    public void setOrderEntryNumber(Integer orderEntryNumber) {
        this.orderEntryNumber = orderEntryNumber;
    }

    public PromotionResultModel getPromotionResult() {
        return promotionResult;
    }

    public void setPromotionResult(PromotionResultModel promotionResult) {
        this.promotionResult = promotionResult;
    }

    public AbstractOrderEntryModel getOrderEntry() {
        return orderEntry;
    }

    public void setOrderEntry(AbstractOrderEntryModel orderEntry) {
        this.orderEntry = orderEntry;
    }

    public Long getAppliedQuantity() {
        return appliedQuantity;
    }

    public void setAppliedQuantity(Long appliedQuantity) {
        this.appliedQuantity = appliedQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PromotionOrderEntryConsumedModel)) return false;
        PromotionOrderEntryConsumedModel that = (PromotionOrderEntryConsumedModel) o;
        if(getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId());
    }
}
