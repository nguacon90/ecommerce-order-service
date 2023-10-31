package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.OrderModel;

import java.util.List;

public class UpdateReturnOrderBillDTO {
    private Long companyId;
    private Long warehouseId;
    private Long returnOrderId;
    private OrderModel originOrder;
    private List<UpdateReturnOrderBillDetail> entries;

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

    public Long getReturnOrderId() {
        return returnOrderId;
    }

    public void setReturnOrderId(Long returnOrderId) {
        this.returnOrderId = returnOrderId;
    }

    public List<UpdateReturnOrderBillDetail> getEntries() {
        return entries;
    }

    public void setEntries(List<UpdateReturnOrderBillDetail> entries) {
        this.entries = entries;
    }

    public OrderModel getOriginOrder() {
        return originOrder;
    }

    public void setOriginOrder(OrderModel originOrder) {
        this.originOrder = originOrder;
    }
}
