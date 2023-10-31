package com.vctek.orderservice.service;

import com.vctek.dto.VatData;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.feignclient.dto.PriceProductRequest;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.FreeProductRAO;
import com.vctek.redis.ProductData;
import com.vctek.redis.elastic.ProductSearchData;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductService {
    ProductData getBasicProductDetail(Long productId);

    ProductImageData getImageDefault(Long productId);

    Boolean productIsAvailableToSell(Long productId);

    PriceData getPriceOfProduct(Long productId, Integer quantity);

    boolean checkValid(Long productId, Long companyId);

    boolean checkValid(Long productId, Long companyId, Long supplierId);

    ComboData getCombo(Long comboId, Long companyId);

    List<ProductInComboData> getProductInCombo(Long comboId, Long companyId, String product);

    ProductIsCombo checkIsCombo(Long productId, Long companyId, Integer quantity);

    boolean productExistInGroupCombo(Long comboId, String productIds);

    List<ProductSearchData> search(ProductSearchRequest searchRequest);

    List<CategoryData> findAllProductCategories(Long productId);

    boolean isFnB(Long productId);

    void checkAvailableToSale(ProductIsCombo productIsCombo, AbstractOrderModel abstractOrderModel);

    Integer getComboAvailableStock(Long comboId, Long companyId);

    List<PriceData> getListPriceOfProductIds(String productIds);

    Map<Long, VatData> getVATOf(Set<Long> productIds);

    List<CategoryData> getCategoryByIdIn(Set<Long> categoryIds, Long companyId);

    boolean isOnsite(Long productId, Long companyId);

    Map<Long, Double> getPriceOfProductList(PriceProductRequest request);

    void validateProductInCombo(Long comboId, Long companyId, String product);

    boolean isValidFreeProduct(FreeProductRAO freeProductRAO);
}
