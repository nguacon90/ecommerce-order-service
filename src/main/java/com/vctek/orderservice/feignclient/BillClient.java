package com.vctek.orderservice.feignclient;


import com.vctek.kafka.data.ReturnOrderBillDTO;
import com.vctek.orderservice.dto.WarehouseData;
import com.vctek.orderservice.dto.request.ComboOrToppingOrderRequest;
import com.vctek.orderservice.dto.request.LinkReturnOrderforbillRequest;
import com.vctek.orderservice.feignclient.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Component
@FeignClient(name = "${vctek.microservices.logistic:logistic-service}")
public interface BillClient {

    @PostMapping("/return-bills/orders")
    Long createReturnBillWithOrder(@RequestBody BillRequest billRequest);

    @PostMapping("/return-bills/orders/revert")
    ResponseEntity revertReturnBillWithOrder(@RequestBody OrderBillRequest orderBillRequest);

    @PutMapping("/return-bills/orders/update-product")
    ResponseEntity updateProductInReturnBillWithOrder(@RequestBody OrderBillRequest orderBillRequest);

    @PutMapping("/return-bills/orders/update-discount-price")
    ResponseEntity updateDiscountPriceInReturnBillWithOrder(@RequestBody BillRequest orderBillRequest);

    @PostMapping("/return-bills/orders/delete-product")
    ResponseEntity deleteProductInReturnBillWithOrder(@RequestBody OrderBillRequest orderBillRequest);

    @PostMapping("/return-bills/orders/add-products")
    ResponseEntity addProductToReturnBillWithOrder(@RequestBody AddProductBillRequest addProductBillRequest);

    @PutMapping("/return-bills/orders/update-combo")
    ResponseEntity updateComBoInReturnBillWithOrder(@RequestBody ComboOrToppingOrderRequest comboOrToppingOrderRequest);

    @PostMapping("/return-bills/orders/delete-combo")
    ResponseEntity deleteComboInReturnBillWithOrder(@RequestBody ComboOrToppingOrderRequest comboOrToppingOrderRequest);

    @PostMapping("/return-bills/orders/create-update-delete-topping")
    ResponseEntity updateOrDeleteToppingInReturnBillWithOrder(@RequestBody ComboOrToppingOrderRequest comboOrToppingOrderRequest);

    @GetMapping("/warehouses/{warehouseId}")
    WarehouseData getWarehouse(@PathVariable("warehouseId") Long warehouseId,
                               @RequestParam("companyId") Long companyId);

    @PostMapping("/return-bills/online-orders")
    Long createReturnBillWithOrderOnline(@RequestBody BillRequest orderBillRequest);

    @PostMapping("/bills/{orderCode}/change-to-order-return")
    ResponseEntity changeOrderStatusToOrderReturn(@PathVariable("orderCode") String orderCode,
                                                  @RequestParam("billId") Long billId,
                                                  @RequestParam("companyId") Long companyId);

    @PostMapping("/bills/{billId}/add-stock/{statusCode}")
    ResponseEntity addStockOfInventoryStatusBy(@PathVariable("billId") Long billId,
                                               @PathVariable("statusCode") String statusCode,
                                               @RequestParam("companyId") Long companyId);

    @PostMapping("/bills/{billId}/subtract-stock/{statusCode}")
    ResponseEntity subtractStockOfInventoryStatusBy(@PathVariable("billId") Long billId,
                                                    @PathVariable("statusCode") String statusCode,
                                                    @RequestParam("companyId") Long companyId);

    @PostMapping("/receipt-bills/orders")
    Long createReceiptBillForOrder(@RequestBody BillRequest billRequest);

    @GetMapping("/bills/{billId}/return-order/{returnOrderId}")
    ReturnOrderBillData getReturnOrderBill(@PathVariable("billId") Long billId, @PathVariable("returnOrderId") Long returnOrderId,
                                           @RequestParam("companyId") Long companyId);

    @GetMapping("/bills/orders/{orderCode}")
    Long getBillOfOrder(@PathVariable("orderCode") String orderCode, @RequestParam("companyId") Long companyId,
                        @RequestParam("type") String type, @RequestParam("status") String status);

    @GetMapping("/bills/{billId}/return-order/{returnOrderId}")
    ReturnOrderBillDTO getBillWithReturnOrder(@PathVariable("billId") Long billId, @PathVariable("returnOrderId") Long returnOrderId,
                                              @RequestParam("companyId") Long companyId);

    @PutMapping("/bills/{billId}/return-order/{returnOrderId}/origin-order")
    ResponseEntity updateOriginOrderCode(@PathVariable("billId") Long billId,
                                         @PathVariable("returnOrderId") Long returnOrderId,
                                         @RequestBody UpdateReturnOrderBillRequest updateReturnOrderBillRequest);

    @GetMapping("/warehouses/by-company/{companyId}")
    List<WarehouseData> getWarehouseByCompany(@PathVariable("companyId") Long companyId);

    @PostMapping("/migration-data/link/return-order")
    Long linkReturnOrderforbill(@RequestBody LinkReturnOrderforbillRequest request);

    @PostMapping("/bills/orders/cancel-online-order")
    void cancelOnlineOrder(@RequestBody OrderBillRequest cancelOnlineOrderReq);

    @PostMapping("/return-bills/online-orders/revert")
    void revertReturnBillWithOnlineOrder(OrderBillRequest revertOrderBillReq);
}
