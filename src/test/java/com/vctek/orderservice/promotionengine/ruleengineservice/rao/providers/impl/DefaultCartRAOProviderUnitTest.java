package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl;

import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.RuleEngineCalculationService;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.CartRaoConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.OrderEntryRAOConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator.CartRaoPopulator;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator.OrderEntryRaoPopulator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOFactsExtractor;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.util.CartTestContextBuilder;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.CustomerService;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.redis.ProductData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl.DefaultCartRAOProvider.*;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


public class DefaultCartRAOProviderUnitTest
{
	private final static String PAYMENT_MODE_CODE = "paymentCode";
	private DefaultCartRAOProvider cartRAOProvider;
	private CartTestContextBuilder cartContext;
	private CartRaoPopulator cartRaoPopulator;
    private CartRaoConverter cartRaoConverter;
    private RuleEngineCalculationService ruleEngineCalculationService;
    private OrderEntryRAOConverter orderEntryConverter;
    private ProductSearchService productSearchService;
    private ProductData productData;
    private List<RAOFactsExtractor> raoExtractors = new ArrayList<>();
    @Mock
    private CartService cartService;
    @Mock
    private EntryRepository entryRepository;
    @Mock
    private ProductSearchModel productSearchModel;
    @Mock
    private CustomerService customerService;

    @Before
	public void setUp() {
        MockitoAnnotations.initMocks(this);
        productSearchService = mock(ProductSearchService.class);
        productData = mock(ProductData.class);
        ruleEngineCalculationService = mock(RuleEngineCalculationService.class);
		cartContext = createNewCartTestContextBuilder();
        cartRaoConverter = new CartRaoConverter();
        orderEntryConverter = new OrderEntryRAOConverter();
        final OrderEntryRaoPopulator orderEntryPopulator = new OrderEntryRaoPopulator();
        orderEntryPopulator.setProductSearchService(productSearchService);
        orderEntryConverter.setTargetClass(OrderEntryRAO.class);
        orderEntryConverter.setPopulators(orderEntryPopulator);
		cartRaoPopulator = new CartRaoPopulator();
		cartRaoPopulator.setOrderEntryRaoPopulator(orderEntryPopulator);
        cartRaoPopulator.setCartService(cartService);
        cartRaoConverter.setTargetClass(CartRAO.class);
        cartRaoConverter.setPopulators(cartRaoPopulator);
        cartRaoPopulator.setEntryRepository(entryRepository);
        cartRaoPopulator.setCustomerService(customerService);
//		cartRaoPopulator.setPaymentModeConverter(createNewConverter(PaymentModeRAO.class, new PaymentModeRaoPopulator()));
//		cartRaoPopulator.setDiscountConverter(createNewConverter(DiscountValueRAO.class, new DiscountValueRaoPopulator()));
//		final ProductRaoPopulator productRaoPopulator = new ProductRaoPopulator();
//		productRaoPopulator.setCategoryConverter(createNewConverter(CategoryRAO.class, new CategoryRaoPopulator()));
//		productRaoPopulator.setCategoryService(mock(CategoryService.class));
//		orderEntryPopulator.setProductConverter(createNewConverter(ProductRAO.class, productRaoPopulator));
//		cartRaoPopulator.setEntryConverter(createNewConverter(OrderEntryRAO.class, orderEntryPopulator));
//		final UserRaoPopulator userRaoPopulator = new UserRaoPopulator();
//		userRaoPopulator.setAuthService(userService);
//		userRaoPopulator.setUserGroupConverter(createNewConverter(UserGroupRAO.class, new UserGroupRaoPopulator()));
//		cartRaoPopulator.setUserConverter(createNewConverter(UserRAO.class, userRaoPopulator));
//
//		final AbstractPopulatingConverter<AbstractOrderModel, CartRAO> cartRaoConverter = new AbstractPopulatingConverter<>();
//		cartRaoConverter.setTargetClass(CartRAO.class);
//		cartRaoConverter.setPopulators(getCartPopulators());
        when(cartService.isValidEntryForPromotion(any())).thenReturn(true);
		final RuleEngineCalculationService ruleEngineCalculationService = mock(RuleEngineCalculationService.class);

		cartRAOProvider = new DefaultCartRAOProvider(cartRaoConverter, ruleEngineCalculationService, raoExtractors);
//		cartRAOProvider.setCartRaoConverter(cartRaoConverter);
//		cartRAOProvider.setRuleEngineCalculationService(ruleEngineCalculationService);

//		cartRAOProvider = spy(cartRAOProvider);
	}

