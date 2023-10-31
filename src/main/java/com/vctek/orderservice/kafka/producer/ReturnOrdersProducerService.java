package com.vctek.orderservice.kafka.producer;

import com.vctek.kafka.data.ReturnOrderBillDTO;
import com.vctek.orderservice.model.ReturnOrderModel;

public interface ReturnOrdersProducerService {
    void sendReturnOrdersKafka(ReturnOrderModel returnOrder);

    void produceReturnOrderMessage(final ReturnOrderBillDTO returnOrderBillDTO);
}
