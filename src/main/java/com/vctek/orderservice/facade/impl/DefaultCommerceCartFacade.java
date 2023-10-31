package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.dto.request.storefront.*;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.CommerceCartFacade;
import com.vctek.orderservice.facade.OrderElasticSearchFacade;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.strategy.CommerceChangeOrderStatusStrategy;
import com.vctek.orderservice.strategy.CommercePlaceOrderStrategy;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.service.UserService;
import com.vctek.sync.Mutex;
import com.vctek.util.ComboType;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import com.vctek.util.PriceType;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.cluster.lock.support.DelegatingDistributedLock;
import org.springframework.cloud.cluster.redis.lock.RedisLockService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DefaultCommerceCartFacade extends AbstractOrderFacade implements CommerceCartFacade {
    private ProductSearchService productSearchService;
    private CommerceCartService commerceCartService;
    private Converter<CartModel, MiniCartData> storefrontMiniCartConverter;
    private OrderStorefrontSetupService orderStorefrontSetupService;
    private Converter<AbstractOrderModel, CommerceCartData> commerceCartDataConverter;
    private Converter<StorefrontOrderEntryDTO, CommerceAbstractOrderParameter> storefrontCommerceCartParameterConverter;
    private UserService userService;
    private CartService cartService;
    private Validator<CommerceAbstractOrderParameter> updateCommerceEntryValidator;
    private Validator<StoreFrontSubOrderEntryRequest> changeProductInComboValidator;
    private CouponService couponService;
    private CommercePlaceOrderStrategy commercePlaceOrderStrategy;
    private OrderElasticSearchFacade orderElasticSearchFacade;
    private Converter<OrderSearchModel, CommerceOrderData> storefrontOrderDataConverter;

    private OrderService orderService;

    private RedisLockService redisLockService;
    private CommerceChangeOrderStatusStrategy commerceChangeOrderStatusStrategy;
    private BillService billService;
    private Validator<StoreFrontCheckoutRequest> storefrontCheckoutValidator;
    private CommerceCartShippingFeeService commerceCartShippingFeeService;

    @Override
    public Map<Long, Double> calculateProductPromotionPrice(ProductPromotionRequest request) {
        if (CollectionUtils.isEmpty(request.getProductList())) {
            return new HashMap<>();
        }
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setPageSize(request.getProductList().size());
        List<Long> productIds = request.getProductList().stream().map(ProductPromotion::getProductId).collect(Collectors.toList());
        searchRequest.setProductIds(productIds);
        searchRequest.setCompanyId(request.getCompanyId());

        List<ProductSearchModel> productSearchModels = productSearchService.findAllByCompanyId(searchRequest);
        if (CollectionUtils.isEmpty(productSearchModels)) {
            return new HashMap<>();
        }
        request.setProductSearchModels(productSearchModels);
        return commerceCartService.getDiscountPriceFor(request);
    }

    @Override
    public MiniCartData getMiniCart(Long companyId, String cartCode) {
        CartModel cartModel = commerceCartService.getStorefrontCart(cartCode, companyId);
        if (cartModel == null) {
            return new MiniCartData();
        }

        return storefrontMiniCartConverter.convert(cartModel);
    }

    @Override
    public CommerceCartData getOrCreateNewCart(CreateCartParam param) {
        Long companyId = param.getCompanyId();
        String oldCartGuid = param.getOldCartGuid();
        OrderStorefrontSetupModel setupModel = orderStorefrontSetupService.findByCompanyId(companyId);
        if (setupModel == null || setupModel.getWarehouseId() == null) {
            ErrorCodes err = ErrorCodes.CANNOT_CREATE_STORE_FRONT_CART;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        validateSellSignal(param);
        CartInfoParameter parameter = new CartInfoParameter();
        parameter.setSellSignal(param.getSellSignal());
        parameter.setCompanyId(companyId);
        parameter.setWarehouseId(setupModel.getWarehouseId());
        parameter.setOrderType(OrderType.ONLINE.toString());
        parameter.setPriceType(PriceType.RETAIL_PRICE.toString());
        parameter.setOrderSourceId(setupModel.getOrderSourceId());
        CartModel toCart = commerceCartService.getOrCreateNewStorefrontCart(parameter);

        if (userService.getCurrentUserId() == null) {
            CommerceCartData cartData = commerceCartDataConverter.convert(toCart);
            cartData.setCode(cartData.getGuid());
            return cartData;
        }

        if (StringUtils.isBlank(oldCartGuid)) {
            return commerceCartDataConverter.convert(toCart);
        }

        CartModel fromCart = commerceCartService.getByCompanyIdAndGuid(companyId, oldCartGuid);
        if (fromCart == null || fromCart.getId().equals(toCart.getId())) {
            return commerceCartDataConverter.convert(toCart);
        }

        if (CollectionUtils.isNotEmpty(fromCart.getEntries())) {
            commerceCartService.mergeCarts(fromCart, toCart);
        } else {
            cartService.delete(fromCart);
        }

        return commerceCartDataConverter.convert(toCart);
    }

    private void validateSellSignal(CreateCartParam param) {
        String sellSignal = param.getSellSignal();
        if(StringUtils.isBlank(sellSignal)) {
            ErrorCodes err = ErrorCodes.INVALID_SELL_SIGNAL;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        SellSignal signal = SellSignal.findByName(sellSignal);
        if(signal == null) {
            ErrorCodes err = ErrorCodes.INVALID_SELL_SIGNAL;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    @Transactional
    public CommerceCartData addToCart(StorefrontOrderEntryDTO storefrontOrderEntryDTO) {
        Mutex<String> mutex = mutexFactory.getMutex(storefrontOrderEntryDTO.getOrderCode());
        synchronized (mutex) {
            CommerceAbstractOrderParameter cartParameter = storefrontCommerceCartParameterConverter.convert(storefrontOrderEntryDTO);
            AbstractOrderEntryModel cartEntryModel = commerceCartService.getExistedEntry(cartParameter);
            if (cartEntryModel != null) {
                cartParameter.setQuantity(cartParameter.getQuantity() + cartEntryModel.getQuantity());
                cartParameter.setEntryId(cartEntryModel.getId());
                commerceCartService.updateQuantityForCartEntry(cartParameter);
                CartModel cart = (CartModel) cartParameter.getOrder();
                return commerceCartDataConverter.convert(cart);
            }

            CommerceCartModification commerceCartModification = commerceCartService.addToCart(cartParameter);
            AbstractOrderEntryModel entry = commerceCartModification.getEntry();
            if (StringUtils.isNotBlank(entry.getComboType()) && !ComboType.FIXED_COMBO.toString().equalsIgnoreCase(entry.getComboType())) {
                ComboData comboData = productService.getCombo(entry.getProductId(), storefrontOrderEntryDTO.getCompanyId());
                for (AddSubOrderEntryRequest request : storefrontOrderEntryDTO.getSubOrderEntries()) {
                    request.setCompanyId(storefrontOrderEntryDTO.getCompanyId());
                    ProductInComboData productInComboData = getValidatedProductInEntryCombo(request, comboData, entry);
                    productInComboData.setComboGroupNumber(request.getComboGroupNumber());
                    productInComboData.setQuantity(request.getQuantity());
                    productInComboData.setUpdateQuantity(false);
                    CommerceAbstractOrderEntryParameter parameter = new CommerceAbstractOrderEntryParameter(entry, cartParameter.getOrder());
                    parameter.setProductInComboData(productInComboData);
                    parameter.setComboData(comboData);
                    commerceCartService.addProductToCombo(parameter);
                }
            }
            CartModel cart = (CartModel) cartParameter.getOrder();
            return commerceCartDataConverter.convert(cart);
        }
    }

    @Override
    public CommerceCartData updateCartEntry(StorefrontOrderEntryDTO orderEntryDTO) {
        if (orderEntryDTO.getQuantity() == null) {
            ErrorCodes err = ErrorCodes.INVALID_QUANTITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        Mutex<String> mutex = mutexFactory.getMutex(orderEntryDTO.getOrderCode());
        synchronized (mutex) {
            CartModel cart = commerceCartService.getStorefrontCart(orderEntryDTO.getOrderCode(), orderEntryDTO.getCompanyId());
            validateAbstractOrder(cart);
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(cart);
            parameter.setQuantity(orderEntryDTO.getQuantity());
            parameter.setEntryId(orderEntryDTO.getEntryId());
            updateCommerceEntryValidator.validate(parameter);
            commerceCartService.updateQuantityForCartEntry(parameter);
            CommerceCartData commerceCartData = commerceCartDataConverter.convert(cart);
            validateCartModel(cart, commerceCartData);
            return commerceCartData;
        }
    }

    @Override
    public CommerceCartData getCartDetail(Long companyId, String cartCode) {
        CartModel cartModel = commerceCartService.getStorefrontCart(cartCode, companyId);
        if (cartModel == null) {
            return new CommerceCartData();
        }
        CommerceCartModification commerceCartModification = commerceCartService.updateLatestPriceForEntries(cartModel);
        if(!commerceCartModification.isUpdatePrice()) {
            commerceCartService.recalculate(cartModel, false);
        }
        CommerceCartData commerceCartData = commerceCartDataConverter.convert(cartModel);
        validateCartModel(cartModel, commerceCartData);
        return commerceCartData;
    }

    @Override
    public CommerceCartData changeProductInCombo(StoreFrontSubOrderEntryRequest subOrderEntryRequest) {
        changeProductInComboValidator.validate(subOrderEntryRequest);
        CartModel cartModel = commerceCartService.changeProductInCombo(subOrderEntryRequest);
        CommerceCartData commerceCartData = commerceCartDataConverter.convert(cartModel);
        validateCartModel(cartModel, commerceCartData);
        return commerceCartData;
    }

    @Override
    public CommerceCartData applyCoupon(AppliedCouponRequest appliedCouponRequest) {
        Mutex<String> mutex = mutexFactory.getMutex(appliedCouponRequest.getOrderCode());
        synchronized (mutex) {
            CartModel cart = commerceCartService.getStorefrontCart(appliedCouponRequest.getOrderCode(), appliedCouponRequest.getCompanyId());
            validateAbstractOrder(cart);
            CommerceRedeemCouponParameter parameter = new CommerceRedeemCouponParameter(cart, appliedCouponRequest.getCouponCode());
            parameter.setRedemptionQuantity(appliedCouponRequest.getRedemptionQuantity());
            couponService.redeemCoupon(parameter);
            CommerceCartData commerceCartData = commerceCartDataConverter.convert(cart);
            validateCartModel(cart, commerceCartData);
            return commerceCartData;
        }
    }

    @Override
    public CommerceCartData removeCoupon(AppliedCouponRequest appliedCouponRequest) {
        Mutex<String> mutex = mutexFactory.getMutex(appliedCouponRequest.getOrderCode());
        synchronized (mutex) {
            CartModel cart = commerceCartService.getStorefrontCart(appliedCouponRequest.getOrderCode(), appliedCouponRequest.getCompanyId());
            validateAbstractOrder(cart);
            CommerceRedeemCouponParameter parameter = new CommerceRedeemCouponParameter(cart, appliedCouponRequest.getCouponCode());
            couponService.releaseCoupon(parameter);
            CommerceCartData commerceCartData = commerceCartDataConverter.convert(cart);
            validateCartModel(cart, commerceCartData);
            return commerceCartData;
        }
    }

    @Override
    public CommerceCartData appliedPromotion(String cartCode, Long companyId, Long promotionSourceRuleId) {
        CartModel cartModel = commerceCartService.getStorefrontCart(cartCode, companyId);
        validateAbstractOrder(cartModel);
        cartModel.setAppliedPromotionSourceRuleId(promotionSourceRuleId);
        commerceCartService.recalculate(cartModel, false);
        CommerceCartData commerceCartData = commerceCartDataConverter.convert(cartModel);
        validateCartModel(cartModel, commerceCartData);
        return commerceCartData;
    }

    @Override
    public void cancelOrder(CommerceCancelOrderRequest request) {
        String lockKey = "changeOrderStatus:" + request.getOrderCode();
        DelegatingDistributedLock lock = (DelegatingDistributedLock) redisLockService.obtain(lockKey);
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            ErrorCodes err = ErrorCodes.REJECT_REDUNDANT_REQUEST;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        try {
            OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(request.getOrderCode(), request.getCompanyId(), false);
            validateAbstractOrder(order);
            if(!OrderStatus.CONFIRMED.code().equalsIgnoreCase(order.getOrderStatus())) {
                ErrorCodes err = ErrorCodes.CUSTOMER_CAN_NOT_CANCEL_NOT_CONFIRMED_ORDER;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            OrderStatus newStatus = OrderStatus.CUSTOMER_CANCEL;
            OrderStatus oldStatus = OrderStatus.findByCode(order.getOrderStatus());
            CommerceChangeOrderStatusParameter parameter = new CommerceChangeOrderStatusParameter(order, oldStatus, newStatus);
            parameter.setCancelText(request.getCancelText());
            try {
                commerceChangeOrderStatusStrategy.changeStatusOrder(parameter);
                couponService.revertAllCouponToOrder(order);
            } catch (RuntimeException e) {
                billService.revertOnlineBillWhenError(oldStatus, newStatus, order);
                throw e;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void resetChangeGift(String cartCode, Long companyId) {
        CartModel storefrontCart = commerceCartService.getStorefrontCart(cartCode, companyId);
        if(storefrontCart != null) {
            storefrontCart.setHasChangeGift(false);
            cartService.save(storefrontCart);
        }
    }

    private void validateCartModel(CartModel cartModel, CommerceCartData commerceCartData) {
        CommerceCartValidateParam param = new CommerceCartValidateParam(cartModel);
        CommerceCartValidateData validateData = commerceCartService.validate(param);
        commerceCartData.setHasError(validateData.isHasError());
        Map<Long, CommerceEntryError> entryErrors = validateData.getEntryErrors();
        if(MapUtils.isNotEmpty(entryErrors)) {
            commerceCartData.getEntries().forEach(e -> {
                CommerceEntryError commerceEntryError = entryErrors.get(e.getId());
                e.setCommerceEntryError(commerceEntryError);
            });
        }
    }

    @Override
    public CommerceCartData placeOrder(StoreFrontCheckoutRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getCode());
        synchronized (mutex) {
            OrderStorefrontSetupModel setupModel = orderStorefrontSetupService.findByCompanyId(request.getCompanyId());
            if (setupModel == null || setupModel.getWarehouseId() == null || setupModel.getOrderSourceId() == null) {
                ErrorCodes err = ErrorCodes.CANNOT_CREATE_STORE_FRONT_CART;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            CartModel cart = commerceCartService.getStorefrontCart(request.getCode(), request.getCompanyId());
            validateAbstractOrder(cart);
            validateChangeFinalPriceCart(cart);
            storefrontCheckoutValidator.validate(request);
            CustomerRequest customer = request.getCustomer();
            customer.setCompanyId(cart.getCompanyId());
            CommerceCheckoutParameter checkoutParameter = new CommerceCheckoutParameter();
            checkoutParameter.setCart(cart);
            checkoutParameter.setCreatedByUser(userService.getCurrentUserId());
            checkoutParameter.setCustomerRequest(customer);
            checkoutParameter.setDeliveryCost(request.getDeliveryCost());
            checkoutParameter.setCompanyShippingFee(request.getCompanyShippingFee());
            ShippingFeeData shippingFeeData = populateShippingFee(request, cart);
            checkoutParameter.setShippingCompanyId(shippingFeeData.getShippingCompanyId());
            checkoutParameter.setShippingFeeSettingId(shippingFeeData.getShippingFeeSettingId());
            checkoutParameter.setProductWeight(shippingFeeData.getProductWeight());
            checkoutParameter.setOrderSourceId(setupModel.getOrderSourceId());
            checkoutParameter.setCustomerNote(request.getCustomerNote());
            checkoutParameter.setCustomerSupportNote("Thanh toán COD - " + shippingFeeData.getShippingCompanyName());
            OrderModel orderModel = commercePlaceOrderStrategy.storefrontPlaceOrder(checkoutParameter);
            return commerceCartDataConverter.convert(orderModel);
        }
    }

    private void validateChangeFinalPriceCart(CartModel model) {
        CommerceCartModification commerceCartModification = commerceCartService.updateLatestPriceForEntries(model);
        if(commerceCartModification.isUpdatePrice()) {
            ErrorCodes err = ErrorCodes.ORDER_ENTRY_PRICE_CHANGE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{model.getFinalPrice()});
        }
    }

    private ShippingFeeData populateShippingFee(StoreFrontCheckoutRequest request, AbstractOrderModel model) {
        ShippingFeeData shippingFeeData = commerceCartShippingFeeService.getValidateShippingFee(model, request);
        return shippingFeeData != null ? shippingFeeData : new ShippingFeeData();
    }

    @Override
    public Page<CommerceOrderData> getOrderByUser(OrderSearchRequest request, Pageable pageable) {
        request.setCreatedBy(userService.getCurrentUserId());
        request.setOrderType(OrderType.ONLINE.toString());
        request.setSellSignal(SellSignal.ECOMMERCE_WEB.toString());
        Page<OrderSearchModel> modelPage = orderElasticSearchFacade.orderStorefrontSearch(request, pageable);
        List<CommerceOrderData> orderDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(modelPage.getContent())) {
            orderDataList = storefrontOrderDataConverter.convertAll(modelPage.getContent());
        }
        return new PageImpl<>(orderDataList, modelPage.getPageable(), modelPage.getTotalElements());
    }

    @Override
    public CommerceCartData getDetailOrder(String orderCode, Long companyId) {
        OrderModel order = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, companyId, false);
        validateAbstractOrder(order);
        return commerceCartDataConverter.convert(order);
    }

    @Override
    public List<CountOrderData> countOrderByUser(Long companyId) {
        OrderSearchRequest request = new OrderSearchRequest();
        request.setCompanyId(companyId);
        request.setCreatedBy(userService.getCurrentUserId());
        request.setOrderType(OrderType.ONLINE.toString());
        request.setSellSignal(SellSignal.ECOMMERCE_WEB.toString());
        return orderService.storefrontCountOrderByUser(request);
    }

    @Override
    public CommerceCartData updateAddressShipping(StoreFrontCheckoutRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getCode());
        synchronized (mutex) {
            OrderModel orderModel = orderService.findByCodeAndCompanyId(request.getCode(), request.getCompanyId());
            validateAbstractOrder(orderModel);
            request.getCustomer().setCompanyId(request.getCompanyId());
            ShippingFeeData shippingFeeData = populateShippingFee(request, orderModel);
            OrderModel orderSaved = commercePlaceOrderStrategy.updateAddressShipping(orderModel, shippingFeeData, request);
            return commerceCartDataConverter.convert(orderSaved);
        }
    }

    @Autowired
    public void setProductSearchService(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @Autowired
    public void setCommerceCartService(CommerceCartService commerceCartService) {
        this.commerceCartService = commerceCartService;
    }

    @Autowired
    @Qualifier("storefrontMiniCartConverter")
    public void setStorefrontMiniCartConverter(Converter<CartModel, MiniCartData> storefrontMiniCartConverter) {
        this.storefrontMiniCartConverter = storefrontMiniCartConverter;
    }

    @Autowired
    public void setOrderStorefrontSetupService(OrderStorefrontSetupService orderStorefrontSetupService) {
        this.orderStorefrontSetupService = orderStorefrontSetupService;
    }

    @Autowired
    public void setCommerceCartDataConverter(Converter<AbstractOrderModel, CommerceCartData> commerceCartDataConverter) {
        this.commerceCartDataConverter = commerceCartDataConverter;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    @Autowired
    public void setStorefrontCommerceCartParameterConverter(Converter<StorefrontOrderEntryDTO, CommerceAbstractOrderParameter> storefrontCommerceCartParameterConverter) {
        this.storefrontCommerceCartParameterConverter = storefrontCommerceCartParameterConverter;
    }

    @Autowired
    @Qualifier("updateCommerceEntryValidator")
    public void setUpdateCommerceEntryValidator(Validator<CommerceAbstractOrderParameter> updateCommerceEntryValidator) {
        this.updateCommerceEntryValidator = updateCommerceEntryValidator;
    }

    @Autowired
    @Qualifier("changeProductInComboValidator")
    public void setChangeProductInComboValidator(Validator<StoreFrontSubOrderEntryRequest> changeProductInComboValidator) {
        this.changeProductInComboValidator = changeProductInComboValidator;
    }

    @Autowired
    public void setCommercePlaceOrderStrategy(CommercePlaceOrderStrategy commercePlaceOrderStrategy) {
        this.commercePlaceOrderStrategy = commercePlaceOrderStrategy;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }

    @Autowired
    public void setOrderElasticSearchFacade(OrderElasticSearchFacade orderElasticSearchFacade) {
        this.orderElasticSearchFacade = orderElasticSearchFacade;
    }

    @Autowired
    public void setStorefrontOrderDataConverter(Converter<OrderSearchModel, CommerceOrderData> storefrontOrderDataConverter) {
        this.storefrontOrderDataConverter = storefrontOrderDataConverter;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setRedisLockService(RedisLockService redisLockService) {
        this.redisLockService = redisLockService;
    }

    @Autowired
    public void setCommerceChangeOrderStatusStrategy(CommerceChangeOrderStatusStrategy commerceChangeOrderStatusStrategy) {
        this.commerceChangeOrderStatusStrategy = commerceChangeOrderStatusStrategy;
    }

    @Autowired
    public void setBillService(BillService billService) {
        this.billService = billService;
    }

    @Autowired
    @Qualifier("storefrontCheckoutValidator")
    public void setStorefrontCheckoutValidator(Validator<StoreFrontCheckoutRequest> storefrontCheckoutValidator) {
        this.storefrontCheckoutValidator = storefrontCheckoutValidator;
    }

    @Autowired
    public void setCommerceCartShippingFeeService(CommerceCartShippingFeeService commerceCartShippingFeeService) {
        this.commerceCartShippingFeeService = commerceCartShippingFeeService;
    }
}
