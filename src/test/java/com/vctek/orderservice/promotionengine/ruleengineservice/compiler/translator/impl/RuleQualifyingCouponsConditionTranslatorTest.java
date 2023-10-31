package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrAttributeCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrFalseCondition;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CouponRAO;
import com.vctek.orderservice.service.CouponService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RuleQualifyingCouponsConditionTranslatorTest {
	private static final String COUPON_ID = "couponId";

	private static final String COUPON_RAO_VARIABLE = "couponRaoVariable";

	private RuleQualifyingCouponsConditionTranslator translator;

	@Mock
	private RuleCompilerContext context;
	@Mock
	private RuleConditionData condition;
	@Mock
	private RuleConditionDefinitionData conditionDefinition;
	@Mock
	private Map<String, RuleParameterData> parameters;
	@Mock
	private RuleParameterData couponsParameter;
	@Mock
    private CouponService couponService;
	@Mock
    private PromotionSourceRuleModel rule;

    @Before
	public void setUp()
	{
	    MockitoAnnotations.initMocks(this);
        translator = new RuleQualifyingCouponsConditionTranslator();
        translator.setCouponService(couponService);
		when(condition.getParameters()).thenReturn(parameters);
		when(parameters.get(RuleQualifyingCouponsConditionTranslator.COUPONS_PARAM)).thenReturn(couponsParameter);
		when(context.generateVariable(CouponRAO.class)).thenReturn(COUPON_RAO_VARIABLE);
		when(couponsParameter.getValue()).thenReturn(Collections.singletonList(COUPON_ID));
	}

	@Test
	public void testTranslateOperatorParamNull()
	{
		when(parameters.get(RuleQualifyingCouponsConditionTranslator.COUPONS_PARAM)).thenReturn(null);
		final RuleIrCondition ruleIrCondition = translator.translate(context, condition, conditionDefinition);

		assertThat(ruleIrCondition, instanceOf(RuleIrFalseCondition.class));
	}

	@Test
	public void testTranslateParamValueNull()
	{
		when(couponsParameter.getValue()).thenReturn(null);
		final RuleIrCondition ruleIrCondition = translator.translate(context, condition, conditionDefinition);

		assertThat(ruleIrCondition, instanceOf(RuleIrFalseCondition.class));
	}

	@Test
	public void testTranslate()
	{
	    when(context.getRule()).thenReturn(rule);
		final RuleIrCondition ruleIrCondition = translator.translate(context, condition, conditionDefinition);

		assertThat(ruleIrCondition, instanceOf(RuleIrAttributeCondition.class));
		assertEquals(RuleIrAttributeOperator.IN, ((RuleIrAttributeCondition) ruleIrCondition).getOperator());
		assertEquals(RuleQualifyingCouponsConditionTranslator.COUPON_RAO_COUPON_ID_ATTRIBUTE,
				((RuleIrAttributeCondition) ruleIrCondition).getAttribute());
		assertEquals(COUPON_ID, ((List<String>) ((RuleIrAttributeCondition) ruleIrCondition).getValue()).get(0));
		assertEquals(COUPON_RAO_VARIABLE, ((RuleIrAttributeCondition) ruleIrCondition).getVariable());
		verify(couponService).updateUseForPromotion(anyList(), eq(rule));
	}

	@Test
	public void testTranslateAlternative()
	{
        when(context.getRule()).thenReturn(rule);
		when(couponsParameter.getValue()).thenReturn(Arrays.asList("ttt", COUPON_ID));
		final RuleIrCondition ruleIrCondition = translator.translate(context, condition, conditionDefinition);

		assertThat(ruleIrCondition, instanceOf(RuleIrAttributeCondition.class));
		assertEquals(RuleIrAttributeOperator.IN, ((RuleIrAttributeCondition) ruleIrCondition).getOperator());
		assertEquals(RuleQualifyingCouponsConditionTranslator.COUPON_RAO_COUPON_ID_ATTRIBUTE,
				((RuleIrAttributeCondition) ruleIrCondition).getAttribute());
		assertTrue(((List<String>) ((RuleIrAttributeCondition) ruleIrCondition).getValue()).contains("ttt"));
		assertTrue(((List<String>) ((RuleIrAttributeCondition) ruleIrCondition).getValue()).contains(COUPON_ID));
		assertEquals(COUPON_RAO_VARIABLE, ((RuleIrAttributeCondition) ruleIrCondition).getVariable());
        verify(couponService).updateUseForPromotion(anyList(), eq(rule));
	}
}
