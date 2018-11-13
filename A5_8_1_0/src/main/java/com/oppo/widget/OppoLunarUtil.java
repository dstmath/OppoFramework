package com.oppo.widget;

import android.text.TextUtils;
import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class OppoLunarUtil {
    private static final String[] ALL_SC_SOLAR_TERM_NAMES = new String[]{"小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"};
    private static final String[] ALL_TC_SOLAR_TERM_NAMES = new String[]{"小寒", "大寒", "立春", "雨水", "驚蟄", "春分", "清明", "穀雨", "立夏", "小滿", "芒種", "夏至", "小暑", "大暑", "立秋", "處暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"};
    public static final int DECREATE_A_LUANR_YEAR = -1;
    public static final int INCREASE_A_LUANR_YEAR = 1;
    public static final int LEAP_MONTH = 0;
    public static final int NORMAL_MONTH = 1;
    private static final int[][] SOLAR_TERM_DAYS = new int[][]{new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 6, 21, 5, 21, 6, 22, 6, 22, 8, 23, 8, 24, 8, 24, 9, 24, 8, 23, 8, 22}, new int[]{6, 21, 5, 19, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 21, 6, 22, 6, 22, 8, 23, 8, 24, 8, 24, 9, 24, 8, 23, 8, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 5, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 23, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 21, 6, 22, 6, 22, 8, 23, 8, 24, 8, 23, 9, 24, 8, 23, 8, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 19, 5, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 8, 23, 8, 24, 8, 23, 9, 24, 8, 23, 8, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 19, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 8, 23, 8, 24, 8, 23, 9, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 20, 5, 21, 7, 22, 7, 23, 7, 22, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 5, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 7, 22, 7, 23, 7, 22, 8, 23, 7, 22, 6, 21}, new int[]{5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 7, 22, 7, 23, 7, 22, 8, 23, 7, 22, 6, 21}, new int[]{5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 23, 7, 22, 8, 23, 7, 22, 6, 21}, new int[]{5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 22, 7, 22, 8, 23, 7, 22, 6, 21}, new int[]{5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 22, 7, 22, 8, 23, 7, 22, 6, 21}, new int[]{5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 22, 7, 22, 8, 23, 7, 22, 6, 21}};
    private static final String TAG = "OppoLunar";
    static SimpleDateFormat sChineseDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    static final String[] sChineseNumber = new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};
    static final long[] sLunarInfo = new long[]{19416, 19168, 42352, 21717, 53856, 55632, 91476, 22176, 39632, 21970, 19168, 42422, 42192, 53840, 119381, 46400, 54944, 44450, 38320, 84343, 18800, 42160, 46261, 27216, 27968, 109396, 11104, 38256, 21234, 18800, 25958, 54432, 59984, 92821, 23248, 11104, 100067, 37600, 116951, 51536, 54432, 120998, 46416, 22176, 107956, 9680, 37584, 53938, 43344, 46423, 27808, 46416, 86869, 19872, 42416, 83315, 21168, 43432, 59728, 27296, 44710, 43856, 19296, 43748, 42352, 21088, 62051, 55632, 23383, 22176, 38608, 19925, 19152, 42192, 54484, 53840, 54616, 46400, 46752, 103846, 38320, 18864, 43380, 42160, 45690, 27216, 27968, 44870, 43872, 38256, 19189, 18800, 25776, 29859, 59984, 27480, 23232, 43872, 38613, 37600, 51552, 55636, 54432, 55888, 30034, 22176, 43959, 9680, 37584, 51893, 43344, 46240, 47780, 44368, 21977, 19360, 42416, 86390, 21168, 43312, 31060, 27296, 44368, 23378, 19296, 42726, 42208, 53856, 60005, 54576, 23200, 30371, 38608, 19195, 19152, 42192, 118966, 53840, 54560, 56645, 46496, 22224, 21938, 18864, 42359, 42160, 43600, 111189, 27936, 44448};

    public static int daysOfLunarYear(int lunarYear) {
        int sum = 348;
        for (int i = 32768; i > 8; i >>= 1) {
            if ((sLunarInfo[lunarYear - 1900] & ((long) i)) != 0) {
                sum++;
            }
        }
        return daysOfLeapMonthInLunarYear(lunarYear) + sum;
    }

    public static int daysOfLeapMonthInLunarYear(int lunarYear) {
        if (leapMonth(lunarYear) == 0) {
            return 0;
        }
        if ((sLunarInfo[lunarYear - 1900] & 65536) != 0) {
            return 30;
        }
        return 29;
    }

    public static int leapMonth(int lunarYear) {
        if (lunarYear >= 1900 && lunarYear <= 2100) {
            return (int) (sLunarInfo[lunarYear - 1900] & 15);
        }
        Log.e(TAG, "get leapMonth:" + lunarYear + "is out of range.return 0.");
        return 0;
    }

    public static int daysOfALunarMonth(int luanrYear, int lunarMonth) {
        if ((sLunarInfo[luanrYear - 1900] & ((long) (65536 >> lunarMonth))) == 0) {
            return 29;
        }
        return 30;
    }

    public static String chneseStringOfALunarDay(int lunarDay) {
        String[] chineseTen = new String[]{"初", "十", "廿", "卅"};
        int n = lunarDay % 10 == 0 ? 9 : (lunarDay % 10) - 1;
        if (lunarDay > 30) {
            return "";
        }
        if (lunarDay == 10) {
            return "初十";
        }
        if (lunarDay == 20) {
            return "二十";
        }
        if (lunarDay == 30) {
            return "三十";
        }
        return chineseTen[lunarDay / 10] + sChineseNumber[n];
    }

    public static int[] calculateLunarByGregorian(int gregorianYear, int gregorianMonth, int gregorianDay) {
        int[] lunar = new int[]{2000, 1, 1, 1};
        Date baseDate = null;
        Date currentDate = null;
        try {
            baseDate = sChineseDateFormat.parse("1900年1月31日");
        } catch (ParseException e) {
            Log.e(TAG, "calculateLunarByGregorian(),parse baseDate error.");
            e.printStackTrace();
        }
        if (baseDate == null) {
            Log.e(TAG, "baseDate is null,return lunar date:2000.1.1");
            return lunar;
        }
        try {
            currentDate = sChineseDateFormat.parse(gregorianYear + "年" + gregorianMonth + "月" + gregorianDay + "日");
        } catch (ParseException e2) {
            Log.e(TAG, "calculateLunarByGregorian(),parse currentDate error.");
            e2.printStackTrace();
        }
        if (currentDate == null) {
            Log.e(TAG, "currentDate is null,return lunar date:2000.1.1");
            return lunar;
        }
        int offsetDaysFromBaseDate = Math.round(((float) (currentDate.getTime() - baseDate.getTime())) / 8.64E7f);
        int daysOfTempLunaryear = 0;
        int tempLunaryear = 1900;
        while (tempLunaryear < 10000 && offsetDaysFromBaseDate > 0) {
            daysOfTempLunaryear = daysOfLunarYear(tempLunaryear);
            offsetDaysFromBaseDate -= daysOfTempLunaryear;
            tempLunaryear++;
        }
        if (offsetDaysFromBaseDate < 0) {
            offsetDaysFromBaseDate += daysOfTempLunaryear;
            tempLunaryear--;
        }
        int lunarYear = tempLunaryear;
        int leapMonth = leapMonth(tempLunaryear);
        boolean isMinusLeapMonthDays = false;
        int daysOfTempLunarMonth = 0;
        int tempLunarMonth = 1;
        while (tempLunarMonth < 13 && offsetDaysFromBaseDate > 0) {
            if (leapMonth <= 0 || tempLunarMonth != leapMonth + 1 || (isMinusLeapMonthDays ^ 1) == 0) {
                daysOfTempLunarMonth = daysOfALunarMonth(lunarYear, tempLunarMonth);
            } else {
                tempLunarMonth--;
                isMinusLeapMonthDays = true;
                daysOfTempLunarMonth = daysOfLeapMonthInLunarYear(lunarYear);
            }
            offsetDaysFromBaseDate -= daysOfTempLunarMonth;
            if (isMinusLeapMonthDays && tempLunarMonth == leapMonth + 1) {
                isMinusLeapMonthDays = false;
            }
            tempLunarMonth++;
        }
        if (offsetDaysFromBaseDate == 0 && leapMonth > 0 && tempLunarMonth == leapMonth + 1) {
            if (isMinusLeapMonthDays) {
                isMinusLeapMonthDays = false;
            } else {
                isMinusLeapMonthDays = true;
                tempLunarMonth--;
            }
        }
        if (offsetDaysFromBaseDate < 0) {
            offsetDaysFromBaseDate += daysOfTempLunarMonth;
            tempLunarMonth--;
        }
        int lunarMonth = tempLunarMonth;
        int lunarDay = offsetDaysFromBaseDate + 1;
        lunar[0] = lunarYear;
        lunar[1] = lunarMonth;
        lunar[2] = lunarDay;
        lunar[3] = isMinusLeapMonthDays ? 0 : 1;
        return lunar;
    }

    public static String getLunarDateString(Calendar cal) {
        int[] lunarDate = calculateLunarByGregorian(cal.get(1), cal.get(2) + 1, cal.get(5));
        return getLunarDateString(lunarDate[0], lunarDate[1], lunarDate[2], lunarDate[3]);
    }

    public static String getLunarDateString(int gregorianYear, int gregorianMonth, int gregorianDay) {
        int[] lunarDate = calculateLunarByGregorian(gregorianYear, gregorianMonth, gregorianDay);
        return getLunarDateString(lunarDate[0], lunarDate[1], lunarDate[2], lunarDate[3]);
    }

    private static String getLunarDateString(int lunarYear, int lunarMonth, int LunarDay, int leapMonthCode) {
        return lunarYear + "年" + (leapMonthCode == 0 ? "闰" : "") + sChineseNumber[lunarMonth - 1] + "月" + chneseStringOfALunarDay(LunarDay);
    }

    public static Calendar decreaseOrIncreaseALunarYear(Calendar calendar, int lunarMonth, int lunarDay, int operatorType) {
        if (operatorType == 1 || operatorType == -1) {
            int offset = operatorType * 400;
            Calendar newCalendar = Calendar.getInstance();
            newCalendar.setTimeInMillis(calendar.getTimeInMillis());
            newCalendar.add(5, offset);
            for (int i = 0; i < 200; i++) {
                int[] lunarDates = calculateLunarByGregorian(newCalendar.get(1), newCalendar.get(2) + 1, newCalendar.get(5));
                if (lunarDates[1] == lunarMonth && lunarDates[2] == lunarDay) {
                    break;
                }
                newCalendar.add(5, -operatorType);
            }
            return newCalendar;
        }
        Log.w(TAG, "operatorType:" + operatorType + " error! Cann't increase or decrease a lunar year on this time.");
        return calendar;
    }

    public static Calendar changeALunarYearByOne(Calendar calendar, int lunarMonth, int lunarDay, int isLeap, int oldYear, int newYear) {
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTimeInMillis(calendar.getTimeInMillis());
        int oldDays = getDays(oldYear, lunarMonth, lunarDay, isLeap);
        int newDays = getDays(newYear, lunarMonth, lunarDay, isLeap);
        if (oldYear > newYear) {
            newCalendar.add(5, -((daysOfLunarYear(newYear) - newDays) + oldDays));
        } else if (oldYear >= newYear) {
            return newCalendar;
        } else {
            newCalendar.add(5, (daysOfLunarYear(oldYear) + newDays) - oldDays);
        }
        return newCalendar;
    }

    public static Calendar changeALunarYear(Calendar calendar, int lunarMonth, int lunarDay, int isLeap, int oldYear, int newYear) {
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTimeInMillis(calendar.getTimeInMillis());
        int oldYear2;
        if (oldYear > newYear) {
            while (true) {
                oldYear2 = oldYear;
                if (oldYear2 <= newYear) {
                    break;
                }
                oldYear = oldYear2 - 1;
                newCalendar = changeALunarYearByOne(newCalendar, lunarMonth, lunarDay, isLeap, oldYear2, oldYear);
            }
        } else {
            if (oldYear < newYear) {
                while (true) {
                    oldYear2 = oldYear;
                    if (oldYear2 >= newYear) {
                        break;
                    }
                    oldYear = oldYear2 + 1;
                    newCalendar = changeALunarYearByOne(newCalendar, lunarMonth, lunarDay, isLeap, oldYear2, oldYear);
                }
            }
            return newCalendar;
        }
        return newCalendar;
    }

    public static int getDays(int year, int month, int day, int isLeap) {
        int days = day;
        for (int i = 1; i < month; i++) {
            days += daysOfALunarMonth(year, i);
        }
        if (leapMonth(year) < month) {
            return days + daysOfLeapMonthInLunarYear(year);
        }
        if (leapMonth(year) == month && isLeap == 0) {
            return days + daysOfALunarMonth(year, month);
        }
        return days;
    }

    public static String getSolarTerm(int gregorianYear, int gregorianMonth, int gregorianDay) {
        int[] days = getAMonthSolarTermDays(gregorianYear, gregorianMonth);
        if (gregorianDay != days[0] && gregorianDay != days[1]) {
            return null;
        }
        String[] names = getAMonthSolarTermNames(gregorianMonth);
        if (gregorianDay == days[0]) {
            return names[0];
        }
        if (gregorianDay == days[1]) {
            return names[1];
        }
        return null;
    }

    private static int[] getAMonthSolarTermDays(int gregorianYear, int gregorianMonth) {
        int firstSolarTermIndex = (gregorianMonth - 1) * 2;
        int[] days = new int[]{0, 0};
        if (gregorianYear > 1969 && gregorianYear < 2037) {
            int firstSolarTermDay = SOLAR_TERM_DAYS[gregorianYear - 1970][firstSolarTermIndex];
            int secondSolarTermDay = SOLAR_TERM_DAYS[gregorianYear - 1970][firstSolarTermIndex + 1];
            days[0] = firstSolarTermDay;
            days[1] = secondSolarTermDay;
        }
        return days;
    }

    private static String[] getAMonthSolarTermNames(int gregorianMonth) {
        if (gregorianMonth < 1 || gregorianMonth > 12) {
            Log.e(TAG, "getAMonthSolarTermNames(),param gregorianMonth:" + gregorianMonth + " is error");
            return new String[]{"", ""};
        }
        int firstSolarTermIndex = (gregorianMonth - 1) * 2;
        String firstSolarTermName = ALL_TC_SOLAR_TERM_NAMES[firstSolarTermIndex];
        String secondSolarTermName = ALL_TC_SOLAR_TERM_NAMES[firstSolarTermIndex + 1];
        return new String[]{firstSolarTermName, secondSolarTermName};
    }

    public static String getLunarFestivalChineseString(int gregorianYear, int gregorianMonth, int gregorianDay) {
        String chineseString = getGregFestival(gregorianMonth, gregorianDay);
        if (!TextUtils.isEmpty(chineseString)) {
            return chineseString;
        }
        int[] lunarDate = calculateLunarByGregorian(gregorianYear, gregorianMonth, gregorianDay);
        chineseString = getLunarFestival(lunarDate[1], lunarDate[2]);
        if (!TextUtils.isEmpty(chineseString)) {
            return chineseString;
        }
        chineseString = getSolarTerm(gregorianYear, gregorianMonth, gregorianDay);
        if (!TextUtils.isEmpty(chineseString)) {
            return chineseString;
        }
        return getLunarNumber(lunarDate[1], lunarDate[2], lunarDate[3] == 0);
    }

    private static String getLunarNumber(int lunarMonth, int lunarDay, boolean isLeapMonth) {
        if (lunarDay != 1) {
            return chneseStringOfALunarDay(lunarDay);
        }
        if (isLeapMonth) {
            return "闰" + sChineseNumber[lunarMonth - 1];
        }
        return sChineseNumber[lunarMonth - 1] + "月";
    }

    private static String getLunarFestival(int lunarMonth, int lunarDay) {
        if (lunarMonth == 1 && lunarDay == 1) {
            return "春節";
        }
        if (lunarMonth == 5 && lunarDay == 5) {
            return "端午";
        }
        if (lunarMonth == 8 && lunarDay == 15) {
            return "中秋";
        }
        return null;
    }

    private static String getGregFestival(int gregorianMonth, int gregorianDay) {
        if (gregorianMonth == 1 && gregorianDay == 1) {
            return "";
        }
        if (gregorianMonth == 5 && gregorianDay == 1) {
            return "";
        }
        if (gregorianMonth == 10 && gregorianDay == 1) {
            return "";
        }
        return null;
    }
}
