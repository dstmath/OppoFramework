package android.util;

import android.os.SystemClock;
import android.text.format.DateFormat;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.telephony.SmsConstants;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import libcore.util.TimeZoneFinder;
import libcore.util.ZoneInfoDB;

public class TimeUtils {
    private static final boolean DBG = false;
    public static final int HUNDRED_DAY_FIELD_LEN = 19;
    public static final long NANOS_PER_MS = 1000000;
    private static final int SECONDS_PER_DAY = 86400;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final String TAG = "TimeUtils";
    private static char[] sFormatStr = new char[29];
    private static final Object sFormatSync = new Object();
    private static String sLastUniqueCountry = null;
    private static final Object sLastUniqueLockObj = new Object();
    private static List<String> sLastUniqueZoneOffsets = null;
    private static SimpleDateFormat sLoggingFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static char[] sTmpFormatStr = new char[29];

    public static TimeZone getTimeZone(int offset, boolean dst, long when, String country) {
        android.icu.util.TimeZone icuTimeZone = getIcuTimeZone(offset, dst, when, country);
        if (icuTimeZone != null) {
            return TimeZone.getTimeZone(icuTimeZone.getID());
        }
        return null;
    }

    private static android.icu.util.TimeZone getIcuTimeZone(int offset, boolean dst, long when, String country) {
        if (country == null) {
            return null;
        }
        return TimeZoneFinder.getInstance().lookupTimeZoneByCountryAndOffset(country, offset, dst, when, android.icu.util.TimeZone.getDefault());
    }

    /* JADX WARNING: Missing block: B:10:0x0012, code:
            r5 = getIcuTimeZones(r8);
            r2 = new java.util.ArrayList();
            r4 = r5.iterator();
     */
    /* JADX WARNING: Missing block: B:12:0x0023, code:
            if (r4.hasNext() == false) goto L_0x0050;
     */
    /* JADX WARNING: Missing block: B:13:0x0025, code:
            r3 = (android.icu.util.TimeZone) r4.next();
            r0 = false;
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:15:0x0031, code:
            if (r1 >= r2.size()) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:17:0x0041, code:
            if (((android.icu.util.TimeZone) r2.get(r1)).getRawOffset() != r3.getRawOffset()) goto L_0x004d;
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
            sLastUniqueZoneOffsets = extractZoneIds(r2);
            sLastUniqueCountry = r8;
            r6 = sLastUniqueZoneOffsets;
     */
    /* JADX WARNING: Missing block: B:29:0x005d, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:30:0x005e, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<String> getTimeZoneIdsWithUniqueOffsets(String country) {
        synchronized (sLastUniqueLockObj) {
            if (country != null) {
                if (country.equals(sLastUniqueCountry)) {
                    List<String> list = sLastUniqueZoneOffsets;
                    return list;
                }
            }
        }
    }

    private static List<String> extractZoneIds(List<android.icu.util.TimeZone> timeZones) {
        List<String> ids = new ArrayList(timeZones.size());
        for (android.icu.util.TimeZone timeZone : timeZones) {
            ids.add(timeZone.getID());
        }
        return Collections.unmodifiableList(ids);
    }

    private static List<android.icu.util.TimeZone> getIcuTimeZones(String countryIso) {
        if (countryIso == null) {
            return Collections.emptyList();
        }
        List<android.icu.util.TimeZone> timeZones = TimeZoneFinder.getInstance().lookupTimeZonesByCountry(countryIso);
        if (timeZones == null) {
            return Collections.emptyList();
        }
        return timeZones;
    }

    public static String getTimeZoneDatabaseVersion() {
        return ZoneInfoDB.getInstance().getVersion();
    }

    private static int accumField(int amt, int suffix, boolean always, int zeropad) {
        if (amt > MetricsEvent.ASSIST_GESTURE_TRIGGERED) {
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
        if (amt > MetricsEvent.ASSIST_GESTURE_TRIGGERED) {
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
        return String.format("%tm-%td %tH:%tM:%tS.%tL", new Object[]{c, c, c, c, c, c});
    }

    public static String formatForLogging(long millis) {
        if (millis <= 0) {
            return SmsConstants.FORMAT_UNKNOWN;
        }
        return sLoggingFormat.format(new Date(millis));
    }
}
