package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.PaymentTransactionData;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.facade.PaymentTransactionFacade;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/payment-transactions")
public class PaymentTransactionController {
    private PaymentTransactionFacade paymentTransactionFacade;
    private Validator<PaymentTransactionRequest> paymentTransactionRequestValidator;

    @Autowired
    public PaymentTransactionController(PaymentTransactionFacade paymentTransactionFacade, Validator<PaymentTransactionRequest> paymentTransactionRequestValidator) {
        this.paymentTransactionFacade = paymentTransactionFacade;
        this.paymentTransactionRequestValidator = paymentTransactionRequestValidator;
    }


    @PostMapping
    public ResponseEntity<PaymentTransactionData> create(@RequestBody PaymentTransactionRequest request) {
        paymentTransactionRequestValidator.validate(request);
        PaymentTransactionData paymentMethodData = paymentTransactionFacade.create(request);
        return new ResponseEntity<>(paymentMethodData, HttpStatus.OK);
    }

    @PutMapping("/{paymentTransactionId}")
    public ResponseEntity<PaymentTransactionData> update(@RequestBody PaymentTransactionRequest request,
                                                    @PathVariable("paymentTransactionId") Long paymentTransactionId) {
        request.setId(paymentTransactionId);
        paymentTransactionRequestValidator.validate(request);
        PaymentTransactionData paymentMethodData = paymentTransactionFacade.update(request);
        return new ResponseEntity<>(paymentMethodData, HttpStatus.OK);
    }

    @GetMapping("/{paymentTransactionId}")
    public ResponseEntity<PaymentTransactionData> getPaymentTransaction(@RequestParam("companyId") Long companyId,
                                                            @PathVariable("paymentTransactionId") Long paymentTransactionId) {
        PaymentTransactionData data = paymentTransactionFacade.findById(paymentTransactionId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{paymentTransactionId}/delete")
    public ResponseEntity delete(@RequestParam("companyId") Long companyId,
                                 @PathVariable("paymentTransactionId") Long paymentTransactionId) {
        paymentTransactionFacade.delete(paymentTransactionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/migrate-invoice")
    public ResponseEntity migrateInvoice() {
        paymentTransactionFacade.migratePaymentForInvoice();
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<List<PaymentTransactionData>> findAll() {
        List<PaymentTransactionData> data = paymentTransactionFacade.findAll();
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
