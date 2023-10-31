package com.vctek.orderservice.feignclient;


import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.feignclient.dto.UpdateInventoryStatusRequest;
import com.vctek.orderservice.feignclient.dto.UpdateProductInventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Component
@FeignClient(name = "${vctek.microservices.logistic:logistic-service}")
public interface InventoryClient {

    @GetMapping(value = "/inventories/{productId}/available-stock")
    ProductStockData getAvailableStock(@PathVariable("productId") Long productId,
                                       @RequestParam("companyId") Long companyId,
                                       @RequestParam(value = "warehouseId", required = false) Long warehouseId);

    @GetMapping(value = "/inventories/{productId}/broken-stock")
    ProductStockData getBrokenStock(@PathVariable("productId") Long productId,
                                       @RequestParam("companyId") Long companyId,
                                       @RequestParam("warehouseId") Long warehouseId);

    @PostMapping("/inventories/change-stock-by-status")
    ResponseEntity changeInventoryByStatus(@RequestBody UpdateProductInventoryRequest request);

    @PostMapping("/inventories/{statusCode}/add-stock")
    ResponseEntity addStockWithInventoryStatus(@PathVariable("statusCode") String statusCode, @RequestBody UpdateInventoryStatusRequest request);

    @PostMapping("/inventories/{statusCode}/subtract-stock")
    ResponseEntity subtractStockWithInventoryStatus(@PathVariable("statusCode") String statusCode, @RequestBody UpdateInventoryStatusRequest request);

    @GetMapping("/storefront/{companyId}/inventory")
    Map<Long, Integer> getStoreFrontStockOfProduct(@PathVariable("companyId") Long companyId,
                                                              @RequestParam("productIds") List<Long> productIds);
}
