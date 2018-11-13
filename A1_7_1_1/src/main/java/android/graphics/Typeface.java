package android.graphics;

import android.content.res.AssetManager;
import android.graphics.FontListParser.Alias;
import android.graphics.FontListParser.Config;
import android.graphics.FontListParser.Family;
import android.graphics.FontListParser.Font;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.provider.ContactsContract.Aas;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.LruCache;
import android.util.SparseArray;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oppo.content.res.OppoFontUtils;
import org.xmlpull.v1.XmlPullParserException;

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
public class Typeface {
    public static final int BOLD = 1;
    public static final int BOLD_ITALIC = 3;
    public static final Typeface COLOROSUI_LIGHT = null;
    public static final Typeface COLOROSUI_THIN = null;
    public static final Typeface COLOROSUI_XLIGHT = null;
    public static final Typeface COLOROSUI_XTHIN = null;
    public static final Typeface DEFAULT = null;
    public static final Typeface DEFAULT_BOLD = null;
    static final String FONTS_CONFIG = "fonts.xml";
    private static final String[] FontsLikeDefault = null;
    public static final int ITALIC = 2;
    public static final String LIGHT_PATH = "/system/fonts/Roboto-Light.ttf";
    public static final String MEDIUM_PATH = "/system/fonts/ColorOSUI-Medium.ttf";
    public static final String MEDIUM_PATH2 = "/system/fonts/NotoSansSC-Medium.otf";
    public static final Typeface MONOSPACE = null;
    public static final int NORMAL = 0;
    public static final Typeface SANS_SERIF = null;
    public static final Typeface SERIF = null;
    private static String TAG = null;
    public static final String THIN_PATH = "/system/fonts/Roboto-Thin.ttf";
    public static final String XLIGHT_PATH = "/system/fonts/ColorOSUI-XLight.ttf";
    public static final String XTHIN_PATH = "/system/fonts/ColorOSUI-XThin.ttf";
    static Typeface sDefaultTypeface;
    static Typeface[] sDefaults;
    private static final LruCache<String, Typeface> sDynamicTypefaceCache = null;
    static FontFamily[] sFallbackFonts;
    static Map<String, Typeface> sSystemFontMap;
    private static final LongSparseArray<SparseArray<Typeface>> sTypefaceCache = null;
    public boolean isLikeDefault;
    private int mStyle;
    public long native_instance;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.Typeface.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.Typeface.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.Typeface.<clinit>():void");
    }

    private static native void nativeClearEmojiFonts();

    private static native long nativeCreateFromArray(long[] jArr);

    private static native long nativeCreateFromTypeface(long j, int i);

    private static native long nativeCreateWeightAlias(long j, int i);

    private static native boolean nativeGetCanReplace(long j);

    private static native int nativeGetStyle(long j);

    private static native void nativeSetCanReplace(long j, boolean z);

    private static native void nativeSetDefault(long j);

    private static native void nativeUnref(long j);

    private static native long nativeUpdateEmojiFonts(String[] strArr);

    private static native void nativeUpdateLovelyfonts();

    private static void setDefault(Typeface t) {
        sDefaultTypeface = t;
        nativeSetDefault(t.native_instance);
    }

    public int getStyle() {
        return this.mStyle;
    }

    public final boolean isBold() {
        return (this.mStyle & 1) != 0;
    }

    public final boolean isItalic() {
        return (this.mStyle & 2) != 0;
    }

    public static Typeface create(String familyName, int style) {
        if (sSystemFontMap == null) {
            return null;
        }
        Typeface tf = create((Typeface) sSystemFontMap.get(familyName), style);
        int ix = 0;
        while (ix < FontsLikeDefault.length) {
            if (familyName != null && familyName.equals(FontsLikeDefault[ix])) {
                tf.isLikeDefault = true;
                break;
            }
            ix++;
        }
        return tf;
    }

    public static Typeface create(Typeface family, int style) {
        Typeface typeface;
        if (style < 0 || style > 3) {
            style = 0;
        }
        long ni = 0;
        if (family != null) {
            if (family.mStyle == style) {
                return family;
            }
            ni = family.native_instance;
        }
        SparseArray<Typeface> styles = (SparseArray) sTypefaceCache.get(ni);
        if (styles != null) {
            typeface = (Typeface) styles.get(style);
            if (typeface != null) {
                return typeface;
            }
        }
        typeface = new Typeface(nativeCreateFromTypeface(ni, style));
        if (styles == null) {
            styles = new SparseArray(4);
            sTypefaceCache.put(ni, styles);
        }
        styles.put(style, typeface);
        return typeface;
    }

    public static Typeface defaultFromStyle(int style) {
        if (OppoFontUtils.isFlipFontUsed) {
            return OppoFontUtils.flipTypeface(sDefaults[style]);
        }
        return sDefaults[style];
    }

    public static Typeface createFromAsset(AssetManager mgr, String path) {
        if (sFallbackFonts != null) {
            synchronized (sDynamicTypefaceCache) {
                String key = createAssetUid(mgr, path);
                Typeface typeface = (Typeface) sDynamicTypefaceCache.get(key);
                if (typeface != null) {
                    return typeface;
                }
                FontFamily fontFamily = new FontFamily();
                if (fontFamily.addFontFromAsset(mgr, path)) {
                    FontFamily[] families = new FontFamily[1];
                    families[0] = fontFamily;
                    typeface = createFromFamiliesWithDefault(families);
                    if (WifiEnterpriseConfig.ENGINE_ENABLE.equals(SystemProperties.get("ro.lovelyfonts_support", WifiEnterpriseConfig.ENGINE_DISABLE))) {
                        fontFamily.setCanBeReplaced(false);
                        nativeSetCanReplace(typeface.native_instance, false);
                    }
                    sDynamicTypefaceCache.put(key, typeface);
                    return typeface;
                }
            }
        }
        throw new RuntimeException("Font asset not found " + path);
    }

    private static String createAssetUid(AssetManager mgr, String path) {
        SparseArray<String> pkgs = mgr.getAssignedPackageIdentifiers();
        StringBuilder builder = new StringBuilder();
        int size = pkgs.size();
        for (int i = 0; i < size; i++) {
            builder.append((String) pkgs.valueAt(i));
            builder.append(Aas.ENCODE_SYMBOL);
        }
        builder.append(path);
        return builder.toString();
    }

    public static Typeface createFromFile(File path) {
        return createFromFile(path.getAbsolutePath());
    }

    public static Typeface createFromFile(String path) {
        if (path != null) {
            if (XTHIN_PATH.equals(path)) {
                return COLOROSUI_XTHIN;
            }
            if (MEDIUM_PATH.equals(path) || MEDIUM_PATH2.equals(path)) {
                return DEFAULT_BOLD;
            }
            if (THIN_PATH.equals(path)) {
                return COLOROSUI_THIN;
            }
            if (LIGHT_PATH.equals(path)) {
                return COLOROSUI_LIGHT;
            }
            if (XLIGHT_PATH.equals(path)) {
                return COLOROSUI_XLIGHT;
            }
        }
        if (sFallbackFonts != null) {
            FontFamily fontFamily = new FontFamily();
            if (fontFamily.addFont(path, 0)) {
                FontFamily[] families = new FontFamily[1];
                families[0] = fontFamily;
                if (!WifiEnterpriseConfig.ENGINE_ENABLE.equals(SystemProperties.get("ro.lovelyfonts_support", WifiEnterpriseConfig.ENGINE_DISABLE))) {
                    return createFromFamiliesWithDefault(families);
                }
                fontFamily.setCanBeReplaced(false);
                Typeface typeface = createFromFamiliesWithDefault(families);
                nativeSetCanReplace(typeface.native_instance, false);
                return typeface;
            }
        }
        throw new RuntimeException("Font not found " + path);
    }

    public static Typeface createFromFamilies(FontFamily[] families) {
        long[] ptrArray = new long[families.length];
        for (int i = 0; i < families.length; i++) {
            ptrArray[i] = families[i].mNativePtr;
        }
        return new Typeface(nativeCreateFromArray(ptrArray));
    }

    public static Typeface createFromFamiliesWithDefault(FontFamily[] families) {
        int i;
        long[] ptrArray = new long[(families.length + sFallbackFonts.length)];
        for (i = 0; i < families.length; i++) {
            ptrArray[i] = families[i].mNativePtr;
        }
        for (i = 0; i < sFallbackFonts.length; i++) {
            ptrArray[families.length + i] = sFallbackFonts[i].mNativePtr;
        }
        return new Typeface(nativeCreateFromArray(ptrArray));
    }

    private Typeface(long ni) {
        this.mStyle = 0;
        this.isLikeDefault = false;
        if (ni == 0) {
            throw new RuntimeException("native typeface cannot be made");
        }
        this.native_instance = ni;
        this.mStyle = nativeGetStyle(ni);
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x008a A:{SYNTHETIC, Splitter: B:26:0x008a} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x009d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x008f A:{SYNTHETIC, Splitter: B:29:0x008f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static FontFamily makeFamilyFromParsed(Family family, Map<String, ByteBuffer> bufferForPath) {
        Throwable th;
        Throwable th2;
        FontFamily fontFamily = new FontFamily(family.lang, family.variant);
        for (Font font : family.fonts) {
            ByteBuffer fontBuffer = (ByteBuffer) bufferForPath.get(font.fontName);
            if (fontBuffer == null) {
                Throwable th3 = null;
                FileInputStream file = null;
                try {
                    FileInputStream fileInputStream = new FileInputStream(font.fontName);
                    try {
                        FileChannel fileChannel = fileInputStream.getChannel();
                        fontBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
                        bufferForPath.put(font.fontName, fontBuffer);
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable th4) {
                                th3 = th4;
                            }
                        }
                        if (th3 != null) {
                            try {
                                throw th3;
                            } catch (IOException e) {
                                file = fileInputStream;
                            }
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        th2 = null;
                        file = fileInputStream;
                        if (file != null) {
                            try {
                                file.close();
                            } catch (Throwable th6) {
                                if (th2 == null) {
                                    th2 = th6;
                                } else if (th2 != th6) {
                                    th2.addSuppressed(th6);
                                }
                            }
                        }
                        if (th2 == null) {
                            try {
                                throw th2;
                            } catch (IOException e2) {
                                Log.e(TAG, "Error mapping font file " + font.fontName);
                            }
                        } else {
                            throw th;
                        }
                    }
                } catch (Throwable th7) {
                    th = th7;
                    th2 = null;
                    if (file != null) {
                    }
                    if (th2 == null) {
                    }
                }
            }
            if (!fontFamily.addFontWeightStyle(fontBuffer, font.ttcIndex, font.axes, font.weight, font.isItalic)) {
                Log.e(TAG, "Error creating font " + font.fontName + "#" + font.ttcIndex);
            }
        }
        return fontFamily;
    }

    public static void init() {
        File configFilename = new File(getSystemFontConfigLocation(), FONTS_CONFIG);
        try {
            int i;
            Family f;
            Config fontConfig = FontListParser.parse(new FileInputStream(configFilename));
            Map<String, ByteBuffer> bufferForPath = new HashMap();
            List<FontFamily> familyList = new ArrayList();
            for (i = 0; i < fontConfig.families.size(); i++) {
                f = (Family) fontConfig.families.get(i);
                if (i == 0 || f.name == null) {
                    familyList.add(makeFamilyFromParsed(f, bufferForPath));
                }
            }
            sFallbackFonts = (FontFamily[]) familyList.toArray(new FontFamily[familyList.size()]);
            setDefault(createFromFamilies(sFallbackFonts));
            Map<String, Typeface> systemFonts = new HashMap();
            for (i = 0; i < fontConfig.families.size(); i++) {
                f = (Family) fontConfig.families.get(i);
                if (f.name != null) {
                    Typeface typeface;
                    if (i == 0) {
                        typeface = sDefaultTypeface;
                    } else {
                        FontFamily[] families = new FontFamily[1];
                        families[0] = makeFamilyFromParsed(f, bufferForPath);
                        typeface = createFromFamiliesWithDefault(families);
                    }
                    systemFonts.put(f.name, typeface);
                }
            }
            for (Alias alias : fontConfig.aliases) {
                Typeface base = (Typeface) systemFonts.get(alias.toName);
                Typeface newFace = base;
                int weight = alias.weight;
                if (weight != 400) {
                    Typeface typeface2 = new Typeface(nativeCreateWeightAlias(base.native_instance, weight));
                }
                systemFonts.put(alias.name, newFace);
            }
            sSystemFontMap = systemFonts;
        } catch (RuntimeException e) {
            Log.w(TAG, "Didn't create default family (most likely, non-Minikin build)", e);
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "Error opening " + configFilename, e2);
        } catch (IOException e3) {
            Log.e(TAG, "Error reading " + configFilename, e3);
        } catch (XmlPullParserException e4) {
            Log.e(TAG, "XML parse exception for " + configFilename, e4);
        }
    }

    private static Typeface createOsFontFromFile(String path) {
        if (sFallbackFonts != null) {
            FontFamily fontFamily = new FontFamily();
            if (fontFamily.addFont(path, 0)) {
                FontFamily[] families = new FontFamily[1];
                families[0] = fontFamily;
                return createFromFamiliesWithDefault(families);
            }
        }
        throw new RuntimeException("Font not found " + path);
    }

    private static File getSystemFontConfigLocation() {
        return new File("/system/etc/");
    }

    protected void finalize() throws Throwable {
        try {
            nativeUnref(this.native_instance);
            this.native_instance = 0;
        } finally {
            super.finalize();
        }
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Typeface typeface = (Typeface) o;
        if (!(this.mStyle == typeface.mStyle && this.native_instance == typeface.native_instance)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((((int) (this.native_instance ^ (this.native_instance >>> 32))) + 527) * 31) + this.mStyle;
    }

    public static void updateLovelyFonts() {
        nativeUpdateLovelyfonts();
        Canvas.freeCaches();
        Canvas.freeTextLayoutCaches();
    }

    public static void updateLovelyEmojis() {
        nativeClearEmojiFonts();
        nativeUpdateEmojiFonts(null);
        Canvas.freeCaches();
        Canvas.freeTextLayoutCaches();
    }

    public void setCanBeReplaced(boolean can) {
        nativeSetCanReplace(this.native_instance, can);
    }
}
