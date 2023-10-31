package com.vctek.orderservice.promotionengine.ruleengine.model.eveluation;


import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface RuleActionContext {
    void setParameters(Map<String, Object> parameters);

    Map<String, Object> getParameters();

    Map<String, Object> getRuleMetadata();

    String getRuleName();

    void updateScheduledFacts();

    Optional<String> getRulesModuleName();

    Object getParameter(String value);

    <T> T getParameter(String parameterName, Class<T> type);

    CartRAO getCartRao();

    RuleEngineResultRAO getRuleEngineResultRao();

    void scheduleForUpdate(Object[] objects);

    void insertFacts(Object[] objects);

    void insertFacts(Collection facts);

    Object getDelegate();

    <T> Set<T> getValues(Class<T> type);

    <T> Set<T> getValues(Class<T> type, String... path);

    <T> T getValue(Class<T> type);
}
