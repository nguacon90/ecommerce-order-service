package com.vctek.orderservice.promotionengine.promotionservice.model;


import com.vctek.orderservice.model.ItemModel;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "promotion_action")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type")
public class AbstractPromotionActionModel extends ItemModel {

    @Column(name = "marked_applied")
    protected boolean markedApplied;

    @Column(name = "guid")
    protected String guid;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "promotion_result_id" )
    private PromotionResultModel promotionResult;

    public boolean isMarkedApplied() {
        return markedApplied;
    }

    public void setMarkedApplied(boolean markedApplied) {
        this.markedApplied = markedApplied;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public PromotionResultModel getPromotionResult() {
        return promotionResult;
    }

    public void setPromotionResult(PromotionResultModel promotionResult) {
        this.promotionResult = promotionResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractPromotionActionModel)) return false;
        AbstractPromotionActionModel that = (AbstractPromotionActionModel) o;
        if(getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId());
    }
}
