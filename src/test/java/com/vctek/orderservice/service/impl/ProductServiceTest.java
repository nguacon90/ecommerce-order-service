package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.feignclient.ProductClient;
import com.vctek.orderservice.service.ProductService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class ProductServiceTest {
    @Mock
    private ProductClient productClient;
    private ProductService productService;
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        productService = new ProductServiceImpl(productClient);

    }

    @Test
    public void getProduct() {
        productService.getBasicProductDetail(1l);
        verify(productClient).getBasicProductInfo(1l);
    }

    @Test
    public void getImageDefault() {
        productService.getImageDefault(1l);
        verify(productClient).getImageDefault(1l);
    }

    @Test
    public void productIsAvailableToSell() {
        productService.productIsAvailableToSell(1l);
        verify(productClient).productIsAvailableToSell(1l);
    }

    @Test
    public void getPriceOfProduct() {
        productService.getPriceOfProduct(1l, 2);
        verify(productClient).getPriceOfProduct(1l, 2);
    }

    @Test
    public void checkValid() {
        productService.checkValid(1l, 2l);
        verify(productClient).isValid(1l, 2l, null);
    }

    @Test
    public void checkValid2() {
        productService.checkValid(1l, 2l, 3l);
        verify(productClient).isValid(1l, 2l, 3l);
    }

    @Test
    public void productExistInGroupCombo() {
        productService.productExistInGroupCombo(1l, "2,3");
        verify(productClient).checkProductInGroupCombo(1l, "2,3");
    }

    @Test
    public void getListPriceOfProductIds() {
        productService.getListPriceOfProductIds("1");
        verify(productClient).getListPriceOfProductIds("1");
    }
}
