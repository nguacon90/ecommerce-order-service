package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoveSubOrderEntryRequest extends AddSubOrderEntryRequest{
    private Long subEntryId;

    public Long getSubEntryId() {
        return subEntryId;
    }

    public void setSubEntryId(Long subEntryId) {
        this.subEntryId = subEntryId;
    }
}
