package android.graphics;

import android.content.res.AssetManager.AssetInputStream;
import android.content.res.Resources;
import android.graphics.Bitmap.Config;
import android.os.Process;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import android.util.TypedValue;
import com.mediatek.dcfdecoder.DcfDecoder;
import com.oppo.hypnus.HypnusManager;
import com.oppo.luckymoney.LMManager;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
public class BitmapFactory {
    private static final int DECODE_BUFFER_SIZE = 65536;
    private static HypnusManager mHM;

    public static class Options {
        public Bitmap inBitmap;
        public int inDensity;
        public boolean inDither = false;
        @Deprecated
        public boolean inInputShareable;
        public boolean inJustDecodeBounds;
        public boolean inMutable;
        public boolean inPostProc = false;
        public int inPostProcFlag = 0;
        public boolean inPreferQualityOverSpeed;
        public int inPreferSize = 0;
        public Config inPreferredConfig = Config.ARGB_8888;
        public boolean inPremultiplied = true;
        @Deprecated
        public boolean inPurgeable;
        public int inSampleSize;
        public boolean inScaled = true;
        public int inScreenDensity;
        public int inTargetDensity;
        public byte[] inTempStorage;
        public boolean mCancel;
        public int outHeight;
        public String outMimeType;
        public int outWidth;

        public void requestCancelDecode() {
            this.mCancel = true;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.BitmapFactory.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.BitmapFactory.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.BitmapFactory.<clinit>():void");
    }

    private static native Bitmap nativeDecodeAsset(long j, Rect rect, Options options);

    private static native Bitmap nativeDecodeByteArray(byte[] bArr, int i, int i2, Options options);

    private static native Bitmap nativeDecodeFileDescriptor(FileDescriptor fileDescriptor, Rect rect, Options options);

    private static native Bitmap nativeDecodeStream(InputStream inputStream, byte[] bArr, Rect rect, Options options);

    private static native boolean nativeIsSeekable(FileDescriptor fileDescriptor);

