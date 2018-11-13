package com.oppo.statistics.util;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint({"SimpleDateFormat"})
public class TimeInfoUtil {
    public static final long MILLISECOND_OF_A_DAY = 86400000;
    public static final long MILLISECOND_OF_A_MINUTE = 60000;
    public static final long MILLISECOND_OF_A_WEEK = 604800000;
    public static final String TIME_PATTERN_01 = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_PATTERN_02 = "yyyyMMddHH";
    public static final String TIME_PATTERN_03 = "yyyyMMdd";

    public static String getFormatTime() {
        return new SimpleDateFormat(TIME_PATTERN_01).format(new Date());
    }

    public static String getFormatTime(long timeMills) {
        return new SimpleDateFormat(TIME_PATTERN_01).format(new Date(timeMills));
    }

    public static String getFormatHour() {
        return new SimpleDateFormat(TIME_PATTERN_02).format(new Date());
    }

    public static String getFormatHour(long timeMills) {
        return new SimpleDateFormat(TIME_PATTERN_02).format(new Date(timeMills));
    }

    public static String getFormatDate() {
        return new SimpleDateFormat(TIME_PATTERN_03).format(new Date());
    }

    public static String getFormatDate(long timeMills) {
        return new SimpleDateFormat(TIME_PATTERN_03).format(new Date(timeMills));
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
