package com.vctek.orderservice.promotionengine.ruleengine.repository;

import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DroolsKIEModuleRepository extends JpaRepository<DroolsKIEModuleModel, Long> {
    DroolsKIEModuleModel findByNameAndActive(String name, boolean active);

    DroolsKIEModuleModel findByName(String moduleName);

    List<DroolsKIEModuleModel> findAllByCompanyIdOrderByCreationTimeDesc(Long companyId);

}
