package com.vctek.orderservice.excel.mapper;

import com.vctek.orderservice.dto.excel.OrderSettingDiscountDTO;
import com.vctek.util.CommonUtils;
import org.springframework.batch.item.excel.RowMapper;
import org.springframework.batch.item.excel.support.rowset.RowSet;

public class OrderSettingDiscountRowMapper implements RowMapper<OrderSettingDiscountDTO> {
    private static final int PRODUCT_COL_INDEX = 0;
    private static final int DISCOUNT_COL = 1;
    private static final int DISCOUNT_COL_TYPE = 2;

    @Override
    public OrderSettingDiscountDTO mapRow(RowSet rowSet) {
        if (rowSet.getCurrentRow() == null) {
            return null;
        }
        int currentRowIndex = rowSet.getCurrentRowIndex();
        OrderSettingDiscountDTO dto = new OrderSettingDiscountDTO();
        dto.setRowExcel(currentRowIndex + 1);
        String productSku = rowSet.getColumnValue(PRODUCT_COL_INDEX);
        productSku = productSku != null ? productSku.trim() : productSku;
        dto.setProductSku(CommonUtils.readTextField(productSku));
        dto.setDiscount(rowSet.getColumnValue(DISCOUNT_COL));
        dto.setDiscountType(rowSet.getColumnValue(DISCOUNT_COL_TYPE));
        return dto;
    }
}
