package com.oppo.internal.telephony;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.telephony.IOemLinkLatencyManager;
import com.android.internal.telephony.OemLinkLatencyInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.mediatek.powerhalmgr.PowerHalMgr;
import com.mediatek.powerhalmgr.PowerHalMgrFactory;

public class OppoLinkLatencyManagerService {
    private static final int CMD_SET_NETD_BOOST_UID = 101;
    public static final String LINK_LATENCY_SERVICE = "oemlinklatency";
    public static final int OPTIMIZE_DEFAULT = 0;
    public static final int OPTIMIZE_PROTOCOL = 2;
    public static final int OPTIMIZE_TRM = 1;
    private static final int SCNENABLE_ALWAYS_ENABLE = 0;
    private static final String TAG = "OemLinkLatencyManagerService";
    private static OppoLinkLatencyManagerService mInstance;
    private IBinder mBinder = new IOemLinkLatencyManager.Stub() {
        /* class com.oppo.internal.telephony.OppoLinkLatencyManagerService.AnonymousClass1 */

        public long prioritizeDefaultDataSubscription(boolean isEnabled) {
            Log.d(OppoLinkLatencyManagerService.TAG, "prioritizeDefaultDataSubscription...");
            return 0;
        }

        public void setLevel(long rat, long uplink, long downlink) {
            Log.d(OppoLinkLatencyManagerService.TAG, "setLevel...");
        }

        public OemLinkLatencyInfo getCurrentLevel() {
            Log.d(OppoLinkLatencyManagerService.TAG, "getCurrentLevel..uplink:" + OppoLinkLatencyManagerService.this.mCurrentStatus.getEffectiveUplink() + ",downlink:" + OppoLinkLatencyManagerService.this.mCurrentStatus.getEffectiveDownlink());
            return OppoLinkLatencyManagerService.this.mCurrentStatus;
        }

        public boolean gameOptimizeSetLoad(int id, String pkgName) {
            Log.d(OppoLinkLatencyManagerService.TAG, "gameOptimizeSetLoad.id:" + id + ", pkgName:" + pkgName);
            boolean protocolOpt = false;
            if ((id & 1) > 0) {
            }
            if ((id & 2) > 0) {
                protocolOpt = true;
            }
            if (!protocolOpt) {
                return true;
            }
            try {
                OppoLinkLatencyManagerService.this.setModemTAG(pkgName);
                Log.d(OppoLinkLatencyManagerService.TAG, "setModemTAG ret:true");
                return true;
            } catch (Exception e) {
                Log.d(OppoLinkLatencyManagerService.TAG, "OemLinkLatencyInfo Exception: " + e);
                return false;
            }
        }

        public boolean gameOptimizeExit() {
            Log.d(OppoLinkLatencyManagerService.TAG, "gameOptimizeExit...");
            try {
                if (OppoLinkLatencyManagerService.this.mPowerHalService == null || OppoLinkLatencyManagerService.this.mPowerHandle == -1) {
                    return true;
                }
                OppoLinkLatencyManagerService.this.mPowerHalService.scnDisable(OppoLinkLatencyManagerService.this.mPowerHandle);
                OppoLinkLatencyManagerService.this.mPowerHalService.scnUnreg(OppoLinkLatencyManagerService.this.mPowerHandle);
                OppoLinkLatencyManagerService.this.mPowerHandle = -1;
                Log.d(OppoLinkLatencyManagerService.TAG, "gameOptimizeExit...mPowerHalService");
                return true;
            } catch (Exception e) {
                Log.d(OppoLinkLatencyManagerService.TAG, "gameOptimizeExit Exception: " + e);
                return false;
            }
        }
    };
    private Context mContext;
    private OemLinkLatencyInfo mCurrentStatus = new OemLinkLatencyInfo();
    private Phone mPhone = null;
    private PowerHalMgr mPowerHalService = null;
    private int mPowerHandle = -1;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setModemTAG(String pkgName) {
        if (this.mPowerHalService == null) {
            this.mPowerHalService = PowerHalMgrFactory.getInstance().makePowerHalMgr();
        }
        PowerHalMgr powerHalMgr = this.mPowerHalService;
        if (powerHalMgr != null && -1 == this.mPowerHandle) {
            this.mPowerHandle = powerHalMgr.scnReg();
            Log.e(TAG, "mPowerHandle:" + this.mPowerHandle);
        }
        if (this.mPowerHalService != null && this.mPowerHandle != -1) {
            this.mPowerHalService.scnConfig(this.mPowerHandle, 101, getPackageUid(this.mContext, pkgName), 0, 0, 0);
            this.mPowerHalService.scnEnable(this.mPowerHandle, 0);
        }
    }

    private int getPackageUid(Context context, String packageName) {
        try {
            int uid = context.getPackageManager().getPackageUid(packageName, 0);
            Log.e(TAG, "getPackageUid:" + uid);
            return uid;
        } catch (Exception e) {
            Log.e(TAG, "getPackageUid exception:" + e);
            return -1;
        }
    }

    public static void make(Context context) {
        if (mInstance == null) {
            mInstance = new OppoLinkLatencyManagerService(context);
        }
    }

    /* JADX WARN: Type inference failed for: r0v3, types: [com.oppo.internal.telephony.OppoLinkLatencyManagerService$1, android.os.IBinder] */
    private OppoLinkLatencyManagerService(Context context) {
        this.mContext = context;
        this.mPhone = PhoneFactory.getDefaultPhone();
        addService();
    }

    private void addService() {
        try {
            Log.d(TAG, "Start Service...");
            ServiceManager.addService(LINK_LATENCY_SERVICE, this.mBinder);
        } catch (Throwable e) {
            Log.e(TAG, "Start Service failed", e);
        }
    }

    private boolean isValueValid(long values) {
        return false;
    }
}
