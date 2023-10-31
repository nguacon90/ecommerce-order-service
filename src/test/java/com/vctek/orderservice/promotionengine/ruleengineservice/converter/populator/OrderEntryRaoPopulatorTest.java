package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class OrderEntryRaoPopulatorTest {
    private OrderEntryRaoPopulator populator;

    @Mock
    private AbstractOrderEntryModel source;
    private OrderEntryRAO target;
    @Mock
    private ProductSearchService productSearchService;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = new OrderEntryRAO();
        populator = new OrderEntryRaoPopulator();
        populator.setProductSearchService(productSearchService);
    }

    @Test
    public void populate() {
        when(source.getId()).thenReturn(2l);
        populator.populate(source, target);
        assertEquals(2l, target.getId(), 0);

    }
}
