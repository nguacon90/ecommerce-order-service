package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.impl;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.*;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.util.Map2StringUtils;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedPromotionModel;
import com.vctek.orderservice.promotionengine.promotionservice.repository.RuleBasePromotionRepository;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleCompilerException;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleConverterException;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsKIEModuleService;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsRuleService;
import com.vctek.orderservice.promotionengine.ruleengine.strategy.DroolsKIEBaseFinderStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIr;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIrVariable;
import com.vctek.orderservice.promotionengine.ruleengineservice.maintenance.RuleCompilationContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.AbstractRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.EvaluationTimeRRD;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleConfigurationRRD;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleParametersService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component("droolsRuleTargetCodeGenerator")
public class DefaultDroolsRuleTargetCodeGenerator implements RuleTargetCodeGenerator {
    public static final int BUFFER_SIZE = 4096;
    public static final int RULE_CONFIG_BUFFER_SIZE = 128;
    public static final String DROOLS_RULES_PACKAGE = "com.vctek.droolsruleengine";
    public static final String DEFAULT_RULE_GROUP_CODE = "defaultPromotionRuleGroup";
    private DroolsRuleService droolsRuleService;
    private DroolsRuleConditionsGenerator droolsRuleConditionsGenerator;
    private DroolsRuleActionsGenerator droolsRuleActionsGenerator;
    private DroolsRuleMetadataGenerator droolsRuleMetadataGenerator;
    private RuleParametersService ruleParametersService;
    private DroolsKIEModuleService droolsKIEModuleService;
    private DroolsKIEBaseFinderStrategy droolsKIEBaseFinderStrategy;
    private RuleBasePromotionRepository ruleBasePromotionRepository;

    public DefaultDroolsRuleTargetCodeGenerator(DroolsRuleService droolsRuleService,
                                                DroolsRuleConditionsGenerator droolsRuleConditionsGenerator,
                                                DroolsRuleActionsGenerator droolsRuleActionsGenerator,
                                                DroolsRuleMetadataGenerator droolsRuleMetadataGenerator,
                                                RuleParametersService ruleParametersService) {
        this.droolsRuleService = droolsRuleService;
        this.droolsRuleConditionsGenerator = droolsRuleConditionsGenerator;
        this.droolsRuleActionsGenerator = droolsRuleActionsGenerator;
        this.droolsRuleMetadataGenerator = droolsRuleMetadataGenerator;
        this.ruleParametersService = ruleParametersService;
    }

    @Override
    @Transactional
    public DroolsRuleModel generate(RuleCompilerContext context, RuleIr ruleIr) {
        String moduleName = context.getModuleName();
        PromotionSourceRuleModel rule = context.getRule();
        DroolsRuleModel droolsRule = droolsRuleService.getRuleForCodeAndModule(rule.getCode(), moduleName);
        if (droolsRule == null) {
            droolsRule = new DroolsRuleModel();
            droolsRule.setCode(rule.getCode());
            droolsRule.setRuleType(RuleType.PROMOTION.toString());
        }

        droolsRule.setUuid(UUID.randomUUID().toString());
        droolsRule.setPromotionSourceRule(rule);
        droolsRule.setActive(rule.isActive());
        droolsRule.setMessageFired(rule.getMessageFired());
        droolsRule.setRuleGroupCode(getRuleGroupCode(rule));
        try {
            String ruleParameters = ruleParametersService.convertParametersToString(context.getRuleParameters());
            droolsRule.setRuleParameters(ruleParameters);
        } catch (RuleConverterException var13) {
            throw new RuleCompilerException("RuleConverterException caught: ", var13);
        }

        DroolsRuleGeneratorContext generatorContext = new DefaultDroolsGeneratorContext(context, ruleIr, droolsRule);
        String ruleContent = this.generateRuleContent(generatorContext);
        Map<String, String> globals = this.generateGlobals(generatorContext);
        droolsRule.setRuleContent(ruleContent);
        droolsRule.setGlobals(Map2StringUtils.mapToString(globals));
        droolsRule.setMaxAllowedRuns(rule.getMaxAllowedRuns());
        droolsRule.setRulePackage(DROOLS_RULES_PACKAGE);
        DroolsKIEModuleModel rulesModule = droolsKIEModuleService.findByName(moduleName);
        DroolsKIEBaseModel baseForKIEModule = droolsKIEBaseFinderStrategy.getKIEBaseForKIEModule(rulesModule);
        droolsRule.setKieBase(baseForKIEModule);
        droolsRule.setCurrentVersion(Boolean.TRUE);
        this.setVersionIfAbsent(context.getRuleCompilationContext(), droolsRule, moduleName);
        doPrepare(droolsRule);

        return droolsRuleService.save(droolsRule);
    }

