package com.android.server.mdmcrsh;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IOppoService;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;

public final class OppomodemService extends IOppoService.Stub {
    static final boolean DEBUG = true;
    public static final int DELAY_TIME = 36000000;
    public static final String FILTER_NAME = "criticallog_config";
    private static final String TAG = "OppomodemService";
    private HandlerThread handlerThread = new HandlerThread(TAG);
    private AlertDialog mCheckNetworkDialog = null;
    private Context mContext;
    private Handler mHandler;
    private ModemcrashLogObserver mModemcrashLogObserver;

    public OppomodemService(Context context) {
        this.mContext = context;
        this.handlerThread.start();
        this.mHandler = this.handlerThread.getThreadHandler();
        this.mModemcrashLogObserver = new ModemcrashLogObserver(this.mContext, this.handlerThread.getLooper());
        this.mModemcrashLogObserver.init();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        OppomodemService.super.finalize();
    }

    /* access modifiers changed from: package-private */
    public void SyncCacheToEmmcTimmer() {
        Log.v(TAG, "Do nothing 15,just for complie");
    }

    public void systemReady() {
        this.mHandler.postDelayed(new Runnable() {
            /* class com.android.server.mdmcrsh.OppomodemService.AnonymousClass1 */

            public void run() {
                Log.v(OppomodemService.TAG, "systemReady initLogCoreService");
                if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                    Log.v(OppomodemService.TAG, "systemReady test initLogCoreService");
                    return;
                }
                Log.v(OppomodemService.TAG, "check customer log tag");
                if (SystemProperties.getBoolean("persist.sys.log.customer", false)) {
                    Log.v(OppomodemService.TAG, "customer test logging");
                }
            }
        }, 20000);
    }

    public String getOppoLogInfoString(int index) {
        if (Binder.getCallingUid() != 1000) {
            return null;
        }
        Log.v(TAG, "Do nothing 12,just for complie");
        return null;
    }

    public void assertKernelPanic() {
        Log.e(TAG, "Do nothing 11,just for complie");
    }

    public void deleteSystemLogFile() {
        Log.e(TAG, "Do nothing 10,just for complie");
    }

    public boolean iScoreLogServiceRunning() {
        Log.e(TAG, "Do nothing 9,just for complie");
        return false;
    }

    public void StartLogCoreService() {
        Log.e(TAG, "StartLogCoreService Do nothing 8,just for complie");
    }

    public void startOppoFileEncodeHelperService() {
        Log.v(TAG, "Do nothing 7,just for complie");
    }

    public void unbindOppoFileEncodeHelperService() {
        Log.v(TAG, "Do nothing 6,just for complie");
    }

    public void unbindCoreLogService() {
        Log.e(TAG, "Do nothing 5,just for complie");
    }

    public boolean copyFileForDcs(String srcPath, String destPath) {
        Log.e(TAG, "Do nothing 4,just for complie");
        return false;
    }

    public void deleteFileForDcs(String fileName) {
        Log.d(TAG, "pid " + Binder.getCallingPid() + " call deleteFileForDcs");
        Log.d(TAG, "Do nothing 3,just for complie");
    }

    public void startSensorLog(boolean isOutPutFile) {
        Slog.v(TAG, "set sys.oppo.reboot 1");
    }

    public void stopSensorLog() {
        Slog.v(TAG, "set sys.oppo.reboot 1");
    }

    public boolean openFlashLight() {
        return false;
    }

    public boolean closeFlashLight() {
        return false;
    }

    public String getFlashLightState() {
        return "";
    }

    public void iotop() {
    }

    public boolean copyFile(String destPath, String srcPath) {
        if (this.mContext == null) {
            return false;
        }
        Slog.v(TAG, "Do nothing 2,just for complie");
        return false;
    }

    public boolean deleteFile(String path) {
        Slog.v(TAG, "Do nothing 1,just for complie");
        return false;
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        Slog.v(TAG, "do nothing");
    }

    public void sendOnStampEvent(String eventId, Map map) {
        Slog.e(TAG, "stamp no oppo component");
    }

    public void sendDeleteStampId(String eventId) {
        Slog.e(TAG, "delete stamp no oppo component");
    }
}
