package com.vctek.orderservice.service.impl;

import com.vctek.dto.CheckCreateTransferWarehouseData;
import com.vctek.dto.request.CheckCreateTransferWarehouseRequest;
import com.vctek.orderservice.feignclient.LogisticClient;
import com.vctek.orderservice.feignclient.dto.DistributorSetingPriceData;
import com.vctek.orderservice.util.DiscountType;
import com.vctek.util.OrderStatus;
import com.vctek.util.SettingPriceType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogisticServiceImplTest {
    private LogisticServiceImpl logisticService;
    @Mock
    private LogisticClient logisticClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        logisticService = new LogisticServiceImpl(logisticClient);
    }

    @Test
    public void findByIdAndCompanyId() {
        logisticService.findByIdAndCompanyId(2L, 3L);
        verify(logisticClient).getBasicWarehouse(anyLong(), anyLong());
    }

    @Test
    public void getProductPriceSetting() {
        logisticService.getProductPriceSetting(2L, 3L, Arrays.asList(2L));
        verify(logisticClient).getProductPriceSetting(anyLong(), anyLong(), anyList());
    }

    @Test
    public void getDetailDistributor() {
        logisticService.getDetailDistributor(2L, 3L);
        verify(logisticClient).getDetailDistributor(anyLong(), anyLong());
    }

    @Test
    public void calculateDistributorSettingPrice_settingTypeEquals_PriceNet() {
        DistributorSetingPriceData setingPriceData = new DistributorSetingPriceData();
        setingPriceData.setType(SettingPriceType.PRICE_NET.toString());
        setingPriceData.setPrice(1000d);
        Double price = logisticService.calculateDistributorSettingPrice(setingPriceData, 5000d);
        assertEquals(1000d, price, 0);
    }

    @Test
    public void calculateDistributorSettingPrice_settingTypeEquals_PriceDiscount() {
        DistributorSetingPriceData setingPriceData = new DistributorSetingPriceData();
        setingPriceData.setType(SettingPriceType.PRICE_BY_DISCOUNT.toString());
        setingPriceData.setDiscount(1000d);
        setingPriceData.setDiscountType(DiscountType.CASH.toString());
        Double price = logisticService.calculateDistributorSettingPrice(setingPriceData, 5000d);
        assertEquals(4000d, price, 0);
    }

    @Test
    public void calculateDistributorSettingPrice_settingType_null() {
        DistributorSetingPriceData setingPriceData = new DistributorSetingPriceData();
        setingPriceData.setDiscount(1000d);
        setingPriceData.setDiscountType(DiscountType.CASH.toString());
        Double price = logisticService.calculateDistributorSettingPrice(setingPriceData, 5000d);
        assertEquals(5000d, price, 0);
    }

    @Test
    public void checkValidCreateTransferWarehouse_data_null() {
        String orderCode = "code";
        CheckCreateTransferWarehouseRequest request = new CheckCreateTransferWarehouseRequest();
        request.setOrderStatus(OrderStatus.SHIPPING.code());
        request.setCurrentOrderStatus(OrderStatus.CONFIRMED.code());
        request.setWarehouseId(2L);
        request.setOrderCode(orderCode);
        CheckCreateTransferWarehouseRequest validRequest = new CheckCreateTransferWarehouseRequest();
        validRequest.setRequests(Arrays.asList(request));
        Map<String, CheckCreateTransferWarehouseData> checkCreateTransferWarehouseDataMap = new HashMap<>();
        checkCreateTransferWarehouseDataMap.put(orderCode, new CheckCreateTransferWarehouseData());
        when(logisticClient.checkValidCreateTransferWarehouse(validRequest)).thenReturn(checkCreateTransferWarehouseDataMap);
        Map<String, CheckCreateTransferWarehouseData> checkValid = logisticService.checkValidCreateTransferWarehouse(validRequest);
        assertFalse(checkValid.get(orderCode).isHasCreateTransferWarehouse());
    }

    @Test
    public void checkValidCreateTransferWarehouse() {
        String orderCode = "code";
        CheckCreateTransferWarehouseRequest request = new CheckCreateTransferWarehouseRequest();
        request.setOrderStatus(OrderStatus.SHIPPING.code());
        request.setCurrentOrderStatus(OrderStatus.CONFIRMED.code());
        request.setWarehouseId(2L);
        request.setOrderCode(orderCode);
        CheckCreateTransferWarehouseRequest validRequest = new CheckCreateTransferWarehouseRequest();
        validRequest.setRequests(Arrays.asList(request));
        Map<String, CheckCreateTransferWarehouseData> checkCreateTransferWarehouseDataMap = new HashMap<>();
        CheckCreateTransferWarehouseData data = new CheckCreateTransferWarehouseData();
        checkCreateTransferWarehouseDataMap.put(orderCode, data);
        when(logisticClient.checkValidCreateTransferWarehouse(validRequest)).thenReturn(checkCreateTransferWarehouseDataMap);
        Map<String, CheckCreateTransferWarehouseData> checkValid = logisticService.checkValidCreateTransferWarehouse(validRequest);
        assertFalse(checkValid.get(orderCode).isHasCreateTransferWarehouse());
    }
}
