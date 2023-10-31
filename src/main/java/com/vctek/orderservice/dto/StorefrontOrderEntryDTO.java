package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.dto.request.AddSubOrderEntryRequest;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StorefrontOrderEntryDTO extends OrderEntryDTO {
    private List<AddSubOrderEntryRequest> subOrderEntries;

    public List<AddSubOrderEntryRequest> getSubOrderEntries() {
        return subOrderEntries;
    }

    public void setSubOrderEntries(List<AddSubOrderEntryRequest> subOrderEntries) {
        this.subOrderEntries = subOrderEntries;
    }
}
