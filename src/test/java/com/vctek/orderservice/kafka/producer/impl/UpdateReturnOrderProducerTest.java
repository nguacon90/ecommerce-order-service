package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.kafka.data.OrderData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.OrderKafkaOutStream;
import com.vctek.orderservice.feignclient.dto.BillDetailData;
import com.vctek.orderservice.feignclient.dto.ReturnOrderBillData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.BillService;
import com.vctek.orderservice.service.ReturnOrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class UpdateReturnOrderProducerTest {
    private UpdateReturnOrderProducerImpl producer;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private OrderKafkaOutStream orderKafkaOutStream;
    @Mock
    private BillService billService;
    @Mock
    private ReturnOrderService returnOrderService;
    @Mock
    private ReturnOrderModel returnOrderMock;
    @Mock
    private ReturnOrderBillData returnOrderDataMock;
    @Mock
    private BillDetailData entryMock;

    private ArgumentCaptor<KafkaMessage<OrderData>> captor = ArgumentCaptor.forClass(KafkaMessage.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        producer = new UpdateReturnOrderProducerImpl(returnOrderService);
        producer.setBillService(billService);
        producer.setKafkaProducerService(kafkaProducerService);
        producer.setOrderKafkaOutStream(orderKafkaOutStream);
        when(returnOrderService.getOriginOrderOf(returnOrderMock)).thenReturn(new OrderModel());
    }

    @Test
    public void process_nullShouldIgnore() {
        producer.process(null);
        verify(billService, times(0)).getReturnOrderBill(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void process() {
        when(returnOrderMock.getId()).thenReturn(1l);
        when(returnOrderMock.getCompanyId()).thenReturn(1l);
        when(returnOrderMock.getBillId()).thenReturn(1l);
        when(billService.getReturnOrderBill(anyLong(), anyLong(), anyLong())).thenReturn(returnOrderDataMock);
        when(returnOrderDataMock.getWarehouseId()).thenReturn(11l);
        when(returnOrderDataMock.getFinalPrice()).thenReturn(200000d);
        when(returnOrderDataMock.getTotalPrice()).thenReturn(200000d);
        when(returnOrderDataMock.getEntries()).thenReturn(Arrays.asList(entryMock));
        when(entryMock.getQuantity()).thenReturn(20);
        when(entryMock.getProductId()).thenReturn(11112l);
        when(entryMock.getPrice()).thenReturn(10000d);
        when(entryMock.getTotalPrice()).thenReturn(200000d);
        when(entryMock.getFinalPrice()).thenReturn(200000d);
        OrderModel originOrderModel = new OrderModel();
        originOrderModel.setCode("code");
        when(returnOrderMock.getOriginOrder()).thenReturn(originOrderModel);

        producer.process(returnOrderMock);
        verify(kafkaProducerService).send(captor.capture(), any());
        KafkaMessage<OrderData> actualMessage = captor.getValue();
        OrderData orderData = actualMessage.getContent();
        assertEquals(200000d, orderData.getTotalPrice(), 0);
        assertEquals(200000d, orderData.getFinalPrice(), 0);
        assertEquals(20, orderData.getTotalQuantity(), 0);
        assertEquals(1, orderData.getTotalProduct(), 0);
        assertEquals(1, orderData.getEntryDataList().size());
        assertEquals(10000d, orderData.getEntryDataList().get(0).getPrice(), 0);
        assertEquals(200000d, orderData.getEntryDataList().get(0).getOrderEntryTotalPrice(), 0);
        assertEquals(11112l, orderData.getEntryDataList().get(0).getProductId(), 0);
    }
}
