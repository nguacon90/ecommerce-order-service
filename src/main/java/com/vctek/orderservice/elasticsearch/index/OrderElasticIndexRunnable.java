package com.vctek.orderservice.elasticsearch.index;

import com.vctek.converter.Converter;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.service.OrderElasticSearchService;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.OrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

public class OrderElasticIndexRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderElasticIndexRunnable.class);
    private Authentication authentication;
    private OrderService orderService;
    private int indexOfThread;
    private int pageSize;
    private int numOfThread;
    private Converter<OrderModel, OrderSearchModel> orderSearchModelConverter;
    private OrderElasticSearchService orderElasticSearchService;

    public OrderElasticIndexRunnable(Authentication authentication, int indexOfThread, int pageSize, int numOfThread) {
        this.authentication = authentication;
        this.indexOfThread = indexOfThread;
        this.pageSize = pageSize;
        this.numOfThread = numOfThread;
    }

    @Override
    public void run() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        int index = this.indexOfThread;
        Pageable pageable = PageRequest.of(index, this.pageSize);
        while (true) {
            Page<OrderModel> data = this.findOrderModel(pageable);
            List<OrderSearchModel> orderSearchModels = index(data);
            if (CollectionUtils.isEmpty(orderSearchModels)) {
                LOGGER.info("Index done! totalItems: {}", data.getTotalElements());
                break;
            }

            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Indexed: success {} items / {} pages/ {} totalItems", orderSearchModels.size(), index, data.getTotalElements());
            }

            index += this.numOfThread;
            pageable = PageRequest.of(index, this.pageSize);
        }
    }

    protected Page<OrderModel> findOrderModel(Pageable pageable) {
        return this.orderService.findAll(pageable);
    }

    protected List<OrderSearchModel> index(Page<OrderModel> data) {
        List<OrderModel> orderModels = data.getContent();
        if(CollectionUtils.isEmpty(orderModels)) {
            return new ArrayList<>();
        }

        List<OrderSearchModel> orderSearchModels = new ArrayList<>();
        for(OrderModel order : orderModels) {
            try {
                OrderSearchModel convert = convertOrderSearchModel(order);
                orderSearchModels.add(convert);
            } catch (RuntimeException e) {
                LOGGER.error("Convert error: OrderId: {}, message: {}", order.getId(), e.getMessage(), e);
            }
        }

        orderElasticSearchService.bulkOrderIndex(orderSearchModels);
        return orderSearchModels;
    }

    protected OrderSearchModel convertOrderSearchModel(OrderModel order) {
        return orderSearchModelConverter.convert(order);
    }

    public void setOrderSearchModelConverter(Converter<OrderModel, OrderSearchModel> orderSearchModelConverter) {
        this.orderSearchModelConverter = orderSearchModelConverter;
    }

    public void setOrderElasticSearchService(OrderElasticSearchService orderElasticSearchService) {
        this.orderElasticSearchService = orderElasticSearchService;
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
