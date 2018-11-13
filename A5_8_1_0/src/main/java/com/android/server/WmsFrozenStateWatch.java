package com.android.server;

import android.os.SystemProperties;
import android.util.Log;
import com.android.server.face.FaceDaemonWrapper;
import com.android.server.wm.WindowManagerService;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WmsFrozenStateWatch extends StateWatch {
    static final String TAG = "WmsFrozenStateWatch";
    boolean mAgingTestVersion = false;
    WindowManagerService mWms;

    public void setWmsInstance(WindowManagerService wms) {
        this.mWms = wms;
        this.mAgingTestVersion = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"));
    }

    boolean isCheckEnable() {
        return (this.mAgingTestVersion || this.mWms == null) ? false : true;
    }

    boolean isStateOk() {
        return !this.mWms.GetDisplayFrozen();
    }

    int getCheckInterval() {
        return 100;
    }

    int getCheckCount() {
        return 600;
    }

    void dealAction() {
        Log.i(TAG, "WmsFrozenValueWatch dealAction");
        CheckBlockedException.getInstance().WriteLastExceptionMsgToProc("Wms,Frozen" + new SimpleDateFormat("yyyy_MM_dd HH:mm:ss ").format(new Date(System.currentTimeMillis())));
        Log.i(TAG, "WmsFrozenValueWatch kill systemserver");
        CheckBlockedException.getInstance();
        CheckBlockedException.DeathHealerDumpStack("DeathHealer Wms Frozen long time");
        CheckBlockedException.getInstance();
        CheckBlockedException.rebootSystemServer();
    }

    boolean RemedyAction() {
        Log.i(TAG, "WmsFrozenValueWatch RemedyAction");
        Thread RemedyThread = new Thread() {
            public void run() {
                WmsFrozenStateWatch.this.mWms.killNotDrawnAppsWhenFrozen();
            }
        };
        RemedyThread.start();
        try {
            RemedyThread.join(FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK);
        } catch (InterruptedException e) {
            Log.w(TAG, "WmsFrozenStateWatch,RemedyAction timeout!");
        }
        return true;
    }
}
