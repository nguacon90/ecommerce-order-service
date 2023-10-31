package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionDefinitionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsRegistry;
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


public class DefaultRuleActionsConverterTest
{
	private static final String JSON_EMPTY = "[]";
	private static final String JSON_SIMPLE = "[{\"definitionId\":\"action\",\"parameters\":{\"param\":{\"value\":\"testabcd\"}}}]";
	private static final String JSON_COMPLEX = "[{\"definitionId\":\"action1\",\"parameters\":{\"param1\":{\"value\":123}}},{\"definitionId\":\"action2\"}]";

	private static final String STRING_VALUE = "testabcd";
	private static final String STRING_VALUE_JSON = "\"testabcd\"";

	private static final Integer INTEGER_VALUE = Integer.valueOf(123);
	private static final String INTEGER_VALUE_JSON = "123";

    @Mock
    private RuleActionsRegistry ruleActionsRegistry;

	@Mock
	private RuleParameterValueConverter ruleParameterValueConverter;

	@Mock
	private RuleParameterUuidGenerator ruleParameterUuidGenerator;

	@Mock
    private RuleActionDefinitionService ruleActionDefinitionService;

    @Mock
    private Converter<RuleActionDefinitionModel, RuleActionDefinitionData> actionDefinitionConverter;

	private DefaultRuleActionsConverter ruleActionsConverter;

	private RuleActionDefinitionData ruleActionDefinitionData = new RuleActionDefinitionData();

    private Map<String, RuleParameterDefinitionData> parameters = new HashMap<>();

    private RuleActionDefinitionModel ruleActionDefinitionModel1  = new RuleActionDefinitionModel();
    private RuleActionDefinitionModel ruleActionDefinitionModel2  = new RuleActionDefinitionModel();

    @Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(ruleParameterValueConverter.toString(STRING_VALUE)).thenReturn(STRING_VALUE_JSON);
		when(ruleParameterValueConverter.toString(INTEGER_VALUE)).thenReturn(INTEGER_VALUE_JSON);

