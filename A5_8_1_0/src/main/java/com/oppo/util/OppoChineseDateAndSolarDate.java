package com.oppo.util;

public class OppoChineseDateAndSolarDate {
    private static native int[] NativeChineseDateToSunDate(int i, int i2, int i3);

    private static native int NativeGetChineseLeapMonth(int i);

    private static native int NativeGetChineseMonthDays(int i, int i2);

    private static native int NativeGetSunMonthDays(int i, int i2);

    private static native int[] NativeSunDateToChineseDate(int i, int i2, int i3);

    static {
        System.loadLibrary("ChineseDateAndSolarDate");
    }

    public static int[] ChineseDateToSunDate(int iChineseYear, int iChineseMonth, int iChineseDay) {
        return NativeChineseDateToSunDate(iChineseYear, iChineseMonth, iChineseDay);
    }

    public static int[] SunDateToChineseDate(int iSunDateYear, int iSunDateMonth, int iSunDateDay) {
        return NativeSunDateToChineseDate(iSunDateYear, iSunDateMonth, iSunDateDay);
    }

    public static int GetChLeapMonth(int iChineseYear) {
        return NativeGetChineseLeapMonth(iChineseYear);
    }

    public static int GetChMonthDays(int iChineseYear, int iChineseMonth) {
        return NativeGetChineseMonthDays(iChineseYear, iChineseMonth);
    }

    public static int GetSolarMonthDays(int iSolarYear, int iSolarMonth) {
        return NativeGetSunMonthDays(iSolarYear, iSolarMonth);
    }
}
