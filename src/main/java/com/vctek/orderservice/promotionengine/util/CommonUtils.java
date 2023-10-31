package com.vctek.orderservice.promotionengine.util;

import com.vctek.orderservice.util.CurrencyType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.StringJoiner;

public final class CommonUtils {
    private CommonUtils() {}
    public static final String COMMA = ",";
    public static final String MINUS = "-";
    public static final String PERCENT = "%";
    public static final String CASH = "VND";

    public static String join(Collection objects, String delimiter) {
        if(CollectionUtils.isEmpty(objects)) {
            return StringUtils.EMPTY;
        }

        StringJoiner joiner = new StringJoiner(delimiter);
        objects.stream().forEach(obj -> joiner.add(String.valueOf(obj)));
        return joiner.toString();
    }

    public static double getDoubleValue(Double d) {
        return  d == null ? 0 : d;
    }

    public static int getIntValue(Integer d) {
        return  d == null ? 0 : d;
    }

    public static Double calculateValueByCurrencyType(Double total, Double discount, String type) {
        if (CurrencyType.PERCENT.toString().equals(type)) {
            return total * (discount / 100);
        }

        return discount;
    }

    public static int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }
}
