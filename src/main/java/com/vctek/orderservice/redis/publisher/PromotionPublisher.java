package com.vctek.orderservice.redis.publisher;

import com.vctek.orderservice.redis.data.PublishPromotionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component("promotionPublisher")
public class PromotionPublisher implements RedisPublisher<PublishPromotionData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionPublisher.class);
    private RedisTemplate redisTemplate;
    private ChannelTopic channelTopic;
    private Executor executor;

    @Override
    public void publish(PublishPromotionData data) {
        try {
            executor.execute(() -> redisTemplate.convertAndSend(channelTopic.getTopic(), data));
            LOGGER.info("Publish promotion {} finished", data.getPromotionSourceRuleId());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    @Qualifier("publishPromotionTopic")
    public void setChannelTopic(ChannelTopic channelTopic) {
        this.channelTopic = channelTopic;
    }

    @Autowired
    @Qualifier("promotionPublisherTaskExecutor")
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
