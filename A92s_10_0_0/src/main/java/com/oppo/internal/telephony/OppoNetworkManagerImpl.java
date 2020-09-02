package com.oppo.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.AbstractServiceStateTracker;
import com.android.internal.telephony.IOppoNetworkManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.recovery.OppoFastRecovery;
import com.oppo.internal.telephony.utils.OppoServiceStateTrackerUtil;

public class OppoNetworkManagerImpl implements IOppoNetworkManager {
    private static final boolean DBG = true;
    private static final String TAG = "OppoNetworkManagerImpl";
    private static OppoNetworkManagerImpl sInstance = null;
    int PREFERRED_NETWORK_MODE_NON_DDS;

    public OppoNetworkManagerImpl() {
        this.PREFERRED_NETWORK_MODE_NON_DDS = "1".equals(SystemProperties.get("ro.boot.opt_c2k_support")) ? 7 : 0;
    }

    public static OppoNetworkManagerImpl getInstance() {
        OppoNetworkManagerImpl oppoNetworkManagerImpl;
        OppoNetworkManagerImpl oppoNetworkManagerImpl2 = sInstance;
        if (oppoNetworkManagerImpl2 != null) {
            return oppoNetworkManagerImpl2;
        }
        synchronized (OppoNetworkManagerImpl.class) {
            if (sInstance == null) {
                sInstance = new OppoNetworkManagerImpl();
            }
            oppoNetworkManagerImpl = sInstance;
        }
        return oppoNetworkManagerImpl;
    }

    public void oppoResetOosDelayState(Phone phone) {
        ServiceStateTracker SST;
        ServiceStateTracker SST2 = phone.getServiceStateTracker();
        if (SST2 != null) {
            ((AbstractServiceStateTracker) OemTelephonyUtils.typeCasting(AbstractServiceStateTracker.class, SST2)).oppoResetOosDelayState();
        }
        Phone oPhone = PhoneFactory.getPhone(phone.getPhoneId() == 0 ? 1 : 0);
        if (oPhone != null && (SST = oPhone.getServiceStateTracker()) != null) {
            ((AbstractServiceStateTracker) OemTelephonyUtils.typeCasting(AbstractServiceStateTracker.class, SST)).oppoResetOosDelayState();
        }
    }

    public boolean isMvnoPlmn(String plmn) {
        return OppoServiceStateTrackerUtil.isMvnoPlmn(plmn);
    }

    public int calculatePreferredNetworkTypeWithPhoneId(Context context, int phoneSubId, int phoneid) {
        int defaultPrefNetworkType = RILConstants.PREFERRED_NETWORK_MODE;
        if (phoneid != Settings.Global.getInt(context.getContentResolver(), "oppo_multi_sim_network_primary_slot", 0) && TelephonyManager.getDefault().hasIccCard(0) && TelephonyManager.getDefault().hasIccCard(1)) {
            defaultPrefNetworkType = this.PREFERRED_NETWORK_MODE_NON_DDS;
        }
        Rlog.d(TAG, "calculatePreferredNetworkTypeWithPhoneId: defaultPrefNetworkType = " + defaultPrefNetworkType);
        ContentResolver contentResolver = context.getContentResolver();
        int networkType = Settings.Global.getInt(contentResolver, "preferred_network_mode" + phoneSubId, defaultPrefNetworkType);
        Rlog.d(TAG, "calculatePreferredNetworkTypeWithPhoneId: SubId = " + phoneSubId + " networkType = " + networkType);
        boolean mIsSet5gRatHere = OppoFastRecovery.make(context).getIsSet5gRatHere();
        if (networkType <= 22 || !mIsSet5gRatHere) {
            return networkType;
        }
        int networkType2 = changeFastRecovRAT(networkType);
        Rlog.d(TAG, "mIsSet5gRatHere = " + mIsSet5gRatHere + ", changeFastRecovRAT = " + networkType2);
        return networkType2;
    }

    public void oppoProcessUnsolOemKeyLogErrMsg(Context context, int phoneId, Object ret) {
        OppoRIL oppoRIL = OppoTelephonyController.getInstance(context).getOppoRIL(phoneId);
        if (oppoRIL != null) {
            oppoRIL.oppoProcessUnsolOemKeyLogErrMsg(ret);
        }
    }

    public void oppoCountUnsolMsg(int response) {
        OppoNetworkPowerState.countUnsolMsg(response);
    }

    public void oppoCountGetCellInfo(int getCellUid, int getCellPid, String getCellPackage) {
        OppoNetworkPowerState.countGetCellInfo(getCellUid, getCellPid, getCellPackage);
    }

    public int changeFastRecovRAT(int networkType) {
        switch (networkType) {
            case OppoRIL.SYS_OEM_NW_DIAG_CAUSE_MO_CALL_DROP:
            case OppoRIL.SYS_OEM_NW_DIAG_CAUSE_MT_CALL_DROP:
                return 11;
            case 25:
                return 8;
            case 26:
                return 9;
            case OppoRIL.SYS_OEM_NW_DIAG_CAUSE_DATA_USER_DATA_ENABLE_NUM:
                return 10;
            case 28:
                return 12;
            case OppoRIL.SYS_OEM_NW_DIAG_CAUSE_DATA_PDN_ACTIVATION_DURATION:
                return 15;
            case OppoRIL.SYS_OEM_NW_DIAG_CAUSE_DATA_DISCONNECT_CALL_ERROR:
                return 17;
            case OppoRIL.SYS_OEM_NW_DIAG_CAUSE_APN_REASON_DATA_CALL_FAIL:
                return 19;
            case OppoRIL.SYS_OEM_NW_DIAG_CAUSE_NOT_APN_REASON_DATA_CALL_FAIL:
                return 20;
            case OppoRIL.SYS_OEM_NW_DIAG_CAUSE_SCREEN_ON_DURATION:
                return 22;
            default:
                return networkType;
        }
    }
}
