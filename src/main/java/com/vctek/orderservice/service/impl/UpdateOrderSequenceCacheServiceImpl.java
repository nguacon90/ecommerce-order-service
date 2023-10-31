package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.service.UpdateOrderSequenceCacheService;
import com.vctek.util.CommonUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class UpdateOrderSequenceCacheServiceImpl extends DefaultCacheService<String, Long> implements UpdateOrderSequenceCacheService {

    private long defaultTimeOutCacheInMinutes = 1l;

    @PostConstruct
    public void init() {
        super.setDefaultTimeOutCacheInMinutes(defaultTimeOutCacheInMinutes);
    }

    @Override
    public void putTimeRequest(String function, String orderCode, Long entryId, Long timeRequest) {
        String key = function + CommonUtils.UNDERSCORE + orderCode + CommonUtils.UNDERSCORE + entryId;
        put(key, timeRequest);
    }

    @Override
    public Long getProcessedTimeRequest(String function, String orderCode, Long entryId) {
        String key = function + CommonUtils.UNDERSCORE + orderCode + CommonUtils.UNDERSCORE + entryId;
        return getValue(key);
    }

    @Override
    public boolean isValidTimeRequest(String function, String orderCode, Long entryId, Long timeRequest) {
        Long processedTime = getProcessedTimeRequest(function, orderCode, entryId);
        if(processedTime == null) {
            putTimeRequest(function, orderCode, entryId, timeRequest);
            return true;
        }

        if(processedTime < timeRequest) {
            putTimeRequest(function, orderCode, entryId, timeRequest);
            return true;
        }

        return false;
    }
}
