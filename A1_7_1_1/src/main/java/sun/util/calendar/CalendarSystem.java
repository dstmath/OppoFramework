package sun.util.calendar;

import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentMap;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class CalendarSystem {
    private static final Gregorian GREGORIAN_INSTANCE = null;
    private static final ConcurrentMap<String, CalendarSystem> calendars = null;
    private static final Map<String, Class<?>> names = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.util.calendar.CalendarSystem.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.util.calendar.CalendarSystem.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.CalendarSystem.<clinit>():void");
    }

    public abstract CalendarDate getCalendarDate();

    public abstract CalendarDate getCalendarDate(long j);

    public abstract CalendarDate getCalendarDate(long j, TimeZone timeZone);

    public abstract CalendarDate getCalendarDate(long j, CalendarDate calendarDate);

    public abstract Era getEra(String str);

    public abstract Era[] getEras();

    public abstract int getMonthLength(CalendarDate calendarDate);

    public abstract String getName();

    public abstract CalendarDate getNthDayOfWeek(int i, int i2, CalendarDate calendarDate);

    public abstract long getTime(CalendarDate calendarDate);

    public abstract int getWeekLength();

    public abstract int getYearLength(CalendarDate calendarDate);

    public abstract int getYearLengthInMonths(CalendarDate calendarDate);

    public abstract CalendarDate newCalendarDate();

    public abstract CalendarDate newCalendarDate(TimeZone timeZone);

    public abstract boolean normalize(CalendarDate calendarDate);

    public abstract void setEra(CalendarDate calendarDate, String str);

    public abstract CalendarDate setTimeOfDay(CalendarDate calendarDate, int i);

    public abstract boolean validate(CalendarDate calendarDate);

    public static Gregorian getGregorianCalendar() {
        return GREGORIAN_INSTANCE;
    }

    public static CalendarSystem forName(String calendarName) {
        if ("gregorian".equals(calendarName)) {
            return GREGORIAN_INSTANCE;
        }
        CalendarSystem cal = (CalendarSystem) calendars.get(calendarName);
        if (cal != null) {
            return cal;
        }
        Class<?> calendarClass = (Class) names.get(calendarName);
        if (calendarClass == null) {
            return null;
        }
        if (calendarClass.isAssignableFrom(LocalGregorianCalendar.class)) {
            cal = LocalGregorianCalendar.getLocalGregorianCalendar(calendarName);
        } else {
            try {
                cal = (CalendarSystem) calendarClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("internal error", e);
            }
        }
        if (cal == null) {
            return null;
        }
        CalendarSystem cs = (CalendarSystem) calendars.putIfAbsent(calendarName, cal);
        if (cs != null) {
            cal = cs;
        }
        return cal;
    }
}
