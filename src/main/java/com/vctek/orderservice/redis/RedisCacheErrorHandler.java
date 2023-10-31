package com.vctek.orderservice.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

public class RedisCacheErrorHandler implements CacheErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RedisCacheErrorHandler.class);
    public static final String REDIS_CACHE_ERROR = "Redis cache error";

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        LOG.error(REDIS_CACHE_ERROR, exception);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        LOG.error(REDIS_CACHE_ERROR, exception);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        LOG.error(REDIS_CACHE_ERROR, exception);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        LOG.error(REDIS_CACHE_ERROR, exception);
    }
}
