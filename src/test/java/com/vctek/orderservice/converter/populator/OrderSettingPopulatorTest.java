package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Converter;
import com.vctek.orderservice.dto.OrderSettingData;
import com.vctek.orderservice.dto.OrderSettingDiscountData;
import com.vctek.orderservice.model.OrderSettingDiscountModel;
import com.vctek.orderservice.model.OrderSettingModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

public class OrderSettingPopulatorTest {
    private OrderSettingPopulator populator;
    @Mock
    private Converter<OrderSettingDiscountModel, OrderSettingDiscountData> orderSettingDiscountConverter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new OrderSettingPopulator();
        populator.setOrderSettingDiscountConverter(orderSettingDiscountConverter);
    }

    @Test
    public void populate() {
        OrderSettingModel source = new OrderSettingModel();
        source.setId(12l);
        source.setType("type");
        source.setCompanyId(1l);
        List<OrderSettingDiscountModel> settingDiscountModels = new ArrayList<>();
        OrderSettingDiscountModel discountModel = new OrderSettingDiscountModel();
        discountModel.setId(1l);
        settingDiscountModels.add(discountModel);
        source.setSettingDiscountModel(settingDiscountModels);

        OrderSettingData target = new OrderSettingData();
        OrderSettingDiscountData discountData = new OrderSettingDiscountData();
        discountData.setId(1l);

        populator.populate(source, target);

        assertEquals("type", target.getType());
        assertEquals(12l, target.getId(), 0);
        assertEquals(1l, target.getCompanyId(), 0);

    }
}
