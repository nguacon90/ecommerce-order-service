package com.vctek.orderservice.elasticsearch.index;

import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.orderservice.dto.request.OrderReportRequest;
import com.vctek.orderservice.kafka.producer.OrderProducerService;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.util.OrderStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;

import java.util.List;

public class OrderReportRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderReportRunnable.class);
    private Authentication authentication;
    private int indexOfThread;
    private int pageSize;
    private int numOfThread;
    private OrderReportRequest orderReportRequest;
    private OrderProducerService producerService;
    private OrderService orderService;
    private KafkaMessageType kafkaMessageType;

    public OrderReportRunnable(Authentication authentication, int indexOfThread, int pageSize, int numOfThread) {
        this.authentication = authentication;
        this.indexOfThread = indexOfThread;
        this.pageSize = pageSize;
        this.numOfThread = numOfThread;
    }

    @Override
    public void run() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        int index = this.indexOfThread;
        Pageable pageable = PageRequest.of(index, this.pageSize, new Sort(Sort.Direction.ASC, "id"));
        while (true) {
            Page<OrderModel> page = getModels(pageable);
            List<OrderModel> orderModels = page.getContent();
            if (CollectionUtils.isEmpty(orderModels)) {
                LOGGER.info("Run order report done!: {} totalItems", page.getTotalElements());
                break;
            }

            for (OrderModel orderModel : orderModels) {
                try {
                    producerService.recalculateOrderReport(orderModel, kafkaMessageType, null);
                } catch (RuntimeException e) {
                    LOGGER.error("Convert error: orderCode: {}, message: {}", orderModel.getCode(), e.getMessage(), e);
                }
            }

            index += this.numOfThread;
            pageable = PageRequest.of(index, this.pageSize);
        }
    }

    private Page<OrderModel> getModels(Pageable pageable) {
        if(StringUtils.isNotBlank(orderReportRequest.getOrderType())) {
            return orderService.findAllByCompanyIdAndTypeAndFromDate(pageable, orderReportRequest.getCompanyId(), orderReportRequest.getOrderType(),
                    orderReportRequest.getFromDate());
        }
        if(KafkaMessageType.ORDER_TAG.toString().equals(orderReportRequest.getKafkaMessageType())) {
            return orderService.findAllByCompanyIdAndOrderStatus(pageable, orderReportRequest.getCompanyId(), OrderStatus.PRE_ORDER);
        }
        return orderService.findAllByAndCompanyIdFromDate(orderReportRequest.getCompanyId(), orderReportRequest.getFromDate(), pageable);
    }

    public void setOrderReportRequest(OrderReportRequest orderReportRequest) {
        this.orderReportRequest = orderReportRequest;
    }

    public void setProducerService(OrderProducerService producerService) {
        this.producerService = producerService;
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    public void setKafkaMessageType(KafkaMessageType kafkaMessageType) {
        this.kafkaMessageType = kafkaMessageType;
    }
}
