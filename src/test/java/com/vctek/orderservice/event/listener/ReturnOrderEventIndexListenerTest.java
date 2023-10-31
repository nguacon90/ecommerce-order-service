package com.vctek.orderservice.event.listener;

import com.vctek.orderservice.event.ReturnOrderEvent;
import com.vctek.orderservice.event.ReturnOrderEventType;
import com.vctek.orderservice.facade.ReturnOrderDocumentFacade;
import com.vctek.orderservice.kafka.producer.UpdateReturnOrderProducer;
import com.vctek.orderservice.model.ReturnOrderModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReturnOrderEventIndexListenerTest {
    private ReturnOrderEventIndexListener listener;
    @Mock
    private ReturnOrderDocumentFacade returnOrderDocumentFacade;
    @Mock
    private UpdateReturnOrderProducer updateReturnOrderProducer;
    private ReturnOrderEvent event = new ReturnOrderEvent(null, null);
    private ReturnOrderModel returnOrder = new ReturnOrderModel();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        listener = new ReturnOrderEventIndexListener(returnOrderDocumentFacade);
        listener.setUpdateReturnOrderProducer(updateReturnOrderProducer);
    }

    @Test
    public void handleEvent_NullObject() {
        listener.handleReturnOrderEvent(event);

        verify(returnOrderDocumentFacade, times(0)).index(returnOrder);
        verify(updateReturnOrderProducer, times(0)).process(returnOrder);
    }

    @Test
    public void handleEvent_UPDATE_ORDER_EXCHANGE_EVENT() {
        event = new ReturnOrderEvent(returnOrder, ReturnOrderEventType.UPDATE_EXCHANGE_ORDER);
        listener.handleReturnOrderEvent(event);

        verify(returnOrderDocumentFacade, times(1)).index(returnOrder);
        verify(updateReturnOrderProducer, times(1)).process(returnOrder);
    }

    @Test
    public void handleEvent_CREATE_EVENT() {
        event = new ReturnOrderEvent(returnOrder, ReturnOrderEventType.CREATE);
        listener.handleReturnOrderEvent(event);

        verify(returnOrderDocumentFacade, times(1)).index(returnOrder);
        verify(updateReturnOrderProducer, times(1)).process(returnOrder);
    }
}
