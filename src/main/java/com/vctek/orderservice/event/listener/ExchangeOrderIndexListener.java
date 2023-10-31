package com.vctek.orderservice.event.listener;

import com.vctek.orderservice.facade.ReturnOrderDocumentFacade;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ExchangeOrderIndexListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeOrderIndexListener.class);
    private ReturnOrderDocumentFacade returnOrderDocumentFacade;

    @TransactionalEventListener
    public void handleOrderEventToIndexExchangeOrder(OrderEvent orderEvent) {
        OrderModel orderModel = orderEvent.getOrderModel();
        if(orderModel == null) {
            return;
        }
        try {
            ReturnOrderModel returnOrder = orderModel.getReturnOrder();
            if (orderModel.isExchange() && returnOrder != null) {
                returnOrderDocumentFacade.updateExchangeOrder(orderModel, returnOrder);
            }
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Autowired
    public void setReturnOrderDocumentFacade(ReturnOrderDocumentFacade returnOrderDocumentFacade) {
        this.returnOrderDocumentFacade = returnOrderDocumentFacade;
    }
}
