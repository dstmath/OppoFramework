package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.ims.ImsReasonInfo;
import com.android.ims.ImsCall;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;
import com.android.internal.telephony.imsphone.ImsPhoneCall;
import com.android.internal.telephony.imsphone.ImsPhoneConnection;

public interface IOppoCallTracker extends IOppoCommonFeature {
    public static final IOppoCallTracker DEFAULT = new IOppoCallTracker() {
        /* class com.android.internal.telephony.IOppoCallTracker.AnonymousClass1 */
    };

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoCallTracker;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoCallTracker getDefault() {
        return DEFAULT;
    }

    default void handleCallinControl(int index, DriverCall dc) {
    }

    default void handleCallinControl(ImsCall imsCall) {
    }

    default boolean handleCallinControl(CdmaCallWaitingNotification cw) {
        return false;
    }

    default boolean handlDuplicateCdmaCW(CdmaCallWaitingNotification cw) {
        return false;
    }

    default void handleAutoAnswer(Phone phone) {
    }

    default void oemHandleFeatureCapabilityChanged() {
    }

    default boolean oemSRVCCWhenHangup(ImsPhoneCall call, ImsPhoneConnection pendingMO) {
        return false;
    }

    default int oemGetDisconnectCauseFromReasonInfo(int code) {
        return 36;
    }

    default void processPendingHangup(String msg) {
    }

    default void oemRetryResumeAfterResumeFail(ImsReasonInfo reasonInfo) {
    }

    default boolean getVowifiRegStatus() {
        return false;
    }

    default void oemResetImsCapabilities() {
    }

    default void handlePhoneStateChanged(GsmCdmaCallTracker mGCCT, PhoneConstants.State oldState, PhoneConstants.State mState) {
    }

    default void handleDropedCall() {
    }

    default void handleCallStatePhoneIdle(Connection mPendingMO, int mPendingOperations) {
    }

    default void handlePendingHangupSRVCC(CallTracker mImsPhoneCallTracker, int index) {
    }

    default void handleExtraCallStateChanged(GsmCdmaConnection conn, DriverCall dc, int dcSize, boolean hasNonHangupStateChanged) {
    }

    default void handleSwitchResult(GsmCdmaConnection mPendingMO, Message msg) {
    }

    default void hanleEndIncomingCall(GsmCdmaConnection mRingingConnection) {
    }

    default void handleDialComplete(GsmCdmaConnection mPendingMO, AsyncResult ar) {
    }

    default void handleHangupComplete(Message msg) {
    }

    default void handleAcceptComplete(Message msg) {
    }

    default void handleImsPhoneStateChanged(PhoneConstants.State oldState, PhoneConstants.State mState) {
    }

    default boolean oemProcessPendingHangup(ImsPhoneCall call, ImsPhoneConnection conn) {
        return false;
    }

    default void oemNotifySrvccComplete() {
    }
}
