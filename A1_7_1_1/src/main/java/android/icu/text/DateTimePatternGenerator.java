package android.icu.text;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.Utility;
import android.icu.util.Calendar;
import android.icu.util.Freezable;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
public class DateTimePatternGenerator implements Freezable<DateTimePatternGenerator>, Cloneable {
    private static final String[] CANONICAL_ITEMS = null;
    private static final Set<String> CANONICAL_SET = null;
    private static final String[] CLDR_FIELD_APPEND = null;
    private static final String[] CLDR_FIELD_NAME = null;
    private static final int DATE_MASK = 1023;
    public static final int DAY = 7;
    public static final int DAYPERIOD = 10;
    public static final int DAY_OF_WEEK_IN_MONTH = 9;
    public static final int DAY_OF_YEAR = 8;
    private static final boolean DEBUG = false;
    private static final int DELTA = 16;
    private static ICUCache<String, DateTimePatternGenerator> DTPNG_CACHE = null;
    public static final int ERA = 0;
    private static final int EXTRA_FIELD = 65536;
    private static final String[] FIELD_NAME = null;
    private static final int FRACTIONAL_MASK = 16384;
    public static final int FRACTIONAL_SECOND = 14;
    public static final int HOUR = 11;
    private static final int LONG = -259;
    public static final int MATCH_ALL_FIELDS_LENGTH = 65535;
    public static final int MATCH_HOUR_FIELD_LENGTH = 2048;
    @Deprecated
    public static final int MATCH_MINUTE_FIELD_LENGTH = 4096;
    public static final int MATCH_NO_OPTIONS = 0;
    @Deprecated
    public static final int MATCH_SECOND_FIELD_LENGTH = 8192;
    public static final int MINUTE = 12;
    private static final int MISSING_FIELD = 4096;
    public static final int MONTH = 3;
    private static final int NARROW = -257;
    private static final int NONE = 0;
    private static final int NUMERIC = 256;
    public static final int QUARTER = 2;
    public static final int SECOND = 13;
    private static final int SECOND_AND_FRACTIONAL_MASK = 24576;
    private static final int SHORT = -258;
    private static final int TIME_MASK = 64512;
    public static final int TYPE_LIMIT = 16;
    public static final int WEEKDAY = 6;
    public static final int WEEK_OF_MONTH = 5;
    public static final int WEEK_OF_YEAR = 4;
    public static final int YEAR = 1;
    public static final int ZONE = 15;
    private static final int[][] types = null;
    private transient DistanceInfo _distanceInfo;
    private String[] appendItemFormats;
    private String[] appendItemNames;
    private TreeMap<String, PatternWithSkeletonFlag> basePattern_pattern;
    private Set<String> cldrAvailableFormatKeys;
    private transient DateTimeMatcher current;
    private String dateTimeFormat;
    private String decimal;
    private char defaultHourFormatChar;
    private transient FormatParser fp;
    private volatile boolean frozen;
    private TreeMap<DateTimeMatcher, PatternWithSkeletonFlag> skeleton2pattern;

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
    private enum DTPGflags {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.DateTimePatternGenerator.DTPGflags.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.DateTimePatternGenerator.DTPGflags.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateTimePatternGenerator.DTPGflags.<clinit>():void");
        }
    }

    private static class DateTimeMatcher implements Comparable<DateTimeMatcher> {
        private String[] baseOriginal;
        private String[] original;
        private int[] type;

        /* synthetic */ DateTimeMatcher(DateTimeMatcher dateTimeMatcher) {
            this();
        }

        private DateTimeMatcher() {
            this.type = new int[16];
            this.original = new String[16];
            this.baseOriginal = new String[16];
        }

        public String origStringForField(int field) {
            return this.original[field];
        }

        public boolean fieldIsNumeric(int field) {
            return this.type[field] > 0;
        }

        public String toString() {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                if (this.original[i].length() != 0) {
                    result.append(this.original[i]);
                }
            }
            return result.toString();
        }

        public String toCanonicalString() {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                if (this.original[i].length() != 0) {
                    int j = 0;
                    while (j < DateTimePatternGenerator.types.length) {
                        int[] row = DateTimePatternGenerator.types[j];
                        if (row[1] == i) {
                            char originalChar = this.original[i].charAt(0);
                            char repeatChar = (originalChar == 'h' || originalChar == 'K') ? 'h' : (char) row[0];
                            result.append(Utility.repeat(String.valueOf(repeatChar), this.original[i].length()));
                        } else {
                            j++;
                        }
                    }
                }
            }
            return result.toString();
        }

        String getBasePattern() {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                if (this.baseOriginal[i].length() != 0) {
                    result.append(this.baseOriginal[i]);
                }
            }
            return result.toString();
        }

        DateTimeMatcher set(String pattern, FormatParser fp, boolean allowDuplicateFields) {
            for (int i = 0; i < 16; i++) {
                this.type[i] = 0;
                this.original[i] = "";
                this.baseOriginal[i] = "";
            }
            fp.set(pattern);
            for (VariableField obj : fp.getItems()) {
                if (obj instanceof VariableField) {
                    VariableField item = obj;
                    String field = item.toString();
                    if (field.charAt(0) != 'a') {
                        int[] row = DateTimePatternGenerator.types[item.getCanonicalIndex()];
                        int typeValue = row[1];
                        if (this.original[typeValue].length() == 0) {
                            this.original[typeValue] = field;
                            char repeatChar = (char) row[0];
                            int repeatCount = row[3];
                            if ("GEzvQ".indexOf(repeatChar) >= 0) {
                                repeatCount = 1;
                            }
                            this.baseOriginal[typeValue] = Utility.repeat(String.valueOf(repeatChar), repeatCount);
                            int subTypeValue = row[2];
                            if (subTypeValue > 0) {
                                subTypeValue += field.length();
                            }
                            this.type[typeValue] = subTypeValue;
                        } else if (!(allowDuplicateFields || ((this.original[typeValue].charAt(0) == 'r' && field.charAt(0) == 'U') || (this.original[typeValue].charAt(0) == 'U' && field.charAt(0) == 'r')))) {
                            throw new IllegalArgumentException("Conflicting fields:\t" + this.original[typeValue] + ", " + field + "\t in " + pattern);
                        }
                    }
                    continue;
                }
            }
            return this;
        }

        int getFieldMask() {
            int result = 0;
            for (int i = 0; i < this.type.length; i++) {
                if (this.type[i] != 0) {
                    result |= 1 << i;
                }
            }
            return result;
        }

        void extractFrom(DateTimeMatcher source, int fieldMask) {
            for (int i = 0; i < this.type.length; i++) {
                if (((1 << i) & fieldMask) != 0) {
                    this.type[i] = source.type[i];
                    this.original[i] = source.original[i];
                } else {
                    this.type[i] = 0;
                    this.original[i] = "";
                }
            }
        }

        int getDistance(DateTimeMatcher other, int includeMask, DistanceInfo distanceInfo) {
            int result = 0;
            distanceInfo.clear();
            int i = 0;
            while (i < this.type.length) {
                int myType = ((1 << i) & includeMask) == 0 ? 0 : this.type[i];
                int otherType = other.type[i];
                if (myType != otherType) {
                    if (myType == 0) {
                        result += 65536;
                        distanceInfo.addExtra(i);
                    } else if (otherType == 0) {
                        result += 4096;
                        distanceInfo.addMissing(i);
                    } else {
                        result += Math.abs(myType - otherType);
                    }
                }
                i++;
            }
            return result;
        }

        public int compareTo(DateTimeMatcher that) {
            for (int i = 0; i < this.original.length; i++) {
                int comp = this.original[i].compareTo(that.original[i]);
                if (comp != 0) {
                    return -comp;
                }
            }
            return 0;
        }

        public boolean equals(Object other) {
            if (!(other instanceof DateTimeMatcher)) {
                return false;
            }
            DateTimeMatcher that = (DateTimeMatcher) other;
            for (int i = 0; i < this.original.length; i++) {
                if (!this.original[i].equals(that.original[i])) {
                    return false;
                }
            }
            return true;
        }

        public int hashCode() {
            int result = 0;
            for (String hashCode : this.original) {
                result ^= hashCode.hashCode();
            }
            return result;
        }
    }

    private static class DistanceInfo {
        int extraFieldMask;
        int missingFieldMask;

        /* synthetic */ DistanceInfo(DistanceInfo distanceInfo) {
            this();
        }

        private DistanceInfo() {
        }

        void clear() {
            this.extraFieldMask = 0;
            this.missingFieldMask = 0;
        }

        void setTo(DistanceInfo other) {
            this.missingFieldMask = other.missingFieldMask;
            this.extraFieldMask = other.extraFieldMask;
        }

        void addMissing(int field) {
            this.missingFieldMask |= 1 << field;
        }

        void addExtra(int field) {
            this.extraFieldMask |= 1 << field;
        }

        public String toString() {
            return "missingFieldMask: " + DateTimePatternGenerator.showMask(this.missingFieldMask) + ", extraFieldMask: " + DateTimePatternGenerator.showMask(this.extraFieldMask);
        }
    }

    @Deprecated
    public static class FormatParser {
        private static final UnicodeSet QUOTING_CHARS = null;
        private static final UnicodeSet SYNTAX_CHARS = null;
        private List<Object> items;
        private transient PatternTokenizer tokenizer;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.text.DateTimePatternGenerator.FormatParser.<clinit>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.text.DateTimePatternGenerator.FormatParser.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateTimePatternGenerator.FormatParser.<clinit>():void");
        }

        @Deprecated
        public FormatParser() {
            this.tokenizer = new PatternTokenizer().setSyntaxCharacters(SYNTAX_CHARS).setExtraQuotingCharacters(QUOTING_CHARS).setUsingQuote(true);
            this.items = new ArrayList();
        }

        @Deprecated
        public final FormatParser set(String string) {
            return set(string, false);
        }

        @Deprecated
        public FormatParser set(String string, boolean strict) {
            this.items.clear();
            if (string.length() == 0) {
                return this;
            }
            this.tokenizer.setPattern(string);
            StringBuffer buffer = new StringBuffer();
            StringBuffer variable = new StringBuffer();
            while (true) {
                buffer.setLength(0);
                int status = this.tokenizer.next(buffer);
                if (status == 0) {
                    addVariable(variable, false);
                    return this;
                } else if (status == 1) {
                    if (!(variable.length() == 0 || buffer.charAt(0) == variable.charAt(0))) {
                        addVariable(variable, false);
                    }
                    variable.append(buffer);
                } else {
                    addVariable(variable, false);
                    this.items.add(buffer.toString());
                }
            }
        }

        private void addVariable(StringBuffer variable, boolean strict) {
            if (variable.length() != 0) {
                this.items.add(new VariableField(variable.toString(), strict));
                variable.setLength(0);
            }
        }

        @Deprecated
        public List<Object> getItems() {
            return this.items;
        }

        @Deprecated
        public String toString() {
            return toString(0, this.items.size());
        }

        @Deprecated
        public String toString(int start, int limit) {
            StringBuilder result = new StringBuilder();
            for (int i = start; i < limit; i++) {
                String item = this.items.get(i);
                if (item instanceof String) {
                    result.append(this.tokenizer.quoteLiteral(item));
                } else {
                    result.append(this.items.get(i).toString());
                }
            }
            return result.toString();
        }

        @Deprecated
        public boolean hasDateAndTimeFields() {
            int foundMask = 0;
            for (Object item : this.items) {
                if (item instanceof VariableField) {
                    foundMask |= 1 << ((VariableField) item).getType();
                }
            }
            boolean isDate = (foundMask & 1023) != 0;
            boolean isTime = (DateTimePatternGenerator.TIME_MASK & foundMask) != 0;
            if (isDate) {
                return isTime;
            }
            return false;
        }

        @Deprecated
        public Object quoteLiteral(String string) {
            return this.tokenizer.quoteLiteral(string);
        }
    }

    public static final class PatternInfo {
        public static final int BASE_CONFLICT = 1;
        public static final int CONFLICT = 2;
        public static final int OK = 0;
        public String conflictingPattern;
        public int status;
    }

    private static class PatternWithMatcher {
        public DateTimeMatcher matcherWithSkeleton;
        public String pattern;

        public PatternWithMatcher(String pat, DateTimeMatcher matcher) {
            this.pattern = pat;
            this.matcherWithSkeleton = matcher;
        }
    }

    private static class PatternWithSkeletonFlag {
        public String pattern;
        public boolean skeletonWasSpecified;

        public PatternWithSkeletonFlag(String pat, boolean skelSpecified) {
            this.pattern = pat;
            this.skeletonWasSpecified = skelSpecified;
        }

        public String toString() {
            return this.pattern + "," + this.skeletonWasSpecified;
        }
    }

    @Deprecated
    public static class VariableField {
        private final int canonicalIndex;
        private final String string;

        @Deprecated
        public VariableField(String string) {
            this(string, false);
        }

        @Deprecated
        public VariableField(String string, boolean strict) {
            this.canonicalIndex = DateTimePatternGenerator.getCanonicalIndex(string, strict);
            if (this.canonicalIndex < 0) {
                throw new IllegalArgumentException("Illegal datetime field:\t" + string);
            }
            this.string = string;
        }

        @Deprecated
        public int getType() {
            return DateTimePatternGenerator.types[this.canonicalIndex][1];
        }

        @Deprecated
        public static String getCanonicalCode(int type) {
            try {
                return DateTimePatternGenerator.CANONICAL_ITEMS[type];
            } catch (Exception e) {
                return String.valueOf(type);
            }
        }

        @Deprecated
        public boolean isNumeric() {
            return DateTimePatternGenerator.types[this.canonicalIndex][2] > 0;
        }

        private int getCanonicalIndex() {
            return this.canonicalIndex;
        }

        @Deprecated
        public String toString() {
            return this.string;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.DateTimePatternGenerator.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.DateTimePatternGenerator.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateTimePatternGenerator.<clinit>():void");
    }

    public static DateTimePatternGenerator getEmptyInstance() {
        return new DateTimePatternGenerator();
    }

    protected DateTimePatternGenerator() {
        this.skeleton2pattern = new TreeMap();
        this.basePattern_pattern = new TreeMap();
        this.decimal = "?";
        this.dateTimeFormat = "{1} {0}";
        this.appendItemFormats = new String[16];
        this.appendItemNames = new String[16];
        for (int i = 0; i < 16; i++) {
            this.appendItemFormats[i] = "{0} ├{2}: {1}┤";
            this.appendItemNames[i] = "F" + i;
        }
        this.defaultHourFormatChar = 'H';
        this.frozen = false;
        this.current = new DateTimeMatcher();
        this.fp = new FormatParser();
        this._distanceInfo = new DistanceInfo();
        complete();
        this.cldrAvailableFormatKeys = new HashSet(20);
    }

    public static DateTimePatternGenerator getInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT));
    }

    public static DateTimePatternGenerator getInstance(ULocale uLocale) {
        return getFrozenInstance(uLocale).cloneAsThawed();
    }

    public static DateTimePatternGenerator getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    @Deprecated
    public static DateTimePatternGenerator getFrozenInstance(ULocale uLocale) {
        String localeKey = uLocale.toString();
        DateTimePatternGenerator result = (DateTimePatternGenerator) DTPNG_CACHE.get(localeKey);
        if (result != null) {
            return result;
        }
        int i;
        ICUResourceBundle itemBundle;
        result = new DateTimePatternGenerator();
        PatternInfo returnInfo = new PatternInfo();
        String shortTimePattern = null;
        for (i = 0; i <= 3; i++) {
            result.addPattern(((SimpleDateFormat) DateFormat.getDateInstance(i, uLocale)).toPattern(), false, returnInfo);
            SimpleDateFormat df = (SimpleDateFormat) DateFormat.getTimeInstance(i, uLocale);
            result.addPattern(df.toPattern(), false, returnInfo);
            if (i == 3) {
                shortTimePattern = df.toPattern();
                FormatParser fp = new FormatParser();
                fp.set(shortTimePattern);
                List<Object> items = fp.getItems();
                for (int idx = 0; idx < items.size(); idx++) {
                    VariableField item = items.get(idx);
                    if (item instanceof VariableField) {
                        VariableField fld = item;
                        if (fld.getType() == 11) {
                            result.defaultHourFormatChar = fld.toString().charAt(0);
                            break;
                        }
                    }
                }
            }
        }
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", uLocale);
        String calendarTypeToUse = uLocale.getKeywordValue("calendar");
        if (calendarTypeToUse == null) {
            calendarTypeToUse = Calendar.getKeywordValuesForLocale("calendar", uLocale, true)[0];
        }
        if (calendarTypeToUse == null) {
            calendarTypeToUse = "gregorian";
        }
        try {
            itemBundle = rb.getWithFallback("calendar/" + calendarTypeToUse + "/appendItems");
            for (i = 0; i < itemBundle.getSize(); i++) {
                ICUResourceBundle formatBundle = (ICUResourceBundle) itemBundle.get(i);
                String formatName = itemBundle.get(i).getKey();
                result.setAppendItemFormat(getAppendFormatNumber(formatName), formatBundle.getString());
            }
        } catch (MissingResourceException e) {
        }
        try {
            itemBundle = rb.getWithFallback("fields");
            for (i = 0; i < 16; i++) {
                if (isCLDRFieldName(i)) {
                    result.setAppendItemName(i, itemBundle.getWithFallback(CLDR_FIELD_NAME[i]).getWithFallback("dn").getString());
                }
            }
        } catch (MissingResourceException e2) {
        }
        ICUResourceBundle availFormatsBundle = null;
        try {
            availFormatsBundle = rb.getWithFallback("calendar/" + calendarTypeToUse + "/availableFormats");
        } catch (MissingResourceException e3) {
        }
        boolean override = true;
        while (availFormatsBundle != null) {
            for (i = 0; i < availFormatsBundle.getSize(); i++) {
                String formatKey = availFormatsBundle.get(i).getKey();
                if (!result.isAvailableFormatSet(formatKey)) {
                    result.setAvailableFormat(formatKey);
                    result.addPatternWithSkeleton(availFormatsBundle.get(i).getString(), formatKey, override, returnInfo);
                }
            }
            ICUResourceBundle pbundle = (ICUResourceBundle) availFormatsBundle.getParent();
            if (pbundle == null) {
                break;
            }
            try {
                availFormatsBundle = pbundle.getWithFallback("calendar/" + calendarTypeToUse + "/availableFormats");
            } catch (MissingResourceException e4) {
                availFormatsBundle = null;
            }
            if (availFormatsBundle != null && pbundle.getULocale().getBaseName().equals("root")) {
                override = false;
            }
        }
        if (shortTimePattern != null) {
            hackTimes(result, returnInfo, shortTimePattern);
        }
        result.setDateTimeFormat(Calendar.getDateTimePattern(Calendar.getInstance(uLocale), uLocale, 2));
        result.setDecimal(String.valueOf(new DecimalFormatSymbols(uLocale).getDecimalSeparator()));
        result.freeze();
        DTPNG_CACHE.put(localeKey, result);
        return result;
    }

    @Deprecated
    public char getDefaultHourFormatChar() {
        return this.defaultHourFormatChar;
    }

    @Deprecated
    public void setDefaultHourFormatChar(char defaultHourFormatChar) {
        this.defaultHourFormatChar = defaultHourFormatChar;
    }

    private static void hackTimes(DateTimePatternGenerator result, PatternInfo returnInfo, String hackPattern) {
        int i;
        Object item;
        char ch;
        result.fp.set(hackPattern);
        StringBuilder mmss = new StringBuilder();
        boolean gotMm = false;
        for (i = 0; i < result.fp.items.size(); i++) {
            item = result.fp.items.get(i);
            if (!(item instanceof String)) {
                ch = item.toString().charAt(0);
                if (ch != 'm') {
                    if (ch != 's') {
                        if (gotMm || ch == 'z' || ch == 'Z' || ch == 'v' || ch == 'V') {
                            break;
                        }
                    } else if (gotMm) {
                        mmss.append(item);
                        result.addPattern(mmss.toString(), false, returnInfo);
                    }
                } else {
                    gotMm = true;
                    mmss.append(item);
                }
            } else if (gotMm) {
                mmss.append(result.fp.quoteLiteral(item.toString()));
            }
        }
        BitSet variables = new BitSet();
        BitSet nuke = new BitSet();
        for (i = 0; i < result.fp.items.size(); i++) {
            item = result.fp.items.get(i);
            if (item instanceof VariableField) {
                variables.set(i);
                ch = item.toString().charAt(0);
                if (ch == 's' || ch == 'S') {
                    nuke.set(i);
                    int j = i - 1;
                    while (j >= 0 && !variables.get(j)) {
                        nuke.set(i);
                        j++;
                    }
                }
            }
        }
        result.addPattern(getFilteredPattern(result.fp, nuke), false, returnInfo);
    }

    private static String getFilteredPattern(FormatParser fp, BitSet nuke) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fp.items.size(); i++) {
            if (!nuke.get(i)) {
                Object item = fp.items.get(i);
                if (item instanceof String) {
                    result.append(fp.quoteLiteral(item.toString()));
                } else {
                    result.append(item.toString());
                }
            }
        }
        return result.toString();
    }

    @Deprecated
    public static int getAppendFormatNumber(String string) {
        for (int i = 0; i < CLDR_FIELD_APPEND.length; i++) {
            if (CLDR_FIELD_APPEND[i].equals(string)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isCLDRFieldName(int index) {
        if ((index >= 0 || index < 16) && CLDR_FIELD_NAME[index].charAt(0) != '*') {
            return true;
        }
        return false;
    }

    public String getBestPattern(String skeleton) {
        return getBestPattern(skeleton, null, 0);
    }

    public String getBestPattern(String skeleton, int options) {
        return getBestPattern(skeleton, null, options);
    }

    /* JADX WARNING: Missing block: B:28:0x00b6, code:
            if (r10 != null) goto L_0x00c1;
     */
    /* JADX WARNING: Missing block: B:29:0x00b8, code:
            if (r16 != null) goto L_0x00bd;
     */
    /* JADX WARNING: Missing block: B:30:0x00ba, code:
            r16 = "";
     */
    /* JADX WARNING: Missing block: B:31:0x00bd, code:
            return r16;
     */
    /* JADX WARNING: Missing block: B:35:0x00c1, code:
            if (r16 != null) goto L_0x00c4;
     */
    /* JADX WARNING: Missing block: B:36:0x00c3, code:
            return r10;
     */
    /* JADX WARNING: Missing block: B:37:0x00c4, code:
            r2 = getDateTimeFormat();
            r3 = new java.lang.Object[2];
            r3[0] = r16;
            r3[1] = r10;
     */
    /* JADX WARNING: Missing block: B:38:0x00d5, code:
            return android.icu.text.MessageFormat.format(r2, r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getBestPattern(String skeleton, DateTimeMatcher skipMatcher, int options) {
        EnumSet<DTPGflags> flags = EnumSet.noneOf(DTPGflags.class);
        StringBuilder skeletonCopy = new StringBuilder(skeleton);
        boolean inQuoted = false;
        for (int patPos = 0; patPos < skeletonCopy.length(); patPos++) {
            char patChr = skeletonCopy.charAt(patPos);
            if (patChr == PatternTokenizer.SINGLE_QUOTE) {
                inQuoted = !inQuoted;
            } else if (!inQuoted) {
                if (patChr == 'j') {
                    skeletonCopy.setCharAt(patPos, this.defaultHourFormatChar);
                } else if (patChr == 'J') {
                    skeletonCopy.setCharAt(patPos, 'H');
                    flags.add(DTPGflags.SKELETON_USES_CAP_J);
                }
            }
        }
        synchronized (this) {
            this.current.set(skeletonCopy.toString(), this.fp, false);
            PatternWithMatcher bestWithMatcher = getBestRaw(this.current, -1, this._distanceInfo, skipMatcher);
            if (this._distanceInfo.missingFieldMask == 0 && this._distanceInfo.extraFieldMask == 0) {
                String adjustFieldTypes = adjustFieldTypes(bestWithMatcher, this.current, flags, options);
                return adjustFieldTypes;
            }
            int neededFields = this.current.getFieldMask();
            String datePattern = getBestAppending(this.current, neededFields & 1023, this._distanceInfo, skipMatcher, flags, options);
            String timePattern = getBestAppending(this.current, neededFields & TIME_MASK, this._distanceInfo, skipMatcher, flags, options);
        }
    }

    public DateTimePatternGenerator addPattern(String pattern, boolean override, PatternInfo returnInfo) {
        return addPatternWithSkeleton(pattern, null, override, returnInfo);
    }

    @Deprecated
    public DateTimePatternGenerator addPatternWithSkeleton(String pattern, String skeletonToUse, boolean override, PatternInfo returnInfo) {
        DateTimeMatcher matcher;
        boolean z = true;
        checkFrozen();
        if (skeletonToUse == null) {
            matcher = new DateTimeMatcher().set(pattern, this.fp, false);
        } else {
            matcher = new DateTimeMatcher().set(skeletonToUse, this.fp, false);
        }
        String basePattern = matcher.getBasePattern();
        PatternWithSkeletonFlag previousPatternWithSameBase = (PatternWithSkeletonFlag) this.basePattern_pattern.get(basePattern);
        if (!(previousPatternWithSameBase == null || (previousPatternWithSameBase.skeletonWasSpecified && (skeletonToUse == null || override)))) {
            returnInfo.status = 1;
            returnInfo.conflictingPattern = previousPatternWithSameBase.pattern;
            if (!override) {
                return this;
            }
        }
        PatternWithSkeletonFlag previousValue = (PatternWithSkeletonFlag) this.skeleton2pattern.get(matcher);
        if (previousValue != null) {
            returnInfo.status = 2;
            returnInfo.conflictingPattern = previousValue.pattern;
            if (!override || (skeletonToUse != null && previousValue.skeletonWasSpecified)) {
                return this;
            }
        }
        returnInfo.status = 0;
        returnInfo.conflictingPattern = "";
        if (skeletonToUse == null) {
            z = false;
        }
        PatternWithSkeletonFlag patWithSkelFlag = new PatternWithSkeletonFlag(pattern, z);
        this.skeleton2pattern.put(matcher, patWithSkelFlag);
        this.basePattern_pattern.put(basePattern, patWithSkelFlag);
        return this;
    }

    public String getSkeleton(String pattern) {
        String dateTimeMatcher;
        synchronized (this) {
            this.current.set(pattern, this.fp, false);
            dateTimeMatcher = this.current.toString();
        }
        return dateTimeMatcher;
    }

    @Deprecated
    public String getSkeletonAllowingDuplicates(String pattern) {
        String dateTimeMatcher;
        synchronized (this) {
            this.current.set(pattern, this.fp, true);
            dateTimeMatcher = this.current.toString();
        }
        return dateTimeMatcher;
    }

    @Deprecated
    public String getCanonicalSkeletonAllowingDuplicates(String pattern) {
        String toCanonicalString;
        synchronized (this) {
            this.current.set(pattern, this.fp, true);
            toCanonicalString = this.current.toCanonicalString();
        }
        return toCanonicalString;
    }

    public String getBaseSkeleton(String pattern) {
        String basePattern;
        synchronized (this) {
            this.current.set(pattern, this.fp, false);
            basePattern = this.current.getBasePattern();
        }
        return basePattern;
    }

    public Map<String, String> getSkeletons(Map<String, String> result) {
        if (result == null) {
            result = new LinkedHashMap();
        }
        for (DateTimeMatcher item : this.skeleton2pattern.keySet()) {
            String pattern = ((PatternWithSkeletonFlag) this.skeleton2pattern.get(item)).pattern;
            if (!CANONICAL_SET.contains(pattern)) {
                result.put(item.toString(), pattern);
            }
        }
        return result;
    }

    public Set<String> getBaseSkeletons(Set<String> result) {
        if (result == null) {
            result = new HashSet();
        }
        result.addAll(this.basePattern_pattern.keySet());
        return result;
    }

    public String replaceFieldTypes(String pattern, String skeleton) {
        return replaceFieldTypes(pattern, skeleton, 0);
    }

    public String replaceFieldTypes(String pattern, String skeleton, int options) {
        String adjustFieldTypes;
        synchronized (this) {
            adjustFieldTypes = adjustFieldTypes(new PatternWithMatcher(pattern, null), this.current.set(skeleton, this.fp, false), EnumSet.noneOf(DTPGflags.class), options);
        }
        return adjustFieldTypes;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        checkFrozen();
        this.dateTimeFormat = dateTimeFormat;
    }

    public String getDateTimeFormat() {
        return this.dateTimeFormat;
    }

    public void setDecimal(String decimal) {
        checkFrozen();
        this.decimal = decimal;
    }

    public String getDecimal() {
        return this.decimal;
    }

    @Deprecated
    public Collection<String> getRedundants(Collection<String> output) {
        synchronized (this) {
            if (output == null) {
                output = new LinkedHashSet();
            }
            for (DateTimeMatcher cur : this.skeleton2pattern.keySet()) {
                String pattern = ((PatternWithSkeletonFlag) this.skeleton2pattern.get(cur)).pattern;
                if (!CANONICAL_SET.contains(pattern) && getBestPattern(cur.toString(), cur, 0).equals(pattern)) {
                    output.add(pattern);
                }
            }
        }
        return output;
    }

    public void setAppendItemFormat(int field, String value) {
        checkFrozen();
        this.appendItemFormats[field] = value;
    }

    public String getAppendItemFormat(int field) {
        return this.appendItemFormats[field];
    }

    public void setAppendItemName(int field, String value) {
        checkFrozen();
        this.appendItemNames[field] = value;
    }

    public String getAppendItemName(int field) {
        return this.appendItemNames[field];
    }

    @Deprecated
    public static boolean isSingleField(String skeleton) {
        char first = skeleton.charAt(0);
        for (int i = 1; i < skeleton.length(); i++) {
            if (skeleton.charAt(i) != first) {
                return false;
            }
        }
        return true;
    }

    private void setAvailableFormat(String key) {
        checkFrozen();
        this.cldrAvailableFormatKeys.add(key);
    }

    private boolean isAvailableFormatSet(String key) {
        return this.cldrAvailableFormatKeys.contains(key);
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public DateTimePatternGenerator freeze() {
        this.frozen = true;
        return this;
    }

    public DateTimePatternGenerator cloneAsThawed() {
        DateTimePatternGenerator result = (DateTimePatternGenerator) clone();
        this.frozen = false;
        return result;
    }

    public Object clone() {
        try {
            DateTimePatternGenerator result = (DateTimePatternGenerator) super.clone();
            result.skeleton2pattern = (TreeMap) this.skeleton2pattern.clone();
            result.basePattern_pattern = (TreeMap) this.basePattern_pattern.clone();
            result.appendItemFormats = (String[]) this.appendItemFormats.clone();
            result.appendItemNames = (String[]) this.appendItemNames.clone();
            result.current = new DateTimeMatcher();
            result.fp = new FormatParser();
            result._distanceInfo = new DistanceInfo();
            result.frozen = false;
            return result;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException("Internal Error", e);
        }
    }

    @Deprecated
    public boolean skeletonsAreSimilar(String id, String skeleton) {
        if (id.equals(skeleton)) {
            return true;
        }
        TreeSet<String> parser1 = getSet(id);
        TreeSet<String> parser2 = getSet(skeleton);
        if (parser1.size() != parser2.size()) {
            return false;
        }
        Iterator<String> it2 = parser2.iterator();
        for (String item : parser1) {
            if (types[getCanonicalIndex(item, false)][1] != types[getCanonicalIndex((String) it2.next(), false)][1]) {
                return false;
            }
        }
        return true;
    }

    private TreeSet<String> getSet(String id) {
        List<Object> items = this.fp.set(id).getItems();
        TreeSet<String> result = new TreeSet();
        for (Object obj : items) {
            String item = obj.toString();
            if (!(item.startsWith("G") || item.startsWith("a"))) {
                result.add(item);
            }
        }
        return result;
    }

    private void checkFrozen() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
    }

    private String getBestAppending(DateTimeMatcher source, int missingFields, DistanceInfo distInfo, DateTimeMatcher skipMatcher, EnumSet<DTPGflags> flags, int options) {
        String resultPattern = null;
        if (missingFields != 0) {
            PatternWithMatcher resultPatternWithMatcher = getBestRaw(source, missingFields, distInfo, skipMatcher);
            resultPattern = adjustFieldTypes(resultPatternWithMatcher, source, flags, options);
            while (distInfo.missingFieldMask != 0) {
                if ((distInfo.missingFieldMask & SECOND_AND_FRACTIONAL_MASK) == 16384 && (missingFields & SECOND_AND_FRACTIONAL_MASK) == SECOND_AND_FRACTIONAL_MASK) {
                    resultPatternWithMatcher.pattern = resultPattern;
                    flags = EnumSet.copyOf(flags);
                    flags.add(DTPGflags.FIX_FRACTIONAL_SECONDS);
                    resultPattern = adjustFieldTypes(resultPatternWithMatcher, source, flags, options);
                    distInfo.missingFieldMask &= -16385;
                } else {
                    int startingMask = distInfo.missingFieldMask;
                    String temp = adjustFieldTypes(getBestRaw(source, distInfo.missingFieldMask, distInfo, skipMatcher), source, flags, options);
                    int topField = getTopBitNumber(startingMask & (~distInfo.missingFieldMask));
                    String appendFormat = getAppendFormat(topField);
                    Object[] objArr = new Object[3];
                    objArr[0] = resultPattern;
                    objArr[1] = temp;
                    objArr[2] = getAppendName(topField);
                    resultPattern = MessageFormat.format(appendFormat, objArr);
                }
            }
        }
        return resultPattern;
    }

    private String getAppendName(int foundMask) {
        return "'" + this.appendItemNames[foundMask] + "'";
    }

    private String getAppendFormat(int foundMask) {
        return this.appendItemFormats[foundMask];
    }

    private int getTopBitNumber(int foundMask) {
        int i = 0;
        while (foundMask != 0) {
            foundMask >>>= 1;
            i++;
        }
        return i - 1;
    }

    private void complete() {
        PatternInfo patternInfo = new PatternInfo();
        for (Object valueOf : CANONICAL_ITEMS) {
            addPattern(String.valueOf(valueOf), false, patternInfo);
        }
    }

    private PatternWithMatcher getBestRaw(DateTimeMatcher source, int includeMask, DistanceInfo missingFields, DateTimeMatcher skipMatcher) {
        int bestDistance = Integer.MAX_VALUE;
        PatternWithMatcher bestPatternWithMatcher = new PatternWithMatcher("", null);
        DistanceInfo tempInfo = new DistanceInfo();
        for (DateTimeMatcher trial : this.skeleton2pattern.keySet()) {
            if (!trial.equals(skipMatcher)) {
                int distance = source.getDistance(trial, includeMask, tempInfo);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    PatternWithSkeletonFlag patternWithSkelFlag = (PatternWithSkeletonFlag) this.skeleton2pattern.get(trial);
                    bestPatternWithMatcher.pattern = patternWithSkelFlag.pattern;
                    if (patternWithSkelFlag.skeletonWasSpecified) {
                        bestPatternWithMatcher.matcherWithSkeleton = trial;
                    } else {
                        bestPatternWithMatcher.matcherWithSkeleton = null;
                    }
                    missingFields.setTo(tempInfo);
                    if (distance == 0) {
                        break;
                    }
                } else {
                    continue;
                }
            }
        }
        return bestPatternWithMatcher;
    }

    private String adjustFieldTypes(PatternWithMatcher patternWithMatcher, DateTimeMatcher inputRequest, EnumSet<DTPGflags> flags, int options) {
        this.fp.set(patternWithMatcher.pattern);
        StringBuilder newPattern = new StringBuilder();
        for (VariableField item : this.fp.getItems()) {
            if (item instanceof String) {
                newPattern.append(this.fp.quoteLiteral((String) item));
            } else {
                VariableField variableField = item;
                StringBuilder fieldBuilder = new StringBuilder(variableField.toString());
                int type = variableField.getType();
                if (flags.contains(DTPGflags.FIX_FRACTIONAL_SECONDS) && type == 13) {
                    String newField = inputRequest.original[14];
                    fieldBuilder.append(this.decimal);
                    fieldBuilder.append(newField);
                } else if (inputRequest.type[type] != 0) {
                    String reqField = inputRequest.original[type];
                    int reqFieldLen = reqField.length();
                    if (reqField.charAt(0) == 'E' && reqFieldLen < 3) {
                        reqFieldLen = 3;
                    }
                    int adjFieldLen = reqFieldLen;
                    DateTimeMatcher matcherWithSkeleton = patternWithMatcher.matcherWithSkeleton;
                    if ((type == 11 && (options & 2048) == 0) || ((type == 12 && (options & 4096) == 0) || (type == 13 && (options & 8192) == 0))) {
                        adjFieldLen = fieldBuilder.length();
                    } else if (matcherWithSkeleton != null) {
                        int skelFieldLen = matcherWithSkeleton.origStringForField(type).length();
                        boolean patFieldIsNumeric = variableField.isNumeric();
                        boolean skelFieldIsNumeric = matcherWithSkeleton.fieldIsNumeric(type);
                        if (skelFieldLen == reqFieldLen || ((patFieldIsNumeric && !skelFieldIsNumeric) || (skelFieldIsNumeric && !patFieldIsNumeric))) {
                            adjFieldLen = fieldBuilder.length();
                        }
                    }
                    char c = (type == 11 || type == 3 || type == 6 || (type == 1 && reqField.charAt(0) != 'Y')) ? fieldBuilder.charAt(0) : reqField.charAt(0);
                    if (type == 11 && flags.contains(DTPGflags.SKELETON_USES_CAP_J)) {
                        c = this.defaultHourFormatChar;
                    }
                    fieldBuilder = new StringBuilder();
                    for (int i = adjFieldLen; i > 0; i--) {
                        fieldBuilder.append(c);
                    }
                }
                newPattern.append(fieldBuilder);
            }
        }
        return newPattern.toString();
    }

    @Deprecated
    public String getFields(String pattern) {
        this.fp.set(pattern);
        StringBuilder newPattern = new StringBuilder();
        for (Object item : this.fp.getItems()) {
            if (item instanceof String) {
                newPattern.append(this.fp.quoteLiteral((String) item));
            } else {
                newPattern.append("{").append(getName(item.toString())).append("}");
            }
        }
        return newPattern.toString();
    }

    private static String showMask(int mask) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            if (((1 << i) & mask) != 0) {
                if (result.length() != 0) {
                    result.append(" | ");
                }
                result.append(FIELD_NAME[i]);
                result.append(" ");
            }
        }
        return result.toString();
    }

    private static String getName(String s) {
        boolean string = true;
        int i = getCanonicalIndex(s, true);
        String name = FIELD_NAME[types[i][1]];
        int subtype = types[i][2];
        if (subtype >= 0) {
            string = false;
        }
        if (string) {
            subtype = -subtype;
        }
        if (subtype < 0) {
            return name + ":S";
        }
        return name + ":N";
    }

    private static int getCanonicalIndex(String s, boolean strict) {
        int len = s.length();
        if (len == 0) {
            return -1;
        }
        int i;
        char ch = s.charAt(0);
        for (i = 1; i < len; i++) {
            if (s.charAt(i) != ch) {
                return -1;
            }
        }
        int bestRow = -1;
        for (i = 0; i < types.length; i++) {
            int[] row = types[i];
            if (row[0] == ch) {
                bestRow = i;
                if (row[3] <= len && row[row.length - 1] >= len) {
                    return i;
                }
            }
        }
        if (strict) {
            bestRow = -1;
        }
        return bestRow;
    }
}
