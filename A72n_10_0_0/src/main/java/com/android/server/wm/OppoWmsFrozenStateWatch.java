package com.android.server.wm;

import android.os.SystemProperties;
import android.util.Log;
import com.android.server.OppoCheckBlockedException;
import com.android.server.OppoStateWatch;
import com.android.server.SystemService;
import com.android.server.theia.NoFocusWindow;
import com.color.util.ColorTypeCastingHelper;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OppoWmsFrozenStateWatch extends OppoStateWatch {
    static final String TAG = "OppoWmsFrozenStateWatch";
    boolean mAgingTestVersion = false;
    WindowManagerService mWms;

    public void setWmsInstance(WindowManagerService wms) {
        this.mWms = wms;
        this.mAgingTestVersion = NoFocusWindow.HUNG_CONFIG_ENABLE.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"));
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.OppoStateWatch
    public boolean isCheckEnable() {
        return !this.mAgingTestVersion && this.mWms != null;
    }

    @Override // com.android.server.OppoStateWatch
    public boolean isStateOk() {
        return !this.mWms.mDisplayFrozen;
    }

    @Override // com.android.server.OppoStateWatch
    public int getCheckInterval() {
        return 100;
    }

    @Override // com.android.server.OppoStateWatch
    public int getCheckCount() {
        return SystemService.PHASE_THIRD_PARTY_APPS_CAN_START;
    }

    @Override // com.android.server.OppoStateWatch
    public void dealAction() {
        Log.i(TAG, "WmsFrozenValueWatch dealAction");
        String strNow = new SimpleDateFormat("yyyy_MM_dd HH:mm:ss ").format(new Date(System.currentTimeMillis()));
        OppoCheckBlockedException instance = OppoCheckBlockedException.getInstance();
        instance.WriteLastExceptionMsgToProc("Wms,Frozen" + strNow);
        Log.i(TAG, "WmsFrozenValueWatch kill systemserver");
        OppoCheckBlockedException.getInstance();
        OppoCheckBlockedException.DeathHealerDumpStack("DeathHealer Wms Frozen long time");
        OppoCheckBlockedException.getInstance();
        OppoCheckBlockedException.rebootSystemServer();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.OppoStateWatch
    public boolean remedyAction() {
        Log.i(TAG, "WmsFrozenValueWatch remedyAction");
        Thread RemedyThread = new Thread() {
            /* class com.android.server.wm.OppoWmsFrozenStateWatch.AnonymousClass1 */

            public void run() {
                OppoBaseWindowManagerService baseWms = OppoWmsFrozenStateWatch.typeCasting(OppoWmsFrozenStateWatch.this.mWms);
                if (baseWms != null) {
                    baseWms.killNotDrawnAppsWhenFrozen();
                }
            }
        };
        RemedyThread.start();
        try {
            RemedyThread.join(5000);
            return true;
        } catch (InterruptedException e) {
            Log.w(TAG, "OppoWmsFrozenStateWatch,remedyAction timeout!");
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static OppoBaseWindowManagerService typeCasting(WindowManagerService wms) {
        if (wms != null) {
            return (OppoBaseWindowManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseWindowManagerService.class, wms);
        }
        return null;
    }
}
