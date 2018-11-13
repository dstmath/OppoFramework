package java.util;

import java.io.Serializable;
import libcore.icu.ICU;
import sun.util.locale.BaseLocale;

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
public final class Currency implements Serializable {
    private static HashSet<Currency> available = null;
    private static HashMap<String, Currency> instances = null;
    private static final long serialVersionUID = -158308464356906721L;
    private final String currencyCode;
    private final transient android.icu.util.Currency icuCurrency;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.Currency.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.Currency.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.Currency.<clinit>():void");
    }

    private Currency(android.icu.util.Currency icuCurrency) {
        this.icuCurrency = icuCurrency;
        this.currencyCode = icuCurrency.getCurrencyCode();
    }

    /* JADX WARNING: Missing block: B:12:0x0021, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Currency getInstance(String currencyCode) {
        synchronized (instances) {
            Currency instance = (Currency) instances.get(currencyCode);
            if (instance == null) {
                android.icu.util.Currency icuInstance = android.icu.util.Currency.getInstance(currencyCode);
                if (icuInstance == null) {
                    return null;
                }
                instance = new Currency(icuInstance);
                instances.put(currencyCode, instance);
            }
        }
    }

    public static Currency getInstance(Locale locale) {
        android.icu.util.Currency icuInstance = android.icu.util.Currency.getInstance(locale);
        String variant = locale.getVariant();
        String country = locale.getCountry();
        if (!variant.isEmpty() && (variant.equals("EURO") || variant.equals("HK") || variant.equals("PREEURO"))) {
            country = country + BaseLocale.SEP + variant;
        }
        String currencyCode = ICU.getCurrencyCode(country);
        if (currencyCode == null) {
            throw new IllegalArgumentException("Unsupported ISO 3166 country: " + locale);
        } else if (icuInstance == null || icuInstance.getCurrencyCode().equals("XXX")) {
            return null;
        } else {
            return getInstance(currencyCode);
        }
    }

    public static Set<Currency> getAvailableCurrencies() {
        Set<Currency> set;
        synchronized (Currency.class) {
            if (available == null) {
                Set<android.icu.util.Currency> icuAvailableCurrencies = android.icu.util.Currency.getAvailableCurrencies();
                available = new HashSet();
                for (android.icu.util.Currency icuCurrency : icuAvailableCurrencies) {
                    Currency currency = getInstance(icuCurrency.getCurrencyCode());
                    if (currency == null) {
                        currency = new Currency(icuCurrency);
                        instances.put(currency.currencyCode, currency);
                    }
                    available.add(currency);
                }
            }
            set = (Set) available.clone();
        }
        return set;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public String getSymbol() {
        return this.icuCurrency.getSymbol();
    }

    public String getSymbol(Locale locale) {
        if (locale != null) {
            return this.icuCurrency.getSymbol(locale);
        }
        throw new NullPointerException("locale == null");
    }

    public int getDefaultFractionDigits() {
        if (this.icuCurrency.getCurrencyCode().equals("XXX")) {
            return -1;
        }
        return this.icuCurrency.getDefaultFractionDigits();
    }

    public int getNumericCode() {
        return this.icuCurrency.getNumericCode();
    }

    public String getDisplayName() {
        return this.icuCurrency.getDisplayName();
    }

    public String getDisplayName(Locale locale) {
        return this.icuCurrency.getDisplayName(locale);
    }

    public String toString() {
        return this.icuCurrency.toString();
    }

    private Object readResolve() {
        return getInstance(this.currencyCode);
    }
}
