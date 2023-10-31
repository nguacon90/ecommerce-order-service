package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.dto.OrderSourceData;
import com.vctek.orderservice.model.OrderSourceModel;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrderSourcePopulatorTest {
    private OrderSourcePopulator populator;

    @Before
    public void setUp() {
        populator = new OrderSourcePopulator();
    }

    @Test
    public void populate() {
        OrderSourceModel source = new OrderSourceModel();
        source.setId(12l);
        source.setName("name");
        source.setCompanyId(1l);

        OrderSourceData target = new OrderSourceData();
        populator.populate(source, target);

        assertEquals("name", target.getName());
        assertEquals(12l, target.getId(), 0);
        assertEquals(1l, target.getCompanyId(), 0);

    }
}
