package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl;

import com.google.common.collect.Sets;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.ExpandedRAOProvider;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOFactsExtractor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public abstract class AbstractExpandedRAOProvider<T, R> implements ExpandedRAOProvider<T>, InitializingBean {
    protected Collection<String> validOptions = new ArrayList<>();
    protected Collection<String> defaultOptions = new ArrayList<>();
    protected Collection<String> minOptions = new ArrayList<>();
    private List<RAOFactsExtractor> factExtractorList = new ArrayList<>();
    private Map<String, BiConsumer<Set<Object>, R>> consumerMap;

    public Set expandFactModel(T modelFact) {
        return this.expandFactModel(modelFact, this.getDefaultOptions());
    }

    public Set expandFactModel(T modelFact, Collection<String> options) {
        Collection<String> filteredOptions = this.getFilteredOptions(options);
        R raoFact = this.createRAO(modelFact);
        Set expandedFactsSet = this.expandRAO(raoFact, filteredOptions);
        this.populateRaoFactsExtractorConsumers();
        this.addExtraRAOFacts(expandedFactsSet, raoFact, filteredOptions);
        return expandedFactsSet;
    }

    public void afterPropertiesSet() {
        this.setConsumerMap(new ConcurrentHashMap());
    }

    protected void populateRaoFactsExtractorConsumers() {
        List<RAOFactsExtractor> myFactExtractorList = this.getFactExtractorList();
        if (CollectionUtils.isNotEmpty(myFactExtractorList)) {
            Map<String, BiConsumer<Set<Object>, R>> myConsumerMap = this.getConsumerMap();
            myFactExtractorList.stream().filter((e) -> !myConsumerMap.containsKey(e.getTriggeringOption()))
                    .forEach(this::addOptionConsumers);
        }

    }

    protected void addExtraRAOFacts(Set expandedFactsSet, R raoFact, Collection<String> filteredOptions) {
        Map<String, BiConsumer<Set<Object>, R>> myConsumerMap = this.getConsumerMap();
        if (MapUtils.isNotEmpty(myConsumerMap) && CollectionUtils.isNotEmpty(filteredOptions)) {
            filteredOptions.stream().filter(myConsumerMap::containsKey)
                    .forEach((o) -> myConsumerMap.get(o).accept(expandedFactsSet, raoFact));
        }

    }

    protected abstract R createRAO(T var1);

    protected void addOptionConsumers(RAOFactsExtractor raoFactsExtractor) {
        String triggeringOption = raoFactsExtractor.getTriggeringOption();
        Map<String, BiConsumer<Set<Object>, R>> myConsumerMap = this.getConsumerMap();
        myConsumerMap.put(triggeringOption, (f, r) -> f.addAll(raoFactsExtractor.expandFact(r)));
    }

    protected Optional<BiConsumer<Set<Object>, R>> getConsumer(String option) {
        Map<String, BiConsumer<Set<Object>, R>> myConsumerMap = this.getConsumerMap();
        return !MapUtils.isEmpty(myConsumerMap) && !Objects.isNull(myConsumerMap.get(option)) ?
                Optional.of(myConsumerMap.get(option)) : Optional.empty();
    }

    protected Set<Object> expandRAO(R rao, Collection<String> options) {
        Set<Object> facts = new LinkedHashSet();
        if (Objects.nonNull(rao)) {
            options.stream().map(this::getConsumer).filter(Optional::isPresent)
                    .forEach((c) -> c.get().accept(facts, rao));
        }

        return facts;
    }

    protected Collection<String> getFilteredOptions(Collection<String> options) {
        Set<String> onlyValidOptions = new HashSet(options);
        Collection<String> localMinOptions = this.getMinOptions();
        if (CollectionUtils.isNotEmpty(localMinOptions)) {
            onlyValidOptions.addAll(localMinOptions);
        }

        Collection<String> localValidOptions = this.getValidOptions();
        if (CollectionUtils.isNotEmpty(localValidOptions)) {
            onlyValidOptions.retainAll(localValidOptions);
        }

        return onlyValidOptions;
    }

    protected Set<String> addExtraValidOptions(List<RAOFactsExtractor> raoExtractorList) {
        return (Set)(CollectionUtils.isNotEmpty(raoExtractorList) ? raoExtractorList.stream()
                .filter((e) -> StringUtils.isNotEmpty(e.getTriggeringOption()))
                .map(RAOFactsExtractor::getTriggeringOption)
                .collect(Collectors.toSet()) : Sets.newHashSet());
    }

    protected Set<String> addExtraDefaultOptions(List<RAOFactsExtractor> raoExtractorList) {
        return (Set)(CollectionUtils.isNotEmpty(raoExtractorList) ? raoExtractorList.stream()
                .filter(RAOFactsExtractor::isDefault)
                .map(RAOFactsExtractor::getTriggeringOption)
                .collect(Collectors.toSet()) : new HashSet<>());
    }

    protected Set<String> addExtraMinOptions(List<RAOFactsExtractor> raoExtractorList) {
        return CollectionUtils.isNotEmpty(raoExtractorList) ? raoExtractorList.stream()
                .filter(RAOFactsExtractor::isMinOption)
                .map(RAOFactsExtractor::getTriggeringOption)
                .collect(Collectors.toSet()) : new HashSet<>();
    }

    protected List<RAOFactsExtractor> getFactExtractorList() {
        return this.factExtractorList;
    }

    public void setFactExtractorList(List<RAOFactsExtractor> factExtractorList) {
        this.factExtractorList = factExtractorList;
    }

    protected Collection<String> getDefaultOptions() {
        Collection<String> combinedDefaultOptions = this.getConcurrentlySafeOptions(this.defaultOptions);
        combinedDefaultOptions.addAll(this.addExtraDefaultOptions(this.getFactExtractorList()));
        return combinedDefaultOptions;
    }

    protected Collection<String> getValidOptions() {
        Collection<String> combinedValidOptions = this.getConcurrentlySafeOptions(this.validOptions);
        combinedValidOptions.addAll(this.addExtraValidOptions(this.getFactExtractorList()));
        return combinedValidOptions;
    }

    protected Collection<String> getMinOptions() {
        Collection<String> combinedMinOptions = this.getConcurrentlySafeOptions(this.minOptions);
        combinedMinOptions.addAll(this.addExtraMinOptions(this.getFactExtractorList()));
        return combinedMinOptions;
    }

    protected Collection<String> getConcurrentlySafeOptions(Collection<String> options) {
        Set<String> optionSet  = ConcurrentHashMap.newKeySet();
        optionSet.addAll(options);
        return optionSet;
    }

    public void setConsumerMap(Map<String, BiConsumer<Set<Object>, R>> consumerMap) {
        this.consumerMap = consumerMap;
    }

    protected Map<String, BiConsumer<Set<Object>, R>> getConsumerMap() {
        return this.consumerMap;
    }
}
