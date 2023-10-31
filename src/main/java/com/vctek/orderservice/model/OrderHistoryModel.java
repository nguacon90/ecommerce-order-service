package com.vctek.orderservice.model;

import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "order_history")
@EntityListeners({AuditingEntityListener.class})
public class OrderHistoryModel extends ItemModel {

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "current_status")
    private String currentStatus;


    @Column(name = "extra_data")
    private String extraData;

    @Column(name = "type")
    private String type;


    @Column(name = "modified_time")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @DiffIgnore
    protected Date modifiedTime;

    @LastModifiedBy
    @Column(name = "modified_by")
    protected Long modifiedBy;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_id")
    private AbstractOrderModel order;

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public AbstractOrderModel getOrder() {
        return order;
    }

    public void setOrder(AbstractOrderModel order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderHistoryModel)) return false;
        OrderHistoryModel itemModel = (OrderHistoryModel) o;
        if(getId() == null && itemModel.getId() == null) return false;
        return getId().equals(itemModel.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
