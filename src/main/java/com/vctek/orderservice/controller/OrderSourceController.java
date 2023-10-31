package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.OrderSourceData;
import com.vctek.orderservice.dto.request.OrderSourceRequest;
import com.vctek.orderservice.facade.OrderSourceFacade;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/order-source")
public class OrderSourceController {
    private OrderSourceFacade facade;
    private Validator<OrderSourceRequest> orderSourceRequestValidator;

    @Autowired
    public OrderSourceController(OrderSourceFacade facade, Validator<OrderSourceRequest> orderSourceRequestValidator) {
        this.facade = facade;
        this.orderSourceRequestValidator = orderSourceRequestValidator;
    }

    @PostMapping
    @PreAuthorize("hasAnyPermission(#orderSourceRequest.companyId, T(com.vctek.util.PermissionCodes).CREATE_ORDER_SOURCE.code())")
    public ResponseEntity<OrderSourceData> createdNew(@RequestBody OrderSourceRequest orderSourceRequest) {
        orderSourceRequestValidator.validate(orderSourceRequest);
        OrderSourceData orderSourceData = facade.create(orderSourceRequest);
        return ResponseEntity.ok(orderSourceData);
    }
    
    @GetMapping("/{orderSourceId}")
    public ResponseEntity<OrderSourceData> getOrderSourceById(@PathVariable("orderSourceId") Long orderSourceId,
                                                      @RequestParam("companyId") Long companyId) {
        OrderSourceData orderSourceData = facade.findByIdAndCompanyId(orderSourceId, companyId);
        return new ResponseEntity<>(orderSourceData, HttpStatus.OK);
    }

    @PutMapping("/{orderSourceId}")
    @PreAuthorize("hasAnyPermission(#orderSourceRequest.companyId, T(com.vctek.util.PermissionCodes).UPDATE_ORDER_SOURCE.code())")
    public ResponseEntity<OrderSourceData> update(@RequestBody OrderSourceRequest orderSourceRequest,
                                                       @PathVariable("orderSourceId") Long orderSourceId) {
        orderSourceRequest.setId(orderSourceId);
        orderSourceRequestValidator.validate(orderSourceRequest);
        OrderSourceData orderSourceData = facade.update(orderSourceRequest);
        return new ResponseEntity<>(orderSourceData, HttpStatus.OK);
    }

    @PutMapping("/rearrange-order")
    @PreAuthorize("hasAnyPermission(#companyId, T(com.vctek.util.PermissionCodes).UPDATE_ORDER_SOURCE.code())")
    public ResponseEntity<List<OrderSourceData>> rearrangeOrder(@RequestParam Long companyId, @RequestBody List<OrderSourceRequest> requests) {
        for (OrderSourceRequest request: requests) {
            orderSourceRequestValidator.validate(request);
        }
        List<OrderSourceData> data = facade.rearrangeOrder(requests);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<OrderSourceData>> getOrderSourceByCompany(@RequestParam Long companyId) {
        List<OrderSourceData> orderSourceData = facade.findAllByCompanyId(companyId);
        return new ResponseEntity<>(orderSourceData, HttpStatus.OK);

    }

}
