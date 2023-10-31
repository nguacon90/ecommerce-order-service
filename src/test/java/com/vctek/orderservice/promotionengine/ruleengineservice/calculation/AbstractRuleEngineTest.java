package com.vctek.orderservice.promotionengine.ruleengineservice.calculation;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.impl.DefaultMinimumAmountValidationStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.impl.DefaultRuleEngineCalculationService;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl.CalculationStrategies;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl.DefaultRoundingStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl.DefaultTaxRoundingStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.AbstractOrderRaoToCurrencyConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.AbstractOrderRaoToOrderConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.OrderEntryRao2LineItemConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.util.DefaultRaoService;
import com.vctek.orderservice.promotionengine.ruleengineservice.util.OrderUtils;
import com.vctek.orderservice.promotionengine.ruleengineservice.util.RaoUtils;
import com.vctek.orderservice.promotionengine.util.CurrencyIsoCode;
import com.vctek.orderservice.service.ModelService;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;


public abstract class AbstractRuleEngineTest
{
    @Mock
    private ModelService modelService;
	protected static final String VND = CurrencyIsoCode.VND.toString();
    private DefaultRaoService raoService;
	private AbstractOrderRaoToOrderConverter cartRaoToOrderConverter;
	private DefaultRuleEngineCalculationService ruleEngineCalculationService;
	private AbstractOrderRaoToCurrencyConverter abstractOrderRaoToCurrencyConverter;
	private static int entryNumber = 0;

	@Before
	public void setUpAbstractRuleEngineTest()
	{
        MockitoAnnotations.initMocks(this);
		final RaoUtils raoUtils = new RaoUtils();

		final OrderUtils orderUtils = new OrderUtils();
		orderUtils.setModelService(modelService);
        raoService = new DefaultRaoService();
		abstractOrderRaoToCurrencyConverter = newAbstractOrderRaoToCurrencyConverter();
		final OrderEntryRao2LineItemConverter orderEntryRaoToNumberedLineItemConverter = newOrderEntryRaoToNumberedLineItemConverter(
				abstractOrderRaoToCurrencyConverter);
		cartRaoToOrderConverter = newCartRaoToOrderConverter(orderEntryRaoToNumberedLineItemConverter,
				abstractOrderRaoToCurrencyConverter, orderUtils, raoUtils);
//		unitForBundleSelectorStrategies = newUnitForBundleSelectorStrategies();
//		final AbstractPopulatingConverter<ProductModel, ProductRAO> productRaoConverter = new AbstractPopulatingConverter<ProductModel, ProductRAO>();
//		productRaoConverter.setPopulators(Collections.singletonList(new ProductRaoPopulator()));
//		productRaoConverter.setTargetClass(ProductRAO.class);
		final MinimumAmountValidationStrategy minimumAmountValidationStrategy = newMinimumAmountValidationStrategy();
		ruleEngineCalculationService = newRuleEngineCalculationService(cartRaoToOrderConverter, minimumAmountValidationStrategy, orderUtils, raoUtils);
	}

	private MinimumAmountValidationStrategy newMinimumAmountValidationStrategy()
	{
		return new DefaultMinimumAmountValidationStrategy();
	}

	private DefaultRuleEngineCalculationService newRuleEngineCalculationService(
			final AbstractOrderRaoToOrderConverter abstractOrderRaoToOrderConverter,
			final MinimumAmountValidationStrategy minimumAmountValidationStrategy, final OrderUtils orderUtils,
			final RaoUtils raoUtils)

	{
		return new DefaultRuleEngineCalculationService(abstractOrderRaoToOrderConverter,
                raoUtils, minimumAmountValidationStrategy);
//		ruleEngineCalculationService.setMinimumAmountValidationStrategy(minimumAmountValidationStrategy);
//		ruleEngineCalculationService.setOrderUtils(orderUtils);
//		ruleEngineCalculationService.setRaoUtils(raoUtils);
//		return ruleEngineCalculationService;
	}

//	private Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> newUnitForBundleSelectorStrategies()
//	{
//		final Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> result = new EnumMap<>(OrderEntrySelectionStrategy.class);
//		result.put(OrderEntrySelectionStrategy.CHEAPEST, new DefaultEntriesSelectionStrategy());
//		return result;
//	}

	private AbstractOrderRaoToOrderConverter newCartRaoToOrderConverter(
            final OrderEntryRao2LineItemConverter orderEntryRaoToNumberedLineItemConverter,
            final AbstractOrderRaoToCurrencyConverter cartRaoToCurrencyConverter, final OrderUtils orderUtils, final RaoUtils raoUtils)
	{
		final CalculationStrategies calculationStrategies = new CalculationStrategies(new DefaultRoundingStrategy(),
                new DefaultTaxRoundingStrategy());
		final AbstractOrderRaoToOrderConverter cartRaoToOrderConverter = new AbstractOrderRaoToOrderConverter(raoUtils, calculationStrategies,
                orderEntryRaoToNumberedLineItemConverter, cartRaoToCurrencyConverter);
//		cartRaoToOrderConverter.setOrderEntryRaoToNumberedLineItemConverter(orderEntryRaoToNumberedLineItemConverter);
		cartRaoToOrderConverter.setOrderUtils(orderUtils);
		return cartRaoToOrderConverter;
	}

