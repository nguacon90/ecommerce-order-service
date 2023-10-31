package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.model.PaymentTransactionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("orderPaymentTransactionConverter")
public class OrderPaymentTransactionConverter extends AbstractPopulatingConverter<PaymentTransactionModel, PaymentTransactionData> {

    @Autowired
    @Qualifier("orderPaymentTransactionPopulator")
    private Populator<PaymentTransactionModel, PaymentTransactionData> orderPaymentTransactionPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(PaymentTransactionData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(orderPaymentTransactionPopulator);
    }
}
