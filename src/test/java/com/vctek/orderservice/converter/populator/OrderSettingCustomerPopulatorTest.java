package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.dto.OrderSettingCustomerData;
import com.vctek.orderservice.model.OrderSettingCustomerModel;
import com.vctek.orderservice.model.OrderSettingCustomerOptionModel;
import com.vctek.orderservice.model.OrderTypeSettingCustomerModel;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OrderSettingCustomerPopulatorTest {
    private OrderSettingCustomerPopulator populator;

    @Before
    public void setUp() {
        populator = new OrderSettingCustomerPopulator();
    }

    @Test
    public void populate() {
        OrderSettingCustomerModel source = new OrderSettingCustomerModel();
        source.setId(12l);
        source.setName("name");
        source.setCompanyId(1l);
        List<OrderTypeSettingCustomerModel> orderTypeSettingCustomerModels = new ArrayList<>();
        OrderTypeSettingCustomerModel orderTypeSettingCustomerModel = new OrderTypeSettingCustomerModel();

        orderTypeSettingCustomerModel.setId(1l);
        orderTypeSettingCustomerModel.setOrderType("orderType");
        orderTypeSettingCustomerModels.add(orderTypeSettingCustomerModel);
        source.setOrderTypeSettingCustomerModels(orderTypeSettingCustomerModels);

        List<OrderSettingCustomerOptionModel> optionModels = new ArrayList<>();
        OrderSettingCustomerOptionModel optionModel = new OrderSettingCustomerOptionModel();
        optionModel.setId(2L);
        optionModel.setName("optionName");
        optionModels.add(optionModel);
        OrderSettingCustomerOptionModel optionModel2 = new OrderSettingCustomerOptionModel();
        optionModel2.setId(2L);
        optionModel2.setName("optionName");
        optionModels.add(optionModel2);
        source.setOptionModels(optionModels);

        OrderSettingCustomerData target = new OrderSettingCustomerData();
        populator.populate(source, target);

        assertEquals(source.getName(), target.getName());
        assertEquals(source.getId(), target.getId(), 0);
        assertEquals(source.getCompanyId(), target.getCompanyId(), 0);

    }
}
