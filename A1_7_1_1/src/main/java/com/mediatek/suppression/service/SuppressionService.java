package com.mediatek.suppression.service;

import android.app.AppGlobals;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.Trace;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.app.procstats.ProcessStats.ProcessStateHolder;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ActivityManagerService.SuppressManager;
import com.android.server.am.BroadcastQueue;
import com.android.server.job.JobSchedulerInternal;
import com.mediatek.am.AMEventHookData.PackageStoppedStatusChanged;
import com.mediatek.apm.suppression.SuppressionAction;
import com.mediatek.server.am.AMEventHook.Event;
import java.util.ArrayList;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SuppressionService extends SystemService {
    static a b;
    private SuppressionAction a;
    private ActivityManagerService mAm;
    private Context mContext;
    private SuppressManager mSuppressManager;

    final class a extends Handler {
        public a(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 5000:
                    Trace.traceBegin(64, "cancelJobsForUid");
                    JobSchedulerInternal jobSchedulerInternal = (JobSchedulerInternal) LocalServices.getService(JobSchedulerInternal.class);
                    if (jobSchedulerInternal == null) {
                        Slog.i("SuppressionService", "[process suppression] mJobSchedulerInternal = null!");
                    } else {
                        jobSchedulerInternal.cancelJobsForUid(((Integer) message.obj).intValue());
                    }
                    Trace.traceEnd(64);
                    return;
                default:
                    super.handleMessage(message);
                    return;
            }
        }
    }

    class b extends SuppressionInternal {
        b() {
        }

        public void suppressPackages(List<String> list, int i, String str) {
            SuppressionService.this.suppressPackages(list, i, str);
        }

        public void unsuppressPackages(String str) {
            int i = 0;
            if (SuppressionService.this.a == null) {
                SuppressionService.this.a = SuppressionAction.getInstance(SuppressionService.this.mContext);
            }
            if ((SuppressionService.this.a.getUnsuppressPackagePolicy(0) & 1) != 0) {
                List unsuppressPackageList = SuppressionService.this.a.getUnsuppressPackageList(str);
                if (unsuppressPackageList != null) {
                    while (true) {
                        int i2 = i;
                        if (i2 < unsuppressPackageList.size()) {
                            try {
                                AppGlobals.getPackageManager().setPackageStoppedState((String) unsuppressPackageList.get(i2), false, UserHandle.myUserId());
                            } catch (RemoteException e) {
                                Slog.w("SuppressionService", "RemoteException: " + e);
                            } catch (IllegalArgumentException e2) {
                                Slog.w("SuppressionService", "Failed trying to unstop package " + ((String) unsuppressPackageList.get(i2)) + ": " + e2);
                            }
                            i = i2 + 1;
                        } else {
                            return;
                        }
                    }
                }
            }
        }

        public int doingSuppress(String str, int i, int i2, int i3, int i4, int i5, ArraySet<String> arraySet, ArrayMap<String, ProcessStateHolder> arrayMap) {
            if (str != null) {
                int i6;
                if (arraySet != null && arraySet.contains(str)) {
                    i6 = 1;
                } else {
                    i6 = 0;
                }
                if (i6 == 0 && UserHandle.getAppId(i4) != i2) {
                    return 1;
                }
                if (i != -1 && i != 0 && i3 != i) {
                    return 1;
                }
                if (!arrayMap.containsKey(str) && i6 == 0) {
                    return 1;
                }
            } else if (i != -1 && i != 0 && i3 != i) {
                return 1;
            } else {
                if (i2 >= 0 && UserHandle.getAppId(i4) != i2) {
                    return 1;
                }
            }
            return i5 > 2 ? 0 : 2;
        }

        public boolean isAllPackagesInList(ArrayMap<String, ProcessStateHolder> arrayMap, List<String> list) {
            for (int i = 0; i < arrayMap.size(); i++) {
                boolean z;
                for (int i2 = 0; i2 < list.size(); i2++) {
                    if (((String) arrayMap.keyAt(i)).equals(list.get(i2))) {
                        z = true;
                        break;
                    }
                }
                z = false;
                if (!z) {
                    return false;
                }
            }
            return true;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.suppression.service.SuppressionService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.suppression.service.SuppressionService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.suppression.service.SuppressionService.<clinit>():void");
    }

    public SuppressionService(Context context) {
        super(context);
        this.mAm = null;
        this.mContext = null;
        this.mSuppressManager = null;
        this.a = null;
        this.mContext = context;
    }

    public void onStart() {
        publishLocalService(SuppressionInternal.class, new b());
    }

    public void setActivityManager(ActivityManagerService activityManagerService) {
        this.mAm = activityManagerService;
        if (this.mAm != null) {
            if (this.mSuppressManager == null) {
                this.mSuppressManager = this.mAm.getSuppressManager();
            }
            if (this.mSuppressManager != null) {
                b = new a(this.mSuppressManager.getKillThread().getLooper());
            }
        }
    }

    private void suppressPackages(List<String> list, int i, String str) {
        int i2 = 0;
        if (this.mAm == null) {
            Slog.e("SuppressionService", "[process suppression] mAm = null");
        } else if (this.mSuppressManager == null) {
            Slog.e("SuppressionService", "[process suppression] mSuppressManager = null");
        } else if (list != null) {
            int myUserId = UserHandle.myUserId();
            if (this.mSuppressManager.checkCallingPermission("android.permission.FORCE_STOP_PACKAGES") == 0) {
                if (this.a == null) {
                    this.a = SuppressionAction.getInstance(this.mContext);
                }
                int suppressPackagePolicy = this.a.getSuppressPackagePolicy(i);
                while (true) {
                    int i3 = i2;
                    if (i3 < list.size()) {
                        a((String) list.get(i3), list, suppressPackagePolicy, myUserId, str);
                        i2 = i3 + 1;
                    } else {
                        return;
                    }
                }
            }
            String str2 = "Permission Denial: suppressPackages() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.FORCE_STOP_PACKAGES";
            Slog.w("SuppressionService", str2);
            throw new SecurityException(str2);
        } else {
            Slog.e("SuppressionService", "[process suppression] packageList = null");
        }
    }

    private void a(String str, List<String> list, int i, int i2, String str2) {
        int handleIncomingUser = this.mSuppressManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), i2, true, 2, "doSuppressPackage", null);
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            IPackageManager packageManager = AppGlobals.getPackageManager();
            synchronized (this.mAm) {
                int[] iArr;
                if (handleIncomingUser != -1) {
                    iArr = new int[1];
                    iArr[0] = handleIncomingUser;
                } else {
                    iArr = this.mSuppressManager.getUsers();
                }
                for (int i3 : iArr) {
                    int i4 = -1;
                    try {
                        i4 = packageManager.getPackageUid(str, 268435456, i3);
                    } catch (RemoteException e) {
                        Slog.w("SuppressionService", "RemoteException: " + e);
                    }
                    if (i4 == -1) {
                        Slog.w("SuppressionService", "Invalid packageName: " + str);
                    } else if (this.mSuppressManager.isUserRunningLocked(i3, 0)) {
                        a(str, list, i, i4, i3, str2, packageManager);
                    }
                }
            }
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    private void a(String str, List<String> list, int i, int i2, int i3, String str2, IPackageManager iPackageManager) {
        int appId;
        int appId2 = UserHandle.getAppId(i2);
        int userId = UserHandle.getUserId(i2);
        if (appId2 < 0 && str != null) {
            try {
                appId = UserHandle.getAppId(AppGlobals.getPackageManager().getPackageUid(str, 268435456, 0));
            } catch (RemoteException e) {
                Slog.w("SuppressionService", "RemoteException: " + e);
                appId = appId2;
            }
        } else {
            appId = appId2;
        }
        ArrayList arrayList = new ArrayList();
        if (this.mSuppressManager.isSuppressedProcessesLocked(str, arrayList, appId, userId)) {
            boolean z;
            SuppressManager suppressManager = this.mSuppressManager;
            if (str != null) {
                z = false;
            } else {
                z = true;
            }
            suppressManager.resetProcessCrashTimeLocked(z, appId, userId);
            if ((i & 2) != 0) {
                try {
                    iPackageManager.setPackageStoppedState(str, true, i3);
                } catch (RemoteException e2) {
                    Slog.w("SuppressionService", "RemoteException: " + e2);
                } catch (IllegalArgumentException e3) {
                    Slog.w("SuppressionService", "Failed trying to unstop package " + str + ": " + e3);
                }
                PackageStoppedStatusChanged createInstance = PackageStoppedStatusChanged.createInstance();
                Object[] objArr = new Object[3];
                objArr[0] = str;
                objArr[1] = Integer.valueOf(1);
                objArr[2] = str2;
                createInstance.set(objArr);
                this.mSuppressManager.triggerEventHook(Event.AM_PackageStoppedStatusChanged, createInstance);
            }
            if ((i & 1) != 0) {
                this.mSuppressManager.killSuppressedProcessesLocked(str, arrayList, list, userId);
            }
            if ((i & 16) != 0) {
                this.mSuppressManager.bringDownDisabledPackageServicesLocked(str, null, userId, false, true, true);
            }
            if ((i & 128) != 0) {
                this.mSuppressManager.removeDyingProviderLocked(str, userId);
            }
            if ((i & 256) != 0) {
                this.mSuppressManager.removeUriPermissionsForPackageLocked(str, userId, false);
            }
            if ((i & 64) != 0) {
                BroadcastQueue[] broadcastQueues = this.mSuppressManager.getBroadcastQueues();
                for (int length = broadcastQueues.length - 1; length >= 0; length--) {
                    this.mSuppressManager.cleanupDisabledPackageReceiversLocked(broadcastQueues[length], str, null, -1, true);
                }
            }
            if (this.mSuppressManager.getBooted()) {
                this.mSuppressManager.resumeFocusedStackTopActivityLocked();
                this.mSuppressManager.scheduleIdleLocked();
            }
            if ((i & 4) != 0) {
                Intent intent = new Intent("android.intent.action.PACKAGE_RESTARTED", Uri.fromParts("package", str, null));
                if (!this.mSuppressManager.getProcessesReady()) {
                    intent.addFlags(1342177280);
                }
                intent.putExtra("android.intent.extra.UID", i2);
                intent.putExtra("android.intent.extra.user_handle", UserHandle.getUserId(i2));
                this.mSuppressManager.broadcastIntentLocked(intent, null, null, 0, null, null, null, -1, null, false, false, Process.myPid(), 1000, UserHandle.getUserId(i2));
            }
            if (b == null) {
                Slog.w("SuppressionService", "[process suppression] sCancelJobHandler = null!");
            } else {
                b.sendMessage(b.obtainMessage(5000, Integer.valueOf(i2)));
            }
        }
    }
}
