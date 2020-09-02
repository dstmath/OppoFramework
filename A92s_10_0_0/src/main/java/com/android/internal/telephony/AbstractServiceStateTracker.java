package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellIdentity;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import java.util.List;

public class AbstractServiceStateTracker extends Handler {
    protected IOppoServiceStateTracker mReference;

    public int oppoOosDelayState(ServiceState ss) {
        return this.mReference.oppoOosDelayState(ss);
    }

    public void oppoResetOosDelayState() {
        this.mReference.oppoResetOosDelayState();
    }

    public boolean onSignalStrengthResultEx(AsyncResult ar, SignalStrength signalStrength) {
        return this.mReference.onSignalStrengthResultEx(ar, signalStrength);
    }

    public void setSwitchingDdsState(boolean state) {
        this.mReference.setSwitchingDdsState(state);
    }

    public void setSignalStrength(SignalStrength signalStrength) {
    }

    public void updateSignalStrengthLevel(SignalStrength signalStrength) {
        this.mReference.updateSignalStrengthLevel(signalStrength);
    }

    public boolean oppoIsInDelayOOSState() {
        return this.mReference.oppoIsInDelayOOSState();
    }

    public void oppoSetAutoNetworkSelect(ServiceState ss) {
        this.mReference.oppoSetAutoNetworkSelect(ss);
    }

    public String getPnnName(String plmn, boolean longName) {
        return this.mReference.getPnnName(plmn, longName);
    }

    public String oppoGetPlmn() {
        return this.mReference.oppoGetPlmn();
    }

    public String getPlmnResult() {
        return this.mReference.getPlmnResult();
    }

    public String getSpnResult() {
        return this.mReference.getSpnResult();
    }

    public boolean getShowPlmnResult() {
        return this.mReference.getShowPlmnResult();
    }

    public boolean getShowSpnResult() {
        return this.mReference.getShowSpnResult();
    }

    public void oppoUpdateGsmSpnDisplay(String plmn, String spn, boolean showPlmn, boolean showSpn) {
        this.mReference.oppoUpdateGsmSpnDisplay(plmn, spn, showPlmn, showSpn);
    }

    public void oppoUpdateCdmaSpnDisplay(String plmn, String spn, boolean showPlmn, boolean showSpn) {
        this.mReference.oppoUpdateCdmaSpnDisplay(plmn, spn, showPlmn, showSpn);
    }

    public String getOemSpn() {
        return this.mReference.getOemSpn();
    }

    public boolean isTelstraVersion() {
        return this.mReference.isTelstraVersion();
    }

    public boolean updateOperatorRoaming(ServiceState ss, String simMccmnc, boolean roaming) {
        return this.mReference.updateOperatorRoaming(ss, simMccmnc, roaming);
    }

    public int getSignalLevel() {
        return this.mReference.getSignalLevel();
    }

    public SignalStrength getOrigSignalStrength() {
        return this.mReference.getOrigSignalStrength();
    }

    public void oppoAddDataCallCount() {
        this.mReference.oppoAddDataCallCount();
    }

    public void oppoAddSmsSendCount() {
        this.mReference.oppoAddSmsSendCount();
    }

    public void oppoAddNitzCount() {
        this.mReference.oppoAddNitzCount();
    }

    public void oppoUpdateVoiceRegState(int state) {
        this.mReference.oppoUpdateVoiceRegState(state);
    }

    public boolean isCtCard(Phone mPhone) {
        return this.mReference.isCtCard(mPhone);
    }

    public boolean checkCtMacauSimRoamingState(ServiceState mNewSS) {
        return this.mReference.checkCtMacauSimRoamingState(mNewSS);
    }

    public void oppoPollStateDone(ServiceState mNewSS) {
        this.mReference.oppoPollStateDone(mNewSS);
    }

    public void onSubscriptionsChangedForOppo(int subId) {
        this.mReference.onSubscriptionsChangedForOppo(subId);
    }

    public boolean isNeedupdateNitzTime() {
        return this.mReference.isNeedupdateNitzTime();
    }

    public void broadcastMccChange(String plmn) {
        this.mReference.broadcastMccChange(plmn);
    }

    public void cleanMccProperties(int phoneId) {
        this.mReference.cleanMccProperties(phoneId);
    }

    public void setMccProperties(int phoneId, String mcc) {
        this.mReference.setMccProperties(phoneId, mcc);
    }

    public CellLocation oppoGetCTLteCellLocation() {
        return this.mReference.oppoGetCTLteCellLocation();
    }

    public boolean isOppoRegionLockedState(ServiceState ss, CellIdentity cid, boolean hasLocationChanged) {
        return this.mReference.isOppoRegionLockedState(ss, cid, hasLocationChanged);
    }

    public int oppoUpdateNrState(int newNrState, boolean hasNrSecondaryServingCell, ServiceState ss) {
        return this.mReference.oppoUpdateNrState(newNrState, hasNrSecondaryServingCell, ss);
    }

    public boolean checkDeepSleepStatus(Context context, List<CellInfo> lastCellInfoList, Message rspMsg) {
        IOppoServiceStateTracker iOppoServiceStateTracker = this.mReference;
        if (iOppoServiceStateTracker == null) {
            return false;
        }
        return iOppoServiceStateTracker.checkDeepSleepStatus(context, lastCellInfoList, rspMsg);
    }
}
