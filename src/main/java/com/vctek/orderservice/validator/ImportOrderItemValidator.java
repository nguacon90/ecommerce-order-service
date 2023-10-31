package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.excel.OrderItemDTO;
import com.vctek.orderservice.excel.RowMapperErrorCodes;
import com.vctek.validate.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImportOrderItemValidator implements Validator<List<OrderItemDTO>> {

    @Override
    public void validate(List<OrderItemDTO> orderItemDTOS) throws ServiceException {
        for(OrderItemDTO itemDTO : orderItemDTOS) {
            if(StringUtils.isBlank(itemDTO.getSku())) {
                itemDTO.setError(RowMapperErrorCodes.EMPTY_SKU.toString());
            }
            validateQuantity(itemDTO);
            validateDiscount(itemDTO);
            validatePrice(itemDTO);
        }
    }

    private void validatePrice(OrderItemDTO itemDTO) {
        if(StringUtils.isBlank(itemDTO.getPrice())) {
            return;
        }
        try {
            Double.parseDouble(itemDTO.getPrice());
        } catch (NumberFormatException e) {
            itemDTO.setError(RowMapperErrorCodes.INVALID_PRICE.toString());
        }
    }

    private void validateQuantity(OrderItemDTO dto) {
        try {
            Double qty = Double.parseDouble(dto.getQuantity());
            if(qty <= 0 ) {
                dto.setError(RowMapperErrorCodes.INVALID_QUANTITY.toString());
            }
        } catch (NumberFormatException e) {
            dto.setError(RowMapperErrorCodes.INVALID_QUANTITY.toString());
        }
    }

    private void validateDiscount(OrderItemDTO dto) {
        if(StringUtils.isBlank(dto.getDiscount())) {
            return;
        }
        try {
            Double.parseDouble(dto.getDiscount());
        } catch (NumberFormatException e) {
            dto.setError(RowMapperErrorCodes.INVALID_DISCOUNT.toString());
        }
    }
}
