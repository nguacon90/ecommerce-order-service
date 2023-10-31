package com.vctek.orderservice.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "order_storefront_setup")
public class OrderStorefrontSetupModel extends AuditModel {

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "order_source_id")
    private Long orderSourceId;

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

    public Long getOrderSourceId() {
        return orderSourceId;
    }

    public void setOrderSourceId(Long orderSourceId) {
        this.orderSourceId = orderSourceId;
    }

}
