package com.mediatek.internal.telephony;

import android.hardware.radio.V1_0.SuppSvcNotification;
import com.android.internal.telephony.RIL;
import java.util.ArrayList;
import vendor.mediatek.hardware.mtkradioex.V1_0.CfuStatusNotification;
import vendor.mediatek.hardware.mtkradioex.V1_0.CipherNotification;
import vendor.mediatek.hardware.mtkradioex.V1_0.CrssNotification;
import vendor.mediatek.hardware.mtkradioex.V1_0.DedicateDataCall;
import vendor.mediatek.hardware.mtkradioex.V1_0.EtwsNotification;
import vendor.mediatek.hardware.mtkradioex.V1_0.IncomingCallNotification;
import vendor.mediatek.hardware.mtkradioex.V1_0.PcoDataAttachedInfo;
import vendor.mediatek.hardware.mtkradioex.V1_0.SignalStrengthWithWcdmaEcio;
import vendor.mediatek.hardware.mtkradioex.V1_0.VsimOperationEvent;
import vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication;

public class MtkRadioExIndicationBase extends IMtkRadioExIndication.Stub {
    MtkRadioExIndicationBase(RIL ril) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void currentSignalStrengthWithWcdmaEcioInd(int indicationType, SignalStrengthWithWcdmaEcio signalStrength) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void cfuStatusNotify(int indicationType, CfuStatusNotification cfuStatus) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void incomingCallIndication(int indicationType, IncomingCallNotification inCallNotify) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void callAdditionalInfoInd(int indicationType, int ciType, ArrayList<String> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void cipherIndication(int indicationType, CipherNotification cipherNotify) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void suppSvcNotifyEx(int indicationType, SuppSvcNotification suppSvc) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void crssIndication(int indicationType, CrssNotification crssNotification) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void cdmaCallAccepted(int indicationType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void eccNumIndication(int indicationType, String eccListWithCard, String eccListNoCard) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void responseCsNetworkStateChangeInd(int indicationType, ArrayList<String> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void responsePsNetworkStateChangeInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void responseNetworkEventInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void responseModulationInfoInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void responseInvalidSimInd(int indicationType, ArrayList<String> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void responseFemtocellInfo(int indicationType, ArrayList<String> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void responseLteNetworkInfo(int indicationType, int info) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onMccMncChanged(int indicationType, String mccmnc) {
    }

    public void onVirtualSimOn(int indicationType, int simInserted) {
    }

    public void onVirtualSimOff(int indicationType, int simInserted) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onVirtualSimStatusChanged(int indicationType, int simInserted) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onImeiLock(int indicationType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onImsiRefreshDone(int indicationType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onCardDetectedInd(int indicationType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void newEtwsInd(int indicationType, EtwsNotification etws) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void meSmsStorageFullInd(int indicationType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void smsReadyInd(int indicationType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void dataAllowedNotification(int indicationType, int isAllowed) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onPseudoCellInfoInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void eMBMSSessionStatusIndication(int indicationType, int status) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void eMBMSAtInfoIndication(int indicationType, String info) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void plmnChangedIndication(int indicationType, ArrayList<String> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void registrationSuspendedIndication(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void gmssRatChangedIndication(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void worldModeChangedIndication(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void resetAttachApnInd(int indicationType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void mdChangedApnInd(int indicationType, int apnClassType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void esnMeidChangeInd(int indicationType, String esnMeid) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void phbReadyNotification(int indicationType, int isPhbReady) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void bipProactiveCommand(int indicationType, String cmd) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void triggerOtaSP(int indicationType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onStkMenuReset(int indicationType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onMdDataRetryCountReset(int indicationType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onRemoveRestrictEutran(int indicationType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onLteAccessStratumStateChanged(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onSimHotSwapInd(int indicationType, int event, String info) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onSimPowerChangedInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onRsuSimLockEvent(int indicationType, int eventId) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void smlSlotLockInfoChangedInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void networkInfoInd(int indicationType, ArrayList<String> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onSimMeLockEvent(int indicationType) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void pcoDataAfterAttached(int indicationType, PcoDataAttachedInfo pco) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void confSRVCC(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onVsimEventIndication(int indicationType, VsimOperationEvent event) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void dedicatedBearerActivationInd(int indicationType, DedicateDataCall ddcResult) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void dedicatedBearerModificationInd(int indicationType, DedicateDataCall ddcResult) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void dedicatedBearerDeactivationInd(int indicationType, int ddcResult) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void oemHookRaw(int indicationType, ArrayList<Byte> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onTxPowerIndication(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onTxPowerStatusIndication(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void networkRejectCauseInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void dsbpStateChanged(int indicationType, int dsbpState) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void networkBandInfoInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void smsInfoExtInd(int indicationType, String info) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onDsdaChangedInd(int indicationType, int mode) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void qualifiedNetworkTypesChangedInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void onCellularQualityChangedInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void mobileDataUsageInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void smlDeviceLockInfoChangedInd(int indicationType, ArrayList<Integer> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    public void smlEncryptedSerialIdInd(int indicationType, ArrayList<String> arrayList) {
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
    public void onNwLimitInd(int indicationType, ArrayList<Integer> arrayList) {
    }
}
