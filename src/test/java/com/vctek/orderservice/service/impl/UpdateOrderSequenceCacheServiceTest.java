package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.OrderEntryDTO;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class UpdateOrderSequenceCacheServiceTest {
    private UpdateOrderSequenceCacheServiceImpl service;
    private OrderEntryDTO orderEntryDTO;

    @Before
    public void setUp() {
        orderEntryDTO = new OrderEntryDTO();
        orderEntryDTO.setEntryId(0l);
        service = new UpdateOrderSequenceCacheServiceImpl(){
            private HashMap<String, Long> cached = new HashMap<>();
            @Override
            public void put(String key, Long value) {
                cached.put(key, value);
            }

            @Override
            public Long getValue(String key) {
                return cached.get(key);
            }
        };

        service.init();
    }

    @Test
    public void isValidTimeRequest_NotRequestTimeInCached() {
        assertEquals(true, service.isValidTimeRequest("updateOrderEntry", "123", orderEntryDTO.getEntryId(), 1l));
    }

    @Test
    public void isValidTimeRequest_AllRequestTimeOlderThanInCached() {
        assertEquals(true, service.isValidTimeRequest("updateOrderEntry", "123", orderEntryDTO.getEntryId(), 1l));
        assertEquals(true, service.isValidTimeRequest("updateOrderEntry", "123", orderEntryDTO.getEntryId(), 2l));
        assertEquals(true, service.isValidTimeRequest("updateOrderEntry", "123", orderEntryDTO.getEntryId(), 3l));
    }

    @Test
    public void isValidTimeRequest_MixedRequestTime() {
        assertEquals(true, service.isValidTimeRequest("updateOrderEntry", "123", orderEntryDTO.getEntryId(), 1l));
        assertEquals(true, service.isValidTimeRequest("updateOrderEntry", "123", orderEntryDTO.getEntryId(), 2l));
        assertEquals(true, service.isValidTimeRequest("updateOrderEntry", "123", orderEntryDTO.getEntryId(), 4l));
        assertEquals(true, service.isValidTimeRequest("updateOrderEntry", "123", orderEntryDTO.getEntryId(), 5l));
        assertEquals(false, service.isValidTimeRequest("updateOrderEntry", "123", orderEntryDTO.getEntryId(), 3l));
    }

}
