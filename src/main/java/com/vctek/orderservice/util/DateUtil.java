package com.vctek.orderservice.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateUtil {
    public static final String ISO_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DDMMYY_PATTERN = "ddMMyy";
    public static final String ISO_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String VN_PATTERN = "dd-MM-yyyy";

    private DateUtil() {
        //NOSONAR
    }

    public static String getDateStr(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    public static Date parseDate(String date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            return format.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String dateToIsoDateStr(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ISO_DATE_TIME_PATTERN);
        return simpleDateFormat.format(date);
    }

    public static Date getDateWithoutTime(Date date) {
        if(date == null) {
            return date;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getEndDay(Date date) {
        if(date == null) {
            return date;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }
}
