package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.OrderNoteData;
import com.vctek.orderservice.dto.request.OrderNoteRequest;

import java.util.List;

public interface OrderNoteFacade {
    OrderNoteData create(OrderNoteRequest orderNoteRequest);

    List<OrderNoteData> findAllByOrderCode(String orderCode);

    void remove(Long orderNoteId);
}
