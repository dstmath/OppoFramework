package android.graphics;

import android.content.res.AssetManager;
import android.graphics.FontListParser.Axis;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.List;

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
public class FontFamily {
    private static String TAG;
    public long mNativePtr;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.FontFamily.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.FontFamily.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.FontFamily.<clinit>():void");
    }

    private static native boolean nAddFont(long j, ByteBuffer byteBuffer, int i);

    private static native boolean nAddFontFromAsset(long j, AssetManager assetManager, String str);

    private static native boolean nAddFontWeightStyle(long j, ByteBuffer byteBuffer, int i, List<Axis> list, int i2, boolean z);

    private static native long nCreateFamily(String str, int i);

    private static native void nSetCanBeReplaced(long j, boolean z);

    private static native void nUnrefFamily(long j);

    public FontFamily() {
        this.mNativePtr = nCreateFamily(null, 0);
        if (this.mNativePtr == 0) {
            throw new IllegalStateException("error creating native FontFamily");
        }
    }

    public FontFamily(String lang, String variant) {
        int varEnum = 0;
        if ("compact".equals(variant)) {
            varEnum = 1;
        } else if ("elegant".equals(variant)) {
            varEnum = 2;
        }
        this.mNativePtr = nCreateFamily(lang, varEnum);
        if (this.mNativePtr == 0) {
            throw new IllegalStateException("error creating native FontFamily");
        }
    }

    protected void finalize() throws Throwable {
        try {
            nUnrefFamily(this.mNativePtr);
        } finally {
            super.finalize();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x004d A:{SYNTHETIC, Splitter: B:23:0x004d} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0060 A:{Catch:{ IOException -> 0x0053 }} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0052 A:{SYNTHETIC, Splitter: B:26:0x0052} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean addFont(String path, int ttcIndex) {
        Throwable th;
        Throwable th2;
        Throwable th3 = null;
        FileInputStream file = null;
        try {
            FileInputStream file2 = new FileInputStream(path);
            try {
                FileChannel fileChannel = file2.getChannel();
                boolean nAddFont = nAddFont(this.mNativePtr, fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size()), ttcIndex);
                if (file2 != null) {
                    try {
                        file2.close();
                    } catch (Throwable th4) {
                        th3 = th4;
                    }
                }
                if (th3 == null) {
                    return nAddFont;
                }
                try {
                    throw th3;
                } catch (IOException e) {
                    file = file2;
                }
            } catch (Throwable th5) {
                th = th5;
                th2 = null;
                file = file2;
                if (file != null) {
                }
                if (th2 == null) {
                }
            }
        } catch (Throwable th6) {
            th = th6;
            th2 = null;
            if (file != null) {
                try {
                    file.close();
                } catch (Throwable th7) {
                    if (th2 == null) {
                        th2 = th7;
                    } else if (th2 != th7) {
                        th2.addSuppressed(th7);
                    }
                }
            }
            if (th2 == null) {
                try {
                    throw th2;
                } catch (IOException e2) {
                    Log.e(TAG, "Error mapping font file " + path);
                    return false;
                }
            }
            throw th;
        }
    }

    public boolean addFontWeightStyle(ByteBuffer font, int ttcIndex, List<Axis> axes, int weight, boolean style) {
        return nAddFontWeightStyle(this.mNativePtr, font, ttcIndex, axes, weight, style);
    }

    public boolean addFontFromAsset(AssetManager mgr, String path) {
        return nAddFontFromAsset(this.mNativePtr, mgr, path);
    }

    public void setCanBeReplaced(boolean can) {
        nSetCanBeReplaced(this.mNativePtr, can);
    }
}
