package android.icu.impl;

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
public class LocaleIDs {
    private static final String[] _countries = null;
    private static final String[] _countries3 = null;
    private static final String[] _deprecatedCountries = null;
    private static final String[] _languages = null;
    private static final String[] _languages3 = null;
    private static final String[] _obsoleteCountries = null;
    private static final String[] _obsoleteCountries3 = null;
    private static final String[] _obsoleteLanguages = null;
    private static final String[] _obsoleteLanguages3 = null;
    private static final String[] _replacementCountries = null;
    private static final String[] _replacementLanguages = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.LocaleIDs.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.LocaleIDs.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.LocaleIDs.<clinit>():void");
    }

    public static String[] getISOCountries() {
        return (String[]) _countries.clone();
    }

    public static String[] getISOLanguages() {
        return (String[]) _languages.clone();
    }

    public static String getISO3Country(String country) {
        int offset = findIndex(_countries, country);
        if (offset >= 0) {
            return _countries3[offset];
        }
        offset = findIndex(_obsoleteCountries, country);
        if (offset >= 0) {
            return _obsoleteCountries3[offset];
        }
        return "";
    }

    public static String getISO3Language(String language) {
        int offset = findIndex(_languages, language);
        if (offset >= 0) {
            return _languages3[offset];
        }
        offset = findIndex(_obsoleteLanguages, language);
        if (offset >= 0) {
            return _obsoleteLanguages3[offset];
        }
        return "";
    }

    public static String threeToTwoLetterLanguage(String lang) {
        int offset = findIndex(_languages3, lang);
        if (offset >= 0) {
            return _languages[offset];
        }
        offset = findIndex(_obsoleteLanguages3, lang);
        if (offset >= 0) {
            return _obsoleteLanguages[offset];
        }
        return null;
    }

    public static String threeToTwoLetterRegion(String region) {
        int offset = findIndex(_countries3, region);
        if (offset >= 0) {
            return _countries[offset];
        }
        offset = findIndex(_obsoleteCountries3, region);
        if (offset >= 0) {
            return _obsoleteCountries[offset];
        }
        return null;
    }

    private static int findIndex(String[] array, String target) {
        for (int i = 0; i < array.length; i++) {
            if (target.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static String getCurrentCountryID(String oldID) {
        int offset = findIndex(_deprecatedCountries, oldID);
        if (offset >= 0) {
            return _replacementCountries[offset];
        }
        return oldID;
    }

    public static String getCurrentLanguageID(String oldID) {
        int offset = findIndex(_obsoleteLanguages, oldID);
        if (offset >= 0) {
            return _replacementLanguages[offset];
        }
        return oldID;
    }
}
