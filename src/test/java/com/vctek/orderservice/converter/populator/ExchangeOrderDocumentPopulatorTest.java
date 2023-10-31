package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.elasticsearch.model.returnorder.ExchangeOrder;
import com.vctek.orderservice.model.OrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.repository.ToppingItemRepository;
import com.vctek.orderservice.repository.ToppingOptionRepository;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.ProductService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExchangeOrderDocumentPopulatorTest {
    private ExchangeOrderDocumentPopulator populator;
    @Mock
    private OrderModel exchangeOrder;
    private ExchangeOrder doc = new ExchangeOrder();
    @Mock
    private OrderEntryRepository orderEntryRepository;
    private List<OrderEntryModel> entries = new ArrayList<>();
    @Mock
    private OrderEntryModel entry;
    @Mock
    private CalculationService calculationService;
    @Mock
    private ProductService productService;
    @Mock
    private ToppingOptionRepository toppingOptionRepository;
    @Mock
    private ToppingItemRepository toppingItemRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new ExchangeOrderDocumentPopulator();
        populator.setOrderEntryRepository(orderEntryRepository);
        populator.setCalculationService(calculationService);
        populator.setProductService(productService);
        populator.setToppingItemRepository(toppingItemRepository);
        populator.setToppingOptionRepository(toppingOptionRepository);
        entries.add(entry);
    }

    @Test
    public void populate() {
        when(orderEntryRepository.findAllByOrder(exchangeOrder)).thenReturn(entries);
        when(entry.getProductId()).thenReturn(11l);
        populator.populate(exchangeOrder, doc);
        verify(calculationService).calculateFinalDiscountOfEntry(entry);
        verify(productService).getBasicProductDetail(anyLong());
    }
}
