package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;

public class CommerceCartModification {
    private long quantityAdded;
    private AbstractOrderEntryModel entry;
    private Long productId;
    private AbstractOrderModel order;
    private boolean deletedEntry;
    private boolean updatePrice;

    public long getQuantityAdded() {
        return quantityAdded;
    }

    public void setQuantityAdded(long quantityAdded) {
        this.quantityAdded = quantityAdded;
    }

    public AbstractOrderEntryModel getEntry() {
        return entry;
    }

    public void setEntry(AbstractOrderEntryModel entry) {
        this.entry = entry;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public AbstractOrderModel getOrder() {
        return order;
    }

    public void setOrder(AbstractOrderModel order) {
        this.order = order;
    }

    public boolean isDeletedEntry() {
        return deletedEntry;
    }

    public void setDeletedEntry(boolean deletedEntry) {
        this.deletedEntry = deletedEntry;
    }

    public boolean isUpdatePrice() {
        return updatePrice;
    }

    public void setUpdatePrice(boolean updatePrice) {
        this.updatePrice = updatePrice;
    }
}
