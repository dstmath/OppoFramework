package android.text;

import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.telephony.PhoneConstants;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.Locale;
import java.util.Locale.Builder;

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
public class Hyphenator {
    private static final String[][] LOCALE_FALLBACK_DATA = null;
    private static String TAG;
    static final Hyphenator sEmptyHyphenator = null;
    private static final Object sLock = null;
    @GuardedBy("sLock")
    static final HashMap<Locale, Hyphenator> sMap = null;
    private final ByteBuffer mBuffer;
    private final long mNativePtr;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.text.Hyphenator.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.text.Hyphenator.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.Hyphenator.<clinit>():void");
    }

    private Hyphenator(long nativePtr, ByteBuffer b) {
        this.mNativePtr = nativePtr;
        this.mBuffer = b;
    }

    public long getNativePtr() {
        return this.mNativePtr;
    }

    public static Hyphenator get(Locale locale) {
        synchronized (sLock) {
            Hyphenator result = (Hyphenator) sMap.get(locale);
            if (result != null) {
                return result;
            }
            String variant = locale.getVariant();
            if (!variant.isEmpty()) {
                result = (Hyphenator) sMap.get(new Locale(locale.getLanguage(), PhoneConstants.MVNO_TYPE_NONE, variant));
                if (result != null) {
                    sMap.put(locale, result);
                    return result;
                }
            }
            result = (Hyphenator) sMap.get(new Locale(locale.getLanguage()));
            if (result != null) {
                sMap.put(locale, result);
                return result;
            }
            String script = locale.getScript();
            if (!script.equals(PhoneConstants.MVNO_TYPE_NONE)) {
                result = (Hyphenator) sMap.get(new Builder().setLanguage("und").setScript(script).build());
                if (result != null) {
                    sMap.put(locale, result);
                    return result;
                }
            }
            sMap.put(locale, sEmptyHyphenator);
            return sEmptyHyphenator;
        }
    }

    private static Hyphenator loadHyphenator(String languageTag) {
        File patternFile = new File(getSystemHyphenatorLocation(), "hyph-" + languageTag.toLowerCase(Locale.US) + ".hyb");
        RandomAccessFile f;
        try {
            f = new RandomAccessFile(patternFile, "r");
            FileChannel fc = f.getChannel();
            MappedByteBuffer buf = fc.map(MapMode.READ_ONLY, 0, fc.size());
            Hyphenator hyphenator = new Hyphenator(StaticLayout.nLoadHyphenator(buf, 0), buf);
            f.close();
            return hyphenator;
        } catch (IOException e) {
            Log.e(TAG, "error loading hyphenation " + patternFile, e);
            return null;
        } catch (Throwable th) {
            f.close();
        }
    }

    private static File getSystemHyphenatorLocation() {
        return new File("/system/usr/hyphen-data");
    }

    public static void init() {
        int i;
        sMap.put(null, null);
        String[] availableLanguages = new String[33];
        availableLanguages[0] = "as";
        availableLanguages[1] = "bn";
        availableLanguages[2] = "cy";
        availableLanguages[3] = "da";
        availableLanguages[4] = "de-1901";
        availableLanguages[5] = "de-1996";
        availableLanguages[6] = "de-CH-1901";
        availableLanguages[7] = "en-GB";
        availableLanguages[8] = "en-US";
        availableLanguages[9] = "es";
        availableLanguages[10] = "et";
        availableLanguages[11] = "eu";
        availableLanguages[12] = "fr";
        availableLanguages[13] = "ga";
        availableLanguages[14] = "gu";
        availableLanguages[15] = "hi";
        availableLanguages[16] = "hr";
        availableLanguages[17] = "hu";
        availableLanguages[18] = "hy";
        availableLanguages[19] = "kn";
        availableLanguages[20] = "ml";
        availableLanguages[21] = "mn-Cyrl";
        availableLanguages[22] = "mr";
        availableLanguages[23] = "nb";
        availableLanguages[24] = "nn";
        availableLanguages[25] = "or";
        availableLanguages[26] = "pa";
        availableLanguages[27] = "pt";
        availableLanguages[28] = "sl";
        availableLanguages[29] = "ta";
        availableLanguages[30] = "te";
        availableLanguages[31] = "tk";
        availableLanguages[32] = "und-Ethi";
        for (String languageTag : availableLanguages) {
            Hyphenator h = loadHyphenator(languageTag);
            if (h != null) {
                sMap.put(Locale.forLanguageTag(languageTag), h);
            }
        }
        for (i = 0; i < LOCALE_FALLBACK_DATA.length; i++) {
            sMap.put(Locale.forLanguageTag(LOCALE_FALLBACK_DATA[i][0]), (Hyphenator) sMap.get(Locale.forLanguageTag(LOCALE_FALLBACK_DATA[i][1])));
        }
    }
}
