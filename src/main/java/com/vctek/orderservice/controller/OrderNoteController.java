package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.OrderNoteData;
import com.vctek.orderservice.dto.request.OrderNoteRequest;
import com.vctek.orderservice.facade.OrderNoteFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/qtn/order-note")
public class OrderNoteController {
    private OrderNoteFacade orderNoteFacade;

    @Autowired
    public OrderNoteController(OrderNoteFacade orderNoteFacade) {
        this.orderNoteFacade = orderNoteFacade;
    }

    @PostMapping
    public ResponseEntity<OrderNoteData> createNote(@RequestBody OrderNoteRequest orderNoteRequest){
        OrderNoteData orderNoteData = orderNoteFacade.create(orderNoteRequest);
        return new ResponseEntity<>(orderNoteData, HttpStatus.OK);
    }

    @GetMapping("/{orderCode}")
    public ResponseEntity<List<OrderNoteData>> getNote(@PathVariable("orderCode") String orderCode){
        List<OrderNoteData> orderNoteData = orderNoteFacade.findAllByOrderCode(orderCode);
        return new ResponseEntity<>(orderNoteData, HttpStatus.OK);
    }

    @PostMapping("/{orderNoteId}/delete")
    public ResponseEntity<Void> deleteOrderNote(@PathVariable Long orderNoteId) {
        orderNoteFacade.remove(orderNoteId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
