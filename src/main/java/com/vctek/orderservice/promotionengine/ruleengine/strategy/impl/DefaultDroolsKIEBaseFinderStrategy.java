package com.vctek.orderservice.promotionengine.ruleengine.strategy.impl;

import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolKIEBaseRepository;
import com.vctek.orderservice.promotionengine.ruleengine.strategy.DroolsKIEBaseFinderStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultDroolsKIEBaseFinderStrategy implements DroolsKIEBaseFinderStrategy {
    private DroolKIEBaseRepository droolKIEBaseRepository;

    public DefaultDroolsKIEBaseFinderStrategy(DroolKIEBaseRepository droolKIEBaseRepository) {
        this.droolKIEBaseRepository = droolKIEBaseRepository;
    }

    @Override
    public DroolsKIEBaseModel getKIEBaseForKIEModule(DroolsKIEModuleModel kieModule) {
        DroolsKIEBaseModel kieBase = kieModule.getDefaultKIEBase();
        if (kieBase == null) {
            List<DroolsKIEBaseModel> droolsKIEBases = droolKIEBaseRepository.findAllByDroolsKIEModule(kieModule);
            if (CollectionUtils.isEmpty(droolsKIEBases)) {
                return null;
            }

            kieBase = droolsKIEBases.iterator().next();
        }

        return kieBase;
    }
}
