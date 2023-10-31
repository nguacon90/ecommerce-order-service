package com.vctek.orderservice.feignclient;

import com.vctek.health.VersionClient;
import com.vctek.orderservice.dto.MoneySourceData;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.feignclient.dto.InvoiceData;
import com.vctek.orderservice.feignclient.dto.InvoiceOrderData;
import com.vctek.orderservice.feignclient.dto.InvoiceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Component
@FeignClient(name = "${vctek.microservices.finance:finance-service}")
public interface FinanceClient extends VersionClient {
    @GetMapping("/money-sources/{moneySourceId}/basic-info")
    MoneySourceData getMoneySource(@PathVariable(name = "moneySourceId") Long moneySourceId, @RequestParam("companyId") Long companyId);

    @GetMapping("/payment-methods/{paymentMethodId}")
    PaymentMethodData getPaymentMethodData(@PathVariable(name = "paymentMethodId") Long paymentMethodId);

    @PostMapping("/invoices/create-invoice-order")
    InvoiceOrderData createInvoiceOrder(@RequestBody List<InvoiceRequest> request);

    @PostMapping("/invoices/create-invoice-return-order/{invoiceType}")
    InvoiceOrderData createInvoiceReturnOrder(@RequestBody List<InvoiceRequest> request, @PathVariable("invoiceType") String invoiceType);

    @GetMapping("/invoices/get-paid-amount-order")
    Double getPaidAmountOrder(@RequestParam("orderCode") String code, @RequestParam("companyId") Long companyId);

    @GetMapping("/payment-methods")
    List<PaymentMethodData> getPaymentMethodDataByCompanyId(@RequestParam("companyId") Long companyId);

    @GetMapping("/order-invoices/")
    List<InvoiceData> findAllOrderInvoices(@RequestParam("companyId") Long companyId,
                                           @RequestParam(value = "orderCode", required = false) String orderCode,
                                           @RequestParam(value = "returnOrderId", required = false) Long returnOrderId,
                                           @RequestParam(value = "type") String type);

    @PostMapping("/invoices/unverify-invoice-order/{orderCode}")
    void unverifyInvoiceOrder(@RequestBody List<Long> invoiceIds, @PathVariable("orderCode") String orderCode);

    @GetMapping("/payment-methods/code/{paymentMethodCode}")
    PaymentMethodData getPaymentMethodDataByCode(@PathVariable("paymentMethodCode") String paymentMethodCode);

    @PostMapping("/invoices/reverse-verify-invoice-online")
    void reverseVerifyInvoiceWithOnline(@RequestParam("orderCode") String orderCode, @RequestParam("companyId") Long companyId);
}
