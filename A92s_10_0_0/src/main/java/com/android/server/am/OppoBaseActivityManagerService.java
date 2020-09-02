package com.android.server.am;

import android.app.ApplicationErrorReport;
import android.app.IActivityManager;
import android.app.OppoActivityManagerInternal;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.OppoPackageManagerInternal;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.LocalServices;
import java.io.File;
import java.util.List;

public abstract class OppoBaseActivityManagerService extends IActivityManager.Stub {
    static final int COLOR_AMS_BG_HANDLER = 3;
    static final int COLOR_AMS_KILL_HANDLER = 4;
    static final int COLOR_AMS_MAIN_HANLDER = 1;
    static final int COLOR_AMS_MSG_INDEX = 500;
    static final int COLOR_AMS_UI_HANDLER = 2;
    public static boolean DEBUG_COLOROS_AMS = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = "ActivityManager";
    public IColorActivityManagerServiceEx mColorAmsEx = null;
    public IColorActivityManagerServiceInner mColorAmsInner = null;
    OppoPackageManagerInternal mOppoPackageManagerInt;
    public IPswActivityManagerServiceEx mPswAmsEx = null;

    /* access modifiers changed from: package-private */
    public OppoPackageManagerInternal getOppoPackageManagerInternalLocked() {
        if (this.mOppoPackageManagerInt == null) {
            this.mOppoPackageManagerInt = (OppoPackageManagerInternal) LocalServices.getService(OppoPackageManagerInternal.class);
        }
        return this.mOppoPackageManagerInt;
    }

    /* access modifiers changed from: protected */
    public void onOppoStart() {
        warn("onStart");
    }

    /* access modifiers changed from: protected */
    public void onOppoSystemReady() {
        warn("onOppoSystemReady");
    }

    /* access modifiers changed from: protected */
    public void handleOppoMessage(Message msg, int whichHandler) {
        warn("handleOppoMessage");
    }

    /* access modifiers changed from: protected */
    public BroadcastQueue createBroadcastQueue(ActivityManagerService service, Handler handler, String name, BroadcastConstants constants, boolean allowDelayBehindServices) {
        return new BroadcastQueue(service, handler, name, constants, allowDelayBehindServices);
    }

    private final void warn(String methodName) {
        Slog.w(TAG, methodName + " not implemented");
    }

    public List<String> getAllTopPkgName() {
        return null;
    }

    public ApplicationInfo getFreeFormAppInfo() {
        return null;
    }

    public List<ApplicationInfo> getAllTopAppInfo() {
        return null;
    }

    public ComponentName getDockTopAppName() {
        return null;
    }

    public int getWindowMode(IBinder token) throws RemoteException {
        return 0;
    }

    public int startActivityForFreeform(Intent intent, Bundle bOptions, int userId, String callPkg) {
        return -1;
    }

    public void exitColorosFreeform(Bundle bOptions) {
    }

    public void addTimeInfo(StringBuilder sb) {
        OppoFeatureCache.get(IColorEapManager.DEFAULT).addTimeInfo(sb);
    }

    public void setErrorPackageName(String pkg) {
        OppoFeatureCache.get(IColorEapManager.DEFAULT).setErrorPackageName(pkg);
    }

    public void collectEapInfo(Context context, String dropboxTag, String eventType, ProcessRecord process, String subject, File dataFile, ApplicationErrorReport.CrashInfo crashInfo) {
        OppoFeatureCache.get(IColorEapManager.DEFAULT).collectEapInfo(context, dropboxTag, eventType, process, subject, dataFile, crashInfo);
    }

    public int getDataFileSizeAjusted(int prevSize, int lineSize, File file) {
        return OppoFeatureCache.get(IColorEapManager.DEFAULT).getDataFileSizeAjusted(prevSize, lineSize, file);
    }

    public void appendCpuInfo(StringBuilder sb, String eventType) {
        OppoFeatureCache.get(IColorEapManager.DEFAULT).appendCpuInfo(sb, eventType);
    }

    public void setCrashProcessRecord(ProcessRecord processRecord) {
        OppoFeatureCache.get(IColorEapManager.DEFAULT).setCrashProcessRecord(processRecord);
    }

    public void collectExceptionStatistics(SecurityException ex1, String callerPackage) {
    }

    /* access modifiers changed from: protected */
    public void publishOppoAmsInternal() {
        LocalServices.addService(OppoActivityManagerInternal.class, new OppoActivityManagerInternalImpl());
    }

    private class OppoActivityManagerInternalImpl extends OppoActivityManagerInternal {
        private OppoActivityManagerInternalImpl() {
        }

        public int getProcPid(int pid) {
            return pid;
        }

        public int startActivityAsUserEmpty(Bundle options) {
            return OppoBaseActivityManagerService.this.requestStartActivityAsUserEmpty(options);
        }
    }

    public int requestStartActivityAsUserEmpty(Bundle options) {
        return -1;
    }
}
