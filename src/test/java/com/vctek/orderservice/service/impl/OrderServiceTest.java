package com.vctek.orderservice.service.impl;

import com.vctek.kafka.data.BillDto;
import com.vctek.migration.dto.OrderBillLinkDTO;
import com.vctek.migration.dto.PaidAmountOrderData;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.dto.request.SaleQuantityRequest;
import com.vctek.orderservice.feignclient.FinanceClient;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.repository.AbstractPromotionActionRepository;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionOrderEntryConsumedRepository;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.repository.*;
import com.vctek.orderservice.repository.dao.OrderDAO;
import com.vctek.orderservice.repository.dao.OrderSaleDAO;
import com.vctek.orderservice.service.ModelService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.service.SubOrderEntryService;
import com.vctek.orderservice.service.TagService;
import com.vctek.orderservice.service.event.OrderTagEvent;
import com.vctek.redis.ProductData;
import com.vctek.util.BillType;
import com.vctek.util.ComboType;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartModel cart;
    private AbstractOrderEntryModel entry = new CartEntryModel();
    private OrderServiceImpl orderService;
    private Set<PromotionResultModel> promotionResults = new HashSet<>();
    private PromotionResultModel promotionResult = new PromotionResultModel();
    private AbstractPromotionModel promotion = new RuleBasedPromotionModel();
    private Set<PromotionOrderEntryConsumedModel> consumedEntries = new HashSet<>();
    private PromotionOrderEntryConsumedModel consumedEntry = new PromotionOrderEntryConsumedModel();
    private Set<AbstractPromotionActionModel> actions = new HashSet<>();
    private AbstractPromotionActionModel action = new PromotionOrderAdjustTotalActionModel();
    private SaleQuantityRequest saleRequest;
    @Mock
    private OrderEntryRepository orderEntryRepository;
    @Mock
    private FinanceClient financeClient;
    @Mock
    private OrderSaleDAO orderSaleDAO;
    @Mock
    private SaleQuantity saleOrderEntryMock1;
    @Mock
    private SaleQuantity saleOrderEntryMock2;
    private UpdateReturnOrderBillDTO updateReturnOrderDto;
    @Mock
    private ModelService modelService;
    @Mock
    private OrderModel orderMock;
    @Mock
    private OrderEntryModel entryMock1;
    @Mock
    private ProductService productService;
    private ArgumentCaptor<Long> returnQtyCaptor = ArgumentCaptor.forClass(Long.class);
    @Mock
    private ProductData productDataMock;
    @Mock
    private PromotionResultService promotionResultService;
    @Mock
    private AbstractPromotionActionRepository abstractPromotionActionRepository;
    @Mock
    private PromotionOrderEntryConsumedRepository promotionOrderEntryConsumedRepository;
    @Mock
    private SubOrderEntryService subOrderEntryService;
    @Mock
    private ToppingOptionRepository toppingOptionRepository;
    @Mock
    private ToppingItemRepository toppingItemRepository;
    @Mock
    private OrderSettingCustomerOptionRepository settingCustomerOptionRepository;
    @Mock
    private OrderHasCouponRepository orderHasCouponRepository;
    @Mock
    private AddTagRequest addTagRequest;
    @Mock
    private TagService tagService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private OrderDAO orderDAO;

    private UpdateReturnOrderBillDetail generateReturnEntry(Long entryId, boolean deleted, Integer originQty, int qty) {
        UpdateReturnOrderBillDetail detail = new UpdateReturnOrderBillDetail();
        detail.setOrderEntryId(entryId);
        detail.setDeleted(deleted);
        detail.setOriginQuantity(originQty);
        detail.setQuantity(qty);
        return detail;
    }
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        consumedEntry.setOrderEntry(entry);
        consumedEntry.setPromotionResult(promotionResult);
        consumedEntries.add(consumedEntry);
        actions.add(action);
        orderService = new OrderServiceImpl(orderRepository);
        orderService.setOrderEntryRepository(orderEntryRepository);
        orderService.setFinanceClient(financeClient);
        orderService.setOrderSaleDAO(orderSaleDAO);
        orderService.setModelService(modelService);
        orderService.setProductService(productService);
        orderService.setAbstractPromotionActionRepository(abstractPromotionActionRepository);
        orderService.setPromotionOrderEntryConsumedRepository(promotionOrderEntryConsumedRepository);
        orderService.setPromotionResultService(promotionResultService);
        orderService.setSubOrderEntryService(subOrderEntryService);
        orderService.setToppingOptionRepository(toppingOptionRepository);
        orderService.setToppingItemRepository(toppingItemRepository);
        orderService.setSettingCustomerOptionRepository(settingCustomerOptionRepository);
        orderService.setOrderHasCouponRepository(orderHasCouponRepository);
        orderService.setTagService(tagService);
        orderService.setApplicationEventPublisher(applicationEventPublisher);
        orderService.setOrderDAO(orderDAO);
        promotionResult.setPromotion(promotion);
        promotionResult.setConsumedEntries(consumedEntries);
        promotionResult.setActions(actions);
        promotionResults.add(promotionResult);
        when(cart.getPromotionResults()).thenReturn(promotionResults);
        when(promotionResultService.findAllByOrder(cart)).thenReturn(promotionResults.stream().collect(Collectors.toList()));
        when(abstractPromotionActionRepository.findAllByPromotionResult(promotionResult)).thenReturn(actions.stream().collect(Collectors.toList()));
        when(promotionOrderEntryConsumedRepository.findAllByPromotionResult(promotionResult)).thenReturn(consumedEntries.stream().collect(Collectors.toList()));
        when(cart.getEntries()).thenReturn(Arrays.asList(entry));
        saleRequest = new SaleQuantityRequest();
        updateReturnOrderDto = new UpdateReturnOrderBillDTO();
        updateReturnOrderDto.setOriginOrder(orderMock);

        when(productService.getBasicProductDetail(anyLong())).thenReturn(productDataMock);
        when(orderMock.getCompanyId()).thenReturn(1l);
    }

    @Test
    public void findById() {
        orderService.findById(1l);
        verify(orderRepository).findById(anyLong());
    }

    @Test
    public void save() {
        orderService.save(new OrderModel());
        verify(orderRepository).save(any(OrderModel.class));
    }
    @Test
    public void createOrderFromCart() {
        entry.setId(1l);
        entry.setBasePrice(100000d);
        entry.setProductId(11l);
        entry.setQuantity(2l);
        entry.setEntryNumber(0);
        entry.setTotalDiscount(10d);

        OrderModel orderModel = orderService.createOrderFromCart(cart);
        verify(cart).getCode();
        verify(cart).getSubTotal();
        verify(cart).getEntries();
        verify(cart).getCurrencyCode();
        verify(cart).getDeliveryCost();
        verify(cart).getDiscountValues();
        verify(cart).getDiscount();
        verify(cart).getPaymentCost();
        verify(cart).getTotalPrice();
        verify(cart).getDiscountType();
        verify(cart).getFixedDiscount();
        verify(cart).getVat();
        verify(cart).getVatType();
        verify(cart).getVatDate();
        verify(cart).getVatNumber();
        verify(cart).getNote();
        verify(cart).getCompanyId();
        verify(cart).getCreateByUser();
        verify(cart).getWarehouseId();
        verify(cart).getCustomerId();
        verify(cart).getType();
        verify(cart).getTotalTax();
        verify(cart).getTotalDiscount();
        verify(cart).getSubTotalDiscount();
        verify(cart).getGuid();
        verify(cart).getFinalPrice();
        verify(cart).getOrderHasCouponCodeModels();
        verify(cart).getTotalTax();
        verify(cart).getPaymentCost();


        AbstractOrderEntryModel actualEntry = orderModel.getEntries().get(0);
        assertNull(actualEntry.getId());
        assertEquals(actualEntry.getQuantity(), entry.getQuantity());
        assertEquals(actualEntry.getProductId(), entry.getProductId());
        assertEquals(actualEntry.getEntryNumber(), entry.getEntryNumber());
        assertEquals(actualEntry.getBasePrice(), entry.getBasePrice());
        assertEquals(actualEntry.getTotalDiscount(), entry.getTotalDiscount());

        PromotionResultModel actual = orderModel.getPromotionResults().iterator().next();
        PromotionResultModel oldPromotionResult = promotionResults.iterator().next();
        assertNotEquals(actual, oldPromotionResult);
        assertNotEquals(actual.getConsumedEntries().iterator().next(),
                oldPromotionResult.getConsumedEntries().iterator().next());
    }

    @Test
    public void findAll() {
        orderService.findAll(PageRequest.of(0, 10));
        verify(orderRepository).findAllByDeleted(eq(false), any(Pageable.class));
    }

    @Test
    public void findByCodeAndCompanyIdAndDeleted() {
        orderService.findByCodeAndCompanyIdAndDeleted(anyString(),anyLong(),anyBoolean());
        verify(orderRepository).findByCodeAndCompanyIdAndDeleted(anyString(),anyLong(),anyBoolean());
    }

    @Test
    public void findAllEntryBy() {
        saleRequest.setCompanyId(1l);
        saleRequest.setProductIds("1111, 2222,  3333");
        saleRequest.setToDate(Calendar.getInstance().getTime());
        saleRequest.setFromDate(Calendar.getInstance().getTime());
        when(orderSaleDAO.findEntrySaleQuantity(any()))
                .thenReturn(Arrays.asList(saleOrderEntryMock1, saleOrderEntryMock2), Collections.emptyList());

        List<SaleQuantity> entries = orderService.findAllSaleEntryBy(saleRequest);
        assertEquals(2, entries.size());
    }

    @Test
    public void findAllByCompanyIdAndCreateTime() {
        when(orderRepository.findAllByCompanyIdAndCreatedTimeBetween(anyLong(), any(Date.class), any(Date.class), any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList(new OrderModel())));
        orderService.findAllByCompanyIdAndCreateTime(1l, Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), PageRequest.of(0, 2));
        verify(orderRepository).findAllByCompanyIdAndCreatedTimeBetween(anyLong(), any(Date.class), any(Date.class), any(Pageable.class));
    }

    @Test
    public void findAllByTypeAndFromDate() {
        when(orderRepository.findAllByCompanyIdAndTypeAndCreatedTimeGreaterThanEqualAndDeleted(anyLong(), anyString(), any(Date.class), anyBoolean(), any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList(new OrderModel())));
        orderService.findAllByCompanyIdAndTypeAndFromDate(PageRequest.of(0, 2), 2l, "online", Calendar.getInstance().getTime());
        verify(orderRepository).findAllByCompanyIdAndTypeAndCreatedTimeGreaterThanEqualAndDeleted(anyLong(), anyString(), any(Date.class), anyBoolean(), any(Pageable.class));
    }

    @Test
    public void updatePaidAmountOrder() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCode("12345");
        orderModel.setCompanyId(2l);
        when(financeClient.getPaidAmountOrder(anyString(), anyLong())).thenReturn(10d);
        orderService.updatePaidAmountOrder(orderModel);
        verify(financeClient).getPaidAmountOrder(anyString(), anyLong());
        verify(orderRepository).save(orderModel);
    }

    @Test
    public void updateReturnQuantityForEntries_EmptyEntries() {
        double diffAmount = orderService.updateAndCalculateDiffRevertAmountOfReturnEntries(updateReturnOrderDto);
        verify(orderEntryRepository, times(0)).saveAll(anyCollection());
        assertEquals(0, diffAmount, 0);
    }

    @Test
    public void updateReturnQuantityForEntries_deleteReturnEntry_FullReturn() {
        updateReturnOrderDto.setEntries(Arrays.asList(generateReturnEntry(1l, true, 3, 0)));
        when(orderEntryRepository.findByOrderAndId(orderMock, 1l)).thenReturn(entryMock1);
        when(entryMock1.getQuantity()).thenReturn(3l);
        when(entryMock1.getRewardAmount()).thenReturn(6000d);
        when(entryMock1.getReturnQuantity()).thenReturn(3l);

        double diffAmount = orderService.updateAndCalculateDiffRevertAmountOfReturnEntries(updateReturnOrderDto);
        verify(orderEntryRepository, times(1)).saveAll(anyCollection());
        verify(entryMock1).setReturnQuantity(returnQtyCaptor.capture());
        assertEquals(0, returnQtyCaptor.getValue(), 0);
        assertEquals(-6000, diffAmount, 0);
    }

    @Test
    public void updateReturnQuantityForEntries_deleteReturnEntry_SecondTime() {
        updateReturnOrderDto.setEntries(Arrays.asList(generateReturnEntry(1l, true, 1, 0)));
        when(orderEntryRepository.findByOrderAndId(orderMock, 1l)).thenReturn(entryMock1);
        when(entryMock1.getQuantity()).thenReturn(3l);
        when(entryMock1.getRewardAmount()).thenReturn(6000d);
        when(entryMock1.getReturnQuantity()).thenReturn(2l);

        double diffAmount = orderService.updateAndCalculateDiffRevertAmountOfReturnEntries(updateReturnOrderDto);
        verify(orderEntryRepository, times(1)).saveAll(anyCollection());
        verify(entryMock1).setReturnQuantity(returnQtyCaptor.capture());
        assertEquals(1, returnQtyCaptor.getValue(), 0);
        assertEquals(-2000, diffAmount, 0);
    }

    @Test
    public void updateReturnQuantityForEntries_updateReturnEntry_firstTime() {
        updateReturnOrderDto.setEntries(Arrays.asList(generateReturnEntry(1l, false, 1, 2)));
        when(orderEntryRepository.findByOrderAndId(orderMock, 1l)).thenReturn(entryMock1);
        when(entryMock1.getQuantity()).thenReturn(3l);
        when(entryMock1.getRewardAmount()).thenReturn(6000d);
        when(entryMock1.getReturnQuantity()).thenReturn(1l);

        double diffAmount = orderService.updateAndCalculateDiffRevertAmountOfReturnEntries(updateReturnOrderDto);
        verify(orderEntryRepository, times(1)).saveAll(anyCollection());
        verify(entryMock1).setReturnQuantity(returnQtyCaptor.capture());
        assertEquals(2, returnQtyCaptor.getValue(), 0);
        assertEquals(2000, diffAmount, 0);
    }


    @Test
    public void updateReturnQuantityForEntries_updateReturnEntry_secondTime() {
        updateReturnOrderDto.setEntries(Arrays.asList(generateReturnEntry(1l, false, 2, 1)));
        when(orderEntryRepository.findByOrderAndId(orderMock, 1l)).thenReturn(entryMock1);
        when(entryMock1.getQuantity()).thenReturn(3l);
        when(entryMock1.getRewardAmount()).thenReturn(6000d);
        when(entryMock1.getReturnQuantity()).thenReturn(3l);

        double diffAmount = orderService.updateAndCalculateDiffRevertAmountOfReturnEntries(updateReturnOrderDto);
        verify(orderEntryRepository, times(1)).saveAll(anyCollection());
        verify(entryMock1).setReturnQuantity(returnQtyCaptor.capture());
        assertEquals(2, returnQtyCaptor.getValue(), 0);
        assertEquals(-2000, diffAmount, 0);
    }

    @Test
    public void findByCodeAndCompanyIdAndOrderTypeAndDeleted() {
        when(orderRepository.findByCodeAndCompanyIdAndTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean())).thenReturn(Optional.empty());
        orderService.findByCodeAndCompanyIdAndOrderTypeAndDeleted("1111", 2l, "ONLINE", false);
        verify(orderRepository).findByCodeAndCompanyIdAndTypeAndDeleted(anyString(), anyLong(), anyString(), anyBoolean());
    }

    @Test
    public void findEntryBy() {
        when(orderEntryRepository.findByOrderAndEntryNumber(any(), anyInt())).thenReturn(new OrderEntryModel());
        orderService.findEntryBy(new OrderModel(), 0);
        verify(orderEntryRepository).findByOrderAndEntryNumber(any(), anyInt());
    }

    @Test
    public void findAllByCompanyIdAndType() {
        when(orderRepository.findAllByCompanyIdAndTypeAndDeleted(anyLong(), anyString(), anyBoolean(), any(PageRequest.class))).thenReturn(new PageImpl<>(new ArrayList<>()));
        orderService.findAllByCompanyIdAndType(PageRequest.of(0, 2), 2l, "online");
        verify(orderRepository).findAllByCompanyIdAndTypeAndDeleted(anyLong(), anyString(), anyBoolean(), any(PageRequest.class));
    }

    @Test
    public void findAllSaleComboEntries() {
        SaleQuantityRequest request = new SaleQuantityRequest();
        when(orderSaleDAO.findComboEntrySaleQuantity(any(SaleQuantityRequest.class))).thenReturn(Arrays.asList(new SaleQuantity()));
        orderService.findAllSaleComboEntries(request);
        verify(orderSaleDAO).findComboEntrySaleQuantity(any(SaleQuantityRequest.class));
    }

    @Test
    public void findOrderCombo() {
        when(orderRepository.findOrderCombo(anyLong())).thenReturn(Arrays.asList(new OrderModel()));
        orderService.findOrderCombo(2l);
        verify(orderRepository).findOrderCombo(anyLong());
    }

    @Test
    public void findAllByCompanyId() {
        when(orderRepository.findAllByCompanyIdOrderByIdAsc(anyLong(), any(PageRequest.class))).thenReturn(new PageImpl<>(Arrays.asList(new OrderModel())));
        orderService.findAllByCompanyId(2l, PageRequest.of(0, 10));
        verify(orderRepository).findAllByCompanyIdOrderByIdAsc(anyLong(), any(PageRequest.class));
    }

    @Test
    public void isValidEntryForPromotion_NormalEntry_ReturnTrue() {
        when(entryMock1.getComboType()).thenReturn(null);
        when(entryMock1.getSubOrderEntries()).thenReturn(null);

        assertTrue(orderService.isValidEntryForPromotion(entryMock1));
    }

    @Test
    public void isValidEntryForPromotion_NormalSaleOffEntry_ReturnFalse() {
        when(entryMock1.getComboType()).thenReturn(null);
        when(entryMock1.getSubOrderEntries()).thenReturn(null);
        when(entryMock1.isSaleOff()).thenReturn(true);

        assertFalse(orderService.isValidEntryForPromotion(entryMock1));
    }

    @Test
    public void isValidEntryForPromotion_ComboEntryNotAllowAppliedPromotion_ReturnFalse() {
        when(entryMock1.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(entryMock1.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(new SubOrderEntryModel())));
        when(productDataMock.isAllowAppliedPromotion()).thenReturn(false);
        assertFalse(orderService.isValidEntryForPromotion(entryMock1));
    }

    @Test
    public void isValidEntryForPromotion_ComboEntryAllowAppliedPromotion_ReturnTrue() {
        when(entryMock1.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(entryMock1.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(new SubOrderEntryModel())));
        when(productDataMock.isAllowAppliedPromotion()).thenReturn(true);
        assertTrue(orderService.isValidEntryForPromotion(entryMock1));
    }

    @Test
    public void updateBillForOrder_BillHasNotOrder() {
        BillDto billDto = new BillDto();
        orderService.updateOrderBill(billDto);
        verify(orderRepository, times(0)).findByCodeAndCompanyId(anyString(), anyLong());
        verify(orderRepository, times(0)).updateBillToOrder(anyLong(), anyString(), anyLong());
    }

    @Test
    public void updateBillForOrder_OrderTypeNotOnline() {
        BillDto billDto = new BillDto();
        billDto.setOrderCode("code");
        billDto.setCompanyId(1l);
        billDto.setOrderType(OrderType.RETAIL.toString());
        orderService.updateOrderBill(billDto);
        verify(orderRepository, times(0)).findByCodeAndCompanyId(anyString(), anyLong());
        verify(orderRepository, times(0)).updateBillToOrder(anyLong(), anyString(), anyLong());
    }

    @Test
    public void updateBillForOrder_OrderHasNotBill() {
        BillDto billDto = new BillDto();
        billDto.setId(1l);
        billDto.setCompanyId(1l);
        billDto.setOrderCode("code");
        billDto.setOrderType(OrderType.ONLINE.toString());
        billDto.setType(BillType.RETURN_BILL.code());
        OrderModel orderModel = new OrderModel();
        when(orderRepository.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(Optional.of(orderModel));
        orderService.updateOrderBill(billDto);
        verify(orderRepository, times(1)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());
        verify(orderRepository, times(1)).updateBillToOrder(anyLong(), anyString(), anyLong());
    }

    @Test
    public void updateBillForOrder_OrderHasSameBill() {
        BillDto billDto = new BillDto();
        billDto.setId(1l);
        billDto.setCompanyId(1l);
        billDto.setOrderCode("code");
        billDto.setType(BillType.RETURN_BILL.code());
        billDto.setOrderType(OrderType.ONLINE.toString());
        OrderModel orderModel = new OrderModel();
        orderModel.setBillId(1l);
        when(orderRepository.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(Optional.of(orderModel));
        orderService.updateOrderBill(billDto);
        verify(orderRepository, times(1)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());
        verify(orderRepository, times(1)).updateBillToOrder(anyLong(), anyString(), anyLong());
    }

    @Test
    public void updateBillForOrder_OrderHasDifferentBill() {
        BillDto billDto = new BillDto();
        billDto.setId(1l);
        billDto.setCompanyId(1l);
        billDto.setOrderCode("code");
        billDto.setType(BillType.RETURN_BILL.code());
        billDto.setOrderType(OrderType.ONLINE.toString());
        OrderModel orderModel = new OrderModel();
        orderModel.setBillId(2l);
        when(orderRepository.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(Optional.of(orderModel));
        orderService.updateOrderBill(billDto);
        verify(orderRepository, times(1)).findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean());
        verify(orderRepository, times(1)).updateBillToOrder(anyLong(), anyString(), anyLong());
    }

    @Test
    public void linkBillToOrder() {
        List<OrderBillLinkDTO> orderBillLinkDTOS = new ArrayList<>();
        OrderBillLinkDTO dto = new OrderBillLinkDTO() ;
        dto.setCompanyId(2L);
        dto.setOrderCode("code");
        orderBillLinkDTOS.add(dto);
        when(orderRepository.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderModel());
        orderService.linkBillToOrder(orderBillLinkDTOS);
        verify(orderRepository).saveAll(anyList());
    }

    @Test
    public void updatePaidAmountOrders() {
        PaidAmountOrderData data = new PaidAmountOrderData();
        data.setCompanyId(2L);
        data.setOrderCode("code");
        data.setPaidAmount(100d);
        orderService.updatePaidAmountOrder(Arrays.asList(data));
        verify(orderRepository, times(1)).updatePaidAmount(anyDouble(), anyString(), anyLong());
    }

    @Test
    public void findAllByAndCompanyIdAndOrderTypes() {
        orderService.findAllByAndCompanyIdAndOrderTypes(2L, Arrays.asList("type"), PageRequest.of(0, 2));
        verify(orderRepository).findAllByCompanyIdAndTypeIn(anyLong(), anyList(), any());
    }

    @Test
    public void findByCompanyIdAndOrderCodeIn() {
        orderService.findByCompanyIdAndOrderCodeIn(2L, Arrays.asList("type"));
        verify(orderRepository).findAllByCompanyIdAndCodeIn(anyLong(), anyList());
    }

    @Test
    public void findEntryById() {
        orderService.findEntryBy(2L, orderMock);
        verify(orderEntryRepository).findByIdAndOrder(anyLong(), any());
    }

    @Test
    public void findAllByFromDate() {
        orderService.findAllByFromDate(PageRequest.of(0, 2), Calendar.getInstance().getTime());
        verify(orderRepository).findAllByCreatedTimeGreaterThanEqual(any(), any());
    }

    @Test
    public void findAllByAndCompanyIdFromDate() {
        orderService.findAllByAndCompanyIdFromDate(2L, Calendar.getInstance().getTime(), PageRequest.of(0, 2));
        verify(orderRepository).findAllByCompanyIdAndCreatedTimeGreaterThanEqual(anyLong(), any(), any());
    }

    @Test
    public void resetPreAndHoldingStockOf() {
        OrderModel orderModel = new OrderModel();
        AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
        entryModel.setHolding(true);
        orderModel.setEntries(Arrays.asList(entryModel));
        orderService.resetPreAndHoldingStockOf(orderModel);
        assertFalse(entryModel.isHolding());
    }

    @Test
    public void findOrderByExternalIdAndSellSignal() {
        CartInfoParameter cartInfoParameter = new CartInfoParameter();
        cartInfoParameter.setCompanyId(2L);
        cartInfoParameter.setExternalId(2L);
        cartInfoParameter.setSellSignal("abc");
        orderService.findOrderByExternalIdAndSellSignal(cartInfoParameter);
        verify(orderRepository).findByCompanyIdAndExternalIdAndSellSignal(anyLong(), anyLong(), anyString());
    }

    @Test
    public void holdingStockAndResetPreStockOf() {
        OrderEntryModel entryModel = new OrderEntryModel();
        entryModel.setQuantity(20L);
        entryModel.setHolding(false);
        entryModel.setPreOrder(true);
        entryModel.setHoldingStock(12L);
        orderService.holdingStockAndResetPreStockOf(Arrays.asList(entryModel));
        assertFalse(entryModel.isPreOrder());
        assertTrue(entryModel.isHolding());
        assertEquals(20L, entryModel.getHoldingStock(), 0);
    }

    @Test
    public void resetPreAndHoldingStockOfEntries() {
        OrderEntryModel entryModel = new OrderEntryModel();
        entryModel.setHolding(true);
        entryModel.setHoldingStock(12L);
        orderService.resetPreAndHoldingStockOfEntries(Arrays.asList(entryModel));
        assertFalse(entryModel.isHolding());
    }

    @Test
    public void cloneSubOrderEntriesForKafkaImportOrderStatus() {
        OrderEntryModel cloneEntry = new OrderEntryModel();
        AbstractOrderEntryModel entryModel = new OrderEntryModel();
        SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
        when(subOrderEntryService.findAllBy(any())).thenReturn(Arrays.asList(subOrderEntryModel));
        orderService.cloneSubOrderEntriesForKafkaImportOrderStatus(entryModel, cloneEntry);
        verify(subOrderEntryService).findAllBy(any());
        assertEquals(1, cloneEntry.getSubOrderEntries().size());
    }

    @Test
    public void cloneToppingOptionsForKafkaImportOrderStatus() {
        OrderEntryModel cloneEntry = new OrderEntryModel();
        AbstractOrderEntryModel entryModel = new OrderEntryModel();
        ToppingOptionModel optionModel = new ToppingOptionModel();
        ToppingItemModel itemModel = new ToppingItemModel();
        when(toppingOptionRepository.findAllByOrderEntry(any())).thenReturn(Arrays.asList(optionModel));
        when(toppingItemRepository.findAllByToppingOptionModel(any())).thenReturn(Arrays.asList(itemModel));
        orderService.cloneToppingOptionsForKafkaImportOrderStatus(entryModel, cloneEntry);
        verify(toppingOptionRepository).findAllByOrderEntry(any());
        verify(toppingItemRepository).findAllByToppingOptionModel(any());
        assertEquals(1, cloneEntry.getToppingOptionModels().size());
    }

    @Test
    public void cloneSettingCustomerOption() {
        OrderModel order = new OrderModel();
        order.setId(2L);
        OrderModel cloneOrder = new OrderModel();
        OrderSettingCustomerOptionModel setting = new OrderSettingCustomerOptionModel();
        when(settingCustomerOptionRepository.findAllByOrderId(any())).thenReturn(Arrays.asList(setting));
        orderService.cloneSettingCustomerOption(order, cloneOrder);
        verify(settingCustomerOptionRepository).findAllByOrderId(any());
        assertEquals(1, cloneOrder.getOrderSettingCustomerOptionModels().size());
    }

    @Test
    public void transferCouponCodeToOrderForKafkaImportOrderStatus() {
        OrderModel order = new OrderModel();
        order.setId(2L);
        OrderModel cloneOrder = new OrderModel();
        OrderHasCouponCodeModel codeModel = new OrderHasCouponCodeModel();
        when(orderHasCouponRepository.findAllByOrderId(any())).thenReturn(Arrays.asList(codeModel));
        orderService.transferCouponCodeToOrderForKafkaImportOrderStatus(order, cloneOrder);
        verify(orderHasCouponRepository).findAllByOrderId(any());
        assertEquals(1, cloneOrder.getOrderHasCouponCodeModels().size());
    }

    @Test
    public void cloneOrderFormModel() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCode("code");
        orderModel.setCompanyId(2L);
        OrderModel cloneModel = orderService.cloneOrderFormModel(orderModel);
        assertEquals("code", cloneModel.getCode());
        assertEquals(2L, cloneModel.getCompanyId(), 0);
    }

    @Test
    public void updateLockOrder() {
        when(orderMock.getCode()).thenReturn("code");
        orderService.updateLockOrder(orderMock, false);
        verify(orderRepository).updateLockOrder(anyString(), anyBoolean());
    }

    @Test
    public void updateLockOrders() {
        orderService.updateLockOrders(2L, Arrays.asList("code"), false);
        verify(orderRepository).updateLockOrders(anyLong(), anyList(), anyBoolean());
    }

    @Test
    public void addTag() {
        when(addTagRequest.getCompanyId()).thenReturn(1l);
        when(addTagRequest.getOrderCode()).thenReturn("code");
        when(addTagRequest.getTagId()).thenReturn(1l);
        when(orderRepository.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(Optional.of(orderMock));
        when(orderMock.getTags()).thenReturn(new HashSet<>());
        when(tagService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(new TagModel());
        orderService.addTag(addTagRequest);
        verify(orderRepository).save(orderMock);
        verify(applicationEventPublisher).publishEvent(any(OrderTagEvent.class));
    }

    @Test
    public void removeTag() {
        when(orderRepository.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean())).thenReturn(Optional.of(orderMock));
        when(orderMock.getTags()).thenReturn(new HashSet<>());
        when(tagService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(new TagModel());
        orderService.removeTag(1l, "code", 22l);
        verify(orderRepository).save(orderMock);
        verify(applicationEventPublisher).publishEvent(any(OrderTagEvent.class));
    }

    @Test
    public void storefrontCountOrderByUser() {
        OrderSearchRequest request = new OrderSearchRequest();
        request.setCompanyId(2L);
        orderService.storefrontCountOrderByUser(request);
        verify(orderDAO).storefrontCountOrderByUser(any(OrderSearchRequest.class));
    }

}
