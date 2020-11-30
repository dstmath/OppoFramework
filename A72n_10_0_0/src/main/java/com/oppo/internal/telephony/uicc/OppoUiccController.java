package com.oppo.internal.telephony.uicc;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.uicc.AbstractUiccController;
import com.android.internal.telephony.uicc.IOppoUiccController;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseUtils;
import com.oppo.internal.telephony.utils.OppoManagerHelper;

public class OppoUiccController extends Handler implements IOppoUiccController {
    public static final String ACTION_HOT_SWAP_STATE_CHANGED = "com.dmyk.android.telephony.action.SIM_STATE_CHANGED";
    private static final String ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED = "org.codeaurora.intent.action.ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED";
    protected static final boolean DBG = true;
    private static final String EXTRA_NEW_PROVISION_STATE = "newProvisionState";
    public static final String EXTRA_SIM_PHONEID = "com.dmyk.android.telephony.extra.SIM_PHONEID";
    public static final String EXTRA_SIM_STATE = "com.dmyk.android.telephony.extra.SIM_STATE";
    private static final String INTENT_VALUE_PRIVISION = "PRIVISION";
    protected static final int LOG_TYPE_SIM_PLUG_IN = 162;
    protected static final int LOG_TYPE_SIM_PLUG_OUT = 163;
    protected static final int LOG_TYPE_SIM_PLUG_RECOVERED = 164;
    public static final String PKG_CMCC_ATUO_REG = "com.coloros.regservice";
    protected static final boolean VDBG = false;
    public static final String WHITE_SIM_CARD_INSERT_PROC = "persist.radio.oppo.white_sim_card_insert";
    public static UiccController mUiccController;
    public final String FEATURE_ENABLE_HOTSWAP = "gsm.enable_hotswap";
    private String LOG_TAG = "OppoUiccController";
    protected boolean bIsNeedRecordLog = false;
    public Context mContext;

    public OppoUiccController(UiccController uiccController) {
        mUiccController = uiccController;
        this.mContext = mUiccController.mContext;
    }

    public void notifyIccIdForSimPlugOut(int slotid) {
        log("notifyIccIdForSimPlugOut plug out sim slotid = " + slotid);
        SystemProperties.set("gsm.ims.type" + slotid, "");
        broadcastIccStateChangedIntent("ABSENT", "PLUGOUT", slotid);
    }

    public void notifyIccIdForSimPlugIn(int slotid) {
        if (slotid < 0 || slotid > 1) {
            log("notifyIccIdForSimPlugIn failed slotid = " + slotid);
            return;
        }
        log("notifyIccIdForSimPlugIn slotid = " + slotid);
        boolean hotswapSimReboot = isHotSwapSimReboot();
        log("hotswapSimReboot:" + hotswapSimReboot);
        if (hotswapSimReboot) {
            broadcastIccStateChangedIntent("NOT_READY", "PLUGIN", slotid);
        }
    }

    private void broadcastIccStateChangedIntent(String value, String reason, int slotid) {
        Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
        intent.putExtra("phoneName", "Phone");
        intent.putExtra(NetworkDiagnoseUtils.INFO_SERVICESTATE, value);
        intent.putExtra("reason", reason);
        intent.putExtra("slot", slotid);
        int phoneId = mUiccController.getPhoneIdFromSlotId(slotid);
        UiccController uiccController = mUiccController;
        if (-1 != phoneId) {
            intent.putExtra("phone", phoneId);
        }
        intent.putExtra("simid", slotid);
        int[] subIds = SubscriptionManager.getSubId(slotid);
        if (subIds != null && subIds.length > 0) {
            intent.putExtra("subscription", (long) subIds[0]);
        }
        log("Broadcasting intent ACTION_SIM_STATE_CHANGED " + value + " reason " + reason + " sim id " + slotid + " phoneid " + phoneId);
        this.mContext.sendBroadcast(intent);
    }

    public void notifyIccIdForTrayPlugIn(int slotid) {
        if (slotid < 0 || slotid > 1) {
            log("notifyIccIdForTrayPlugIn failed slotid = " + slotid);
            return;
        }
        log("notifyIccIdForTrayPlugIn slotid = " + slotid);
        broadcastIccStateChangedIntent("ABSENT", "TRAYPLUGIN", slotid);
    }

    public boolean isHotSwapSimReboot() {
        return SystemProperties.get("gsm.enable_hotswap", "true").equals("false");
    }

    public boolean getSimHotSwapPlugInState() {
        boolean SimPlugInState = false;
        int i = 0;
        while (true) {
            if (i >= TelephonyManager.getDefault().getPhoneCount()) {
                break;
            } else if (((AbstractUiccController) OemTelephonyUtils.typeCasting(AbstractUiccController.class, mUiccController)).isSimPlugIn[i]) {
                SimPlugInState = DBG;
                break;
            } else {
                i++;
            }
        }
        log("getSimHotSwapPlugInState, SimPlugInState:" + SimPlugInState);
        return SimPlugInState;
    }