	@Test
	public void testCreateRAO()
	{
		final CartRAO cartRao = cartRAOProvider.createRAO(cartContext.getCartModel());
		assertThat(cartRao).isNotNull();
//		assertThat(cartRao.getPaymentMode()).isEqualTo(cartContext.getPaymentModeRAO());
	}

//	@Test
//	public void testExpandRAOpaymentMode()
//	{
////		cartContext = cartContext.withPaymentModeModel(PAYMENT_MODE_CODE).withPaymentModeRAO(PAYMENT_MODE_CODE);
//
//		final Set<Object> raoObjects = cartRAOProvider.expandRAO(cartContext.getCartRAO(), singletonList(EXPAND_PAYMENT_MODE));
//
//		assertThat(raoObjects).isNotEmpty().containsOnly(cartContext.getPaymentModeRAO());
//	}

	@Test
	public void testExpandFactModelWithCartModel()
	{
		final Set facts = cartRAOProvider.expandFactModel(cartContext.getCartModel());
		final CartRAO cartRAO = cartContext.getCartRAO();
		cartRAO.setCode(cartContext.getCartModel().getCode());
        cartRAO.setId(cartContext.getCartModel().getId());
		assertThat(facts).isNotEmpty().containsOnly(cartRAO);
	}

//	@Test
//	public void testExpandFactModelOnlyPaymentMode()
//	{
////		cartContext = cartContext.withPaymentModeModel(PAYMENT_MODE_CODE).withPaymentModeRAO(PAYMENT_MODE_CODE);
//
//		final Set facts = cartRAOProvider.expandFactModel(cartContext.getCartModel(), singletonList(EXPAND_PAYMENT_MODE));
////		verify(cartRAOProvider, Mockito.times(1)).createRAO(any(CartModel.class));
//		final CartRAO cartRAO = cartContext.getCartRAO();
//		cartRAO.setCode(cartContext.getCartModel().getCode());
//		assertThat(facts).isNotEmpty().containsOnly(cartRAO, cartContext.getPaymentModeRAO());
//	}

	@Test
	public void testExpandFactModelDiscounts()
	{
		cartContext = cartContext.withDiscounts(1000d, CurrencyType.CASH.toString());
		final Set facts = cartRAOProvider.expandFactModel(cartContext.getCartModel(), singletonList(EXPAND_DISCOUNTS));
		final CartRAO cartRAO = cartContext.getCartRAO();
		cartRAO.setCode(cartContext.getCartModel().getCode());
		cartRAO.setId(cartContext.getCartModel().getId());
		assertThat(facts).isNotEmpty().contains(cartRAO);
		final List discountsList = (List) facts.stream().filter(f -> f instanceof DiscountValueRAO).collect(Collectors.toList());
		assertThat(discountsList).hasSize(1);
	}

	@Test
	public void testExpandFactModelInvalidOption()
	{
		final Set facts = cartRAOProvider.expandFactModel(cartContext.getCartModel(), singletonList("NON_VALID_OPTION"));
		final CartRAO cartRAO = cartContext.getCartRAO();
		cartRAO.setCode(cartContext.getCartModel().getCode());
        cartRAO.setId(cartContext.getCartModel().getId());
		assertThat(facts).isNotEmpty().containsOnly(cartRAO);
	}

	@Test
	public void testExpandFactModelExpandEntries()
	{
		cartContext = cartContext.addEntry(new AbstractOrderEntryModel());
        when(entryRepository.findAllByOrder(cartContext.getCartModel())).thenReturn(cartContext.getCartModel().getEntries());
		final Set facts = cartRAOProvider.expandFactModel(cartContext.getCartModel(), singletonList(EXPAND_ENTRIES));
		final CartRAO cartRAO = cartContext.getCartRAO();
		cartRAO.setCode(cartContext.getCartModel().getCode());
        cartRAO.setId(cartContext.getCartModel().getId());
		assertThat(facts).isNotEmpty().contains(cartRAO);
		final List entriesList = (List) facts.stream().filter(f -> f instanceof OrderEntryRAO).collect(Collectors.toList());
		assertThat(entriesList).hasSize(1);
	}

