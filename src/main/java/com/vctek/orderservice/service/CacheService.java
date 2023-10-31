package com.vctek.orderservice.service;

public interface CacheService<K, V> {
    void put(K key, V value);

    V getValue(K key);

}
