package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.data.ReturnOrdersDTO;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.ReturnOrdersKafkaInStream;
import com.vctek.orderservice.facade.OrderElasticSearchFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class ReturnOrderListener implements KafkaListener<ReturnOrdersDTO> {

    private OrderElasticSearchFacade orderElasticSearchFacade;

    @Override
    @StreamListener(ReturnOrdersKafkaInStream.RETURN_ORDERS_IN)
    public void handleMessage(@Payload KafkaMessage<ReturnOrdersDTO> kafkaMessage) {
        ReturnOrdersDTO returnOrdersDTO = kafkaMessage.getContent();
        orderElasticSearchFacade.indexReturnOrderIds(returnOrdersDTO);
    }

    @Autowired
    public void setOrderElasticSearchFacade(OrderElasticSearchFacade orderElasticSearchFacade) {
        this.orderElasticSearchFacade = orderElasticSearchFacade;
    }
}
