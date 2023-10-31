package com.vctek.orderservice.controller;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderFacade;
import com.vctek.orderservice.facade.OrderHistoryFacade;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/qtn/orders")
public class OrderController {
    private Validator<OrderRequest> orderRequestValidator;
    private Validator<OrderRequest> orderUpdateValidator;
    private Validator<OrderEntryDTO> orderEntryValidator;

    private OrderFacade orderFacade;
    private PermissionFacade permissionFacade;
    private OrderHistoryFacade orderHistoryFacade;
    private Validator<AppliedCouponRequest> appliedCouponRequestValidator;
    private Validator<AddSubOrderEntryRequest> addSubOrderEntryRequestValidator;
    private Validator<RemoveSubOrderEntryRequest> removeSubOrderEntryRequestValidator;
    private Validator<ToppingItemRequest> toppingItemRequestValidator;
    private Validator<ToppingOptionRequest> toppingOptionRequestValidator;
    private Validator<RefreshCartRequest> refreshOrderRequestValidator;
    private Validator<AddTagRequest> addTagValidator;

    public OrderController(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @PostMapping
    public ResponseEntity<OrderData> placeOrder(@RequestBody OrderRequest orderRequest) {
        permissionFacade.checkPlaceOrder(orderRequest.getCompanyId(), orderRequest.getCode());
        orderRequestValidator.validate(orderRequest);
        OrderData orderData = orderFacade.placeOrder(orderRequest);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}")
    public ResponseEntity<OrderData> updateOrder(@RequestBody OrderRequest orderRequest,
                                                 @PathVariable("orderCode") String orderCode) {
        permissionFacade.checkUpdateOrderInfo(orderRequest.getCompanyId(), orderCode);
        orderRequest.setCode(orderCode);
        orderUpdateValidator.validate(orderRequest);
        OrderData orderData = orderFacade.updateOrderInfo(orderRequest);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/info")
    public ResponseEntity<OrderData> updateOrderInfo(@RequestBody OrderRequest orderRequest,
                                                     @PathVariable("orderCode") String orderCode) {
        permissionFacade.checkUpdateOrderInfo(orderRequest.getCompanyId(), orderCode);
        orderRequest.setCode(orderCode);
        orderUpdateValidator.validate(orderRequest);
        OrderData orderData = orderFacade.updateInfoOnlineOrder(orderRequest);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/order-source")
    public ResponseEntity<OrderData> changeOrderSource(@PathVariable("orderCode") String orderCode,
                                                      @RequestBody CartInfoParameter cartInfoParameter) {
        permissionFacade.checkUpdateOrderInfo(cartInfoParameter.getCompanyId(), orderCode);
        cartInfoParameter.setCode(orderCode);
        OrderData orderData = orderFacade.changeOrderSource(cartInfoParameter);
        return new ResponseEntity(orderData, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/refresh")
    public ResponseEntity<OrderData> refreshCart(@PathVariable("orderCode") String orderCode,
                                                 @RequestBody RefreshCartRequest refreshCartRequest) {
        refreshCartRequest.setCode(orderCode);
        refreshOrderRequestValidator.validate(refreshCartRequest);
        OrderData orderData = orderFacade.refresh(refreshCartRequest);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderData> getDetail(@PathVariable("orderCode") String orderCode,
                                               @RequestParam("companyId") Long companyId,
                                               @RequestParam("orderType") String orderType,
                                               @RequestParam(value = "isExchange", defaultValue = "false") boolean isExchange) {
        permissionFacade.checkViewOrderDetail(companyId, orderCode);

        OrderData data = orderFacade.findBy(orderCode, companyId, orderType, isExchange);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/entries")
    public ResponseEntity<CartData> addOrderEntry(@PathVariable("orderCode") String orderCode,
                                                  @RequestBody OrderEntryDTO orderEntryDTO) {
        permissionFacade.checkUpdateOrder(orderEntryDTO.getCompanyId(), orderCode);

        orderEntryDTO.setOrderCode(orderCode);
        orderEntryValidator.validate(orderEntryDTO);
        OrderData data = orderFacade.addEntryToOrder(orderEntryDTO);
        return new ResponseEntity(data, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/entries/{entryId}/delete")
    public ResponseEntity<OrderData> removeOrderEntry(@PathVariable("orderCode") String orderCode,
                                                      @PathVariable("entryId") Long entryId,
                                                      @RequestParam("companyId") Long companyId,
                                                      @RequestParam(value = "time", required = false) Long timeRequest) {
        permissionFacade.checkUpdateOrder(companyId, orderCode);

        OrderEntryDTO orderEntryDTO = new OrderEntryDTO();
        orderEntryDTO.setEntryId(entryId);
        orderEntryDTO.setOrderCode(orderCode);
        orderEntryDTO.setQuantity(0L);
        orderEntryDTO.setCompanyId(companyId);
        orderEntryDTO.setTimeRequest(timeRequest);
        OrderData data = orderFacade.updateEntry(orderEntryDTO);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/entries/{entryId}")
    public ResponseEntity<OrderData> updateQuantityForOrderEntry(@PathVariable("orderCode") String orderCode,
                                                                 @PathVariable("entryId") Long entryId,
                                                                 @RequestBody OrderEntryDTO orderEntryDTO,
                                                                 @RequestParam(value = "time", required = false) Long timeRequest) {
        permissionFacade.checkUpdateOrder(orderEntryDTO.getCompanyId(), orderCode);

        orderEntryDTO.setOrderCode(orderCode);
        orderEntryDTO.setEntryId(entryId);
        orderEntryDTO.setTimeRequest(timeRequest);
        OrderData data = orderFacade.updateEntry(orderEntryDTO);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/entries/{entryId}/price")
    public ResponseEntity<OrderData> updatePriceForOrderEntry(@PathVariable("orderCode") String cartCode,
                                                              @PathVariable("entryId") Long entryId,
                                                              @RequestBody OrderEntryDTO orderEntryDTO,
                                                              @RequestParam(value = "time", required = false) Long timeRequest) {
        permissionFacade.checkUpdateOrderPrice(orderEntryDTO, cartCode);

        orderEntryDTO.setOrderCode(cartCode);
        orderEntryDTO.setEntryId(entryId);
        orderEntryDTO.setTimeRequest(timeRequest);
        OrderData data = orderFacade.updatePriceOrderEntry(orderEntryDTO);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/entries/{entryId}/recommended-retail-price")
    public ResponseEntity<OrderData> updateRecommendedRetailPriceForCartEntry(@PathVariable("orderCode") String orderCode,
                                                                              @PathVariable("entryId") Long entryId,
                                                                              @RequestBody OrderEntryDTO orderEntryDTO,
                                                                              @RequestParam(value = "time", required = false) Long timeRequest) {
        permissionFacade.checkUpdateRecommendedRetailPriceOrder(orderEntryDTO, orderCode);

        orderEntryDTO.setOrderCode(orderCode);
        orderEntryDTO.setEntryId(entryId);
        orderEntryDTO.setTimeRequest(timeRequest);
        OrderData data = orderFacade.updateRecommendedRetailPriceForOrderEntry(orderEntryDTO);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/entries/{entryId}/discount")
    public ResponseEntity<OrderData> updateDiscountForOrderEntry(@PathVariable("orderCode") String orderCode,
                                                                 @PathVariable("entryId") Long entryId,
                                                                 @RequestBody OrderEntryDTO orderEntryDTO,
                                                                 @RequestParam(value = "time", required = false) Long timeRequest) {
        orderEntryDTO.setOrderCode(orderCode);
        orderEntryDTO.setEntryId(entryId);
        orderEntryDTO.setTimeRequest(timeRequest);
        permissionFacade.checkUpdateOrderDiscount(orderEntryDTO.getCompanyId(), orderCode);
        OrderData data = orderFacade.updateDiscountOfEntry(orderEntryDTO);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/entries/{entryId}/weight")
    public ResponseEntity<CartData> updateWeightForCartEntry(@PathVariable("orderCode") String orderCode,
                                                             @PathVariable("entryId") Long entryId,
                                                             @RequestBody OrderEntryDTO orderEntryDTO) {
        orderEntryDTO.setOrderCode(orderCode);
        orderEntryDTO.setEntryId(entryId);
        orderFacade.updateWeightForOrderEntry(orderEntryDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/discount")
    public ResponseEntity<OrderData> updateDiscountForOrder(@PathVariable("orderCode") String orderCode,
                                                            @RequestBody CartDiscountRequest cartDiscountRequest,
                                                            @RequestParam(value = "time", required = false) Long timeRequest) {
        permissionFacade.checkUpdateOrderDiscount(cartDiscountRequest.getCompanyId(), orderCode);

        cartDiscountRequest.setCode(orderCode);
        cartDiscountRequest.setTimeRequest(timeRequest);
        OrderData data = orderFacade.updateDiscountOfOrder(cartDiscountRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/vat")
    public ResponseEntity<OrderData> updateVatForOrder(@PathVariable("orderCode") String orderCode,
                                                       @RequestBody VatRequest vatRequest,
                                                       @RequestParam(value = "time", required = false) Long timeRequest) {
        permissionFacade.checkUpdateOrder(vatRequest.getCompanyId(), orderCode);

        vatRequest.setCode(orderCode);
        vatRequest.setTimeRequest(timeRequest);
        OrderData data = orderFacade.updateVatOfOrder(vatRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/apply-coupon")
    public ResponseEntity<OrderData> applyCoupon(@PathVariable("orderCode") String orderCode,
                                                 @RequestBody AppliedCouponRequest appliedCouponRequest) {
        permissionFacade.checkUpdateOrder(appliedCouponRequest.getCompanyId(), orderCode);
        appliedCouponRequest.setOrderCode(orderCode);
        appliedCouponRequestValidator.validate(appliedCouponRequest);
        OrderData data = orderFacade.applyCoupon(appliedCouponRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/remove-coupon")
    public ResponseEntity<OrderData> removeCoupon(@PathVariable("orderCode") String orderCode,
                                                  @RequestBody AppliedCouponRequest appliedCouponRequest) {
        permissionFacade.checkUpdateOrder(appliedCouponRequest.getCompanyId(), orderCode);
        appliedCouponRequest.setOrderCode(orderCode);
        appliedCouponRequestValidator.validate(appliedCouponRequest);
        OrderData data = orderFacade.removeCoupon(appliedCouponRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/change-status")
    @PreAuthorize("hasAnyPermission(#request.companyId, " +
            "T(com.vctek.util.PermissionCodes).UPDATE_STATUS_ORDER.code())")
    public ResponseEntity<Void> changeStatusOrder(@PathVariable("orderCode") String orderCode,
                                                  @RequestBody ChangeOrderStatusRequest request) {
        request.setOrderCode(orderCode);
        orderFacade.changeStatusOrder(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{orderCode}/change-to-retail")
    public ResponseEntity<String> changeToRetail(@PathVariable String orderCode,
                                                 @RequestParam Long companyId) {
        String newOrderCode = orderFacade.changeOrderToRetail(orderCode, companyId);
        return new ResponseEntity<>(newOrderCode, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/holding-product")
    public ResponseEntity<Void> holdingProduct(@RequestBody HoldingProductRequest request,
                                               @PathVariable("orderCode") String orderCode) {
        if (request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        request.setOrderCode(orderCode);
        orderFacade.holdingProductOfOrder(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{orderCode}/entries/{entryId}/holding-product")
    public ResponseEntity<Void> updateHoldingProductOfEntry(@RequestBody HoldingData holdingData,
                                                            @PathVariable("orderCode") String orderCode,
                                                            @PathVariable("entryId") Long entryId) {
        if (holdingData.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        orderFacade.updateHoldingProductBy(orderCode, entryId, holdingData);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{orderCode}/get-status-history")
    public ResponseEntity<EmployeeChangeData> getOrderStatusHistory(@PathVariable String orderCode,
                                                                    @RequestParam("companyId") Long companyId) {
        EmployeeChangeData data = orderHistoryFacade.getStatusHistory(orderCode, companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/delete")
    public ResponseEntity<Void> remove(@RequestParam("companyId") Long companyId,
                                       @PathVariable("orderCode") String orderCode) {
        orderFacade.remove(orderCode, companyId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/update-note")
    public ResponseEntity<Void> updateNoteInOrder(@RequestBody NoteRequest noteRequest,
                                                  @PathVariable("orderCode") String orderCode) {
        noteRequest.setOrderCode(orderCode);
        orderFacade.updateNoteInOrder(noteRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/entries/{entryId}/sub-entries")
    public ResponseEntity<OrderData> addProductToCombo(@PathVariable("orderCode") String orderCode,
                                                            @PathVariable("entryId") Long entryId,
                                                            @RequestBody AddSubOrderEntryRequest request) {
        permissionFacade.checkUpdateOrder(request.getCompanyId(), orderCode);
        request.setOrderCode(orderCode);
        request.setEntryId(entryId);
        addSubOrderEntryRequestValidator.validate(request);
        OrderData orderData = orderFacade.addProductToCombo(request);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/entries/{entryId}/change-to-combo")
    public ResponseEntity<OrderData> addComboToOrderIndirectly(@PathVariable("orderCode") String orderCode,
                                                               @PathVariable("entryId") Long entryId,
                                                               @RequestBody AddSubOrderEntryRequest request) {
        permissionFacade.checkUpdateOrder(request.getCompanyId(), orderCode);
        request.setOrderCode(orderCode);
        request.setEntryId(entryId);
        addSubOrderEntryRequestValidator.validate(request);
        OrderData data = orderFacade.addComboToOrderIndirectly(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/entries/{entryId}/remove-sub-entries")
    public ResponseEntity removeSubEntry(@PathVariable("orderCode") String orderCode,
                                         @PathVariable("entryId") Long entryId,
                                         @RequestBody RemoveSubOrderEntryRequest request) {
        permissionFacade.checkUpdateOrder(request.getCompanyId(), orderCode);
        request.setOrderCode(orderCode);
        request.setEntryId(entryId);
        removeSubOrderEntryRequestValidator.validate(request);
        orderFacade.removeSubEntry(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{orderCode}/entries/import")
    public <T extends AbstractOrderData> ResponseEntity<T> importOrderItems(@PathVariable("orderCode") String orderCode,
                                                                            @RequestParam("companyId") Long companyId,
                                                                            @RequestParam("file") MultipartFile multipartFile) {
        T data = orderFacade.importOrderItem(orderCode, companyId, multipartFile);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/sale-quantity")
    @PreAuthorize("hasAnyPermission(#request.companyId, " +
            "T(com.vctek.util.PermissionCodes).CREATE_RECONCILIATION.code(), " +
            "T(com.vctek.util.PermissionCodes).VIEW_DETAIL_RECONCILIATION.code(), " +
            "T(com.vctek.util.PermissionCodes).VERIFY_RECONCILIATION.code(), " +
            "T(com.vctek.util.PermissionCodes).UPDATE_RECONCILIATION.code())")
    public ResponseEntity<Map<Long, OrderSaleData>> getSaleQuantity(SaleQuantityRequest request) {
        Map<Long, OrderSaleData> data = orderFacade.getSaleQuantity(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/applied-promotion/{promotionSourceRuleId}")
    public ResponseEntity<OrderData> addComboToOrderIndirectly(@PathVariable("orderCode") String orderCode,
                                                               @RequestParam("companyId") Long companyId,
                                                               @PathVariable("promotionSourceRuleId") Long promotionSourceRuleId) {
        permissionFacade.checkUpdateOrder(companyId, orderCode);
        OrderData data = orderFacade.appliedPromotion(orderCode, companyId, promotionSourceRuleId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/entries/{entryId}/topping-options/{optionId}")
    public ResponseEntity<OrderData> updateToppingOption(@PathVariable("orderCode") String orderCode,
                                                         @PathVariable("entryId") Long entryId,
                                                         @PathVariable("optionId") Long optionId,
                                                         @RequestBody ToppingOptionRequest request,
                                                         @RequestParam(value = "time", required = false) Long timeRequest) {
        permissionFacade.checkUpdateOrder(request.getCompanyId(), orderCode);
        request.setId(optionId);
        request.setEntryId(entryId);
        request.setTimeRequest(timeRequest);
        toppingOptionRequestValidator.validate(request);
        OrderData orderData = orderFacade.updateToppingOption(request, orderCode);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/entries/{entryId}/topping-options")
    public ResponseEntity<OrderData> addToppingOptionsToOrder(@PathVariable("orderCode") String orderCode,
                                                              @PathVariable("entryId") Long entryId,
                                                              @RequestBody ToppingOptionRequest request) {
        permissionFacade.checkUpdateOrder(request.getCompanyId(), orderCode);
        request.setEntryId(entryId);
        toppingOptionRequestValidator.validate(request);
        OrderData orderData = orderFacade.addToppingOptionsToOrder(request, orderCode);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/entries/{entryId}/topping-options/{optionId}/items")
    public ResponseEntity<OrderData> addToppingItems(@PathVariable("orderCode") String orderCode,
                                                     @PathVariable("entryId") Long entryId,
                                                     @PathVariable("optionId") Long optionId,
                                                     @RequestBody ToppingItemRequest request) {
        permissionFacade.checkUpdateOrder(request.getCompanyId(), orderCode);
        request.setToppingOptionId(optionId);
        request.setEntryId(entryId);
        request.setOrderCode(orderCode);
        toppingItemRequestValidator.validate(request);
        OrderData orderData = orderFacade.addToppingItems(orderCode, request);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/entries/{entryId}/topping-options/{optionId}/delete")
    public ResponseEntity<OrderData> removeToppingOptions(@PathVariable("orderCode") String orderCode,
                                                          @PathVariable("entryId") Long entryId,
                                                          @PathVariable("optionId") Long optionId,
                                                          @RequestParam("companyId") Long companyId) {
        permissionFacade.checkUpdateOrder(companyId, orderCode);
        OrderData orderData = orderFacade.removeToppingOptions(orderCode, entryId, optionId, companyId);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/entries/{entryId}/topping-options/{optionId}/items/{itemId}/delete")
    public ResponseEntity<OrderData> removeToppingItems(@PathVariable("orderCode") String orderCode,
                                                        @PathVariable("entryId") Long entryId,
                                                        @PathVariable("optionId") Long optionId,
                                                        @PathVariable("itemId") Long itemId,
                                                        @RequestParam("companyId") Long companyId) {
        permissionFacade.checkUpdateOrder(companyId, orderCode);
        OrderData orderData = orderFacade.removeToppingItems(orderCode, entryId, optionId, itemId, companyId);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/entries/{entryId}/topping-options/{optionId}/items/{itemId}")
    public ResponseEntity<OrderData> updateQuantityToppingItems(@PathVariable("orderCode") String orderCode,
                                                                @PathVariable("entryId") Long entryId,
                                                                @PathVariable("optionId") Long optionId,
                                                                @PathVariable("itemId") Long itemId,
                                                                @RequestBody ToppingItemRequest request,
                                                                @RequestParam(value = "time", required = false) Long timeRequest) {
        request.setOrderCode(orderCode);
        request.setEntryId(entryId);
        request.setToppingOptionId(optionId);
        request.setId(itemId);
        request.setTimeRequest(timeRequest);
        permissionFacade.checkUpdateOrder(request.getCompanyId(), orderCode);
        OrderData orderData = orderFacade.updateQuantityToppingItems(request, orderCode);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/entries/{entryId}/topping-options/{optionId}/items/{itemId}/discount")
    public ResponseEntity<OrderData> updateDiscountForToppingItem(@PathVariable("orderCode") String orderCode,
                                                                  @PathVariable("optionId") Long optionId,
                                                                  @PathVariable("entryId") Long entryId,
                                                                  @PathVariable("itemId") Long itemId,
                                                                  @RequestBody ToppingItemRequest request,
                                                                  @RequestParam(value = "time", required = false) Long timeRequest) {

        permissionFacade.checkUpdateOrder(request.getCompanyId(), orderCode);
        request.setId(itemId);
        request.setToppingOptionId(optionId);
        request.setOrderCode(orderCode);
        request.setEntryId(entryId);
        request.setTimeRequest(timeRequest);
        toppingItemRequestValidator.validate(request);
        OrderData data = orderFacade.updateDiscountForToppingItem(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/create-update-invoices")
    @PreAuthorize("hasAnyPermission(#invoiceOrderRequest.companyId, " +
            "T(com.vctek.util.PermissionCodes).EDIT_ALL_INVOICE.code())")
    public ResponseEntity createOrUpdateInvoices(@RequestBody InvoiceOrderRequest invoiceOrderRequest) {
        orderFacade.createOrUpdateInvoices(invoiceOrderRequest);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/update-paid-amount-all-order")
    public ResponseEntity updatePaidAmountAllOrder(@RequestBody OrderPartialIndexRequest request) {
        orderFacade.updatePaidAmountAllOrder(request);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/update-combo-report")
    public ResponseEntity updateComboReport(@RequestParam("companyId") Long companyId) {
        orderFacade.updateComboReport(companyId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @PostMapping("/{orderCode}/entries/delete-list")
    public ResponseEntity<OrderData> removeListEntry(@PathVariable("orderCode") String orderCode,
                                                     @RequestBody EntryRequest request) {
        request.setOrderCode(orderCode);
        permissionFacade.checkUpdateOrder(request.getCompanyId(), orderCode);
        OrderData data = orderFacade.removeListEntry(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/entries/price")
    @PreAuthorize("hasAnyPermission(#orderRequest.companyId, T(com.vctek.util.PermissionCodes).CAN_EDIT_TYPE_PRICE_ONLINE.code())")
    public ResponseEntity<OrderData> updatePriceForCartEntries(@PathVariable("orderCode") String orderCode,
                                                               @RequestBody OrderRequest orderRequest) {
        permissionFacade.checkUpdateOrder(orderRequest.getCompanyId(), orderCode);
        orderRequest.setCode(orderCode);
        OrderData data = orderFacade.updatePriceForOrderEntries(orderRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/shipping-fee")
    @PreAuthorize("hasAnyPermission(#orderRequest.companyId, T(com.vctek.util.PermissionCodes).UPDATE_ORDER.code())")
    public ResponseEntity<OrderData> updateShippingFee(@PathVariable("orderCode") String orderCode,
                                                       @RequestBody OrderRequest orderRequest) {
        orderRequest.setCode(orderCode);
        OrderData data = orderFacade.updateShippingFee(orderRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/default-setting-customer")
    @PreAuthorize("hasAnyPermission(#orderRequest.companyId, T(com.vctek.util.PermissionCodes).UPDATE_ORDER.code())")
    public ResponseEntity<OrderData> updateDefaultSettingCustomer(@PathVariable("orderCode") String orderCode,
                                                                  @RequestBody OrderRequest orderRequest) {
        orderRequest.setCode(orderCode);
        OrderData data = orderFacade.updateDefaultSettingCustomer(orderRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/check-discount-maximum")
    public ResponseEntity<List<OrderSettingDiscountData>> checkDiscountMaximum(@PathVariable("orderCode") String orderCode,
                                                                               @RequestParam("companyId") Long companyId) {
        List<OrderSettingDiscountData> data = orderFacade.checkDiscountMaximum(companyId, orderCode);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/update-all-discount")
    public ResponseEntity<OrderData> updateAllDiscount(@PathVariable("orderCode") String orderCode,
                                                       @RequestBody UpdateAllDiscountRequest updateAllDiscountRequest) {
        OrderData data = orderFacade.updateAllDiscountForOrder(orderCode, updateAllDiscountRequest);
        return new ResponseEntity(data, HttpStatus.OK);
    }

    @GetMapping("/find-by-externalId")
    public ResponseEntity<OrderData> findOrderByExternalId(CartInfoParameter cartInfoParameter) {
        OrderData data = orderFacade.findOrderByExternalId(cartInfoParameter);
        return new ResponseEntity(data, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/upload-images")
    public ResponseEntity uploadImage(@PathVariable("orderCode") String orderCode,
                                      @RequestBody OrderImagesRequest request,
                                      @RequestParam("companyId") Long companyId) {

        request.setCompanyId(companyId);
        orderFacade.uploadImageToOrder(request, orderCode);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{orderCode}/cancel-redeem")
    public ResponseEntity<OrderData> cancelRedeem(@PathVariable String orderCode,
                                                  @RequestParam Long companyId) {
        OrderData orderData = orderFacade.cancelRedeem(orderCode, companyId);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/create-redeem")
    public ResponseEntity<Double> createRedeemOnline(@PathVariable String orderCode,
                                                     @RequestBody PaymentTransactionRequest request,
                                                     @RequestParam Long companyId) {
        double redeemPoint = orderFacade.createRedeemOnline(orderCode, companyId, request);
        return new ResponseEntity<>(redeemPoint, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/update-redeem")
    public ResponseEntity<Double> updateRedeemOnline(@PathVariable String orderCode,
                                                     @RequestBody PaymentTransactionRequest request,
                                                     @RequestParam Long companyId) {
        double redeemPoint = orderFacade.updateRedeemOnline(orderCode, companyId, request);
        return new ResponseEntity<>(redeemPoint, HttpStatus.OK);
    }

    @GetMapping("/{orderCode}/loyalty-points")
    public ResponseEntity<AwardLoyaltyData> getLoyaltyPoints(@PathVariable("orderCode") String orderCode,
                                                             @RequestParam("companyId") Long companyId) {
        AwardLoyaltyData loyaltyPoints = orderFacade.getLoyaltyPointsFor(orderCode, companyId);
        return new ResponseEntity<>(loyaltyPoints, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/setting-customer")
    @PreAuthorize("hasAnyPermission(#orderRequest.companyId, T(com.vctek.util.PermissionCodes).UPDATE_ORDER.code())")
    public ResponseEntity<OrderData> updateSettingCustomerToOrder(@PathVariable("orderCode") String orderCode,
                                                                  @RequestBody OrderRequest orderRequest) {
        orderRequest.setCode(orderCode);
        OrderData data = orderFacade.updateSettingCustomerToOrder(orderRequest);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/tags")
    public ResponseEntity addTag(@PathVariable("orderCode") String orderCode, @RequestBody AddTagRequest addTagRequest) {
        addTagRequest.setOrderCode(orderCode);
        addTagValidator.validate(addTagRequest);
        orderFacade.addTag(addTagRequest);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{orderCode}/tags/{tagId}/delete")
    public ResponseEntity removeTag(@PathVariable("orderCode") String orderCode,
                                    @PathVariable("tagId") Long tagId,
                                    @RequestParam("companyId") Long companyId) {
        orderFacade.removeTag(companyId, orderCode, tagId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{cartCode}/entries/{entryId}/sale-off")
    public ResponseEntity<OrderData> markEntrySaleOff(@PathVariable("cartCode") String cartCode,
                                                      @PathVariable("entryId") Long entryId,
                                                      @RequestBody EntrySaleOffRequest request) {
        request.setOrderCode(cartCode);
        request.setEntryId(entryId);
        OrderData data = orderFacade.markEntrySaleOff(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}/update-customer")
    public ResponseEntity<OrderData> updateCustomer(@PathVariable("orderCode") String orderCode,
                                                    @RequestBody UpdateCustomerRequest request) {
        permissionFacade.checkUpdateOrderInfo(request.getCompanyId(), orderCode);
        request.setCode(orderCode);
        OrderData orderData = orderFacade.updateCustomer(request);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PostMapping("/{orderCode}/add-vat")
    public ResponseEntity<OrderData> addVAT(@PathVariable("orderCode") String orderCode,
                                           @RequestParam("companyId") Long companyId,
                                           @RequestParam("addVat") Boolean addVat) {
        OrderData orderData = orderFacade.addVAT(companyId, orderCode, addVat);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @Autowired
    @Qualifier("orderEntryValidator")
    public void setOrderEntryValidator(Validator<OrderEntryDTO> orderEntryValidator) {
        this.orderEntryValidator = orderEntryValidator;
    }

    @Autowired
    @Qualifier("orderUpdateValidator")
    public void setOrderRequestUpdateValidator(Validator<OrderRequest> orderUpdateValidator) {
        this.orderUpdateValidator = orderUpdateValidator;
    }

    @Autowired
    @Qualifier("orderRequestValidator")
    public void setOrderRequestValidator(Validator<OrderRequest> orderRequestValidator) {
        this.orderRequestValidator = orderRequestValidator;
    }

    @Autowired
    public void setPermissionFacade(PermissionFacade permissionFacade) {
        this.permissionFacade = permissionFacade;
    }

    @Autowired
    public void setOrderHistoryFacade(OrderHistoryFacade orderHistoryFacade) {
        this.orderHistoryFacade = orderHistoryFacade;
    }

    @Autowired
    public void setAppliedCouponRequestValidator(Validator<AppliedCouponRequest> appliedCouponRequestValidator) {
        this.appliedCouponRequestValidator = appliedCouponRequestValidator;
    }

    @Autowired
    public void setAddSubOrderEntryRequestValidator(Validator<AddSubOrderEntryRequest> addSubOrderEntryRequestValidator) {
        this.addSubOrderEntryRequestValidator = addSubOrderEntryRequestValidator;
    }

    @Autowired
    public void setRemoveSubOrderEntryRequestValidator(Validator<RemoveSubOrderEntryRequest> removeSubOrderEntryRequestValidator) {
        this.removeSubOrderEntryRequestValidator = removeSubOrderEntryRequestValidator;
    }

    @Autowired
    public void setToppingItemRequestValidator(Validator<ToppingItemRequest> toppingItemRequestValidator) {
        this.toppingItemRequestValidator = toppingItemRequestValidator;
    }

    @Autowired
    public void setToppingOptionRequestValidator(Validator<ToppingOptionRequest> toppingOptionRequestValidator) {
        this.toppingOptionRequestValidator = toppingOptionRequestValidator;
    }

    @Autowired
    public void setRefreshOrderRequestValidator(Validator<RefreshCartRequest> refreshOrderRequestValidator) {
        this.refreshOrderRequestValidator = refreshOrderRequestValidator;
    }

    @Autowired
    public void setAddTagValidator(Validator<AddTagRequest> addTagValidator) {
        this.addTagValidator = addTagValidator;
    }
}
