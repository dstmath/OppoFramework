package android.icu.text;

import android.icu.impl.CalendarData;
import android.icu.impl.CalendarUtil;
import android.icu.impl.ICUCache;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.Utility;
import android.icu.text.TimeZoneNames.NameType;
import android.icu.util.Calendar;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.ULocale.Type;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
public class DateFormatSymbols implements Serializable, Cloneable {
    public static final int ABBREVIATED = 0;
    static final String ALTERNATE_TIME_SEPARATOR = ".";
    private static final String[][] CALENDAR_CLASSES = null;
    static final String DEFAULT_TIME_SEPARATOR = ":";
    private static ICUCache<String, DateFormatSymbols> DFSCACHE = null;
    @Deprecated
    public static final int DT_CONTEXT_COUNT = 3;
    static final int DT_LEAP_MONTH_PATTERN_FORMAT_ABBREV = 1;
    static final int DT_LEAP_MONTH_PATTERN_FORMAT_NARROW = 2;
    static final int DT_LEAP_MONTH_PATTERN_FORMAT_WIDE = 0;
    static final int DT_LEAP_MONTH_PATTERN_NUMERIC = 6;
    static final int DT_LEAP_MONTH_PATTERN_STANDALONE_ABBREV = 4;
    static final int DT_LEAP_MONTH_PATTERN_STANDALONE_NARROW = 5;
    static final int DT_LEAP_MONTH_PATTERN_STANDALONE_WIDE = 3;
    static final int DT_MONTH_PATTERN_COUNT = 7;
    @Deprecated
    public static final int DT_WIDTH_COUNT = 4;
    public static final int FORMAT = 0;
    public static final int NARROW = 2;
    @Deprecated
    public static final int NUMERIC = 2;
    public static final int SHORT = 3;
    public static final int STANDALONE = 1;
    public static final int WIDE = 1;
    private static final Map<String, CapitalizationContextUsage> contextUsageTypeMap = null;
    static final int millisPerHour = 3600000;
    static final String patternChars = "GyMdkHmsSEDFwWahKzYeugAZvcLQqVUOXxr";
    private static final long serialVersionUID = -5987973545549424702L;
    private ULocale actualLocale;
    String[] ampms;
    String[] ampmsNarrow;
    Map<CapitalizationContextUsage, boolean[]> capitalization;
    String[] eraNames;
    String[] eras;
    String[] leapMonthPatterns;
    String localPatternChars;
    String[] months;
    String[] narrowEras;
    String[] narrowMonths;
    String[] narrowWeekdays;
    String[] quarters;
    private ULocale requestedLocale;
    String[] shortMonths;
    String[] shortQuarters;
    String[] shortWeekdays;
    String[] shortYearNames;
    String[] shortZodiacNames;
    String[] shorterWeekdays;
    String[] standaloneMonths;
    String[] standaloneNarrowMonths;
    String[] standaloneNarrowWeekdays;
    String[] standaloneQuarters;
    String[] standaloneShortMonths;
    String[] standaloneShortQuarters;
    String[] standaloneShortWeekdays;
    String[] standaloneShorterWeekdays;
    String[] standaloneWeekdays;
    private String timeSeparator;
    private ULocale validLocale;
    String[] weekdays;
    private String[][] zoneStrings;

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
    enum CapitalizationContextUsage {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.DateFormatSymbols.CapitalizationContextUsage.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.DateFormatSymbols.CapitalizationContextUsage.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateFormatSymbols.CapitalizationContextUsage.<clinit>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.DateFormatSymbols.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.DateFormatSymbols.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateFormatSymbols.<clinit>():void");
    }

    public DateFormatSymbols() {
        this(ULocale.getDefault(Category.FORMAT));
    }

    public DateFormatSymbols(Locale locale) {
        this(ULocale.forLocale(locale));
    }

    public DateFormatSymbols(ULocale locale) {
        this.eras = null;
        this.eraNames = null;
        this.narrowEras = null;
        this.months = null;
        this.shortMonths = null;
        this.narrowMonths = null;
        this.standaloneMonths = null;
        this.standaloneShortMonths = null;
        this.standaloneNarrowMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.shorterWeekdays = null;
        this.narrowWeekdays = null;
        this.standaloneWeekdays = null;
        this.standaloneShortWeekdays = null;
        this.standaloneShorterWeekdays = null;
        this.standaloneNarrowWeekdays = null;
        this.ampms = null;
        this.ampmsNarrow = null;
        this.timeSeparator = null;
        this.shortQuarters = null;
        this.quarters = null;
        this.standaloneShortQuarters = null;
        this.standaloneQuarters = null;
        this.leapMonthPatterns = null;
        this.shortYearNames = null;
        this.shortZodiacNames = null;
        this.zoneStrings = null;
        this.localPatternChars = null;
        this.capitalization = null;
        initializeData(locale, CalendarUtil.getCalendarType(locale));
    }

