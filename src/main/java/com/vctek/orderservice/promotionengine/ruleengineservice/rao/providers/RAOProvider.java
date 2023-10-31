package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers;

import java.util.Set;

public interface RAOProvider<T> {
    Set expandFactModel(T var1);
}