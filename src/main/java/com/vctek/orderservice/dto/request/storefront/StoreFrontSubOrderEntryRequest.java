package com.vctek.orderservice.dto.request.storefront;

import com.vctek.orderservice.dto.request.AddSubOrderEntryRequest;

public class StoreFrontSubOrderEntryRequest extends AddSubOrderEntryRequest {
    private Long subEntryId;

    public Long getSubEntryId() {
        return subEntryId;
    }

    public void setSubEntryId(Long subEntryId) {
        this.subEntryId = subEntryId;
    }
}
