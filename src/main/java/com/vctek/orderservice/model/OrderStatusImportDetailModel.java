package com.vctek.orderservice.model;

import javax.persistence.*;

@Entity
@Table(name = "order_status_import_detail")
public class OrderStatusImportDetailModel extends ItemModel{

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "old_order_status")
    private String oldOrderStatus;

    @Column(name = "new_order_status")
    private String newOrderStatus;

    @Column(name = "status")
    private String status;

    @Column(name = "note")
    private String note;

    @Column(name = "integration_service_status")
    private String integrationServiceStatus;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "order_status_import_id")
    private OrderStatusImportModel orderStatusImportModel;

    public OrderStatusImportModel getOrderStatusImportModel() {
        return orderStatusImportModel;
    }

    public void setOrderStatusImportModel(OrderStatusImportModel orderStatusImportModel) {
        this.orderStatusImportModel = orderStatusImportModel;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getOldOrderStatus() {
        return oldOrderStatus;
    }

    public void setOldOrderStatus(String oldOrderStatus) {
        this.oldOrderStatus = oldOrderStatus;
    }

    public String getNewOrderStatus() {
        return newOrderStatus;
    }

    public void setNewOrderStatus(String newOrderStatus) {
        this.newOrderStatus = newOrderStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getIntegrationServiceStatus() {
        return integrationServiceStatus;
    }

    public void setIntegrationServiceStatus(String integrationServiceStatus) {
        this.integrationServiceStatus = integrationServiceStatus;
    }
}
