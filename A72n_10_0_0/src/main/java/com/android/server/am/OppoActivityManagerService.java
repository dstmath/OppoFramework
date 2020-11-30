package com.android.server.am;

import android.app.ActivityManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.ProfilerInfo;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;
import android.util.Slog;
import android.util.TimingsTraceLog;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.ColorAppSwitchManagerService;
import com.android.server.wm.OppoBaseActivityTaskManagerService;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.color.util.ColorTypeCastingHelper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

public class OppoActivityManagerService extends ActivityManagerService {
    private static final String UPLOAD_LOGTAG_SYSTEM = "20120";
    private static final String UPLOAD_REGISTER_CRASH = "register_crash";
    private OppoBaseActivityManagerService mBase = ((OppoBaseActivityManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityManagerService.class, this));
    private OppoBaseActivityTaskManagerService mBastAtms;
    private final Object mLock = new Object();

    public OppoActivityManagerService(Context context, ActivityTaskManagerService atm) {
        super(context, atm);
        this.mBastAtms = (OppoBaseActivityTaskManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityTaskManagerService.class, atm);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.am.OppoBaseActivityManagerService
    public void onOppoStart() {
        OppoBaseActivityManagerService oppoBaseActivityManagerService = this.mBase;
        if (oppoBaseActivityManagerService != null) {
            if (oppoBaseActivityManagerService.mColorAmsEx != null) {
                this.mBase.mColorAmsEx.onStart();
            }
            if (this.mBase.mPswAmsEx != null) {
                this.mBase.mPswAmsEx.onStart();
            }
        }
    }

    @Override // com.android.server.am.ActivityManagerService
    public void systemReady(Runnable goingCallback, TimingsTraceLog traceLog) {
        super.systemReady(goingCallback, traceLog);
        OppoBaseActivityManagerService oppoBaseActivityManagerService = this.mBase;
        if (oppoBaseActivityManagerService != null) {
            if (oppoBaseActivityManagerService.mColorAmsEx != null) {
                this.mBase.mColorAmsEx.systemReady();
            }
            if (this.mBase.mPswAmsEx != null) {
                this.mBase.mPswAmsEx.systemReady();
            }
        }
        ColorAppSwitchManagerService.getInstance().init(this.mContext, this);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.am.OppoBaseActivityManagerService
    public void handleOppoMessage(Message msg, int whichHandler) {
        OppoBaseActivityManagerService oppoBaseActivityManagerService = this.mBase;
        if (oppoBaseActivityManagerService != null) {
            if (oppoBaseActivityManagerService.mColorAmsEx != null) {
                this.mBase.mColorAmsEx.handleMessage(msg, whichHandler);
            }
            if (this.mBase.mPswAmsEx != null) {
                this.mBase.mPswAmsEx.handleMessage(msg, whichHandler);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.am.OppoBaseActivityManagerService
    public BroadcastQueue createBroadcastQueue(ActivityManagerService service, Handler handler, String name, BroadcastConstants constants, boolean allowDelayBehindServices) {
        return new BroadcastQueue(service, handler, name, constants, allowDelayBehindServices);
    }

    @Override // com.android.server.am.ActivityManagerService
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (super.onTransact(code, data, reply, flags)) {
            return true;
        }
        OppoBaseActivityManagerService oppoBaseActivityManagerService = this.mBase;
        if (oppoBaseActivityManagerService != null && oppoBaseActivityManagerService.mColorAmsEx != null && this.mBase.mColorAmsEx.onTransact(code, data, reply, flags)) {
            return true;
        }
        OppoBaseActivityManagerService oppoBaseActivityManagerService2 = this.mBase;
        if (oppoBaseActivityManagerService2 == null || oppoBaseActivityManagerService2.mPswAmsEx == null || !this.mBase.mPswAmsEx.onTransact(code, data, reply, flags)) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.am.OppoBaseActivityManagerService
    public List<ApplicationInfo> getAllTopAppInfo() {
        OppoBaseActivityTaskManagerService oppoBaseActivityTaskManagerService = this.mBastAtms;
        if (oppoBaseActivityTaskManagerService != null) {
            return oppoBaseActivityTaskManagerService.getAllTopAppInfo();
        }
        return null;
    }

    @Override // com.android.server.am.OppoBaseActivityManagerService
    public ComponentName getDockTopAppName() {
        OppoBaseActivityTaskManagerService oppoBaseActivityTaskManagerService = this.mBastAtms;
        if (oppoBaseActivityTaskManagerService != null) {
            return oppoBaseActivityTaskManagerService.getDockTopAppName();
        }
        return null;
    }

    @Override // com.android.server.am.OppoBaseActivityManagerService
    public int getWindowMode(IBinder token) throws RemoteException {
        OppoBaseActivityTaskManagerService oppoBaseActivityTaskManagerService = this.mBastAtms;
        if (oppoBaseActivityTaskManagerService != null) {
            return oppoBaseActivityTaskManagerService.getWindowMode(token);
        }
        return 0;
    }

    @Override // com.android.server.am.OppoBaseActivityManagerService
    public final int startActivityForFreeform(Intent intent, Bundle bOptions, int userId, String callPkg) {
        OppoBaseActivityTaskManagerService oppoBaseActivityTaskManagerService = this.mBastAtms;
        if (oppoBaseActivityTaskManagerService != null) {
            return oppoBaseActivityTaskManagerService.startActivityForFreeform(intent, bOptions, userId, callPkg);
        }
        return -1;
    }

    @Override // com.android.server.am.OppoBaseActivityManagerService
    public final void exitColorosFreeform(Bundle bOptions) {
        OppoBaseActivityTaskManagerService oppoBaseActivityTaskManagerService = this.mBastAtms;
        if (oppoBaseActivityTaskManagerService != null) {
            oppoBaseActivityTaskManagerService.exitColorosFreeform(bOptions);
        }
    }

    @Override // com.android.server.am.ActivityManagerService
    public int startActivity(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(2, Binder.getCallingUid());
        return super.startActivity(caller, callingPackage, intent, resolvedType, resultTo, resultWho, requestCode, startFlags, profilerInfo, bOptions);
    }

    @Override // com.android.server.am.ActivityManagerService
    public void killBackgroundProcesses(String packageName, int userId) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(77, Binder.getCallingUid());
        super.killBackgroundProcesses(packageName, userId);
    }

    @Override // com.android.server.am.ActivityManagerService
    public void closeSystemDialogs(String reason) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(7, Binder.getCallingUid());
        super.closeSystemDialogs(reason);
    }

    @Override // com.android.server.am.ActivityManagerService
    public Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(76, Binder.getCallingUid());
        return super.getProcessMemoryInfo(pids);
    }

    @Override // com.android.server.am.ActivityManagerService
    public void moveTaskToFront(IApplicationThread appThread, String callingPackage, int taskId, int flags, Bundle bOptions) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(3, Binder.getCallingUid());
        super.moveTaskToFront(appThread, callingPackage, taskId, flags, bOptions);
    }

    @Override // com.android.server.am.ActivityManagerService
    public List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState() {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(75, Binder.getCallingUid());
        return super.getProcessesInErrorState();
    }

    @Override // com.android.server.am.ActivityManagerService
    public int bindService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String callingPackage, int userId) throws TransactionTooLargeException {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(11, Binder.getCallingUid());
        return super.bindService(caller, token, service, resolvedType, connection, flags, callingPackage, userId);
    }

