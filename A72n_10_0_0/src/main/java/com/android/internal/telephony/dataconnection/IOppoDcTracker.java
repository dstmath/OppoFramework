package com.android.internal.telephony.dataconnection;

import android.telephony.data.ApnSetting;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;
import com.android.internal.telephony.dataconnection.DataConnectionReasons;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.ArrayList;

public interface IOppoDcTracker extends IOppoCommonFeature {
    public static final IOppoDcTracker DEFAULT = new IOppoDcTracker() {
        /* class com.android.internal.telephony.dataconnection.IOppoDcTracker.AnonymousClass1 */
    };
    public static final String TAG = "IOppoDcTracker";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoDcTracker;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoDcTracker getDefault() {
        return DEFAULT;
    }

    default void unregisterForOppoAllEvents() {
    }

    default String getOperatorNumeric() {
        return PhoneConfigurationManager.SSSS;
    }

    default void updateWaitingApns(ApnSetting preferredApn, ApnContext apnContext) {
    }

    default boolean checkIfNeedDataStall() {
        return false;
    }

    default void setNeedDataStallFlag(ApnSetting apn) {
    }

    default DataConnectionReasons.DataDisallowedReasonType isOppoRoamingAllowed() {
        return null;
    }

    default void OppoSetupDataOnAllConnectableApns(String reason) {
    }

    default void oppoRegisterForImsiReady(IccRecords iccRecords) {
    }

    default void oppoUnregisterForImsiReady(IccRecords iccRecords) {
    }

    default void oppoHandleRoamingTypeChange(IccRecords iccRecords) {
    }

    default void startMobileDataHongbaoPolicy(int time1, int time2, String value1, String value2) {
    }

    default boolean informNewSimCardLoaded(int slotIndex) {
        return false;
    }

    default void updateMapValue(int slotIndex, int value) {
    }

    default void setDataRoamingEnabledForOperator(int slotIndex) {
    }

    default void oppoWlanAssistantMeasureForDataEnabled(boolean enabled) {
    }

    default void oppoWlanAssistantMeasureForRatChange() {
    }

    default void oppoWlanAssistantMeasureForDataStateChanged() {
    }

    default boolean oppoWlanAssistantBlockTrySetupData(ApnContext apnContext, String reason) {
        return false;
    }

    default void oppoWlanAssistantDelayMeasure() {
    }

    default void oppoWlanAssistantDelaySetupData() {
    }

    default void checkIfRetryAfterDisconnected(ApnContext apnContext, boolean retry) {
    }

    default boolean oemAllowMmsWhenDataDisableNonRoaming(ApnContext apnContext, DataEnabledSettings settings) {
        return false;
    }

    default boolean haveVsimIgnoreUserDataSetting() {
        return false;
    }

    default void registerOnImsCallStateChange() {
    }

    default void notifyDataConnectionOnVoiceCallStateChange(String reasonForVoiceCall) {
    }

    default DctConstants.State getApnState(String apnType) {
        return null;
    }

    default boolean needManualSelectAPN(ApnSetting preferredApn) {
        return false;
    }

    default boolean needManualSelectAPN(String apnType, ApnSetting preferredApn) {
        return false;
    }

    default ArrayList<ApnSetting> getDunApnList(ArrayList<ApnSetting> arrayList) {
        return null;
    }

    default boolean isTelstraSimAndNetworkClassNotChange() {
        return false;
    }

    default boolean checkIfValidIAApn(ApnSetting initialAttachApnSetting) {
        return false;
    }

    default void writeLogToPartionForLteLimit(int nwLimitState, String cellLocation) {
    }

    default String getCellLocation() {
        return PhoneConfigurationManager.SSSS;
    }

    default boolean recordDataStallInfo(boolean hasInboundData, long sentSinceLastRecv) {
        return false;
    }

    default void recordNoApnAvailable() {
    }

    default void recordNoOperatorError() {
    }

    default void printNoReceiveDataError(long sendNumber) {
    }

    default void oemCloseNr(Phone phone) {
    }

    default boolean isTargetVersion() {
        return false;
    }

    default ArrayList<ApnSetting> sortApnList(ArrayList<ApnSetting> arrayList) {
        return null;
    }
}
