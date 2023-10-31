package com.vctek.orderservice.excel.mapper;

import com.vctek.orderservice.dto.excel.ProductLoyaltyRewardRateDTO;
import com.vctek.util.CommonUtils;
import org.springframework.batch.item.excel.RowMapper;
import org.springframework.batch.item.excel.support.rowset.RowSet;

public class ProductLoyaltyRewardRateRowMapper implements RowMapper<ProductLoyaltyRewardRateDTO> {

    private static final int PRODUCTID_COL_INDEX = 0;
    private static final int PRODUCTSKU_COL_INDEX = 1;
    private static final int PRODUCTNAME_COL_INDEX = 2;
    private static final int REWARDRATE_COL_INDEX = 3;

    @Override
    public ProductLoyaltyRewardRateDTO mapRow(RowSet rowSet) {
        int currentRowIndex = rowSet.getCurrentRowIndex();
        ProductLoyaltyRewardRateDTO dto = new ProductLoyaltyRewardRateDTO();
        dto.setRowExcel(currentRowIndex + 1);
        dto.setProductId(CommonUtils.readTextField(rowSet.getColumnValue(PRODUCTID_COL_INDEX)));
        dto.setProductSku(CommonUtils.readTextField(rowSet.getColumnValue(PRODUCTSKU_COL_INDEX)));
        dto.setProductName(rowSet.getColumnValue(PRODUCTNAME_COL_INDEX));
        dto.setRewardRate(rowSet.getColumnValue(REWARDRATE_COL_INDEX));
        return dto;
    }
}