    @Override // com.android.server.am.ActivityManagerService
    public int bindIsolatedService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String instanceName, String callingPackage, int userId) throws TransactionTooLargeException {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(11, Binder.getCallingUid());
        return super.bindIsolatedService(caller, token, service, resolvedType, connection, flags, instanceName, callingPackage, userId);
    }

    @Override // com.android.server.am.ActivityManagerService
    public boolean unbindService(IServiceConnection connection) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(10, Binder.getCallingUid());
        return super.unbindService(connection);
    }

    @Override // com.android.server.am.ActivityManagerService
    public void unregisterReceiver(IIntentReceiver receiver) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(9, Binder.getCallingUid());
        super.unregisterReceiver(receiver);
    }

    @Override // com.android.server.am.ActivityManagerService
    public boolean updateConfiguration(Configuration values) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(8, Binder.getCallingUid());
        return super.updateConfiguration(values);
    }

    @Override // com.android.server.am.OppoBaseActivityManagerService
    public void collectExceptionStatistics(SecurityException ex1, String callerPackage) throws SecurityException {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex1.printStackTrace(pw);
            pw.close();
            final Map<String, String> eventMap = new HashMap<>();
            eventMap.put("crash_package", callerPackage);
            eventMap.put("crash_trace", sw.toString());
            new Thread(new Runnable() {
                /* class com.android.server.am.OppoActivityManagerService.AnonymousClass1 */

                public void run() {
                    OppoStatistics.onCommon(OppoActivityManagerService.this.mContext, "20120", OppoActivityManagerService.UPLOAD_REGISTER_CRASH, eventMap, false);
                }
            }).start();
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }
        Slog.e("ActivityManager", "registerReceiver failed.");
        ex1.printStackTrace();
    }

    private static BroadcastQueue createOppoBroadcastQueue(ActivityManagerService service, Handler handler, String name, BroadcastConstants constants, boolean allowDelayBehindServices) {
        Slog.i("ActivityManager", "createOppoBroadcastQueue reflect");
        try {
            return (BroadcastQueue) Class.forName("com.android.server.am.OppoBroadcastQueue").getDeclaredConstructor(ActivityManagerService.class, Handler.class, String.class, BroadcastConstants.class, Boolean.TYPE).newInstance(service, handler, name, constants, Boolean.valueOf(allowDelayBehindServices));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return null;
        } catch (InstantiationException e4) {
            e4.printStackTrace();
            return null;
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
            return null;
        }
    }
}
