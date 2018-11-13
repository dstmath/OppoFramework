package org.codeaurora.ims;

import org.codeaurora.ims.internal.IQtiImsExtListener.Stub;

public class QtiImsExtListenerBaseImpl extends Stub {
    public void onSetCallForwardUncondTimer(int phoneId, int status) {
    }

    public void onGetCallForwardUncondTimer(int phoneId, int startHour, int endHour, int startMinute, int endMinute, int reason, int status, String number, int service) {
    }

    public void onUTReqFailed(int phoneId, int errCode, String errString) {
    }

    public void onGetPacketCount(int phoneId, int status, long packetCount) {
    }

    public void onGetPacketErrorCount(int phoneId, int status, long packetErrorCount) {
    }

    public void receiveCallDeflectResponse(int phoneId, int result) {
    }

    public void receiveCallTransferResponse(int phoneId, int result) {
    }

    public void receiveCancelModifyCallResponse(int phoneId, int result) {
    }

    public void notifyVopsStatus(int phoneId, boolean vopsStatus) {
    }

    public void notifySsacStatus(int phoneId, boolean ssacStatusResponse) {
    }

    public void notifyParticipantStatusInfo(int phoneId, int operation, int sipStatus, String participantUri, boolean isEct) {
    }

    public void onVoltePreferenceUpdated(int phoneId, int result) {
    }

    public void onVoltePreferenceQueried(int phoneId, int result, int mode) {
    }

    public void onSetHandoverConfig(int phoneId, int result) {
    }

    public void onGetHandoverConfig(int phoneId, int result, int hoConfig) {
    }
}
