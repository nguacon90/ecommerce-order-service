package com.vctek.orderservice.promotionengine.promotionservice.model;

import com.vctek.orderservice.model.ItemModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "promotion_campaign")
public class CampaignModel extends ItemModel {
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "name")
    private String name;

    @Column(name = "status")
    private String status;

    @ManyToMany(mappedBy = "campaigns")
    private Set<PromotionSourceRuleModel> promotionSourceRules = new HashSet<>();

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

    public Set<PromotionSourceRuleModel> getPromotionSourceRules() {
        return promotionSourceRules;
    }

    public void setPromotionSourceRules(Set<PromotionSourceRuleModel> promotionSourceRules) {
        this.promotionSourceRules = promotionSourceRules;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