		when(ruleParameterValueConverter.fromString(STRING_VALUE_JSON, String.class.getName())).thenReturn(STRING_VALUE);
		when(ruleParameterValueConverter.fromString(INTEGER_VALUE_JSON, Integer.class.getName())).thenReturn(INTEGER_VALUE);
        ruleActionDefinitionModel1.setCode("action1");
        ruleActionDefinitionModel2.setCode("action2");
		when(ruleActionDefinitionService.findByCode("action")).thenReturn(ruleActionDefinitionModel1);
		when(ruleActionDefinitionService.findByCode("action1")).thenReturn(ruleActionDefinitionModel1);
		when(ruleActionDefinitionService.findByCode("action2")).thenReturn(ruleActionDefinitionModel2);
		ruleActionsConverter = new DefaultRuleActionsConverter(ruleParameterValueConverter, ruleParameterUuidGenerator);
		ruleActionsConverter.afterPropertiesSet();
	}

	protected List<RuleActionData> createRuleActionsSimple()
	{
		final RuleParameterDefinitionData ruleParameterDefinition = new RuleParameterDefinitionData();
		ruleParameterDefinition.setType(String.class.getName());

		final RuleActionDefinitionData ruleActionDefinition = new RuleActionDefinitionData();
		ruleActionDefinition.setCode("action");
		ruleActionDefinition.setParameters(Collections.singletonMap("param", ruleParameterDefinition));
        when(ruleActionsRegistry.getAllActionDefinitionsAsMap()).thenReturn(
                Collections.singletonMap("action", ruleActionDefinition));

		final RuleParameterData ruleParameter = new RuleParameterData();
		ruleParameter.setValue(STRING_VALUE);

		final RuleActionData ruleAction = new RuleActionData();
		ruleAction.setDefinitionId("action");
		ruleAction.setParameters(Collections.singletonMap("param", ruleParameter));

        return Arrays.asList(ruleAction);
	}

	protected List<RuleActionData> createRuleActionsComplex()
	{
		final RuleParameterDefinitionData ruleParameterDefinition1 = new RuleParameterDefinitionData();
		ruleParameterDefinition1.setType(Integer.class.getName());

        final RuleActionDefinitionData ruleActionDefinition1 = new RuleActionDefinitionData();
        ruleActionDefinition1.setCode("action1");
        ruleActionDefinition1.setParameters(Collections.singletonMap("param1", ruleParameterDefinition1));

        final RuleActionDefinitionData ruleActionDefinition2 = new RuleActionDefinitionData();
        ruleActionDefinition2.setCode("action2");

        final Map<String, RuleActionDefinitionData> ruleActionDefinitions = new HashMap<>();
        ruleActionDefinitions.put("action1", ruleActionDefinition1);
        ruleActionDefinitions.put("action2", ruleActionDefinition2);

        when(ruleActionsRegistry.getAllActionDefinitionsAsMap()).thenReturn(ruleActionDefinitions);

		final RuleParameterData ruleParameter1 = new RuleParameterData();
		ruleParameter1.setValue(INTEGER_VALUE);

		final RuleActionData ruleAction1 = new RuleActionData();
		ruleAction1.setDefinitionId("action1");
		ruleAction1.setParameters(Collections.singletonMap("param1", ruleParameter1));

		final RuleActionData ruleAction2 = new RuleActionData();
		ruleAction2.setDefinitionId("action2");

        parameters.put("param1", ruleParameterDefinition1);

		return Arrays.asList(ruleAction1, ruleAction2);
	}

	@Test
	public void convertToStringEmpty() {
		// given
		final List<RuleActionData> actions = new ArrayList<>();

		// when
		final String value = ruleActionsConverter.toString(actions, MapUtils.EMPTY_MAP);

		// then
		assertEquals(JSON_EMPTY, value);
	}

	@Test
	public void convertToStringSimple() {
		// given
		final List<RuleActionData> ruleActions = createRuleActionsSimple();

		// when
		final String value = ruleActionsConverter.toString(ruleActions, ruleActionsRegistry.getAllActionDefinitionsAsMap());

		// then
		assertEquals(JSON_SIMPLE, value);
	}

	@Test
	public void convertToStringComplex() {
		// given
		final List<RuleActionData> ruleActions = createRuleActionsComplex();

		// when
		final String value = ruleActionsConverter.toString(ruleActions, ruleActionsRegistry.getAllActionDefinitionsAsMap());

		// then
		assertEquals(JSON_COMPLEX, value);
	}

	@Test
	public void convertFromStringEmpty() {
		// given
		final List<RuleActionData> expectedRuleActions = new ArrayList<>();

		// when
		final List<RuleActionData> ruleActions = ruleActionsConverter.fromString(JSON_EMPTY, ruleActionsRegistry.getAllActionDefinitionsAsMap());

		// then
		assertEquals(expectedRuleActions, ruleActions);
	}

	@Test
	public void convertFromStringSimple() {
		// given
		final List<RuleActionData> expectedRuleActions = createRuleActionsSimple();

		// when
		final List<RuleActionData> ruleActions = ruleActionsConverter.fromString(JSON_SIMPLE, ruleActionsRegistry.getAllActionDefinitionsAsMap());

		// then
		assertTrue(isSameActions(expectedRuleActions, ruleActions));
	}

	@Test
	public void convertFromStringComplex() {
		// given
		final List<RuleActionData> expectedRuleActions = createRuleActionsComplex();

		// when
		final List<RuleActionData> ruleActions = ruleActionsConverter.fromString(JSON_COMPLEX, ruleActionsRegistry.getAllActionDefinitionsAsMap());

		// then
		assertTrue(isSameActions(expectedRuleActions, ruleActions));
	}

	protected boolean isSameActions(final List<RuleActionData> ruleActions1, final List<RuleActionData> ruleActions2)
	{
		if (ruleActions1 == ruleActions2) // NOPMD
		{
			return true;
		}

		if (ruleActions1.size() != ruleActions2.size())
		{
			return false;
		}

		final int size = ruleActions1.size();

		for (int index = 0; index < size; index++)
		{
			final RuleActionData ruleAction1 = ruleActions1.get(index);
			final RuleActionData ruleAction2 = ruleActions2.get(index);

			if (!isSameAction(ruleAction1, ruleAction2))
			{
				return false;
			}
		}

		return true;
	}

	protected boolean isSameAction(final RuleActionData ruleAction1, final RuleActionData ruleAction2)
	{
		return Objects.equals(ruleAction1.getDefinitionId(), ruleAction2.getDefinitionId())
				&& isSameParameters(ruleAction1.getParameters(), ruleAction2.getParameters());
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
