package com.vctek.orderservice.event.listener;

import com.vctek.orderservice.event.ReturnOrderEvent;
import com.vctek.orderservice.event.ReturnOrderEventType;
import com.vctek.orderservice.facade.ReturnOrderDocumentFacade;
import com.vctek.orderservice.kafka.producer.UpdateReturnOrderProducer;
import com.vctek.orderservice.model.ReturnOrderModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ReturnOrderEventIndexListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnOrderEventIndexListener.class);
    private ReturnOrderDocumentFacade returnOrderDocumentFacade;
    private UpdateReturnOrderProducer updateReturnOrderProducer;

    public ReturnOrderEventIndexListener(ReturnOrderDocumentFacade returnOrderDocumentFacade) {
        this.returnOrderDocumentFacade = returnOrderDocumentFacade;
    }

    @EventListener
    public void handleReturnOrderEvent(final ReturnOrderEvent event) {
        try {
            ReturnOrderModel returnOrder = event.getReturnOrder();
            if (returnOrder == null) {
                LOGGER.warn("Empty return order");
                return;
            }
            if (ReturnOrderEventType.CREATE.equals(event.getEventType()) ||
                    ReturnOrderEventType.UPDATE_EXCHANGE_ORDER.equals(event.getEventType())) {
                returnOrderDocumentFacade.index(returnOrder);
                updateReturnOrderProducer.process(returnOrder);
            } else if (ReturnOrderEventType.UPDATE.equals(event.getEventType())) {
                returnOrderDocumentFacade.updateReturnOrderInfo(returnOrder);
                updateReturnOrderProducer.process(returnOrder);
            }
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Autowired
    public void setUpdateReturnOrderProducer(UpdateReturnOrderProducer updateReturnOrderProducer) {
        this.updateReturnOrderProducer = updateReturnOrderProducer;
    }
}
