package android.icu.text;

import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import android.icu.impl.locale.LanguageTag;
import android.icu.lang.UCharacter;
import android.icu.math.MathContext;
import android.icu.text.NumberFormat.Field;
import android.icu.text.PluralRules.FixedDecimal;
import android.icu.util.Currency;
import android.icu.util.Currency.CurrencyUsage;
import android.icu.util.CurrencyAmount;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.ChoiceFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
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
public class DecimalFormat extends NumberFormat {
    private static final char CURRENCY_SIGN = '¤';
    private static final int CURRENCY_SIGN_COUNT_IN_ISO_FORMAT = 2;
    private static final int CURRENCY_SIGN_COUNT_IN_PLURAL_FORMAT = 3;
    private static final int CURRENCY_SIGN_COUNT_IN_SYMBOL_FORMAT = 1;
    private static final int CURRENCY_SIGN_COUNT_ZERO = 0;
    static final int DOUBLE_FRACTION_DIGITS = 340;
    static final int DOUBLE_INTEGER_DIGITS = 309;
    static final int MAX_INTEGER_DIGITS = 2000000000;
    static final int MAX_SCIENTIFIC_INTEGER_DIGITS = 8;
    static final Unit NULL_UNIT = null;
    public static final int PAD_AFTER_PREFIX = 1;
    public static final int PAD_AFTER_SUFFIX = 3;
    public static final int PAD_BEFORE_PREFIX = 0;
    public static final int PAD_BEFORE_SUFFIX = 2;
    static final char PATTERN_DECIMAL_SEPARATOR = '.';
    static final char PATTERN_DIGIT = '#';
    static final char PATTERN_EIGHT_DIGIT = '8';
    static final char PATTERN_EXPONENT = 'E';
    static final char PATTERN_FIVE_DIGIT = '5';
    static final char PATTERN_FOUR_DIGIT = '4';
    static final char PATTERN_GROUPING_SEPARATOR = ',';
    private static final char PATTERN_MINUS = '-';
    static final char PATTERN_NINE_DIGIT = '9';
    static final char PATTERN_ONE_DIGIT = '1';
    static final char PATTERN_PAD_ESCAPE = '*';
    private static final char PATTERN_PERCENT = '%';
    private static final char PATTERN_PER_MILLE = '‰';
    static final char PATTERN_PLUS_SIGN = '+';
    private static final char PATTERN_SEPARATOR = ';';
    static final char PATTERN_SEVEN_DIGIT = '7';
    static final char PATTERN_SIGNIFICANT_DIGIT = '@';
    static final char PATTERN_SIX_DIGIT = '6';
    static final char PATTERN_THREE_DIGIT = '3';
    static final char PATTERN_TWO_DIGIT = '2';
    static final char PATTERN_ZERO_DIGIT = '0';
    private static final char QUOTE = '\'';
    private static final int STATUS_INFINITE = 0;
    private static final int STATUS_LENGTH = 3;
    private static final int STATUS_POSITIVE = 1;
    private static final int STATUS_UNDERFLOW = 2;
    private static final UnicodeSet commaEquivalents = null;
    static final int currentSerialVersion = 4;
    private static final UnicodeSet defaultGroupingSeparators = null;
    private static final UnicodeSet dotEquivalents = null;
    private static double epsilon = 0.0d;
    static final UnicodeSet minusSigns = null;
    static final UnicodeSet plusSigns = null;
    static final double roundingIncrementEpsilon = 1.0E-9d;
    private static final long serialVersionUID = 864413376551465018L;
    static final boolean skipExtendedSeparatorParsing = false;
    private static final UnicodeSet strictCommaEquivalents = null;
    private static final UnicodeSet strictDefaultGroupingSeparators = null;
    private static final UnicodeSet strictDotEquivalents = null;
    private int PARSE_MAX_EXPONENT;
    private transient BigDecimal actualRoundingIncrement;
    private transient android.icu.math.BigDecimal actualRoundingIncrementICU;
    private transient Set<AffixForCurrency> affixPatternsForCurrency;
    private ArrayList<FieldPosition> attributes;
    private ChoiceFormat currencyChoice;
    private CurrencyPluralInfo currencyPluralInfo;
    private int currencySignCount;
    private CurrencyUsage currencyUsage;
    private boolean decimalSeparatorAlwaysShown;
    private transient DigitList digitList;
    private boolean exponentSignAlwaysShown;
    private String formatPattern;
    private int formatWidth;
    private byte groupingSize;
    private byte groupingSize2;
    private transient boolean isReadyForParsing;
    private MathContext mathContext;
    private int maxSignificantDigits;
    private byte minExponentDigits;
    private int minSignificantDigits;
    private int multiplier;
    private String negPrefixPattern;
    private String negSuffixPattern;
    private String negativePrefix;
    private String negativeSuffix;
    private char pad;
    private int padPosition;
    private boolean parseBigDecimal;
    boolean parseRequireDecimalPoint;
    private String posPrefixPattern;
    private String posSuffixPattern;
    private String positivePrefix;
    private String positiveSuffix;
    private transient double roundingDouble;
    private transient double roundingDoubleReciprocal;
    private BigDecimal roundingIncrement;
    private transient android.icu.math.BigDecimal roundingIncrementICU;
    private int roundingMode;
    private int serialVersionOnStream;
    private int style;
    private DecimalFormatSymbols symbols;
    private boolean useExponentialNotation;
    private boolean useSignificantDigits;

    private static final class AffixForCurrency {
        private String negPrefixPatternForCurrency;
        private String negSuffixPatternForCurrency;
        private final int patternType;
        private String posPrefixPatternForCurrency;
        private String posSuffixPatternForCurrency;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.text.DecimalFormat.AffixForCurrency.<init>(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public AffixForCurrency(java.lang.String r1, java.lang.String r2, java.lang.String r3, java.lang.String r4, int r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.text.DecimalFormat.AffixForCurrency.<init>(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DecimalFormat.AffixForCurrency.<init>(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.DecimalFormat.AffixForCurrency.getNegPrefix():java.lang.String, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.String getNegPrefix() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.DecimalFormat.AffixForCurrency.getNegPrefix():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DecimalFormat.AffixForCurrency.getNegPrefix():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.DecimalFormat.AffixForCurrency.getNegSuffix():java.lang.String, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.String getNegSuffix() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.DecimalFormat.AffixForCurrency.getNegSuffix():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DecimalFormat.AffixForCurrency.getNegSuffix():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.icu.text.DecimalFormat.AffixForCurrency.getPatternType():int, dex:  in method: android.icu.text.DecimalFormat.AffixForCurrency.getPatternType():int, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.icu.text.DecimalFormat.AffixForCurrency.getPatternType():int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public int getPatternType() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.icu.text.DecimalFormat.AffixForCurrency.getPatternType():int, dex:  in method: android.icu.text.DecimalFormat.AffixForCurrency.getPatternType():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DecimalFormat.AffixForCurrency.getPatternType():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.DecimalFormat.AffixForCurrency.getPosPrefix():java.lang.String, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.String getPosPrefix() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.DecimalFormat.AffixForCurrency.getPosPrefix():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DecimalFormat.AffixForCurrency.getPosPrefix():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.icu.text.DecimalFormat.AffixForCurrency.getPosSuffix():java.lang.String, dex:  in method: android.icu.text.DecimalFormat.AffixForCurrency.getPosSuffix():java.lang.String, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.icu.text.DecimalFormat.AffixForCurrency.getPosSuffix():java.lang.String, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public java.lang.String getPosSuffix() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.icu.text.DecimalFormat.AffixForCurrency.getPosSuffix():java.lang.String, dex:  in method: android.icu.text.DecimalFormat.AffixForCurrency.getPosSuffix():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DecimalFormat.AffixForCurrency.getPosSuffix():java.lang.String");
        }
    }

    static class Unit {
        private final String prefix;
        private final String suffix;

        public Unit(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public void writeSuffix(StringBuffer toAppendTo) {
            toAppendTo.append(this.suffix);
        }

        public void writePrefix(StringBuffer toAppendTo) {
            toAppendTo.append(this.prefix);
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Unit)) {
                return false;
            }
            Unit other = (Unit) obj;
            if (this.prefix.equals(other.prefix)) {
                z = this.suffix.equals(other.suffix);
            }
            return z;
        }

        public String toString() {
            return this.prefix + "/" + this.suffix;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.text.DecimalFormat.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.text.DecimalFormat.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DecimalFormat.<clinit>():void");
    }

    public DecimalFormat() {
        this.parseRequireDecimalPoint = false;
        this.PARSE_MAX_EXPONENT = 1000;
        this.digitList = new DigitList();
        this.positivePrefix = "";
        this.positiveSuffix = "";
        this.negativePrefix = LanguageTag.SEP;
        this.negativeSuffix = "";
        this.multiplier = 1;
        this.groupingSize = (byte) 3;
        this.groupingSize2 = (byte) 0;
        this.decimalSeparatorAlwaysShown = false;
        this.symbols = null;
        this.useSignificantDigits = false;
        this.minSignificantDigits = 1;
        this.maxSignificantDigits = 6;
        this.exponentSignAlwaysShown = false;
        this.roundingIncrement = null;
        this.roundingIncrementICU = null;
        this.roundingMode = 6;
        this.mathContext = new MathContext(0, 0);
        this.formatWidth = 0;
        this.pad = ' ';
        this.padPosition = 0;
        this.parseBigDecimal = false;
        this.currencyUsage = CurrencyUsage.STANDARD;
        this.serialVersionOnStream = 4;
        this.attributes = new ArrayList();
        this.formatPattern = "";
        this.style = 0;
        this.currencySignCount = 0;
        this.affixPatternsForCurrency = null;
        this.isReadyForParsing = false;
        this.currencyPluralInfo = null;
        this.actualRoundingIncrementICU = null;
        this.actualRoundingIncrement = null;
        this.roundingDouble = 0.0d;
        this.roundingDoubleReciprocal = 0.0d;
        ULocale def = ULocale.getDefault(Category.FORMAT);
        String pattern = NumberFormat.getPattern(def, 0);
        this.symbols = new DecimalFormatSymbols(def);
        setCurrency(Currency.getInstance(def));
        applyPatternWithoutExpandAffix(pattern, false);
        if (this.currencySignCount == 3) {
            this.currencyPluralInfo = new CurrencyPluralInfo(def);
        } else {
            expandAffixAdjustWidth(null);
        }
    }

    public DecimalFormat(String pattern) {
        this.parseRequireDecimalPoint = false;
        this.PARSE_MAX_EXPONENT = 1000;
        this.digitList = new DigitList();
        this.positivePrefix = "";
        this.positiveSuffix = "";
        this.negativePrefix = LanguageTag.SEP;
        this.negativeSuffix = "";
        this.multiplier = 1;
        this.groupingSize = (byte) 3;
        this.groupingSize2 = (byte) 0;
        this.decimalSeparatorAlwaysShown = false;
        this.symbols = null;
        this.useSignificantDigits = false;
        this.minSignificantDigits = 1;
        this.maxSignificantDigits = 6;
        this.exponentSignAlwaysShown = false;
        this.roundingIncrement = null;
        this.roundingIncrementICU = null;
        this.roundingMode = 6;
        this.mathContext = new MathContext(0, 0);
        this.formatWidth = 0;
        this.pad = ' ';
        this.padPosition = 0;
        this.parseBigDecimal = false;
        this.currencyUsage = CurrencyUsage.STANDARD;
        this.serialVersionOnStream = 4;
        this.attributes = new ArrayList();
        this.formatPattern = "";
        this.style = 0;
        this.currencySignCount = 0;
        this.affixPatternsForCurrency = null;
        this.isReadyForParsing = false;
        this.currencyPluralInfo = null;
        this.actualRoundingIncrementICU = null;
        this.actualRoundingIncrement = null;
        this.roundingDouble = 0.0d;
        this.roundingDoubleReciprocal = 0.0d;
        ULocale def = ULocale.getDefault(Category.FORMAT);
        this.symbols = new DecimalFormatSymbols(def);
        setCurrency(Currency.getInstance(def));
        applyPatternWithoutExpandAffix(pattern, false);
        if (this.currencySignCount == 3) {
            this.currencyPluralInfo = new CurrencyPluralInfo(def);
        } else {
            expandAffixAdjustWidth(null);
        }
    }

    public DecimalFormat(String pattern, DecimalFormatSymbols symbols) {
        this.parseRequireDecimalPoint = false;
        this.PARSE_MAX_EXPONENT = 1000;
        this.digitList = new DigitList();
        this.positivePrefix = "";
        this.positiveSuffix = "";
        this.negativePrefix = LanguageTag.SEP;
        this.negativeSuffix = "";
        this.multiplier = 1;
        this.groupingSize = (byte) 3;
        this.groupingSize2 = (byte) 0;
        this.decimalSeparatorAlwaysShown = false;
        this.symbols = null;
        this.useSignificantDigits = false;
        this.minSignificantDigits = 1;
        this.maxSignificantDigits = 6;
        this.exponentSignAlwaysShown = false;
        this.roundingIncrement = null;
        this.roundingIncrementICU = null;
        this.roundingMode = 6;
        this.mathContext = new MathContext(0, 0);
        this.formatWidth = 0;
        this.pad = ' ';
        this.padPosition = 0;
        this.parseBigDecimal = false;
        this.currencyUsage = CurrencyUsage.STANDARD;
        this.serialVersionOnStream = 4;
        this.attributes = new ArrayList();
        this.formatPattern = "";
        this.style = 0;
        this.currencySignCount = 0;
        this.affixPatternsForCurrency = null;
        this.isReadyForParsing = false;
        this.currencyPluralInfo = null;
        this.actualRoundingIncrementICU = null;
        this.actualRoundingIncrement = null;
        this.roundingDouble = 0.0d;
        this.roundingDoubleReciprocal = 0.0d;
        createFromPatternAndSymbols(pattern, symbols);
    }

    private void createFromPatternAndSymbols(String pattern, DecimalFormatSymbols inputSymbols) {
        this.symbols = (DecimalFormatSymbols) inputSymbols.clone();
        if (pattern.indexOf(164) >= 0) {
            setCurrencyForSymbols();
        }
        applyPatternWithoutExpandAffix(pattern, false);
        if (this.currencySignCount == 3) {
            this.currencyPluralInfo = new CurrencyPluralInfo(this.symbols.getULocale());
        } else {
            expandAffixAdjustWidth(null);
        }
    }

    public DecimalFormat(String pattern, DecimalFormatSymbols symbols, CurrencyPluralInfo infoInput, int style) {
        this.parseRequireDecimalPoint = false;
        this.PARSE_MAX_EXPONENT = 1000;
        this.digitList = new DigitList();
        this.positivePrefix = "";
        this.positiveSuffix = "";
        this.negativePrefix = LanguageTag.SEP;
        this.negativeSuffix = "";
        this.multiplier = 1;
        this.groupingSize = (byte) 3;
        this.groupingSize2 = (byte) 0;
        this.decimalSeparatorAlwaysShown = false;
        this.symbols = null;
        this.useSignificantDigits = false;
        this.minSignificantDigits = 1;
        this.maxSignificantDigits = 6;
        this.exponentSignAlwaysShown = false;
        this.roundingIncrement = null;
        this.roundingIncrementICU = null;
        this.roundingMode = 6;
        this.mathContext = new MathContext(0, 0);
        this.formatWidth = 0;
        this.pad = ' ';
        this.padPosition = 0;
        this.parseBigDecimal = false;
        this.currencyUsage = CurrencyUsage.STANDARD;
        this.serialVersionOnStream = 4;
        this.attributes = new ArrayList();
        this.formatPattern = "";
        this.style = 0;
        this.currencySignCount = 0;
        this.affixPatternsForCurrency = null;
        this.isReadyForParsing = false;
        this.currencyPluralInfo = null;
        this.actualRoundingIncrementICU = null;
        this.actualRoundingIncrement = null;
        this.roundingDouble = 0.0d;
        this.roundingDoubleReciprocal = 0.0d;
        CurrencyPluralInfo info = infoInput;
        if (style == 6) {
            info = (CurrencyPluralInfo) infoInput.clone();
        }
        create(pattern, symbols, info, style);
    }

    private void create(String pattern, DecimalFormatSymbols inputSymbols, CurrencyPluralInfo info, int inputStyle) {
        if (inputStyle != 6) {
            createFromPatternAndSymbols(pattern, inputSymbols);
        } else {
            this.symbols = (DecimalFormatSymbols) inputSymbols.clone();
            this.currencyPluralInfo = info;
            applyPatternWithoutExpandAffix(this.currencyPluralInfo.getCurrencyPluralPattern(PluralRules.KEYWORD_OTHER), false);
            setCurrencyForSymbols();
        }
        this.style = inputStyle;
    }

    DecimalFormat(String pattern, DecimalFormatSymbols inputSymbols, int style) {
        this.parseRequireDecimalPoint = false;
        this.PARSE_MAX_EXPONENT = 1000;
        this.digitList = new DigitList();
        this.positivePrefix = "";
        this.positiveSuffix = "";
        this.negativePrefix = LanguageTag.SEP;
        this.negativeSuffix = "";
        this.multiplier = 1;
        this.groupingSize = (byte) 3;
        this.groupingSize2 = (byte) 0;
        this.decimalSeparatorAlwaysShown = false;
        this.symbols = null;
        this.useSignificantDigits = false;
        this.minSignificantDigits = 1;
        this.maxSignificantDigits = 6;
        this.exponentSignAlwaysShown = false;
        this.roundingIncrement = null;
        this.roundingIncrementICU = null;
        this.roundingMode = 6;
        this.mathContext = new MathContext(0, 0);
        this.formatWidth = 0;
        this.pad = ' ';
        this.padPosition = 0;
        this.parseBigDecimal = false;
        this.currencyUsage = CurrencyUsage.STANDARD;
        this.serialVersionOnStream = 4;
        this.attributes = new ArrayList();
        this.formatPattern = "";
        this.style = 0;
        this.currencySignCount = 0;
        this.affixPatternsForCurrency = null;
        this.isReadyForParsing = false;
        this.currencyPluralInfo = null;
        this.actualRoundingIncrementICU = null;
        this.actualRoundingIncrement = null;
        this.roundingDouble = 0.0d;
        this.roundingDoubleReciprocal = 0.0d;
        CurrencyPluralInfo currencyPluralInfo = null;
        if (style == 6) {
            currencyPluralInfo = new CurrencyPluralInfo(inputSymbols.getULocale());
        }
        create(pattern, inputSymbols, currencyPluralInfo, style);
    }

    public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private boolean isNegative(double number) {
        if (number >= 0.0d) {
            return number == 0.0d && 1.0d / number < 0.0d;
        } else {
            return true;
        }
    }

    private double round(double number) {
        boolean isNegative = isNegative(number);
        if (isNegative) {
            number = -number;
        }
        if (this.roundingDouble <= 0.0d) {
            return number;
        }
        return round(number, this.roundingDouble, this.roundingDoubleReciprocal, this.roundingMode, isNegative);
    }

    private double multiply(double number) {
        if (this.multiplier != 1) {
            return ((double) this.multiplier) * number;
        }
        return number;
    }

    private StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition, boolean parseAttr) {
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);
        if (Double.isNaN(number)) {
            if (fieldPosition.getField() == 0) {
                fieldPosition.setBeginIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                fieldPosition.setBeginIndex(result.length());
            }
            result.append(this.symbols.getNaN());
            if (parseAttr) {
                addAttribute(Field.INTEGER, result.length() - this.symbols.getNaN().length(), result.length());
            }
            if (fieldPosition.getField() == 0) {
                fieldPosition.setEndIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                fieldPosition.setEndIndex(result.length());
            }
            addPadding(result, fieldPosition, 0, 0);
            return result;
        }
        number = multiply(number);
        boolean isNegative = isNegative(number);
        number = round(number);
        if (Double.isInfinite(number)) {
            int prefixLen = appendAffix(result, isNegative, true, fieldPosition, parseAttr);
            if (fieldPosition.getField() == 0) {
                fieldPosition.setBeginIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                fieldPosition.setBeginIndex(result.length());
            }
            result.append(this.symbols.getInfinity());
            if (parseAttr) {
                addAttribute(Field.INTEGER, result.length() - this.symbols.getInfinity().length(), result.length());
            }
            if (fieldPosition.getField() == 0) {
                fieldPosition.setEndIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                fieldPosition.setEndIndex(result.length());
            }
            addPadding(result, fieldPosition, prefixLen, appendAffix(result, isNegative, false, fieldPosition, parseAttr));
            return result;
        }
        StringBuffer subformat;
        int precision = precision(false);
        if (this.useExponentialNotation && precision > 0 && number != 0.0d && this.roundingMode != 6) {
            int log10RoundingIncr = (1 - precision) + ((int) Math.floor(Math.log10(Math.abs(number))));
            double roundingIncReciprocal = 0.0d;
            double roundingInc = 0.0d;
            if (log10RoundingIncr < 0) {
                roundingIncReciprocal = android.icu.math.BigDecimal.ONE.movePointRight(-log10RoundingIncr).doubleValue();
            } else {
                roundingInc = android.icu.math.BigDecimal.ONE.movePointRight(log10RoundingIncr).doubleValue();
            }
            number = round(number, roundingInc, roundingIncReciprocal, this.roundingMode, isNegative);
        }
        synchronized (this.digitList) {
            DigitList digitList = this.digitList;
            boolean z = !this.useExponentialNotation ? !areSignificantDigitsUsed() : false;
            digitList.set(number, precision, z);
            subformat = subformat(number, result, fieldPosition, isNegative, false, parseAttr);
        }
        return subformat;
    }

