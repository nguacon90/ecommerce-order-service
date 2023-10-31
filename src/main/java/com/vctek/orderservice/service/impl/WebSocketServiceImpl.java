package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderSettingModel;
import com.vctek.orderservice.service.OrderSettingService;
import com.vctek.orderservice.service.WebSocketService;
import com.vctek.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketServiceImpl implements WebSocketService {
    private OrderSettingService orderSettingService;
    private SimpMessagingTemplate simpMessagingTemplate;
    private UserService userService;

    @Override
    public void sendNotificationChangeOrderStatus(OrderModel orderModel) {
        OrderSettingModel orderSettingModel = orderSettingService.findCreateNotificationChangeStatus(orderModel.getCompanyId());
        if (orderSettingModel == null || !orderModel.getOrderStatus().equals(orderSettingModel.getOrderStatus())) {
            return;
        }
        String textMessage = "Đơn hàng của quý khác đã được giao thành công!";
        if (StringUtils.isNotBlank(orderSettingModel.getNote())) {
            textMessage = orderSettingModel.getNote();
        }
        simpMessagingTemplate.convertAndSendToUser(userService.getCurrentUserId().toString(), "/queue/notification", textMessage);
    }

    @Autowired
    public void setOrderSettingService(OrderSettingService orderSettingService) {
        this.orderSettingService = orderSettingService;
    }

    @Autowired
    public void setSimpMessagingTemplate(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
