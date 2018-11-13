package android.icu.text;

import android.icu.impl.Grego;
import android.icu.util.Currency.CurrencyUsage;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
public class CurrencyMetaInfo {
    @Deprecated
    protected static final CurrencyDigits defaultDigits = null;
    private static final boolean hasData = false;
    private static final CurrencyMetaInfo impl = null;

    public static final class CurrencyDigits {
        public final int fractionDigits;
        public final int roundingIncrement;

        public CurrencyDigits(int fractionDigits, int roundingIncrement) {
            this.fractionDigits = fractionDigits;
            this.roundingIncrement = roundingIncrement;
        }

        public String toString() {
            return CurrencyMetaInfo.debugString(this);
        }
    }

    public static final class CurrencyFilter {
        private static final CurrencyFilter ALL = null;
        public final String currency;
        public final long from;
        public final String region;
        @Deprecated
        public final boolean tenderOnly;
        public final long to;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.CurrencyMetaInfo.CurrencyFilter.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.CurrencyMetaInfo.CurrencyFilter.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CurrencyMetaInfo.CurrencyFilter.<clinit>():void");
        }

        private CurrencyFilter(String region, String currency, long from, long to, boolean tenderOnly) {
            this.region = region;
            this.currency = currency;
            this.from = from;
            this.to = to;
            this.tenderOnly = tenderOnly;
        }

        public static CurrencyFilter all() {
            return ALL;
        }

        public static CurrencyFilter now() {
            return ALL.withDate(new Date());
        }

        public static CurrencyFilter onRegion(String region) {
            return ALL.withRegion(region);
        }

        public static CurrencyFilter onCurrency(String currency) {
            return ALL.withCurrency(currency);
        }

        public static CurrencyFilter onDate(Date date) {
            return ALL.withDate(date);
        }

        public static CurrencyFilter onDateRange(Date from, Date to) {
            return ALL.withDateRange(from, to);
        }

        public static CurrencyFilter onDate(long date) {
            return ALL.withDate(date);
        }

        public static CurrencyFilter onDateRange(long from, long to) {
            return ALL.withDateRange(from, to);
        }

        public static CurrencyFilter onTender() {
            return ALL.withTender();
        }

        public CurrencyFilter withRegion(String region) {
            return new CurrencyFilter(region, this.currency, this.from, this.to, this.tenderOnly);
        }

        public CurrencyFilter withCurrency(String currency) {
            return new CurrencyFilter(this.region, currency, this.from, this.to, this.tenderOnly);
        }

        public CurrencyFilter withDate(Date date) {
            return new CurrencyFilter(this.region, this.currency, date.getTime(), date.getTime(), this.tenderOnly);
        }

        public CurrencyFilter withDateRange(Date from, Date to) {
            return new CurrencyFilter(this.region, this.currency, from == null ? Long.MIN_VALUE : from.getTime(), to == null ? Long.MAX_VALUE : to.getTime(), this.tenderOnly);
        }

        public CurrencyFilter withDate(long date) {
            return new CurrencyFilter(this.region, this.currency, date, date, this.tenderOnly);
        }

        public CurrencyFilter withDateRange(long from, long to) {
            return new CurrencyFilter(this.region, this.currency, from, to, this.tenderOnly);
        }

        public CurrencyFilter withTender() {
            return new CurrencyFilter(this.region, this.currency, this.from, this.to, true);
        }

        public boolean equals(Object rhs) {
            if (rhs instanceof CurrencyFilter) {
                return equals((CurrencyFilter) rhs);
            }
            return false;
        }

        public boolean equals(CurrencyFilter rhs) {
            if (this == rhs) {
                return true;
            }
            if (rhs != null && equals(this.region, rhs.region) && equals(this.currency, rhs.currency) && this.from == rhs.from && this.to == rhs.to) {
                return this.tenderOnly == rhs.tenderOnly;
            } else {
                return false;
            }
        }

