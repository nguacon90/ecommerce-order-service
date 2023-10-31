package com.vctek.orderservice.strategy.impl;

import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.loyalty.TransactionData;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.AddressRequest;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.dto.request.storefront.ShippingFeeData;
import com.vctek.orderservice.dto.request.storefront.StoreFrontCheckoutRequest;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.feignclient.dto.OrderBillRequest;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import com.vctek.orderservice.util.EventType;
import com.vctek.util.ComboType;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DefaultCommercePlaceOrderStrategyTest {
    @Mock
    private OrderService orderService;
    @Mock
    private CartService cartService;
    @Mock
    private BillService billService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private GenerateCartCodeService generateCartCodeService;

    @Mock
    private OrderSourceService orderSourceService;

    @Mock
    private CouponService couponService;

    @Mock
    private CalculationService calculationService;

    @Mock
    private LoyaltyService loyaltyService;

    @Mock
    private FinanceService financeService;

    @Mock
    private CommerceCartModification commerceCartModification;

    @Mock
    private CustomerService customerService;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private OrderSettingCustomerOptionService orderSettingCustomerOptionService;

    private DefaultCommercePlaceOrderStrategy strategy;
    @Mock
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;
    @Mock
    private OrderEntryRepository entryRepository;
    @Mock
    private PaymentTransactionService paymentTransactionService;
    private CommerceCheckoutParameter param = new CommerceCheckoutParameter();
    private CartModel cart = new CartModel();
    private OrderModel order = new OrderModel();
    private Set<PaymentTransactionModel> payments = new HashSet<>();
    private List<AbstractOrderEntryModel> entries = new ArrayList<>();
    private ArgumentCaptor<OrderModel>  captorOrder ;
    private PaymentMethodData  paymentMethodData ;
    private Long warehouseId = 404l;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        strategy = new DefaultCommercePlaceOrderStrategy();
        strategy.setCartService(cartService);
        strategy.setOrderService(orderService);
        strategy.setEventPublisher(eventPublisher);
        strategy.setBillService(billService);
        strategy.setGenerateCartCodeService(generateCartCodeService);
        strategy.setOrderSourceService(orderSourceService);
        strategy.setCouponService(couponService);
        strategy.setCalculationService(calculationService);
        strategy.setLoyaltyService(loyaltyService);
        strategy.setFinanceService(financeService);
        strategy.setCustomerService(customerService);
        strategy.setInvoiceService(invoiceService);
        strategy.setCommerceCartCalculationStrategy(commerceCartCalculationStrategy);
        strategy.setOrderSettingCustomerOptionService(orderSettingCustomerOptionService);
        strategy.setEntryRepository(entryRepository);
        strategy.setPaymentTransactionService(paymentTransactionService);
        order.setEntries(entries);
        order.setWarehouseId(warehouseId);
        param.setCart(cart);
        param.setPaymentTransactions(payments);
        param.setOrderSourceId(1l);
        List<Long> settingCustomerOptionIds = new ArrayList<>();
        settingCustomerOptionIds.add(1l);
        settingCustomerOptionIds.add(2l);
        param.setSettingCustomerOptionIds(settingCustomerOptionIds);
        captorOrder = ArgumentCaptor.forClass(OrderModel.class);
        paymentMethodData = new PaymentMethodData();
        paymentMethodData.setId(1l);
        when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
        when(customerService.update(eq(order), any(CustomerRequest.class))).thenReturn(new CustomerData());
    }

    @Test
    public void placeOrder() {
        cart = new CartModel();
        cart.setCompanyId(1l);
        order.setCompanyId(1L);
        AbstractOrderEntryModel orderEntryModel = new AbstractOrderEntryModel();
        orderEntryModel.setComboType(ComboType.FIXED_COMBO.toString());
        orderEntryModel.setQuantity(1l);
        orderEntryModel.setProductId(1l);
        cart.setEntries(Arrays.asList(orderEntryModel));
        Set<SubOrderEntryModel> subOrderEntrySet = new HashSet<>();
        SubOrderEntryModel subOrderEntry = new SubOrderEntryModel();
        subOrderEntry.setQuantity(1);
        subOrderEntrySet.add(subOrderEntry);
        orderEntryModel.setSubOrderEntries(subOrderEntrySet);
        ComboData comboData = new ComboData();
        comboData.setTotalItemQuantity(1);
        param.setCart(cart);


        payments.add(new PaymentTransactionModel());
        entries.add(orderEntryModel);
        when(orderService.createOrderFromCart(this.cart)).thenReturn(order);
        when(orderService.save(order)).thenReturn(order);

        OrderSourceModel orderSourceModel = new OrderSourceModel();
        orderSourceModel.setId(1l);
        when(orderSourceService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(orderSourceModel);

        strategy.placeOrder(param);
        verify(commerceCartCalculationStrategy).splitOrderPromotionToEntries(order);
        verify(cartService).delete(this.cart);
        verify(orderService, times(2)).save(order);
        verify(billService).createBillForOrder(any(OrderModel.class));
        verify(couponService).createCouponRedemption(order);
        verify(loyaltyService).assignCardToCustomerIfNeed(any(), any(), anyBoolean(), eq(warehouseId));
        verify(orderSettingCustomerOptionService, times(2)).findByIdAndCompanyId(anyLong(), anyLong());
    }

    @Test
    public void placeOrder_Online_ShouldStatusIsNew_NotCreateBill() {
        cart = new CartModel();
        cart.setCompanyId(1l);
        cart.setType(OrderType.ONLINE.toString());
        order.setCompanyId(1l);
        AbstractOrderEntryModel orderEntryModel = new AbstractOrderEntryModel();
        orderEntryModel.setComboType(ComboType.FIXED_COMBO.toString());
        orderEntryModel.setQuantity(1l);
        orderEntryModel.setProductId(1l);
        cart.setEntries(Arrays.asList(orderEntryModel));
        Set<SubOrderEntryModel> subOrderEntrySet = new HashSet<>();
        SubOrderEntryModel subOrderEntry = new SubOrderEntryModel();
        subOrderEntry.setQuantity(1);
        subOrderEntrySet.add(subOrderEntry);
        orderEntryModel.setSubOrderEntries(subOrderEntrySet);
        ComboData comboData = new ComboData();
        comboData.setTotalItemQuantity(1);
        param.setCart(cart);

        payments.add(new PaymentTransactionModel());
        entries.add(orderEntryModel);
        when(orderService.createOrderFromCart(this.cart)).thenReturn(order);
        order.setType(OrderType.ONLINE.toString());
        order.setOrderStatus(OrderStatus.COMPLETED.code());
        when(orderService.save(order)).thenReturn(order);

        OrderSourceModel orderSourceModel = new OrderSourceModel();
        orderSourceModel.setId(1l);
        when(orderSourceService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(orderSourceModel);

        strategy.placeOrder(param);
        verify(commerceCartCalculationStrategy).splitOrderPromotionToEntries(order);
        verify(cartService).delete(this.cart);
        verify(orderService, times(2)).save(order);
        verify(billService, times(0)).createBillForOrder(any(OrderModel.class));
        verify(couponService).createCouponRedemption(order);
        verify(loyaltyService).assignCardToCustomerIfNeed(any(), any(), anyBoolean(), eq(warehouseId));
        assertEquals(OrderStatus.NEW.code(), order.getOrderStatus());
    }

    @Test
    public void placeOrder_Online_ExchangeOrder_ShouldStatusIsCompleted_CreateBill() {
        cart = new CartModel();
        cart.setCompanyId(1l);
        cart.setType(OrderType.ONLINE.toString());
        cart.setExchange(true);
        order.setCompanyId(1l);
        AbstractOrderEntryModel orderEntryModel = new AbstractOrderEntryModel();
        orderEntryModel.setComboType(ComboType.FIXED_COMBO.toString());
        orderEntryModel.setQuantity(1l);
        orderEntryModel.setProductId(1l);
        cart.setEntries(Arrays.asList(orderEntryModel));
        Set<SubOrderEntryModel> subOrderEntrySet = new HashSet<>();
        SubOrderEntryModel subOrderEntry = new SubOrderEntryModel();
        subOrderEntry.setQuantity(1);
        subOrderEntrySet.add(subOrderEntry);
        orderEntryModel.setSubOrderEntries(subOrderEntrySet);
        ComboData comboData = new ComboData();
        comboData.setTotalItemQuantity(1);
        param.setCart(cart);

        payments.add(new PaymentTransactionModel());
        entries.add(orderEntryModel);
        when(orderService.createOrderFromCart(this.cart)).thenReturn(order);
        order.setOrderStatus(OrderStatus.COMPLETED.code());
        when(orderService.save(order)).thenReturn(order);
        order.setType(OrderType.ONLINE.toString());
        order.setExchange(true);

        OrderSourceModel orderSourceModel = new OrderSourceModel();
        orderSourceModel.setId(1l);
        when(orderSourceService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(orderSourceModel);

        strategy.placeOrder(param);
        verify(commerceCartCalculationStrategy).splitOrderPromotionToEntries(order);
        verify(cartService).delete(this.cart);
        verify(orderService, times(2)).save(order);
        verify(billService, times(1)).createBillForOrder(any(OrderModel.class));
        verify(couponService).createCouponRedemption(order);
        verify(loyaltyService).assignCardToCustomerIfNeed(any(), any(), anyBoolean(), eq(warehouseId));
        assertEquals(OrderStatus.COMPLETED.code(), order.getOrderStatus());
    }

    @Test
    public void placeOrder_Retail_createInvoice() {
        cart = new CartModel();
        cart.setCompanyId(1l);
        cart.setType(OrderType.RETAIL.toString());
        cart.setExchange(true);
        order.setCompanyId(1l);
        AbstractOrderEntryModel orderEntryModel = new AbstractOrderEntryModel();
        orderEntryModel.setComboType(ComboType.FIXED_COMBO.toString());
        orderEntryModel.setQuantity(1l);
        orderEntryModel.setProductId(1l);
        cart.setEntries(Arrays.asList(orderEntryModel));
        Set<SubOrderEntryModel> subOrderEntrySet = new HashSet<>();
        SubOrderEntryModel subOrderEntry = new SubOrderEntryModel();
        subOrderEntry.setQuantity(1);
        subOrderEntrySet.add(subOrderEntry);
        orderEntryModel.setSubOrderEntries(subOrderEntrySet);
        ComboData comboData = new ComboData();
        comboData.setTotalItemQuantity(1);
        param.setCart(cart);

        payments.add(new PaymentTransactionModel());
        entries.add(orderEntryModel);
        when(orderService.createOrderFromCart(this.cart)).thenReturn(order);
        order.setOrderStatus(OrderStatus.COMPLETED.code());
        when(orderService.save(order)).thenReturn(order);
        order.setType(OrderType.RETAIL.toString());
        order.setExchange(true);
        ReturnOrderModel returnOrderModel = new ReturnOrderModel();
        OrderModel originModel = new OrderModel();
        returnOrderModel.setOriginOrder(originModel);
        order.setReturnOrder(returnOrderModel);
        param.setCustomerRequest(new CustomerRequest());

        OrderSourceModel orderSourceModel = new OrderSourceModel();
        orderSourceModel.setId(1l);
        when(orderSourceService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(orderSourceModel);

        strategy.placeOrder(param);
        verify(commerceCartCalculationStrategy).splitOrderPromotionToEntries(order);
        verify(cartService).delete(this.cart);
        verify(orderService, times(2)).save(order);
        verify(billService, times(1)).createBillForOrder(any(OrderModel.class));
        verify(couponService).createCouponRedemption(order);
        verify(loyaltyService).assignCardToCustomerIfNeed(any(), any(), anyBoolean(), eq(warehouseId));
        verify(invoiceService).saveInvoices(any(OrderModel.class), any());
        assertEquals(OrderStatus.COMPLETED.code(), order.getOrderStatus());
    }

    @Test
    public void placeOrder_invalidOrderSourceId() {
        cart = new CartModel();
        cart.setCompanyId(1l);
        cart.setType(OrderType.ONLINE.toString());
        cart.setExchange(true);
        order.setCompanyId(1l);
        AbstractOrderEntryModel orderEntryModel = new AbstractOrderEntryModel();
        orderEntryModel.setComboType(ComboType.FIXED_COMBO.toString());
        orderEntryModel.setQuantity(1l);
        orderEntryModel.setProductId(1l);
        cart.setEntries(Arrays.asList(orderEntryModel));
        Set<SubOrderEntryModel> subOrderEntrySet = new HashSet<>();
        SubOrderEntryModel subOrderEntry = new SubOrderEntryModel();
        subOrderEntry.setQuantity(1);
        subOrderEntrySet.add(subOrderEntry);
        orderEntryModel.setSubOrderEntries(subOrderEntrySet);
        ComboData comboData = new ComboData();
        comboData.setTotalItemQuantity(1);
        param.setCart(cart);

        payments.add(new PaymentTransactionModel());
        entries.add(orderEntryModel);
        when(orderService.createOrderFromCart(this.cart)).thenReturn(order);
        order.setOrderStatus(OrderStatus.COMPLETED.code());
        when(orderService.save(order)).thenReturn(order);
        order.setType(OrderType.ONLINE.toString());
        order.setExchange(true);

        OrderSourceModel orderSourceModel = new OrderSourceModel();
        orderSourceModel.setId(1l);
        when(orderSourceService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
        try{
            strategy.placeOrder(param);
            fail("Must throw exception");
        }catch (ServiceException e){
            assertEquals(ErrorCodes.INVALID_ORDER_SOURCE_ID.code(),e.getCode());
        }


    }

    private OrderEntryModel generateEntry(Long id, Double finalPrice) {
        OrderEntryModel model = new OrderEntryModel();
        model.setId(id);
        model.setFinalPrice(finalPrice);
        return model;
    }

    @Test
    public void updateProductInReturnBillWithOrder() {
        OrderEntryModel entry = generateEntry(1l, 2000d);
        when(commerceCartModification.getEntry()).thenReturn(entry);
        strategy.updateProductInReturnBillWithOrder(order, commerceCartModification);
        verify(billService).updateProductInReturnBillWithOrder(any(OrderModel.class), any(CommerceCartModification.class));
    }

    @Test
    public void deleteProductInReturnBillWithOrder() {
        OrderEntryModel entry = generateEntry(1l, 2000d);
        CommerceCartModification commerceCartModification = new CommerceCartModification();
        commerceCartModification.setEntry(entry);

        strategy.deleteProductInReturnBillWithOrder(order, commerceCartModification);
        verify(billService).deleteProductInReturnBillWithOrder(any(OrderModel.class), any(CommerceCartModification.class));
    }

    @Test
    public void updateOrder() {
        UpdateOrderParameter updateOrderParameter = new UpdateOrderParameter();
        updateOrderParameter.setOrder(order);
        updateOrderParameter.setOrderSourceId(2L);
        order.setCompanyId(1L);
        OrderSourceModel orderSource = new OrderSourceModel();
        orderSource.setId(2L);
        when(orderService.save(order)).thenReturn(order);
        when(orderSourceService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(orderSource);
        when(loyaltyService.recalculateRewardAmount(any(OrderModel.class))).thenReturn(order);
        when(loyaltyService.updateRedeem(any(OrderModel.class))).thenReturn(new TransactionData());
        when(loyaltyService.updateReward(any(OrderModel.class))).thenReturn(new TransactionData());
        strategy.updateOrder(updateOrderParameter);
        verify(commerceCartCalculationStrategy).splitOrderPromotionToEntries(order);
        verify(loyaltyService).assignCardToCustomerIfNeed(any(), any(), anyBoolean(), eq(warehouseId));
        verify(orderService).save(captorOrder.capture());
        assertEquals(2L,captorOrder.getValue().getOrderSourceModel().getId(),0);
    }

    @Test
    public void changeBillToRetail() {
        order.setCode("code");
        order.setCompanyId(1l);
        order.setPaymentTransactions(Collections.emptySet());
        OrderEntryModel orderEntry = new OrderEntryModel();
        orderEntry.setSubOrderEntries(new HashSet<>(Arrays.asList(new SubOrderEntryModel())));
//        order.setAppliedCouponCodes(new HashSet<>(Arrays.asList(new CouponCodeModel())));
        entries.add(orderEntry);

        OrderModel orderModel = new OrderModel();
        orderModel.setId(32l);
        orderModel.setCompanyId(1l);
        when(orderService.save(any(OrderModel.class))).thenReturn(orderModel);
        when(generateCartCodeService.generateCartCode(any(AbstractOrderModel.class))).thenReturn("gdjkgdfjkgdfk");
        when(orderService.cloneOrderEntry(any(), any())).thenReturn(new OrderEntryModel());
        strategy.changeBillToRetail(order);
        verify(orderService).cloneSubOrderEntries(any(AbstractOrderEntryModel.class), any(AbstractOrderEntryModel.class));
        verify(orderService).transferCouponCodeToOrder(any(), any(OrderModel.class));
        verify(calculationService, times(1)).calculateTotals(any(OrderModel.class),eq(true));
        verify(orderService, times(3)).save(any(OrderModel.class));
        verify(eventPublisher, times(1)).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void placeOrder_exist_redeem_withType_ONLINE() {
        cart = new CartModel();
        cart.setCompanyId(1l);
        cart.setType(OrderType.ONLINE.toString());
        order.setCompanyId(1L);
        order.setType(OrderType.ONLINE.toString());
        AbstractOrderEntryModel orderEntryModel = new AbstractOrderEntryModel();
        orderEntryModel.setComboType(ComboType.FIXED_COMBO.toString());
        orderEntryModel.setQuantity(1l);
        orderEntryModel.setProductId(1l);
        cart.setEntries(Arrays.asList(orderEntryModel));
        Set<SubOrderEntryModel> subOrderEntrySet = new HashSet<>();
        SubOrderEntryModel subOrderEntry = new SubOrderEntryModel();
        subOrderEntry.setQuantity(1);
        subOrderEntrySet.add(subOrderEntry);
        orderEntryModel.setSubOrderEntries(subOrderEntrySet);
        ComboData comboData = new ComboData();
        comboData.setTotalItemQuantity(1);
        param.setCart(cart);
        param.setCardNumber("card");
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setAmount(10000d);
        paymentTransactionModel.setId(1l);
        paymentTransactionModel.setPaymentMethodId(1l);
        payments.add(paymentTransactionModel);
        param.setPaymentTransactions(payments);
        entries.add(orderEntryModel);
        when(orderService.createOrderFromCart(this.cart)).thenReturn(order);
        when(orderService.save(order)).thenReturn(order);
        OrderSourceModel orderSourceModel = new OrderSourceModel();
        orderSourceModel.setId(1l);
        when(orderSourceService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(orderSourceModel);
        LoyaltyCardData loyaltyCardData = new LoyaltyCardData();
        loyaltyCardData.setConversionRate(1000d);
        when(loyaltyService.findByCardNumber(anyString(),anyLong())).thenReturn(loyaltyCardData);
        TransactionData transactionData = new TransactionData();
        transactionData.setRedeemAmount(10000d);
        when(loyaltyService.createRedeemPending(any(),anyDouble())).thenReturn(transactionData);
        strategy.placeOrder(param);
        verify(commerceCartCalculationStrategy).splitOrderPromotionToEntries(order);
        verify(cartService).delete(this.cart);
        verify(orderService, times(2)).save(order);
        verify(billService, times(0)).createBillForOrder(any(OrderModel.class));
        verify(loyaltyService).assignCardToCustomerIfNeed(any(), any(), anyBoolean(), eq(warehouseId));
        verify(invoiceService).saveInvoices(eq(order), any());
        verify(loyaltyService, times(0)).redeem(any(), anyDouble());
        verify(loyaltyService, times(1)).createRedeemPending(any(), anyDouble());
    }

    @Test
    public void placeOrder_exist_redeem() {
        cart = new CartModel();
        cart.setCompanyId(1l);
        order.setCompanyId(1L);
        AbstractOrderEntryModel orderEntryModel = new AbstractOrderEntryModel();
        orderEntryModel.setComboType(ComboType.FIXED_COMBO.toString());
        orderEntryModel.setQuantity(1l);
        orderEntryModel.setProductId(1l);
        cart.setEntries(Arrays.asList(orderEntryModel));
        Set<SubOrderEntryModel> subOrderEntrySet = new HashSet<>();
        SubOrderEntryModel subOrderEntry = new SubOrderEntryModel();
        subOrderEntry.setQuantity(1);
        subOrderEntrySet.add(subOrderEntry);
        orderEntryModel.setSubOrderEntries(subOrderEntrySet);
        ComboData comboData = new ComboData();
        comboData.setTotalItemQuantity(1);
        param.setCart(cart);
        param.setCardNumber("card");
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        paymentTransactionModel.setAmount(10000d);
        paymentTransactionModel.setId(1l);
        paymentTransactionModel.setPaymentMethodId(1l);
        payments.add(paymentTransactionModel);
        param.setPaymentTransactions(payments);
        entries.add(orderEntryModel);
        when(orderService.createOrderFromCart(this.cart)).thenReturn(order);
        when(orderService.save(order)).thenReturn(order);
        OrderSourceModel orderSourceModel = new OrderSourceModel();
        orderSourceModel.setId(1l);
        when(orderSourceService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(orderSourceModel);
        LoyaltyCardData loyaltyCardData = new LoyaltyCardData();
        loyaltyCardData.setConversionRate(1000d);
        when(loyaltyService.findByCardNumber(anyString(),anyLong())).thenReturn(loyaltyCardData);
        TransactionData transactionData = new TransactionData();
        transactionData.setRedeemAmount(10000d);
        when(loyaltyService.redeem(any(),anyDouble())).thenReturn(transactionData);
        strategy.placeOrder(param);
        verify(commerceCartCalculationStrategy).splitOrderPromotionToEntries(order);
        verify(cartService).delete(this.cart);
        verify(orderService, times(2)).save(order);
        verify(billService, times(1)).createBillForOrder(any(OrderModel.class));
        verify(loyaltyService).assignCardToCustomerIfNeed(any(), any(), anyBoolean(), eq(warehouseId));
        verify(invoiceService).saveInvoices(eq(order), any());
        verify(loyaltyService, times(1)).redeem(any(), anyDouble());
        verify(loyaltyService, times(0)).createRedeemPending(any(), anyDouble());
    }

    @Test
    public void deleteProductOfComboInReturnBillWithOrder_NotUpdateBill() {
        when(billService.shouldUpdateBillOf(order)).thenReturn(false);
        strategy.deleteProductOfComboInReturnBillWithOrder(order, new OrderEntryModel(), new SubOrderEntryModel());
        verify(billService, times(0)).deleteProductOfComboInReturnBillWithOrder(any(), any());
        verify(orderService).saveEntry(any());
        verify(eventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void deleteProductOfComboInReturnBillWithOrder_UpdateBill() {
        when(billService.shouldUpdateBillOf(order)).thenReturn(true);
        strategy.deleteProductOfComboInReturnBillWithOrder(order, new OrderEntryModel(), new SubOrderEntryModel());
        verify(billService).deleteProductOfComboInReturnBillWithOrder(any(), any());
        verify(orderService).saveEntry(any());
        verify(eventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void revertComboSaleQuantity_NotUpdateBill() {
        OrderEntryModel entryModel = new OrderEntryModel();
        entryModel.setOrder(order);
        when(billService.shouldUpdateBillOf(order)).thenReturn(false);
        strategy.revertComboSaleQuantity(1l, entryModel);
        verify(billService, times(0)).revertComboSaleQuantity(1l, entryModel);
    }

    @Test
    public void revertComboSaleQuantity_UpdateBill() {
        OrderEntryModel entryModel = new OrderEntryModel();
        entryModel.setOrder(order);
        when(billService.shouldUpdateBillOf(order)).thenReturn(true);
        strategy.revertComboSaleQuantity(1l, entryModel);
        verify(billService, times(1)).revertComboSaleQuantity(1l, entryModel);
    }

    @Test
    public void cancelRedeem() {
        order.setCompanyId(2L);
        order.setRedeemAmount(1000d);
        order.setPaymentTransactions(payments);
        strategy.cancelRedeem(order);
        verify(orderService).save(any(OrderModel.class));
        verify(loyaltyService, times(1)).cancelPendingRedeem(any(OrderModel.class));
        assertNull(order.getRedeemAmount());
    }

    @Test
    public void updateRedeemOnline() {
        PaymentTransactionRequest request = new PaymentTransactionRequest();
        order.setCompanyId(2L);
        TransactionData transactionData = new TransactionData();
        transactionData.setRedeemAmount(20000d);
        transactionData.setPoint(20);
        when(loyaltyService.updatePendingRedeem(any(), any())).thenReturn(transactionData);
        strategy.updateRedeemOnline(order, request);
        verify(orderService).save(any(OrderModel.class));
        verify(loyaltyService, times(1)).updatePendingRedeem(any(OrderModel.class), any());
        verify(loyaltyService, times(0)).updateRedeem(any(OrderModel.class));
        verify(invoiceService, times(0)).saveInvoices(any(OrderModel.class), anyLong());
        verify(financeService, times(0)).getPaymentMethodByCode(anyString());
        assertEquals(20000d, order.getRedeemAmount(), 0);
    }

    @Test
    public void updateRedeemOnline_withStatus_Completed() {
        PaymentTransactionRequest request = new PaymentTransactionRequest();
        request.setAmount(2000d);
        PaymentTransactionModel payment = new PaymentTransactionModel();
        payment.setPaymentMethodId(2L);
        payments.add(payment);
        order.setOrderStatus(OrderStatus.COMPLETED.toString());
        order.setCompanyId(2L);
        order.setCustomerId(2L);
        order.setRedeemAmount(1000d);
        order.setPaymentTransactions(payments);
        PaymentMethodData paymentMethodData = new PaymentMethodData();
        paymentMethodData.setId(2L);
        TransactionData transactionData = new TransactionData();
        transactionData.setRedeemAmount(2000d);
        transactionData.setPoint(20);
        when(loyaltyService.updateRedeem(any())).thenReturn(transactionData);
        when(financeService.getPaymentMethodByCode(anyString())).thenReturn(paymentMethodData);
        strategy.updateRedeemOnline(order, request);
        verify(orderService).save(any(OrderModel.class));
        verify(loyaltyService, times(0)).updatePendingRedeem(any(OrderModel.class), any());
        verify(loyaltyService, times(1)).updateRedeem(any(OrderModel.class));
        verify(invoiceService, times(1)).saveInvoices(any(OrderModel.class), anyLong());
        verify(financeService, times(1)).getPaymentMethodByCode(anyString());
        assertEquals(2000d, order.getRedeemAmount(), 0);
    }

    @Test
    public void createRedeemOnline() {
        PaymentTransactionRequest request = new PaymentTransactionRequest();
        request.setPaymentMethodId(2L);
        order.setCompanyId(2L);
        TransactionData transactionData = new TransactionData();
        transactionData.setRedeemAmount(20000d);
        transactionData.setPoint(20);
        when(loyaltyService.createRedeemPending(any(), any())).thenReturn(transactionData);
        strategy.createRedeemOnline(order, request);
        verify(orderService).save(any(OrderModel.class));
        verify(loyaltyService, times(1)).createRedeemPending(any(OrderModel.class), any());
        verify(financeService, times(1)).getPaymentMethodByCode(any());
        assertEquals(20000d, order.getRedeemAmount(), 0);
        assertEquals(1, order.getPaymentTransactions().size());
    }

    @Test
    public void createRedeemOnline_OrderStatusIsCompleted() {
        PaymentTransactionRequest request = new PaymentTransactionRequest();
        request.setPaymentMethodId(2L);
        order.setCompanyId(2L);
        order.setOrderStatus(OrderStatus.COMPLETED.code());
        TransactionData transactionData = new TransactionData();
        transactionData.setRedeemAmount(20000d);
        transactionData.setPoint(20);
        when(loyaltyService.createRedeemPending(any(), any())).thenReturn(transactionData);
        strategy.createRedeemOnline(order, request);
        verify(orderService).save(any(OrderModel.class));
        verify(loyaltyService, times(1)).createRedeemPending(any(OrderModel.class), any());
        verify(financeService, times(1)).getPaymentMethodByCode(any());
        verify(invoiceService, times(1)).saveRedeemLoyaltyForOnlineOrChangeToRetailOrder(any(OrderModel.class));
        verify(loyaltyService, times(1)).completeRedeemLoyaltyForOnline(any(OrderModel.class));
        assertEquals(20000d, order.getRedeemAmount(), 0);
        assertEquals(1, order.getPaymentTransactions().size());
    }

    @Test
    public void populateCreateOrderEvent_PlaceRetail() {
        order.setOrderStatus(OrderStatus.COMPLETED.code());
        order.setType(OrderType.RETAIL.toString());

        OrderEvent orderEvent = strategy.populateCreateOrderEvent(order);
        assertEquals(EventType.CREATE, orderEvent.getEventType());
    }

    @Test
    public void populateCreateOrderEvent_PlaceWholesale() {
        order.setOrderStatus(OrderStatus.COMPLETED.code());
        order.setType(OrderType.WHOLESALE.toString());

        OrderEvent orderEvent = strategy.populateCreateOrderEvent(order);
        assertEquals(EventType.CREATE, orderEvent.getEventType());
    }

    @Test
    public void populateCreateOrderEvent_PlaceOnline() {
        order.setOrderStatus(OrderStatus.COMPLETED.code());
        order.setType(OrderType.ONLINE.toString());
        order.setExchange(false);

        OrderEvent orderEvent = strategy.populateCreateOrderEvent(order);
        assertEquals(EventType.CREATE, orderEvent.getEventType());
    }

    @Test
    public void populateCreateOrderEvent_PlaceExchangeOnline() {
        order.setOrderStatus(OrderStatus.COMPLETED.code());
        order.setType(OrderType.ONLINE.toString());
        order.setExchange(true);

        OrderEvent orderEvent = strategy.populateCreateOrderEvent(order);
        assertEquals(EventType.CHANGE_COMPLETED_ONLINE, orderEvent.getEventType());
    }

    @Test
    public void changeBillToRetailForKafkaImportOrderStatus() {
        order.setCode("code");
        order.setCompanyId(1l);
        order.setId(2L);
        OrderEntryModel orderEntry = new OrderEntryModel();
        when(entryRepository.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(orderEntry));
        when(paymentTransactionService.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(new PaymentTransactionModel()));
        OrderModel orderModel = new OrderModel();
        orderModel.setId(32l);
        orderModel.setCompanyId(1l);
        when(commerceCartCalculationStrategy.calculateTotalRewardAmount(any())).thenReturn(null);
        when(orderService.save(any(OrderModel.class))).thenReturn(orderModel);
        when(generateCartCodeService.generateCartCode(any(AbstractOrderModel.class))).thenReturn("gdjkgdfjkgdfk");
        when(orderService.cloneOrderFormModel(any())).thenReturn(order);
        when(orderService.cloneOrderEntry(any(), any())).thenReturn(new OrderEntryModel());
        strategy.changeBillToRetailForKafkaImportOrderStatus(order);
        verify(orderService).cloneSubOrderEntriesForKafkaImportOrderStatus(any(AbstractOrderEntryModel.class), any(OrderEntryModel.class));
        verify(orderService).cloneToppingOptionsForKafkaImportOrderStatus(any(AbstractOrderEntryModel.class), any(OrderEntryModel.class));
        verify(orderService).cloneSettingCustomerOption(any(), any(OrderModel.class));
        verify(orderService).transferPromotionsToOrderForKafkaImportOrderStatus(any(), any(OrderModel.class));
        verify(orderService).transferCouponCodeToOrderForKafkaImportOrderStatus(any(), any(OrderModel.class));
        verify(calculationService, times(1)).calculateTotals(any(OrderModel.class),eq(true));
        verify(commerceCartCalculationStrategy, times(1)).calculateTotalRewardAmount(any(OrderModel.class));
        verify(orderService, times(3)).save(any(OrderModel.class));
    }

    @Test
    public void updateSettingCustomerToOrder_emptySettingOptionModel() {
        order.setCompanyId(2L);
        when(orderSettingCustomerOptionService.findAllByCompanyIdAndIdIn(anyLong(), anyList())).thenReturn(new ArrayList<>());
        when(orderService.save(any())).thenReturn(order);
        strategy.updateSettingCustomerToOrder(order, Arrays.asList(2L));
        verify(orderSettingCustomerOptionService).findAllByCompanyIdAndIdIn(anyLong(), anyList());
        verify(orderService).save(any(OrderModel.class));
        assertEquals(0, order.getOrderSettingCustomerOptionModels().size());
    }

    @Test
    public void updateSettingCustomerToOrder() {
        order.setCompanyId(2L);
        when(orderSettingCustomerOptionService.findAllByCompanyIdAndIdIn(anyLong(), anyList())).thenReturn(Arrays.asList(new OrderSettingCustomerOptionModel()));
        when(orderService.save(any())).thenReturn(order);
        strategy.updateSettingCustomerToOrder(order, Arrays.asList(2L));
        verify(orderSettingCustomerOptionService).findAllByCompanyIdAndIdIn(anyLong(), anyList());
        verify(orderService).save(any(OrderModel.class));
        assertEquals(1, order.getOrderSettingCustomerOptionModels().size());
    }

    @Test
    public void updateCustomerInfoInOnlineOrder() {
        UpdateOrderParameter parameter = new UpdateOrderParameter();
        CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setId(2L);
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setProvinceId(2L);
        customerRequest.setShippingAddress(addressRequest);
        parameter.setCustomerRequest(customerRequest);
        order.setOrderStatus(OrderStatus.CONFIRMING.code());
        parameter.setOrder(order);
        strategy.updateCustomerInfoInOnlineOrder(parameter);
        verify(orderService).save(any(OrderModel.class));
        verify(eventPublisher).publishEvent(any(OrderEvent.class));
    }

    @Test
    public void updateComboInReturnBillWithOrder() {
        strategy.updateComboInReturnBillWithOrder(new OrderModel(), new AbstractOrderEntryModel());
        verify(billService).updateComboInReturnBillWithOrder(any(OrderModel.class), any(AbstractOrderEntryModel.class));
        verify(orderService).save(any(OrderModel.class));
    }

    @Test
    public void updateOrDeleteToppingInReturnBillWithOrder() {
        strategy.updateOrDeleteToppingInReturnBillWithOrder(new OrderModel(), Arrays.asList(new OrderBillRequest()));
        verify(billService).updateOrDeleteToppingInReturnBillWithOrder(any(OrderModel.class), anyList());
    }

    @Test
    public void addProductToReturnBill() {
        strategy.addProductToReturnBill(new OrderModel(), new AbstractOrderEntryModel());
        verify(billService).addProductToReturnBill(any(OrderModel.class), any(AbstractOrderEntryModel.class));
    }

    @Test
    public void storefrontPlaceOrder() {
        CommerceCheckoutParameter parameter = new CommerceCheckoutParameter();
        parameter.setCart(cart);
        parameter.setOrderSourceId(2L);
        parameter.setCustomerRequest(new CustomerRequest());
        AbstractOrderEntryModel entry = new AbstractOrderEntryModel();
        entry.setProductId(2L);
        entries.add(entry);
        ProductSearchModel productSearchModel = new ProductSearchModel();
        productSearchModel.setId(2L);
        productSearchModel.setWeight(1000d);
        order.setCompanyId(2L);
        when(orderService.createOrderFromCart(any(CartModel.class))).thenReturn(order);
        when(orderSourceService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(new OrderSourceModel());
        when(customerService.update(any(OrderModel.class), any(CustomerRequest.class))).thenReturn(new CustomerData());
        when(orderService.save(any(OrderModel.class))).thenReturn(order);
        strategy.storefrontPlaceOrder(parameter);
        verify(cartService).delete(any(CartModel.class));
        verify(orderService, times(2)).save(any(OrderModel.class));
        verify(commerceCartCalculationStrategy).splitOrderPromotionToEntries(any(OrderModel.class));
    }

    @Test
    public void updateAddressShipping() {
        ShippingFeeData shippingFeeData = new ShippingFeeData();
        StoreFrontCheckoutRequest request = new StoreFrontCheckoutRequest();
        request.setCustomer(new CustomerRequest());
        when(orderService.save(any(OrderModel.class))).thenReturn(order);
        strategy.updateAddressShipping(order, shippingFeeData, request);
        verify(calculationService).calculate(any(OrderModel.class));
        verify(customerService).update(any(OrderModel.class), any(CustomerRequest.class));
    }
}
