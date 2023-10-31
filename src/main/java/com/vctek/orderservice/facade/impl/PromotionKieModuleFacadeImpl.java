package com.vctek.orderservice.facade.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.facade.PromotionKieModuleFacade;
import com.vctek.orderservice.promotionengine.promotionservice.service.impl.DefaultPromotionEngineService;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengine.init.RuleEngineBootstrap;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIESessionModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsKIEModuleService;
import org.apache.commons.collections4.CollectionUtils;
import org.kie.api.builder.model.KieSessionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Set;
import java.util.UUID;

@Component
public class PromotionKieModuleFacadeImpl implements PromotionKieModuleFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionKieModuleFacadeImpl.class);
    public static final String KIE_BASE_NAME_PATTERN = "company-{0}-kie-base";
    public static final String KIE_SESSION_NAME_PATTERN = "company-{0}-kie-session";
    public static final String KIE_MODULE_NAME_PATTERN = "company-{0}-kie-module";
    public static final String KIE_MODULE_GROUP_ID_PATTERN = "company-{0}-groupId";
    public static final String RULE_ENGINE_CONTEXT_NAME_PATTERN = "drool-engine-context-{0}";
    public static final String MVN_VERSION = "1.0.0";
    private DroolsKIEModuleService droolsKIEModuleService;
    private RuleEngineBootstrap ruleEngineBootstrap;

    public PromotionKieModuleFacadeImpl(DroolsKIEModuleService droolsKIEModuleService) {
        this.droolsKIEModuleService = droolsKIEModuleService;
    }

    @Override
    public void init(Long companyId) {
        DroolsKIEModuleModel droolsKIEModuleModel;
        try {
            droolsKIEModuleModel = droolsKIEModuleService.findByCompanyId(companyId);
        } catch (ServiceException e) {
            droolsKIEModuleModel = new DroolsKIEModuleModel();
        }
        if(droolsKIEModuleModel.getId() != null) {
            LOGGER.info("Had initialized promotion module");
            return;
        }
        droolsKIEModuleModel.setCompanyId(companyId);
        droolsKIEModuleModel.setName(MessageFormat.format(KIE_MODULE_NAME_PATTERN, companyId.toString()));
        droolsKIEModuleModel.setRuleType(RuleType.PROMOTION.toString());
        droolsKIEModuleModel.setActive(true);
        droolsKIEModuleModel.setLockAcquired(false);
        DroolsKIEBaseModel kieBaseModel = droolsKIEModuleModel.getDefaultKIEBase() != null ? droolsKIEModuleModel.getDefaultKIEBase() : new DroolsKIEBaseModel();
        DroolsKIESessionModel kieSessionModel = kieBaseModel.getDefaultKieSession() != null ? kieBaseModel.getDefaultKieSession() : new DroolsKIESessionModel();
        kieSessionModel.setName(MessageFormat.format(KIE_SESSION_NAME_PATTERN, companyId.toString()));
        kieSessionModel.setSessionType(KieSessionModel.KieSessionType.STATEFUL.toString());

        kieBaseModel.setName(MessageFormat.format(KIE_BASE_NAME_PATTERN, companyId.toString()));
        kieBaseModel.setDefaultKieSession(kieSessionModel);
        kieBaseModel.setDroolsKIEModule(droolsKIEModuleModel);
        kieSessionModel.setDroolsKIEBase(kieBaseModel);

        Set<DroolsRuleEngineContextModel> droolsRuleEngineContexts = kieSessionModel.getDroolsRuleEngineContexts();
        if(CollectionUtils.isEmpty(droolsRuleEngineContexts)) {
            DroolsRuleEngineContextModel contextModel = new DroolsRuleEngineContextModel();
            contextModel.setKieSession(kieSessionModel);
            contextModel.setCode(UUID.randomUUID().toString());
            contextModel.setRuleFiringLimit(DefaultPromotionEngineService.DEFAULT_RULE_FIRING_LIMIT);
            contextModel.setName(MessageFormat.format(RULE_ENGINE_CONTEXT_NAME_PATTERN, companyId.toString()));
            kieSessionModel.getDroolsRuleEngineContexts().add(contextModel);
        }

        droolsKIEModuleModel.setDefaultKIEBase(kieBaseModel);
        droolsKIEModuleModel.setMvnGroupId(MessageFormat.format(KIE_MODULE_GROUP_ID_PATTERN, companyId.toString()));
        droolsKIEModuleModel.setMvnArtifactId("promotions");
        droolsKIEModuleModel.setMvnVersion(MVN_VERSION);
        droolsKIEModuleModel.setDeployedMvnVersion(MVN_VERSION);
        droolsKIEModuleModel.setVersion(0l);

        droolsKIEModuleService.save(droolsKIEModuleModel);
        ruleEngineBootstrap.startup(droolsKIEModuleModel);
    }

    @Autowired
    public void setRuleEngineBootstrap(RuleEngineBootstrap ruleEngineBootstrap) {
        this.ruleEngineBootstrap = ruleEngineBootstrap;
    }
}
