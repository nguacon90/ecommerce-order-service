package com.vctek.orderservice.service.impl;

import com.vctek.kafka.data.InvoiceKafkaData;
import com.vctek.migration.dto.InvoiceLinkDto;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.feignclient.FinanceClient;
import com.vctek.orderservice.feignclient.dto.InvoiceData;
import com.vctek.orderservice.feignclient.dto.ItemData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.repository.PaymentTransactionRepository;
import com.vctek.orderservice.service.AuthService;
import com.vctek.util.BillStatus;
import com.vctek.util.OrderType;
import com.vctek.util.PaymentMethodType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class PaymentTransactionServiceTest {
    private PaymentTransactionServiceImpl service;
    @Mock
    private PaymentTransactionRepository repository;
    @Mock
    private OrderModel orderMock;
    @Mock
    private FinanceClient financeClient;
    @Mock
    private AuthService authService;

    @Before
    public  void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new PaymentTransactionServiceImpl(repository);
        service.setFinanceClient(financeClient);
        service.setAuthService(authService);
    }

    @Test
    public void save() {
        service.save(new PaymentTransactionModel());
        verify(repository).save(any(PaymentTransactionModel.class));
    }

    @Test
    public void delete() {
        service.delete(new PaymentTransactionModel());
        verify(repository).delete(any(PaymentTransactionModel.class));
    }

    @Test
    public void findAll() {
        service.findAll();
        verify(repository).findAll();
    }

    @Test
    public void findById() {
        service.findById(anyLong());
        verify(repository).findById(anyLong());
    }

    @Test
    public void removePaymentByInvoice_RetailShouldIgnore() {
        when(orderMock.getType()).thenReturn(OrderType.RETAIL.toString());
        service.removePaymentByInvoice(orderMock, 1l);

        verify(repository, times(0)).deleteAll(anyList());
    }

    @Test
    public void removePaymentByInvoice_WholeSaleShouldIgnore() {
        when(orderMock.getType()).thenReturn(OrderType.WHOLESALE.toString());
        service.removePaymentByInvoice(orderMock, 1l);

        verify(repository, times(0)).deleteAll(anyList());
    }

    @Test
    public void removePaymentByInvoice_OnlineButNotFoundPaymentsShouldIgnore() {
        when(orderMock.getType()).thenReturn(OrderType.ONLINE.toString());
        when(repository.findAllByOrderModelAndInvoiceId(orderMock, 1l)).thenReturn(Collections.emptyList());
        service.removePaymentByInvoice(orderMock, 1l);

        verify(repository, times(0)).deleteAll(anyList());
    }

    @Test
    public void removePaymentByInvoice_OnlineFoundPaymentsShouldRemove() {
        when(orderMock.getType()).thenReturn(OrderType.ONLINE.toString());
        when(repository.findAllByOrderModelAndInvoiceId(orderMock, 1l)).thenReturn(Arrays.asList(new PaymentTransactionModel()));
        service.removePaymentByInvoice(orderMock, 1l);

        verify(repository, times(1)).deleteAll(anyList());
    }

    @Test
    public void updatePaymentByInvoice_RetailShouldIgnore() {
        InvoiceKafkaData invoiceKafkaData = new InvoiceKafkaData();
        when(orderMock.getType()).thenReturn(OrderType.RETAIL.toString());
        service.updatePaymentByInvoice(orderMock, invoiceKafkaData);
        verify(repository, times(0)).saveAll(anyList());
    }

    @Test
    public void updatePaymentByInvoice_WholeSaleShouldIgnore() {
        InvoiceKafkaData invoiceKafkaData = new InvoiceKafkaData();
        when(orderMock.getType()).thenReturn(OrderType.WHOLESALE.toString());
        service.updatePaymentByInvoice(orderMock, invoiceKafkaData);

        verify(repository, times(0)).saveAll(anyList());
    }

    @Test
    public void updatePaymentByInvoice_OnlineButNotFoundPaymentsEmpty() {
        InvoiceKafkaData invoiceKafkaData = new InvoiceKafkaData();
        invoiceKafkaData.setMoneySourceId(2L);
        invoiceKafkaData.setPaymentMethodId(2L);
        when(orderMock.getType()).thenReturn(OrderType.ONLINE.toString());
        when(orderMock.getCode()).thenReturn("code");
        when(repository.findByMoneySourceIdAndPaymentMethodIdAndOrderCode(anyLong(), anyLong(), anyString())).thenReturn(Collections.emptyList());
        service.updatePaymentByInvoice(orderMock, invoiceKafkaData);
        verify(repository, times(0)).saveAll(anyList());
    }

    @Test
    public void updatePaymentByInvoice_OnlineFoundPaymentsShouldRemove() {
        InvoiceKafkaData invoiceKafkaData = new InvoiceKafkaData();
        invoiceKafkaData.setMoneySourceId(2L);
        invoiceKafkaData.setPaymentMethodId(2L);
        invoiceKafkaData.setStatus(BillStatus.UNVERIFIED.code());
        PaymentTransactionModel payment = new PaymentTransactionModel();
        when(orderMock.getType()).thenReturn(OrderType.ONLINE.toString());
        when(orderMock.getCode()).thenReturn("code");
        when(repository.findByMoneySourceIdAndPaymentMethodIdAndOrderCode(anyLong(), anyLong(), anyString())).thenReturn(Arrays.asList(payment));
        service.updatePaymentByInvoice(orderMock, invoiceKafkaData);

        verify(repository, times(1)).saveAll(anyList());
        assertEquals(true, payment.isDeleted());
    }

    @Test
    public void updatePaymentByInvoice_OnlineFoundPaymentsShouldUpdate() {
        InvoiceKafkaData invoiceKafkaData = new InvoiceKafkaData();
        invoiceKafkaData.setMoneySourceId(2L);
        invoiceKafkaData.setInvoiceId(2L);
        invoiceKafkaData.setPaymentMethodId(2L);
        invoiceKafkaData.setStatus(BillStatus.VERIFIED.code());
        PaymentTransactionModel payment = new PaymentTransactionModel();
        when(orderMock.getType()).thenReturn(OrderType.ONLINE.toString());
        when(orderMock.getCode()).thenReturn("code");
        when(repository.findByMoneySourceIdAndPaymentMethodIdAndOrderCode(anyLong(), anyLong(), anyString())).thenReturn(Arrays.asList(payment));
        service.updatePaymentByInvoice(orderMock, invoiceKafkaData);

        verify(repository, times(1)).saveAll(anyList());
        assertNotNull(payment.getInvoiceId());
    }

    @Test
    public void findAllByOrderCode() {
        service.findAllByOrderCode("code");
        verify(repository).findAllByOrderCode(anyString());
    }

    @Test
    public void findAllByReturnOrder() {
        service.findAllByReturnOrder(new ReturnOrderModel());
        verify(repository).findAllByReturnOrder(any(ReturnOrderModel.class));
    }

    @Test
    public void saveAll() {
        service.saveAll(Arrays.asList(new PaymentTransactionModel()));
        verify(repository).saveAll(anyList());
    }

    @Test
    public void findAllPaymentInvoiceOrder_empty_currentUserId() {
        when(authService.isCurrentCustomerUserOrAnonymous()).thenReturn(true);
        service.findAllPaymentInvoiceOrder(new OrderModel());
        verify(financeClient, times(0)).findAllOrderInvoices(anyLong(), anyString(), anyLong(), anyString());
    }

    @Test
    public void findAllPaymentInvoiceOrder_empty_Data() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCompanyId(2L);
        orderModel.setCode("code");
        when(authService.isCurrentCustomerUserOrAnonymous()).thenReturn(false);
        when(financeClient.findAllOrderInvoices(anyLong(), anyString(), any(), anyString())).thenReturn(new ArrayList<>());
        List<PaymentTransactionData> dataList = service.findAllPaymentInvoiceOrder(orderModel);
        verify(financeClient, times(1)).findAllOrderInvoices(anyLong(), anyString(), any(), anyString());
        assertEquals(0, dataList.size(), 0);
    }

    @Test
    public void findAllPaymentInvoiceOrder() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCompanyId(2L);
        orderModel.setCode("code");
        List<InvoiceData> invoiceData = new ArrayList<>();
        InvoiceData data = new InvoiceData();
        data.setStatus(BillStatus.VERIFIED.code());
        data.setId(2L);
        data.setFinalAmount(20000d);
        ItemData itemData = new ItemData();
        itemData.setId(2L);
        itemData.setType("type");
        data.setMoneySource(itemData);
        data.setPaymentMethod(itemData);
        invoiceData.add(data);
        when(authService.isCurrentCustomerUserOrAnonymous()).thenReturn(false);
        when(financeClient.findAllOrderInvoices(anyLong(), anyString(), any(), anyString())).thenReturn(invoiceData);
        List<PaymentTransactionData> dataList = service.findAllPaymentInvoiceOrder(orderModel);
        verify(financeClient, times(1)).findAllOrderInvoices(anyLong(), anyString(), any(), anyString());
        assertEquals(1, dataList.size(), 0);
    }

    @Test
    public void findAllPaymentInvoiceReturnOrder_emptyData() {
        ReturnOrderModel returnOrderModel = new ReturnOrderModel();
        returnOrderModel.setCompanyId(2L);
        returnOrderModel.setId(2L);
        when(financeClient.findAllOrderInvoices(anyLong(), anyString(), anyLong(), anyString())).thenReturn(new ArrayList<>());
        List<PaymentTransactionData> data = service.findAllPaymentInvoiceReturnOrder(returnOrderModel);
        assertEquals(0, data.size(), 0);
    }

    @Test
    public void findAllPaymentInvoiceReturnOrder() {
        ReturnOrderModel returnOrderModel = new ReturnOrderModel();
        returnOrderModel.setCompanyId(2L);
        returnOrderModel.setId(2L);
        List<InvoiceData> invoiceData = new ArrayList<>();
        InvoiceData data = new InvoiceData();
        data.setStatus(BillStatus.VERIFIED.code());
        data.setId(2L);
        data.setFinalAmount(20000d);
        ItemData itemData = new ItemData();
        itemData.setId(2L);
        itemData.setType("type");
        data.setMoneySource(itemData);
        data.setPaymentMethod(itemData);
        invoiceData.add(data);
        when(financeClient.findAllOrderInvoices(anyLong(), any(), anyLong(), eq(OrderType.RETURN_ORDER.toString()))).thenReturn(invoiceData);
        List<PaymentTransactionData> paymentInvoiceReturnOrder = service.findAllPaymentInvoiceReturnOrder(returnOrderModel);
        assertEquals(1, paymentInvoiceReturnOrder.size(), 0);
    }

    @Test
    public void findAllForMigratePaymentMethod() {
        service.findAllForMigratePaymentMethod(PageRequest.of(0, 20));
        verify(repository).findAllByInvoiceIdIsNotNull(any(PageRequest.class));
    }

    @Test
    public void findPaymentForInvoiceLink() {
        InvoiceLinkDto dto = new InvoiceLinkDto();
        dto.setMoneySourceId(2L);
        dto.setPaymentMethodId(2L);
        dto.setOrderCode("code");
        service.findPaymentForInvoiceLink(dto);
        verify(repository).findByMoneySourceIdAndPaymentMethodIdAndOrderCode(anyLong(), anyLong(), anyString());
    }

    @Test
    public void findByMoneySourceIdAndPaymentMethodIdAndReturnOrderExternalIdAndCompanyId() {
        service.findByMoneySourceIdAndPaymentMethodIdAndReturnOrderExternalIdAndCompanyId(2L, 3L, 4L, 5L);
        verify(repository).findByMoneySourceIdAndPaymentMethodIdAndReturnOrderExternalIdAndCompanyId(anyLong(), anyLong(), anyLong(), anyLong());
    }

    @Test
    public void removePaymentByInvoice_typeNotEquals_ONLINE() {
        OrderModel orderModel = new OrderModel();
        orderModel.setType(OrderType.RETAIL.toString());
        service.removePaymentByInvoice(orderModel, 5L);
        verify(repository, times(0)).deleteAll(anyList());
    }

    @Test
    public void removePaymentByInvoice() {
        OrderModel orderModel = new OrderModel();
        orderModel.setType(OrderType.ONLINE.toString());
        List<PaymentTransactionModel> paymentTransactionModels = new ArrayList<>();
        PaymentTransactionModel payment = new PaymentTransactionModel();
        paymentTransactionModels.add(payment);
        when(repository.findAllByOrderModelAndInvoiceId(any(), anyLong())).thenReturn(paymentTransactionModels);
        service.removePaymentByInvoice(orderModel, 5L);
        verify(repository).deleteAll(anyList());
    }

    @Test
    public void findLoyaltyRedeem_emptyPaymentModel() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCode("code");
        when(repository.findAllByOrderCode(anyString())).thenReturn(new ArrayList<>());
        service.findLoyaltyRedeem(orderModel);
        verify(financeClient, times(0)).getPaymentMethodDataByCode(eq(PaymentMethodType.LOYALTY_POINT.code()));
    }

    @Test
    public void findLoyaltyRedeem() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCode("code");
        List<PaymentTransactionModel> paymentTransactionModels = new ArrayList<>();
        PaymentTransactionModel payment = new PaymentTransactionModel();
        payment.setPaymentMethodId(2L);
        payment.setDeleted(false);
        paymentTransactionModels.add(payment);
        PaymentMethodData paymentMethodData = new PaymentMethodData();
        paymentMethodData.setId(2L);
        when(repository.findAllByOrderCode(anyString())).thenReturn(paymentTransactionModels);
        when(financeClient.getPaymentMethodDataByCode(eq(PaymentMethodType.LOYALTY_POINT.code()))).thenReturn(paymentMethodData);
        PaymentTransactionModel model = service.findLoyaltyRedeem(orderModel);
        verify(financeClient, times(1)).getPaymentMethodDataByCode(eq(PaymentMethodType.LOYALTY_POINT.code()));
        assertNotNull(model);
    }

    @Test
    public void resetPaymentForLoyaltyRedeem_emptyPaymentModel() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCode("code");
        when(repository.findAllByOrderCode(anyString())).thenReturn(new ArrayList<>());
        service.resetPaymentForLoyaltyRedeem(orderModel);
        verify(repository, times(0)).saveAll(anyList());
    }

    @Test
    public void resetPaymentForLoyaltyRedeem() {
        OrderModel orderModel = new OrderModel();
        orderModel.setCode("code");
        List<PaymentTransactionModel> paymentTransactionModels = new ArrayList<>();
        PaymentTransactionModel payment = new PaymentTransactionModel();
        payment.setPaymentMethodId(2L);
        payment.setDeleted(false);
        paymentTransactionModels.add(payment);
        PaymentMethodData paymentMethodData = new PaymentMethodData();
        paymentMethodData.setId(2L);
        when(repository.findAllByOrderCode(anyString())).thenReturn(paymentTransactionModels);
        when(financeClient.getPaymentMethodDataByCode(eq(PaymentMethodType.LOYALTY_POINT.code()))).thenReturn(paymentMethodData);
        service.resetPaymentForLoyaltyRedeem(orderModel);
        verify(repository, times(1)).saveAll(anyList());
    }
}
