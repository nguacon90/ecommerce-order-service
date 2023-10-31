package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.kafka.data.order.OrderProcessData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.OrderProcessKafkaOutStream;
import com.vctek.orderservice.dto.OrderStatusImportDetailData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class OrderProcessProducerServiceTest {
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private OrderProcessKafkaOutStream outStream;
    private OrderProcessProducerServiceImpl service;
    private OrderProcessData data;
    private OrderStatusImportDetailData detailData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new OrderProcessProducerServiceImpl(kafkaProducerService, outStream);
        data =  new OrderProcessData();
        data.setImportOrderStatusDetailId(1L);
        data.setCompanyId(2L);
        detailData = new OrderStatusImportDetailData();
        detailData.setId(2L);
        detailData.setOrderCode("code");
    }

    @Test
    public void sendOrderStatusImportKafka() {
        service.sendOrderStatusImportKafka(data);
        verify(kafkaProducerService).send(any(KafkaMessage.class), any());
    }

}
