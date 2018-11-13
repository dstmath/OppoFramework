package com.mediatek.telephony;

import java.util.Arrays;
import java.util.Locale;

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
public class PhoneNumberFormatUtilEx {
    public static final String[] AUSTRALIA_INTERNATIONAL_PREFIXS = null;
    public static final String[] BRAZIL_INTERNATIONAL_PREFIXS = null;
    public static final boolean DEBUG = false;
    public static final int FORMAT_AUSTRALIA = 21;
    public static final int FORMAT_BRAZIL = 23;
    public static final int FORMAT_CHINA_HONGKONG = 4;
    public static final int FORMAT_CHINA_MACAU = 5;
    public static final int FORMAT_CHINA_MAINLAND = 3;
    public static String[] FORMAT_COUNTRY_CODES = null;
    public static final String[] FORMAT_COUNTRY_NAMES = null;
    public static final int FORMAT_ENGLAND = 7;
    public static final int FORMAT_FRANCE = 8;
    public static final int FORMAT_GERMANY = 10;
    public static final int FORMAT_INDIA = 12;
    public static final int FORMAT_INDONESIA = 16;
    public static final int FORMAT_ITALY = 9;
    public static final int FORMAT_JAPAN = 2;
    public static final int FORMAT_MALAYSIA = 14;
    public static final int FORMAT_NANP = 1;
    public static final int FORMAT_NEW_ZEALAND = 22;
    public static final int FORMAT_POLAND = 20;
    public static final int FORMAT_PORTUGAL = 19;
    public static final int FORMAT_RUSSIAN = 11;
    public static final int FORMAT_SINGAPORE = 15;
    public static final int FORMAT_SPAIN = 13;
    public static final int FORMAT_TAIWAN = 6;
    public static final int FORMAT_THAILAND = 17;
    public static final int FORMAT_TURKEY = 24;
    public static final int FORMAT_UNKNOWN = 0;
    public static final int FORMAT_VIETNAM = 18;
    public static final String[] FRANCE_INTERNATIONAL_PREFIXS = null;
    private static final int[] Germany_FOUR_PART_REGION_CODES = null;
    private static final int[] Germany_THREE_PART_REGION_CODES = null;
    public static final String[] HONGKONG_INTERNATIONAL_PREFIXS = null;
    private static final int[] INDIA_THREE_DIGIG_AREA_CODES = null;
    public static final String[] INDONESIA_INTERNATIONAL_PREFIXS = null;
    private static final int[] ITALY_MOBILE_PREFIXS = null;
    public static final String[] JAPAN_INTERNATIONAL_PREFIXS = null;
    private static final String[] NANP_COUNTRIES = null;
    public static final String[] NANP_INTERNATIONAL_PREFIXS = null;
    public static final String[] SINGAPORE_INTERNATIONAL_PREFIXS = null;
    public static final String TAG = "PhoneNumberFormatUtilEx";
    public static final String[] TAIWAN_INTERNATIONAL_PREFIXS = null;
    public static final String[] THAILAND_INTERNATIONAL_PREFIXS = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.<init>():void, dex: 
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
    public PhoneNumberFormatUtilEx() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatAustraliaNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatAustraliaNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatAustraliaNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatAustraliaNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatBrazilNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatBrazilNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatBrazilNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatBrazilNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatChinaNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatChinaNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatChinaNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatChinaNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatEnglandNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatEnglandNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatEnglandNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatEnglandNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatFranceNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatFranceNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatFranceNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatFranceNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatGermanyNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatGermanyNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatGermanyNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatGermanyNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatHeightLengthWithoutRegionCodeNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatHeightLengthWithoutRegionCodeNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatHeightLengthWithoutRegionCodeNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatHeightLengthWithoutRegionCodeNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatIndiaNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatIndiaNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatIndiaNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatIndiaNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatIndonesiaNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatIndonesiaNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatIndonesiaNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatIndonesiaNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatItalyNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatItalyNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatItalyNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatItalyNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatMacauNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatMacauNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatMacauNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatMacauNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatMalaysiaNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatMalaysiaNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatMalaysiaNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatMalaysiaNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatNewZealandNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatNewZealandNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatNewZealandNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatNewZealandNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatNumber(java.lang.String, int):java.lang.String, dex: 
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
    public static java.lang.String formatNumber(java.lang.String r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatNumber(java.lang.String, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatNumber(java.lang.String, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatNumber(android.text.Editable, int):void, dex: 
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
    public static void formatNumber(android.text.Editable r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatNumber(android.text.Editable, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatNumber(android.text.Editable, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatPolandNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatPolandNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatPolandNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatPolandNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatPortugalNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatPortugalNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatPortugalNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatPortugalNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatRussianNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatRussianNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatRussianNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatRussianNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatSpainNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatSpainNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatSpainNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatSpainNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatTaiwanNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatTaiwanNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatTaiwanNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatTaiwanNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatThailandNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatThailandNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatThailandNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatThailandNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatTurkeyNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatTurkeyNumber(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatTurkeyNumber(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatTurkeyNumber(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatVietnamNubmer(java.lang.StringBuilder, int):java.lang.String, dex: 
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
    private static java.lang.String formatVietnamNubmer(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatVietnamNubmer(java.lang.StringBuilder, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.formatVietnamNubmer(java.lang.StringBuilder, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.getDefaultSimCountryIso():java.lang.String, dex: 
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
    static java.lang.String getDefaultSimCountryIso() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.getDefaultSimCountryIso():java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.getDefaultSimCountryIso():java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatNumberBySpecialPrefix(java.lang.String, java.lang.String[]):int[], dex: 
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
    private static int[] getFormatNumberBySpecialPrefix(java.lang.String r1, java.lang.String[] r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatNumberBySpecialPrefix(java.lang.String, java.lang.String[]):int[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatNumberBySpecialPrefix(java.lang.String, java.lang.String[]):int[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatTypeByCommonPrefix(java.lang.String):int[], dex: 
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
    private static int[] getFormatTypeByCommonPrefix(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatTypeByCommonPrefix(java.lang.String):int[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatTypeByCommonPrefix(java.lang.String):int[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatTypeForLocale(java.util.Locale):int, dex: 
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
    public static int getFormatTypeForLocale(java.util.Locale r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatTypeForLocale(java.util.Locale):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatTypeForLocale(java.util.Locale):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatTypeFromCountryCode(java.lang.String):int, dex: 
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
    public static int getFormatTypeFromCountryCode(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatTypeFromCountryCode(java.lang.String):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatTypeFromCountryCode(java.lang.String):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatTypeFromCountryCodeInternal(java.lang.String):int, dex: 
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
    private static int getFormatTypeFromCountryCodeInternal(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatTypeFromCountryCodeInternal(java.lang.String):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.getFormatTypeFromCountryCodeInternal(java.lang.String):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.log(java.lang.String):void, dex: 
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
    public static void log(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.log(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.log(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.mtkFormatNumber(java.lang.String, int):java.lang.String, dex: 
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
    static java.lang.String mtkFormatNumber(java.lang.String r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.mtkFormatNumber(java.lang.String, int):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.mtkFormatNumber(java.lang.String, int):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.removeAllDash(java.lang.StringBuilder):java.lang.String, dex: 
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
    private static java.lang.String removeAllDash(java.lang.StringBuilder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.removeAllDash(java.lang.StringBuilder):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.removeAllDash(java.lang.StringBuilder):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.removeAllDashAndFormatBlank(java.lang.StringBuilder, int):int, dex: 
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
    private static int removeAllDashAndFormatBlank(java.lang.StringBuilder r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.removeAllDashAndFormatBlank(java.lang.StringBuilder, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.removeAllDashAndFormatBlank(java.lang.StringBuilder, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.removeTrailingDashes(java.lang.StringBuilder):java.lang.String, dex: 
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
    private static java.lang.String removeTrailingDashes(java.lang.StringBuilder r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.telephony.PhoneNumberFormatUtilEx.removeTrailingDashes(java.lang.StringBuilder):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.telephony.PhoneNumberFormatUtilEx.removeTrailingDashes(java.lang.StringBuilder):java.lang.String");
    }

    public static String formatNumber(String source) {
        return formatNumber(source, getFormatTypeForLocale(Locale.getDefault()));
    }

    static boolean checkInputNormalNumber(CharSequence text) {
        for (int index = 0; index < text.length(); index++) {
            char c = text.charAt(index);
            if ((c < '0' || c > '9') && c != '*' && c != '#' && c != '+' && c != ' ' && c != '-') {
                return false;
            }
        }
        return true;
    }

    private static int[] getFormatTypeFromNumber(String text, int defaultFormatType) {
        switch (defaultFormatType) {
            case 1:
                return getFormatNumberBySpecialPrefix(text, NANP_INTERNATIONAL_PREFIXS);
            case 2:
                return getFormatNumberBySpecialPrefix(text, JAPAN_INTERNATIONAL_PREFIXS);
            case 3:
            case 5:
            case 7:
            case FORMAT_ITALY /*9*/:
            case FORMAT_GERMANY /*10*/:
            case FORMAT_RUSSIAN /*11*/:
            case FORMAT_INDIA /*12*/:
            case FORMAT_SPAIN /*13*/:
            case FORMAT_MALAYSIA /*14*/:
            case FORMAT_VIETNAM /*18*/:
            case FORMAT_PORTUGAL /*19*/:
            case 20:
            case FORMAT_NEW_ZEALAND /*22*/:
            case FORMAT_TURKEY /*24*/:
                return getFormatTypeByCommonPrefix(text);
            case 4:
                return getFormatNumberBySpecialPrefix(text, HONGKONG_INTERNATIONAL_PREFIXS);
            case 6:
                return getFormatNumberBySpecialPrefix(text, TAIWAN_INTERNATIONAL_PREFIXS);
            case 8:
                return getFormatNumberBySpecialPrefix(text, FRANCE_INTERNATIONAL_PREFIXS);
            case FORMAT_SINGAPORE /*15*/:
                return getFormatNumberBySpecialPrefix(text, SINGAPORE_INTERNATIONAL_PREFIXS);
            case 16:
                return getFormatNumberBySpecialPrefix(text, INDONESIA_INTERNATIONAL_PREFIXS);
            case FORMAT_THAILAND /*17*/:
                return getFormatNumberBySpecialPrefix(text, THAILAND_INTERNATIONAL_PREFIXS);
            case FORMAT_AUSTRALIA /*21*/:
                return getFormatNumberBySpecialPrefix(text, AUSTRALIA_INTERNATIONAL_PREFIXS);
            case FORMAT_BRAZIL /*23*/:
                return getFormatNumberBySpecialPrefix(text, BRAZIL_INTERNATIONAL_PREFIXS);
            default:
                return null;
        }
    }

    private static int checkIndiaNumber(char c1, char c2, char c3, char c4) {
        int result = -1;
        int temp = ((c3 - 48) * 10) + (c4 - 48);
        if (c1 == '9') {
            result = 0;
        } else if (c1 == '8') {
            if ((c2 == '0' && (temp < 20 || ((temp >= 50 && temp <= 60) || temp >= 80))) || ((c2 == '1' && (temp < 10 || ((temp >= 20 && temp <= 29) || (temp >= 40 && temp <= 49)))) || ((c2 == '7' && (temp >= 90 || temp == 69)) || ((c2 == '8' && (temp < 10 || temp == 17 || ((temp >= 25 && temp <= 28) || temp == 44 || temp == 53 || temp >= 90))) || (c3 == '9' && (temp < 10 || temp == 23 || temp == 39 || ((temp >= 50 && temp <= 62) || temp == 67 || temp == 68 || temp >= 70))))))) {
                result = 0;
            }
        } else if (c1 == '7' && (c2 == '0' || ((c2 == '2' && (temp == 0 || ((temp >= 4 && temp <= 9) || temp == 50 || temp == 59 || ((temp >= 75 && temp <= 78) || temp == 93 || temp == 9)))) || ((c2 == '3' && (temp == 73 || temp == 76 || temp == 77 || temp == 96 || temp == 98 || temp == 99)) || ((c2 == '4' && (temp < 10 || temp == 11 || ((temp >= 15 && temp <= 19) || temp == 28 || temp == 29 || temp == 39 || temp == 83 || temp == 88 || temp == 89 || temp == 98 || temp == 99))) || ((c2 == '5' && (temp <= 4 || temp == 49 || temp == 50 || ((temp >= 66 && temp <= 69) || temp == 79 || ((temp >= 87 && temp <= 89) || temp >= 97)))) || ((c2 == '6' && (temp == 0 || temp == 2 || temp == 7 || temp == 20 || temp == 31 || temp == 39 || temp == 54 || temp == 55 || ((temp >= 65 && temp <= 69) || ((temp >= 76 && temp <= 79) || temp >= 96)))) || ((c2 == '7' && (temp == 2 || temp == 8 || temp == 9 || ((temp >= 35 && temp <= 39) || temp == 42 || temp == 60 || temp == 77 || temp >= 95))) || ((c2 == '8' && temp <= 39 && (temp == 0 || ((temp >= 7 && temp <= 9) || temp == 14 || ((temp >= 27 && temp <= 30) || (temp >= 37 && temp <= 39))))) || (c2 == '8' && temp > 39 && (temp == 42 || temp == 45 || temp == 60 || ((temp >= 69 && temp <= 79) || temp >= 90)))))))))))) {
            result = 0;
        }
        if (result == 0) {
            return result;
        }
        if ((c1 == '1' && c2 == '1') || ((c1 == '2' && (c2 == '0' || c2 == '2')) || ((c1 == '3' && c2 == '3') || ((c1 == '4' && (c2 == '0' || c2 == '4')) || ((c1 == '7' && c2 == '9') || (c1 == '8' && c2 == '0')))))) {
            result = 2;
        } else {
            if (Arrays.binarySearch(INDIA_THREE_DIGIG_AREA_CODES, (((c1 - 48) * 100) + ((c2 - 48) * 10)) + (c3 - 48)) >= 0) {
                result = 3;
            } else {
                result = 4;
            }
        }
        return result;
    }
}
