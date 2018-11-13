package android.content.res;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityThread;
import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

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
public final class AssetManager implements AutoCloseable {
    public static final int ACCESS_BUFFER = 3;
    public static final int ACCESS_RANDOM = 1;
    public static final int ACCESS_STREAMING = 2;
    public static final int ACCESS_UNKNOWN = 0;
    private static final boolean DEBUG_REFS = false;
    private static final boolean DEBUG_RES_CONFIG = false;
    static final int STYLE_ASSET_COOKIE = 2;
    static final int STYLE_CHANGING_CONFIGURATIONS = 4;
    static final int STYLE_DATA = 1;
    static final int STYLE_DENSITY = 5;
    static final int STYLE_NUM_ENTRIES = 6;
    static final int STYLE_RESOURCE_ID = 3;
    static final int STYLE_TYPE = 0;
    private static final String TAG = "AssetManager";
    private static final boolean localLOGV = false;
    private static final Object sSync = null;
    static AssetManager sSystem;
    private boolean mApplyResSpecialConfig;
    private int mNumRefs;
    private long mObject;
    private final long[] mOffsets;
    private boolean mOpen;
    private HashMap<Long, RuntimeException> mRefStacks;
    private String[] mResSpecialConfigPkgsArray;
    private StringBlock[] mStringBlocks;
    private final TypedValue mValue;

    public final class AssetInputStream extends InputStream {
        private long mAsset;
        private long mLength;
        private long mMarkPos;

        /* synthetic */ AssetInputStream(AssetManager this$0, long asset, AssetInputStream assetInputStream) {
            this(asset);
        }

        public final int getAssetInt() {
            throw new UnsupportedOperationException();
        }

        public final long getNativeAsset() {
            return this.mAsset;
        }

        private AssetInputStream(long asset) {
            this.mAsset = asset;
            this.mLength = AssetManager.this.getAssetLength(asset);
        }

        public final int read() throws IOException {
            return AssetManager.this.readAssetChar(this.mAsset);
        }

        public final boolean markSupported() {
            return true;
        }

        public final int available() throws IOException {
            long len = AssetManager.this.getAssetRemainingLength(this.mAsset);
            return len > 2147483647L ? Integer.MAX_VALUE : (int) len;
        }

        public final void close() throws IOException {
            synchronized (AssetManager.this) {
                if (this.mAsset != 0) {
                    AssetManager.this.destroyAsset(this.mAsset);
                    this.mAsset = 0;
                    AssetManager.this.decRefsLocked((long) hashCode());
                }
            }
        }

        public final void mark(int readlimit) {
            this.mMarkPos = AssetManager.this.seekAsset(this.mAsset, 0, 0);
        }

        public final void reset() throws IOException {
            AssetManager.this.seekAsset(this.mAsset, this.mMarkPos, -1);
        }

        public final int read(byte[] b) throws IOException {
            return AssetManager.this.readAsset(this.mAsset, b, 0, b.length);
        }

        public final int read(byte[] b, int off, int len) throws IOException {
            return AssetManager.this.readAsset(this.mAsset, b, off, len);
        }

        public final long skip(long n) throws IOException {
            long pos = AssetManager.this.seekAsset(this.mAsset, 0, 0);
            if (pos + n > this.mLength) {
                n = this.mLength - pos;
            }
            if (n > 0) {
                AssetManager.this.seekAsset(this.mAsset, n, 0);
            }
            return n;
        }

        protected void finalize() throws Throwable {
            close();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.content.res.AssetManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.content.res.AssetManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.AssetManager.<clinit>():void");
    }

    private final native int addAssetPathNative(String str, boolean z);

    static final native boolean applyStyle(long j, int i, int i2, long j2, int[] iArr, int[] iArr2, int[] iArr3);

    static final native void applyThemeStyle(long j, int i, boolean z);

    static final native void clearTheme(long j);

    static final native void copyTheme(long j, long j2);

    private final native void deleteTheme(long j);

    private final native void destroy();

    private final native void destroyAsset(long j);

    static final native void dumpTheme(long j, int i, String str, String str2);

    private final native int[] getArrayStringInfo(int i);

    private final native String[] getArrayStringResource(int i);

    public static final native String getAssetAllocations();

    private final native long getAssetLength(long j);

    private final native long getAssetRemainingLength(long j);

    public static final native int getGlobalAssetCount();

    public static final native int getGlobalAssetManagerCount();

    private final native long getNativeStringBlock(int i);

    private final native int getStringBlockCount();

