package com.vctek.orderservice.dto;

import com.vctek.orderservice.model.OrderModel;
import com.vctek.util.OrderStatus;

public class CommerceChangeOrderStatusParameter {
    private OrderModel order;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private String cancelText;
    private boolean confirmDiscount;
    private Long importDetailId;

    public CommerceChangeOrderStatusParameter(OrderModel order, OrderStatus oldStatus, OrderStatus newStatus) {
        this.order = order;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public OrderModel getOrder() {
        return order;
    }

    public void setOrder(OrderModel order) {
        this.order = order;
    }

    public OrderStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(OrderStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(OrderStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getCancelText() {
        return cancelText;
    }

    public void setCancelText(String cancelText) {
        this.cancelText = cancelText;
    }

    public boolean isConfirmDiscount() {
        return confirmDiscount;
    }

    public void setConfirmDiscount(boolean confirmDiscount) {
        this.confirmDiscount = confirmDiscount;
    }

    public Long getImportDetailId() {
        return importDetailId;
    }

    public void setImportDetailId(Long importDetailId) {
        this.importDetailId = importDetailId;
    }
}
