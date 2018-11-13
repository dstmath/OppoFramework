package android.icu.text;

import android.icu.impl.ICUResourceBundle;
import android.icu.math.BigDecimal;
import android.icu.text.DisplayContext.Type;
import android.icu.util.Currency;
import android.icu.util.Currency.CurrencyUsage;
import android.icu.util.CurrencyAmount;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class NumberFormat extends UFormat {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f8-assertionsDisabled = false;
    public static final int ACCOUNTINGCURRENCYSTYLE = 7;
    public static final int CASHCURRENCYSTYLE = 8;
    public static final int CURRENCYSTYLE = 1;
    public static final int FRACTION_FIELD = 1;
    public static final int INTEGERSTYLE = 4;
    public static final int INTEGER_FIELD = 0;
    public static final int ISOCURRENCYSTYLE = 5;
    public static final int NUMBERSTYLE = 0;
    public static final int PERCENTSTYLE = 2;
    public static final int PLURALCURRENCYSTYLE = 6;
    public static final int SCIENTIFICSTYLE = 3;
    public static final int STANDARDCURRENCYSTYLE = 9;
    static final int currentSerialVersion = 2;
    private static final char[] doubleCurrencySign = null;
    private static final String doubleCurrencyStr = null;
    private static final long serialVersionUID = -2308460125733713944L;
    private static NumberFormatShim shim;
    private DisplayContext capitalizationSetting;
    private Currency currency;
    private boolean groupingUsed;
    private byte maxFractionDigits;
    private byte maxIntegerDigits;
    private int maximumFractionDigits;
    private int maximumIntegerDigits;
    private byte minFractionDigits;
    private byte minIntegerDigits;
    private int minimumFractionDigits;
    private int minimumIntegerDigits;
    private boolean parseIntegerOnly;
    private boolean parseStrict;
    private int serialVersionOnStream;

    public static class Field extends java.text.Format.Field {
        public static final Field CURRENCY = null;
        public static final Field DECIMAL_SEPARATOR = null;
        public static final Field EXPONENT = null;
        public static final Field EXPONENT_SIGN = null;
        public static final Field EXPONENT_SYMBOL = null;
        public static final Field FRACTION = null;
        public static final Field GROUPING_SEPARATOR = null;
        public static final Field INTEGER = null;
        public static final Field PERCENT = null;
        public static final Field PERMILLE = null;
        public static final Field SIGN = null;
        static final long serialVersionUID = -4516273749929385842L;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.NumberFormat.Field.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.NumberFormat.Field.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NumberFormat.Field.<clinit>():void");
        }

        protected Field(String fieldName) {
            super(fieldName);
        }

        protected Object readResolve() throws InvalidObjectException {
            if (getName().equals(INTEGER.getName())) {
                return INTEGER;
            }
            if (getName().equals(FRACTION.getName())) {
                return FRACTION;
            }
            if (getName().equals(EXPONENT.getName())) {
                return EXPONENT;
            }
            if (getName().equals(EXPONENT_SIGN.getName())) {
                return EXPONENT_SIGN;
            }
            if (getName().equals(EXPONENT_SYMBOL.getName())) {
                return EXPONENT_SYMBOL;
            }
            if (getName().equals(CURRENCY.getName())) {
                return CURRENCY;
            }
            if (getName().equals(DECIMAL_SEPARATOR.getName())) {
                return DECIMAL_SEPARATOR;
            }
            if (getName().equals(GROUPING_SEPARATOR.getName())) {
                return GROUPING_SEPARATOR;
            }
            if (getName().equals(PERCENT.getName())) {
                return PERCENT;
            }
            if (getName().equals(PERMILLE.getName())) {
                return PERMILLE;
            }
            if (getName().equals(SIGN.getName())) {
                return SIGN;
            }
            throw new InvalidObjectException("An invalid object.");
        }
    }

    public static abstract class NumberFormatFactory {
        public static final int FORMAT_CURRENCY = 1;
        public static final int FORMAT_INTEGER = 4;
        public static final int FORMAT_NUMBER = 0;
        public static final int FORMAT_PERCENT = 2;
        public static final int FORMAT_SCIENTIFIC = 3;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.NumberFormat.NumberFormatFactory.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected NumberFormatFactory() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.NumberFormat.NumberFormatFactory.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NumberFormat.NumberFormatFactory.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.text.NumberFormat.NumberFormatFactory.createFormat(android.icu.util.ULocale, int):android.icu.text.NumberFormat, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public android.icu.text.NumberFormat createFormat(android.icu.util.ULocale r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.text.NumberFormat.NumberFormatFactory.createFormat(android.icu.util.ULocale, int):android.icu.text.NumberFormat, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NumberFormat.NumberFormatFactory.createFormat(android.icu.util.ULocale, int):android.icu.text.NumberFormat");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.text.NumberFormat.NumberFormatFactory.createFormat(java.util.Locale, int):android.icu.text.NumberFormat, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public android.icu.text.NumberFormat createFormat(java.util.Locale r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.text.NumberFormat.NumberFormatFactory.createFormat(java.util.Locale, int):android.icu.text.NumberFormat, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NumberFormat.NumberFormatFactory.createFormat(java.util.Locale, int):android.icu.text.NumberFormat");
        }

        public abstract Set<String> getSupportedLocaleNames();

        public boolean visible() {
            return true;
        }
    }

    static abstract class NumberFormatShim {
        abstract NumberFormat createInstance(ULocale uLocale, int i);

        abstract Locale[] getAvailableLocales();

        abstract ULocale[] getAvailableULocales();

        abstract Object registerFactory(NumberFormatFactory numberFormatFactory);

        abstract boolean unregister(Object obj);

        NumberFormatShim() {
        }
    }

    public static abstract class SimpleNumberFormatFactory extends NumberFormatFactory {
        final Set<String> localeNames;
        final boolean visible;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.text.NumberFormat.SimpleNumberFormatFactory.<init>(android.icu.util.ULocale, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public SimpleNumberFormatFactory(android.icu.util.ULocale r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.text.NumberFormat.SimpleNumberFormatFactory.<init>(android.icu.util.ULocale, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NumberFormat.SimpleNumberFormatFactory.<init>(android.icu.util.ULocale, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.text.NumberFormat.SimpleNumberFormatFactory.<init>(java.util.Locale, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public SimpleNumberFormatFactory(java.util.Locale r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.text.NumberFormat.SimpleNumberFormatFactory.<init>(java.util.Locale, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NumberFormat.SimpleNumberFormatFactory.<init>(java.util.Locale, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.NumberFormat.SimpleNumberFormatFactory.getSupportedLocaleNames():java.util.Set<java.lang.String>, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public final java.util.Set<java.lang.String> getSupportedLocaleNames() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.NumberFormat.SimpleNumberFormatFactory.getSupportedLocaleNames():java.util.Set<java.lang.String>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NumberFormat.SimpleNumberFormatFactory.getSupportedLocaleNames():java.util.Set<java.lang.String>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.icu.text.NumberFormat.SimpleNumberFormatFactory.visible():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public final boolean visible() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.icu.text.NumberFormat.SimpleNumberFormatFactory.visible():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NumberFormat.SimpleNumberFormatFactory.visible():boolean");
        }

        public SimpleNumberFormatFactory(Locale locale) {
            this(locale, true);
        }

        public SimpleNumberFormatFactory(ULocale locale) {
            this(locale, true);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.text.NumberFormat.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.text.NumberFormat.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NumberFormat.<clinit>():void");
    }

    public abstract StringBuffer format(double d, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(long j, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(BigDecimal bigDecimal, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(java.math.BigDecimal bigDecimal, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(BigInteger bigInteger, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract Number parse(String str, ParsePosition parsePosition);

    public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
        if (number instanceof Long) {
            return format(((Long) number).longValue(), toAppendTo, pos);
        }
        if (number instanceof BigInteger) {
            return format((BigInteger) number, toAppendTo, pos);
        }
        if (number instanceof java.math.BigDecimal) {
            return format((java.math.BigDecimal) number, toAppendTo, pos);
        }
        if (number instanceof BigDecimal) {
            return format((BigDecimal) number, toAppendTo, pos);
        }
        if (number instanceof CurrencyAmount) {
            return format((CurrencyAmount) number, toAppendTo, pos);
        }
        if (number instanceof Number) {
            return format(((Number) number).doubleValue(), toAppendTo, pos);
        }
        throw new IllegalArgumentException("Cannot format given Object as a Number");
    }

    public final Object parseObject(String source, ParsePosition parsePosition) {
        return parse(source, parsePosition);
    }

    public final String format(double number) {
        return format(number, new StringBuffer(), new FieldPosition(0)).toString();
    }

    public final String format(long number) {
        StringBuffer buf = new StringBuffer(19);
        format(number, buf, new FieldPosition(0));
        return buf.toString();
    }

    public final String format(BigInteger number) {
        return format(number, new StringBuffer(), new FieldPosition(0)).toString();
    }

    public final String format(java.math.BigDecimal number) {
        return format(number, new StringBuffer(), new FieldPosition(0)).toString();
    }

    public final String format(BigDecimal number) {
        return format(number, new StringBuffer(), new FieldPosition(0)).toString();
    }

    public final String format(CurrencyAmount currAmt) {
        return format(currAmt, new StringBuffer(), new FieldPosition(0)).toString();
    }

    public StringBuffer format(CurrencyAmount currAmt, StringBuffer toAppendTo, FieldPosition pos) {
        Currency save = getCurrency();
        Currency curr = currAmt.getCurrency();
        boolean same = curr.equals(save);
        if (!same) {
            setCurrency(curr);
        }
        format(currAmt.getNumber(), toAppendTo, pos);
        if (!same) {
            setCurrency(save);
        }
        return toAppendTo;
    }

    public Number parse(String text) throws ParseException {
        ParsePosition parsePosition = new ParsePosition(0);
        Number result = parse(text, parsePosition);
        if (parsePosition.getIndex() != 0) {
            return result;
        }
        throw new ParseException("Unparseable number: \"" + text + '\"', parsePosition.getErrorIndex());
    }

    public CurrencyAmount parseCurrency(CharSequence text, ParsePosition pos) {
        Number n = parse(text.toString(), pos);
        if (n == null) {
            return null;
        }
        return new CurrencyAmount(n, getEffectiveCurrency());
    }

    public boolean isParseIntegerOnly() {
        return this.parseIntegerOnly;
    }

    public void setParseIntegerOnly(boolean value) {
        this.parseIntegerOnly = value;
    }

    public void setParseStrict(boolean value) {
        this.parseStrict = value;
    }

    public boolean isParseStrict() {
        return this.parseStrict;
    }

    public void setContext(DisplayContext context) {
        if (context.type() == Type.CAPITALIZATION) {
            this.capitalizationSetting = context;
        }
    }

    public DisplayContext getContext(Type type) {
        return (type != Type.CAPITALIZATION || this.capitalizationSetting == null) ? DisplayContext.CAPITALIZATION_NONE : this.capitalizationSetting;
    }

    public static final NumberFormat getInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT), 0);
    }

    public static NumberFormat getInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale), 0);
    }

    public static NumberFormat getInstance(ULocale inLocale) {
        return getInstance(inLocale, 0);
    }

    public static final NumberFormat getInstance(int style) {
        return getInstance(ULocale.getDefault(Category.FORMAT), style);
    }

    public static NumberFormat getInstance(Locale inLocale, int style) {
        return getInstance(ULocale.forLocale(inLocale), style);
    }

    public static final NumberFormat getNumberInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT), 0);
    }

    public static NumberFormat getNumberInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale), 0);
    }

    public static NumberFormat getNumberInstance(ULocale inLocale) {
        return getInstance(inLocale, 0);
    }

    public static final NumberFormat getIntegerInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT), 4);
    }

    public static NumberFormat getIntegerInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale), 4);
    }

    public static NumberFormat getIntegerInstance(ULocale inLocale) {
        return getInstance(inLocale, 4);
    }

    public static final NumberFormat getCurrencyInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT), 1);
    }

    public static NumberFormat getCurrencyInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale), 1);
    }

    public static NumberFormat getCurrencyInstance(ULocale inLocale) {
        return getInstance(inLocale, 1);
    }

    public static final NumberFormat getPercentInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT), 2);
    }

    public static NumberFormat getPercentInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale), 2);
    }

    public static NumberFormat getPercentInstance(ULocale inLocale) {
        return getInstance(inLocale, 2);
    }

    public static final NumberFormat getScientificInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT), 3);
    }

    public static NumberFormat getScientificInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale), 3);
    }

    public static NumberFormat getScientificInstance(ULocale inLocale) {
        return getInstance(inLocale, 3);
    }

    private static NumberFormatShim getShim() {
        if (shim == null) {
            try {
                shim = (NumberFormatShim) Class.forName("android.icu.text.NumberFormatServiceShim").newInstance();
            } catch (MissingResourceException e) {
                throw e;
            } catch (Exception e2) {
                throw new RuntimeException(e2.getMessage());
            }
        }
        return shim;
    }

    public static Locale[] getAvailableLocales() {
        if (shim == null) {
            return ICUResourceBundle.getAvailableLocales();
        }
        return getShim().getAvailableLocales();
    }

    public static ULocale[] getAvailableULocales() {
        if (shim == null) {
            return ICUResourceBundle.getAvailableULocales();
        }
        return getShim().getAvailableULocales();
    }

    public static Object registerFactory(NumberFormatFactory factory) {
        if (factory != null) {
            return getShim().registerFactory(factory);
        }
        throw new IllegalArgumentException("factory must not be null");
    }

    public static boolean unregister(Object registryKey) {
        if (registryKey == null) {
            throw new IllegalArgumentException("registryKey must not be null");
        } else if (shim == null) {
            return false;
        } else {
            return shim.unregister(registryKey);
        }
    }

    public int hashCode() {
        return (this.maximumIntegerDigits * 37) + this.maxFractionDigits;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NumberFormat other = (NumberFormat) obj;
        if (this.maximumIntegerDigits != other.maximumIntegerDigits || this.minimumIntegerDigits != other.minimumIntegerDigits || this.maximumFractionDigits != other.maximumFractionDigits || this.minimumFractionDigits != other.minimumFractionDigits || this.groupingUsed != other.groupingUsed || this.parseIntegerOnly != other.parseIntegerOnly || this.parseStrict != other.parseStrict) {
            z = false;
        } else if (this.capitalizationSetting != other.capitalizationSetting) {
            z = false;
        }
        return z;
    }

    public Object clone() {
        return (NumberFormat) super.clone();
    }

    public boolean isGroupingUsed() {
        return this.groupingUsed;
    }

    public void setGroupingUsed(boolean newValue) {
        this.groupingUsed = newValue;
    }

    public int getMaximumIntegerDigits() {
        return this.maximumIntegerDigits;
    }

    public void setMaximumIntegerDigits(int newValue) {
        this.maximumIntegerDigits = Math.max(0, newValue);
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.minimumIntegerDigits = this.maximumIntegerDigits;
        }
    }

    public int getMinimumIntegerDigits() {
        return this.minimumIntegerDigits;
    }

    public void setMinimumIntegerDigits(int newValue) {
        this.minimumIntegerDigits = Math.max(0, newValue);
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.maximumIntegerDigits = this.minimumIntegerDigits;
        }
    }

    public int getMaximumFractionDigits() {
        return this.maximumFractionDigits;
    }

    public void setMaximumFractionDigits(int newValue) {
        this.maximumFractionDigits = Math.max(0, newValue);
        if (this.maximumFractionDigits < this.minimumFractionDigits) {
            this.minimumFractionDigits = this.maximumFractionDigits;
        }
    }

    public int getMinimumFractionDigits() {
        return this.minimumFractionDigits;
    }

    public void setMinimumFractionDigits(int newValue) {
        this.minimumFractionDigits = Math.max(0, newValue);
        if (this.maximumFractionDigits < this.minimumFractionDigits) {
            this.maximumFractionDigits = this.minimumFractionDigits;
        }
    }

    public void setCurrency(Currency theCurrency) {
        this.currency = theCurrency;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    @Deprecated
    protected Currency getEffectiveCurrency() {
        Currency c = getCurrency();
        if (c != null) {
            return c;
        }
        ULocale uloc = getLocale(ULocale.VALID_LOCALE);
        if (uloc == null) {
            uloc = ULocale.getDefault(Category.FORMAT);
        }
        return Currency.getInstance(uloc);
    }

    public int getRoundingMode() {
        throw new UnsupportedOperationException("getRoundingMode must be implemented by the subclass implementation.");
    }

    public void setRoundingMode(int roundingMode) {
        throw new UnsupportedOperationException("setRoundingMode must be implemented by the subclass implementation.");
    }

    public static NumberFormat getInstance(ULocale desiredLocale, int choice) {
        if (choice >= 0 && choice <= 9) {
            return getShim().createInstance(desiredLocale, choice);
        }
        throw new IllegalArgumentException("choice should be from NUMBERSTYLE to STANDARDCURRENCYSTYLE");
    }

    static NumberFormat createInstance(ULocale desiredLocale, int choice) {
        String pattern = getPattern(desiredLocale, choice);
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(desiredLocale);
        if (choice == 1 || choice == 5 || choice == 7 || choice == 8 || choice == 9) {
            String temp = decimalFormatSymbols.getCurrencyPattern();
            if (temp != null) {
                pattern = temp;
            }
        }
        if (choice == 5) {
            pattern = pattern.replace("¤", doubleCurrencyStr);
        }
        NumberingSystem ns = NumberingSystem.getInstance(desiredLocale);
        if (ns == null) {
            return null;
        }
        NumberFormat format;
        if (ns == null || !ns.isAlgorithmic()) {
            NumberFormat f = new DecimalFormat(pattern, decimalFormatSymbols, choice);
            if (choice == 4) {
                f.setMaximumFractionDigits(0);
                f.setDecimalSeparatorAlwaysShown(false);
                f.setParseIntegerOnly(true);
            }
            if (choice == 8) {
                f.setCurrencyUsage(CurrencyUsage.CASH);
            }
            format = f;
        } else {
            String nsRuleSetName;
            ULocale nsLoc;
            int desiredRulesType = 4;
            String nsDesc = ns.getDescription();
            int firstSlash = nsDesc.indexOf("/");
            int lastSlash = nsDesc.lastIndexOf("/");
            if (lastSlash > firstSlash) {
                String nsLocID = nsDesc.substring(0, firstSlash);
                String nsRuleSetGroup = nsDesc.substring(firstSlash + 1, lastSlash);
                nsRuleSetName = nsDesc.substring(lastSlash + 1);
                nsLoc = new ULocale(nsLocID);
                if (nsRuleSetGroup.equals("SpelloutRules")) {
                    desiredRulesType = 1;
                }
            } else {
                nsLoc = desiredLocale;
                nsRuleSetName = nsDesc;
            }
            NumberFormat r = new RuleBasedNumberFormat(nsLoc, desiredRulesType);
            r.setDefaultRuleSet(nsRuleSetName);
            format = r;
        }
        format.setLocale(decimalFormatSymbols.getLocale(ULocale.VALID_LOCALE), decimalFormatSymbols.getLocale(ULocale.ACTUAL_LOCALE));
        return format;
    }

    @Deprecated
    protected static String getPattern(Locale forLocale, int choice) {
        return getPattern(ULocale.forLocale(forLocale), choice);
    }

    protected static String getPattern(ULocale forLocale, int choice) {
        String patternKey;
        switch (choice) {
            case 0:
            case 4:
                patternKey = "decimalFormat";
                break;
            case 1:
                String cfKeyValue = forLocale.getKeywordValue("cf");
                if (cfKeyValue != null && cfKeyValue.equals("account")) {
                    patternKey = "accountingFormat";
                    break;
                }
                patternKey = "currencyFormat";
                break;
            case 2:
                patternKey = "percentFormat";
                break;
            case 3:
                patternKey = "scientificFormat";
                break;
            case 5:
            case 6:
            case 8:
            case 9:
                patternKey = "currencyFormat";
                break;
            case 7:
                patternKey = "accountingFormat";
                break;
            default:
                if (f8-assertionsDisabled) {
                    patternKey = "decimalFormat";
                    break;
                }
                throw new AssertionError();
        }
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", forLocale);
        try {
            return rb.getStringWithFallback("NumberElements/" + NumberingSystem.getInstance(forLocale).getName() + "/patterns/" + patternKey);
        } catch (MissingResourceException e) {
            return rb.getStringWithFallback("NumberElements/latn/patterns/" + patternKey);
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.serialVersionOnStream < 1) {
            this.maximumIntegerDigits = this.maxIntegerDigits;
            this.minimumIntegerDigits = this.minIntegerDigits;
            this.maximumFractionDigits = this.maxFractionDigits;
            this.minimumFractionDigits = this.minFractionDigits;
        }
        if (this.serialVersionOnStream < 2) {
            this.capitalizationSetting = DisplayContext.CAPITALIZATION_NONE;
        }
        if (this.minimumIntegerDigits > this.maximumIntegerDigits || this.minimumFractionDigits > this.maximumFractionDigits || this.minimumIntegerDigits < 0 || this.minimumFractionDigits < 0) {
            throw new InvalidObjectException("Digit count range invalid");
        }
        this.serialVersionOnStream = 2;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        byte b = Bidi.LEVEL_DEFAULT_RTL;
        this.maxIntegerDigits = this.maximumIntegerDigits > 127 ? Bidi.LEVEL_DEFAULT_RTL : (byte) this.maximumIntegerDigits;
        this.minIntegerDigits = this.minimumIntegerDigits > 127 ? Bidi.LEVEL_DEFAULT_RTL : (byte) this.minimumIntegerDigits;
        this.maxFractionDigits = this.maximumFractionDigits > 127 ? Bidi.LEVEL_DEFAULT_RTL : (byte) this.maximumFractionDigits;
        if (this.minimumFractionDigits <= 127) {
            b = (byte) this.minimumFractionDigits;
        }
        this.minFractionDigits = b;
        stream.defaultWriteObject();
    }

    public NumberFormat() {
        this.groupingUsed = true;
        this.maxIntegerDigits = (byte) 40;
        this.minIntegerDigits = (byte) 1;
        this.maxFractionDigits = (byte) 3;
        this.minFractionDigits = (byte) 0;
        this.parseIntegerOnly = false;
        this.maximumIntegerDigits = 40;
        this.minimumIntegerDigits = 1;
        this.maximumFractionDigits = 3;
        this.minimumFractionDigits = 0;
        this.serialVersionOnStream = 2;
        this.capitalizationSetting = DisplayContext.CAPITALIZATION_NONE;
    }
}
