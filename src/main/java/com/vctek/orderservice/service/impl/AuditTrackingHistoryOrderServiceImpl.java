package com.vctek.orderservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.kafka.data.OrderData;
import com.vctek.orderservice.dto.TrackingHistoryOrderData;
import com.vctek.orderservice.dto.TrackingOrderData;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.TrackingUpdateOrderModel;
import com.vctek.orderservice.service.AuditTrackingHistoryOrderService;
import com.vctek.orderservice.service.OrderHistoryService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.TrackingUpdateOrderService;
import com.vctek.orderservice.util.HistoryOrderType;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditTrackingHistoryOrderServiceImpl implements AuditTrackingHistoryOrderService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AuditTrackingHistoryOrderServiceImpl.class);
    private TrackingUpdateOrderService trackingUpdateOrderService;
    private ObjectMapper objectMapper;
    private Populator<List<TrackingOrderData>, List<TrackingHistoryOrderData>> trackingUpdateOrderPopulator;
    private OrderHistoryService orderHistoryService;
    private OrderService orderService;
    private Converter<OrderData, TrackingOrderData> trackingOrderDataConverter;

    @Override
    @Transactional
    public void compareChangeFields(OrderData orderData) {
        if (!OrderType.ONLINE.toString().equals(orderData.getOrderType())) return;
        OrderStatus currentStatus = OrderStatus.findByCode(orderData.getOrderStatus());
        if (currentStatus.value() < OrderStatus.CONFIRMED.value()) return;
        TrackingUpdateOrderModel trackingUpdateOrderModel = trackingUpdateOrderService.findByOrderCode(orderData.getOrderCode());
        TrackingOrderData trackingOrderData = trackingOrderDataConverter.convert(orderData);
        if (trackingUpdateOrderModel == null) {
            LOGGER.debug("CANNOT FIND CREATE ORDER CODE: {}", orderData.getOrderCode());
            trackingUpdateOrderService.createNew(orderData, trackingOrderData);
            return;
        }
        TrackingOrderData oldData = getOrderModelBy(trackingUpdateOrderModel);
        if (oldData == null) return;
        compareOrderData(oldData, trackingOrderData, orderData.getCurrentUserId());
        trackingUpdateOrderService.updateModel(orderData, trackingOrderData);
    }

    private void compareOrderData(TrackingOrderData oldData, TrackingOrderData newData, Long currentUserId) {
        List<TrackingHistoryOrderData> historyOrderData = new ArrayList<>();
        List<TrackingOrderData> compare = new ArrayList<>();
        compare.add(oldData);
        compare.add(newData);
        trackingUpdateOrderPopulator.populate(compare, historyOrderData);
        if (CollectionUtils.isEmpty(historyOrderData)) return;
        OrderHistoryModel orderHistoryModel = new OrderHistoryModel();
        populateExtraData(orderHistoryModel, historyOrderData);
        OrderModel orderModel = orderService.findByCodeAndCompanyId(newData.getOrderCode(), newData.getCompanyId());
        orderHistoryModel.setCurrentStatus(orderModel.getOrderStatus());
        orderHistoryModel.setPreviousStatus(orderModel.getOrderStatus());
        orderHistoryModel.setOrder(orderModel);
        orderHistoryModel.setModifiedBy(currentUserId);
        orderHistoryModel.setType(HistoryOrderType.TRACKING_ORDER_UPDATE.toString());
        orderHistoryService.save(orderHistoryModel);
    }

    private void populateExtraData(OrderHistoryModel orderHistoryModel, List<TrackingHistoryOrderData> historyOrderData) {
        try {
            orderHistoryModel.setExtraData(objectMapper.writeValueAsString(historyOrderData));
        } catch (JsonProcessingException e) {
            LOGGER.debug("CANNOT WRITE OrderHistoryModel ExtraData Order Code: {}", orderHistoryModel.getOrder().getCode());
        }
    }

    private TrackingOrderData getOrderModelBy(TrackingUpdateOrderModel trackingUpdateOrderModel) {
        try {
            return objectMapper.readValue(trackingUpdateOrderModel.getContent(), TrackingOrderData.class);
        } catch (IOException e) {
            LOGGER.debug("CANNOT READ CONTENT TrackingUpdateOrderModel ID: {}", trackingUpdateOrderModel.getId());
        }
        return null;
    }

    @Autowired
    public void setTrackingUpdateOrderService(TrackingUpdateOrderService trackingUpdateOrderService) {
        this.trackingUpdateOrderService = trackingUpdateOrderService;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setOrderHistoryService(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    @Qualifier("trackingUpdateOrderPopulator")
    public void setTrackingUpdateOrderPopulator(Populator<List<TrackingOrderData>, List<TrackingHistoryOrderData>> trackingUpdateOrderPopulator) {
        this.trackingUpdateOrderPopulator = trackingUpdateOrderPopulator;
    }

    @Autowired
    public void setTrackingOrderDataConverter(Converter<OrderData, TrackingOrderData> trackingOrderDataConverter) {
        this.trackingOrderDataConverter = trackingOrderDataConverter;
    }
}
