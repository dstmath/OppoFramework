package libcore.icu;

import android.icu.text.DateFormat;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import libcore.util.BasicLruCache;
import libcore.util.ZoneInfoDB;

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
public final class TimeZoneNames {
    public static final int LONG_NAME = 1;
    public static final int LONG_NAME_DST = 3;
    public static final int NAME_COUNT = 5;
    public static final int OLSON_NAME = 0;
    public static final int SHORT_NAME = 2;
    public static final int SHORT_NAME_DST = 4;
    private static final Comparator<String[]> ZONE_STRINGS_COMPARATOR = null;
    private static final String[] availableTimeZoneIds = null;
    private static final ZoneStringsCache cachedZoneStrings = null;

    public static class ZoneStringsCache extends BasicLruCache<Locale, String[][]> {
        public ZoneStringsCache() {
            super(5);
        }

        protected String[][] create(Locale locale) {
            long start = System.nanoTime();
            int[] iArr = new int[2];
            iArr[0] = TimeZoneNames.availableTimeZoneIds.length;
            iArr[1] = 5;
            String[][] result = (String[][]) Array.newInstance(String.class, iArr);
            for (int i = 0; i < TimeZoneNames.availableTimeZoneIds.length; i++) {
                result[i][0] = TimeZoneNames.availableTimeZoneIds[i];
            }
            long nativeStart = System.nanoTime();
            TimeZoneNames.fillZoneStrings(locale.toLanguageTag(), result);
            long nativeEnd = System.nanoTime();
            internStrings(result);
            long end = System.nanoTime();
            System.logI("Loaded time zone names for \"" + locale + "\" in " + TimeUnit.NANOSECONDS.toMillis(end - start) + DateFormat.MINUTE_SECOND + " (" + TimeUnit.NANOSECONDS.toMillis(nativeEnd - nativeStart) + "ms in ICU)");
            return result;
        }

        private synchronized void internStrings(String[][] result) {
            HashMap<String, String> internTable = new HashMap();
            for (int i = 0; i < result.length; i++) {
                for (int j = 1; j < 5; j++) {
                    String original = result[i][j];
                    String nonDuplicate = (String) internTable.get(original);
                    if (nonDuplicate == null) {
                        internTable.put(original, original);
                    } else {
                        result[i][j] = nonDuplicate;
                    }
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: libcore.icu.TimeZoneNames.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: libcore.icu.TimeZoneNames.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.icu.TimeZoneNames.<clinit>():void");
    }

    private static native void fillZoneStrings(String str, String[][] strArr);

    public static native String getExemplarLocation(String str, String str2);

    private TimeZoneNames() {
    }

    public static String getDisplayName(String[][] zoneStrings, String id, boolean daylight, int style) {
        String[] needle = new String[1];
        needle[0] = id;
        int index = Arrays.binarySearch(zoneStrings, needle, ZONE_STRINGS_COMPARATOR);
        if (index < 0) {
            return null;
        }
        String[] row = zoneStrings[index];
        if (daylight) {
            return style == 1 ? row[3] : row[4];
        }
        return style == 1 ? row[1] : row[2];
    }

    public static String[][] getZoneStrings(Locale locale) {
        Object locale2;
        if (locale2 == null) {
            locale2 = Locale.getDefault();
        }
        return (String[][]) cachedZoneStrings.get(locale2);
    }

    public static String[] forLocale(Locale locale) {
        String countryCode = locale.getCountry();
        ArrayList<String> ids = new ArrayList();
        for (String line : ZoneInfoDB.getInstance().getZoneTab().split("\n")) {
            if (line.startsWith(countryCode)) {
                int olsonIdStart = line.indexOf(9, 4) + 1;
                int olsonIdEnd = line.indexOf(9, olsonIdStart);
                if (olsonIdEnd == -1) {
                    olsonIdEnd = line.length();
                }
                ids.add(line.substring(olsonIdStart, olsonIdEnd));
            }
        }
        return (String[]) ids.toArray(new String[ids.size()]);
    }
}
