package com.vctek.orderservice.promotionengine.ruleengine.repository;

import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DroolKIEBaseRepository extends JpaRepository<DroolsKIEBaseModel, Long> {
    List<DroolsKIEBaseModel> findAllByDroolsKIEModule(DroolsKIEModuleModel kieModule);
}
