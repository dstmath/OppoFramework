package com.oppo.launcher.graphic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GaussianBlur {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final float DEFAULT_BRIGHTNESS = 0.8f;
    private static final int DEFAULT_RADIUS = 25;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS);
    private static final int EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors();
    public static final float MAX_BRIGHTNESS = 1.0f;
    public static final float MINIM_BRIGHTNESS = 0.0f;
    private static final String TAG = "GaussianBlur";
    private static GaussianBlur sGaussianBlur = null;
    public static int sScreenHeight = -1;
    public static int sScreenWidth = -1;
    private Bitmap mBitmap;
    private SparseArray<Bitmap> mBitmapMapMemoryCache = new SparseArray<>();
    private SparseArray<int[]> mInoutPixelMapMemoryCache = new SparseArray<>();
    private int[] mInoutPixels;

    public native void blurBrightness_native(int[] iArr, int i, int i2, float f);

    public native void blurIterationAlpha_native(int[] iArr, int i, int i2, int i3, int i4, int i5, int i6);

    public native void blurIteration_native(int[] iArr, int i, int i2, int i3, int i4, int i5, int i6);

    static {
        System.loadLibrary("gaussgraphic");
    }

    private GaussianBlur() {
    }

    public static GaussianBlur getInstance() {
        if (sGaussianBlur == null) {
            sGaussianBlur = new GaussianBlur();
        }
        return sGaussianBlur;
    }

    public static void setScreenWidth(Context context) {
        if (sScreenWidth == -1 || sScreenHeight == -1) {
            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            DisplayMetrics mDisplayMetrics = new DisplayMetrics();
            display.getRealMetrics(mDisplayMetrics);
            int realWidth = mDisplayMetrics.widthPixels;
            int realHeight = mDisplayMetrics.heightPixels;
            sScreenWidth = realWidth < realHeight ? realWidth : realHeight;
            sScreenHeight = realWidth < realHeight ? realHeight : realWidth;
            if (DEBUG) {
                Log.d(TAG, "GaussianBlur:setScreenWidth   getRotation() " + display.getRotation() + ", sScreenWidth = " + sScreenWidth);
            }
        }
    }

    public static Bitmap scaleBitmap(Bitmap bm) {
        return scaleBitmap(bm, 0.25f);
    }

    public static Bitmap scaleBitmap(Bitmap bm, float scale) {
        int width;
        int beginX;
        Bitmap bmp = null;
        if (bm != null) {
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            int beginX2 = bm.getWidth() - sScreenWidth;
            int width2 = sScreenWidth;
            if (beginX2 < 0) {
                beginX = 0;
                width = bm.getWidth();
            } else {
                beginX = beginX2;
                width = width2;
            }
            bmp = Bitmap.createBitmap(bm, beginX, 0, width, bm.getHeight(), matrix, true);
        }
        if (bmp != null && DEBUG) {
            Log.d(TAG, "Gaussian:captureWallpaper bm.getWidth = " + bm.getWidth() + ", bmp.getWidth = " + bmp.getWidth());
        }
        return bmp;
    }

    public void setParameter(int h, int v, int i) {
    }

    public Bitmap captureScreen(Context context, int width, int height, float scale, int minLayer, int maxLayer) {
        if (width == -1 || height == -1) {
            setScreenWidth(context);
            int w = sScreenWidth;
            int h = sScreenHeight;
        }
        return null;
    }

    public Bitmap generateGaussianWallpaper(Context context, float scale, int radius, float brightness) {
        int wallpaperLayer = Settings.System.getInt(context.getContentResolver(), "LAYER_WALLPAPER", -1);
        return generateGaussianBitmap(captureScreen(context, -1, -1, scale, wallpaperLayer, wallpaperLayer), radius, brightness, true);
    }

    public Bitmap generateGaussianScreenshot(Context context, float scale, int radius, float brightness) {
        return generateGaussianBitmap(captureScreen(context, -1, -1, scale, -1, -1), radius, brightness, true);
    }

    public Bitmap generateGaussianBitmap(Bitmap bmp, boolean recycle) {
        return generateGaussianBitmap(bmp, 25, DEFAULT_BRIGHTNESS, recycle);
    }

    public Bitmap generateGaussianBitmap(Bitmap bmp, float brightness, boolean recycle) {
        return generateGaussianBitmap(bmp, 25, brightness, recycle);
    }

    public Bitmap generateGaussianBitmap(Bitmap bmp, int radius, float brightness, boolean recycle) {
        return generateGaussianBitmap(bmp, radius, brightness, false, recycle);
    }

    public Bitmap generateGaussianBitmapResuse(Bitmap bmp, int radius, float brightness, boolean recycle) {
        if (bmp == null || bmp.isRecycled()) {
            Log.w(TAG, "GaussianBlur:generateGaussianBitmapResuse  bmp is null or isRecycled!");
            return null;
        }
        if (DEBUG) {
            Log.v(TAG, "GaussianBlur:generateGaussianBitmapResuse  Enter");
        }
        boolean isAlpha = bmp.hasAlpha();
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        if (this.mInoutPixelMapMemoryCache == null) {
            this.mInoutPixelMapMemoryCache = new SparseArray<>();
        }
        int[] pixelCache = this.mInoutPixelMapMemoryCache.get(height * width);
        if (pixelCache != null) {
            this.mInoutPixels = pixelCache;
        } else {
            this.mInoutPixels = new int[(width * height)];
            this.mInoutPixelMapMemoryCache.put(height * width, this.mInoutPixels);
        }
        if (this.mBitmapMapMemoryCache == null) {
            this.mBitmapMapMemoryCache = new SparseArray<>();
        }
        Bitmap bitmapCache = this.mBitmapMapMemoryCache.get(height);
        if (bitmapCache != null && bitmapCache.getWidth() == width && bitmapCache.getHeight() == height) {
            this.mBitmap = bitmapCache;
        } else {
            this.mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            this.mBitmapMapMemoryCache.put(height, this.mBitmap);
        }
        bmp.getPixels(this.mInoutPixels, 0, width, 0, 0, width, height);
        blurIteration_native(this.mInoutPixels, width, height, radius, isAlpha);
        boolean z = DEBUG;
        if (brightness > 0.0f && brightness < 1.0f) {
            blurBrightness_native(this.mInoutPixels, height, width, brightness);
        }
        this.mBitmap.setPixels(this.mInoutPixels, 0, width, 0, 0, width, height);
        if (recycle) {
            bmp.recycle();
        }
        if (DEBUG) {
            Log.d(TAG, "GaussianBlur:generateGaussianBitmap  generate Complete");
        }
        return this.mBitmap;
    }

    public Bitmap generateGaussianBitmap(Bitmap bmp, int radius, float brightness, boolean hasAlpha, boolean recycle) {
        Bitmap bmp2 = bmp;
        if (bmp2 != null) {
            if (!bmp.isRecycled()) {
                if (DEBUG) {
                    Log.v(TAG, "GaussianBlur:generateGaussianBitmap  Enter");
                }
                boolean isAlpha = bmp.hasAlpha() ? hasAlpha : false;
                int width = bmp.getWidth();
                int height = bmp.getHeight();
                int[] inoutPixels = new int[(width * height)];
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                if (bmp.getConfig() == Bitmap.Config.HARDWARE) {
                    bmp2 = bmp2.copy(Bitmap.Config.ARGB_8888, true);
                }
                bmp2.getPixels(inoutPixels, 0, width, 0, 0, width, height);
                blurIteration_native(inoutPixels, width, height, radius, isAlpha);
                if (DEBUG) {
                    Log.d(TAG, "GaussianBlur:generateGaussianBitmap  generate brightness");
                }
                if (brightness > 0.0f && brightness < 1.0f) {
                    blurBrightness_native(inoutPixels, height, width, brightness);
                }
                bitmap.setPixels(inoutPixels, 0, width, 0, 0, width, height);
                if (recycle) {
                    bmp2.recycle();
                }
                if (DEBUG) {
                    Log.d(TAG, "GaussianBlur:generateGaussianBitmap  generate Complete");
                }
                return bitmap;
            }
        }
        Log.w(TAG, "GaussianBlur:generateGaussianBitmap  bmp is null or isRecycled!");
        return null;
    }

    public Bitmap generateGaussianBitmapTask(Bitmap bmp, int radius, float brightness, boolean hasAlpha, boolean recycle) {
        if (bmp != null) {
            if (!bmp.isRecycled()) {
                if (DEBUG) {
                    Log.v(TAG, "GaussianBlur:generateGaussianBitmapTask  Enter");
                }
                boolean isAlpha = bmp.hasAlpha() ? hasAlpha : false;
                int width = bmp.getWidth();
                int height = bmp.getHeight();
                int[] inoutPixels = new int[(width * height)];
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bmp.getPixels(inoutPixels, 0, width, 0, 0, width, height);
                blurIteration_nativeTask(inoutPixels, width, height, radius, isAlpha);
                if (DEBUG) {
                    Log.d(TAG, "GaussianBlur:generateGaussianBitmap  generate brightness");
                }
                if (brightness > 0.0f && brightness < 1.0f) {
                    blurBrightness_native(inoutPixels, height, width, brightness);
                }
                bitmap.setPixels(inoutPixels, 0, width, 0, 0, width, height);
                if (recycle) {
                    bmp.recycle();
                }
                if (DEBUG) {
                    Log.d(TAG, "GaussianBlur:generateGaussianBitmapTask  generate Complete");
                }
                return bitmap;
            }
        }
        Log.w(TAG, "GaussianBlur:generateGaussianBitmapTask  bmp is null or isRecycled!");
        return null;
    }

    public void releaseResource() {
        Bitmap bitmap = this.mBitmap;
        if (bitmap != null && bitmap.isRecycled()) {
            this.mBitmap.recycle();
        }
        if (this.mInoutPixels != null) {
            this.mInoutPixels = null;
        }
        SparseArray<Bitmap> sparseArray = this.mBitmapMapMemoryCache;
        if (sparseArray != null) {
            int size = sparseArray.size();
            for (int i = 0; i < size; i++) {
                Bitmap cacheBitmap = this.mBitmapMapMemoryCache.valueAt(i);
                if (cacheBitmap != null && !cacheBitmap.isRecycled()) {
                    cacheBitmap.recycle();
                }
            }
            this.mBitmapMapMemoryCache.clear();
            this.mBitmapMapMemoryCache = null;
        }
        SparseArray<int[]> sparseArray2 = this.mInoutPixelMapMemoryCache;
        if (sparseArray2 != null) {
            sparseArray2.clear();
            this.mInoutPixelMapMemoryCache = null;
        }
    }

    /* access modifiers changed from: private */
    public class BlurTask implements Callable<Void> {
        private final int mCore;
        private final int mCores;
        private final boolean mHasAlpha;
        private final int mHeight;
        private final int[] mInout;
        private final int mRadius;
        private final int mStep;
        private final int mWidth;

        public BlurTask(int[] src, int w, int h, int r, int totalCores, int coreIndex, int round, boolean alpha) {
            this.mInout = src;
            this.mWidth = w;
            this.mHeight = h;
            this.mRadius = r;
            this.mCores = totalCores;
            this.mCore = coreIndex;
            this.mStep = round;
            this.mHasAlpha = alpha;
        }

        @Override // java.util.concurrent.Callable
        public Void call() throws Exception {
            if (this.mHasAlpha) {
                GaussianBlur.this.blurIterationAlpha_native(this.mInout, this.mWidth, this.mHeight, this.mRadius, this.mCores, this.mCore, this.mStep);
                return null;
            }
            GaussianBlur.this.blurIteration_native(this.mInout, this.mWidth, this.mHeight, this.mRadius, this.mCores, this.mCore, this.mStep);
            return null;
        }
    }

    public void blurIteration_native(int[] inout, int width, int height, int radius, boolean hasAlpha) {
        if (hasAlpha) {
            blurIterationAlpha_native(inout, width, height, radius, 1, 0, 1);
            blurIterationAlpha_native(inout, width, height, radius, 1, 0, 2);
            return;
        }
        blurIteration_native(inout, width, height, radius, 1, 0, 1);
        blurIteration_native(inout, width, height, radius, 1, 0, 2);
    }

    public void blurIteration_nativeTask(int[] inout, int width, int height, int radius, boolean hasAlpha) {
        int cores = EXECUTOR_THREADS;
        ArrayList<BlurTask> horizontal = new ArrayList<>(cores);
        ArrayList<BlurTask> vertical = new ArrayList<>(cores);
        for (int i = 0; i < cores; i++) {
            horizontal.add(new BlurTask(inout, width, height, radius, cores, i, 1, hasAlpha));
            vertical.add(new BlurTask(inout, width, height, radius, cores, i, 2, hasAlpha));
        }
        try {
            EXECUTOR.invokeAll(horizontal);
            try {
                EXECUTOR.invokeAll(vertical);
            } catch (Exception e) {
                Log.e(TAG, "GaussianBlur:blurIteration_nativeTask  e2 = " + e);
            }
        } catch (Exception e2) {
            Log.e(TAG, "GaussianBlur:blurIteration_nativeTask  e1 = " + e2);
        }
    }
}
