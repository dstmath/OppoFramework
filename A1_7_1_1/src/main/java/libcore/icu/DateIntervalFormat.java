package libcore.icu;

import android.icu.util.Calendar;
import android.icu.util.ULocale;
import java.text.FieldPosition;
import java.util.TimeZone;
import libcore.util.BasicLruCache;

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
public final class DateIntervalFormat {
    private static final BasicLruCache<String, android.icu.text.DateIntervalFormat> CACHED_FORMATTERS = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: libcore.icu.DateIntervalFormat.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: libcore.icu.DateIntervalFormat.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.icu.DateIntervalFormat.<clinit>():void");
    }

    private DateIntervalFormat() {
    }

    public static String formatDateRange(long startMs, long endMs, int flags, String olsonId) {
        if ((flags & 8192) != 0) {
            olsonId = "UTC";
        }
        return formatDateRange(ULocale.getDefault(), DateUtilsBridge.icuTimeZone(olsonId != null ? TimeZone.getTimeZone(olsonId) : TimeZone.getDefault()), startMs, endMs, flags);
    }

    public static String formatDateRange(ULocale icuLocale, android.icu.util.TimeZone icuTimeZone, long startMs, long endMs, int flags) {
        Calendar endCalendar;
        String stringBuffer;
        Calendar startCalendar = DateUtilsBridge.createIcuCalendar(icuTimeZone, icuLocale, startMs);
        if (startMs == endMs) {
            endCalendar = startCalendar;
        } else {
            endCalendar = DateUtilsBridge.createIcuCalendar(icuTimeZone, icuLocale, endMs);
        }
        boolean endsAtMidnight = isMidnight(endCalendar);
        if (startMs != endMs && endsAtMidnight && ((flags & 1) == 0 || DateUtilsBridge.dayDistance(startCalendar, endCalendar) <= 1)) {
            endCalendar.add(5, -1);
        }
        String skeleton = DateUtilsBridge.toSkeleton(startCalendar, endCalendar, flags);
        synchronized (CACHED_FORMATTERS) {
            stringBuffer = getFormatter(skeleton, icuLocale, icuTimeZone).format(startCalendar, endCalendar, new StringBuffer(), new FieldPosition(0)).toString();
        }
        return stringBuffer;
    }

    private static android.icu.text.DateIntervalFormat getFormatter(String skeleton, ULocale locale, android.icu.util.TimeZone icuTimeZone) {
        String key = skeleton + "\t" + locale + "\t" + icuTimeZone;
        android.icu.text.DateIntervalFormat formatter = (android.icu.text.DateIntervalFormat) CACHED_FORMATTERS.get(key);
        if (formatter != null) {
            return formatter;
        }
        formatter = android.icu.text.DateIntervalFormat.getInstance(skeleton, locale);
        formatter.setTimeZone(icuTimeZone);
        CACHED_FORMATTERS.put(key, formatter);
        return formatter;
    }

    private static boolean isMidnight(Calendar c) {
        if (c.get(11) == 0 && c.get(12) == 0 && c.get(13) == 0 && c.get(14) == 0) {
            return true;
        }
        return false;
    }
}
