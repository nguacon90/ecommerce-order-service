package com.vctek.orderservice.kafka.producer;

import com.vctek.kafka.data.ReturnOrderBillDTO;
import com.vctek.orderservice.model.ReturnOrderModel;

public interface UpdateReturnOrderProducer {
    void process(ReturnOrderModel returnOrder);

    void processReturnOrderBill(final ReturnOrderBillDTO returnOrderBillDTO);

    void sendRequestUpdateReturnOrder(ReturnOrderModel model);
}
