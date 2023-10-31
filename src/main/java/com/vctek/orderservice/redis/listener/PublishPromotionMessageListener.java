package com.vctek.orderservice.redis.listener;

import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsKIEModuleRepository;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsRuleRepository;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.RuleMaintenanceService;
import com.vctek.orderservice.redis.data.PublishPromotionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class PublishPromotionMessageListener implements MessageListener {
    private Jackson2JsonRedisSerializer<PublishPromotionData> jackson2JsonRedisSerializer;
    private RuleMaintenanceService ruleMaintenanceService;
    private DroolsRuleRepository droolsRuleRepository;
    private DroolsKIEModuleRepository droolsKIEModuleRepository;

    public PublishPromotionMessageListener() {
        jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(PublishPromotionData.class);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        PublishPromotionData promotionData = jackson2JsonRedisSerializer.deserialize(message.getBody());
        Long droolsRuleId = promotionData.getDroolsRuleId();
        Optional<DroolsRuleModel> ruleOptional = droolsRuleRepository.findById(droolsRuleId);
        Optional<DroolsKIEModuleModel> kieModuleOptional = droolsKIEModuleRepository.findById(promotionData.getKieModuleId());
        if(ruleOptional.isPresent() && kieModuleOptional.isPresent()) {
            DroolsRuleModel droolsRuleModel = ruleOptional.get();
            ruleMaintenanceService.publishDroolsRules(Arrays.asList(droolsRuleModel),
                    kieModuleOptional.get().getName(), false, true);
        }
    }

    @Autowired
    public void setRuleMaintenanceService(RuleMaintenanceService ruleMaintenanceService) {
        this.ruleMaintenanceService = ruleMaintenanceService;
    }

    @Autowired
    public void setDroolsRuleRepository(DroolsRuleRepository droolsRuleRepository) {
        this.droolsRuleRepository = droolsRuleRepository;
    }

    @Autowired
    public void setDroolsKIEModuleRepository(DroolsKIEModuleRepository droolsKIEModuleRepository) {
        this.droolsKIEModuleRepository = droolsKIEModuleRepository;
    }
}
