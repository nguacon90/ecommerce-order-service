package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.FactContextFactory;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class DefaultFactContextFactory implements FactContextFactory {
    private Map<String, Map<Class, List<RAOProvider>>> raoProviders;

    public FactContext createFactContext(FactContextType type, Collection<?> facts) {
        if (!this.getRaoProviders().containsKey(type.toString())) {
            throw new IllegalArgumentException(String.format("The Fact Context Type with name '%s' is not defined",
                    type.name()));
        }
        return new FactContext(type, this.getRaoProviders().get(type.toString()), facts);
    }

    protected Map<String, Map<Class, List<RAOProvider>>> getRaoProviders() {
        return this.raoProviders;
    }

    @Autowired
    @Qualifier("raoProviders")
    public void setRaoProviders(Map<String, Map<Class, List<RAOProvider>>> raoProviders) {
        this.raoProviders = raoProviders;
    }
}
