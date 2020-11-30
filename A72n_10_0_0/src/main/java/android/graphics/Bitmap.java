package android.graphics;

import android.annotation.UnsupportedAppUsage;
import android.common.OppoFeatureCache;
import android.content.res.ResourcesImpl;
import android.graphics.ColorSpace;
import android.graphics.NinePatch;
import android.hardware.HardwareBuffer;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.Trace;
import android.telecom.Logging.Session;
import android.util.DisplayMetrics;
import android.util.Half;
import android.util.Log;
import android.view.ThreadedRenderer;
import com.color.darkmode.IColorDarkModeManager;
import com.oppo.hypnus.Hypnus;
import com.oppo.hypnus.HypnusManager;
import com.oppo.luckymoney.LMManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import libcore.util.NativeAllocationRegistry;

public final class Bitmap extends OppoBaseBitmap implements Parcelable {
    public static final Parcelable.Creator<Bitmap> CREATOR = new Parcelable.Creator<Bitmap>() {
        /* class android.graphics.Bitmap.AnonymousClass4 */

        @Override // android.os.Parcelable.Creator
        public Bitmap createFromParcel(Parcel p) {
            Bitmap bm = Bitmap.nativeCreateFromParcel(p);
            if (bm != null) {
                return bm;
            }
            throw new RuntimeException("Failed to unparcel Bitmap");
        }

        @Override // android.os.Parcelable.Creator
        public Bitmap[] newArray(int size) {
            return new Bitmap[size];
        }
    };
    public static final int DENSITY_NONE = 0;
    private static final String LM_HASH_DIR = "/sdcard/tencent/MicroMsg/lmhash/";
    private static final long NATIVE_ALLOCATION_SIZE = 32;
    private static final String TAG = "Bitmap";
    private static final int WORKING_COMPRESS_STORAGE = 4096;
    private static HypnusManager mHM = null;
    private static final int mMinHeightToTriggerMultiThreadDraw = 2160;
    private static final int mMinWidthToTriggerMultiThreadDraw = 2160;
    private static volatile int sDefaultDensity = -1;
    private static int sGetHashCount = 0;
    public static volatile int sPreloadTracingNumInstantiatedBitmaps;
    public static volatile long sPreloadTracingTotalBitmapsSize;
    private ColorSpace mColorSpace;
    public int mDensity;
    @UnsupportedAppUsage
    private int mHeight;
    @UnsupportedAppUsage
    private final long mNativePtr;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769491)
    private byte[] mNinePatchChunk;
    @UnsupportedAppUsage
    private NinePatch.InsetStruct mNinePatchInsets;
    private boolean mRecycled;
    private boolean mRequestPremultiplied;
    @UnsupportedAppUsage
    private int mWidth;

    private static native boolean nativeCheckLM(long j, byte[] bArr);

    private static native boolean nativeCompress(long j, int i, int i2, OutputStream outputStream, byte[] bArr);

    private static native ColorSpace nativeComputeColorSpace(long j);

    private static native int nativeConfig(long j);

    private static native Bitmap nativeCopy(long j, int i, boolean z);

    private static native Bitmap nativeCopyAshmem(long j);

    private static native Bitmap nativeCopyAshmemConfig(long j, int i);

    private static native void nativeCopyPixelsFromBuffer(long j, Buffer buffer);

    private static native void nativeCopyPixelsToBuffer(long j, Buffer buffer);

    private static native Bitmap nativeCopyPreserveInternalConfig(long j);

    private static native Bitmap nativeCreate(int[] iArr, int i, int i2, int i3, int i4, int i5, boolean z, long j);

    /* access modifiers changed from: private */
    public static native Bitmap nativeCreateFromParcel(Parcel parcel);

    private static native GraphicBuffer nativeCreateGraphicBufferHandle(long j);

    private static native void nativeDumpRawdata(long j, String str);

    private static native void nativeErase(long j, int i);

    private static native void nativeErase(long j, long j2, long j3);

    private static native Bitmap nativeExtractAlpha(long j, long j2, int[] iArr);

    private static native int nativeGenerationId(long j);

    private static native int nativeGetAllocationByteCount(long j);

    private static native long nativeGetColor(long j, int i, int i2);

    private static native long nativeGetNativeFinalizer();

    private static native int nativeGetPixel(long j, int i, int i2);

    private static native void nativeGetPixels(long j, int[] iArr, int i, int i2, int i3, int i4, int i5, int i6);

    private static native boolean nativeHasAlpha(long j);

    private static native boolean nativeHasMipMap(long j);

    private static native boolean nativeIsImmutable(long j);

    private static native boolean nativeIsPremultiplied(long j);

    private static native boolean nativeIsSRGB(long j);

    private static native boolean nativeIsSRGBLinear(long j);

    private static native void nativePrepareToDraw(long j);

    @UnsupportedAppUsage
    private static native void nativeReconfigure(long j, int i, int i2, int i3, boolean z);

    private static native void nativeRecycle(long j);

    private static native int nativeRowBytes(long j);

    private static native boolean nativeSameAs(long j, long j2);

    private static native void nativeSetColorSpace(long j, long j2);

    private static native void nativeSetHasAlpha(long j, boolean z, boolean z2);

    private static native void nativeSetHasMipMap(long j, boolean z);

    private static native void nativeSetImmutable(long j);

    private static native void nativeSetPixel(long j, int i, int i2, int i3);

    private static native void nativeSetPixels(long j, int[] iArr, int i, int i2, int i3, int i4, int i5, int i6);

    private static native void nativeSetPremultiplied(long j, boolean z);

    private static native Bitmap nativeWrapHardwareBufferBitmap(HardwareBuffer hardwareBuffer, long j);

    private static native boolean nativeWriteToParcel(long j, boolean z, int i, Parcel parcel);

    @UnsupportedAppUsage
    public static void setDefaultDensity(int density) {
        sDefaultDensity = density;
    }

    @UnsupportedAppUsage
    static int getDefaultDensity() {
        if (sDefaultDensity >= 0) {
            return sDefaultDensity;
        }
        sDefaultDensity = DisplayMetrics.DENSITY_DEVICE;
        return sDefaultDensity;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
    Bitmap(long nativeBitmap, int width, int height, int density, boolean requestPremultiplied, byte[] ninePatchChunk, NinePatch.InsetStruct ninePatchInsets) {
        this(nativeBitmap, width, height, density, requestPremultiplied, ninePatchChunk, ninePatchInsets, true);
    }

    Bitmap(long nativeBitmap, int width, int height, int density, boolean requestPremultiplied, byte[] ninePatchChunk, NinePatch.InsetStruct ninePatchInsets, boolean fromMalloc) {
        NativeAllocationRegistry registry;
        this.mDensity = getDefaultDensity();
        if (nativeBitmap != 0) {
            this.mWidth = width;
            this.mHeight = height;
            this.mRequestPremultiplied = requestPremultiplied;
            this.mNinePatchChunk = ninePatchChunk;
            this.mNinePatchInsets = ninePatchInsets;
            if (density >= 0) {
                this.mDensity = density;
            }
            this.mNativePtr = nativeBitmap;
            int allocationByteCount = getAllocationByteCount();
            if (fromMalloc) {
                registry = NativeAllocationRegistry.createMalloced(Bitmap.class.getClassLoader(), nativeGetNativeFinalizer(), (long) allocationByteCount);
            } else {
                registry = NativeAllocationRegistry.createNonmalloced(Bitmap.class.getClassLoader(), nativeGetNativeFinalizer(), (long) allocationByteCount);
            }
            registry.registerNativeAllocation(this, nativeBitmap);
            if (ResourcesImpl.TRACE_FOR_DETAILED_PRELOAD) {
                sPreloadTracingNumInstantiatedBitmaps++;
                sPreloadTracingTotalBitmapsSize += ((long) allocationByteCount) + 32;
                return;
            }
            return;
        }
        throw new RuntimeException("internal error: native bitmap is 0");
    }

    public long getNativeInstance() {
        return this.mNativePtr;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void reinit(int width, int height, boolean requestPremultiplied) {
        this.mWidth = width;
        this.mHeight = height;
        this.mRequestPremultiplied = requestPremultiplied;
        this.mColorSpace = null;
    }

    public int getDensity() {
        if (this.mRecycled) {
            Log.w(TAG, "Called getDensity() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return this.mDensity;
    }

    public void setDensity(int density) {
        this.mDensity = density;
    }

    public void reconfigure(int width, int height, Config config) {
        checkRecycled("Can't call reconfigure() on a recycled bitmap");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        } else if (isMutable()) {
            nativeReconfigure(this.mNativePtr, width, height, config.nativeInt, this.mRequestPremultiplied);
            this.mWidth = width;
            this.mHeight = height;
            this.mColorSpace = null;
        } else {
            throw new IllegalStateException("only mutable bitmaps may be reconfigured");
        }
    }

    public void setWidth(int width) {
        reconfigure(width, getHeight(), getConfig());
    }

    public void setHeight(int height) {
        reconfigure(getWidth(), height, getConfig());
    }

    public void setConfig(Config config) {
        reconfigure(getWidth(), getHeight(), config);
    }

    @UnsupportedAppUsage
    public void setNinePatchChunk(byte[] chunk) {
        this.mNinePatchChunk = chunk;
    }

    public void recycle() {
        if (!this.mRecycled) {
            nativeRecycle(this.mNativePtr);
            this.mNinePatchChunk = null;
            this.mRecycled = true;
        }
    }

    public final boolean isRecycled() {
        return this.mRecycled;
    }

    public int getGenerationId() {
        if (this.mRecycled) {
            Log.w(TAG, "Called getGenerationId() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return nativeGenerationId(this.mNativePtr);
    }

    private void checkRecycled(String errorMessage) {
        if (this.mRecycled) {
            throw new IllegalStateException(errorMessage);
        }
    }

    private void checkHardware(String errorMessage) {
        if (getConfig() == Config.HARDWARE) {
            throw new IllegalStateException(errorMessage);
        }
    }

    private static void checkXYSign(int x, int y) {
        if (x < 0) {
            throw new IllegalArgumentException("x must be >= 0");
        } else if (y < 0) {
            throw new IllegalArgumentException("y must be >= 0");
        }
    }

    private static void checkWidthHeight(int width, int height) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be > 0");
        } else if (height <= 0) {
            throw new IllegalArgumentException("height must be > 0");
        }
    }

    public enum Config {
        ALPHA_8(1),
        RGB_565(3),
        ARGB_4444(4),
        ARGB_8888(5),
        RGBA_F16(6),
        HARDWARE(7);
        
        private static Config[] sConfigs = {null, ALPHA_8, null, RGB_565, ARGB_4444, ARGB_8888, RGBA_F16, HARDWARE};
        @UnsupportedAppUsage
        final int nativeInt;

        private Config(int ni) {
            this.nativeInt = ni;
        }

        @UnsupportedAppUsage
        static Config nativeToConfig(int ni) {
            return sConfigs[ni];
        }
    }

    public void copyPixelsToBuffer(Buffer dst) {
        int shift;
        checkHardware("unable to copyPixelsToBuffer, pixel access is not supported on Config#HARDWARE bitmaps");
        int elements = dst.remaining();
        if (dst instanceof ByteBuffer) {
            shift = 0;
        } else if (dst instanceof ShortBuffer) {
            shift = 1;
        } else if (dst instanceof IntBuffer) {
            shift = 2;
        } else {
            throw new RuntimeException("unsupported Buffer subclass");
        }
        long pixelSize = (long) getByteCount();
        if ((((long) elements) << shift) >= pixelSize) {
            nativeCopyPixelsToBuffer(this.mNativePtr, dst);
            dst.position((int) (((long) dst.position()) + (pixelSize >> shift)));
            return;
        }
        throw new RuntimeException("Buffer not large enough for pixels");
    }

    public void copyPixelsFromBuffer(Buffer src) {
        int shift;
        checkRecycled("copyPixelsFromBuffer called on recycled bitmap");
        checkHardware("unable to copyPixelsFromBuffer, Config#HARDWARE bitmaps are immutable");
        int elements = src.remaining();
        if (src instanceof ByteBuffer) {
            shift = 0;
        } else if (src instanceof ShortBuffer) {
            shift = 1;
        } else if (src instanceof IntBuffer) {
            shift = 2;
        } else {
            throw new RuntimeException("unsupported Buffer subclass");
        }
        long bitmapBytes = (long) getByteCount();
        if ((((long) elements) << shift) >= bitmapBytes) {
            nativeCopyPixelsFromBuffer(this.mNativePtr, src);
            src.position((int) (((long) src.position()) + (bitmapBytes >> shift)));
            return;
        }
        throw new RuntimeException("Buffer not large enough for pixels");
    }

    private void noteHardwareBitmapSlowCall() {
        if (getConfig() == Config.HARDWARE) {
            StrictMode.noteSlowCall("Warning: attempt to read pixels from hardware bitmap, which is very slow operation");
        }
    }

    public Bitmap copy(Config config, boolean isMutable) {
        checkRecycled("Can't copy a recycled bitmap");
        if (config != Config.HARDWARE || !isMutable) {
            noteHardwareBitmapSlowCall();
            Bitmap b = nativeCopy(this.mNativePtr, config.nativeInt, isMutable);
            if (b != null) {
                b.setPremultiplied(this.mRequestPremultiplied);
                b.mDensity = this.mDensity;
            }
            return b;
        }
        throw new IllegalArgumentException("Hardware bitmaps are always immutable");
    }

    @UnsupportedAppUsage
    public Bitmap createAshmemBitmap() {
        checkRecycled("Can't copy a recycled bitmap");
        noteHardwareBitmapSlowCall();
        Bitmap b = nativeCopyAshmem(this.mNativePtr);
        if (b != null) {
            b.setPremultiplied(this.mRequestPremultiplied);
            b.mDensity = this.mDensity;
        }
        return b;
    }

    @UnsupportedAppUsage
    public Bitmap createAshmemBitmap(Config config) {
        checkRecycled("Can't copy a recycled bitmap");
        noteHardwareBitmapSlowCall();
        Bitmap b = nativeCopyAshmemConfig(this.mNativePtr, config.nativeInt);
        if (b != null) {
            b.setPremultiplied(this.mRequestPremultiplied);
            b.mDensity = this.mDensity;
        }
        return b;
    }

    public static Bitmap wrapHardwareBuffer(HardwareBuffer hardwareBuffer, ColorSpace colorSpace) {
        if ((hardwareBuffer.getUsage() & 256) != 0) {
            hardwareBuffer.getFormat();
            if (colorSpace == null) {
                colorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
            }
            return nativeWrapHardwareBufferBitmap(hardwareBuffer, colorSpace.getNativeInstance());
        }
        throw new IllegalArgumentException("usage flags must contain USAGE_GPU_SAMPLED_IMAGE.");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0011, code lost:
        if (r0 != null) goto L_0x0013;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0017, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0018, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001b, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0010, code lost:
        r2 = move-exception;
     */
    public static Bitmap wrapHardwareBuffer(GraphicBuffer graphicBuffer, ColorSpace colorSpace) {
        HardwareBuffer hb = HardwareBuffer.createFromGraphicBuffer(graphicBuffer);
        Bitmap wrapHardwareBuffer = wrapHardwareBuffer(hb, colorSpace);
        if (hb != null) {
            hb.close();
        }
        return wrapHardwareBuffer;
    }

    public static Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
        Matrix m = new Matrix();
        int width = src.getWidth();
        int height = src.getHeight();
        if (!(width == dstWidth && height == dstHeight)) {
            m.setScale(((float) dstWidth) / ((float) width), ((float) dstHeight) / ((float) height));
        }
        return createBitmap(src, 0, 0, width, height, m, filter);
    }

    public static Bitmap createBitmap(Bitmap src) {
        return createBitmap(src, 0, 0, src.getWidth(), src.getHeight());
    }

    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height) {
        return createBitmap(source, x, y, width, height, (Matrix) null, false);
    }

    /* JADX INFO: Multiple debug info for r7v9 'transformedConfig'  android.graphics.Bitmap$Config: [D('srcR' android.graphics.Rect), D('transformedConfig' android.graphics.Bitmap$Config)] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0153  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01a7  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0286  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0299  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x02a1 A[RETURN] */
    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height, Matrix m, boolean filter) {
        boolean callfromwechat;
        boolean enablemultithread;
        Bitmap source2;
        Config newConfig;
        Rect srcR;
        RectF deviceR;
        final Bitmap source3;
        Bitmap bitmap;
        ColorSpace cs;
        final Paint paint;
        Rect srcR2;
        RectF deviceR2;
        Rect srcR3;
        RectF deviceR3;
        Config transformedConfig;
        Config transformedConfig2;
        if (checkAppPackageName().startsWith(LMManager.MM_PACKAGENAME)) {
            callfromwechat = true;
        } else {
            callfromwechat = false;
        }
        if (!callfromwechat || (width <= 2160 && height <= 2160)) {
            enablemultithread = false;
        } else {
            enablemultithread = true;
        }
        checkXYSign(x, y);
        checkWidthHeight(width, height);
        if (x + width > source.getWidth()) {
            throw new IllegalArgumentException("x + width must be <= bitmap.width()");
        } else if (y + height > source.getHeight()) {
            throw new IllegalArgumentException("y + height must be <= bitmap.height()");
        } else if (source.isRecycled()) {
            throw new IllegalArgumentException("cannot use a recycled source in createBitmap");
        } else if (!source.isMutable() && x == 0 && y == 0 && width == source.getWidth() && height == source.getHeight() && (m == null || m.isIdentity())) {
            return source;
        } else {
            boolean isHardware = source.getConfig() == Config.HARDWARE;
            if (isHardware) {
                source.noteHardwareBitmapSlowCall();
                source2 = nativeCopyPreserveInternalConfig(source.mNativePtr);
            } else {
                source2 = source;
            }
            int neww = width;
            int newh = height;
            Rect srcR4 = new Rect(x, y, x + width, y + height);
            RectF dstR = new RectF(0.0f, 0.0f, (float) width, (float) height);
            RectF deviceR4 = new RectF();
            Config newConfig2 = Config.ARGB_8888;
            Config config = source2.getConfig();
            if (config != null) {
                int i = AnonymousClass5.$SwitchMap$android$graphics$Bitmap$Config[config.ordinal()];
                if (i == 1) {
                    newConfig = Config.RGB_565;
                } else if (i == 2) {
                    newConfig = Config.ALPHA_8;
                } else if (i != 3) {
                    newConfig = Config.ARGB_8888;
                } else {
                    newConfig = Config.RGBA_F16;
                }
            } else {
                newConfig = newConfig2;
            }
            ColorSpace cs2 = source2.getColorSpace();
            if (m == null) {
                deviceR2 = deviceR4;
                srcR2 = srcR4;
            } else if (m.isIdentity()) {
                deviceR2 = deviceR4;
                srcR2 = srcR4;
            } else {
                boolean transformed = !m.rectStaysRect();
                m.mapRect(deviceR4, dstR);
                neww = Math.round(deviceR4.width());
                newh = Math.round(deviceR4.height());
                if (transformed) {
                    deviceR3 = deviceR4;
                    srcR3 = srcR4;
                    transformedConfig2 = newConfig;
                    if (!(transformedConfig2 == Config.ARGB_8888 || transformedConfig2 == Config.RGBA_F16)) {
                        transformedConfig = Config.ARGB_8888;
                        if (cs2 == null) {
                            cs2 = ColorSpace.get(ColorSpace.Named.SRGB);
                        }
                        bitmap = createBitmap((DisplayMetrics) null, neww, newh, transformedConfig, !transformed || source2.hasAlpha(), cs2);
                        Paint paint2 = new Paint();
                        paint2.setFilterBitmap(filter);
                        if (transformed) {
                            paint2.setAntiAlias(true);
                        }
                        paint = paint2;
                        deviceR = deviceR3;
                        srcR = srcR3;
                        cs = cs2;
                        source3 = source2;
                        bitmap.mDensity = source3.mDensity;
                        bitmap.setHasAlpha(source3.hasAlpha());
                        bitmap.setPremultiplied(source3.mRequestPremultiplied);
                        final Canvas canvas = new Canvas(bitmap);
                        canvas.translate(-deviceR.left, -deviceR.top);
                        canvas.concat(m);
                        if (enablemultithread) {
                            Rect srcR1 = new Rect(x, y, (width / 2) + x, y + (height / 2));
                            RectF dstR1 = new RectF(0.0f, 0.0f, (float) (width / 2), (float) (height / 2));
                            final Rect srcR22 = new Rect((width / 2) + x, y, x + width, y + (height / 2));
                            final RectF dstR2 = new RectF((float) (width / 2), 0.0f, (float) width, (float) (height / 2));
                            final Rect srcR32 = new Rect(x, (height / 2) + y, (width / 2) + x, y + height);
                            final RectF dstR3 = new RectF(0.0f, (float) (height / 2), (float) (width / 2), (float) height);
                            final Rect srcR42 = new Rect((width / 2) + x, (height / 2) + y, x + width, y + height);
                            final RectF dstR4 = new RectF((float) (width / 2), (float) (height / 2), (float) width, (float) height);
                            Thread drawthread1 = new Thread(new Runnable() {
                                /* class android.graphics.Bitmap.AnonymousClass1 */

                                public void run() {
                                    Trace.traceBegin(32, "drawbitmap 2");
                                    Canvas.this.drawBitmap(source3, srcR22, dstR2, paint);
                                    Trace.traceEnd(32);
                                }
                            });
                            Thread drawthread2 = new Thread(new Runnable() {
                                /* class android.graphics.Bitmap.AnonymousClass2 */

                                public void run() {
                                    Trace.traceBegin(32, "drawbitmap 3");
                                    Canvas.this.drawBitmap(source3, srcR32, dstR3, paint);
                                    Trace.traceEnd(32);
                                }
                            });
                            Thread drawthread3 = new Thread(new Runnable() {
                                /* class android.graphics.Bitmap.AnonymousClass3 */

                                public void run() {
                                    Trace.traceBegin(32, "drawbitmap 4");
                                    Canvas.this.drawBitmap(source3, srcR42, dstR4, paint);
                                    Trace.traceEnd(32);
                                }
                            });
                            drawthread1.start();
                            drawthread2.start();
                            drawthread3.start();
                            Trace.traceBegin(32, "drawbitmap 1");
                            canvas.drawBitmap(source3, srcR1, dstR1, paint);
                            Trace.traceEnd(32);
                            try {
                                drawthread1.join();
                                drawthread2.join();
                                drawthread3.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            canvas.drawBitmap(source3, srcR, dstR, paint);
                        }
                        canvas.setBitmap(null);
                        if (isHardware) {
                            return bitmap.copy(Config.HARDWARE, false);
                        }
                        return bitmap;
                    }
                } else {
                    deviceR3 = deviceR4;
                    srcR3 = srcR4;
                    transformedConfig2 = newConfig;
                }
                transformedConfig = transformedConfig2;
                bitmap = createBitmap((DisplayMetrics) null, neww, newh, transformedConfig, !transformed || source2.hasAlpha(), cs2);
                Paint paint22 = new Paint();
                paint22.setFilterBitmap(filter);
                if (transformed) {
                }
                paint = paint22;
                deviceR = deviceR3;
                srcR = srcR3;
                cs = cs2;
                source3 = source2;
                bitmap.mDensity = source3.mDensity;
                bitmap.setHasAlpha(source3.hasAlpha());
                bitmap.setPremultiplied(source3.mRequestPremultiplied);
                final Canvas canvas2 = new Canvas(bitmap);
                canvas2.translate(-deviceR.left, -deviceR.top);
                canvas2.concat(m);
                if (enablemultithread) {
                }
                canvas2.setBitmap(null);
                if (isHardware) {
                }
            }
            deviceR = deviceR2;
            srcR = srcR2;
            source3 = source2;
            bitmap = createBitmap((DisplayMetrics) null, neww, newh, newConfig, source2.hasAlpha(), cs2);
            paint = null;
            cs = cs2;
            bitmap.mDensity = source3.mDensity;
            bitmap.setHasAlpha(source3.hasAlpha());
            bitmap.setPremultiplied(source3.mRequestPremultiplied);
            final Canvas canvas22 = new Canvas(bitmap);
            canvas22.translate(-deviceR.left, -deviceR.top);
            canvas22.concat(m);
            if (enablemultithread) {
            }
            canvas22.setBitmap(null);
            if (isHardware) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: android.graphics.Bitmap$5  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$android$graphics$Bitmap$Config = new int[Config.values().length];

        static {
            try {
                $SwitchMap$android$graphics$Bitmap$Config[Config.RGB_565.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$graphics$Bitmap$Config[Config.ALPHA_8.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$graphics$Bitmap$Config[Config.RGBA_F16.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$graphics$Bitmap$Config[Config.ARGB_4444.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$graphics$Bitmap$Config[Config.ARGB_8888.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public static Bitmap createBitmap(int width, int height, Config config) {
        return createBitmap(width, height, config, true);
    }

    public static Bitmap createBitmap(DisplayMetrics display, int width, int height, Config config) {
        return createBitmap(display, width, height, config, true);
    }

    public static Bitmap createBitmap(int width, int height, Config config, boolean hasAlpha) {
        return createBitmap((DisplayMetrics) null, width, height, config, hasAlpha);
    }

    public static Bitmap createBitmap(int width, int height, Config config, boolean hasAlpha, ColorSpace colorSpace) {
        return createBitmap((DisplayMetrics) null, width, height, config, hasAlpha, colorSpace);
    }

    public static Bitmap createBitmap(DisplayMetrics display, int width, int height, Config config, boolean hasAlpha) {
        return createBitmap(display, width, height, config, hasAlpha, ColorSpace.get(ColorSpace.Named.SRGB));
    }

    public static Bitmap createBitmap(DisplayMetrics display, int width, int height, Config config, boolean hasAlpha, ColorSpace colorSpace) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        } else if (config == Config.HARDWARE) {
            throw new IllegalArgumentException("can't create mutable bitmap with Config.HARDWARE");
        } else if (colorSpace != null || config == Config.ALPHA_8) {
            Bitmap bm = nativeCreate(null, 0, width, width, height, config.nativeInt, true, colorSpace == null ? 0 : colorSpace.getNativeInstance());
            if (display != null) {
                bm.mDensity = display.densityDpi;
            }
            bm.setHasAlpha(hasAlpha);
            if ((config == Config.ARGB_8888 || config == Config.RGBA_F16) && !hasAlpha) {
                nativeErase(bm.mNativePtr, -16777216);
            }
            return bm;
        } else {
            throw new IllegalArgumentException("can't create bitmap without a color space");
        }
    }

    public static Bitmap createBitmap(int[] colors, int offset, int stride, int width, int height, Config config) {
        return createBitmap((DisplayMetrics) null, colors, offset, stride, width, height, config);
    }

    public static Bitmap createBitmap(DisplayMetrics display, int[] colors, int offset, int stride, int width, int height, Config config) {
        checkWidthHeight(width, height);
        if (Math.abs(stride) >= width) {
            int lastScanline = offset + ((height - 1) * stride);
            int length = colors.length;
            if (offset < 0 || offset + width > length || lastScanline < 0 || lastScanline + width > length) {
                throw new ArrayIndexOutOfBoundsException();
            } else if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("width and height must be > 0");
            } else {
                Bitmap bm = nativeCreate(colors, offset, stride, width, height, config.nativeInt, false, ColorSpace.get(ColorSpace.Named.SRGB).getNativeInstance());
                if (display != null) {
                    bm.mDensity = display.densityDpi;
                }
                return bm;
            }
        } else {
            throw new IllegalArgumentException("abs(stride) must be >= width");
        }
    }

    public static Bitmap createBitmap(int[] colors, int width, int height, Config config) {
        return createBitmap((DisplayMetrics) null, colors, 0, width, width, height, config);
    }

    public static Bitmap createBitmap(DisplayMetrics display, int[] colors, int width, int height, Config config) {
        return createBitmap(display, colors, 0, width, width, height, config);
    }

    public static Bitmap createBitmap(Picture source) {
        return createBitmap(source, source.getWidth(), source.getHeight(), Config.HARDWARE);
    }

    public static Bitmap createBitmap(Picture source, int width, int height, Config config) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width & height must be > 0");
        } else if (config != null) {
            source.endRecording();
            if (source.requiresHardwareAcceleration() && config != Config.HARDWARE) {
                StrictMode.noteSlowCall("GPU readback");
            }
            if (config == Config.HARDWARE || source.requiresHardwareAcceleration()) {
                RenderNode node = RenderNode.create("BitmapTemporary", null);
                node.setLeftTopRightBottom(0, 0, width, height);
                node.setClipToBounds(false);
                node.setForceDarkAllowed(false);
                RecordingCanvas canvas = node.beginRecording(width, height);
                if (!(source.getWidth() == width && source.getHeight() == height)) {
                    canvas.scale(((float) width) / ((float) source.getWidth()), ((float) height) / ((float) source.getHeight()));
                }
                canvas.drawPicture(source);
                node.endRecording();
                Bitmap bitmap = ThreadedRenderer.createHardwareBitmap(node, width, height);
                if (config != Config.HARDWARE) {
                    return bitmap.copy(config, false);
                }
                return bitmap;
            }
            Bitmap bitmap2 = createBitmap(width, height, config);
            Canvas canvas2 = new Canvas(bitmap2);
            if (!(source.getWidth() == width && source.getHeight() == height)) {
                canvas2.scale(((float) width) / ((float) source.getWidth()), ((float) height) / ((float) source.getHeight()));
            }
            canvas2.drawPicture(source);
            canvas2.setBitmap(null);
            bitmap2.setImmutable();
            return bitmap2;
        } else {
            throw new IllegalArgumentException("Config must not be null");
        }
    }

    public byte[] getNinePatchChunk() {
        return this.mNinePatchChunk;
    }

    public void getOpticalInsets(Rect outInsets) {
        NinePatch.InsetStruct insetStruct = this.mNinePatchInsets;
        if (insetStruct == null) {
            outInsets.setEmpty();
        } else {
            outInsets.set(insetStruct.opticalRect);
        }
    }

    public NinePatch.InsetStruct getNinePatchInsets() {
        return this.mNinePatchInsets;
    }

    public enum CompressFormat {
        JPEG(0),
        PNG(1),
        WEBP(2);
        
        final int nativeInt;

        private CompressFormat(int nativeInt2) {
            this.nativeInt = nativeInt2;
        }
    }

    public boolean compress(CompressFormat format, int quality, OutputStream stream) {
        checkRecycled("Can't compress a recycled bitmap");
        if (stream == null) {
            throw new NullPointerException();
        } else if (quality < 0 || quality > 100) {
            throw new IllegalArgumentException("quality must be 0..100");
        } else {
            StrictMode.noteSlowCall("Compression of a bitmap is slow");
            if (mHM == null && SystemClock.uptimeMillis() > 100000) {
                mHM = HypnusManager.getHypnusManager();
            }
            if (mHM != null && (this.mWidth > 1000 || this.mHeight > 1000)) {
                mHM.hypnusSetSignatureAction(15, 20000, Hypnus.getLocalSignature());
            }
            Trace.traceBegin(8192, "Bitmap.compress");
            boolean result = nativeCompress(this.mNativePtr, format.nativeInt, quality, stream, new byte[4096]);
            Trace.traceEnd(8192);
            if (mHM != null && (this.mWidth > 1000 || this.mHeight > 1000)) {
                mHM.hypnusSetSignatureAction(15, 0, Hypnus.getLocalSignature());
            }
            return result;
        }
    }

    public final boolean isMutable() {
        return !nativeIsImmutable(this.mNativePtr);
    }

    public void setImmutable() {
        if (isMutable()) {
            nativeSetImmutable(this.mNativePtr);
        }
    }

    public final boolean isPremultiplied() {
        if (this.mRecycled) {
            Log.w(TAG, "Called isPremultiplied() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return nativeIsPremultiplied(this.mNativePtr);
    }

    public final void setPremultiplied(boolean premultiplied) {
        checkRecycled("setPremultiplied called on a recycled bitmap");
        this.mRequestPremultiplied = premultiplied;
        nativeSetPremultiplied(this.mNativePtr, premultiplied);
    }

    public final int getWidth() {
        if (this.mRecycled) {
            Log.w(TAG, "Called getWidth() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return this.mWidth;
    }

    public final int getHeight() {
        if (this.mRecycled) {
            Log.w(TAG, "Called getHeight() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return this.mHeight;
    }

    public int getScaledWidth(Canvas canvas) {
        return scaleFromDensity(getWidth(), this.mDensity, canvas.mDensity);
    }

    public int getScaledHeight(Canvas canvas) {
        return scaleFromDensity(getHeight(), this.mDensity, canvas.mDensity);
    }

    public int getScaledWidth(DisplayMetrics metrics) {
        return scaleFromDensity(getWidth(), this.mDensity, metrics.densityDpi);
    }

    public int getScaledHeight(DisplayMetrics metrics) {
        return scaleFromDensity(getHeight(), this.mDensity, metrics.densityDpi);
    }

    public int getScaledWidth(int targetDensity) {
        return scaleFromDensity(getWidth(), this.mDensity, targetDensity);
    }

    public int getScaledHeight(int targetDensity) {
        return scaleFromDensity(getHeight(), this.mDensity, targetDensity);
    }

    @UnsupportedAppUsage
    public static int scaleFromDensity(int size, int sdensity, int tdensity) {
        if (sdensity == 0 || tdensity == 0 || sdensity == tdensity) {
            return size;
        }
        return ((size * tdensity) + (sdensity >> 1)) / sdensity;
    }

    public final int getRowBytes() {
        if (this.mRecycled) {
            Log.w(TAG, "Called getRowBytes() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return nativeRowBytes(this.mNativePtr);
    }

    public final int getByteCount() {
        if (!this.mRecycled) {
            return getRowBytes() * getHeight();
        }
        Log.w(TAG, "Called getByteCount() on a recycle()'d bitmap! This is undefined behavior!");
        return 0;
    }

    public final int getAllocationByteCount() {
        if (!this.mRecycled) {
            return nativeGetAllocationByteCount(this.mNativePtr);
        }
        Log.w(TAG, "Called getAllocationByteCount() on a recycle()'d bitmap! This is undefined behavior!");
        return 0;
    }

    public final Config getConfig() {
        if (this.mRecycled) {
            Log.w(TAG, "Called getConfig() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return Config.nativeToConfig(nativeConfig(this.mNativePtr));
    }

    public final boolean hasAlpha() {
        if (this.mRecycled) {
            Log.w(TAG, "Called hasAlpha() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return nativeHasAlpha(this.mNativePtr);
    }

    public void setHasAlpha(boolean hasAlpha) {
        checkRecycled("setHasAlpha called on a recycled bitmap");
        nativeSetHasAlpha(this.mNativePtr, hasAlpha, this.mRequestPremultiplied);
    }

    public final boolean hasMipMap() {
        if (this.mRecycled) {
            Log.w(TAG, "Called hasMipMap() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return nativeHasMipMap(this.mNativePtr);
    }

    public final void setHasMipMap(boolean hasMipMap) {
        checkRecycled("setHasMipMap called on a recycled bitmap");
        nativeSetHasMipMap(this.mNativePtr, hasMipMap);
    }

    public final ColorSpace getColorSpace() {
        checkRecycled("getColorSpace called on a recycled bitmap");
        if (this.mColorSpace == null) {
            this.mColorSpace = nativeComputeColorSpace(this.mNativePtr);
        }
        return this.mColorSpace;
    }

    public void setColorSpace(ColorSpace colorSpace) {
        checkRecycled("setColorSpace called on a recycled bitmap");
        if (colorSpace == null) {
            throw new IllegalArgumentException("The colorSpace cannot be set to null");
        } else if (getConfig() != Config.ALPHA_8) {
            ColorSpace oldColorSpace = getColorSpace();
            nativeSetColorSpace(this.mNativePtr, colorSpace.getNativeInstance());
            this.mColorSpace = null;
            ColorSpace newColorSpace = getColorSpace();
            try {
                if (oldColorSpace.getComponentCount() == newColorSpace.getComponentCount()) {
                    for (int i = 0; i < oldColorSpace.getComponentCount(); i++) {
                        if (oldColorSpace.getMinValue(i) < newColorSpace.getMinValue(i)) {
                            throw new IllegalArgumentException("The new ColorSpace cannot increase the minimum value for any of the components compared to the current ColorSpace. To perform this type of conversion create a new Bitmap in the desired ColorSpace and draw this Bitmap into it.");
                        } else if (oldColorSpace.getMaxValue(i) > newColorSpace.getMaxValue(i)) {
                            throw new IllegalArgumentException("The new ColorSpace cannot decrease the maximum value for any of the components compared to the current ColorSpace/ To perform this type of conversion create a new Bitmap in the desired ColorSpace and draw this Bitmap into it.");
                        }
                    }
                    return;
                }
                throw new IllegalArgumentException("The new ColorSpace must have the same component count as the current ColorSpace");
            } catch (IllegalArgumentException e) {
                this.mColorSpace = oldColorSpace;
                nativeSetColorSpace(this.mNativePtr, this.mColorSpace.getNativeInstance());
                throw e;
            }
        } else {
            throw new IllegalArgumentException("Cannot set a ColorSpace on ALPHA_8");
        }
    }

    public void eraseColor(int c) {
        checkRecycled("Can't erase a recycled bitmap");
        if (isMutable()) {
            nativeErase(this.mNativePtr, ((IColorDarkModeManager) OppoFeatureCache.getOrCreate(IColorDarkModeManager.DEFAULT, new Object[0])).handleEraseColor(c));
            return;
        }
        throw new IllegalStateException("cannot erase immutable bitmaps");
    }

    public void eraseColor(long color) {
        checkRecycled("Can't erase a recycled bitmap");
        if (isMutable()) {
            nativeErase(this.mNativePtr, Color.colorSpace(color).getNativeInstance(), color);
            return;
        }
        throw new IllegalStateException("cannot erase immutable bitmaps");
    }

    public int getPixel(int x, int y) {
        checkRecycled("Can't call getPixel() on a recycled bitmap");
        checkHardware("unable to getPixel(), pixel access is not supported on Config#HARDWARE bitmaps");
        checkPixelAccess(x, y);
        return nativeGetPixel(this.mNativePtr, x, y);
    }

    private static float clamp(float value, ColorSpace cs, int index) {
        return Math.max(Math.min(value, cs.getMaxValue(index)), cs.getMinValue(index));
    }

    public Color getColor(int x, int y) {
        checkRecycled("Can't call getColor() on a recycled bitmap");
        checkHardware("unable to getColor(), pixel access is not supported on Config#HARDWARE bitmaps");
        checkPixelAccess(x, y);
        ColorSpace cs = getColorSpace();
        if (cs.equals(ColorSpace.get(ColorSpace.Named.SRGB))) {
            return Color.valueOf(nativeGetPixel(this.mNativePtr, x, y));
        }
        long rgba = nativeGetColor(this.mNativePtr, x, y);
        float r = Half.toFloat((short) ((int) ((rgba >> 0) & 65535)));
        float g = Half.toFloat((short) ((int) ((rgba >> 16) & 65535)));
        float b = Half.toFloat((short) ((int) ((rgba >> 32) & 65535)));
        return Color.valueOf(clamp(r, cs, 0), clamp(g, cs, 1), clamp(b, cs, 2), Half.toFloat((short) ((int) (65535 & (rgba >> 48)))), cs);
    }

    public void getPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height) {
        checkRecycled("Can't call getPixels() on a recycled bitmap");
        checkHardware("unable to getPixels(), pixel access is not supported on Config#HARDWARE bitmaps");
        if (width != 0 && height != 0) {
            checkPixelsAccess(x, y, width, height, offset, stride, pixels);
            nativeGetPixels(this.mNativePtr, pixels, offset, stride, x, y, width, height);
        }
    }

    private void checkPixelAccess(int x, int y) {
        checkXYSign(x, y);
        if (x >= getWidth()) {
            throw new IllegalArgumentException("x must be < bitmap.width()");
        } else if (y >= getHeight()) {
            throw new IllegalArgumentException("y must be < bitmap.height()");
        }
    }

    private void checkPixelsAccess(int x, int y, int width, int height, int offset, int stride, int[] pixels) {
        checkXYSign(x, y);
        if (width < 0) {
            throw new IllegalArgumentException("width must be >= 0");
        } else if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0");
        } else if (x + width > getWidth()) {
            throw new IllegalArgumentException("x + width must be <= bitmap.width()");
        } else if (y + height > getHeight()) {
            throw new IllegalArgumentException("y + height must be <= bitmap.height()");
        } else if (Math.abs(stride) >= width) {
            int lastScanline = ((height - 1) * stride) + offset;
            int length = pixels.length;
            if (offset < 0 || offset + width > length || lastScanline < 0 || lastScanline + width > length) {
                throw new ArrayIndexOutOfBoundsException();
            }
        } else {
            throw new IllegalArgumentException("abs(stride) must be >= width");
        }
    }

    public void setPixel(int x, int y, int color) {
        checkRecycled("Can't call setPixel() on a recycled bitmap");
        if (isMutable()) {
            checkPixelAccess(x, y);
            nativeSetPixel(this.mNativePtr, x, y, color);
            return;
        }
        throw new IllegalStateException();
    }

    public void setPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height) {
        checkRecycled("Can't call setPixels() on a recycled bitmap");
        if (!isMutable()) {
            throw new IllegalStateException();
        } else if (width != 0 && height != 0) {
            checkPixelsAccess(x, y, width, height, offset, stride, pixels);
            nativeSetPixels(this.mNativePtr, pixels, offset, stride, x, y, width, height);
        }
    }

    public boolean checkLM(byte[] hash, boolean getHash, int width, int height) {
        synchronized (Bitmap.class) {
            if (getHash) {
                File f = new File(LM_HASH_DIR);
                if (!f.exists()) {
                    f.mkdirs();
                } else if (!f.isDirectory()) {
                    f.delete();
                    f.mkdirs();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(LM_HASH_DIR);
                int i = sGetHashCount;
                sGetHashCount = i + 1;
                sb.append(i);
                sb.append(Session.SESSION_SEPARATION_CHAR_CHILD);
                sb.append(this.mWidth);
                sb.append("x");
                sb.append(this.mHeight);
                sb.append(".raw");
                nativeDumpRawdata(this.mNativePtr, sb.toString());
            }
        }
        if (width == this.mWidth && height == this.mHeight && hash != null) {
            return nativeCheckLM(this.mNativePtr, hash);
        }
        return false;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel p, int flags) {
        checkRecycled("Can't parcel a recycled bitmap");
        noteHardwareBitmapSlowCall();
        if (!nativeWriteToParcel(this.mNativePtr, isMutable(), this.mDensity, p)) {
            throw new RuntimeException("native writeToParcel failed");
        }
    }

    public Bitmap extractAlpha() {
        return extractAlpha(null, null);
    }

    public Bitmap extractAlpha(Paint paint, int[] offsetXY) {
        checkRecycled("Can't extractAlpha on a recycled bitmap");
        long nativePaint = paint != null ? paint.getNativeInstance() : 0;
        noteHardwareBitmapSlowCall();
        Bitmap bm = nativeExtractAlpha(this.mNativePtr, nativePaint, offsetXY);
        if (bm != null) {
            bm.mDensity = this.mDensity;
            return bm;
        }
        throw new RuntimeException("Failed to extractAlpha on Bitmap");
    }

    public boolean sameAs(Bitmap other) {
        checkRecycled("Can't call sameAs on a recycled bitmap!");
        noteHardwareBitmapSlowCall();
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        other.noteHardwareBitmapSlowCall();
        if (!other.isRecycled()) {
            return nativeSameAs(this.mNativePtr, other.mNativePtr);
        }
        throw new IllegalArgumentException("Can't compare to a recycled bitmap!");
    }

    public void prepareToDraw() {
        checkRecycled("Can't prepareToDraw on a recycled bitmap!");
        nativePrepareToDraw(this.mNativePtr);
    }

    @UnsupportedAppUsage
    public GraphicBuffer createGraphicBufferHandle() {
        return nativeCreateGraphicBufferHandle(this.mNativePtr);
    }

    private static String checkAppPackageName() {
        String callingApp = "";
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream("/proc/" + Process.myPid() + "/cmdline");
            byte[] buffer = new byte[50];
            int count = fis2.read(buffer);
            if (count > 0) {
                callingApp = new String(buffer, 0, count);
            }
            try {
                fis2.close();
            } catch (Exception e) {
            }
        } catch (Exception e2) {
            if (0 != 0) {
                fis.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fis.close();
                } catch (Exception e3) {
                }
            }
            throw th;
        }
        return callingApp;
    }
}
