package com.oppo.util;

import android.content.Context;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import libcore.icu.ICU;
import libcore.icu.LocaleData;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoThailandCalendarUtil {
    private static final String FILE_FULL_PATH_NAME_FOR_THAI_CALENDAR_DIR = "/data/thaicalendar/";
    private static final String FILE_FULL_PATH_NAME_FOR_THAI_CALENDAR_FILE = "enable_state.properties";
    private static final String KEY_THAI_CALENDAR_ENABLE_STATE = "key_thai_calendar_enable_state";
    private static final String TAG = "OppoThailandCalendarUtil";

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static class OppoAndroidDateFormat {
        private static boolean sIs24Hour;
        private static Locale sIs24HourLocale;
        private static final Object sLocaleLock = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public OppoAndroidDateFormat() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.appendQuotedText(android.text.SpannableStringBuilder, int, int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static int appendQuotedText(android.text.SpannableStringBuilder r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.appendQuotedText(android.text.SpannableStringBuilder, int, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.appendQuotedText(android.text.SpannableStringBuilder, int, int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.formatZoneOffset(int, int):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static java.lang.String formatZoneOffset(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.formatZoneOffset(int, int):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.formatZoneOffset(int, int):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getDateFormat(android.content.Context):java.text.DateFormat, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static final java.text.DateFormat getDateFormat(android.content.Context r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getDateFormat(android.content.Context):java.text.DateFormat, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getDateFormat(android.content.Context):java.text.DateFormat");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getDateFormatStringForSetting(android.content.Context, java.lang.String):java.lang.String, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getDateFormatStringForSetting(android.content.Context, java.lang.String):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getDateFormatStringForSetting(android.content.Context, java.lang.String):java.lang.String, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterRange(InstructionCodec.java:985)
            	at com.android.dx.io.instructions.InstructionCodec.access$1100(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$28.decode(InstructionCodec.java:611)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private static java.lang.String getDateFormatStringForSetting(android.content.Context r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getDateFormatStringForSetting(android.content.Context, java.lang.String):java.lang.String, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getDateFormatStringForSetting(android.content.Context, java.lang.String):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getDateFormatStringForSetting(android.content.Context, java.lang.String):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getDayOfWeekString(libcore.icu.LocaleData, int, int, int):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private static java.lang.String getDayOfWeekString(libcore.icu.LocaleData r1, int r2, int r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getDayOfWeekString(libcore.icu.LocaleData, int, int, int):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getDayOfWeekString(libcore.icu.LocaleData, int, int, int):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getMonthString(libcore.icu.LocaleData, int, int, int):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private static java.lang.String getMonthString(libcore.icu.LocaleData r1, int r2, int r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getMonthString(libcore.icu.LocaleData, int, int, int):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getMonthString(libcore.icu.LocaleData, int, int, int):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getTimeFormatString(android.content.Context):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static java.lang.String getTimeFormatString(android.content.Context r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getTimeFormatString(android.content.Context):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getTimeFormatString(android.content.Context):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getTimeZoneString(java.util.Calendar, int):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static java.lang.String getTimeZoneString(java.util.Calendar r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getTimeZoneString(java.util.Calendar, int):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.getTimeZoneString(java.util.Calendar, int):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.is24HourFormat(android.content.Context):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static boolean is24HourFormat(android.content.Context r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.is24HourFormat(android.content.Context):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.is24HourFormat(android.content.Context):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.oppoFormat(java.lang.CharSequence, java.util.Calendar):java.lang.CharSequence, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static java.lang.CharSequence oppoFormat(java.lang.CharSequence r1, java.util.Calendar r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.oppoFormat(java.lang.CharSequence, java.util.Calendar):java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.oppoFormat(java.lang.CharSequence, java.util.Calendar):java.lang.CharSequence");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.oppoFormat(java.lang.CharSequence, java.util.Date):java.lang.CharSequence, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static final java.lang.CharSequence oppoFormat(java.lang.CharSequence r1, java.util.Date r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.oppoFormat(java.lang.CharSequence, java.util.Date):java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.oppoFormat(java.lang.CharSequence, java.util.Date):java.lang.CharSequence");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.zeroPad(int, int):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static java.lang.String zeroPad(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.zeroPad(int, int):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateFormat.zeroPad(int, int):java.lang.String");
        }

        public static DateFormat getTimeFormat(Context context) {
            return new OppoJavaSimpleDateFormat(getTimeFormatString(context));
        }

        public static DateFormat getDateFormatForSetting(Context context, String value) {
            return new OppoJavaSimpleDateFormat(getDateFormatStringForSetting(context, value));
        }

        public static final CharSequence oppoFormat(CharSequence inFormat, long inTimeInMillis) {
            return oppoFormat(inFormat, new Date(inTimeInMillis));
        }

        private static String getYearString(int year, int count) {
            if (OppoThailandCalendarUtil.isThaiCalendarEnabled()) {
                year += 543;
            }
            if (count <= 2) {
                return zeroPad(year % 100, 2);
            }
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(year);
            return String.format(Locale.getDefault(), "%d", objArr);
        }
    }

    public static class OppoAndroidDateUtils {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateUtils.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public OppoAndroidDateUtils() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateUtils.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateUtils.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateUtils.formatDateRangeForSetting(android.content.Context, long, long, int):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        @java.lang.Deprecated
        public static java.lang.String formatDateRangeForSetting(android.content.Context r1, long r2, long r4, int r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateUtils.formatDateRangeForSetting(android.content.Context, long, long, int):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoAndroidDateUtils.formatDateRangeForSetting(android.content.Context, long, long, int):java.lang.String");
        }

        @Deprecated
        public static String formatDateTimeForSetting(Context context, long millis, int flags) {
            return OppoDateUtils.formatDateTime(context, millis, flags);
        }

        @Deprecated
        public static Formatter formatDateRangeForSetting(Context context, Formatter formatter, long startMillis, long endMillis, int flags) {
            return OppoDateUtils.formatDateRange(context, formatter, startMillis, endMillis, flags);
        }
    }

    static class OppoDateFormatSymbols implements Serializable, Cloneable {
        private static final long serialVersionUID = -5987973545549424702L;
        String[] ampms;
        transient boolean customZoneStrings;
        String[] eras;
        private String localPatternChars;
        final transient Locale locale;
        transient LocaleData localeData;
        String[] months;
        String[] shortMonths;
        String[] shortWeekdays;
        String[] weekdays;
        String[][] zoneStrings;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.<init>(java.util.Locale):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public OppoDateFormatSymbols(java.util.Locale r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.<init>(java.util.Locale):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.<init>(java.util.Locale):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.clone2dStringArray(java.lang.String[][]):java.lang.String[][], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static java.lang.String[][] clone2dStringArray(java.lang.String[][] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.clone2dStringArray(java.lang.String[][]):java.lang.String[][], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.clone2dStringArray(java.lang.String[][]):java.lang.String[][]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.readObject(java.io.ObjectInputStream):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.readObject(java.io.ObjectInputStream):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.readObject(java.io.ObjectInputStream):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private void readObject(java.io.ObjectInputStream r1) throws java.io.IOException, java.lang.ClassNotFoundException {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.readObject(java.io.ObjectInputStream):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.readObject(java.io.ObjectInputStream):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.readObject(java.io.ObjectInputStream):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.timeZoneStringsEqual(com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols, com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private static boolean timeZoneStringsEqual(com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols r1, com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.timeZoneStringsEqual(com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols, com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.timeZoneStringsEqual(com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols, com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.writeObject(java.io.ObjectOutputStream):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void writeObject(java.io.ObjectOutputStream r1) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.writeObject(java.io.ObjectOutputStream):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.writeObject(java.io.ObjectOutputStream):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.equals(java.lang.Object):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public boolean equals(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.equals(java.lang.Object):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.equals(java.lang.Object):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getAmPmStrings():java.lang.String[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String[] getAmPmStrings() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getAmPmStrings():java.lang.String[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getAmPmStrings():java.lang.String[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getEras():java.lang.String[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String[] getEras() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getEras():java.lang.String[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getEras():java.lang.String[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getLocalPatternChars():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String getLocalPatternChars() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getLocalPatternChars():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getLocalPatternChars():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getMonths():java.lang.String[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String[] getMonths() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getMonths():java.lang.String[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getMonths():java.lang.String[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getShortMonths():java.lang.String[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String[] getShortMonths() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getShortMonths():java.lang.String[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getShortMonths():java.lang.String[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getShortWeekdays():java.lang.String[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String[] getShortWeekdays() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getShortWeekdays():java.lang.String[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getShortWeekdays():java.lang.String[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getWeekdays():java.lang.String[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String[] getWeekdays() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getWeekdays():java.lang.String[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getWeekdays():java.lang.String[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getZoneStrings():java.lang.String[][], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public java.lang.String[][] getZoneStrings() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getZoneStrings():java.lang.String[][], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.getZoneStrings():java.lang.String[][]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.hashCode():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public int hashCode() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.hashCode():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.hashCode():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.internalZoneStrings():java.lang.String[][], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        synchronized java.lang.String[][] internalZoneStrings() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.internalZoneStrings():java.lang.String[][], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.internalZoneStrings():java.lang.String[][]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setAmPmStrings(java.lang.String[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void setAmPmStrings(java.lang.String[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setAmPmStrings(java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setAmPmStrings(java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setEras(java.lang.String[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void setEras(java.lang.String[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setEras(java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setEras(java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setLocalPatternChars(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void setLocalPatternChars(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setLocalPatternChars(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setLocalPatternChars(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setMonths(java.lang.String[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void setMonths(java.lang.String[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setMonths(java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setMonths(java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setShortMonths(java.lang.String[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void setShortMonths(java.lang.String[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setShortMonths(java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setShortMonths(java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setShortWeekdays(java.lang.String[]):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setShortWeekdays(java.lang.String[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setShortWeekdays(java.lang.String[]):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void setShortWeekdays(java.lang.String[] r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setShortWeekdays(java.lang.String[]):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setShortWeekdays(java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setShortWeekdays(java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setWeekdays(java.lang.String[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void setWeekdays(java.lang.String[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setWeekdays(java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setWeekdays(java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setZoneStrings(java.lang.String[][]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void setZoneStrings(java.lang.String[][] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setZoneStrings(java.lang.String[][]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.setZoneStrings(java.lang.String[][]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.toString():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols.toString():java.lang.String");
        }

        public OppoDateFormatSymbols() {
            this(Locale.getDefault());
        }

        public static final OppoDateFormatSymbols getInstance() {
            return getInstance(Locale.getDefault());
        }

        public static final OppoDateFormatSymbols getInstance(Locale locale) {
            if (locale != null) {
                return new OppoDateFormatSymbols(locale);
            }
            throw new NullPointerException("locale == null");
        }

        public static Locale[] getAvailableLocales() {
            return ICU.getAvailableDateFormatSymbolsLocales();
        }

        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    public static class OppoDateUtils {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateUtils.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public OppoDateUtils() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateUtils.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateUtils.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateUtils.formatDateRange(android.content.Context, long, long, int):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static java.lang.String formatDateRange(android.content.Context r1, long r2, long r4, int r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateUtils.formatDateRange(android.content.Context, long, long, int):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateUtils.formatDateRange(android.content.Context, long, long, int):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateUtils.formatDateRange(android.content.Context, java.util.Formatter, long, long, int, java.lang.String):java.util.Formatter, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public static java.util.Formatter formatDateRange(android.content.Context r1, java.util.Formatter r2, long r3, long r5, int r7, java.lang.String r8) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateUtils.formatDateRange(android.content.Context, java.util.Formatter, long, long, int, java.lang.String):java.util.Formatter, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateUtils.formatDateRange(android.content.Context, java.util.Formatter, long, long, int, java.lang.String):java.util.Formatter");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateUtils.toThailandDate(java.lang.String):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static java.lang.String toThailandDate(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoDateUtils.toThailandDate(java.lang.String):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoDateUtils.toThailandDate(java.lang.String):java.lang.String");
        }

        public static String formatDateTime(Context context, long millis, int flags) {
            return formatDateRange(context, millis, millis, flags);
        }

        public static Formatter formatDateRange(Context context, Formatter formatter, long startMillis, long endMillis, int flags) {
            return formatDateRange(context, formatter, startMillis, endMillis, flags, null);
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static class OppoJavaSimpleDateFormat extends DateFormat {
        static final String PATTERN_CHARS = "GyMdkHmsSEDFwWahKzZLc";
        private static final int RFC_822_TIMEZONE_FIELD = 18;
        private static final int STAND_ALONE_DAY_OF_WEEK_FIELD = 20;
        private static final int STAND_ALONE_MONTH_FIELD = 19;
        private static final ObjectStreamField[] serialPersistentFields = null;
        private static final long serialVersionUID = 4774881970558875024L;
        private transient int creationYear;
        private Date defaultCenturyStart;
        private OppoDateFormatSymbols formatData;
        private String pattern;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>():void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>():void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public OppoJavaSimpleDateFormat() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>():void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public OppoJavaSimpleDateFormat(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String, com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String, com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String, com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public OppoJavaSimpleDateFormat(java.lang.String r1, com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String, com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String, com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String, com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String, java.util.Locale):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String, java.util.Locale):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String, java.util.Locale):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public OppoJavaSimpleDateFormat(java.lang.String r1, java.util.Locale r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String, java.util.Locale):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String, java.util.Locale):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.lang.String, java.util.Locale):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.util.Locale):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private OppoJavaSimpleDateFormat(java.util.Locale r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.util.Locale):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.<init>(java.util.Locale):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.append(java.lang.StringBuffer, java.text.FieldPosition, java.util.List, char, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void append(java.lang.StringBuffer r1, java.text.FieldPosition r2, java.util.List<java.text.FieldPosition> r3, char r4, int r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.append(java.lang.StringBuffer, java.text.FieldPosition, java.util.List, char, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.append(java.lang.StringBuffer, java.text.FieldPosition, java.util.List, char, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendDayOfWeek(java.lang.StringBuffer, int, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void appendDayOfWeek(java.lang.StringBuffer r1, int r2, boolean r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendDayOfWeek(java.lang.StringBuffer, int, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendDayOfWeek(java.lang.StringBuffer, int, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendMonth(java.lang.StringBuffer, int, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void appendMonth(java.lang.StringBuffer r1, int r2, boolean r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendMonth(java.lang.StringBuffer, int, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendMonth(java.lang.StringBuffer, int, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendNumber(java.lang.StringBuffer, int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void appendNumber(java.lang.StringBuffer r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendNumber(java.lang.StringBuffer, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendNumber(java.lang.StringBuffer, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendNumericTimeZone(java.lang.StringBuffer, int, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void appendNumericTimeZone(java.lang.StringBuffer r1, int r2, boolean r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendNumericTimeZone(java.lang.StringBuffer, int, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendNumericTimeZone(java.lang.StringBuffer, int, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendTimeZone(java.lang.StringBuffer, int, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void appendTimeZone(java.lang.StringBuffer r1, int r2, boolean r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendTimeZone(java.lang.StringBuffer, int, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.appendTimeZone(java.lang.StringBuffer, int, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.convertPattern(java.lang.String, java.lang.String, java.lang.String, boolean):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static java.lang.String convertPattern(java.lang.String r1, java.lang.String r2, java.lang.String r3, boolean r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.convertPattern(java.lang.String, java.lang.String, java.lang.String, boolean):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.convertPattern(java.lang.String, java.lang.String, java.lang.String, boolean):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.defaultPattern():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private static java.lang.String defaultPattern() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.defaultPattern():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.defaultPattern():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.error(java.text.ParsePosition, int, java.util.TimeZone):java.util.Date, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private java.util.Date error(java.text.ParsePosition r1, int r2, java.util.TimeZone r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.error(java.text.ParsePosition, int, java.util.TimeZone):java.util.Date, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.error(java.text.ParsePosition, int, java.util.TimeZone):java.util.Date");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.formatImpl(java.util.Date, java.lang.StringBuffer, java.text.FieldPosition, java.util.List):java.lang.StringBuffer, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private java.lang.StringBuffer formatImpl(java.util.Date r1, java.lang.StringBuffer r2, java.text.FieldPosition r3, java.util.List<java.text.FieldPosition> r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.formatImpl(java.util.Date, java.lang.StringBuffer, java.text.FieldPosition, java.util.List):java.lang.StringBuffer, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.formatImpl(java.util.Date, java.lang.StringBuffer, java.text.FieldPosition, java.util.List):java.lang.StringBuffer");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.formatToCharacterIteratorImpl(java.util.Date):java.text.AttributedCharacterIterator, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private java.text.AttributedCharacterIterator formatToCharacterIteratorImpl(java.util.Date r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.formatToCharacterIteratorImpl(java.util.Date):java.text.AttributedCharacterIterator, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.formatToCharacterIteratorImpl(java.util.Date):java.text.AttributedCharacterIterator");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parse(java.lang.String, int, char, int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private int parse(java.lang.String r1, int r2, char r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parse(java.lang.String, int, char, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parse(java.lang.String, int, char, int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseDayOfWeek(java.lang.String, int, boolean):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private int parseDayOfWeek(java.lang.String r1, int r2, boolean r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseDayOfWeek(java.lang.String, int, boolean):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseDayOfWeek(java.lang.String, int, boolean):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseMonth(java.lang.String, int, int, int, boolean):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private int parseMonth(java.lang.String r1, int r2, int r3, int r4, boolean r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseMonth(java.lang.String, int, int, int, boolean):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseMonth(java.lang.String, int, int, int, boolean):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseNumber(int, java.lang.String, int, int, int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private int parseNumber(int r1, java.lang.String r2, int r3, int r4, int r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseNumber(int, java.lang.String, int, int, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseNumber(int, java.lang.String, int, int, int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseNumber(int, java.lang.String, java.text.ParsePosition):java.lang.Number, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private java.lang.Number parseNumber(int r1, java.lang.String r2, java.text.ParsePosition r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseNumber(int, java.lang.String, java.text.ParsePosition):java.lang.Number, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseNumber(int, java.lang.String, java.text.ParsePosition):java.lang.Number");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseText(java.lang.String, int, java.lang.String[], int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private int parseText(java.lang.String r1, int r2, java.lang.String[] r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseText(java.lang.String, int, java.lang.String[], int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseText(java.lang.String, int, java.lang.String[], int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseTimeZone(java.lang.String, int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private int parseTimeZone(java.lang.String r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseTimeZone(java.lang.String, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parseTimeZone(java.lang.String, int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.readObject(java.io.ObjectInputStream):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void readObject(java.io.ObjectInputStream r1) throws java.io.IOException, java.lang.ClassNotFoundException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.readObject(java.io.ObjectInputStream):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.readObject(java.io.ObjectInputStream):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.validateFormat(char):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void validateFormat(char r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.validateFormat(char):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.validateFormat(char):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.validatePattern(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void validatePattern(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.validatePattern(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.validatePattern(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.writeObject(java.io.ObjectOutputStream):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void writeObject(java.io.ObjectOutputStream r1) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.writeObject(java.io.ObjectOutputStream):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.writeObject(java.io.ObjectOutputStream):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.applyLocalizedPattern(java.lang.String):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.applyLocalizedPattern(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.applyLocalizedPattern(java.lang.String):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void applyLocalizedPattern(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.applyLocalizedPattern(java.lang.String):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.applyLocalizedPattern(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.applyLocalizedPattern(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.applyPattern(java.lang.String):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.applyPattern(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.applyPattern(java.lang.String):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void applyPattern(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.applyPattern(java.lang.String):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.applyPattern(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.applyPattern(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.clone():java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.Object clone() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.clone():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.clone():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.equals(java.lang.Object):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public boolean equals(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.equals(java.lang.Object):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.equals(java.lang.Object):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.formatToCharacterIterator(java.lang.Object):java.text.AttributedCharacterIterator, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public java.text.AttributedCharacterIterator formatToCharacterIterator(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.formatToCharacterIterator(java.lang.Object):java.text.AttributedCharacterIterator, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.formatToCharacterIterator(java.lang.Object):java.text.AttributedCharacterIterator");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.get2DigitYearStart():java.util.Date, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.util.Date get2DigitYearStart() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.get2DigitYearStart():java.util.Date, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.get2DigitYearStart():java.util.Date");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.getDateFormatSymbols():com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols getDateFormatSymbols() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.getDateFormatSymbols():com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.getDateFormatSymbols():com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.hashCode():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public int hashCode() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.hashCode():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.hashCode():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parse(java.lang.String, java.text.ParsePosition):java.util.Date, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public java.util.Date parse(java.lang.String r1, java.text.ParsePosition r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parse(java.lang.String, java.text.ParsePosition):java.util.Date, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.parse(java.lang.String, java.text.ParsePosition):java.util.Date");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.set2DigitYearStart(java.util.Date):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void set2DigitYearStart(java.util.Date r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.set2DigitYearStart(java.util.Date):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.set2DigitYearStart(java.util.Date):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.setDateFormatSymbols(com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.setDateFormatSymbols(com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.setDateFormatSymbols(com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void setDateFormatSymbols(com.oppo.util.OppoThailandCalendarUtil.OppoDateFormatSymbols r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.setDateFormatSymbols(com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):void, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.setDateFormatSymbols(com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.setDateFormatSymbols(com.oppo.util.OppoThailandCalendarUtil$OppoDateFormatSymbols):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.toLocalizedPattern():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String toLocalizedPattern() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.toLocalizedPattern():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.toLocalizedPattern():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.toPattern():java.lang.String, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.toPattern():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.toPattern():java.lang.String, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public java.lang.String toPattern() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.toPattern():java.lang.String, dex:  in method: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.toPattern():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.OppoJavaSimpleDateFormat.toPattern():java.lang.String");
        }

        public StringBuffer format(Date date, StringBuffer buffer, FieldPosition fieldPos) {
            return formatImpl(date, buffer, fieldPos, null);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.<init>():void, dex: 
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
    public OppoThailandCalendarUtil() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoThailandCalendarUtil.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.isThaiCalendarEnabled():boolean, dex: 
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
    private static boolean isThaiCalendarEnabled() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoThailandCalendarUtil.isThaiCalendarEnabled():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoThailandCalendarUtil.isThaiCalendarEnabled():boolean");
    }
}
