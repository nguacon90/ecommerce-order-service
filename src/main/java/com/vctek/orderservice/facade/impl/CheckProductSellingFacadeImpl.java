package com.vctek.orderservice.facade.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.CheckTotalSellingOfProductRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.CheckProductSellingFacade;
import com.vctek.orderservice.service.CheckProductSellingService;
import com.vctek.orderservice.service.ProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CheckProductSellingFacadeImpl implements CheckProductSellingFacade {

    private ProductService productService;
    private CheckProductSellingService checkProductSellingService;

    public CheckProductSellingFacadeImpl(ProductService productService, CheckProductSellingService checkProductSellingService) {
        this.productService = productService;
        this.checkProductSellingService = checkProductSellingService;
    }

    @Override
    public Map<Long, Long> checkTotalSellingOfProduct(CheckTotalSellingOfProductRequest request) {
        if (request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if (StringUtils.isBlank(request.getProductIds())) {
            ErrorCodes err = ErrorCodes.EMPTY_PRODUCT_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        List<Long> productIds = Arrays.asList(StringUtils.split(request.getProductIds(), ",")).stream().map(i -> Long.valueOf(i)).collect(Collectors.toList());
        Map<Long, Long> results = new HashMap<>();
        for (Long productId : productIds) {
            boolean isValid = productService.checkValid(productId, request.getCompanyId());
            if (!isValid) {
                ErrorCodes err = ErrorCodes.NOT_FOUND_DATA;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            Long totalWholesaleAndRetail = checkProductSellingService.countTotalInWholeSaleAndRetail(request, productId);
            if (totalWholesaleAndRetail == null) {
                totalWholesaleAndRetail = 0l;
            }
            Long totalOnline = checkProductSellingService.countTotalInOnline(request, productId);
            if (totalOnline == null) {
                totalOnline = 0l;
            }

            results.put(productId, totalOnline + totalWholesaleAndRetail);

        }
        return results;
    }
}
