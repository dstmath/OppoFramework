package java.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.sql.Types;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale.Category;
import libcore.icu.LocaleData;
import sun.misc.FormattedFloatingDecimal;
import sun.misc.FormattedFloatingDecimal.Form;
import sun.misc.FpUtils;
import sun.net.www.protocol.http.AuthenticationInfo;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public final class Formatter implements Closeable, Flushable {
    private static final int MAX_FD_CHARS = 30;
    private static double scaleUp;
    private Appendable a;
    private final Locale l;
    private IOException lastException;
    private final char zero;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
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
    public enum BigDecimalLayoutForm {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.Formatter.BigDecimalLayoutForm.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.Formatter.BigDecimalLayoutForm.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.Formatter.BigDecimalLayoutForm.<clinit>():void");
        }
    }

    private static class Conversion {
        static final char BOOLEAN = 'b';
        static final char BOOLEAN_UPPER = 'B';
        static final char CHARACTER = 'c';
        static final char CHARACTER_UPPER = 'C';
        static final char DATE_TIME = 't';
        static final char DATE_TIME_UPPER = 'T';
        static final char DECIMAL_FLOAT = 'f';
        static final char DECIMAL_INTEGER = 'd';
        static final char GENERAL = 'g';
        static final char GENERAL_UPPER = 'G';
        static final char HASHCODE = 'h';
        static final char HASHCODE_UPPER = 'H';
        static final char HEXADECIMAL_FLOAT = 'a';
        static final char HEXADECIMAL_FLOAT_UPPER = 'A';
        static final char HEXADECIMAL_INTEGER = 'x';
        static final char HEXADECIMAL_INTEGER_UPPER = 'X';
        static final char LINE_SEPARATOR = 'n';
        static final char OCTAL_INTEGER = 'o';
        static final char PERCENT_SIGN = '%';
        static final char SCIENTIFIC = 'e';
        static final char SCIENTIFIC_UPPER = 'E';
        static final char STRING = 's';
        static final char STRING_UPPER = 'S';

        private Conversion() {
        }

        static boolean isValid(char c) {
            if (isGeneral(c) || isInteger(c) || isFloat(c) || isText(c) || c == DATE_TIME) {
                return true;
            }
            return isCharacter(c);
        }

        static boolean isGeneral(char c) {
            switch (c) {
                case 'B':
                case 'H':
                case 'S':
                case 'b':
                case 'h':
                case 's':
                    return true;
                default:
                    return false;
            }
        }

        static boolean isCharacter(char c) {
            switch (c) {
                case 'C':
                case 'c':
                    return true;
                default:
                    return false;
            }
        }

        static boolean isInteger(char c) {
            switch (c) {
                case 'X':
                case 'd':
                case 'o':
                case 'x':
                    return true;
                default:
                    return false;
            }
        }

        static boolean isFloat(char c) {
            switch (c) {
                case 'A':
                case 'E':
                case 'G':
                case 'a':
                case 'e':
                case 'f':
                case 'g':
                    return true;
                default:
                    return false;
            }
        }

        static boolean isText(char c) {
            switch (c) {
                case '%':
                case 'n':
                    return true;
                default:
                    return false;
            }
        }
    }

    private static class DateTime {
        static final char AM_PM = 'p';
        static final char CENTURY = 'C';
        static final char DATE = 'D';
        static final char DATE_TIME = 'c';
        static final char DAY_OF_MONTH = 'e';
        static final char DAY_OF_MONTH_0 = 'd';
        static final char DAY_OF_YEAR = 'j';
        static final char HOUR = 'l';
        static final char HOUR_0 = 'I';
        static final char HOUR_OF_DAY = 'k';
        static final char HOUR_OF_DAY_0 = 'H';
        static final char ISO_STANDARD_DATE = 'F';
        static final char MILLISECOND = 'L';
        static final char MILLISECOND_SINCE_EPOCH = 'Q';
        static final char MINUTE = 'M';
        static final char MONTH = 'm';
        static final char NAME_OF_DAY = 'A';
        static final char NAME_OF_DAY_ABBREV = 'a';
        static final char NAME_OF_MONTH = 'B';
        static final char NAME_OF_MONTH_ABBREV = 'b';
        static final char NAME_OF_MONTH_ABBREV_X = 'h';
        static final char NANOSECOND = 'N';
        static final char SECOND = 'S';
        static final char SECONDS_SINCE_EPOCH = 's';
        static final char TIME = 'T';
        static final char TIME_12_HOUR = 'r';
        static final char TIME_24_HOUR = 'R';
        static final char YEAR_2 = 'y';
        static final char YEAR_4 = 'Y';
        static final char ZONE = 'Z';
        static final char ZONE_NUMERIC = 'z';

        private DateTime() {
        }

        static boolean isValid(char c) {
            switch (c) {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case Types.DATALINK /*70*/:
                case 'H':
                case 'I':
                case 'L':
                case 'M':
                case 'N':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'h':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'p':
                case 'r':
                case 's':
                case 'y':
                case 'z':
                    return true;
                default:
                    return false;
            }
        }
    }

    private interface FormatString {
        int index();

        void print(Object obj, Locale locale) throws IOException;

        String toString();
    }

    private class FixedString implements FormatString {
        private String s;

        FixedString(String s) {
            this.s = s;
        }

        public int index() {
            return -2;
        }

        public void print(Object arg, Locale l) throws IOException {
            Formatter.this.a.append(this.s);
        }

        public String toString() {
            return this.s;
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
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
    private static class Flags {
        static final Flags ALTERNATE = null;
        static final Flags GROUP = null;
        static final Flags LEADING_SPACE = null;
        static final Flags LEFT_JUSTIFY = null;
        static final Flags NONE = null;
        static final Flags PARENTHESES = null;
        static final Flags PLUS = null;
        static final Flags PREVIOUS = null;
        static final Flags UPPERCASE = null;
        static final Flags ZERO_PAD = null;
        private int flags;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.Formatter.Flags.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.Formatter.Flags.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.Formatter.Flags.<clinit>():void");
        }

        private Flags(int f) {
            this.flags = f;
        }

        public int valueOf() {
            return this.flags;
        }

        public boolean contains(Flags f) {
            return (this.flags & f.valueOf()) == f.valueOf();
        }

        public Flags dup() {
            return new Flags(this.flags);
        }

        private Flags add(Flags f) {
            this.flags |= f.valueOf();
            return this;
        }

        public Flags remove(Flags f) {
            this.flags &= ~f.valueOf();
            return this;
        }

        public static Flags parse(String s) {
            char[] ca = s.toCharArray();
            Flags f = new Flags(0);
            for (char parse : ca) {
                Flags v = parse(parse);
                if (f.contains(v)) {
                    throw new DuplicateFormatFlagsException(v.toString());
                }
                f.add(v);
            }
            return f;
        }

        private static Flags parse(char c) {
            switch (c) {
                case ' ':
                    return LEADING_SPACE;
                case '#':
                    return ALTERNATE;
                case '(':
                    return PARENTHESES;
                case '+':
                    return PLUS;
                case ',':
                    return GROUP;
                case '-':
                    return LEFT_JUSTIFY;
                case '0':
                    return ZERO_PAD;
                case '<':
                    return PREVIOUS;
                default:
                    throw new UnknownFormatFlagsException(String.valueOf(c));
            }
        }

        public static String toString(Flags f) {
            return f.toString();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (contains(LEFT_JUSTIFY)) {
                sb.append('-');
            }
            if (contains(UPPERCASE)) {
                sb.append('^');
            }
            if (contains(ALTERNATE)) {
                sb.append('#');
            }
            if (contains(PLUS)) {
                sb.append('+');
            }
            if (contains(LEADING_SPACE)) {
                sb.append(' ');
            }
            if (contains(ZERO_PAD)) {
                sb.append('0');
            }
            if (contains(GROUP)) {
                sb.append(',');
            }
            if (contains(PARENTHESES)) {
                sb.append('(');
            }
            if (contains(PREVIOUS)) {
                sb.append('<');
            }
            return sb.toString();
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
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
    private class FormatSpecifier implements FormatString {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f11-assertionsDisabled = false;
        final /* synthetic */ boolean $assertionsDisabled;
        private char c;
        private boolean dt;
        private Flags f;
        private int index;
        private int precision;
        final /* synthetic */ Formatter this$0;
        private int width;

        private class BigDecimalLayout {
            private boolean dot;
            private StringBuilder exp;
            private StringBuilder mant;
            private int scale;
            final /* synthetic */ FormatSpecifier this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.<init>(java.util.Formatter$FormatSpecifier, java.math.BigInteger, int, java.util.Formatter$BigDecimalLayoutForm):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public BigDecimalLayout(java.util.Formatter.FormatSpecifier r1, java.math.BigInteger r2, int r3, java.util.Formatter.BigDecimalLayoutForm r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.<init>(java.util.Formatter$FormatSpecifier, java.math.BigInteger, int, java.util.Formatter$BigDecimalLayoutForm):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.Formatter.FormatSpecifier.BigDecimalLayout.<init>(java.util.Formatter$FormatSpecifier, java.math.BigInteger, int, java.util.Formatter$BigDecimalLayoutForm):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.layout(java.math.BigInteger, int, java.util.Formatter$BigDecimalLayoutForm):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            private void layout(java.math.BigInteger r1, int r2, java.util.Formatter.BigDecimalLayoutForm r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.layout(java.math.BigInteger, int, java.util.Formatter$BigDecimalLayoutForm):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.Formatter.FormatSpecifier.BigDecimalLayout.layout(java.math.BigInteger, int, java.util.Formatter$BigDecimalLayoutForm):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.toCharArray(java.lang.StringBuilder):char[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            private char[] toCharArray(java.lang.StringBuilder r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.toCharArray(java.lang.StringBuilder):char[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.Formatter.FormatSpecifier.BigDecimalLayout.toCharArray(java.lang.StringBuilder):char[]");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.exponent():char[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public char[] exponent() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.exponent():char[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.Formatter.FormatSpecifier.BigDecimalLayout.exponent():char[]");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.hasDot():boolean, dex:  in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.hasDot():boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.hasDot():boolean, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 11 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
                	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 12 more
                */
            public boolean hasDot() {
                /*
                // Can't load method instructions: Load method exception: null in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.hasDot():boolean, dex:  in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.hasDot():boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.Formatter.FormatSpecifier.BigDecimalLayout.hasDot():boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.layoutChars():char[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public char[] layoutChars() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.layoutChars():char[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.Formatter.FormatSpecifier.BigDecimalLayout.layoutChars():char[]");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.mantissa():char[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public char[] mantissa() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.mantissa():char[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.Formatter.FormatSpecifier.BigDecimalLayout.mantissa():char[]");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.scale():int, dex:  in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.scale():int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.scale():int, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 11 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
                	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 12 more
                */
            public int scale() {
                /*
                // Can't load method instructions: Load method exception: null in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.scale():int, dex:  in method: java.util.Formatter.FormatSpecifier.BigDecimalLayout.scale():int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.Formatter.FormatSpecifier.BigDecimalLayout.scale():int");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.Formatter.FormatSpecifier.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.Formatter.FormatSpecifier.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.Formatter.FormatSpecifier.<clinit>():void");
        }

        private int index(String s) {
            if (s != null) {
                try {
                    this.index = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    if (!f11-assertionsDisabled) {
                        throw new AssertionError();
                    }
                }
            }
            this.index = 0;
            return this.index;
        }

        public int index() {
            return this.index;
        }

        private Flags flags(String s) {
            this.f = Flags.parse(s);
            if (this.f.contains(Flags.PREVIOUS)) {
                this.index = -1;
            }
            return this.f;
        }

        Flags flags() {
            return this.f;
        }

        private int width(String s) {
            this.width = -1;
            if (s != null) {
                try {
                    this.width = Integer.parseInt(s);
                    if (this.width < 0) {
                        throw new IllegalFormatWidthException(this.width);
                    }
                } catch (NumberFormatException e) {
                    if (!f11-assertionsDisabled) {
                        throw new AssertionError();
                    }
                }
            }
            return this.width;
        }

        int width() {
            return this.width;
        }

        private int precision(String s) {
            this.precision = -1;
            if (s != null) {
                try {
                    this.precision = Integer.parseInt(s);
                    if (this.precision < 0) {
                        throw new IllegalFormatPrecisionException(this.precision);
                    }
                } catch (NumberFormatException e) {
                    if (!f11-assertionsDisabled) {
                        throw new AssertionError();
                    }
                }
            }
            return this.precision;
        }

        int precision() {
            return this.precision;
        }

        private char conversion(String s) {
            this.c = s.charAt(0);
            if (!this.dt) {
                if (Conversion.isValid(this.c)) {
                    if (Character.isUpperCase(this.c)) {
                        this.f.add(Flags.UPPERCASE);
                    }
                    this.c = Character.toLowerCase(this.c);
                    if (Conversion.isText(this.c)) {
                        this.index = -2;
                    }
                } else {
                    throw new UnknownFormatConversionException(String.valueOf(this.c));
                }
            }
            return this.c;
        }

        private char conversion() {
            return this.c;
        }

        FormatSpecifier(Formatter this$0, String indexStr, String flagsStr, String widthStr, String precisionStr, String tTStr, String convStr) {
            this.this$0 = this$0;
            this.index = -1;
            this.f = Flags.NONE;
            this.dt = false;
            index(indexStr);
            flags(flagsStr);
            width(widthStr);
            precision(precisionStr);
            if (tTStr != null) {
                this.dt = true;
                if (tTStr.equals("T")) {
                    this.f.add(Flags.UPPERCASE);
                }
            }
            conversion(convStr);
            if (this.dt) {
                checkDateTime();
            } else if (Conversion.isGeneral(this.c)) {
                checkGeneral();
            } else if (Conversion.isCharacter(this.c)) {
                checkCharacter();
            } else if (Conversion.isInteger(this.c)) {
                checkInteger();
            } else if (Conversion.isFloat(this.c)) {
                checkFloat();
            } else if (Conversion.isText(this.c)) {
                checkText();
            } else {
                throw new UnknownFormatConversionException(String.valueOf(this.c));
            }
        }

        public void print(Object arg, Locale l) throws IOException {
            if (this.dt) {
                printDateTime(arg, l);
                return;
            }
            switch (this.c) {
                case '%':
                    this.this$0.a.append('%');
                    break;
                case 'C':
                case 'c':
                    printCharacter(arg);
                    break;
                case 'a':
                case 'e':
                case 'f':
                case 'g':
                    printFloat(arg, l);
                    break;
                case 'b':
                    printBoolean(arg);
                    break;
                case 'd':
                case 'o':
                case 'x':
                    printInteger(arg, l);
                    break;
                case 'h':
                    printHashCode(arg);
                    break;
                case 'n':
                    this.this$0.a.append(System.lineSeparator());
                    break;
                case 's':
                    printString(arg, l);
                    break;
                default:
                    if (!f11-assertionsDisabled) {
                        throw new AssertionError();
                    }
                    break;
            }
        }

        private void printInteger(Object arg, Locale l) throws IOException {
            if (arg == null) {
                print("null");
            } else if (arg instanceof Byte) {
                print(((Byte) arg).byteValue(), l);
            } else if (arg instanceof Short) {
                print(((Short) arg).shortValue(), l);
            } else if (arg instanceof Integer) {
                print(((Integer) arg).intValue(), l);
            } else if (arg instanceof Long) {
                print(((Long) arg).longValue(), l);
            } else if (arg instanceof BigInteger) {
                print((BigInteger) arg, l);
            } else {
                failConversion(this.c, arg);
            }
        }

        private void printFloat(Object arg, Locale l) throws IOException {
            if (arg == null) {
                print("null");
            } else if (arg instanceof Float) {
                print(((Float) arg).floatValue(), l);
            } else if (arg instanceof Double) {
                print(((Double) arg).doubleValue(), l);
            } else if (arg instanceof BigDecimal) {
                print((BigDecimal) arg, l);
            } else {
                failConversion(this.c, arg);
            }
        }

        private void printDateTime(Object arg, Locale l) throws IOException {
            if (arg == null) {
                print("null");
                return;
            }
            Calendar cal = null;
            Locale locale;
            if (arg instanceof Long) {
                if (l == null) {
                    locale = Locale.US;
                } else {
                    locale = l;
                }
                cal = Calendar.getInstance(locale);
                cal.setTimeInMillis(((Long) arg).longValue());
            } else if (arg instanceof Date) {
                if (l == null) {
                    locale = Locale.US;
                } else {
                    locale = l;
                }
                cal = Calendar.getInstance(locale);
                cal.setTime((Date) arg);
            } else if (arg instanceof Calendar) {
                cal = (Calendar) ((Calendar) arg).clone();
                cal.setLenient(true);
            } else {
                failConversion(this.c, arg);
            }
            print(cal, this.c, l);
        }

        private void printCharacter(Object arg) throws IOException {
            if (arg == null) {
                print("null");
                return;
            }
            String s = null;
            if (arg instanceof Character) {
                s = ((Character) arg).toString();
            } else if (arg instanceof Byte) {
                byte i = ((Byte) arg).byteValue();
                if (Character.isValidCodePoint(i)) {
                    s = new String(Character.toChars(i));
                } else {
                    throw new IllegalFormatCodePointException(i);
                }
            } else if (arg instanceof Short) {
                short i2 = ((Short) arg).shortValue();
                if (Character.isValidCodePoint(i2)) {
                    s = new String(Character.toChars(i2));
                } else {
                    throw new IllegalFormatCodePointException(i2);
                }
            } else if (arg instanceof Integer) {
                int i3 = ((Integer) arg).intValue();
                if (Character.isValidCodePoint(i3)) {
                    s = new String(Character.toChars(i3));
                } else {
                    throw new IllegalFormatCodePointException(i3);
                }
            } else {
                failConversion(this.c, arg);
            }
            print(s);
        }

        private void printString(Object arg, Locale l) throws IOException {
            if (arg instanceof Formattable) {
                Formatter fmt = this.this$0;
                if (fmt.locale() != l) {
                    fmt = new Formatter(fmt.out(), l);
                }
                ((Formattable) arg).formatTo(fmt, this.f.valueOf(), this.width, this.precision);
                return;
            }
            if (this.f.contains(Flags.ALTERNATE)) {
                failMismatch(Flags.ALTERNATE, AuthenticationInfo.SERVER_AUTHENTICATION);
            }
            if (arg == null) {
                print("null");
            } else {
                print(arg.toString());
            }
        }

        private void printBoolean(Object arg) throws IOException {
            String s;
            if (arg == null) {
                s = Boolean.toString(false);
            } else if (arg instanceof Boolean) {
                s = ((Boolean) arg).toString();
            } else {
                s = Boolean.toString(true);
            }
            print(s);
        }

        private void printHashCode(Object arg) throws IOException {
            String s;
            if (arg == null) {
                s = "null";
            } else {
                s = Integer.toHexString(arg.hashCode());
            }
            print(s);
        }

        private void print(String s) throws IOException {
            if (this.precision != -1 && this.precision < s.length()) {
                s = s.substring(0, this.precision);
            }
            if (this.f.contains(Flags.UPPERCASE)) {
                s = s.toUpperCase(this.this$0.l != null ? this.this$0.l : Locale.getDefault());
            }
            this.this$0.a.append(justify(s));
        }

        private String justify(String s) {
            if (this.width == -1) {
                return s;
            }
            int i;
            StringBuilder sb = new StringBuilder();
            boolean pad = this.f.contains(Flags.LEFT_JUSTIFY);
            int sp = this.width - s.length();
            if (!pad) {
                for (i = 0; i < sp; i++) {
                    sb.append(' ');
                }
            }
            sb.append(s);
            if (pad) {
                for (i = 0; i < sp; i++) {
                    sb.append(' ');
                }
            }
            return sb.toString();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("%");
            sb.append(this.f.dup().remove(Flags.UPPERCASE).toString());
            if (this.index > 0) {
                sb.append(this.index).append('$');
            }
            if (this.width != -1) {
                sb.append(this.width);
            }
            if (this.precision != -1) {
                sb.append('.').append(this.precision);
            }
            if (this.dt) {
                sb.append(this.f.contains(Flags.UPPERCASE) ? 'T' : 't');
            }
            sb.append(this.f.contains(Flags.UPPERCASE) ? Character.toUpperCase(this.c) : this.c);
            return sb.toString();
        }

        private void checkGeneral() {
            if ((this.c == 'b' || this.c == 'h') && this.f.contains(Flags.ALTERNATE)) {
                failMismatch(Flags.ALTERNATE, this.c);
            }
            if (this.width == -1 && this.f.contains(Flags.LEFT_JUSTIFY)) {
                throw new MissingFormatWidthException(toString());
            }
            Flags[] flagsArr = new Flags[5];
            flagsArr[0] = Flags.PLUS;
            flagsArr[1] = Flags.LEADING_SPACE;
            flagsArr[2] = Flags.ZERO_PAD;
            flagsArr[3] = Flags.GROUP;
            flagsArr[4] = Flags.PARENTHESES;
            checkBadFlags(flagsArr);
        }

        private void checkDateTime() {
            if (this.precision != -1) {
                throw new IllegalFormatPrecisionException(this.precision);
            } else if (DateTime.isValid(this.c)) {
                Flags[] flagsArr = new Flags[6];
                flagsArr[0] = Flags.ALTERNATE;
                flagsArr[1] = Flags.PLUS;
                flagsArr[2] = Flags.LEADING_SPACE;
                flagsArr[3] = Flags.ZERO_PAD;
                flagsArr[4] = Flags.GROUP;
                flagsArr[5] = Flags.PARENTHESES;
                checkBadFlags(flagsArr);
                if (this.width == -1 && this.f.contains(Flags.LEFT_JUSTIFY)) {
                    throw new MissingFormatWidthException(toString());
                }
            } else {
                throw new UnknownFormatConversionException("t" + this.c);
            }
        }

        private void checkCharacter() {
            if (this.precision != -1) {
                throw new IllegalFormatPrecisionException(this.precision);
            }
            Flags[] flagsArr = new Flags[6];
            flagsArr[0] = Flags.ALTERNATE;
            flagsArr[1] = Flags.PLUS;
            flagsArr[2] = Flags.LEADING_SPACE;
            flagsArr[3] = Flags.ZERO_PAD;
            flagsArr[4] = Flags.GROUP;
            flagsArr[5] = Flags.PARENTHESES;
            checkBadFlags(flagsArr);
            if (this.width == -1 && this.f.contains(Flags.LEFT_JUSTIFY)) {
                throw new MissingFormatWidthException(toString());
            }
        }

        private void checkInteger() {
            checkNumeric();
            Flags[] flagsArr;
            if (this.precision != -1) {
                throw new IllegalFormatPrecisionException(this.precision);
            } else if (this.c == 'd') {
                flagsArr = new Flags[1];
                flagsArr[0] = Flags.ALTERNATE;
                checkBadFlags(flagsArr);
            } else if (this.c == 'o') {
                flagsArr = new Flags[1];
                flagsArr[0] = Flags.GROUP;
                checkBadFlags(flagsArr);
            } else {
                flagsArr = new Flags[1];
                flagsArr[0] = Flags.GROUP;
                checkBadFlags(flagsArr);
            }
        }

        private void checkBadFlags(Flags... badFlags) {
            for (int i = 0; i < badFlags.length; i++) {
                if (this.f.contains(badFlags[i])) {
                    failMismatch(badFlags[i], this.c);
                }
            }
        }

        private void checkFloat() {
            checkNumeric();
            if (this.c != 'f') {
                Flags[] flagsArr;
                if (this.c == 'a') {
                    flagsArr = new Flags[2];
                    flagsArr[0] = Flags.PARENTHESES;
                    flagsArr[1] = Flags.GROUP;
                    checkBadFlags(flagsArr);
                } else if (this.c == 'e') {
                    flagsArr = new Flags[1];
                    flagsArr[0] = Flags.GROUP;
                    checkBadFlags(flagsArr);
                } else if (this.c == 'g') {
                    flagsArr = new Flags[1];
                    flagsArr[0] = Flags.ALTERNATE;
                    checkBadFlags(flagsArr);
                }
            }
        }

        private void checkNumeric() {
            if (this.width != -1 && this.width < 0) {
                throw new IllegalFormatWidthException(this.width);
            } else if (this.precision != -1 && this.precision < 0) {
                throw new IllegalFormatPrecisionException(this.precision);
            } else if (this.width == -1 && (this.f.contains(Flags.LEFT_JUSTIFY) || this.f.contains(Flags.ZERO_PAD))) {
                throw new MissingFormatWidthException(toString());
            } else if ((this.f.contains(Flags.PLUS) && this.f.contains(Flags.LEADING_SPACE)) || (this.f.contains(Flags.LEFT_JUSTIFY) && this.f.contains(Flags.ZERO_PAD))) {
                throw new IllegalFormatFlagsException(this.f.toString());
            }
        }

        private void checkText() {
            if (this.precision != -1) {
                throw new IllegalFormatPrecisionException(this.precision);
            }
            switch (this.c) {
                case '%':
                    if (this.f.valueOf() != Flags.LEFT_JUSTIFY.valueOf() && this.f.valueOf() != Flags.NONE.valueOf()) {
                        throw new IllegalFormatFlagsException(this.f.toString());
                    } else if (this.width == -1 && this.f.contains(Flags.LEFT_JUSTIFY)) {
                        throw new MissingFormatWidthException(toString());
                    } else {
                        return;
                    }
                case 'n':
                    if (this.width != -1) {
                        throw new IllegalFormatWidthException(this.width);
                    } else if (this.f.valueOf() != Flags.NONE.valueOf()) {
                        throw new IllegalFormatFlagsException(this.f.toString());
                    } else {
                        return;
                    }
                default:
                    if (!f11-assertionsDisabled) {
                        throw new AssertionError();
                    }
                    return;
            }
        }

        private void print(byte value, Locale l) throws IOException {
            Object obj = null;
            long v = (long) value;
            if (value < (byte) 0 && (this.c == 'o' || this.c == Locale.PRIVATE_USE_EXTENSION)) {
                v += 256;
                if (!f11-assertionsDisabled) {
                    if (v >= 0) {
                        obj = 1;
                    }
                    if (obj == null) {
                        throw new AssertionError(Long.valueOf(v));
                    }
                }
            }
            print(v, l);
        }

        private void print(short value, Locale l) throws IOException {
            Object obj = null;
            long v = (long) value;
            if (value < (short) 0 && (this.c == 'o' || this.c == Locale.PRIVATE_USE_EXTENSION)) {
                v += 65536;
                if (!f11-assertionsDisabled) {
                    if (v >= 0) {
                        obj = 1;
                    }
                    if (obj == null) {
                        throw new AssertionError(Long.valueOf(v));
                    }
                }
            }
            print(v, l);
        }

        private void print(int value, Locale l) throws IOException {
            Object obj = null;
            long v = (long) value;
            if (value < 0 && (this.c == 'o' || this.c == Locale.PRIVATE_USE_EXTENSION)) {
                v += 4294967296L;
                if (!f11-assertionsDisabled) {
                    if (v >= 0) {
                        obj = 1;
                    }
                    if (obj == null) {
                        throw new AssertionError(Long.valueOf(v));
                    }
                }
            }
            print(v, l);
        }

        private void print(long value, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            Flags[] flagsArr;
            String s;
            int len;
            int i;
            if (this.c == 'd') {
                char[] va;
                boolean neg = value < 0;
                if (value < 0) {
                    va = Long.toString(value, 10).substring(1).toCharArray();
                } else {
                    va = Long.toString(value, 10).toCharArray();
                }
                leadingSign(sb, neg);
                localizedMagnitude(sb, va, this.f, adjustWidth(this.width, this.f, neg), l);
                trailingSign(sb, neg);
            } else if (this.c == 'o') {
                flagsArr = new Flags[3];
                flagsArr[0] = Flags.PARENTHESES;
                flagsArr[1] = Flags.LEADING_SPACE;
                flagsArr[2] = Flags.PLUS;
                checkBadFlags(flagsArr);
                s = Long.toOctalString(value);
                if (this.f.contains(Flags.ALTERNATE)) {
                    len = s.length() + 1;
                } else {
                    len = s.length();
                }
                if (this.f.contains(Flags.ALTERNATE)) {
                    sb.append('0');
                }
                if (this.f.contains(Flags.ZERO_PAD)) {
                    for (i = 0; i < this.width - len; i++) {
                        sb.append('0');
                    }
                }
                sb.append(s);
            } else if (this.c == Locale.PRIVATE_USE_EXTENSION) {
                flagsArr = new Flags[3];
                flagsArr[0] = Flags.PARENTHESES;
                flagsArr[1] = Flags.LEADING_SPACE;
                flagsArr[2] = Flags.PLUS;
                checkBadFlags(flagsArr);
                s = Long.toHexString(value);
                if (this.f.contains(Flags.ALTERNATE)) {
                    len = s.length() + 2;
                } else {
                    len = s.length();
                }
                if (this.f.contains(Flags.ALTERNATE)) {
                    sb.append(this.f.contains(Flags.UPPERCASE) ? "0X" : "0x");
                }
                if (this.f.contains(Flags.ZERO_PAD)) {
                    for (i = 0; i < this.width - len; i++) {
                        sb.append('0');
                    }
                }
                if (this.f.contains(Flags.UPPERCASE)) {
                    s = s.toUpperCase();
                }
                sb.append(s);
            }
            this.this$0.a.append(justify(sb.toString()));
        }

        private StringBuilder leadingSign(StringBuilder sb, boolean neg) {
            if (neg) {
                if (this.f.contains(Flags.PARENTHESES)) {
                    sb.append('(');
                } else {
                    sb.append('-');
                }
            } else if (this.f.contains(Flags.PLUS)) {
                sb.append('+');
            } else if (this.f.contains(Flags.LEADING_SPACE)) {
                sb.append(' ');
            }
            return sb;
        }

        private StringBuilder trailingSign(StringBuilder sb, boolean neg) {
            if (neg && this.f.contains(Flags.PARENTHESES)) {
                sb.append(')');
            }
            return sb;
        }

        private void print(BigInteger value, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            boolean neg = value.signum() == -1;
            BigInteger v = value.abs();
            leadingSign(sb, neg);
            String s;
            int len;
            int i;
            if (this.c == 'd') {
                localizedMagnitude(sb, v.toString().toCharArray(), this.f, adjustWidth(this.width, this.f, neg), l);
            } else if (this.c == 'o') {
                s = v.toString(8);
                len = s.length() + sb.length();
                if (neg && this.f.contains(Flags.PARENTHESES)) {
                    len++;
                }
                if (this.f.contains(Flags.ALTERNATE)) {
                    len++;
                    sb.append('0');
                }
                if (this.f.contains(Flags.ZERO_PAD)) {
                    for (i = 0; i < this.width - len; i++) {
                        sb.append('0');
                    }
                }
                sb.append(s);
            } else if (this.c == Locale.PRIVATE_USE_EXTENSION) {
                s = v.toString(16);
                len = s.length() + sb.length();
                if (neg && this.f.contains(Flags.PARENTHESES)) {
                    len++;
                }
                if (this.f.contains(Flags.ALTERNATE)) {
                    len += 2;
                    sb.append(this.f.contains(Flags.UPPERCASE) ? "0X" : "0x");
                }
                if (this.f.contains(Flags.ZERO_PAD)) {
                    for (i = 0; i < this.width - len; i++) {
                        sb.append('0');
                    }
                }
                if (this.f.contains(Flags.UPPERCASE)) {
                    s = s.toUpperCase();
                }
                sb.append(s);
            }
            trailingSign(sb, value.signum() == -1);
            this.this$0.a.append(justify(sb.toString()));
        }

        private void print(float value, Locale l) throws IOException {
            print((double) value, l);
        }

        private void print(double value, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            boolean neg = Double.compare(value, 0.0d) == -1;
            if (Double.isNaN(value)) {
                sb.append(this.f.contains(Flags.UPPERCASE) ? "NAN" : "NaN");
            } else {
                double v = Math.abs(value);
                leadingSign(sb, neg);
                if (Double.isInfinite(v)) {
                    sb.append(this.f.contains(Flags.UPPERCASE) ? "INFINITY" : "Infinity");
                } else {
                    print(sb, v, l, this.f, this.c, this.precision, neg);
                }
                trailingSign(sb, neg);
            }
            this.this$0.a.append(justify(sb.toString()));
        }

        private void print(StringBuilder sb, double value, Locale l, Flags f, char c, int precision, boolean neg) throws IOException {
            int prec;
            char[] v;
            int len;
            char[] mant;
            char[] exp;
            int newW;
            Flags flags;
            char sign;
            Object obj;
            char[] tmp;
            FormattedFloatingDecimal formattedFloatingDecimal;
            if (c == 'e') {
                String toUpperCase;
                prec = precision == -1 ? 6 : precision;
                v = new char[30];
                len = new FormattedFloatingDecimal(value, prec, Form.SCIENTIFIC).getChars(v);
                mant = addZeros(mantissa(v, len), prec);
                if (f.contains(Flags.ALTERNATE) && prec == 0) {
                    mant = addDot(mant);
                }
                exp = value == 0.0d ? new char[]{'+', '0', '0'} : exponent(v, len);
                newW = this.width;
                if (this.width != -1) {
                    newW = adjustWidth((this.width - exp.length) - 1, f, neg);
                }
                localizedMagnitude(sb, mant, f, newW, l);
                Locale separatorLocale = l != null ? l : Locale.getDefault();
                LocaleData localeData = LocaleData.get(separatorLocale);
                if (f.contains(Flags.UPPERCASE)) {
                    toUpperCase = localeData.exponentSeparator.toUpperCase(separatorLocale);
                } else {
                    toUpperCase = localeData.exponentSeparator.toLowerCase(separatorLocale);
                }
                sb.append(toUpperCase);
                flags = f.dup().remove(Flags.GROUP);
                sign = exp[0];
                if (!f11-assertionsDisabled) {
                    if (sign == '+' || sign == '-') {
                        obj = 1;
                    } else {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                sb.append(sign);
                tmp = new char[(exp.length - 1)];
                System.arraycopy(exp, 1, tmp, 0, exp.length - 1);
                sb.append(localizedMagnitude(null, tmp, flags, -1, l));
            } else if (c == 'f') {
                prec = precision == -1 ? 6 : precision;
                formattedFloatingDecimal = new FormattedFloatingDecimal(value, prec, Form.DECIMAL_FLOAT);
                v = new char[(Math.abs(formattedFloatingDecimal.getExponent()) + 31)];
                mant = addZeros(mantissa(v, formattedFloatingDecimal.getChars(v)), prec);
                if (f.contains(Flags.ALTERNATE) && prec == 0) {
                    mant = addDot(mant);
                }
                newW = this.width;
                if (this.width != -1) {
                    newW = adjustWidth(this.width, f, neg);
                }
                localizedMagnitude(sb, mant, f, newW, l);
            } else if (c == 'g') {
                prec = precision;
                if (precision == -1) {
                    prec = 6;
                } else if (precision == 0) {
                    prec = 1;
                }
                formattedFloatingDecimal = new FormattedFloatingDecimal(value, prec, Form.GENERAL);
                v = new char[(Math.abs(formattedFloatingDecimal.getExponent()) + 31)];
                len = formattedFloatingDecimal.getChars(v);
                exp = exponent(v, len);
                if (exp != null) {
                    prec--;
                } else {
                    prec = (prec - (value == 0.0d ? 0 : formattedFloatingDecimal.getExponentRounded())) - 1;
                }
                mant = addZeros(mantissa(v, len), prec);
                if (f.contains(Flags.ALTERNATE) && prec == 0) {
                    mant = addDot(mant);
                }
                newW = this.width;
                if (this.width != -1) {
                    if (exp != null) {
                        newW = adjustWidth((this.width - exp.length) - 1, f, neg);
                    } else {
                        newW = adjustWidth(this.width, f, neg);
                    }
                }
                localizedMagnitude(sb, mant, f, newW, l);
                if (exp != null) {
                    sb.append(f.contains(Flags.UPPERCASE) ? 'E' : 'e');
                    flags = f.dup().remove(Flags.GROUP);
                    sign = exp[0];
                    if (!f11-assertionsDisabled) {
                        if (sign == '+' || sign == '-') {
                            obj = 1;
                        } else {
                            obj = null;
                        }
                        if (obj == null) {
                            throw new AssertionError();
                        }
                    }
                    sb.append(sign);
                    tmp = new char[(exp.length - 1)];
                    System.arraycopy(exp, 1, tmp, 0, exp.length - 1);
                    sb.append(localizedMagnitude(null, tmp, flags, -1, l));
                }
            } else if (c == 'a') {
                prec = precision;
                if (precision == -1) {
                    prec = 0;
                } else if (precision == 0) {
                    prec = 1;
                }
                String s = hexDouble(value, prec);
                boolean upper = f.contains(Flags.UPPERCASE);
                sb.append(upper ? "0X" : "0x");
                if (f.contains(Flags.ZERO_PAD)) {
                    for (int i = 0; i < (this.width - s.length()) - 2; i++) {
                        sb.append('0');
                    }
                }
                int idx = s.indexOf(112);
                char[] va = s.substring(0, idx).toCharArray();
                if (upper) {
                    va = new String(va).toUpperCase(Locale.US).toCharArray();
                }
                if (prec != 0) {
                    va = addZeros(va, prec);
                }
                sb.append(va);
                sb.append(upper ? 'P' : AuthenticationInfo.PROXY_AUTHENTICATION);
                sb.append(s.substring(idx + 1));
            }
        }

        private char[] mantissa(char[] v, int len) {
            int i = 0;
            while (i < len && v[i] != 'e') {
                i++;
            }
            char[] tmp = new char[i];
            System.arraycopy(v, 0, tmp, 0, i);
            return tmp;
        }

        private char[] exponent(char[] v, int len) {
            int i = len - 1;
            while (i >= 0 && v[i] != 'e') {
                i--;
            }
            if (i == -1) {
                return null;
            }
            char[] tmp = new char[((len - i) - 1)];
            System.arraycopy(v, i + 1, tmp, 0, (len - i) - 1);
            return tmp;
        }

        private char[] addZeros(char[] v, int prec) {
            int i;
            int i2 = 1;
            int i3 = 0;
            while (i3 < v.length && v[i3] != '.') {
                i3++;
            }
            boolean needDot = false;
            if (i3 == v.length) {
                needDot = true;
            }
            int length = v.length - i3;
            if (needDot) {
                i = 0;
            } else {
                i = 1;
            }
            int outPrec = length - i;
            if (!f11-assertionsDisabled) {
                if (outPrec <= prec) {
                    i = 1;
                } else {
                    i = 0;
                }
                if (i == 0) {
                    throw new AssertionError();
                }
            }
            if (outPrec == prec) {
                return v;
            }
            i = (v.length + prec) - outPrec;
            if (!needDot) {
                i2 = 0;
            }
            char[] tmp = new char[(i + i2)];
            System.arraycopy(v, 0, tmp, 0, v.length);
            int start = v.length;
            if (needDot) {
                tmp[v.length] = '.';
                start++;
            }
            for (int j = start; j < tmp.length; j++) {
                tmp[j] = '0';
            }
            return tmp;
        }

        private String hexDouble(double d, int prec) {
            if (!FpUtils.isFinite(d) || d == 0.0d || prec == 0 || prec >= 13) {
                return Double.toHexString(d).substring(2);
            }
            Object obj;
            if (!f11-assertionsDisabled) {
                obj = (prec < 1 || prec > 12) ? null : 1;
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            boolean subnormal = FpUtils.getExponent(d) == -1023;
            if (subnormal) {
                Formatter.scaleUp = FpUtils.scalb(1.0d, 54);
                d *= Formatter.scaleUp;
                int exponent = FpUtils.getExponent(d);
                if (!f11-assertionsDisabled) {
                    obj = exponent >= -1022 ? exponent <= 1023 ? 1 : null : null;
                    if (obj == null) {
                        throw new AssertionError(Integer.valueOf(exponent));
                    }
                }
            }
            int shiftDistance = 53 - ((prec * 4) + 1);
            if (!f11-assertionsDisabled) {
                obj = (shiftDistance < 1 || shiftDistance >= 53) ? null : 1;
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            long doppel = Double.doubleToLongBits(d);
            long newSignif = (Long.MAX_VALUE & doppel) >> shiftDistance;
            long roundingBits = doppel & (~(-1 << shiftDistance));
            boolean leastZero = (1 & newSignif) == 0;
            boolean round = ((1 << (shiftDistance + -1)) & roundingBits) != 0;
            boolean sticky = shiftDistance > 1 ? ((~(1 << (shiftDistance + -1))) & roundingBits) != 0 : false;
            if ((leastZero && round && sticky) || (!leastZero && round)) {
                newSignif++;
            }
            double result = Double.longBitsToDouble((doppel & Long.MIN_VALUE) | (newSignif << shiftDistance));
            if (Double.isInfinite(result)) {
                return "1.0p1024";
            }
            String res = Double.toHexString(result).substring(2);
            if (!subnormal) {
                return res;
            }
            int idx = res.indexOf(112);
            if (idx != -1) {
                return res.substring(0, idx) + "p" + Integer.toString(Integer.parseInt(res.substring(idx + 1)) - 54);
            } else if (f11-assertionsDisabled) {
                return null;
            } else {
                throw new AssertionError();
            }
        }

        private void print(BigDecimal value, Locale l) throws IOException {
            if (this.c == 'a') {
                failConversion(this.c, value);
            }
            StringBuilder sb = new StringBuilder();
            boolean neg = value.signum() == -1;
            BigDecimal v = value.abs();
            leadingSign(sb, neg);
            print(sb, v, l, this.f, this.c, this.precision, neg);
            trailingSign(sb, neg);
            this.this$0.a.append(justify(sb.toString()));
        }

        /* JADX WARNING: Missing block: B:32:0x00dc, code:
            if (r45.contains(java.util.Formatter.Flags.ALTERNATE) != false) goto L_0x00de;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void print(StringBuilder sb, BigDecimal value, Locale l, Flags f, char c, int precision, boolean neg) throws IOException {
            int prec;
            int scale;
            int nzeros;
            int compPrec;
            BigDecimalLayout bigDecimalLayout;
            char[] mant;
            if (c == 'e') {
                prec = precision == -1 ? 6 : precision;
                scale = value.scale();
                int origPrec = value.precision();
                nzeros = 0;
                if (prec > origPrec - 1) {
                    compPrec = origPrec;
                    nzeros = prec - (origPrec - 1);
                } else {
                    compPrec = prec + 1;
                }
                BigDecimal bigDecimal = new BigDecimal(value.unscaledValue(), scale, new MathContext(compPrec));
                bigDecimalLayout = new BigDecimalLayout(this, bigDecimal.unscaledValue(), bigDecimal.scale(), BigDecimalLayoutForm.SCIENTIFIC);
                mant = bigDecimalLayout.mantissa();
                if (origPrec == 1 || !bigDecimalLayout.hasDot()) {
                    if (nzeros <= 0) {
                    }
                    mant = addDot(mant);
                }
                mant = trailingZeros(mant, nzeros);
                char[] exp = bigDecimalLayout.exponent();
                int newW = this.width;
                if (this.width != -1) {
                    newW = adjustWidth((this.width - exp.length) - 1, f, neg);
                }
                localizedMagnitude(sb, mant, f, newW, l);
                sb.append(f.contains(Flags.UPPERCASE) ? 'E' : 'e');
                Flags flags = f.dup().remove(Flags.GROUP);
                char sign = exp[0];
                if (!f11-assertionsDisabled) {
                    Object obj;
                    if (sign == '+' || sign == '-') {
                        obj = 1;
                    } else {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                sb.append(exp[0]);
                char[] tmp = new char[(exp.length - 1)];
                System.arraycopy(exp, 1, tmp, 0, exp.length - 1);
                sb.append(localizedMagnitude(null, tmp, flags, -1, l));
            } else if (c == 'f') {
                prec = precision == -1 ? 6 : precision;
                scale = value.scale();
                if (scale > prec) {
                    compPrec = value.precision();
                    if (compPrec <= scale) {
                        value = value.setScale(prec, RoundingMode.HALF_UP);
                    } else {
                        value = new BigDecimal(value.unscaledValue(), scale, new MathContext(compPrec - (scale - prec)));
                    }
                }
                bigDecimalLayout = new BigDecimalLayout(this, value.unscaledValue(), value.scale(), BigDecimalLayoutForm.DECIMAL_FLOAT);
                mant = bigDecimalLayout.mantissa();
                nzeros = bigDecimalLayout.scale() < prec ? prec - bigDecimalLayout.scale() : 0;
                if (bigDecimalLayout.scale() == 0) {
                    if (f.contains(Flags.ALTERNATE) || nzeros > 0) {
                        mant = addDot(bigDecimalLayout.mantissa());
                    }
                }
                localizedMagnitude(sb, trailingZeros(mant, nzeros), f, adjustWidth(this.width, f, neg), l);
            } else if (c == 'g') {
                prec = precision;
                if (precision == -1) {
                    prec = 6;
                } else if (precision == 0) {
                    prec = 1;
                }
                BigDecimal tenToTheNegFour = BigDecimal.valueOf(1, 4);
                BigDecimal tenToThePrec = BigDecimal.valueOf(1, -prec);
                if (value.equals(BigDecimal.ZERO) || (value.compareTo(tenToTheNegFour) != -1 && value.compareTo(tenToThePrec) == -1)) {
                    print(sb, value, l, f, 'f', (prec - ((-value.scale()) + (value.unscaledValue().toString().length() - 1))) - 1, neg);
                } else {
                    print(sb, value, l, f, 'e', prec - 1, neg);
                }
            } else if (c == 'a' && !f11-assertionsDisabled) {
                throw new AssertionError();
            }
        }

        private int adjustWidth(int width, Flags f, boolean neg) {
            int newW = width;
            if (width != -1 && neg && f.contains(Flags.PARENTHESES)) {
                return width - 1;
            }
            return newW;
        }

        private char[] addDot(char[] mant) {
            char[] tmp = mant;
            tmp = new char[(mant.length + 1)];
            System.arraycopy(mant, 0, tmp, 0, mant.length);
            tmp[tmp.length - 1] = '.';
            return tmp;
        }

        private char[] trailingZeros(char[] mant, int nzeros) {
            char[] tmp = mant;
            if (nzeros > 0) {
                tmp = new char[(mant.length + nzeros)];
                System.arraycopy(mant, 0, tmp, 0, mant.length);
                for (int i = mant.length; i < tmp.length; i++) {
                    tmp[i] = '0';
                }
            }
            return tmp;
        }

        private void print(Calendar t, char c, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            print(sb, t, c, l);
            CharSequence s = justify(sb.toString());
            if (this.f.contains(Flags.UPPERCASE)) {
                s = s.toUpperCase();
            }
            this.this$0.a.append(s);
        }

        private Appendable print(StringBuilder sb, Calendar t, char c, Locale l) throws IOException {
            if (!f11-assertionsDisabled) {
                if ((this.width == -1 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            if (sb == null) {
                sb = new StringBuilder();
            }
            int i;
            Locale lt;
            DateFormatSymbols dfs;
            Flags flags;
            switch (c) {
                case 'A':
                case 'a':
                    i = t.get(7);
                    if (l == null) {
                        lt = Locale.US;
                    } else {
                        lt = l;
                    }
                    dfs = DateFormatSymbols.getInstance(lt);
                    if (c != 'A') {
                        sb.append(dfs.getShortWeekdays()[i]);
                        break;
                    }
                    sb.append(dfs.getWeekdays()[i]);
                    break;
                case 'B':
                case 'b':
                case 'h':
                    i = t.get(2);
                    if (l == null) {
                        lt = Locale.US;
                    } else {
                        lt = l;
                    }
                    dfs = DateFormatSymbols.getInstance(lt);
                    if (c != 'B') {
                        sb.append(dfs.getShortMonths()[i]);
                        break;
                    }
                    sb.append(dfs.getMonths()[i]);
                    break;
                case 'C':
                case 'Y':
                case 'y':
                    i = t.get(1);
                    int size = 2;
                    switch (c) {
                        case 'C':
                            i /= 100;
                            break;
                        case 'Y':
                            size = 4;
                            break;
                        case 'y':
                            i %= 100;
                            break;
                    }
                    sb.append(localizedMagnitude(null, (long) i, Flags.ZERO_PAD, size, l));
                    break;
                case 'D':
                    print(sb, t, 'm', l).append('/');
                    print(sb, t, 'd', l).append('/');
                    print(sb, t, 'y', l);
                    break;
                case Types.DATALINK /*70*/:
                    print(sb, t, 'Y', l).append('-');
                    print(sb, t, 'm', l).append('-');
                    print(sb, t, 'd', l);
                    break;
                case 'H':
                case 'I':
                case 'k':
                case 'l':
                    i = t.get(11);
                    if (c == 'I' || c == 'l') {
                        i = (i == 0 || i == 12) ? 12 : i % 12;
                    }
                    if (c == 'H' || c == 'I') {
                        flags = Flags.ZERO_PAD;
                    } else {
                        flags = Flags.NONE;
                    }
                    sb.append(localizedMagnitude(null, (long) i, flags, 2, l));
                    break;
                case 'L':
                    sb.append(localizedMagnitude(null, (long) t.get(14), Flags.ZERO_PAD, 3, l));
                    break;
                case 'M':
                    sb.append(localizedMagnitude(null, (long) t.get(12), Flags.ZERO_PAD, 2, l));
                    break;
                case 'N':
                    sb.append(localizedMagnitude(null, (long) (t.get(14) * 1000000), Flags.ZERO_PAD, 9, l));
                    break;
                case 'Q':
                    sb.append(localizedMagnitude(null, t.getTimeInMillis(), Flags.NONE, this.width, l));
                    break;
                case 'R':
                case 'T':
                    print(sb, t, 'H', l).append(':');
                    print(sb, t, 'M', l);
                    if (c == 'T') {
                        sb.append(':');
                        print(sb, t, 'S', l);
                        break;
                    }
                    break;
                case 'S':
                    sb.append(localizedMagnitude(null, (long) t.get(13), Flags.ZERO_PAD, 2, l));
                    break;
                case 'Z':
                    TimeZone tz = t.getTimeZone();
                    boolean z = t.get(16) != 0;
                    if (l == null) {
                        l = Locale.US;
                    }
                    sb.append(tz.getDisplayName(z, 0, l));
                    break;
                case 'c':
                    print(sb, t, 'a', l).append(' ');
                    print(sb, t, 'b', l).append(' ');
                    print(sb, t, 'd', l).append(' ');
                    print(sb, t, 'T', l).append(' ');
                    print(sb, t, 'Z', l).append(' ');
                    print(sb, t, 'Y', l);
                    break;
                case 'd':
                case 'e':
                    i = t.get(5);
                    if (c == 'd') {
                        flags = Flags.ZERO_PAD;
                    } else {
                        flags = Flags.NONE;
                    }
                    sb.append(localizedMagnitude(null, (long) i, flags, 2, l));
                    break;
                case 'j':
                    sb.append(localizedMagnitude(null, (long) t.get(6), Flags.ZERO_PAD, 3, l));
                    break;
                case 'm':
                    sb.append(localizedMagnitude(null, (long) (t.get(2) + 1), Flags.ZERO_PAD, 2, l));
                    break;
                case 'p':
                    String[] ampm = new String[2];
                    ampm[0] = "AM";
                    ampm[1] = "PM";
                    if (!(l == null || l == Locale.US)) {
                        ampm = DateFormatSymbols.getInstance(l).getAmPmStrings();
                    }
                    String s = ampm[t.get(9)];
                    if (l == null) {
                        l = Locale.US;
                    }
                    sb.append(s.toLowerCase(l));
                    break;
                case 'r':
                    print(sb, t, 'I', l).append(':');
                    print(sb, t, 'M', l).append(':');
                    print(sb, t, 'S', l).append(' ');
                    StringBuilder tsb = new StringBuilder();
                    print(tsb, t, AuthenticationInfo.PROXY_AUTHENTICATION, l);
                    String stringBuilder = tsb.toString();
                    if (l == null) {
                        l = Locale.US;
                    }
                    sb.append(stringBuilder.toUpperCase(l));
                    break;
                case 's':
                    sb.append(localizedMagnitude(null, t.getTimeInMillis() / 1000, Flags.NONE, this.width, l));
                    break;
                case 'z':
                    i = t.get(15) + t.get(16);
                    boolean neg = i < 0;
                    sb.append(neg ? '-' : '+');
                    if (neg) {
                        i = -i;
                    }
                    int min = i / 60000;
                    sb.append(localizedMagnitude(null, (long) (((min / 60) * 100) + (min % 60)), Flags.ZERO_PAD, 4, l));
                    break;
                default:
                    if (!f11-assertionsDisabled) {
                        throw new AssertionError();
                    }
                    break;
            }
            return sb;
        }

        private void failMismatch(Flags f, char c) {
            throw new FormatFlagsConversionMismatchException(f.toString(), c);
        }

        private void failConversion(char c, Object arg) {
            throw new IllegalFormatConversionException(c, arg.getClass());
        }

        private char getZero(Locale l) {
            if (l == null || l.equals(this.this$0.locale())) {
                return this.this$0.zero;
            }
            return DecimalFormatSymbols.getInstance(l).getZeroDigit();
        }

        private StringBuilder localizedMagnitude(StringBuilder sb, long value, Flags f, int width, Locale l) {
            return localizedMagnitude(sb, Long.toString(value, 10).toCharArray(), f, width, l);
        }

        private StringBuilder localizedMagnitude(StringBuilder sb, char[] value, Flags f, int width, Locale l) {
            int j;
            if (sb == null) {
                sb = new StringBuilder();
            }
            int begin = sb.length();
            char zero = getZero(l);
            char grpSep = 0;
            int grpSize = -1;
            char decSep = 0;
            int len = value.length;
            int dot = len;
            for (j = 0; j < len; j++) {
                if (value[j] == '.') {
                    dot = j;
                    break;
                }
            }
            if (dot < len) {
                if (l != null) {
                    if (!l.equals(Locale.US)) {
                        decSep = DecimalFormatSymbols.getInstance(l).getDecimalSeparator();
                    }
                }
                decSep = '.';
            }
            if (f.contains(Flags.GROUP)) {
                if (l != null) {
                    if (!l.equals(Locale.US)) {
                        grpSep = DecimalFormatSymbols.getInstance(l).getGroupingSeparator();
                        grpSize = ((DecimalFormat) NumberFormat.getIntegerInstance(l)).getGroupingSize();
                    }
                }
                grpSep = ',';
                grpSize = 3;
            }
            j = 0;
            while (j < len) {
                if (j == dot) {
                    sb.append(decSep);
                    grpSep = 0;
                } else {
                    sb.append((char) ((value[j] - 48) + zero));
                    if (!(grpSep == 0 || j == dot - 1 || (dot - j) % grpSize != 1)) {
                        sb.append(grpSep);
                    }
                }
                j++;
            }
            len = sb.length();
            if (width != -1) {
                if (f.contains(Flags.ZERO_PAD)) {
                    for (int k = 0; k < width - len; k++) {
                        sb.insert(begin, zero);
                    }
                }
            }
            return sb;
        }
    }

    private class FormatSpecifierParser {
        private static final String FLAGS = ",-(+# 0<";
        private String conv;
        private int cursor;
        private String flags;
        private final String format;
        private FormatSpecifier fs;
        private String index;
        private String precision;
        private String tT;
        final /* synthetic */ Formatter this$0;
        private String width;

        public FormatSpecifierParser(Formatter this$0, String format, int startIdx) {
            this.this$0 = this$0;
            this.format = format;
            this.cursor = startIdx;
            if (nextIsInt()) {
                String nint = nextInt();
                if (peek() == '$') {
                    this.index = nint;
                    advance();
                } else if (nint.charAt(0) == '0') {
                    back(nint.length());
                } else {
                    this.width = nint;
                }
            }
            this.flags = "";
            while (this.width == null && FLAGS.indexOf(peek()) >= 0) {
                this.flags += advance();
            }
            if (this.width == null && nextIsInt()) {
                this.width = nextInt();
            }
            if (peek() == '.') {
                advance();
                if (nextIsInt()) {
                    this.precision = nextInt();
                } else {
                    throw new IllegalFormatPrecisionException(peek());
                }
            }
            if (peek() == 't' || peek() == 'T') {
                this.tT = String.valueOf(advance());
            }
            this.conv = String.valueOf(advance());
            this.fs = new FormatSpecifier(this$0, this.index, this.flags, this.width, this.precision, this.tT, this.conv);
        }

        private String nextInt() {
            int strBegin = this.cursor;
            while (nextIsInt()) {
                advance();
            }
            return this.format.substring(strBegin, this.cursor);
        }

        private boolean nextIsInt() {
            return !isEnd() ? Character.isDigit(peek()) : false;
        }

        private char peek() {
            if (!isEnd()) {
                return this.format.charAt(this.cursor);
            }
            throw new UnknownFormatConversionException("End of String");
        }

        private char advance() {
            if (isEnd()) {
                throw new UnknownFormatConversionException("End of String");
            }
            String str = this.format;
            int i = this.cursor;
            this.cursor = i + 1;
            return str.charAt(i);
        }

        private void back(int len) {
            this.cursor -= len;
        }

        private boolean isEnd() {
            return this.cursor == this.format.length();
        }

        public FormatSpecifier getFormatSpecifier() {
            return this.fs;
        }

        public int getEndIdx() {
            return this.cursor;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x000b A:{ExcHandler: java.nio.charset.IllegalCharsetNameException (e java.nio.charset.IllegalCharsetNameException), Splitter: B:1:0x0006} */
    /* JADX WARNING: Missing block: B:6:0x0011, code:
            throw new java.io.UnsupportedEncodingException(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Charset toCharset(String csn) throws UnsupportedEncodingException {
        Objects.requireNonNull((Object) csn, "charsetName");
        try {
            return Charset.forName(csn);
        } catch (IllegalCharsetNameException e) {
        }
    }

    private static final Appendable nonNullAppendable(Appendable a) {
        if (a == null) {
            return new StringBuilder();
        }
        return a;
    }

    private Formatter(Locale l, Appendable a) {
        this.a = a;
        this.l = l;
        this.zero = getZero(l);
    }

    private Formatter(Charset charset, Locale l, File file) throws FileNotFoundException {
        this(l, new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset)));
    }

    public Formatter() {
        this(Locale.getDefault(Category.FORMAT), new StringBuilder());
    }

    public Formatter(Appendable a) {
        this(Locale.getDefault(Category.FORMAT), nonNullAppendable(a));
    }

    public Formatter(Locale l) {
        this(l, new StringBuilder());
    }

    public Formatter(Appendable a, Locale l) {
        this(l, nonNullAppendable(a));
    }

    public Formatter(String fileName) throws FileNotFoundException {
        this(Locale.getDefault(Category.FORMAT), new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))));
    }

    public Formatter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(fileName, csn, Locale.getDefault(Category.FORMAT));
    }

    public Formatter(String fileName, String csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
        this(toCharset(csn), l, new File(fileName));
    }

    public Formatter(File file) throws FileNotFoundException {
        this(Locale.getDefault(Category.FORMAT), new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))));
    }

    public Formatter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(file, csn, Locale.getDefault(Category.FORMAT));
    }

    public Formatter(File file, String csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
        this(toCharset(csn), l, file);
    }

    public Formatter(PrintStream ps) {
        this(Locale.getDefault(Category.FORMAT), (Appendable) Objects.requireNonNull(ps));
    }

    public Formatter(OutputStream os) {
        this(Locale.getDefault(Category.FORMAT), new BufferedWriter(new OutputStreamWriter(os)));
    }

    public Formatter(OutputStream os, String csn) throws UnsupportedEncodingException {
        this(os, csn, Locale.getDefault(Category.FORMAT));
    }

    public Formatter(OutputStream os, String csn, Locale l) throws UnsupportedEncodingException {
        this(l, new BufferedWriter(new OutputStreamWriter(os, csn)));
    }

    private static char getZero(Locale l) {
        if (l == null || l.equals(Locale.US)) {
            return '0';
        }
        return DecimalFormatSymbols.getInstance(l).getZeroDigit();
    }

    public Locale locale() {
        ensureOpen();
        return this.l;
    }

    public Appendable out() {
        ensureOpen();
        return this.a;
    }

    public String toString() {
        ensureOpen();
        return this.a.toString();
    }

    public void flush() {
        ensureOpen();
        if (this.a instanceof Flushable) {
            try {
                ((Flushable) this.a).flush();
            } catch (IOException ioe) {
                this.lastException = ioe;
            }
        }
    }

    public void close() {
        if (this.a != null) {
            try {
                if (this.a instanceof Closeable) {
                    ((Closeable) this.a).close();
                }
            } catch (IOException ioe) {
                this.lastException = ioe;
            } catch (Throwable th) {
                this.a = null;
            }
            this.a = null;
        }
    }

    private void ensureOpen() {
        if (this.a == null) {
            throw new FormatterClosedException();
        }
    }

    public IOException ioException() {
        return this.lastException;
    }

    public Formatter format(String format, Object... args) {
        return format(this.l, format, args);
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Formatter format(Locale l, String format, Object... args) {
        ensureOpen();
        int last = -1;
        int lasto = -1;
        FormatString[] fsa = parse(format);
        for (FormatString fs : fsa) {
            int index = fs.index();
            switch (index) {
                case -2:
                    fs.print(null, l);
                    break;
                case -1:
                    if (last >= 0 && (args == null || last <= args.length - 1)) {
                        fs.print(args == null ? null : args[last], l);
                        break;
                    }
                    throw new MissingFormatArgumentException(fs.toString());
                    break;
                case 0:
                    lasto++;
                    last = lasto;
                    if (args == null || lasto <= args.length - 1) {
                        fs.print(args == null ? null : args[lasto], l);
                        break;
                    }
                    throw new MissingFormatArgumentException(fs.toString());
                    break;
                default:
                    last = index - 1;
                    if (args != null) {
                        try {
                            if (last > args.length - 1) {
                                throw new MissingFormatArgumentException(fs.toString());
                            }
                        } catch (IOException x) {
                            this.lastException = x;
                            break;
                        }
                    }
                    fs.print(args == null ? null : args[last], l);
                    break;
            }
        }
        return this;
    }

    private FormatString[] parse(String s) {
        ArrayList<FormatString> al = new ArrayList();
        int i = 0;
        int len = s.length();
        while (i < len) {
            int nextPercent = s.indexOf(37, i);
            if (s.charAt(i) != '%') {
                int plainTextStart = i;
                int plainTextEnd = nextPercent == -1 ? len : nextPercent;
                al.add(new FixedString(s.substring(plainTextStart, plainTextEnd)));
                i = plainTextEnd;
            } else {
                FormatSpecifierParser fsp = new FormatSpecifierParser(this, s, i + 1);
                al.add(fsp.getFormatSpecifier());
                i = fsp.getEndIdx();
            }
        }
        return (FormatString[]) al.toArray(new FormatString[al.size()]);
    }
}
