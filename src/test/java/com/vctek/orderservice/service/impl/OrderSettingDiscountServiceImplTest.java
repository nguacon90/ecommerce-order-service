package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderSettingDiscountModel;
import com.vctek.orderservice.repository.OrderSettingDiscountRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OrderSettingDiscountServiceImplTest {
    private OrderSettingDiscountRepository repository;
    private OrderSettingDiscountServiceImpl service;
    private ArgumentCaptor<OrderSettingDiscountModel> captor;
    private ArgumentCaptor<List<OrderSettingDiscountModel>> captors;

    @Before
    public void setUp() {
        repository = mock(OrderSettingDiscountRepository.class);
        captor = ArgumentCaptor.forClass(OrderSettingDiscountModel.class);
        captors = ArgumentCaptor.forClass(List.class);
        service = new OrderSettingDiscountServiceImpl();
        service.setRepository(repository);
    }

    @Test
    public void save() {
        OrderSettingDiscountModel model = new OrderSettingDiscountModel();
        model.setCompanyId(2l);
        model.setProductId(10l);
        service.save(model);

        verify(repository).save(captor.capture());
        OrderSettingDiscountModel actual = captor.getValue();
        assertEquals(2l, actual.getCompanyId(), 0);
        assertEquals(10l, actual.getProductId(), 0);
    }

    @Test
    public void saveAll() {
        OrderSettingDiscountModel model = new OrderSettingDiscountModel();
        model.setCompanyId(1l);
        model.setProductId(10l);
        OrderSettingDiscountModel model2 = new OrderSettingDiscountModel();
        model2.setCompanyId(2l);
        model2.setProductId(20l);
        service.saveAll(Arrays.asList(model, model2));

        verify(repository).saveAll(captors.capture());
        List<OrderSettingDiscountModel> modelList = captors.getValue();
        assertEquals(2, modelList.size(), 0);
        assertEquals(1l, modelList.get(0).getCompanyId(), 0);
        assertEquals(10l, modelList.get(0).getProductId(), 0);
    }

    @Test
    public void findAllByCompanyIdAndProductIdAndDeleted() {
        service.findAllByCompanyIdAndProductIdAndDeleted(1l, Arrays.asList(12l));
        verify(repository).findAllByCompanyIdAndProductIdInAndAndDeleted(anyLong(), anyList(), anyBoolean());
    }

    @Test
    public void findAllByCompanyIdAndCategoryCodeAndDeleted() {
        service.findAllByCompanyIdAndCategoryCodeAndDeleted(1l, Arrays.asList("code"));
        verify(repository).findAllByCompanyIdAndCategoryCodeInAndDeleted(anyLong(), anyList(), anyBoolean());
    }

    @Test
    public void findOneByIdAndCompanyId() {
        service.findOneByIdAndCompanyId(12l, 1l);
        verify(repository).findByCompanyIdAndId(anyLong(), anyLong());
    }

    @Test
    public void findByCompanyIdAndProductIdAndDeleted() {
        service.findByCompanyIdAndProductIdAndDeleted(12l, 1l);
        verify(repository).findByCompanyIdAndProductIdAndDeleted(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    public void findAllCatgorySetting() {
        service.findAllCatgorySetting(12l);
        verify(repository).findAllByCompanyIdAndCategoryCodeIsNotNullAndDeleted(anyLong(), anyBoolean());
    }
}