    public void saveSimPlugState(int index, int type) {
        String issue;
        log("saveSimPlugState(), bIsNeedRecordLog = " + this.bIsNeedRecordLog);
        if (this.bIsNeedRecordLog) {
            String logString = "old cardState is: absent";
            if (!(mUiccController.mUiccSlots == null || mUiccController.mUiccSlots[index] == null)) {
                logString = "old cardState is:" + mUiccController.mUiccSlots[index].getCardState();
            }
            switch (type) {
                case LOG_TYPE_SIM_PLUG_IN /* 162 */:
                    issue = "Sim PlugIn";
                    break;
                case LOG_TYPE_SIM_PLUG_OUT /* 163 */:
                    issue = "Sim PlugOut";
                    break;
                case LOG_TYPE_SIM_PLUG_RECOVERED /* 164 */:
                    issue = "Sim Recovered";
                    break;
                default:
                    issue = "unknow";
                    break;
            }
            OppoManagerHelper.writeLogToPartition(this.mContext, "zz_oppo_critical_log_" + type, logString, issue);
        }
    }

    public void turnOffHotspot(int index) {
        String str = this.LOG_TAG;
        Rlog.d(str, "turnOffHotspot: index = " + index);
        IccRecords records = mUiccController.getIccRecords(index, 1);
        if (records == null) {
            return;
        }
        if ("21407".equals(records.getOperatorNumeric()) || "21405".equals(records.getOperatorNumeric())) {
            try {
                Context context = this.mContext;
                Context context2 = this.mContext;
                WifiManager wm = (WifiManager) context.getSystemService("wifi");
                if (wm != null) {
                    int state = wm.getWifiApState();
                    String str2 = this.LOG_TAG;
                    Rlog.d(str2, "isHotspotEnable: state = " + state);
                    Context context3 = this.mContext;
                    Context context4 = this.mContext;
                    ConnectivityManager cm = (ConnectivityManager) context3.getSystemService("connectivity");
                    if (cm != null && (state == 13 || state == 12)) {
                        cm.stopTethering(0);
                    }
                    return;
                }
                Rlog.e(this.LOG_TAG, "isHotspotEnable: get wifimanager fail");
            } catch (Exception e) {
                String str3 = this.LOG_TAG;
                Rlog.e(str3, "turnOffHotspot: e = " + e);
            }
        }
    }

    private void log(String string) {
        Rlog.d(this.LOG_TAG, string);
    }

    public void broadcastCardHotSwapState(int slotId) {
        TelephonyManager tm = TelephonyManager.from(this.mContext);
        int simState = 0;
        if (tm != null) {
            simState = tm.getSimState(slotId);
        } else {
            log("broadcastCardHotSwapState, tm is null for slotid:" + slotId);
        }
        Intent intent = new Intent(ACTION_HOT_SWAP_STATE_CHANGED);
        intent.putExtra(EXTRA_SIM_PHONEID, slotId);
        intent.putExtra(EXTRA_SIM_STATE, simState);
        intent.addFlags(16777216);
        intent.setPackage(PKG_CMCC_ATUO_REG);
        log("Broadcasting intent ACTION_HOT_SWAP_STATE_CHANGED slotid:" + slotId + " simState:" + simState + " for CmccAutoReg");
        this.mContext.sendBroadcast(intent);
    }

    public void SendbroadcastSimInfoContentChanged() {
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        intent.putExtra(INTENT_VALUE_PRIVISION, INTENT_VALUE_PRIVISION);
        intent.addFlags(16777216);
        this.mContext.sendBroadcast(intent);
        log("SendbroadcastSimInfoContentChanged");
    }

    public void broadcastManualProvisionStatusChanged(int phoneId, int newProvisionState) {
        int newProvisionState2;
        if (newProvisionState == 10 || newProvisionState == 11) {
            newProvisionState2 = newProvisionState - 10;
        } else {
            newProvisionState2 = -1;
        }
        Intent intent = new Intent(ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED);
        intent.putExtra("phone", phoneId);
        intent.putExtra(EXTRA_NEW_PROVISION_STATE, newProvisionState2);
        intent.addFlags(16777216);
        this.mContext.sendBroadcast(intent);
        log("broadcastManualProvisionStatusChanged phoneid:" + phoneId + " newProvisionState" + newProvisionState2);
    }

    public boolean getHaveInsertTestCard() {
        return SystemProperties.getBoolean(WHITE_SIM_CARD_INSERT_PROC, false);
    }
}
