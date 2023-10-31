package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.dto.LoyaltyRewardSearchExcelData;
import com.vctek.orderservice.dto.request.OrderPartialIndexRequest;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.elasticsearch.model.LoyaltyRewardRateSearchModel;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.service.LoyaltyRewardRateSearchService;
import com.vctek.orderservice.elasticsearch.service.OrderElasticSearchService;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.ProductLoyaltyRewardRateService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LoyaltyRewardRateSearchFacadeTest {
    private LoyaltyRewardRateSearchFacadeImpl facade;
    private Converter<ProductLoyaltyRewardRateModel, LoyaltyRewardRateSearchModel> loyaltyRewardRateSearchConverter;
    private LoyaltyRewardRateSearchService searchService;
    private ProductLoyaltyRewardRateService service;
    private Page<ProductLoyaltyRewardRateModel> data;
    private int numberOfThread = 5;
    private int bulkSize = 100;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        data = mock(Page.class);
        loyaltyRewardRateSearchConverter = mock(Converter.class);
        service = mock(ProductLoyaltyRewardRateService.class);
        searchService = mock(LoyaltyRewardRateSearchService.class);
        facade = new LoyaltyRewardRateSearchFacadeImpl(loyaltyRewardRateSearchConverter, numberOfThread, bulkSize);
        facade.setSearchService(searchService);
        facade.setService(service);
    }

    @Test
    public void index() {
        when(loyaltyRewardRateSearchConverter.convert(any(ProductLoyaltyRewardRateModel.class))).thenReturn(new LoyaltyRewardRateSearchModel());
        facade.index(new ProductLoyaltyRewardRateModel());
        verify(loyaltyRewardRateSearchConverter).convert(any(ProductLoyaltyRewardRateModel.class));
        verify(searchService).save(any(LoyaltyRewardRateSearchModel.class));
    }

    @Test
    public void fullIndex_EmptyData() {
        when(data.getContent()).thenReturn(Collections.emptyList());
        facade.fullIndex();
        verify(loyaltyRewardRateSearchConverter, times(0)).convertAll(anyList());
        verify(searchService, times(0)).bulkIndex(anyList());
    }

    @Test
    public void exportExcelOrder() {
        LoyaltyRewardRateSearchModel model = new LoyaltyRewardRateSearchModel();
        model.setProductId(1l);
        model.setProductName("name");
        model.setProductSku("sku");
        model.setRewardRate(10d);
        Page<LoyaltyRewardRateSearchModel> loyaltyRewardRateSearchModelPage = new PageImpl<>(Arrays.asList(model));
        when(searchService.search(any())).thenReturn(loyaltyRewardRateSearchModelPage, Page.empty());
        LoyaltyRewardSearchExcelData data = facade.exportExcel(1l);
        assertEquals(1, data.getDataList().size());
    }
}
