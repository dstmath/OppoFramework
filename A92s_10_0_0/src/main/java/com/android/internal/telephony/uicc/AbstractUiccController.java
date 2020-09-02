package com.android.internal.telephony.uicc;

import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

public class AbstractUiccController extends Handler {
    protected static final int EVENT_TRAY_PLUG_IN = 203;
    protected static final String HOTSWAP_SIM_PLUG_IN = "PLUGIN";
    protected static final String HOTSWAP_SIM_PLUG_OUT = "PLUGOUT";
    protected static final String HOTSWAP_SIM_TRAY_PLUG_IN = "TRAYPLUGIN";
    protected static final int LOG_TYPE_SIM_PLUG_IN = 162;
    protected static final int LOG_TYPE_SIM_PLUG_OUT = 163;
    protected static final int LOG_TYPE_SIM_PLUG_RECOVERED = 164;
    protected static final String SIM_HOTSWAP_STATE_CHANGE = "oppo.intent.action.SIM_HOTSWAP_STATE_CHANGE";
    public final String FEATURE_ENABLE_HOTSWAP = "gsm.enable_hotswap";
    protected boolean bIsNeedRecordLog = true;
    public boolean[] isSimPlugIn = new boolean[TelephonyManager.getDefault().getPhoneCount()];
    public boolean[] isTrayPlugIn = new boolean[TelephonyManager.getDefault().getPhoneCount()];
    protected IOppoUiccController mReference;

    /* access modifiers changed from: protected */
    public void notifyIccIdForSimPlugOut(int slotid) {
        this.mReference.notifyIccIdForSimPlugOut(slotid);
    }

    /* access modifiers changed from: protected */
    public void notifyIccIdForSimPlugIn(int slotid) {
        this.mReference.notifyIccIdForSimPlugIn(slotid);
    }

    /* access modifiers changed from: protected */
    public void notifyIccIdForTrayPlugIn(int slotid) {
        this.mReference.notifyIccIdForTrayPlugIn(slotid);
    }

    public boolean getSimHotSwapPlugInState() {
        return this.mReference.getSimHotSwapPlugInState();
    }

    /* access modifiers changed from: protected */
    public void saveSimPlugState(int index, int type) {
        this.mReference.saveSimPlugState(index, type);
    }

    /* access modifiers changed from: protected */
    public void turnOffHotspot(int index) {
        this.mReference.turnOffHotspot(index);
    }

    public void broadcastCardHotSwapState(int slotId) {
        this.mReference.broadcastCardHotSwapState(slotId);
    }

    /* access modifiers changed from: protected */
    public void SendbroadcastSimInfoContentChanged() {
        this.mReference.SendbroadcastSimInfoContentChanged();
    }

    /* access modifiers changed from: protected */
    public void broadcastManualProvisionStatusChanged(int phoneId, int newProvisionState) {
        this.mReference.broadcastManualProvisionStatusChanged(phoneId, newProvisionState);
    }

    public void setSimPower(int slotId, int state, Message onComplete) {
    }

    public boolean getHaveInsertTestCard() {
        return this.mReference.getHaveInsertTestCard();
    }
}
