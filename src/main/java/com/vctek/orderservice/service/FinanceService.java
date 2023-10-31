package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.MoneySourceData;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.feignclient.dto.InvoiceData;
import com.vctek.orderservice.feignclient.dto.InvoiceOrderData;
import com.vctek.orderservice.feignclient.dto.InvoiceRequest;

import java.util.List;

public interface FinanceService {
    PaymentMethodData getPaymentMethod(Long paymentMethodId);

    MoneySourceData getMoneySource(Long moneySourceId, Long companyId);

    PaymentMethodData getPaymentMethodByCode(String paymentMethodCode);

    InvoiceOrderData createInvoiceOrder(List<InvoiceRequest> invoiceRequests);

    InvoiceOrderData createInvoiceReturnOrder(List<InvoiceRequest> invoiceRequests, String invoiceType);

    void unverifyInvoiceOrder(List<Long> invoiceIds, String orderCode);

    void reverseVerifyInvoiceWithOnline(String orderCode, Long companyId);

    List<InvoiceData> findAllOrderInvoices(Long companyId, String orderCode, Long returnOrderId, String orderType);
}
