package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.dto.PrintSettingData;
import com.vctek.dto.redis.AddressData;
import com.vctek.exception.ServiceException;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.producer.ProductInfoKafkaData;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.OrderFileParameter;
import com.vctek.orderservice.dto.request.OrderPartialIndexRequest;
import com.vctek.orderservice.dto.request.OrderReportRequest;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.elasticsearch.model.OrderEntryData;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.OrderElasticSearchService;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.kafka.producer.OrderProducerService;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderSettingCustomerOptionModel;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.util.PriceType;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class OrderElasticSearchFacadeTest {
    private OrderElasticSearchFacadeImpl orderElasticSearchFacade;
    private Converter<OrderModel, OrderSearchModel> orderSearchModelConverter;
    private OrderService orderService;
    private OrderElasticSearchService orderElasticSearchService;
    private Page<OrderModel> data;
    private OrderPartialIndexRequest request;
    private OrderSearchRequest orderSearchRequest;
    private int numberOfThread = 5;
    private int bulkSize = 100;
    @Mock
    private PermissionFacade permissionFacade;
    @Mock
    private AuthService authService;
    @Mock
    private OrderReportRequest orderReportReq;
    @Mock
    private OrderHistoryService orderHistoryServiceMock;
    @Mock
    private ProductInfoKafkaData kafkaProductMock;
    private OrderEntryData subEntry = new OrderEntryData();
    @Mock
    private OrderProducerService producerService;
    @Mock
    private OrderFileService orderFileService;
    @Mock
    private LogisticService logisticService;
    @Mock
    private CRMService crmService;
    @Mock
    private ProductSearchService productSearchService;
    @Mock
    private Executor executor;
    @Mock
    private Converter<OrderSearchRequest, OrderFileParameter> fileParameterConverter;
    @Mock
    private OrderSettingCustomerOptionService settingCustomerOptionService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        executor = Executors.newFixedThreadPool(1);
        request = new OrderPartialIndexRequest();
        request.setCompanyId(1l);
        orderSearchRequest = new OrderSearchRequest();
        data = mock(Page.class);
        orderSearchModelConverter = mock(Converter.class);
        orderService = mock(OrderService.class);
        orderElasticSearchService = mock(OrderElasticSearchService.class);
        orderElasticSearchFacade = new OrderElasticSearchFacadeImpl(orderSearchModelConverter, orderService,
                orderElasticSearchService, numberOfThread, bulkSize);
        orderElasticSearchFacade.setAuthService(authService);
        orderElasticSearchFacade.setPermissionFacade(permissionFacade);
        orderElasticSearchFacade.setOrderHistoryService(orderHistoryServiceMock);
        orderElasticSearchFacade.setProducerService(producerService);
        orderElasticSearchFacade.setOrderFileService(orderFileService);
        orderElasticSearchFacade.setExportExcelExecutor(executor);
        orderElasticSearchFacade.setFileParameterConverter(fileParameterConverter);
        orderElasticSearchFacade.setLogisticService(logisticService);
        orderElasticSearchFacade.setCrmService(crmService);
        orderElasticSearchFacade.setProductSearchService(productSearchService);
        orderElasticSearchFacade.setSettingCustomerOptionService(settingCustomerOptionService);
        when(orderService.findAll(any(Pageable.class))).thenReturn(data, data);
    }

    @Test
    public void index() {
        when(orderSearchModelConverter.convert(any(OrderModel.class))).thenReturn(new OrderSearchModel());
        orderElasticSearchFacade.index(new OrderModel());
        verify(orderSearchModelConverter).convert(any(OrderModel.class));
        verify(orderElasticSearchService).save(any(OrderSearchModel.class));
    }

    @Test
    public void fullIndex_EmptyData() {
        when(data.getContent()).thenReturn(Collections.emptyList());
        orderElasticSearchFacade.fullIndex();
        verify(orderSearchModelConverter, times(0)).convertAll(anyList());
        verify(orderElasticSearchService, times(0)).bulkIndex(anyList());
    }

    @Test
    public void fullIndex_OnlyOnePage() throws InterruptedException {
        when(data.getContent()).thenReturn(Arrays.asList(new OrderModel()), Collections.EMPTY_LIST);
        orderElasticSearchFacade.fullIndex();
        Thread.sleep(500);
        verify(orderService, times(6)).findAll(any(Pageable.class));
    }

    @Test
    public void partialIndex_ByBillId() {
        request.setOrderCode("2233");
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderModel());
        when(orderSearchModelConverter.convert(any(OrderModel.class))).thenReturn(new OrderSearchModel());

        orderElasticSearchFacade.partialIndex(request);

        verify(orderService).findByCodeAndCompanyId(anyString(), anyLong());
        verify(orderSearchModelConverter).convert(any(OrderModel.class));
        verify(orderElasticSearchService).save(any(OrderSearchModel.class));
    }

    @Test
    public void partialIndex_ByFromDate_emptyData() {
        request.setFromDate(Calendar.getInstance().getTime());
        when(orderService.findAllByFromDate(any(Pageable.class), any(Date.class)))
                .thenReturn(data);
        when(data.getContent()).thenReturn(Collections.emptyList());

        orderElasticSearchFacade.partialIndex(request);

        verify(orderSearchModelConverter, times(0)).convertAll(anyList());
        verify(orderElasticSearchService, times(0)).bulkIndex(anyList());
    }

    @Test
    public void partialIndex_ByFromDate() throws InterruptedException {
        request.setFromDate(Calendar.getInstance().getTime());
        when(orderService.findAllByAndCompanyIdFromDate(any(), any(Date.class), any(Pageable.class)))
                .thenReturn(data);
        when(data.getContent()).thenReturn(Arrays.asList(new OrderModel()), Collections.EMPTY_LIST);

        orderElasticSearchFacade.partialIndex(request);
        Thread.sleep(200);
        verify(orderService, times(6)).findAllByAndCompanyIdFromDate(any(), any(Date.class),
                any(Pageable.class));

    }

    @Test
    public void search() {
        orderSearchRequest.setId(10l);
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setFromCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setToCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setVerifiedBy(10l);
        orderSearchRequest.setWarehouseId(10l);
        orderSearchRequest.setDistributorId(10l);
        orderSearchRequest.setProduct("name");
        orderSearchRequest.setFinalPrice("{\"lt\":2000}");
        orderSearchRequest.setHolding(true);
        orderSearchRequest.setPreOrder(true);
        orderSearchRequest.setOrderSourceId(1l);
        orderSearchRequest.setOrderSourceIds(Arrays.asList(1l, 2l));
        orderSearchRequest.setOrderStatus(OrderStatus.COMPLETED.code());
        orderSearchRequest.setOrderStatusList(OrderStatus.COMPLETED.code());
        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void search_FinalPrice_Lt() {
        orderSearchRequest.setId(10l);
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setFromCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setToCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setVerifiedBy(10l);
        orderSearchRequest.setWarehouseId(10l);
        orderSearchRequest.setFinalPrice("{\"lt\":2000}");
        orderSearchRequest.setProduct("name");
        orderSearchRequest.setCompanyId(1l);

        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void search_NoFilter_HasPermissionManageAllWarehouse() {
        orderSearchRequest.setCompanyId(2l);
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(true);
        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void search_NoFilter_HasNotManageAnyWarehouse() {
        try {
            orderSearchRequest.setCompanyId(2l);
            when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(false);
            when(authService.getAllWarehouseOfCurrentUser(anyLong())).thenReturn(Collections.emptyList());
            orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.ACCESS_DENIED.code(), e.getCode());
        }
    }

    @Test
    public void search_NoFilter_HasNotManageOneWarehouse() {
        orderSearchRequest.setCompanyId(2l);
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(false);
        when(authService.getAllWarehouseOfCurrentUser(anyLong())).thenReturn(Collections.singletonList(12l));
        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void search_FinalPrice_Gt() {
        orderSearchRequest.setId(10l);
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setFromCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setToCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setVerifiedBy(10l);
        orderSearchRequest.setWarehouseId(10l);
        orderSearchRequest.setFinalPrice("{\"gt\":2000}");
        orderSearchRequest.setProduct("name");
        orderSearchRequest.setCompanyId(1l);

        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void search_FinalPrice_EQUAL() {
        orderSearchRequest.setId(10l);
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setFromCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setToCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setVerifiedBy(10l);
        orderSearchRequest.setWarehouseId(10l);
        orderSearchRequest.setFinalPrice("{\"equal\":2000}");
        orderSearchRequest.setProduct("name");
        orderSearchRequest.setCompanyId(1l);

        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void search_invalid_json_format() {
        try {
            orderSearchRequest.setId(10l);
            orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
            orderSearchRequest.setFromCreatedTime(Calendar.getInstance().getTime());
            orderSearchRequest.setToCreatedTime(Calendar.getInstance().getTime());
            orderSearchRequest.setCreatedBy(10l);
            orderSearchRequest.setVerifiedBy(10l);
            orderSearchRequest.setWarehouseId(10l);
            orderSearchRequest.setFinalPrice("{equal:2000}");
            orderSearchRequest.setProduct("name");
            orderSearchRequest.setCompanyId(1l);

            orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_JSON_FORMAT_OF_SEARCH_FIELD.code(), e.getCode());
        }
    }

    @Test
    public void search_FromCreateTime() {
        orderSearchRequest.setId(10l);
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setFromCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setVerifiedBy(10l);
        orderSearchRequest.setWarehouseId(10l);
        orderSearchRequest.setFinalPrice("{\"equal\":2000}");
        orderSearchRequest.setProduct("name");
        orderSearchRequest.setCompanyId(1l);

        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void search_ToCreateTime() {
        orderSearchRequest.setId(10l);
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setToCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setVerifiedBy(10l);
        orderSearchRequest.setWarehouseId(10l);
        orderSearchRequest.setFinalPrice("{\"equal\":2000}");
        orderSearchRequest.setProduct("name");
        orderSearchRequest.setCompanyId(1l);

        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void search_ProductId() {
        orderSearchRequest.setId(10l);
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setToCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setVerifiedBy(10l);
        orderSearchRequest.setWarehouseId(10l);
        orderSearchRequest.setFinalPrice("{\"equal\":2000}");
        orderSearchRequest.setProduct("1");
        orderSearchRequest.setCompanyId(1l);
        orderSearchRequest.setCurrentStatus(OrderStatus.CHANGE_TO_RETAIL.code());
        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void search_Age() {
        orderSearchRequest.setId(10l);
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setToCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setVerifiedBy(10l);
        orderSearchRequest.setWarehouseId(10l);
        orderSearchRequest.setFinalPrice("{\"equal\":2000}");
        orderSearchRequest.setProduct("1");
        orderSearchRequest.setCompanyId(1l);
        orderSearchRequest.setAge("MIDDLE_AGE");
        orderSearchRequest.setFromModifiedTimeLastStatus(Calendar.getInstance().getTime());

        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void search_invalidPaymentMethodId() {
        orderSearchRequest.setId(10l);
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setToCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setVerifiedBy(10l);
        orderSearchRequest.setWarehouseId(10l);
        orderSearchRequest.setFinalPrice("{\"equal\":2000}");
        orderSearchRequest.setProduct("1");
        orderSearchRequest.setCompanyId(1l);
        orderSearchRequest.setWarehouseId(1l);
        orderSearchRequest.setPaymentMethodId("CASH");
        orderSearchRequest.setCurrentStatus(OrderStatus.CHANGE_TO_RETAIL.code());
        orderSearchRequest.setToModifiedTimeLastStatus(Calendar.getInstance().getTime());
        try {
            orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));

        } catch (ServiceException err) {
            assertEquals(ErrorCodes.INVALID_PAYMENT_METHOD_ID.code(), err.getCode());
        }

    }

    @Test
    public void search_DeliveryTime() {
        orderSearchRequest.setId(10l);
        orderSearchRequest.setOrderType(OrderType.ONLINE.toString());
        orderSearchRequest.setToDeliveryTime(Calendar.getInstance().getTime());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setVerifiedBy(10l);
        orderSearchRequest.setWarehouseId(10l);
        orderSearchRequest.setFinalPrice("{\"equal\":2000}");
        orderSearchRequest.setProduct("name");
        orderSearchRequest.setCompanyId(1l);

        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void search_PaymentMethod() {
        orderSearchRequest.setId(10l);
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setToCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setVerifiedBy(10l);
        orderSearchRequest.setFinalPrice("{\"equal\":2000}");
        orderSearchRequest.setProduct("1");
        orderSearchRequest.setCompanyId(1l);
        orderSearchRequest.setPaymentMethodId("-1");
        orderSearchRequest.setWarehouseIds("17,4,5");
        orderSearchRequest.setCurrentStatusList(OrderStatus.CHANGE_TO_RETAIL.code());
        orderSearchRequest.setFromModifiedTimeLastStatus(Calendar.getInstance().getTime());
        orderSearchRequest.setToModifiedTimeLastStatus(Calendar.getInstance().getTime());
        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void search_mustNot_settingCustomerOption() {
        OrderSettingCustomerOptionModel optionModel = new OrderSettingCustomerOptionModel();
        optionModel.setId(2L);
        orderSearchRequest.setId(10l);
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setToCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setVerifiedBy(10l);
        orderSearchRequest.setFinalPrice("{\"equal\":2000}");
        orderSearchRequest.setProduct("1");
        orderSearchRequest.setCompanyId(1l);
        orderSearchRequest.setCustomerOptionIds(Arrays.asList(-1L));
        orderSearchRequest.setWarehouseIds("17,4,5");
        orderSearchRequest.setFromModifiedTimeLastStatus(Calendar.getInstance().getTime());
        orderSearchRequest.setToModifiedTimeLastStatus(Calendar.getInstance().getTime());
        when(settingCustomerOptionService.findAllByCompanyNotHasOrder(anyLong())).thenReturn(Arrays.asList(optionModel));
        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void search_must_settingCustomerOption() {
        orderSearchRequest.setId(10l);
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setToCreatedTime(Calendar.getInstance().getTime());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setVerifiedBy(10l);
        orderSearchRequest.setFinalPrice("{\"equal\":2000}");
        orderSearchRequest.setProduct("1");
        orderSearchRequest.setCompanyId(1l);
        orderSearchRequest.setCustomerOptionIds(Arrays.asList(1L));
        orderSearchRequest.setWarehouseIds("17,4,5");
        orderSearchRequest.setFromModifiedTimeLastStatus(Calendar.getInstance().getTime());
        orderSearchRequest.setToModifiedTimeLastStatus(Calendar.getInstance().getTime());
        orderElasticSearchFacade.search(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }

    @Test
    public void exportExcelOrder() {
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setCode("112233");
        orderSearchRequest.setCompanyId(2l);

        OrderSearchModel orderSearchModel = new OrderSearchModel();
        orderSearchModel.setId("112233");

        OrderEntryData entryData = new OrderEntryData();
        entryData.setId(12l);
        entryData.setName("product");
        entryData.setSku("sku");
        entryData.setTotalPrice(1000d);
        orderSearchModel.setOrderEntries(Arrays.asList(entryData));

        OrderEntryExcelData orderEntryExcelData = new OrderEntryExcelData();
        orderEntryExcelData.setId("112233");
        orderEntryExcelData.setName("112233");

        OrderSearchExcelData orderSearchExcelData = new OrderSearchExcelData();
        orderSearchExcelData.setOrderEntryExcelData(Arrays.asList(orderEntryExcelData));

        Page<OrderSearchModel> pages = new PageImpl<>(Arrays.asList(orderSearchModel));
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(true);
        when(orderElasticSearchService.search(any())).thenReturn(pages, Page.empty());
        when(orderElasticSearchService.exportExcelOrder(any(), any())).thenReturn(orderSearchExcelData);
        OrderSearchExcelData data = orderElasticSearchFacade.exportExcelOrder(orderSearchRequest, true);
        assertEquals(1, data.getOrderEntryExcelData().size());
    }

    @Test
    public void createOrderHistoryReport_ByProduct() throws InterruptedException {
        when(orderReportReq.getProductId()).thenReturn(1l);
        when(orderReportReq.getCompanyId()).thenReturn(1l);
        when(orderHistoryServiceMock.findAllByCompanyIdAndProductId(anyLong(), anyLong(), any()))
                .thenReturn(new PageImpl<>(Arrays.asList(new OrderHistoryModel()), PageRequest.of(0, 500), 1), Page.empty());
        orderElasticSearchFacade.createOrderHistoryReport(orderReportReq);
        Thread.sleep(200);
        verify(orderHistoryServiceMock, times(6)).findAllByCompanyIdAndProductId(anyLong(), anyLong(), any());
        verify(orderHistoryServiceMock, times(0)).findAllByAndCompanyId(anyLong(), any());

    }

    @Test
    public void createOrderReport_ByCode() {
        when(orderReportReq.getProductId()).thenReturn(1l);
        when(orderReportReq.getCompanyId()).thenReturn(1l);
        when(orderReportReq.getOrderCodes()).thenReturn(Arrays.asList("orderCode"));
        when(orderReportReq.getKafkaMessageType()).thenReturn(KafkaMessageType.RECALCULATE_ALL_ORDER_REPORT.toString());
        when(orderService.findByCodeAndCompanyIdAndDeleted(anyString(), anyLong(), anyBoolean()))
                .thenReturn(new OrderModel());
        orderElasticSearchFacade.createOrderReport(orderReportReq);
        verify(producerService, times(1)).recalculateOrderReport(any(), any(), any());
    }

    @Test
    public void createOrderReport_AllByCompany() throws InterruptedException {
        when(orderReportReq.getProductId()).thenReturn(1l);
        when(orderReportReq.getCompanyId()).thenReturn(1l);
        when(orderReportReq.getKafkaMessageType()).thenReturn(KafkaMessageType.RECALCULATE_FACT_PROMOTION.toString());
        when(orderService.findAllByAndCompanyIdFromDate(anyLong(), any(), any()))
                .thenReturn(new PageImpl<>(Arrays.asList(new OrderModel()), PageRequest.of(0, 500), 1), Page.empty());
        orderElasticSearchFacade.createOrderReport(orderReportReq);
        Thread.sleep(200);
        verify(producerService, times(1)).recalculateOrderReport(any(), any(), any());
    }

    @Test
    public void updateSkuOrName() {
        when(kafkaProductMock.getCompanyId()).thenReturn(1l);
        when(kafkaProductMock.getId()).thenReturn(11111l);
        OrderSearchModel orderSearchModel = new OrderSearchModel();
        OrderEntryData orderEntryData = new OrderEntryData();
        orderEntryData.setId(11111l);
        subEntry.setId(11111l);
        orderEntryData.setSubOrderEntries(Arrays.asList(subEntry));
        orderSearchModel.setOrderEntries(Arrays.asList(orderEntryData));
        when(orderElasticSearchService.search(any())).thenReturn(new PageImpl<>(Arrays.asList(orderSearchModel)),
                Page.empty());
        orderElasticSearchFacade.updateSkuOrName(kafkaProductMock);
        verify(orderElasticSearchService).bulkIndexOrderEntries(anyList());
    }

    @Test
    public void requestExportExcelAllProduct() {
        orderSearchRequest.setOrderType(OrderType.RETAIL.toString());
        orderSearchRequest.setCode("112233");
        orderSearchRequest.setCompanyId(2l);

        OrderSearchModel orderSearchModel = new OrderSearchModel();
        orderSearchModel.setId("112233");

        OrderEntryData entryData = new OrderEntryData();
        entryData.setId(12l);
        entryData.setName("product");
        entryData.setSku("sku");
        entryData.setTotalPrice(1000d);
        orderSearchModel.setOrderEntries(Arrays.asList(entryData));

        OrderEntryExcelData orderEntryExcelData = new OrderEntryExcelData();
        orderEntryExcelData.setId("112233");
        orderEntryExcelData.setName("112233");

        OrderSearchExcelData orderSearchExcelData = new OrderSearchExcelData();
        orderSearchExcelData.setOrderEntryExcelData(Arrays.asList(orderEntryExcelData));

        Page<OrderSearchModel> pages = new PageImpl<>(Arrays.asList(orderSearchModel));
        when(permissionFacade.hasPermission(anyString(), anyLong())).thenReturn(true);
        when(orderElasticSearchService.search(any())).thenReturn(pages, Page.empty());
        when(orderElasticSearchService.exportExcelOrder(any(), any())).thenReturn(orderSearchExcelData);
        when(authService.getCurrentUserId()).thenReturn(1l);

        orderElasticSearchFacade.requestExportExcelAllProduct(orderSearchRequest);
        verify(fileParameterConverter, times(1)).convert(any(OrderSearchRequest.class));

    }

    @Test
    public void exportExcelOrderTypeDistributor() {
        OrderModel orderModel = new OrderModel();
        orderModel.setType(OrderType.ONLINE.toString());
        orderModel.setDistributorId(2L);
        orderModel.setCompanyId(2L);
        orderModel.setWarehouseId(2L);
        orderModel.setFinalPrice(2000d);
        orderModel.setCustomerId(2L);
        orderModel.setShippingAddressId(2L);
        orderModel.setCreatedTime(Calendar.getInstance().getTime());
        orderModel.setPriceType(PriceType.DISTRIBUTOR_PRICE.toString());

        AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
        entryModel.setProductId(2L);
        orderModel.setEntries(Arrays.asList(entryModel));

        ProductSearchModel productSearchModel = new ProductSearchModel();
        productSearchModel.setId(2L);

        DistributorData distributorData = new DistributorData();
        InvoiceInformationData invoiceInformationData = new InvoiceInformationData();
        distributorData.setInvoices(Arrays.asList(invoiceInformationData));
        when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(orderModel);
        when(logisticService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(new WarehouseData());
        when(logisticService.getDetailDistributor(anyLong(), anyLong())).thenReturn(distributorData);
        when(crmService.getPrintSettingById(anyLong(), anyLong())).thenReturn(new PrintSettingData());
        when(crmService.getCustomer(anyLong(), anyLong())).thenReturn(new CustomerData());
        when(crmService.getAddress(anyLong())).thenReturn(new AddressData());
        when(productSearchService.findAllByCompanyId(any())).thenReturn(new ArrayList<>(Arrays.asList(productSearchModel)));

        orderElasticSearchFacade.exportExcelOrderTypeDistributor(2L, "code", 2L);
        verify(logisticService, times(1)).findByIdAndCompanyId(anyLong(), anyLong());
        verify(orderService, times(1)).findByCodeAndCompanyId(anyString(), anyLong());
        verify(crmService, times(1)).getPrintSettingById(anyLong(), anyLong());
        verify(crmService, times(1)).getCustomer(anyLong(), anyLong());
        verify(crmService, times(1)).getAddress(anyLong());
        verify(productSearchService, times(1)).findAllByCompanyId(any());
    }

    @Test
    public void exportExcelOrderTypeDistributor_invalidOrder() {
        try {
            when(orderService.findByCodeAndCompanyId(anyString(), anyLong())).thenReturn(null);
            orderElasticSearchFacade.exportExcelOrderTypeDistributor(2L, "code", 2L);
            fail("fail throw Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_ID.message(), e.getMessage());
        }
    }

    @Test
    public void orderStorefrontSearch() {
        orderSearchRequest.setOrderType(OrderType.ONLINE.toString());
        orderSearchRequest.setCreatedBy(10l);
        orderSearchRequest.setCompanyId(1l);
        orderSearchRequest.setProduct("123");
        orderSearchRequest.setSellSignal(SellSignal.ECOMMERCE_WEB.toString());
        orderElasticSearchFacade.orderStorefrontSearch(orderSearchRequest, PageRequest.of(0, 10));
        verify(orderElasticSearchService).search(any(SearchQuery.class));
    }
}
