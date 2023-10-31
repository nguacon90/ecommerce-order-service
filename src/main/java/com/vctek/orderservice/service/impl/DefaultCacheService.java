package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

public class DefaultCacheService<K, V> implements CacheService<K, V> {
    private RedisTemplate redisTemplate;

    private long defaultTimeOutCacheInMinutes;

    @Override
    public void put(K key, V value) {
        redisTemplate.opsForValue().set(key, value, defaultTimeOutCacheInMinutes, TimeUnit.MINUTES);
    }

    @Override
    public V getValue(K key) {
        return (V) redisTemplate.opsForValue().get(key);
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setDefaultTimeOutCacheInMinutes(long defaultTimeOutCacheInMinutes) {
        this.defaultTimeOutCacheInMinutes = defaultTimeOutCacheInMinutes;
    }
}
