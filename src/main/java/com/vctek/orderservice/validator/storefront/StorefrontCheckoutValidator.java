package com.vctek.orderservice.validator.storefront;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceCartValidateData;
import com.vctek.orderservice.dto.CommerceCartValidateParam;
import com.vctek.orderservice.dto.CommerceEntryError;
import com.vctek.orderservice.dto.request.storefront.StoreFrontCheckoutRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.service.CommerceCartService;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component("storefrontCheckoutValidator")
public class StorefrontCheckoutValidator extends AbstractStorefrontShippingAddressValidator implements Validator<StoreFrontCheckoutRequest> {
    private PromotionResultService promotionResultService;
    private PromotionSourceRuleService promotionSourceRuleService;
    private CommerceCartService commerceCartService;

    @Override
    public void validate(StoreFrontCheckoutRequest request) {
        validateCartCode(request);
        CartModel model = commerceCartService.getStorefrontCart(request.getCode(), request.getCompanyId());;
        if(model == null) {
            ErrorCodes err = ErrorCodes.NOT_EXISTED_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        validateCustomerInfo(request);
        validateEntries(model);
        validatePromotions(model);
    }

    private void validatePromotions(final CartModel model) {
        Set<PromotionSourceRuleModel> sourceRules = promotionResultService.findAllPromotionSourceRulesByOrder(model);
        if(CollectionUtils.isEmpty(sourceRules)) {
            return;
        }
        PromotionSourceRuleModel invalidSourceRule = sourceRules.stream()
                .filter(sr -> !promotionSourceRuleService.isValidToAppliedForCart(sr, model))
                .findFirst().orElse(null);
        if(invalidSourceRule != null) {
            ErrorCodes err = ErrorCodes.ORDER_HAS_INVALID_PROMOTION;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    private void validateEntries(CartModel model) {
        if (CollectionUtils.isEmpty(model.getEntries())) {
            ErrorCodes err = ErrorCodes.CART_HAS_NOT_ENTRIES;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        CommerceCartValidateParam param = new CommerceCartValidateParam(model);
        param.setValidateCoupon(true);
        CommerceCartValidateData validateData = commerceCartService.validate(param);
        if(validateData == null || !validateData.isHasError()) {
            return;
        }

        Map<Long, CommerceEntryError> entryErrors = validateData.getEntryErrors();
        if(MapUtils.isNotEmpty(entryErrors)) {
            ErrorCodes err = ErrorCodes.SWITCH_SALE_OFF_QUANTITY_OVER_AVAILABLE_STOCK;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        List<String> orderErrorCodes = validateData.getOrderErrorCodes();
        if(CollectionUtils.isNotEmpty(orderErrorCodes) && orderErrorCodes.contains(ErrorCodes.COUPON_OVER_MAX_REDEMPTION_QUANTITY.code())) {
            ErrorCodes err = ErrorCodes.COUPON_OVER_MAX_REDEMPTION_QUANTITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(CollectionUtils.isNotEmpty(orderErrorCodes) && orderErrorCodes.contains(ErrorCodes.COUPON_NOT_APPLIED.code())) {
            ErrorCodes err = ErrorCodes.COUPON_NOT_APPLIED;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Autowired
    public void setPromotionResultService(PromotionResultService promotionResultService) {
        this.promotionResultService = promotionResultService;
    }

    @Autowired
    public void setPromotionSourceRuleService(PromotionSourceRuleService promotionSourceRuleService) {
        this.promotionSourceRuleService = promotionSourceRuleService;
    }

    @Autowired
    public void setCommerceCartService(CommerceCartService commerceCartService) {
        this.commerceCartService = commerceCartService;
    }
}
