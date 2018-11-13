package android.icu.impl;

import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.util.MissingResourceException;

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
public class CalendarUtil {
    private static final String CALKEY = "calendar";
    private static ICUCache<String, String> CALTYPE_CACHE = null;
    private static final String DEFCAL = "gregorian";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.CalendarUtil.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.CalendarUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.CalendarUtil.<clinit>():void");
    }

    public static String getCalendarType(ULocale loc) {
        String calType = loc.getKeywordValue(CALKEY);
        if (calType != null) {
            return calType;
        }
        String baseLoc = loc.getBaseName();
        calType = (String) CALTYPE_CACHE.get(baseLoc);
        if (calType != null) {
            return calType;
        }
        ULocale canonical = ULocale.createCanonical(loc.toString());
        calType = canonical.getKeywordValue(CALKEY);
        if (calType == null) {
            String region = canonical.getCountry();
            if (region.length() == 0) {
                region = ULocale.addLikelySubtags(canonical).getCountry();
            }
            try {
                UResourceBundle order;
                UResourceBundle calPref = UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("calendarPreferenceData");
                try {
                    order = calPref.get(region);
                } catch (MissingResourceException e) {
                    order = calPref.get("001");
                }
                calType = order.getString(0);
            } catch (MissingResourceException e2) {
            }
            if (calType == null) {
                calType = DEFCAL;
            }
        }
        CALTYPE_CACHE.put(baseLoc, calType);
        return calType;
    }
}
