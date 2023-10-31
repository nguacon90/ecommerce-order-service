package com.vctek.orderservice.model;

import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "order_status_import")
@EntityListeners({AuditingEntityListener.class})
public class OrderStatusImportModel extends ItemModel{

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "order_status")
    private String orderStatus;

    @Column(name = "status")
    private String status;

    @Column(name = "created_time")
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @DiffIgnore
    private Date createdTime;

    @CreatedBy
    @Column(name = "created_by")
    @DiffIgnore
    private Long createdBy;

    @OneToMany(mappedBy = "orderStatusImportModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderStatusImportDetailModel> orderStatusImportDetailModels = new ArrayList<>();

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public List<OrderStatusImportDetailModel> getOrderStatusImportDetailModels() {
        return orderStatusImportDetailModels;
    }

    public void setOrderStatusImportDetailModels(List<OrderStatusImportDetailModel> orderStatusImportDetailModels) {
        this.orderStatusImportDetailModels = orderStatusImportDetailModels;
    }
}
