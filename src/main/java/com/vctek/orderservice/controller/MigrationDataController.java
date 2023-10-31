package com.vctek.orderservice.controller;

import com.vctek.orderservice.service.ReturnOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/migration-data")
public class MigrationDataController {
    private ReturnOrderService returnOrderService;


    @PostMapping("/link/return-order")
    public ResponseEntity<Long> linkReturnOrderForBill(@RequestParam("companyId") Long companyId) {
        returnOrderService.linkReturnOrderForBill(companyId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Autowired
    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }
}
