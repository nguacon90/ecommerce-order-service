package com.vctek.orderservice.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "order_source")
public class OrderSourceModel extends AuditModel {
    @Column(name = "name")
    private String name;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "orderSourceModel")
    private Set<OrderModel> orderModels;

    @Column(name = "transaction_name")
    private String transactionName;

    @Column(name = "display_order")
    private Integer order;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<OrderModel> getOrderModels() {
        return orderModels;
    }

    public void setOrderModels(Set<OrderModel> orderModels) {
        this.orderModels = orderModels;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}

