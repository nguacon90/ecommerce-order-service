package com.vctek.orderservice.util;

public enum ExcelTemplateFile {
    RETAIL_LIST_TEMPLATE("templates/retail_list_template.xlsx"),
    ORDER_LIST_TEMPLATE("templates/order_list_template.xlsx"),
    WHOLESALE_LIST_TEMPLATE("templates/wholesale_list_template.xlsx"),

    ;
    private String filePath;

    ExcelTemplateFile(String filePath) {
        this.filePath = filePath;
    }

    public String filePath() {
        return this.filePath;
    }

}
