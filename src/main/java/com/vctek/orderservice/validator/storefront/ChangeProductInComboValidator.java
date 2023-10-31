package com.vctek.orderservice.validator.storefront;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.ComboData;
import com.vctek.orderservice.dto.request.storefront.StoreFrontSubOrderEntryRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.CommerceCartService;
import com.vctek.orderservice.service.InventoryService;
import com.vctek.orderservice.validator.AbstractCartValidator;
import com.vctek.util.ComboType;
import com.vctek.util.CommonUtils;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("changeProductInComboValidator")
public class ChangeProductInComboValidator extends AbstractCartValidator implements Validator<StoreFrontSubOrderEntryRequest> {
    private CommerceCartService commerceCartService;
    private CartService cartService;

    private InventoryService inventoryService;
    @Override
    public void validate(StoreFrontSubOrderEntryRequest request){
        CartModel storefrontCart = commerceCartService.getStorefrontCart(request.getOrderCode(), request.getCompanyId());
        if(storefrontCart == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        CartEntryModel cartEntryModel = cartService.findEntryBy(request.getEntryId(), storefrontCart);
        if(cartEntryModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ENTRY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(!cartService.isComboEntry(cartEntryModel)) {
            ErrorCodes err = ErrorCodes.NOT_ACCEPT_CHANGE_ORDER_ENTRY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(ComboType.FIXED_COMBO.toString().equalsIgnoreCase(cartEntryModel.getComboType())) {
            ErrorCodes err = ErrorCodes.CANNOT_CHANGE_COMBO_ENTRY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        SubOrderEntryModel subEntry = cartEntryModel.getSubOrderEntries().stream()
                .filter(soe -> soe.getId().equals(request.getSubEntryId()))
                .findFirst().orElse(null);

        if(subEntry == null) {
            ErrorCodes err = ErrorCodes.INVALID_SUB_ORDER_ENTRY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        Long newProductId = request.getProductId();
        if(newProductId == null) {
            ErrorCodes err = ErrorCodes.EMPTY_PRODUCT_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        validateNewProductExistedInCombo(request, cartEntryModel, subEntry);

        ProductStockData stockOfProduct = inventoryService.getStoreFrontStockOfProduct(newProductId, request.getCompanyId());
        if(CommonUtils.readValue(stockOfProduct.getQuantity()) <= 0) {
            ErrorCodes err = ErrorCodes.PRODUCT_OUT_OF_STOCK;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    private void validateNewProductExistedInCombo(StoreFrontSubOrderEntryRequest request, CartEntryModel cartEntryModel, SubOrderEntryModel subEntry) {
        SubOrderEntryModel existedSubEntryWithNewProduct = cartEntryModel.getSubOrderEntries().stream()
                .filter(soe -> soe.getProductId().equals(request.getProductId()))
                .findFirst().orElse(null);
        if(existedSubEntryWithNewProduct == null || existedSubEntryWithNewProduct.equals(subEntry)) {
            return;
        }

        if(ComboType.MULTI_GROUP.toString().equalsIgnoreCase(cartEntryModel.getComboType())) {
            ErrorCodes err = ErrorCodes.EXISTED_PRODUCT_IN_COMBO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        ComboData comboData = productService.getCombo(cartEntryModel.getProductId(), request.getCompanyId());
        if(ComboType.ONE_GROUP.toString().equalsIgnoreCase(cartEntryModel.getComboType())
            && comboData != null && !comboData.isDuplicateSaleProduct()) {
            ErrorCodes err = ErrorCodes.EXISTED_PRODUCT_IN_COMBO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }


    @Autowired
    public void setCommerceCartService(CommerceCartService commerceCartService) {
        this.commerceCartService = commerceCartService;
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    @Autowired
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
}
