package com.vctek.orderservice.model;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tag")
public class TagModel extends ItemModel {

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "name")
    private String name;

    @ManyToMany(mappedBy = "tags", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private Set<AbstractOrderModel> orderModels;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<AbstractOrderModel> getOrderModels() {
        return orderModels;
    }

    public void setOrderModels(Set<AbstractOrderModel> orderModels) {
        this.orderModels = orderModels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagModel)) return false;
        TagModel that = (TagModel) o;
        if(this.getId() == null && that.getId() == null) return false;
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
