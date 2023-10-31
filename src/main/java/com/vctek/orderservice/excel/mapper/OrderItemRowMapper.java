package com.vctek.orderservice.excel.mapper;

import com.vctek.orderservice.dto.excel.OrderItemDTO;
import com.vctek.orderservice.excel.RowMapperErrorCodes;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import com.vctek.orderservice.util.CurrencyType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.excel.RowMapper;
import org.springframework.batch.item.excel.support.rowset.RowSet;

public class OrderItemRowMapper implements RowMapper<OrderItemDTO> {

    private static final int SKU_COL_INDEX = 0;
    private static final int QUANTITY_COL_INDEX = 1;
    private static final int PRICE_COL_INDEX = 2;
    private static final int DISCOUNT_COL_INDEX = 3;
    private static final int DISCOUNT_TYPE_COL_INDEX = 4;

    @Override
    public OrderItemDTO mapRow(RowSet rowSet) {
        int currentRowIndex = rowSet.getCurrentRowIndex();
        OrderItemDTO dto = new OrderItemDTO();
        dto.setRowExcel(currentRowIndex + 1);
        dto.setSku(com.vctek.util.CommonUtils.readTextField(rowSet.getColumnValue(SKU_COL_INDEX)));
        dto.setQuantity(rowSet.getColumnValue(QUANTITY_COL_INDEX));
        dto.setPrice(rowSet.getColumnValue(PRICE_COL_INDEX));
        dto.setDiscount(rowSet.getColumnValue(DISCOUNT_COL_INDEX));
        String discountType = rowSet.getColumnValue(DISCOUNT_TYPE_COL_INDEX);
        String discountTypeValue = getDiscountType(dto, discountType);
        dto.setDiscountType(discountTypeValue);
        return dto;
    }

    private String getDiscountType(OrderItemDTO dto, String discountType) {
        if(StringUtils.isBlank(discountType)) {
            return CurrencyType.CASH.toString();
        }

        String discountTypeClean = discountType.trim();
        if(CommonUtils.PERCENT.equals(discountTypeClean)) {
            return CurrencyType.PERCENT.toString();
        }

        if(CommonUtils.CASH.equals(discountTypeClean)) {
            return CurrencyType.CASH.toString();
        }
        dto.setError(RowMapperErrorCodes.INVALID_DISCOUNT_TYPE.toString());

        return CurrencyType.CASH.toString();
    }
}
