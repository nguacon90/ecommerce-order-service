package com.vctek.orderservice.event.listener;

import com.vctek.orderservice.event.PublishPromotionSourceRuleEvent;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.redis.data.PublishPromotionData;
import com.vctek.orderservice.redis.publisher.RedisPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PromotionSourceRuleEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionSourceRuleEventListener.class);
    private RedisPublisher<PublishPromotionData> promotionPublisher;

    @TransactionalEventListener
    public void processUpdatePromotionDroolRule(PublishPromotionSourceRuleEvent event) {
        DroolsRuleModel droolsRuleModel = event.getDroolsRuleModel();
        if(droolsRuleModel == null) {
            LOGGER.warn("Drool rule model is null");
            return;
        }

        PublishPromotionData data = new PublishPromotionData();
        data.setDroolsRuleId(droolsRuleModel.getId());
        PromotionSourceRuleModel promotionSourceRuleModel = event.getPromotionSourceRuleModel();
        data.setPromotionSourceRuleId(promotionSourceRuleModel != null ? promotionSourceRuleModel.getId() : null);
        data.setKieModuleId(event.getKieModule().getId());
        promotionPublisher.publish(data);
    }

    @Autowired
    public void setPromotionPublisher(RedisPublisher<PublishPromotionData> promotionPublisher) {
        this.promotionPublisher = promotionPublisher;
    }
}
