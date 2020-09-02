package com.android.internal.telephony.dataconnection;

import android.os.Handler;
import android.provider.Settings;
import android.telephony.data.ApnSetting;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.OppoModemLogManager;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.DataConnectionReasons;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractDcTracker extends Handler {
    protected IOppoDcTracker mReference;

    protected AbstractDcTracker() {
    }

    public void unregisterForOppoAllEvents() {
        this.mReference.unregisterForOppoAllEvents();
    }

    public String getOperatorNumeric() {
        return this.mReference.getOperatorNumeric();
    }

    /* access modifiers changed from: protected */
    public void updateWaitingApns(ApnSetting preferredApn, ApnContext apnContext) {
        this.mReference.updateWaitingApns(preferredApn, apnContext);
    }

    public boolean checkIfNeedDataStall() {
        return this.mReference.checkIfNeedDataStall();
    }

    public void setNeedDataStallFlag(ApnSetting apn) {
        this.mReference.setNeedDataStallFlag(apn);
    }

    /* access modifiers changed from: protected */
    public DataConnectionReasons.DataDisallowedReasonType isOppoRoamingAllowed() {
        return this.mReference.isOppoRoamingAllowed();
    }

    /* access modifiers changed from: protected */
    public void oppoRegisterForImsiReady(IccRecords iccRecords) {
        this.mReference.oppoRegisterForImsiReady(iccRecords);
    }

    /* access modifiers changed from: protected */
    public void oppoUnregisterForImsiReady(IccRecords iccRecords) {
        this.mReference.oppoUnregisterForImsiReady(iccRecords);
    }

    /* access modifiers changed from: protected */
    public void OppoSetupDataOnAllConnectableApns(String reason) {
        this.mReference.OppoSetupDataOnAllConnectableApns(reason);
    }

    /* access modifiers changed from: protected */
    public void oppoHandleRoamingTypeChange(IccRecords iccRecords) {
        this.mReference.oppoHandleRoamingTypeChange(iccRecords);
    }

    public void onRoamingTypeChanged() {
    }

    public void startMobileDataHongbaoPolicy(int time1, int time2, String value1, String value2) {
        this.mReference.startMobileDataHongbaoPolicy(time1, time2, value1, value2);
    }

    public boolean informNewSimCardLoaded(int slotIndex) {
        return this.mReference.informNewSimCardLoaded(slotIndex);
    }

    public void updateMapValue(int slotIndex, int value) {
        this.mReference.updateMapValue(slotIndex, value);
    }

    public void setDataRoamingEnabledForOperator(int slotIndex) {
        this.mReference.setDataRoamingEnabledForOperator(slotIndex);
    }

    public void oppoWlanAssistantMeasureForDataEnabled(boolean enabled) {
        this.mReference.oppoWlanAssistantMeasureForDataEnabled(enabled);
    }

    public void oppoWlanAssistantMeasureForRatChange() {
        this.mReference.oppoWlanAssistantMeasureForRatChange();
    }

    public void oppoWlanAssistantMeasureForDataStateChanged() {
        this.mReference.oppoWlanAssistantMeasureForDataStateChanged();
    }

    public boolean oppoWlanAssistantBlockTrySetupData(ApnContext apnContext, String reason) {
        return this.mReference.oppoWlanAssistantBlockTrySetupData(apnContext, reason);
    }

    public void oppoWlanAssistantDelayMeasure() {
        this.mReference.oppoWlanAssistantDelayMeasure();
    }

    public void oppoWlanAssistantDelaySetupData() {
        this.mReference.oppoWlanAssistantDelaySetupData();
    }

    public void checkIfRetryAfterDisconnected(ApnContext apnContext, boolean retry) {
        this.mReference.checkIfRetryAfterDisconnected(apnContext, retry);
    }

    public void writeLogToPartionForLteLimit(int nwLimitState, String cellLocation) {
        this.mReference.writeLogToPartionForLteLimit(nwLimitState, cellLocation);
    }

    /* access modifiers changed from: protected */
    public boolean oemAllowMmsWhenDataDisableNonRoaming(ApnContext apnContext, DataEnabledSettings settings) {
        return this.mReference.oemAllowMmsWhenDataDisableNonRoaming(apnContext, settings);
    }

    public boolean haveVsimIgnoreUserDataSetting() {
        return this.mReference.haveVsimIgnoreUserDataSetting();
    }

    public void registerOnImsCallStateChange() {
        this.mReference.registerOnImsCallStateChange();
    }

    public void notifyDataConnectionOnVoiceCallStateChange(String reasonForVoiceCall) {
        IOppoDcTracker iOppoDcTracker = this.mReference;
        if (iOppoDcTracker != null) {
            iOppoDcTracker.notifyDataConnectionOnVoiceCallStateChange(reasonForVoiceCall);
        }
    }

    public DctConstants.State getApnState(String apnType) {
        return this.mReference.getApnState(apnType);
    }

    public boolean needManualSelectAPN(ApnSetting preferredApn) {
        return this.mReference.needManualSelectAPN(preferredApn);
    }

    public boolean needManualSelectAPN(String apnType, ApnSetting preferredApn) {
        return this.mReference.needManualSelectAPN(apnType, preferredApn);
    }

    public ArrayList<ApnSetting> getDunApnList(ArrayList<ApnSetting> mAllApnSettings) {
        return this.mReference.getDunApnList(mAllApnSettings);
    }

    public boolean isTelstraSimAndNetworkClassNotChange() {
        return this.mReference.isTelstraSimAndNetworkClassNotChange();
    }

    public boolean checkIfValidIAApn(ApnSetting initialAttachApnSetting) {
        return this.mReference.checkIfValidIAApn(initialAttachApnSetting);
    }

    public void setupDataOnAllConnectableApns(String reason) {
    }

    public void notifyOffApnsOfAvailability() {
    }

    public AtomicReference<IccRecords> getIccRecords() {
        return null;
    }

    public int getTransportType() {
        return 0;
    }

    public ConcurrentHashMap<String, ApnContext> getApnContexts() {
        return null;
    }

    public ApnContext getApnContextByType(int apnType) {
        return null;
    }

    public void refreshNetStat(boolean isScreenOn) {
    }

    public void setAutoAttachOnCreationConfig(boolean autoAttachOnCreationConfig) {
    }

    public void setDataStallNoRxEnabled(boolean dataStallNoRxEnabled) {
    }

    public String getCellLocation() {
        return this.mReference.getCellLocation();
    }

    public boolean recordDataStallInfo(boolean hasInboundData, long sentSinceLastRecv) {
        return this.mReference.recordDataStallInfo(hasInboundData, sentSinceLastRecv);
    }

    public void recordNoApnAvailable() {
        this.mReference.recordNoApnAvailable();
    }

    public void recordNoOperatorError() {
        this.mReference.recordNoOperatorError();
    }

    public void printNoReceiveDataError(long sendNumber) {
        this.mReference.printNoReceiveDataError(sendNumber);
    }

    public void oemCloseNr(Phone phone) {
        this.mReference.oemCloseNr(phone);
    }

    public void oemCloseNrCase0(Phone phone) {
        try {
            String enable = Settings.Global.getString(phone.getContext().getContentResolver(), "FEATURE_DATA_DO_RECOVERY_CLOSE_5G_CASE_0");
            OppoRlog.Rlog.d("AbstractDcTracker", "oemCloseNrCase0,enable=" + enable);
            if (!OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK.equals(enable)) {
                oemCloseNr(phone);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void oemCloseNrCase1(Phone phone) {
        try {
            String enable = Settings.Global.getString(phone.getContext().getContentResolver(), "FEATURE_DATA_DO_RECOVERY_CLOSE_5G_CASE_1");
            OppoRlog.Rlog.d("AbstractDcTracker", "oemCloseNrCase1,enable=" + enable);
            if (!OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK.equals(enable)) {
                oemCloseNr(phone);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
