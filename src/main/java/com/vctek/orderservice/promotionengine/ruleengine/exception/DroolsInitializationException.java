package com.vctek.orderservice.promotionengine.ruleengine.exception;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.vctek.orderservice.promotionengine.ruleengine.ResultItem;

import java.util.Collection;
import java.util.stream.Collectors;

public class DroolsInitializationException extends RuntimeException {
    private final Collection<ResultItem> results;

    public DroolsInitializationException(String message) {
        super(message);
        this.results = Lists.newArrayList();
    }

    public DroolsInitializationException(String message, Throwable cause) {
        super(message, cause);
        this.results = Lists.newArrayList();
    }

    public DroolsInitializationException(Collection<ResultItem> results, Throwable cause) {
        super(cause);
        this.results = results;
    }

    public DroolsInitializationException(Collection<ResultItem> results, String message) {
        super(message + Joiner.on(", ").join(results.stream().map(ResultItem::getMessage).collect(Collectors.toList())));
        this.results = results;
    }

    public Collection<ResultItem> getResults() {
        return this.results;
    }
}
