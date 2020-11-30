package com.oppo.benchmark;

import android.app.ActivityThread;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemProperties;
import android.util.Log;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class OppoBenchHelper {
    private static final String BENCH_MARK_ANTUTU = "com.antutu.ABenchMark";
    private static final int BENCH_MARK_ANTUTU_FINISHED = -2;
    private static final int BENCH_MARK_ANTUTU_FIRST_STEP = 31;
    static final int BENCH_MARK_ANTUTU_MULTITASK = 11;
    static final int BENCH_MARK_ANTUTU_MULTITHREAD = 10;
    private static final int BENCH_MARK_ANTUTU_UX_FIRST_STEP = 6;
    static final String BENCH_MARK_CPU_MULTI_THREAD_PROC = "/proc/sys/kernel/cpu_multi_thread";
    private static final String BENCH_MARK_LUDASHI = "com.ludashi.benchmark";
    private static final String BENCH_MODE_DISABLE = "0";
    private static final String BENCH_MODE_ENABLE = "1";
    private static final String BENCH_MODE_ENABLE_WITH_JPEG_MUTIL = "2";
    private static final long BITMAP_CACHE_TIMEOUT = 1000;
    private static final String CLASS_NAME_OPPOSCREENMODEINJECTOR = "android.hardware.display.DisplayManager";
    private static final boolean DEBUG = (!SystemProperties.getBoolean("ro.build.release_type", false));
    private static final String SYSTEM_PROPERTIES_EXP = "oppo.version.exp";
    private static final String SYSTEM_PROPERTIES_SPEC = "sys.oppo.high.performance.spec";
    private static final String TAG = "OppoBenchHelper";
    private static Bitmap bitmapCache = null;
    private static int lastResId = -999;
    private static String lastResStr = "";
    private static long lastTimestamp = -999;
    private static final Object mLock = new Object();
    private static OppoBenchHelper sInstance = null;
    private int mExp = -1;

    private OppoBenchHelper() {
    }

    public static OppoBenchHelper getInstance() {
        OppoBenchHelper oppoBenchHelper;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new OppoBenchHelper();
            }
            oppoBenchHelper = sInstance;
        }
        return oppoBenchHelper;
    }

    private boolean isExp() {
        int i = this.mExp;
        if (-1 != i) {
            return 1 == i;
        }
        if ("".equals(SystemProperties.get(SYSTEM_PROPERTIES_EXP, ""))) {
            this.mExp = 0;
            return false;
        }
        this.mExp = 1;
        return true;
    }

    public boolean isEnableBitmapCache() {
        String benchMode = SystemProperties.get(SYSTEM_PROPERTIES_SPEC, "0");
        if (isExp()) {
            return false;
        }
        if ("1".equals(benchMode) && BENCH_MARK_LUDASHI.equals(ActivityThread.currentPackageName())) {
            return true;
        }
        Bitmap bitmap = bitmapCache;
        if (bitmap != null) {
            bitmap.recycle();
            bitmapCache = null;
        }
        return false;
    }

    public Bitmap getBitmapCache(Resources res, int id, BitmapFactory.Options opts) {
        if (!isEnableBitmapCache()) {
            return null;
        }
        synchronized (mLock) {
            if (opts == null) {
                if (lastResId == id && lastResStr.equals(res.toString()) && bitmapCache != null && System.currentTimeMillis() - lastTimestamp < 1000) {
                    if (DEBUG) {
                        Log.i(TAG, "using bitmap cache");
                    }
                    lastTimestamp = System.currentTimeMillis();
                    return Bitmap.createBitmap(bitmapCache);
                }
            }
            return null;
        }
    }

    public void setBitmapCache(Bitmap cache, Resources res, int id) {
        if (isEnableBitmapCache()) {
            synchronized (mLock) {
                lastResStr = res.toString();
                lastResId = id;
                bitmapCache = cache;
                lastTimestamp = System.currentTimeMillis();
                if (DEBUG) {
                    Log.i(TAG, "caching bitmap");
                }
            }
        }
    }

    public void benchStepCheck(Context mContext, Intent intent) {
        String benchMode = SystemProperties.get(SYSTEM_PROPERTIES_SPEC, "0");
        String pkgName = intent.getPackage();
        if (DEBUG) {
            Log.d(TAG, "benchMode:" + benchMode + "; pkgName" + pkgName);
        }
        if (!isExp() && !"0".equals(benchMode) && BENCH_MARK_ANTUTU.equals(pkgName)) {
            int bm_uid = intent.getIntExtra("uid", -999);
            if (DEBUG) {
                Log.d(TAG, "bm_uid:" + bm_uid);
            }
            if (bm_uid == -2) {
                SystemProperties.set(SYSTEM_PROPERTIES_SPEC, "1");
                if (DEBUG) {
                    Log.d(TAG, "BENCH_MARK_ANTUTU_FINISHED");
                }
            } else if (bm_uid == 6) {
                requestRefreshRate(mContext, true);
                SystemProperties.set(SYSTEM_PROPERTIES_SPEC, BENCH_MODE_ENABLE_WITH_JPEG_MUTIL);
                if (DEBUG) {
                    Log.d(TAG, "BENCH_MARK_ANTUTU_UX_FIRST_STEP");
                }
            } else if (bm_uid == 31) {
                requestRefreshRate(mContext, false);
                SystemProperties.set(SYSTEM_PROPERTIES_SPEC, "1");
                if (DEBUG) {
                    Log.d(TAG, "BENCH_MARK_ANTUTU_FIRST_STEP");
                }
            } else if (bm_uid == 10) {
                enableMBMultiThread(true);
            } else if (bm_uid == 11) {
                enableMBMultiThread(false);
            }
        }
    }

    private void requestRefreshRate(Context mContext, Boolean open) {
        try {
            Class<?> cls = Class.forName(CLASS_NAME_OPPOSCREENMODEINJECTOR);
            Constructor<?> constructor = cls.getDeclaredConstructor(Context.class);
            Method requestRefreshRate = cls.getMethod("enterBenchmarkMode", Boolean.TYPE);
            requestRefreshRate.setAccessible(true);
            requestRefreshRate.invoke(constructor.newInstance(mContext), open);
            if (!DEBUG) {
                return;
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.i(TAG, "requestRefreshRate exception : " + e.getMessage());
            }
            if (!DEBUG) {
                return;
            }
        } catch (Throwable th) {
            if (DEBUG) {
                Log.d(TAG, "do requestRefreshRate right");
            }
            throw th;
        }
        Log.d(TAG, "do requestRefreshRate right");
    }

    private void enableMBMultiThread(boolean enable) {
        FileWriter fileWritter = null;
        try {
            FileWriter fileWritter2 = new FileWriter(BENCH_MARK_CPU_MULTI_THREAD_PROC);
            if (enable) {
                fileWritter2.write("1");
            } else {
                fileWritter2.write("0");
            }
            try {
                fileWritter2.close();
            } catch (Exception e) {
            }
        } catch (Exception e2) {
            if (0 != 0) {
                fileWritter.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fileWritter.close();
                } catch (Exception e3) {
                }
            }
            throw th;
        }
    }
}
