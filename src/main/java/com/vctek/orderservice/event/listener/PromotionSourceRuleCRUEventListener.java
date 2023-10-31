package com.vctek.orderservice.event.listener;

import com.vctek.orderservice.event.CouponCRUEvent;
import com.vctek.orderservice.event.PromotionSourceRuleCRUEvent;
import com.vctek.orderservice.kafka.producer.PromotionProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PromotionSourceRuleCRUEventListener {
    private static Logger LOGGER = LoggerFactory.getLogger(PromotionSourceRuleCRUEventListener.class);
    private PromotionProducer promotionProducer;

    @TransactionalEventListener
    public void promotionSourceRuleCRUEventListen(PromotionSourceRuleCRUEvent event) {
        if(event.getSourceRuleModel() == null) {
            return;
        }
        try {
            promotionProducer.sendPromotionToKafka(event.getSourceRuleModel());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @TransactionalEventListener
    public void couponCRUEventListen(CouponCRUEvent event) {
        if(event.getCouponModel() == null) {
            return;
        }

        try {
            promotionProducer.sendCouponToKafka(event.getCouponModel());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    @Autowired
    public void setPromotionProducer(PromotionProducer promotionProducer) {
        this.promotionProducer = promotionProducer;
    }
}
