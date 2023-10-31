package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl;


import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOProvider;

import java.util.*;

public class FactContext {
    private final FactContextType type;
    private final Collection facts;
    private final Map<Class, List<RAOProvider>> raoProviders;

    FactContext(FactContextType type, Map<Class, List<RAOProvider>> raoProviders, Collection<?> facts) {
        this.type = type;
        this.facts = facts;
        this.raoProviders = raoProviders;
    }

    public FactContextType getType() {
        return this.type;
    }

    public Collection getFacts() {
        return this.facts;
    }

    public Set<RAOProvider> getProviders(Object obj) {
        Set<RAOProvider> result = new HashSet();
        Iterator var4 = this.getFactClasses(obj).iterator();

        while(var4.hasNext()) {
            Class clazz = (Class)var4.next();
            if (this.raoProviders.containsKey(clazz)) {
                result.addAll(this.raoProviders.get(clazz));
            }
        }

        return result;
    }

    protected Set<Class> getFactClasses(Object obj) {
        Set<Class> result = new HashSet();

        for(Class clazz = obj.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            result.add(clazz);
        }

        return result;
    }
}