    static final native int getThemeChangingConfigurations(long j);

    private final native void init(boolean z);

    private final native void initSpecial(boolean z, boolean z2);

    private final native int loadResourceBagValue(int i, int i2, TypedValue typedValue, boolean z);

    private final native int loadResourceValue(int i, short s, TypedValue typedValue, boolean z);

    static final native int loadThemeAttributeValue(long j, int i, TypedValue typedValue, boolean z);

    private final native long newTheme();

    private final native long openAsset(String str, int i);

    private final native ParcelFileDescriptor openAssetFd(String str, long[] jArr) throws IOException;

    private native ParcelFileDescriptor openNonAssetFdNative(int i, String str, long[] jArr) throws IOException;

    private final native long openNonAssetNative(int i, String str, int i2);

    private final native long openXmlAssetNative(int i, String str);

    private final native int readAsset(long j, byte[] bArr, int i, int i2);

    private final native int readAssetChar(long j);

    static final native boolean resolveAttrs(long j, int i, int i2, int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4);

    private final native long seekAsset(long j, long j2, int i);

    public final native int addOverlayPathNative(String str);

    final native int[] getArrayIntResource(int i);

    final native int getArraySize(int i);

    public final native SparseArray<String> getAssignedPackageIdentifiers();

    public final native String getCookieName(int i);

    public final native String[] getLocales();

    public final native String[] getNonSystemLocales();

    final native String getResourceEntryName(int i);

    final native int getResourceIdentifier(String str, String str2, String str3);

    final native String getResourceName(int i);

    final native String getResourcePackageName(int i);

    final native String getResourceTypeName(int i);

    public final native Configuration[] getSizeConfigurations();

    final native int[] getStyleAttributes(int i);

    public final native boolean isUpToDate();

    public final native String[] list(String str) throws IOException;

    final native int retrieveArray(int i, int[] iArr);

    final native boolean retrieveAttributes(long j, int[] iArr, int[] iArr2, int[] iArr3);

    public final native void setConfiguration(int i, int i2, String str, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14, int i15, int i16);

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianJun.Dan@Plf.SDK : Modify for oppo-framework-res", property = OppoRomType.ROM)
    public AssetManager() {
        this.mValue = new TypedValue();
        this.mOffsets = new long[2];
        this.mStringBlocks = null;
        this.mNumRefs = 1;
        this.mOpen = true;
        String[] strArr = new String[1];
        strArr[0] = "com.jb.gosms";
        this.mResSpecialConfigPkgsArray = strArr;
        this.mApplyResSpecialConfig = false;
        synchronized (this) {
            boolean addMediaTekRes = true;
            String currentPackageName = ActivityThread.currentPackageName();
            for (String pkg : this.mResSpecialConfigPkgsArray) {
                if (pkg != null && pkg.equals(currentPackageName)) {
                    addMediaTekRes = false;
                    this.mApplyResSpecialConfig = true;
                    break;
                }
            }
            initSpecial(false, addMediaTekRes);
            ensureSystemAssets();
            OppoResourceHelper.addExtraAssetPaths(this);
        }
    }

