package com.vctek.orderservice.service;

import com.vctek.kafka.data.OrderData;
import com.vctek.orderservice.dto.TrackingOrderData;
import com.vctek.orderservice.model.TrackingUpdateOrderModel;

public interface TrackingUpdateOrderService {
    TrackingUpdateOrderModel createNew(OrderData orderData, TrackingOrderData trackingOrderData);

    TrackingUpdateOrderModel updateModel(OrderData orderData, TrackingOrderData trackingOrderData);

    TrackingUpdateOrderModel findByOrderCode(String orderCode);
}
