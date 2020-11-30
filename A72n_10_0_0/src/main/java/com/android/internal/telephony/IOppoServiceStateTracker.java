package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.CellIdentity;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;
import java.util.List;

public interface IOppoServiceStateTracker extends IOppoCommonFeature {
    public static final IOppoServiceStateTracker DEFAULT = new IOppoServiceStateTracker() {
        /* class com.android.internal.telephony.IOppoServiceStateTracker.AnonymousClass1 */
    };
    public static final String TAG = "IOppoServiceStateTracker";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoServiceStateTracker;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoServiceStateTracker getDefault() {
        return DEFAULT;
    }

    default int oppoOosDelayState(ServiceState ss) {
        return 0;
    }

    default boolean onSignalStrengthResultEx(AsyncResult ar, SignalStrength signalStrength) {
        return false;
    }

    default void updateSignalStrengthLevel(SignalStrength signalStrength) {
    }

    default void oppoResetOosDelayState() {
    }

    default void setSwitchingDdsState(boolean state) {
    }

    default boolean oppoIsInDelayOOSState() {
        return false;
    }

    default void oppoSetAutoNetworkSelect(ServiceState ss) {
    }

    default String getPnnName(String plmn, boolean longName) {
        return PhoneConfigurationManager.SSSS;
    }

    default String oppoGetPlmn() {
        return PhoneConfigurationManager.SSSS;
    }

    default String getPlmnResult() {
        return PhoneConfigurationManager.SSSS;
    }

    default String getSpnResult() {
        return PhoneConfigurationManager.SSSS;
    }

    default boolean getShowPlmnResult() {
        return false;
    }

    default boolean getShowSpnResult() {
        return false;
    }

    default void oppoUpdateGsmSpnDisplay(String plmn, String spn, boolean showPlmn, boolean showSpn) {
    }

    default void oppoUpdateCdmaSpnDisplay(String plmn, String spn, boolean showPlmn, boolean showSpn) {
    }

    default boolean isCtCard(Phone mPhone) {
        return false;
    }

    default String getOemSpn() {
        return PhoneConfigurationManager.SSSS;
    }

    default boolean isTelstraVersion() {
        return false;
    }

    default int getSignalLevel() {
        return 0;
    }

    default boolean updateOperatorRoaming(ServiceState ss, String simMccmnc, boolean roaming) {
        return false;
    }

    default SignalStrength getOrigSignalStrength() {
        return null;
    }

    default void oppoAddDataCallCount() {
    }

    default void oppoAddSmsSendCount() {
    }

    default void oppoAddNitzCount() {
    }

    default void oppoUpdateVoiceRegState(int state) {
    }

    default boolean checkCtMacauSimRoamingState(ServiceState mNewSS) {
        return false;
    }

    default void oppoPollStateDone(ServiceState mNewSS) {
    }

    default void onSubscriptionsChangedForOppo(int subId) {
    }

    default boolean isNeedupdateNitzTime() {
        return false;
    }

    default void broadcastMccChange(String plmn) {
    }

    default void cleanMccProperties(int phoneId) {
    }

    default void setMccProperties(int phoneId, String mcc) {
    }

    default CellLocation oppoGetCTLteCellLocation() {
        return null;
    }

    default boolean isOppoRegionLockedState(ServiceState ss, CellIdentity cid, boolean hasLocationChanged) {
        return false;
    }

    default int oppoUpdateNrState(int newNrState, boolean hasNrSecondaryServingCell, ServiceState ss) {
        return 0;
    }

    default boolean checkDeepSleepStatus(Context context, List<CellInfo> list, Message rspMsg) {
        return false;
    }

    default void setOemRadioReseting(boolean enable) {
    }

    default boolean isNotNotifyMergeServiceOperator() {
        return false;
    }
}
