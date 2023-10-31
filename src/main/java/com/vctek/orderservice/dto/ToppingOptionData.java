package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ToppingOptionData implements Serializable {
    private Long id;
    private Integer quantity;
    private Integer sugar;
    private Integer ice;
    private List<ToppingItemData> toppingItems = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<ToppingItemData> getToppingItems() {
        return toppingItems;
    }

    public void setToppingItems(List<ToppingItemData> toppingItems) {
        this.toppingItems = toppingItems;
    }
}
