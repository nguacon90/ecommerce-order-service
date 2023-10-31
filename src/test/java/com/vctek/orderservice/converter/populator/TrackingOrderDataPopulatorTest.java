package com.vctek.orderservice.converter.populator;

import com.vctek.kafka.data.CustomerDto;
import com.vctek.kafka.data.OrderData;
import com.vctek.kafka.data.OrderEntryData;
import com.vctek.orderservice.dto.TrackingOrderData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TrackingOrderDataPopulatorTest {
    private TrackingOrderDataPopulator populator;
    private OrderData orderData;

    @Before
    public void setUp() {
        populator = new TrackingOrderDataPopulator();
        orderData = new OrderData();
        orderData.setOrderCode("code");
        orderData.setCompanyId(2L);
        CustomerDto dimCustomerData = new CustomerDto();
        dimCustomerData.setCustomerId(2L);
        orderData.setDimCustomerData(dimCustomerData);
        List<OrderEntryData> entries = new ArrayList<>();
        OrderEntryData entry = new OrderEntryData();
        entry.setProductId(2L);
        entry.setOrderEntryId(2L);
        entries.add(entry);
        orderData.setEntryDataList(entries);
    }

    @Test
    public void populate() {
        TrackingOrderData trackingOrderData = new TrackingOrderData();
        populator.populate(orderData, trackingOrderData);
        assertEquals(2L, trackingOrderData.getCompanyId(), 0);
        assertEquals("code", trackingOrderData.getOrderCode());
        assertEquals(1, trackingOrderData.getDetails().size(), 0);
    }
}
