package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.request.CheckTotalSellingOfProductRequest;
import com.vctek.orderservice.facade.CheckProductSellingFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CheckProductSellingController {

    private CheckProductSellingFacade checkProductSellingFacade;

    public CheckProductSellingController(CheckProductSellingFacade checkProductSellingFacade) {
        this.checkProductSellingFacade = checkProductSellingFacade;
    }

    @GetMapping("/check-total-sell")
    public ResponseEntity<Map<Long, Long>> checkProductHasSelling
            (CheckTotalSellingOfProductRequest request) {
        Map<Long, Long> data = checkProductSellingFacade.checkTotalSellingOfProduct(request);
        return new ResponseEntity(data, HttpStatus.OK);
    }
}
