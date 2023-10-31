package com.vctek.orderservice.feignclient;


import com.vctek.dto.CheckCreateTransferWarehouseData;
import com.vctek.dto.request.CheckCreateTransferWarehouseRequest;
import com.vctek.dto.request.ValidateTransferLessZeroData;
import com.vctek.health.VersionClient;
import com.vctek.orderservice.dto.DistributorData;
import com.vctek.orderservice.dto.ShippingCompanyData;
import com.vctek.orderservice.dto.WarehouseData;
import com.vctek.orderservice.dto.request.storefront.ShippingFeeData;
import com.vctek.orderservice.dto.request.storefront.ShippingFeeRequest;
import com.vctek.orderservice.feignclient.dto.DistributorSetingPriceData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Component
@FeignClient(name = "${vctek.microservices.logistic:logistic-service}")
public interface LogisticClient extends VersionClient {

    @GetMapping("/warehouses/{warehouseId}/basic")
    WarehouseData getBasicWarehouse(@PathVariable("warehouseId") Long warehouseId,
                                    @RequestParam("companyId") Long companyId);

    @GetMapping("/shipping-company/by-company/{companyId}")
    List<ShippingCompanyData> getShippingCompanyByCompany(@PathVariable("companyId") Long companyId);

    @GetMapping("/distributor-setting-price/{distributorId}")
    Map<Long, DistributorSetingPriceData> getProductPriceSetting(@PathVariable("distributorId") Long distributorId,
                                                                 @RequestParam("companyId") Long companyId,
                                                                 @RequestParam("productIds") List<Long> productIds);

    @GetMapping("/distributors/{distributorId}")
    DistributorData getDetailDistributor(@PathVariable("distributorId") Long distributorId,
                                         @RequestParam("companyId") Long companyId);

    @PostMapping("order-transfer-warehouse-setting/check-create-transfer-warehouse")
    Map<String, CheckCreateTransferWarehouseData> checkValidCreateTransferWarehouse(@RequestBody CheckCreateTransferWarehouseRequest requestValid);

    @PostMapping("transfer-bills/validate-transfer-less-zero")
    void validateTransferLessZero(@RequestBody ValidateTransferLessZeroData request);

    @PostMapping("/storefront/{companyId}/shipping-fees")
    List<ShippingFeeData> getStoreFrontShippingFee(@RequestBody ShippingFeeRequest request,
                                             @PathVariable("companyId") Long companyId);
}
