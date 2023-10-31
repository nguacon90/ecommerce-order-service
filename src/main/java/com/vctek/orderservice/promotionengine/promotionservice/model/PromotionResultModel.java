package com.vctek.orderservice.promotionengine.promotionservice.model;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.ItemModel;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "promotion_results")
public class PromotionResultModel extends ItemModel {

    @Column(name = "message_fired")
    private String messageFired;

    @Column(name = "rules_module_name")
    private String rulesModuleName;

    @Column(name = "module_version")
    private Long moduleVersion;

    @Column(name = "rule_version")
    private Long ruleVersion;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "certainty")
    private Float certainty;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "promotion_id")
    private AbstractPromotionModel promotion;

    @OneToMany(mappedBy = "promotionResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AbstractPromotionActionModel> actions = new HashSet<>();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private AbstractOrderModel order;

    @OneToMany(mappedBy = "promotionResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PromotionOrderEntryConsumedModel> consumedEntries = new HashSet<>();

    @OneToOne(mappedBy = "promotionResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private PromotionBudgetConsumeModel budgetConsumeModel;

    public String getRulesModuleName() {
        return rulesModuleName;
    }

    public void setRulesModuleName(String rulesModuleName) {
        this.rulesModuleName = rulesModuleName;
    }

    public Long getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(Long moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public Long getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(Long ruleVersion) {
        this.ruleVersion = ruleVersion;
    }

    public String getMessageFired() {
        return messageFired;
    }

    public void setMessageFired(String messageFired) {
        this.messageFired = messageFired;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public AbstractPromotionModel getPromotion() {
        return promotion;
    }

    public void setPromotion(AbstractPromotionModel promotion) {
        this.promotion = promotion;
    }

    public Set<AbstractPromotionActionModel> getActions() {
        return actions;
    }

    public void setActions(Set<AbstractPromotionActionModel> actions) {
        this.actions = actions;
    }

    public AbstractOrderModel getOrder() {
        return order;
    }

    public void setOrder(AbstractOrderModel order) {
        this.order = order;
    }

    public Float getCertainty() {
        return certainty;
    }

    public void setCertainty(Float certainty) {
        this.certainty = certainty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PromotionResultModel)) return false;
        PromotionResultModel that = (PromotionResultModel) o;
        if(getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getOrderCode(), that.getOrderCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getOrderCode());
    }

    public Set<PromotionOrderEntryConsumedModel> getConsumedEntries() {
        return consumedEntries;
    }

    public void setConsumedEntries(Set<PromotionOrderEntryConsumedModel> consumedEntries) {
        this.consumedEntries = consumedEntries;
    }

    public PromotionBudgetConsumeModel getBudgetConsumeModel() {
        return budgetConsumeModel;
    }

    public void setBudgetConsumeModel(PromotionBudgetConsumeModel budgetConsumeModel) {
        this.budgetConsumeModel = budgetConsumeModel;
    }
}
