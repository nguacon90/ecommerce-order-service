package com.vctek.orderservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.PaymentTransactionData;
import com.vctek.orderservice.model.PaymentTransactionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionDataConverter extends AbstractPopulatingConverter<PaymentTransactionModel, PaymentTransactionData> {

    @Autowired
    private Populator<PaymentTransactionModel, PaymentTransactionData> basicPaymentTransactionPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(PaymentTransactionData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(basicPaymentTransactionPopulator);
    }
}
