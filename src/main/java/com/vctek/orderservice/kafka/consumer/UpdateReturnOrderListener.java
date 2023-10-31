package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.data.ReturnOrderBillDTO;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.UpdateReturnOrderInStream;
import com.vctek.orderservice.kafka.producer.ReturnOrdersProducerService;
import com.vctek.orderservice.kafka.producer.UpdateReturnOrderProducer;
import com.vctek.orderservice.service.ReturnOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
public class UpdateReturnOrderListener implements KafkaListener<ReturnOrderBillDTO> {
    private UpdateReturnOrderProducer updateReturnOrderProducer;
    private ReturnOrdersProducerService returnOrdersProducerService;
    private ReturnOrderService returnOrderService;

    public UpdateReturnOrderListener(UpdateReturnOrderProducer updateReturnOrderProducer) {
        this.updateReturnOrderProducer = updateReturnOrderProducer;
    }

    @Override
    @StreamListener(UpdateReturnOrderInStream.UPDATE_RETURN_ORDER_TOPIC_IN)
    public void handleMessage(KafkaMessage<ReturnOrderBillDTO> kafkaMessage) {
        if(KafkaMessageType.REQUEST_UPDATE_RETURN_ORDER.equals(kafkaMessage.getType())) {
            return;
        }

        ReturnOrderBillDTO returnOrderBillDTO = kafkaMessage.getContent();
        if(KafkaMessageType.UPDATE_RETURN_ORDER_INFO.equals(kafkaMessage.getType())) {
            updateReturnOrderProducer.processReturnOrderBill(returnOrderBillDTO);
            return;
        }

        updateReturnOrderProducer.processReturnOrderBill(returnOrderBillDTO);
        returnOrdersProducerService.produceReturnOrderMessage(returnOrderBillDTO);
        returnOrderService.updateReturnOrder(kafkaMessage);
    }

    @Autowired
    public void setReturnOrdersProducerService(ReturnOrdersProducerService returnOrdersProducerService) {
        this.returnOrdersProducerService = returnOrdersProducerService;
    }

    @Autowired
    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }
}
