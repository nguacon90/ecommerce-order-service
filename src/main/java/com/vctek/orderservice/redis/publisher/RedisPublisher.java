package com.vctek.orderservice.redis.publisher;

public interface RedisPublisher<D> {
    void publish(D data);
}
