package com.vctek.orderservice.service;

public interface UpdateOrderSequenceCacheService {
    void putTimeRequest(String function, String orderCode, Long entryId, Long timeRequest);

    Long getProcessedTimeRequest(String function, String orderCode, Long entryId);

    boolean isValidTimeRequest(String function, String orderCode, Long entryId, Long timeRequest);
}
