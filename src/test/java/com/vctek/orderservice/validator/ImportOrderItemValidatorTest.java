package com.vctek.orderservice.validator;

import com.vctek.orderservice.dto.excel.OrderItemDTO;
import com.vctek.orderservice.excel.RowMapperErrorCodes;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ImportOrderItemValidatorTest {
    private ImportOrderItemValidator validator;
    private List<OrderItemDTO> orderItemDTOS = new ArrayList<>();
    private OrderItemDTO orderItemDTO = new OrderItemDTO();

    @Before
    public void setUp() {
        validator = new ImportOrderItemValidator();
        orderItemDTOS.add(orderItemDTO);
    }

    @Test
    public void validate_EMPTY_SKU() {
        orderItemDTO.setQuantity("10");
        validator.validate(orderItemDTOS);
        assertEquals(RowMapperErrorCodes.EMPTY_SKU.toString(), orderItemDTO.getError());
    }

    @Test
    public void validate_INVALID_QUANTITY() {
        orderItemDTO.setQuantity("-10");
        validator.validate(orderItemDTOS);
        assertEquals(RowMapperErrorCodes.INVALID_QUANTITY.toString(), orderItemDTO.getError());
    }

    @Test
    public void validate_INVALID_DISCOUNT() {
        orderItemDTO.setQuantity("10");
        orderItemDTO.setDiscount("kd");
        validator.validate(orderItemDTOS);
        assertEquals(RowMapperErrorCodes.INVALID_DISCOUNT.toString(), orderItemDTO.getError());
    }

    @Test
    public void validate_INVALID_PRICE() {
        orderItemDTO.setQuantity("10");
        orderItemDTO.setPrice("kd");
        validator.validate(orderItemDTOS);
        assertEquals(RowMapperErrorCodes.INVALID_PRICE.toString(), orderItemDTO.getError());
    }
}
