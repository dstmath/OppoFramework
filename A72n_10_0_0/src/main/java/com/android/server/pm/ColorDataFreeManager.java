package com.android.server.pm;

import android.os.Handler;
import android.os.StatFs;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.storage.ColorDeviceStorageMonitorService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorDataFreeManager implements IColorDataFreeManager {
    private static final int CREATE_RESERVE_DELAY_TIME = 20000;
    protected static final int DATA_FREE_SIZE_THRESHOLD = 64;
    private static final int DATA_MIN_SIZE = 16;
    private static final int DATA_PROTECT_WRITE_SIZE = 64;
    private static final String DATA_RESERVE_PATH = "/data/system/reserve.log";
    private static final int KB = 1024;
    private static final int MB = 1048576;
    protected static final String PROP_NAME_ENABLE_DATA_FREE = "oppo.service.datafree.enable";
    protected static final String PROP_VALUE_ENABLE_DATA_FREE = "1";
    private static final int RESERVE_FILE_SIZE = 64;
    public static final String TAG = "ColorDataFreeManager";
    private static Handler mH = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorDataFreeManager sInstance = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private final Runnable mCreateDataReserveFile = new Runnable() {
        /* class com.android.server.pm.ColorDataFreeManager.AnonymousClass1 */

        public void run() {
            Slog.d(ColorDataFreeManager.TAG, "run create reserve file");
            ColorDataFreeManager.this.oppoCreateFileInData();
        }
    };
    boolean mDynamicDebug = false;

    public static ColorDataFreeManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorDataFreeManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorDataFreeManager();
                }
            }
        }
        return sInstance;
    }

    private ColorDataFreeManager() {
        mH = BackgroundThread.getHandler();
        if (mH == null) {
            Slog.e(TAG, "background thread handler is null, ERROR!");
        }
    }

    public void init() {
    }

    public boolean startDataFree() {
        int dataFreeSize = getAvaiDataSize();
        Slog.d(TAG, "startDataFree::dataFreeSize = " + dataFreeSize + "M");
        if (dataFreeSize >= 64) {
            return false;
        }
        Slog.w(TAG, "data avaible size " + dataFreeSize + "M is less than 64M, start datafree");
        SystemProperties.set(PROP_NAME_ENABLE_DATA_FREE, PROP_VALUE_ENABLE_DATA_FREE);
        return true;
    }

    public void generatePlaceHolderFiles() {
        Handler handler = mH;
        if (handler != null) {
            handler.postDelayed(this.mCreateDataReserveFile, 20000);
        } else {
            Slog.e(TAG, "generate placeholder files invalid, ERROR!");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void oppoCreateFileInData() {
        mH.removeCallbacks(this.mCreateDataReserveFile);
        File file = new File(DATA_RESERVE_PATH);
        if (!file.exists()) {
            int dataAviSize = getAvaiDataSize() - 64;
            Slog.d(TAG, "free data size is:" + dataAviSize);
            if (dataAviSize > 64) {
                Slog.d(TAG, "create a reserve file 64M");
                createReserveFile(file, 64);
            } else if (dataAviSize > 64 || dataAviSize <= 16) {
                Slog.e(TAG, "data size is less than 16M");
            } else {
                createReserveFile(file, dataAviSize);
                Slog.d(TAG, "create a reserve file dataFreeSize:" + dataAviSize + " finish!");
            }
        }
    }

    private void createReserveFile(File file, int size) {
        StringBuilder sb;
        FileOutputStream fos = null;
        try {
            FileOutputStream fos2 = new FileOutputStream(file, true);
            byte[] buf = new byte[1024];
            int large = size * 1024;
            for (long i = 0; i < ((long) large); i++) {
                fos2.write(buf);
            }
            fos2.flush();
            try {
                fos2.close();
                return;
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
            sb.append("fos close fatal error:");
            sb.append(e);
            Slog.e(TAG, sb.toString());
        } catch (IOException e2) {
            Slog.e(TAG, "reserve.log create failed!!!");
            try {
                fos.close();
            } catch (IOException e3) {
                e = e3;
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            try {
                fos.close();
            } catch (IOException e4) {
                Slog.e(TAG, "fos close fatal error:" + e4);
            }
            throw th;
        }
    }

    private int getAvaiDataSize() {
        return (int) (new StatFs("/data").getAvailableBytes() / ColorDeviceStorageMonitorService.MB_BYTES);
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorDataFreeManager.class.getName());
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }
}
