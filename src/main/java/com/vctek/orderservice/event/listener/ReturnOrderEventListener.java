package com.vctek.orderservice.event.listener;

import com.vctek.orderservice.event.ReturnOrderEvent;
import com.vctek.orderservice.event.ReturnOrderEventType;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ReturnOrderEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnOrderEventListener.class);
    private InvoiceService invoiceService;

    @EventListener
    public void handleReturnOrderEvent(final ReturnOrderEvent event) {
        if (ReturnOrderEventType.CREATE.equals(event.getEventType())) {
            try {
                ReturnOrderModel returnOrder = event.getReturnOrder();
                if (returnOrder == null) {
                    LOGGER.warn("Empty return order");
                    return;
                }
                invoiceService.createInvoiceForReturnOrder(event);

            } catch (RuntimeException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Autowired
    public void setInvoiceService(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

}
