package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.MoneySourceData;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.feignclient.FinanceClient;
import com.vctek.orderservice.feignclient.dto.InvoiceData;
import com.vctek.orderservice.feignclient.dto.InvoiceOrderData;
import com.vctek.orderservice.feignclient.dto.InvoiceRequest;
import com.vctek.orderservice.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinanceServiceImpl implements FinanceService {
    private FinanceClient financeClient;

    @Autowired
    public FinanceServiceImpl(FinanceClient financeClient) {
        this.financeClient = financeClient;
    }

    @Override
    @Cacheable(unless="#result == null", value = "paymentMethod", key = "#paymentMethodId", cacheManager = "microServiceCacheManager")
    public PaymentMethodData getPaymentMethod(Long paymentMethodId) {
        return financeClient.getPaymentMethodData(paymentMethodId);
    }

    @Override
    @Cacheable(unless="#result == null", value = "moneySource",
            key = "#moneySourceId.toString() + '_' + #companyId.toString()", cacheManager = "microServiceCacheManager")
    public MoneySourceData getMoneySource(Long moneySourceId, Long companyId) {
        return financeClient.getMoneySource(moneySourceId, companyId);
    }

    @Override
    @Cacheable(unless="#result == null", value = "paymentMethodByCode",
            key = "#paymentMethodCode.toString()", cacheManager = "microServiceCacheManager")
    public PaymentMethodData getPaymentMethodByCode(String paymentMethodCode) {
        return financeClient.getPaymentMethodDataByCode(paymentMethodCode);
    }

    @Override
    public InvoiceOrderData createInvoiceOrder(List<InvoiceRequest> invoiceRequests) {
        return financeClient.createInvoiceOrder(invoiceRequests);
    }

    @Override
    public InvoiceOrderData createInvoiceReturnOrder(List<InvoiceRequest> invoiceRequests, String invoiceType) {
        return financeClient.createInvoiceReturnOrder(invoiceRequests, invoiceType);
    }

    @Override
    public void unverifyInvoiceOrder(List<Long> invoiceIds, String orderCode) {
        financeClient.unverifyInvoiceOrder(invoiceIds, orderCode);
    }

    @Override
    public void reverseVerifyInvoiceWithOnline(String orderCode, Long companyId) {
        financeClient.reverseVerifyInvoiceWithOnline(orderCode, companyId);
    }

    @Override
    public List<InvoiceData> findAllOrderInvoices(Long companyId, String orderCode, Long returnOrderId, String orderType) {
        return financeClient.findAllOrderInvoices(companyId, orderCode, returnOrderId, orderType);
    }
}
