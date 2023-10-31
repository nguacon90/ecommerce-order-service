package com.vctek.orderservice.facade.impl;

import com.vctek.exception.ServiceException;
import com.vctek.migration.dto.MigrateOrderHistoryDto;
import com.vctek.orderservice.dto.EmployeeChangeData;
import com.vctek.orderservice.dto.OrderHistoryData;
import com.vctek.orderservice.dto.UserData;
import com.vctek.orderservice.dto.UserHistoryData;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderHistoryFacade;
import com.vctek.orderservice.facade.SyncOrderFacade;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.repository.dao.OrderHistoryDAO;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.OrderHistoryService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.util.CommonUtils;
import com.vctek.orderservice.util.DateUtil;
import com.vctek.util.OrderStatus;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrderHistoryFacadeImpl implements OrderHistoryFacade {
    private static final int DUMMY_MINUTE_SHIPPING_BEFORE_COMPLETED = -3;
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncOrderFacade.class);
    private OrderHistoryService orderHistoryService;
    private OrderHistoryDAO orderHistoryDAO;
    private OrderService orderService;
    private AuthService authService;

    public OrderHistoryFacadeImpl(OrderHistoryService orderHistoryService, OrderService orderService, AuthService authService) {
        this.orderHistoryService = orderHistoryService;
        this.orderService = orderService;
        this.authService = authService;
    }


    @Override
    public EmployeeChangeData getStatusHistory(String orderCode, Long companyId) {
        OrderModel orderModel = orderService.findByCodeAndCompanyId(orderCode, companyId);
        if (orderCode == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        List<OrderHistoryModel> historyModelList = orderHistoryService.findAllByOrder(orderModel);
        if (CollectionUtils.isEmpty(historyModelList)) {
            return new EmployeeChangeData();
        }

        Set<UserHistoryData> userDataList = new HashSet<>();
        List<OrderHistoryData> list = new ArrayList<>();
        for (OrderHistoryModel orderHistoryModel : historyModelList) {
            String date = DateUtil.getDateStr(orderHistoryModel.getModifiedTime(), DateUtil.ISO_DATE_PATTERN);
            OrderHistoryData historyData = new OrderHistoryData();
            historyData.setId(orderHistoryModel.getId());
            historyData.setPreviousStatus(orderHistoryModel.getPreviousStatus());
            historyData.setCurrentStatus(orderHistoryModel.getCurrentStatus());
            historyData.setExtraData(orderHistoryModel.getExtraData());
            historyData.setType(orderHistoryModel.getType());
            UserData userData = authService.getUserById(orderHistoryModel.getModifiedBy());
            if (userData != null) {
                UserHistoryData userHistoryData = new UserHistoryData();
                userHistoryData.setId(userData.getId());
                userHistoryData.setModifiedTime(date);
                userHistoryData.setName(userData.getName());
                if (!userDataList.contains(userHistoryData)) {
                    userDataList.add(userHistoryData);
                }
                historyData.setUserName(userData.getName());
            }
            historyData.setUserId(orderHistoryModel.getModifiedBy());

            historyData.setFullDate(DateUtil.getDateStr(orderHistoryModel.getModifiedTime(), DateUtil.ISO_DATE_TIME_PATTERN));
            historyData.setDate(date);
            list.add(historyData);
        }
        Map<String, List<OrderHistoryData>> data = new TreeMap<>(Collections.reverseOrder());
        data.putAll(list.stream().collect(
                Collectors.groupingBy(OrderHistoryData::getDate, Collectors.toList())
        ));
        EmployeeChangeData employeeChangeData = new EmployeeChangeData();
        employeeChangeData.setOrderHistory(data);
        Map<String, Set<UserHistoryData>> userHistory = new TreeMap<>(Collections.reverseOrder());
        userHistory.putAll(userDataList.stream().collect(
                Collectors.groupingBy(UserHistoryData::getModifiedTime, Collectors.toSet())
        ));
        employeeChangeData.setListUserData(userHistory);
        return employeeChangeData;
    }

    @Override
    @Transactional
    public void migrateOrderHistory(MigrateOrderHistoryDto migrateOrderHistoryDto) {
        String orderCode = migrateOrderHistoryDto.getOrderCode();
        try {
            OrderModel orderModel = orderService.findByCodeAndCompanyId(orderCode, migrateOrderHistoryDto.getCompanyId());
            if (orderModel == null) {
                LOGGER.error("Migrate order history not found order code: {}", orderCode);
                return;
            }

            OrderHistoryModel orderHistoryLatest = findOrderHistoryLatestByOrder(orderModel);
            OrderHistoryModel orderHistoryModel = new OrderHistoryModel();
            String latestCurrentStatus = null;
            if (orderHistoryLatest != null) {
                latestCurrentStatus = orderHistoryLatest.getCurrentStatus();
                orderHistoryModel.setPreviousStatus(latestCurrentStatus);
            }

            Date modifiedTime = DateUtil.parseDate(migrateOrderHistoryDto.getModifiedTime(), DateUtil.ISO_DATE_TIME_PATTERN);

            String currentStatus = migrateOrderHistoryDto.getCurrentStatus();

            String orderStatus = orderModel.getOrderStatus();
            if(isIgnoreEvent(currentStatus, orderStatus, latestCurrentStatus)) {
                return;
            }

            if(shouldCreateDummyChangeShippingEvent(orderModel, latestCurrentStatus, currentStatus)) {
                OrderHistoryModel shippingOrderHistoryModel = new OrderHistoryModel();
                shippingOrderHistoryModel.setOrder(orderModel);
                shippingOrderHistoryModel.setCurrentStatus(OrderStatus.SHIPPING.code());
                shippingOrderHistoryModel.setPreviousStatus(latestCurrentStatus);
                shippingOrderHistoryModel.setModifiedBy(migrateOrderHistoryDto.getModifiedBy());
                shippingOrderHistoryModel.setModifiedTime(modifiedTime);
                orderHistoryService.save(shippingOrderHistoryModel);
                MigrateOrderHistoryDto shippingDto = new MigrateOrderHistoryDto();
                Date shippingModifiedTime = CommonUtils.add(modifiedTime, Calendar.MINUTE, DUMMY_MINUTE_SHIPPING_BEFORE_COMPLETED);
                shippingDto.setModifiedTime(DateUtil.getDateStr(shippingModifiedTime, DateUtil.ISO_DATE_TIME_PATTERN));
                orderHistoryDAO.updateAuditing(shippingOrderHistoryModel, shippingDto);

                orderHistoryModel.setPreviousStatus(OrderStatus.SHIPPING.code());
                orderHistoryModel.setCurrentStatus(currentStatus);
                orderHistoryModel.setModifiedBy(migrateOrderHistoryDto.getModifiedBy());
                orderHistoryModel.setModifiedTime(modifiedTime);
                orderHistoryModel.setOrder(orderModel);
                orderHistoryService.save(orderHistoryModel);
                orderHistoryDAO.updateAuditing(orderHistoryModel, migrateOrderHistoryDto);
            } else {

                orderHistoryModel.setCurrentStatus(currentStatus);
                orderHistoryModel.setModifiedBy(migrateOrderHistoryDto.getModifiedBy());
                orderHistoryModel.setModifiedTime(modifiedTime);
                orderHistoryModel.setOrder(orderModel);
                orderHistoryService.save(orderHistoryModel);
                orderHistoryDAO.updateAuditing(orderHistoryModel, migrateOrderHistoryDto);
            }
        } catch (Exception e) {
            LOGGER.error("=======  Migrate order history: fail: " + orderCode + ", error: ", e);
        }
    }

    private boolean shouldCreateDummyChangeShippingEvent(OrderModel orderModel, String latestCurrentStatus, String currentStatus) {
        if(OrderStatus.COMPLETED.code().equals(currentStatus) &&
                (latestCurrentStatus == null || !OrderStatus.SHIPPING.code().equals(latestCurrentStatus))) {
           return !orderHistoryService.hasChangeShippingToOtherStatus(orderModel);
        }

        return false;
    }

    private boolean isIgnoreEvent(String currentStatus, String orderStatus, String latestCurrentStatus) {
        if(OrderStatus.COMPLETED.code().equals(currentStatus) && OrderStatus.COMPLETED.code().equals(latestCurrentStatus)) {
            return true;
        }

        return (OrderStatus.SYSTEM_CANCEL.code().equals(orderStatus) ||
            OrderStatus.CUSTOMER_CANCEL.code().equals(orderStatus)) && OrderStatus.COMPLETED.code().equals(currentStatus);
    }

    private OrderHistoryModel findOrderHistoryLatestByOrder(OrderModel orderModel) {
        List<OrderHistoryModel> orderHistoryModels = orderHistoryService.findAllByOrder(orderModel);
        if (CollectionUtils.isNotEmpty(orderHistoryModels)) {
            return orderHistoryModels.get(0);
        }
        return null;
    }

    @Autowired
    public void setOrderHistoryDAO(OrderHistoryDAO orderHistoryDAO) {
        this.orderHistoryDAO = orderHistoryDAO;
    }
}
