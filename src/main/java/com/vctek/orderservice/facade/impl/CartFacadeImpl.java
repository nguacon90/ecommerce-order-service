package com.vctek.orderservice.facade.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.excel.OrderItemDTO;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.CartFacade;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.util.CurrencyType;
import com.vctek.sync.Mutex;
import com.vctek.util.ComboType;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartFacadeImpl extends AbstractOrderFacade implements CartFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(CartFacadeImpl.class);
    private CartService cartService;
    private AuthService authService;
    private Converter<CartModel, CartData> cartConverter;
    private CommerceCartService commerceCartService;
    private Converter<OrderEntryDTO, CommerceAbstractOrderParameter> commerceCartParameterConverter;
    private CouponService couponService;
    private Converter<AddSubOrderEntryRequest, CommerceAbstractOrderParameter> commerceSubEntryParameterConverter;
    private SubOrderEntryService subOrderEntryService;
    private Populator<ToppingItemRequest, ToppingItemParameter> toppingItemParameterPopulator;
    private LoyaltyService loyaltyService;
    private UpdateOrderSequenceCacheService updateOrderSequenceCacheService;
    private ObjectMapper objectMapper;
    private Validator<CommerceAbstractOrderParameter> saleOffCartEntryValidator;
    private Validator<CommerceAbstractOrderParameter> saleOffUpdateQuantityCartEntryValidator;
    private OrderSourceService orderSourceService;

    public CartFacadeImpl(CartService cartService, AuthService userService,
                          Converter<CartModel, CartData> cartConverter) {
        this.cartService = cartService;
        this.authService = userService;
        this.cartConverter = cartConverter;
    }

    @Override
    public CartData getDetail(CartInfoParameter parameter) {
        parameter.setUserId(authService.getCurrentUserId());
        CartModel cart = cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(parameter);

        validateAbstractOrder(cart);
        commerceCartService.recalculate(cart, false);
        cart = cartService.save(cart);
        return cartConverter.convert(cart);
    }

    @Override
    public CartData createNewCart(CartInfoParameter cartInfoParameter) {
        cartInfoParameter.setUserId(authService.getCurrentUserId());
        Long orderSourceId = cartInfoParameter.getOrderSourceId();
        if(orderSourceId != null) {
            cartInfoParameter.setOrderSourceModel(orderSourceService.findByIdAndCompanyId(cartInfoParameter.getOrderSourceId(), cartInfoParameter.getCompanyId()));
        }
        CartModel cart = cartService.getOrCreateNewCart(cartInfoParameter);
        return cartConverter.convert(cart);
    }

    @Override
    public void createNewImageInCart(OrderImagesRequest request, String cardCode) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cardCode, request.getCompanyId());
        validateAbstractOrder(cartModel);
        if (CollectionUtils.isEmpty(request.getOrderImages())) {
            return;
        }

        try {
            cartModel.setImages(objectMapper.writeValueAsString(request.getOrderImages()));
        } catch (JsonProcessingException e) {
            LOGGER.error("CANNOT WRITE ORDER IMAGE: {}", cardCode);
        }
        cartService.save(cartModel);
    }

    @Override
    public void remove(CartInfoParameter cartInfoParameter) {
        cartInfoParameter.setUserId(authService.getCurrentUserId());
        CartModel cart = cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(cartInfoParameter);
        validateAbstractOrder(cart);
        cartService.delete(cart);
    }

    @Override
    public CartData addToCart(OrderEntryDTO orderEntryDTO) {
        Mutex<String> mutex = mutexFactory.getMutex(orderEntryDTO.getOrderCode());
        synchronized (mutex) {
            CommerceAbstractOrderParameter cartParameter = commerceCartParameterConverter.convert(orderEntryDTO);
            commerceCartService.addToCart(cartParameter);
            CartModel cart = (CartModel) cartParameter.getOrder();
            return cartConverter.convert(cart);
        }
    }

    @Override
    public CartData refresh(RefreshCartRequest refreshCartRequest) {
        CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(refreshCartRequest.getCode(), authService.getCurrentUserId(),
                refreshCartRequest.getOldCompanyId());
        validateAbstractOrder(cart);

        if (!refreshCartRequest.getCompanyId().equals(cart.getCompanyId())) {
            cart.setDistributorId(null);
        }
        if (!shouldClearCartData(cart, refreshCartRequest)) {
            cart = cartService.save(cart);
            return cartConverter.convert(cart);
        }
        cart = cartService.save(cart);

        if (CollectionUtils.isNotEmpty(cart.getEntries())) {
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(cart);
            commerceCartService.removeAllEntries(parameter);
        }

        return cartConverter.convert(cart);
    }

    @Override
    public CartData updateCartEntry(OrderEntryDTO orderEntryDTO) {
        if (orderEntryDTO.getQuantity() == null) {
            ErrorCodes err = ErrorCodes.INVALID_QUANTITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        Mutex<String> mutex = mutexFactory.getMutex(orderEntryDTO.getOrderCode());
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(orderEntryDTO.getOrderCode(),
                    authService.getCurrentUserId(), orderEntryDTO.getCompanyId());
            validateAbstractOrder(cart);
            if(!updateOrderSequenceCacheService.isValidTimeRequest("updateCartEntry", orderEntryDTO.getOrderCode(),
                    orderEntryDTO.getEntryId(), orderEntryDTO.getTimeRequest())) {
                return cartConverter.convert(cart);
            }
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(cart);
            parameter.setQuantity(orderEntryDTO.getQuantity());
            parameter.setEntryId(orderEntryDTO.getEntryId());
            saleOffUpdateQuantityCartEntryValidator.validate(parameter);
            commerceCartService.updateQuantityForCartEntry(parameter);
            return cartConverter.convert(cart);
        }
    }

    @Override
    public CartData updateDiscountOfCartEntry(OrderEntryDTO orderEntryDTO) {
        Mutex<String> mutex = mutexFactory.getMutex(orderEntryDTO.getOrderCode());
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(orderEntryDTO.getOrderCode(),
                    authService.getCurrentUserId(), orderEntryDTO.getCompanyId());
            if(!updateOrderSequenceCacheService.isValidTimeRequest("updateDiscountOfCartEntry",
                    orderEntryDTO.getOrderCode(), orderEntryDTO.getEntryId(), orderEntryDTO.getTimeRequest())) {
                return cartConverter.convert(cart);
            }
            validateAbstractOrder(cart);
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(cart);
            parameter.setDiscount(orderEntryDTO.getDiscount());
            parameter.setDiscountType(orderEntryDTO.getDiscountType());
            parameter.setEntryId(orderEntryDTO.getEntryId());
//            parameter.setEntryNumber(orderEntryDTO.getEntryNumber());
            commerceCartService.updateDiscountForCartEntry(parameter);
            return cartConverter.convert(cart);
        }
    }

    @Override
    public CartData updateDiscountOfCart(CartDiscountRequest cartDiscountRequest) {
        Mutex<String> mutex = mutexFactory.getMutex(cartDiscountRequest.getCode());
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(cartDiscountRequest.getCode(),
                    authService.getCurrentUserId(), cartDiscountRequest.getCompanyId());
            validateAbstractOrder(cart);
            if(!updateOrderSequenceCacheService.isValidTimeRequest("updateDiscountOfCart", cartDiscountRequest.getCode(),
                    null, cartDiscountRequest.getTimeRequest())) {
                return cartConverter.convert(cart);
            }
            CurrencyType currencyType = CurrencyType.findByCode(cartDiscountRequest.getDiscountType());
            validateCurrencyType(currencyType);
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(cart);
            parameter.setDiscount(cartDiscountRequest.getDiscount());
            parameter.setDiscountType(cartDiscountRequest.getDiscountType());
            commerceCartService.updateDiscountForCart(parameter);
            return cartConverter.convert(cart);
        }
    }

    @Override
    public CartData updateVatOfCart(VatRequest vatRequest) {
        Mutex<String> mutex = mutexFactory.getMutex(vatRequest.getCode());
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(vatRequest.getCode(),
                    authService.getCurrentUserId(), vatRequest.getCompanyId());
            validateAbstractOrder(cart);
            if(!updateOrderSequenceCacheService.isValidTimeRequest("updateVatOfCart", vatRequest.getCode(),
                    null, vatRequest.getTimeRequest())) {
                return cartConverter.convert(cart);
            }
            CurrencyType currencyType = CurrencyType.findByCode(vatRequest.getVatType());
            if (currencyType == null) {
                ErrorCodes err = ErrorCodes.INVALID_VAT_TYPE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(cart);
            parameter.setVat(vatRequest.getVat());
            parameter.setVatType(vatRequest.getVatType());
            commerceCartService.updateVatForCart(parameter);
            return cartConverter.convert(cart);
        }
    }

    @Override
    public CartData updatePriceCartEntry(OrderEntryDTO orderEntryDTO) {
        Mutex<String> mutex = mutexFactory.getMutex(orderEntryDTO.getOrderCode());
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(orderEntryDTO.getOrderCode(),
                    authService.getCurrentUserId(), orderEntryDTO.getCompanyId());
            validateAbstractOrder(cart);
            if(!updateOrderSequenceCacheService.isValidTimeRequest("updatePriceCartEntry", orderEntryDTO.getOrderCode(),
                    orderEntryDTO.getEntryId(), orderEntryDTO.getTimeRequest())) {
                return cartConverter.convert(cart);
            }

            validatePriceOrderEntry(cart, orderEntryDTO);
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(cart);
            parameter.setEntryId(orderEntryDTO.getEntryId());
            parameter.setBasePrice(orderEntryDTO.getPrice());
            commerceCartService.updatePriceForCartEntry(parameter);
            return cartConverter.convert(cart);
        }
    }

    @Override
    public CartData updateWeightForCartEntry(OrderEntryDTO orderEntryDTO) {
        CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(orderEntryDTO.getOrderCode(),
                authService.getCurrentUserId(), orderEntryDTO.getCompanyId());
        validateAbstractOrder(cart);
        CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
        parameter.setOrder(cart);
        parameter.setWeight(orderEntryDTO.getWeight());
        parameter.setEntryId(orderEntryDTO.getEntryId());
        commerceCartService.updateWeightForOrderEntry(parameter);
        return cartConverter.convert(cart);
    }

    @Override
    public CartData applyCoupon(AppliedCouponRequest appliedCouponRequest) {
        Mutex<String> mutex = mutexFactory.getMutex(appliedCouponRequest.getOrderCode());
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(appliedCouponRequest.getOrderCode(),
                    authService.getCurrentUserId(), appliedCouponRequest.getCompanyId());
            validateAbstractOrder(cart);
            CommerceRedeemCouponParameter parameter = new CommerceRedeemCouponParameter(cart, appliedCouponRequest.getCouponCode());
            parameter.setRedemptionQuantity(appliedCouponRequest.getRedemptionQuantity());
            couponService.redeemCoupon(parameter);
            return cartConverter.convert(cart);
        }
    }

    @Override
    public CartData removeCoupon(AppliedCouponRequest appliedCouponRequest) {
        Mutex<String> mutex = mutexFactory.getMutex(appliedCouponRequest.getOrderCode());
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(appliedCouponRequest.getOrderCode(),
                    authService.getCurrentUserId(), appliedCouponRequest.getCompanyId());
            validateAbstractOrder(cart);
            CommerceRedeemCouponParameter parameter = new CommerceRedeemCouponParameter(cart, appliedCouponRequest.getCouponCode());
            couponService.releaseCoupon(parameter);
            return cartConverter.convert(cart);
        }
    }

    @Override
    public CartData addProductToCombo(AddSubOrderEntryRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getOrderCode());
        synchronized (mutex) {
            CartModel cartModel = cartService.findByCodeAndCompanyId(request.getOrderCode(), request.getCompanyId());
            validateAbstractOrder(cartModel);
            ComboData comboData = productService.getCombo(request.getComboId(), request.getCompanyId());
            CartEntryModel entryModel = cartService.findEntryBy(request.getEntryId(), cartModel);
            validateAbstractOrderEntry(entryModel);
            ProductInComboData productInComboData = getValidatedProductInEntryCombo(request, comboData, entryModel);
            productInComboData.setComboGroupNumber(request.getComboGroupNumber());
            productInComboData.setQuantity(request.getQuantity());
            productInComboData.setUpdateQuantity(request.isUpdateQuantity());
            CommerceAbstractOrderEntryParameter parameter = new CommerceAbstractOrderEntryParameter(entryModel, cartModel);
            parameter.setProductInComboData(productInComboData);
            parameter.setComboData(comboData);
            CommerceCartModification modification = commerceCartService.addProductToCombo(parameter);
            int totalItemOfEntry = getTotalItemOfEntry(parameter.getOrderEntryModel());
            int maxTotalItemsOfCombo = getMaxTotalItemsOfCombo(comboData, parameter.getOrderEntryModel());
            if (totalItemOfEntry == maxTotalItemsOfCombo) {
                commerceCartService.calculateComboEntryPrices(entryModel);
            }
            OrderEntryData updatedEntry = orderEntryConverter.convert(modification.getEntry());
            CartData cartData = cartConverter.convert(cartModel);
            cartData.setUpdatedOrderEntry(updatedEntry);
            return cartData;
        }
    }


    @Override
    public CartData addComboToOrderIndirectly(AddSubOrderEntryRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getOrderCode());
        synchronized (mutex) {
            CommerceAbstractOrderParameter cartParameter = commerceSubEntryParameterConverter.convert(request);
            commerceCartService.changeOrderEntryToComboEntry(cartParameter);
            CartModel cart = (CartModel) cartParameter.getOrder();
            return cartConverter.convert(cart);
        }
    }
    @Override
    @Transactional
    public void removeSubEntry(RemoveSubOrderEntryRequest request) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(request.getOrderCode(), request.getCompanyId());
        validateAbstractOrder(cartModel);
        CartEntryModel entryModel = cartService.findEntryBy(request.getEntryId(), cartModel);
        validateAbstractOrderEntry(entryModel);
        if (ComboType.FIXED_COMBO.toString().equals(entryModel.getComboType())) {
            ErrorCodes err = ErrorCodes.CANNOT_REMOVE_SUB_ORDER_ENTRY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        SubOrderEntryModel subOrderEntryModel = subOrderEntryService.findByOrderEntryAndId(entryModel, request.getSubEntryId());
        if (subOrderEntryModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_SUB_ORDER_ENTRY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        entryModel.getSubOrderEntries().remove(subOrderEntryModel);
        commerceCartService.clearComboEntryPrices(entryModel);
        cartService.saveEntry(entryModel);
    }

    @Override
    @Transactional
    public <T extends AbstractOrderData> T importOrderItem(String cartCode, Long companyId, MultipartFile multipartFile) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, companyId);
        validateAbstractOrder(cartModel);
        List<OrderItemDTO> orderItemDTOList = orderItemExcelFileReader.read(multipartFile);
        if (CollectionUtils.isEmpty(orderItemDTOList)) {
            ErrorCodes err = ErrorCodes.EMPTY_IMPORT_ORDER_PRODUCT;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        importOrderItemValidator.validate(orderItemDTOList);
        List<OrderItemDTO> errorItems = orderItemDTOList.stream().filter(item -> StringUtils.isNotBlank(item.getError()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(errorItems)) {
            OrderImportData orderImportData = createErrorOrderImportData(errorItems);
            return (T) orderImportData;
        }
        AbstractOrderItemImportParameter param = new AbstractOrderItemImportParameter(orderItemDTOList);

        orderEntriesPopulator.populate(param, cartModel);
        errorItems = orderItemDTOList.stream().filter(item -> StringUtils.isNotBlank(item.getError()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(errorItems)) {
            OrderImportData orderImportData = createErrorOrderImportData(errorItems);
            return (T) orderImportData;
        }

        commerceCartService.recalculate(cartModel, true);
        return (T) cartConverter.convert(cartModel);
    }

    @Override
    public CartData appliedPromotion(String cartCode, Long companyId, Long promotionSourceRuleId) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, companyId);
        validateAbstractOrder(cartModel);
        cartModel.setAppliedPromotionSourceRuleId(promotionSourceRuleId);
        commerceCartService.recalculate(cartModel, true);
        return cartConverter.convert(cartModel);
    }

    @Override
    public CartData addToppingOption(ToppingOptionRequest request, String cartCode) {
        CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, request.getCompanyId());
        validateAbstractOrder(cartModel);
        CartEntryModel entryModel = cartService.findEntryBy(request.getEntryId(), cartModel);
        validateAbstractOrderEntry(entryModel);
        ToppingOptionParameter parameter = new ToppingOptionParameter();
        parameter.setAbstractOrderModel(cartModel);
        parameter.setAbstractOrderEntryModel(entryModel);
        parameter.setQuantity(request.getQuantity());
        parameter.setIce(request.getIce());
        parameter.setSugar(request.getSugar());

        commerceCartService.addToppingOption(parameter);
        return cartConverter.convert(cartModel);
    }
    @Override
    public CartData updateToppingOption(ToppingOptionRequest request, String cartCode) {
        Mutex<String> mutex = mutexFactory.getMutex(cartCode);
        synchronized (mutex) {
            CartModel cartModel = cartService.findByCodeAndCompanyId(cartCode, request.getCompanyId());
            validateAbstractOrder(cartModel);
            CartEntryModel entryModel = cartService.findEntryBy(request.getEntryId(), cartModel);
            validateAbstractOrderEntry(entryModel);
            if(!updateOrderSequenceCacheService.isValidTimeRequest("updateToppingOptionCart", cartCode,
                    request.getId(), request.getTimeRequest())) {
                return cartConverter.convert(cartModel);
            }
            ToppingOptionParameter parameter = new ToppingOptionParameter();
            parameter.setId(request.getId());
            parameter.setAbstractOrderModel(cartModel);
            parameter.setAbstractOrderEntryModel(entryModel);
            parameter.setQuantity(request.getQuantity());
            parameter.setIce(request.getIce());
            parameter.setSugar(request.getSugar());

            commerceCartService.updateToppingOption(parameter);
            return cartConverter.convert(cartModel);
        }
    }

    @Override
    public CartData addToppingItem(ToppingItemRequest request) {
        ToppingItemParameter parameter = new ToppingItemParameter();
        toppingItemParameterPopulator.populate(request, parameter);
        commerceCartService.addToppingItem(parameter);
        return cartConverter.convert((CartModel) parameter.getAbstractOrderModel());
    }

    @Override
    public CartData updateToppingItem(ToppingItemRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getOrderCode());
        synchronized (mutex) {
            CartModel cartModel = cartService.findByCodeAndCompanyId(request.getOrderCode(), request.getCompanyId());
            validateAbstractOrder(cartModel);
            CartEntryModel cartEntryModel = cartService.findEntryBy(request.getEntryId(), cartModel);
            validateAbstractOrderEntry(cartEntryModel);

            if(!updateOrderSequenceCacheService.isValidTimeRequest("updateToppingItemCart", request.getOrderCode(),
                    request.getId(), request.getTimeRequest())) {
                return cartConverter.convert(cartModel);
            }

            ToppingItemParameter parameter = poulateToppingItemParameter(cartModel, cartEntryModel, request);
            commerceCartService.updateToppingItem(parameter);
            return cartConverter.convert((CartModel) parameter.getAbstractOrderModel());
        }
    }

    @Override
    public CartData updateDiscountForToppingItem(ToppingItemRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getOrderCode());
        synchronized (mutex) {
            CartModel cartModel = cartService.findByCodeAndCompanyId(request.getOrderCode(), request.getCompanyId());
            validateAbstractOrder(cartModel);
            CurrencyType currencyType = CurrencyType.findByCode(request.getDiscountType());
            validateCurrencyType(currencyType);
            CartEntryModel cartEntryModel = cartService.findEntryBy(request.getEntryId(), cartModel);
            validateAbstractOrderEntry(cartEntryModel);
            if(!updateOrderSequenceCacheService.isValidTimeRequest("updateDiscountForToppingItemCart", request.getOrderCode(),
                    request.getId(), request.getTimeRequest())) {
                return cartConverter.convert(cartModel);
            }
            ToppingItemParameter parameter = poulateToppingItemParameter(cartModel, cartEntryModel, request);
            parameter.setDiscountType(request.getDiscountType());
            parameter.setDiscount(request.getDiscount());
            commerceCartService.updateDiscountForToppingItem(parameter);
            return cartConverter.convert((CartModel) parameter.getAbstractOrderModel());
        }
    }

    @Override
    public CartData removeListCartEntry(EntryRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getOrderCode());
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(request.getOrderCode(),
                    authService.getCurrentUserId(), request.getCompanyId());
            validateAbstractOrder(cart);
            commerceCartService.updateListOrderEntry(cart, request);
            return cartConverter.convert(cart);
        }
    }

    @Override
    public AwardLoyaltyData getLoyaltyPointsFor(String cartCode, Long companyId) {
        CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(cartCode,
                authService.getCurrentUserId(), companyId);
        validateAbstractOrder(cart);
        return loyaltyService.getLoyaltyPointsOf(cart);
    }

    @Override
    public CartData updatePriceForCartEntries(CartInfoParameter parameter) {
        Mutex<String> mutex = mutexFactory.getMutex(parameter.getCode());
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(parameter.getCode(),
                    authService.getCurrentUserId(), parameter.getCompanyId());
            validateAbstractOrder(cart);
            cart.setPriceType(parameter.getPriceType());
            cart.setDistributorId(parameter.getDistributorId());

            validateDistributorPriceOrder(cart);
            if (CollectionUtils.isEmpty(cart.getEntries())) {
                cartService.save(cart);
                return cartConverter.convert(cart);
            }

            commerceCartService.updatePriceForCartEntries(cart);
            return cartConverter.convert(cart);
        }
    }

    @Override
    public List<OrderSettingDiscountData> checkDiscountMaximum(Long companyId, String cartCode) {
        CartModel cart = cartService.findByCodeAndCompanyId(cartCode, companyId);

        validateAbstractOrder(cart);
        return commerceCartService.checkDiscountMaximumOrder(cart);
    }

    @Override
    public CartData updateAllDiscountForCart(String cartCode, UpdateAllDiscountRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(cartCode);
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(cartCode,
                    authService.getCurrentUserId(), request.getCompanyId());
            validateAbstractOrder(cart);
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(cart);
            commerceCartService.updateAllDiscountForCart(parameter, request);
            return cartConverter.convert(cart);
        }
    }

    @Override
    public CartData updateRecommendedRetailPriceForCartEntry(OrderEntryDTO orderEntryDTO) {
        Mutex<String> mutex = mutexFactory.getMutex(orderEntryDTO.getOrderCode());
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(orderEntryDTO.getOrderCode(),
                    authService.getCurrentUserId(), orderEntryDTO.getCompanyId());
            validateAbstractOrder(cart);
            if(!updateOrderSequenceCacheService.isValidTimeRequest("updateRecommendedRetailPriceForCartEntry", orderEntryDTO.getOrderCode(),
                    orderEntryDTO.getEntryId(), orderEntryDTO.getTimeRequest())) {
                return cartConverter.convert(cart);
            }

            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(cart);
            parameter.setEntryId(orderEntryDTO.getEntryId());
            parameter.setRecommendedRetailPrice(orderEntryDTO.getRecommendedRetailPrice());
            boolean reload = commerceCartService.updateRecommendedRetailPriceForCartEntry(parameter);
            CartData cartData = cartConverter.convert(cart);
            cartData.setReload(reload);
            return cartData;
        }
    }

    @Override
    public CartData markEntrySaleOff(EntrySaleOffRequest request) {
        if(request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        Mutex<String> mutex = mutexFactory.getMutex(request.getOrderCode());
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(request.getOrderCode(),
                    authService.getCurrentUserId(), request.getCompanyId());
            validateAbstractOrder(cart);
            CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
            parameter.setOrder(cart);
            parameter.setEntryId(request.getEntryId());
            parameter.setSaleOff(request.isSaleOff());
            saleOffCartEntryValidator.validate(parameter);
            commerceCartService.markEntrySaleOff(parameter);
            return cartConverter.convert(cart);
        }
    }

    @Override
    public boolean isSaleOffEntry(OrderEntryDTO orderEntryDTO) {
        return cartService.isSaleOffEntry(orderEntryDTO);
    }

    @Override
    public CartData updateCustomer(UpdateCustomerRequest request) {
        Mutex<String> mutex = mutexFactory.getMutex(request.getCode());
        synchronized (mutex) {
            validateCustomer(request.getCustomer(), request.getCompanyId());
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(request.getCode(),
                    authService.getCurrentUserId(), request.getCompanyId());
            validateAbstractOrder(cart);
            AbstractOrderModel abstractOrderModel = commerceCartService.updateCustomer(request, cart);
            CartModel cartModel = (CartModel) abstractOrderModel;
            return cartConverter.convert(cartModel);
        }
    }

    @Override
    public CartData addVAT(Long companyId, String cartCode, Boolean addVat) {
        Mutex<String> mutex = mutexFactory.getMutex(cartCode);
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(cartCode,
                    authService.getCurrentUserId(), companyId);
            validateAbstractOrder(cart);
            AbstractOrderModel abstractOrderModel = commerceCartService.addVatOf(cart, addVat);
            CartModel cartModel = (CartModel) abstractOrderModel;
            return cartConverter.convert(cartModel);
        }
    }

    @Override
    public CartData changeOrderSource(CartInfoParameter cartInfoParameter) {
        if(cartInfoParameter.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        Mutex<String> mutex = mutexFactory.getMutex(cartInfoParameter.getCode());
        synchronized (mutex) {
            CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(cartInfoParameter.getCode(),
                    authService.getCurrentUserId(), cartInfoParameter.getCompanyId());
            validateAbstractOrder(cart);
            AbstractOrderModel abstractOrderModel = commerceCartService.changeOrderSource(cart, cartInfoParameter.getOrderSourceId());
            CartModel cartModel = (CartModel) abstractOrderModel;
            return cartConverter.convert(cartModel);
        }
    }

    @Autowired
    public void setCommerceCartService(CommerceCartService commerceCartService) {
        this.commerceCartService = commerceCartService;
    }

    @Autowired
    @Qualifier("commerceCartParameterConverter")
    public void setCommerceCartParameterConverter
            (Converter<OrderEntryDTO, CommerceAbstractOrderParameter> commerceCartParameterConverter) {
        this.commerceCartParameterConverter = commerceCartParameterConverter;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }

    @Autowired
    public void setSubOrderEntryService(SubOrderEntryService subOrderEntryService) {
        this.subOrderEntryService = subOrderEntryService;
    }

    @Autowired
    @Qualifier("commerceSubCartEntryParameterConverter")
    public void setCommerceSubEntryParameterConverter
            (Converter<AddSubOrderEntryRequest, CommerceAbstractOrderParameter> commerceSubEntryParameterConverter) {
        this.commerceSubEntryParameterConverter = commerceSubEntryParameterConverter;
    }

    @Autowired
    @Qualifier("toppingItemCartParameterPopulator")
    public void setToppingItemParameterPopulator
            (Populator<ToppingItemRequest, ToppingItemParameter> toppingItemParameterPopulator) {
        this.toppingItemParameterPopulator = toppingItemParameterPopulator;
    }


    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @Autowired
    public void setUpdateOrderSequenceCacheService(UpdateOrderSequenceCacheService updateOrderSequenceCacheService) {
        this.updateOrderSequenceCacheService = updateOrderSequenceCacheService;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    @Qualifier("saleOffCartEntryValidator")
    public void setSaleOffCartEntryValidator(Validator<CommerceAbstractOrderParameter> saleOffCartEntryValidator) {
        this.saleOffCartEntryValidator = saleOffCartEntryValidator;
    }

    @Autowired
    @Qualifier("saleOffUpdateQuantityCartEntryValidator")
    public void setSaleOffUpdateQuantityCartEntryValidator(Validator<CommerceAbstractOrderParameter> saleOffUpdateQuantityCartEntryValidator) {
        this.saleOffUpdateQuantityCartEntryValidator = saleOffUpdateQuantityCartEntryValidator;
    }

    @Autowired
    public void setOrderSourceService(OrderSourceService orderSourceService) {
        this.orderSourceService = orderSourceService;
    }
}
