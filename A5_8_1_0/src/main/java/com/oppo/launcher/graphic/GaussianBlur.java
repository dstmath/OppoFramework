package com.oppo.launcher.graphic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceControl;
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
    private static final String TAG = "GaussianBlur";
    private static GaussianBlur mGaussianBlur = null;
    public static int mScreenHeight = -1;
    public static int mScreenWidth = -1;

    private class BlurTask implements Callable<Void> {
        private final int core;
        private final int cores;
        private final boolean hasAlpha;
        private final int height;
        private final int[] inout;
        private final int radius;
        private final int step;
        private final int width;

        public BlurTask(int[] src, int w, int h, int r, int totalCores, int coreIndex, int round, boolean alpha) {
            this.inout = src;
            this.width = w;
            this.height = h;
            this.radius = r;
            this.cores = totalCores;
            this.core = coreIndex;
            this.step = round;
            this.hasAlpha = alpha;
        }

        public Void call() throws Exception {
            if (this.hasAlpha) {
                GaussianBlur.this.blurIterationAlpha_native(this.inout, this.width, this.height, this.radius, this.cores, this.core, this.step);
            } else {
                GaussianBlur.this.blurIteration_native(this.inout, this.width, this.height, this.radius, this.cores, this.core, this.step);
            }
            return null;
        }
    }

    public native void blurBrightness_native(int[] iArr, int i, int i2, float f);

    public native void blurIterationAlpha_native(int[] iArr, int i, int i2, int i3, int i4, int i5, int i6);

    public native void blurIteration_native(int[] iArr, int i, int i2, int i3, int i4, int i5, int i6);

    static {
        System.loadLibrary("gaussgraphic");
    }

    private GaussianBlur() {
    }

    public static GaussianBlur getInstance() {
        if (mGaussianBlur == null) {
            mGaussianBlur = new GaussianBlur();
        }
        return mGaussianBlur;
    }

    public static void setScreenWidth(Context context) {
        if (mScreenWidth == -1 || mScreenWidth == -1) {
            int i;
            Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics mDisplayMetrics = new DisplayMetrics();
            display.getRealMetrics(mDisplayMetrics);
            int realWidth = mDisplayMetrics.widthPixels;
            int realHeight = mDisplayMetrics.heightPixels;
            if (realWidth < realHeight) {
                i = realWidth;
            } else {
                i = realHeight;
            }
            mScreenWidth = i;
            if (realWidth >= realHeight) {
                realHeight = realWidth;
            }
            mScreenHeight = realHeight;
            if (DEBUG) {
                Log.d(TAG, "GaussianBlur:setScreenWidth   getRotation() " + display.getRotation() + ", mScreenWidth = " + mScreenWidth);
            }
        }
    }

    public static Bitmap scaleBitmap(Bitmap bm) {
        return scaleBitmap(bm, 0.25f);
    }

    public static Bitmap scaleBitmap(Bitmap bm, float scale) {
        Bitmap bmp = null;
        if (bm != null) {
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            int beginX = bm.getWidth() - mScreenWidth;
            int width = mScreenWidth;
            if (beginX < 0) {
                beginX = 0;
                width = bm.getWidth();
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
        int w = width;
        int h = height;
        if (width == -1 || height == -1) {
            setScreenWidth(context);
            w = mScreenWidth;
            h = mScreenHeight;
        }
        if (minLayer < 0 || maxLayer < 0) {
            return SurfaceControl.screenshot((int) (((float) w) * scale), (int) (((float) h) * scale));
        }
        return SurfaceControl.screenshot(new Rect(0, 0, w, h), (int) (((float) w) * scale), (int) (((float) h) * scale), minLayer, maxLayer, false, 0);
    }

    public Bitmap generateGaussianWallpaper(Context context, float scale, int radius, float brightness) {
        int wallpaperLayer = System.getInt(context.getContentResolver(), "LAYER_WALLPAPER", -1);
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

    public Bitmap generateGaussianBitmap(Bitmap bmp, int radius, float brightness, boolean hasAlpha, boolean recycle) {
        if (bmp == null || bmp.isRecycled()) {
            Log.w(TAG, "GaussianBlur:generateGaussianBitmap  bmp is null or isRecycled!");
            return null;
        }
        if (DEBUG) {
            Log.v(TAG, "GaussianBlur:generateGaussianBitmap  Enter");
        }
        boolean isAlpha = bmp.hasAlpha() ? hasAlpha : false;
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] inoutPixels = new int[(width * height)];
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        bmp.getPixels(inoutPixels, 0, width, 0, 0, width, height);
        blurIteration_native(inoutPixels, width, height, radius, isAlpha);
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
            Log.d(TAG, "GaussianBlur:generateGaussianBitmap  generate Complete");
        }
        return bitmap;
    }

    public Bitmap generateGaussianBitmapTask(Bitmap bmp, int radius, float brightness, boolean hasAlpha, boolean recycle) {
        if (bmp == null || bmp.isRecycled()) {
            Log.w(TAG, "GaussianBlur:generateGaussianBitmapTask  bmp is null or isRecycled!");
            return null;
        }
        if (DEBUG) {
            Log.v(TAG, "GaussianBlur:generateGaussianBitmapTask  Enter");
        }
        boolean isAlpha = bmp.hasAlpha() ? hasAlpha : false;
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] inoutPixels = new int[(width * height)];
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
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
        ArrayList<BlurTask> horizontal = new ArrayList(cores);
        ArrayList<BlurTask> vertical = new ArrayList(cores);
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