	private OrderEntryRao2LineItemConverter newOrderEntryRaoToNumberedLineItemConverter(
			final AbstractOrderRaoToCurrencyConverter cartRaoToCurrencyConverter)
	{
		return new OrderEntryRao2LineItemConverter(cartRaoToCurrencyConverter);
	}

	private AbstractOrderRaoToCurrencyConverter newAbstractOrderRaoToCurrencyConverter()
	{
		return new AbstractOrderRaoToCurrencyConverter();
	}

	protected Set<OrderEntryRAO> set(final OrderEntryRAO... entries)
	{
		return new LinkedHashSet<>(Arrays.asList(entries));
	}

	protected CartRAO createCartRAO(final String code, final String currencyIso)
	{
		final CartRAO cart = raoService.createCart();
		cart.setCode(code);
		cart.setCurrencyIsoCode(currencyIso);
		return cart;
	}

	protected OrderEntryRAO createOrderEntryRAO(final AbstractOrderRAO order, final String basePrice, final String currencyIso,
                                                final int quantity)
	{
		return createOrderEntryRAO(order, basePrice, currencyIso, quantity, ++entryNumber);
	}

	protected OrderEntryRAO createOrderEntryRAO(final String basePrice, final String currencyIso, final int quantity,
			final int entryNumber)
	{
		return createOrderEntryRAO(null, basePrice, currencyIso, quantity, entryNumber);
	}

	protected OrderEntryRAO createOrderEntryRAO(final AbstractOrderRAO order, final String basePrice, final String currencyIso,
			final int quantity, final int entryNumber)
	{
		final OrderEntryRAO entry = raoService.createOrderEntry();
		entry.setOrder(order);
		entry.setCurrencyIsoCode(currencyIso);
		entry.setEntryNumber(Integer.valueOf(entryNumber));
		final ProductRAO product = new ProductRAO();
		product.setId(22321l);
		entry.setBasePrice(new BigDecimal(basePrice));
		entry.setQuantity(quantity);
		entry.setProduct(product);
		return entry;
	}

	protected DiscountRAO createDiscount(final AbstractActionedRAO discountFor, final String discountValue,
                                         final String currencyIso)
	{
		final DiscountRAO discountRAO = new DiscountRAO();
		discountRAO.setCurrencyIsoCode(currencyIso);
		discountRAO.setValue(new BigDecimal(discountValue));
		discountFor.getActions().add(discountRAO);
		return discountRAO;
	}

//	protected EntriesSelectionStrategyRPD createEntriesSelectionStrategyRPD(final OrderEntrySelectionStrategy strategy,
//			final int quantity, final boolean isTargetOfAction, final OrderEntryRAO... orderEntryRAO)
//	{
//		final EntriesSelectionStrategyRPD rao = raoService.createEntriesSelectionStrategyRPD();
//		rao.setSelectionStrategy(strategy);
//		rao.setQuantity(quantity);
//		rao.setOrderEntries(Arrays.asList(orderEntryRAO));
//		rao.setTargetOfAction(isTargetOfAction);
//		return rao;
//	}

	protected DefaultRuleEngineCalculationService getRuleEngineCalculationService()
	{
		return ruleEngineCalculationService;
	}

	public void setRuleEngineCalculationService(final DefaultRuleEngineCalculationService ruleEngineCalculationService)
	{
		this.ruleEngineCalculationService = ruleEngineCalculationService;
	}

//	protected DefaultRaoService getRaoService()
//	{
//		return raoService;
//	}
//
//	public void setRaoService(final DefaultRaoService raoService)
//	{
//		this.raoService = raoService;
//	}

	protected AbstractOrderRaoToOrderConverter getCartRaoToOrderConverter()
	{
		return cartRaoToOrderConverter;
	}

	public void setCartRaoToOrderConverter(final AbstractOrderRaoToOrderConverter cartRaoToOrderConverter)
	{
		this.cartRaoToOrderConverter = cartRaoToOrderConverter;
	}

	protected AbstractOrderRaoToCurrencyConverter getAbstractOrderRaoToCurrencyConverter()
	{
		return abstractOrderRaoToCurrencyConverter;
	}

	public void setAbstractOrderRaoToCurrencyConverter(
			final AbstractOrderRaoToCurrencyConverter abstractOrderRaoToCurrencyConverter)
	{
		this.abstractOrderRaoToCurrencyConverter = abstractOrderRaoToCurrencyConverter;
	}

//	protected Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> getUnitForBundleSelectorStrategies()
//	{
//		return unitForBundleSelectorStrategies;
//	}
//
//	public void setUnitForBundleSelectorStrategies(
//			final Map<OrderEntrySelectionStrategy, EntriesSelectionStrategy> unitForBundleSelectorStrategies)
//	{
//		this.unitForBundleSelectorStrategies = unitForBundleSelectorStrategies;
//	}
}
