package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.ToppingItemParameter;
import com.vctek.orderservice.dto.request.ToppingItemRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.ToppingOptionModel;
import com.vctek.orderservice.service.ToppingOptionService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractToppingItemParameterPopulator extends AbstractCommerceCartParameterPopulator
        implements Populator<ToppingItemRequest, ToppingItemParameter> {

    protected ToppingOptionService toppingOptionService;

    @Override
    public void populate(ToppingItemRequest source, ToppingItemParameter target) {
        populateAbstractOrderModel(source, target);
        Boolean isAvailableToSell = productService.productIsAvailableToSell(source.getProductId());
        if (!Boolean.TRUE.equals(isAvailableToSell)) {
            ErrorCodes err = ErrorCodes.PRODUCT_STOP_SELLING;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        target.setProductId(source.getProductId());
        target.setQuantity(source.getQuantity());
        double price = getPriceOf(source.getProductId(), 0);
        target.setPrice(price);
        target.setToppingItemId(source.getId());
        target.setDiscount(source.getDiscount());
        target.setDiscountType(source.getDiscountType());
    }

    protected abstract AbstractOrderModel getOrderModel(String code, Long companyId);

    protected void populateAbstractOrderModel(ToppingItemRequest source, ToppingItemParameter target) {
        if (source.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        AbstractOrderModel abstractOrderModel = this.getOrderModel(source.getOrderCode(), source.getCompanyId());
        if (abstractOrderModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        AbstractOrderEntryModel abstractOrderEntryModel = this.getEntry(abstractOrderModel, source.getId());
        if (abstractOrderEntryModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ENTRY_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        ToppingOptionModel toppingOption = toppingOptionService.findByIdAndOrderEntry(source.getToppingOptionId(),
                abstractOrderEntryModel);
        if (toppingOption == null) {
            ErrorCodes err = ErrorCodes.INVALID_TOPPING_OPTION_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        target.setAbstractOrderModel(abstractOrderModel);
        target.setAbstractOrderEntryModel(abstractOrderEntryModel);
        target.setToppingOptionModel(toppingOption);
    }

    protected abstract AbstractOrderEntryModel getEntry(AbstractOrderModel abstractOrderModel, Long entryId);

    @Autowired
    public void setToppingOptionService(ToppingOptionService toppingOptionService) {
        this.toppingOptionService = toppingOptionService;
    }
}
