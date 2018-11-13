package android.icu.text;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUResourceBundle;
import android.icu.lang.UCharacter;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;

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
public class NumberingSystem {
    private static ICUCache<String, NumberingSystem> cachedLocaleData;
    private static ICUCache<String, NumberingSystem> cachedStringData;
    private boolean algorithmic;
    private String desc;
    private String name;
    private int radix;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.NumberingSystem.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.NumberingSystem.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NumberingSystem.<clinit>():void");
    }

    public NumberingSystem() {
        this.radix = 10;
        this.algorithmic = false;
        this.desc = "0123456789";
        this.name = "latn";
    }

    public static NumberingSystem getInstance(int radix_in, boolean isAlgorithmic_in, String desc_in) {
        return getInstance(null, radix_in, isAlgorithmic_in, desc_in);
    }

    private static NumberingSystem getInstance(String name_in, int radix_in, boolean isAlgorithmic_in, String desc_in) {
        if (radix_in < 2) {
            throw new IllegalArgumentException("Invalid radix for numbering system");
        } else if (isAlgorithmic_in || (desc_in.length() == radix_in && isValidDigitString(desc_in))) {
            NumberingSystem ns = new NumberingSystem();
            ns.radix = radix_in;
            ns.algorithmic = isAlgorithmic_in;
            ns.desc = desc_in;
            ns.name = name_in;
            return ns;
        } else {
            throw new IllegalArgumentException("Invalid digit string for numbering system");
        }
    }

    public static NumberingSystem getInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale));
    }

    public static NumberingSystem getInstance(ULocale locale) {
        NumberingSystem ns;
        String[] OTHER_NS_KEYWORDS = new String[3];
        OTHER_NS_KEYWORDS[0] = "native";
        OTHER_NS_KEYWORDS[1] = "traditional";
        OTHER_NS_KEYWORDS[2] = "finance";
        Boolean nsResolved = Boolean.valueOf(true);
        String numbersKeyword = locale.getKeywordValue("numbers");
        if (numbersKeyword != null) {
            for (String keyword : OTHER_NS_KEYWORDS) {
                if (numbersKeyword.equals(keyword)) {
                    nsResolved = Boolean.valueOf(false);
                    break;
                }
            }
        } else {
            numbersKeyword = "default";
            nsResolved = Boolean.valueOf(false);
        }
        if (nsResolved.booleanValue()) {
            ns = getInstanceByName(numbersKeyword);
            if (ns != null) {
                return ns;
            }
            numbersKeyword = "default";
            nsResolved = Boolean.valueOf(false);
        }
        String baseName = locale.getBaseName();
        ns = (NumberingSystem) cachedLocaleData.get(baseName + "@numbers=" + numbersKeyword);
        if (ns != null) {
            return ns;
        }
        String originalNumbersKeyword = numbersKeyword;
        String resolvedNumberingSystem = null;
        while (!nsResolved.booleanValue()) {
            try {
                resolvedNumberingSystem = ((ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", locale)).getWithFallback("NumberElements").getStringWithFallback(numbersKeyword);
                nsResolved = Boolean.valueOf(true);
            } catch (MissingResourceException e) {
                if (numbersKeyword.equals("native") || numbersKeyword.equals("finance")) {
                    numbersKeyword = "default";
                } else if (numbersKeyword.equals("traditional")) {
                    numbersKeyword = "native";
                } else {
                    nsResolved = Boolean.valueOf(true);
                }
            }
        }
        if (resolvedNumberingSystem != null) {
            ns = getInstanceByName(resolvedNumberingSystem);
        }
        if (ns == null) {
            ns = new NumberingSystem();
        }
        cachedLocaleData.put(baseName + "@numbers=" + originalNumbersKeyword, ns);
        return ns;
    }

    public static NumberingSystem getInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT));
    }

    public static NumberingSystem getInstanceByName(String name) {
        NumberingSystem ns = (NumberingSystem) cachedStringData.get(name);
        if (ns != null) {
            return ns;
        }
        try {
            UResourceBundle nsTop = UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", "numberingSystems").get("numberingSystems").get(name);
            ns = getInstance(name, nsTop.get("radix").getInt(), nsTop.get("algorithmic").getInt() == 1, nsTop.getString("desc"));
            cachedStringData.put(name, ns);
            return ns;
        } catch (MissingResourceException e) {
            return null;
        }
    }

    public static String[] getAvailableNames() {
        UResourceBundle nsCurrent = UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", "numberingSystems").get("numberingSystems");
        ArrayList<String> output = new ArrayList();
        UResourceBundleIterator it = nsCurrent.getIterator();
        while (it.hasNext()) {
            output.add(it.next().getKey());
        }
        return (String[]) output.toArray(new String[output.size()]);
    }

    public static boolean isValidDigitString(String str) {
        int i = 0;
        UCharacterIterator it = UCharacterIterator.getInstance(str);
        it.setToStart();
        while (true) {
            int c = it.nextCodePoint();
            if (c != -1) {
                if (UCharacter.isSupplementary(c)) {
                    return false;
                }
                i++;
            } else if (i != 10) {
                return false;
            } else {
                return true;
            }
        }
    }

    public int getRadix() {
        return this.radix;
    }

    public String getDescription() {
        return this.desc;
    }

    public String getName() {
        return this.name;
    }

    public boolean isAlgorithmic() {
        return this.algorithmic;
    }
}
