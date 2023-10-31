package com.vctek.orderservice.feignclient;

import com.vctek.dto.RestPageImpl;
import com.vctek.dto.VatData;
import com.vctek.health.VersionClient;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.feignclient.dto.ComboRequest;
import com.vctek.orderservice.feignclient.dto.PriceProductRequest;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.feignclient.dto.ProductVatRequest;
import com.vctek.redis.ProductData;
import com.vctek.redis.elastic.ProductSearchData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Component
@FeignClient(name = "${vctek.microservices.product:qtn-product-service}")
public interface ProductClient extends VersionClient {

    @GetMapping("/products/{productId}/basic-info")
    ProductData getBasicProductInfo(@PathVariable("productId") Long productId);

    @GetMapping("/products/{productId}/price")
    PriceData getPriceOfProduct(@PathVariable("productId") Long productId, @RequestParam("quantity") Integer quantity);

    @GetMapping("/products/{productId}/is-available-to-sell")
    Boolean productIsAvailableToSell(@PathVariable("productId") Long productId);

    @GetMapping("/products/{productId}/categories")
    List<CategoryData> findAllProductCategories(@PathVariable("productId") Long productId);

    @GetMapping("/images/product/{productId}/default")
    ProductImageData getImageDefault(@PathVariable("productId") Long productId);

    @GetMapping("/products/checkValid")
    Boolean isValid(@RequestParam("productId") Long productId,
                    @RequestParam("companyId") Long companyId,
                    @RequestParam(value = "supplier", required = false) Long supplierId);

    @GetMapping("/combos/{comboId}")
    ComboData getComboDetail(@PathVariable("comboId") Long comboId,
                             @RequestParam("companyId") Long companyId);

    @GetMapping("/products/combo/{comboId}/search")
    List<ProductInComboData> getProductInCombo(@PathVariable("comboId") Long comboId,
                                               @RequestParam("companyId") Long companyId,
                                               @RequestParam("product") String product,
                                               @RequestParam("option") String option);

    @GetMapping("/products/{productId}/is-combo")
    ProductIsCombo checkIsCombo(@PathVariable("productId") Long productId, @RequestParam("companyId") Long companyId,
                                @RequestParam("quantity") Integer quantity);

    @GetMapping("/combos/validate-group-combo/{comboId}")
    Boolean checkProductInGroupCombo(@PathVariable("comboId") Long comboId, @RequestParam("productIds") String productIds);

    @GetMapping("/products")
    RestPageImpl<ProductSearchData> search(@RequestParam("companyId") Long companyId,
                                           @RequestParam(value = "sku", required = false) String sku,
                                           @RequestParam(value = "ids", required = false) String ids,
                                           @RequestParam("pageSize") int pageSize,
                                           @RequestParam(value = "allowReward", required = false) Boolean allowReward);

    @GetMapping("/combos/available-to-sale/{comboId}")
    Void checkAvailableToSale(@PathVariable("comboId") Long comboId);

    @GetMapping("/combos/{comboId}/available-stock")
    Integer getAvailableStock(@PathVariable("comboId") Long comboId, @RequestParam("companyId") Long companyId);

    @PostMapping("/combos/update-sale-quantity")
    void updateSaleQuantity(List<ComboRequest> comboRequests);

    @GetMapping("/products/prices")
    List<PriceData> getListPriceOfProductIds(@RequestParam("productIds") String productIds);

    @PostMapping("/products/vat")
    Map<Long, VatData> findAllVat(@RequestBody ProductVatRequest productVatRequest);

    @GetMapping("/categories/find-by-ids")
    List<CategoryData> findAllCategoryByIdIn(@RequestParam("companyId") Long companyId,
                                     @RequestParam("ids") List<Long> categoryIds);

    @GetMapping("/storefront/{companyId}/products/{productId}/on-site")
    Boolean isOnsite(@PathVariable("companyId") Long companyId,
                     @PathVariable("productId") Long productId);

    @PostMapping("/products/get-prices")
    Map<Long, Double> getPriceOfProductList(@RequestBody PriceProductRequest request);
}
