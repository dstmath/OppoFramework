package android_maps_conflict_avoidance.com.google.common;

import android_maps_conflict_avoidance.com.google.common.util.text.TextUtil;
import android_maps_conflict_avoidance.com.google.debug.DebugUtil;

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
public class I18n {
    private static String STRING_RESOURCE;
    private static I18n instance;
    private String[] embeddedLocalizedStrings;
    private String[] remoteLocalizedStrings;
    private String systemLanguage;
    private String systemLocale;
    private String uiLanguage;
    private String uiLocale;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.I18n.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.I18n.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.I18n.<clinit>():void");
    }

    public static String locale() {
        return DebugUtil.getAntPropertyOrNull("en");
    }

    public static I18n init(String downloadLocale) {
        instance = new I18n(downloadLocale);
        return instance;
    }

    public static String normalizeLocale(String rawLocale) {
        String locale = "en";
        if (rawLocale == null) {
            return locale;
        }
        String[] localeParts = TextUtil.split(rawLocale.replace('-', '_'), '_');
        if (localeParts[0].length() != 2 && localeParts[0].length() != 3) {
            return locale;
        }
        locale = localeParts[0].toLowerCase();
        if (localeParts.length < 2 || localeParts[1].length() != 2) {
            return locale;
        }
        return locale + "_" + localeParts[1].toUpperCase();
    }

    private static String calculateSystemLocale(String downloadLocale) {
        downloadLocale = normalizeLocale(downloadLocale);
        String locale = normalizeLocale(System.getProperty("microedition.locale"));
        if ("en".equals(locale) || (locale.length() == 2 && downloadLocale.startsWith(locale))) {
            return downloadLocale;
        }
        return locale;
    }

    I18n(String downloadLocale) {
        this.embeddedLocalizedStrings = null;
        this.remoteLocalizedStrings = null;
        setSystemLocale(calculateSystemLocale(downloadLocale));
        setUiLocale(locale());
    }

    public String getUiLocale() {
        return this.uiLocale;
    }

    public void setSystemLocale(String locale) {
        this.systemLocale = normalizeLocale(locale);
        int split = this.systemLocale.indexOf(95);
        this.systemLanguage = split < 0 ? this.systemLocale : this.systemLocale.substring(0, split);
    }

    public void setUiLocale(String locale) {
        this.uiLocale = locale != null ? normalizeLocale(locale) : this.systemLocale;
        this.uiLanguage = getLanguage(this.uiLocale);
    }

    public static String getLanguage(String locale) {
        int split = locale.indexOf(95);
        if (split < 0) {
            split = locale.indexOf(45);
        }
        return split < 0 ? locale : locale.substring(0, split);
    }
}
