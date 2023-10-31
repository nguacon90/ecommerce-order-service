package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.ModelService;
import com.vctek.orderservice.service.ToppingItemService;
import com.vctek.orderservice.service.impl.DefaultCalculationService;
import com.vctek.orderservice.util.DiscountType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CartRaoPopulatorTest {

    private CartRaoPopulator populator;
    @Mock
    private Populator<AbstractOrderEntryModel, OrderEntryRAO> orderEntryRaoPopulator;
    private AbstractOrderModel source;
    private CartRAO target;
    private List<AbstractOrderEntryModel> orderEntries = new ArrayList<>();
    private CartEntryModel normalEntry;
    private CartEntryModel comboEntry1;
    private CartEntryModel comboEntry2;
    @Mock
    private ModelService modelServiceMock;
    private DefaultCalculationService defaultCalculationService;
    private Set<SubOrderEntryModel> subEntriesCombo1 = new HashSet<>();
    private Set<SubOrderEntryModel> subEntriesCombo2 = new HashSet<>();

    @Mock
    private CartService cartService;
    @Mock
    private ToppingItemService toppingItemService;
    @Mock
    private EntryRepository entryRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        source = new AbstractOrderModel();
        SubOrderEntryModel subOrderEntryModel1 = new SubOrderEntryModel();
        subOrderEntryModel1.setOriginPrice(10000.0);
        subOrderEntryModel1.setQuantity(2);
        SubOrderEntryModel subOrderEntryModel2 = new SubOrderEntryModel();
        subOrderEntryModel2.setOriginPrice(2000.0);
        subOrderEntryModel2.setQuantity(10);
        subEntriesCombo1.add(subOrderEntryModel1);
        subEntriesCombo2.add(subOrderEntryModel2);
        defaultCalculationService = new DefaultCalculationService(modelServiceMock, toppingItemService);
        normalEntry = new CartEntryModel();
        normalEntry.setProductId(111l);
        normalEntry.setQuantity(2l);
        normalEntry.setBasePrice(15000d);
        normalEntry.setOrder(source);

        comboEntry1 = new CartEntryModel();
        comboEntry1.setProductId(112l);
        comboEntry1.setBasePrice(200000d);
        comboEntry1.setQuantity(3l);
        comboEntry1.setDiscount(10000d);
        comboEntry1.setDiscountType(DiscountType.CASH.toString());
        comboEntry1.setSubOrderEntries(subEntriesCombo1);
        comboEntry1.setOrder(source);

        comboEntry2 = new CartEntryModel();
        comboEntry2.setProductId(113l);
        comboEntry2.setBasePrice(250000d);
        comboEntry2.setQuantity(5l);
        comboEntry2.setSubOrderEntries(subEntriesCombo2);
        comboEntry2.setOrder(source);

        source.setEntries(orderEntries);
        source.setWarehouseId(22l);

        target = new CartRAO();
        populator = new CartRaoPopulator();
        populator.setOrderEntryRaoPopulator(orderEntryRaoPopulator);
        populator.setCartService(cartService);
        populator.setEntryRepository(entryRepository);
        when(entryRepository.findAllByOrder(source)).thenReturn(orderEntries);
    }

    @Test
    public void populate() {
        when(cartService.isValidEntryForPromotion(any())).thenReturn(true);
        orderEntries.add(normalEntry);
        populator.populate(source, target);
        verify(orderEntryRaoPopulator).populate(any(), any());
        assertEquals(22l, target.getWarehouse(), 0);
    }

    @Test
    public void populate_IgnoreAllComboEntries() {
        orderEntries.add(normalEntry);
        orderEntries.add(comboEntry1);
        orderEntries.add(comboEntry2);
        when(cartService.isValidEntryForPromotion(normalEntry)).thenReturn(true);
        when(cartService.isValidEntryForPromotion(comboEntry1)).thenReturn(false);
        when(cartService.isValidEntryForPromotion(comboEntry2)).thenReturn(false);

        populator.populate(source, target);
        assertEquals(22l, target.getWarehouse(), 0);

    }

    @Test
    public void populate_IgnoreOneComboEntries() {
        orderEntries.add(normalEntry);
        orderEntries.add(comboEntry1);
        orderEntries.add(comboEntry2);
        when(cartService.isValidEntryForPromotion(normalEntry)).thenReturn(true);
        when(cartService.isValidEntryForPromotion(comboEntry1)).thenReturn(true);
        when(cartService.isValidEntryForPromotion(comboEntry2)).thenReturn(false);

        populator.populate(source, target);
        assertEquals(22l, target.getWarehouse(), 0);

    }
}
