package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.repository.OrderHistoryRepository;
import com.vctek.orderservice.service.OrderHistoryService;
import com.vctek.util.OrderStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderHistoryServiceImpl implements OrderHistoryService {

    private OrderHistoryRepository orderHistoryRepository;

    public OrderHistoryServiceImpl(OrderHistoryRepository orderHistoryRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
    }

    @Override
    public List<OrderHistoryModel> findAllByOrderId(Long orderId) {
        return orderHistoryRepository.findAllByOrderId(orderId);
    }

    @Override
    public List<OrderHistoryModel> findAllByOrder(AbstractOrderModel orderModel) {
        return orderHistoryRepository.findAllByOrderOrderByModifiedTimeDesc(orderModel);
    }

    @Override
    public OrderHistoryModel save(OrderHistoryModel model) {
        OrderHistoryModel saved = orderHistoryRepository.save(model);
        return saved;
    }

    @Override
    public Optional<OrderHistoryModel> findFirstSuccessStatusOf(AbstractOrderModel orderModel) {
        return orderHistoryRepository.findFirstOldestBy(orderModel.getId(), OrderStatus.COMPLETED.code());
    }

    public Page<OrderHistoryModel> findAllByAndCompanyId(Long companyId, Pageable pageable) {
        return orderHistoryRepository.findAllByAndCompanyId(companyId, pageable);
    }

    @Override
    public Page<OrderHistoryModel> findAllByCompanyIdAndProductId(Long companyId, Long productId, Pageable pageable) {
        return orderHistoryRepository.findAllByAndCompanyIdAndProductId(companyId, productId, pageable);
    }

    @Override
    public boolean hasChangeShippingToOtherStatus(OrderModel orderModel) {
        List<OrderHistoryModel> orderHistory = orderModel.getOrderHistory();
        if(CollectionUtils.isEmpty(orderHistory)) {
            return false;
        }
        for(OrderHistoryModel model : orderHistory) {
            if(OrderStatus.SHIPPING.code().equals(model.getPreviousStatus()) &&
                    !OrderStatus.SHIPPING.code().equals(model.getCurrentStatus())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Page<OrderHistoryModel> findAllByAndCompanyIdAndFromDate(Long companyId, Date fromDate, Pageable pageable) {
        return orderHistoryRepository.findAllByAndCompanyIdAndFromDate(companyId, fromDate, pageable);
    }

    @Override
    public Date getLastCompletedDateOf(OrderModel orderModel) {
        Optional<OrderHistoryModel> optional = orderHistoryRepository.findLastHistoryByOrderIdAndStatus(orderModel.getId(), OrderStatus.COMPLETED.code());
        if(optional.isPresent()) {
            return optional.get().getModifiedTime();
        }

        return null;
    }

}
