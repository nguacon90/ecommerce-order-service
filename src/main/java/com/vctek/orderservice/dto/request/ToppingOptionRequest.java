package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Calendar;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ToppingOptionRequest {
    private Long id;
    private Integer quantity;
    private Integer sugar;
    private Integer ice;
    private Long entryId;
    private Long companyId;
    private Long timeRequest;
    private List<ToppingItemRequest> toppingItems;

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

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public List<ToppingItemRequest> getToppingItems() {
        return toppingItems;
    }

    public void setToppingItems(List<ToppingItemRequest> toppingItems) {
        this.toppingItems = toppingItems;
    }

    public Long getTimeRequest() {
        return timeRequest;
    }

    public void setTimeRequest(Long timeRequest) {
        if(timeRequest == null) {
            this.timeRequest = Calendar.getInstance().getTimeInMillis();
            return;
        }

        this.timeRequest = timeRequest;
    }

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }
}
