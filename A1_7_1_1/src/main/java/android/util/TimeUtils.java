package android.util;

import android.os.SystemClock;
import android.text.format.DateFormat;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import libcore.util.ZoneInfoDB;
import oppo.util.OppoMultiLauncherUtil;

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
public class TimeUtils {
    private static final boolean DBG = false;
    public static final int HUNDRED_DAY_FIELD_LEN = 19;
    public static final long NANOS_PER_MS = 1000000;
    private static final int SECONDS_PER_DAY = 86400;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final String TAG = "TimeUtils";
    private static char[] sFormatStr;
    private static final Object sFormatSync = null;
    private static String sLastCountry;
    private static final Object sLastLockObj = null;
    private static String sLastUniqueCountry;
    private static final Object sLastUniqueLockObj = null;
    private static ArrayList<TimeZone> sLastUniqueZoneOffsets;
    private static ArrayList<TimeZone> sLastZones;
    private static SimpleDateFormat sLoggingFormat;
    private static char[] sTmpFormatStr;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.util.TimeUtils.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.util.TimeUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.TimeUtils.<clinit>():void");
    }

    public static TimeZone getTimeZone(int offset, boolean dst, long when, String country) {
        TimeZone best = null;
        Date d = new Date(when);
        TimeZone current = TimeZone.getDefault();
        String currentName = current.getID();
        int currentOffset = current.getOffset(when);
        boolean currentDst = current.inDaylightTime(d);
        for (TimeZone tz : getTimeZones(country)) {
            if (tz.getID().equals(currentName) && currentOffset == offset && currentDst == dst) {
                return current;
            }
            if (best == null && tz.getOffset(when) == offset && tz.inDaylightTime(d) == dst) {
                best = tz;
            }
        }
        return best;
    }

    /* JADX WARNING: Missing block: B:10:0x0012, code:
            r5 = getTimeZones(r8);
            r2 = new java.util.ArrayList();
            r4 = r5.iterator();
     */
    /* JADX WARNING: Missing block: B:12:0x0023, code:
            if (r4.hasNext() == false) goto L_0x0050;
     */
    /* JADX WARNING: Missing block: B:13:0x0025, code:
            r3 = (java.util.TimeZone) r4.next();
            r0 = false;
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:15:0x0031, code:
            if (r1 >= r2.size()) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:17:0x0041, code:
            if (((java.util.TimeZone) r2.get(r1)).getRawOffset() != r3.getRawOffset()) goto L_0x004d;
     */
    /* JADX WARNING: Missing block: B:18:0x0043, code:
            r0 = true;
     */
    /* JADX WARNING: Missing block: B:19:0x0044, code:
            if (r0 != false) goto L_0x001f;
     */
    /* JADX WARNING: Missing block: B:20:0x0046, code:
            r2.add(r3);
     */
    /* JADX WARNING: Missing block: B:24:0x004d, code:
            r1 = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:25:0x0050, code:
            r7 = sLastUniqueLockObj;
     */
    /* JADX WARNING: Missing block: B:26:0x0052, code:
            monitor-enter(r7);
     */
    /* JADX WARNING: Missing block: B:28:?, code:
            sLastUniqueZoneOffsets = r2;
            sLastUniqueCountry = r8;
            r6 = sLastUniqueZoneOffsets;
     */
    /* JADX WARNING: Missing block: B:29:0x0059, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:30:0x005a, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ArrayList<TimeZone> getTimeZonesWithUniqueOffsets(String country) {
        synchronized (sLastUniqueLockObj) {
            if (country != null) {
                if (country.equals(sLastUniqueCountry)) {
                    ArrayList<TimeZone> arrayList = sLastUniqueZoneOffsets;
                    return arrayList;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0012, code:
            r7 = new java.util.ArrayList();
     */
    /* JADX WARNING: Missing block: B:11:0x0017, code:
            if (r12 != null) goto L_0x001d;
     */
    /* JADX WARNING: Missing block: B:12:0x0019, code:
            return r7;
     */
    /* JADX WARNING: Missing block: B:16:0x001d, code:
            r4 = android.content.res.Resources.getSystem().getXml(com.android.internal.R.xml.time_zones_by_country);
     */
    /* JADX WARNING: Missing block: B:18:?, code:
            com.android.internal.util.XmlUtils.beginDocument(r4, "timezones");
     */
    /* JADX WARNING: Missing block: B:19:0x002e, code:
            com.android.internal.util.XmlUtils.nextElement(r4);
            r3 = r4.getName();
     */
    /* JADX WARNING: Missing block: B:20:0x0035, code:
            if (r3 == null) goto L_0x009e;
     */
    /* JADX WARNING: Missing block: B:22:0x003e, code:
            if (r3.equals("timezone") == false) goto L_0x009e;
     */
    /* JADX WARNING: Missing block: B:24:0x004c, code:
            if (r12.equals(r4.getAttributeValue(null, "code")) == false) goto L_0x002e;
     */
    /* JADX WARNING: Missing block: B:26:0x0053, code:
            if (r4.next() != 4) goto L_0x002e;
     */
    /* JADX WARNING: Missing block: B:27:0x0055, code:
            r6 = java.util.TimeZone.getTimeZone(r4.getText());
     */
    /* JADX WARNING: Missing block: B:28:0x0068, code:
            if (r6.getID().startsWith("GMT") != false) goto L_0x002e;
     */
    /* JADX WARNING: Missing block: B:29:0x006a, code:
            r7.add(r6);
     */
    /* JADX WARNING: Missing block: B:31:0x006e, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            android.util.Log.e(TAG, "Got xml parser exception getTimeZone('" + r12 + "'): e=", r2);
     */
    /* JADX WARNING: Missing block: B:34:0x0090, code:
            r4.close();
     */
    /* JADX WARNING: Missing block: B:41:0x009e, code:
            r4.close();
     */
    /* JADX WARNING: Missing block: B:42:0x00a2, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:44:?, code:
            android.util.Log.e(TAG, "Got IO exception getTimeZone('" + r12 + "'): e=", r1);
     */
    /* JADX WARNING: Missing block: B:45:0x00c4, code:
            r4.close();
     */
    /* JADX WARNING: Missing block: B:46:0x00c8, code:
            r9 = move-exception;
     */
    /* JADX WARNING: Missing block: B:47:0x00c9, code:
            r4.close();
     */
    /* JADX WARNING: Missing block: B:48:0x00cc, code:
            throw r9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ArrayList<TimeZone> getTimeZones(String country) {
        ArrayList<TimeZone> arrayList;
        synchronized (sLastLockObj) {
            if (country != null) {
                try {
                    if (country.equals(sLastCountry)) {
                        arrayList = sLastZones;
                        return arrayList;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
        synchronized (sLastLockObj) {
            try {
                sLastZones = tzs;
                sLastCountry = country;
                arrayList = sLastZones;
            } catch (Throwable th2) {
                throw th2;
            }
        }
        return arrayList;
    }

    public static String getTimeZoneDatabaseVersion() {
        return ZoneInfoDB.getInstance().getVersion();
    }

    private static int accumField(int amt, int suffix, boolean always, int zeropad) {
        if (amt > OppoMultiLauncherUtil.USER_ID) {
            int num = 0;
            while (amt != 0) {
                num++;
                amt /= 10;
            }
            return num + suffix;
        } else if (amt > 99 || (always && zeropad >= 3)) {
            return suffix + 3;
        } else {
            if (amt > 9 || (always && zeropad >= 2)) {
                return suffix + 2;
            }
            if (always || amt > 0) {
                return suffix + 1;
            }
            return 0;
        }
    }

    private static int printFieldLocked(char[] formatStr, int amt, char suffix, int pos, boolean always, int zeropad) {
        if (!always && amt <= 0) {
            return pos;
        }
        int startPos = pos;
        if (amt > OppoMultiLauncherUtil.USER_ID) {
            int tmp = 0;
            while (amt != 0 && tmp < sTmpFormatStr.length) {
                sTmpFormatStr[tmp] = (char) ((amt % 10) + 48);
                tmp++;
                amt /= 10;
            }
            for (tmp--; tmp >= 0; tmp--) {
                formatStr[pos] = sTmpFormatStr[tmp];
                pos++;
            }
        } else {
            int dig;
            if ((always && zeropad >= 3) || amt > 99) {
                dig = amt / 100;
                formatStr[pos] = (char) (dig + 48);
                pos++;
                amt -= dig * 100;
            }
            if ((always && zeropad >= 2) || amt > 9 || startPos != pos) {
                dig = amt / 10;
                formatStr[pos] = (char) (dig + 48);
                pos++;
                amt -= dig * 10;
            }
            formatStr[pos] = (char) (amt + 48);
            pos++;
        }
        formatStr[pos] = suffix;
        return pos + 1;
    }

    private static int formatDurationLocked(long duration, int fieldLen) {
        if (sFormatStr.length < fieldLen) {
            sFormatStr = new char[fieldLen];
        }
        char[] formatStr = sFormatStr;
        int pos;
        if (duration == 0) {
            pos = 0;
            fieldLen--;
            while (true) {
                int pos2 = pos;
                if (pos2 < fieldLen) {
                    pos = pos2 + 1;
                    formatStr[pos2] = ' ';
                } else {
                    formatStr[pos2] = '0';
                    return pos2 + 1;
                }
            }
        }
        char prefix;
        if (duration > 0) {
            prefix = '+';
        } else {
            prefix = '-';
            duration = -duration;
        }
        int millis = (int) (duration % 1000);
        int seconds = (int) Math.floor((double) (duration / 1000));
        int days = 0;
        int hours = 0;
        int minutes = 0;
        if (seconds >= SECONDS_PER_DAY) {
            days = seconds / SECONDS_PER_DAY;
            seconds -= SECONDS_PER_DAY * days;
        }
        if (seconds >= SECONDS_PER_HOUR) {
            hours = seconds / SECONDS_PER_HOUR;
            seconds -= hours * SECONDS_PER_HOUR;
        }
        if (seconds >= 60) {
            minutes = seconds / 60;
            seconds -= minutes * 60;
        }
        pos = 0;
        if (fieldLen != 0) {
            int myLen = accumField(days, 1, false, 0);
            myLen += accumField(hours, 1, myLen > 0, 2);
            myLen += accumField(minutes, 1, myLen > 0, 2);
            myLen += accumField(seconds, 1, myLen > 0, 2);
            for (myLen += accumField(millis, 2, true, myLen > 0 ? 3 : 0) + 1; myLen < fieldLen; myLen++) {
                formatStr[pos] = ' ';
                pos++;
            }
        }
        formatStr[pos] = prefix;
        pos++;
        int start = pos;
        boolean zeropad = fieldLen != 0;
        pos = printFieldLocked(formatStr, days, DateFormat.DATE, pos, false, 0);
        pos = printFieldLocked(formatStr, hours, DateFormat.HOUR, pos, pos != start, zeropad ? 2 : 0);
        pos = printFieldLocked(formatStr, minutes, DateFormat.MINUTE, pos, pos != start, zeropad ? 2 : 0);
        pos = printFieldLocked(formatStr, seconds, DateFormat.SECONDS, pos, pos != start, zeropad ? 2 : 0);
        int i = (!zeropad || pos == start) ? 0 : 3;
        pos = printFieldLocked(formatStr, millis, DateFormat.MINUTE, pos, true, i);
        formatStr[pos] = DateFormat.SECONDS;
        return pos + 1;
    }

    public static void formatDuration(long duration, StringBuilder builder) {
        synchronized (sFormatSync) {
            builder.append(sFormatStr, 0, formatDurationLocked(duration, 0));
        }
    }

    public static void formatDuration(long duration, PrintWriter pw, int fieldLen) {
        synchronized (sFormatSync) {
            pw.print(new String(sFormatStr, 0, formatDurationLocked(duration, fieldLen)));
        }
    }

    public static void formatDuration(long duration, PrintWriter pw) {
        formatDuration(duration, pw, 0);
    }

    public static void formatDuration(long time, long now, PrintWriter pw) {
        if (time == 0) {
            pw.print("--");
        } else {
            formatDuration(time - now, pw, 0);
        }
    }

    public static String formatUptime(long time) {
        long diff = time - SystemClock.uptimeMillis();
        if (diff > 0) {
            return time + " (in " + diff + " ms)";
        }
        if (diff < 0) {
            return time + " (" + (-diff) + " ms ago)";
        }
        return time + " (now)";
    }

    public static String logTimeOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        if (millis < 0) {
            return Long.toString(millis);
        }
        c.setTimeInMillis(millis);
        Object[] objArr = new Object[6];
        objArr[0] = c;
        objArr[1] = c;
        objArr[2] = c;
        objArr[3] = c;
        objArr[4] = c;
        objArr[5] = c;
        return String.format("%tm-%td %tH:%tM:%tS.%tL", objArr);
    }

    public static String formatForLogging(long millis) {
        if (millis <= 0) {
            return "unknown";
        }
        return sLoggingFormat.format(new Date(millis));
    }
}
