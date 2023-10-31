package com.vctek.orderservice.strategy.impl;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.OrderHasCouponCodeModel;
import com.vctek.orderservice.repository.OrderHasCouponRepository;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.CommerceCartService;
import com.vctek.orderservice.strategy.CommerceCartMergingStrategy;
import com.vctek.orderservice.strategy.EntryMergeStrategy;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.service.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
public class DefaultCommerceCartMergingStrategy extends AbstractCommerceCartStrategy implements CommerceCartMergingStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCommerceCartMergingStrategy.class);
    private static final String ERROR_LOG_PATTERN = "=== ERROR MERGE CART: from %s to %s, error message: %s";
    private CartService cartService;
    private UserService userService;
    private EntryMergeStrategy entryMergeStrategy;
    private CommerceCartService commerceCartService;
    private OrderHasCouponRepository orderHasCouponRepository;

    @Override
    @Transactional
    public void mergeCarts(CartModel fromCart, CartModel toCart) {
        if (!isValidForMerge(fromCart, toCart)) {
            return;
        }

        List<AbstractOrderEntryModel> toEntries = toCart.getEntries();
        if (fromCart.getAppliedPromotionSourceRuleId() != null) {
            toCart.setAppliedPromotionSourceRuleId(fromCart.getAppliedPromotionSourceRuleId());
        }

        for (final AbstractOrderEntryModel entry : fromCart.getEntries()) {
            mergeEntryWithCart(entry, toCart, toEntries);
        }
        mergeCouponCodes(fromCart, toCart);
        cartService.normalizeEntryNumbers(toCart, true);
        toCart.setCalculated(false);
        cartService.delete(fromCart);
        cartService.save(toCart);
        CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
        parameter.setOrder(toCart);
        commerceCartCalculationStrategy.recalculateCart(parameter);

    }

    private void mergeCouponCodes(CartModel fromCart, CartModel toCart) {
        Set<OrderHasCouponCodeModel> currentCouponAppliedList = fromCart.getOrderHasCouponCodeModels();
        if (CollectionUtils.isEmpty(currentCouponAppliedList)) {
            return;
        }

        Set<OrderHasCouponCodeModel> oldCouponList = toCart.getOrderHasCouponCodeModels();
        if (CollectionUtils.isNotEmpty(oldCouponList)) {
            oldCouponList.forEach(oc -> {
                oc.setOrder(null);
                oc.setCouponCode(null);
            });
            toCart.getOrderHasCouponCodeModels().removeAll(oldCouponList);
            orderHasCouponRepository.deleteAll(oldCouponList);
        }

        for (OrderHasCouponCodeModel currentCoupon : currentCouponAppliedList) {
            OrderHasCouponCodeModel orderHasCouponCodeModel = new OrderHasCouponCodeModel();
            orderHasCouponCodeModel.setCouponCode(currentCoupon.getCouponCode());
            orderHasCouponCodeModel.setOrder(toCart);
            orderHasCouponCodeModel.setRedemptionQuantity(currentCoupon.getRedemptionQuantity());
            toCart.getOrderHasCouponCodeModels().add(orderHasCouponCodeModel);
        }
    }

    private void mergeEntryWithCart(final AbstractOrderEntryModel entry, final CartModel toCart, List<AbstractOrderEntryModel> toEntries) {
        if (entry.isGiveAway()) {
            return;
        }
        final AbstractOrderEntryModel entryToMerge = entryMergeStrategy.getEntryToMerge(toEntries, entry);
        if (entryToMerge == null) {
            CartEntryModel cloneEntry = cartService.cloneEntry(entry, toCart);
            cartService.cloneSubOrderEntries(entry, cloneEntry);
            toEntries.add(cloneEntry);
            modelService.save(cloneEntry);
            return;
        }

        CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
        parameter.setOrder(toCart);
        parameter.setQuantity(entryToMerge.getQuantity() + entry.getQuantity());
        parameter.setEntryId(entryToMerge.getId());
        parameter.setRecalculate(false);
        commerceCartService.updateQuantityForCartEntry(parameter);
    }

    private boolean isValidForMerge(CartModel fromCart, CartModel toCart) {
        if (fromCart == null || toCart == null) {
            LOGGER.warn("=== IGNORE MERGE CARTS: HAS NULL CART");
            return false;
        }

        if (fromCart.getCreateByUser() != null) {
            String errorMessage = String.format(ERROR_LOG_PATTERN, fromCart.getCode(), toCart.getCode(), "Not Accept merge user cart to user cart!");
            LOGGER.error(errorMessage);
            return false;
        }

        if (!SellSignal.ECOMMERCE_WEB.toString().equalsIgnoreCase(fromCart.getSellSignal()) ||
                !SellSignal.ECOMMERCE_WEB.toString().equalsIgnoreCase(toCart.getSellSignal())) {
            String errorMessage = String.format(ERROR_LOG_PATTERN, fromCart.getCode(), toCart.getCode(), "Cannot merge cart not belong to ecommerce web sell signal!");
            LOGGER.error(errorMessage);
            return false;
        }

        if (fromCart.getId().equals(toCart.getId())) {
            String errorMessage = String.format(ERROR_LOG_PATTERN, fromCart.getCode(), toCart.getCode(), "Cannot merge cart to itself!");
            LOGGER.error(errorMessage);
            return false;
        }

        if (userService.getCurrentUserId() == null) {
            String errorMessage = String.format(ERROR_LOG_PATTERN, fromCart.getCode(), toCart.getCode(), "Only logged user can merge cart!");
            LOGGER.error(errorMessage);
            return false;
        }

        if (!fromCart.getCompanyId().equals(toCart.getCompanyId())) {
            String errorMessage = String.format(ERROR_LOG_PATTERN, fromCart.getCode(), toCart.getCode(), "Cannot merge cart with different company!");
            LOGGER.error(errorMessage);
            return false;
        }

        return true;
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setEntryMergeStrategy(EntryMergeStrategy entryMergeStrategy) {
        this.entryMergeStrategy = entryMergeStrategy;
    }

    @Autowired
    public void setCommerceCartService(CommerceCartService commerceCartService) {
        this.commerceCartService = commerceCartService;
    }

    @Autowired
    public void setOrderHasCouponRepository(OrderHasCouponRepository orderHasCouponRepository) {
        this.orderHasCouponRepository = orderHasCouponRepository;
    }
}
