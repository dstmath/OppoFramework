package android.icu.text;

import android.icu.impl.CalendarData;
import android.icu.impl.DateNumberFormat;
import android.icu.impl.ICUCache;
import android.icu.impl.PatternProps;
import android.icu.impl.PatternTokenizer;
import android.icu.lang.UCharacter;
import android.icu.text.DateFormat.BooleanAttribute;
import android.icu.text.DateFormat.Field;
import android.icu.text.DisplayContext.Type;
import android.icu.text.TimeZoneFormat.Style;
import android.icu.text.TimeZoneFormat.TimeType;
import android.icu.util.BasicTimeZone;
import android.icu.util.Calendar;
import android.icu.util.Calendar.FormatConfiguration;
import android.icu.util.HebrewCalendar;
import android.icu.util.Output;
import android.icu.util.TimeZone;
import android.icu.util.TimeZoneTransition;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.UUID;

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
public class SimpleDateFormat extends DateFormat {
    /* renamed from: -android-icu-text-DisplayContextSwitchesValues */
    private static final /* synthetic */ int[] f51-android-icu-text-DisplayContextSwitchesValues = null;
    private static final int[] CALENDAR_FIELD_TO_LEVEL = null;
    static final UnicodeSet DATE_PATTERN_TYPE = null;
    private static final int DECIMAL_BUF_SIZE = 10;
    static boolean DelayedHebrewMonthCheck = false;
    private static final String FALLBACKPATTERN = "yy/MM/dd HH:mm";
    private static final int HEBREW_CAL_CUR_MILLENIUM_END_YEAR = 6000;
    private static final int HEBREW_CAL_CUR_MILLENIUM_START_YEAR = 5000;
    private static final int ISOSpecialEra = -32000;
    private static final String NUMERIC_FORMAT_CHARS = "ADdFgHhKkmrSsuWwYy";
    private static final String NUMERIC_FORMAT_CHARS2 = "ceLMQq";
    private static ICUCache<String, Object[]> PARSED_PATTERN_CACHE = null;
    private static final boolean[] PATTERN_CHAR_IS_SYNTAX = null;
    private static final int[] PATTERN_CHAR_TO_INDEX = null;
    private static final int[] PATTERN_CHAR_TO_LEVEL = null;
    private static final int[] PATTERN_INDEX_TO_CALENDAR_FIELD = null;
    private static final Field[] PATTERN_INDEX_TO_DATE_FORMAT_ATTRIBUTE = null;
    private static final int[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD = null;
    private static final String SUPPRESS_NEGATIVE_PREFIX = "꬀";
    private static ULocale cachedDefaultLocale = null;
    private static String cachedDefaultPattern = null;
    static final int currentSerialVersion = 2;
    private static final int millisPerHour = 3600000;
    private static final long serialVersionUID = 4774881970558875024L;
    private transient BreakIterator capitalizationBrkIter;
    private transient char[] decDigits;
    private transient char[] decimalBuf;
    private transient long defaultCenturyBase;
    private Date defaultCenturyStart;
    private transient int defaultCenturyStartYear;
    private DateFormatSymbols formatData;
    private transient ULocale locale;
    private HashMap<String, NumberFormat> numberFormatters;
    private String override;
    private HashMap<Character, String> overrideMap;
    private String pattern;
    private transient Object[] patternItems;
    private int serialVersionOnStream;
    private volatile TimeZoneFormat tzFormat;
    private transient boolean useFastFormat;
    private transient boolean useLocalZeroPaddingNumberFormat;

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private enum ContextValue {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.SimpleDateFormat.ContextValue.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.SimpleDateFormat.ContextValue.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.SimpleDateFormat.ContextValue.<clinit>():void");
        }
    }

    private static class PatternItem {
        final boolean isNumeric;
        final int length;
        final char type;

        PatternItem(char type, int length) {
            this.type = type;
            this.length = length;
            this.isNumeric = SimpleDateFormat.isNumeric(type, length);
        }
    }

