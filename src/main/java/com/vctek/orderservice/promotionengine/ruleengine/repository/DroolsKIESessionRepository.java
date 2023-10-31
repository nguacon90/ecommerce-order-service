package com.vctek.orderservice.promotionengine.ruleengine.repository;

import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIESessionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DroolsKIESessionRepository extends JpaRepository<DroolsKIESessionModel, Long> {
    List<DroolsKIESessionModel> findAllByDroolsKIEBase(DroolsKIEBaseModel kieBase);
}
