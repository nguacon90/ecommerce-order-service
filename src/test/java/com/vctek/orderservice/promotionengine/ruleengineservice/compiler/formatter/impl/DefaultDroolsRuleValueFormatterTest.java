package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.formatter.impl;

import com.google.common.collect.Maps;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleGeneratorContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


public class DefaultDroolsRuleValueFormatterTest
{
	private DefaultDroolsRuleValueFormatter formatter = new DefaultDroolsRuleValueFormatter();

	@Mock
	private DroolsRuleGeneratorContext droolsRuleGeneratorContext;

	@Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        formatter.initFormatters();
    }

	@Test
	public void shouldConsiderEmptyMapAsNullValue()
	{
		assertThat(formatter.formatValue(droolsRuleGeneratorContext, Maps.newHashMap())).isEqualTo("null");
	}

	@Test
	public void shouldConsiderEmptyCollectionAsNullValue()
	{
		assertThat(formatter.formatValue(droolsRuleGeneratorContext, Collections.emptyList())).isEqualTo("null");
	}

    @Test
    public void formatBooleanValue()
    {
        assertThat(formatter.formatValue(droolsRuleGeneratorContext, Boolean.TRUE)).isEqualTo("Boolean.TRUE");
    }
}
