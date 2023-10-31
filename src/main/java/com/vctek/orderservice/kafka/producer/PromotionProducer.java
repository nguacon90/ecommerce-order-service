package com.vctek.orderservice.kafka.producer;

import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;

public interface PromotionProducer {
    void sendPromotionToKafka(PromotionSourceRuleModel model);

    void sendCouponToKafka(CouponModel couponModel);
}
