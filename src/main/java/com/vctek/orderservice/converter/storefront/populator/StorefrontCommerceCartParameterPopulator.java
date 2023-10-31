package com.vctek.orderservice.converter.storefront.populator;

import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.converter.populator.AbstractCommerceCartParameterPopulator;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.StorefrontOrderEntryDTO;
import com.vctek.orderservice.dto.request.AddSubOrderEntryRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.service.CommerceCartService;
import com.vctek.util.ComboType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("storefrontCommerceCartParameterPopulator")
public class StorefrontCommerceCartParameterPopulator extends AbstractCommerceCartParameterPopulator
        implements Populator<StorefrontOrderEntryDTO, CommerceAbstractOrderParameter> {
    private CommerceCartService commerceCartService;

    @Override
    public void populate(StorefrontOrderEntryDTO source, CommerceAbstractOrderParameter target) {
        boolean isOnsite = productService.isOnsite(source.getProductId(), source.getCompanyId());
        if(!isOnsite) {
            ErrorCodes err = ErrorCodes.PRODUCT_STOP_SELLING;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }


        populateCartModel(source, target);

        double price = getPriceOf(source.getProductId(), source.getQuantity().intValue());
        target.setBasePrice(price);
        target.setOriginBasePrice(price);
        target.setProductId(source.getProductId());
        target.setQuantity(source.getQuantity());
        target.setDiscount(source.getDiscount());
        target.setDiscountType(source.getDiscountType());
        target.setWeight(source.getWeight());
        populateCombo(source, target);
    }

    private void populateCombo(StorefrontOrderEntryDTO source, CommerceAbstractOrderParameter target) {
        ProductIsCombo productIsCombo = productService.checkIsCombo(source.getProductId(), source.getCompanyId(), source.getQuantity().intValue());
        target.setComboType(productIsCombo.getComboType());
        if(!productIsCombo.isCombo() || ComboType.FIXED_COMBO.toString().equalsIgnoreCase(productIsCombo.getComboType())) {
            return;
        }
        List<AddSubOrderEntryRequest> subOrderEntries = source.getSubOrderEntries();
        subOrderEntries.stream().forEach(item -> item.setComboId(source.getProductId()));
        target.setSubEntries(subOrderEntries);
    }

    private void populateCartModel(StorefrontOrderEntryDTO source, CommerceAbstractOrderParameter target) {
        CartModel cart = commerceCartService.getStorefrontCart(source.getOrderCode(), source.getCompanyId());
        if (cart == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        target.setCompanyId(source.getCompanyId());
        target.setOrder(cart);
        target.setWarehouseId(cart.getWarehouseId());
    }


    @Autowired
    public void setCommerceCartService(CommerceCartService commerceCartService) {
        this.commerceCartService = commerceCartService;
    }

}
