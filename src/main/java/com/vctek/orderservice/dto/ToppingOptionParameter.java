package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;

public class ToppingOptionParameter {
    private AbstractOrderModel abstractOrderModel;
    private AbstractOrderEntryModel abstractOrderEntryModel;
    private Long id;
    private Integer quantity;
    private Integer sugar;
    private Integer ice;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getSugar() {
        return sugar;
    }

    public void setSugar(Integer sugar) {
        this.sugar = sugar;
    }

    public Integer getIce() {
        return ice;
    }

    public void setIce(Integer ice) {
        this.ice = ice;
    }
}
