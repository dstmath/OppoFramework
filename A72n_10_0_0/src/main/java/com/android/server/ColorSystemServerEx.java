package com.android.server;

import android.app.ColorExSystemServiceHelper;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.UserHandle;
import android.util.Slog;
import android.util.TimingsTraceLog;
import com.android.server.job.OppoJobSchedulerService;
import com.android.server.notification.OppoNotificationManagerService;
import com.android.server.policy.OppoPhoneWindowManager;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.storage.ColorDeviceStorageMonitorService;
import com.android.server.wm.WindowManagerService;
import com.coloros.exsystemservice.IColorExSystemService;

public class ColorSystemServerEx extends ColorDummySystemServerEx {
    private static final TimingsTraceLog BOOT_TIMINGS_TRACE_LOG = new TimingsTraceLog(SYSTEM_SERVER_TIMING_TAG, 524288);
    private static final String SYSTEM_SERVER_TIMING_ASYNC_TAG = "ColorSystemServerTimingAsync";
    private static final String SYSTEM_SERVER_TIMING_TAG = "ColorSystemServerTiming";
    private ServiceConnection mConn = new ServiceConnection() {
        /* class com.android.server.ColorSystemServerEx.AnonymousClass1 */

        public void onServiceConnected(ComponentName name, IBinder service) {
            String str = ColorSystemServerEx.this.TAG;
            Slog.i(str, "bindOppoExSystemService onServiceConnected pid = " + Process.myPid() + ",name = " + name);
            ColorLocalServices.addService(IColorExSystemService.class, IColorExSystemService.Stub.asInterface(service));
        }

        public void onServiceDisconnected(ComponentName name) {
            String str = ColorSystemServerEx.this.TAG;
            Slog.i(str, "bindOppoExSystemService onServiceDisconnected name = " + name);
        }
    };

    public ColorSystemServerEx(Context context) {
        super(context);
    }

    public void startBootstrapServices() {
        Slog.d(this.TAG, "startBootstrapServices");
    }

    public void startCoreServices() {
        Slog.d(this.TAG, "startCoreServices");
        traceBeginAndSlog("Start OppoExSystemService");
        try {
            bindOppoExSystemService();
        } catch (Throwable e) {
            Slog.w(this.TAG, "***********************************************");
            Slog.wtf(this.TAG, "BOOT FAILURE starting OppoExSystemService ", e);
        }
        traceEnd();
    }

    public void startOtherServices() {
        Slog.d(this.TAG, "startOtherServices");
        startTencentTRPEngService();
        startQihoo360TransmitPointService();
    }

    public void systemReady() {
        Slog.d(this.TAG, "systemReady");
    }

    public void systemRunning() {
        Slog.d(this.TAG, "systemRunning");
    }

    public boolean startColorLightService() {
        return true;
    }

    public boolean startColorDeviceStorageMonitorService() {
        this.mSystemServiceManager.startService(ColorDeviceStorageMonitorService.class);
        return true;
    }

    public boolean startColorAccessibilityService() {
        return ColorSystemServerEx.super.startColorAccessibilityService();
    }

    public boolean startColorStatusBarService(WindowManagerService wms) {
        return ColorSystemServerEx.super.startColorStatusBarService(wms);
    }

    public boolean startColorNotificationManagerService() {
        this.mSystemServiceManager.startService(OppoNotificationManagerService.class);
        return true;
    }

    public PhoneWindowManager startPhoneWindowManager() {
        return new OppoPhoneWindowManager();
    }

    public boolean startColorJobSchedulerService() {
        this.mSystemServiceManager.startService(OppoJobSchedulerService.class);
        return true;
    }

    private void startTencentTRPEngService() {
    }

    private void startQihoo360TransmitPointService() {
    }

    private void bindOppoExSystemService() {
        Intent intent = new Intent();
        intent.addFlags(256);
        intent.setComponent(ColorExSystemServiceHelper.getInstance().getComponentName());
        this.mSystemContext.bindServiceAsUser(intent, this.mConn, 1, UserHandle.SYSTEM);
    }

    private void traceBeginAndSlog(String name) {
        Slog.i(this.TAG, name);
        BOOT_TIMINGS_TRACE_LOG.traceBegin(name);
    }

    private void traceEnd() {
        BOOT_TIMINGS_TRACE_LOG.traceEnd();
    }
}