    /* JADX WARNING: Removed duplicated region for block: B:21:0x003b A:{SYNTHETIC, Splitter: B:21:0x003b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Bitmap decodeFile(String pathName, Options opts) {
        Exception e;
        Throwable th;
        Bitmap bm = null;
        InputStream stream = null;
        try {
            InputStream stream2 = new FileInputStream(pathName);
            try {
                bm = decodeStream(stream2, null, opts);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e2) {
                    }
                }
                stream = stream2;
            } catch (Exception e3) {
                e = e3;
                stream = stream2;
            } catch (Throwable th2) {
                th = th2;
                stream = stream2;
                if (stream != null) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            try {
                Log.e("BitmapFactory", "Unable to decode stream: " + e);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e5) {
                    }
                }
                return bm;
            } catch (Throwable th3) {
                th = th3;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6) {
                    }
                }
                throw th;
            }
        }
        return bm;
    }

    public static Bitmap decodeFile(String pathName) {
        return decodeFile(pathName, null);
    }

    public static Bitmap decodeResourceStream(Resources res, TypedValue value, InputStream is, Rect pad, Options opts) {
        if (opts == null) {
            opts = new Options();
        }
        if (opts.inDensity == 0 && value != null) {
            int density = value.density;
            if (density == 0) {
                opts.inDensity = 160;
            } else if (density != 65535) {
                opts.inDensity = density;
            }
        }
        if (opts.inTargetDensity == 0 && res != null) {
            opts.inTargetDensity = res.getDisplayMetrics().densityDpi;
        }
        return decodeStream(is, pad, opts);
    }

    public static Bitmap decodeResource(Resources res, int id, Options opts) {
        Bitmap bm = null;
        InputStream inputStream = null;
        try {
            TypedValue value = new TypedValue();
            inputStream = res.openRawResource(id, value);
            bm = decodeResourceStream(res, value, inputStream, null, opts);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        } catch (Exception e2) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                }
            }
        }
        if (bm != null || opts == null || opts.inBitmap == null) {
            return bm;
        }
        throw new IllegalArgumentException("Problem decoding into existing bitmap");
    }

    public static Bitmap decodeResource(Resources res, int id) {
        return decodeResource(res, id, null);
    }

    public static Bitmap decodeByteArray(byte[] data, int offset, int length, Options opts) {
        if ((offset | length) < 0 || data.length < offset + length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        String calling = checkAppPackageName();
        if (opts != null && calling.startsWith("com.tencent.mm")) {
            opts.inPostProc = true;
        }
        Trace.traceBegin(2, "decodeBitmap");
        if (mHM == null && SystemClock.uptimeMillis() > 100000) {
            mHM = new HypnusManager();
        }
        if (!(mHM == null || opts == null || opts.inJustDecodeBounds || length <= 524288)) {
            mHM.hypnusSetAction(20, length >> 13);
        }
        try {
            Bitmap bm = nativeDecodeByteArray(data, offset, length, opts);
            if (bm != null || opts == null || opts.inBitmap == null) {
                setDensityFromOptions(bm, opts);
                if (bm == null) {
                    return DcfDecoder.decodeDrmImageIfNeeded(data, opts);
                }
                return bm;
            }
            throw new IllegalArgumentException("Problem decoding into existing bitmap");
        } finally {
            Trace.traceEnd(2);
        }
    }

    public static Bitmap decodeByteArray(byte[] data, int offset, int length) {
        return decodeByteArray(data, offset, length, null);
    }

    private static void setDensityFromOptions(Bitmap outputBitmap, Options opts) {
        if (outputBitmap != null && opts != null) {
            int density = opts.inDensity;
            if (density != 0) {
                outputBitmap.setDensity(density);
                int targetDensity = opts.inTargetDensity;
                if (targetDensity != 0 && density != targetDensity && density != opts.inScreenDensity) {
                    byte[] np = outputBitmap.getNinePatchChunk();
                    boolean isNinePatch = np != null ? NinePatch.isNinePatchChunk(np) : false;
                    if (opts.inScaled || isNinePatch) {
                        outputBitmap.setDensity(targetDensity);
                    }
                }
            } else if (opts.inBitmap != null) {
                outputBitmap.setDensity(Bitmap.getDefaultDensity());
            }
        }
    }

    public static Bitmap decodeStream(InputStream is, Rect outPadding, Options opts) {
        if (is == null) {
            return null;
        }
        String calling = checkAppPackageName();
        boolean isMM = false;
        if (opts != null && calling.startsWith("com.tencent.mm")) {
            opts.inPostProc = true;
            isMM = true;
        }
        Bitmap bm = null;
        Trace.traceBegin(2, "decodeBitmap");
        try {
            if (is instanceof AssetInputStream) {
                bm = nativeDecodeAsset(((AssetInputStream) is).getNativeAsset(), outPadding, opts);
            } else {
                bm = decodeStreamInternal(is, outPadding, opts);
            }
            if (bm != null || opts == null || opts.inBitmap == null) {
                if (isMM && bm != null) {
                    LMManager lm = LMManager.getLMManager();
                    if (LMManager.sBoostMode == 2 && LMManager.getNewMsgDetected() && bm.checkLM(LMManager.sMODE_2_VALUE_HB_HASH, LMManager.sGetHash, LMManager.sMODE_2_VALUE_HB_WIDTH, LMManager.sMODE_2_VALUE_HB_HEIGHT)) {
                        lm.enableBoost(0, 2014);
                        LMManager.setNewMsgDetected(null, false);
                    }
                }
                setDensityFromOptions(bm, opts);
                return bm;
            }
            throw new IllegalArgumentException("Problem decoding into existing bitmap");
        } finally {
            Trace.traceEnd(2);
        }
    }

    private static Bitmap decodeStreamInternal(InputStream is, Rect outPadding, Options opts) {
        int size;
        byte[] tempStorage = null;
        if (opts != null) {
            tempStorage = opts.inTempStorage;
        }
        if (tempStorage == null) {
            tempStorage = new byte[65536];
        }
        if (mHM == null && SystemClock.uptimeMillis() > 100000) {
            mHM = new HypnusManager();
        }
        try {
            size = is.available();
        } catch (IOException e) {
            size = 0;
            Log.e("BitmapFactory", "decodeStreamInternal is.available err!");
        }
        if (!(mHM == null || opts == null || opts.inJustDecodeBounds || size <= 524288)) {
            mHM.hypnusSetAction(20, size >> 13);
        }
        Bitmap bm = nativeDecodeStream(is, tempStorage, outPadding, opts);
        if (bm == null) {
            return DcfDecoder.decodeDrmImageIfNeeded(tempStorage, is, opts);
        }
        return bm;
    }

    public static Bitmap decodeStream(InputStream is) {
        return decodeStream(is, null, null);
    }

    public static Bitmap decodeFileDescriptor(FileDescriptor fd, Rect outPadding, Options opts) {
        Trace.traceBegin(2, "decodeFileDescriptor");
        FileInputStream fis;
        try {
            Bitmap bm;
            if (nativeIsSeekable(fd)) {
                bm = nativeDecodeFileDescriptor(fd, outPadding, opts);
            } else {
                fis = new FileInputStream(fd);
                bm = decodeStreamInternal(fis, outPadding, opts);
                try {
                    fis.close();
                } catch (Throwable th) {
                }
            }
            if (bm != null || opts == null || opts.inBitmap == null) {
                setDensityFromOptions(bm, opts);
                Trace.traceEnd(2);
                if (bm == null) {
                    return DcfDecoder.decodeDrmImageIfNeeded(fd, opts);
                }
                return bm;
            }
            throw new IllegalArgumentException("Problem decoding into existing bitmap");
        } catch (Throwable th2) {
            Trace.traceEnd(2);
        }
    }

    public static Bitmap decodeFileDescriptor(FileDescriptor fd) {
        return decodeFileDescriptor(fd, null, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0045 A:{SYNTHETIC, Splitter: B:17:0x0045} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x004e A:{SYNTHETIC, Splitter: B:22:0x004e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String checkAppPackageName() {
        Throwable th;
        String callingApp = "";
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream("/proc/" + Process.myPid() + "/cmdline");
            try {
                byte[] buffer = new byte[50];
                int count = fis2.read(buffer);
                if (count > 0) {
                    callingApp = new String(buffer, 0, count);
                }
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (Exception e) {
                    }
                }
                fis = fis2;
            } catch (Exception e2) {
                fis = fis2;
                if (fis != null) {
                }
                return callingApp;
            } catch (Throwable th2) {
                th = th2;
                fis = fis2;
                if (fis != null) {
                }
                throw th;
            }
        } catch (Exception e3) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e4) {
                }
            }
            return callingApp;
        } catch (Throwable th3) {
            th = th3;
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e5) {
                }
            }
            throw th;
        }
        return callingApp;
    }
}
