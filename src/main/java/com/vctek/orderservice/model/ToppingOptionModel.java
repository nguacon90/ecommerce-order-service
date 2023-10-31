package com.vctek.orderservice.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "topping_option")
public class ToppingOptionModel extends ItemModel {

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "sugar")
    private Integer sugar;

    @Column(name = "ice")
    private Integer ice;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "order_entry_id", referencedColumnName = "id")
    private AbstractOrderEntryModel orderEntry;

    @OneToMany(mappedBy = "toppingOptionModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ToppingItemModel> toppingItemModels = new HashSet<>();

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getSugar() {
        return sugar;
    }

    public void setSugar(Integer sugar) {
        this.sugar = sugar;
    }

    public Integer getIce() {
        return ice;
    }

    public void setIce(Integer ice) {
        this.ice = ice;
    }

    public AbstractOrderEntryModel getOrderEntry() {
        return orderEntry;
    }

    public void setOrderEntry(AbstractOrderEntryModel orderEntry) {
        this.orderEntry = orderEntry;
    }

    public Set<ToppingItemModel> getToppingItemModels() {
        return toppingItemModels;
    }

    public void setToppingItemModels(Set<ToppingItemModel> toppingItemModels) {
        this.toppingItemModels = toppingItemModels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToppingOptionModel that = (ToppingOptionModel) o;
        if(this.getId() == null && that.getId() == null) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
