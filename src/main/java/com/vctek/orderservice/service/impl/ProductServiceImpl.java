package com.vctek.orderservice.service.impl;

import com.vctek.dto.RestPageImpl;
import com.vctek.dto.VatData;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.ProductClient;
import com.vctek.orderservice.feignclient.dto.*;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.FreeProductRAO;
import com.vctek.orderservice.service.InventoryService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.redis.ProductData;
import com.vctek.redis.elastic.ProductSearchData;
import com.vctek.util.CommonUtils;
import com.vctek.util.ProductType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private InventoryService inventoryService;
    private ProductClient productClient;

    @Autowired
    public ProductServiceImpl(ProductClient productClient) {
        this.productClient = productClient;
    }

    @Override
    @Cacheable(unless = "#result == null", value = "redis_product_detail", key = "#productId", cacheManager = "microServiceCacheManager")
    public ProductData getBasicProductDetail(Long productId) {
        return productClient.getBasicProductInfo(productId);
    }

    @Override
    @Cacheable(unless = "#result == null", value = "productImageDefault", key = "#productId", cacheManager = "microServiceCacheManager")
    public ProductImageData getImageDefault(Long productId) {
        return productClient.getImageDefault(productId);
    }

    @Override
    public Boolean productIsAvailableToSell(Long productId) {
        return productClient.productIsAvailableToSell(productId);
    }

    @Override
    public PriceData getPriceOfProduct(Long productId, Integer quantity) {
        return productClient.getPriceOfProduct(productId, quantity);
    }

    @Override
    public boolean checkValid(Long productId, Long companyId) {
        return checkValid(productId, companyId, null);
    }

    @Override
    public boolean checkValid(Long productId, Long companyId, Long supplierId) {
        return productClient.isValid(productId, companyId, supplierId);
    }

    @Override
    @Cacheable(unless = "#result == null", value = "comboData", key = "#comboId + '_' + #companyId", cacheManager = "microServiceCacheManager")
    public ComboData getCombo(Long comboId, Long companyId) {
        return productClient.getComboDetail(comboId, companyId);
    }

    @Override
    public List<ProductInComboData> getProductInCombo(Long comboId, Long companyId, String product) {
        return productClient.getProductInCombo(comboId, companyId, product, StringUtils.EMPTY);
    }

    @Override
    public ProductIsCombo checkIsCombo(Long productId, Long companyId, Integer quantity) {
        return productClient.checkIsCombo(productId, companyId, quantity);
    }

    @Override
    public boolean productExistInGroupCombo(Long comboId, String productIds) {
        return productClient.checkProductInGroupCombo(comboId, productIds);
    }

    @Override
    public List<ProductSearchData> search(ProductSearchRequest searchRequest) {
        RestPageImpl<ProductSearchData> searchData = productClient.search(searchRequest.getCompanyId(), searchRequest.getSku(),
                searchRequest.getIds(), searchRequest.getPageSize(), searchRequest.isAllowReward());
        return searchData.getContent();
    }

    @Override
    @Cacheable(unless = "#result == null", value = "product_categories", key = "#productId", cacheManager = "microServiceCacheManager")
    public List<CategoryData> findAllProductCategories(Long productId) {
        return productClient.findAllProductCategories(productId);
    }

    @Override
    @Cacheable(unless = "#result == null", value = "fnb_product", key = "#productId", cacheManager = "microServiceCacheManager")
    public boolean isFnB(Long productId) {
        ProductData basicProductDetail = productClient.getBasicProductInfo(productId);
        if (basicProductDetail == null) {
            return false;
        }
        if (ProductType.FOOD.code().equals(basicProductDetail.getProductType()) ||
                ProductType.BEVERAGE.code().equals(basicProductDetail.getProductType())) {
            return true;
        }
        return false;
    }

    @Override
    public void checkAvailableToSale(ProductIsCombo productIsCombo, AbstractOrderModel abstractOrderModel) {
        if (!SellSignal.ECOMMERCE_WEB.toString().equals(abstractOrderModel.getSellSignal())) {
            productClient.checkAvailableToSale(productIsCombo.getId());
            return;
        }
        ProductStockData productStock = inventoryService.getStoreFrontStockOfProduct(productIsCombo.getId(), productIsCombo.getCompanyId());
        if (CommonUtils.readValue(productStock.getQuantity()) <= 0) {
            ErrorCodes err = ErrorCodes.PRODUCT_OUT_OF_STOCK;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public Integer getComboAvailableStock(Long comboId, Long companyId) {
        return productClient.getAvailableStock(comboId, companyId);
    }

    @Override
    public List<PriceData> getListPriceOfProductIds(String productIds) {
        return productClient.getListPriceOfProductIds(productIds);
    }

    @Override
    public Map<Long, VatData> getVATOf(Set<Long> productIds) {
        ProductVatRequest productVatRequest = new ProductVatRequest();
        productVatRequest.setProductIds(productIds.stream().collect(Collectors.toList()));
        return productClient.findAllVat(productVatRequest);
    }

    @Override
    public List<CategoryData> getCategoryByIdIn(Set<Long> categoryIds, Long companyId) {
        return productClient.findAllCategoryByIdIn(companyId, categoryIds.stream().collect(Collectors.toList()));
    }

    @Override
    public boolean isOnsite(Long productId, Long companyId) {
        Boolean onsite = productClient.isOnsite(companyId, productId);
        return Boolean.TRUE.equals(onsite);
    }

    @Override
    public Map<Long, Double> getPriceOfProductList(PriceProductRequest request) {
        return productClient.getPriceOfProductList(request);
    }

    @Override
    public void validateProductInCombo(Long comboId, Long companyId, String product) {
        List<ProductInComboData> productInCombo = this.getProductInCombo(comboId, companyId, product);
        if(CollectionUtils.isEmpty(productInCombo)) {
            ErrorCodes err = ErrorCodes.NOT_EXISTED_PRODUCT_IN_COMBO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public boolean isValidFreeProduct(FreeProductRAO freeProductRAO) {
        freeProductRAO.getAddedOrderEntry().getProduct();
        return false;
    }

    @Autowired
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
}