	@Test
	public void testExpandFactModelExpandProducts()
	{
        Long productId = 2222l;
        cartContext = cartContext.addNewEntry(productId);
        when(entryRepository.findAllByOrder(cartContext.getCartModel())).thenReturn(cartContext.getCartModel().getEntries());
		final Set facts = cartRAOProvider.expandFactModel(cartContext.getCartModel(), singletonList(EXPAND_PRODUCTS));
		final CartRAO cartRAO = cartContext.getCartRAO();
		cartRAO.setCode(cartContext.getCartModel().getCode());
        cartRAO.setId(cartContext.getCartModel().getId());
		assertThat(facts).isNotEmpty().contains(cartRAO);
		final List productsList = (List) facts.stream().filter(f -> f instanceof ProductRAO).collect(Collectors.toList());
		assertThat(productsList).hasSize(1);
	}

	@Test
	public void testExpandFactModelExpandCategories()
	{
        Long productId = 2222l;
        cartContext = cartContext.addNewEntry(productId);
        when(productSearchModel.getFullCategoryIds()).thenReturn(Arrays.asList(11l));
        when(productSearchService.findById(anyLong())).thenReturn(productSearchModel);
        when(entryRepository.findAllByOrder(cartContext.getCartModel())).thenReturn(cartContext.getCartModel().getEntries());
		final Set facts = cartRAOProvider.expandFactModel(cartContext.getCartModel(), singletonList(EXPAND_CATEGORIES));
		final CartRAO cartRAO = cartContext.getCartRAO();
		cartRAO.setCode(cartContext.getCartModel().getCode());
        cartRAO.setId(cartContext.getCartModel().getId());
		assertThat(facts).isNotEmpty().contains(cartRAO);
		final List categoryList = (List) facts.stream().filter(f -> f instanceof CategoryRAO).collect(Collectors.toList());
		assertThat(categoryList).hasSize(1);
	}

	@Test
	public void testExpandFactModelExpandUsers()
	{
        cartContext = cartContext.withUser(2345l);
		final Set facts = cartRAOProvider.expandFactModel(cartContext.getCartModel(), singletonList(EXPAND_USERS));
		final UserRAO userRAO = new UserRAO();
		userRAO.setId(2345l);
		final CartRAO cartRAO = cartContext.getCartRAO();
		cartRAO.setCode(cartContext.getCartModel().getCode());
        cartRAO.setId(cartContext.getCartModel().getId());
		assertThat(facts).isNotEmpty().containsOnly(cartRAO, userRAO);
	}

//	@Test
//	public void testExpandFactModelExpandUsersWithGroups()
//	{
//		cartContext = cartContext.withUserGroups("testUser", new PrincipalGroupModel());
//		final UserGroupModel userGroupModel = new UserGroupModel();
//		Mockito.when(userService.getAllUserGroupsForUser(cartContext.getCartModel().getUser())).thenReturn(
//				ImmutableSet.of(userGroupModel));
//
//		final Set facts = cartRAOProvider.expandFactModel(cartContext.getCartModel(), singletonList(EXPAND_USERS));
//		verify(cartRAOProvider, Mockito.times(1)).createRAO(any(CartModel.class));
//		final UserRAO userRAO = new UserRAO();
//		userRAO.setId("testUser");
//		userRAO.setPk(cartContext.getCartModel().getUser().getPk().getLongValueAsString());
//		final UserGroupRAO userGroupRAO = new UserGroupRAO();
//		final CartRAO cartRAO = cartContext.getCartRAO();
//		cartRAO.setCode(cartContext.getCartModel().getCode());
//		assertThat(facts).isNotEmpty().containsOnly(cartRAO, userRAO, userGroupRAO);
//	}

	protected CartTestContextBuilder getCartTestContextBuilder()
	{
		return cartContext;
	}

	protected DefaultCartRAOProvider getCartRAOProvider()
	{
		return cartRAOProvider;
	}

	protected CartTestContextBuilder createNewCartTestContextBuilder()
	{
		return new CartTestContextBuilder();
	}

}
