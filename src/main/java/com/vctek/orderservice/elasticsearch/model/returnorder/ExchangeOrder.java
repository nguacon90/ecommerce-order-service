package com.vctek.orderservice.elasticsearch.model.returnorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeOrder implements Serializable {
    private static final long serialVersionUID = -6991327725901724333L;
    private String code;
    private Double finalPrice;
    private Long warehouseId;
    private Double vatExchange;

    @Field(type = FieldType.Nested)
    private List<ReturnOrderEntry> entries;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public List<ReturnOrderEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ReturnOrderEntry> entries) {
        this.entries = entries;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Double getVatExchange() {
        return vatExchange;
    }

    public void setVatExchange(Double vatExchange) {
        this.vatExchange = vatExchange;
    }
}
