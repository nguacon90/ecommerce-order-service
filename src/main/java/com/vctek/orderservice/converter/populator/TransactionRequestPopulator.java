package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.kafka.data.loyalty.TransactionRequest;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.util.CommonUtils;
import org.springframework.stereotype.Component;

@Component
public class TransactionRequestPopulator implements Populator<OrderModel, TransactionRequest> {

    @Override
    public void populate(OrderModel orderModel, TransactionRequest transactionRequest) {
        transactionRequest.setCompanyId(orderModel.getCompanyId());
        transactionRequest.setCardNumber(orderModel.getCardNumber());
        String invoiceNumber = CommonUtils.generateUuId();
        transactionRequest.setInvoiceNumber(invoiceNumber);
        transactionRequest.setWarehouseId(orderModel.getWarehouseId());
    }
}
