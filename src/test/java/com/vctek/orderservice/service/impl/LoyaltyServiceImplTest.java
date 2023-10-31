package com.vctek.orderservice.service.impl;

import com.vctek.converter.Populator;
import com.vctek.dto.request.CheckValidCardParameter;
import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.loyalty.TransactionData;
import com.vctek.kafka.data.loyalty.TransactionRequest;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.orderservice.dto.AvailablePointAmountData;
import com.vctek.orderservice.dto.AwardLoyaltyData;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.dto.ProductCanRewardDto;
import com.vctek.orderservice.dto.request.AvailablePointAmountRequest;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.LoyaltyClient;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.feignclient.dto.RewardSettingData;
import com.vctek.orderservice.kafka.producer.OrderLoyaltyRewardRequestProducer;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.repository.SubOrderEntryRepository;
import com.vctek.orderservice.repository.ToppingItemRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.DateUtil;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import com.vctek.util.TransactionType;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LoyaltyServiceImplTest {
    private LoyaltyServiceImpl loyaltyService;
    @Mock
    private LoyaltyClient loyaltyClient;
    @Mock
    private PromotionResultService promotionResultService;
    @Mock
    private LoyaltyTransactionService loyaltyTransactionService;
    @Mock
    private CalculationService calculationService;
    @Mock
    private ProductRedeemRateUseService productRedeemRateUseService;
    @Mock
    private CartService cartService;
    @Mock
    private OrderModel originModel;
    @Mock
    private ReturnOrderModel returnOrderModel;
    @Mock
    private LoyaltyTransactionModel loyaltyTransactionModel;
    @Mock
    private TransactionData transactionDataMock;
    @Mock
    private FinanceService financeService;
    @Mock
    private ModelService modelService;
    @Mock
    private ProductSearchService productSearchService;
    @Mock
    private Populator<OrderModel, TransactionRequest> transactionRequestPopulator;
    @Mock
    private OrderLoyaltyRewardRequestProducer orderLoyaltyRewardRequestProducer;
    @Mock
    private OrderService orderService;

    private ArgumentCaptor<TransactionRequest> captorTR;
    private ArgumentCaptor<LoyaltyTransactionModel> captorLT;

    private AvailablePointAmountRequest availablePointAmountRequest;

    private OrderModel orderModel;
    private OrderEntryModel toppingItemEntryModel;
    private OrderEntryModel productInPromotion;
    private ArgumentCaptor<TransactionRequest> transactionCaptor;
    private CartModel cartModel;
    private OrderEntryModel comboEntryModel;
    private OrderEntryModel normalEntryModel;
    private SubOrderEntryModel subOrderEntryModel1;
    @Mock
    private CustomerRequest customerReqMock;
    @Mock
    private ToppingItemService toppingItemService;
    @Mock
    private ProductLoyaltyRewardRateService productRewardRateSettingService;
    @Mock
    private EntryRepository entryRepository;
    @Mock
    private ToppingItemRepository toppingItemRepository;
    @Mock
    private SubOrderEntryRepository subOrderEntryRepository;
    @Mock
    private CustomerService customerService;
    private Map<Long, Double> productRewardRate = new HashMap<>();
    private Long warehouseId = 404l;
    private Set<ToppingItemModel> toppingItemModels = new HashSet<>();

    private ToppingOptionModel createToppingOptionModel(Long id, Integer quantity) {
        ToppingOptionModel model = new ToppingOptionModel();
        model.setId(id);
        model.setOrderEntry(toppingItemEntryModel);
        model.setQuantity(quantity);
        return model;
    }

    private ToppingItemModel createToppingItemModel(long id, long productId, int quantity, ToppingOptionModel toppingOptionModel1, double basePrice, double discountOrderToItem, double discount) {
        return createToppingItemModel(id, productId, quantity, toppingOptionModel1, basePrice, discountOrderToItem, discount, CurrencyType.CASH.toString());
    }

    private ToppingItemModel createToppingItemModel(long id, long productId, int quantity, ToppingOptionModel toppingOptionModel1, double basePrice, double discountOrderToItem, double discount, String discountType) {
        ToppingItemModel model = new ToppingItemModel();
        model.setId(id);
        model.setProductId(productId);
        model.setToppingOptionModel(toppingOptionModel1);
        model.setBasePrice(basePrice);
        model.setDiscountOrderToItem(discountOrderToItem);
        model.setQuantity(quantity);
        model.setDiscount(discount);
        model.setDiscountType(discountType);
        return model;
    }

    private Set<PromotionSourceRuleModel> createSourceRules(boolean allowReward) {
        Set<PromotionSourceRuleModel> sourceRuleModels = new HashSet<>();
        PromotionSourceRuleModel sourceRuleModel = new PromotionSourceRuleModel();
        sourceRuleModel.setAllowReward(allowReward);
        sourceRuleModels.add(sourceRuleModel);
        return sourceRuleModels;
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        loyaltyService = new LoyaltyServiceImpl();
        loyaltyService.setLoyaltyClient(loyaltyClient);
        loyaltyService.setPromotionResultService(promotionResultService);
        loyaltyService.setLoyaltyTransactionService(loyaltyTransactionService);
        loyaltyService.setCalculationService(calculationService);
        loyaltyService.setFinanceService(financeService);
        loyaltyService.setCartService(cartService);
        loyaltyService.setProductRedeemRateUseService(productRedeemRateUseService);
        loyaltyService.setProductSearchService(productSearchService);
        loyaltyService.setTransactionRequestPopulator(transactionRequestPopulator);
        loyaltyService.setOrderLoyaltyRewardRequestProducer(orderLoyaltyRewardRequestProducer);
        loyaltyService.setOrderService(orderService);
        loyaltyService.setEntryRepository(entryRepository);
        loyaltyService.setToppingItemRepository(toppingItemRepository);
        loyaltyService.setSubOrderEntryRepository(subOrderEntryRepository);
        loyaltyService.setCustomerService(customerService);
        transactionCaptor = ArgumentCaptor.forClass(TransactionRequest.class);
        captorTR = ArgumentCaptor.forClass(TransactionRequest.class);
        captorLT = ArgumentCaptor.forClass(LoyaltyTransactionModel.class);
        orderModel = new OrderModel();
        orderModel.setId(1L);
        orderModel.setCompanyId(1L);
        orderModel.setCardNumber("master-card");
        orderModel.setCode("this.is.order.code");

        toppingItemEntryModel = new OrderEntryModel();
        toppingItemEntryModel.setId(3l);
        toppingItemEntryModel.setProductId(5l);
        toppingItemEntryModel.setOrder(orderModel);
        toppingItemEntryModel.setFinalPrice(23500d);

        ToppingOptionModel toppingOptionModel1 = createToppingOptionModel(1l, 1);
        ToppingItemModel toppingItemModels1 = createToppingItemModel(1, 20, 2, toppingOptionModel1, 2000, 1000, 1000);
        ToppingItemModel toppingItemModels2 = createToppingItemModel(2, 21, 1, toppingOptionModel1, 1000, 0, 1000);
        ToppingItemModel toppingItemModels3 = createToppingItemModel(3, 22, 3, toppingOptionModel1, 1500, 0, 1000);
        toppingOptionModel1.getToppingItemModels().add(toppingItemModels1);
        toppingOptionModel1.getToppingItemModels().add(toppingItemModels2);
        toppingOptionModel1.getToppingItemModels().add(toppingItemModels3);

        ToppingOptionModel toppingOptionModel2 = createToppingOptionModel(2l, 1);
        ToppingItemModel toppingItemModels4 = createToppingItemModel(4, 20, 1, toppingOptionModel2, 2000, 1000, 1000);
        ToppingItemModel toppingItemModels5 = createToppingItemModel(5, 21, 1, toppingOptionModel2, 1000, 0, 1000);
        ToppingItemModel toppingItemModels6 = createToppingItemModel(6, 22, 1, toppingOptionModel2, 1500, 0, 1000);
        toppingOptionModel2.getToppingItemModels().add(toppingItemModels4);
        toppingOptionModel2.getToppingItemModels().add(toppingItemModels5);
        toppingOptionModel2.getToppingItemModels().add(toppingItemModels6);

        toppingItemEntryModel.getToppingOptionModels().add(toppingOptionModel1);
        toppingItemEntryModel.getToppingOptionModels().add(toppingOptionModel2);
        orderModel.getEntries().add(toppingItemEntryModel);
        toppingItemModels.add(toppingItemModels1);
        toppingItemModels.add(toppingItemModels2);
        toppingItemModels.add(toppingItemModels3);
        toppingItemModels.add(toppingItemModels4);
        toppingItemModels.add(toppingItemModels5);
        toppingItemModels.add(toppingItemModels6);

        productInPromotion = new OrderEntryModel();
        productInPromotion.setId(4l);
        productInPromotion.setProductId(2l);
        productInPromotion.setQuantity(10l);
        productInPromotion.setFixedDiscount(1000.0);
        productInPromotion.setDiscountOrderToItem(2000.0);
        productInPromotion.setBasePrice(10000.0);
        productInPromotion.setFinalPrice(10000.0);
        productInPromotion.setOrder(orderModel);

        availablePointAmountRequest = new AvailablePointAmountRequest();
        availablePointAmountRequest.setCompanyId(1l);

        cartModel = new CartModel();
        cartModel.setId(2L);
        cartModel.setFinalPrice(100000.0);
        cartModel.setCompanyId(availablePointAmountRequest.getCompanyId());
        cartModel.setCode("Keo502");
        cartModel.setCardNumber("code12chuso.");
        List<AbstractOrderEntryModel> entries = orderModel.getEntries().stream().collect(Collectors.toList());

        comboEntryModel = new OrderEntryModel();
        comboEntryModel.setId(1l);
        comboEntryModel.setFinalPrice(1000.0);
        comboEntryModel.setProductId(100l);
        subOrderEntryModel1 = new SubOrderEntryModel();
        subOrderEntryModel1.setProductId(10l);
        subOrderEntryModel1.setOrderEntry(comboEntryModel);
        subOrderEntryModel1.setFinalPrice(5000.0);
        comboEntryModel.setSubOrderEntries(new HashSet<>(Arrays.asList(subOrderEntryModel1)));
        comboEntryModel.setOrder(cartModel);

        normalEntryModel = new OrderEntryModel();
        normalEntryModel.setId(6l);
        normalEntryModel.setQuantity(10l);
        normalEntryModel.setFinalPrice(10000.0);
        normalEntryModel.setDiscountOrderToItem(2000.0);
        normalEntryModel.setProductId(1l);
        normalEntryModel.setOrder(cartModel);
        entries.add(normalEntryModel);
        entries.add(comboEntryModel);
        entries.add(productInPromotion);

        cartModel.setEntries(entries);

        when(customerReqMock.getCompanyId()).thenReturn(1l);
        productRewardRate.put(5l, 1d);
        productRewardRate.put(100l, 1d);
        productRewardRate.put(1l, 1d);
        productRewardRate.put(2l, 1d);
        productRewardRate.put(20l, 1d);
        productRewardRate.put(21l, 1d);
        productRewardRate.put(22l, 1d);
    }

    @Test
    public void reward() {
        List<AbstractOrderEntryModel> orderEntryModels = new ArrayList<>();
        orderEntryModels.add(productInPromotion);
        orderModel.setEntries(orderEntryModels);
        Date date = DateUtil.parseDate("2020-02-22 10:10:00)", DateUtil.ISO_DATE_TIME_PATTERN);
        orderModel.setCreatedTime(date);
        orderModel.setCompanyId(1L);
        orderModel.setWarehouseId(2L);
        orderModel.setCardNumber("cardNumber");
        orderModel.setCode("orderCode");
        orderModel.setType("orderType");
        orderModel.setTotalRewardAmount(1d);
        orderModel.setCustomerId(2L);
        when(customerService.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(new CustomerData());
        when(promotionResultService.findAllPromotionSourceRulesByOrder(orderModel)).thenReturn(new HashSet<>());
        when(promotionResultService.findAllPromotionSourceRulesAppliedToOrderEntry(any(AbstractOrderEntryModel.class)))
                .thenReturn(createSourceRules(false).stream().collect(Collectors.toSet()));
        when(promotionResultService.getTotalAppliedQuantityOf(any(AbstractOrderEntryModel.class))).thenReturn(2l);
        when(loyaltyClient.isAppliedCardNumber(any(CheckValidCardParameter.class))).thenReturn(true);
        when(calculationService.calculateLoyaltyAmount(anyList(), anyLong())).thenReturn(100000d);
        TransactionData transactionData = new TransactionData();
        transactionData.setConversionRate(1000d);
        transactionData.setPoint(100d);
        transactionData.setInvoiceNumber("invoiceNumber");
        when(loyaltyClient.reward(any(TransactionRequest.class))).thenReturn(transactionData);
        loyaltyService.reward(orderModel);
        verify(transactionRequestPopulator).populate(any(OrderModel.class), any(TransactionRequest.class));
        verify(orderLoyaltyRewardRequestProducer).sendLoyaltyRewardRequestKafka(any(TransactionRequest.class), any(KafkaMessageType.class));
    }

    @Test
    public void getAwardProducts_NoPromotionApplied() {
        when(promotionResultService.findAllByOrder(orderModel)).thenReturn(new ArrayList<>());
        when(entryRepository.findAllByOrder(any())).thenReturn(Arrays.asList(toppingItemEntryModel));
        when(toppingItemRepository.findAllByOrderId(anyLong())).thenReturn(toppingItemModels);
        List<ProductCanRewardDto> data = loyaltyService.getAwardProducts(orderModel);
        assertEquals(7, data.size());
        for (ProductCanRewardDto dto : data) {
            if (dto.getOrderEntryId().equals(2l)) {
                assertEquals(8000.0, dto.getFinalPrice(), 0);
                assertEquals(1l, dto.getProductId(), 0);
                assertNull(dto.getToppingItemId());
                assertNull(dto.getToppingOptionId());
            }
            if (dto.getOrderEntryId().equals(3l)) {
                if (dto.getToppingOptionId() != null && dto.getToppingOptionId().equals(1L)) {
                    if (dto.getProductId().equals(20l)) {
                        assertEquals(2000, dto.getFinalPrice(), 0);
                    }
                    if (dto.getProductId().equals(21)) {
                        assertEquals(-1000, dto.getFinalPrice(), 0);
                    }
                    if (dto.getProductId().equals(22)) {
                        assertEquals(2500, dto.getFinalPrice(), 0);
                    }
                }
            }
        }
    }

    @Test
    public void getAwardProducts_OrderPromotion_AllowReward() {
        Set<PromotionSourceRuleModel> sourceRules = createSourceRules(true);
        when(entryRepository.findAllByOrder(any())).thenReturn(Arrays.asList(toppingItemEntryModel));
        when(toppingItemRepository.findAllByOrderId(anyLong())).thenReturn(toppingItemModels);
        when(promotionResultService.findAllPromotionSourceRulesByOrder(eq(orderModel))).thenReturn(sourceRules);
        List<ProductCanRewardDto> data = loyaltyService.getAwardProducts(orderModel);
        assertEquals(7, data.size());
    }

    @Test
    public void getAwardProducts_OrderPromotion_NotAllowReward() {
        Set<PromotionSourceRuleModel> sourceRules = createSourceRules(false);
        when(promotionResultService.findAllPromotionSourceRulesAppliedToOrder(orderModel)).thenReturn(sourceRules);
        List<ProductCanRewardDto> data = loyaltyService.getAwardProducts(orderModel);
        assertEquals(0, data.size());
    }

    @Test
    public void getAwardProducts_HasNotProductPromotion() {
        List<AbstractOrderEntryModel> orderEntryModels = new ArrayList<>();
        orderEntryModels.add(productInPromotion);
        orderModel.setEntries(orderEntryModels);
        when(entryRepository.findAllByOrder(any())).thenReturn(Arrays.asList(productInPromotion));
        when(promotionResultService.findAllPromotionSourceRulesByOrder(orderModel)).thenReturn(new HashSet<>());
        when(promotionResultService.findAllPromotionSourceRulesAppliedToOrderEntry(any(AbstractOrderEntryModel.class)))
                .thenReturn(createSourceRules(false).stream().collect(Collectors.toSet()));
        when(promotionResultService.getTotalAppliedQuantityOf(any(AbstractOrderEntryModel.class))).thenReturn(2l);
        List<ProductCanRewardDto> data = loyaltyService.getAwardProducts(orderModel);
        assertEquals(1, data.size(), 0);
        for (ProductCanRewardDto dto : data) {
            if (dto.getProductId().equals(2l)) {
                assertEquals(77600, dto.getFinalPrice(), 0);
            }
        }
    }

    @Test
    public void getAwardProducts_withCombo_notAward() {
        comboEntryModel.setOrder(orderModel);
        List<AbstractOrderEntryModel> orderEntryModels = new ArrayList<>();
        orderEntryModels.add(productInPromotion);
        orderEntryModels.add(comboEntryModel);
        when(entryRepository.findAllByOrder(any())).thenReturn(orderEntryModels);
        ProductSearchModel productSearchData = new ProductSearchModel();
        productSearchData.setId(comboEntryModel.getProductId());
        when(cartService.isComboEntry(comboEntryModel)).thenReturn(true);
        when(promotionResultService.findAllPromotionSourceRulesByOrder(orderModel)).thenReturn(new HashSet<>());
        when(promotionResultService.findAllPromotionSourceRulesAppliedToOrderEntry(any(AbstractOrderEntryModel.class)))
                .thenReturn(createSourceRules(false).stream().collect(Collectors.toSet()));
        when(promotionResultService.getTotalAppliedQuantityOf(any(AbstractOrderEntryModel.class))).thenReturn(2l);
        when(productSearchService.findAllByCompanyId(any(ProductSearchRequest.class))).thenReturn(Arrays.asList(productSearchData));
        List<ProductCanRewardDto> data = loyaltyService.getAwardProducts(orderModel);
        assertEquals(1, data.size(), 0);
        for (ProductCanRewardDto dto : data) {
            if (dto.getProductId().equals(2l)) {
                assertEquals(77600, dto.getFinalPrice(), 0);
            }
        }
    }

    @Test
    public void getAwardProducts_withCombo() {
        comboEntryModel.setOrder(orderModel);
        List<AbstractOrderEntryModel> orderEntryModels = new ArrayList<>();
        orderEntryModels.add(productInPromotion);
        orderEntryModels.add(comboEntryModel);
        when(entryRepository.findAllByOrder(any())).thenReturn(orderEntryModels);
        when(subOrderEntryRepository.findAllByOrderEntry(any())).thenReturn(Arrays.asList(subOrderEntryModel1));
        when(cartService.isComboEntry(comboEntryModel)).thenReturn(true);
        ProductSearchModel productSearchData = new ProductSearchModel();
        productSearchData.setId(comboEntryModel.getProductId());
        productSearchData.setAllowReward(true);
        when(promotionResultService.findAllPromotionSourceRulesByOrder(orderModel)).thenReturn(new HashSet<>());
        when(promotionResultService.findAllPromotionSourceRulesAppliedToOrderEntry(any(AbstractOrderEntryModel.class)))
                .thenReturn(createSourceRules(false).stream().collect(Collectors.toSet()));
        when(promotionResultService.getTotalAppliedQuantityOf(any(AbstractOrderEntryModel.class))).thenReturn(2l);
        when(productSearchService.findAllByCompanyId(any(ProductSearchRequest.class))).thenReturn(Arrays.asList(productSearchData));
        List<ProductCanRewardDto> data = loyaltyService.getAwardProducts(orderModel);
        assertEquals(2, data.size(), 0);
        for (ProductCanRewardDto dto : data) {
            if (dto.getProductId().equals(2l)) {
                assertEquals(77600, dto.getFinalPrice(), 0);
            }
            if (dto.getProductId().equals(100l)) {
                assertEquals(1, dto.getSubOrderEntries().size());
            }
        }
    }

    @Test
    public void redeem() {
        Date date = DateUtil.parseDate("2020-02-22 10:10:00)", DateUtil.ISO_DATE_TIME_PATTERN);
        orderModel.setCreatedTime(date);
        orderModel.setCompanyId(1L);
        orderModel.setWarehouseId(2L);
        orderModel.setCardNumber("cardNumber");
        orderModel.setCode("orderCode");
        orderModel.setType("orderType");

        TransactionData transactionData = new TransactionData();
        transactionData.setConversionRate(1000d);
        transactionData.setPoint(100d);
        transactionData.setInvoiceNumber("invoiceNumber");
        when(loyaltyClient.redeem(any(TransactionRequest.class))).thenReturn(transactionData);
        loyaltyService.redeem(orderModel, 10000d);
        verify(transactionRequestPopulator).populate(any(OrderModel.class), any(TransactionRequest.class));
        verify(loyaltyClient).redeem(transactionCaptor.capture());
        TransactionRequest transactionRequest = transactionCaptor.getValue();
        assertEquals(10000, transactionRequest.getAmount(), 0);
        assertNull(transactionRequest.getReferInvoiceNumber());

        verify(loyaltyTransactionService).save(captorLT.capture());
        LoyaltyTransactionModel loyaltyTransaction = captorLT.getValue();
        assertEquals("orderCode", loyaltyTransaction.getOrderCode());
        assertEquals(TransactionType.REDEEM.name(), loyaltyTransaction.getType());
        assertEquals("invoiceNumber", loyaltyTransaction.getInvoiceNumber());
        assertEquals(1000, loyaltyTransaction.getConversionRate(), 0);
        assertNull(loyaltyTransaction.getReturnOrderId());

    }

    @Test
    public void findByCardNumber() {
        when(loyaltyClient.getDetailByCardNumber(anyString(), anyLong())).thenReturn(null);
        loyaltyService.findByCardNumber("trung quoc la cua Viet Nam", 1l);
        verify(loyaltyClient).getDetailByCardNumber(anyString(), anyLong());
    }

    @Test
    public void computeAvailablePointAmountOfOrder_modelNotFound() {
        try {
            when(cartService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(null);
            loyaltyService.computeAvailablePointAmountOf(availablePointAmountRequest);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_CODE.code(), e.getCode());
        }
    }

    @Test
    public void computeAvailablePointAmountOfOrder() {
        availablePointAmountRequest.setOrderCode("quanhanhquanguyhiem");
        availablePointAmountRequest.setCardNumber("setCardNumber");
        LoyaltyCardData loyaltyCardData = new LoyaltyCardData();
        loyaltyCardData.setCompanyId(availablePointAmountRequest.getCompanyId());
        loyaltyCardData.setConversionRate(1500.0);
        loyaltyCardData.setPointAmount(2000.0);
        Map<Long, Boolean> canRedeem = new HashMap<>();
        canRedeem.put(20l, false);
        canRedeem.put(21l, true);
        canRedeem.put(22l, true);
        canRedeem.put(2l, true);
        canRedeem.put(6l, true);
        canRedeem.put(10l, false); // sp combo
        canRedeem.put(1l, true);
        canRedeem.put(5l, false);
        when(cartService.findByOrderCodeAndCompanyId(anyString(), anyLong())).thenReturn(cartModel);
        when(loyaltyClient.getDetailByCardNumber(anyString(), anyLong())).thenReturn(loyaltyCardData);
        when(productRedeemRateUseService.productCanRedeem(anyLong(), anyList())).thenReturn(canRedeem);
        AvailablePointAmountData data = loyaltyService.computeAvailablePointAmountOf(availablePointAmountRequest);
        verify(cartService).findByOrderCodeAndCompanyId(anyString(), anyLong());
        verify(loyaltyClient).getDetailByCardNumber(anyString(), anyLong());
        verify(productRedeemRateUseService).productCanRedeem(anyLong(), anyList());
        assertEquals(2000.0, data.getAvailableAmount(), 0);
        assertEquals(16, data.getPointAmount(), 0);
    }

    @Test
    public void revert_HasNotExistReferInvoice() {
        when(originModel.getCode()).thenReturn("orderCode");
        when(loyaltyTransactionService.findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()))).
                thenReturn(null);
        try {
            loyaltyService.revert(originModel, returnOrderModel, 5000d);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_EXISTED_REFER_INVOICE_FOR_REVERT.code(), e.getCode());
        }

    }

    @Test
    public void revertSuccess() {
        when(originModel.getCode()).thenReturn("orderCode");
        when(originModel.getCompanyId()).thenReturn(1L);
        when(originModel.getWarehouseId()).thenReturn(2L);
        when(originModel.getCardNumber()).thenReturn("cardNumber");
        when(returnOrderModel.getId()).thenReturn(3L);
        Date date = DateUtil.parseDate("2020-02-22 10:10:00)", DateUtil.ISO_DATE_TIME_PATTERN);
        when(returnOrderModel.getCreatedTime()).thenReturn(date);
        when(loyaltyTransactionService.findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()))).
                thenReturn(loyaltyTransactionModel);
        when(loyaltyTransactionModel.getInvoiceNumber()).thenReturn("referInvoice");
        when(loyaltyClient.revert(any(TransactionRequest.class))).thenReturn(transactionDataMock);
        when(transactionDataMock.getConversionRate()).thenReturn(1000d);
        when(transactionDataMock.getInvoiceNumber()).thenReturn("invoiceNumber");

        loyaltyService.revert(originModel, returnOrderModel, 5000d);

        verify(loyaltyTransactionService).findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()));
        verify(transactionRequestPopulator).populate(any(OrderModel.class), any(TransactionRequest.class));
        verify(loyaltyClient).revert(captorTR.capture());
        verify(loyaltyTransactionService).save(captorLT.capture());
        TransactionRequest transactionRequest = captorTR.getValue();
        assertEquals(5000, transactionRequest.getAmount(), 0);
        assertEquals("referInvoice", transactionRequest.getReferInvoiceNumber());
        LoyaltyTransactionModel loyaltyTransaction = captorLT.getValue();
        assertEquals("orderCode", loyaltyTransaction.getOrderCode());
        assertEquals(TransactionType.REVERT.name(), loyaltyTransaction.getType());
        assertEquals("invoiceNumber", loyaltyTransaction.getInvoiceNumber());
        assertEquals(1000, loyaltyTransaction.getConversionRate(), 0);
        assertEquals(3, loyaltyTransaction.getReturnOrderId(), 0);
    }

    @Test
    public void revertOnlineOrderReward_NotFoundAwardTransaction() {
        when(originModel.getCode()).thenReturn("orderCode");
        when(originModel.getCompanyId()).thenReturn(1L);
        when(originModel.getWarehouseId()).thenReturn(2L);
        when(originModel.getCardNumber()).thenReturn("cardNumber");
        Date date = DateUtil.parseDate("2020-02-22 10:10:00)", DateUtil.ISO_DATE_TIME_PATTERN);
        when(returnOrderModel.getCreatedTime()).thenReturn(date);
        when(loyaltyTransactionService.findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()))).
                thenReturn(null);

        loyaltyService.revertOnlineOrderReward(originModel, 5000d);

        verify(loyaltyTransactionService).findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()));
        verify(transactionRequestPopulator, times(0)).populate(any(OrderModel.class), any(TransactionRequest.class));
        verify(loyaltyClient, times(0)).revert(captorTR.capture());
        verify(loyaltyTransactionService, times(0)).save(any(LoyaltyTransactionModel.class));
    }

    @Test
    public void revertOnlineOrderReward_success() {
        when(originModel.getCode()).thenReturn("orderCode");
        when(originModel.getCompanyId()).thenReturn(1L);
        when(originModel.getWarehouseId()).thenReturn(2L);
        when(originModel.getCardNumber()).thenReturn("cardNumber");
        Date date = DateUtil.parseDate("2020-02-22 10:10:00)", DateUtil.ISO_DATE_TIME_PATTERN);
        when(returnOrderModel.getCreatedTime()).thenReturn(date);
        when(loyaltyTransactionService.findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()))).
                thenReturn(loyaltyTransactionModel);
        when(loyaltyTransactionModel.getInvoiceNumber()).thenReturn("referInvoice");
        when(loyaltyClient.revert(any(TransactionRequest.class))).thenReturn(transactionDataMock);
        when(transactionDataMock.getConversionRate()).thenReturn(1000d);
        when(transactionDataMock.getInvoiceNumber()).thenReturn("invoiceNumber");

        loyaltyService.revertOnlineOrderReward(originModel, 5000d);

        verify(loyaltyTransactionService).findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()));
        verify(transactionRequestPopulator).populate(any(OrderModel.class), any(TransactionRequest.class));
        verify(loyaltyClient).revert(captorTR.capture());
        verify(loyaltyTransactionService).save(captorLT.capture());
        TransactionRequest transactionRequest = captorTR.getValue();
        assertEquals(5000, transactionRequest.getAmount(), 0);
        assertEquals("referInvoice", transactionRequest.getReferInvoiceNumber());
        LoyaltyTransactionModel loyaltyTransaction = captorLT.getValue();
        assertEquals("orderCode", loyaltyTransaction.getOrderCode());
        assertEquals(TransactionType.REVERT.name(), loyaltyTransaction.getType());
        assertEquals("invoiceNumber", loyaltyTransaction.getInvoiceNumber());
        assertEquals(1000, loyaltyTransaction.getConversionRate(), 0);
        assertNull(loyaltyTransaction.getReturnOrderId());
    }

    @Test
    public void refund_HasNotExistRefer_Invoice() {
        when(originModel.getCode()).thenReturn("orderCode");
        when(loyaltyTransactionService.findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.REDEEM.name(), TransactionType.REFUND.name()))).
                thenReturn(null);
        try {
            loyaltyService.refund(originModel, returnOrderModel, 5000d);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_EXISTED_REFER_INVOICE_FOR_REFUND.code(), e.getCode());
        }
    }


    @Test
    public void refundSuccess() {
        when(originModel.getCode()).thenReturn("orderCode");
        when(originModel.getCompanyId()).thenReturn(1L);
        when(originModel.getWarehouseId()).thenReturn(2L);
        when(originModel.getCardNumber()).thenReturn("cardNumber");
        when(returnOrderModel.getId()).thenReturn(3L);
        Date date = DateUtil.parseDate("2020-02-22 10:10:00)", DateUtil.ISO_DATE_TIME_PATTERN);
        when(returnOrderModel.getCreatedTime()).thenReturn(date);
        when(loyaltyTransactionService.findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.REDEEM.name(), TransactionType.REFUND.name()))).
                thenReturn(loyaltyTransactionModel);
        when(loyaltyTransactionModel.getInvoiceNumber()).thenReturn("referInvoice");
        when(loyaltyClient.refund(any(TransactionRequest.class))).thenReturn(transactionDataMock);
        when(transactionDataMock.getConversionRate()).thenReturn(1000d);
        when(transactionDataMock.getInvoiceNumber()).thenReturn("invoiceNumber");
        loyaltyService.refund(originModel, returnOrderModel, 5000d);
        verify(loyaltyTransactionService).findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.REDEEM.name(), TransactionType.REFUND.name()));
        verify(loyaltyClient).refund(captorTR.capture());
        verify(loyaltyTransactionService).save(captorLT.capture());
        TransactionRequest transactionRequest = captorTR.getValue();
        assertEquals(5000, transactionRequest.getAmount(), 0);
        LoyaltyTransactionModel loyaltyTransaction = captorLT.getValue();
        assertEquals("orderCode", loyaltyTransaction.getOrderCode());
        assertEquals(TransactionType.REFUND.name(), loyaltyTransaction.getType());
        assertEquals("invoiceNumber", loyaltyTransaction.getInvoiceNumber());
        assertEquals(1000, loyaltyTransaction.getConversionRate(), 0);
        assertEquals(3, loyaltyTransaction.getReturnOrderId(), 0);
    }

    @Test
    public void updateReward() {
        orderModel.setCardNumber("card-number");
        orderModel.setCode("order-code");
        orderModel.setTotalRewardAmount(10000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setInvoiceNumber("abc-xyz");
        loyaltyTransactionModel.setConversionRate(1000d);
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(loyaltyTransactionModel);
        TransactionData transactionData = new TransactionData();
        transactionData.setConversionRate(1500d);
        when(loyaltyClient.updateReward(any(), anyString())).thenReturn(transactionData);
        loyaltyService.updateReward(orderModel);
        verify(loyaltyClient).updateReward(any(TransactionRequest.class), anyString());
        assertEquals(1500d, loyaltyTransactionModel.getConversionRate(), 0);
    }

    @Test
    public void updateRedeem() {
        orderModel.setCardNumber("card-number");
        orderModel.setCode("order-code");
        orderModel.setTotalRewardAmount(10000d);

        Set<PaymentTransactionModel> paymentTransactions = new HashSet<>();
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setPaymentMethodId(1L);
        paymentTransactionModel.setAmount(10000d);
        paymentTransactions.add(paymentTransactionModel);
        orderModel.setPaymentTransactions(paymentTransactions);

        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setInvoiceNumber("abc-xyz");
        loyaltyTransactionModel.setConversionRate(1000d);
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(loyaltyTransactionModel);
        TransactionData transactionData = new TransactionData();
        transactionData.setConversionRate(1500d);
        when(loyaltyClient.updateRedeem(any(), anyString())).thenReturn(transactionData);

        PaymentMethodData paymentMethodData = new PaymentMethodData();
        paymentMethodData.setId(1L);
        when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
        loyaltyService.updateRedeem(orderModel);
        verify(loyaltyClient).updateRedeem(any(TransactionRequest.class), anyString());
        assertEquals(1500d, loyaltyTransactionModel.getConversionRate(), 0);
    }

    @Test
    public void assignCardToCustomerIfNeed_emptyCardNumber() {
        loyaltyService.assignCardToCustomerIfNeed(StringUtils.EMPTY, customerReqMock, false, warehouseId);
        verify(loyaltyClient, times(0)).getDetailByCardNumber(anyString(), anyLong());
        verify(loyaltyClient, times(0)).assignCard(any());
    }

    @Test
    public void assignCardToCustomerIfNeed_emptyCustomer() {
        loyaltyService.assignCardToCustomerIfNeed("111", null, false, warehouseId);
        verify(loyaltyClient, times(0)).getDetailByCardNumber(anyString(), anyLong());
        verify(loyaltyClient, times(0)).assignCard(any());
    }

    @Test
    public void assignCardToCustomerIfNeed_cardHadBeenAssigned() {
        LoyaltyCardData data = new LoyaltyCardData();
        data.setAssignedPhone("098727827262");
        when(loyaltyClient.getDetailByCardNumber(anyString(), anyLong())).thenReturn(data);
        loyaltyService.assignCardToCustomerIfNeed("111", customerReqMock, false, warehouseId);
        verify(loyaltyClient, times(1)).getDetailByCardNumber(anyString(), anyLong());
        verify(loyaltyClient, times(0)).assignCard(any());
    }

    @Test
    public void assignCardToCustomerIfNeed_cardHaveNotBeenAssigned() {
        LoyaltyCardData data = new LoyaltyCardData();
        data.setAssignedPhone(null);
        when(loyaltyClient.getDetailByCardNumber(anyString(), anyLong())).thenReturn(data);
        loyaltyService.assignCardToCustomerIfNeed("111", customerReqMock, false, warehouseId);
        verify(loyaltyClient, times(1)).getDetailByCardNumber(anyString(), anyLong());
        verify(loyaltyClient, times(1)).assignCard(any());
    }

    @Test
    public void updateRefund() {
        orderModel.setCardNumber("cardNumber");
        orderModel.setCode("code");
        ReturnOrderModel returnOrderModel = new ReturnOrderModel();
        returnOrderModel.setOriginOrder(orderModel);
        returnOrderModel.setRefundAmount(1000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setInvoiceNumber("invoiceNumber");
        loyaltyTransactionModel.setConversionRate(1000d);
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(loyaltyTransactionModel);
        TransactionData transactionData = new TransactionData();
        transactionData.setConversionRate(1500d);
        when(loyaltyClient.updateRefund(any(), anyString())).thenReturn(transactionData);
        loyaltyService.updateRefund(returnOrderModel);
        verify(loyaltyClient).updateRefund(any(TransactionRequest.class), anyString());
        verify(loyaltyTransactionService).save(any(LoyaltyTransactionModel.class));
        assertEquals(1500d, loyaltyTransactionModel.getConversionRate(), 0);
    }

    @Test
    public void findByInvoiceNumberAndCompanyIdAndType() {
        when(loyaltyClient.findByInvoiceNumberAndCompanyIdAndType(any(), anyString())).thenReturn(new TransactionData());
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setInvoiceNumber("abc");
        transactionRequest.setCompanyId(2l);
        transactionRequest.setType(TransactionType.AWARD.name());
        loyaltyService.findByInvoiceNumberAndCompanyIdAndType(transactionRequest);
        verify(loyaltyClient).findByInvoiceNumberAndCompanyIdAndType(any(TransactionRequest.class), anyString());
    }

    @Test
    public void getLoyaltyPoints_HasNotEntryToAward() {
        Set<PromotionSourceRuleModel> sourceRules = createSourceRules(false);
        when(promotionResultService.findAllPromotionSourceRulesAppliedToOrder(orderModel)).thenReturn(sourceRules);
        AwardLoyaltyData data = loyaltyService.getLoyaltyPointsOf(cartModel);
        Map<Long, Double> entryPoints = data.getEntryPoints();
        Map<Long, Double> toppingPoints = data.getToppingPoints();
        assertEquals(0, entryPoints.size());
        assertEquals(0, toppingPoints.size());
    }

    @Test
    public void getLoyaltyPoints_hasNotRewardUnitSetting() {
        when(promotionResultService.findAllByOrder(orderModel)).thenReturn(new ArrayList<>());
        when(loyaltyClient.findRewardUnit(any())).thenReturn(null);
        AwardLoyaltyData data = loyaltyService.getLoyaltyPointsOf(cartModel);
        Map<Long, Double> entryPoints = data.getEntryPoints();
        Map<Long, Double> toppingPoints = data.getToppingPoints();
        assertEquals(0, entryPoints.size());
        assertEquals(0, toppingPoints.size());
    }

    @Test
    public void getLoyaltyPoints_hasRewardUnitSettingIsZero() {
        when(promotionResultService.findAllByOrder(orderModel)).thenReturn(new ArrayList<>());
        when(loyaltyClient.findRewardUnit(any())).thenReturn(new RewardSettingData());
        AwardLoyaltyData data = loyaltyService.getLoyaltyPointsOf(cartModel);
        Map<Long, Double> entryPoints = data.getEntryPoints();
        Map<Long, Double> toppingPoints = data.getToppingPoints();
        assertEquals(0, entryPoints.size());
        assertEquals(0, toppingPoints.size());
    }

    @Test
    public void getLoyaltyPoints() {
        DefaultCalculationService calculationService = new DefaultCalculationService(modelService, toppingItemService);
        loyaltyService.setCalculationService(calculationService);
        calculationService.setProductLoyaltyRewardRateService(productRewardRateSettingService);

        when(entryRepository.findAllByOrder(any())).thenReturn(Arrays.asList(normalEntryModel, comboEntryModel, productInPromotion, toppingItemEntryModel));
        when(toppingItemRepository.findAllByOrderId(any())).thenReturn(toppingItemModels);
        when(subOrderEntryRepository.findAllByOrderEntry(any())).thenReturn(Arrays.asList(subOrderEntryModel1));
        when(productRewardRateSettingService.getRewardRateByProductIds(anySet(), any(), anyBoolean()))
                .thenReturn(productRewardRate);
        when(promotionResultService.findAllByOrder(cartModel)).thenReturn(new ArrayList<>());
        RewardSettingData rewardSettingData = new RewardSettingData();
        rewardSettingData.setConversionRate(1000d);
        when(loyaltyClient.findRewardUnit(any())).thenReturn(rewardSettingData);
        ProductSearchModel productSearchData = new ProductSearchModel();
        productSearchData.setId(comboEntryModel.getProductId());
        productSearchData.setAllowReward(true);
        when(productSearchService.findAllByCompanyId(any(ProductSearchRequest.class))).thenReturn(Arrays.asList(productSearchData));

        AwardLoyaltyData data = loyaltyService.getLoyaltyPointsOf(cartModel);
        Map<Long, Double> entryPoints = data.getEntryPoints();
        Map<Long, Double> toppingPoints = data.getToppingPoints();
        assertEquals(4, entryPoints.size());
        assertEquals(0.24, entryPoints.get(3l), 0);
        assertEquals(0.08, entryPoints.get(4l), 0);
        assertEquals(0.01, entryPoints.get(1l), 0);
        assertEquals(0.08, entryPoints.get(6l), 0);

        assertEquals(6, toppingPoints.size());
        assertEquals(0.02, toppingPoints.get(1l), 0);
        assertEquals(0, toppingPoints.get(2l), 0);
        assertEquals(0.04, toppingPoints.get(3l), 0);
        assertEquals(0, toppingPoints.get(4l), 0);
        assertEquals(0, toppingPoints.get(5l), 0);
        assertEquals(0.01, toppingPoints.get(6l), 0);
    }

    @Test
    public void convertAmountToPoint() {
        loyaltyService.convertAmountToPoint(20000d, 2L);
        verify(loyaltyClient).convertAmountToPoint(any(TransactionRequest.class));
    }

    @Test
    public void splitRewardAmountToEntriesAndCreateLoyaltyTransaction_emptyOrder() {
        TransactionData data = new TransactionData();
        data.setReferCode("code");
        data.setCompanyId(2L);
        data.setInvoiceNumber("434545");
        data.setConversionRate(1000d);
        data.setAwardAmount(12000d);
        data.setPoint(12);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(null);
        loyaltyService.splitRewardAmountToEntriesAndCreateLoyaltyTransaction(data);
        verify(orderService, times(1)).findByCodeAndCompanyId(anyString(), anyLong());
        verify(calculationService, times(0)).calculateLoyaltyAmount(anyList(), anyLong());
        verify(calculationService, times(0)).saveRewardAmountToEntries(any(), anyDouble(), anyDouble(), anyList(), anyBoolean());
        verify(loyaltyTransactionService, times(0)).save(any());
    }

    @Test
    public void splitRewardAmountToEntriesAndCreateLoyaltyTransaction() {
        TransactionData data = new TransactionData();
        data.setReferCode("code");
        data.setCompanyId(2L);
        data.setInvoiceNumber("434545");
        data.setCardNumber("434545");
        data.setConversionRate(1000d);
        data.setAwardAmount(12000d);
        data.setPoint(12);
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModel);
        loyaltyService.splitRewardAmountToEntriesAndCreateLoyaltyTransaction(data);
        verify(orderService, times(1)).findByCodeAndCompanyId(anyString(), anyLong());
        verify(orderService, times(1)).save(any());
        verify(calculationService, times(1)).calculateLoyaltyAmount(anyList(), anyLong());
        verify(calculationService, times(1)).saveRewardAmountToEntries(any(), anyDouble(), anyDouble(), anyList(), anyBoolean());
        verify(loyaltyTransactionService, times(1)).save(any());
    }

    @Test
    public void cancelRedeem_loyaltyTransactionModel_null() {
        try {
            when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(null);
            loyaltyService.cancelPendingRedeem(orderModel);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_EXISTED_REFER_INVOICE_FOR_REDEEM.message(), e.getMessage());
        }
    }

    @Test
    public void cancelRedeem() {
        PaymentMethodData paymentMethodData = new PaymentMethodData();
        Set<PaymentTransactionModel> paymentTransactionModels = new HashSet<>();
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setPaymentMethodId(2L);
        paymentTransactionModels.add(paymentTransactionModel);
        paymentMethodData.setId(2L);
        orderModel.setPaymentTransactions(paymentTransactionModels);
        orderModel.setRedeemAmount(10000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setInvoiceNumber("invoice number");
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(loyaltyTransactionModel);
        when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
        loyaltyService.cancelPendingRedeem(orderModel);
        verify(loyaltyClient).cancelRedeem(any(TransactionRequest.class), anyString());
        verify(loyaltyTransactionService).findLastByOrderCodeAndListType(anyString(), anyList());
        verify(loyaltyTransactionService).save(any());
    }

    @Test
    public void updateRedeemOnline() {
        orderModel.setCardNumber("card-number");
        orderModel.setCode("order-code");
        orderModel.setTotalRewardAmount(10000d);
        PaymentTransactionRequest request = new PaymentTransactionRequest();
        request.setAmount(10000d);
        PaymentMethodData paymentMethodData = new PaymentMethodData();
        Set<PaymentTransactionModel> paymentTransactionModels = new HashSet<>();
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setPaymentMethodId(2L);
        paymentTransactionModels.add(paymentTransactionModel);
        paymentMethodData.setId(2L);
        orderModel.setPaymentTransactions(paymentTransactionModels);

        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setInvoiceNumber("abc-xyz");
        loyaltyTransactionModel.setConversionRate(1000d);
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(loyaltyTransactionModel);
        TransactionData transactionData = new TransactionData();
        transactionData.setConversionRate(1500d);
        when(loyaltyClient.updatePendingRedeem(any(), anyString())).thenReturn(transactionData);
        when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
        loyaltyService.updatePendingRedeem(orderModel, request);
        verify(loyaltyClient).updatePendingRedeem(any(TransactionRequest.class), anyString());
        assertEquals(1500d, loyaltyTransactionModel.getConversionRate(), 0);
        assertEquals(1, orderModel.getPaymentTransactions().size());
    }

    @Test
    public void createRedeemPending() {
        Date date = DateUtil.parseDate("2020-02-22 10:10:00)", DateUtil.ISO_DATE_TIME_PATTERN);
        orderModel.setCreatedTime(date);
        orderModel.setCompanyId(1L);
        orderModel.setWarehouseId(2L);
        orderModel.setCardNumber("cardNumber");
        orderModel.setCode("orderCode");
        orderModel.setType("orderType");

        TransactionData transactionData = new TransactionData();
        transactionData.setConversionRate(1000d);
        transactionData.setPoint(100d);
        transactionData.setInvoiceNumber("invoiceNumber");
        when(loyaltyClient.createRedeemPending(any(TransactionRequest.class))).thenReturn(transactionData);
        loyaltyService.createRedeemPending(orderModel, 10000d);
        verify(transactionRequestPopulator).populate(any(OrderModel.class), any(TransactionRequest.class));
        verify(loyaltyClient).createRedeemPending(transactionCaptor.capture());
        TransactionRequest transactionRequest = transactionCaptor.getValue();
        assertEquals(10000, transactionRequest.getAmount(), 0);
        assertNull(transactionRequest.getReferInvoiceNumber());

        verify(loyaltyTransactionService).save(captorLT.capture());
        LoyaltyTransactionModel loyaltyTransaction = captorLT.getValue();
        assertEquals("orderCode", loyaltyTransaction.getOrderCode());
        assertEquals(TransactionType.REDEEM.name(), loyaltyTransaction.getType());
        assertEquals("invoiceNumber", loyaltyTransaction.getInvoiceNumber());
        assertEquals(1000, loyaltyTransaction.getConversionRate(), 0);
        assertNull(loyaltyTransaction.getReturnOrderId());

    }

    @Test
    public void completeRedeemLoyaltyForOnline_HasNotTransactionRedeem() {
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(null);
        loyaltyService.completeRedeemLoyaltyForOnline(orderModel);
        verify(loyaltyClient, times(0)).completePendingRedeem(any(TransactionRequest.class), anyString());
    }

    @Test
    public void completeRedeemLoyaltyForOnline_RefundTransaction() {
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(loyaltyTransactionModel);
        when(loyaltyTransactionModel.getType()).thenReturn(TransactionType.REFUND.name());
        when(loyaltyTransactionModel.getInvoiceNumber()).thenReturn("invoiceNo");
        loyaltyService.completeRedeemLoyaltyForOnline(orderModel);
        verify(loyaltyClient, times(0)).completePendingRedeem(any(TransactionRequest.class), anyString());
    }

    @Test
    public void completeRedeemLoyaltyForOnline_RedeemTransaction() {
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(loyaltyTransactionModel);
        when(loyaltyTransactionModel.getType()).thenReturn(TransactionType.REDEEM.name());
        when(loyaltyTransactionModel.getInvoiceNumber()).thenReturn("invoiceNo");
        loyaltyService.completeRedeemLoyaltyForOnline(orderModel);
        verify(loyaltyClient, times(1)).completePendingRedeem(any(TransactionRequest.class), anyString());
    }

    @Test
    public void updateRewardRedeemForOrder_Retail() {
        orderModel.setType(OrderType.RETAIL.toString());
        orderModel.setCardNumber("loyaltyCardNo");
        orderModel.setTotalRewardAmount(2000d);
        orderModel.setCustomerId(2L);
        when(customerService.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(new CustomerData());
        when(calculationService.saveRewardAmountToEntries(any(AbstractOrderModel.class), anyDouble(),
                anyDouble(), anyList(), anyBoolean())).thenReturn(orderModel);
        when(loyaltyClient.reward(any(TransactionRequest.class))).thenReturn(new TransactionData());

        loyaltyService.updateRewardRedeemForOrder(orderModel);
        verify(calculationService).calculateLoyaltyAmount(anyList(), anyLong());
    }

    @Test
    public void updateRewardRedeemForOrder_OnlineNotCompletedStatus() {
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setOrderStatus(OrderStatus.NEW.code());
        orderModel.setCardNumber("loyaltyCardNo");
        orderModel.setTotalRewardAmount(2000d);
        when(calculationService.saveRewardAmountToEntries(any(AbstractOrderModel.class), anyDouble(),
                anyDouble(), anyList(), anyBoolean())).thenReturn(orderModel);
        when(loyaltyClient.reward(any(TransactionRequest.class))).thenReturn(new TransactionData());

        loyaltyService.updateRewardRedeemForOrder(orderModel);
        verify(calculationService, times(0)).calculateLoyaltyAmount(anyList(), anyLong());
    }

    @Test
    public void updateRewardRedeemForOrder_OnlineCompletedStatus() {
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setOrderStatus(OrderStatus.COMPLETED.code());
        orderModel.setCardNumber("loyaltyCardNo");
        orderModel.setTotalRewardAmount(2000d);
        orderModel.setCustomerId(2L);
        when(customerService.getBasicCustomerInfo(anyLong(), anyLong())).thenReturn(new CustomerData());
        when(calculationService.saveRewardAmountToEntries(any(AbstractOrderModel.class), anyDouble(),
                anyDouble(), anyList(), anyBoolean())).thenReturn(orderModel);
        when(loyaltyClient.reward(any(TransactionRequest.class))).thenReturn(new TransactionData());

        loyaltyService.updateRewardRedeemForOrder(orderModel);
        verify(calculationService, times(1)).calculateLoyaltyAmount(anyList(), anyLong());
    }

    @Test
    public void cancelPendingRedeemForCancelOrder_notExistTransaction() {
        orderModel.setType(OrderType.ONLINE.toString());
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(null);
        loyaltyService.cancelPendingRedeemForCancelOrder(orderModel);
        verify(loyaltyClient, times(0)).cancelRedeem(any(), anyString());
        verify(loyaltyTransactionService, times(0)).save(any());
    }

    @Test
    public void cancelPendingRedeemForCancelOrder() {
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setInvoiceNumber("invoice number");
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setRedeemAmount(12000d);
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(loyaltyTransactionModel);
        loyaltyService.cancelPendingRedeemForCancelOrder(orderModel);
        verify(loyaltyClient, times(1)).cancelRedeem(any(), anyString());
        verify(loyaltyTransactionService, times(1)).save(any());
    }

    @Test
    public void populateRedeemKafkaForOnlineOrderNotAmount() {
        TransactionRequest transactionRequest = loyaltyService.populateRedeemKafkaForOnlineOrder(orderModel, 0d);
        assertNull(transactionRequest);
    }

    @Test
    public void populateRedeemKafkaForOnlineOrder() {
        Date date = DateUtil.parseDate("2020-02-22 10:10:00)", DateUtil.ISO_DATE_TIME_PATTERN);
        orderModel.setCreatedTime(date);
        orderModel.setCompanyId(1L);
        orderModel.setWarehouseId(2L);
        orderModel.setCardNumber("cardNumber");
        orderModel.setCode("orderCode");
        orderModel.setType("orderType");

        TransactionRequest transactionRequest = loyaltyService.populateRedeemKafkaForOnlineOrder(orderModel, 10000d);
        verify(transactionRequestPopulator).populate(any(OrderModel.class), any(TransactionRequest.class));
        assertEquals(10000, transactionRequest.getAmount(), 0);
        assertNull(transactionRequest.getReferInvoiceNumber());
    }

    @Test
    public void populateRefundKafkaForOnlineOrderNotExistedInvoice() {
        when(originModel.getCode()).thenReturn("orderCode");
        when(loyaltyTransactionService.findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.REDEEM.name(), TransactionType.REFUND.name()))).
                thenReturn(null);
        try {
            loyaltyService.populateRefundKafkaForOnlineOrder(orderModel, 1d);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_EXISTED_REFER_INVOICE_FOR_REFUND.code(), e.getCode());
        }
    }

    @Test
    public void populateRefundKafkaForOnlineOrder() {
        when(originModel.getCode()).thenReturn("orderCode");
        when(originModel.getCompanyId()).thenReturn(1L);
        when(originModel.getWarehouseId()).thenReturn(2L);
        when(originModel.getCardNumber()).thenReturn("cardNumber");
        when(loyaltyTransactionService.findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.REDEEM.name(), TransactionType.REFUND.name()))).
                thenReturn(loyaltyTransactionModel);
        when(loyaltyTransactionModel.getInvoiceNumber()).thenReturn("referInvoice");
        TransactionRequest transactionRequest = loyaltyService.populateRefundKafkaForOnlineOrder(originModel, 5000d);
        verify(loyaltyTransactionService).findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.REDEEM.name(), TransactionType.REFUND.name()));
        verify(transactionRequestPopulator).populate(any(OrderModel.class), any(TransactionRequest.class));
        assertEquals(5000, transactionRequest.getAmount(), 0);
    }

    @Test
    public void populateRevertKafkaForOnlineOrderNotExistedInvoice() {
        when(originModel.getCode()).thenReturn("orderCode");
        when(loyaltyTransactionService.findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()))).
                thenReturn(null);
        try {
            loyaltyService.populateRevertKafkaForOnlineOrder(orderModel, 1d);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_EXISTED_REFER_INVOICE_FOR_REVERT.code(), e.getCode());
        }
    }

    @Test
    public void populateRevertKafkaForOnlineOrder() {
        when(originModel.getCode()).thenReturn("orderCode");
        when(originModel.getCompanyId()).thenReturn(1L);
        when(originModel.getWarehouseId()).thenReturn(2L);
        when(originModel.getCardNumber()).thenReturn("cardNumber");
        when(loyaltyTransactionService.findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()))).
                thenReturn(loyaltyTransactionModel);
        when(loyaltyTransactionModel.getInvoiceNumber()).thenReturn("referInvoice");
        TransactionRequest transactionRequest = loyaltyService.populateRevertKafkaForOnlineOrder(originModel, 5000d);
        verify(loyaltyTransactionService).findLastByOrderCodeAndListType("orderCode", Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()));
        verify(transactionRequestPopulator).populate(any(OrderModel.class), any(TransactionRequest.class));
        assertEquals(5000, transactionRequest.getAmount(), 0);
        assertEquals("referInvoice", transactionRequest.getReferInvoiceNumber());
    }

    @Test
    public void populateCancelRedeemKafkaForOnlineOrder_loyaltyTransactionModel_null() {
        try {
            when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(null);
            loyaltyService.populateCancelRedeemKafkaForOnlineOrder(orderModel);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.HAS_NOT_EXISTED_REFER_INVOICE_FOR_REDEEM.message(), e.getMessage());
        }
    }

    @Test
    public void populateCancelRedeemKafkaForOnlineOrder() {
        PaymentMethodData paymentMethodData = new PaymentMethodData();
        Set<PaymentTransactionModel> paymentTransactionModels = new HashSet<>();
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setPaymentMethodId(2L);
        paymentTransactionModels.add(paymentTransactionModel);
        paymentMethodData.setId(2L);
        orderModel.setPaymentTransactions(paymentTransactionModels);
        orderModel.setRedeemAmount(10000d);
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setInvoiceNumber("invoice number");
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(loyaltyTransactionModel);
        when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
        TransactionRequest transactionRequest = loyaltyService.populateCancelRedeemKafkaForOnlineOrder(orderModel);
        verify(loyaltyTransactionService).findLastByOrderCodeAndListType(anyString(), anyList());
        verify(transactionRequestPopulator).populate(any(OrderModel.class), any(TransactionRequest.class));
        assertEquals(10000, transactionRequest.getAmount(), 0);
    }

    @Test
    public void populateCompleteRedeemKafkaForOnline_HasNotTransactionRedeem() {
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(null);
        TransactionRequest transactionRequest = loyaltyService.populateCompleteRedeemKafkaForOnlineOrder(orderModel);
        assertNull(transactionRequest);
    }

    @Test
    public void populateCompleteRedeemKafkaForOnline_RefundTransaction() {
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(loyaltyTransactionModel);
        when(loyaltyTransactionModel.getType()).thenReturn(TransactionType.REFUND.name());
        when(loyaltyTransactionModel.getInvoiceNumber()).thenReturn("invoiceNo");
        TransactionRequest transactionRequest = loyaltyService.populateCompleteRedeemKafkaForOnlineOrder(orderModel);
        assertNull(transactionRequest);
    }

    @Test
    public void populateCompleteRedeemKafkaForOnline_RedeemTransaction() {
        when(loyaltyTransactionService.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(loyaltyTransactionModel);
        when(loyaltyTransactionModel.getType()).thenReturn(TransactionType.REDEEM.name());
        when(loyaltyTransactionModel.getInvoiceNumber()).thenReturn("invoiceNo");
        TransactionRequest transactionRequest = loyaltyService.populateCompleteRedeemKafkaForOnlineOrder(orderModel);
        assertEquals("invoiceNo", transactionRequest.getInvoiceNumber());
    }

}
