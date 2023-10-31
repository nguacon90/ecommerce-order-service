package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.impl.DefaultDroolsRuleTargetCodeGenerator;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleEngineRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rrd.RuleConfigurationRRD;
import com.vctek.orderservice.promotionengine.util.RuleEngineServiceConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static java.lang.Integer.valueOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


public class RuleConfigurationRrdPopulatorUnitTest
{

	private static final String RULE_CODE = "RULE_CODE";
	private static final String RULE_GROUP_CODE = "RULE_GROUP_CODE";
	private static final int MAX_RUNS = 12;
	private static final String DEFAULT_RULE_GROUP_CODE = DefaultDroolsRuleTargetCodeGenerator.DEFAULT_RULE_GROUP_CODE;

	@InjectMocks
	private RuleConfigurationRRDPopulator populator;

	@Mock
	private AbstractRuleEngineRuleModel source;

	@Before
	public void setUp() {
	    MockitoAnnotations.initMocks(this);
		populator = new RuleConfigurationRRDPopulator();
		when(source.getMaxAllowedRuns()).thenReturn(valueOf(MAX_RUNS));
		when(source.getCode()).thenReturn(RULE_CODE);
		when(source.getRuleGroupCode()).thenReturn(RULE_GROUP_CODE);
	}

	@Test
	public void testPopulate()
	{
		final RuleConfigurationRRD target = new RuleConfigurationRRD();
		populator.populate(source, target);

		assertEquals(RULE_CODE, target.getRuleCode());
		assertEquals(RULE_GROUP_CODE, target.getRuleGroupCode());
		assertEquals(valueOf(MAX_RUNS), valueOf(target.getMaxAllowedRuns()));
		assertEquals(valueOf(0), target.getCurrentRuns());
	}

	@Test
	public void testDefaultMaxRuns()
	{
		// force default value
		when(source.getMaxAllowedRuns()).thenReturn(null);

		final RuleConfigurationRRD target = new RuleConfigurationRRD();
		populator.populate(source, target);
		assertEquals(valueOf(RuleEngineServiceConstants.DEFAULT_MAX_ALLOWED_RUNS), target.getMaxAllowedRuns());
	}

	@Test
	public void testDefaultRuleGroupCode()
	{
		// force default value
		when(source.getRuleGroupCode()).thenReturn(null);

		final RuleConfigurationRRD target = new RuleConfigurationRRD();
		populator.populate(source, target);
		assertEquals(DEFAULT_RULE_GROUP_CODE, target.getRuleGroupCode());
	}
}
