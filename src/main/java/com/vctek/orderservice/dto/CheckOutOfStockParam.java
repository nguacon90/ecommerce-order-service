package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.AbstractOrderModel;

public class CheckOutOfStockParam {
    private Long productId;
    private Long companyId;
    private Long warehouseId;
    private long quantity;
    private AbstractOrderModel abstractOrderModel;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public AbstractOrderModel getAbstractOrderModel() {
        return abstractOrderModel;
    }

    public void setAbstractOrderModel(AbstractOrderModel abstractOrderModel) {
        this.abstractOrderModel = abstractOrderModel;
    }
}
