package com.oppo.widget;

import android.text.TextUtils;
import java.text.SimpleDateFormat;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoLunarUtil {
    private static final String[] ALL_SC_SOLAR_TERM_NAMES = null;
    private static final String[] ALL_TC_SOLAR_TERM_NAMES = null;
    public static final int DECREATE_A_LUANR_YEAR = -1;
    public static final int INCREASE_A_LUANR_YEAR = 1;
    public static final int LEAP_MONTH = 0;
    public static final int NORMAL_MONTH = 1;
    private static final int[][] SOLAR_TERM_DAYS = null;
    private static final String TAG = "OppoLunar";
    static SimpleDateFormat sChineseDateFormat;
    static final String[] sChineseNumber = null;
    static final long[] sLunarInfo = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.OppoLunarUtil.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.OppoLunarUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.OppoLunarUtil.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.OppoLunarUtil.<init>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public OppoLunarUtil() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.widget.OppoLunarUtil.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.OppoLunarUtil.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.oppo.widget.OppoLunarUtil.calculateLunarByGregorian(int, int, int):int[], dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static int[] calculateLunarByGregorian(int r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.oppo.widget.OppoLunarUtil.calculateLunarByGregorian(int, int, int):int[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.OppoLunarUtil.calculateLunarByGregorian(int, int, int):int[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.changeALunarYear(java.util.Calendar, int, int, int, int, int):java.util.Calendar, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static java.util.Calendar changeALunarYear(java.util.Calendar r1, int r2, int r3, int r4, int r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.changeALunarYear(java.util.Calendar, int, int, int, int, int):java.util.Calendar, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.OppoLunarUtil.changeALunarYear(java.util.Calendar, int, int, int, int, int):java.util.Calendar");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.changeALunarYearByOne(java.util.Calendar, int, int, int, int, int):java.util.Calendar, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static java.util.Calendar changeALunarYearByOne(java.util.Calendar r1, int r2, int r3, int r4, int r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.changeALunarYearByOne(java.util.Calendar, int, int, int, int, int):java.util.Calendar, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.OppoLunarUtil.changeALunarYearByOne(java.util.Calendar, int, int, int, int, int):java.util.Calendar");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.chneseStringOfALunarDay(int):java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static java.lang.String chneseStringOfALunarDay(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.chneseStringOfALunarDay(int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.OppoLunarUtil.chneseStringOfALunarDay(int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.decreaseOrIncreaseALunarYear(java.util.Calendar, int, int, int):java.util.Calendar, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static java.util.Calendar decreaseOrIncreaseALunarYear(java.util.Calendar r1, int r2, int r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.decreaseOrIncreaseALunarYear(java.util.Calendar, int, int, int):java.util.Calendar, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.OppoLunarUtil.decreaseOrIncreaseALunarYear(java.util.Calendar, int, int, int):java.util.Calendar");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.getAMonthSolarTermNames(int):java.lang.String[], dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private static java.lang.String[] getAMonthSolarTermNames(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.getAMonthSolarTermNames(int):java.lang.String[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.OppoLunarUtil.getAMonthSolarTermNames(int):java.lang.String[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.getLunarDateString(int, int, int, int):java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private static java.lang.String getLunarDateString(int r1, int r2, int r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.getLunarDateString(int, int, int, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.OppoLunarUtil.getLunarDateString(int, int, int, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.getLunarDateString(java.util.Calendar):java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static java.lang.String getLunarDateString(java.util.Calendar r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.getLunarDateString(java.util.Calendar):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.OppoLunarUtil.getLunarDateString(java.util.Calendar):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.getLunarNumber(int, int, boolean):java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private static java.lang.String getLunarNumber(int r1, int r2, boolean r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.getLunarNumber(int, int, boolean):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.OppoLunarUtil.getLunarNumber(int, int, boolean):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.leapMonth(int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static int leapMonth(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.widget.OppoLunarUtil.leapMonth(int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.widget.OppoLunarUtil.leapMonth(int):int");
    }

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

    public static int daysOfALunarMonth(int luanrYear, int lunarMonth) {
        if ((sLunarInfo[luanrYear - 1900] & ((long) (65536 >> lunarMonth))) == 0) {
            return 29;
        }
        return 30;
    }

    public static String getLunarDateString(int gregorianYear, int gregorianMonth, int gregorianDay) {
        int[] lunarDate = calculateLunarByGregorian(gregorianYear, gregorianMonth, gregorianDay);
        return getLunarDateString(lunarDate[0], lunarDate[1], lunarDate[2], lunarDate[3]);
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
