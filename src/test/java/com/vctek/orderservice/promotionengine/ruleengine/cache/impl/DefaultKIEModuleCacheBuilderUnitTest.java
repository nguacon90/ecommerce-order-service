package com.vctek.orderservice.promotionengine.ruleengine.cache.impl;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.util.Map2StringUtils;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleGlobalsBeanProvider;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;


public class DefaultKIEModuleCacheBuilderUnitTest
{

	private static final String MODULE_NAME = "MODULE_NAME";

	@Mock
	private DroolsKIEModuleModel kieModule1;

	@Mock
	private DroolsRuleModel rule1;
	@Mock
	private DroolsRuleModel rule2;
	@Mock
	private DroolsKIEBaseModel kieBase1;
	@Mock
	private RuleGlobalsBeanProvider ruleGlobalsBeanProvider;


	private DefaultKIEModuleCacheBuilder cacheBuilder;

	private final Map<String, String> rule1Globals = new HashMap<>();
	private final Map<String, String> rule2Globals = new HashMap<>();

	@Before
	public void setup()
	{
        MockitoAnnotations.initMocks(this);
		addGlobals(3, "rule1-", rule1Globals);
        when(rule1.getKieBase()).thenReturn(kieBase1);
		when(rule1.getGlobals()).thenReturn(Map2StringUtils.mapToString(rule1Globals));
		when(rule1.getCode()).thenReturn("rule1");
		setupBeanLookups(rule1Globals);

		when(rule2.getKieBase()).thenReturn(kieBase1);
		when(rule2.getGlobals()).thenReturn(Map2StringUtils.mapToString(rule2Globals));
		when(rule2.getCode()).thenReturn("rule2");
		when(kieModule1.getId()).thenReturn(2l);
		when(kieModule1.getName()).thenReturn(MODULE_NAME);
		when(kieBase1.getDroolsKIEModule()).thenReturn(kieModule1);

		cacheBuilder = new DefaultKIEModuleCacheBuilder(ruleGlobalsBeanProvider, kieModule1, true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullCacheKey()
	{
		new DefaultKIEModuleCacheBuilder(ruleGlobalsBeanProvider, null, true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testProcessNullRule()
	{
		cacheBuilder.processRule(null);
	}

	@Test(expected = IllegalStateException.class)
	public void testProcessRuleWithNullKieBase()
	{
		when(rule1.getKieBase()).thenReturn(null);
		cacheBuilder.processRule(rule1);
	}

	@Test
	public void testProcessRule()
	{
		// adding the rules globals to the cache
		cacheBuilder.processRule(rule1);
		final Map<Object, Map<String, Object>> globalsCache = cacheBuilder.getGlobalsCache();
		assertNotNull(globalsCache);
		assertEquals(1, globalsCache.size());
		final Map<String, Object> kieBaseGlobals = cacheBuilder.getCachedGlobalsForKieBase(kieBase1);
		assertEquals(3, kieBaseGlobals.size());
		assertTrue(kieBaseGlobals.containsKey("rule1-1"));
		assertTrue(kieBaseGlobals.containsKey("rule1-2"));
		assertTrue(kieBaseGlobals.containsKey("rule1-3"));
		assertTrue(kieBaseGlobals.containsValue("rule1-1"));
		assertTrue(kieBaseGlobals.containsValue("rule1-2"));
		assertTrue(kieBaseGlobals.containsValue("rule1-3"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testProcessRuleWithMismatchingGlobals()
	{
		// adding the rules globals to the cache
		cacheBuilder.processRule(rule1);

		// rule2 has a different type for the same key rule1-1
		rule2Globals.put("rule1-1", "rule2mismatch");
        when(rule2.getGlobals()).thenReturn(Map2StringUtils.mapToString(rule2Globals));
		when(ruleGlobalsBeanProvider.getRuleGlobals("rule2mismatch")).thenReturn(Integer.MIN_VALUE);

		cacheBuilder.processRule(rule2);

	}

	protected void addGlobals(final int count, final String rule, final Map<String, String> globals)
	{
		for (int i = 1; i <= count; i++)
		{
			globals.put(rule + i, rule + i);
		}
	}

	protected void setupBeanLookups(final Map<String, String> globals)
	{
		globals.forEach((k, v) ->
		{
			when(ruleGlobalsBeanProvider.getRuleGlobals(v)).thenReturn(v);
		});
	}
}
