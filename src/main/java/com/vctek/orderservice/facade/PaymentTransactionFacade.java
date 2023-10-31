package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.PaymentTransactionData;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;

import java.util.List;

public interface PaymentTransactionFacade {
    PaymentTransactionData create(PaymentTransactionRequest request);

    PaymentTransactionData update(PaymentTransactionRequest request);

    void delete(Long paymentMethodId);

    List<PaymentTransactionData> findAll();

    PaymentTransactionData findById(Long paymentMethodId);

    void migratePaymentForInvoice();
}