    @Deprecated
    double adjustNumberAsInFormatting(double number) {
        if (Double.isNaN(number)) {
            return number;
        }
        number = round(multiply(number));
        if (Double.isInfinite(number)) {
            return number;
        }
        return toDigitList(number).getDouble();
    }

    @Deprecated
    DigitList toDigitList(double number) {
        DigitList result = new DigitList();
        result.set(number, precision(false), false);
        return result;
    }

    @Deprecated
    boolean isNumberNegative(double number) {
        if (Double.isNaN(number)) {
            return false;
        }
        return isNegative(multiply(number));
    }

    private static double round(double number, double roundingInc, double roundingIncReciprocal, int mode, boolean isNegative) {
        double div = roundingIncReciprocal == 0.0d ? number / roundingInc : number * roundingIncReciprocal;
        switch (mode) {
            case 0:
                div = Math.ceil(div - epsilon);
                break;
            case 1:
                div = Math.floor(epsilon + div);
                break;
            case 2:
                if (!isNegative) {
                    div = Math.ceil(div - epsilon);
                    break;
                }
                div = Math.floor(epsilon + div);
                break;
            case 3:
                if (!isNegative) {
                    div = Math.floor(epsilon + div);
                    break;
                }
                div = Math.ceil(div - epsilon);
                break;
            case 7:
                if (div == Math.floor(div)) {
                    return number;
                }
                throw new ArithmeticException("Rounding necessary");
            default:
                double ceil = Math.ceil(div);
                double ceildiff = ceil - div;
                double floor = Math.floor(div);
                double floordiff = div - floor;
                switch (mode) {
                    case 4:
                        if (ceildiff > epsilon + floordiff) {
                            div = floor;
                            break;
                        }
                        div = ceil;
                        break;
                    case 5:
                        if (floordiff > epsilon + ceildiff) {
                            div = ceil;
                            break;
                        }
                        div = floor;
                        break;
                    case 6:
                        if (epsilon + floordiff >= ceildiff) {
                            if (epsilon + ceildiff >= floordiff) {
                                double testFloor = floor / 2.0d;
                                if (testFloor != Math.floor(testFloor)) {
                                    div = ceil;
                                    break;
                                }
                                div = floor;
                                break;
                            }
                            div = ceil;
                            break;
                        }
                        div = floor;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid rounding mode: " + mode);
                }
        }
        return roundingIncReciprocal == 0.0d ? div * roundingInc : div / roundingIncReciprocal;
    }

