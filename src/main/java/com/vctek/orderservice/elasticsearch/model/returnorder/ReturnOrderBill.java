package com.vctek.orderservice.elasticsearch.model.returnorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnOrderBill implements Serializable {
    private static final long serialVersionUID = 5267402478568513076L;
    private Long id;
    private Long companyId;
    private Long warehouseId;
    private Double finalPrice;

    @Field(type = FieldType.Nested)
    private List<ReturnOrderEntry> entries;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public List<ReturnOrderEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ReturnOrderEntry> entries) {
        this.entries = entries;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }
}
