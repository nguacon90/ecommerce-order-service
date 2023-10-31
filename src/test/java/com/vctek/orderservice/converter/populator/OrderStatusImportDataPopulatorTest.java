package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.dto.OrderStatusImportData;
import com.vctek.orderservice.model.OrderStatusImportDetailModel;
import com.vctek.orderservice.model.OrderStatusImportModel;
import com.vctek.util.OrderStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class OrderStatusImportDataPopulatorTest {
    OrderStatusImportModel model;
    OrderStatusImportDataPopulator populator;

    @Before
    public void setUp() {
        populator = new OrderStatusImportDataPopulator();
    }

    @Test
    public void populate() {
        model = new OrderStatusImportModel();
        model.setId(2L);
        model.setCompanyId(2L);
        model.setOrderStatus(OrderStatus.NEW.code());
        model.setStatus("PENDING");
        model.setCreatedBy(2L);
        model.setCreatedTime(new Date());
        List<OrderStatusImportDetailModel> detailModels = new ArrayList<>();
        OrderStatusImportDetailModel detailModel = new OrderStatusImportDetailModel();
        detailModel.setId(2L);
        detailModel.setOrderCode("ORDER_CODE");
        detailModel.setOldOrderStatus("OLD_STATUS");
        detailModel.setNewOrderStatus("NEW_STATUS");
        detailModel.setStatus("STATUS");
        detailModel.setNote("NOTE");
        detailModels.add(detailModel);
        detailModel.setOrderStatusImportModel(model);
        model.setOrderStatusImportDetailModels(detailModels);
        OrderStatusImportData data = new OrderStatusImportData();

        populator.populate(model, data);

        assertEquals(model.getId(), data.getId());
        assertEquals(model.getCompanyId(), data.getCompanyId());
        assertEquals(model.getOrderStatus(), data.getOrderStatus());
        assertEquals(model.getStatus(), data.getStatus());
        assertEquals(model.getCreatedTime(), data.getCreatedTime());
        assertEquals(model.getCreatedBy(), data.getCreatedBy());
        assertEquals(detailModel.getId(), data.getDetails().get(0).getId());
        assertEquals(detailModel.getOrderCode(), data.getDetails().get(0).getOrderCode());
        assertEquals(detailModel.getOldOrderStatus(), data.getDetails().get(0).getOldOrderStatus());
        assertEquals(detailModel.getNewOrderStatus(), data.getDetails().get(0).getNewOrderStatus());
        assertEquals(detailModel.getStatus(), data.getDetails().get(0).getStatus());
        assertEquals("NOTE", data.getDetails().get(0).getNote());

    }
}