    public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition, boolean parseAttr) {
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);
        if (this.actualRoundingIncrementICU != null) {
            return format(android.icu.math.BigDecimal.valueOf(number), result, fieldPosition);
        }
        StringBuffer subformat;
        boolean isNegative = number < 0;
        if (isNegative) {
            number = -number;
        }
        if (this.multiplier != 1) {
            boolean tooBig = number < 0 ? number <= Long.MIN_VALUE / ((long) this.multiplier) : number > Long.MAX_VALUE / ((long) this.multiplier);
            if (tooBig) {
                if (isNegative) {
                    number = -number;
                }
                return format(BigInteger.valueOf(number), result, fieldPosition, parseAttr);
            }
        }
        number *= (long) this.multiplier;
        synchronized (this.digitList) {
            this.digitList.set(number, precision(true));
            if (this.digitList.wasRounded() && this.roundingMode == 7) {
                throw new ArithmeticException("Rounding necessary");
            }
            subformat = subformat((double) number, result, fieldPosition, isNegative, true, parseAttr);
        }
        return subformat;
    }

    public StringBuffer format(BigInteger number, StringBuffer result, FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private StringBuffer format(BigInteger number, StringBuffer result, FieldPosition fieldPosition, boolean parseAttr) {
        boolean z = true;
        if (this.actualRoundingIncrementICU != null) {
            return format(new android.icu.math.BigDecimal(number), result, fieldPosition);
        }
        StringBuffer subformat;
        if (this.multiplier != 1) {
            number = number.multiply(BigInteger.valueOf((long) this.multiplier));
        }
        synchronized (this.digitList) {
            this.digitList.set(number, precision(true));
            if (this.digitList.wasRounded() && this.roundingMode == 7) {
                throw new ArithmeticException("Rounding necessary");
            }
            int intValue = number.intValue();
            if (number.signum() >= 0) {
                z = false;
            }
            subformat = subformat(intValue, result, fieldPosition, z, true, parseAttr);
        }
        return subformat;
    }

    public StringBuffer format(BigDecimal number, StringBuffer result, FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private StringBuffer format(BigDecimal number, StringBuffer result, FieldPosition fieldPosition, boolean parseAttr) {
        StringBuffer subformat;
        boolean z = true;
        if (this.multiplier != 1) {
            number = number.multiply(BigDecimal.valueOf((long) this.multiplier));
        }
        if (this.actualRoundingIncrement != null) {
            number = number.divide(this.actualRoundingIncrement, 0, this.roundingMode).multiply(this.actualRoundingIncrement);
        }
        synchronized (this.digitList) {
            DigitList digitList = this.digitList;
            int precision = precision(false);
            boolean z2 = !this.useExponentialNotation ? !areSignificantDigitsUsed() : false;
            digitList.set(number, precision, z2);
            if (this.digitList.wasRounded() && this.roundingMode == 7) {
                throw new ArithmeticException("Rounding necessary");
            }
            double doubleValue = number.doubleValue();
            if (number.signum() >= 0) {
                z = false;
            }
            subformat = subformat(doubleValue, result, fieldPosition, z, false, parseAttr);
        }
        return subformat;
    }

    public StringBuffer format(android.icu.math.BigDecimal number, StringBuffer result, FieldPosition fieldPosition) {
        StringBuffer subformat;
        boolean z = true;
        if (this.multiplier != 1) {
            number = number.multiply(android.icu.math.BigDecimal.valueOf((long) this.multiplier), this.mathContext);
        }
        if (this.actualRoundingIncrementICU != null) {
            number = number.divide(this.actualRoundingIncrementICU, 0, this.roundingMode).multiply(this.actualRoundingIncrementICU, this.mathContext);
        }
        synchronized (this.digitList) {
            DigitList digitList = this.digitList;
            int precision = precision(false);
            boolean z2 = !this.useExponentialNotation ? !areSignificantDigitsUsed() : false;
            digitList.set(number, precision, z2);
            if (this.digitList.wasRounded() && this.roundingMode == 7) {
                throw new ArithmeticException("Rounding necessary");
            }
            double doubleValue = number.doubleValue();
            if (number.signum() >= 0) {
                z = false;
            }
            subformat = subformat(doubleValue, result, fieldPosition, z, false, false);
        }
        return subformat;
    }

    private boolean isGroupingPosition(int pos) {
        if (!isGroupingUsed() || pos <= 0 || this.groupingSize <= (byte) 0) {
            return false;
        }
        return (this.groupingSize2 <= (byte) 0 || pos <= this.groupingSize) ? pos % this.groupingSize == 0 : (pos - this.groupingSize) % this.groupingSize2 == 0;
    }

    private int precision(boolean isIntegral) {
        if (areSignificantDigitsUsed()) {
            return getMaximumSignificantDigits();
        }
        if (this.useExponentialNotation) {
            return getMinimumIntegerDigits() + getMaximumFractionDigits();
        }
        return isIntegral ? 0 : getMaximumFractionDigits();
    }

    private StringBuffer subformat(int number, StringBuffer result, FieldPosition fieldPosition, boolean isNegative, boolean isInteger, boolean parseAttr) {
        if (this.currencySignCount != 3) {
            return subformat(result, fieldPosition, isNegative, isInteger, parseAttr);
        }
        return subformat(this.currencyPluralInfo.select(getFixedDecimal((double) number)), result, fieldPosition, isNegative, isInteger, parseAttr);
    }

    FixedDecimal getFixedDecimal(double number) {
        return getFixedDecimal(number, this.digitList);
    }

    FixedDecimal getFixedDecimal(double number, DigitList dl) {
        int maxFractionalDigits;
        int minFractionalDigits;
        int fractionalDigitsInDigitList = dl.count - dl.decimalAt;
        if (this.useSignificantDigits) {
            maxFractionalDigits = this.maxSignificantDigits - dl.decimalAt;
            minFractionalDigits = this.minSignificantDigits - dl.decimalAt;
            if (minFractionalDigits < 0) {
                minFractionalDigits = 0;
            }
            if (maxFractionalDigits < 0) {
                maxFractionalDigits = 0;
            }
        } else {
            maxFractionalDigits = getMaximumFractionDigits();
            minFractionalDigits = getMinimumFractionDigits();
        }
        int v = fractionalDigitsInDigitList;
        if (fractionalDigitsInDigitList < minFractionalDigits) {
            v = minFractionalDigits;
        } else if (fractionalDigitsInDigitList > maxFractionalDigits) {
            v = maxFractionalDigits;
        }
        long f = 0;
        if (v > 0) {
            int i;
            for (i = Math.max(0, dl.decimalAt); i < dl.count; i++) {
                f = (f * 10) + ((long) (dl.digits[i] - 48));
            }
            for (i = v; i < fractionalDigitsInDigitList; i++) {
                f *= 10;
            }
        }
        return new FixedDecimal(number, v, f);
    }

    private StringBuffer subformat(double number, StringBuffer result, FieldPosition fieldPosition, boolean isNegative, boolean isInteger, boolean parseAttr) {
        if (this.currencySignCount != 3) {
            return subformat(result, fieldPosition, isNegative, isInteger, parseAttr);
        }
        return subformat(this.currencyPluralInfo.select(getFixedDecimal(number)), result, fieldPosition, isNegative, isInteger, parseAttr);
    }

    private StringBuffer subformat(String pluralCount, StringBuffer result, FieldPosition fieldPosition, boolean isNegative, boolean isInteger, boolean parseAttr) {
        if (this.style == 6) {
            String currencyPluralPattern = this.currencyPluralInfo.getCurrencyPluralPattern(pluralCount);
            if (!this.formatPattern.equals(currencyPluralPattern)) {
                applyPatternWithoutExpandAffix(currencyPluralPattern, false);
            }
        }
        expandAffixAdjustWidth(pluralCount);
        return subformat(result, fieldPosition, isNegative, isInteger, parseAttr);
    }

    private StringBuffer subformat(StringBuffer result, FieldPosition fieldPosition, boolean isNegative, boolean isInteger, boolean parseAttr) {
        if (this.digitList.isZero()) {
            this.digitList.decimalAt = 0;
        }
        int prefixLen = appendAffix(result, isNegative, true, fieldPosition, parseAttr);
        if (this.useExponentialNotation) {
            subformatExponential(result, fieldPosition, parseAttr);
        } else {
            subformatFixed(result, fieldPosition, isInteger, parseAttr);
        }
        addPadding(result, fieldPosition, prefixLen, appendAffix(result, isNegative, false, fieldPosition, parseAttr));
        return result;
    }

    private void subformatFixed(StringBuffer result, FieldPosition fieldPosition, boolean isInteger, boolean parseAttr) {
        char[] digits = this.symbols.getDigitsLocal();
        char grouping = this.currencySignCount == 0 ? this.symbols.getGroupingSeparator() : this.symbols.getMonetaryGroupingSeparator();
        char decimal = this.currencySignCount == 0 ? this.symbols.getDecimalSeparator() : this.symbols.getMonetaryDecimalSeparator();
        boolean useSigDig = areSignificantDigitsUsed();
        int maxIntDig = getMaximumIntegerDigits();
        int minIntDig = getMinimumIntegerDigits();
        int intBegin = result.length();
        if (fieldPosition.getField() == 0) {
            fieldPosition.setBeginIndex(result.length());
        } else {
            if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                fieldPosition.setBeginIndex(result.length());
            }
        }
        long fractionalDigits = 0;
        int fractionalDigitsCount = 0;
        int sigCount = 0;
        int minSigDig = getMinimumSignificantDigits();
        int maxSigDig = getMaximumSignificantDigits();
        if (!useSigDig) {
            minSigDig = 0;
            maxSigDig = Integer.MAX_VALUE;
        }
        int count = useSigDig ? Math.max(1, this.digitList.decimalAt) : minIntDig;
        if (this.digitList.decimalAt > 0 && count < this.digitList.decimalAt) {
            count = this.digitList.decimalAt;
        }
        int digitIndex = 0;
        if (count > maxIntDig && maxIntDig >= 0) {
            count = maxIntDig;
            digitIndex = this.digitList.decimalAt - maxIntDig;
        }
        int sizeBeforeIntegerPart = result.length();
        int posSinceLastGrouping = result.length();
        int i = count - 1;
        int digitIndex2 = digitIndex;
        while (i >= 0) {
            if (i >= this.digitList.decimalAt || digitIndex2 >= this.digitList.count || sigCount >= maxSigDig) {
                result.append(digits[0]);
                if (sigCount > 0) {
                    sigCount++;
                    digitIndex = digitIndex2;
                } else {
                    digitIndex = digitIndex2;
                }
            } else {
                digitIndex = digitIndex2 + 1;
                result.append(digits[this.digitList.getDigitValue(digitIndex2)]);
                sigCount++;
            }
            if (isGroupingPosition(i)) {
                if (parseAttr) {
                    addAttribute(Field.INTEGER, posSinceLastGrouping, result.length());
                }
                result.append(grouping);
                if (parseAttr) {
                    addAttribute(Field.GROUPING_SEPARATOR, result.length() - 1, result.length());
                }
                if (fieldPosition.getFieldAttribute() == Field.GROUPING_SEPARATOR && fieldPosition.getEndIndex() == 0) {
                    fieldPosition.setBeginIndex(result.length() - 1);
                    fieldPosition.setEndIndex(result.length());
                }
                posSinceLastGrouping = result.length();
            }
            i--;
            digitIndex2 = digitIndex;
        }
        if (fieldPosition.getField() == 0) {
            fieldPosition.setEndIndex(result.length());
        } else {
            if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                fieldPosition.setEndIndex(result.length());
            }
        }
        if (parseAttr) {
            addAttribute(Field.INTEGER, posSinceLastGrouping, result.length());
        }
        if (sigCount == 0 && this.digitList.count == 0) {
            sigCount = 1;
        }
        boolean fractionPresent = (isInteger || digitIndex2 >= this.digitList.count) ? useSigDig ? sigCount >= minSigDig : getMinimumFractionDigits() <= 0 : true;
        if (!fractionPresent && result.length() == sizeBeforeIntegerPart) {
            result.append(digits[0]);
        }
        if (parseAttr) {
            addAttribute(Field.INTEGER, intBegin, result.length());
        }
        if (this.decimalSeparatorAlwaysShown || fractionPresent) {
            if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                fieldPosition.setBeginIndex(result.length());
            }
            result.append(decimal);
            if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                fieldPosition.setEndIndex(result.length());
            }
            if (parseAttr) {
                addAttribute(Field.DECIMAL_SEPARATOR, result.length() - 1, result.length());
            }
        }
        if (fieldPosition.getField() == 1) {
            fieldPosition.setBeginIndex(result.length());
        } else {
            if (fieldPosition.getFieldAttribute() == Field.FRACTION) {
                fieldPosition.setBeginIndex(result.length());
            }
        }
        int fracBegin = result.length();
        boolean recordFractionDigits = fieldPosition instanceof UFieldPosition;
        count = useSigDig ? Integer.MAX_VALUE : getMaximumFractionDigits();
        if (useSigDig && (sigCount == maxSigDig || (sigCount >= minSigDig && digitIndex2 == this.digitList.count))) {
            count = 0;
        }
        i = 0;
        while (i < count) {
            if (!useSigDig && i >= getMinimumFractionDigits() && (isInteger || digitIndex2 >= this.digitList.count)) {
                digitIndex = digitIndex2;
                break;
            }
            if (-1 - i <= this.digitList.decimalAt - 1) {
                if (isInteger || digitIndex2 >= this.digitList.count) {
                    result.append(digits[0]);
                    if (recordFractionDigits) {
                        fractionalDigitsCount++;
                        fractionalDigits *= 10;
                        digitIndex = digitIndex2;
                    } else {
                        digitIndex = digitIndex2;
                    }
                } else {
                    digitIndex = digitIndex2 + 1;
                    byte digit = this.digitList.getDigitValue(digitIndex2);
                    result.append(digits[digit]);
                    if (recordFractionDigits) {
                        fractionalDigitsCount++;
                        fractionalDigits = (fractionalDigits * 10) + ((long) digit);
                    }
                }
                sigCount++;
                if (useSigDig) {
                    if (sigCount != maxSigDig) {
                        if (digitIndex == this.digitList.count && sigCount >= minSigDig) {
                            break;
                        }
                    }
                    break;
                }
                continue;
            } else {
                result.append(digits[0]);
                if (recordFractionDigits) {
                    fractionalDigitsCount++;
                    fractionalDigits *= 10;
                }
                digitIndex = digitIndex2;
            }
            i++;
            digitIndex2 = digitIndex;
        }
        if (fieldPosition.getField() == 1) {
            fieldPosition.setEndIndex(result.length());
        } else {
            if (fieldPosition.getFieldAttribute() == Field.FRACTION) {
                fieldPosition.setEndIndex(result.length());
            }
        }
        if (recordFractionDigits) {
            ((UFieldPosition) fieldPosition).setFractionDigits(fractionalDigitsCount, fractionalDigits);
        }
        if (!parseAttr) {
            return;
        }
        if (this.decimalSeparatorAlwaysShown || fractionPresent) {
            addAttribute(Field.FRACTION, fracBegin, result.length());
        }
    }

    private void subformatExponential(StringBuffer result, FieldPosition fieldPosition, boolean parseAttr) {
        int minFracDig;
        char[] digits = this.symbols.getDigitsLocal();
        char decimal = this.currencySignCount == 0 ? this.symbols.getDecimalSeparator() : this.symbols.getMonetaryDecimalSeparator();
        boolean useSigDig = areSignificantDigitsUsed();
        int maxIntDig = getMaximumIntegerDigits();
        int minIntDig = getMinimumIntegerDigits();
        if (fieldPosition.getField() == 0) {
            fieldPosition.setBeginIndex(result.length());
            fieldPosition.setEndIndex(-1);
        } else if (fieldPosition.getField() == 1) {
            fieldPosition.setBeginIndex(-1);
        } else {
            if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                fieldPosition.setBeginIndex(result.length());
                fieldPosition.setEndIndex(-1);
            } else {
                if (fieldPosition.getFieldAttribute() == Field.FRACTION) {
                    fieldPosition.setBeginIndex(-1);
                }
            }
        }
        int intBegin = result.length();
        int intEnd = -1;
        int fracBegin = -1;
        if (useSigDig) {
            minIntDig = 1;
            maxIntDig = 1;
            minFracDig = getMinimumSignificantDigits() - 1;
        } else {
            minFracDig = getMinimumFractionDigits();
            if (maxIntDig > 8) {
                maxIntDig = 1;
                if (1 < minIntDig) {
                    maxIntDig = minIntDig;
                }
            }
            if (maxIntDig > minIntDig) {
                minIntDig = 1;
            }
        }
        long fractionalDigits = 0;
        int fractionalDigitsCount = 0;
        boolean recordFractionDigits = false;
        int exponent = this.digitList.decimalAt;
        if (maxIntDig <= 1 || maxIntDig == minIntDig) {
            int i = (minIntDig > 0 || minFracDig > 0) ? minIntDig : 1;
            exponent -= i;
        } else {
            exponent = (exponent > 0 ? (exponent - 1) / maxIntDig : (exponent / maxIntDig) - 1) * maxIntDig;
        }
        int minimumDigits = minIntDig + minFracDig;
        int integerDigits = this.digitList.isZero() ? minIntDig : this.digitList.decimalAt - exponent;
        int totalDigits = this.digitList.count;
        if (minimumDigits > totalDigits) {
            totalDigits = minimumDigits;
        }
        if (integerDigits > totalDigits) {
            totalDigits = integerDigits;
        }
        int i2 = 0;
        while (i2 < totalDigits) {
            if (i2 == integerDigits) {
                if (fieldPosition.getField() == 0) {
                    fieldPosition.setEndIndex(result.length());
                } else {
                    if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                        fieldPosition.setEndIndex(result.length());
                    }
                }
                if (parseAttr) {
                    intEnd = result.length();
                    addAttribute(Field.INTEGER, intBegin, result.length());
                }
                if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                    fieldPosition.setBeginIndex(result.length());
                }
                result.append(decimal);
                if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                    fieldPosition.setEndIndex(result.length());
                }
                if (parseAttr) {
                    addAttribute(Field.DECIMAL_SEPARATOR, result.length() - 1, result.length());
                    fracBegin = result.length();
                }
                if (fieldPosition.getField() == 1) {
                    fieldPosition.setBeginIndex(result.length());
                } else {
                    if (fieldPosition.getFieldAttribute() == Field.FRACTION) {
                        fieldPosition.setBeginIndex(result.length());
                    }
                }
                recordFractionDigits = fieldPosition instanceof UFieldPosition;
            }
            byte digit = i2 < this.digitList.count ? this.digitList.getDigitValue(i2) : (byte) 0;
            result.append(digits[digit]);
            if (recordFractionDigits) {
                fractionalDigitsCount++;
                fractionalDigits = (fractionalDigits * 10) + ((long) digit);
            }
            i2++;
        }
        if (this.digitList.isZero() && totalDigits == 0) {
            result.append(digits[0]);
        }
        if (fieldPosition.getField() == 0) {
            if (fieldPosition.getEndIndex() < 0) {
                fieldPosition.setEndIndex(result.length());
            }
        } else if (fieldPosition.getField() == 1) {
            if (fieldPosition.getBeginIndex() < 0) {
                fieldPosition.setBeginIndex(result.length());
            }
            fieldPosition.setEndIndex(result.length());
        } else {
            if (fieldPosition.getFieldAttribute() != Field.INTEGER) {
                if (fieldPosition.getFieldAttribute() == Field.FRACTION) {
                    if (fieldPosition.getBeginIndex() < 0) {
                        fieldPosition.setBeginIndex(result.length());
                    }
                    fieldPosition.setEndIndex(result.length());
                }
            } else if (fieldPosition.getEndIndex() < 0) {
                fieldPosition.setEndIndex(result.length());
            }
        }
        if (recordFractionDigits) {
            ((UFieldPosition) fieldPosition).setFractionDigits(fractionalDigitsCount, fractionalDigits);
        }
        if (parseAttr) {
            if (intEnd < 0) {
                addAttribute(Field.INTEGER, intBegin, result.length());
            }
            if (fracBegin > 0) {
                addAttribute(Field.FRACTION, fracBegin, result.length());
            }
        }
        if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SYMBOL) {
            fieldPosition.setBeginIndex(result.length());
        }
        result.append(this.symbols.getExponentSeparator());
        if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SYMBOL) {
            fieldPosition.setEndIndex(result.length());
        }
        if (parseAttr) {
            addAttribute(Field.EXPONENT_SYMBOL, result.length() - this.symbols.getExponentSeparator().length(), result.length());
        }
        if (this.digitList.isZero()) {
            exponent = 0;
        }
        boolean negativeExponent = exponent < 0;
        if ((negativeExponent || this.exponentSignAlwaysShown) && fieldPosition.getFieldAttribute() == Field.EXPONENT_SIGN) {
            fieldPosition.setBeginIndex(result.length());
        }
        if (negativeExponent) {
            exponent = -exponent;
            result.append(this.symbols.getMinusString());
            if (parseAttr) {
                addAttribute(Field.EXPONENT_SIGN, result.length() - 1, result.length());
            }
        } else if (this.exponentSignAlwaysShown) {
            result.append(this.symbols.getPlusString());
            if (parseAttr) {
                addAttribute(Field.EXPONENT_SIGN, result.length() - 1, result.length());
            }
        }
        if ((negativeExponent || this.exponentSignAlwaysShown) && fieldPosition.getFieldAttribute() == Field.EXPONENT_SIGN) {
            fieldPosition.setEndIndex(result.length());
        }
        int expBegin = result.length();
        this.digitList.set((long) exponent);
        int expDig = this.minExponentDigits;
        if (this.useExponentialNotation && expDig < 1) {
            expDig = 1;
        }
        for (i2 = this.digitList.decimalAt; i2 < expDig; i2++) {
            result.append(digits[0]);
        }
        for (i2 = 0; i2 < this.digitList.decimalAt; i2++) {
            char c;
            if (i2 < this.digitList.count) {
                c = digits[this.digitList.getDigitValue(i2)];
            } else {
                c = digits[0];
            }
            result.append(c);
        }
        if (fieldPosition.getFieldAttribute() == Field.EXPONENT) {
            fieldPosition.setBeginIndex(expBegin);
            fieldPosition.setEndIndex(result.length());
        }
        if (parseAttr) {
            addAttribute(Field.EXPONENT, expBegin, result.length());
        }
    }

    private final void addPadding(StringBuffer result, FieldPosition fieldPosition, int prefixLen, int suffixLen) {
        if (this.formatWidth > 0) {
            int len = this.formatWidth - result.length();
            if (len > 0) {
                char[] padding = new char[len];
                for (int i = 0; i < len; i++) {
                    padding[i] = this.pad;
                }
                switch (this.padPosition) {
                    case 0:
                        result.insert(0, padding);
                        break;
                    case 1:
                        result.insert(prefixLen, padding);
                        break;
                    case 2:
                        result.insert(result.length() - suffixLen, padding);
                        break;
                    case 3:
                        result.append(padding);
                        break;
                }
                if (this.padPosition == 0 || this.padPosition == 1) {
                    fieldPosition.setBeginIndex(fieldPosition.getBeginIndex() + len);
                    fieldPosition.setEndIndex(fieldPosition.getEndIndex() + len);
                }
            }
        }
    }

    public Number parse(String text, ParsePosition parsePosition) {
        return (Number) parse(text, parsePosition, null);
    }

    public CurrencyAmount parseCurrency(CharSequence text, ParsePosition pos) {
        return (CurrencyAmount) parse(text.toString(), pos, new Currency[1]);
    }

    private Object parse(String text, ParsePosition parsePosition, Currency[] currency) {
        int backup = parsePosition.getIndex();
        int i = backup;
        if (this.formatWidth > 0 && (this.padPosition == 0 || this.padPosition == 1)) {
            i = skipPadding(text, backup);
        }
        if (text.regionMatches(i, this.symbols.getNaN(), 0, this.symbols.getNaN().length())) {
            i += this.symbols.getNaN().length();
            if (this.formatWidth > 0 && (this.padPosition == 2 || this.padPosition == 3)) {
                i = skipPadding(text, i);
            }
            parsePosition.setIndex(i);
            return new Double(Double.NaN);
        }
        Number n;
        i = backup;
        boolean[] status = new boolean[3];
        if (this.currencySignCount == 0) {
            if (!subparse(text, parsePosition, this.digitList, status, currency, this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, false, 0)) {
                parsePosition.setIndex(backup);
                return null;
            }
        } else if (!parseForCurrency(text, parsePosition, currency, status)) {
            return null;
        }
        Number d;
        if (status[0]) {
            double d2;
            if (status[1]) {
                d2 = Double.POSITIVE_INFINITY;
            } else {
                d2 = Double.NEGATIVE_INFINITY;
            }
            d = new Double(d2);
        } else if (status[2]) {
            if (status[1]) {
                d = new Double("0.0");
            } else {
                d = new Double("-0.0");
            }
        } else if (status[1] || !this.digitList.isZero()) {
            int mult = this.multiplier;
            while (mult % 10 == 0) {
                DigitList digitList = this.digitList;
                digitList.decimalAt--;
                mult /= 10;
            }
            if (this.parseBigDecimal || mult != 1 || !this.digitList.isIntegral()) {
                Number big = this.digitList.getBigDecimalICU(status[1]);
                n = big;
                if (mult != 1) {
                    n = big.divide(android.icu.math.BigDecimal.valueOf((long) mult), this.mathContext);
                }
            } else if (this.digitList.decimalAt < 12) {
                long l = 0;
                if (this.digitList.count > 0) {
                    int nx = 0;
                    while (nx < this.digitList.count) {
                        l = ((10 * l) + ((long) ((char) this.digitList.digits[nx]))) - 48;
                        nx++;
                    }
                    while (true) {
                        int nx2 = nx + 1;
                        if (nx >= this.digitList.decimalAt) {
                            break;
                        }
                        l *= 10;
                        nx = nx2;
                    }
                    if (!status[1]) {
                        l = -l;
                    }
                }
                n = Long.valueOf(l);
            } else {
                Number big2 = this.digitList.getBigInteger(status[1]);
                n = big2.bitLength() < 64 ? Long.valueOf(big2.longValue()) : big2;
            }
        } else {
            d = new Double("-0.0");
        }
        if (currency != null) {
            n = new CurrencyAmount(n, currency[0]);
        }
        return n;
    }

    private boolean parseForCurrency(String text, ParsePosition parsePosition, Currency[] currency, boolean[] status) {
        boolean found;
        int origPos = parsePosition.getIndex();
        if (!this.isReadyForParsing) {
            int savedCurrencySignCount = this.currencySignCount;
            setupCurrencyAffixForAllPatterns();
            if (savedCurrencySignCount == 3) {
                applyPatternWithoutExpandAffix(this.formatPattern, false);
            } else {
                applyPattern(this.formatPattern, false);
            }
            this.isReadyForParsing = true;
        }
        int maxPosIndex = origPos;
        int maxErrorPos = -1;
        boolean[] savedStatus = null;
        boolean[] tmpStatus = new boolean[3];
        ParsePosition tmpPos = new ParsePosition(origPos);
        DigitList tmpDigitList = new DigitList();
        if (this.style == 6) {
            found = subparse(text, tmpPos, tmpDigitList, tmpStatus, currency, this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, true, 1);
        } else {
            found = subparse(text, tmpPos, tmpDigitList, tmpStatus, currency, this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, true, 0);
        }
        if (!found) {
            maxErrorPos = tmpPos.getErrorIndex();
        } else if (tmpPos.getIndex() > origPos) {
            maxPosIndex = tmpPos.getIndex();
            savedStatus = tmpStatus;
            this.digitList = tmpDigitList;
        }
        for (AffixForCurrency affix : this.affixPatternsForCurrency) {
            tmpStatus = new boolean[3];
            tmpPos = new ParsePosition(origPos);
            tmpDigitList = new DigitList();
            if (subparse(text, tmpPos, tmpDigitList, tmpStatus, currency, affix.getNegPrefix(), affix.getNegSuffix(), affix.getPosPrefix(), affix.getPosSuffix(), true, affix.getPatternType())) {
                found = true;
                if (tmpPos.getIndex() > maxPosIndex) {
                    maxPosIndex = tmpPos.getIndex();
                    savedStatus = tmpStatus;
                    this.digitList = tmpDigitList;
                }
            } else if (tmpPos.getErrorIndex() > maxErrorPos) {
                maxErrorPos = tmpPos.getErrorIndex();
            }
        }
        tmpStatus = new boolean[3];
        tmpPos = new ParsePosition(origPos);
        tmpDigitList = new DigitList();
        if (subparse(text, tmpPos, tmpDigitList, tmpStatus, currency, this.negativePrefix, this.negativeSuffix, this.positivePrefix, this.positiveSuffix, false, 0)) {
            if (tmpPos.getIndex() > maxPosIndex) {
                maxPosIndex = tmpPos.getIndex();
                savedStatus = tmpStatus;
                this.digitList = tmpDigitList;
            }
            found = true;
        } else if (tmpPos.getErrorIndex() > maxErrorPos) {
            maxErrorPos = tmpPos.getErrorIndex();
        }
        if (found) {
            parsePosition.setIndex(maxPosIndex);
            parsePosition.setErrorIndex(-1);
            for (int index = 0; index < 3; index++) {
                status[index] = savedStatus[index];
            }
        } else {
            parsePosition.setErrorIndex(maxErrorPos);
        }
        return found;
    }

    private void setupCurrencyAffixForAllPatterns() {
        if (this.currencyPluralInfo == null) {
            this.currencyPluralInfo = new CurrencyPluralInfo(this.symbols.getULocale());
        }
        this.affixPatternsForCurrency = new HashSet();
        String savedFormatPattern = this.formatPattern;
        applyPatternWithoutExpandAffix(NumberFormat.getPattern(this.symbols.getULocale(), 1), false);
        this.affixPatternsForCurrency.add(new AffixForCurrency(this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, 0));
        Iterator<String> iter = this.currencyPluralInfo.pluralPatternIterator();
        Set<String> currencyUnitPatternSet = new HashSet();
        while (iter.hasNext()) {
            String currencyPattern = this.currencyPluralInfo.getCurrencyPluralPattern((String) iter.next());
            if (!(currencyPattern == null || currencyUnitPatternSet.contains(currencyPattern))) {
                currencyUnitPatternSet.add(currencyPattern);
                applyPatternWithoutExpandAffix(currencyPattern, false);
                this.affixPatternsForCurrency.add(new AffixForCurrency(this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, 1));
            }
        }
        this.formatPattern = savedFormatPattern;
    }

    private final boolean subparse(String text, ParsePosition parsePosition, DigitList digits, boolean[] status, Currency[] currency, String negPrefix, String negSuffix, String posPrefix, String posSuffix, boolean parseComplexCurrency, int type) {
        int position = parsePosition.getIndex();
        int oldStart = parsePosition.getIndex();
        if (this.formatWidth > 0 && this.padPosition == 0) {
            position = skipPadding(text, position);
        }
        int posMatch = compareAffix(text, position, false, true, posPrefix, parseComplexCurrency, type, currency);
        int negMatch = compareAffix(text, position, true, true, negPrefix, parseComplexCurrency, type, currency);
        if (posMatch >= 0 && negMatch >= 0) {
            if (posMatch > negMatch) {
                negMatch = -1;
            } else if (negMatch > posMatch) {
                posMatch = -1;
            }
        }
        if (posMatch >= 0) {
            position += posMatch;
        } else if (negMatch >= 0) {
            position += negMatch;
        } else {
            parsePosition.setErrorIndex(position);
            return false;
        }
        if (this.formatWidth > 0 && this.padPosition == 1) {
            position = skipPadding(text, position);
        }
        status[0] = false;
        if (text.regionMatches(position, this.symbols.getInfinity(), 0, this.symbols.getInfinity().length())) {
            position += this.symbols.getInfinity().length();
            status[0] = true;
        } else {
            digits.count = 0;
            digits.decimalAt = 0;
            char[] digitSymbols = this.symbols.getDigitsLocal();
            char decimal = this.currencySignCount == 0 ? this.symbols.getDecimalSeparator() : this.symbols.getMonetaryDecimalSeparator();
            char grouping = this.currencySignCount == 0 ? this.symbols.getGroupingSeparator() : this.symbols.getMonetaryGroupingSeparator();
            String exponentSep = this.symbols.getExponentSeparator();
            boolean sawDecimal = false;
            boolean sawGrouping = false;
            boolean sawDigit = false;
            long exponent = 0;
            boolean strictParse = isParseStrict();
            boolean strictFail = false;
            int lastGroup = -1;
            int digitStart = position;
            if (this.groupingSize2 == (byte) 0) {
                int gs2 = this.groupingSize;
            } else {
                byte gs22 = this.groupingSize2;
            }
            UnicodeSet decimalEquiv;
            if (skipExtendedSeparatorParsing) {
                decimalEquiv = UnicodeSet.EMPTY;
            } else {
                decimalEquiv = getEquivalentDecimals(decimal, strictParse);
            }
            UnicodeSet groupEquiv = skipExtendedSeparatorParsing ? UnicodeSet.EMPTY : strictParse ? strictDefaultGroupingSeparators : defaultGroupingSeparators;
            int digitCount = 0;
            int backup = -1;
            while (position < text.length()) {
                char ch = UTF16.charAt(text, position);
                int digit = ch - digitSymbols[0];
                if (digit < 0 || digit > 9) {
                    digit = UCharacter.digit(ch, 10);
                }
                if (digit < 0 || digit > 9) {
                    digit = 0;
                    while (digit < 10 && ch != digitSymbols[digit]) {
                        digit++;
                    }
                }
                if (digit == 0) {
                    if (strictParse && backup != -1) {
                        if ((lastGroup != -1 && countCodePoints(text, lastGroup, backup) - 1 != gs22) || (lastGroup == -1 && countCodePoints(text, digitStart, position) - 1 > gs22)) {
                            strictFail = true;
                            break;
                        }
                        lastGroup = backup;
                    }
                    backup = -1;
                    sawDigit = true;
                    if (digits.count != 0) {
                        digitCount++;
                        digits.append((char) (digit + 48));
                    } else if (sawDecimal) {
                        digits.decimalAt--;
                    }
                } else if (digit <= 0 || digit > 9) {
                    if (ch != decimal) {
                        if (!isGroupingUsed() || ch != grouping) {
                            if (!sawDecimal && decimalEquiv.contains((int) ch)) {
                                if (!strictParse || (backup == -1 && (lastGroup == -1 || countCodePoints(text, lastGroup, position) == this.groupingSize + 1))) {
                                    if (isParseIntegerOnly()) {
                                        break;
                                    }
                                    digits.decimalAt = digitCount;
                                    decimal = (char) ch;
                                    sawDecimal = true;
                                } else {
                                    strictFail = true;
                                    break;
                                }
                            } else if (isGroupingUsed() && !sawGrouping && groupEquiv.contains((int) ch)) {
                                if (!sawDecimal) {
                                    if (strictParse && (!sawDigit || backup != -1)) {
                                        strictFail = true;
                                        break;
                                    }
                                    grouping = (char) ch;
                                    backup = position;
                                    sawGrouping = true;
                                } else {
                                    break;
                                }
                            } else if (!false) {
                                if (text.regionMatches(true, position, exponentSep, 0, exponentSep.length())) {
                                    boolean negExp = false;
                                    int pos = position + exponentSep.length();
                                    if (pos < text.length()) {
                                        ch = UTF16.charAt(text, pos);
                                        if (ch == this.symbols.getPlusSign()) {
                                            pos++;
                                        } else if (ch == this.symbols.getMinusSign()) {
                                            pos++;
                                            negExp = true;
                                        }
                                    }
                                    DigitList exponentDigits = new DigitList();
                                    exponentDigits.count = 0;
                                    while (pos < text.length()) {
                                        digit = UTF16.charAt(text, pos) - digitSymbols[0];
                                        if (digit < 0 || digit > 9) {
                                            digit = UCharacter.digit(UTF16.charAt(text, pos), 10);
                                        }
                                        if (digit < 0 || digit > 9) {
                                            break;
                                        }
                                        exponentDigits.append((char) (digit + 48));
                                        pos += UTF16.getCharCount(UTF16.charAt(text, pos));
                                    }
                                    if (exponentDigits.count > 0) {
                                        if (!strictParse || (backup == -1 && lastGroup == -1)) {
                                            if (exponentDigits.count <= 10) {
                                                exponentDigits.decimalAt = exponentDigits.count;
                                                exponent = exponentDigits.getLong();
                                                if (negExp) {
                                                    exponent = -exponent;
                                                }
                                            } else if (negExp) {
                                                status[2] = true;
                                            } else {
                                                status[0] = true;
                                            }
                                            position = pos;
                                        } else {
                                            strictFail = true;
                                        }
                                    }
                                }
                            }
                        } else if (!sawDecimal) {
                            if (strictParse && (!sawDigit || backup != -1)) {
                                strictFail = true;
                                break;
                            }
                            backup = position;
                            sawGrouping = true;
                        } else {
                            break;
                        }
                    } else if (!strictParse || (backup == -1 && (lastGroup == -1 || countCodePoints(text, lastGroup, position) == this.groupingSize + 1))) {
                        if (isParseIntegerOnly() || sawDecimal) {
                            break;
                        }
                        digits.decimalAt = digitCount;
                        sawDecimal = true;
                    } else {
                        strictFail = true;
                        break;
                    }
                } else {
                    if (strictParse && backup != -1) {
                        if ((lastGroup != -1 && countCodePoints(text, lastGroup, backup) - 1 != gs22) || (lastGroup == -1 && countCodePoints(text, digitStart, position) - 1 > gs22)) {
                            strictFail = true;
                            break;
                        }
                        lastGroup = backup;
                    }
                    sawDigit = true;
                    digitCount++;
                    digits.append((char) (digit + 48));
                    backup = -1;
                }
                position += UTF16.getCharCount(ch);
            }
            if (digits.decimalAt == 0 && isDecimalPatternMatchRequired() && this.formatPattern.indexOf(decimal) != -1) {
                parsePosition.setIndex(oldStart);
                parsePosition.setErrorIndex(position);
                return false;
            }
            if (backup != -1) {
                position = backup;
            }
            if (!sawDecimal) {
                digits.decimalAt = digitCount;
            }
            if (!(!strictParse || sawDecimal || lastGroup == -1 || countCodePoints(text, lastGroup, position) == this.groupingSize + 1)) {
                strictFail = true;
            }
            if (strictFail) {
                parsePosition.setIndex(oldStart);
                parsePosition.setErrorIndex(position);
                return false;
            }
            exponent += (long) digits.decimalAt;
            if (exponent < ((long) (-getParseMaxDigits()))) {
                status[2] = true;
            } else if (exponent > ((long) getParseMaxDigits())) {
                status[0] = true;
            } else {
                digits.decimalAt = (int) exponent;
            }
            if (!sawDigit && digitCount == 0) {
                parsePosition.setIndex(oldStart);
                parsePosition.setErrorIndex(oldStart);
                return false;
            }
        }
        if (this.formatWidth > 0 && this.padPosition == 2) {
            position = skipPadding(text, position);
        }
        if (posMatch >= 0) {
            posMatch = compareAffix(text, position, false, false, posSuffix, parseComplexCurrency, type, currency);
        }
        if (negMatch >= 0) {
            negMatch = compareAffix(text, position, true, false, negSuffix, parseComplexCurrency, type, currency);
        }
        if (posMatch >= 0 && negMatch >= 0) {
            if (posMatch > negMatch) {
                negMatch = -1;
            } else if (negMatch > posMatch) {
                posMatch = -1;
            }
        }
        if ((posMatch >= 0 ? 1 : null) == (negMatch >= 0 ? 1 : null)) {
            parsePosition.setErrorIndex(position);
            return false;
        }
        if (posMatch >= 0) {
            negMatch = posMatch;
        }
        position += negMatch;
        if (this.formatWidth > 0 && this.padPosition == 3) {
            position = skipPadding(text, position);
        }
        parsePosition.setIndex(position);
        status[1] = posMatch >= 0;
        if (parsePosition.getIndex() != oldStart) {
            return true;
        }
        parsePosition.setErrorIndex(position);
        return false;
    }

    private int countCodePoints(String str, int start, int end) {
        int count = 0;
        int index = start;
        while (index < end) {
            count++;
            index += UTF16.getCharCount(UTF16.charAt(str, index));
        }
        return count;
    }

    private UnicodeSet getEquivalentDecimals(char decimal, boolean strictParse) {
        UnicodeSet equivSet = UnicodeSet.EMPTY;
        if (strictParse) {
            if (strictDotEquivalents.contains((int) decimal)) {
                return strictDotEquivalents;
            }
            if (strictCommaEquivalents.contains((int) decimal)) {
                return strictCommaEquivalents;
            }
            return equivSet;
        } else if (dotEquivalents.contains((int) decimal)) {
            return dotEquivalents;
        } else {
            if (commaEquivalents.contains((int) decimal)) {
                return commaEquivalents;
            }
            return equivSet;
        }
    }

    private final int skipPadding(String text, int position) {
        while (position < text.length() && text.charAt(position) == this.pad) {
            position++;
        }
        return position;
    }

    private int compareAffix(String text, int pos, boolean isNegative, boolean isPrefix, String affixPat, boolean complexCurrencyParsing, int type, Currency[] currency) {
        if (currency != null || this.currencyChoice != null || (this.currencySignCount != 0 && complexCurrencyParsing)) {
            return compareComplexAffix(affixPat, text, pos, type, currency);
        }
        if (isPrefix) {
            return compareSimpleAffix(isNegative ? this.negativePrefix : this.positivePrefix, text, pos);
        }
        return compareSimpleAffix(isNegative ? this.negativeSuffix : this.positiveSuffix, text, pos);
    }

    private static boolean isBidiMark(int c) {
        return c == 8206 || c == 8207 || c == 1564;
    }

    private static String trimMarksFromAffix(String affix) {
        boolean hasBidiMark = false;
        int idx = 0;
        while (idx < affix.length()) {
            if (isBidiMark(affix.charAt(idx))) {
                hasBidiMark = true;
                break;
            }
            idx++;
        }
        if (!hasBidiMark) {
            return affix;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(affix, 0, idx);
        while (true) {
            idx++;
            if (idx >= affix.length()) {
                return buf.toString();
            }
            char c = affix.charAt(idx);
            if (!isBidiMark(c)) {
                buf.append(c);
            }
        }
    }

    private static int compareSimpleAffix(String affix, String input, int pos) {
        int start = pos;
        String trimmedAffix = affix.length() > 1 ? trimMarksFromAffix(affix) : affix;
        int i = 0;
        while (i < trimmedAffix.length()) {
            int c = UTF16.charAt(trimmedAffix, i);
            int len = UTF16.getCharCount(c);
            int ic;
            if (PatternProps.isWhiteSpace(c)) {
                boolean literalMatch = false;
                while (pos < input.length()) {
                    ic = UTF16.charAt(input, pos);
                    if (ic != c) {
                        if (!isBidiMark(ic)) {
                            break;
                        }
                        pos++;
                    } else {
                        literalMatch = true;
                        i += len;
                        pos += len;
                        if (i == trimmedAffix.length()) {
                            break;
                        }
                        c = UTF16.charAt(trimmedAffix, i);
                        len = UTF16.getCharCount(c);
                        if (!PatternProps.isWhiteSpace(c)) {
                            break;
                        }
                    }
                }
                i = skipPatternWhiteSpace(trimmedAffix, i);
                int s = pos;
                pos = skipUWhiteSpace(input, pos);
                if (pos == s && !literalMatch) {
                    return -1;
                }
                i = skipUWhiteSpace(trimmedAffix, i);
            } else {
                boolean match = false;
                while (pos < input.length()) {
                    ic = UTF16.charAt(input, pos);
                    if (match || !equalWithSignCompatibility(ic, c)) {
                        if (!isBidiMark(ic)) {
                            break;
                        }
                        pos++;
                    } else {
                        i += len;
                        pos += len;
                        match = true;
                    }
                }
                if (!match) {
                    return -1;
                }
            }
        }
        return pos - start;
    }

    private static boolean equalWithSignCompatibility(int lhs, int rhs) {
        if (lhs == rhs || (minusSigns.contains(lhs) && minusSigns.contains(rhs))) {
            return true;
        }
        return plusSigns.contains(lhs) ? plusSigns.contains(rhs) : false;
    }

    private static int skipPatternWhiteSpace(String text, int pos) {
        while (pos < text.length()) {
            int c = UTF16.charAt(text, pos);
            if (!PatternProps.isWhiteSpace(c)) {
                break;
            }
            pos += UTF16.getCharCount(c);
        }
        return pos;
    }

    private static int skipUWhiteSpace(String text, int pos) {
        while (pos < text.length()) {
            int c = UTF16.charAt(text, pos);
            if (!UCharacter.isUWhiteSpace(c)) {
                break;
            }
            pos += UTF16.getCharCount(c);
        }
        return pos;
    }

    private static int skipBidiMarks(String text, int pos) {
        while (pos < text.length()) {
            int c = UTF16.charAt(text, pos);
            if (!isBidiMark(c)) {
                break;
            }
            pos += UTF16.getCharCount(c);
        }
        return pos;
    }

    private int compareComplexAffix(String affixPat, String text, int pos, int type, Currency[] currency) {
        int start = pos;
        int i = 0;
        while (i < affixPat.length() && pos >= 0) {
            int i2 = i + 1;
            int c = affixPat.charAt(i);
            if (c == '\'') {
                i = i2;
                while (true) {
                    int j = affixPat.indexOf(39, i);
                    if (j == i) {
                        pos = match(text, pos, 39);
                        i = j + 1;
                    } else if (j > i) {
                        pos = match(text, pos, affixPat.substring(i, j));
                        i = j + 1;
                        if (i < affixPat.length() && affixPat.charAt(i) == '\'') {
                            pos = match(text, pos, 39);
                            i++;
                        }
                    } else {
                        throw new RuntimeException();
                    }
                }
            }
            switch (c) {
                case 37:
                    c = this.symbols.getPercent();
                    break;
                case 45:
                    c = this.symbols.getMinusSign();
                    break;
                case 164:
                    boolean intl = i2 < affixPat.length() && affixPat.charAt(i2) == CURRENCY_SIGN;
                    if (intl) {
                        i = i2 + 1;
                    } else {
                        i = i2;
                    }
                    boolean plural = i < affixPat.length() && affixPat.charAt(i) == CURRENCY_SIGN;
                    if (plural) {
                        i++;
                    }
                    ULocale uloc = getLocale(ULocale.VALID_LOCALE);
                    if (uloc == null) {
                        uloc = this.symbols.getLocale(ULocale.VALID_LOCALE);
                    }
                    ParsePosition ppos = new ParsePosition(pos);
                    String iso = Currency.parse(uloc, text, type, ppos);
                    if (iso == null) {
                        pos = -1;
                        break;
                    }
                    if (currency == null) {
                        if (iso.compareTo(getEffectiveCurrency().getCurrencyCode()) != 0) {
                            pos = -1;
                            break;
                        }
                    }
                    currency[0] = Currency.getInstance(iso);
                    pos = ppos.getIndex();
                    continue;
                    break;
                case 8240:
                    c = this.symbols.getPerMill();
                    break;
            }
            pos = match(text, pos, c);
            i = PatternProps.isWhiteSpace(c) ? skipPatternWhiteSpace(affixPat, i2) : i2;
        }
        return pos - start;
    }

    static final int match(String text, int pos, int ch) {
        if (pos < 0 || pos >= text.length()) {
            return -1;
        }
        pos = skipBidiMarks(text, pos);
        if (PatternProps.isWhiteSpace(ch)) {
            int s = pos;
            int pos2 = skipPatternWhiteSpace(text, pos);
            if (pos2 == pos) {
                return -1;
            }
            return pos2;
        } else if (pos >= text.length() || UTF16.charAt(text, pos) != ch) {
            return -1;
        } else {
            return skipBidiMarks(text, UTF16.getCharCount(ch) + pos);
        }
    }

    static final int match(String text, int pos, String str) {
        int i = 0;
        while (i < str.length() && pos >= 0) {
            int ch = UTF16.charAt(str, i);
            i += UTF16.getCharCount(ch);
            pos = match(text, pos, ch);
            if (PatternProps.isWhiteSpace(ch)) {
                i = skipPatternWhiteSpace(str, i);
            }
        }
        return pos;
    }

    public DecimalFormatSymbols getDecimalFormatSymbols() {
        try {
            return (DecimalFormatSymbols) this.symbols.clone();
        } catch (Exception e) {
            return null;
        }
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        this.symbols = (DecimalFormatSymbols) newSymbols.clone();
        setCurrencyForSymbols();
        expandAffixes(null);
    }

    private void setCurrencyForSymbols() {
        DecimalFormatSymbols def = new DecimalFormatSymbols(this.symbols.getULocale());
        if (this.symbols.getCurrencySymbol().equals(def.getCurrencySymbol()) && this.symbols.getInternationalCurrencySymbol().equals(def.getInternationalCurrencySymbol())) {
            setCurrency(Currency.getInstance(this.symbols.getULocale()));
        } else {
            setCurrency(null);
        }
    }

    public String getPositivePrefix() {
        return this.positivePrefix;
    }

    public void setPositivePrefix(String newValue) {
        this.positivePrefix = newValue;
        this.posPrefixPattern = null;
    }

    public String getNegativePrefix() {
        return this.negativePrefix;
    }

    public void setNegativePrefix(String newValue) {
        this.negativePrefix = newValue;
        this.negPrefixPattern = null;
    }

    public String getPositiveSuffix() {
        return this.positiveSuffix;
    }

    public void setPositiveSuffix(String newValue) {
        this.positiveSuffix = newValue;
        this.posSuffixPattern = null;
    }

    public String getNegativeSuffix() {
        return this.negativeSuffix;
    }

    public void setNegativeSuffix(String newValue) {
        this.negativeSuffix = newValue;
        this.negSuffixPattern = null;
    }

    public int getMultiplier() {
        return this.multiplier;
    }

    public void setMultiplier(int newValue) {
        if (newValue == 0) {
            throw new IllegalArgumentException("Bad multiplier: " + newValue);
        }
        this.multiplier = newValue;
    }

    public BigDecimal getRoundingIncrement() {
        if (this.roundingIncrementICU == null) {
            return null;
        }
        return this.roundingIncrementICU.toBigDecimal();
    }

    public void setRoundingIncrement(BigDecimal newValue) {
        if (newValue == null) {
            setRoundingIncrement((android.icu.math.BigDecimal) null);
        } else {
            setRoundingIncrement(new android.icu.math.BigDecimal(newValue));
        }
    }

    public void setRoundingIncrement(android.icu.math.BigDecimal newValue) {
        int i = 0;
        if (newValue != null) {
            i = newValue.compareTo(android.icu.math.BigDecimal.ZERO);
        }
        if (i < 0) {
            throw new IllegalArgumentException("Illegal rounding increment");
        }
        if (i == 0) {
            setInternalRoundingIncrement(null);
        } else {
            setInternalRoundingIncrement(newValue);
        }
        resetActualRounding();
    }

    public void setRoundingIncrement(double newValue) {
        if (newValue < 0.0d) {
            throw new IllegalArgumentException("Illegal rounding increment");
        }
        if (newValue == 0.0d) {
            setInternalRoundingIncrement((android.icu.math.BigDecimal) null);
        } else {
            setInternalRoundingIncrement(android.icu.math.BigDecimal.valueOf(newValue));
        }
        resetActualRounding();
    }

    public int getRoundingMode() {
        return this.roundingMode;
    }

    public void setRoundingMode(int roundingMode) {
        if (roundingMode < 0 || roundingMode > 7) {
            throw new IllegalArgumentException("Invalid rounding mode: " + roundingMode);
        }
        this.roundingMode = roundingMode;
        resetActualRounding();
    }

    public int getFormatWidth() {
        return this.formatWidth;
    }

    public void setFormatWidth(int width) {
        if (width < 0) {
            throw new IllegalArgumentException("Illegal format width");
        }
        this.formatWidth = width;
    }

    public char getPadCharacter() {
        return this.pad;
    }

    public void setPadCharacter(char padChar) {
        this.pad = padChar;
    }

    public int getPadPosition() {
        return this.padPosition;
    }

    public void setPadPosition(int padPos) {
        if (padPos < 0 || padPos > 3) {
            throw new IllegalArgumentException("Illegal pad position");
        }
        this.padPosition = padPos;
    }

    public boolean isScientificNotation() {
        return this.useExponentialNotation;
    }

    public void setScientificNotation(boolean useScientific) {
        this.useExponentialNotation = useScientific;
    }

    public byte getMinimumExponentDigits() {
        return this.minExponentDigits;
    }

    public void setMinimumExponentDigits(byte minExpDig) {
        if (minExpDig < (byte) 1) {
            throw new IllegalArgumentException("Exponent digits must be >= 1");
        }
        this.minExponentDigits = minExpDig;
    }

    public boolean isExponentSignAlwaysShown() {
        return this.exponentSignAlwaysShown;
    }

    public void setExponentSignAlwaysShown(boolean expSignAlways) {
        this.exponentSignAlwaysShown = expSignAlways;
    }

    public int getGroupingSize() {
        return this.groupingSize;
    }

    public void setGroupingSize(int newValue) {
        this.groupingSize = (byte) newValue;
    }

    public int getSecondaryGroupingSize() {
        return this.groupingSize2;
    }

    public void setSecondaryGroupingSize(int newValue) {
        this.groupingSize2 = (byte) newValue;
    }

    public MathContext getMathContextICU() {
        return this.mathContext;
    }

    public java.math.MathContext getMathContext() {
        java.math.MathContext mathContext = null;
        try {
            if (this.mathContext != null) {
                mathContext = new java.math.MathContext(this.mathContext.getDigits(), RoundingMode.valueOf(this.mathContext.getRoundingMode()));
            }
            return mathContext;
        } catch (Exception e) {
            return null;
        }
    }

    public void setMathContextICU(MathContext newValue) {
        this.mathContext = newValue;
    }

    public void setMathContext(java.math.MathContext newValue) {
        this.mathContext = new MathContext(newValue.getPrecision(), 1, false, newValue.getRoundingMode().ordinal());
    }

    public boolean isDecimalSeparatorAlwaysShown() {
        return this.decimalSeparatorAlwaysShown;
    }

    public void setDecimalPatternMatchRequired(boolean value) {
        this.parseRequireDecimalPoint = value;
    }

    public boolean isDecimalPatternMatchRequired() {
        return this.parseRequireDecimalPoint;
    }

    public void setDecimalSeparatorAlwaysShown(boolean newValue) {
        this.decimalSeparatorAlwaysShown = newValue;
    }

    public CurrencyPluralInfo getCurrencyPluralInfo() {
        try {
            CurrencyPluralInfo currencyPluralInfo;
            if (this.currencyPluralInfo == null) {
                currencyPluralInfo = null;
            } else {
                currencyPluralInfo = (CurrencyPluralInfo) this.currencyPluralInfo.clone();
            }
            return currencyPluralInfo;
        } catch (Exception e) {
            return null;
        }
    }

    public void setCurrencyPluralInfo(CurrencyPluralInfo newInfo) {
        this.currencyPluralInfo = (CurrencyPluralInfo) newInfo.clone();
        this.isReadyForParsing = false;
    }

    public Object clone() {
        try {
            DecimalFormat other = (DecimalFormat) super.clone();
            other.symbols = (DecimalFormatSymbols) this.symbols.clone();
            other.digitList = new DigitList();
            if (this.currencyPluralInfo != null) {
                other.currencyPluralInfo = (CurrencyPluralInfo) this.currencyPluralInfo.clone();
            }
            other.attributes = new ArrayList();
            other.currencyUsage = this.currencyUsage;
            return other;
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !super.equals(obj)) {
            return false;
        }
        DecimalFormat other = (DecimalFormat) obj;
        if (this.currencySignCount == other.currencySignCount && ((this.style != 6 || (equals(this.posPrefixPattern, other.posPrefixPattern) && equals(this.posSuffixPattern, other.posSuffixPattern) && equals(this.negPrefixPattern, other.negPrefixPattern) && equals(this.negSuffixPattern, other.negSuffixPattern))) && this.multiplier == other.multiplier && this.groupingSize == other.groupingSize && this.groupingSize2 == other.groupingSize2 && this.decimalSeparatorAlwaysShown == other.decimalSeparatorAlwaysShown && this.useExponentialNotation == other.useExponentialNotation && ((!this.useExponentialNotation || this.minExponentDigits == other.minExponentDigits) && this.useSignificantDigits == other.useSignificantDigits && ((!this.useSignificantDigits || (this.minSignificantDigits == other.minSignificantDigits && this.maxSignificantDigits == other.maxSignificantDigits)) && this.symbols.equals(other.symbols) && Utility.objectEquals(this.currencyPluralInfo, other.currencyPluralInfo))))) {
            z = this.currencyUsage.equals(other.currencyUsage);
        }
        return z;
    }

    private boolean equals(String pat1, String pat2) {
        boolean z = true;
        if (pat1 == null || pat2 == null) {
            if (!(pat1 == null && pat2 == null)) {
                z = false;
            }
            return z;
        } else if (pat1.equals(pat2)) {
            return true;
        } else {
            return unquote(pat1).equals(unquote(pat2));
        }
    }

    private String unquote(String pat) {
        StringBuilder buf = new StringBuilder(pat.length());
        int i = 0;
        while (i < pat.length()) {
            int i2 = i + 1;
            char ch = pat.charAt(i);
            if (ch != '\'') {
                buf.append(ch);
            }
            i = i2;
        }
        return buf.toString();
    }

    public int hashCode() {
        return (super.hashCode() * 37) + this.positivePrefix.hashCode();
    }

    public String toPattern() {
        if (this.style == 6) {
            return this.formatPattern;
        }
        return toPattern(false);
    }

    public String toLocalizedPattern() {
        if (this.style == 6) {
            return this.formatPattern;
        }
        return toPattern(true);
    }

    private void expandAffixes(String pluralCount) {
        this.currencyChoice = null;
        StringBuffer buffer = new StringBuffer();
        if (this.posPrefixPattern != null) {
            expandAffix(this.posPrefixPattern, pluralCount, buffer, false);
            this.positivePrefix = buffer.toString();
        }
        if (this.posSuffixPattern != null) {
            expandAffix(this.posSuffixPattern, pluralCount, buffer, false);
            this.positiveSuffix = buffer.toString();
        }
        if (this.negPrefixPattern != null) {
            expandAffix(this.negPrefixPattern, pluralCount, buffer, false);
            this.negativePrefix = buffer.toString();
        }
        if (this.negSuffixPattern != null) {
            expandAffix(this.negSuffixPattern, pluralCount, buffer, false);
            this.negativeSuffix = buffer.toString();
        }
    }

    private void expandAffix(String pattern, String pluralCount, StringBuffer buffer, boolean doFormat) {
        buffer.setLength(0);
        int i = 0;
        while (i < pattern.length()) {
            int i2 = i + 1;
            char c = pattern.charAt(i);
            if (c == '\'') {
                i = i2;
                while (true) {
                    int j = pattern.indexOf(39, i);
                    if (j == i) {
                        buffer.append('\'');
                        i = j + 1;
                    } else if (j > i) {
                        buffer.append(pattern.substring(i, j));
                        i = j + 1;
                        if (i < pattern.length() && pattern.charAt(i) == '\'') {
                            buffer.append('\'');
                            i++;
                        }
                    } else {
                        throw new RuntimeException();
                    }
                }
            }
            switch (c) {
                case '%':
                    c = this.symbols.getPercent();
                    break;
                case '-':
                    buffer.append(this.symbols.getMinusString());
                    i = i2;
                    continue;
                case 164:
                    String s;
                    boolean intl = i2 < pattern.length() && pattern.charAt(i2) == CURRENCY_SIGN;
                    boolean plural = false;
                    if (intl) {
                        i = i2 + 1;
                        if (i < pattern.length() && pattern.charAt(i) == CURRENCY_SIGN) {
                            plural = true;
                            intl = false;
                            i++;
                        }
                    } else {
                        i = i2;
                    }
                    Currency currency = getCurrency();
                    if (currency != null) {
                        if (plural && pluralCount != null) {
                            s = currency.getName(this.symbols.getULocale(), 2, pluralCount, new boolean[1]);
                        } else if (!intl) {
                            boolean[] isChoiceFormat = new boolean[1];
                            s = currency.getName(this.symbols.getULocale(), 0, isChoiceFormat);
                            if (isChoiceFormat[0]) {
                                if (doFormat) {
                                    this.currencyChoice.format(this.digitList.getDouble(), buffer, new FieldPosition(0));
                                    break;
                                }
                                if (this.currencyChoice == null) {
                                    this.currencyChoice = new ChoiceFormat(s);
                                }
                                s = String.valueOf(CURRENCY_SIGN);
                            }
                        } else {
                            s = currency.getCurrencyCode();
                        }
                    } else if (intl) {
                        s = this.symbols.getInternationalCurrencySymbol();
                    } else {
                        s = this.symbols.getCurrencySymbol();
                    }
                    buffer.append(s);
                    continue;
                case 8240:
                    c = this.symbols.getPerMill();
                    break;
            }
            buffer.append(c);
            i = i2;
        }
    }

    private int appendAffix(StringBuffer buf, boolean isNegative, boolean isPrefix, FieldPosition fieldPosition, boolean parseAttr) {
        if (this.currencyChoice != null) {
            String affixPat = isPrefix ? isNegative ? this.negPrefixPattern : this.posPrefixPattern : isNegative ? this.negSuffixPattern : this.posSuffixPattern;
            StringBuffer affixBuf = new StringBuffer();
            expandAffix(affixPat, null, affixBuf, true);
            buf.append(affixBuf);
            return affixBuf.length();
        }
        String affix;
        String pattern;
        if (isPrefix) {
            affix = isNegative ? this.negativePrefix : this.positivePrefix;
            pattern = isNegative ? this.negPrefixPattern : this.posPrefixPattern;
        } else {
            affix = isNegative ? this.negativeSuffix : this.positiveSuffix;
            pattern = isNegative ? this.negSuffixPattern : this.posSuffixPattern;
        }
        if (parseAttr) {
            int offset = affix.indexOf(this.symbols.getCurrencySymbol());
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.CURRENCY, buf, offset, this.symbols.getCurrencySymbol().length());
            }
            offset = affix.indexOf(this.symbols.getMinusString());
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.SIGN, buf, offset, this.symbols.getMinusString().length());
            }
            offset = affix.indexOf(this.symbols.getPercent());
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.PERCENT, buf, offset, 1);
            }
            offset = affix.indexOf(this.symbols.getPerMill());
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.PERMILLE, buf, offset, 1);
            }
            offset = pattern.indexOf("¤¤¤");
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.CURRENCY, buf, offset, affix.length() - offset);
            }
        }
        if (fieldPosition.getFieldAttribute() == Field.CURRENCY) {
            String aff;
            int start;
            int end;
            if (affix.indexOf(this.symbols.getCurrencySymbol()) > -1) {
                aff = this.symbols.getCurrencySymbol();
                start = buf.length() + affix.indexOf(aff);
                end = start + aff.length();
                fieldPosition.setBeginIndex(start);
                fieldPosition.setEndIndex(end);
            } else if (affix.indexOf(this.symbols.getInternationalCurrencySymbol()) > -1) {
                aff = this.symbols.getInternationalCurrencySymbol();
                start = buf.length() + affix.indexOf(aff);
                end = start + aff.length();
                fieldPosition.setBeginIndex(start);
                fieldPosition.setEndIndex(end);
            } else if (pattern.indexOf("¤¤¤") > -1) {
                end = buf.length() + affix.length();
                fieldPosition.setBeginIndex(buf.length() + pattern.indexOf("¤¤¤"));
                fieldPosition.setEndIndex(end);
            }
        }
        buf.append(affix);
        return affix.length();
    }

    private void formatAffix2Attribute(boolean isPrefix, Field fieldType, StringBuffer buf, int offset, int symbolSize) {
        int begin = offset;
        if (!isPrefix) {
            begin = offset + buf.length();
        }
        addAttribute(fieldType, begin, begin + symbolSize);
    }

    private void addAttribute(Field field, int begin, int end) {
        FieldPosition pos = new FieldPosition(field);
        pos.setBeginIndex(begin);
        pos.setEndIndex(end);
        this.attributes.add(pos);
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        return formatToCharacterIterator(obj, NULL_UNIT);
    }

    AttributedCharacterIterator formatToCharacterIterator(Object obj, Unit unit) {
        if (obj instanceof Number) {
            Number number = (Number) obj;
            StringBuffer text = new StringBuffer();
            unit.writePrefix(text);
            this.attributes.clear();
            if (obj instanceof BigInteger) {
                format((BigInteger) number, text, new FieldPosition(0), true);
            } else if (obj instanceof BigDecimal) {
                format((BigDecimal) number, text, new FieldPosition(0), true);
            } else if (obj instanceof Double) {
                format(number.doubleValue(), text, new FieldPosition(0), true);
            } else if ((obj instanceof Integer) || (obj instanceof Long)) {
                format(number.longValue(), text, new FieldPosition(0), true);
            } else {
                throw new IllegalArgumentException();
            }
            unit.writeSuffix(text);
            AttributedString as = new AttributedString(text.toString());
            for (int i = 0; i < this.attributes.size(); i++) {
                FieldPosition pos = (FieldPosition) this.attributes.get(i);
                Format.Field attribute = pos.getFieldAttribute();
                as.addAttribute(attribute, attribute, pos.getBeginIndex(), pos.getEndIndex());
            }
            return as.getIterator();
        }
        throw new IllegalArgumentException();
    }

    private void appendAffixPattern(StringBuffer buffer, boolean isNegative, boolean isPrefix, boolean localized) {
        String affixPat = isPrefix ? isNegative ? this.negPrefixPattern : this.posPrefixPattern : isNegative ? this.negSuffixPattern : this.posSuffixPattern;
        int i;
        char ch;
        if (affixPat == null) {
            String affix = isPrefix ? isNegative ? this.negativePrefix : this.positivePrefix : isNegative ? this.negativeSuffix : this.positiveSuffix;
            buffer.append('\'');
            for (i = 0; i < affix.length(); i++) {
                ch = affix.charAt(i);
                if (ch == '\'') {
                    buffer.append(ch);
                }
                buffer.append(ch);
            }
            buffer.append('\'');
            return;
        }
        if (localized) {
            i = 0;
            while (i < affixPat.length()) {
                ch = affixPat.charAt(i);
                switch (ch) {
                    case '%':
                        ch = this.symbols.getPercent();
                        break;
                    case '\'':
                        int j = affixPat.indexOf(39, i + 1);
                        if (j < 0) {
                            throw new IllegalArgumentException("Malformed affix pattern: " + affixPat);
                        }
                        buffer.append(affixPat.substring(i, j + 1));
                        i = j;
                        continue;
                    case '-':
                        ch = this.symbols.getMinusSign();
                        break;
                    case 8240:
                        ch = this.symbols.getPerMill();
                        break;
                }
                if (ch == this.symbols.getDecimalSeparator() || ch == this.symbols.getGroupingSeparator()) {
                    buffer.append('\'');
                    buffer.append(ch);
                    buffer.append('\'');
                    i++;
                } else {
                    buffer.append(ch);
                    i++;
                }
            }
        } else {
            buffer.append(affixPat);
        }
    }

    private String toPattern(boolean localized) {
        char padEscape;
        String padSpec;
        int i;
        StringBuffer result = new StringBuffer();
        char zero = localized ? this.symbols.getZeroDigit() : PATTERN_ZERO_DIGIT;
        char digit = localized ? this.symbols.getDigit() : PATTERN_DIGIT;
        char sigDigit = 0;
        boolean useSigDig = areSignificantDigitsUsed();
        if (useSigDig) {
            sigDigit = localized ? this.symbols.getSignificantDigit() : PATTERN_SIGNIFICANT_DIGIT;
        }
        char group = localized ? this.symbols.getGroupingSeparator() : PATTERN_GROUPING_SEPARATOR;
        int roundingDecimalPos = 0;
        String roundingDigits = null;
        int padPos = this.formatWidth > 0 ? this.padPosition : -1;
        if (this.formatWidth > 0) {
            StringBuffer stringBuffer = new StringBuffer(2);
            if (localized) {
                padEscape = this.symbols.getPadEscape();
            } else {
                padEscape = PATTERN_PAD_ESCAPE;
            }
            padSpec = stringBuffer.append(padEscape).append(this.pad).toString();
        } else {
            padSpec = null;
        }
        if (this.roundingIncrementICU != null) {
            i = this.roundingIncrementICU.scale();
            roundingDigits = this.roundingIncrementICU.movePointRight(i).toString();
            roundingDecimalPos = roundingDigits.length() - i;
        }
        int part = 0;
        while (part < 2) {
            int minDig;
            int maxDig;
            int pos;
            if (padPos == 0) {
                result.append(padSpec);
            }
            appendAffixPattern(result, part != 0, true, localized);
            if (padPos == 1) {
                result.append(padSpec);
            }
            int sub0Start = result.length();
            int g = isGroupingUsed() ? Math.max(0, this.groupingSize) : 0;
            if (g > 0 && this.groupingSize2 > (byte) 0 && this.groupingSize2 != this.groupingSize) {
                g += this.groupingSize2;
            }
            int maxSigDig = 0;
            if (useSigDig) {
                minDig = getMinimumSignificantDigits();
                maxSigDig = getMaximumSignificantDigits();
                maxDig = maxSigDig;
            } else {
                minDig = getMinimumIntegerDigits();
                maxDig = getMaximumIntegerDigits();
            }
            if (this.useExponentialNotation) {
                if (maxDig > 8) {
                    maxDig = 1;
                }
            } else if (useSigDig) {
                maxDig = Math.max(maxDig, g + 1);
            } else {
                maxDig = Math.max(Math.max(g, getMinimumIntegerDigits()), roundingDecimalPos) + 1;
            }
            i = maxDig;
            while (i > 0) {
                if (!this.useExponentialNotation && i < maxDig && isGroupingPosition(i)) {
                    result.append(group);
                }
                if (useSigDig) {
                    if (maxSigDig < i || i <= maxSigDig - minDig) {
                        padEscape = digit;
                    } else {
                        padEscape = sigDigit;
                    }
                    result.append(padEscape);
                } else {
                    if (roundingDigits != null) {
                        pos = roundingDecimalPos - i;
                        if (pos >= 0 && pos < roundingDigits.length()) {
                            result.append((char) ((roundingDigits.charAt(pos) - 48) + zero));
                        }
                    }
                    result.append(i <= minDig ? zero : digit);
                }
                i--;
            }
            if (!useSigDig) {
                if (getMaximumFractionDigits() > 0 || this.decimalSeparatorAlwaysShown) {
                    if (localized) {
                        padEscape = this.symbols.getDecimalSeparator();
                    } else {
                        padEscape = PATTERN_DECIMAL_SEPARATOR;
                    }
                    result.append(padEscape);
                }
                pos = roundingDecimalPos;
                i = 0;
                while (i < getMaximumFractionDigits()) {
                    if (roundingDigits == null || pos >= roundingDigits.length()) {
                        result.append(i < getMinimumFractionDigits() ? zero : digit);
                    } else {
                        if (pos < 0) {
                            padEscape = zero;
                        } else {
                            padEscape = (char) ((roundingDigits.charAt(pos) - 48) + zero);
                        }
                        result.append(padEscape);
                        pos++;
                    }
                    i++;
                }
            }
            if (this.useExponentialNotation) {
                if (localized) {
                    result.append(this.symbols.getExponentSeparator());
                } else {
                    result.append(PATTERN_EXPONENT);
                }
                if (this.exponentSignAlwaysShown) {
                    result.append(localized ? this.symbols.getPlusSign() : PATTERN_PLUS_SIGN);
                }
                for (byte i2 = (byte) 0; i2 < this.minExponentDigits; i2++) {
                    result.append(zero);
                }
            }
            if (!(padSpec == null || this.useExponentialNotation)) {
                int length;
                int length2 = (this.formatWidth - result.length()) + sub0Start;
                if (part == 0) {
                    length = this.positivePrefix.length() + this.positiveSuffix.length();
                } else {
                    length = this.negativePrefix.length() + this.negativeSuffix.length();
                }
                int add = length2 - length;
                while (add > 0) {
                    result.insert(sub0Start, digit);
                    maxDig++;
                    add--;
                    if (add > 1 && isGroupingPosition(maxDig)) {
                        result.insert(sub0Start, group);
                        add--;
                    }
                }
            }
            if (padPos == 2) {
                result.append(padSpec);
            }
            appendAffixPattern(result, part != 0, false, localized);
            if (padPos == 3) {
                result.append(padSpec);
            }
            if (part == 0) {
                if (this.negativeSuffix.equals(this.positiveSuffix) && this.negativePrefix.equals(PATTERN_MINUS + this.positivePrefix)) {
                    break;
                }
                if (localized) {
                    padEscape = this.symbols.getPatternSeparator();
                } else {
                    padEscape = PATTERN_SEPARATOR;
                }
                result.append(padEscape);
            }
            part++;
        }
        return result.toString();
    }

    public void applyPattern(String pattern) {
        applyPattern(pattern, false);
    }

    public void applyLocalizedPattern(String pattern) {
        applyPattern(pattern, true);
    }

    private void applyPattern(String pattern, boolean localized) {
        applyPatternWithoutExpandAffix(pattern, localized);
        expandAffixAdjustWidth(null);
    }

    private void expandAffixAdjustWidth(String pluralCount) {
        expandAffixes(pluralCount);
        if (this.formatWidth > 0) {
            this.formatWidth += this.positivePrefix.length() + this.positiveSuffix.length();
        }
    }

    /* JADX WARNING: Missing block: B:13:0x00fa, code:
            r42 = r42 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void applyPatternWithoutExpandAffix(String pattern, boolean localized) {
        String stringBuilder;
        char zeroDigit = PATTERN_ZERO_DIGIT;
        char sigDigit = PATTERN_SIGNIFICANT_DIGIT;
        char groupingSeparator = PATTERN_GROUPING_SEPARATOR;
        char decimalSeparator = PATTERN_DECIMAL_SEPARATOR;
        char percent = PATTERN_PERCENT;
        char perMill = PATTERN_PER_MILLE;
        char digit = PATTERN_DIGIT;
        char separator = PATTERN_SEPARATOR;
        String exponent = String.valueOf(PATTERN_EXPONENT);
        char plus = PATTERN_PLUS_SIGN;
        char padEscape = PATTERN_PAD_ESCAPE;
        char minus = PATTERN_MINUS;
        if (localized) {
            zeroDigit = this.symbols.getZeroDigit();
            sigDigit = this.symbols.getSignificantDigit();
            groupingSeparator = this.symbols.getGroupingSeparator();
            decimalSeparator = this.symbols.getDecimalSeparator();
            percent = this.symbols.getPercent();
            perMill = this.symbols.getPerMill();
            digit = this.symbols.getDigit();
            separator = this.symbols.getPatternSeparator();
            exponent = this.symbols.getExponentSeparator();
            plus = this.symbols.getPlusSign();
            padEscape = this.symbols.getPadEscape();
            minus = this.symbols.getMinusSign();
        }
        char nineDigit = (char) (zeroDigit + 9);
        boolean gotNegative = false;
        int pos = 0;
        int part = 0;
        while (part < 2 && pos < pattern.length()) {
            int subpart = 1;
            int sub0Start = 0;
            int sub0Limit = 0;
            int sub2Limit = 0;
            StringBuilder prefix = new StringBuilder();
            StringBuilder suffix = new StringBuilder();
            int decimalPos = -1;
            int multpl = 1;
            int digitLeftCount = 0;
            int zeroDigitCount = 0;
            int digitRightCount = 0;
            int sigDigitCount = 0;
            byte groupingCount = (byte) -1;
            byte groupingCount2 = (byte) -1;
            int padPos = -1;
            char padChar = 0;
            int incrementPos = -1;
            long incrementVal = 0;
            byte expDigits = (byte) -1;
            boolean expSignAlways = false;
            int currencySignCnt = 0;
            StringBuilder affix = prefix;
            int start = pos;
            while (pos < pattern.length()) {
                char ch = pattern.charAt(pos);
                switch (subpart) {
                    case 0:
                        if (ch != digit) {
                            if ((ch < zeroDigit || ch > nineDigit) && ch != sigDigit) {
                                if (ch != groupingSeparator) {
                                    if (ch != decimalSeparator) {
                                        if (pattern.regionMatches(pos, exponent, 0, exponent.length())) {
                                            if (expDigits >= (byte) 0) {
                                                patternError("Multiple exponential symbols", pattern);
                                            }
                                            if (groupingCount >= (byte) 0) {
                                                patternError("Grouping separator in exponential", pattern);
                                            }
                                            pos += exponent.length();
                                            if (pos < pattern.length() && pattern.charAt(pos) == plus) {
                                                expSignAlways = true;
                                                pos++;
                                            }
                                            expDigits = (byte) 0;
                                            while (pos < pattern.length() && pattern.charAt(pos) == zeroDigit) {
                                                expDigits = (byte) (expDigits + 1);
                                                pos++;
                                            }
                                            if ((digitLeftCount + zeroDigitCount < 1 && sigDigitCount + digitRightCount < 1) || ((sigDigitCount > 0 && digitLeftCount > 0) || expDigits < (byte) 1)) {
                                                patternError("Malformed exponential", pattern);
                                            }
                                        }
                                        subpart = 2;
                                        affix = suffix;
                                        sub0Limit = pos;
                                        pos--;
                                        break;
                                    }
                                    if (decimalPos >= 0) {
                                        patternError("Multiple decimal separators", pattern);
                                    }
                                    decimalPos = (digitLeftCount + zeroDigitCount) + digitRightCount;
                                    break;
                                }
                                if (ch == '\'' && pos + 1 < pattern.length()) {
                                    char after = pattern.charAt(pos + 1);
                                    if (after != digit && (after < zeroDigit || after > nineDigit)) {
                                        if (after != '\'') {
                                            if (groupingCount >= (byte) 0) {
                                                subpart = 2;
                                                affix = suffix;
                                                sub0Limit = pos;
                                                pos--;
                                                break;
                                            }
                                            subpart = 3;
                                            break;
                                        }
                                        pos++;
                                    }
                                }
                                if (decimalPos >= 0) {
                                    patternError("Grouping separator after decimal", pattern);
                                }
                                groupingCount2 = groupingCount;
                                groupingCount = (byte) 0;
                                break;
                            }
                            if (digitRightCount > 0) {
                                patternError("Unexpected '" + ch + '\'', pattern);
                            }
                            if (ch == sigDigit) {
                                sigDigitCount++;
                            } else {
                                zeroDigitCount++;
                                if (ch != zeroDigit) {
                                    int p = (digitLeftCount + zeroDigitCount) + digitRightCount;
                                    if (incrementPos >= 0) {
                                        while (incrementPos < p) {
                                            incrementVal *= 10;
                                            incrementPos++;
                                        }
                                    } else {
                                        incrementPos = p;
                                    }
                                    incrementVal += (long) (ch - zeroDigit);
                                }
                            }
                            if (groupingCount >= (byte) 0 && decimalPos < 0) {
                                groupingCount = (byte) (groupingCount + 1);
                                break;
                            }
                        }
                        if (zeroDigitCount > 0 || sigDigitCount > 0) {
                            digitRightCount++;
                        } else {
                            digitLeftCount++;
                        }
                        if (groupingCount >= (byte) 0 && decimalPos < 0) {
                            groupingCount = (byte) (groupingCount + 1);
                            break;
                        }
                        break;
                    case 1:
                    case 2:
                        if (ch != digit && ch != groupingSeparator && ch != decimalSeparator && ((ch < zeroDigit || ch > nineDigit) && ch != sigDigit)) {
                            if (ch != 164) {
                                if (ch != '\'') {
                                    if (ch != separator) {
                                        if (ch != percent && ch != perMill) {
                                            if (ch != minus) {
                                                if (ch == padEscape) {
                                                    if (padPos >= 0) {
                                                        patternError("Multiple pad specifiers", pattern);
                                                    }
                                                    if (pos + 1 == pattern.length()) {
                                                        patternError("Invalid pad specifier", pattern);
                                                    }
                                                    padPos = pos;
                                                    pos++;
                                                    padChar = pattern.charAt(pos);
                                                    break;
                                                }
                                            }
                                            ch = PATTERN_MINUS;
                                        } else {
                                            if (multpl != 1) {
                                                patternError("Too many percent/permille characters", pattern);
                                            }
                                            multpl = ch == percent ? 100 : 1000;
                                            ch = ch == percent ? PATTERN_PERCENT : PATTERN_PER_MILLE;
                                        }
                                    } else {
                                        if (subpart == 1 || part == 1) {
                                            patternError("Unquoted special character '" + ch + '\'', pattern);
                                        }
                                        sub2Limit = pos;
                                        pos++;
                                        break;
                                    }
                                }
                                if (pos + 1 >= pattern.length() || pattern.charAt(pos + 1) != '\'') {
                                    subpart += 2;
                                } else {
                                    pos++;
                                    affix.append(ch);
                                }
                            } else {
                                boolean doubled = pos + 1 < pattern.length() ? pattern.charAt(pos + 1) == 164 : false;
                                if (doubled) {
                                    pos++;
                                    affix.append(ch);
                                    if (pos + 1 >= pattern.length() || pattern.charAt(pos + 1) != 164) {
                                        currencySignCnt = 2;
                                    } else {
                                        pos++;
                                        affix.append(ch);
                                        currencySignCnt = 3;
                                    }
                                } else {
                                    currencySignCnt = 1;
                                }
                            }
                        } else if (subpart == 1) {
                            subpart = 0;
                            sub0Start = pos;
                            pos--;
                            break;
                        } else if (ch == '\'') {
                            if (pos + 1 < pattern.length() && pattern.charAt(pos + 1) == '\'') {
                                pos++;
                                affix.append(ch);
                                break;
                            }
                            subpart += 2;
                            break;
                        } else {
                            patternError("Unquoted special character '" + ch + '\'', pattern);
                        }
                        affix.append(ch);
                        break;
                    case 3:
                    case 4:
                        if (ch == '\'') {
                            if (pos + 1 >= pattern.length() || pattern.charAt(pos + 1) != '\'') {
                                subpart -= 2;
                            } else {
                                pos++;
                                affix.append(ch);
                            }
                        }
                        affix.append(ch);
                        break;
                    default:
                        break;
                }
            }
            if (subpart == 3 || subpart == 4) {
                patternError("Unterminated quote", pattern);
            }
            if (sub0Limit == 0) {
                sub0Limit = pattern.length();
            }
            if (sub2Limit == 0) {
                sub2Limit = pattern.length();
            }
            if (zeroDigitCount == 0 && sigDigitCount == 0 && digitLeftCount > 0 && decimalPos >= 0) {
                int n = decimalPos;
                if (decimalPos == 0) {
                    n++;
                }
                digitRightCount = digitLeftCount - n;
                digitLeftCount = n - 1;
                zeroDigitCount = 1;
            }
            if ((decimalPos < 0 && digitRightCount > 0 && sigDigitCount == 0) || ((decimalPos >= 0 && (sigDigitCount > 0 || decimalPos < digitLeftCount || decimalPos > digitLeftCount + zeroDigitCount)) || groupingCount == (byte) 0 || groupingCount2 == (byte) 0 || ((sigDigitCount > 0 && zeroDigitCount > 0) || subpart > 2))) {
                patternError("Malformed pattern", pattern);
            }
            if (padPos >= 0) {
                if (padPos == start) {
                    padPos = 0;
                } else if (padPos + 2 == sub0Start) {
                    padPos = 1;
                } else if (padPos == sub0Limit) {
                    padPos = 2;
                } else if (padPos + 2 == sub2Limit) {
                    padPos = 3;
                } else {
                    patternError("Illegal pad position", pattern);
                }
            }
            if (part == 0) {
                stringBuilder = prefix.toString();
                this.negPrefixPattern = stringBuilder;
                this.posPrefixPattern = stringBuilder;
                stringBuilder = suffix.toString();
                this.negSuffixPattern = stringBuilder;
                this.posSuffixPattern = stringBuilder;
                this.useExponentialNotation = expDigits >= (byte) 0;
                if (this.useExponentialNotation) {
                    this.minExponentDigits = expDigits;
                    this.exponentSignAlwaysShown = expSignAlways;
                }
                int digitTotalCount = (digitLeftCount + zeroDigitCount) + digitRightCount;
                int effectiveDecimalPos = decimalPos >= 0 ? decimalPos : digitTotalCount;
                boolean useSigDig = sigDigitCount > 0;
                setSignificantDigitsUsed(useSigDig);
                if (useSigDig) {
                    setMinimumSignificantDigits(sigDigitCount);
                    setMaximumSignificantDigits(sigDigitCount + digitRightCount);
                } else {
                    int i;
                    int minInt = effectiveDecimalPos - digitLeftCount;
                    setMinimumIntegerDigits(minInt);
                    if (this.useExponentialNotation) {
                        i = digitLeftCount + minInt;
                    } else {
                        i = DOUBLE_INTEGER_DIGITS;
                    }
                    setMaximumIntegerDigits(i);
                    _setMaximumFractionDigits(decimalPos >= 0 ? digitTotalCount - decimalPos : 0);
                    setMinimumFractionDigits(decimalPos >= 0 ? (digitLeftCount + zeroDigitCount) - decimalPos : 0);
                }
                setGroupingUsed(groupingCount > (byte) 0);
                this.groupingSize = groupingCount > (byte) 0 ? groupingCount : (byte) 0;
                if (groupingCount2 <= (byte) 0 || groupingCount2 == groupingCount) {
                    groupingCount2 = (byte) 0;
                }
                this.groupingSize2 = groupingCount2;
                this.multiplier = multpl;
                boolean z = decimalPos == 0 || decimalPos == digitTotalCount;
                setDecimalSeparatorAlwaysShown(z);
                if (padPos >= 0) {
                    this.padPosition = padPos;
                    this.formatWidth = sub0Limit - sub0Start;
                    this.pad = padChar;
                } else {
                    this.formatWidth = 0;
                }
                if (incrementVal != 0) {
                    int scale = incrementPos - effectiveDecimalPos;
                    this.roundingIncrementICU = android.icu.math.BigDecimal.valueOf(incrementVal, scale > 0 ? scale : 0);
                    if (scale < 0) {
                        this.roundingIncrementICU = this.roundingIncrementICU.movePointRight(-scale);
                    }
                    this.roundingMode = 6;
                } else {
                    setRoundingIncrement((android.icu.math.BigDecimal) null);
                }
                this.currencySignCount = currencySignCnt;
            } else {
                this.negPrefixPattern = prefix.toString();
                this.negSuffixPattern = suffix.toString();
                gotNegative = true;
            }
            part++;
        }
        if (pattern.length() == 0) {
            stringBuilder = "";
            this.posSuffixPattern = stringBuilder;
            this.posPrefixPattern = stringBuilder;
            setMinimumIntegerDigits(0);
            setMaximumIntegerDigits(DOUBLE_INTEGER_DIGITS);
            setMinimumFractionDigits(0);
            _setMaximumFractionDigits(DOUBLE_FRACTION_DIGITS);
        }
        if (!gotNegative || (this.negPrefixPattern.equals(this.posPrefixPattern) && this.negSuffixPattern.equals(this.posSuffixPattern))) {
            this.negSuffixPattern = this.posSuffixPattern;
            this.negPrefixPattern = PATTERN_MINUS + this.posPrefixPattern;
        }
        setLocale(null, null);
        this.formatPattern = pattern;
        if (this.currencySignCount != 0) {
            Currency theCurrency = getCurrency();
            if (theCurrency != null) {
                setRoundingIncrement(theCurrency.getRoundingIncrement(this.currencyUsage));
                int d = theCurrency.getDefaultFractionDigits(this.currencyUsage);
                setMinimumFractionDigits(d);
                _setMaximumFractionDigits(d);
            }
            if (this.currencySignCount == 3 && this.currencyPluralInfo == null) {
                this.currencyPluralInfo = new CurrencyPluralInfo(this.symbols.getULocale());
            }
        }
        resetActualRounding();
    }

    private void patternError(String msg, String pattern) {
        throw new IllegalArgumentException(msg + " in pattern \"" + pattern + '\"');
    }

    public void setMaximumIntegerDigits(int newValue) {
        super.setMaximumIntegerDigits(Math.min(newValue, MAX_INTEGER_DIGITS));
    }

    public void setMinimumIntegerDigits(int newValue) {
        super.setMinimumIntegerDigits(Math.min(newValue, DOUBLE_INTEGER_DIGITS));
    }

    public int getMinimumSignificantDigits() {
        return this.minSignificantDigits;
    }

    public int getMaximumSignificantDigits() {
        return this.maxSignificantDigits;
    }

    public void setMinimumSignificantDigits(int min) {
        if (min < 1) {
            min = 1;
        }
        int max = Math.max(this.maxSignificantDigits, min);
        this.minSignificantDigits = min;
        this.maxSignificantDigits = max;
        setSignificantDigitsUsed(true);
    }

    public void setMaximumSignificantDigits(int max) {
        if (max < 1) {
            max = 1;
        }
        this.minSignificantDigits = Math.min(this.minSignificantDigits, max);
        this.maxSignificantDigits = max;
        setSignificantDigitsUsed(true);
    }

    public boolean areSignificantDigitsUsed() {
        return this.useSignificantDigits;
    }

    public void setSignificantDigitsUsed(boolean useSignificantDigits) {
        this.useSignificantDigits = useSignificantDigits;
    }

    public void setCurrency(Currency theCurrency) {
        super.setCurrency(theCurrency);
        if (theCurrency != null) {
            String s = theCurrency.getName(this.symbols.getULocale(), 0, new boolean[1]);
            this.symbols.setCurrency(theCurrency);
            this.symbols.setCurrencySymbol(s);
        }
        if (this.currencySignCount != 0) {
            if (theCurrency != null) {
                setRoundingIncrement(theCurrency.getRoundingIncrement(this.currencyUsage));
                int d = theCurrency.getDefaultFractionDigits(this.currencyUsage);
                setMinimumFractionDigits(d);
                setMaximumFractionDigits(d);
            }
            if (this.currencySignCount != 3) {
                expandAffixes(null);
            }
        }
    }

    public void setCurrencyUsage(CurrencyUsage newUsage) {
        if (newUsage == null) {
            throw new NullPointerException("return value is null at method AAA");
        }
        this.currencyUsage = newUsage;
        Currency theCurrency = getCurrency();
        if (theCurrency != null) {
            setRoundingIncrement(theCurrency.getRoundingIncrement(this.currencyUsage));
            int d = theCurrency.getDefaultFractionDigits(this.currencyUsage);
            setMinimumFractionDigits(d);
            _setMaximumFractionDigits(d);
        }
    }

    public CurrencyUsage getCurrencyUsage() {
        return this.currencyUsage;
    }

    @Deprecated
    protected Currency getEffectiveCurrency() {
        Currency c = getCurrency();
        if (c == null) {
            return Currency.getInstance(this.symbols.getInternationalCurrencySymbol());
        }
        return c;
    }

    public void setMaximumFractionDigits(int newValue) {
        _setMaximumFractionDigits(newValue);
        resetActualRounding();
    }

    private void _setMaximumFractionDigits(int newValue) {
        super.setMaximumFractionDigits(Math.min(newValue, DOUBLE_FRACTION_DIGITS));
    }

    public void setMinimumFractionDigits(int newValue) {
        super.setMinimumFractionDigits(Math.min(newValue, DOUBLE_FRACTION_DIGITS));
    }

    public void setParseBigDecimal(boolean value) {
        this.parseBigDecimal = value;
    }

    public boolean isParseBigDecimal() {
        return this.parseBigDecimal;
    }

    public void setParseMaxDigits(int newValue) {
        if (newValue > 0) {
            this.PARSE_MAX_EXPONENT = newValue;
        }
    }

    public int getParseMaxDigits() {
        return this.PARSE_MAX_EXPONENT;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        this.attributes.clear();
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (getMaximumIntegerDigits() > MAX_INTEGER_DIGITS) {
            setMaximumIntegerDigits(MAX_INTEGER_DIGITS);
        }
        if (getMaximumFractionDigits() > DOUBLE_FRACTION_DIGITS) {
            _setMaximumFractionDigits(DOUBLE_FRACTION_DIGITS);
        }
        if (this.serialVersionOnStream < 2) {
            this.exponentSignAlwaysShown = false;
            setInternalRoundingIncrement(null);
            this.roundingMode = 6;
            this.formatWidth = 0;
            this.pad = ' ';
            this.padPosition = 0;
            if (this.serialVersionOnStream < 1) {
                this.useExponentialNotation = false;
            }
        }
        if (this.serialVersionOnStream < 3) {
            setCurrencyForSymbols();
        }
        if (this.serialVersionOnStream < 4) {
            this.currencyUsage = CurrencyUsage.STANDARD;
        }
        this.serialVersionOnStream = 4;
        this.digitList = new DigitList();
        if (this.roundingIncrement != null) {
            setInternalRoundingIncrement(new android.icu.math.BigDecimal(this.roundingIncrement));
        }
        resetActualRounding();
    }

    private void setInternalRoundingIncrement(android.icu.math.BigDecimal value) {
        BigDecimal bigDecimal = null;
        this.roundingIncrementICU = value;
        if (value != null) {
            bigDecimal = value.toBigDecimal();
        }
        this.roundingIncrement = bigDecimal;
    }

    private void resetActualRounding() {
        if (this.roundingIncrementICU != null) {
            android.icu.math.BigDecimal byWidth = getMaximumFractionDigits() > 0 ? android.icu.math.BigDecimal.ONE.movePointLeft(getMaximumFractionDigits()) : android.icu.math.BigDecimal.ONE;
            if (this.roundingIncrementICU.compareTo(byWidth) >= 0) {
                this.actualRoundingIncrementICU = this.roundingIncrementICU;
            } else {
                if (byWidth.equals(android.icu.math.BigDecimal.ONE)) {
                    byWidth = null;
                }
                this.actualRoundingIncrementICU = byWidth;
            }
        } else if (this.roundingMode == 6 || isScientificNotation()) {
            this.actualRoundingIncrementICU = null;
        } else if (getMaximumFractionDigits() > 0) {
            this.actualRoundingIncrementICU = android.icu.math.BigDecimal.ONE.movePointLeft(getMaximumFractionDigits());
        } else {
            this.actualRoundingIncrementICU = android.icu.math.BigDecimal.ONE;
        }
        if (this.actualRoundingIncrementICU == null) {
            setRoundingDouble(0.0d);
            this.actualRoundingIncrement = null;
            return;
        }
        setRoundingDouble(this.actualRoundingIncrementICU.doubleValue());
        this.actualRoundingIncrement = this.actualRoundingIncrementICU.toBigDecimal();
    }

    private void setRoundingDouble(double newValue) {
        this.roundingDouble = newValue;
        if (this.roundingDouble > 0.0d) {
            double rawRoundedReciprocal = 1.0d / this.roundingDouble;
            this.roundingDoubleReciprocal = Math.rint(rawRoundedReciprocal);
            if (Math.abs(rawRoundedReciprocal - this.roundingDoubleReciprocal) > roundingIncrementEpsilon) {
                this.roundingDoubleReciprocal = 0.0d;
                return;
            }
            return;
        }
        this.roundingDoubleReciprocal = 0.0d;
    }
}
