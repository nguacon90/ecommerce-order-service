package com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.impl;

import com.vctek.orderservice.promotionengine.ruleengine.ExecutionContext;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;
import com.vctek.orderservice.promotionengine.ruleengine.init.InitializationFuture;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsKIEModuleRepository;
import com.vctek.orderservice.promotionengine.ruleengine.service.RuleEngineService;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.PublishResult;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.RuleMaintenanceService;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.RulePublisherResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RuleMaintenanceServiceImpl implements RuleMaintenanceService {
    private RuleEngineService ruleEngineService;
    private DroolsKIEModuleRepository droolsKIEModuleRepository;

    @Override
    public <T extends AbstractRuleEngineRuleModel> RulePublisherResult publishDroolsRules(List<T> rules, String moduleName,
                                                              boolean enableIncrementalUpdate, boolean isBlocking) {
        if(CollectionUtils.isEmpty(rules)) {
            return RulePublisherResult.SUCCESS;
        }
        DroolsKIEModuleModel moduleModel = droolsKIEModuleRepository.findByName(moduleName);
        Map<String, Long> versions = rules.stream()
                .collect(Collectors.toMap(AbstractRuleEngineRuleModel::getCode,
                        AbstractRuleEngineRuleModel::getVersion));
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setRuleVersions(versions);

        InitializationFuture initializationFuture = ruleEngineService.initialize(Collections.singletonList(moduleModel),
                                                        enableIncrementalUpdate, executionContext);

        if(isBlocking) {
            initializationFuture.waitForInitializationToFinish();
        }

        List<RuleEngineActionResult> publisherResults = initializationFuture.getResults();
        PublishResult result = publisherResults.stream().filter(RuleEngineActionResult::isActionFailed)
                .findAny().map((r) -> PublishResult.ERROR).orElse(PublishResult.SUCCESS);

        return new RulePublisherResult(result, publisherResults);
    }

    @Autowired
    @Qualifier("droolsRuleEngineService")
    public void setRuleEngineService(RuleEngineService ruleEngineService) {
        this.ruleEngineService = ruleEngineService;
    }

    @Autowired
    public void setDroolsKIEModuleRepository(DroolsKIEModuleRepository droolsKIEModuleRepository) {
        this.droolsKIEModuleRepository = droolsKIEModuleRepository;
    }
}