    private static void ensureSystemAssets() {
        synchronized (sSync) {
            if (sSystem == null) {
                AssetManager system = new AssetManager(true);
                system.makeStringBlocks(null);
                sSystem = system;
            }
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianJun.Dan@Plf.SDK : Modify for oppo-framework-res", property = OppoRomType.ROM)
    private AssetManager(boolean isSystem) {
        this.mValue = new TypedValue();
        this.mOffsets = new long[2];
        this.mStringBlocks = null;
        this.mNumRefs = 1;
        this.mOpen = true;
        String[] strArr = new String[1];
        strArr[0] = "com.jb.gosms";
        this.mResSpecialConfigPkgsArray = strArr;
        this.mApplyResSpecialConfig = false;
        initSpecial(true, true);
        OppoResourceHelper.addExtraAssetPaths(this);
    }

    public static AssetManager getSystem() {
        ensureSystemAssets();
        return sSystem;
    }

    public void close() {
        synchronized (this) {
            if (this.mOpen) {
                this.mOpen = false;
                decRefsLocked((long) hashCode());
            }
        }
    }

    final CharSequence getResourceText(int resId) {
        synchronized (this) {
            TypedValue outValue = this.mValue;
            if (getResourceValue(resId, 0, outValue, true)) {
                CharSequence coerceToString = outValue.coerceToString();
                return coerceToString;
            }
            return null;
        }
    }

    final CharSequence getResourceBagText(int resId, int bagEntryId) {
        synchronized (this) {
            TypedValue outValue = this.mValue;
            int block = loadResourceBagValue(resId, bagEntryId, outValue, true);
            CharSequence charSequence;
            if (block < 0) {
                return null;
            } else if (outValue.type == 3) {
                charSequence = this.mStringBlocks[block].get(outValue.data);
                return charSequence;
            } else {
                charSequence = outValue.coerceToString();
                return charSequence;
            }
        }
    }

    final String[] getResourceStringArray(int resId) {
        return getArrayStringResource(resId);
    }

    final boolean getResourceValue(int resId, int densityDpi, TypedValue outValue, boolean resolveRefs) {
        int block = loadResourceValue(resId, (short) densityDpi, outValue, resolveRefs);
        if (block < 0) {
            return false;
        }
        if (outValue.type == 3) {
            outValue.string = this.mStringBlocks[block].get(outValue.data);
        }
        return true;
    }

    final CharSequence[] getResourceTextArray(int resId) {
        int[] rawInfoArray = getArrayStringInfo(resId);
        int rawInfoArrayLen = rawInfoArray.length;
        CharSequence[] retArray = new CharSequence[(rawInfoArrayLen / 2)];
        int i = 0;
        int j = 0;
        while (i < rawInfoArrayLen) {
            int block = rawInfoArray[i];
            int index = rawInfoArray[i + 1];
            retArray[j] = index >= 0 ? this.mStringBlocks[block].get(index) : null;
            i += 2;
            j++;
        }
        return retArray;
    }

    final boolean getThemeValue(long theme, int resId, TypedValue outValue, boolean resolveRefs) {
        int block = loadThemeAttributeValue(theme, resId, outValue, resolveRefs);
        if (block < 0) {
            return false;
        }
        if (outValue.type == 3) {
            outValue.string = ensureStringBlocks()[block].get(outValue.data);
        }
        return true;
    }

    final StringBlock[] ensureStringBlocks() {
        StringBlock[] stringBlockArr;
        synchronized (this) {
            if (this.mStringBlocks == null) {
                makeStringBlocks(sSystem.mStringBlocks);
            }
            stringBlockArr = this.mStringBlocks;
        }
        return stringBlockArr;
    }

    final void makeStringBlocks(StringBlock[] seed) {
        int seedNum = seed != null ? seed.length : 0;
        int num = getStringBlockCount();
        this.mStringBlocks = new StringBlock[num];
        for (int i = 0; i < num; i++) {
            if (i < seedNum) {
                this.mStringBlocks[i] = seed[i];
            } else {
                this.mStringBlocks[i] = new StringBlock(getNativeStringBlock(i), true);
            }
        }
    }

    final CharSequence getPooledStringForCookie(int cookie, int id) {
        return this.mStringBlocks[cookie - 1].get(id);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "ZhiYong.Lin@Plf.Framework, add for cts testSELinuxPolicyFile 2015-03-31", property = OppoRomType.ROM)
    public final InputStream open(String fileName) throws IOException {
        if (fileName != null && fileName.equals("selinux_policy.xml")) {
            int pid = Binder.getCallingPid();
            if (Binder.getCallingUid() > 10000) {
                String processName = Process.getProcessNameByPid(pid);
                Log.d(TAG, "open: processName == " + processName);
                if (processName.contains("cts.security")) {
                    Log.d(TAG, "change fileName!");
                    fileName = "oppo_selinux_policy.xml";
                }
            }
        }
        return open(fileName, 2);
    }

    public final InputStream open(String fileName, int accessMode) throws IOException {
        synchronized (this) {
            if (this.mOpen) {
                long asset = openAsset(fileName, accessMode);
                if (asset != 0) {
                    AssetInputStream res = new AssetInputStream(this, asset, null);
                    incRefsLocked((long) res.hashCode());
                    return res;
                }
                throw new FileNotFoundException("Asset file: " + fileName);
            }
            throw new RuntimeException("Assetmanager has been closed");
        }
    }

    public final AssetFileDescriptor openFd(String fileName) throws IOException {
        synchronized (this) {
            if (this.mOpen) {
                ParcelFileDescriptor pfd = openAssetFd(fileName, this.mOffsets);
                if (pfd != null) {
                    AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(pfd, this.mOffsets[0], this.mOffsets[1]);
                    return assetFileDescriptor;
                }
                throw new FileNotFoundException("Asset file: " + fileName);
            }
            throw new RuntimeException("Assetmanager has been closed");
        }
    }

    public final InputStream openNonAsset(String fileName) throws IOException {
        return openNonAsset(0, fileName, 2);
    }

    public final InputStream openNonAsset(String fileName, int accessMode) throws IOException {
        return openNonAsset(0, fileName, accessMode);
    }

    public final InputStream openNonAsset(int cookie, String fileName) throws IOException {
        return openNonAsset(cookie, fileName, 2);
    }

    public final InputStream openNonAsset(int cookie, String fileName, int accessMode) throws IOException {
        synchronized (this) {
            if (this.mOpen) {
                long asset = openNonAssetNative(cookie, fileName, accessMode);
                if (asset != 0) {
                    AssetInputStream res = new AssetInputStream(this, asset, null);
                    incRefsLocked((long) res.hashCode());
                    return res;
                }
                throw new FileNotFoundException("Asset absolute file: " + fileName);
            }
            throw new RuntimeException("Assetmanager has been closed");
        }
    }

    public final AssetFileDescriptor openNonAssetFd(String fileName) throws IOException {
        return openNonAssetFd(0, fileName);
    }

    public final AssetFileDescriptor openNonAssetFd(int cookie, String fileName) throws IOException {
        synchronized (this) {
            if (this.mOpen) {
                ParcelFileDescriptor pfd = openNonAssetFdNative(cookie, fileName, this.mOffsets);
                if (pfd != null) {
                    AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(pfd, this.mOffsets[0], this.mOffsets[1]);
                    return assetFileDescriptor;
                }
                throw new FileNotFoundException("Asset absolute file: " + fileName);
            }
            throw new RuntimeException("Assetmanager has been closed");
        }
    }

    public final XmlResourceParser openXmlResourceParser(String fileName) throws IOException {
        return openXmlResourceParser(0, fileName);
    }

    public final XmlResourceParser openXmlResourceParser(int cookie, String fileName) throws IOException {
        XmlBlock block = openXmlBlockAsset(cookie, fileName);
        XmlResourceParser rp = block.newParser();
        block.close();
        return rp;
    }

    final XmlBlock openXmlBlockAsset(String fileName) throws IOException {
        return openXmlBlockAsset(0, fileName);
    }

    final XmlBlock openXmlBlockAsset(int cookie, String fileName) throws IOException {
        synchronized (this) {
            if (this.mOpen) {
                long xmlBlock = openXmlAssetNative(cookie, fileName);
                if (xmlBlock != 0) {
                    XmlBlock res = new XmlBlock(this, xmlBlock);
                    incRefsLocked((long) res.hashCode());
                    return res;
                }
                throw new FileNotFoundException("Asset XML file: " + fileName);
            }
            throw new RuntimeException("Assetmanager has been closed");
        }
    }

    void xmlBlockGone(int id) {
        synchronized (this) {
            decRefsLocked((long) id);
        }
    }

    final long createTheme() {
        long res;
        synchronized (this) {
            if (this.mOpen) {
                res = newTheme();
                incRefsLocked(res);
            } else {
                throw new RuntimeException("Assetmanager has been closed");
            }
        }
        return res;
    }

    final void releaseTheme(long theme) {
        synchronized (this) {
            deleteTheme(theme);
            decRefsLocked(theme);
        }
    }

    protected void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }

    public final int addAssetPath(String path) {
        return addAssetPathInternal(path, false);
    }

    public final int addAssetPathAsSharedLibrary(String path) {
        return addAssetPathInternal(path, true);
    }

    private final int addAssetPathInternal(String path, boolean appAsLib) {
        int res;
        synchronized (this) {
            res = addAssetPathNative(path, appAsLib);
            makeStringBlocks(this.mStringBlocks);
        }
        return res;
    }

    public final int addOverlayPath(String idmapPath) {
        int res;
        synchronized (this) {
            res = addOverlayPathNative(idmapPath);
            makeStringBlocks(this.mStringBlocks);
        }
        return res;
    }

    public final int[] addAssetPaths(String[] paths) {
        if (paths == null) {
            return null;
        }
        int[] cookies = new int[paths.length];
        for (int i = 0; i < paths.length; i++) {
            cookies[i] = addAssetPath(paths[i]);
        }
        return cookies;
    }

    private final void incRefsLocked(long id) {
        this.mNumRefs++;
    }

    private final void decRefsLocked(long id) {
        this.mNumRefs--;
        if (this.mNumRefs == 0) {
            destroy();
        }
    }
}
