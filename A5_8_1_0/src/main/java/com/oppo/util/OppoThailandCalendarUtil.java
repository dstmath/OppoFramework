package com.oppo.util;

import android.content.Context;
import android.provider.Settings.System;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import com.color.util.ColorDialogUtil;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format.Field;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.icu.DateIntervalFormat;
import libcore.icu.ICU;
import libcore.icu.LocaleData;
import libcore.icu.TimeZoneNames;

public class OppoThailandCalendarUtil {
    private static final String FILE_FULL_PATH_NAME_FOR_THAI_CALENDAR_DIR = "/data/thaicalendar/";
    private static final String FILE_FULL_PATH_NAME_FOR_THAI_CALENDAR_FILE = "enable_state.properties";
    private static final String KEY_THAI_CALENDAR_ENABLE_STATE = "key_thai_calendar_enable_state";
    private static final String TAG = "OppoThailandCalendarUtil";

    public static class OppoAndroidDateFormat {
        private static boolean sIs24Hour;
        private static Locale sIs24HourLocale;
        private static final Object sLocaleLock = new Object();

        public static final DateFormat getDateFormat(Context context) {
            return getDateFormatForSetting(context, System.getString(context.getContentResolver(), "date_format"));
        }

        /* JADX WARNING: Missing block: B:13:0x002b, code:
            r1 = java.text.DateFormat.getTimeInstance(1, r0);
     */
        /* JADX WARNING: Missing block: B:14:0x0032, code:
            if ((r1 instanceof java.text.SimpleDateFormat) == false) goto L_0x005f;
     */
        /* JADX WARNING: Missing block: B:16:0x0041, code:
            if (((java.text.SimpleDateFormat) r1).toPattern().indexOf(72) < 0) goto L_0x005b;
     */
        /* JADX WARNING: Missing block: B:17:0x0043, code:
            r4 = "24";
     */
        /* JADX WARNING: Missing block: B:18:0x0046, code:
            r6 = sLocaleLock;
     */
        /* JADX WARNING: Missing block: B:19:0x0048, code:
            monitor-enter(r6);
     */
        /* JADX WARNING: Missing block: B:21:?, code:
            sIs24HourLocale = r0;
            sIs24Hour = r4.equals("24");
     */
        /* JADX WARNING: Missing block: B:22:0x0054, code:
            monitor-exit(r6);
     */
        /* JADX WARNING: Missing block: B:24:0x0057, code:
            return sIs24Hour;
     */
        /* JADX WARNING: Missing block: B:28:0x005b, code:
            r4 = "12";
     */
        /* JADX WARNING: Missing block: B:29:0x005f, code:
            r4 = "12";
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public static boolean is24HourFormat(Context context) {
            String value = System.getString(context.getContentResolver(), "time_12_24");
            if (value != null) {
                return value.equals("24");
            }
            Locale locale = context.getResources().getConfiguration().locale;
            synchronized (sLocaleLock) {
                if (sIs24HourLocale == null || !sIs24HourLocale.equals(locale)) {
                } else {
                    boolean z = sIs24Hour;
                    return z;
                }
            }
        }

        public static DateFormat getTimeFormat(Context context) {
            return new OppoJavaSimpleDateFormat(getTimeFormatString(context));
        }

        public static String getTimeFormatString(Context context) {
            LocaleData d = LocaleData.get(context.getResources().getConfiguration().locale);
            return android.text.format.DateFormat.getTimeFormatString(context);
        }

        public static DateFormat getDateFormatForSetting(Context context, String value) {
            return new OppoJavaSimpleDateFormat(getDateFormatStringForSetting(context, value));
        }

        private static String getDateFormatStringForSetting(Context context, String value) {
            if (value != null) {
                int month = value.indexOf(77);
                int day = value.indexOf(100);
                int year = value.indexOf(121);
                if (month >= 0 && day >= 0 && year >= 0) {
                    String template = context.getString(201589949);
                    if (year >= month || year >= day) {
                        if (month < day) {
                            if (day < year) {
                                value = String.format(template, new Object[]{"MM", "dd", "yyyy"});
                            } else {
                                value = String.format(template, new Object[]{"MM", "yyyy", "dd"});
                            }
                        } else if (month < year) {
                            value = String.format(template, new Object[]{"dd", "MM", "yyyy"});
                        } else {
                            value = String.format(template, new Object[]{"dd", "yyyy", "MM"});
                        }
                    } else if (month < day) {
                        value = String.format(template, new Object[]{"yyyy", "MM", "dd"});
                    } else {
                        value = String.format(template, new Object[]{"yyyy", "dd", "MM"});
                    }
                    return value;
                }
            }
            return "";
        }

        public static final CharSequence oppoFormat(CharSequence inFormat, long inTimeInMillis) {
            return oppoFormat(inFormat, new Date(inTimeInMillis));
        }

        public static final CharSequence oppoFormat(CharSequence inFormat, Date inDate) {
            Calendar c = new GregorianCalendar();
            c.setTime(inDate);
            return oppoFormat(inFormat, c);
        }

        public static CharSequence oppoFormat(CharSequence inFormat, Calendar inDate) {
            SpannableStringBuilder s = new SpannableStringBuilder(inFormat);
            LocaleData localeData = LocaleData.get(Locale.getDefault());
            int len = inFormat.length();
            int i = 0;
            while (i < len) {
                int count = 1;
                char c = s.charAt(i);
                if (c == '\'') {
                    count = appendQuotedText(s, i, len);
                    len = s.length();
                } else {
                    Object replacement;
                    while (i + count < len && s.charAt(i + count) == c) {
                        count++;
                    }
                    switch (c) {
                        case 'A':
                        case 'a':
                            replacement = localeData.amPm[inDate.get(9) + 0];
                            break;
                        case 'E':
                        case 'c':
                            replacement = getDayOfWeekString(localeData, inDate.get(7), count, c);
                            break;
                        case 'H':
                        case 'k':
                            replacement = zeroPad(inDate.get(11), count);
                            break;
                        case 'K':
                        case 'h':
                            int hour = inDate.get(10);
                            if (c == 'h' && hour == 0) {
                                hour = 12;
                            }
                            replacement = zeroPad(hour, count);
                            break;
                        case 'L':
                        case 'M':
                            replacement = getMonthString(localeData, inDate.get(2), count, c);
                            break;
                        case 'd':
                            replacement = zeroPad(inDate.get(5), count);
                            break;
                        case 'm':
                            replacement = zeroPad(inDate.get(12), count);
                            break;
                        case 's':
                            replacement = zeroPad(inDate.get(13), count);
                            break;
                        case 'y':
                            replacement = getYearString(inDate.get(1), count);
                            break;
                        case 'z':
                            replacement = getTimeZoneString(inDate, count);
                            break;
                        default:
                            replacement = null;
                            break;
                    }
                    if (replacement != null) {
                        s.replace(i, i + count, replacement);
                        count = replacement.length();
                        len = s.length();
                    }
                }
                i += count;
            }
            if (inFormat instanceof Spanned) {
                return new SpannedString(s);
            }
            return s.toString();
        }

        private static String getDayOfWeekString(LocaleData ld, int day, int count, int kind) {
            boolean standalone = kind == 99;
            if (count == 5) {
                return standalone ? ld.tinyStandAloneWeekdayNames[day] : ld.tinyWeekdayNames[day];
            } else if (count == 4) {
                return standalone ? ld.longStandAloneWeekdayNames[day] : ld.longWeekdayNames[day];
            } else {
                return standalone ? ld.shortStandAloneWeekdayNames[day] : ld.shortWeekdayNames[day];
            }
        }

        private static String getMonthString(LocaleData ld, int month, int count, int kind) {
            boolean standalone = kind == 76;
            if (count == 5) {
                return standalone ? ld.tinyStandAloneMonthNames[month] : ld.tinyMonthNames[month];
            } else if (count == 4) {
                return standalone ? ld.longStandAloneMonthNames[month] : ld.longMonthNames[month];
            } else if (count != 3) {
                return zeroPad(month + 1, count);
            } else {
                return standalone ? ld.shortStandAloneMonthNames[month] : ld.shortMonthNames[month];
            }
        }

        private static String getTimeZoneString(Calendar inDate, int count) {
            TimeZone tz = inDate.getTimeZone();
            if (count < 2) {
                return formatZoneOffset(inDate.get(16) + inDate.get(15), count);
            }
            return tz.getDisplayName(inDate.get(16) != 0, 0);
        }

        private static String formatZoneOffset(int offset, int count) {
            offset /= 1000;
            StringBuilder tb = new StringBuilder();
            if (offset < 0) {
                tb.insert(0, "-");
                offset = -offset;
            } else {
                tb.insert(0, "+");
            }
            int minutes = (offset % 3600) / 60;
            tb.append(zeroPad(offset / 3600, 2));
            tb.append(zeroPad(minutes, 2));
            return tb.toString();
        }

        private static String getYearString(int year, int count) {
            if (OppoThailandCalendarUtil.isThaiCalendarEnabled()) {
                year += 543;
            }
            if (count <= 2) {
                return zeroPad(year % 100, 2);
            }
            return String.format(Locale.getDefault(), "%d", new Object[]{Integer.valueOf(year)});
        }

        private static String zeroPad(int inValue, int inMinDigits) {
            return String.format(Locale.getDefault(), "%0" + inMinDigits + "d", new Object[]{Integer.valueOf(inValue)});
        }

        private static int appendQuotedText(SpannableStringBuilder s, int i, int len) {
            if (i + 1 >= len || s.charAt(i + 1) != '\'') {
                int count = 0;
                s.delete(i, i + 1);
                len--;
                while (i < len) {
                    if (s.charAt(i) != '\'') {
                        i++;
                        count++;
                    } else if (i + 1 >= len || s.charAt(i + 1) != '\'') {
                        s.delete(i, i + 1);
                        break;
                    } else {
                        s.delete(i, i + 1);
                        len--;
                        count++;
                        i++;
                    }
                }
                return count;
            }
            s.delete(i, i + 1);
            return 1;
        }
    }

    public static class OppoAndroidDateUtils {
        @Deprecated
        public static String formatDateTimeForSetting(Context context, long millis, int flags) {
            return OppoDateUtils.formatDateTime(context, millis, flags);
        }

        @Deprecated
        public static String formatDateRangeForSetting(Context context, long startMillis, long endMillis, int flags) {
            return OppoDateUtils.formatDateRange(context, startMillis, endMillis, flags).toString();
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

        synchronized String[][] internalZoneStrings() {
            if (this.zoneStrings == null) {
                this.zoneStrings = TimeZoneNames.getZoneStrings(this.locale);
            }
            return this.zoneStrings;
        }

        public OppoDateFormatSymbols() {
            this(Locale.getDefault());
        }

        public OppoDateFormatSymbols(Locale locale) {
            this.locale = locale;
            this.localPatternChars = "GyMdkHmsSEDFwWahKzZLc";
            this.localeData = LocaleData.get(locale);
            this.ampms = this.localeData.amPm;
            this.eras = this.localeData.eras;
            this.months = this.localeData.longMonthNames;
            this.shortMonths = this.localeData.shortMonthNames;
            this.weekdays = this.localeData.longWeekdayNames;
            this.shortWeekdays = this.localeData.shortWeekdayNames;
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

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            this.localeData = LocaleData.get(this.locale);
        }

        private void writeObject(ObjectOutputStream oos) throws IOException {
            internalZoneStrings();
            oos.defaultWriteObject();
        }

        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }

        public boolean equals(Object object) {
            boolean z = false;
            if (this == object) {
                return true;
            }
            if (!(object instanceof OppoDateFormatSymbols)) {
                return false;
            }
            OppoDateFormatSymbols rhs = (OppoDateFormatSymbols) object;
            if (this.localPatternChars.equals(rhs.localPatternChars) && Arrays.equals(this.ampms, rhs.ampms) && Arrays.equals(this.eras, rhs.eras) && Arrays.equals(this.months, rhs.months) && Arrays.equals(this.shortMonths, rhs.shortMonths) && Arrays.equals(this.shortWeekdays, rhs.shortWeekdays) && Arrays.equals(this.weekdays, rhs.weekdays)) {
                z = timeZoneStringsEqual(this, rhs);
            }
            return z;
        }

        private static boolean timeZoneStringsEqual(OppoDateFormatSymbols lhs, OppoDateFormatSymbols rhs) {
            if (lhs.zoneStrings == null && rhs.zoneStrings == null && lhs.locale.equals(rhs.locale)) {
                return true;
            }
            return Arrays.deepEquals(lhs.internalZoneStrings(), rhs.internalZoneStrings());
        }

        public String toString() {
            return getClass().getName() + "[amPmStrings=" + Arrays.toString(this.ampms) + ",customZoneStrings=" + this.customZoneStrings + ",eras=" + Arrays.toString(this.eras) + ",localPatternChars=" + this.localPatternChars + ",months=" + Arrays.toString(this.months) + ",shortMonths=" + Arrays.toString(this.shortMonths) + ",shortWeekdays=" + Arrays.toString(this.shortWeekdays) + ",weekdays=" + Arrays.toString(this.weekdays) + ",zoneStrings=[" + Arrays.toString(internalZoneStrings()[0]) + "...]" + "]";
        }

        public String[] getAmPmStrings() {
            return (String[]) this.ampms.clone();
        }

        public String[] getEras() {
            return (String[]) this.eras.clone();
        }

        public String getLocalPatternChars() {
            return this.localPatternChars;
        }

        public String[] getMonths() {
            return (String[]) this.months.clone();
        }

        public String[] getShortMonths() {
            return (String[]) this.shortMonths.clone();
        }

        public String[] getShortWeekdays() {
            return (String[]) this.shortWeekdays.clone();
        }

        public String[] getWeekdays() {
            return (String[]) this.weekdays.clone();
        }

        public String[][] getZoneStrings() {
            String[][] result = clone2dStringArray(internalZoneStrings());
            for (String[] zone : result) {
                String id = zone[0];
                if (zone[1] == null) {
                    zone[1] = TimeZone.getTimeZone(id).getDisplayName(false, 1, this.locale);
                }
                if (zone[2] == null) {
                    zone[2] = TimeZone.getTimeZone(id).getDisplayName(false, 0, this.locale);
                }
                if (zone[3] == null) {
                    zone[3] = TimeZone.getTimeZone(id).getDisplayName(true, 1, this.locale);
                }
                if (zone[4] == null) {
                    zone[4] = TimeZone.getTimeZone(id).getDisplayName(true, 0, this.locale);
                }
            }
            return result;
        }

        private static String[][] clone2dStringArray(String[][] array) {
            String[][] result = new String[array.length][];
            for (int i = 0; i < array.length; i++) {
                result[i] = (String[]) array[i].clone();
            }
            return result;
        }

        public int hashCode() {
            int i = 0;
            String[][] zoneStrings = internalZoneStrings();
            int hashCode = this.localPatternChars.hashCode();
            for (String element : this.ampms) {
                hashCode += element.hashCode();
            }
            for (String element2 : this.eras) {
                hashCode += element2.hashCode();
            }
            for (String element22 : this.months) {
                hashCode += element22.hashCode();
            }
            for (String element222 : this.shortMonths) {
                hashCode += element222.hashCode();
            }
            for (String element2222 : this.shortWeekdays) {
                hashCode += element2222.hashCode();
            }
            for (String element22222 : this.weekdays) {
                hashCode += element22222.hashCode();
            }
            int length = zoneStrings.length;
            while (i < length) {
                String[] element3 = zoneStrings[i];
                for (int j = 0; j < element3.length; j++) {
                    if (element3[j] != null) {
                        hashCode += element3[j].hashCode();
                    }
                }
                i++;
            }
            return hashCode;
        }

        public void setAmPmStrings(String[] data) {
            this.ampms = (String[]) data.clone();
        }

        public void setEras(String[] data) {
            this.eras = (String[]) data.clone();
        }

        public void setLocalPatternChars(String data) {
            if (data == null) {
                throw new NullPointerException("data == null");
            }
            this.localPatternChars = data;
        }

        public void setMonths(String[] data) {
            this.months = (String[]) data.clone();
        }

        public void setShortMonths(String[] data) {
            this.shortMonths = (String[]) data.clone();
        }

        public void setShortWeekdays(String[] data) {
            this.shortWeekdays = (String[]) data.clone();
        }

        public void setWeekdays(String[] data) {
            this.weekdays = (String[]) data.clone();
        }

        public void setZoneStrings(String[][] zoneStrings) {
            if (zoneStrings == null) {
                throw new NullPointerException("zoneStrings == null");
            }
            for (String[] row : zoneStrings) {
                if (row.length < 5) {
                    throw new IllegalArgumentException(Arrays.toString(row) + ".length < 5");
                }
            }
            this.zoneStrings = clone2dStringArray(zoneStrings);
            this.customZoneStrings = true;
        }
    }

    public static class OppoDateUtils {
        public static String formatDateTime(Context context, long millis, int flags) {
            return formatDateRange(context, millis, millis, flags);
        }

        public static String formatDateRange(Context context, long startMillis, long endMillis, int flags) {
            return formatDateRange(context, new Formatter(new StringBuilder(50), Locale.getDefault()), startMillis, endMillis, flags).toString();
        }

        public static Formatter formatDateRange(Context context, Formatter formatter, long startMillis, long endMillis, int flags) {
            return formatDateRange(context, formatter, startMillis, endMillis, flags, null);
        }

        public static Formatter formatDateRange(Context context, Formatter formatter, long startMillis, long endMillis, int flags, String timeZone) {
            if ((flags & 193) == 1) {
                flags |= android.text.format.DateFormat.is24HourFormat(context) ? 128 : 64;
            }
            try {
                formatter.out().append(toThailandDate(DateIntervalFormat.formatDateRange(startMillis, endMillis, flags, timeZone)));
                return formatter;
            } catch (IOException impossible) {
                throw new AssertionError(impossible);
            }
        }

        private static String toThailandDate(String geoDateString) {
            if (!OppoThailandCalendarUtil.isThaiCalendarEnabled()) {
                return geoDateString;
            }
            Matcher matcher = Pattern.compile("\\d{4}").matcher(geoDateString);
            while (matcher.find()) {
                String geoYearString = matcher.group();
                int geoYear = Integer.parseInt(geoYearString);
                if (geoYear < 1900 || geoYear > 2100) {
                    break;
                }
                geoDateString = geoDateString.replace(geoYearString, String.valueOf(geoYear + 543));
            }
            return geoDateString;
        }
    }

    public static class OppoJavaSimpleDateFormat extends DateFormat {
        static final String PATTERN_CHARS = "GyMdkHmsSEDFwWahKzZLc";
        private static final int RFC_822_TIMEZONE_FIELD = 18;
        private static final int STAND_ALONE_DAY_OF_WEEK_FIELD = 20;
        private static final int STAND_ALONE_MONTH_FIELD = 19;
        private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("defaultCenturyStart", Date.class), new ObjectStreamField("formatData", OppoDateFormatSymbols.class), new ObjectStreamField("pattern", String.class), new ObjectStreamField("serialVersionOnStream", Integer.TYPE)};
        private static final long serialVersionUID = 4774881970558875024L;
        private transient int creationYear;
        private Date defaultCenturyStart;
        private OppoDateFormatSymbols formatData;
        private String pattern;

        public OppoJavaSimpleDateFormat() {
            this(Locale.getDefault());
            this.pattern = defaultPattern();
            this.formatData = new OppoDateFormatSymbols(Locale.getDefault());
        }

        public OppoJavaSimpleDateFormat(String pattern) {
            this(pattern, Locale.getDefault());
        }

        private void validateFormat(char format) {
            if (PATTERN_CHARS.indexOf(format) == -1) {
                throw new IllegalArgumentException("Unknown pattern character '" + format + "'");
            }
        }

        private void validatePattern(String template) {
            int quote = 0;
            int last = -1;
            int count = 0;
            int patternLength = template.length();
            for (int i = 0; i < patternLength; i++) {
                int next = template.charAt(i);
                if (next == 39) {
                    if (count > 0) {
                        validateFormat((char) last);
                        count = 0;
                    }
                    if (last == next) {
                        last = -1;
                    } else {
                        last = next;
                    }
                    quote ^= 1;
                } else if (quote != 0 || (last != next && ((next < 97 || next > 122) && (next < 65 || next > 90)))) {
                    if (count > 0) {
                        validateFormat((char) last);
                        count = 0;
                    }
                    last = -1;
                } else if (last == next) {
                    count++;
                } else {
                    if (count > 0) {
                        validateFormat((char) last);
                    }
                    last = next;
                    count = 1;
                }
            }
            if (count > 0) {
                validateFormat((char) last);
            }
            if (quote != 0) {
                throw new IllegalArgumentException("Unterminated quote");
            }
        }

        public OppoJavaSimpleDateFormat(String template, OppoDateFormatSymbols value) {
            this(Locale.getDefault());
            validatePattern(template);
            this.pattern = template;
            this.formatData = (OppoDateFormatSymbols) value.clone();
        }

        public OppoJavaSimpleDateFormat(String template, Locale locale) {
            this(locale);
            validatePattern(template);
            this.pattern = template;
            this.formatData = new OppoDateFormatSymbols(locale);
        }

        private OppoJavaSimpleDateFormat(Locale locale) {
            this.numberFormat = NumberFormat.getInstance(locale);
            this.numberFormat.setParseIntegerOnly(true);
            this.numberFormat.setGroupingUsed(false);
            this.calendar = new GregorianCalendar(locale);
            this.calendar.add(1, -80);
            this.creationYear = this.calendar.get(1);
            this.defaultCenturyStart = this.calendar.getTime();
        }

        public void applyLocalizedPattern(String template) {
            this.pattern = convertPattern(template, this.formatData.getLocalPatternChars(), PATTERN_CHARS, true);
        }

        public void applyPattern(String template) {
            validatePattern(template);
            this.pattern = template;
        }

        public Object clone() {
            OppoJavaSimpleDateFormat clone = (OppoJavaSimpleDateFormat) super.clone();
            clone.formatData = (OppoDateFormatSymbols) this.formatData.clone();
            clone.defaultCenturyStart = new Date(this.defaultCenturyStart.getTime());
            return clone;
        }

        private static String defaultPattern() {
            LocaleData localeData = LocaleData.get(Locale.getDefault());
            return localeData.getDateFormat(3) + " " + localeData.getTimeFormat(3);
        }

        public boolean equals(Object object) {
            boolean z = false;
            if (this == object) {
                return true;
            }
            if (!(object instanceof OppoJavaSimpleDateFormat)) {
                return false;
            }
            OppoJavaSimpleDateFormat simple = (OppoJavaSimpleDateFormat) object;
            if (super.equals(object) && this.pattern.equals(simple.pattern)) {
                z = this.formatData.equals(simple.formatData);
            }
            return z;
        }

        public AttributedCharacterIterator formatToCharacterIterator(Object object) {
            if (object == null) {
                throw new NullPointerException("object == null");
            } else if (object instanceof Date) {
                return formatToCharacterIteratorImpl((Date) object);
            } else {
                if (object instanceof Number) {
                    return formatToCharacterIteratorImpl(new Date(((Number) object).longValue()));
                }
                throw new IllegalArgumentException("Bad class: " + object.getClass());
            }
        }

        private AttributedCharacterIterator formatToCharacterIteratorImpl(Date date) {
            StringBuffer buffer = new StringBuffer();
            ArrayList<FieldPosition> fields = new ArrayList();
            formatImpl(date, buffer, null, fields);
            AttributedString as = new AttributedString(buffer.toString());
            for (FieldPosition pos : fields) {
                Field attribute = pos.getFieldAttribute();
                as.addAttribute(attribute, attribute, pos.getBeginIndex(), pos.getEndIndex());
            }
            return as.getIterator();
        }

        private StringBuffer formatImpl(Date date, StringBuffer buffer, FieldPosition field, List<FieldPosition> fields) {
            int quote = 0;
            int last = -1;
            int count = 0;
            this.calendar.setTime(date);
            if (field != null) {
                field.setBeginIndex(0);
                field.setEndIndex(0);
            }
            int patternLength = this.pattern.length();
            for (int i = 0; i < patternLength; i++) {
                int next = this.pattern.charAt(i);
                if (next == 39) {
                    if (count > 0) {
                        append(buffer, field, fields, (char) last, count);
                        count = 0;
                    }
                    if (last == next) {
                        buffer.append('\'');
                        last = -1;
                    } else {
                        last = next;
                    }
                    quote ^= 1;
                } else if (quote != 0 || (last != next && ((next < 97 || next > 122) && (next < 65 || next > 90)))) {
                    if (count > 0) {
                        append(buffer, field, fields, (char) last, count);
                        count = 0;
                    }
                    last = -1;
                    buffer.append((char) next);
                } else if (last == next) {
                    count++;
                } else {
                    if (count > 0) {
                        append(buffer, field, fields, (char) last, count);
                    }
                    last = next;
                    count = 1;
                }
            }
            if (count > 0) {
                append(buffer, field, fields, (char) last, count);
            }
            return buffer;
        }

        private void append(StringBuffer buffer, FieldPosition position, List<FieldPosition> fields, char format, int count) {
            int field = -1;
            int index = PATTERN_CHARS.indexOf(format);
            if (index == -1) {
                throw new IllegalArgumentException("Unknown pattern character '" + format + "'");
            }
            int beginPosition = buffer.length();
            Field dateFormatField = null;
            int hour;
            switch (index) {
                case 0:
                    dateFormatField = DateFormat.Field.ERA;
                    buffer.append(this.formatData.eras[this.calendar.get(0)]);
                    break;
                case 1:
                    dateFormatField = DateFormat.Field.YEAR;
                    int year = this.calendar.get(1);
                    if (OppoThailandCalendarUtil.isThaiCalendarEnabled() && year < 2443) {
                        year += 543;
                    }
                    if (count != 2) {
                        appendNumber(buffer, count, year);
                        break;
                    } else {
                        appendNumber(buffer, 2, year % 100);
                        break;
                    }
                case 2:
                    dateFormatField = DateFormat.Field.MONTH;
                    appendMonth(buffer, count, false);
                    break;
                case 3:
                    dateFormatField = DateFormat.Field.DAY_OF_MONTH;
                    field = 5;
                    break;
                case 4:
                    dateFormatField = DateFormat.Field.HOUR_OF_DAY1;
                    hour = this.calendar.get(11);
                    if (hour == 0) {
                        hour = 24;
                    }
                    appendNumber(buffer, count, hour);
                    break;
                case 5:
                    dateFormatField = DateFormat.Field.HOUR_OF_DAY0;
                    field = 11;
                    break;
                case 6:
                    dateFormatField = DateFormat.Field.MINUTE;
                    field = 12;
                    break;
                case 7:
                    dateFormatField = DateFormat.Field.SECOND;
                    field = 13;
                    break;
                case 8:
                    dateFormatField = DateFormat.Field.MILLISECOND;
                    appendNumber(buffer, count, this.calendar.get(14));
                    break;
                case 9:
                    dateFormatField = DateFormat.Field.DAY_OF_WEEK;
                    appendDayOfWeek(buffer, count, false);
                    break;
                case 10:
                    dateFormatField = DateFormat.Field.DAY_OF_YEAR;
                    field = 6;
                    break;
                case 11:
                    dateFormatField = DateFormat.Field.DAY_OF_WEEK_IN_MONTH;
                    field = 8;
                    break;
                case 12:
                    dateFormatField = DateFormat.Field.WEEK_OF_YEAR;
                    field = 3;
                    break;
                case 13:
                    dateFormatField = DateFormat.Field.WEEK_OF_MONTH;
                    field = 4;
                    break;
                case 14:
                    dateFormatField = DateFormat.Field.AM_PM;
                    buffer.append(this.formatData.ampms[this.calendar.get(9)]);
                    break;
                case 15:
                    dateFormatField = DateFormat.Field.HOUR1;
                    hour = this.calendar.get(10);
                    if (hour == 0) {
                        hour = 12;
                    }
                    appendNumber(buffer, count, hour);
                    break;
                case ColorDialogUtil.BIT_FOUSED_BUTTON_NEGATIVE /*16*/:
                    dateFormatField = DateFormat.Field.HOUR0;
                    field = 10;
                    break;
                case 17:
                    dateFormatField = DateFormat.Field.TIME_ZONE;
                    appendTimeZone(buffer, count, true);
                    break;
                case RFC_822_TIMEZONE_FIELD /*18*/:
                    dateFormatField = DateFormat.Field.TIME_ZONE;
                    appendNumericTimeZone(buffer, count, false);
                    break;
                case STAND_ALONE_MONTH_FIELD /*19*/:
                    dateFormatField = DateFormat.Field.MONTH;
                    appendMonth(buffer, count, true);
                    break;
                case STAND_ALONE_DAY_OF_WEEK_FIELD /*20*/:
                    dateFormatField = DateFormat.Field.DAY_OF_WEEK;
                    appendDayOfWeek(buffer, count, true);
                    break;
            }
            if (field != -1) {
                appendNumber(buffer, count, this.calendar.get(field));
            }
            if (fields != null) {
                position = new FieldPosition(dateFormatField);
                position.setBeginIndex(beginPosition);
                position.setEndIndex(buffer.length());
                fields.add(position);
            } else if ((position.getFieldAttribute() == dateFormatField || (position.getFieldAttribute() == null && position.getField() == index)) && position.getEndIndex() == 0) {
                position.setBeginIndex(beginPosition);
                position.setEndIndex(buffer.length());
            }
        }

        private void appendDayOfWeek(StringBuffer buffer, int count, boolean standAlone) {
            LocaleData ld = this.formatData.localeData;
            String[] days = count == 4 ? standAlone ? ld.longStandAloneWeekdayNames : this.formatData.weekdays : count == 5 ? standAlone ? ld.tinyStandAloneWeekdayNames : this.formatData.localeData.tinyWeekdayNames : standAlone ? ld.shortStandAloneWeekdayNames : this.formatData.shortWeekdays;
            buffer.append(days[this.calendar.get(7)]);
        }

        private void appendMonth(StringBuffer buffer, int count, boolean standAlone) {
            int month = this.calendar.get(2);
            if (count <= 2) {
                appendNumber(buffer, count, month + 1);
                return;
            }
            LocaleData ld = this.formatData.localeData;
            String[] months = count == 4 ? standAlone ? ld.longStandAloneMonthNames : this.formatData.months : count == 5 ? standAlone ? ld.tinyStandAloneMonthNames : ld.tinyMonthNames : standAlone ? ld.shortStandAloneMonthNames : this.formatData.shortMonths;
            buffer.append(months[month]);
        }

        private void appendTimeZone(StringBuffer buffer, int count, boolean generalTimeZone) {
            if (generalTimeZone) {
                TimeZone tz = this.calendar.getTimeZone();
                boolean daylight = this.calendar.get(16) != 0;
                int style = count < 4 ? 0 : 1;
                if (this.formatData.customZoneStrings) {
                    String custom = TimeZoneNames.getDisplayName(this.formatData.zoneStrings, tz.getID(), daylight, style);
                    if (custom != null) {
                        buffer.append(custom);
                        return;
                    }
                }
                buffer.append(tz.getDisplayName(daylight, style, this.formatData.locale));
                return;
            }
            appendNumericTimeZone(buffer, count, generalTimeZone);
        }

        private void appendNumericTimeZone(StringBuffer buffer, int count, boolean generalTimeZone) {
            int offset = this.calendar.get(15) + this.calendar.get(16);
            char sign = '+';
            if (offset < 0) {
                sign = '-';
                offset = -offset;
            }
            if (generalTimeZone || count == 4) {
                buffer.append("GMT");
            }
            buffer.append(sign);
            appendNumber(buffer, 2, offset / 3600000);
            if (generalTimeZone || count >= 4) {
                buffer.append(':');
            }
            appendNumber(buffer, 2, (offset % 3600000) / 60000);
        }

        private void appendNumber(StringBuffer buffer, int count, int value) {
            int minimumIntegerDigits = this.numberFormat.getMinimumIntegerDigits();
            this.numberFormat.setMinimumIntegerDigits(count);
            this.numberFormat.format(Integer.valueOf(value), buffer, new FieldPosition(0));
            this.numberFormat.setMinimumIntegerDigits(minimumIntegerDigits);
        }

        private Date error(ParsePosition position, int offset, TimeZone zone) {
            position.setErrorIndex(offset);
            this.calendar.setTimeZone(zone);
            return null;
        }

        public StringBuffer format(Date date, StringBuffer buffer, FieldPosition fieldPos) {
            return formatImpl(date, buffer, fieldPos, null);
        }

        public Date get2DigitYearStart() {
            return (Date) this.defaultCenturyStart.clone();
        }

        public OppoDateFormatSymbols getDateFormatSymbols() {
            return (OppoDateFormatSymbols) this.formatData.clone();
        }

        public int hashCode() {
            return ((super.hashCode() + this.pattern.hashCode()) + this.formatData.hashCode()) + this.creationYear;
        }

        private int parse(String string, int offset, char format, int count) {
            int index = PATTERN_CHARS.indexOf(format);
            if (index == -1) {
                throw new IllegalArgumentException("Unknown pattern character '" + format + "'");
            }
            int field = -1;
            int absolute = 0;
            if (count < 0) {
                count = -count;
                absolute = count;
            }
            ParsePosition position;
            Number result;
            int hour;
            switch (index) {
                case 0:
                    return parseText(string, offset, this.formatData.eras, 0);
                case 1:
                    if (count >= 3) {
                        field = 1;
                        break;
                    }
                    position = new ParsePosition(offset);
                    result = parseNumber(absolute, string, position);
                    if (result == null) {
                        return (-position.getErrorIndex()) - 1;
                    }
                    int year = result.intValue();
                    if (position.getIndex() - offset == 2 && year >= 0) {
                        year += (this.creationYear / 100) * 100;
                        if (year < this.creationYear) {
                            year += 100;
                        }
                    }
                    this.calendar.set(1, year);
                    return position.getIndex();
                case 2:
                    return parseMonth(string, offset, count, absolute, false);
                case 3:
                    field = 5;
                    break;
                case 4:
                    position = new ParsePosition(offset);
                    result = parseNumber(absolute, string, position);
                    if (result == null) {
                        return (-position.getErrorIndex()) - 1;
                    }
                    hour = result.intValue();
                    if (hour == 24) {
                        hour = 0;
                    }
                    this.calendar.set(11, hour);
                    return position.getIndex();
                case 5:
                    field = 11;
                    break;
                case 6:
                    field = 12;
                    break;
                case 7:
                    field = 13;
                    break;
                case 8:
                    field = 14;
                    break;
                case 9:
                    return parseDayOfWeek(string, offset, false);
                case 10:
                    field = 6;
                    break;
                case 11:
                    field = 8;
                    break;
                case 12:
                    field = 3;
                    break;
                case 13:
                    field = 4;
                    break;
                case 14:
                    return parseText(string, offset, this.formatData.ampms, 9);
                case 15:
                    position = new ParsePosition(offset);
                    result = parseNumber(absolute, string, position);
                    if (result == null) {
                        return (-position.getErrorIndex()) - 1;
                    }
                    hour = result.intValue();
                    if (hour == 12) {
                        hour = 0;
                    }
                    this.calendar.set(10, hour);
                    return position.getIndex();
                case ColorDialogUtil.BIT_FOUSED_BUTTON_NEGATIVE /*16*/:
                    field = 10;
                    break;
                case 17:
                    return parseTimeZone(string, offset);
                case RFC_822_TIMEZONE_FIELD /*18*/:
                    return parseTimeZone(string, offset);
                case STAND_ALONE_MONTH_FIELD /*19*/:
                    return parseMonth(string, offset, count, absolute, true);
                case STAND_ALONE_DAY_OF_WEEK_FIELD /*20*/:
                    return parseDayOfWeek(string, offset, true);
            }
            if (field != -1) {
                return parseNumber(absolute, string, offset, field, 0);
            }
            return offset;
        }

        private int parseDayOfWeek(String string, int offset, boolean standAlone) {
            LocaleData ld = this.formatData.localeData;
            int index = parseText(string, offset, standAlone ? ld.longStandAloneWeekdayNames : this.formatData.weekdays, 7);
            if (index >= 0) {
                return index;
            }
            return parseText(string, offset, standAlone ? ld.shortStandAloneWeekdayNames : this.formatData.shortWeekdays, 7);
        }

        private int parseMonth(String string, int offset, int count, int absolute, boolean standAlone) {
            if (count <= 2) {
                return parseNumber(absolute, string, offset, 2, -1);
            }
            LocaleData ld = this.formatData.localeData;
            int index = parseText(string, offset, standAlone ? ld.longStandAloneMonthNames : this.formatData.months, 2);
            if (index < 0) {
                index = parseText(string, offset, standAlone ? ld.shortStandAloneMonthNames : this.formatData.shortMonths, 2);
            }
            return index;
        }

        public Date parse(String string, ParsePosition position) {
            int quote = 0;
            char last = 65535;
            int count = 0;
            int offset = position.getIndex();
            int length = string.length();
            this.calendar.clear();
            TimeZone zone = this.calendar.getTimeZone();
            int patternLength = this.pattern.length();
            for (int i = 0; i < patternLength; i++) {
                char next = this.pattern.charAt(i);
                if (next == '\'') {
                    if (count > 0) {
                        offset = parse(string, offset, (char) last, count);
                        if (offset < 0) {
                            return error(position, (-offset) - 1, zone);
                        }
                        count = 0;
                    }
                    if (last != next) {
                        last = next;
                    } else if (offset >= length || string.charAt(offset) != '\'') {
                        return error(position, offset, zone);
                    } else {
                        offset++;
                        last = 65535;
                    }
                    quote ^= 1;
                } else if (quote != 0 || (last != next && ((next < 'a' || next > 'z') && (next < 'A' || next > 'Z')))) {
                    if (count > 0) {
                        offset = parse(string, offset, (char) last, count);
                        if (offset < 0) {
                            return error(position, (-offset) - 1, zone);
                        }
                        count = 0;
                    }
                    last = 65535;
                    if (offset >= length || string.charAt(offset) != next) {
                        return error(position, offset, zone);
                    }
                    offset++;
                } else if (last == next) {
                    count++;
                } else {
                    if (count > 0) {
                        offset = parse(string, offset, (char) last, -count);
                        if (offset < 0) {
                            return error(position, (-offset) - 1, zone);
                        }
                    }
                    last = next;
                    count = 1;
                }
            }
            if (count > 0) {
                offset = parse(string, offset, (char) last, count);
                if (offset < 0) {
                    return error(position, (-offset) - 1, zone);
                }
            }
            try {
                int year = this.calendar.get(1);
                if (year > 2443) {
                    this.calendar.set(1, year - 543);
                }
                Date date = this.calendar.getTime();
                position.setIndex(offset);
                this.calendar.setTimeZone(zone);
                return date;
            } catch (IllegalArgumentException e) {
                return error(position, offset, zone);
            }
        }

        private Number parseNumber(int max, String string, ParsePosition position) {
            int length = string.length();
            int index = position.getIndex();
            if (max > 0 && max < length - index) {
                length = index + max;
            }
            while (index < length && (string.charAt(index) == ' ' || string.charAt(index) == 9)) {
                index++;
            }
            if (max == 0) {
                position.setIndex(index);
                Number n = this.numberFormat.parse(string, position);
                if (n != null && n.longValue() < 0 && (this.numberFormat instanceof DecimalFormat)) {
                    if (string.charAt(position.getIndex() - 1) == this.numberFormat.getDecimalFormatSymbols().getMinusSign()) {
                        n = Long.valueOf(-n.longValue());
                        position.setIndex(position.getIndex() - 1);
                    }
                }
                return n;
            }
            int result = 0;
            while (index < length) {
                int digit = Character.digit(string.charAt(index), 10);
                if (digit == -1) {
                    break;
                }
                result = (result * 10) + digit;
                index++;
            }
            if (index == position.getIndex()) {
                position.setErrorIndex(index);
                return null;
            }
            position.setIndex(index);
            return Integer.valueOf(result);
        }

        private int parseNumber(int max, String string, int offset, int field, int skew) {
            ParsePosition position = new ParsePosition(offset);
            Number result = parseNumber(max, string, position);
            if (result == null) {
                return (-position.getErrorIndex()) - 1;
            }
            this.calendar.set(field, result.intValue() + skew);
            return position.getIndex();
        }

        private int parseText(String string, int offset, String[] text, int field) {
            int found = -1;
            int i = 0;
            while (i < text.length) {
                if (!text[i].isEmpty()) {
                    if (string.regionMatches(true, offset, text[i], 0, text[i].length()) && (found == -1 || text[i].length() > text[found].length())) {
                        found = i;
                    }
                }
                i++;
            }
            if (found == -1) {
                return (-offset) - 1;
            }
            this.calendar.set(field, found);
            return text[found].length() + offset;
        }

        private int parseTimeZone(String string, int offset) {
            int raw;
            boolean foundGMT = string.regionMatches(offset, "GMT", 0, 3);
            if (foundGMT) {
                offset += 3;
            }
            if (offset < string.length()) {
                char sign = string.charAt(offset);
                if (sign == '+' || sign == '-') {
                    ParsePosition position = new ParsePosition(offset + 1);
                    Number result = this.numberFormat.parse(string, position);
                    if (result == null) {
                        return (-position.getErrorIndex()) - 1;
                    }
                    int hour = result.intValue();
                    raw = hour * 3600000;
                    int index = position.getIndex();
                    if (index < string.length() && string.charAt(index) == ':') {
                        position.setIndex(index + 1);
                        result = this.numberFormat.parse(string, position);
                        if (result == null) {
                            return (-position.getErrorIndex()) - 1;
                        }
                        raw += 60000 * result.intValue();
                    } else if (hour >= 24) {
                        raw = ((hour / 100) * 3600000) + ((hour % 100) * 60000);
                    }
                    if (sign == '-') {
                        raw = -raw;
                    }
                    this.calendar.setTimeZone(new SimpleTimeZone(raw, ""));
                    return position.getIndex();
                }
            }
            if (foundGMT) {
                this.calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
                return offset;
            }
            String[][] internalZoneStrings = this.formatData.internalZoneStrings();
            int i = 0;
            int length = internalZoneStrings.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    return (-offset) - 1;
                }
                String[] row = internalZoneStrings[i2];
                int i3 = 1;
                while (i3 < 5) {
                    if (row[i3] != null) {
                        if (string.regionMatches(true, offset, row[i3], 0, row[i3].length())) {
                            TimeZone zone = TimeZone.getTimeZone(row[0]);
                            if (zone == null) {
                                return (-offset) - 1;
                            }
                            raw = zone.getRawOffset();
                            if (i3 == 3 || i3 == 4) {
                                int dstSavings = zone.getDSTSavings();
                                if (dstSavings == 0) {
                                    dstSavings = 3600000;
                                }
                                raw += dstSavings;
                            }
                            this.calendar.setTimeZone(new SimpleTimeZone(raw, ""));
                            return row[i3].length() + offset;
                        }
                    }
                    i3++;
                }
                i = i2 + 1;
            }
        }

        public void set2DigitYearStart(Date date) {
            this.defaultCenturyStart = (Date) date.clone();
            Calendar cal = new GregorianCalendar();
            cal.setTime(this.defaultCenturyStart);
            this.creationYear = cal.get(1);
        }

        public void setDateFormatSymbols(OppoDateFormatSymbols value) {
            this.formatData = (OppoDateFormatSymbols) value.clone();
        }

        public String toLocalizedPattern() {
            return convertPattern(this.pattern, PATTERN_CHARS, this.formatData.getLocalPatternChars(), false);
        }

        private static String convertPattern(String template, String fromChars, String toChars, boolean check) {
            if (!check && fromChars.equals(toChars)) {
                return template;
            }
            int quote = 0;
            StringBuilder output = new StringBuilder();
            int length = template.length();
            int i = 0;
            while (i < length) {
                char next = template.charAt(i);
                if (next == '\'') {
                    quote ^= 1;
                }
                if (quote == 0) {
                    int index = fromChars.indexOf(next);
                    if (index != -1) {
                        output.append(toChars.charAt(index));
                        i++;
                    }
                }
                if (!check || (quote ^ 1) == 0 || ((next < 'a' || next > 'z') && (next < 'A' || next > 'Z'))) {
                    output.append(next);
                    i++;
                } else {
                    throw new IllegalArgumentException("Invalid pattern character '" + next + "' in " + "'" + template + "'");
                }
            }
            if (quote == 0) {
                return output.toString();
            }
            throw new IllegalArgumentException("Unterminated quote");
        }

        public String toPattern() {
            return this.pattern;
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            PutField fields = stream.putFields();
            fields.put("defaultCenturyStart", this.defaultCenturyStart);
            fields.put("formatData", this.formatData);
            fields.put("pattern", this.pattern);
            fields.put("serialVersionOnStream", 1);
            stream.writeFields();
        }

        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            Date date;
            GetField fields = stream.readFields();
            if (fields.get("serialVersionOnStream", 0) > 0) {
                date = (Date) fields.get("defaultCenturyStart", new Date());
            } else {
                date = new Date();
            }
            set2DigitYearStart(date);
            this.formatData = (OppoDateFormatSymbols) fields.get("formatData", null);
            this.pattern = (String) fields.get("pattern", "");
        }
    }

    private static boolean isThaiCalendarEnabled() {
        Properties prop = new Properties();
        String state = "false";
        if (state != null && state.equals("true")) {
            return true;
        }
        return false;
    }
}
