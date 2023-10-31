package com.vctek.orderservice.promotionengine.ruleengine.repository;

import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIESessionModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DroolsRuleEngineContextRepository extends JpaRepository<DroolsRuleEngineContextModel, Long> {
    DroolsRuleEngineContextModel findByCode(String code);

    DroolsRuleEngineContextModel findByKieSession(DroolsKIESessionModel kieSession);
}
