package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.producer.ProductInfoKafkaData;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.ProductInfoInStream;
import com.vctek.orderservice.facade.OrderElasticSearchFacade;
import com.vctek.orderservice.facade.ReturnOrderDocumentFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class ProductInfoListener implements KafkaListener<ProductInfoKafkaData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductInfoListener.class);
    private OrderElasticSearchFacade orderElasticSearchFacade;
    private ReturnOrderDocumentFacade returnOrderDocumentFacade;

    @Override
    @StreamListener(ProductInfoInStream.PRODUCT_INFO_TOPIC_IN)
    public void handleMessage(@Payload KafkaMessage<ProductInfoKafkaData> kafkaMessage) {
        ProductInfoKafkaData productInfoKafkaData = kafkaMessage.getContent();
        if(productInfoKafkaData.isChangeSkuOrName()) {
            try {
                orderElasticSearchFacade.updateSkuOrName(productInfoKafkaData);
                returnOrderDocumentFacade.updateSkuOrName(productInfoKafkaData);
            } catch (RuntimeException e) {
                LOGGER.error(e.getMessage(), e);
                LOGGER.error("ERROR UPDATE SKU OR NAME: PRODUCT_ID: {}", productInfoKafkaData.getId());
            }
        }
    }

    @Autowired
    public void setOrderElasticSearchFacade(OrderElasticSearchFacade orderElasticSearchFacade) {
        this.orderElasticSearchFacade = orderElasticSearchFacade;
    }

    @Autowired
    public void setReturnOrderDocumentFacade(ReturnOrderDocumentFacade returnOrderDocumentFacade) {
        this.returnOrderDocumentFacade = returnOrderDocumentFacade;
    }
}
