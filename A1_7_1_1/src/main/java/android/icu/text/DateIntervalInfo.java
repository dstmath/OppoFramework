package android.icu.text;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.Utility;
import android.icu.util.Calendar;
import android.icu.util.Freezable;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;

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
public class DateIntervalInfo implements Cloneable, Freezable<DateIntervalInfo>, Serializable {
    static final String[] CALENDAR_FIELD_TO_PATTERN_LETTER = null;
    private static final String DEBUG_SKELETON = null;
    private static final ICUCache<String, DateIntervalInfo> DIICACHE = null;
    private static String EARLIEST_FIRST_PREFIX = null;
    private static String FALLBACK_STRING = null;
    private static String LATEST_FIRST_PREFIX = null;
    private static final int MINIMUM_SUPPORTED_CALENDAR_FIELD = 13;
    static final int currentSerialVersion = 1;
    private static final long serialVersionUID = 1;
    private String fFallbackIntervalPattern;
    private boolean fFirstDateInPtnIsLaterDate;
    private Map<String, Map<String, PatternInfo>> fIntervalPatterns;
    private transient boolean fIntervalPatternsReadOnly;
    private volatile transient boolean frozen;

    public static final class PatternInfo implements Cloneable, Serializable {
        static final int currentSerialVersion = 1;
        private static final long serialVersionUID = 1;
        private final boolean fFirstDateInPtnIsLaterDate;
        private final String fIntervalPatternFirstPart;
        private final String fIntervalPatternSecondPart;

        public PatternInfo(String firstPart, String secondPart, boolean firstDateInPtnIsLaterDate) {
            this.fIntervalPatternFirstPart = firstPart;
            this.fIntervalPatternSecondPart = secondPart;
            this.fFirstDateInPtnIsLaterDate = firstDateInPtnIsLaterDate;
        }

        public String getFirstPart() {
            return this.fIntervalPatternFirstPart;
        }

        public String getSecondPart() {
            return this.fIntervalPatternSecondPart;
        }

        public boolean firstDateInPtnIsLaterDate() {
            return this.fFirstDateInPtnIsLaterDate;
        }

        public boolean equals(Object a) {
            boolean z = false;
            if (!(a instanceof PatternInfo)) {
                return false;
            }
            PatternInfo patternInfo = (PatternInfo) a;
            if (Utility.objectEquals(this.fIntervalPatternFirstPart, patternInfo.fIntervalPatternFirstPart) && Utility.objectEquals(this.fIntervalPatternSecondPart, this.fIntervalPatternSecondPart) && this.fFirstDateInPtnIsLaterDate == patternInfo.fFirstDateInPtnIsLaterDate) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            int hash = this.fIntervalPatternFirstPart != null ? this.fIntervalPatternFirstPart.hashCode() : 0;
            if (this.fIntervalPatternSecondPart != null) {
                hash ^= this.fIntervalPatternSecondPart.hashCode();
            }
            if (this.fFirstDateInPtnIsLaterDate) {
                return hash ^ -1;
            }
            return hash;
        }

        @Deprecated
        public String toString() {
            return "{first=«" + this.fIntervalPatternFirstPart + "», second=«" + this.fIntervalPatternSecondPart + "», reversed:" + this.fFirstDateInPtnIsLaterDate + "}";
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.DateIntervalInfo.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.DateIntervalInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateIntervalInfo.<clinit>():void");
    }

    @Deprecated
    public DateIntervalInfo() {
        this.fFirstDateInPtnIsLaterDate = false;
        this.fIntervalPatterns = null;
        this.frozen = false;
        this.fIntervalPatternsReadOnly = false;
        this.fIntervalPatterns = new HashMap();
        this.fFallbackIntervalPattern = "{0} – {1}";
    }

    public DateIntervalInfo(ULocale locale) {
        this.fFirstDateInPtnIsLaterDate = false;
        this.fIntervalPatterns = null;
        this.frozen = false;
        this.fIntervalPatternsReadOnly = false;
        initializeData(locale);
    }

    public DateIntervalInfo(Locale locale) {
        this(ULocale.forLocale(locale));
    }

    private void initializeData(ULocale locale) {
        String key = locale.toString();
        DateIntervalInfo dii = (DateIntervalInfo) DIICACHE.get(key);
        if (dii == null) {
            setup(locale);
            this.fIntervalPatternsReadOnly = true;
            DIICACHE.put(key, ((DateIntervalInfo) clone()).freeze());
            return;
        }
        initializeFromReadOnlyPatterns(dii);
    }

