package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers;

import java.util.Set;

public interface RAOFactsExtractor {
    Set expandFact(Object var1);

    String getTriggeringOption();

    boolean isMinOption();

    boolean isDefault();
}
