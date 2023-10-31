package com.vctek.orderservice.controller.storefront;

import com.vctek.dto.redis.OrderStorefrontSetupData;
import com.vctek.orderservice.dto.CommerceCartData;
import com.vctek.orderservice.dto.request.CommerceCancelOrderRequest;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.dto.request.storefront.CommerceOrderData;
import com.vctek.orderservice.dto.request.storefront.CountOrderData;
import com.vctek.orderservice.dto.request.storefront.ProductPromotionRequest;
import com.vctek.orderservice.dto.request.storefront.StoreFrontCheckoutRequest;
import com.vctek.orderservice.facade.CommerceCartFacade;
import com.vctek.orderservice.facade.OrderStorefrontSetupFacade;
import com.vctek.util.OrderType;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/storefront")
public class StoreFrontOrderController {
    private CommerceCartFacade commerceCartFacade;
    private OrderStorefrontSetupFacade orderStorefrontSetupFacade;
    private Validator<StoreFrontCheckoutRequest> storefrontShippingAddressValidator;

    public StoreFrontOrderController(CommerceCartFacade commerceCartFacade) {
        this.commerceCartFacade = commerceCartFacade;
    }

    @PostMapping("{companyId}/products/promotion-prices")
    public ResponseEntity<Map<Long, Double>> doAppliedPromotions(@RequestBody ProductPromotionRequest request,
                                                                 @PathVariable("companyId") Long companyId) {
        request.setCompanyId(companyId);
        Map<Long, Double> data = commerceCartFacade.calculateProductPromotionPrice(request);
        return new ResponseEntity(data, HttpStatus.OK);
    }

    @GetMapping("{companyId}/setup-order")
    public ResponseEntity<OrderStorefrontSetupData> getOrderStorefrontSetup(@PathVariable("companyId") Long companyId) {
        OrderStorefrontSetupData data = orderStorefrontSetupFacade.findByCompanyId(companyId);
        return new ResponseEntity(data, HttpStatus.OK);
    }

    @PostMapping("{companyId}/checkout")
    public ResponseEntity<CommerceCartData> placeOrder(@RequestBody StoreFrontCheckoutRequest request,
                                                       @PathVariable("companyId") Long companyId) {
        request.setCompanyId(companyId);
        request.setOrderType(OrderType.ONLINE.toString());
        CommerceCartData orderData = commerceCartFacade.placeOrder(request);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @GetMapping("{companyId}/orders")
    public ResponseEntity<Page<CommerceOrderData>> getOrderByUser(OrderSearchRequest request,
                                                                  @PathVariable("companyId") Long companyId,
                                                                  @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                                                  @RequestParam(value = "page", defaultValue = "0") int page) {
        request.setCompanyId(companyId);
        Pageable pageable = PageRequest.of(page, pageSize, new Sort(Sort.Direction.DESC, "createdTime"));
        Page<CommerceOrderData> orderData = commerceCartFacade.getOrderByUser(request, pageable);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PostMapping("/{companyId}/orders/{orderCode}/cancel")
    public ResponseEntity cancelOrder(@PathVariable("companyId") Long companyId,
                                      @PathVariable("orderCode") String orderCode,
                                      @RequestBody CommerceCancelOrderRequest request) {
        request.setCompanyId(companyId);
        request.setOrderCode(orderCode);
        commerceCartFacade.cancelOrder(request);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{companyId}/orders/{orderCode}")
    public ResponseEntity<CommerceCartData> getDetailOrder(@PathVariable("companyId") Long companyId,
                                      @PathVariable("orderCode") String orderCode) {
        CommerceCartData data = commerceCartFacade.getDetailOrder(orderCode, companyId);
        return new ResponseEntity(data, HttpStatus.OK);
    }

    @GetMapping("{companyId}/orders/count")
    public ResponseEntity<List<CountOrderData>> countOrderByUser(@PathVariable("companyId") Long companyId) {
        List<CountOrderData> orderData = commerceCartFacade.countOrderByUser(companyId);
        return new ResponseEntity<>(orderData, HttpStatus.OK);
    }

    @PutMapping("/{companyId}/orders/{orderCode}/update-address-shipping")
    public ResponseEntity<CommerceCartData> updateAddressShipping(@RequestBody StoreFrontCheckoutRequest request,
                                                                  @PathVariable("companyId") Long companyId,
                                                                  @PathVariable("orderCode") String orderCode) {
        request.setCompanyId(companyId);
        request.setCode(orderCode);
        storefrontShippingAddressValidator.validate(request);
        CommerceCartData data = commerceCartFacade.updateAddressShipping(request);
        return new ResponseEntity(data, HttpStatus.OK);
    }

    @Autowired
    public void setOrderStorefrontSetupFacade(OrderStorefrontSetupFacade orderStorefrontSetupFacade) {
        this.orderStorefrontSetupFacade = orderStorefrontSetupFacade;
    }

    @Autowired
    @Qualifier("storefrontShippingAddressValidator")
    public void setStorefrontShippingAddressValidator(Validator<StoreFrontCheckoutRequest> storefrontShippingAddressValidator) {
        this.storefrontShippingAddressValidator = storefrontShippingAddressValidator;
    }
}
