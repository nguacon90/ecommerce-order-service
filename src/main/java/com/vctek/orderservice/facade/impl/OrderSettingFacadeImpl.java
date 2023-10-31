package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderSettingData;
import com.vctek.orderservice.dto.request.OrderSettingRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderSettingFacade;
import com.vctek.orderservice.model.OrderSettingModel;
import com.vctek.orderservice.service.ComboPriceSettingService;
import com.vctek.orderservice.service.OrderSettingService;
import com.vctek.orderservice.util.OrderSettingType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cloud.cluster.lock.support.DelegatingDistributedLock;
import org.springframework.cloud.cluster.redis.lock.RedisLockService;
import org.springframework.stereotype.Component;

@Component
public class OrderSettingFacadeImpl implements OrderSettingFacade {
    public static final int MAXIMUM_NOTE_LENGTH = 100;
    private ComboPriceSettingService comboPriceSettingService;
    private Converter<OrderSettingModel, OrderSettingData> orderSettingDataConverter;
    private RedisLockService redisLockService;
    private OrderSettingService orderSettingService;

    @Override
    public OrderSettingData createOrUpdateComboPriceSetting(OrderSettingRequest orderSettingRequest) {
        String lockKey = "SETUP_COMBO_PRICE";
        DelegatingDistributedLock lock = (DelegatingDistributedLock) redisLockService.obtain(lockKey);
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            ErrorCodes err = ErrorCodes.REJECT_REDUNDANT_REQUEST;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        try {
            OrderSettingModel model = comboPriceSettingService.save(orderSettingRequest);
            return orderSettingDataConverter.convert(model);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public OrderSettingData getComboPriceSetting(Long companyId) {
        OrderSettingModel model = comboPriceSettingService.findByTypeAndCompanyId(OrderSettingType.COMBO_PRICE_SETTING.code(), companyId);
        if(model != null) {
            return orderSettingDataConverter.convert(model);
        }

        return null;
    }

    @Override
    public OrderSettingData getOrderMaximumDiscount(Long companyId) {
        OrderSettingModel model = orderSettingService.findByTypeAndCompanyId(OrderSettingType.MAXIMUM_DISCOUNT_SETTING.code(), companyId);
        if(model != null) {
            return orderSettingDataConverter.convert(model);
        }

        return null;
    }

    @Override
    @CacheEvict(value = "create_notification_change_order_status", key = "#request.companyId")
    public OrderSettingData createOrUpdateSettingNotificationChangeStatus(OrderSettingRequest request) {
        String lockKey = "SETUP_NOTIFICATION_CHANGE_ORDER_STATUS";
        DelegatingDistributedLock lock = (DelegatingDistributedLock) redisLockService.obtain(lockKey);
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            ErrorCodes err = ErrorCodes.REJECT_REDUNDANT_REQUEST;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        try {
            if (StringUtils.isNotBlank(request.getNote()) && request.getNote().length() > MAXIMUM_NOTE_LENGTH) {
                ErrorCodes err = ErrorCodes.OVER_MAX_LENGTH_100;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            OrderSettingModel model = orderSettingService.findByTypeAndCompanyId(OrderSettingType.CREATE_NOTIFICATION_CHANGE_ORDER_STATUS.code(), request.getCompanyId());
            if (model == null) {
                model = new OrderSettingModel();
                model.setCompanyId(request.getCompanyId());
                model.setType(OrderSettingType.CREATE_NOTIFICATION_CHANGE_ORDER_STATUS.code());
            }
            model.setOrderStatus(request.getOrderStatus());
            model.setNote(request.getNote());
            OrderSettingModel savedModel = orderSettingService.save(model);
            return orderSettingDataConverter.convert(savedModel);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public OrderSettingData getSettingNotificationChangeStatus(Long companyId) {
        OrderSettingModel model = orderSettingService.findByTypeAndCompanyId(OrderSettingType.CREATE_NOTIFICATION_CHANGE_ORDER_STATUS.code(), companyId);
        if (model != null) {
            return orderSettingDataConverter.convert(model);
        }
        return new OrderSettingData();
    }

    @Autowired
    public void setComboPriceSettingService(ComboPriceSettingService comboPriceSettingService) {
        this.comboPriceSettingService = comboPriceSettingService;
    }

    @Autowired
    public void setOrderSettingDataConverter(Converter<OrderSettingModel, OrderSettingData> orderSettingDataConverter) {
        this.orderSettingDataConverter = orderSettingDataConverter;
    }

    @Autowired
    public void setRedisLockService(RedisLockService redisLockService) {
        this.redisLockService = redisLockService;
    }

    @Autowired
    public void setOrderSettingService(OrderSettingService orderSettingService) {
        this.orderSettingService = orderSettingService;
    }
}

