package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.OrderStatusImportData;
import com.vctek.orderservice.dto.request.OrderStatusImportRequest;
import com.vctek.orderservice.dto.request.OrderStatusImportSearchRequest;
import com.vctek.orderservice.facade.OrderStatusImportFacade;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order-status-import")
public class OrderStatusImportController {
    private OrderStatusImportFacade facade;
    private Validator<OrderStatusImportRequest> orderStatusImportRequestValidator;

    @PostMapping
    @PreAuthorize("hasAnyPermission(#request.companyId, " +
            "T(com.vctek.util.PermissionCodes).UPDATE_STATUS_ORDER.code())")
    public ResponseEntity<OrderStatusImportData> createStatusImport(@RequestBody OrderStatusImportRequest request) {
        orderStatusImportRequestValidator.validate(request);
        OrderStatusImportData data = facade.createStatusImport(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderStatusImportData> getDetail(@PathVariable("id") Long id,
                                                        @RequestParam("companyId") Long companyId) {
        OrderStatusImportData data = facade.findByIdAndCompanyId(id, companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<OrderStatusImportData>> search(@RequestParam("companyId") Long companyId,
                                                              OrderStatusImportSearchRequest request,
                                                              @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize,
                                                              @RequestParam(value = "page", required = false, defaultValue = "0") int page) {
        request.setCompanyId(companyId);
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<OrderStatusImportData> data = facade.search(request, pageable);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @Autowired
    public void setFacade(OrderStatusImportFacade facade) { this.facade = facade; }

    @Autowired
    public void setOrderStatusImportRequestValidator(Validator<OrderStatusImportRequest> orderStatusImportRequestValidator) {
        this.orderStatusImportRequestValidator = orderStatusImportRequestValidator;
    }
}
