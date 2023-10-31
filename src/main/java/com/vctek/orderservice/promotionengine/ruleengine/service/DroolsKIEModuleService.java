package com.vctek.orderservice.promotionengine.ruleengine.service;


import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;

public interface DroolsKIEModuleService {
    DroolsKIEModuleModel findByName(String moduleName);

    DroolsKIEModuleModel findByCompanyId(Long companyId);

    void save(DroolsKIEModuleModel droolsKIEModuleModel);
}
