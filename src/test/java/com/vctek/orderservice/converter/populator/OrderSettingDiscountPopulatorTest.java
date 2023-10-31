package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.dto.OrderSettingData;
import com.vctek.orderservice.dto.OrderSettingDiscountData;
import com.vctek.orderservice.model.OrderSettingDiscountModel;
import com.vctek.orderservice.model.OrderSettingModel;
import com.vctek.orderservice.util.CurrencyType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OrderSettingDiscountPopulatorTest {
    private OrderSettingDiscountPopulator populator;

    @Before
    public void setUp() {
        populator = new OrderSettingDiscountPopulator();
    }

    @Test
    public void populate() {
        OrderSettingDiscountModel source = new OrderSettingDiscountModel();
        source.setId(1l);
        source.setDiscount(10000d);
        source.setDiscountType(CurrencyType.CASH.toString());

        OrderSettingDiscountData target = new OrderSettingDiscountData();
        populator.populate(source, target);

        assertEquals(10000d, target.getDiscount(), 0);
        assertEquals(1l, target.getId(), 0);

    }
}
