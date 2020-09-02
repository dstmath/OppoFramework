package com.android.server.am;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
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
    private static final String BENCH_MODE_DISABLE = "0";
    private static final String BENCH_MODE_ENABLE = "1";
    private static final String BENCH_MODE_ENABLE_WITH_JPEG_MUTIL = "2";
    private static final String CLASS_NAME_OPPOSCREENMODEINJECTOR = "android.hardware.display.DisplayManager";
    private static final boolean DEBUG = false;
    private static final String TAG = "OppoBenchHelper";
    private static final Object mLock = new Object();
    private static OppoBenchHelper sInstance = null;

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

    public void benchStepCheck(Context mContext, Intent intent) {
        String benchMode = SystemProperties.get("sys.oppo.high.performance.spec", "-999");
        String pkgName = intent.getPackage();
        if (!BENCH_MODE_DISABLE.equals(benchMode) && BENCH_MARK_ANTUTU.equals(pkgName)) {
            int bm_uid = intent.getIntExtra(WatchlistLoggingHandler.WatchlistEventKeys.UID, -999);
            if (bm_uid == -2) {
                SystemProperties.set("sys.oppo.high.performance.spec", BENCH_MODE_ENABLE);
            } else if (bm_uid == 6) {
                requestRefreshRate(mContext, true);
                SystemProperties.set("sys.oppo.high.performance.spec", BENCH_MODE_ENABLE_WITH_JPEG_MUTIL);
            } else if (bm_uid == 31) {
                requestRefreshRate(mContext, false);
                SystemProperties.set("sys.oppo.high.performance.spec", BENCH_MODE_ENABLE);
            } else if (bm_uid == 10) {
                enableMBMultiThread(true);
            } else if (bm_uid == 11) {
                enableMBMultiThread(false);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
        return;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    private void requestRefreshRate(Context mContext, Boolean open) {
        Class<?> cls = Class.forName(CLASS_NAME_OPPOSCREENMODEINJECTOR);
        Constructor<?> constructor = cls.getDeclaredConstructor(Context.class);
        Method requestRefreshRate = cls.getMethod("enterBenchmarkMode", Boolean.TYPE);
        requestRefreshRate.setAccessible(true);
        requestRefreshRate.invoke(constructor.newInstance(mContext), open);
    }

    private void enableMBMultiThread(boolean enable) {
        FileWriter fileWritter = null;
        try {
            FileWriter fileWritter2 = new FileWriter(BENCH_MARK_CPU_MULTI_THREAD_PROC);
            if (enable) {
                fileWritter2.write(BENCH_MODE_ENABLE);
            } else {
                fileWritter2.write(BENCH_MODE_DISABLE);
            }
            try {
                fileWritter2.close();
            } catch (Exception e) {
            }
        } catch (Exception e2) {
            if (fileWritter != null) {
                fileWritter.close();
            }
        } catch (Throwable th) {
            if (fileWritter != null) {
                try {
                    fileWritter.close();
                } catch (Exception e3) {
                }
            }
            throw th;
        }
    }
}
