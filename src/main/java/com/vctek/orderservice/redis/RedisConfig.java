package com.vctek.orderservice.redis;

import com.vctek.orderservice.redis.listener.PublishPromotionMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cloud.cluster.redis.lock.RedisLockService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.Executor;

@Configuration
public class RedisConfig extends SpringBootServletInitializer implements CachingConfigurer {


    @Override
    public CacheManager cacheManager() {
        return null;
    }

    @Override
    public CacheResolver cacheResolver() {
        return null;
    }

    @Override
    public KeyGenerator keyGenerator() {
        return null;
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new RedisCacheErrorHandler();
    }

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Value("${vctek.redis.topic.publishPromotion:publish-promotion}")
    private String publishPromotionTopic;

    @Value("${vctek.redis.microServiceCacheDurationInSeconds:28800}")
    private long microServiceCacheDuration;

    @Bean("promotionPublisherTaskExecutor")
    public Executor productPublisherTaskExecutor(@Value("${vctek.executor.promotionPublisher.corePoolSize:5}") int corePoolSize,
                                                 @Value("${vctek.executor.promotionPublisher.maxPoolSize:30}") int maxPoolSize) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setThreadNamePrefix("productRedisPublisher");
        return taskExecutor;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        final RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return template;
    }

    @Bean("publishPromotionTopic")
    public ChannelTopic publishPromotionTopic() {
        return new ChannelTopic(publishPromotionTopic);
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(PublishPromotionMessageListener publishPromotionMessageListener) {
        RedisMessageListenerContainer container
                = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(publishPromotionMessageListener, publishPromotionTopic());
        return container;
    }

    @Bean
    public RedisLockService redisLockService() {
        return new RedisLockService(redisConnectionFactory);
    }

    @Bean("microServiceCacheManager")
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofSeconds(microServiceCacheDuration))
                        .disableCachingNullValues();
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration).build();
    }
}
