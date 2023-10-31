package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;

public class CommerceAddComboParameter {
    private AbstractOrderModel abstractOrderModel;
    private Long productComboId;
    private long quantityToAdd;
    private AbstractOrderEntryModel entryModel;

    public AbstractOrderModel getAbstractOrderModel() {
        return abstractOrderModel;
    }

    public void setAbstractOrderModel(AbstractOrderModel abstractOrderModel) {
        this.abstractOrderModel = abstractOrderModel;
    }

    public Long getProductComboId() {
        return productComboId;
    }

    public void setProductComboId(Long productComboId) {
        this.productComboId = productComboId;
    }

    public long getQuantityToAdd() {
        return quantityToAdd;
    }

    public void setQuantityToAdd(long quantityToAdd) {
        this.quantityToAdd = quantityToAdd;
    }

    public AbstractOrderEntryModel getEntryModel() {
        return entryModel;
    }

    public void setEntryModel(AbstractOrderEntryModel entryModel) {
        this.entryModel = entryModel;
    }

}
