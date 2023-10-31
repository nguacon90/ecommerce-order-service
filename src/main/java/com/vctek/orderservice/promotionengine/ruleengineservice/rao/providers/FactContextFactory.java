package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers;


import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl.FactContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl.FactContextType;

import java.util.Collection;

public interface FactContextFactory {
    FactContext createFactContext(FactContextType type, Collection<?> facts);
}
