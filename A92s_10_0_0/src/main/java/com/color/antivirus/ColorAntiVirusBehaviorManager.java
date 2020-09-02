package com.color.antivirus;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.ServiceManager;
import com.color.antivirus.IColorAntiViruStateChangeCallback;
import com.color.antivirus.IColorAntiVirusManagerService;
import com.color.antivirus.qihoo.BehaviorBidSender;
import com.color.antivirus.qihoo.BinderStubClient;
import com.color.antivirus.tencent.TRPEngManager;

public class ColorAntiVirusBehaviorManager implements IColorAntiVirusBehaviorManager {
    private static final String TAG = "ColorAntiVirusBehaviorManager";
    /* access modifiers changed from: private */
    public static boolean isAIAntiVirusOn = false;
    private static boolean isTRPEngInitialized = false;
    private static final IColorAntiViruStateChangeCallback mSateChangeCallback = new IColorAntiViruStateChangeCallback.Stub() {
        /* class com.color.antivirus.ColorAntiVirusBehaviorManager.AnonymousClass1 */

        public void onAntiVirusStateChange(boolean state) {
            boolean unused = ColorAntiVirusBehaviorManager.isAIAntiVirusOn = state;
        }
    };
    private static volatile ColorAntiVirusBehaviorManager sInstance;

    private static void checkRegionAndFeature() {
        IColorAntiVirusManagerService antiVirusManager = IColorAntiVirusManagerService.Stub.asInterface(ServiceManager.getService("anti_virus_manager"));
        if (antiVirusManager != null) {
            try {
                antiVirusManager.registerStateChangeCallback(mSateChangeCallback);
            } catch (Exception e) {
                AntivirusLog.e(TAG, "registerStateChangeCallback failed!", e);
            }
        } else {
            AntivirusLog.e(TAG, "failed to get ColorAntiviruManagerService");
        }
    }

    private static boolean uidNotInMonitorRange(int uid) {
        if (uid < 10000 || uid > 19999) {
            return true;
        }
        return false;
    }

    public static ColorAntiVirusBehaviorManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorAntiVirusBehaviorManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorAntiVirusBehaviorManager();
                }
            }
        }
        return sInstance;
    }

    public void checkAndSendReceiverInvocation(Intent intent, boolean dy) {
        if (isAIAntiVirusOn) {
            BinderStubClient.checkAndSendReceiverInvocation(intent, dy);
        }
    }

    public void senderInit() {
        if (!uidNotInMonitorRange(Process.myUid())) {
            checkRegionAndFeature();
            if (isAIAntiVirusOn) {
                BehaviorBidSender.getInstance().init();
            }
        }
    }

    public IBinder getOrCreateFakeBinder(IBinder origBinder, String svcName) {
        if (!isAIAntiVirusOn || uidNotInMonitorRange(Process.myUid())) {
            return origBinder;
        }
        return BinderStubClient.getOrCreateFakeBinder(origBinder, svcName);
    }

    public boolean isInstance(IBinder binder) {
        if (!isAIAntiVirusOn || uidNotInMonitorRange(Process.myUid())) {
            return false;
        }
        return BinderStubClient.class.isInstance(binder);
    }

    public boolean checkNeedReplace(String name) {
        if (!isAIAntiVirusOn || uidNotInMonitorRange(Process.myUid())) {
            return false;
        }
        return BinderStubClient.checkNeedReplace(name);
    }

    private void initTRPEngManager() {
        if (!isTRPEngInitialized) {
            isTRPEngInitialized = true;
            checkRegionAndFeature();
            TRPEngManager.getInstance();
        }
    }

    public void setAction(int actionId, int uid) {
        if (!uidNotInMonitorRange(uid)) {
            initTRPEngManager();
            if (isAIAntiVirusOn) {
                TRPEngManager.setAction(actionId, uid);
            }
        }
    }

    public void broadcastIntent(Intent intent, int uid) {
        int trpid;
        if (!uidNotInMonitorRange(uid)) {
            initTRPEngManager();
            if (isAIAntiVirusOn) {
                TRPEngManager.setAction(81, Binder.getCallingUid());
                if (intent != null) {
                    String action = intent.getAction();
                    if (action == null) {
                        trpid = 1107;
                    } else if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                        trpid = 1100;
                    } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                        trpid = 1101;
                    } else if (action.equals("android.intent.action.TIME_TICK")) {
                        trpid = 1102;
                    } else if (action.equals("android.intent.action.TIME_SET")) {
                        trpid = 1103;
                    } else if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                        trpid = 1104;
                    } else if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                        trpid = 1105;
                    } else if (action.equals("com.android.launcher.action.INSTALL_SHORTCUT")) {
                        trpid = 1106;
                    } else {
                        trpid = 1107;
                    }
                    TRPEngManager.setAction(trpid, uid);
                }
            }
        }
    }

    public void setForegroundApp(String packageName) {
        initTRPEngManager();
        if (isAIAntiVirusOn) {
            TRPEngManager.setForegroundApp(packageName);
        }
    }

    public void setAction_startActivityMayWait(int uid, String action, ComponentName comp, String pkg, Uri dat) {
        if (!uidNotInMonitorRange(uid)) {
            initTRPEngManager();
            if (isAIAntiVirusOn) {
                TRPEngManager.setAction_startActivityMayWait(80, uid, action, comp, pkg, dat);
            }
        }
    }

    public void setAction_AMS_getContentProvider(int uid, String name) {
        if (!uidNotInMonitorRange(uid)) {
            initTRPEngManager();
            if (isAIAntiVirusOn) {
                TRPEngManager.setAction_AMS_getContentProvider(uid, name);
            }
        }
    }

    public void setAction_SensorManager_registerListenerImp(int uid, int sensortype) {
        if (!uidNotInMonitorRange(uid)) {
            initTRPEngManager();
            if (isAIAntiVirusOn) {
                TRPEngManager.setAction_SensorManager_registerListenerImp(uid, sensortype);
            }
        }
    }

    public void setAction_SetContentProviderAction(Uri uri, int action, int uid) {
        if (!uidNotInMonitorRange(uid)) {
            initTRPEngManager();
            if (isAIAntiVirusOn) {
                TRPEngManager.setAction_SetContentProviderAction(uri, action, uid);
            }
        }
    }

    public void setComponentEnabledSetting(int newState, int uid) {
        if (!uidNotInMonitorRange(uid)) {
            initTRPEngManager();
            if (isAIAntiVirusOn) {
                setAction(38, uid);
                int trpid = 0;
                if (newState == 0) {
                    trpid = 1301;
                } else if (newState == 1) {
                    trpid = 1302;
                } else if (newState == 2) {
                    trpid = 1303;
                } else if (newState == 3) {
                    trpid = 1304;
                }
                if (trpid != 0) {
                    setAction(trpid, uid);
                }
            }
        }
    }

    public void setAction_addWindow(int actionId, int uid, int type) {
        if (!uidNotInMonitorRange(uid)) {
            initTRPEngManager();
            if (isAIAntiVirusOn) {
                TRPEngManager.setAction_addWindow(actionId, uid, type);
            }
        }
    }
}
