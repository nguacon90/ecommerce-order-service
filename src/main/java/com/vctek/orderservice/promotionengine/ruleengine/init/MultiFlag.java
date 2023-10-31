package com.vctek.orderservice.promotionengine.ruleengine.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiFlag {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiFlag.class);
    private final Map<String, AtomicBoolean> keyToFlagMap;

    public MultiFlag() {
        this.keyToFlagMap = new ConcurrentHashMap<>(3, 0.75f, 2);
    }

    public boolean compareAndSet(String key, boolean expected, boolean update) {
        AtomicBoolean flagForKey = this.keyToFlagMap.computeIfAbsent(key, (k) -> new AtomicBoolean(false));
        boolean result = flagForKey.compareAndSet(expected, update);
        LOGGER.debug("MultiFlag.compareAndSet called with:  module: {}, expected:{}, update:{}, result:{}",
                new Object[]{key, expected, update, result});
        return result;
    }
}