        public int hashCode() {
            int hc = 0;
            if (this.region != null) {
                hc = this.region.hashCode();
            }
            if (this.currency != null) {
                hc = (hc * 31) + this.currency.hashCode();
            }
            return (((((((((hc * 31) + ((int) this.from)) * 31) + ((int) (this.from >>> 32))) * 31) + ((int) this.to)) * 31) + ((int) (this.to >>> 32))) * 31) + (this.tenderOnly ? 1 : 0);
        }

        public String toString() {
            return CurrencyMetaInfo.debugString(this);
        }

        private static boolean equals(String lhs, String rhs) {
            if (lhs != rhs) {
                return lhs != null ? lhs.equals(rhs) : false;
            } else {
                return true;
            }
        }
    }

    public static final class CurrencyInfo {
        public final String code;
        public final long from;
        public final int priority;
        public final String region;
        private final boolean tender;
        public final long to;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.text.CurrencyMetaInfo.CurrencyInfo.<init>(java.lang.String, java.lang.String, long, long, int, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public CurrencyInfo(java.lang.String r1, java.lang.String r2, long r3, long r5, int r7, boolean r8) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.text.CurrencyMetaInfo.CurrencyInfo.<init>(java.lang.String, java.lang.String, long, long, int, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CurrencyMetaInfo.CurrencyInfo.<init>(java.lang.String, java.lang.String, long, long, int, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.icu.text.CurrencyMetaInfo.CurrencyInfo.isTender():boolean, dex:  in method: android.icu.text.CurrencyMetaInfo.CurrencyInfo.isTender():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.icu.text.CurrencyMetaInfo.CurrencyInfo.isTender():boolean, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public boolean isTender() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.icu.text.CurrencyMetaInfo.CurrencyInfo.isTender():boolean, dex:  in method: android.icu.text.CurrencyMetaInfo.CurrencyInfo.isTender():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CurrencyMetaInfo.CurrencyInfo.isTender():boolean");
        }

        @Deprecated
        public CurrencyInfo(String region, String code, long from, long to, int priority) {
            this(region, code, from, to, priority, true);
        }

        public String toString() {
            return CurrencyMetaInfo.debugString(this);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.text.CurrencyMetaInfo.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.text.CurrencyMetaInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CurrencyMetaInfo.<clinit>():void");
    }

    public static CurrencyMetaInfo getInstance() {
        return impl;
    }

    public static CurrencyMetaInfo getInstance(boolean noSubstitute) {
        return hasData ? impl : null;
    }

    @Deprecated
    public static boolean hasData() {
        return hasData;
    }

    @Deprecated
    protected CurrencyMetaInfo() {
    }

    public List<CurrencyInfo> currencyInfo(CurrencyFilter filter) {
        return Collections.emptyList();
    }

    public List<String> currencies(CurrencyFilter filter) {
        return Collections.emptyList();
    }

    public List<String> regions(CurrencyFilter filter) {
        return Collections.emptyList();
    }

    public CurrencyDigits currencyDigits(String isoCode) {
        return currencyDigits(isoCode, CurrencyUsage.STANDARD);
    }

    public CurrencyDigits currencyDigits(String isoCode, CurrencyUsage currencyUsage) {
        return defaultDigits;
    }

    private static String dateString(long date) {
        if (date == Long.MAX_VALUE || date == Long.MIN_VALUE) {
            return null;
        }
        return Grego.timeToString(date);
    }

    private static String debugString(Object o) {
        StringBuilder sb = new StringBuilder();
        try {
            for (Field f : o.getClass().getFields()) {
                Object v = f.get(o);
                if (v != null) {
                    String s;
                    if (v instanceof Date) {
                        s = dateString(((Date) v).getTime());
                    } else if (v instanceof Long) {
                        s = dateString(((Long) v).longValue());
                    } else {
                        s = String.valueOf(v);
                    }
                    if (s != null) {
                        if (sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(f.getName()).append("='").append(s).append("'");
                    }
                }
            }
        } catch (Throwable th) {
        }
        sb.insert(0, o.getClass().getSimpleName() + "(");
        sb.append(")");
        return sb.toString();
    }
}
