package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.ToppingItemModel;
import com.vctek.orderservice.model.ToppingOptionModel;
import com.vctek.orderservice.repository.ToppingItemRepository;
import com.vctek.orderservice.service.ToppingItemService;
import com.vctek.orderservice.util.CurrencyType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ToppingItemServiceTest {
    private ToppingItemRepository repository;

    private ToppingItemService toppingItemService;

    @Before
    public void setUp() {
        repository = mock(ToppingItemRepository.class);
        toppingItemService = new ToppingItemServiceImpl(repository);
    }

    @Test
    public void findById() {
        ToppingOptionModel model = new ToppingOptionModel();
        toppingItemService.findByIdAndToppingOption(1L, model);
        verify(repository).findByIdAndToppingOptionModel(1L, model);
    }

    @Test
    public void delete() {
        ToppingItemModel toppingItemModel = new ToppingItemModel();
        toppingItemService.delete(toppingItemModel);
        verify(repository).delete(toppingItemModel);
    }

    @Test
    public void totalDiscountToppingItem() {
        ToppingItemModel toppingItemModel = new ToppingItemModel();
        toppingItemModel.setBasePrice(20000d);
        toppingItemModel.setDiscount(10d);
        toppingItemModel.setQuantity(2);
        toppingItemModel.setDiscountType(CurrencyType.PERCENT.toString());
        ToppingOptionModel toppingOptionModel = new ToppingOptionModel();
        toppingOptionModel.setQuantity(1);
        toppingOptionModel.setToppingItemModels(Collections.singleton(toppingItemModel));
        when(repository.findAllByToppingOptionModel(toppingOptionModel)).thenReturn(Arrays.asList(toppingItemModel));
        double totalDiscount = toppingItemService.totalDiscountToppingItem(Arrays.asList(toppingOptionModel));
        assertEquals(4000d, totalDiscount, 0);
    }

    @Test
    public void findAllByOrderId() {
        toppingItemService.findAllByOrderId(2l);
        verify(repository).findAllByOrderId(anyLong());
    }
}


