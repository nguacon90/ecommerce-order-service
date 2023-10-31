package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.data.InvoiceKafkaData;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.InvoiceKafkaInStream;
import com.vctek.orderservice.facade.OrderFacade;
import com.vctek.orderservice.facade.ReturnOrderDocumentFacade;
import com.vctek.util.ReferType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OrderInvoiceListener implements KafkaListener<InvoiceKafkaData> {

    private OrderFacade orderFacade;
    private ReturnOrderDocumentFacade returnOrderFacade;

    @Override
    @StreamListener(InvoiceKafkaInStream.INVOICE_IN)
    public void handleMessage(@Payload KafkaMessage<InvoiceKafkaData> kafkaMessage) {
        InvoiceKafkaData invoiceKafkaData = kafkaMessage.getContent();
        if (ReferType.ORDER.code().equals(invoiceKafkaData.getReferType())) {
            orderFacade.updatePaymentTransactionDataAndPaidAmount(invoiceKafkaData);
        }
        if (ReferType.RETURN_ORDER.code().equals(invoiceKafkaData.getReferType())) {
            returnOrderFacade.updatePaymentData(invoiceKafkaData);
        }
    }

    @Autowired
    public void setOrderFacade(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @Autowired
    public void setReturnOrderFacade(ReturnOrderDocumentFacade returnOrderFacade) {
        this.returnOrderFacade = returnOrderFacade;
    }
}
