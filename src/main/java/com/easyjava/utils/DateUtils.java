package com.easyjava.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDD = "yyyy/MM/dd";
    public static final String YYYYMMDDHHMMSS = "yyyy-MM-dd hh:mm:ss";

    public static String Format(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }

    public static Date Parse(String date, String pattern) throws ParseException {
        return new SimpleDateFormat(pattern).parse(date);
    }
}
