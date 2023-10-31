package com.vctek.orderservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.kafka.data.OrderData;
import com.vctek.orderservice.dto.TrackingOrderData;
import com.vctek.orderservice.model.TrackingUpdateOrderModel;
import com.vctek.orderservice.repository.TrackingUpdateOrderRepository;
import com.vctek.orderservice.service.TrackingUpdateOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrackingUpdateOrderServiceImpl implements TrackingUpdateOrderService {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackingUpdateOrderServiceImpl.class);
    private TrackingUpdateOrderRepository repository;
    private ObjectMapper objectMapper;

    public TrackingUpdateOrderServiceImpl(TrackingUpdateOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public TrackingUpdateOrderModel createNew(OrderData orderData, TrackingOrderData trackingOrderData) {
        TrackingUpdateOrderModel model = new TrackingUpdateOrderModel();
        model.setOrderCode(orderData.getOrderCode());
        populateContent(model, trackingOrderData);
        return repository.save(model);
    }

    @Override
    public TrackingUpdateOrderModel updateModel(OrderData orderData, TrackingOrderData trackingOrderData) {
        TrackingUpdateOrderModel model = findByOrderCode(orderData.getOrderCode());
        if (model == null) {
            LOGGER.debug("CANNOT FIND MODEL BY ORDER_CODE: {}", orderData.getOrderCode());
            return null;
        }
        populateContent(model, trackingOrderData);
        return repository.save(model);
    }

    private void populateContent(TrackingUpdateOrderModel model, TrackingOrderData trackingOrderData) {
        try {
            model.setContent(objectMapper.writeValueAsString(trackingOrderData));
        } catch (JsonProcessingException e) {
            LOGGER.debug("CANNOT WRITE CONTENT JSON");
        }
    }

    @Override
    public TrackingUpdateOrderModel findByOrderCode(String orderCode) {
        return repository.findDistinctTopByOrderCode(orderCode);
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
