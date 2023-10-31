package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.data.CustomerDto;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.CustomerKafkaInStream;
import com.vctek.orderservice.facade.OrderElasticSearchFacade;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerListener implements KafkaListener<CustomerDto> {

    private OrderElasticSearchFacade orderElasticSearchFacade;

    public CustomerListener(OrderElasticSearchFacade orderElasticSearchFacade) {
        this.orderElasticSearchFacade = orderElasticSearchFacade;
    }

    @Override
    @StreamListener(CustomerKafkaInStream.CUSTOMER_TOPIC_IN)
    public void handleMessage(KafkaMessage<CustomerDto> kafkaMessage) {
        CustomerDto customerDto = kafkaMessage.getContent();
        if(!customerDto.isChangedName()) {
            return;
        }

        orderElasticSearchFacade.updateCustomerName(customerDto);
    }
}