    private void initializeFromReadOnlyPatterns(DateIntervalInfo dii) {
        this.fFallbackIntervalPattern = dii.fFallbackIntervalPattern;
        this.fFirstDateInPtnIsLaterDate = dii.fFirstDateInPtnIsLaterDate;
        this.fIntervalPatterns = dii.fIntervalPatterns;
        this.fIntervalPatternsReadOnly = true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0051 A:{Catch:{ MissingResourceException -> 0x01a7 }} */
    /* JADX WARNING: Can't wrap try/catch for R(6:12|(4:15|(1:71)(3:18|(4:21|(1:73)(4:24|(1:26)(2:38|(1:40)(2:41|(1:43)(2:44|(1:46)(2:47|(1:49)(2:50|(1:52)(2:53|(1:55)(2:56|(1:58))))))))|27|(1:74)(2:29|(2:59|77)(2:33|(2:35|76)(1:75))))|23|19)|72)|17|13)|60|61|(3:63|64|(1:81))|80) */
    /* JADX WARNING: Missing block: B:69:?, code:
            r6 = r7.getFallback();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setup(ULocale locale) {
        this.fIntervalPatterns = new HashMap(19);
        this.fFallbackIntervalPattern = "{0} – {1}";
        HashSet<String> skeletonKeyPairs = new HashSet();
        ULocale currentLocale = locale;
        try {
            ULocale currentLocale2;
            String calendarTypeToUse = locale.getKeywordValue("calendar");
            if (calendarTypeToUse == null) {
                calendarTypeToUse = Calendar.getKeywordValuesForLocale("calendar", locale, true)[0];
            }
            if (calendarTypeToUse == null) {
                calendarTypeToUse = "gregorian";
                currentLocale2 = currentLocale;
                if (currentLocale2.getName().length() != 0) {
                    currentLocale = currentLocale2;
                } else {
                    ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", currentLocale2);
                    ICUResourceBundle itvDtPtnResource = rb.getWithFallback("calendar/" + calendarTypeToUse + "/intervalFormats");
                    setFallbackIntervalPattern(itvDtPtnResource.getStringWithFallback(FALLBACK_STRING));
                    int size = itvDtPtnResource.getSize();
                    for (int index = 0; index < size; index++) {
                        String skeleton = itvDtPtnResource.get(index).getKey();
                        if (skeleton.compareTo(FALLBACK_STRING) != 0) {
                            ICUResourceBundle intervalPatterns = (ICUResourceBundle) itvDtPtnResource.get(skeleton);
                            int ptnNum = intervalPatterns.getSize();
                            for (int ptnIndex = 0; ptnIndex < ptnNum; ptnIndex++) {
                                String key = intervalPatterns.get(ptnIndex).getKey();
                                String skeletonKeyPair = skeleton + "\u0001" + key;
                                if (!skeletonKeyPairs.contains(skeletonKeyPair)) {
                                    skeletonKeyPairs.add(skeletonKeyPair);
                                    String pattern = intervalPatterns.get(ptnIndex).getString();
                                    int calendarField = -1;
                                    if (key.equals(CALENDAR_FIELD_TO_PATTERN_LETTER[1])) {
                                        calendarField = 1;
                                    } else if (key.equals(CALENDAR_FIELD_TO_PATTERN_LETTER[2])) {
                                        calendarField = 2;
                                    } else if (key.equals(CALENDAR_FIELD_TO_PATTERN_LETTER[5])) {
                                        calendarField = 5;
                                    } else if (key.equals(CALENDAR_FIELD_TO_PATTERN_LETTER[9])) {
                                        calendarField = 9;
                                    } else if (key.equals(CALENDAR_FIELD_TO_PATTERN_LETTER[10])) {
                                        calendarField = 10;
                                        key = CALENDAR_FIELD_TO_PATTERN_LETTER[10];
                                    } else if (key.equals(CALENDAR_FIELD_TO_PATTERN_LETTER[11])) {
                                        calendarField = 10;
                                        key = CALENDAR_FIELD_TO_PATTERN_LETTER[10];
                                    } else if (key.equals(CALENDAR_FIELD_TO_PATTERN_LETTER[12])) {
                                        calendarField = 12;
                                    } else if (key.equals(CALENDAR_FIELD_TO_PATTERN_LETTER[13])) {
                                        calendarField = 13;
                                    }
                                    if (calendarField != -1) {
                                        if (DEBUG_SKELETON == null || !DEBUG_SKELETON.equals(skeleton)) {
                                            setIntervalPatternInternally(skeleton, key, pattern);
                                        } else {
                                            Map<String, PatternInfo> oldValue = (Map) this.fIntervalPatterns.get(skeleton);
                                            setIntervalPatternInternally(skeleton, key, pattern);
                                            Map<String, PatternInfo> newValue = (Map) this.fIntervalPatterns.get(skeleton);
                                            if (!Utility.objectEquals(oldValue, newValue)) {
                                                System.out.println("\n" + currentLocale2 + ", skeleton: " + skeleton + ", oldValue: " + oldValue + ", newValue: " + newValue);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    currentLocale = new ULocale(rb.get("%%Parent").getString());
                    if (currentLocale != null) {
                        if (currentLocale.getBaseName().equals("root")) {
                            return;
                        }
                    }
                    return;
                }
                currentLocale = currentLocale2;
            }
            currentLocale2 = currentLocale;
            try {
                if (currentLocale2.getName().length() != 0) {
                }
                currentLocale = currentLocale2;
            } catch (MissingResourceException e) {
                currentLocale = currentLocale2;
            }
        } catch (MissingResourceException e2) {
        }
    }

    private static int splitPatternInto2Part(String intervalPattern) {
        boolean inQuote = false;
        int prevCh = 0;
        int count = 0;
        int[] patternRepeated = new int[58];
        boolean foundRepetition = false;
        int i = 0;
        while (i < intervalPattern.length()) {
            char ch = intervalPattern.charAt(i);
            if (ch != prevCh && count > 0) {
                if (patternRepeated[prevCh - 65] != 0) {
                    foundRepetition = true;
                    break;
                }
                patternRepeated[prevCh - 65] = 1;
                count = 0;
            }
            if (ch == PatternTokenizer.SINGLE_QUOTE) {
                if (i + 1 >= intervalPattern.length() || intervalPattern.charAt(i + 1) != PatternTokenizer.SINGLE_QUOTE) {
                    inQuote = !inQuote;
                } else {
                    i++;
                }
            } else if (!inQuote && ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))) {
                char prevCh2 = ch;
                count++;
            }
            i++;
        }
        if (count > 0 && !foundRepetition && patternRepeated[prevCh2 - 65] == 0) {
            count = 0;
        }
        return i - count;
    }

    public void setIntervalPattern(String skeleton, int lrgDiffCalUnit, String intervalPattern) {
        if (this.frozen) {
            throw new UnsupportedOperationException("no modification is allowed after DII is frozen");
        } else if (lrgDiffCalUnit > 13) {
            throw new IllegalArgumentException("calendar field is larger than MINIMUM_SUPPORTED_CALENDAR_FIELD");
        } else {
            if (this.fIntervalPatternsReadOnly) {
                this.fIntervalPatterns = cloneIntervalPatterns(this.fIntervalPatterns);
                this.fIntervalPatternsReadOnly = false;
            }
            PatternInfo ptnInfo = setIntervalPatternInternally(skeleton, CALENDAR_FIELD_TO_PATTERN_LETTER[lrgDiffCalUnit], intervalPattern);
            if (lrgDiffCalUnit == 11) {
                setIntervalPattern(skeleton, CALENDAR_FIELD_TO_PATTERN_LETTER[9], ptnInfo);
                setIntervalPattern(skeleton, CALENDAR_FIELD_TO_PATTERN_LETTER[10], ptnInfo);
            } else if (lrgDiffCalUnit == 5 || lrgDiffCalUnit == 7) {
                setIntervalPattern(skeleton, CALENDAR_FIELD_TO_PATTERN_LETTER[5], ptnInfo);
            }
        }
    }

    private PatternInfo setIntervalPatternInternally(String skeleton, String lrgDiffCalUnit, String intervalPattern) {
        Map<String, PatternInfo> patternsOfOneSkeleton = (Map) this.fIntervalPatterns.get(skeleton);
        boolean emptyHash = false;
        if (patternsOfOneSkeleton == null) {
            patternsOfOneSkeleton = new HashMap();
            emptyHash = true;
        }
        boolean order = this.fFirstDateInPtnIsLaterDate;
        if (intervalPattern.startsWith(LATEST_FIRST_PREFIX)) {
            order = true;
            intervalPattern = intervalPattern.substring(LATEST_FIRST_PREFIX.length(), intervalPattern.length());
        } else if (intervalPattern.startsWith(EARLIEST_FIRST_PREFIX)) {
            order = false;
            intervalPattern = intervalPattern.substring(EARLIEST_FIRST_PREFIX.length(), intervalPattern.length());
        }
        PatternInfo itvPtnInfo = genPatternInfo(intervalPattern, order);
        patternsOfOneSkeleton.put(lrgDiffCalUnit, itvPtnInfo);
        if (emptyHash) {
            this.fIntervalPatterns.put(skeleton, patternsOfOneSkeleton);
        }
        return itvPtnInfo;
    }

    private void setIntervalPattern(String skeleton, String lrgDiffCalUnit, PatternInfo ptnInfo) {
        ((Map) this.fIntervalPatterns.get(skeleton)).put(lrgDiffCalUnit, ptnInfo);
    }

    @Deprecated
    public static PatternInfo genPatternInfo(String intervalPattern, boolean laterDateFirst) {
        int splitPoint = splitPatternInto2Part(intervalPattern);
        String firstPart = intervalPattern.substring(0, splitPoint);
        String secondPart = null;
        if (splitPoint < intervalPattern.length()) {
            secondPart = intervalPattern.substring(splitPoint, intervalPattern.length());
        }
        return new PatternInfo(firstPart, secondPart, laterDateFirst);
    }

    public PatternInfo getIntervalPattern(String skeleton, int field) {
        if (field > 13) {
            throw new IllegalArgumentException("no support for field less than SECOND");
        }
        Map<String, PatternInfo> patternsOfOneSkeleton = (Map) this.fIntervalPatterns.get(skeleton);
        if (patternsOfOneSkeleton != null) {
            PatternInfo intervalPattern = (PatternInfo) patternsOfOneSkeleton.get(CALENDAR_FIELD_TO_PATTERN_LETTER[field]);
            if (intervalPattern != null) {
                return intervalPattern;
            }
        }
        return null;
    }

    public String getFallbackIntervalPattern() {
        return this.fFallbackIntervalPattern;
    }

    public void setFallbackIntervalPattern(String fallbackPattern) {
        if (this.frozen) {
            throw new UnsupportedOperationException("no modification is allowed after DII is frozen");
        }
        int firstPatternIndex = fallbackPattern.indexOf("{0}");
        int secondPatternIndex = fallbackPattern.indexOf("{1}");
        if (firstPatternIndex == -1 || secondPatternIndex == -1) {
            throw new IllegalArgumentException("no pattern {0} or pattern {1} in fallbackPattern");
        }
        if (firstPatternIndex > secondPatternIndex) {
            this.fFirstDateInPtnIsLaterDate = true;
        }
        this.fFallbackIntervalPattern = fallbackPattern;
    }

    public boolean getDefaultOrder() {
        return this.fFirstDateInPtnIsLaterDate;
    }

    public Object clone() {
        if (this.frozen) {
            return this;
        }
        return cloneUnfrozenDII();
    }

    private Object cloneUnfrozenDII() {
        try {
            DateIntervalInfo other = (DateIntervalInfo) super.clone();
            other.fFallbackIntervalPattern = this.fFallbackIntervalPattern;
            other.fFirstDateInPtnIsLaterDate = this.fFirstDateInPtnIsLaterDate;
            if (this.fIntervalPatternsReadOnly) {
                other.fIntervalPatterns = this.fIntervalPatterns;
                other.fIntervalPatternsReadOnly = true;
            } else {
                other.fIntervalPatterns = cloneIntervalPatterns(this.fIntervalPatterns);
                other.fIntervalPatternsReadOnly = false;
            }
            other.frozen = false;
            return other;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException("clone is not supported", e);
        }
    }

    private static Map<String, Map<String, PatternInfo>> cloneIntervalPatterns(Map<String, Map<String, PatternInfo>> patterns) {
        Map<String, Map<String, PatternInfo>> result = new HashMap();
        for (Entry<String, Map<String, PatternInfo>> skeletonEntry : patterns.entrySet()) {
            String skeleton = (String) skeletonEntry.getKey();
            Map<String, PatternInfo> patternsOfOneSkeleton = (Map) skeletonEntry.getValue();
            Map<String, PatternInfo> oneSetPtn = new HashMap();
            for (Entry<String, PatternInfo> calEntry : patternsOfOneSkeleton.entrySet()) {
                oneSetPtn.put((String) calEntry.getKey(), (PatternInfo) calEntry.getValue());
            }
            result.put(skeleton, oneSetPtn);
        }
        return result;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public DateIntervalInfo freeze() {
        this.fIntervalPatternsReadOnly = true;
        this.frozen = true;
        return this;
    }

    public DateIntervalInfo cloneAsThawed() {
        return (DateIntervalInfo) cloneUnfrozenDII();
    }

    static void parseSkeleton(String skeleton, int[] skeletonFieldWidth) {
        for (int i = 0; i < skeleton.length(); i++) {
            int charAt = skeleton.charAt(i) - 65;
            skeletonFieldWidth[charAt] = skeletonFieldWidth[charAt] + 1;
        }
    }

    private static boolean stringNumeric(int fieldWidth, int anotherFieldWidth, char patternLetter) {
        if (patternLetter != 'M' || ((fieldWidth > 2 || anotherFieldWidth <= 2) && (fieldWidth <= 2 || anotherFieldWidth > 2))) {
            return false;
        }
        return true;
    }

    BestMatchInfo getBestSkeleton(String inputSkeleton) {
        String bestSkeleton = inputSkeleton;
        int[] inputSkeletonFieldWidth = new int[58];
        int[] skeletonFieldWidth = new int[58];
        boolean replaceZWithV = false;
        if (inputSkeleton.indexOf(122) != -1) {
            inputSkeleton = inputSkeleton.replace('z', 'v');
            replaceZWithV = true;
        }
        parseSkeleton(inputSkeleton, inputSkeletonFieldWidth);
        int bestDistance = Integer.MAX_VALUE;
        int bestFieldDifference = 0;
        for (String skeleton : this.fIntervalPatterns.keySet()) {
            int i;
            for (i = 0; i < skeletonFieldWidth.length; i++) {
                skeletonFieldWidth[i] = 0;
            }
            parseSkeleton(skeleton, skeletonFieldWidth);
            int distance = 0;
            int fieldDifference = 1;
            for (i = 0; i < inputSkeletonFieldWidth.length; i++) {
                int inputFieldWidth = inputSkeletonFieldWidth[i];
                int fieldWidth = skeletonFieldWidth[i];
                if (inputFieldWidth != fieldWidth) {
                    if (inputFieldWidth == 0) {
                        fieldDifference = -1;
                        distance += 4096;
                    } else if (fieldWidth == 0) {
                        fieldDifference = -1;
                        distance += 4096;
                    } else if (stringNumeric(inputFieldWidth, fieldWidth, (char) (i + 65))) {
                        distance += 256;
                    } else {
                        distance += Math.abs(inputFieldWidth - fieldWidth);
                    }
                }
            }
            if (distance < bestDistance) {
                bestSkeleton = skeleton;
                bestDistance = distance;
                bestFieldDifference = fieldDifference;
                continue;
            }
            if (distance == 0) {
                bestFieldDifference = 0;
                break;
            }
        }
        if (replaceZWithV && bestFieldDifference != -1) {
            bestFieldDifference = 2;
        }
        return new BestMatchInfo(bestSkeleton, bestFieldDifference);
    }

    public boolean equals(Object a) {
        if (!(a instanceof DateIntervalInfo)) {
            return false;
        }
        return this.fIntervalPatterns.equals(((DateIntervalInfo) a).fIntervalPatterns);
    }

    public int hashCode() {
        return this.fIntervalPatterns.hashCode();
    }

    @Deprecated
    public Map<String, Set<String>> getPatterns() {
        LinkedHashMap<String, Set<String>> result = new LinkedHashMap();
        for (Entry<String, Map<String, PatternInfo>> entry : this.fIntervalPatterns.entrySet()) {
            result.put((String) entry.getKey(), new LinkedHashSet(((Map) entry.getValue()).keySet()));
        }
        return result;
    }

    @Deprecated
    public Map<String, Map<String, PatternInfo>> getRawPatterns() {
        LinkedHashMap<String, Map<String, PatternInfo>> result = new LinkedHashMap();
        for (Entry<String, Map<String, PatternInfo>> entry : this.fIntervalPatterns.entrySet()) {
            result.put((String) entry.getKey(), new LinkedHashMap((Map) entry.getValue()));
        }
        return result;
    }
}
