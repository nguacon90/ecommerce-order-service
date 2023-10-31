package com.vctek.orderservice.validator;

import com.vctek.orderservice.dto.OrderEntryDTO;
import org.springframework.stereotype.Component;

@Component("orderEntryValidator")
public class OrderEntryValidator extends CartEntryValidator {

    @Override
    public void validateOrderType(OrderEntryDTO orderEntryDTO) {
        //NOSONAR ignore valida order type
    }
}
