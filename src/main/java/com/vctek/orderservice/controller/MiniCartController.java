package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.CartInfoParameter;
import com.vctek.orderservice.dto.MiniCartData;
import com.vctek.orderservice.facade.MiniCartFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/qtn/mini-carts")
public class MiniCartController {

    private MiniCartFacade miniCartFacade;

    public MiniCartController(MiniCartFacade miniCartFacade) {
        this.miniCartFacade = miniCartFacade;
    }

    @GetMapping
    public ResponseEntity<List<MiniCartData>> findAllCartIds(CartInfoParameter cartInfoParameter) {
        List<MiniCartData> miniCartData = miniCartFacade.findAllByUser(cartInfoParameter);
        return new ResponseEntity<>(miniCartData, HttpStatus.OK);
    }
}
