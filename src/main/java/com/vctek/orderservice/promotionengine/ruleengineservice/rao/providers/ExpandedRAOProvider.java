package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers;

import java.util.Collection;
import java.util.Set;

public interface ExpandedRAOProvider<T> extends RAOProvider<T> {
    Set expandFactModel(T var1, Collection<String> var2);
}