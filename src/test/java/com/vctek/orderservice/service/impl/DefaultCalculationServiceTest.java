package com.vctek.orderservice.service.impl;

import com.vctek.dto.VatData;
import com.vctek.orderservice.dto.ProductCanRewardDto;
import com.vctek.orderservice.dto.ReturnOrderCommerceParameter;
import com.vctek.orderservice.dto.request.ReturnOrderEntryRequest;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.feignclient.dto.BillRequest;
import com.vctek.orderservice.kafka.producer.LoyaltyInvoiceProducerService;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.repository.SubOrderEntryRepository;
import com.vctek.orderservice.repository.ToppingOptionRepository;
import com.vctek.orderservice.service.ModelService;
import com.vctek.orderservice.service.ProductLoyaltyRewardRateService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.service.ToppingItemService;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.DiscountType;
import com.vctek.util.ComboType;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class DefaultCalculationServiceTest {
    private DefaultCalculationService service;
    @Mock
    private ModelService modelService;
    private CartModel cart;

    private double basePrice = 120000d;
    private double basePrice2 = 155000d;
    private ArgumentCaptor<CartModel> cartCaptor = ArgumentCaptor.forClass(CartModel.class);
    private ArgumentCaptor<AbstractOrderModel> captorOrderModel = ArgumentCaptor.forClass(AbstractOrderModel.class);

    private List<AbstractOrderEntryModel> entries;
    private ToppingOptionModel toppingOptionModel1;
    private ToppingOptionModel toppingOptionModel2;
    @Mock
    private ToppingItemModel topping1;
    @Mock
    private ToppingItemModel topping2;
    @Mock
    private ToppingItemModel topping3;
    @Mock
    private ToppingItemService toppingItemService;
    @Mock
    private LoyaltyInvoiceProducerService loyaltyInvoiceProducerService;

    @Mock
    private ProductLoyaltyRewardRateService productLoyaltyRewardRateService;
    @Mock
    private SubOrderEntryRepository subOrderEntryRepository;

    @Mock
    private EntryRepository entryRepository;
    @Mock
    private ToppingOptionRepository toppingOptionRepository;
    @Mock
    private ProductService productService;
    private Map<Long, VatData> vatDataMap;

    private CartEntryModel cartEntryModel(Long productId, Double basePrice, Double discount, String discountType, Long quantity) {
        CartEntryModel model = new CartEntryModel();
        model.setOrder(cart);
        model.setBasePrice(basePrice);
        model.setDiscount(discount);
        model.setDiscountType(discountType);
        model.setQuantity(quantity);
        model.setProductId(productId);
        return model;
    }

    private VatData vatData(double vat) {
        VatData data = new VatData();
        data.setVat(vat);
        data.setVatType(CurrencyType.PERCENT.toString());
        return data;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        entries = new ArrayList<>();
        vatDataMap = new HashMap<>();
        cart = new CartModel();
        cart.setId(111l);
        cart.setEntries(entries);
        service = new DefaultCalculationService(modelService, toppingItemService);
        service.setProductLoyaltyRewardRateService(productLoyaltyRewardRateService);
        service.setLoyaltyInvoiceProducerService(loyaltyInvoiceProducerService);
        service.setSubOrderEntryRepository(subOrderEntryRepository);
        service.setToppingOptionRepository(toppingOptionRepository);
        service.setEntryRepository(entryRepository);
        service.setProductService(productService);
        when(entryRepository.findAllByOrder(cart)).thenReturn(entries);
        toppingOptionModel1 = new ToppingOptionModel();
        toppingOptionModel1.setToppingItemModels(new HashSet<>(Arrays.asList(topping1, topping2)));
        when(toppingItemService.findAllByToppingOptionModel(toppingOptionModel1)).thenReturn(new HashSet<>(Arrays.asList(topping1, topping2)));
        toppingOptionModel2 = new ToppingOptionModel();
        toppingOptionModel2.setToppingItemModels(new HashSet<>(Collections.singletonList(topping3)));
        when(toppingItemService.findAllByToppingOptionModel(toppingOptionModel2)).thenReturn(new HashSet<>(Arrays.asList(topping3)));
        when(productService.getVATOf(anySet())).thenReturn(vatDataMap);
    }

    @Test
    public void calculateTotals_HasNotDiscount_HasNotPromotion_HasNotVat() {
        long qty = 10L;
        entries.add(cartEntryModel(111l, basePrice, 0d, null, qty));
        service.calculateTotals(cart, true);
        double expectedSubtotal = basePrice * qty;
        assertEquals(expectedSubtotal, cart.getSubTotal(), 0);
        assertEquals(expectedSubtotal, cart.getTotalPrice(), 0);
        assertEquals(expectedSubtotal, cart.getFinalPrice(), 0);
        assertEquals(0d, cart.getTotalDiscount(), 0);
        assertEquals(0d, cart.getTotalTax(), 0);
    }

    @Test
    public void calculateTotals_HasNotDiscount_HasNotPromotion_HasVat() {
        long qty = 10L;
        long qty2 = 15L;
        entries.add(cartEntryModel(111l, basePrice, 0d, null, qty));
        entries.add(cartEntryModel(111l, basePrice2, 0d, null, qty2));
        cart.setVat(10d);
        cart.setVatType(CurrencyType.PERCENT.toString());

        service.calculateTotals(cart, true);
        double expectedSubtotal = basePrice * qty + basePrice2 * qty2;
        double expectedTotalPrice = basePrice * qty + basePrice2 * qty2;
        double totalTax = 0.1 * expectedTotalPrice;
        double finalPrice = expectedTotalPrice + totalTax;
        verifyOrder(cart, expectedSubtotal, expectedTotalPrice, totalTax, finalPrice, 0d, 0);

        assertEquals(2, cart.getEntries().size());
        List<Double> totalPrices = cart.getEntries().stream().map(AbstractOrderEntryModel::getTotalPrice).collect(Collectors.toList());
        assertTrue(totalPrices.contains(basePrice * qty));
        assertTrue(totalPrices.contains(basePrice2 * qty2));
    }


    @Test
    public void calculateTotals_HasNotOrderDiscount_HasNotPromotion_HasVat_HasEntryDiscount() {
        long qty = 10L;
        long qty2 = 15L;
        double discountEntry1 = 10000d;
        entries.add(cartEntryModel(111l, basePrice, discountEntry1, CurrencyType.CASH.toString(), qty));
        entries.add(cartEntryModel(111l, basePrice2, 0d, null, qty2));
        cart.setVat(10d);
        cart.setVatType(CurrencyType.PERCENT.toString());

        service.calculateTotals(cart, true);
        double totalPriceEntry1 = basePrice * qty - discountEntry1;
        double totalPriceEntry2 = basePrice2 * qty2;
        double expectedSubtotal = totalPriceEntry1 + totalPriceEntry2;
        double expectedTotalPrice = totalPriceEntry1 + totalPriceEntry2;
        double totalTax = 0.1 * expectedTotalPrice;
        double finalPrice = expectedTotalPrice + totalTax;
        verifyOrder(cart, expectedSubtotal, expectedTotalPrice, totalTax, finalPrice, 0, discountEntry1);

        assertEquals(2, cart.getEntries().size());
        List<Double> finalPrices = cart.getEntries().stream().map(AbstractOrderEntryModel::getFinalPrice).collect(Collectors.toList());
        assertTrue(finalPrices.contains(totalPriceEntry1));
        assertTrue(finalPrices.contains(totalPriceEntry2));
    }

    @Test
    public void calculateTotals_HasOrderDiscount_HasNotPromotion_HasVat_HasEntryDiscount() {
        long qty = 10L;
        long qty2 = 15L;
        double discountEntry1 = 10000d;
        entries.add(cartEntryModel(111l, basePrice, discountEntry1, CurrencyType.CASH.toString(), qty));
        entries.add(cartEntryModel(111l, basePrice2, 0d, null, qty2));
        cart.setDiscount(10d);
        cart.setDiscountType(CurrencyType.PERCENT.toString());
        cart.setVat(10d);
        cart.setVatType(CurrencyType.PERCENT.toString());

        service.calculateTotals(cart, true);
        double totalPriceEntry1 = basePrice * qty - discountEntry1;
        double totalPriceEntry2 = basePrice2 * qty2;
        double expectedSubtotal = totalPriceEntry1 + totalPriceEntry2;
        double cartDisCount = expectedSubtotal * 0.1;
        double expectedSubtotalDiscount = discountEntry1;

        double expectedTotalPrice = expectedSubtotal - cartDisCount;
        double totalTax = 0.1 * expectedTotalPrice;
        double finalPrice = expectedTotalPrice + totalTax;
        verifyOrder(cart, expectedSubtotal, expectedTotalPrice, totalTax, finalPrice, cartDisCount, expectedSubtotalDiscount);

        assertEquals(2, cart.getEntries().size());
        List<Double> finalPrices = cart.getEntries().stream().map(AbstractOrderEntryModel::getFinalPrice).collect(Collectors.toList());
        assertTrue(finalPrices.contains(totalPriceEntry1));
        assertTrue(finalPrices.contains(totalPriceEntry2));
    }

    @Test
    public void calculateVat() {
        cart.setTotalPrice(2000d);
        cart.setVatType(CurrencyType.PERCENT.toString());
        cart.setVat(10d);
        cart.setDeliveryCost(100d);
        cart.setCompanyShippingFee(100d);
        cart.setCollaboratorShippingFee(100d);
        service.calculateVat(cart);
        verify(modelService).save(cartCaptor.capture());
        CartModel captorValue = cartCaptor.getValue();
        assertEquals(2300d, captorValue.getFinalPrice(), 0);
        assertTrue(captorValue.isCalculated());
    }

    private void verifyOrder(CartModel cart, double expectedSubtotal,
                             double expectedTotalPrice, double totalTax,
                             double finalPrice, double totalDiscount, double expectedSubtotalDiscount) {
        assertEquals(expectedSubtotal, cart.getSubTotal(), 0);
        assertEquals(expectedTotalPrice, cart.getTotalPrice(), 0);
        assertEquals(finalPrice, cart.getFinalPrice(), 0);
        assertEquals(totalDiscount, cart.getTotalDiscount(), 0);
        assertEquals(totalTax, cart.getTotalTax(), 0);
        assertEquals(expectedSubtotalDiscount, cart.getSubTotalDiscount(), 0);
    }

    @Test
    public void calculateTotals_HasNotOrderDiscount_HasNotPromotion_HasTopping_OptionWithQtyIsOne() {
        long qty = 10L;
        long qty2 = 15L;
        entries.add(cartEntryModel(111l, basePrice, 0d, CurrencyType.CASH.toString(), qty));
        CartEntryModel entry2 = cartEntryModel(111l, basePrice2, 0d, null, qty2);
        toppingOptionModel1.setQuantity(1);
        toppingOptionModel2.setQuantity(1);
        entry2.setToppingOptionModels(new HashSet<>(Arrays.asList(toppingOptionModel1, toppingOptionModel2)));
        when(topping1.getBasePrice()).thenReturn(10000d);
        when(topping1.getQuantity()).thenReturn(1);
        when(topping2.getBasePrice()).thenReturn(10000d);
        when(topping2.getQuantity()).thenReturn(2);
        when(topping3.getBasePrice()).thenReturn(10000d);
        when(topping3.getQuantity()).thenReturn(3);
        when(toppingOptionRepository.findAllByOrderEntry(entry2)).thenReturn(Arrays.asList(toppingOptionModel1, toppingOptionModel2));

        entries.add(entry2);
        cart.setVat(10d);
        cart.setVatType(CurrencyType.PERCENT.toString());

        service.calculateTotals(cart, true);
        double totalPriceEntry1 = basePrice * qty;
        double totalPriceEntry2 = basePrice2 * qty2;
        double toppingTotalPrice = 6 * 10000d;
        double expectedSubtotal = totalPriceEntry1 + totalPriceEntry2 + toppingTotalPrice;
        double expectedTotalPrice = totalPriceEntry1 + totalPriceEntry2 + toppingTotalPrice;
        double totalTax = 0.1 * expectedTotalPrice;
        double finalPrice = expectedTotalPrice + totalTax;
        verifyOrder(cart, expectedSubtotal, expectedTotalPrice, totalTax, finalPrice, 0, 0);

        assertEquals(2, cart.getEntries().size());
        List<Double> finalPrices = cart.getEntries().stream().map(AbstractOrderEntryModel::getFinalPrice).collect(Collectors.toList());
        assertTrue(finalPrices.contains(totalPriceEntry1));
        assertTrue(finalPrices.contains(totalPriceEntry2));
    }

    @Test
    public void calculateToppingTotalPrice_EntryHasNotToppingOption() {
        CartEntryModel entry2 = cartEntryModel(111l, basePrice2, 0d, null, 10L);
        double toppingTotalPrice = service.calculateToppingTotalPrice(entry2);
        assertEquals(0, toppingTotalPrice, 0);
    }

    @Test
    public void calculateToppingTotalPrice_EntryHasOneToppingOption() {
        CartEntryModel entry2 = cartEntryModel(111l, basePrice2, 0d, null, 10L);
        entry2.setToppingOptionModels(new HashSet<>(Collections.singletonList(toppingOptionModel1)));
        toppingOptionModel1.setQuantity(1);
        when(topping1.getBasePrice()).thenReturn(10000d);
        when(topping1.getQuantity()).thenReturn(1);
        when(topping2.getBasePrice()).thenReturn(10000d);
        when(topping2.getQuantity()).thenReturn(2);
        when(toppingOptionRepository.findAllByOrderEntry(entry2)).thenReturn(Collections.singletonList(toppingOptionModel1));
        double toppingTotalPrice = service.calculateToppingTotalPrice(entry2);
        assertEquals(30000d, toppingTotalPrice, 0);
    }

    @Test
    public void calculateToppingTotalPrice_EntryHas1ToppingOptionWithQtyIs2() {
        CartEntryModel entry2 = cartEntryModel(111l, basePrice2, 0d, null, 10L);
        entry2.setToppingOptionModels(new HashSet<>(Collections.singletonList(toppingOptionModel1)));
        toppingOptionModel1.setQuantity(2);
        when(topping1.getBasePrice()).thenReturn(10000d);
        when(topping1.getQuantity()).thenReturn(1);
        when(topping2.getBasePrice()).thenReturn(10000d);
        when(topping2.getQuantity()).thenReturn(2);
        when(toppingOptionRepository.findAllByOrderEntry(entry2)).thenReturn(Arrays.asList(toppingOptionModel1));
        double toppingTotalPrice = service.calculateToppingTotalPrice(entry2);
        assertEquals(60000d, toppingTotalPrice, 0);
    }

    @Test
    public void calculateToppingTotalPrice_EntryHas2ToppingOption() {
        CartEntryModel entry2 = cartEntryModel(111l, basePrice2, 0d, null, 10L);
        entry2.setToppingOptionModels(new HashSet<>(Arrays.asList(toppingOptionModel1, toppingOptionModel2)));
        toppingOptionModel1.setQuantity(2);
        toppingOptionModel2.setQuantity(3);
        when(topping1.getBasePrice()).thenReturn(10000d);
        when(topping1.getQuantity()).thenReturn(1);
        when(topping2.getBasePrice()).thenReturn(10000d);
        when(topping2.getQuantity()).thenReturn(2);

        when(topping3.getBasePrice()).thenReturn(10000d);
        when(topping3.getQuantity()).thenReturn(3);
        when(toppingOptionRepository.findAllByOrderEntry(entry2))
                .thenReturn(Arrays.asList(toppingOptionModel1, toppingOptionModel2));
        double toppingTotalPrice = service.calculateToppingTotalPrice(entry2);
        assertEquals(150000d, toppingTotalPrice, 0);
    }

    @Test
    public void calculateTotals_HasNotOrderDiscount_HasNotPromotion_HasTopping_HasDiscountTopping() {
        long qty = 10L;
        long qty1 = 1L;
        entries.add(cartEntryModel(111l, basePrice, 0d, CurrencyType.CASH.toString(), qty));
        toppingOptionModel1.setQuantity(1);
        toppingOptionModel1.setId(1L);
        ToppingItemModel toppingItemModel = new ToppingItemModel();
        toppingItemModel.setId(1L);
        toppingItemModel.setBasePrice(10000d);
        toppingItemModel.setQuantity(1);
        toppingItemModel.setDiscount(10d);
        toppingItemModel.setDiscountType(CurrencyType.PERCENT.toString());
        toppingOptionModel1.setToppingItemModels(Collections.singleton(toppingItemModel));
        when(toppingItemService.findAllByToppingOptionModel(toppingOptionModel1)).thenReturn(Collections.singleton(toppingItemModel));
        CartEntryModel cartEntryModel = new CartEntryModel();
        cartEntryModel.setToppingOptionModels(Collections.singleton(toppingOptionModel1));
        cartEntryModel.setBasePrice(20000d);
        cartEntryModel.setQuantity(qty1);
        cartEntryModel.setDiscountType(CurrencyType.CASH.toString());
        when(entryRepository.findAllEntryHasToppingOf(anyLong())).thenReturn(Arrays.asList(cartEntryModel));
        entries.add(cartEntryModel);
        cart.setVat(10d);
        cart.setVatType(CurrencyType.PERCENT.toString());
        cartEntryModel.setOrder(cart);
        when(toppingOptionRepository.findAllByOrderEntry(cartEntryModel))
                .thenReturn(Arrays.asList(toppingOptionModel1));
        double totalDiscountTopping = toppingItemModel.getBasePrice() * toppingItemModel.getDiscount() / 100;
        when(toppingItemService.totalDiscountToppingItem(anyList())).thenReturn(totalDiscountTopping);
        service.calculateTotals(cart, true);

        double totalPriceEntry1 = basePrice * qty;
        double totalPriceEntry2 = cartEntryModel.getBasePrice() * qty1;
        double toppingTotalPrice = 1 * 10000d;
        double expectedSubtotal = totalPriceEntry1 + toppingTotalPrice + totalPriceEntry2 - totalDiscountTopping;
        double expectedTotalPrice = totalPriceEntry1 + toppingTotalPrice + totalPriceEntry2 - totalDiscountTopping;
        double totalTax = 0.1 * expectedTotalPrice;
        double finalPrice = expectedTotalPrice + totalTax;
        double expectedTotalDiscount = cart.getFixedDiscount();
        verifyOrder(cart, expectedSubtotal, expectedTotalPrice, totalTax, finalPrice, expectedTotalDiscount, 0);

        assertEquals(2, cart.getEntries().size());
        List<Double> finalPrices = cart.getEntries().stream().map(AbstractOrderEntryModel::getFinalPrice).collect(Collectors.toList());
        assertTrue(finalPrices.contains(totalPriceEntry1));
    }

    @Test
    public void calculateLoyaltyAmount() {
        List<ProductCanRewardDto> productCanRewardDtoList = new ArrayList<>();
        ProductCanRewardDto dto1 = new ProductCanRewardDto();
        dto1.setProductId(1L);
        dto1.setFinalPrice(10000d);
        productCanRewardDtoList.add(dto1);

        ProductCanRewardDto dto2 = new ProductCanRewardDto();
        dto2.setProductId(2L);
        dto2.setFinalPrice(120000d);
        productCanRewardDtoList.add(dto2);

        ProductCanRewardDto dto3 = new ProductCanRewardDto();
        dto3.setProductId(1L);
        dto3.setFinalPrice(10000d);
        productCanRewardDtoList.add(dto3);

        ProductCanRewardDto dto4 = new ProductCanRewardDto();
        dto4.setProductId(3L);
        dto4.setFinalPrice(33000d);
        productCanRewardDtoList.add(dto4);

        Map<Long, Double> rewardRateByProductIds = new HashedMap();
        rewardRateByProductIds.put(1L, 10d);
        rewardRateByProductIds.put(2L, 50d);
        rewardRateByProductIds.put(3L, 30d);
        when(productLoyaltyRewardRateService.getRewardRateByProductIds(anySet(), anyLong(), anyBoolean())).thenReturn(rewardRateByProductIds);

        double loyaltyAmount = service.calculateLoyaltyAmount(productCanRewardDtoList, 1L);
        // 1000 (simple) + 60000 (combo) + 1000(item1) + 9900(item2)
        assertEquals(71900, loyaltyAmount, 0);
    }

    @Test
    public void reCalculateRewardAmountInvalidToppingItemId() {
        ProductCanRewardDto productCanRewardDto = new ProductCanRewardDto();
        productCanRewardDto.setToppingItemId(1L);
        productCanRewardDto.setAwardAmount(1000d);
        List<ProductCanRewardDto> productCanRewards = Arrays.asList(productCanRewardDto);
        AbstractOrderModel orderModel = new OrderModel();
        when(toppingItemService.findById(1L)).thenReturn(null);
        service.saveRewardAmountToEntries(orderModel, 5, 1000, productCanRewards, true);
        verify(modelService, times(0)).saveAll(anyList());
        verify(modelService).save(orderModel);
    }

    @Test
    public void reCalculateRewardAmountInvalidOrderEntry() {
        ProductCanRewardDto productCanRewardDto = new ProductCanRewardDto();
        productCanRewardDto.setOrderEntryId(1L);
        productCanRewardDto.setAwardAmount(1000d);
        ProductCanRewardDto productCanRewardDto1 = new ProductCanRewardDto();
        productCanRewardDto1.setToppingItemId(1L);
        productCanRewardDto1.setAwardAmount(1000d);
        List<ProductCanRewardDto> productCanRewards = Arrays.asList(productCanRewardDto, productCanRewardDto1);
        AbstractOrderModel orderModel = new OrderModel();
        service.saveRewardAmountToEntries(orderModel, 5, 1000, productCanRewards, true);
        verify(modelService).save(orderModel);
    }

    @Test
    public void reCalculateRewardAmountSuccess_AllProductDividedPoint() {
        ProductCanRewardDto productCanRewardDto = new ProductCanRewardDto();
        productCanRewardDto.setOrderEntryId(1L);
        productCanRewardDto.setAwardAmount(1500d);
        ProductCanRewardDto productCanRewardDto1 = new ProductCanRewardDto();
        productCanRewardDto1.setOrderEntryId(2L);
        productCanRewardDto1.setAwardAmount(900d);
        List<ProductCanRewardDto> productCanRewards = Arrays.asList(productCanRewardDto, productCanRewardDto1);
        AbstractOrderModel orderModel = new OrderModel();
        OrderEntryModel orderEntryModel1 = new OrderEntryModel();
        orderEntryModel1.setId(1L);
        OrderEntryModel orderEntryModel2 = new OrderEntryModel();
        orderEntryModel2.setId(2L);
        orderModel.setEntries(Arrays.asList(orderEntryModel1, orderEntryModel2));
        service.saveRewardAmountToEntries(orderModel, 2.4, 2400d, productCanRewards, false);
        verify(modelService).save(captorOrderModel.capture());
        AbstractOrderModel abstractOrderModel = captorOrderModel.getValue();
        assertEquals(2400, abstractOrderModel.getTotalRewardAmount(), 0);
        assertEquals(2.4, abstractOrderModel.getRewardPoint(), 0);
        assertEquals(1500d, abstractOrderModel.getEntries().get(0).getRewardAmount(), 0);
        assertEquals(900d, abstractOrderModel.getEntries().get(1).getRewardAmount(), 0);
    }

    @Test
    public void reCalculateRewardAmountSuccess_ZeroLoyaltyPoint_ShouldNotCalculatePointForEachProduct() {
        ProductCanRewardDto productCanRewardDto = new ProductCanRewardDto();
        productCanRewardDto.setOrderEntryId(1L);
        productCanRewardDto.setAwardAmount(400d);
        ProductCanRewardDto productCanRewardDto1 = new ProductCanRewardDto();
        productCanRewardDto1.setOrderEntryId(2L);
        productCanRewardDto1.setAwardAmount(300d);
        ProductCanRewardDto productCanRewardDto2 = new ProductCanRewardDto();
        productCanRewardDto2.setOrderEntryId(1l);
        productCanRewardDto2.setToppingOptionId(12l);
        productCanRewardDto2.setToppingItemId(3L);
        productCanRewardDto2.setAwardAmount(300d);
        List<ProductCanRewardDto> productCanRewards = Arrays.asList(productCanRewardDto, productCanRewardDto1, productCanRewardDto2);
        ToppingItemModel toppingItemModel = new ToppingItemModel();
        AbstractOrderModel orderModel = new OrderModel();
        OrderEntryModel orderEntryModel1 = new OrderEntryModel();
        orderEntryModel1.setId(1L);
        orderEntryModel1.setToppingOptionModels(new HashSet<>(Arrays.asList(toppingOptionModel1)));
        toppingOptionModel1.setId(12l);
        toppingItemModel.setId(3l);
        toppingOptionModel1.setToppingItemModels(new HashSet<>(Arrays.asList(toppingItemModel)));
        OrderEntryModel orderEntryModel2 = new OrderEntryModel();
        orderEntryModel2.setId(2L);

        orderModel.setEntries(Arrays.asList(orderEntryModel1, orderEntryModel2));
        service.saveRewardAmountToEntries(orderModel, 1, 1000, productCanRewards, false);
        verify(modelService, times(1)).save(orderModel);
        assertEquals(1, orderModel.getRewardPoint(), 0);
        assertEquals(1000, orderModel.getTotalRewardAmount(), 0);
        assertEquals(400, orderEntryModel1.getRewardAmount(), 0);
        assertEquals(300d, orderEntryModel2.getRewardAmount(), 0);
        assertEquals(300d, toppingItemModel.getRewardAmount(), 0);
    }

    @Test
    public void calculateRevertAmountHasNotProductRewarded() {
        OrderModel orderModel = new OrderModel();
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setProductId(1L);
        orderEntryModel.setQuantity(5L);
        orderEntryModel.setRewardAmount(10000d);
        orderEntryModel.setEntryNumber(1);
        orderEntryModel.setId(1l);
        OrderEntryModel orderEntryModel1 = new OrderEntryModel();
        orderEntryModel1.setProductId(1L);
        orderEntryModel1.setQuantity(5L);
        orderEntryModel1.setEntryNumber(2);
        orderEntryModel1.setId(2l);
        orderModel.setEntries(Arrays.asList(orderEntryModel, orderEntryModel1));
        ReturnOrderRequest returnOrderRequest = new ReturnOrderRequest();
        ReturnOrderEntryRequest entryRequest = new ReturnOrderEntryRequest();
        entryRequest.setEntryNumber(2);
        entryRequest.setId(2l);
        entryRequest.setQuantity(3);
        entryRequest.setOrderEntryId(2l);
        returnOrderRequest.setReturnOrderEntries(Arrays.asList(entryRequest));
        double revertAmount = service.calculateMaxRevertAmount(returnOrderRequest, orderModel);
        assertEquals(0, revertAmount, 0);
    }


    @Test
    public void calculateRevertAmount() {
        OrderModel orderModel = new OrderModel();
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setProductId(1L);
        orderEntryModel.setQuantity(5L);
        orderEntryModel.setRewardAmount(10000d);
        orderEntryModel.setEntryNumber(1);
        orderEntryModel.setId(1l);
        OrderEntryModel orderEntryModel1 = new OrderEntryModel();
        orderEntryModel1.setProductId(1L);
        orderEntryModel1.setQuantity(5L);
        orderEntryModel1.setEntryNumber(2);
        orderEntryModel1.setId(2l);
        orderModel.setEntries(Arrays.asList(orderEntryModel, orderEntryModel1));
        ReturnOrderRequest returnOrderRequest = new ReturnOrderRequest();
        ReturnOrderEntryRequest entryRequest = new ReturnOrderEntryRequest();
        entryRequest.setEntryNumber(1);
        entryRequest.setId(1l);
        entryRequest.setQuantity(3);
        entryRequest.setOrderEntryId(1l);
        returnOrderRequest.setReturnOrderEntries(Arrays.asList(entryRequest));
        double revertAmount = service.calculateMaxRevertAmount(returnOrderRequest, orderModel);
        assertEquals(6000, revertAmount, 0);
    }

    @Test
    public void calculateRevertAmountWhenProductHasTopping() {
        OrderModel orderModel = new OrderModel();
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setProductId(1L);
        orderEntryModel.setQuantity(5L);
        orderEntryModel.setRewardAmount(10000d);
        orderEntryModel.setEntryNumber(1);
        orderEntryModel.setId(1l);

        OrderEntryModel orderEntryModel1 = new OrderEntryModel();
        orderEntryModel1.setProductId(1L);
        orderEntryModel1.setQuantity(5L);
        orderEntryModel1.setEntryNumber(2);
        orderEntryModel1.setId(2l);

        ToppingItemModel toppingItem1 = new ToppingItemModel();
        toppingItem1.setQuantity(2);
        toppingItem1.setRewardAmount(5000d);

        ToppingItemModel toppingItem2 = new ToppingItemModel();
        toppingItem2.setQuantity(2);
        toppingItem2.setRewardAmount(null);

        Set<ToppingItemModel> toppingItemModelSet = new HashSet<>();
        toppingItemModelSet.add(toppingItem1);
        toppingItemModelSet.add(toppingItem2);

        ToppingOptionModel toppingOptionModel = new ToppingOptionModel();
        toppingOptionModel.setQuantity(3);
        toppingOptionModel.setToppingItemModels(toppingItemModelSet);
        Set<ToppingOptionModel> toppingOptionModelSet = new HashSet<>();
        toppingOptionModelSet.add(toppingOptionModel);

        orderEntryModel1.setToppingOptionModels(toppingOptionModelSet);
        orderModel.setEntries(Arrays.asList(orderEntryModel, orderEntryModel1));
        ReturnOrderRequest returnOrderRequest = new ReturnOrderRequest();
        ReturnOrderEntryRequest entryRequest = new ReturnOrderEntryRequest();
        entryRequest.setEntryNumber(1);
        entryRequest.setOrderEntryId(1l);
        entryRequest.setId(1l);
        entryRequest.setQuantity(3);
        ReturnOrderEntryRequest entryRequest1 = new ReturnOrderEntryRequest();
        entryRequest1.setEntryNumber(2);
        entryRequest1.setOrderEntryId(2l);
        entryRequest1.setId(2l);
        entryRequest1.setQuantity(2);
        returnOrderRequest.setReturnOrderEntries(Arrays.asList(entryRequest, entryRequest1));
        double revertAmount = service.calculateMaxRevertAmount(returnOrderRequest, orderModel);
        assertEquals(11000, revertAmount, 0);
    }


    @Test
    public void calculateRefundAmountWhenOrderHasNotRedeemAmount() {
        ReturnOrderCommerceParameter commerceParameter = new ReturnOrderCommerceParameter();
        OrderModel orderModel = new OrderModel();
        commerceParameter.setOriginOrder(orderModel);
        double refunded = service.calculateMaxRefundAmount(commerceParameter);
        assertEquals(0, refunded, 0);
    }

    @Test
    public void calculateRefundAmountWhenOrderHasRedeemAmountEqual0() {
        ReturnOrderCommerceParameter commerceParameter = new ReturnOrderCommerceParameter();
        OrderModel orderModel = new OrderModel();
        orderModel.setRedeemAmount(0d);
        commerceParameter.setOriginOrder(orderModel);
        double refunded = service.calculateMaxRefundAmount(commerceParameter);
        assertEquals(0, refunded, 0);
    }


    @Test
    public void calculateRefundAmountWhenOrderHasRefundedAll() {
        ReturnOrderCommerceParameter commerceParameter = new ReturnOrderCommerceParameter();
        OrderModel orderModel = new OrderModel();
        orderModel.setRedeemAmount(5000d);
        orderModel.setRefundAmount(5000d);
        commerceParameter.setOriginOrder(orderModel);
        double refunded = service.calculateMaxRefundAmount(commerceParameter);
        assertEquals(0, refunded, 0);
    }

    @Test
    public void calculateRefundAmountWhenOrderHasRemainRefundAmountLessOrEqualFinal() {
        ReturnOrderCommerceParameter commerceParameter = new ReturnOrderCommerceParameter();
        OrderModel orderModel = new OrderModel();
        orderModel.setRedeemAmount(5000d);
        orderModel.setRefundAmount(1000d);
        BillRequest billRequest = new BillRequest();
        billRequest.setFinalCost(10000d);
        commerceParameter.setBillRequest(billRequest);
        commerceParameter.setOriginOrder(orderModel);
        double refunded = service.calculateMaxRefundAmount(commerceParameter);
        assertEquals(4000, refunded, 0);
    }

    @Test
    public void calculateRefundAmountWhenOrderHasNotRefundAndRedeemLessOrEqualFinal() {
        ReturnOrderCommerceParameter commerceParameter = new ReturnOrderCommerceParameter();
        OrderModel orderModel = new OrderModel();
        orderModel.setRedeemAmount(5000d);
        BillRequest billRequest = new BillRequest();
        billRequest.setFinalCost(10000d);
        commerceParameter.setBillRequest(billRequest);
        commerceParameter.setOriginOrder(orderModel);
        double refunded = service.calculateMaxRefundAmount(commerceParameter);
        assertEquals(5000, refunded, 0);
    }

    @Test
    public void calculateRefundAmountWhenOrderHasRemainRefundGreaterFinal() {
        ReturnOrderCommerceParameter commerceParameter = new ReturnOrderCommerceParameter();
        OrderModel orderModel = new OrderModel();
        orderModel.setRedeemAmount(9000d);
        orderModel.setRefundAmount(1000d);
        BillRequest billRequest = new BillRequest();
        billRequest.setFinalCost(4000d);
        commerceParameter.setBillRequest(billRequest);
        commerceParameter.setOriginOrder(orderModel);
        double refunded = service.calculateMaxRefundAmount(commerceParameter);
        assertEquals(4000, refunded, 0);
    }

    @Test
    public void calculateRemainAmount() {
        ReturnOrderCommerceParameter commerceParameter = new ReturnOrderCommerceParameter();
        BillRequest billRequest = new BillRequest();
        billRequest.setFinalCost(10000d);
        commerceParameter.setBillRequest(billRequest);
        OrderModel orderModel = new OrderModel();
        orderModel.setFinalPrice(940000.0);
        orderModel.setRedeemAmount(1000d);

        OrderEntryModel orderEntryModel1 = new OrderEntryModel();
        orderEntryModel1.setProductId(1L);
        orderEntryModel1.setEntryNumber(0);
        orderEntryModel1.setReturnQuantity(1l);
        orderEntryModel1.setQuantity(2l);
        orderEntryModel1.setTotalDiscount(1000.0);
        orderEntryModel1.setBasePrice(2000.0);

        ToppingItemModel toppingItem1 = new ToppingItemModel();
        toppingItem1.setQuantity(2);
        toppingItem1.setBasePrice(10000.0);

        ToppingItemModel toppingItem2 = new ToppingItemModel();
        toppingItem2.setQuantity(2);
        toppingItem1.setBasePrice(11000.0);

        Set<ToppingItemModel> toppingItemModelSet = new HashSet<>();
        toppingItemModelSet.add(toppingItem1);
        toppingItemModelSet.add(toppingItem2);

        ToppingOptionModel toppingOptionModel = new ToppingOptionModel();
        toppingOptionModel.setQuantity(3);
        toppingOptionModel.setToppingItemModels(toppingItemModelSet);
        Set<ToppingOptionModel> toppingOptionModelSet = new HashSet<>();
        toppingOptionModelSet.add(toppingOptionModel);

        orderEntryModel1.setToppingOptionModels(toppingOptionModelSet);
        orderModel.setEntries(Arrays.asList(orderEntryModel1));
        commerceParameter.setOriginOrder(orderModel);
        double finalPrice = service.calculateRemainCashAmount(commerceParameter);
        assertEquals(915500, finalPrice, 0);
    }

    @Test
    public void calculateRemainAmount_with_RedeemAndRefundPoint() {
        ReturnOrderCommerceParameter commerceParameter = new ReturnOrderCommerceParameter();
        OrderModel orderModel = new OrderModel();
        orderModel.setFinalPrice(60000d);
        orderModel.setRedeemAmount(10000d);
        orderModel.setRefundAmount(5000d);

        OrderEntryModel orderEntryModel1 = new OrderEntryModel();
        orderEntryModel1.setProductId(1L);
        orderEntryModel1.setEntryNumber(0);
        orderEntryModel1.setReturnQuantity(3L);
        orderEntryModel1.setQuantity(4L);
        orderEntryModel1.setTotalDiscount(20000d);
        orderEntryModel1.setBasePrice(20000d);

        orderModel.setEntries(Arrays.asList(orderEntryModel1));
        commerceParameter.setOriginOrder(orderModel);
        double finalPrice = service.calculateRemainCashAmount(commerceParameter);
        assertEquals(10000d, finalPrice, 0);
    }

    @Test
    public void clearComboEntryPrices() {
        CartEntryModel entryModel = cartEntryModel(111l, 1000d, 0d, null, 1l);
        SubOrderEntryModel subEntry = new SubOrderEntryModel();
        subEntry.setPrice(2000d);
        subEntry.setTotalPrice(2000d);
        subEntry.setFinalPrice(2000d);
        entryModel.setSubOrderEntries(new HashSet<>(Arrays.asList(subEntry)));
        service.clearComboEntryPrices(entryModel);
        assertNull(subEntry.getPrice());
        assertNull(subEntry.getTotalPrice());
        assertNull(subEntry.getFinalPrice());
    }

    @Test
    public void calculateVatByProductOf_OrderNotVat() {
        entries.add(cartEntryModel(111l, 1000d, 0d, null, 1l));
        cart.setHasGotVat(false);
        service.calculateVatByProductOf(cart, false);
        verify(productService, times(0)).getVATOf(anySet());
        assertNull(cart.getVat());
        assertNull(cart.getVatType());
        assertNull(cart.getTotalTax());
    }

    @Test
    public void calculateVatByProductOf_OrderHasGotVat_RemoveProductVAT_ShouldResetVATofOrder() {
        entries.add(cartEntryModel(111l, 1000d, 0d, null, 1l));
        cart.setHasGotVat(true);
        cart.setVat(1000d);
        cart.setVatType(com.vctek.util.CurrencyType.CASH.toString());
        service.calculateVatByProductOf(cart, true);
        verify(productService, times(1)).getVATOf(anySet());
        assertNull(cart.getVat());
        assertNull(cart.getVatType());
        assertEquals(0, cart.getTotalTax(), 0);
    }

    @Test
    public void calculateVatByProductOf_ProductNotVAT() {
        entries.add(cartEntryModel(111l, 1000d, 0d, null, 1l));
        when(productService.getVATOf(anySet())).thenReturn(new HashMap<>());
        cart.setHasGotVat(true);
        service.calculateVatByProductOf(cart, true);
        assertNull(cart.getVat());
        assertNull(cart.getVatType());
        assertEquals(0, cart.getTotalTax(), 0);
    }

    @Test
    public void calculateVatByProductOf_normalEntryHasVat_NoDiscount() {
        CartEntryModel normalEntry = cartEntryModel(111l, 10000d, 0d, null, 1l);
        normalEntry.setFinalPrice(10000d);
        cart.setFinalPrice(10000d);
        cart.setTotalPrice(10000d);
        cart.setHasGotVat(true);
        entries.add(normalEntry);
        vatDataMap.put(111l, vatData(3d));

        service.calculateVatByProductOf(cart, true);
        assertEquals(300, cart.getVat(), 0);
        assertEquals(com.vctek.util.CurrencyType.CASH.toString(), cart.getVatType());
        assertEquals(300, cart.getTotalTax(), 0);
        assertEquals(10300, cart.getFinalPrice(), 0);
        verify(modelService).save(cart);
    }

    @Test
    public void calculateVatByProductOf_normalEntryHasVat_HasDiscountOrderToItem() {
        CartEntryModel normalEntry = cartEntryModel(111l, 10000d, 0d, null, 1l);
        normalEntry.setDiscountOrderToItem(1000d);
        normalEntry.setFinalPrice(10000d);
        cart.setFinalPrice(9000d);
        cart.setTotalPrice(9000d);
        entries.add(normalEntry);
        vatDataMap.put(111l, vatData(3d));
        cart.setHasGotVat(true);

        service.calculateVatByProductOf(cart, true);
        assertEquals(270, cart.getVat(), 0);
        assertEquals(com.vctek.util.CurrencyType.CASH.toString(), cart.getVatType());
        assertEquals(270, cart.getTotalTax(), 0);
        assertEquals(9270, cart.getFinalPrice(), 0);
        verify(modelService).save(cart);
    }

    @Test
    public void calculateVatByProductOf_comboEntry_OrderHasGotVat() {
        CartEntryModel comboEntry = cartEntryModel(111l, 10000d, 0d, null, 1l);
        comboEntry.setComboType(ComboType.FIXED_COMBO.toString());
        SubOrderEntryModel entry = new SubOrderEntryModel();
        entry.setProductId(222l);
        entry.setFinalPrice(10000d);
        entry.setQuantity(1);

        comboEntry.setSubOrderEntries(new HashSet<>(Arrays.asList(entry)));
        comboEntry.setFinalPrice(10000d);
        cart.setFinalPrice(10000d);
        cart.setTotalPrice(10000d);
        cart.setHasGotVat(true);
        entries.add(comboEntry);
        vatDataMap.put(222l, vatData(3d));

        service.calculateVatByProductOf(cart, true);
        assertEquals(300, cart.getVat(), 0);
        assertEquals(com.vctek.util.CurrencyType.CASH.toString(), cart.getVatType());
        assertEquals(300, cart.getTotalTax(), 0);
        assertEquals(10300, cart.getFinalPrice(), 0);
        assertEquals(3, entry.getVat(), 0);
        verify(modelService).save(cart);
    }

    @Test
    public void calculateVatByProductOf_ToppingItem() {
        CartEntryModel fnbEntry = cartEntryModel(111l, 10000d, 0d, null, 2l);
        fnbEntry.setFinalPrice(20000d);
        cart.setFinalPrice(38000d);
        cart.setTotalPrice(38000d);
        cart.setId(21321l);
        cart.setHasGotVat(true);
        entries.add(fnbEntry);
        when(toppingItemService.findAllByOrderId(anyLong())).thenReturn(new HashSet<>(Arrays.asList(topping1)));
        when(topping1.getProductId()).thenReturn(222l);
        when(topping1.getQuantity()).thenReturn(1);
        when(topping1.getBasePrice()).thenReturn(10000d);
        when(topping1.getDiscount()).thenReturn(10d);
        when(topping1.getDiscountType()).thenReturn(DiscountType.PERCENT.toString());
        when(topping1.getToppingOptionModel()).thenReturn(toppingOptionModel1);
        toppingOptionModel1.setQuantity(2);
        vatDataMap.put(111l, vatData(10d));
        vatDataMap.put(222l, vatData(5d));

        service.calculateVatByProductOf(cart, true);
        assertEquals(2900, cart.getVat(), 0);
        assertEquals(com.vctek.util.CurrencyType.CASH.toString(), cart.getVatType());
        assertEquals(2900, cart.getTotalTax(), 0);
        assertEquals(40900, cart.getFinalPrice(), 0);
        verify(modelService).save(cart);
    }
}
