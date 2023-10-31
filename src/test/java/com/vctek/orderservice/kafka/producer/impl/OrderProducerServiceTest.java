package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.dto.redis.AddressData;
import com.vctek.dto.redis.DistrictData;
import com.vctek.dto.redis.ProvinceData;
import com.vctek.dto.redis.WardData;
import com.vctek.kafka.data.CouponRedemptionDTO;
import com.vctek.kafka.data.OrderData;
import com.vctek.kafka.data.PromotionActionDTO;
import com.vctek.kafka.data.PromotionResultDTO;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.OrderKafkaOutStream;
import com.vctek.kafka.stream.OrderStatusKafkaOutStream;
import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.couponservice.model.CouponRedemptionModel;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.repository.AbstractPromotionActionRepository;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.repository.*;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.service.UserService;
import com.vctek.util.MoneySourceType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderProducerServiceTest {
    private KafkaProducerService kafkaProducerService;
    private OrderKafkaOutStream orderKafkaOutStream;
    private OrderStatusKafkaOutStream orderStatusKafkaOutStream;
    private CustomerService customerService;
    private CRMService crmService;
    private PaymentTransactionService paymentTransactionService;
    private OrderProducerServiceImpl orderProducerService;
    private OrderEntryRepository orderEntryRepository;
    private SubOrderEntryRepository subOrderEntryRepository;
    private ToppingOptionRepository optionRepository;
    private ToppingItemRepository toppingItemRepository;
    private OrderHistoryRepository orderHistoryRepository;
    @Mock
    private PromotionResultService promotionResultService;
    @Mock
    private AbstractPromotionActionRepository abstractPromotionActionRepository;

    @Mock
    private CouponRedemptionService couponRedemptionService;

    private OrderData orderData;
    private OrderModel orderModel;
    private List<OrderEntryModel> orderEntries;
    @Mock
    private CouponRedemptionModel couponRedemptionMock1;
    @Mock
    private CouponRedemptionModel couponRedemptionMock2;
    @Mock
    private CouponRedemptionModel couponRedemptionMock3;
    @Mock
    private CouponCodeModel couponCodeMock1;
    @Mock
    private CouponCodeModel couponCodeMock2;
    @Mock
    private CouponCodeModel couponCodeMock3;
    @Mock
    private CouponModel couponMock1;
    @Mock
    private CouponModel couponMock2;
    @Mock
    private CouponModel couponMock3;
    @Mock
    private PromotionSourceRuleModel sourceRuleMock1;
    @Mock
    private OrderSettingCustomerOptionService customerOptionService;
    @Mock
    private UserService userService;
    @Mock
    private TagService tagService;

    private OrderEntryModel generateOrderEntry(Long id, Long productId, Integer entryNumber) {
        OrderEntryModel entry = new OrderEntryModel();
        entry.setId(id);
        entry.setProductId(productId);
        entry.setEntryNumber(entryNumber);
        return entry;
    }

    private PromotionResultModel generatePromotionResult(Long id) {
        PromotionResultModel model = new PromotionResultModel();
        model.setId(id);
        return model;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        orderHistoryRepository = mock(OrderHistoryRepository.class);
        kafkaProducerService = mock(KafkaProducerService.class);
        orderKafkaOutStream = mock(OrderKafkaOutStream.class);
        orderStatusKafkaOutStream = mock(OrderStatusKafkaOutStream.class);
        customerService = mock(CustomerService.class);
        crmService = mock(CRMService.class);
        paymentTransactionService = mock(PaymentTransactionService.class);
        orderEntryRepository = mock(OrderEntryRepository.class);
        subOrderEntryRepository = mock(SubOrderEntryRepository.class);
        optionRepository = mock(ToppingOptionRepository.class);
        toppingItemRepository = mock(ToppingItemRepository.class);
        orderProducerService = new OrderProducerServiceImpl(kafkaProducerService, orderKafkaOutStream,
                orderStatusKafkaOutStream);

        orderProducerService.setPaymentTransactionService(paymentTransactionService);
        orderProducerService.setOrderEntryRepository(orderEntryRepository);
        orderProducerService.setSubOrderEntryRepository(subOrderEntryRepository);
        orderProducerService.setOptionRepository(optionRepository);
        orderProducerService.setToppingItemRepository(toppingItemRepository);
        orderProducerService.setCrmService(crmService);
        orderProducerService.setUserService(userService);
        orderProducerService.setOrderHistoryRepository(orderHistoryRepository);
        orderProducerService.setPromotionResultService(promotionResultService);
        orderProducerService.setAbstractPromotionActionRepository(abstractPromotionActionRepository);
        orderProducerService.setCouponRedemptionService(couponRedemptionService);
        orderProducerService.setCustomerOptionService(customerOptionService);
        orderProducerService.setTagService(tagService);
        orderEntries = new ArrayList<>();
        orderData = new OrderData();
        orderModel = new OrderModel();
        orderModel.setId(22222l);
        orderModel.getEntries().addAll(orderEntries);

        when(couponRedemptionService.findAllBy(orderModel)).thenReturn(Arrays.asList(couponRedemptionMock1, couponRedemptionMock2,
                couponRedemptionMock3));
        when(couponRedemptionMock1.getCouponCodeModel()).thenReturn(couponCodeMock1);
        when(couponRedemptionMock2.getCouponCodeModel()).thenReturn(couponCodeMock2);
        when(couponRedemptionMock3.getCouponCodeModel()).thenReturn(couponCodeMock3);
        when(couponCodeMock1.getCode()).thenReturn("code1");
        when(couponCodeMock2.getCode()).thenReturn("code2");
        when(couponCodeMock3.getCode()).thenReturn("code3");
        when(sourceRuleMock1.getCode()).thenReturn("sourceRuleMock1");
    }

    @Test
    public void sendOrderKafka() {
        OrderModel orderModel = new OrderModel();
        OrderSourceModel orderSourceModel = new OrderSourceModel();
        orderModel.setId(1l);
        orderModel.setOrderSourceModel(orderSourceModel);
        orderModel.setShippingAddressId(1l);
        orderModel.setCode("abc");
        orderModel.setCurrencyCode("VND");
        orderModel.setCustomerId(1l);
        orderModel.setCompanyId(1l);
        orderModel.setWarehouseId(1l);
        orderModel.setType("ONLINE");
        orderModel.setOrderStatus("success");
        orderModel.setTotalPrice(100000d);
        List<OrderEntryModel> entryModels = new ArrayList<>();
        OrderEntryModel entry = new OrderEntryModel();
        entry.setBasePrice(10000d);
        entry.setQuantity(10l);
        entryModels.add(entry);

        when(orderEntryRepository.findAllByOrder(any(OrderModel.class))).thenReturn(entryModels);
        SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
        subOrderEntryModel.setPrice(1000d);
        subOrderEntryModel.setQuantity(1);
        when(subOrderEntryRepository.findAllByOrderEntry(any(AbstractOrderEntryModel.class))).thenReturn(Collections.singletonList(subOrderEntryModel));
        when(optionRepository.findAllByOrderEntry(any(AbstractOrderEntryModel.class))).thenReturn(Collections.singletonList(new ToppingOptionModel()));
        ToppingItemModel itemModel = new ToppingItemModel();
        itemModel.setBasePrice(10000d);
        when(toppingItemRepository.findAllByToppingOptionModel(any(ToppingOptionModel.class))).thenReturn(Collections.singletonList(itemModel));

        AddressData addressData = new AddressData();
        DistrictData districtData = new DistrictData();
        districtData.setId(1l);
        addressData.setDistrictData(districtData);
        ProvinceData provinceData = new ProvinceData();
        provinceData.setId(1l);
        addressData.setProvinceData(provinceData);
        WardData wardData = new WardData();
        wardData.setId(1l);
        addressData.setWardData(wardData);
        when(crmService.getAddress(anyLong())).thenReturn(addressData);

        List<PaymentTransactionModel> paymentTransactions = new ArrayList<>();
        PaymentTransactionModel payment1 = new PaymentTransactionModel();
        payment1.setMoneySourceType(MoneySourceType.CASH.name());
        payment1.setAmount(1000d);
        paymentTransactions.add(payment1);
        PaymentTransactionModel payment2 = new PaymentTransactionModel();
        payment2.setMoneySourceType(MoneySourceType.BANK_ACCOUNT.name());
        payment2.setAmount(2000d);
        paymentTransactions.add(payment2);
        when(paymentTransactionService.findAllByOrderCode(anyString())).thenReturn(paymentTransactions);

        CustomerData customerData = new CustomerData();
        when(customerService.getCustomerById(anyLong(), anyLong())).thenReturn(customerData);
        OrderSettingCustomerModel groupModel = new OrderSettingCustomerModel();
        OrderSettingCustomerOptionModel optionModel = new OrderSettingCustomerOptionModel();
        optionModel.setOrderSettingCustomerModel(groupModel);
        when(customerOptionService.findAllByOrderId(anyLong())).thenReturn(Arrays.asList(optionModel));

        OrderHistoryModel orderHistoryModel = new OrderHistoryModel();
        orderHistoryModel.setCurrentStatus(orderHistoryModel.getCurrentStatus());
        when(orderHistoryRepository.findLastBy(anyLong())).thenReturn(Optional.of(orderHistoryModel));

        OrderEvent event = new OrderEvent(orderModel);
        event.setCurrentUserId(2L);
        orderProducerService.sendOrderKafka(event);
        verify(kafkaProducerService).send(any(KafkaMessage.class), any());
    }

    @Test
    public void sendChangeStatusKafkaConfirmed() {
        OrderHistoryModel orderHistoryModel = new OrderHistoryModel();
        orderHistoryModel.setCurrentStatus("CONFIRMED");
        OrderModel orderModel = new OrderModel();
        orderModel.setCode("abc");
        orderHistoryModel.setOrder(orderModel);
        orderProducerService.sendChangeStatusKafka(orderHistoryModel);
        verify(kafkaProducerService).send(any(KafkaMessage.class), any());
    }

    @Test
    public void sendChangeStatusKafkaSuccess() {
        OrderHistoryModel orderHistoryModel = new OrderHistoryModel();
        orderHistoryModel.setCurrentStatus("COMPLETED");
        OrderModel orderModel = new OrderModel();
        orderModel.setCode("abc");
        orderHistoryModel.setOrder(orderModel);
        orderProducerService.sendChangeStatusKafka(orderHistoryModel);
        verify(kafkaProducerService).send(any(KafkaMessage.class), any());
    }

    @Test
    public void populateOrderWithPromotion() {
        orderEntries.add(generateOrderEntry(41l, 11l, 0));
        orderEntries.add(generateOrderEntry(42l, 22l, 1));
        List<AbstractPromotionActionModel> actions1 = new ArrayList<>();
        RuleBasedOrderEntryAdjustActionModel entryAdjustActionModel = new RuleBasedOrderEntryAdjustActionModel();
        entryAdjustActionModel.setAmount(new BigDecimal(1000d));
        entryAdjustActionModel.setOrderEntryNumber(0);
        entryAdjustActionModel.setOrderEntryQuantity(1l);
        entryAdjustActionModel.setProductId(11l);
        actions1.add(entryAdjustActionModel);
        RuleBasedOrderAdjustTotalActionModel orderAdjustTotalActionModel = new RuleBasedOrderAdjustTotalActionModel();
        orderAdjustTotalActionModel.setAmount(new BigDecimal(2000d));
        actions1.add(orderAdjustTotalActionModel);

        RuleBasedFixedPriceProductActionModel fixedPriceProductActionModel = new RuleBasedFixedPriceProductActionModel();
        fixedPriceProductActionModel.setProductId(22l);
        actions1.add(fixedPriceProductActionModel);

        RuleBasedOrderAddProductActionModel addProductActionModel = new RuleBasedOrderAddProductActionModel();
        addProductActionModel.setProductId(23l);
        addProductActionModel.setQuantity(1);
        actions1.add(addProductActionModel);

        PromotionResultModel promotionResultModel = generatePromotionResult(1l);
        RuleBasedPromotionModel promotion = new RuleBasedPromotionModel();
        promotion.setCode("promotion-code");
        promotionResultModel.setPromotion(promotion);
        when(promotionResultService.findAllByOrder(orderModel)).thenReturn(Arrays.asList(promotionResultModel));
        when(abstractPromotionActionRepository.findAllByPromotionResult(promotionResultModel)).thenReturn(actions1);
        orderProducerService.populatePromotionResult(orderData, orderModel, orderEntries);
        assertEquals(1, orderData.getPromotionResults().size());
        PromotionResultDTO promotionResultDTO = orderData.getPromotionResults().get(0);
        assertEquals("promotion-code", promotionResultDTO.getPromotionCode());
        assertEquals(4, promotionResultDTO.getPromotionActions().size());
        for (PromotionActionDTO actionDTO : promotionResultDTO.getPromotionActions()) {
            if (RuleBasedOrderAdjustTotalActionModel.class.getSimpleName().equals(actionDTO.getPromotionActionDType())) {
                assertEquals(2000d, actionDTO.getAmount(), 0);
            } else if (RuleBasedOrderEntryAdjustActionModel.class.getSimpleName().equals(actionDTO.getPromotionActionDType())) {
                assertEquals(11l, actionDTO.getProductId(), 0);
                assertEquals(0, actionDTO.getOrderEntryNumber(), 0);
                assertEquals(41l, actionDTO.getOrderEntryId(), 0);
                assertEquals(1000d, actionDTO.getAmount(), 0);
            } else if (RuleBasedFixedPriceProductActionModel.class.getSimpleName().equals(actionDTO.getPromotionActionDType())) {
                assertEquals(22l, actionDTO.getProductId(), 0);
            } else if (RuleBasedOrderAddProductActionModel.class.getSimpleName().equals(actionDTO.getPromotionActionDType())) {
                assertEquals(23l, actionDTO.getProductId(), 0);
                assertEquals(1, actionDTO.getOrderEntryQuantity(), 0);
            }
        }

    }

    @Test
    public void populateOrderWithPromotion_AdjustPrice2Entries() {
        orderEntries.add(generateOrderEntry(41l, 11l, 0));
        orderEntries.add(generateOrderEntry(42l, 22l, 1));
        List<AbstractPromotionActionModel> actions1 = new ArrayList<>();
        List<AbstractPromotionActionModel> actions2 = new ArrayList<>();
        RuleBasedOrderEntryAdjustActionModel entryAdjustActionModel1 = new RuleBasedOrderEntryAdjustActionModel();
        entryAdjustActionModel1.setAmount(new BigDecimal(1000d));
        entryAdjustActionModel1.setOrderEntryNumber(0);
        entryAdjustActionModel1.setOrderEntryQuantity(1l);
        entryAdjustActionModel1.setProductId(11l);
        actions1.add(entryAdjustActionModel1);

        RuleBasedOrderEntryAdjustActionModel entryAdjustActionModel2 = new RuleBasedOrderEntryAdjustActionModel();
        entryAdjustActionModel2.setAmount(new BigDecimal(2000d));
        entryAdjustActionModel2.setOrderEntryNumber(1);
        entryAdjustActionModel2.setOrderEntryQuantity(1l);
        entryAdjustActionModel2.setProductId(22l);
        actions2.add(entryAdjustActionModel2);

        PromotionResultModel promotionResultModel = generatePromotionResult(1l);
        RuleBasedPromotionModel promotion = new RuleBasedPromotionModel();
        promotion.setCode("promotion-code");
        promotionResultModel.setPromotion(promotion);

        PromotionResultModel promotionResultModel2 = generatePromotionResult(2l);
        promotion.setCode("promotion-code");
        promotionResultModel2.setPromotion(promotion);

        when(promotionResultService.findAllByOrder(orderModel)).thenReturn(Arrays.asList(promotionResultModel, promotionResultModel2));
        when(abstractPromotionActionRepository.findAllByPromotionResult(promotionResultModel)).thenReturn(actions1);
        when(abstractPromotionActionRepository.findAllByPromotionResult(promotionResultModel2)).thenReturn(actions2);

        orderProducerService.populatePromotionResult(orderData, orderModel, orderEntries);
        assertEquals(2, orderData.getPromotionResults().size());
        Optional<PromotionActionDTO> promotionActionDTO1 = orderData.getPromotionResults().stream()
                .flatMap(ps -> ps.getPromotionActions().stream()).filter(ac -> ac.getProductId().equals(11l)).findFirst();

        Optional<PromotionActionDTO> promotionActionDTO2 = orderData.getPromotionResults().stream()
                .flatMap(ps -> ps.getPromotionActions().stream()).filter(ac -> ac.getProductId().equals(22l)).findFirst();

        assertEquals(true, promotionActionDTO1.isPresent());
        assertEquals(true, promotionActionDTO2.isPresent());
        assertEquals(0, promotionActionDTO1.get().getOrderEntryNumber(), 0);
        assertEquals(41l, promotionActionDTO1.get().getOrderEntryId(), 0);
        assertEquals(1000d, promotionActionDTO1.get().getAmount(), 0);

        assertEquals(1, promotionActionDTO2.get().getOrderEntryNumber(), 0);
        assertEquals(42, promotionActionDTO2.get().getOrderEntryId(), 0);
        assertEquals(2000d, promotionActionDTO2.get().getAmount(), 0);

    }

    @Test
    public void populateCouponRedemption_NotUsingCoupon() {
        when(couponRedemptionService.findAllBy(orderModel)).thenReturn(Collections.EMPTY_LIST);
        orderProducerService.populateCouponRedemption(orderData, orderModel);
        assertNull(orderData.getCouponRedemptionList());
    }

    @Test
    public void populateCouponRedemption_UsingOneCoupon_OneQuantity() {
        when(couponRedemptionService.findAllBy(orderModel)).thenReturn(Arrays.asList(couponRedemptionMock1));
        when(couponCodeMock1.getCoupon()).thenReturn(couponMock1);
        when(couponMock1.getPromotionSourceRule()).thenReturn(sourceRuleMock1);
        when(couponMock1.getId()).thenReturn(1l);
        orderProducerService.populateCouponRedemption(orderData, orderModel);
        assertEquals(1, orderData.getCouponRedemptionList().size());
        CouponRedemptionDTO couponRedemptionDTO = orderData.getCouponRedemptionList().get(0);
        assertEquals("code1", couponRedemptionDTO.getCouponCode());
        assertEquals("sourceRuleMock1", couponRedemptionDTO.getPromotionCode());
        assertEquals(1, couponRedemptionDTO.getCouponId(), 0);
        assertEquals(1, couponRedemptionDTO.getRedemptionQuantity(), 0);
    }

    @Test
    public void populateCouponRedemption_UsingOneCoupon_2Quantity() {
        when(couponRedemptionService.findAllBy(orderModel)).thenReturn(Arrays.asList(couponRedemptionMock1, couponRedemptionMock2));
        when(couponRedemptionMock2.getCouponCodeModel()).thenReturn(couponCodeMock1);
        when(couponCodeMock1.getCoupon()).thenReturn(couponMock1);
        when(couponMock1.getPromotionSourceRule()).thenReturn(sourceRuleMock1);
        when(couponMock1.getId()).thenReturn(1l);
        orderProducerService.populateCouponRedemption(orderData, orderModel);
        assertEquals(1, orderData.getCouponRedemptionList().size());
        CouponRedemptionDTO couponRedemptionDTO = orderData.getCouponRedemptionList().get(0);
        assertEquals("code1", couponRedemptionDTO.getCouponCode());
        assertEquals("sourceRuleMock1", couponRedemptionDTO.getPromotionCode());
        assertEquals(1, couponRedemptionDTO.getCouponId(), 0);
        assertEquals(2, couponRedemptionDTO.getRedemptionQuantity(), 0);
    }

    @Test
    public void populateCouponRedemption_Using3Coupons_1Quantity() {
        when(couponCodeMock1.getCoupon()).thenReturn(couponMock1);
        when(couponCodeMock2.getCoupon()).thenReturn(couponMock2);
        when(couponCodeMock3.getCoupon()).thenReturn(couponMock3);
        when(couponMock1.getPromotionSourceRule()).thenReturn(sourceRuleMock1);
        when(couponMock1.getId()).thenReturn(1l);

        when(couponMock2.getPromotionSourceRule()).thenReturn(new PromotionSourceRuleModel());
        when(couponMock2.getId()).thenReturn(2l);

        when(couponMock3.getPromotionSourceRule()).thenReturn(new PromotionSourceRuleModel());
        when(couponMock3.getId()).thenReturn(3l);
        orderProducerService.populateCouponRedemption(orderData, orderModel);
        assertEquals(3, orderData.getCouponRedemptionList().size());
        for (CouponRedemptionDTO couponRedemptionDTO : orderData.getCouponRedemptionList()) {
            if ("code1".equalsIgnoreCase(couponRedemptionDTO.getCouponCode())) {
                assertEquals("code1", couponRedemptionDTO.getCouponCode());
                assertEquals("sourceRuleMock1", couponRedemptionDTO.getPromotionCode());
                assertEquals(1, couponRedemptionDTO.getCouponId(), 0);
                assertEquals(1, couponRedemptionDTO.getRedemptionQuantity(), 0);
            } else if ("code2".equalsIgnoreCase(couponRedemptionDTO.getCouponCode())) {
                assertEquals("code2", couponRedemptionDTO.getCouponCode());
                assertEquals(2, couponRedemptionDTO.getCouponId(), 0);
                assertEquals(1, couponRedemptionDTO.getRedemptionQuantity(), 0);
            } else if ("code3".equalsIgnoreCase(couponRedemptionDTO.getCouponCode())) {
                assertEquals("code3", couponRedemptionDTO.getCouponCode());
                assertEquals(3, couponRedemptionDTO.getCouponId(), 0);
                assertEquals(1, couponRedemptionDTO.getRedemptionQuantity(), 0);
            }
        }
    }

}