    public static DateFormatSymbols getInstance() {
        return new DateFormatSymbols();
    }

    public static DateFormatSymbols getInstance(Locale locale) {
        return new DateFormatSymbols(locale);
    }

    public static DateFormatSymbols getInstance(ULocale locale) {
        return new DateFormatSymbols(locale);
    }

    public static Locale[] getAvailableLocales() {
        return ICUResourceBundle.getAvailableLocales();
    }

    public static ULocale[] getAvailableULocales() {
        return ICUResourceBundle.getAvailableULocales();
    }

    public String[] getEras() {
        return duplicate(this.eras);
    }

    public void setEras(String[] newEras) {
        this.eras = duplicate(newEras);
    }

    public String[] getEraNames() {
        return duplicate(this.eraNames);
    }

    public void setEraNames(String[] newEraNames) {
        this.eraNames = duplicate(newEraNames);
    }

    public String[] getMonths() {
        return duplicate(this.months);
    }

    public String[] getMonths(int context, int width) {
        String[] returnValue = null;
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                    case 3:
                        returnValue = this.shortMonths;
                        break;
                    case 1:
                        returnValue = this.months;
                        break;
                    case 2:
                        returnValue = this.narrowMonths;
                        break;
                }
                break;
            case 1:
                switch (width) {
                    case 0:
                    case 3:
                        returnValue = this.standaloneShortMonths;
                        break;
                    case 1:
                        returnValue = this.standaloneMonths;
                        break;
                    case 2:
                        returnValue = this.standaloneNarrowMonths;
                        break;
                }
                break;
        }
        if (returnValue != null) {
            return duplicate(returnValue);
        }
        throw new IllegalArgumentException("Bad context or width argument");
    }

    public void setMonths(String[] newMonths) {
        this.months = duplicate(newMonths);
    }

    public void setMonths(String[] newMonths, int context, int width) {
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                        this.shortMonths = duplicate(newMonths);
                        return;
                    case 1:
                        this.months = duplicate(newMonths);
                        return;
                    case 2:
                        this.narrowMonths = duplicate(newMonths);
                        return;
                    default:
                        return;
                }
            case 1:
                switch (width) {
                    case 0:
                        this.standaloneShortMonths = duplicate(newMonths);
                        return;
                    case 1:
                        this.standaloneMonths = duplicate(newMonths);
                        return;
                    case 2:
                        this.standaloneNarrowMonths = duplicate(newMonths);
                        return;
                    default:
                        return;
                }
            default:
                return;
        }
    }

    public String[] getShortMonths() {
        return duplicate(this.shortMonths);
    }

    public void setShortMonths(String[] newShortMonths) {
        this.shortMonths = duplicate(newShortMonths);
    }

    public String[] getWeekdays() {
        return duplicate(this.weekdays);
    }

    public String[] getWeekdays(int context, int width) {
        String[] returnValue = null;
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                        returnValue = this.shortWeekdays;
                        break;
                    case 1:
                        returnValue = this.weekdays;
                        break;
                    case 2:
                        returnValue = this.narrowWeekdays;
                        break;
                    case 3:
                        if (this.shorterWeekdays == null) {
                            returnValue = this.shortWeekdays;
                            break;
                        }
                        returnValue = this.shorterWeekdays;
                        break;
                }
                break;
            case 1:
                switch (width) {
                    case 0:
                        returnValue = this.standaloneShortWeekdays;
                        break;
                    case 1:
                        returnValue = this.standaloneWeekdays;
                        break;
                    case 2:
                        returnValue = this.standaloneNarrowWeekdays;
                        break;
                    case 3:
                        if (this.standaloneShorterWeekdays == null) {
                            returnValue = this.standaloneShortWeekdays;
                            break;
                        }
                        returnValue = this.standaloneShorterWeekdays;
                        break;
                }
                break;
        }
        if (returnValue != null) {
            return duplicate(returnValue);
        }
        throw new IllegalArgumentException("Bad context or width argument");
    }

    public void setWeekdays(String[] newWeekdays, int context, int width) {
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                        this.shortWeekdays = duplicate(newWeekdays);
                        return;
                    case 1:
                        this.weekdays = duplicate(newWeekdays);
                        return;
                    case 2:
                        this.narrowWeekdays = duplicate(newWeekdays);
                        return;
                    case 3:
                        this.shorterWeekdays = duplicate(newWeekdays);
                        return;
                    default:
                        return;
                }
            case 1:
                switch (width) {
                    case 0:
                        this.standaloneShortWeekdays = duplicate(newWeekdays);
                        return;
                    case 1:
                        this.standaloneWeekdays = duplicate(newWeekdays);
                        return;
                    case 2:
                        this.standaloneNarrowWeekdays = duplicate(newWeekdays);
                        return;
                    case 3:
                        this.standaloneShorterWeekdays = duplicate(newWeekdays);
                        return;
                    default:
                        return;
                }
            default:
                return;
        }
    }

    public void setWeekdays(String[] newWeekdays) {
        this.weekdays = duplicate(newWeekdays);
    }

    public String[] getShortWeekdays() {
        return duplicate(this.shortWeekdays);
    }

    public void setShortWeekdays(String[] newAbbrevWeekdays) {
        this.shortWeekdays = duplicate(newAbbrevWeekdays);
    }

    public String[] getQuarters(int context, int width) {
        String[] returnValue = null;
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                    case 3:
                        returnValue = this.shortQuarters;
                        break;
                    case 1:
                        returnValue = this.quarters;
                        break;
                    case 2:
                        returnValue = null;
                        break;
                }
                break;
            case 1:
                switch (width) {
                    case 0:
                    case 3:
                        returnValue = this.standaloneShortQuarters;
                        break;
                    case 1:
                        returnValue = this.standaloneQuarters;
                        break;
                    case 2:
                        returnValue = null;
                        break;
                }
                break;
        }
        if (returnValue != null) {
            return duplicate(returnValue);
        }
        throw new IllegalArgumentException("Bad context or width argument");
    }

    public void setQuarters(String[] newQuarters, int context, int width) {
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                        this.shortQuarters = duplicate(newQuarters);
                        return;
                    case 1:
                        this.quarters = duplicate(newQuarters);
                        return;
                    default:
                        return;
                }
            case 1:
                switch (width) {
                    case 0:
                        this.standaloneShortQuarters = duplicate(newQuarters);
                        return;
                    case 1:
                        this.standaloneQuarters = duplicate(newQuarters);
                        return;
                    default:
                        return;
                }
            default:
                return;
        }
    }

    public String[] getYearNames(int context, int width) {
        if (this.shortYearNames != null) {
            return duplicate(this.shortYearNames);
        }
        return null;
    }

    public void setYearNames(String[] yearNames, int context, int width) {
        if (context == 0 && width == 0) {
            this.shortYearNames = duplicate(yearNames);
        }
    }

    public String[] getZodiacNames(int context, int width) {
        if (this.shortZodiacNames != null) {
            return duplicate(this.shortZodiacNames);
        }
        return null;
    }

    public void setZodiacNames(String[] zodiacNames, int context, int width) {
        if (context == 0 && width == 0) {
            this.shortZodiacNames = duplicate(zodiacNames);
        }
    }

    @Deprecated
    public String getLeapMonthPattern(int context, int width) {
        if (this.leapMonthPatterns == null) {
            return null;
        }
        int leapMonthPatternIndex = -1;
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                    case 3:
                        leapMonthPatternIndex = 1;
                        break;
                    case 1:
                        leapMonthPatternIndex = 0;
                        break;
                    case 2:
                        leapMonthPatternIndex = 2;
                        break;
                }
                break;
            case 1:
                switch (width) {
                    case 0:
                    case 3:
                        leapMonthPatternIndex = 1;
                        break;
                    case 1:
                        leapMonthPatternIndex = 3;
                        break;
                    case 2:
                        leapMonthPatternIndex = 5;
                        break;
                }
                break;
            case 2:
                leapMonthPatternIndex = 6;
                break;
        }
        if (leapMonthPatternIndex >= 0) {
            return this.leapMonthPatterns[leapMonthPatternIndex];
        }
        throw new IllegalArgumentException("Bad context or width argument");
    }

    @Deprecated
    public void setLeapMonthPattern(String leapMonthPattern, int context, int width) {
        if (this.leapMonthPatterns != null) {
            int leapMonthPatternIndex = -1;
            switch (context) {
                case 0:
                    switch (width) {
                        case 0:
                            leapMonthPatternIndex = 1;
                            break;
                        case 1:
                            leapMonthPatternIndex = 0;
                            break;
                        case 2:
                            leapMonthPatternIndex = 2;
                            break;
                    }
                    break;
                case 1:
                    switch (width) {
                        case 0:
                            leapMonthPatternIndex = 1;
                            break;
                        case 1:
                            leapMonthPatternIndex = 3;
                            break;
                        case 2:
                            leapMonthPatternIndex = 5;
                            break;
                    }
                    break;
                case 2:
                    leapMonthPatternIndex = 6;
                    break;
            }
            if (leapMonthPatternIndex >= 0) {
                this.leapMonthPatterns[leapMonthPatternIndex] = leapMonthPattern;
            }
        }
    }

    public String[] getAmPmStrings() {
        return duplicate(this.ampms);
    }

    public void setAmPmStrings(String[] newAmpms) {
        this.ampms = duplicate(newAmpms);
    }

    public String getTimeSeparatorString() {
        return this.timeSeparator;
    }

    public void setTimeSeparatorString(String newTimeSeparator) {
        this.timeSeparator = newTimeSeparator;
    }

    public String[][] getZoneStrings() {
        if (this.zoneStrings != null) {
            return duplicate(this.zoneStrings);
        }
        String[] tzIDs = TimeZone.getAvailableIDs();
        TimeZoneNames tznames = TimeZoneNames.getInstance(this.validLocale);
        tznames.loadAllDisplayNames();
        NameType[] types = new NameType[4];
        types[0] = NameType.LONG_STANDARD;
        types[1] = NameType.SHORT_STANDARD;
        types[2] = NameType.LONG_DAYLIGHT;
        types[3] = NameType.SHORT_DAYLIGHT;
        long now = System.currentTimeMillis();
        int[] iArr = new int[2];
        iArr[0] = tzIDs.length;
        iArr[1] = 5;
        String[][] array = (String[][]) Array.newInstance(String.class, iArr);
        for (int i = 0; i < tzIDs.length; i++) {
            String canonicalID = TimeZone.getCanonicalID(tzIDs[i]);
            if (canonicalID == null) {
                canonicalID = tzIDs[i];
            }
            array[i][0] = tzIDs[i];
            tznames.getDisplayNames(canonicalID, types, now, array[i], 1);
        }
        this.zoneStrings = array;
        return this.zoneStrings;
    }

    public void setZoneStrings(String[][] newZoneStrings) {
        this.zoneStrings = duplicate(newZoneStrings);
    }

    public String getLocalPatternChars() {
        return this.localPatternChars;
    }

    public void setLocalPatternChars(String newLocalPatternChars) {
        this.localPatternChars = newLocalPatternChars;
    }

    public Object clone() {
        try {
            return (DateFormatSymbols) super.clone();
        } catch (Throwable e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    public int hashCode() {
        return this.requestedLocale.toString().hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DateFormatSymbols that = (DateFormatSymbols) obj;
        if (Utility.arrayEquals(this.eras, that.eras) && Utility.arrayEquals(this.eraNames, that.eraNames) && Utility.arrayEquals(this.months, that.months) && Utility.arrayEquals(this.shortMonths, that.shortMonths) && Utility.arrayEquals(this.narrowMonths, that.narrowMonths) && Utility.arrayEquals(this.standaloneMonths, that.standaloneMonths) && Utility.arrayEquals(this.standaloneShortMonths, that.standaloneShortMonths) && Utility.arrayEquals(this.standaloneNarrowMonths, that.standaloneNarrowMonths) && Utility.arrayEquals(this.weekdays, that.weekdays) && Utility.arrayEquals(this.shortWeekdays, that.shortWeekdays) && Utility.arrayEquals(this.shorterWeekdays, that.shorterWeekdays) && Utility.arrayEquals(this.narrowWeekdays, that.narrowWeekdays) && Utility.arrayEquals(this.standaloneWeekdays, that.standaloneWeekdays) && Utility.arrayEquals(this.standaloneShortWeekdays, that.standaloneShortWeekdays) && Utility.arrayEquals(this.standaloneShorterWeekdays, that.standaloneShorterWeekdays) && Utility.arrayEquals(this.standaloneNarrowWeekdays, that.standaloneNarrowWeekdays) && Utility.arrayEquals(this.ampms, that.ampms) && Utility.arrayEquals(this.ampmsNarrow, that.ampmsNarrow) && Utility.arrayEquals(this.timeSeparator, that.timeSeparator) && arrayOfArrayEquals(this.zoneStrings, that.zoneStrings) && this.requestedLocale.getDisplayName().equals(that.requestedLocale.getDisplayName())) {
            z = Utility.arrayEquals(this.localPatternChars, that.localPatternChars);
        }
        return z;
    }

    protected void initializeData(ULocale desiredLocale, String type) {
        String key = desiredLocale.getBaseName() + "+" + type;
        String ns = desiredLocale.getKeywordValue("numbers");
        if (ns != null && ns.length() > 0) {
            key = key + "+" + ns;
        }
        DateFormatSymbols dfs = (DateFormatSymbols) DFSCACHE.get(key);
        if (dfs == null) {
            initializeData(desiredLocale, new CalendarData(desiredLocale, type));
            if (getClass().getName().equals("android.icu.text.DateFormatSymbols")) {
                DFSCACHE.put(key, (DateFormatSymbols) clone());
                return;
            }
            return;
        }
        initializeData(dfs);
    }

    void initializeData(DateFormatSymbols dfs) {
        this.eras = dfs.eras;
        this.eraNames = dfs.eraNames;
        this.narrowEras = dfs.narrowEras;
        this.months = dfs.months;
        this.shortMonths = dfs.shortMonths;
        this.narrowMonths = dfs.narrowMonths;
        this.standaloneMonths = dfs.standaloneMonths;
        this.standaloneShortMonths = dfs.standaloneShortMonths;
        this.standaloneNarrowMonths = dfs.standaloneNarrowMonths;
        this.weekdays = dfs.weekdays;
        this.shortWeekdays = dfs.shortWeekdays;
        this.shorterWeekdays = dfs.shorterWeekdays;
        this.narrowWeekdays = dfs.narrowWeekdays;
        this.standaloneWeekdays = dfs.standaloneWeekdays;
        this.standaloneShortWeekdays = dfs.standaloneShortWeekdays;
        this.standaloneShorterWeekdays = dfs.standaloneShorterWeekdays;
        this.standaloneNarrowWeekdays = dfs.standaloneNarrowWeekdays;
        this.ampms = dfs.ampms;
        this.ampmsNarrow = dfs.ampmsNarrow;
        this.timeSeparator = dfs.timeSeparator;
        this.shortQuarters = dfs.shortQuarters;
        this.quarters = dfs.quarters;
        this.standaloneShortQuarters = dfs.standaloneShortQuarters;
        this.standaloneQuarters = dfs.standaloneQuarters;
        this.leapMonthPatterns = dfs.leapMonthPatterns;
        this.shortYearNames = dfs.shortYearNames;
        this.shortZodiacNames = dfs.shortZodiacNames;
        this.zoneStrings = dfs.zoneStrings;
        this.localPatternChars = dfs.localPatternChars;
        this.capitalization = dfs.capitalization;
        this.actualLocale = dfs.actualLocale;
        this.validLocale = dfs.validLocale;
        this.requestedLocale = dfs.requestedLocale;
    }

    @Deprecated
    protected void initializeData(ULocale desiredLocale, CalendarData calData) {
        Object nWeekdays;
        ICUResourceBundle monthPatternsBundle;
        ICUResourceBundle cyclicNameSetsBundle;
        CapitalizationContextUsage usage;
        UResourceBundle contextTransformsBundle;
        this.eras = calData.getEras("abbreviated");
        this.eraNames = calData.getEras("wide");
        this.narrowEras = calData.getEras("narrow");
        this.months = calData.getStringArray("monthNames", "wide");
        this.shortMonths = calData.getStringArray("monthNames", "abbreviated");
        this.narrowMonths = calData.getStringArray("monthNames", "narrow");
        this.standaloneMonths = calData.getStringArray("monthNames", "stand-alone", "wide");
        this.standaloneShortMonths = calData.getStringArray("monthNames", "stand-alone", "abbreviated");
        this.standaloneNarrowMonths = calData.getStringArray("monthNames", "stand-alone", "narrow");
        String[] lWeekdays = calData.getStringArray("dayNames", "wide");
        this.weekdays = new String[8];
        this.weekdays[0] = "";
        System.arraycopy(lWeekdays, 0, this.weekdays, 1, lWeekdays.length);
        String[] aWeekdays = calData.getStringArray("dayNames", "abbreviated");
        this.shortWeekdays = new String[8];
        this.shortWeekdays[0] = "";
        System.arraycopy(aWeekdays, 0, this.shortWeekdays, 1, aWeekdays.length);
        Object sWeekdays = calData.getStringArray("dayNames", "short");
        this.shorterWeekdays = new String[8];
        this.shorterWeekdays[0] = "";
        System.arraycopy(sWeekdays, 0, this.shorterWeekdays, 1, sWeekdays.length);
        try {
            nWeekdays = calData.getStringArray("dayNames", "narrow");
        } catch (MissingResourceException e) {
            try {
                nWeekdays = calData.getStringArray("dayNames", "stand-alone", "narrow");
            } catch (MissingResourceException e2) {
                nWeekdays = calData.getStringArray("dayNames", "abbreviated");
            }
        }
        this.narrowWeekdays = new String[8];
        this.narrowWeekdays[0] = "";
        System.arraycopy(nWeekdays, 0, this.narrowWeekdays, 1, nWeekdays.length);
        Object swWeekdays = calData.getStringArray("dayNames", "stand-alone", "wide");
        this.standaloneWeekdays = new String[8];
        this.standaloneWeekdays[0] = "";
        System.arraycopy(swWeekdays, 0, this.standaloneWeekdays, 1, swWeekdays.length);
        Object saWeekdays = calData.getStringArray("dayNames", "stand-alone", "abbreviated");
        this.standaloneShortWeekdays = new String[8];
        this.standaloneShortWeekdays[0] = "";
        System.arraycopy(saWeekdays, 0, this.standaloneShortWeekdays, 1, saWeekdays.length);
        Object ssWeekdays = calData.getStringArray("dayNames", "stand-alone", "short");
        this.standaloneShorterWeekdays = new String[8];
        this.standaloneShorterWeekdays[0] = "";
        System.arraycopy(ssWeekdays, 0, this.standaloneShorterWeekdays, 1, ssWeekdays.length);
        Object snWeekdays = calData.getStringArray("dayNames", "stand-alone", "narrow");
        this.standaloneNarrowWeekdays = new String[8];
        this.standaloneNarrowWeekdays[0] = "";
        System.arraycopy(snWeekdays, 0, this.standaloneNarrowWeekdays, 1, snWeekdays.length);
        this.ampms = calData.getStringArray("AmPmMarkers");
        this.ampmsNarrow = calData.getStringArray("AmPmMarkersNarrow");
        this.quarters = calData.getStringArray("quarters", "wide");
        this.shortQuarters = calData.getStringArray("quarters", "abbreviated");
        this.standaloneQuarters = calData.getStringArray("quarters", "stand-alone", "wide");
        this.standaloneShortQuarters = calData.getStringArray("quarters", "stand-alone", "abbreviated");
        try {
            monthPatternsBundle = calData.get("monthPatterns");
        } catch (MissingResourceException e3) {
            monthPatternsBundle = null;
        }
        if (monthPatternsBundle != null) {
            this.leapMonthPatterns = new String[7];
            this.leapMonthPatterns[0] = calData.get("monthPatterns", "wide").get("leap").getString();
            this.leapMonthPatterns[1] = calData.get("monthPatterns", "abbreviated").get("leap").getString();
            this.leapMonthPatterns[2] = calData.get("monthPatterns", "narrow").get("leap").getString();
            this.leapMonthPatterns[3] = calData.get("monthPatterns", "stand-alone", "wide").get("leap").getString();
            this.leapMonthPatterns[4] = calData.get("monthPatterns", "stand-alone", "abbreviated").get("leap").getString();
            this.leapMonthPatterns[5] = calData.get("monthPatterns", "stand-alone", "narrow").get("leap").getString();
            this.leapMonthPatterns[6] = calData.get("monthPatterns", "numeric", "all").get("leap").getString();
        }
        try {
            cyclicNameSetsBundle = calData.get("cyclicNameSets");
        } catch (MissingResourceException e4) {
            cyclicNameSetsBundle = null;
        }
        if (cyclicNameSetsBundle != null) {
            this.shortYearNames = calData.get("cyclicNameSets", "years", "format", "abbreviated").getStringArray();
            this.shortZodiacNames = calData.get("cyclicNameSets", "zodiacs", "format", "abbreviated").getStringArray();
        }
        this.requestedLocale = desiredLocale;
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", desiredLocale);
        this.localPatternChars = patternChars;
        ULocale uloc = rb.getULocale();
        setLocale(uloc, uloc);
        this.capitalization = new HashMap();
        Object noTransforms = new boolean[2];
        noTransforms[0] = false;
        noTransforms[1] = false;
        for (CapitalizationContextUsage usage2 : CapitalizationContextUsage.values()) {
            this.capitalization.put(usage2, noTransforms);
        }
        try {
            contextTransformsBundle = rb.getWithFallback("contextTransforms");
        } catch (MissingResourceException e5) {
            contextTransformsBundle = null;
        }
        if (contextTransformsBundle != null) {
            UResourceBundleIterator ctIterator = contextTransformsBundle.getIterator();
            while (ctIterator.hasNext()) {
                UResourceBundle contextTransformUsage = ctIterator.next();
                int[] intVector = contextTransformUsage.getIntVector();
                if (intVector.length >= 2) {
                    usage2 = (CapitalizationContextUsage) contextUsageTypeMap.get(contextTransformUsage.getKey());
                    if (usage2 != null) {
                        Object transforms = new boolean[2];
                        transforms[0] = intVector[0] != 0;
                        transforms[1] = intVector[1] != 0;
                        this.capitalization.put(usage2, transforms);
                    }
                }
            }
        }
        NumberingSystem ns = NumberingSystem.getInstance(desiredLocale);
        try {
            setTimeSeparatorString(rb.getStringWithFallback("NumberElements/" + (ns == null ? "latn" : ns.getName()) + "/symbols/timeSeparator"));
        } catch (MissingResourceException e6) {
            setTimeSeparatorString(DEFAULT_TIME_SEPARATOR);
        }
    }

    /* JADX WARNING: Missing block: B:6:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static final boolean arrayOfArrayEquals(Object[][] aa1, Object[][] aa2) {
        if (aa1 == aa2) {
            return true;
        }
        if (aa1 == null || aa2 == null || aa1.length != aa2.length) {
            return false;
        }
        boolean equal = true;
        for (int i = 0; i < aa1.length; i++) {
            equal = Utility.arrayEquals(aa1[i], aa2[i]);
            if (!equal) {
                break;
            }
        }
        return equal;
    }

    private final String[] duplicate(String[] srcArray) {
        return (String[]) srcArray.clone();
    }

    private final String[][] duplicate(String[][] srcArray) {
        String[][] aCopy = new String[srcArray.length][];
        for (int i = 0; i < srcArray.length; i++) {
            aCopy[i] = duplicate(srcArray[i]);
        }
        return aCopy;
    }

    public DateFormatSymbols(Calendar cal, Locale locale) {
        this.eras = null;
        this.eraNames = null;
        this.narrowEras = null;
        this.months = null;
        this.shortMonths = null;
        this.narrowMonths = null;
        this.standaloneMonths = null;
        this.standaloneShortMonths = null;
        this.standaloneNarrowMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.shorterWeekdays = null;
        this.narrowWeekdays = null;
        this.standaloneWeekdays = null;
        this.standaloneShortWeekdays = null;
        this.standaloneShorterWeekdays = null;
        this.standaloneNarrowWeekdays = null;
        this.ampms = null;
        this.ampmsNarrow = null;
        this.timeSeparator = null;
        this.shortQuarters = null;
        this.quarters = null;
        this.standaloneShortQuarters = null;
        this.standaloneQuarters = null;
        this.leapMonthPatterns = null;
        this.shortYearNames = null;
        this.shortZodiacNames = null;
        this.zoneStrings = null;
        this.localPatternChars = null;
        this.capitalization = null;
        initializeData(ULocale.forLocale(locale), cal.getType());
    }

    public DateFormatSymbols(Calendar cal, ULocale locale) {
        this.eras = null;
        this.eraNames = null;
        this.narrowEras = null;
        this.months = null;
        this.shortMonths = null;
        this.narrowMonths = null;
        this.standaloneMonths = null;
        this.standaloneShortMonths = null;
        this.standaloneNarrowMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.shorterWeekdays = null;
        this.narrowWeekdays = null;
        this.standaloneWeekdays = null;
        this.standaloneShortWeekdays = null;
        this.standaloneShorterWeekdays = null;
        this.standaloneNarrowWeekdays = null;
        this.ampms = null;
        this.ampmsNarrow = null;
        this.timeSeparator = null;
        this.shortQuarters = null;
        this.quarters = null;
        this.standaloneShortQuarters = null;
        this.standaloneQuarters = null;
        this.leapMonthPatterns = null;
        this.shortYearNames = null;
        this.shortZodiacNames = null;
        this.zoneStrings = null;
        this.localPatternChars = null;
        this.capitalization = null;
        initializeData(locale, cal.getType());
    }

    public DateFormatSymbols(Class<? extends Calendar> calendarClass, Locale locale) {
        this((Class) calendarClass, ULocale.forLocale(locale));
    }

    public DateFormatSymbols(Class<? extends Calendar> calendarClass, ULocale locale) {
        this.eras = null;
        this.eraNames = null;
        this.narrowEras = null;
        this.months = null;
        this.shortMonths = null;
        this.narrowMonths = null;
        this.standaloneMonths = null;
        this.standaloneShortMonths = null;
        this.standaloneNarrowMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.shorterWeekdays = null;
        this.narrowWeekdays = null;
        this.standaloneWeekdays = null;
        this.standaloneShortWeekdays = null;
        this.standaloneShorterWeekdays = null;
        this.standaloneNarrowWeekdays = null;
        this.ampms = null;
        this.ampmsNarrow = null;
        this.timeSeparator = null;
        this.shortQuarters = null;
        this.quarters = null;
        this.standaloneShortQuarters = null;
        this.standaloneQuarters = null;
        this.leapMonthPatterns = null;
        this.shortYearNames = null;
        this.shortZodiacNames = null;
        this.zoneStrings = null;
        this.localPatternChars = null;
        this.capitalization = null;
        String fullName = calendarClass.getName();
        String className = fullName.substring(fullName.lastIndexOf(46) + 1);
        String calType = null;
        for (String[] calClassInfo : CALENDAR_CLASSES) {
            if (calClassInfo[0].equals(className)) {
                calType = calClassInfo[1];
                break;
            }
        }
        if (calType == null) {
            calType = className.replaceAll("Calendar", "").toLowerCase(Locale.ENGLISH);
        }
        initializeData(locale, calType);
    }

    public DateFormatSymbols(ResourceBundle bundle, Locale locale) {
        this(bundle, ULocale.forLocale(locale));
    }

    public DateFormatSymbols(ResourceBundle bundle, ULocale locale) {
        this.eras = null;
        this.eraNames = null;
        this.narrowEras = null;
        this.months = null;
        this.shortMonths = null;
        this.narrowMonths = null;
        this.standaloneMonths = null;
        this.standaloneShortMonths = null;
        this.standaloneNarrowMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.shorterWeekdays = null;
        this.narrowWeekdays = null;
        this.standaloneWeekdays = null;
        this.standaloneShortWeekdays = null;
        this.standaloneShorterWeekdays = null;
        this.standaloneNarrowWeekdays = null;
        this.ampms = null;
        this.ampmsNarrow = null;
        this.timeSeparator = null;
        this.shortQuarters = null;
        this.quarters = null;
        this.standaloneShortQuarters = null;
        this.standaloneQuarters = null;
        this.leapMonthPatterns = null;
        this.shortYearNames = null;
        this.shortZodiacNames = null;
        this.zoneStrings = null;
        this.localPatternChars = null;
        this.capitalization = null;
        initializeData(locale, new CalendarData((ICUResourceBundle) bundle, CalendarUtil.getCalendarType(locale)));
    }

    @Deprecated
    public static ResourceBundle getDateFormatBundle(Class<? extends Calendar> cls, Locale locale) throws MissingResourceException {
        return null;
    }

    @Deprecated
    public static ResourceBundle getDateFormatBundle(Class<? extends Calendar> cls, ULocale locale) throws MissingResourceException {
        return null;
    }

    @Deprecated
    public static ResourceBundle getDateFormatBundle(Calendar cal, Locale locale) throws MissingResourceException {
        return null;
    }

    @Deprecated
    public static ResourceBundle getDateFormatBundle(Calendar cal, ULocale locale) throws MissingResourceException {
        return null;
    }

    public final ULocale getLocale(Type type) {
        return type == ULocale.ACTUAL_LOCALE ? this.actualLocale : this.validLocale;
    }

    final void setLocale(ULocale valid, ULocale actual) {
        Object obj;
        Object obj2 = 1;
        if (valid == null) {
            obj = 1;
        } else {
            obj = null;
        }
        if (actual != null) {
            obj2 = null;
        }
        if (obj != obj2) {
            throw new IllegalArgumentException();
        }
        this.validLocale = valid;
        this.actualLocale = actual;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }
}
