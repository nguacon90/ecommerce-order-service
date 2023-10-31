package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.converter.storefront.populator.CommerceOrderSearchDataPopulator;
import com.vctek.orderservice.dto.request.storefront.CommerceOrderData;
import com.vctek.orderservice.elasticsearch.model.OrderEntryData;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CommerceOrderSearchDataPopulatorTest {
    private CommerceOrderSearchDataPopulator populator;
    @Mock
    private ProductSearchService productSearchService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new CommerceOrderSearchDataPopulator();
        populator.setProductSearchService(productSearchService);
    }

    @Test
    public void populate() {
        OrderSearchModel source = new OrderSearchModel();
        source.setCode("code");
        source.setCompanyId(1l);
        source.setOrderEntries(Arrays.asList(new OrderEntryData()));

        CommerceOrderData target = new CommerceOrderData();
        populator.populate(source, target);

        assertEquals("code", target.getCode());
        assertEquals(1l, target.getCompanyId(), 0);

    }
}