    protected void setVersionIfAbsent(RuleCompilationContext ruleCompilationContext, AbstractRuleEngineRuleModel ruleModel,
                                      String moduleName) {
        if (Objects.isNull(ruleModel.getVersion())) {
            Long nextRuleEngineRuleVersion = ruleCompilationContext.getNextRuleEngineRuleVersion(moduleName);
            ruleModel.setVersion(nextRuleEngineRuleVersion);
        }

    }

    protected String getRuleGroupCode(AbstractRuleModel rule) {
        return DEFAULT_RULE_GROUP_CODE;
    }

    protected void doPrepare(DroolsRuleModel model) {
        if(!RuleType.PROMOTION.toString().equalsIgnoreCase(model.getRuleType())) {
            return;
        }

        RuleBasedPromotionModel ruleBasedPromotion = model.getPromotion();
        if (Objects.isNull(ruleBasedPromotion)) {
            ruleBasedPromotion = this.createNewPromotionAndAddToRuleModel(model);
        }

        PromotionSourceRuleModel rule = model.getPromotionSourceRule();
        if(rule != null) {
            ruleBasedPromotion.setPriority(rule.getPriority());
            ruleBasedPromotion.setStartDate(rule.getStartDate());
            ruleBasedPromotion.setEndDate(rule.getEndDate());
        }

        ruleBasePromotionRepository.save(ruleBasedPromotion);
    }

    protected RuleBasedPromotionModel createNewPromotionAndAddToRuleModel(DroolsRuleModel ruleModel) {
        RuleBasedPromotionModel ruleBasedPromotion = new RuleBasedPromotionModel();
        ruleBasedPromotion.setCode(ruleModel.getCode());
        ruleBasedPromotion.setTitle(ruleModel.getCode());
        ruleBasedPromotion.setMessageFired(ruleModel.getMessageFired());
        ruleBasedPromotion.setEnabled(Boolean.TRUE);
        ruleBasedPromotion.setRule(ruleModel);
        ruleModel.setPromotion(ruleBasedPromotion);
        return ruleBasedPromotion;
    }

    private Map<String, String> generateGlobals(DroolsRuleGeneratorContext context) {
        Map<String, String> globals = new HashMap<>();
        Iterator var4 = context.getGlobals().entrySet().iterator();
        context.getGlobals().entrySet().iterator();
        while (var4.hasNext()) {
            Map.Entry<String, Class> entry = (Map.Entry<String, Class>) var4.next();
            globals.put(entry.getKey(), entry.getKey());
        }

        return globals;
    }

