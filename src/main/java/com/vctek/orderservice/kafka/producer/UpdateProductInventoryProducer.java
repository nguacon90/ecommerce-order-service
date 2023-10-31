package com.vctek.orderservice.kafka.producer;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.OrderModel;

import java.util.List;

public interface UpdateProductInventoryProducer {

    void sendUpdateStockEntries(OrderModel orderModel, List<AbstractOrderEntryModel> entryModels);
}
