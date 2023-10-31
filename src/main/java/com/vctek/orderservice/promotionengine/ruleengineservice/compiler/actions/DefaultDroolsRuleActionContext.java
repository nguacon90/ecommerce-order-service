package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.drools.core.spi.KnowledgeHelper;

import java.util.*;

public class DefaultDroolsRuleActionContext implements RuleActionContext {
    private final Map<String, Object> variables;
    private final KnowledgeHelper helper;
    private Map<String, Object> parameters;
    private Set<Object> factsToUpdate;

    public DefaultDroolsRuleActionContext(Map<String, Object> variables, Object helper) {
        this.variables = variables;
        this.helper = (KnowledgeHelper) helper;
        this.factsToUpdate = Sets.newHashSet();
    }

    public Map<String, Object> getVariables() {
        return this.variables;
    }

    public Object getDelegate() {
        return this.helper;
    }

    @Override
    public <T> Set<T> getValues(Class<T> type) {
        return this.findValues(type);
    }

    @Override
    public <T> Set<T> getValues(Class<T> type, String... path) {
        return this.findValues(type, path);
    }

    @Override
    public <T> T getValue(Class<T> type) {
        Set<T> values = this.findValues(type);
        return CollectionUtils.isNotEmpty(values) ? values.iterator().next() : null;
    }

    @Override
    public void scheduleForUpdate(Object... facts) {
        this.factsToUpdate.addAll(Arrays.asList(facts));
    }

    public <T> T getValue(Class<T> type, String... path) {
        Set<T> values = this.findValues(type, path);
        return CollectionUtils.isNotEmpty(values) ? values.iterator().next() : null;
    }

    @Override
    public void insertFacts(Object... facts) {
        if (ArrayUtils.isNotEmpty(facts)) {
            Arrays.stream(facts).forEach(this.helper::insert);
        }

    }

    @Override
    public void insertFacts(Collection facts) {
        if (CollectionUtils.isNotEmpty(facts)) {
            facts.forEach(this.helper::insert);
        }
    }

    @Override
    public void updateScheduledFacts() {
        if (CollectionUtils.isNotEmpty(this.factsToUpdate)) {
            this.updateFacts(this.factsToUpdate.toArray(new Object[0]));
            this.factsToUpdate.clear();
        }

    }

    public void updateFacts(Object... facts) {
        if (ArrayUtils.isNotEmpty(facts)) {
            Arrays.stream(facts).forEach(this.helper::update);
        }

    }

    protected <T> Set<T> findValues(Class<T> type, String... path) {
        String key = path.length == 0 ? type.getName() : StringUtils.join(path, "/") + "/" + type.getName();
        Object value = this.variables.get(key);
        if (value instanceof Set) {
            Set<T> values = (Set) value;
            return this.evaluateValues(type, values, path);
        } else if (value instanceof List) {
            List<T> values = (List) value;
            return this.evaluateValues(type, values, path);
        } else {
            return (Set) (Objects.nonNull(value) ? ImmutableSet.of(value) : Collections.emptySet());
        }
    }

    protected <T> Set<T> evaluateValues(Class<T> type, List<T> values, String... path) {
        if (CollectionUtils.isNotEmpty(values)) {
            return new HashSet(values);
        } else {
            return ArrayUtils.isEmpty(path) ? Collections.emptySet() :
                    this.findValues(type, (String[]) Arrays.copyOf(path, path.length - 1));
        }
    }

    protected <T> Set<T> evaluateValues(Class<T> type, Set<T> values, String... path) {
        if (CollectionUtils.isNotEmpty(values)) {
            return values;
        } else {
            return ArrayUtils.isEmpty(path) ? Collections.emptySet() :
                    this.findValues(type, (String[]) Arrays.copyOf(path, path.length - 1));
        }
    }

    @Override
    public CartRAO getCartRao() {
        return this.getValue(CartRAO.class);
    }

    @Override
    public RuleEngineResultRAO getRuleEngineResultRao() {
        return this.getValue(RuleEngineResultRAO.class);
    }

    @Override
    public String getRuleName() {
        KnowledgeHelper knowledgeHelper = this.checkAndGetRuleContext(this.getDelegate());
        return knowledgeHelper.getRule().getName();
    }

    @Override
    public Optional<String> getRulesModuleName() {
        Optional<String> rulesModuleName = Optional.empty();
        KnowledgeHelper knowledgeHelper = this.checkAndGetRuleContext(this.getDelegate());
        Map<String, Object> metaData = knowledgeHelper.getRule().getMetaData();
        if (MapUtils.isNotEmpty(metaData)) {
            rulesModuleName = Optional.ofNullable((String) metaData.get("moduleName"));
        }

        return rulesModuleName;
    }

    @Override
    public Map<String, Object> getRuleMetadata() {
        KnowledgeHelper knowledgeHelper = this.checkAndGetRuleContext(this.getDelegate());
        return knowledgeHelper.getRule().getMetaData();
    }

    protected KnowledgeHelper checkAndGetRuleContext(Object ruleContext) {
        Preconditions.checkState(ruleContext instanceof KnowledgeHelper, "ruleContext must be of type org.drools.core.spi.KnowledgeHelper.");
        return (KnowledgeHelper) ruleContext;
    }

    @Override
    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    @Override
    public Object getParameter(String parameterName) {
        Map<String, Object> params = this.getParameters();
        return params != null ? params.get(parameterName) : null;
    }

    @Override
    public <T> T getParameter(String parameterName, Class<T> type) {
        Map<String, Object> params = this.getParameters();
        Preconditions.checkArgument(params != null &&
                params.containsKey(parameterName) &&
                params.get(parameterName).getClass().isAssignableFrom(type),
        String.format("Property '%1$s' must not be null and must be of type %2$s", parameterName, type.getName()));
        return (T) params.get(parameterName);
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters == null ? null : Collections.unmodifiableMap(parameters);
    }

}
