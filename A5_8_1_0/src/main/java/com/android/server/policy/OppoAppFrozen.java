package com.android.server.policy;

import android.app.ActivityManagerNative;
import android.app.OppoActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.am.ActivityManagerService;
import java.util.Arrays;
import java.util.List;

class OppoAppFrozen {
    private static final List<String> FILTER_LIST = Arrays.asList(new String[]{ActivityManagerService.OPPO_LAUNCHER});
    private static final String ID_APP_FROZEN = "app_frozen_detect";
    private static final String TAG = "OppoAppFrozen";
    private static boolean mHomeMsgSent = false;
    private final String ACTION_OPPO_APP_FROZEN_DCS_UPLOADE = "android.intent.action.OPPO_APP_FROZEN_DCS_UPLOADE";
    private final boolean ALLOW_UPLOAD_DCS = true;
    private final long APP_FROZEN_DELAY_TIME = 3000;
    private boolean OPPODEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private final int OPPO_APP_FROZEN_TIMEOUT = 2;
    private final int OPPO_HOME_DISPATCH_TIMEOUT = 1;
    private Context mContext;
    private WorkerHandler mHandlerOppo;
    private Intent mIntentDcs = null;
    final Object mLock = new Object();
    private OppoPhoneWindowManager mPwm;

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Missing block: B:15:0x0029, code:
            android.util.Log.w(com.android.server.policy.OppoAppFrozen.TAG, "homeDispatchTimeOutHandle: forceStopPackage fgPkg=" + r1);
     */
        /* JADX WARNING: Missing block: B:17:?, code:
            android.app.ActivityManagerNative.getDefault().forceStopPackage(r1, android.os.UserHandle.myUserId());
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            String fgPkg;
            int uid;
            StringBuilder sb;
            if (msg.what == 1) {
                fgPkg = OppoAppFrozen.this.getForegroundPackage();
                synchronized (OppoAppFrozen.this.mLock) {
                    if (OppoAppFrozen.mHomeMsgSent && fgPkg != null) {
                        if (!OppoAppFrozen.FILTER_LIST.contains(fgPkg)) {
                        }
                    }
                    OppoAppFrozen.mHomeMsgSent = false;
                    return;
                }
            } else if (msg.what == 2) {
                String pkgName = msg.getData().getString("PKGNAME");
                if (!"0".equals(SystemProperties.get("sys.app_freeze_timeout", "0"))) {
                    Log.w(OppoAppFrozen.TAG, "OPPO_APP_FROZEN_TIMEOUT: forceStopPackage pkgName=" + pkgName);
                    try {
                        ActivityManagerNative.getDefault().forceStopPackage(pkgName, UserHandle.myUserId());
                    } catch (RemoteException e) {
                    }
                    SystemProperties.set("sys.app_freeze_timeout", "0");
                    uid = OppoAppFrozen.this.getUidForPkgName(pkgName);
                    sb = new StringBuilder();
                    sb.append("frozenPkg:").append(pkgName).append("    uid:").append(uid);
                    OppoAppFrozen.this.uploadDcsKvEvent(OppoAppFrozen.ID_APP_FROZEN, sb.toString());
                } else {
                    Log.w(OppoAppFrozen.TAG, "OPPO_APP_FROZEN_TIMEOUT: isAppFrozen is false!");
                    return;
                }
            }
            OppoAppFrozen.this.mPwm.launchHomeFromHotKey();
            uid = OppoAppFrozen.this.getUidForPkgName(fgPkg);
            sb = new StringBuilder();
            sb.append("fgPkg:").append(fgPkg).append("    uid:").append(uid);
            OppoAppFrozen.this.uploadDcsKvEvent(OppoAppFrozen.ID_APP_FROZEN, sb.toString());
            synchronized (OppoAppFrozen.this.mLock) {
                OppoAppFrozen.mHomeMsgSent = false;
            }
        }
    }

    public OppoAppFrozen(Context context, OppoPhoneWindowManager pwm) {
        this.mContext = context;
        this.mPwm = pwm;
        HandlerThread ht = new HandlerThread(TAG);
        ht.start();
        this.mHandlerOppo = new WorkerHandler(ht.getLooper());
    }

    public void sendHomeDispatchTimeoutMsg() {
        synchronized (this.mLock) {
            if (!mHomeMsgSent) {
                mHomeMsgSent = true;
                this.mHandlerOppo.sendMessageDelayed(this.mHandlerOppo.obtainMessage(1), 3000);
                if (this.OPPODEBUG) {
                    Log.i(TAG, "sendHomeDispatchTimeoutMsg");
                }
            }
        }
    }

    public void clearHomeDispatchTimeoutMsg() {
        synchronized (this.mLock) {
            mHomeMsgSent = false;
            this.mHandlerOppo.removeMessages(1);
            if (this.OPPODEBUG) {
                Log.i(TAG, "clearHomeDispatchTimeoutMsg");
            }
        }
    }

    public void appFrozenHandle() {
        String pkgName = SystemProperties.get("sys.app_freeze_timeout", "0");
        if (!FILTER_LIST.contains(pkgName)) {
            if (!"0".equals(pkgName)) {
                if (pkgName != null) {
                    Message msg = this.mHandlerOppo.obtainMessage(2);
                    Bundle bundle = new Bundle();
                    bundle.putString("PKGNAME", pkgName);
                    msg.setData(bundle);
                    this.mHandlerOppo.sendMessage(msg);
                    Log.i(TAG, "appFrozenHandle: pkgName= " + pkgName);
                } else {
                    Log.i(TAG, "appFrozenHandle: pkgName is null!!! do nothing!");
                }
            }
        }
    }

    private String getForegroundPackage() {
        ComponentName cn;
        try {
            cn = new OppoActivityManager().getTopActivityComponentName();
        } catch (Exception e) {
            Log.w(TAG, "getTopActivityComponentName exception");
            cn = null;
        }
        if (cn != null) {
            return cn.getPackageName();
        }
        return null;
    }

    private int getUidForPkgName(String pkgName) {
        int packageUid = -1;
        try {
            return this.mContext.getPackageManager().getPackageUid(pkgName, 0);
        } catch (Exception e) {
            Log.w(TAG, "getUidForPkgName: Exception");
            return packageUid;
        }
    }

    private void uploadDcsKvEvent(String id, String act) {
        this.mIntentDcs = new Intent("android.intent.action.OPPO_APP_FROZEN_DCS_UPLOADE");
        this.mIntentDcs.putExtra("eventId", id);
        this.mIntentDcs.putExtra("act", act);
        this.mIntentDcs.addFlags(67108864);
        this.mContext.sendBroadcastAsUser(this.mIntentDcs, UserHandle.ALL);
    }
}
