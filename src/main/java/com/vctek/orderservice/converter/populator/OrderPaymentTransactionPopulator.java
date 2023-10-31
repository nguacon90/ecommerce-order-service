package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("orderPaymentTransactionPopulator")
public class OrderPaymentTransactionPopulator implements Populator<PaymentTransactionModel, PaymentTransactionData> {

    protected FinanceService financeService;

    @Override
    public void populate(PaymentTransactionModel model, PaymentTransactionData data) {
        populateCommon(model, data);
        populateOrderData(model, data);
        data.setMoneySourceType(model.getMoneySourceType());
    }

    protected void populateCommon(final PaymentTransactionModel source, final PaymentTransactionData target) {
        target.setAmount(source.getAmount());
        target.setMoneySourceId(source.getMoneySourceId());
        target.setType(source.getMoneySourceType());
        target.setPaymentMethodId(source.getPaymentMethodId());
        target.setInvoiceId(source.getInvoiceId());
    }

    protected void populateOrderData(final PaymentTransactionModel source, final PaymentTransactionData target) {
        target.setOrderId(source.getOrderModel().getId());
    }

    @Autowired
    public void setFinanceService(FinanceService financeService) {
        this.financeService = financeService;
    }
}
