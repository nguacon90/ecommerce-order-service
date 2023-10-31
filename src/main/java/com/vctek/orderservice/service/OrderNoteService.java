package com.vctek.orderservice.service;

import com.vctek.orderservice.model.OrderNoteModel;

import java.util.List;

public interface OrderNoteService {

    OrderNoteModel save(OrderNoteModel orderNoteModel);

    List<OrderNoteModel> findAllByOrderCode(String orderCode);

    void delete(OrderNoteModel orderNoteModel);

    OrderNoteModel findById(Long orderNoteId);
}
