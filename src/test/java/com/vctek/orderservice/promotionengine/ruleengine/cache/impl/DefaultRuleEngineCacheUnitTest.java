package com.vctek.orderservice.promotionengine.ruleengine.cache.impl;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.util.Map2StringUtils;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleGlobalsBeanProvider;
import com.vctek.orderservice.promotionengine.ruleengine.model.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DefaultRuleEngineCacheUnitTest
{
	private static final String MODULE_NAME = "MODULE_NAME";

	private DefaultRuleEngineCache cache;

	@Mock
	private DroolsKIEModuleModel kieModule;

	@Mock
	private DroolsKIEBaseModel kieBase;

	@Mock
	private DroolsKIESessionModel kieSession;

	@Mock
	private RuleGlobalsBeanProvider ruleGlobalsBeanProvider;

	@Mock
	private DroolsRuleModel rule;

	private final Object global1 = new Object();
	private final Object global2 = new Object();
	private final String global1Ref = "global1";
	private final String global2Ref = "global2";
	private final String global1BeanName = "bean1";
	private final String global2BeanName = "bean2";


	@Before
	public void setup()
	{
	    MockitoAnnotations.initMocks(this);
		cache = new DefaultRuleEngineCache(ruleGlobalsBeanProvider);
		when(kieBase.getDroolsKIEModule()).thenReturn(kieModule);
		when(kieSession.getDroolsKIEBase()).thenReturn(kieBase);
		when(rule.getKieBase()).thenReturn(kieBase);

		final Map<String, String> rule1Globals = new HashMap<>();
		rule1Globals.put(global1Ref, global1BeanName);
		rule1Globals.put(global2Ref, global2BeanName);
		when(rule.getGlobals()).thenReturn(Map2StringUtils.mapToString(rule1Globals));
		when(ruleGlobalsBeanProvider.getRuleGlobals(global1BeanName)).thenReturn(global1);
		when(ruleGlobalsBeanProvider.getRuleGlobals(global2BeanName)).thenReturn(global2);

		when(kieModule.getName()).thenReturn(MODULE_NAME);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddKIEModuleCacheWithNullCache()
	{
		cache.addKIEModuleCache(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddKIEModuleCacheWithWrongCacheType()
	{
		cache.addKIEModuleCache(new KIEModuleCacheBuilder()
		{

			@Override
			public <T extends AbstractRuleEngineRuleModel> void processRule(final T rule)
			{
				// do nothing
			}

		});
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateKIEModuleCacheWithNullValue()
	{
		cache.createKIEModuleCacheBuilder(null);
	}

	@Test
	public void testCreateKIEModuleCacheContainer()
	{
		final KIEModuleCacheBuilder cacheBuilder = cache.createKIEModuleCacheBuilder(kieModule);
		assertTrue(cacheBuilder instanceof DefaultKIEModuleCacheBuilder);
	}

	@Test
	public void testAddToCacheAndProvideGlobals()
	{
		final KIEModuleCacheBuilder cacheBuilder = cache.createKIEModuleCacheBuilder(kieModule);
		cacheBuilder.processRule(rule);
		cache.addKIEModuleCache(cacheBuilder);

		final RuleEvaluationContext context = new RuleEvaluationContext();
		final DroolsRuleEngineContextModel engineContext = mock(DroolsRuleEngineContextModel.class);
		context.setRuleEngineContext(engineContext);
		when(engineContext.getKieSession()).thenReturn(kieSession);
		final Map<String, Object> globals = cache.getGlobalsForKIEBase(kieBase);

		assertNotNull(globals);
		assertEquals(2, globals.size());
		assertTrue(globals.containsKey(global1Ref));
		assertTrue(globals.containsKey(global2Ref));
		assertTrue(globals.containsValue(global1));
		assertTrue(globals.containsValue(global2));
	}

}
