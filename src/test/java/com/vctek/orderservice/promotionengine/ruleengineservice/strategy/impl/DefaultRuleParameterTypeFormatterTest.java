package com.vctek.orderservice.promotionengine.ruleengineservice.strategy.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;


public class DefaultRuleParameterTypeFormatterTest
{
	private DefaultRuleParameterTypeFormatter defaultRuleParameterTypeFormatter;

	@Before
	public void init()
	{
		final HashMap paramMap = new HashMap();
		paramMap.put("ItemType\\((.*)\\)", "Reference(%s)");
		paramMap.put("Map\\((.+)\\,\\s*(.+)\\)", "Map(%s,%s)");
		paramMap.put("List\\((.+)\\)", "List(%s)");
		defaultRuleParameterTypeFormatter = new DefaultRuleParameterTypeFormatter(paramMap);
	}

	@Test
	public void testEmptyType()
	{
		//Given
		final String paramType = "";

		//When
		final String result = defaultRuleParameterTypeFormatter.formatParameterType(paramType);

		//Then
		assertEquals(result, "java.lang.String");
	}

	@Test
	public void testMapTypes()
	{
		//Given
		final String paramType = "Map(Currency, ItemType(Product))";

		//When
		final String result = defaultRuleParameterTypeFormatter.formatParameterType(paramType);

		//Then
		assertEquals(result, "Map(Currency,Reference(Product))");
	}

	@Test
	public void testComplexMapTypes()
	{
		//Given
		final String paramType = "Map(ItemType(Currency), ItemType(Product))";

		//When
		final String result = defaultRuleParameterTypeFormatter.formatParameterType(paramType);

		//Then
		assertEquals(result, "Map(Reference(Currency),Reference(Product))");
	}

	@Test
	public void testComplexListTypes()
	{
		//Given
		final String paramType = "List(ItemType(Currency))";

		//When
		final String result = defaultRuleParameterTypeFormatter.formatParameterType(paramType);

		//Then
		assertEquals(result, "List(Reference(Currency))");
	}

	@Test
	public void testConfigurableType()
	{
		//Given
		final String paramType = "ItemType(Product)";

		//When
		final String result = defaultRuleParameterTypeFormatter.formatParameterType(paramType);

		//Then
		assertEquals(result, "Reference(Product)");
	}

	@Test
	public void testNormalType()
	{
		//Given
		final String paramType = "java.lang.Integer";

		//When
		final String result = defaultRuleParameterTypeFormatter.formatParameterType(paramType);

		//Then
		assertEquals(result, "java.lang.Integer");
	}
}
