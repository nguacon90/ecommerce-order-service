package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderNoteData;
import com.vctek.orderservice.dto.UserData;
import com.vctek.orderservice.model.OrderNoteModel;
import com.vctek.orderservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderNotePopulator implements Populator<OrderNoteModel, OrderNoteData> {
    private AuthService authService;

    @Autowired
    public OrderNotePopulator(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void populate(OrderNoteModel orderNoteModel, OrderNoteData orderNoteData) {
        orderNoteData.setId(orderNoteModel.getId());
        orderNoteData.setContent(orderNoteModel.getContent());
        orderNoteData.setCreatedTime(orderNoteModel.getCreatedTime());
        orderNoteData.setOrderId(orderNoteModel.getOrder().getId());
        populateUser(orderNoteModel, orderNoteData);
    }

    protected void populateUser(OrderNoteModel orderNoteModel, OrderNoteData orderNoteData) {
        UserData userData = authService.getUserById(orderNoteModel.getCreatedBy());
        if (userData != null) {
            orderNoteData.setCreatedBy(userData.getId());
            orderNoteData.setCreatedName(userData.getName());
        }
    }
}
