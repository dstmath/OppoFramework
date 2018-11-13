package libcore.icu;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Locale;
import libcore.util.Objects;

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
public final class LocaleData {
    private static final HashMap<String, LocaleData> localeDataCache = null;
    public String NaN;
    public String[] amPm;
    public String currencyPattern;
    public String currencySymbol;
    public char decimalSeparator;
    public String[] eras;
    public String exponentSeparator;
    public Integer firstDayOfWeek;
    public String fullDateFormat;
    public String fullTimeFormat;
    public char groupingSeparator;
    public String infinity;
    public String integerPattern;
    public String internationalCurrencySymbol;
    public String longDateFormat;
    public String[] longMonthNames;
    public String[] longStandAloneMonthNames;
    public String[] longStandAloneWeekdayNames;
    public String longTimeFormat;
    public String[] longWeekdayNames;
    public String mediumDateFormat;
    public String mediumTimeFormat;
    public Integer minimalDaysInFirstWeek;
    public String minusSign;
    public char monetarySeparator;
    public String narrowAm;
    public String narrowPm;
    public String numberPattern;
    public char patternSeparator;
    public char perMill;
    public String percent;
    public String percentPattern;
    public String shortDateFormat;
    public String shortDateFormat4;
    public String[] shortMonthNames;
    public String[] shortStandAloneMonthNames;
    public String[] shortStandAloneWeekdayNames;
    public String shortTimeFormat;
    public String[] shortWeekdayNames;
    public String timeFormat_Hm;
    public String timeFormat_Hms;
    public String timeFormat_hm;
    public String timeFormat_hms;
    public String[] tinyMonthNames;
    public String[] tinyStandAloneMonthNames;
    public String[] tinyStandAloneWeekdayNames;
    public String[] tinyWeekdayNames;
    public String today;
    public String tomorrow;
    public String yesterday;
    public char zeroDigit;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: libcore.icu.LocaleData.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: libcore.icu.LocaleData.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.icu.LocaleData.<clinit>():void");
    }

    private LocaleData() {
    }

    public static Locale mapInvalidAndNullLocales(Locale locale) {
        if (locale == null) {
            return Locale.getDefault();
        }
        if ("und".equals(locale.toLanguageTag())) {
            return Locale.ROOT;
        }
        return locale;
    }

    /* JADX WARNING: Missing block: B:11:0x001f, code:
            r2 = initLocaleData(r5);
            r4 = localeDataCache;
     */
    /* JADX WARNING: Missing block: B:12:0x0025, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            r1 = (libcore.icu.LocaleData) localeDataCache.get(r0);
     */
    /* JADX WARNING: Missing block: B:15:0x002e, code:
            if (r1 == null) goto L_0x0035;
     */
    /* JADX WARNING: Missing block: B:16:0x0030, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:17:0x0031, code:
            return r1;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            localeDataCache.put(r0, r2);
     */
    /* JADX WARNING: Missing block: B:23:0x003a, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:24:0x003b, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static LocaleData get(Locale locale) {
        if (locale == null) {
            throw new NullPointerException("locale == null");
        }
        String languageTag = locale.toLanguageTag();
        synchronized (localeDataCache) {
            LocaleData localeData = (LocaleData) localeDataCache.get(languageTag);
            if (localeData != null) {
                return localeData;
            }
        }
    }

    public String toString() {
        return Objects.toString(this);
    }

    public String getDateFormat(int style) {
        switch (style) {
            case 0:
                return this.fullDateFormat;
            case 1:
                return this.longDateFormat;
            case 2:
                return this.mediumDateFormat;
            case 3:
                return this.shortDateFormat;
            default:
                throw new AssertionError();
        }
    }

    public String getTimeFormat(int style) {
        switch (style) {
            case 0:
                return this.fullTimeFormat;
            case 1:
                return this.longTimeFormat;
            case 2:
                if (DateFormat.is24Hour == null) {
                    return this.mediumTimeFormat;
                }
                return DateFormat.is24Hour.booleanValue() ? this.timeFormat_Hms : this.timeFormat_hms;
            case 3:
                if (DateFormat.is24Hour == null) {
                    return this.shortTimeFormat;
                }
                return DateFormat.is24Hour.booleanValue() ? this.timeFormat_Hm : this.timeFormat_hm;
            default:
                throw new AssertionError();
        }
    }

    private static LocaleData initLocaleData(Locale locale) {
        LocaleData localeData = new LocaleData();
        if (ICU.initLocaleDataNative(locale.toLanguageTag(), localeData)) {
            localeData.timeFormat_hm = ICU.getBestDateTimePattern("hm", locale);
            localeData.timeFormat_Hm = ICU.getBestDateTimePattern(android.icu.text.DateFormat.HOUR24_MINUTE, locale);
            localeData.timeFormat_hms = ICU.getBestDateTimePattern("hms", locale);
            localeData.timeFormat_Hms = ICU.getBestDateTimePattern(android.icu.text.DateFormat.HOUR24_MINUTE_SECOND, locale);
            if (localeData.fullTimeFormat != null) {
                localeData.fullTimeFormat = localeData.fullTimeFormat.replace('v', 'z');
            }
            if (localeData.numberPattern != null) {
                localeData.integerPattern = localeData.numberPattern.replaceAll("\\.[#,]*", "");
            }
            localeData.shortDateFormat4 = localeData.shortDateFormat.replaceAll("\\byy\\b", android.icu.text.DateFormat.YEAR);
            return localeData;
        }
        throw new AssertionError("couldn't initialize LocaleData for locale " + locale);
    }
}
