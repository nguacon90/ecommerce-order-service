package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.converter.Populator;
import com.vctek.kafka.data.OrderData;
import com.vctek.kafka.data.ReturnOrderBillDTO;
import com.vctek.kafka.data.ReturnOrdersDTO;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.ReturnOrdersKafkaOutStream;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.BillService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.ReturnOrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ReturnOrderProducerTest {
    private ReturnOrdersProducerServiceImpl producer;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private ReturnOrdersKafkaOutStream returnOrdersKafkaOutStream;
    @Mock
    private BillService billService;
    @Mock
    private OrderService orderService;
    @Mock
    private ReturnOrderModel returnOrderMock;
    @Mock
    private ReturnOrderBillDTO returnOrderDataMock;
    @Mock
    private Populator<OrderModel, OrderData> orderDataPopulator;

    private ArgumentCaptor<KafkaMessage<ReturnOrdersDTO>> captor = ArgumentCaptor.forClass(KafkaMessage.class);
    @Mock
    private ReturnOrderService returnOrderService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        producer = new ReturnOrdersProducerServiceImpl(orderDataPopulator);
        producer.setBillService(billService);
        producer.setKafkaProducerService(kafkaProducerService);
        producer.setReturnOrdersKafkaOutStream(returnOrdersKafkaOutStream);
        producer.setReturnOrderService(returnOrderService);
        when(returnOrderDataMock.getId()).thenReturn(1l);
        when(returnOrderService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(new ReturnOrderModel());
    }

    @Test
    public void sendReturnOrdersKafka_nullShouldIgnore() {
        producer.sendReturnOrdersKafka(null);
        verify(billService, times(0)).getReturnOrderBill(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void sendReturnOrdersKafka() {
        when(returnOrderMock.getId()).thenReturn(1l);
        when(returnOrderMock.getCompanyId()).thenReturn(1l);
        when(returnOrderMock.getBillId()).thenReturn(1l);
        when(billService.getBillWithReturnOrder(anyLong(), anyLong(), anyLong())).thenReturn(returnOrderDataMock);
        OrderModel orderModel = new OrderModel();
        orderModel.setCode("code");
        orderModel.setFinalPrice(20d);
        when(returnOrderMock.getOriginOrder()).thenReturn(orderModel);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModel);
        when(returnOrderDataMock.getWarehouseId()).thenReturn(11l);
        when(returnOrderDataMock.getFinalPrice()).thenReturn(11d);

        producer.sendReturnOrdersKafka(returnOrderMock);
        verify(kafkaProducerService).send(captor.capture(), any());
        KafkaMessage<ReturnOrdersDTO> actualMessage = captor.getValue();
        ReturnOrdersDTO returnOrdersDTO = actualMessage.getContent();
        assertEquals(1l, returnOrderMock.getBillId(), 0);
        assertEquals(11l, returnOrdersDTO.getWarehouseId(), 0);

    }
}
