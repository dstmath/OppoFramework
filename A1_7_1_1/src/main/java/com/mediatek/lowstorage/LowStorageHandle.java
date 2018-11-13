package com.mediatek.lowstorage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocationManagerService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class LowStorageHandle {
    public static final boolean FORCE_CLEAN_ENABLE = false;
    public static final long LSM_THRESHOLD_FORCE_CLEAN = 0;
    public static final long LSM_THRESHOLD_LOWMEM = 1536;
    public static final long LSM_THRESHOLD_WARN = 4096;
    public static final long PIGGYBANK_MAX_KB_SIZE = 4096;
    public static final String PIGGYBANK_PATH = "/data/piggybank";
    static final String TAG = "LowStorageHandle";
    private static LowStorageHandle sInstance;
    public final String DEL_FILENAME_PATTERN = "^core\\.[0-9]*";
    private Context mContext = null;

    private void LSMRemoveCoredump() {
        Log.d(TAG, "remove system core dump file to save storge memory");
        String root_path = Environment.getDataDirectory().getPath() + "/core";
        File path = new File(root_path);
        if (path.list() != null) {
            for (String filename : path.list()) {
                if (filename.matches("^core\\.[0-9]*")) {
                    File f_remove = new File(root_path + "/" + filename);
                    if (f_remove.exists()) {
                        Log.d(TAG, "find and remove system core dump file: " + filename + ";free :" + f_remove.length());
                        f_remove.delete();
                    }
                }
            }
        }
    }

    public LowStorageHandle(Context context) {
        this.mContext = context;
    }

    public void registerFilter() {
        Slog.d(TAG, "register filter");
        if (this.mContext != null) {
            Slog.d(TAG, "register receiver");
            IntentFilter lsmFilter = new IntentFilter();
            lsmFilter.addAction("android.intent.action.DEVICE_STORAGE_LOW");
            lsmFilter.addAction("android.intent.action.DEVICE_STORAGE_FULL");
            lsmFilter.addAction("android.intent.action.DEVICE_STORAGE_NOT_FULL");
            lsmFilter.addAction("android.intent.action.DEVICE_STORAGE_OK");
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if ("android.intent.action.DEVICE_STORAGE_LOW".equals(intent.getAction())) {
                        long freeKBStorage = 0;
                        StatFs dataFileStats = new StatFs(Environment.getDataDirectory().getPath());
                        Slog.e(LowStorageHandle.TAG, "receive the storage low intent");
                        try {
                            dataFileStats.restat(Environment.getDataDirectory().getPath());
                            freeKBStorage = (((long) dataFileStats.getAvailableBlocks()) * ((long) dataFileStats.getBlockSize())) / 1024;
                        } catch (IllegalArgumentException e) {
                            Slog.d(LowStorageHandle.TAG, "IllegalArgumentException");
                        }
                        if (freeKBStorage < LowStorageHandle.LSM_THRESHOLD_LOWMEM) {
                            SystemProperties.set("sys.lowstorage_flag", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                        }
                    } else if ("android.intent.action.DEVICE_STORAGE_FULL".equals(intent.getAction())) {
                        Slog.d(LowStorageHandle.TAG, "get storage full intent ");
                        SystemProperties.set("sys.lowstorage_flag", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                    } else if ("android.intent.action.DEVICE_STORAGE_NOT_FULL".equals(intent.getAction())) {
                        Slog.e(LowStorageHandle.TAG, "get storage not full intent");
                        SystemProperties.set("sys.lowstorage_flag", "0");
                    } else if ("android.intent.action.DEVICE_STORAGE_OK".equals(intent.getAction())) {
                        Slog.e(LowStorageHandle.TAG, "receive the storage ok intent");
                        SystemProperties.set("sys.lowstorage_flag", "0");
                    }
                }
            }, lsmFilter);
            return;
        }
        Slog.e(TAG, "mContext is null");
    }

    public boolean GetCurrentFlag() {
        return SystemProperties.get("sys.lowstorage_flag", "0").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
    }

    public void systemReadyLSM() {
        long freeKBStorage = 0;
        Log.d(TAG, " LSM_THRESHOLD_FORCE_CLEAN : 0; LSM_THRESHOLD_LOWMEM: 1536;LSM_THRESHOLD_WARN :4096");
        Log.d(TAG, " FORCE_CLEAN_ENABLE : false");
        StatFs dataFileStats = new StatFs(Environment.getDataDirectory().getPath());
        try {
            dataFileStats.restat(Environment.getDataDirectory().getPath());
            freeKBStorage = (((long) dataFileStats.getAvailableBlocks()) * ((long) dataFileStats.getBlockSize())) / 1024;
        } catch (IllegalArgumentException e) {
            Slog.d(TAG, "IllegalArgumentException");
        }
        Log.d(TAG, "data.free.before KB: " + Long.toString(freeKBStorage));
        long piggyKBSize = 4096;
        if (freeKBStorage <= 8192 && freeKBStorage > 4096) {
            LSMRemoveCoredump();
        } else if (freeKBStorage <= 4096 && freeKBStorage >= 2048) {
            piggyKBSize = 2048;
            LSMRemoveCoredump();
        } else if (freeKBStorage < 2048) {
            piggyKBSize = (long) (((double) freeKBStorage) * 0.8d);
            LSMRemoveCoredump();
        }
        Log.d(TAG, "systemReady : want to create piggybank KB:" + Long.toString(piggyKBSize));
        File f = new File(PIGGYBANK_PATH);
        if (!f.exists()) {
            OutputStream out;
            try {
                out = new FileOutputStream(f);
                byte[] buffer = new byte[2048];
                for (int dataWrite = 0; ((long) dataWrite) < piggyKBSize / 2; dataWrite++) {
                    out.write(buffer, 0, 2048);
                }
                out.close();
            } catch (IOException e2) {
                Log.d(TAG, " Can't create piggybank" + e2);
            } catch (Throwable th) {
                out.close();
            }
        }
        try {
            dataFileStats.restat(Environment.getDataDirectory().getPath());
            freeKBStorage = (((long) dataFileStats.getAvailableBlocks()) * ((long) dataFileStats.getBlockSize())) / 1024;
        } catch (IllegalArgumentException e3) {
            Slog.d(TAG, "IllegalArgumentException");
        }
        Log.d(TAG, " data.free.after KB: " + Long.toString(freeKBStorage));
    }
}
