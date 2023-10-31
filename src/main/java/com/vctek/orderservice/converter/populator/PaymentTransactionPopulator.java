package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.PaymentTransactionData;
import com.vctek.orderservice.model.PaymentTransactionModel;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionPopulator implements Populator<PaymentTransactionModel, PaymentTransactionData> {

    @Override
    public void populate(PaymentTransactionModel source, PaymentTransactionData target) {
        target.setId(source.getId());
        target.setNote(source.getNote());
        target.setAmount(source.getAmount());
        target.setMoneySourceId(source.getMoneySourceId());
        target.setType(source.getMoneySourceType());
        target.setOrderId(source.getOrderModel().getId());
        target.setPaymentMethodId(source.getPaymentMethodId());
        target.setConversionRate(source.getConversionRate());
    }
}
