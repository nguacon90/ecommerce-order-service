package com.vctek.orderservice.facade.impl;

import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.dto.request.OrderReportRequest;
import com.vctek.orderservice.facade.SyncReportFacade;
import com.vctek.orderservice.kafka.producer.OrderProducerService;
import com.vctek.orderservice.kafka.producer.PromotionProducer;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.service.CouponService;
import com.vctek.orderservice.service.OrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SyncReportFacadeImpl extends AbstractFacade implements SyncReportFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncReportFacadeImpl.class);
    private PromotionSourceRuleService promotionSourceRuleService;
    private CouponService couponService;
    private PromotionProducer promotionProducer;
    private OrderService orderService;
    private OrderProducerService producerService;

    @Override
    public void syncPromotion(Long companyId, String type) {
        ExecutorService executorService = Executors.newFixedThreadPool(1, new CustomizableThreadFactory("Sync_Promotion"));
        executorService.execute(() -> {
            if ("PROMOTION".equals(type)) {
                syncPromotion(companyId);
            } else {
                syncCoupon(companyId);
            }
        });
        shutdownExecutorService(executorService);
    }

    @Override
    public void updateOriginBasePrice(OrderReportRequest orderReportRequest) {
        ExecutorService executorService = Executors.newFixedThreadPool(1, new CustomizableThreadFactory("Sync_OriginBasePrice"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        executorService.execute(() -> {
            Pageable pageable = PageRequest.of(0, 100);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            while(true) {
                List<OrderModel> updatedOrders = orderService.updateOnlineOriginBasePrice(orderReportRequest, pageable);
                if(CollectionUtils.isEmpty(updatedOrders)) {
                    LOGGER.info("FINISHED SYNC updateOriginBasePrice: {} items", pageable.getPageNumber() * pageable.getPageSize());
                    break;
                }
                for(OrderModel model : updatedOrders) {
                    producerService.recalculateOrderReport(model, KafkaMessageType.RECALCULATE_ALL_ORDER_REPORT, null);
                }
                LOGGER.info("DONE updateOriginBasePrice: {} items", (pageable.getPageNumber() + 1) * pageable.getPageSize());
                pageable = pageable.next();
            }
        });
        shutdownExecutorService(executorService);
    }

    private void syncCoupon(Long companyId) {
        Pageable pageable = PageRequest.of(0, 300);
        while (true) {
            Page<CouponModel> pageContent = couponService.findAllByCompanyId(companyId, pageable);
            List<CouponModel> couponModels = pageContent.getContent();
            if (CollectionUtils.isEmpty(couponModels)) {
                LOGGER.info("FINISHED SYNC COUPON: {} items", pageContent.getTotalElements());
                break;
            }

            for (CouponModel couponModel : couponModels) {
                promotionProducer.sendCouponToKafka(couponModel);
            }
            pageable = pageable.next();
        }
    }

    private void syncPromotion(Long companyId) {
        Pageable pageable = PageRequest.of(0, 300);
        while (true) {
            Page<PromotionSourceRuleModel> pageContent = promotionSourceRuleService.findAllByCompanyId(companyId, pageable);
            List<PromotionSourceRuleModel> promotionSourceRuleModels = pageContent.getContent();
            if (CollectionUtils.isEmpty(promotionSourceRuleModels)) {
                LOGGER.info("FINISHED SYNC PROMOTION: {} items", pageContent.getTotalElements());
                break;
            }
            for (PromotionSourceRuleModel sourceRuleModel : promotionSourceRuleModels) {
                promotionProducer.sendPromotionToKafka(sourceRuleModel);
            }
            pageable = pageable.next();
        }
    }

    @Autowired
    public void setPromotionSourceRuleService(PromotionSourceRuleService promotionSourceRuleService) {
        this.promotionSourceRuleService = promotionSourceRuleService;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }

    @Autowired
    public void setPromotionProducer(PromotionProducer promotionProducer) {
        this.promotionProducer = promotionProducer;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setProducerService(OrderProducerService producerService) {
        this.producerService = producerService;
    }
}
