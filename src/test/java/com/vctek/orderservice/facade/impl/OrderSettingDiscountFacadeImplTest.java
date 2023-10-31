package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CategoryData;
import com.vctek.orderservice.dto.OrderDiscountSettingMapper;
import com.vctek.orderservice.dto.OrderSettingData;
import com.vctek.orderservice.dto.OrderSettingDiscountData;
import com.vctek.orderservice.dto.excel.OrderSettingDiscountDTO;
import com.vctek.orderservice.dto.excel.OrderSettingDiscountErrorDTO;
import com.vctek.orderservice.dto.request.OrderSettingRequest;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.excel.OrderSettingDiscountExcelFileReader;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.OrderSettingDiscountModel;
import com.vctek.orderservice.model.OrderSettingModel;
import com.vctek.orderservice.service.OrderSettingDiscountService;
import com.vctek.orderservice.service.OrderSettingService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.orderservice.util.DiscountType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class OrderSettingDiscountFacadeImplTest {
    private OrderSettingDiscountFacadeImpl facade;

    @Mock
    private OrderSettingDiscountService service;
    @Mock
    private Converter<OrderSettingDiscountModel, OrderSettingDiscountData> converter;
    @Mock
    private ProductSearchService productSearchService;
    @Mock
    private ProductService productService;
    @Mock
    private OrderSettingService orderSettingService;
    @Mock
    private MultipartFile multiplePartFileMock;
    @Mock
    private OrderSettingDiscountExcelFileReader orderSettingDiscountExcelFileReader;
    @Mock
    private Converter<OrderSettingModel, OrderSettingData> orderSettingConverter;

    private ArgumentCaptor<OrderSettingModel> captor;

    private OrderSettingRequest request = new OrderSettingRequest();
    private OrderSettingDiscountData requestProduct;
    private OrderSettingDiscountData requestCategory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        captor = ArgumentCaptor.forClass(OrderSettingModel.class);
        facade = new OrderSettingDiscountFacadeImpl();
        facade.setService(service);
        facade.setOrderSettingDiscountConverter(converter);
        facade.setProductSearchService(productSearchService);
        facade.setOrderSettingDiscountExcelFileReader(orderSettingDiscountExcelFileReader);
        facade.setProductService(productService);
        facade.setOrderSettingService(orderSettingService);
        facade.setOrderSettingConverter(orderSettingConverter);

        requestProduct = new OrderSettingDiscountData();
        requestProduct.setCompanyId(2l);
        requestProduct.setProductId(12l);
        requestProduct.setDiscount(10d);
        requestProduct.setDiscountType(DiscountType.PERCENT.name());

        requestCategory = new OrderSettingDiscountData();
        requestCategory.setCompanyId(2l);
        requestCategory.setCategoryCode("code");
        requestCategory.setDiscount(10000d);
        requestCategory.setDiscountType(DiscountType.CASH.name());
    }

    @Test
    public void createProduct() {
        List<OrderSettingDiscountData> discountData = new ArrayList<>();
        discountData.add(requestProduct);
        request.setSettingDiscountData(discountData);
        when(orderSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderSettingModel());
        when(service.findAllByCompanyIdAndProductIdAndDeleted(anyLong(), anyList())).thenReturn(new ArrayList<>());
        facade.createProduct(request);
        verify(orderSettingService, times(1)).save(captor.capture());
        OrderSettingModel saveModel = captor.getValue();
        assertEquals(1, saveModel.getSettingDiscountModel().size());
    }

    @Test
    public void createCategory() {
        List<OrderSettingDiscountData> discountData = new ArrayList<>();
        discountData.add(requestCategory);
        request.setSettingDiscountData(discountData);
        when(orderSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderSettingModel());
        when(service.findAllCatgorySetting(anyLong())).thenReturn(new ArrayList<>());
        facade.createOrUpdateCategory(request);
        verify(orderSettingService, times(1)).save(captor.capture());
        OrderSettingModel saveModel = captor.getValue();
        assertEquals(1, saveModel.getSettingDiscountModel().size());
    }

    @Test
    public void deleteProductSetting_orderSetting_null() {
        try {
            when(orderSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(null);
            facade.deleteProductSetting(2l, 2l);
            fail("must be exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_SETTING.code(), e.getCode());
        }
    }

    @Test
    public void deleteProductSetting_null() {
        try {
            when(orderSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderSettingModel());
            when(service.findOneByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
            facade.deleteProductSetting(2l, 2l);
            fail("must be exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_SETTING_MAXIMUM_DISCOUNT_PRODUCT.code(), e.getCode());
        }
    }

    @Test
    public void deleteProductSetting() {
        when(orderSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderSettingModel());
        when(service.findOneByIdAndCompanyId(anyLong(), anyLong())).thenReturn(new OrderSettingDiscountModel());
        facade.deleteProductSetting(2l, 2l);
        verify(service).save(any(OrderSettingDiscountModel.class));
    }

    @Test
    public void searchProductSetting() {
        when(service.findAllProductSetting(anyLong(), anyString(), any())).thenReturn(new PageImpl<>(Arrays.asList(new OrderSettingDiscountModel())));
        facade.search(2l, "a", PageRequest.of(0, 20));
        verify(service).findAllProductSetting(anyLong(), anyString(), any(PageRequest.class));
    }


    @Test
    public void exportDiscountSetting() {
        List<OrderDiscountSettingMapper> mapperList = new ArrayList<>();
        OrderDiscountSettingMapper mapper = new OrderDiscountSettingMapper();
        mapper.setName("name");
        mapper.setSku("sku");
        mapper.setDiscount(10d);
        mapperList.add(mapper);
        when(service.findAllByProductId(any(), any())).thenReturn(mapperList).thenReturn(new ArrayList<>());
        byte[] bytes = facade.exportExcel(1l);
        assertNotNull(bytes);
    }

    @Test
    public void importexcel_invalid_orderSetting() {
        try {
            when(orderSettingDiscountExcelFileReader.read(multiplePartFileMock)).thenReturn(new ArrayList<>());
            facade.importExcel(2l, multiplePartFileMock);
            fail("must be exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_ORDER_SETTING.code(), e.getCode());
        }
    }

    @Test
    public void importexcel_empty_import_product_setting_discount() {
        try {
            when(orderSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderSettingModel());
            when(orderSettingDiscountExcelFileReader.read(multiplePartFileMock)).thenReturn(new ArrayList<>());
            facade.importExcel(2l, multiplePartFileMock);
            fail("must be exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_IMPORT_PRODUCT_SETTING_DISCOUNT.code(), e.getCode());
        }
    }

    @Test
    public void importExcel_has_error() {
        List<OrderSettingDiscountDTO> dtoList = new ArrayList<>();
        OrderSettingDiscountDTO dto1 = new OrderSettingDiscountDTO();
        dto1.setProductSku("sku1");
        dto1.setRowExcel(1);
        dto1.setDiscountType(CurrencyType.CASH.toString());
        dto1.setDiscount("20d");
        dtoList.add(dto1);
        OrderSettingDiscountDTO dto2 = new OrderSettingDiscountDTO();
        dto2.setDiscount("20d");
        dto2.setRowExcel(2);
        dtoList.add(dto2);
        OrderSettingDiscountDTO dto3 = new OrderSettingDiscountDTO();
        dto3.setRowExcel(3);
        dto3.setProductSku("sku1");
        dto3.setDiscountType(CurrencyType.CASH.code());
        dtoList.add(dto3);
        OrderSettingDiscountDTO dto4 = new OrderSettingDiscountDTO();
        dto4.setRowExcel(4);
        dto4.setProductSku("sku1");
        dto4.setDiscount("-20");
        dto4.setDiscountType(CurrencyType.CASH.code());
        dtoList.add(dto4);
        OrderSettingDiscountDTO dto5 = new OrderSettingDiscountDTO();
        dto5.setRowExcel(5);
        dto5.setProductSku("sku2");
        dto5.setDiscount("20");
        dto5.setDiscountType(CurrencyType.CASH.code());
        dtoList.add(dto5);
        when(orderSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderSettingModel());
        when(orderSettingDiscountExcelFileReader.read(multiplePartFileMock)).thenReturn(dtoList);
        ProductSearchModel searchData = new ProductSearchModel();
        searchData.setSku("sku1");
        searchData.setId(1l);
        when(productSearchService.findAllByCompanyId(any(ProductSearchRequest.class))).thenReturn(Arrays.asList(searchData));
        OrderSettingDiscountErrorDTO dto = facade.importExcel(2l, multiplePartFileMock);
        assertEquals(8, dto.getErrors().size());
        verify(orderSettingService).findByTypeAndCompanyId(anyString(), anyLong());
    }

    @Test
    public void importExcel_success() {
        List<OrderSettingDiscountDTO> dtoList = new ArrayList<>();
        OrderSettingDiscountDTO dto1 = new OrderSettingDiscountDTO();
        dto1.setProductSku("sku1");
        dto1.setRowExcel(2);
        dto1.setDiscountType(CurrencyType.CASH.code());
        dto1.setDiscount("20");
        dtoList.add(dto1);
        when(orderSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderSettingModel());
        when(orderSettingDiscountExcelFileReader.read(multiplePartFileMock)).thenReturn(dtoList);
        ProductSearchModel searchData = new ProductSearchModel();
        searchData.setSku("sku1");
        searchData.setId(1l);
        when(productSearchService.findAllByCompanyId(any(ProductSearchRequest.class))).thenReturn(Arrays.asList(searchData));
        OrderSettingDiscountErrorDTO dto = facade.importExcel(2l, multiplePartFileMock);
        assertEquals(0, dto.getErrors().size());
        verify(orderSettingService).findByTypeAndCompanyId(anyString(), anyLong());
    }

    @Test
    public void findAllCategory() {
        when(orderSettingService.findByTypeAndCompanyId(anyString(), anyLong())).thenReturn(new OrderSettingModel());
        facade.findAllCategory(2l);
        verify(orderSettingService).findByTypeAndCompanyId(anyString(), anyLong());
        verify(orderSettingConverter).convert(any(OrderSettingModel.class));
    }

    @Test
    public void getDiscountProduct_notFindByCategory() {
        OrderSettingDiscountModel model1 = new OrderSettingDiscountModel();
        model1.setCompanyId(2l);
        model1.setProductId(1l);
        OrderSettingDiscountModel model2 = new OrderSettingDiscountModel();
        model2.setCompanyId(2l);
        model2.setProductId(2l);

        OrderSettingDiscountData data1 = new OrderSettingDiscountData();
        data1.setCompanyId(2l);
        data1.setProductId(1l);
        OrderSettingDiscountData data2 = new OrderSettingDiscountData();
        data2.setCompanyId(2l);
        data2.setProductId(2l);

        when(service.findAllByCompanyIdAndProductIdAndDeleted(anyLong(), anyList())).thenReturn(Arrays.asList(model1, model2));
        when(converter.convertAll(anyCollection())).thenReturn(Arrays.asList(data1, data2));
        Map<Long, OrderSettingDiscountData> mapProducts = facade.getDiscountProduct(2l, Arrays.asList(1l, 2l));
        verify(service, times(1)).findAllByCompanyIdAndProductIdAndDeleted(anyLong(), anyList());
        verify(service, times(0)).findAllByCompanyIdAndCategoryCodeAndDeleted(anyLong(), anyList());
        verify(converter, times(1)).convertAll(anyCollection());
        assertEquals(2, mapProducts.size());
    }

    @Test
    public void getDiscountProduct_andFindByCategory() {
        OrderSettingDiscountModel model1 = new OrderSettingDiscountModel();
        model1.setCompanyId(2l);
        model1.setProductId(1l);
        OrderSettingDiscountModel model2 = new OrderSettingDiscountModel();
        model2.setCompanyId(2l);
        model2.setProductId(2l);
        OrderSettingDiscountModel model3 = new OrderSettingDiscountModel();
        model3.setCompanyId(2l);
        model3.setProductId(3l);
        model3.setCategoryCode("code");

        OrderSettingDiscountData data1 = new OrderSettingDiscountData();
        data1.setCompanyId(2l);
        data1.setProductId(1l);
        OrderSettingDiscountData data2 = new OrderSettingDiscountData();
        data2.setCompanyId(2l);
        data2.setProductId(2l);
        OrderSettingDiscountData data3 = new OrderSettingDiscountData();
        data3.setCompanyId(2l);
        data3.setProductId(3l);
        data3.setCategoryCode("code");

        CategoryData categoryData1 = new CategoryData();
        categoryData1.setCode("code");
        categoryData1.setId(3l);

        CategoryData categoryData2 = new CategoryData();
        categoryData2.setCode("code4");
        categoryData2.setId(4l);

        when(service.findAllByCompanyIdAndProductIdAndDeleted(anyLong(), anyList())).thenReturn(Arrays.asList(model1, model2));
        when(service.findAllByCompanyIdAndCategoryCodeAndDeleted(anyLong(), anyList())).thenReturn(Arrays.asList(model3));
        when(productService.findAllProductCategories(anyLong())).thenReturn(Arrays.asList(categoryData1, categoryData2)).thenReturn(Arrays.asList(categoryData2));
        when(converter.convertAll(anyCollection())).thenReturn(Arrays.asList(data1, data2));
        Map<Long, OrderSettingDiscountData> mapProducts = facade.getDiscountProduct(2l, Arrays.asList(1l, 2l, 3l, 4l));
        verify(service, times(1)).findAllByCompanyIdAndProductIdAndDeleted(anyLong(), anyList());
        verify(service, times(2)).findAllByCompanyIdAndCategoryCodeAndDeleted(anyLong(), anyList());
        verify(converter, times(1)).convertAll(anyCollection());
        verify(converter, times(1)).convert(any(OrderSettingDiscountModel.class));
        verify(productService, times(2)).findAllProductCategories(anyLong());
        assertEquals(3, mapProducts.size());
    }
}
