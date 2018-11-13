package android.icu.util;

import android.icu.text.DateFormat;
import java.util.concurrent.ConcurrentHashMap;

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
public final class VersionInfo implements Comparable<VersionInfo> {
    @Deprecated
    public static final VersionInfo ICU_DATA_VERSION = null;
    @Deprecated
    public static final String ICU_DATA_VERSION_PATH = "56b";
    public static final VersionInfo ICU_VERSION = null;
    private static final String INVALID_VERSION_NUMBER_ = "Invalid version number: Version number may be negative or greater than 255";
    private static final int LAST_BYTE_MASK_ = 255;
    private static final ConcurrentHashMap<Integer, VersionInfo> MAP_ = null;
    private static volatile String TZDATA_VERSION;
    public static final VersionInfo UCOL_BUILDER_VERSION = null;
    public static final VersionInfo UCOL_RUNTIME_VERSION = null;
    @Deprecated
    public static final VersionInfo UCOL_TAILORINGS_VERSION = null;
    public static final VersionInfo UNICODE_1_0 = null;
    public static final VersionInfo UNICODE_1_0_1 = null;
    public static final VersionInfo UNICODE_1_1_0 = null;
    public static final VersionInfo UNICODE_1_1_5 = null;
    public static final VersionInfo UNICODE_2_0 = null;
    public static final VersionInfo UNICODE_2_1_2 = null;
    public static final VersionInfo UNICODE_2_1_5 = null;
    public static final VersionInfo UNICODE_2_1_8 = null;
    public static final VersionInfo UNICODE_2_1_9 = null;
    public static final VersionInfo UNICODE_3_0 = null;
    public static final VersionInfo UNICODE_3_0_1 = null;
    public static final VersionInfo UNICODE_3_1_0 = null;
    public static final VersionInfo UNICODE_3_1_1 = null;
    public static final VersionInfo UNICODE_3_2 = null;
    public static final VersionInfo UNICODE_4_0 = null;
    public static final VersionInfo UNICODE_4_0_1 = null;
    public static final VersionInfo UNICODE_4_1 = null;
    public static final VersionInfo UNICODE_5_0 = null;
    public static final VersionInfo UNICODE_5_1 = null;
    public static final VersionInfo UNICODE_5_2 = null;
    public static final VersionInfo UNICODE_6_0 = null;
    public static final VersionInfo UNICODE_6_1 = null;
    public static final VersionInfo UNICODE_6_2 = null;
    public static final VersionInfo UNICODE_6_3 = null;
    public static final VersionInfo UNICODE_7_0 = null;
    public static final VersionInfo UNICODE_8_0 = null;
    private static final VersionInfo UNICODE_VERSION = null;
    private static volatile VersionInfo javaVersion;
    private int m_version_;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.util.VersionInfo.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.util.VersionInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.VersionInfo.<clinit>():void");
    }

    public static VersionInfo getInstance(String version) {
        int length = version.length();
        int[] array = new int[]{0, 0, 0, 0};
        int count = 0;
        int index = 0;
        while (count < 4 && index < length) {
            char c = version.charAt(index);
            if (c == '.') {
                count++;
            } else {
                c = (char) (c - 48);
                if (c < 0 || c > 9) {
                    throw new IllegalArgumentException(INVALID_VERSION_NUMBER_);
                }
                array[count] = array[count] * 10;
                array[count] = array[count] + c;
            }
            index++;
        }
        if (index != length) {
            throw new IllegalArgumentException("Invalid version number: String '" + version + "' exceeds version format");
        }
        int i = 0;
        while (i < 4) {
            if (array[i] < 0 || array[i] > 255) {
                throw new IllegalArgumentException(INVALID_VERSION_NUMBER_);
            }
            i++;
        }
        return getInstance(array[0], array[1], array[2], array[3]);
    }

    public static VersionInfo getInstance(int major, int minor, int milli, int micro) {
        if (major < 0 || major > 255 || minor < 0 || minor > 255 || milli < 0 || milli > 255 || micro < 0 || micro > 255) {
            throw new IllegalArgumentException(INVALID_VERSION_NUMBER_);
        }
        int version = getInt(major, minor, milli, micro);
        Integer key = Integer.valueOf(version);
        VersionInfo result = (VersionInfo) MAP_.get(key);
        if (result != null) {
            return result;
        }
        result = new VersionInfo(version);
        VersionInfo tmpvi = (VersionInfo) MAP_.putIfAbsent(key, result);
        if (tmpvi != null) {
            return tmpvi;
        }
        return result;
    }

    public static VersionInfo getInstance(int major, int minor, int milli) {
        return getInstance(major, minor, milli, 0);
    }

    public static VersionInfo getInstance(int major, int minor) {
        return getInstance(major, minor, 0, 0);
    }

    public static VersionInfo getInstance(int major) {
        return getInstance(major, 0, 0, 0);
    }

    @Deprecated
    public static VersionInfo javaVersion() {
        if (javaVersion == null) {
            synchronized (VersionInfo.class) {
                if (javaVersion == null) {
                    char[] chars = System.getProperty("java.version").toCharArray();
                    int r = 0;
                    int w = 0;
                    int count = 0;
                    boolean numeric = false;
                    while (true) {
                        int w2 = w;
                        int r2 = r;
                        if (r2 >= chars.length) {
                            w = w2;
                            r = r2;
                            break;
                        }
                        r = r2 + 1;
                        char c = chars[r2];
                        if (c >= '0' && c <= '9') {
                            numeric = true;
                            w = w2 + 1;
                            chars[w2] = c;
                        } else if (!numeric) {
                            w = w2;
                        } else if (count == 3) {
                            w = w2;
                            break;
                        } else {
                            numeric = false;
                            w = w2 + 1;
                            chars[w2] = '.';
                            count++;
                        }
                    }
                    while (w > 0 && chars[w - 1] == '.') {
                        w--;
                    }
                    javaVersion = getInstance(new String(chars, 0, w));
                }
            }
        }
        return javaVersion;
    }

    public String toString() {
        StringBuilder result = new StringBuilder(7);
        result.append(getMajor());
        result.append('.');
        result.append(getMinor());
        result.append('.');
        result.append(getMilli());
        result.append('.');
        result.append(getMicro());
        return result.toString();
    }

    public int getMajor() {
        return (this.m_version_ >> 24) & 255;
    }

    public int getMinor() {
        return (this.m_version_ >> 16) & 255;
    }

    public int getMilli() {
        return (this.m_version_ >> 8) & 255;
    }

    public int getMicro() {
        return this.m_version_ & 255;
    }

    public boolean equals(Object other) {
        return other == this;
    }

    public int compareTo(VersionInfo other) {
        return this.m_version_ - other.m_version_;
    }

    private VersionInfo(int compactversion) {
        this.m_version_ = compactversion;
    }

    private static int getInt(int major, int minor, int milli, int micro) {
        return (((major << 24) | (minor << 16)) | (milli << 8)) | micro;
    }

    public static void main(String[] args) {
        String icuApiVer;
        if (ICU_VERSION.getMajor() <= 4) {
            if (ICU_VERSION.getMinor() % 2 != 0) {
                int major = ICU_VERSION.getMajor();
                int minor = ICU_VERSION.getMinor() + 1;
                if (minor >= 10) {
                    minor -= 10;
                    major++;
                }
                icuApiVer = "" + major + "." + minor + DateFormat.NUM_MONTH + ICU_VERSION.getMilli();
            } else {
                icuApiVer = ICU_VERSION.getVersionString(2, 2);
            }
        } else if (ICU_VERSION.getMinor() == 0) {
            icuApiVer = "" + ICU_VERSION.getMajor() + DateFormat.NUM_MONTH + ICU_VERSION.getMilli();
        } else {
            icuApiVer = ICU_VERSION.getVersionString(2, 2);
        }
        System.out.println("International Components for Unicode for Java " + icuApiVer);
        System.out.println("");
        System.out.println("Implementation Version: " + ICU_VERSION.getVersionString(2, 4));
        System.out.println("Unicode Data Version:   " + UNICODE_VERSION.getVersionString(2, 4));
        System.out.println("CLDR Data Version:      " + LocaleData.getCLDRVersion().getVersionString(2, 4));
        System.out.println("Time Zone Data Version: " + getTZDataVersion());
    }

    @Deprecated
    public String getVersionString(int minDigits, int maxDigits) {
        if (minDigits < 1 || maxDigits < 1 || minDigits > 4 || maxDigits > 4 || minDigits > maxDigits) {
            throw new IllegalArgumentException("Invalid min/maxDigits range");
        }
        int[] digits = new int[4];
        digits[0] = getMajor();
        digits[1] = getMinor();
        digits[2] = getMilli();
        digits[3] = getMicro();
        int numDigits = maxDigits;
        while (numDigits > minDigits && digits[numDigits - 1] == 0) {
            numDigits--;
        }
        StringBuilder verStr = new StringBuilder(7);
        verStr.append(digits[0]);
        for (int i = 1; i < numDigits; i++) {
            verStr.append(".");
            verStr.append(digits[i]);
        }
        return verStr.toString();
    }

    static String getTZDataVersion() {
        if (TZDATA_VERSION == null) {
            synchronized (VersionInfo.class) {
                if (TZDATA_VERSION == null) {
                    TZDATA_VERSION = UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", "zoneinfo64").getString("TZVersion");
                }
            }
        }
        return TZDATA_VERSION;
    }
}
