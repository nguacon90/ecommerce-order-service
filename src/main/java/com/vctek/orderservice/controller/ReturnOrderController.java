package com.vctek.orderservice.controller;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderData;
import com.vctek.orderservice.dto.ReturnOrderData;
import com.vctek.orderservice.dto.ReturnOrderVatData;
import com.vctek.orderservice.dto.ReturnRewardRedeemData;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.dto.request.ReturnOrderSearchRequest;
import com.vctek.orderservice.dto.request.ReturnOrderUpdateParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.ReturnOrderFacade;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qtn/return-orders")
public class ReturnOrderController {

    private Validator<ReturnOrderRequest> returnOrderRequestValidator;
    private Validator<ReturnOrderRequest> updateReturnOrderRequestValidator;
    private Validator<ReturnOrderUpdateParameter> returnOrderUpdateParameterValidator;
    private ReturnOrderFacade returnOrderFacade;

    @PostMapping
    @PreAuthorize("hasAnyPermission(#returnOrderRequest.companyId, " +
            "T(com.vctek.util.PermissionCodes).CREATE_BILL_EXCHANGE.code())")
    public ResponseEntity<ReturnOrderData> createNewReturnOrder(@RequestBody ReturnOrderRequest returnOrderRequest) {
        returnOrderRequestValidator.validate(returnOrderRequest);
        ReturnOrderData data = returnOrderFacade.create(returnOrderRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/{returnOrderId}")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).VIEW_DETAIL_BILL_EXCHANGE.code())")
    public ResponseEntity<ReturnOrderData> getDetail(@PathVariable("returnOrderId") Long returnOrderId,
                                                     @RequestParam("companyId") Long companyId) {
        ReturnOrderData data = returnOrderFacade.getDetail(returnOrderId, companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{returnOrderId}")
    @PreAuthorize("hasAnyPermission(#returnOrderRequest.companyId, " +
            "T(com.vctek.util.PermissionCodes).UPDATE_BILL_EXCHANGE.code()," +
            "T(com.vctek.util.PermissionCodes).CREATE_BILL_EXCHANGE.code())")
    public ResponseEntity<ReturnOrderData> saveInfo(@PathVariable("returnOrderId") Long returnOrderId,
                                                    @RequestBody ReturnOrderRequest returnOrderRequest) {
        returnOrderRequest.setId(returnOrderId);
        updateReturnOrderRequestValidator.validate(returnOrderRequest);
        ReturnOrderData data = returnOrderFacade.updateInfo(returnOrderRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{returnOrderId}/exchange-order")
    @PreAuthorize("hasAnyPermission(#parameter.companyId, " +
            "T(com.vctek.util.PermissionCodes).UPDATE_BILL_EXCHANGE.code()," +
            "T(com.vctek.util.PermissionCodes).CREATE_BILL_EXCHANGE.code())")
    public ResponseEntity<OrderData> createExchangeOrder(@PathVariable("returnOrderId") Long returnOrderId,
                                                         @RequestBody ReturnOrderUpdateParameter parameter) {
        parameter.setReturnOrderId(returnOrderId);
        if (parameter.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        OrderData data = returnOrderFacade.createOrGetExchangeOrder(parameter);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{returnOrderId}/exchange-order/{orderCode}/change-warehouse")
    @PreAuthorize("hasAnyPermission(#parameter.companyId, " +
            "T(com.vctek.util.PermissionCodes).UPDATE_BILL_EXCHANGE.code()," +
            "T(com.vctek.util.PermissionCodes).CREATE_BILL_EXCHANGE.code())")
    public ResponseEntity<OrderData> changeWarehouse(@PathVariable("returnOrderId") Long returnOrderId,
                                                     @PathVariable("orderCode") String exchangeOrderCode,
                                                     @RequestBody ReturnOrderUpdateParameter parameter) {
        parameter.setReturnOrderId(returnOrderId);
        parameter.setExchangeOrderCode(exchangeOrderCode);
        returnOrderUpdateParameterValidator.validate(parameter);
        OrderData data = returnOrderFacade.doChangeWarehouse(parameter);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/info-reward-redeem")
    @PreAuthorize("hasAnyPermission(#request.companyId, " +
            "T(com.vctek.util.PermissionCodes).CREATE_BILL_EXCHANGE.code())")
    public ResponseEntity<ReturnRewardRedeemData> getRevertAmount(@RequestBody ReturnOrderRequest request) {
        returnOrderRequestValidator.validate(request);
        ReturnRewardRedeemData data = returnOrderFacade.getReturnRewardRedeem(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
    @GetMapping("/{originOrderCode}/info-vat")
    public ResponseEntity<ReturnOrderVatData> getInfoVatOfReturnOrderWithOriginOrderCode(@PathVariable("originOrderCode") String originOrderCode,
                                                                                         @RequestParam("companyId") Long companyId) {
        ReturnOrderVatData data = returnOrderFacade.getInfoVatOfReturnOrderWithOriginOrderCode(originOrderCode, companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{returnOrderId}/refund-point")
    @PreAuthorize("hasAnyPermission(#returnOrderRequest.companyId, " +
            "T(com.vctek.util.PermissionCodes).UPDATE_BILL_EXCHANGE.code()," +
            "T(com.vctek.util.PermissionCodes).CREATE_BILL_EXCHANGE.code())")
    public ResponseEntity<ReturnOrderData> updateRefundPoint(@PathVariable("returnOrderId") Long returnOrderId,
                                                    @RequestBody ReturnOrderRequest returnOrderRequest) {
        returnOrderRequest.setId(returnOrderId);
        ReturnOrderData data = returnOrderFacade.updateRefundPoint(returnOrderRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }


    @Autowired
    @Qualifier("returnOrderRequestValidator")
    public void setReturnOrderRequestValidator(Validator<ReturnOrderRequest> returnOrderRequestValidator) {
        this.returnOrderRequestValidator = returnOrderRequestValidator;
    }

    @Autowired
    public void setReturnOrderFacade(ReturnOrderFacade returnOrderFacade) {
        this.returnOrderFacade = returnOrderFacade;
    }

    @Autowired
    public void setReturnOrderUpdateParameterValidator(Validator<ReturnOrderUpdateParameter> returnOrderUpdateParameterValidator) {
        this.returnOrderUpdateParameterValidator = returnOrderUpdateParameterValidator;
    }

    @Autowired
    @Qualifier("updateReturnOrderRequestValidator")
    public void setUpdateReturnOrderRequestValidator(Validator<ReturnOrderRequest> updateReturnOrderRequestValidator) {
        this.updateReturnOrderRequestValidator = updateReturnOrderRequestValidator;
    }
}
