package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionDefinitionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsRegistry;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterUuidGenerator;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import org.apache.commons.collections.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class DefaultRuleConditionsConverterTest
{
	private static final String JSON_EMPTY = "[]";
	private static final String JSON_SIMPLE = "[{\"definitionId\":\"condition\",\"parameters\":{\"param\":{\"value\":\"testabcd\"}}}]";
	private static final String JSON_COMPLEX = "[{\"children\":[{\"definitionId\":\"condition3\",\"parameters\":{\"param2\":{\"value\":\"testabcd\"}}}],\"definitionId\":\"condition1\",\"parameters\":{\"param1\":{\"value\":123}}},{\"definitionId\":\"condition2\"}]";

	private static final String STRING_VALUE = "testabcd";
	private static final String STRING_VALUE_JSON = "\"testabcd\"";

	private static final Integer INTEGER_VALUE = Integer.valueOf(123);
	private static final String INTEGER_VALUE_JSON = "123";

	@Mock
	private RuleConditionsRegistry ruleConditionsRegistry;

	@Mock
	private RuleParameterValueConverter ruleParameterValueConverter;

	@Mock
	private RuleParameterUuidGenerator ruleParameterUuidGenerator;

	@Mock
    private RuleConditionDefinitionService ruleConditionDefinitionService;

	@Mock
    private Converter<RuleConditionDefinitionModel, RuleConditionDefinitionData> conditionDefinitionDataConverter;

	private RuleConditionDefinitionModel ruleConditionDefinitionModel1 = new RuleConditionDefinitionModel();
	private RuleConditionDefinitionModel ruleConditionDefinitionModel2 = new RuleConditionDefinitionModel();
    private RuleConditionDefinitionModel ruleConditionDefinitionModel3 = new RuleConditionDefinitionModel();
	private RuleConditionDefinitionData ruleConditionDefinitionData = new RuleConditionDefinitionData();
    private RuleConditionDefinitionData ruleConditionDefinitionData2 = new RuleConditionDefinitionData();
    private Map<String, RuleParameterDefinitionData> parameters = new HashMap<>();
    private Map<String, RuleParameterDefinitionData> parameters2 = new HashMap<>();
	private DefaultRuleConditionsConverter ruleConditionsConverter;

    @Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(ruleParameterValueConverter.toString(STRING_VALUE)).thenReturn(STRING_VALUE_JSON);
		when(ruleParameterValueConverter.toString(INTEGER_VALUE)).thenReturn(INTEGER_VALUE_JSON);

		when(ruleParameterValueConverter.fromString(STRING_VALUE_JSON, String.class.getName())).thenReturn(STRING_VALUE);
		when(ruleParameterValueConverter.fromString(INTEGER_VALUE_JSON, Integer.class.getName())).thenReturn(INTEGER_VALUE);
        when(ruleConditionDefinitionService.findByCode("condition")).thenReturn(ruleConditionDefinitionModel1);
        when(ruleConditionDefinitionService.findByCode("condition1")).thenReturn(ruleConditionDefinitionModel1);
        when(ruleConditionDefinitionService.findByCode("condition2")).thenReturn(ruleConditionDefinitionModel2);
        when(ruleConditionDefinitionService.findByCode("condition3")).thenReturn(ruleConditionDefinitionModel3);

		ruleConditionsConverter = new DefaultRuleConditionsConverter(ruleParameterValueConverter, ruleParameterUuidGenerator);
        ruleConditionsConverter.setDebugMode(true);
		ruleConditionsConverter.afterPropertiesSet();
	}

	protected List<RuleConditionData> createRuleConditionsSimple()
	{
		final RuleParameterDefinitionData ruleParameterDefinition = new RuleParameterDefinitionData();
		ruleParameterDefinition.setType(String.class.getName());

		final RuleConditionDefinitionData ruleConditionDefinition = new RuleConditionDefinitionData();
		ruleConditionDefinition.setCode("condition");
		ruleConditionDefinition.setParameters(Collections.singletonMap("param", ruleParameterDefinition));

		when(ruleConditionsRegistry.getAllConditionDefinitionsAsMap()).thenReturn(
				Collections.singletonMap("condition", ruleConditionDefinition));

		final RuleParameterData ruleParameter = new RuleParameterData();
		ruleParameter.setValue(STRING_VALUE);

		final RuleConditionData ruleCondition = new RuleConditionData();
		ruleCondition.setDefinitionId("condition");
		ruleCondition.setParameters(Collections.singletonMap("param", ruleParameter));
        parameters.put("param", ruleParameterDefinition);
        ruleConditionDefinitionData.setParameters(parameters);

        when(conditionDefinitionDataConverter.convert(ruleConditionDefinitionModel1)).thenReturn(ruleConditionDefinitionData);
		return Arrays.asList(ruleCondition);
	}

	protected List<RuleConditionData> createRuleConditionsComplex()
	{
		final RuleParameterDefinitionData ruleParameterDefinition1 = new RuleParameterDefinitionData();
		ruleParameterDefinition1.setType(Integer.class.getName());

		final RuleParameterDefinitionData ruleParameterDefinition2 = new RuleParameterDefinitionData();
		ruleParameterDefinition2.setType(String.class.getName());

		final RuleConditionDefinitionData ruleConditionDefinition1 = new RuleConditionDefinitionData();
		ruleConditionDefinition1.setCode("condition1");
		ruleConditionDefinition1.setParameters(Collections.singletonMap("param1", ruleParameterDefinition1));

		final RuleConditionDefinitionData ruleConditionDefinition2 = new RuleConditionDefinitionData();
		ruleConditionDefinition2.setCode("condition2");

		final RuleConditionDefinitionData ruleConditionDefinition3 = new RuleConditionDefinitionData();
		ruleConditionDefinition3.setCode("condition3");
		ruleConditionDefinition3.setParameters(Collections.singletonMap("param2", ruleParameterDefinition2));

		final Map<String, RuleConditionDefinitionData> ruleConditionDefinitions = new HashMap<>();
		ruleConditionDefinitions.put("condition1", ruleConditionDefinition1);
		ruleConditionDefinitions.put("condition2", ruleConditionDefinition2);
		ruleConditionDefinitions.put("condition3", ruleConditionDefinition3);

		when(ruleConditionsRegistry.getAllConditionDefinitionsAsMap()).thenReturn(ruleConditionDefinitions);

		final RuleParameterData ruleParameter1 = new RuleParameterData();
		ruleParameter1.setValue(INTEGER_VALUE);

		final RuleParameterData ruleParameter2 = new RuleParameterData();
		ruleParameter2.setValue(STRING_VALUE);

		final RuleConditionData ruleCondition1 = new RuleConditionData();
		ruleCondition1.setDefinitionId("condition1");
		ruleCondition1.setParameters(Collections.singletonMap("param1", ruleParameter1));

		final RuleConditionData ruleCondition2 = new RuleConditionData();
		ruleCondition2.setDefinitionId("condition2");

		final RuleConditionData ruleCondition3 = new RuleConditionData();
		ruleCondition3.setParameters(Collections.singletonMap("param2", ruleParameter2));
		ruleCondition3.setDefinitionId("condition3");

		ruleCondition1.setChildren(Arrays.asList(ruleCondition3));
        parameters.put("param1", ruleParameterDefinition1);
        ruleConditionDefinitionData.setParameters(parameters);

        parameters2.put("param2", ruleParameterDefinition2);
        ruleConditionDefinitionData2.setParameters(parameters2);

        when(conditionDefinitionDataConverter.convert(ruleConditionDefinitionModel1)).thenReturn(ruleConditionDefinitionData);
        when(conditionDefinitionDataConverter.convert(ruleConditionDefinitionModel2)).thenReturn(new RuleConditionDefinitionData());
        when(conditionDefinitionDataConverter.convert(ruleConditionDefinitionModel3)).thenReturn(ruleConditionDefinitionData2);
		return Arrays.asList(ruleCondition1, ruleCondition2);
	}

	@Test
	public void convertToStringEmpty() {
		// given
		final List<RuleConditionData> ruleConditions = new ArrayList<>();

		// when
		final String value = ruleConditionsConverter.toString(ruleConditions, MapUtils.EMPTY_MAP);

		// then
		assertEquals(JSON_EMPTY, value);
	}

	@Test
	public void convertToStringSimple() {
		// given
		final List<RuleConditionData> ruleConditions = createRuleConditionsSimple();

		// when
		final String value = ruleConditionsConverter.toString(ruleConditions, ruleConditionsRegistry.getAllConditionDefinitionsAsMap());

		// then
		assertEquals(JSON_SIMPLE, value);
	}

	@Test
	public void convertToStringComplex() {
		// given
		final List<RuleConditionData> ruleConditions = createRuleConditionsComplex();

		// when
		final String value = ruleConditionsConverter.toString(ruleConditions, ruleConditionsRegistry.getAllConditionDefinitionsAsMap());

		// then
		assertEquals(JSON_COMPLEX, value);
	}

	@Test
	public void convertFromStringEmpty() {
		// given
		final List<RuleConditionData> expectedValue = new ArrayList<>();

		// when
		final List<RuleConditionData> value = ruleConditionsConverter.fromString(JSON_EMPTY, ruleConditionsRegistry.getAllConditionDefinitionsAsMap());

		// then
		assertEquals(expectedValue, value);
	}

	@Test
	public void convertFromStringSimple() {
		// given
		final List<RuleConditionData> expectedRuleConditions = createRuleConditionsSimple();

		// when
		final List<RuleConditionData> ruleConditions = ruleConditionsConverter.fromString(JSON_SIMPLE, ruleConditionsRegistry.getAllConditionDefinitionsAsMap());

		// then
		assertTrue(isSameConditions(expectedRuleConditions, ruleConditions));
	}

	@Test
	public void convertFromStringComplex() {
		// given
		final List<RuleConditionData> expectedRuleConditions = createRuleConditionsComplex();

		// when
		final List<RuleConditionData> ruleConditions = ruleConditionsConverter.fromString(JSON_COMPLEX, ruleConditionsRegistry.getAllConditionDefinitionsAsMap());

		// then
		assertTrue(isSameConditions(expectedRuleConditions, ruleConditions));
	}

	protected boolean isSameConditions(final List<RuleConditionData> ruleConditions1, final List<RuleConditionData> ruleConditions2)
	{
		if (ruleConditions1 == ruleConditions2) // NOPMD
		{
			return true;
		}

		if (ruleConditions1.size() != ruleConditions2.size())
		{
			return false;
		}

		final int size = ruleConditions1.size();

		for (int index = 0; index < size; index++)
		{
			final RuleConditionData ruleCondition1 = ruleConditions1.get(index);
			final RuleConditionData ruleCondition2 = ruleConditions2.get(index);

			if (!isSameCondition(ruleCondition1, ruleCondition2))
			{
				return false;
			}
		}

		return true;
	}

	protected boolean isSameCondition(final RuleConditionData ruleCondition1, final RuleConditionData ruleCondition2)
	{
		return Objects.equals(ruleCondition1.getDefinitionId(), ruleCondition2.getDefinitionId())
				&& isSameConditions(ruleCondition1.getChildren(), ruleCondition2.getChildren())
				&& isSameParameters(ruleCondition1.getParameters(), ruleCondition2.getParameters());
	}


	protected boolean isSameParameters(final Map<String, RuleParameterData> parameters1,
			final Map<String, RuleParameterData> parameters2)
	{
		if (Objects.equals(parameters1, parameters2))
		{
			return true;
		}

		if (MapUtils.isEmpty(parameters1) && MapUtils.isEmpty(parameters2))
		{
			return true;
		}

		if (parameters1.size() != parameters2.size())
		{
			return false;
		}

		for (final Entry<String, RuleParameterData> entry : parameters1.entrySet())
		{
			final RuleParameterData ruleParameter1 = entry.getValue();
			final RuleParameterData ruleParameter2 = parameters2.get(entry.getKey());

			if (!Objects.equals(ruleParameter1.getValue(), ruleParameter2.getValue()))
			{
				return false;
			}
		}

		return true;
	}
}
