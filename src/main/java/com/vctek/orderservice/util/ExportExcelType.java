package com.vctek.orderservice.util;

public enum ExportExcelType {
    EXPORT_ORDER_WIDTH_DETAIL_COMBO;

    public static ExportExcelType findByCode(String exportType) {
        for(ExportExcelType exportExcelType : ExportExcelType.values()) {
            if(exportExcelType.toString().equalsIgnoreCase(exportType)) {
                return exportExcelType;
            }
        }

        return null;
    }
}
