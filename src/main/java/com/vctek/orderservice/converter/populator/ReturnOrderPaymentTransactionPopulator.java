package com.vctek.orderservice.converter.populator;

import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.model.PaymentTransactionModel;
import org.springframework.stereotype.Component;

@Component("returnOrderPaymentTransactionPopulator")
public class ReturnOrderPaymentTransactionPopulator extends OrderPaymentTransactionPopulator {

    @Override
    public void populate(PaymentTransactionModel model, PaymentTransactionData data) {
        populateCommon(model, data);
        populateOrderData(model, data);
    }

    @Override
    protected void populateOrderData(PaymentTransactionModel source, PaymentTransactionData target) {
        target.setReturnOrderId(source.getReturnOrder().getId());
    }
}