    private String generateRuleContent(DroolsRuleGeneratorContext context) {
        StringBuilder ruleContent = new StringBuilder(BUFFER_SIZE);
        String indentation = context.getIndentationSize();
        String generatedConditions = droolsRuleConditionsGenerator.generateConditions(context, indentation);
        String generatedActions = droolsRuleActionsGenerator.generateActions(context, indentation);
        String generatedQuery = this.generateRuleContentQuery(context, generatedConditions);
        String metadata = droolsRuleMetadataGenerator.generateMetadata(context, indentation);
        String generatedRule = this.generateRuleContentRule(context, generatedActions, metadata);
        ruleContent.append("package ").append(DROOLS_RULES_PACKAGE).append(";\n\n");
        Iterator var10 = context.getImports().iterator();

        while (var10.hasNext()) {
            Class<?> importType = (Class) var10.next();
            ruleContent.append("import ").append(importType.getName()).append(";\n");
        }

        ruleContent.append('\n');
        var10 = context.getGlobals().entrySet().iterator();

        while (var10.hasNext()) {
            Map.Entry<String, Class<?>> globalEntry = (Map.Entry) var10.next();
            ruleContent.append("global ").append(globalEntry.getValue().getName()).append(' ')
                    .append(globalEntry.getKey()).append(";\n");
        }

        ruleContent.append('\n');
        ruleContent.append(generatedQuery);
        ruleContent.append('\n');
        ruleContent.append(generatedRule);
        return ruleContent.toString();
    }

    protected String generateRuleContentQuery(DroolsRuleGeneratorContext context, String conditions) {
        StringJoiner queryParameters = new StringJoiner(", ");
        Map<String, RuleIrVariable> variables = context.getVariables();
        String variableClassName;
        if (MapUtils.isNotEmpty(variables)) {
            Iterator var6 = variables.values().iterator();

            while (var6.hasNext()) {
                RuleIrVariable variable = (RuleIrVariable) var6.next();
                variableClassName = context.generateClassName(variable.getType());
                queryParameters.add(variableClassName + " " + context.getVariablePrefix() + variable.getName());
            }
        }

        StringBuilder buffer = new StringBuilder(BUFFER_SIZE);
        DroolsRuleModel droolsRule = context.getDroolsRule();
        variableClassName = droolsRule.getUuid().replaceAll("-", "");
        buffer.append("query rule_").append(variableClassName).append("_query(").append(queryParameters.toString()).append(")\n");
        buffer.append(conditions);
        buffer.append("end\n");
        return buffer.toString();
    }

    protected String generateRuleContentRule(DroolsRuleGeneratorContext context, String actions, String metadata) {
        AbstractRuleModel rule = context.getRuleCompilerContext().getRule();
        DroolsRuleModel droolsRule = context.getDroolsRule();
        StringBuilder buffer = new StringBuilder(BUFFER_SIZE);
        buffer.append("rule \"").append(droolsRule.getUuid()).append("\"\n");
        buffer.append("@ruleCode(\"").append(rule.getCode()).append("\")\n");
        buffer.append("@moduleName(\"").append(context.getRuleCompilerContext().getModuleName()).append("\")\n");
        buffer.append(metadata);
        buffer.append("dialect \"mvel\" \n");
        buffer.append("salience ").append(rule.getPriority()).append('\n');
        buffer.append("when\n");
        buffer.append(this.generateConfigVariable(context, rule));
        String requiredFactsCheckPattern = droolsRuleConditionsGenerator.generateRequiredFactsCheckPattern(context);
        buffer.append(this.generateRequiredFactsCheck(context, requiredFactsCheckPattern));
        buffer.append(this.generateDateRangeCondition(context, rule));
        buffer.append(this.generateAccumulateFunction(context, droolsRule));
        buffer.append(this.generateResultCountCondition(context));
        buffer.append("then\n");
        buffer.append(actions);
        buffer.append("end\n");
        return buffer.toString();
    }

    protected StringBuilder generateDateRangeCondition(DroolsRuleGeneratorContext context, AbstractRuleModel rule) {
        StringBuilder builder = new StringBuilder(BUFFER_SIZE);
        Date startDate = rule.getStartDate();
        Date endDate = rule.getEndDate();
        if (startDate != null || endDate != null) {
            String evaluationTimeClassName = context.generateClassName(EvaluationTimeRRD.class);
            String indentation = context.getIndentationSize();
            String startDateCondition = startDate != null ? String.format("evaluationTime >= %d", startDate.getTime()) : "";
            String endDateCondition = endDate != null ? String.format("evaluationTime <= %d", endDate.getTime()) : "";
            String dateConditionDelimiter = StringUtils.isNotEmpty(startDateCondition) && StringUtils.isNotEmpty(endDateCondition) ? " && " : "";
            builder.append(indentation).append("$evaluationTimeRRD := ")
                    .append(evaluationTimeClassName).append("(")
                    .append(startDateCondition).append(dateConditionDelimiter)
                    .append(endDateCondition).append(")\n");
        }

        return builder;
    }

