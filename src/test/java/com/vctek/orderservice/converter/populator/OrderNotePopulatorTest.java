package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderNoteData;
import com.vctek.orderservice.dto.UserData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderNoteModel;
import com.vctek.orderservice.service.AuthService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderNotePopulatorTest {
    private Populator<OrderNoteModel, OrderNoteData> orderNoteDataPopulator;
    private AuthService authService;
    @Before
    public void setUp() {
        authService = mock(AuthService.class);
        orderNoteDataPopulator = new OrderNotePopulator(authService);
    }

    @Test
    public void populate() {
        OrderNoteModel source = new OrderNoteModel();
        source.setId(1L);
        source.setContent("note");
        source.setCreatedBy(1l);
        OrderModel orderModel = new OrderModel();
        orderModel.setId(1l);
        source.setOrder(orderModel);
        UserData userData = new UserData();
        userData.setId(1l);
        userData.setName("name");
        when(authService.getUserById(anyLong())).thenReturn(userData);
        OrderNoteData data = new OrderNoteData();
        orderNoteDataPopulator.populate(source, data);

        assertEquals(source.getId(), data.getId());
        assertEquals(source.getOrder().getId(), data.getOrderId());
        assertEquals(source.getContent(), data.getContent());

    }
}
