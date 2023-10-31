package com.vctek.orderservice.facade.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.CheckTotalSellingOfProductRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.CheckProductSellingFacade;
import com.vctek.orderservice.service.CheckProductSellingService;
import com.vctek.orderservice.service.ProductService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheckProductSellingFacadeImplTest {

    private ProductService productService;
    private CheckProductSellingService checkProductSellingService;
    private CheckProductSellingFacade facade;
    private CheckTotalSellingOfProductRequest request;

    @Before
    public void setUp() {
        productService = mock(ProductService.class);
        checkProductSellingService = mock(CheckProductSellingService.class);
        facade = new CheckProductSellingFacadeImpl(productService, checkProductSellingService);
        request = new CheckTotalSellingOfProductRequest();
        request.setCompanyId(1l);
        request.setProductIds("2,3,4,5,7");
        request.setFromDate(32);
    }

    @Test
    public void checkTotalSellingOfProduct() {
        try {
            when(productService.checkValid(anyLong(), anyLong())).thenReturn(false);
            facade.checkTotalSellingOfProduct(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_FOUND_DATA.code(), e.getCode());
        }
    }

    @Test
    public void checkTotalSellingOfProduct2() {
        try {
            request.setCompanyId(null);
            facade.checkTotalSellingOfProduct(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.code(), e.getCode());
        }
    }

    @Test
    public void checkTotalSellingOfProduct4() {
        try {
            request.setProductIds(StringUtils.EMPTY);
            facade.checkTotalSellingOfProduct(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_PRODUCT_ID.code(), e.getCode());
        }
    }

    @Test
    public void checkTotalSellingOfProduct3() {
        when(productService.checkValid(anyLong(), anyLong())).thenReturn(true);
        when(checkProductSellingService.countTotalInWholeSaleAndRetail(any(CheckTotalSellingOfProductRequest.class), anyLong())).thenReturn(20l);
        when(checkProductSellingService.countTotalInOnline(any(CheckTotalSellingOfProductRequest.class), anyLong())).thenReturn(20l);
        facade.checkTotalSellingOfProduct(request);
    }


}