package com.vctek.orderservice.validator.storefront;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.ComboData;
import com.vctek.orderservice.dto.StorefrontOrderEntryDTO;
import com.vctek.orderservice.dto.request.AddSubOrderEntryRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.validator.AbstractCartValidator;
import com.vctek.util.ComboType;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("storefrontCartEntryValidator")
public class StorefrontCartEntryValidator extends AbstractCartValidator implements Validator<StorefrontOrderEntryDTO> {

    @Override
    public void validate(StorefrontOrderEntryDTO storefrontOrderEntryDTO) {
        validateCommonProperties(storefrontOrderEntryDTO);
        validateOnsiteProduct(storefrontOrderEntryDTO.getProductId(), storefrontOrderEntryDTO.getCompanyId());
        validateCombo(storefrontOrderEntryDTO);
    }

    protected void validateCombo(StorefrontOrderEntryDTO storefrontOrderEntryDTO) {
        Long productId = storefrontOrderEntryDTO.getProductId();
        Long companyId = storefrontOrderEntryDTO.getCompanyId();
        int productQty = storefrontOrderEntryDTO.getQuantity().intValue();
        ProductIsCombo productIsCombo = productService.checkIsCombo(productId, companyId,
                productQty);
        if(!productIsCombo.isCombo()) {
            return;
        }

        if(ComboType.FIXED_COMBO.toString().equalsIgnoreCase(productIsCombo.getComboType())) {
            return;
        }

        List<AddSubOrderEntryRequest> subOrderEntries = storefrontOrderEntryDTO.getSubOrderEntries();
        if(CollectionUtils.isEmpty(subOrderEntries)) {
            ErrorCodes err = ErrorCodes.INVALID_SUB_ENTRY_FOR_COMBO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        int totalRequestItem = 0;
        for(AddSubOrderEntryRequest request : subOrderEntries) {
            if(request.getQuantity() == null || request.getQuantity() < 1) {
                ErrorCodes err = ErrorCodes.INVALID_SUB_ORDER_ENTRY_QUANTITY;
                throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{request.getProductId()});
            }

            if(request.getComboGroupNumber() == null) {
                ErrorCodes err = ErrorCodes.EMPTY_COMBO_GROUP_NUMBER;
                throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{request.getProductId()});
            }

            totalRequestItem += request.getQuantity();
        }

        ComboData comboData = productService.getCombo(productId, companyId);
        int totalItemInCombo = comboData.getTotalItemQuantity();
        if(totalRequestItem < totalItemInCombo) {
            ErrorCodes err = ErrorCodes.NOT_ENOUGH_ITEM_IN_COMBO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{totalItemInCombo});
        }

        if(totalRequestItem > totalItemInCombo) {
            ErrorCodes err = ErrorCodes.OVER_MAX_ITEM_IN_COMBO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{totalItemInCombo});
        }
    }

}
