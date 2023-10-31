package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CategoryLoyaltyRewardRateData;
import com.vctek.orderservice.dto.DefaultLoyaltyRewardRateData;
import com.vctek.orderservice.dto.ProductLoyaltyRewardRateData;
import com.vctek.orderservice.dto.excel.ProductLoyaltyRewardRateDTO;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateRequest;
import com.vctek.orderservice.excel.ProductLoyaltyRewardRateExcelFileReader;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.CategoryLoyaltyRewardRateModel;
import com.vctek.orderservice.model.DefaultLoyaltyRewardRateModel;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import com.vctek.orderservice.service.CategoryLoyaltyRewardRateService;
import com.vctek.orderservice.service.DefaultLoyaltyRewardRateService;
import com.vctek.orderservice.service.ProductLoyaltyRewardRateService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.redis.elastic.ProductSearchData;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Fail.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class LoyaltyRewardRateFacadeImplTest {
    private LoyaltyRewardRateFacadeImpl facade;
    private DefaultLoyaltyRewardRateService defaultLoyaltyRewardRateService;
    private ProductLoyaltyRewardRateService productLoyaltyRewardRateService;
    private CategoryLoyaltyRewardRateService categoryLoyaltyRewardRateService;
    private Converter<DefaultLoyaltyRewardRateModel, DefaultLoyaltyRewardRateData> defaultLoyaltyRewardRateConverter;
    private Converter<ProductLoyaltyRewardRateModel, ProductLoyaltyRewardRateData> productLoyaltyRewardRateConverter;
    private Converter<CategoryLoyaltyRewardRateModel, CategoryLoyaltyRewardRateData> categoryLoyaltyRewardRateConverter;
    private ProductService productService;
    private ProductLoyaltyRewardRateExcelFileReader productLoyaltyRewardRateExcelFileReader;
    private MultipartFile multiplePartFileMock;

    @Before
    public void setup() {
        defaultLoyaltyRewardRateService = mock(DefaultLoyaltyRewardRateService.class);
        productLoyaltyRewardRateService = mock(ProductLoyaltyRewardRateService.class);
        categoryLoyaltyRewardRateService = mock(CategoryLoyaltyRewardRateService.class);
        defaultLoyaltyRewardRateConverter = mock(Converter.class);
        productLoyaltyRewardRateConverter = mock(Converter.class);
        categoryLoyaltyRewardRateConverter = mock(Converter.class);
        productService = mock(ProductService.class);
        productLoyaltyRewardRateExcelFileReader = mock(ProductLoyaltyRewardRateExcelFileReader.class);
        multiplePartFileMock = mock(MultipartFile.class);
        facade = new LoyaltyRewardRateFacadeImpl();
        facade.setDefaultLoyaltyRewardRateConverter(defaultLoyaltyRewardRateConverter);
        facade.setProductLoyaltyRewardRateConverter(productLoyaltyRewardRateConverter);
        facade.setCategoryLoyaltyRewardRateConverter(categoryLoyaltyRewardRateConverter);
        facade.setDefaultLoyaltyRewardRateService(defaultLoyaltyRewardRateService);
        facade.setProductLoyaltyRewardRateService(productLoyaltyRewardRateService);
        facade.setCategoryLoyaltyRewardRateService(categoryLoyaltyRewardRateService);
        facade.setProductService(productService);
        facade.setProductLoyaltyRewardRateExcelFileReader(productLoyaltyRewardRateExcelFileReader);
    }

    @Test
    public void findBy() {
        when(defaultLoyaltyRewardRateService.findByCompanyId(anyLong())).thenReturn(new DefaultLoyaltyRewardRateModel());
        when(categoryLoyaltyRewardRateService.findAllByCompanyId(anyLong())).thenReturn(Collections.singletonList(new CategoryLoyaltyRewardRateModel()));
        facade.findBy(1L);
        verify(defaultLoyaltyRewardRateConverter).convert(any(DefaultLoyaltyRewardRateModel.class));
        verify(categoryLoyaltyRewardRateConverter).convertAll(anyList());
    }

    @Test
    public void createOrUpdate_default() {
        LoyaltyRewardRateDetailRequest request = new LoyaltyRewardRateDetailRequest();
        when(defaultLoyaltyRewardRateService.createOrUpdate(any(LoyaltyRewardRateDetailRequest.class))).thenReturn(new DefaultLoyaltyRewardRateModel());
        facade.createOrUpdateDefault(request);
        verify(defaultLoyaltyRewardRateConverter).convert(any(DefaultLoyaltyRewardRateModel.class));
    }

    @Test
    public void createOrUpdate_product() {
        LoyaltyRewardRateRequest request = new LoyaltyRewardRateRequest();
        when(productLoyaltyRewardRateService.createOrUpdate(any(LoyaltyRewardRateRequest.class))).thenReturn(new ArrayList<>());
        facade.createOrUpdateProduct(request);
        verify(productLoyaltyRewardRateConverter).convertAll(anyList());
    }

    @Test
    public void createOrUpdate_category() {
        LoyaltyRewardRateRequest request = new LoyaltyRewardRateRequest();
        when(categoryLoyaltyRewardRateService.createOrUpdate(any(LoyaltyRewardRateRequest.class))).thenReturn(new ArrayList<>());
        facade.createOrUpdateCategory(request);
        verify(categoryLoyaltyRewardRateConverter).convertAll(anyList());
    }

    @Test
    public void importExcelProduct_emptyData() {
        try {
            when(productLoyaltyRewardRateExcelFileReader.read(multiplePartFileMock)).thenReturn(Collections.emptyList());
            facade.importExcelProduct(2l, multiplePartFileMock);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_IMPORT_PRODUCT_LOYALTY_REWARD_RATE.code(), e.getCode());
        }
    }

    @Test
    public void importExcelProduct_hasErrors() {
        List<ProductLoyaltyRewardRateDTO> dtoList = new ArrayList<>();
        ProductLoyaltyRewardRateDTO dto1 = new ProductLoyaltyRewardRateDTO();
        dto1.setProductId("12");
        dto1.setProductSku("sku1");
        dtoList.add(dto1);
        ProductLoyaltyRewardRateDTO dto2 = new ProductLoyaltyRewardRateDTO();
        dto2.setProductId("13");
        dto2.setProductSku("sku2");
        dtoList.add(dto2);

        ProductLoyaltyRewardRateModel rewardRateModel = new ProductLoyaltyRewardRateModel();
        rewardRateModel.setProductId(12l);
        rewardRateModel.setRewardRate(5d);

        ProductSearchData productSearchData = new ProductSearchData();
        productSearchData.setSku("sku1");
        productSearchData.setId(12l);
        productSearchData.setName("name1");
        when(productLoyaltyRewardRateExcelFileReader.read(multiplePartFileMock)).thenReturn(dtoList);
        when(productService.search(any())).thenReturn(Arrays.asList(productSearchData)).thenReturn(new ArrayList<>());
        when(productLoyaltyRewardRateService.findAllByCompanyIdAndProductIds(anyLong(), anyList())).thenReturn(Arrays.asList(rewardRateModel));
        ProductLoyaltyRewardRateDTO rewardRateDTOS = facade.importExcelProduct(2l, multiplePartFileMock);
        verify(productService, times(2)).search(any(ProductSearchRequest.class));
        verify(productLoyaltyRewardRateService).findAllByCompanyIdAndProductIds(anyLong(), anyList());
        assertEquals(1, rewardRateDTOS.getRewardRateData().size());
        assertEquals(1, rewardRateDTOS.getErrors().size());
    }

    @Test
    public void importExcelProduct() {
        List<ProductLoyaltyRewardRateDTO> dtoList = new ArrayList<>();
        ProductLoyaltyRewardRateDTO dto1 = new ProductLoyaltyRewardRateDTO();
        dto1.setProductId("12");
        dto1.setProductSku("sku1");
        dtoList.add(dto1);
        ProductLoyaltyRewardRateDTO dto2 = new ProductLoyaltyRewardRateDTO();
        dto2.setProductId("134");
        dto2.setProductSku("sku2");
        dtoList.add(dto2);

        ProductLoyaltyRewardRateModel rewardRateModel = new ProductLoyaltyRewardRateModel();
        rewardRateModel.setProductId(12l);
        rewardRateModel.setRewardRate(5d);

        ProductSearchData productSearchData = new ProductSearchData();
        productSearchData.setSku("sku1");
        productSearchData.setId(12l);
        productSearchData.setName("name1");

        ProductSearchData productSearchData1 = new ProductSearchData();
        productSearchData1.setSku("sku2");
        productSearchData1.setId(13l);
        productSearchData1.setName("name2");
        when(productLoyaltyRewardRateExcelFileReader.read(multiplePartFileMock)).thenReturn(dtoList);
        when(productService.search(any())).thenReturn(Arrays.asList(productSearchData)).thenReturn(Arrays.asList(productSearchData1));
        when(productLoyaltyRewardRateService.findAllByCompanyIdAndProductIds(anyLong(), anyList())).thenReturn(Arrays.asList(rewardRateModel));
        ProductLoyaltyRewardRateDTO rewardRateDTOS = facade.importExcelProduct(2l, multiplePartFileMock);
        verify(productService, times(2)).search(any(ProductSearchRequest.class));
        verify(productLoyaltyRewardRateService).findAllByCompanyIdAndProductIds(anyLong(), anyList());
        assertEquals(2, rewardRateDTOS.getRewardRateData().size());
        for (ProductLoyaltyRewardRateDTO rewardRateDTO : rewardRateDTOS.getRewardRateData()) {
            if (rewardRateDTO.getProductSku().equals(dto1.getProductSku())) {
                assertEquals("5.0", rewardRateDTO.getOldRewardRate());
            }
        }
    }
}
