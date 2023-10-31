package com.vctek.orderservice.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public final class CommonUtils {
    public static final String APPLIED_ALL = "Tất cả";
    public static final int MAXIMUM_LENGTH_100 = 100;
    public static final String WHOLESALE_NAME = "Hóa đơn bán buôn";
    public static final String RETAIL_NAME = "Hóa đơn bán lẻ";
    public static final String ONLINE_NAME = "Đơn hàng";
    public static final String ACTIVE_STATUS = "Đã duyệt";
    public static final String INACTIVE_STATUS = "Không duyệt";
    public static final String PUBLISH_STATUS = "Đang hoạt động";
    public static final String UNPUBLISH_STATUS = "Không hoạt động";
    public static final int PRODUCT_NO_VAT = -1;
    public static final String PRODUCT_NO_VAT_NAME = "Không chịu VAT";
    public static String generateUuId() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }

    public static String generateInvoiceNumber(String code) {
        StringBuilder stringBuilder = new StringBuilder(code);
        stringBuilder.append("_");
        stringBuilder.append(generateUuId());
        return stringBuilder.toString();
    }

    public static Date add(Date date, int field, int amount) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(field, amount);
        return cal.getTime();
    }

    public static void main(String[] args) {
        Date date = DateUtil.parseDate("2020-07-01 07:02:02", DateUtil.ISO_DATE_TIME_PATTERN);
        Date dd = CommonUtils.add(date, Calendar.MINUTE, -3);
        System.out.println(DateUtil.getDateStr(dd, DateUtil.ISO_DATE_TIME_PATTERN));
    }

    public static String escapeSpecialSymbols(String str) {
        if(StringUtils.isBlank(str)) {
            return str;
        }

        String newStr = StringEscapeUtils.escapeHtml4(str);
        newStr = StringEscapeUtils.escapeJava(newStr);
        return newStr;
    }

    public static String unescapeSpecialSymbols(String str) {
        if(StringUtils.isBlank(str)) {
            return str;
        }

        String newStr = StringEscapeUtils.unescapeHtml4(str);
        newStr = StringEscapeUtils.unescapeJava(newStr);
        return newStr;
    }
}
