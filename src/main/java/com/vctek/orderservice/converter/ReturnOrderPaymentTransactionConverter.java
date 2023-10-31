package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.model.PaymentTransactionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("returnOrderPaymentTransactionConverter")
public class ReturnOrderPaymentTransactionConverter extends AbstractPopulatingConverter<PaymentTransactionModel, PaymentTransactionData> {

    @Autowired
    @Qualifier("returnOrderPaymentTransactionPopulator")
    private Populator<PaymentTransactionModel, PaymentTransactionData> returnOrderPaymentTransactionPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(PaymentTransactionData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(returnOrderPaymentTransactionPopulator);
    }
}
