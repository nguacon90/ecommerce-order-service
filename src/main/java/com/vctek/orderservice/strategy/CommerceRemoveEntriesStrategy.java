package com.vctek.orderservice.strategy;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;

public interface CommerceRemoveEntriesStrategy {
    void removeAllEntries(final CommerceAbstractOrderParameter parameter);
}
