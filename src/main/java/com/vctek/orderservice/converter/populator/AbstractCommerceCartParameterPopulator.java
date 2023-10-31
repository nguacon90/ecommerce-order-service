package com.vctek.orderservice.converter.populator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.PriceData;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCommerceCartParameterPopulator {
    protected ProductService productService;

    protected void checkAvailableToSellOf(Long productId) {
        Boolean isAvailableToSell = productService.productIsAvailableToSell(productId);
        if (!Boolean.TRUE.equals(isAvailableToSell)) {
            ErrorCodes err = ErrorCodes.PRODUCT_STOP_SELLING;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected double getPriceOf(Long productId, Integer quantity) {
        PriceData priceData = productService.getPriceOfProduct(productId, quantity);

        if (priceData == null || priceData.getPrice() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_PRODUCT_PRICE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{productId});
        }

        return priceData.getPrice().doubleValue();
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

}
