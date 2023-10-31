package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.data.OrderData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.OrderKafkaInStream;
import com.vctek.orderservice.facade.OrderFacade;
import com.vctek.orderservice.facade.ReturnOrderDocumentFacade;
import com.vctek.orderservice.service.AuditTrackingHistoryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
public class OrderListener implements KafkaListener<OrderData>  {

    private ReturnOrderDocumentFacade returnOrderDocumentFacade;
    private OrderFacade orderFacade;
    private AuditTrackingHistoryOrderService auditTrackingHistoryOrderService;

    @Override
    @StreamListener(OrderKafkaInStream.ORDERS_TOPIC_IN)
    public void handleMessage(KafkaMessage<OrderData> kafkaMessage) {
        returnOrderDocumentFacade.indexOrderSource(kafkaMessage.getContent());
        orderFacade.updateOrderSourceForReturnOrder(kafkaMessage.getContent());
        auditTrackingHistoryOrderService.compareChangeFields(kafkaMessage.getContent());
    }

    @Autowired
    public void setReturnOrderDocumentFacade(ReturnOrderDocumentFacade returnOrderDocumentFacade) {
        this.returnOrderDocumentFacade = returnOrderDocumentFacade;
    }

    @Autowired
    public void setOrderFacade(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @Autowired
    public void setAuditTrackingHistoryOrderService(AuditTrackingHistoryOrderService auditTrackingHistoryOrderService) {
        this.auditTrackingHistoryOrderService = auditTrackingHistoryOrderService;
    }
}
