package com.vctek.orderservice.promotionengine.promotionservice.model;

import com.vctek.orderservice.model.ItemModel;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "promotion")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type")
public class AbstractPromotionModel extends ItemModel {

    @Column(name = "code")
    protected String code;

    @Column(name = "title")
    protected String title;

    @Column(name = "description")
    protected String description;

    @Column(name = "start_date")
    protected Date startDate;

    @Column(name = "end_date")
    protected Date endDate;

    @Column(name = "enabled")
    protected boolean enabled;

    @Column(name = "priority")
    protected Integer priority;

    @OneToMany(mappedBy = "promotion", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<PromotionResultModel> promotionResults = new HashSet<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Set<PromotionResultModel> getPromotionResults() {
        return promotionResults;
    }

    public void setPromotionResults(Set<PromotionResultModel> promotionResults) {
        this.promotionResults = promotionResults;
    }
}