    protected StringBuilder generateAccumulateFunction(DroolsRuleGeneratorContext context, DroolsRuleModel droolsRule) {
        String l1Indentation = context.getIndentationSize();
        String l2Indentation = l1Indentation + context.getIndentationSize();
        Map<String, RuleIrVariable> variables = context.getVariables();
        StringBuilder buffer = new StringBuilder(4096);
        buffer.append(l1Indentation).append("accumulate (\n");
        StringJoiner queryParameters = new StringJoiner(", ");
        Iterator var9 = variables.values().iterator();

        while (var9.hasNext()) {
            RuleIrVariable variable = (RuleIrVariable) var9.next();
            queryParameters.add(context.getVariablePrefix() + variable.getName());
        }

        String uuid = droolsRule.getUuid().replaceAll("-", "");
        buffer.append(l2Indentation).append("rule_").append(uuid).append("_query(").append(queryParameters).append(";)\n");
        buffer.append(l1Indentation).append(";\n");
        StringJoiner accumulateFunctions = new StringJoiner(",\n");
        Iterator var11 = variables.values().iterator();

        while (var11.hasNext()) {
            RuleIrVariable variable = (RuleIrVariable) var11.next();
            accumulateFunctions.add(l2Indentation + context.getVariablePrefix() + variable.getName() + "_set : collectSet(" + context.getVariablePrefix() + variable.getName() + ")");
        }

        accumulateFunctions.add(l2Indentation + "$result_count : count(1)");
        buffer.append(accumulateFunctions).append('\n');
        buffer.append(l1Indentation).append(")\n");
        return buffer;
    }

    protected StringBuilder generateResultCountCondition(DroolsRuleGeneratorContext context) {
        String l1Indentation = context.getIndentationSize();
        return (new StringBuilder(l1Indentation)).append("eval($result_count > 0)\n");
    }

    protected StringBuilder generateConfigVariable(DroolsRuleGeneratorContext context, AbstractRuleModel rule) {
        String l1Indentation = context.getIndentationSize();
        String ruleConfigurationClassName = context.generateClassName(RuleConfigurationRRD.class);
        return (new StringBuilder(RULE_CONFIG_BUFFER_SIZE)).append(l1Indentation).append("exists (")
                .append(ruleConfigurationClassName).append("(ruleCode == \"").append(rule.getCode()).append("\"))\n");
    }

    protected StringBuilder generateRequiredFactsCheck(DroolsRuleGeneratorContext context, String conditions) {
        if (StringUtils.isNotEmpty(conditions)) {
            String indentation = context.getIndentationSize();
            return (new StringBuilder(RULE_CONFIG_BUFFER_SIZE)).append(indentation)
                    .append("and")
                    .append("\n").append(conditions);
        } else {
            return new StringBuilder();
        }
    }

    @Autowired
    public void setDroolsKIEModuleService(DroolsKIEModuleService droolsKIEModuleService) {
        this.droolsKIEModuleService = droolsKIEModuleService;
    }

    @Autowired
    public void setDroolsKIEBaseFinderStrategy(DroolsKIEBaseFinderStrategy droolsKIEBaseFinderStrategy) {
        this.droolsKIEBaseFinderStrategy = droolsKIEBaseFinderStrategy;
    }

    @Autowired
    public void setRuleBasePromotionRepository(RuleBasePromotionRepository ruleBasePromotionRepository) {
        this.ruleBasePromotionRepository = ruleBasePromotionRepository;
    }
}