    /* renamed from: -getandroid-icu-text-DisplayContextSwitchesValues */
    private static /* synthetic */ int[] m33-getandroid-icu-text-DisplayContextSwitchesValues() {
        if (f51-android-icu-text-DisplayContextSwitchesValues != null) {
            return f51-android-icu-text-DisplayContextSwitchesValues;
        }
        int[] iArr = new int[DisplayContext.values().length];
        try {
            iArr[DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DisplayContext.CAPITALIZATION_FOR_MIDDLE_OF_SENTENCE.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DisplayContext.CAPITALIZATION_FOR_STANDALONE.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[DisplayContext.CAPITALIZATION_NONE.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[DisplayContext.DIALECT_NAMES.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[DisplayContext.LENGTH_FULL.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[DisplayContext.LENGTH_SHORT.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[DisplayContext.STANDARD_NAMES.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        f51-android-icu-text-DisplayContextSwitchesValues = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.text.SimpleDateFormat.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.text.SimpleDateFormat.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.SimpleDateFormat.<clinit>():void");
    }

    private static int getLevelFromChar(char ch) {
        return ch < PATTERN_CHAR_TO_LEVEL.length ? PATTERN_CHAR_TO_LEVEL[ch & 255] : -1;
    }

    private static boolean isSyntaxChar(char ch) {
        return ch < PATTERN_CHAR_IS_SYNTAX.length ? PATTERN_CHAR_IS_SYNTAX[ch & 255] : false;
    }

    public SimpleDateFormat() {
        this(getDefaultPattern(), null, null, null, null, true, null);
    }

    public SimpleDateFormat(String pattern) {
        this(pattern, null, null, null, null, true, null);
    }

    public SimpleDateFormat(String pattern, Locale loc) {
        this(pattern, null, null, null, ULocale.forLocale(loc), true, null);
    }

    public SimpleDateFormat(String pattern, ULocale loc) {
        this(pattern, null, null, null, loc, true, null);
    }

    public SimpleDateFormat(String pattern, String override, ULocale loc) {
        this(pattern, null, null, null, loc, false, override);
    }

    public SimpleDateFormat(String pattern, DateFormatSymbols formatData) {
        this(pattern, (DateFormatSymbols) formatData.clone(), null, null, null, true, null);
    }

    @Deprecated
    public SimpleDateFormat(String pattern, DateFormatSymbols formatData, ULocale loc) {
        this(pattern, (DateFormatSymbols) formatData.clone(), null, null, loc, true, null);
    }

    SimpleDateFormat(String pattern, DateFormatSymbols formatData, Calendar calendar, ULocale locale, boolean useFastFormat, String override) {
        this(pattern, (DateFormatSymbols) formatData.clone(), (Calendar) calendar.clone(), null, locale, useFastFormat, override);
    }

    private SimpleDateFormat(String pattern, DateFormatSymbols formatData, Calendar calendar, NumberFormat numberFormat, ULocale locale, boolean useFastFormat, String override) {
        this.serialVersionOnStream = 2;
        this.capitalizationBrkIter = null;
        this.pattern = pattern;
        this.formatData = formatData;
        this.calendar = calendar;
        this.numberFormat = numberFormat;
        this.locale = locale;
        this.useFastFormat = useFastFormat;
        this.override = override;
        initialize();
    }

    @Deprecated
    public static SimpleDateFormat getInstance(FormatConfiguration formatConfig) {
        String ostr = formatConfig.getOverrideString();
        boolean useFast = ostr != null && ostr.length() > 0;
        return new SimpleDateFormat(formatConfig.getPatternString(), formatConfig.getDateFormatSymbols(), formatConfig.getCalendar(), null, formatConfig.getLocale(), useFast, formatConfig.getOverrideString());
    }

    private void initialize() {
        if (this.locale == null) {
            this.locale = ULocale.getDefault(Category.FORMAT);
        }
        if (this.formatData == null) {
            this.formatData = new DateFormatSymbols(this.locale);
        }
        if (this.calendar == null) {
            this.calendar = Calendar.getInstance(this.locale);
        }
        if (this.numberFormat == null) {
            NumberingSystem ns = NumberingSystem.getInstance(this.locale);
            if (ns.isAlgorithmic()) {
                this.numberFormat = NumberFormat.getInstance(this.locale);
            } else {
                this.numberFormat = new DateNumberFormat(this.locale, ns.getDescription(), ns.getName());
            }
        }
        this.defaultCenturyBase = System.currentTimeMillis();
        setLocale(this.calendar.getLocale(ULocale.VALID_LOCALE), this.calendar.getLocale(ULocale.ACTUAL_LOCALE));
        initLocalZeroPaddingNumberFormat();
        if (this.override != null) {
            initNumberFormatters(this.locale);
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0005, code:
            if (r3.tzFormat == null) goto L_0x0007;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void initializeTimeZoneFormat(boolean bForceUpdate) {
        if (!bForceUpdate) {
        }
        this.tzFormat = TimeZoneFormat.getInstance(this.locale);
        String digits = null;
        if (this.numberFormat instanceof DecimalFormat) {
            digits = new String(((DecimalFormat) this.numberFormat).getDecimalFormatSymbols().getDigits());
        } else if (this.numberFormat instanceof DateNumberFormat) {
            digits = new String(((DateNumberFormat) this.numberFormat).getDigits());
        }
        if (!(digits == null || this.tzFormat.getGMTOffsetDigits().equals(digits))) {
            if (this.tzFormat.isFrozen()) {
                this.tzFormat = this.tzFormat.cloneAsThawed();
            }
            this.tzFormat.setGMTOffsetDigits(digits);
        }
    }

    private TimeZoneFormat tzFormat() {
        if (this.tzFormat == null) {
            initializeTimeZoneFormat(false);
        }
        return this.tzFormat;
    }

    private static synchronized String getDefaultPattern() {
        String str;
        synchronized (SimpleDateFormat.class) {
            ULocale defaultLocale = ULocale.getDefault(Category.FORMAT);
            if (!defaultLocale.equals(cachedDefaultLocale)) {
                cachedDefaultLocale = defaultLocale;
                try {
                    String[] dateTimePatterns = new CalendarData(cachedDefaultLocale, Calendar.getInstance(cachedDefaultLocale).getType()).getDateTimePatterns();
                    int glueIndex = 8;
                    if (dateTimePatterns.length >= 13) {
                        glueIndex = 12;
                    }
                    str = dateTimePatterns[glueIndex];
                    Object[] objArr = new Object[2];
                    objArr[0] = dateTimePatterns[3];
                    objArr[1] = dateTimePatterns[7];
                    cachedDefaultPattern = MessageFormat.format(str, objArr);
                } catch (MissingResourceException e) {
                    cachedDefaultPattern = FALLBACKPATTERN;
                }
            }
            str = cachedDefaultPattern;
        }
        return str;
    }

    private void parseAmbiguousDatesAsAfter(Date startDate) {
        this.defaultCenturyStart = startDate;
        this.calendar.setTime(startDate);
        this.defaultCenturyStartYear = this.calendar.get(1);
    }

    private void initializeDefaultCenturyStart(long baseTime) {
        this.defaultCenturyBase = baseTime;
        Calendar tmpCal = (Calendar) this.calendar.clone();
        tmpCal.setTimeInMillis(baseTime);
        tmpCal.add(1, -80);
        this.defaultCenturyStart = tmpCal.getTime();
        this.defaultCenturyStartYear = tmpCal.get(1);
    }

    private Date getDefaultCenturyStart() {
        if (this.defaultCenturyStart == null) {
            initializeDefaultCenturyStart(this.defaultCenturyBase);
        }
        return this.defaultCenturyStart;
    }

    private int getDefaultCenturyStartYear() {
        if (this.defaultCenturyStart == null) {
            initializeDefaultCenturyStart(this.defaultCenturyBase);
        }
        return this.defaultCenturyStartYear;
    }

    public void set2DigitYearStart(Date startDate) {
        parseAmbiguousDatesAsAfter(startDate);
    }

    public Date get2DigitYearStart() {
        return getDefaultCenturyStart();
    }

    public void setContext(DisplayContext context) {
        super.setContext(context);
        if (this.capitalizationBrkIter != null) {
            return;
        }
        if (context == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE || context == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU || context == DisplayContext.CAPITALIZATION_FOR_STANDALONE) {
            this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.locale);
        }
    }

    public StringBuffer format(Calendar cal, StringBuffer toAppendTo, FieldPosition pos) {
        TimeZone backupTZ = null;
        if (!(cal == this.calendar || cal.getType().equals(this.calendar.getType()))) {
            this.calendar.setTimeInMillis(cal.getTimeInMillis());
            backupTZ = this.calendar.getTimeZone();
            this.calendar.setTimeZone(cal.getTimeZone());
            cal = this.calendar;
        }
        StringBuffer result = format(cal, getContext(Type.CAPITALIZATION), toAppendTo, pos, null);
        if (backupTZ != null) {
            this.calendar.setTimeZone(backupTZ);
        }
        return result;
    }

    private StringBuffer format(Calendar cal, DisplayContext capitalizationContext, StringBuffer toAppendTo, FieldPosition pos, List<FieldPosition> attributes) {
        pos.setBeginIndex(0);
        pos.setEndIndex(0);
        Object[] items = getPatternItems();
        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof String) {
                toAppendTo.append((String) items[i]);
            } else {
                PatternItem item = items[i];
                int start = 0;
                if (attributes != null) {
                    start = toAppendTo.length();
                }
                if (this.useFastFormat) {
                    subFormat(toAppendTo, item.type, item.length, toAppendTo.length(), i, capitalizationContext, pos, cal);
                } else {
                    StringBuffer stringBuffer = toAppendTo;
                    stringBuffer.append(subFormat(item.type, item.length, toAppendTo.length(), i, capitalizationContext, pos, cal));
                }
                if (attributes != null) {
                    int end = toAppendTo.length();
                    if (end - start > 0) {
                        FieldPosition fp = new FieldPosition(patternCharToDateFormatField(item.type));
                        fp.setBeginIndex(start);
                        fp.setEndIndex(end);
                        attributes.add(fp);
                    }
                }
            }
        }
        return toAppendTo;
    }

    private static int getIndexFromChar(char ch) {
        return ch < PATTERN_CHAR_TO_INDEX.length ? PATTERN_CHAR_TO_INDEX[ch & 255] : -1;
    }

    protected Field patternCharToDateFormatField(char ch) {
        int patternCharIndex = getIndexFromChar(ch);
        if (patternCharIndex != -1) {
            return PATTERN_INDEX_TO_DATE_FORMAT_ATTRIBUTE[patternCharIndex];
        }
        return null;
    }

    protected String subFormat(char ch, int count, int beginOffset, FieldPosition pos, DateFormatSymbols fmtData, Calendar cal) throws IllegalArgumentException {
        return subFormat(ch, count, beginOffset, 0, DisplayContext.CAPITALIZATION_NONE, pos, cal);
    }

    @Deprecated
    protected String subFormat(char ch, int count, int beginOffset, int fieldNum, DisplayContext capitalizationContext, FieldPosition pos, Calendar cal) {
        StringBuffer buf = new StringBuffer();
        subFormat(buf, ch, count, beginOffset, fieldNum, capitalizationContext, pos, cal);
        return buf.toString();
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    @Deprecated
    protected void subFormat(StringBuffer buf, char ch, int count, int beginOffset, int fieldNum, DisplayContext capitalizationContext, FieldPosition pos, Calendar cal) {
        int bufstart = buf.length();
        TimeZone tz = cal.getTimeZone();
        long date = cal.getTimeInMillis();
        String result = null;
        int patternCharIndex = getIndexFromChar(ch);
        if (patternCharIndex != -1) {
            int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
            int value = 0;
            if (field >= 0) {
                value = patternCharIndex != 34 ? cal.get(field) : cal.getRelatedYear();
            }
            NumberFormat currentNumberFormat = getNumberFormat(ch);
            CapitalizationContextUsage capContextUsageType = CapitalizationContextUsage.OTHER;
            switch (patternCharIndex) {
                case 0:
                    if (!cal.getType().equals("chinese") && !cal.getType().equals("dangi")) {
                        if (count != 5) {
                            if (count != 4) {
                                safeAppend(this.formatData.eras, value, buf);
                                capContextUsageType = CapitalizationContextUsage.ERA_ABBREV;
                                break;
                            }
                            safeAppend(this.formatData.eraNames, value, buf);
                            capContextUsageType = CapitalizationContextUsage.ERA_WIDE;
                            break;
                        }
                        safeAppend(this.formatData.narrowEras, value, buf);
                        capContextUsageType = CapitalizationContextUsage.ERA_NARROW;
                        break;
                    }
                    zeroPaddingNumber(currentNumberFormat, buf, value, 1, 9);
                    break;
                    break;
                case 1:
                case 18:
                    if (this.override != null && ((this.override.compareTo("hebr") == 0 || this.override.indexOf("y=hebr") >= 0) && value > HEBREW_CAL_CUR_MILLENIUM_START_YEAR && value < HEBREW_CAL_CUR_MILLENIUM_END_YEAR)) {
                        value -= 5000;
                    }
                    if (count != 2) {
                        zeroPaddingNumber(currentNumberFormat, buf, value, count, Integer.MAX_VALUE);
                        break;
                    } else {
                        zeroPaddingNumber(currentNumberFormat, buf, value, 2, 2);
                        break;
                    }
                case 2:
                case 26:
                    if (cal.getType().equals("hebrew")) {
                        boolean isLeap = HebrewCalendar.isLeapYear(cal.get(1));
                        if (isLeap && value == 6 && count >= 3) {
                            value = 13;
                        }
                        if (!isLeap && value >= 6 && count < 3) {
                            value--;
                        }
                    }
                    int isLeapMonth = (this.formatData.leapMonthPatterns == null || this.formatData.leapMonthPatterns.length < 7) ? 0 : cal.get(22);
                    if (count != 5) {
                        if (count != 4) {
                            if (count == 3) {
                                if (patternCharIndex != 2) {
                                    safeAppendWithMonthPattern(this.formatData.standaloneShortMonths, value, buf, isLeapMonth != 0 ? this.formatData.leapMonthPatterns[4] : null);
                                    capContextUsageType = CapitalizationContextUsage.MONTH_STANDALONE;
                                    break;
                                }
                                safeAppendWithMonthPattern(this.formatData.shortMonths, value, buf, isLeapMonth != 0 ? this.formatData.leapMonthPatterns[1] : null);
                                capContextUsageType = CapitalizationContextUsage.MONTH_FORMAT;
                                break;
                            }
                            StringBuffer monthNumber = new StringBuffer();
                            zeroPaddingNumber(currentNumberFormat, monthNumber, value + 1, count, Integer.MAX_VALUE);
                            String[] monthNumberStrings = new String[1];
                            monthNumberStrings[0] = monthNumber.toString();
                            safeAppendWithMonthPattern(monthNumberStrings, 0, buf, isLeapMonth != 0 ? this.formatData.leapMonthPatterns[6] : null);
                            break;
                        } else if (patternCharIndex != 2) {
                            safeAppendWithMonthPattern(this.formatData.standaloneMonths, value, buf, isLeapMonth != 0 ? this.formatData.leapMonthPatterns[3] : null);
                            capContextUsageType = CapitalizationContextUsage.MONTH_STANDALONE;
                            break;
                        } else {
                            safeAppendWithMonthPattern(this.formatData.months, value, buf, isLeapMonth != 0 ? this.formatData.leapMonthPatterns[0] : null);
                            capContextUsageType = CapitalizationContextUsage.MONTH_FORMAT;
                            break;
                        }
                    }
                    if (patternCharIndex == 2) {
                        safeAppendWithMonthPattern(this.formatData.narrowMonths, value, buf, isLeapMonth != 0 ? this.formatData.leapMonthPatterns[2] : null);
                    } else {
                        safeAppendWithMonthPattern(this.formatData.standaloneNarrowMonths, value, buf, isLeapMonth != 0 ? this.formatData.leapMonthPatterns[5] : null);
                    }
                    capContextUsageType = CapitalizationContextUsage.MONTH_NARROW;
                    break;
                    break;
                case 4:
                    if (value != 0) {
                        zeroPaddingNumber(currentNumberFormat, buf, value, count, Integer.MAX_VALUE);
                        break;
                    } else {
                        zeroPaddingNumber(currentNumberFormat, buf, cal.getMaximum(11) + 1, count, Integer.MAX_VALUE);
                        break;
                    }
                case 8:
                    this.numberFormat.setMinimumIntegerDigits(Math.min(3, count));
                    this.numberFormat.setMaximumIntegerDigits(Integer.MAX_VALUE);
                    if (count == 1) {
                        value /= 100;
                    } else if (count == 2) {
                        value /= 10;
                    }
                    FieldPosition fieldPosition = new FieldPosition(-1);
                    this.numberFormat.format((long) value, buf, fieldPosition);
                    if (count > 3) {
                        this.numberFormat.setMinimumIntegerDigits(count - 3);
                        this.numberFormat.format(0, buf, fieldPosition);
                        break;
                    }
                    break;
                case 9:
                    if (count != 5) {
                        if (count != 4) {
                            if (count == 6 && this.formatData.shorterWeekdays != null) {
                                safeAppend(this.formatData.shorterWeekdays, value, buf);
                                capContextUsageType = CapitalizationContextUsage.DAY_FORMAT;
                                break;
                            }
                            safeAppend(this.formatData.shortWeekdays, value, buf);
                            capContextUsageType = CapitalizationContextUsage.DAY_FORMAT;
                            break;
                        }
                        safeAppend(this.formatData.weekdays, value, buf);
                        capContextUsageType = CapitalizationContextUsage.DAY_FORMAT;
                        break;
                    }
                    safeAppend(this.formatData.narrowWeekdays, value, buf);
                    capContextUsageType = CapitalizationContextUsage.DAY_NARROW;
                    break;
                case 14:
                    if (count >= 5 && this.formatData.ampmsNarrow != null) {
                        safeAppend(this.formatData.ampmsNarrow, value, buf);
                        break;
                    } else {
                        safeAppend(this.formatData.ampms, value, buf);
                        break;
                    }
                    break;
                case 15:
                    if (value != 0) {
                        zeroPaddingNumber(currentNumberFormat, buf, value, count, Integer.MAX_VALUE);
                        break;
                    } else {
                        zeroPaddingNumber(currentNumberFormat, buf, cal.getLeastMaximum(10) + 1, count, Integer.MAX_VALUE);
                        break;
                    }
                case 17:
                    if (count < 4) {
                        result = tzFormat().format(Style.SPECIFIC_SHORT, tz, date);
                        capContextUsageType = CapitalizationContextUsage.METAZONE_SHORT;
                    } else {
                        result = tzFormat().format(Style.SPECIFIC_LONG, tz, date);
                        capContextUsageType = CapitalizationContextUsage.METAZONE_LONG;
                    }
                    buf.append(result);
                    break;
                case 19:
                    if (count < 3) {
                        zeroPaddingNumber(currentNumberFormat, buf, value, count, Integer.MAX_VALUE);
                        break;
                    }
                    value = cal.get(7);
                case 23:
                    if (count < 4) {
                        result = tzFormat().format(Style.ISO_BASIC_LOCAL_FULL, tz, date);
                    } else if (count == 5) {
                        result = tzFormat().format(Style.ISO_EXTENDED_FULL, tz, date);
                    } else {
                        result = tzFormat().format(Style.LOCALIZED_GMT, tz, date);
                    }
                    buf.append(result);
                    break;
                case 24:
                    if (count == 1) {
                        result = tzFormat().format(Style.GENERIC_SHORT, tz, date);
                        capContextUsageType = CapitalizationContextUsage.METAZONE_SHORT;
                    } else if (count == 4) {
                        result = tzFormat().format(Style.GENERIC_LONG, tz, date);
                        capContextUsageType = CapitalizationContextUsage.METAZONE_LONG;
                    }
                    buf.append(result);
                    break;
                case 25:
                    if (count >= 3) {
                        value = cal.get(7);
                        if (count != 5) {
                            if (count != 4) {
                                if (count == 6 && this.formatData.standaloneShorterWeekdays != null) {
                                    safeAppend(this.formatData.standaloneShorterWeekdays, value, buf);
                                    capContextUsageType = CapitalizationContextUsage.DAY_STANDALONE;
                                    break;
                                }
                                safeAppend(this.formatData.standaloneShortWeekdays, value, buf);
                                capContextUsageType = CapitalizationContextUsage.DAY_STANDALONE;
                                break;
                            }
                            safeAppend(this.formatData.standaloneWeekdays, value, buf);
                            capContextUsageType = CapitalizationContextUsage.DAY_STANDALONE;
                            break;
                        }
                        safeAppend(this.formatData.standaloneNarrowWeekdays, value, buf);
                        capContextUsageType = CapitalizationContextUsage.DAY_NARROW;
                        break;
                    }
                    zeroPaddingNumber(currentNumberFormat, buf, value, 1, Integer.MAX_VALUE);
                    break;
                    break;
                case 27:
                    if (count < 4) {
                        if (count != 3) {
                            zeroPaddingNumber(currentNumberFormat, buf, (value / 3) + 1, count, Integer.MAX_VALUE);
                            break;
                        } else {
                            safeAppend(this.formatData.shortQuarters, value / 3, buf);
                            break;
                        }
                    }
                    safeAppend(this.formatData.quarters, value / 3, buf);
                    break;
                case 28:
                    if (count < 4) {
                        if (count != 3) {
                            zeroPaddingNumber(currentNumberFormat, buf, (value / 3) + 1, count, Integer.MAX_VALUE);
                            break;
                        } else {
                            safeAppend(this.formatData.standaloneShortQuarters, value / 3, buf);
                            break;
                        }
                    }
                    safeAppend(this.formatData.standaloneQuarters, value / 3, buf);
                    break;
                case 29:
                    if (count == 1) {
                        result = tzFormat().format(Style.ZONE_ID_SHORT, tz, date);
                    } else if (count == 2) {
                        result = tzFormat().format(Style.ZONE_ID, tz, date);
                    } else if (count == 3) {
                        result = tzFormat().format(Style.EXEMPLAR_LOCATION, tz, date);
                    } else if (count == 4) {
                        result = tzFormat().format(Style.GENERIC_LOCATION, tz, date);
                        capContextUsageType = CapitalizationContextUsage.ZONE_LONG;
                    }
                    buf.append(result);
                    break;
                case 30:
                    if (this.formatData.shortYearNames != null && value <= this.formatData.shortYearNames.length) {
                        safeAppend(this.formatData.shortYearNames, value - 1, buf);
                        break;
                    }
                case 31:
                    if (count == 1) {
                        result = tzFormat().format(Style.LOCALIZED_GMT_SHORT, tz, date);
                    } else if (count == 4) {
                        result = tzFormat().format(Style.LOCALIZED_GMT, tz, date);
                    }
                    buf.append(result);
                    break;
                case 32:
                    if (count == 1) {
                        result = tzFormat().format(Style.ISO_BASIC_SHORT, tz, date);
                    } else if (count == 2) {
                        result = tzFormat().format(Style.ISO_BASIC_FIXED, tz, date);
                    } else if (count == 3) {
                        result = tzFormat().format(Style.ISO_EXTENDED_FIXED, tz, date);
                    } else if (count == 4) {
                        result = tzFormat().format(Style.ISO_BASIC_FULL, tz, date);
                    } else if (count == 5) {
                        result = tzFormat().format(Style.ISO_EXTENDED_FULL, tz, date);
                    }
                    buf.append(result);
                    break;
                case 33:
                    if (count == 1) {
                        result = tzFormat().format(Style.ISO_BASIC_LOCAL_SHORT, tz, date);
                    } else if (count == 2) {
                        result = tzFormat().format(Style.ISO_BASIC_LOCAL_FIXED, tz, date);
                    } else if (count == 3) {
                        result = tzFormat().format(Style.ISO_EXTENDED_LOCAL_FIXED, tz, date);
                    } else if (count == 4) {
                        result = tzFormat().format(Style.ISO_BASIC_LOCAL_FULL, tz, date);
                    } else if (count == 5) {
                        result = tzFormat().format(Style.ISO_EXTENDED_LOCAL_FULL, tz, date);
                    }
                    buf.append(result);
                    break;
                case 35:
                    buf.append(this.formatData.getTimeSeparatorString());
                    break;
                default:
                    zeroPaddingNumber(currentNumberFormat, buf, value, count, Integer.MAX_VALUE);
                    break;
            }
            if (fieldNum == 0 && capitalizationContext != null && UCharacter.isLowerCase(buf.codePointAt(bufstart))) {
                boolean titlecase = false;
                switch (m33-getandroid-icu-text-DisplayContextSwitchesValues()[capitalizationContext.ordinal()]) {
                    case 1:
                        titlecase = true;
                        break;
                    case 2:
                    case 3:
                        if (this.formatData.capitalization != null) {
                            boolean[] transforms = (boolean[]) this.formatData.capitalization.get(capContextUsageType);
                            if (capitalizationContext != DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU) {
                                titlecase = transforms[1];
                                break;
                            } else {
                                titlecase = transforms[0];
                                break;
                            }
                        }
                        break;
                }
                if (titlecase) {
                    if (this.capitalizationBrkIter == null) {
                        this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.locale);
                    }
                    buf.replace(bufstart, buf.length(), UCharacter.toTitleCase(this.locale, buf.substring(bufstart), this.capitalizationBrkIter, 768));
                }
            }
            if (pos.getBeginIndex() == pos.getEndIndex()) {
                if (pos.getField() == PATTERN_INDEX_TO_DATE_FORMAT_FIELD[patternCharIndex]) {
                    pos.setBeginIndex(beginOffset);
                    pos.setEndIndex((buf.length() + beginOffset) - bufstart);
                } else if (pos.getFieldAttribute() == PATTERN_INDEX_TO_DATE_FORMAT_ATTRIBUTE[patternCharIndex]) {
                    pos.setBeginIndex(beginOffset);
                    pos.setEndIndex((buf.length() + beginOffset) - bufstart);
                }
            }
        } else if (ch != 'l') {
            throw new IllegalArgumentException("Illegal pattern character '" + ch + "' in \"" + this.pattern + '\"');
        }
    }

    private static void safeAppend(String[] array, int value, StringBuffer appendTo) {
        if (array != null && value >= 0 && value < array.length) {
            appendTo.append(array[value]);
        }
    }

    private static void safeAppendWithMonthPattern(String[] array, int value, StringBuffer appendTo, String monthPattern) {
        if (array != null && value >= 0 && value < array.length) {
            if (monthPattern == null) {
                appendTo.append(array[value]);
                return;
            }
            Object[] objArr = new Object[1];
            objArr[0] = array[value];
            appendTo.append(MessageFormat.format(monthPattern, objArr));
        }
    }

    private Object[] getPatternItems() {
        if (this.patternItems != null) {
            return this.patternItems;
        }
        this.patternItems = (Object[]) PARSED_PATTERN_CACHE.get(this.pattern);
        if (this.patternItems != null) {
            return this.patternItems;
        }
        boolean isPrevQuote = false;
        boolean inQuote = false;
        StringBuilder text = new StringBuilder();
        char itemType = 0;
        int itemLength = 1;
        List<Object> items = new ArrayList();
        for (int i = 0; i < this.pattern.length(); i++) {
            char ch = this.pattern.charAt(i);
            if (ch == PatternTokenizer.SINGLE_QUOTE) {
                if (isPrevQuote) {
                    text.append(PatternTokenizer.SINGLE_QUOTE);
                    isPrevQuote = false;
                } else {
                    isPrevQuote = true;
                    if (itemType != 0) {
                        items.add(new PatternItem(itemType, itemLength));
                        itemType = 0;
                    }
                }
                if (inQuote) {
                    inQuote = false;
                } else {
                    inQuote = true;
                }
            } else {
                isPrevQuote = false;
                if (inQuote) {
                    text.append(ch);
                } else if (!isSyntaxChar(ch)) {
                    if (itemType != 0) {
                        items.add(new PatternItem(itemType, itemLength));
                        itemType = 0;
                    }
                    text.append(ch);
                } else if (ch == itemType) {
                    itemLength++;
                } else {
                    if (itemType != 0) {
                        items.add(new PatternItem(itemType, itemLength));
                    } else if (text.length() > 0) {
                        items.add(text.toString());
                        text.setLength(0);
                    }
                    itemType = ch;
                    itemLength = 1;
                }
            }
        }
        if (itemType != 0) {
            items.add(new PatternItem(itemType, itemLength));
        } else if (text.length() > 0) {
            items.add(text.toString());
            text.setLength(0);
        }
        this.patternItems = items.toArray(new Object[items.size()]);
        PARSED_PATTERN_CACHE.put(this.pattern, this.patternItems);
        return this.patternItems;
    }

    @Deprecated
    protected void zeroPaddingNumber(NumberFormat nf, StringBuffer buf, int value, int minDigits, int maxDigits) {
        if (!this.useLocalZeroPaddingNumberFormat || value < 0) {
            nf.setMinimumIntegerDigits(minDigits);
            nf.setMaximumIntegerDigits(maxDigits);
            nf.format((long) value, buf, new FieldPosition(-1));
            return;
        }
        fastZeroPaddingNumber(buf, value, minDigits, maxDigits);
    }

    public void setNumberFormat(NumberFormat newNumberFormat) {
        super.setNumberFormat(newNumberFormat);
        initLocalZeroPaddingNumberFormat();
        initializeTimeZoneFormat(true);
        if (this.numberFormatters != null) {
            this.numberFormatters = null;
        }
        if (this.overrideMap != null) {
            this.overrideMap = null;
        }
    }

    private void initLocalZeroPaddingNumberFormat() {
        if (this.numberFormat instanceof DecimalFormat) {
            this.decDigits = ((DecimalFormat) this.numberFormat).getDecimalFormatSymbols().getDigits();
            this.useLocalZeroPaddingNumberFormat = true;
        } else if (this.numberFormat instanceof DateNumberFormat) {
            this.decDigits = ((DateNumberFormat) this.numberFormat).getDigits();
            this.useLocalZeroPaddingNumberFormat = true;
        } else {
            this.useLocalZeroPaddingNumberFormat = false;
        }
        if (this.useLocalZeroPaddingNumberFormat) {
            this.decimalBuf = new char[10];
        }
    }

    private void fastZeroPaddingNumber(StringBuffer buf, int value, int minDigits, int maxDigits) {
        int limit;
        int padding;
        if (this.decimalBuf.length < maxDigits) {
            limit = this.decimalBuf.length;
        } else {
            limit = maxDigits;
        }
        int index = limit - 1;
        while (true) {
            this.decimalBuf[index] = this.decDigits[value % 10];
            value /= 10;
            if (index == 0 || value == 0) {
                padding = minDigits - (limit - index);
            } else {
                index--;
            }
        }
        padding = minDigits - (limit - index);
        while (padding > 0 && index > 0) {
            index--;
            this.decimalBuf[index] = this.decDigits[0];
            padding--;
        }
        while (padding > 0) {
            buf.append(this.decDigits[0]);
            padding--;
        }
        buf.append(this.decimalBuf, index, limit - index);
    }

    protected String zeroPaddingNumber(long value, int minDigits, int maxDigits) {
        this.numberFormat.setMinimumIntegerDigits(minDigits);
        this.numberFormat.setMaximumIntegerDigits(maxDigits);
        return this.numberFormat.format(value);
    }

    private static final boolean isNumeric(char formatChar, int count) {
        if (NUMERIC_FORMAT_CHARS.indexOf(formatChar) < 0) {
            return count <= 2 && NUMERIC_FORMAT_CHARS2.indexOf(formatChar) >= 0;
        } else {
            return true;
        }
    }

    public void parse(String text, Calendar cal, ParsePosition parsePos) {
        TimeZone backupTZ = null;
        Calendar resultCal = null;
        if (!(cal == this.calendar || cal.getType().equals(this.calendar.getType()))) {
            this.calendar.setTimeInMillis(cal.getTimeInMillis());
            backupTZ = this.calendar.getTimeZone();
            this.calendar.setTimeZone(cal.getTimeZone());
            resultCal = cal;
            cal = this.calendar;
        }
        int pos = parsePos.getIndex();
        if (pos < 0) {
            parsePos.setErrorIndex(0);
            return;
        }
        int start = pos;
        Output<TimeType> tzTimeType = new Output(TimeType.UNKNOWN);
        boolean[] ambiguousYear = new boolean[1];
        ambiguousYear[0] = false;
        int numericFieldStart = -1;
        int numericFieldLength = 0;
        int numericStartPos = 0;
        MessageFormat messageFormat = null;
        if (this.formatData.leapMonthPatterns != null && this.formatData.leapMonthPatterns.length >= 7) {
            messageFormat = new MessageFormat(this.formatData.leapMonthPatterns[6], this.locale);
        }
        Object[] items = getPatternItems();
        int i = 0;
        while (i < items.length) {
            if (items[i] instanceof PatternItem) {
                PatternItem field = items[i];
                if (field.isNumeric && numericFieldStart == -1 && i + 1 < items.length && (items[i + 1] instanceof PatternItem) && ((PatternItem) items[i + 1]).isNumeric) {
                    numericFieldStart = i;
                    numericFieldLength = field.length;
                    numericStartPos = pos;
                }
                if (numericFieldStart != -1) {
                    int len = field.length;
                    if (numericFieldStart == i) {
                        len = numericFieldLength;
                    }
                    pos = subParse(text, pos, field.type, len, true, false, ambiguousYear, cal, messageFormat, tzTimeType);
                    if (pos < 0) {
                        numericFieldLength--;
                        if (numericFieldLength == 0) {
                            parsePos.setIndex(start);
                            parsePos.setErrorIndex(pos);
                            if (backupTZ != null) {
                                this.calendar.setTimeZone(backupTZ);
                            }
                            return;
                        }
                        i = numericFieldStart;
                        pos = numericStartPos;
                    }
                } else if (field.type != 'l') {
                    numericFieldStart = -1;
                    int s = pos;
                    pos = subParse(text, pos, field.type, field.length, false, true, ambiguousYear, cal, messageFormat, tzTimeType);
                    if (pos < 0) {
                        if (pos == ISOSpecialEra) {
                            pos = s;
                            if (i + 1 < items.length) {
                                try {
                                    String patl = items[i + 1];
                                    if (patl == null) {
                                        patl = items[i + 1];
                                    }
                                    int plen = patl.length();
                                    int idx = 0;
                                    while (idx < plen && PatternProps.isWhiteSpace(patl.charAt(idx))) {
                                        idx++;
                                    }
                                    if (idx == plen) {
                                        i++;
                                    }
                                } catch (ClassCastException e) {
                                    parsePos.setIndex(start);
                                    parsePos.setErrorIndex(s);
                                    if (backupTZ != null) {
                                        this.calendar.setTimeZone(backupTZ);
                                    }
                                    return;
                                }
                            }
                        }
                        parsePos.setIndex(start);
                        parsePos.setErrorIndex(s);
                        if (backupTZ != null) {
                            this.calendar.setTimeZone(backupTZ);
                        }
                        return;
                    }
                }
            }
            numericFieldStart = -1;
            boolean[] complete = new boolean[1];
            pos = matchLiteral(text, pos, items, i, complete);
            if (!complete[0]) {
                parsePos.setIndex(start);
                parsePos.setErrorIndex(pos);
                if (backupTZ != null) {
                    this.calendar.setTimeZone(backupTZ);
                }
                return;
            }
            i++;
        }
        if (pos < text.length() && text.charAt(pos) == '.') {
            if (getBooleanAttribute(BooleanAttribute.PARSE_ALLOW_WHITESPACE) && items.length != 0) {
                Object lastItem = items[items.length - 1];
                if ((lastItem instanceof PatternItem) && !((PatternItem) lastItem).isNumeric) {
                    pos++;
                }
            }
        }
        parsePos.setIndex(pos);
        try {
            TimeType tztype = tzTimeType.value;
            if (ambiguousYear[0] || tztype != TimeType.UNKNOWN) {
                if (ambiguousYear[0]) {
                    if (((Calendar) cal.clone()).getTime().before(getDefaultCenturyStart())) {
                        cal.set(1, getDefaultCenturyStartYear() + 100);
                    }
                }
                if (tztype != TimeType.UNKNOWN) {
                    Calendar copy = (Calendar) cal.clone();
                    TimeZone tz = copy.getTimeZone();
                    BasicTimeZone btz = null;
                    if (tz instanceof BasicTimeZone) {
                        btz = (BasicTimeZone) tz;
                    }
                    copy.set(15, 0);
                    copy.set(16, 0);
                    long localMillis = copy.getTimeInMillis();
                    int[] offsets = new int[2];
                    if (btz == null) {
                        tz.getOffset(localMillis, true, offsets);
                        if ((tztype == TimeType.STANDARD && offsets[1] != 0) || (tztype == TimeType.DAYLIGHT && offsets[1] == 0)) {
                            tz.getOffset(localMillis - 86400000, true, offsets);
                        }
                    } else if (tztype == TimeType.STANDARD) {
                        btz.getOffsetFromLocal(localMillis, 1, 1, offsets);
                    } else {
                        btz.getOffsetFromLocal(localMillis, 3, 3, offsets);
                    }
                    int resolvedSavings = offsets[1];
                    if (tztype == TimeType.STANDARD) {
                        if (offsets[1] != 0) {
                            resolvedSavings = 0;
                        }
                    } else if (offsets[1] == 0) {
                        if (btz != null) {
                            TimeZoneTransition beforeTrs;
                            TimeZoneTransition afterTrs;
                            long time = localMillis + ((long) offsets[0]);
                            long beforeT = time;
                            long afterT = time;
                            int beforeSav = 0;
                            int afterSav = 0;
                            do {
                                beforeTrs = btz.getPreviousTransition(beforeT, true);
                                if (beforeTrs == null) {
                                    break;
                                }
                                beforeT = beforeTrs.getTime() - 1;
                                beforeSav = beforeTrs.getFrom().getDSTSavings();
                            } while (beforeSav == 0);
                            do {
                                afterTrs = btz.getNextTransition(afterT, false);
                                if (afterTrs == null) {
                                    break;
                                }
                                afterT = afterTrs.getTime();
                                afterSav = afterTrs.getTo().getDSTSavings();
                            } while (afterSav == 0);
                            if (beforeTrs == null || afterTrs == null) {
                                if (beforeTrs != null && beforeSav != 0) {
                                    resolvedSavings = beforeSav;
                                } else if (afterTrs == null || afterSav == 0) {
                                    resolvedSavings = btz.getDSTSavings();
                                } else {
                                    resolvedSavings = afterSav;
                                }
                            } else if (time - beforeT > afterT - time) {
                                resolvedSavings = afterSav;
                            } else {
                                resolvedSavings = beforeSav;
                            }
                        } else {
                            resolvedSavings = tz.getDSTSavings();
                        }
                        if (resolvedSavings == 0) {
                            resolvedSavings = 3600000;
                        }
                    }
                    cal.set(15, offsets[0]);
                    cal.set(16, resolvedSavings);
                }
            }
            if (resultCal != null) {
                resultCal.setTimeZone(cal.getTimeZone());
                resultCal.setTimeInMillis(cal.getTimeInMillis());
            }
            if (backupTZ != null) {
                this.calendar.setTimeZone(backupTZ);
            }
        } catch (IllegalArgumentException e2) {
            parsePos.setErrorIndex(pos);
            parsePos.setIndex(start);
            if (backupTZ != null) {
                this.calendar.setTimeZone(backupTZ);
            }
        }
    }

    private int matchLiteral(String text, int pos, Object[] items, int itemIndex, boolean[] complete) {
        Object before;
        int originalPos = pos;
        String patternLiteral = items[itemIndex];
        int plen = patternLiteral.length();
        int tlen = text.length();
        int idx = 0;
        while (idx < plen && pos < tlen) {
            char pch = patternLiteral.charAt(idx);
            char ich = text.charAt(pos);
            if (!PatternProps.isWhiteSpace(pch) || !PatternProps.isWhiteSpace(ich)) {
                if (pch != ich) {
                    if (ich == '.' && pos == originalPos && itemIndex > 0) {
                        if (getBooleanAttribute(BooleanAttribute.PARSE_ALLOW_WHITESPACE)) {
                            before = items[itemIndex - 1];
                            if (!(before instanceof PatternItem) || ((PatternItem) before).isNumeric) {
                                break;
                            }
                            pos++;
                        }
                    }
                    if (pch == ' ' || pch == '.') {
                        if (getBooleanAttribute(BooleanAttribute.PARSE_ALLOW_WHITESPACE)) {
                            idx++;
                        }
                    }
                    if (pos == originalPos) {
                        break;
                    }
                    if (!getBooleanAttribute(BooleanAttribute.PARSE_PARTIAL_LITERAL_MATCH)) {
                        break;
                    }
                    idx++;
                }
            } else {
                while (idx + 1 < plen && PatternProps.isWhiteSpace(patternLiteral.charAt(idx + 1))) {
                    idx++;
                }
                while (pos + 1 < tlen) {
                    if (!PatternProps.isWhiteSpace(text.charAt(pos + 1))) {
                        break;
                    }
                    pos++;
                }
            }
            idx++;
            pos++;
        }
        complete[0] = idx == plen;
        if (complete[0]) {
            return pos;
        }
        if (!getBooleanAttribute(BooleanAttribute.PARSE_ALLOW_WHITESPACE) || itemIndex <= 0 || itemIndex >= items.length - 1 || originalPos >= tlen) {
            return pos;
        }
        before = items[itemIndex - 1];
        Object after = items[itemIndex + 1];
        if (!(before instanceof PatternItem) || !(after instanceof PatternItem)) {
            return pos;
        }
        if (DATE_PATTERN_TYPE.contains(((PatternItem) before).type) == DATE_PATTERN_TYPE.contains(((PatternItem) after).type)) {
            return pos;
        }
        int newPos = originalPos;
        while (PatternProps.isWhiteSpace(text.charAt(newPos))) {
            newPos++;
        }
        complete[0] = newPos > originalPos;
        return newPos;
    }

    protected int matchString(String text, int start, int field, String[] data, Calendar cal) {
        return matchString(text, start, field, data, null, cal);
    }

    @Deprecated
    private int matchString(String text, int start, int field, String[] data, String monthPattern, Calendar cal) {
        int i = 0;
        int count = data.length;
        if (field == 7) {
            i = 1;
        }
        int bestMatchLength = 0;
        int bestMatch = -1;
        int isLeapMonth = 0;
        for (i = 
/*
Method generation error in method: android.icu.text.SimpleDateFormat.matchString(java.lang.String, int, int, java.lang.String[], java.lang.String, android.icu.util.Calendar):int, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r4_2 'i' int) = (r4_0 'i' int), (r4_1 'i' int) binds: {(r4_0 'i' int)=B:1:0x0005, (r4_1 'i' int)=B:2:0x0007} in method: android.icu.text.SimpleDateFormat.matchString(java.lang.String, int, int, java.lang.String[], java.lang.String, android.icu.util.Calendar):int, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:183)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:321)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:259)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:221)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 15 more

*/

    private int regionMatchesWithOptionalDot(String text, int start, String data, int length) {
        if (text.regionMatches(true, start, data, 0, length)) {
            return length;
        }
        if (data.length() > 0 && data.charAt(data.length() - 1) == '.') {
            if (text.regionMatches(true, start, data, 0, length - 1)) {
                return length - 1;
            }
        }
        return -1;
    }

    protected int matchQuarterString(String text, int start, int field, String[] data, Calendar cal) {
        int count = data.length;
        int bestMatchLength = 0;
        int bestMatch = -1;
        for (int i = 0; i < count; i++) {
            int length = data[i].length();
            if (length > bestMatchLength) {
                int matchLength = regionMatchesWithOptionalDot(text, start, data[i], length);
                if (matchLength >= 0) {
                    bestMatch = i;
                    bestMatchLength = matchLength;
                }
            }
        }
        if (bestMatch < 0) {
            return -start;
        }
        cal.set(field, bestMatch * 3);
        return start + bestMatchLength;
    }

    protected int subParse(String text, int start, char ch, int count, boolean obeyCount, boolean allowNegative, boolean[] ambiguousYear, Calendar cal) {
        return subParse(text, start, ch, count, obeyCount, allowNegative, ambiguousYear, cal, null, null);
    }

    /* JADX WARNING: Missing block: B:153:0x02b4, code:
            if (getBooleanAttribute(android.icu.text.DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC) != false) goto L_0x02b6;
     */
    /* JADX WARNING: Missing block: B:229:0x041d, code:
            if (getBooleanAttribute(android.icu.text.DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC) != false) goto L_0x041f;
     */
    /* JADX WARNING: Missing block: B:269:0x04e7, code:
            if (getBooleanAttribute(android.icu.text.DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC) != false) goto L_0x04e9;
     */
    /* JADX WARNING: Missing block: B:303:0x05a1, code:
            if (getBooleanAttribute(android.icu.text.DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) == false) goto L_0x05a3;
     */
    /* JADX WARNING: Missing block: B:309:0x05b8, code:
            if (getBooleanAttribute(android.icu.text.DateFormat.BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) != false) goto L_0x05ba;
     */
    /* JADX WARNING: Missing block: B:403:0x0745, code:
            if (getBooleanAttribute(android.icu.text.DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC) != false) goto L_0x0747;
     */
    /* JADX WARNING: Missing block: B:424:0x07b0, code:
            if (getBooleanAttribute(android.icu.text.DateFormat.BooleanAttribute.PARSE_ALLOW_NUMERIC) != false) goto L_0x07b2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @Deprecated
    private int subParse(String text, int start, char ch, int count, boolean obeyCount, boolean allowNegative, boolean[] ambiguousYear, Calendar cal, MessageFormat numericLeapMonthFormatter, Output<TimeType> tzTimeType) {
        Number number = null;
        int value = 0;
        ParsePosition pos = new ParsePosition(0);
        int patternCharIndex = getIndexFromChar(ch);
        if (patternCharIndex == -1) {
            return ~start;
        }
        NumberFormat currentNumberFormat = getNumberFormat(ch);
        int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
        if (numericLeapMonthFormatter != null) {
            numericLeapMonthFormatter.setFormatByArgumentIndex(0, currentNumberFormat);
        }
        boolean isChineseCalendar = !cal.getType().equals("chinese") ? cal.getType().equals("dangi") : true;
        while (start < text.length()) {
            int c = UTF16.charAt(text, start);
            if (UCharacter.isUWhiteSpace(c) && PatternProps.isWhiteSpace(c)) {
                start += UTF16.getCharCount(c);
            } else {
                int newStart;
                pos.setIndex(start);
                if (patternCharIndex == 4 || patternCharIndex == 15 || ((patternCharIndex == 2 && count <= 2) || patternCharIndex == 26 || patternCharIndex == 19 || patternCharIndex == 25 || patternCharIndex == 1 || patternCharIndex == 18 || patternCharIndex == 30 || ((patternCharIndex == 0 && isChineseCalendar) || patternCharIndex == 27 || patternCharIndex == 28 || patternCharIndex == 8))) {
                    boolean parsedNumericLeapMonth = false;
                    if (numericLeapMonthFormatter != null && (patternCharIndex == 2 || patternCharIndex == 26)) {
                        Object[] args = numericLeapMonthFormatter.parse(text, pos);
                        if (args == null || pos.getIndex() <= start || !(args[0] instanceof Number)) {
                            pos.setIndex(start);
                            cal.set(22, 0);
                        } else {
                            parsedNumericLeapMonth = true;
                            number = args[0];
                            cal.set(22, 1);
                        }
                    }
                    if (!parsedNumericLeapMonth) {
                        if (!obeyCount) {
                            number = parseInt(text, pos, allowNegative, currentNumberFormat);
                        } else if (start + count > text.length()) {
                            return ~start;
                        } else {
                            number = parseInt(text, count, pos, allowNegative, currentNumberFormat);
                        }
                        if (number == null && !allowNumericFallback(patternCharIndex)) {
                            return ~start;
                        }
                    }
                    if (number != null) {
                        value = number.intValue();
                    }
                }
                TimeZone tz;
                Style style;
                switch (patternCharIndex) {
                    case 0:
                        if (isChineseCalendar) {
                            cal.set(0, value);
                            return pos.getIndex();
                        }
                        int ps;
                        if (count == 5) {
                            ps = matchString(text, start, 0, this.formatData.narrowEras, null, cal);
                        } else if (count == 4) {
                            ps = matchString(text, start, 0, this.formatData.eraNames, null, cal);
                        } else {
                            ps = matchString(text, start, 0, this.formatData.eras, null, cal);
                        }
                        if (ps == (~start)) {
                            ps = ISOSpecialEra;
                        }
                        return ps;
                    case 1:
                    case 18:
                        if (this.override != null && ((this.override.compareTo("hebr") == 0 || this.override.indexOf("y=hebr") >= 0) && value < 1000)) {
                            value += HEBREW_CAL_CUR_MILLENIUM_START_YEAR;
                        } else if (count == 2 && pos.getIndex() - start == 2 && cal.haveDefaultCentury() && UCharacter.isDigit(text.charAt(start))) {
                            if (UCharacter.isDigit(text.charAt(start + 1))) {
                                int ambiguousTwoDigitYear = getDefaultCenturyStartYear() % 100;
                                ambiguousYear[0] = value == ambiguousTwoDigitYear;
                                value += (value < ambiguousTwoDigitYear ? 100 : 0) + ((getDefaultCenturyStartYear() / 100) * 100);
                            }
                        }
                        cal.set(field, value);
                        if (DelayedHebrewMonthCheck) {
                            if (!HebrewCalendar.isLeapYear(value)) {
                                cal.add(2, 1);
                            }
                            DelayedHebrewMonthCheck = false;
                        }
                        return pos.getIndex();
                    case 2:
                    case 26:
                        if (count > 2) {
                            if (number != null) {
                                break;
                            }
                            boolean haveMonthPat = this.formatData.leapMonthPatterns != null && this.formatData.leapMonthPatterns.length >= 7;
                            newStart = 0;
                            if (getBooleanAttribute(BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || count == 4) {
                                if (patternCharIndex == 2) {
                                    newStart = matchString(text, start, 2, this.formatData.months, haveMonthPat ? this.formatData.leapMonthPatterns[0] : null, cal);
                                } else {
                                    newStart = matchString(text, start, 2, this.formatData.standaloneMonths, haveMonthPat ? this.formatData.leapMonthPatterns[3] : null, cal);
                                }
                                if (newStart > 0) {
                                    return newStart;
                                }
                            }
                            if (!getBooleanAttribute(BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) && count != 3) {
                                return newStart;
                            }
                            int matchString;
                            if (patternCharIndex == 2) {
                                matchString = matchString(text, start, 2, this.formatData.shortMonths, haveMonthPat ? this.formatData.leapMonthPatterns[1] : null, cal);
                            } else {
                                matchString = matchString(text, start, 2, this.formatData.standaloneShortMonths, haveMonthPat ? this.formatData.leapMonthPatterns[4] : null, cal);
                            }
                            return matchString;
                        }
                        cal.set(2, value - 1);
                        if (cal.getType().equals("hebrew") && value >= 6) {
                            if (!cal.isSet(1)) {
                                DelayedHebrewMonthCheck = true;
                            } else if (!HebrewCalendar.isLeapYear(cal.get(1))) {
                                cal.set(2, value);
                            }
                        }
                        return pos.getIndex();
                    case 4:
                        if (value == cal.getMaximum(11) + 1) {
                            value = 0;
                        }
                        cal.set(11, value);
                        return pos.getIndex();
                    case 8:
                        int i = pos.getIndex() - start;
                        if (i < 3) {
                            while (i < 3) {
                                value *= 10;
                                i++;
                            }
                        } else {
                            int a = 1;
                            while (i > 3) {
                                a *= 10;
                                i--;
                            }
                            value /= a;
                        }
                        cal.set(14, value);
                        return pos.getIndex();
                    case 9:
                        break;
                    case 14:
                        if (this.formatData.ampmsNarrow != null && count >= 5) {
                            break;
                        }
                        newStart = matchString(text, start, 9, this.formatData.ampms, null, cal);
                        if (newStart > 0) {
                            return newStart;
                        }
                        if (this.formatData.ampmsNarrow != null) {
                            if (count < 5) {
                                break;
                            }
                            newStart = matchString(text, start, 9, this.formatData.ampmsNarrow, null, cal);
                            if (newStart > 0) {
                                return newStart;
                            }
                        }
                        return ~start;
                    case 15:
                        if (value == cal.getLeastMaximum(10) + 1) {
                            value = 0;
                        }
                        cal.set(10, value);
                        return pos.getIndex();
                    case 17:
                        tz = tzFormat().parse(count < 4 ? Style.SPECIFIC_SHORT : Style.SPECIFIC_LONG, text, pos, tzTimeType);
                        if (tz == null) {
                            return ~start;
                        }
                        cal.setTimeZone(tz);
                        return pos.getIndex();
                    case 19:
                        if (count > 2) {
                            if (number != null) {
                                break;
                            }
                        }
                        cal.set(field, value);
                        return pos.getIndex();
                    case 23:
                        style = count < 4 ? Style.ISO_BASIC_LOCAL_FULL : count == 5 ? Style.ISO_EXTENDED_FULL : Style.LOCALIZED_GMT;
                        tz = tzFormat().parse(style, text, pos, tzTimeType);
                        if (tz == null) {
                            return ~start;
                        }
                        cal.setTimeZone(tz);
                        return pos.getIndex();
                    case 24:
                        tz = tzFormat().parse(count < 4 ? Style.GENERIC_SHORT : Style.GENERIC_LONG, text, pos, tzTimeType);
                        if (tz == null) {
                            return ~start;
                        }
                        cal.setTimeZone(tz);
                        return pos.getIndex();
                    case 25:
                        if (count != 1) {
                            if (number != null) {
                                break;
                            }
                            newStart = 0;
                            if (getBooleanAttribute(BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || count == 4) {
                                newStart = matchString(text, start, 7, this.formatData.standaloneWeekdays, null, cal);
                                if (newStart > 0) {
                                    return newStart;
                                }
                            }
                            if (getBooleanAttribute(BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || count == 3) {
                                newStart = matchString(text, start, 7, this.formatData.standaloneShortWeekdays, null, cal);
                                if (newStart > 0) {
                                    return newStart;
                                }
                            }
                            if ((!getBooleanAttribute(BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) && count != 6) || this.formatData.standaloneShorterWeekdays == null) {
                                return newStart;
                            }
                            return matchString(text, start, 7, this.formatData.standaloneShorterWeekdays, null, cal);
                        }
                        cal.set(field, value);
                        return pos.getIndex();
                    case 27:
                        if (count > 2) {
                            if (number != null) {
                                break;
                            }
                            newStart = 0;
                            if (getBooleanAttribute(BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || count == 4) {
                                newStart = matchQuarterString(text, start, 2, this.formatData.quarters, cal);
                                if (newStart > 0) {
                                    return newStart;
                                }
                            }
                            if (!getBooleanAttribute(BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) && count != 3) {
                                return newStart;
                            }
                            return matchQuarterString(text, start, 2, this.formatData.shortQuarters, cal);
                        }
                        cal.set(2, (value - 1) * 3);
                        return pos.getIndex();
                    case 28:
                        if (count > 2) {
                            if (number != null) {
                                break;
                            }
                            newStart = 0;
                            if (getBooleanAttribute(BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || count == 4) {
                                newStart = matchQuarterString(text, start, 2, this.formatData.standaloneQuarters, cal);
                                if (newStart > 0) {
                                    return newStart;
                                }
                            }
                            if (!getBooleanAttribute(BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) && count != 3) {
                                return newStart;
                            }
                            return matchQuarterString(text, start, 2, this.formatData.standaloneShortQuarters, cal);
                        }
                        cal.set(2, (value - 1) * 3);
                        return pos.getIndex();
                    case 29:
                        switch (count) {
                            case 1:
                                style = Style.ZONE_ID_SHORT;
                                break;
                            case 2:
                                style = Style.ZONE_ID;
                                break;
                            case 3:
                                style = Style.EXEMPLAR_LOCATION;
                                break;
                            default:
                                style = Style.GENERIC_LOCATION;
                                break;
                        }
                        tz = tzFormat().parse(style, text, pos, tzTimeType);
                        if (tz == null) {
                            return ~start;
                        }
                        cal.setTimeZone(tz);
                        return pos.getIndex();
                    case 30:
                        if (this.formatData.shortYearNames != null) {
                            newStart = matchString(text, start, 1, this.formatData.shortYearNames, null, cal);
                            if (newStart > 0) {
                                return newStart;
                            }
                        }
                        if (number != null) {
                            if (getBooleanAttribute(BooleanAttribute.PARSE_ALLOW_NUMERIC) || this.formatData.shortYearNames == null || value > this.formatData.shortYearNames.length) {
                                cal.set(1, value);
                                return pos.getIndex();
                            }
                        }
                        return ~start;
                    case 31:
                        tz = tzFormat().parse(count < 4 ? Style.LOCALIZED_GMT_SHORT : Style.LOCALIZED_GMT, text, pos, tzTimeType);
                        if (tz == null) {
                            return ~start;
                        }
                        cal.setTimeZone(tz);
                        return pos.getIndex();
                    case 32:
                        switch (count) {
                            case 1:
                                style = Style.ISO_BASIC_SHORT;
                                break;
                            case 2:
                                style = Style.ISO_BASIC_FIXED;
                                break;
                            case 3:
                                style = Style.ISO_EXTENDED_FIXED;
                                break;
                            case 4:
                                style = Style.ISO_BASIC_FULL;
                                break;
                            default:
                                style = Style.ISO_EXTENDED_FULL;
                                break;
                        }
                        tz = tzFormat().parse(style, text, pos, tzTimeType);
                        if (tz == null) {
                            return ~start;
                        }
                        cal.setTimeZone(tz);
                        return pos.getIndex();
                    case 33:
                        switch (count) {
                            case 1:
                                style = Style.ISO_BASIC_LOCAL_SHORT;
                                break;
                            case 2:
                                style = Style.ISO_BASIC_LOCAL_FIXED;
                                break;
                            case 3:
                                style = Style.ISO_EXTENDED_LOCAL_FIXED;
                                break;
                            case 4:
                                style = Style.ISO_BASIC_LOCAL_FULL;
                                break;
                            default:
                                style = Style.ISO_EXTENDED_LOCAL_FULL;
                                break;
                        }
                        tz = tzFormat().parse(style, text, pos, tzTimeType);
                        if (tz == null) {
                            return ~start;
                        }
                        cal.setTimeZone(tz);
                        return pos.getIndex();
                    case 35:
                        ArrayList<String> arrayList = new ArrayList(3);
                        arrayList.add(this.formatData.getTimeSeparatorString());
                        if (!this.formatData.getTimeSeparatorString().equals(":")) {
                            arrayList.add(":");
                        }
                        if (getBooleanAttribute(BooleanAttribute.PARSE_PARTIAL_LITERAL_MATCH) && !this.formatData.getTimeSeparatorString().equals(".")) {
                            arrayList.add(".");
                        }
                        return matchString(text, start, -1, (String[]) arrayList.toArray(new String[0]), cal);
                    default:
                        if (!obeyCount) {
                            number = parseInt(text, pos, allowNegative, currentNumberFormat);
                        } else if (start + count > text.length()) {
                            return -start;
                        } else {
                            number = parseInt(text, count, pos, allowNegative, currentNumberFormat);
                        }
                        if (number == null) {
                            return ~start;
                        }
                        if (patternCharIndex != 34) {
                            cal.set(field, number.intValue());
                        } else {
                            cal.setRelatedYear(number.intValue());
                        }
                        return pos.getIndex();
                }
                newStart = 0;
                if (getBooleanAttribute(BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || count == 4) {
                    newStart = matchString(text, start, 7, this.formatData.weekdays, null, cal);
                    if (newStart > 0) {
                        return newStart;
                    }
                }
                if (getBooleanAttribute(BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || count == 3) {
                    newStart = matchString(text, start, 7, this.formatData.shortWeekdays, null, cal);
                    if (newStart > 0) {
                        return newStart;
                    }
                }
                if ((getBooleanAttribute(BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || count == 6) && this.formatData.shorterWeekdays != null) {
                    newStart = matchString(text, start, 7, this.formatData.shorterWeekdays, null, cal);
                    if (newStart > 0) {
                        return newStart;
                    }
                }
                if ((getBooleanAttribute(BooleanAttribute.PARSE_MULTIPLE_PATTERNS_FOR_MATCH) || count == 5) && this.formatData.narrowWeekdays != null) {
                    newStart = matchString(text, start, 7, this.formatData.narrowWeekdays, null, cal);
                    if (newStart > 0) {
                        return newStart;
                    }
                }
                return newStart;
            }
        }
        return ~start;
    }

    private boolean allowNumericFallback(int patternCharIndex) {
        if (patternCharIndex == 26 || patternCharIndex == 19 || patternCharIndex == 25 || patternCharIndex == 30 || patternCharIndex == 27 || patternCharIndex == 28) {
            return true;
        }
        return false;
    }

    private Number parseInt(String text, ParsePosition pos, boolean allowNegative, NumberFormat fmt) {
        return parseInt(text, -1, pos, allowNegative, fmt);
    }

    private Number parseInt(String text, int maxDigits, ParsePosition pos, boolean allowNegative, NumberFormat fmt) {
        Number number;
        int oldPos = pos.getIndex();
        if (allowNegative) {
            number = fmt.parse(text, pos);
        } else if (fmt instanceof DecimalFormat) {
            String oldPrefix = ((DecimalFormat) fmt).getNegativePrefix();
            ((DecimalFormat) fmt).setNegativePrefix(SUPPRESS_NEGATIVE_PREFIX);
            number = fmt.parse(text, pos);
            ((DecimalFormat) fmt).setNegativePrefix(oldPrefix);
        } else {
            boolean dateNumberFormat = fmt instanceof DateNumberFormat;
            if (dateNumberFormat) {
                ((DateNumberFormat) fmt).setParsePositiveOnly(true);
            }
            number = fmt.parse(text, pos);
            if (dateNumberFormat) {
                ((DateNumberFormat) fmt).setParsePositiveOnly(false);
            }
        }
        if (maxDigits <= 0) {
            return number;
        }
        int nDigits = pos.getIndex() - oldPos;
        if (nDigits <= maxDigits) {
            return number;
        }
        double val = number.doubleValue();
        for (nDigits -= maxDigits; nDigits > 0; nDigits--) {
            val /= 10.0d;
        }
        pos.setIndex(oldPos + maxDigits);
        return Integer.valueOf((int) val);
    }

    private String translatePattern(String pat, String from, String to) {
        StringBuilder result = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < pat.length(); i++) {
            char c = pat.charAt(i);
            if (inQuote) {
                if (c == PatternTokenizer.SINGLE_QUOTE) {
                    inQuote = false;
                }
            } else if (c == PatternTokenizer.SINGLE_QUOTE) {
                inQuote = true;
            } else if (isSyntaxChar(c)) {
                int ci = from.indexOf(c);
                if (ci != -1) {
                    c = to.charAt(ci);
                }
            }
            result.append(c);
        }
        if (!inQuote) {
            return result.toString();
        }
        throw new IllegalArgumentException("Unfinished quote in pattern");
    }

    public String toPattern() {
        return this.pattern;
    }

    public String toLocalizedPattern() {
        return translatePattern(this.pattern, "GyMdkHmsSEDFwWahKzYeugAZvcLQqVUOXxr", this.formatData.localPatternChars);
    }

    public void applyPattern(String pat) {
        this.pattern = pat;
        setLocale(null, null);
        this.patternItems = null;
    }

    public void applyLocalizedPattern(String pat) {
        this.pattern = translatePattern(pat, this.formatData.localPatternChars, "GyMdkHmsSEDFwWahKzYeugAZvcLQqVUOXxr");
        setLocale(null, null);
    }

    public DateFormatSymbols getDateFormatSymbols() {
        return (DateFormatSymbols) this.formatData.clone();
    }

    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols) {
        this.formatData = (DateFormatSymbols) newFormatSymbols.clone();
    }

    protected DateFormatSymbols getSymbols() {
        return this.formatData;
    }

    public TimeZoneFormat getTimeZoneFormat() {
        return tzFormat().freeze();
    }

    public void setTimeZoneFormat(TimeZoneFormat tzfmt) {
        if (tzfmt.isFrozen()) {
            this.tzFormat = tzfmt;
        } else {
            this.tzFormat = tzfmt.cloneAsThawed().freeze();
        }
    }

    public Object clone() {
        SimpleDateFormat other = (SimpleDateFormat) super.clone();
        other.formatData = (DateFormatSymbols) this.formatData.clone();
        if (this.decimalBuf != null) {
            other.decimalBuf = new char[10];
        }
        return other;
    }

    public int hashCode() {
        return this.pattern.hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!super.equals(obj)) {
            return false;
        }
        SimpleDateFormat that = (SimpleDateFormat) obj;
        if (this.pattern.equals(that.pattern)) {
            z = this.formatData.equals(that.formatData);
        }
        return z;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        if (this.defaultCenturyStart == null) {
            initializeDefaultCenturyStart(this.defaultCenturyBase);
        }
        initializeTimeZoneFormat(false);
        stream.defaultWriteObject();
        stream.writeInt(getContext(Type.CAPITALIZATION).value());
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        int capitalizationSettingValue = this.serialVersionOnStream > 1 ? stream.readInt() : -1;
        if (this.serialVersionOnStream < 1) {
            this.defaultCenturyBase = System.currentTimeMillis();
        } else {
            parseAmbiguousDatesAsAfter(this.defaultCenturyStart);
        }
        this.serialVersionOnStream = 2;
        this.locale = getLocale(ULocale.VALID_LOCALE);
        if (this.locale == null) {
            this.locale = ULocale.getDefault(Category.FORMAT);
        }
        initLocalZeroPaddingNumberFormat();
        setContext(DisplayContext.CAPITALIZATION_NONE);
        if (capitalizationSettingValue >= 0) {
            for (DisplayContext context : DisplayContext.values()) {
                if (context.value() == capitalizationSettingValue) {
                    setContext(context);
                    break;
                }
            }
        }
        if (!getBooleanAttribute(BooleanAttribute.PARSE_PARTIAL_MATCH)) {
            setBooleanAttribute(BooleanAttribute.PARSE_PARTIAL_LITERAL_MATCH, false);
        }
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        Calendar cal = this.calendar;
        if (obj instanceof Calendar) {
            cal = (Calendar) obj;
        } else if (obj instanceof Date) {
            this.calendar.setTime((Date) obj);
        } else if (obj instanceof Number) {
            this.calendar.setTimeInMillis(((Number) obj).longValue());
        } else {
            throw new IllegalArgumentException("Cannot format given Object as a Date");
        }
        StringBuffer toAppendTo = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        List<FieldPosition> attributes = new ArrayList();
        format(cal, getContext(Type.CAPITALIZATION), toAppendTo, pos, attributes);
        AttributedString as = new AttributedString(toAppendTo.toString());
        for (int i = 0; i < attributes.size(); i++) {
            FieldPosition fp = (FieldPosition) attributes.get(i);
            Format.Field attribute = fp.getFieldAttribute();
            as.addAttribute(attribute, attribute, fp.getBeginIndex(), fp.getEndIndex());
        }
        return as.getIterator();
    }

    ULocale getLocale() {
        return this.locale;
    }

    boolean isFieldUnitIgnored(int field) {
        return isFieldUnitIgnored(this.pattern, field);
    }

    static boolean isFieldUnitIgnored(String pattern, int field) {
        int fieldLevel = CALENDAR_FIELD_TO_LEVEL[field];
        boolean inQuote = false;
        char prevCh = 0;
        int count = 0;
        int i = 0;
        while (i < pattern.length()) {
            char ch = pattern.charAt(i);
            if (ch != prevCh && count > 0) {
                if (fieldLevel <= getLevelFromChar(prevCh)) {
                    return false;
                }
                count = 0;
            }
            if (ch == PatternTokenizer.SINGLE_QUOTE) {
                if (i + 1 >= pattern.length() || pattern.charAt(i + 1) != PatternTokenizer.SINGLE_QUOTE) {
                    inQuote = !inQuote;
                } else {
                    i++;
                }
            } else if (!inQuote && isSyntaxChar(ch)) {
                prevCh = ch;
                count++;
            }
            i++;
        }
        if (count <= 0 || fieldLevel > getLevelFromChar(prevCh)) {
            return true;
        }
        return false;
    }

    @Deprecated
    public final StringBuffer intervalFormatByAlgorithm(Calendar fromCalendar, Calendar toCalendar, StringBuffer appendTo, FieldPosition pos) throws IllegalArgumentException {
        if (fromCalendar.isEquivalentTo(toCalendar)) {
            Object[] items = getPatternItems();
            int diffBegin = -1;
            int diffEnd = -1;
            int i = 0;
            while (i < items.length) {
                try {
                    if (diffCalFieldValue(fromCalendar, toCalendar, items, i)) {
                        diffBegin = i;
                        break;
                    }
                    i++;
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(e.toString());
                }
            }
            if (diffBegin == -1) {
                return format(fromCalendar, appendTo, pos);
            }
            for (i = items.length - 1; i >= diffBegin; i--) {
                if (diffCalFieldValue(fromCalendar, toCalendar, items, i)) {
                    diffEnd = i;
                    break;
                }
            }
            if (diffBegin == 0 && diffEnd == items.length - 1) {
                format(fromCalendar, appendTo, pos);
                appendTo.append(" – ");
                format(toCalendar, appendTo, pos);
                return appendTo;
            }
            int highestLevel = 1000;
            for (i = diffBegin; i <= diffEnd; i++) {
                if (!(items[i] instanceof String)) {
                    char ch = items[i].type;
                    int patternCharIndex = getIndexFromChar(ch);
                    if (patternCharIndex == -1) {
                        throw new IllegalArgumentException("Illegal pattern character '" + ch + "' in \"" + this.pattern + '\"');
                    } else if (patternCharIndex < highestLevel) {
                        highestLevel = patternCharIndex;
                    }
                }
            }
            i = 0;
            while (i < diffBegin) {
                try {
                    if (lowerLevel(items, i, highestLevel)) {
                        diffBegin = i;
                        break;
                    }
                    i++;
                } catch (IllegalArgumentException e2) {
                    throw new IllegalArgumentException(e2.toString());
                }
            }
            for (i = items.length - 1; i > diffEnd; i--) {
                if (lowerLevel(items, i, highestLevel)) {
                    diffEnd = i;
                    break;
                }
            }
            if (diffBegin == 0 && diffEnd == items.length - 1) {
                format(fromCalendar, appendTo, pos);
                appendTo.append(" – ");
                format(toCalendar, appendTo, pos);
                return appendTo;
            }
            PatternItem item;
            StringBuffer stringBuffer;
            pos.setBeginIndex(0);
            pos.setEndIndex(0);
            DisplayContext capSetting = getContext(Type.CAPITALIZATION);
            for (i = 0; i <= diffEnd; i++) {
                if (items[i] instanceof String) {
                    appendTo.append((String) items[i]);
                } else {
                    item = (PatternItem) items[i];
                    if (this.useFastFormat) {
                        subFormat(appendTo, item.type, item.length, appendTo.length(), i, capSetting, pos, fromCalendar);
                    } else {
                        stringBuffer = appendTo;
                        stringBuffer.append(subFormat(item.type, item.length, appendTo.length(), i, capSetting, pos, fromCalendar));
                    }
                }
            }
            appendTo.append(" – ");
            for (i = diffBegin; i < items.length; i++) {
                if (items[i] instanceof String) {
                    appendTo.append((String) items[i]);
                } else {
                    item = (PatternItem) items[i];
                    if (this.useFastFormat) {
                        subFormat(appendTo, item.type, item.length, appendTo.length(), i, capSetting, pos, toCalendar);
                    } else {
                        stringBuffer = appendTo;
                        stringBuffer.append(subFormat(item.type, item.length, appendTo.length(), i, capSetting, pos, toCalendar));
                    }
                }
            }
            return appendTo;
        }
        throw new IllegalArgumentException("can not format on two different calendars");
    }

    private boolean diffCalFieldValue(Calendar fromCalendar, Calendar toCalendar, Object[] items, int i) throws IllegalArgumentException {
        if (items[i] instanceof String) {
            return false;
        }
        char ch = items[i].type;
        int patternCharIndex = getIndexFromChar(ch);
        if (patternCharIndex == -1) {
            throw new IllegalArgumentException("Illegal pattern character '" + ch + "' in \"" + this.pattern + '\"');
        }
        int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
        if (field < 0 || fromCalendar.get(field) == toCalendar.get(field)) {
            return false;
        }
        return true;
    }

    private boolean lowerLevel(Object[] items, int i, int level) throws IllegalArgumentException {
        if (items[i] instanceof String) {
            return false;
        }
        char ch = items[i].type;
        int patternCharIndex = getLevelFromChar(ch);
        if (patternCharIndex == -1) {
            throw new IllegalArgumentException("Illegal pattern character '" + ch + "' in \"" + this.pattern + '\"');
        } else if (patternCharIndex >= level) {
            return true;
        } else {
            return false;
        }
    }

    public void setNumberFormat(String fields, NumberFormat overrideNF) {
        overrideNF.setGroupingUsed(false);
        String nsName = "$" + UUID.randomUUID().toString();
        if (this.numberFormatters == null) {
            this.numberFormatters = new HashMap();
        }
        if (this.overrideMap == null) {
            this.overrideMap = new HashMap();
        }
        for (int i = 0; i < fields.length(); i++) {
            char field = fields.charAt(i);
            if ("GyMdkHmsSEDFwWahKzYeugAZvcLQqVUOXxr".indexOf(field) == -1) {
                throw new IllegalArgumentException("Illegal field character '" + field + "' in setNumberFormat.");
            }
            this.overrideMap.put(Character.valueOf(field), nsName);
            this.numberFormatters.put(nsName, overrideNF);
        }
        this.useLocalZeroPaddingNumberFormat = false;
    }

    public NumberFormat getNumberFormat(char field) {
        Character ovrField = Character.valueOf(field);
        if (this.overrideMap == null || !this.overrideMap.containsKey(ovrField)) {
            return this.numberFormat;
        }
        return (NumberFormat) this.numberFormatters.get(((String) this.overrideMap.get(ovrField)).toString());
    }

    private void initNumberFormatters(ULocale loc) {
        this.numberFormatters = new HashMap();
        this.overrideMap = new HashMap();
        processOverrideString(loc, this.override);
    }

    private void processOverrideString(ULocale loc, String str) {
        if (str != null && str.length() != 0) {
            int start = 0;
            boolean moreToProcess = true;
            while (moreToProcess) {
                int end;
                String nsName;
                boolean fullOverride;
                int delimiterPosition = str.indexOf(";", start);
                if (delimiterPosition == -1) {
                    moreToProcess = false;
                    end = str.length();
                } else {
                    end = delimiterPosition;
                }
                String currentString = str.substring(start, end);
                int equalSignPosition = currentString.indexOf("=");
                if (equalSignPosition == -1) {
                    nsName = currentString;
                    fullOverride = true;
                } else {
                    nsName = currentString.substring(equalSignPosition + 1);
                    this.overrideMap.put(Character.valueOf(currentString.charAt(0)), nsName);
                    fullOverride = false;
                }
                NumberFormat nf = NumberFormat.createInstance(new ULocale(loc.getBaseName() + "@numbers=" + nsName), 0);
                nf.setGroupingUsed(false);
                if (fullOverride) {
                    setNumberFormat(nf);
                } else {
                    this.useLocalZeroPaddingNumberFormat = false;
                }
                if (!(fullOverride || this.numberFormatters.containsKey(nsName))) {
                    this.numberFormatters.put(nsName, nf);
                }
                start = delimiterPosition + 1;
            }
        }
    }
}
