package com.vctek.orderservice.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderData extends AbstractOrderData {
    private Long billId;
    private Long returnOrderId;
    private List<Long> returnOrderIds;
    private Long shippingCompanyId;
    private Long orderSourceId;
    private Double totalAwardPoint;
    private Long confirmDiscountBy;
    private List<TagData> tags = new ArrayList<>();

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Long getReturnOrderId() {
        return returnOrderId;
    }

    public void setReturnOrderId(Long returnOrderId) {
        this.returnOrderId = returnOrderId;
    }

    @Override
    public List<Long> getReturnOrderIds() {
        return returnOrderIds;
    }

    @Override
    public void setReturnOrderIds(List<Long> returnOrderIds) {
        this.returnOrderIds = returnOrderIds;
    }

    public Long getShippingCompanyId() {
        return shippingCompanyId;
    }

    public void setShippingCompanyId(Long shippingCompanyId) {
        this.shippingCompanyId = shippingCompanyId;
    }

    public Long getOrderSourceId() {
        return orderSourceId;
    }

    public void setOrderSourceId(Long orderSourceId) {
        this.orderSourceId = orderSourceId;
    }

    public Double getTotalAwardPoint() {
        return totalAwardPoint;
    }

    public void setTotalAwardPoint(Double totalAwardPoint) {
        this.totalAwardPoint = totalAwardPoint;
    }

    public Long getConfirmDiscountBy() {
        return confirmDiscountBy;
    }

    public void setConfirmDiscountBy(Long confirmDiscountBy) {
        this.confirmDiscountBy = confirmDiscountBy;
    }

    public List<TagData> getTags() {
        return tags;
    }

    public void setTags(List<TagData> tags) {
        this.tags = tags;
    }
}
