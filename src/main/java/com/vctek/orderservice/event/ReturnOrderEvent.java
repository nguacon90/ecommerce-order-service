package com.vctek.orderservice.event;

import com.vctek.orderservice.feignclient.dto.BillRequest;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;

public class ReturnOrderEvent extends AbstractApplicationEvent {
    private ReturnOrderModel returnOrder;
    private BillRequest billRequest;
    private OrderModel exchangeOrder;

    public ReturnOrderEvent(ReturnOrderModel returnOrder, ReturnOrderEventType eventType) {
        super(eventType);
        this.returnOrder = returnOrder;
    }

    public ReturnOrderModel getReturnOrder() {
        return returnOrder;
    }

    public BillRequest getBillRequest() {
        return billRequest;
    }

    public void setBillRequest(BillRequest billRequest) {
        this.billRequest = billRequest;
    }

    public OrderModel getExchangeOrder() {
        return exchangeOrder;
    }

    public void setExchangeOrder(OrderModel exchangeOrder) {
        this.exchangeOrder = exchangeOrder;
    }
}
