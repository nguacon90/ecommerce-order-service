package com.vctek.orderservice.elasticsearch.index;

import com.vctek.orderservice.dto.request.OrderReportRequest;
import com.vctek.orderservice.kafka.producer.OrderProducerService;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.service.OrderHistoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class OrderHistoryReportRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderHistoryReportRunnable.class);
    private Authentication authentication;
    private int indexOfThread;
    private int pageSize;
    private int numOfThread;
    private OrderReportRequest orderReportRequest;
    private OrderProducerService producerService;
    private OrderHistoryService orderHistoryService;

    public OrderHistoryReportRunnable(Authentication authentication, int indexOfThread, int pageSize, int numOfThread) {
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
            Page<OrderHistoryModel> page = findAllHistory(pageable);
            List<OrderHistoryModel> orderHistoryModels = page.getContent();
            if (CollectionUtils.isEmpty(orderHistoryModels)) {
                LOGGER.info("Run order history report done!: {} totalItems", page.getTotalElements());
                break;
            }

            for (OrderHistoryModel orderHistoryModel : orderHistoryModels) {
                try {
                    producerService.sendChangeStatusKafka(orderHistoryModel);
                } catch (RuntimeException e) {
                    LOGGER.error("Convert error: order history id: {}, message: {}", orderHistoryModel.getId(), e.getMessage(), e);
                }
            }

            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Send Change Order Status To Kafka: success {} items dim bill detail/ {} pages / {} total element",
                        index * this.pageSize + orderHistoryModels.size(), index, page.getTotalElements());
            }
            index += this.numOfThread;
            pageable = PageRequest.of(index, this.pageSize);
        }
    }

    private Page<OrderHistoryModel> findAllHistory(Pageable pageable) {
        if(orderReportRequest.getProductId() != null ) {
            return orderHistoryService.findAllByCompanyIdAndProductId(orderReportRequest.getCompanyId(), orderReportRequest.getProductId(),
                    pageable);
        }

        if(orderReportRequest.getFromDate() != null) {
            return orderHistoryService.findAllByAndCompanyIdAndFromDate(orderReportRequest.getCompanyId(), orderReportRequest.getFromDate(), pageable);
        }

        return orderHistoryService.findAllByAndCompanyId(orderReportRequest.getCompanyId(), pageable);
    }

    public void setOrderReportRequest(OrderReportRequest orderReportRequest) {
        this.orderReportRequest = orderReportRequest;
    }

    public void setProducerService(OrderProducerService producerService) {
        this.producerService = producerService;
    }

    public void setOrderHistoryService(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }
}
