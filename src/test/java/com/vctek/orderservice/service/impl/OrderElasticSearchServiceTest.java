package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.OrderSearchExcelData;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.dto.ShippingCompanyData;
import com.vctek.orderservice.dto.WarehouseData;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.elasticsearch.model.OrderEntryData;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.repository.OrderSearchRepository;
import com.vctek.orderservice.elasticsearch.service.impl.OrderElasticSearchServiceImpl;
import com.vctek.orderservice.feignclient.BillClient;
import com.vctek.orderservice.feignclient.FinanceClient;
import com.vctek.orderservice.feignclient.LogisticClient;
import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.service.OrderSourceService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.util.Arrays;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderElasticSearchServiceTest {

    private OrderElasticSearchServiceImpl service;
    private OrderSearchRepository repository;
    private ElasticsearchTemplate template;
    private BillClient billClient;
    private OrderSourceService orderSourceService;
    private FinanceClient financeClient;
    private LogisticClient logisticClient;
    private OrderSearchModel model;
    private OrderEntryData entryData;

    @Before
    public void setUp() {
        repository = mock(OrderSearchRepository.class);
        template = mock(ElasticsearchTemplate.class);
        billClient = mock(BillClient.class);
        orderSourceService = mock(OrderSourceService.class);
        financeClient = mock(FinanceClient.class);
        logisticClient =mock(LogisticClient.class);
        service = new OrderElasticSearchServiceImpl(repository, template);
        service.setBillClient(billClient);
        service.setFinanceClient(financeClient);
        service.setOrderSourceService(orderSourceService);
        service.setLogisticClient(logisticClient);

        model = new OrderSearchModel();
        model.setId("1122");
        model.setCompanyId(2l);
        model.setWarehouseId(2l);
        model.setCreatedTime(Calendar.getInstance().getTime());

        entryData = new OrderEntryData();
        entryData.setId(1l);
        entryData.setSku("sku");
        entryData.setBarcode("barcode");
        entryData.setQuantity(5l);
        entryData.setPrice(5000d);
        entryData.setName("product");
    }

    @Test
    public void exportExcelOrder() {
        OrderSearchRequest request = new OrderSearchRequest();
        request.setCompanyId(2l);
        WarehouseData warehouseData = new WarehouseData();
        warehouseData.setId(2l);
        warehouseData.setName("warehouse");

        OrderSourceModel orderSourceModel = new OrderSourceModel();
        orderSourceModel.setId(2l);
        orderSourceModel.setName("order source");

        PaymentMethodData paymentMethodData = new PaymentMethodData();
        paymentMethodData.setId(2l);
        paymentMethodData.setName("payment method");

        ShippingCompanyData shippingCompanyData = new ShippingCompanyData();
        shippingCompanyData.setId(2l);
        shippingCompanyData.setName("Anh vuong");


        model.setOrderEntries(Arrays.asList(entryData));

        when(billClient.getWarehouseByCompany(anyLong())).thenReturn(Arrays.asList(warehouseData));
        when(orderSourceService.findAllByCompanyId(anyLong())).thenReturn(Arrays.asList(orderSourceModel));
        when(financeClient.getPaymentMethodDataByCompanyId(anyLong())).thenReturn(Arrays.asList(paymentMethodData));
        when(logisticClient.getShippingCompanyByCompany(anyLong())).thenReturn(Arrays.asList(shippingCompanyData));
        OrderSearchExcelData data = service.exportExcelOrder(Arrays.asList(model), request);
        assertEquals(1, data.getOrderEntryExcelData().size());
    }
}
