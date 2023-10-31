package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderNoteModel;
import com.vctek.orderservice.repository.OrderNoteRepository;
import com.vctek.orderservice.service.OrderNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderNoteServiceImpl implements OrderNoteService {
    private OrderNoteRepository orderNoteRepository;

    @Autowired
    public OrderNoteServiceImpl(OrderNoteRepository orderNoteRepository) {
        this.orderNoteRepository = orderNoteRepository;
    }


    @Override
    public OrderNoteModel save(OrderNoteModel orderNoteModel) {
        return orderNoteRepository.save(orderNoteModel);
    }

    @Override
    public List<OrderNoteModel> findAllByOrderCode(String orderCode) {
        return orderNoteRepository.findAllByOrderCode(orderCode);
    }

    @Override
    public void delete(OrderNoteModel orderNoteModel) {
        orderNoteRepository.delete(orderNoteModel);
    }

    @Override
    public OrderNoteModel findById(Long orderNoteId) {
        Optional<OrderNoteModel> orderNoteModel = orderNoteRepository.findById(orderNoteId);
        return orderNoteModel.isPresent() ? orderNoteModel.get() : null;
    }
}
