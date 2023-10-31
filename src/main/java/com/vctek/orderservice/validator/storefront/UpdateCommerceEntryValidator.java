package com.vctek.orderservice.validator.storefront;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.validator.AbstractCartValidator;
import com.vctek.validate.Validator;
import org.springframework.stereotype.Component;

@Component("updateCommerceEntryValidator")
public class UpdateCommerceEntryValidator extends AbstractCartValidator implements Validator<CommerceAbstractOrderParameter> {
    private EntryRepository entryRepository;

    public UpdateCommerceEntryValidator(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Override
    public void validate(CommerceAbstractOrderParameter parameter) {
        AbstractOrderModel order = parameter.getOrder();
        Long entryId = parameter.getEntryId();
        AbstractOrderEntryModel entry = entryRepository.findByIdAndOrder(entryId, order);
        if(entry == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ENTRY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if(parameter.getQuantity() != 0) {
            validateOnsiteProduct(entry.getProductId(), order.getCompanyId());
        }
    }

}
