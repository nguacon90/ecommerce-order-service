package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.impl;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleActionsGenerator;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleConditionsGenerator;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleGeneratorContext;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleMetadataGenerator;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.repository.RuleBasePromotionRepository;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsKIEModuleService;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsRuleService;
import com.vctek.orderservice.promotionengine.ruleengine.service.RuleEngineService;
import com.vctek.orderservice.promotionengine.ruleengine.strategy.DroolsKIEBaseFinderStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIr;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.actions.RuleIrAction;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrVariablesContainer;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleParametersService;
import com.vctek.orderservice.service.ModelService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class DefaultDroolsRuleTargetCodeGeneratorTest
{
	public static final String RULE_UUID = "7be85ae9-8f69-4d51-a3b4-a4b08b457798";
	public static final String RULE_CODE = "rule_code";
	public static final String RULE_NAME = "rule_name";
	public static final String RULE_GROUP = "rule_group";
	public static final String MODULE_NAME = "MODULE_NAME";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private PromotionSourceRuleModel sourceRule;

	@Mock
	private DroolsRuleModel droolsRule;
	@Mock
	private RuleCompilerContext compilerContext;
	@Mock
	private RuleIrAction action;
	@Mock
	private RuleIrCondition condition;
	@Mock
	private ModelService modelService;
	@Mock
	private RuleEngineService platformRuleEngineService;
	@Mock
	private DroolsRuleConditionsGenerator droolsRuleConditionsGenerator;
	@Mock
	private DroolsRuleActionsGenerator droolsRuleActionsGenerator;
	@Mock
	private RuleParametersService ruleParametersService;
	@Mock
	private DroolsRuleMetadataGenerator droolsRuleMetadataGenerator;
	@Mock
	private DroolsKIEModuleModel rulesModule;
	@Mock
	private DroolsKIEBaseModel kieBase;

	@Mock
    private DroolsKIEModuleService droolsKIEModuleService;

	@Mock
    private DroolsRuleService droolsRuleService;

	@InjectMocks
	private DefaultDroolsRuleTargetCodeGenerator droolsRuleTargetCodeGenerator;

	@Mock
	private DroolsKIEBaseFinderStrategy droolsKIEBaseFinderStrategy;

	@Mock
    private RuleBasePromotionRepository ruleBasePromotionRepository;

	@Before
	public void setUp()
	{
		when(droolsRule.getUuid()).thenReturn(RULE_UUID);

		final RuleEngineActionResult result = new RuleEngineActionResult();
		result.setActionFailed(false);

		when(sourceRule.getUuid()).thenReturn(RULE_UUID);
		when(sourceRule.getCode()).thenReturn(RULE_CODE);
		when(sourceRule.getName()).thenReturn(RULE_NAME);
		when(sourceRule.getStartDate()).thenReturn(new Date());
		when(sourceRule.getEndDate()).thenReturn(new Date());
		when(rulesModule.getName()).thenReturn(MODULE_NAME);
        when(droolsRule.getRuleType()).thenReturn(RuleType.PROMOTION.toString());
		when(compilerContext.getRule()).thenReturn(sourceRule);
		when(compilerContext.getModuleName()).thenReturn(MODULE_NAME);
		when(droolsKIEBaseFinderStrategy.getKIEBaseForKIEModule(rulesModule)).thenReturn(kieBase);
		when(droolsRuleService.getRuleForCodeAndModule(anyString(), anyString())).thenReturn(droolsRule);
		when(droolsKIEModuleService.findByName(anyString())).thenReturn(rulesModule);

        droolsRuleTargetCodeGenerator.setDroolsKIEBaseFinderStrategy(droolsKIEBaseFinderStrategy);
        droolsRuleTargetCodeGenerator.setDroolsKIEModuleService(droolsKIEModuleService);
        droolsRuleTargetCodeGenerator.setRuleBasePromotionRepository(ruleBasePromotionRepository);

	}

//	@Test
//	public void nullTest()
//	{
//		// expect
//		expectedException.expect(IllegalArgumentException.class);
//
//		// when
//		droolsRuleTargetCodeGenerator.generate(compilerContext, null);
//	}

	@Test
	public void validRuleIrTest()
	{
		// given
		final RuleIr ruleIr = new RuleIr();
		ruleIr.setVariablesContainer(new RuleIrVariablesContainer());
		ruleIr.setConditions(Collections.singletonList(condition));
		ruleIr.setActions(Collections.singletonList(action));
		when(droolsRuleConditionsGenerator
					 .generateConditions(new DefaultDroolsGeneratorContext(compilerContext, ruleIr, droolsRule),
								  StringUtils.EMPTY)).thenReturn("");

		// when
		droolsRuleTargetCodeGenerator.generate(compilerContext, ruleIr);

		//then
		Mockito.verify(droolsRule, Mockito.times(1)).setKieBase(kieBase);
		//then
		Mockito.verify(droolsRuleService, Mockito.times(1)).save(droolsRule);
	}

	@Test
	public void validCreateNewRule()
	{
		// given
		final RuleIr ruleIr = new RuleIr();
		ruleIr.setVariablesContainer(new RuleIrVariablesContainer());
		ruleIr.setConditions(Collections.singletonList(condition));
		ruleIr.setActions(Collections.singletonList(action));

		// when
		droolsRuleTargetCodeGenerator.generate(compilerContext, ruleIr);

		//then
		Mockito.verify(droolsRule).setKieBase(kieBase);
	}

	private Calendar getCalendarForLocale(final Locale locale)
	{
		final Calendar calendar = Calendar.getInstance(locale);
		calendar.set(Calendar.YEAR, 2016);
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar;
	}

	@Test
	public void testCreateNewRuleEstablishReferenceToSourceRule()
	{
		// given
		final RuleIr ruleIr = new RuleIr();
		ruleIr.setVariablesContainer(new RuleIrVariablesContainer());
		ruleIr.setConditions(singletonList(condition));
		ruleIr.setActions(singletonList(action));

		// when
		droolsRuleTargetCodeGenerator.generate(compilerContext, ruleIr);

		Mockito.verify(droolsRule).setPromotionSourceRule(sourceRule);
	}

//	@Test
//	public void testCreateNewRuleAssignRuleGroup()
//	{
//		// given
//		final RuleIr ruleIr = new RuleIr();
//		ruleIr.setVariablesContainer(new RuleIrVariablesContainer());
//		ruleIr.setConditions(singletonList(condition));
//		ruleIr.setActions(singletonList(action));
//
//		// when
//		droolsRuleTargetCodeGenerator.generate(compilerContext, ruleIr);
//
//		Mockito.verify(droolsRule).setRuleGroupCode(RULE_GROUP);
//	}

	@Test
	public void testGenerateRequiredFactsCheckIsCalled()
	{
		// given
		final RuleIr ruleIr = new RuleIr();
		ruleIr.setVariablesContainer(new RuleIrVariablesContainer());
		ruleIr.setConditions(singletonList(condition));
		ruleIr.setActions(singletonList(action));

		droolsRuleTargetCodeGenerator.generate(compilerContext, ruleIr);

		Mockito.verify(droolsRuleConditionsGenerator).generateRequiredFactsCheckPattern(any(DroolsRuleGeneratorContext.class));
	}
}
