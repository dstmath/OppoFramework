package com.mediatek.internal.telephony;

import android.content.Intent;
import android.engineer.OppoEngineerManager;
import android.hardware.radio.V1_0.SuppSvcNotification;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.IOppoNetworkManager;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.uicc.IccUtils;
import com.mediatek.internal.telephony.dataconnection.PcoDataAfterAttached;
import com.mediatek.internal.telephony.gsm.MtkSuppCrssNotification;
import com.mediatek.internal.telephony.gsm.MtkSuppServiceNotification;
import com.mediatek.internal.telephony.ims.MtkDedicateDataCallResponse;
import com.mediatek.internal.telephony.worldphone.WorldMode;
import com.mediatek.telephony.internal.telephony.vsim.ExternalSimManager;
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

public class MtkRadioExIndication extends MtkRadioExIndicationBase {
    private static final boolean ENG = "eng".equals(Build.TYPE);
    private static final String TAG = "MtkRadioInd";
    private MtkRIL mMtkRil;

    MtkRadioExIndication(RIL ril) {
        super(ril);
        this.mMtkRil = (MtkRIL) ril;
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void cfuStatusNotify(int indicationType, CfuStatusNotification cfuStatus) {
        this.mMtkRil.processIndication(indicationType);
        int[] notification = {cfuStatus.status, cfuStatus.lineId};
        this.mMtkRil.unsljLogRet(3070, notification);
        if (notification[1] == 1) {
            this.mMtkRil.mCfuReturnValue = notification;
        }
        if (this.mMtkRil.mCallForwardingInfoRegistrants.size() != 0 && notification[1] == 1) {
            this.mMtkRil.mCallForwardingInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, notification, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void incomingCallIndication(int indicationType, IncomingCallNotification inCallNotify) {
        this.mMtkRil.processIndication(indicationType);
        String[] notification = new String[7];
        notification[0] = inCallNotify.callId;
        notification[1] = inCallNotify.number;
        notification[2] = inCallNotify.type;
        notification[3] = inCallNotify.callMode;
        notification[4] = inCallNotify.seqNo;
        notification[5] = inCallNotify.redirectNumber;
        this.mMtkRil.unsljLogRet(3015, null);
        if (this.mMtkRil.mIncomingCallIndicationRegistrant != null) {
            this.mMtkRil.mIncomingCallIndicationRegistrant.notifyRegistrant(new AsyncResult((Object) null, notification, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void callAdditionalInfoInd(int indicationType, int ciType, ArrayList<String> info) {
        this.mMtkRil.processIndication(indicationType);
        String[] notification = new String[(info.size() + 1)];
        notification[0] = Integer.toString(ciType);
        for (int i = 0; i < info.size(); i++) {
            notification[i + 1] = info.get(i);
        }
        this.mMtkRil.unsljLogRet(3126, null);
        if (this.mMtkRil.mCallAdditionalInfoRegistrants != null) {
            this.mMtkRil.mCallAdditionalInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, notification, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void cipherIndication(int indicationType, CipherNotification cipherNotify) {
        this.mMtkRil.processIndication(indicationType);
        String[] notification = {cipherNotify.simCipherStatus, cipherNotify.sessionStatus, cipherNotify.csStatus, cipherNotify.psStatus};
        this.mMtkRil.unsljLogRet(3024, notification);
        if (this.mMtkRil.mCipherIndicationRegistrants != null) {
            this.mMtkRil.mCipherIndicationRegistrants.notifyRegistrants(new AsyncResult((Object) null, notification, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void suppSvcNotifyEx(int indicationType, SuppSvcNotification suppSvcNotification) {
        this.mMtkRil.processIndication(indicationType);
        MtkSuppServiceNotification notification = new MtkSuppServiceNotification();
        notification.notificationType = suppSvcNotification.isMT ? 1 : 0;
        notification.code = suppSvcNotification.code;
        notification.index = suppSvcNotification.index;
        notification.type = suppSvcNotification.type;
        notification.number = suppSvcNotification.number;
        this.mMtkRil.unsljLogRet(3026, null);
        if (this.mMtkRil.mSsnExRegistrant != null) {
            this.mMtkRil.mSsnExRegistrant.notifyRegistrant(new AsyncResult((Object) null, notification, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void crssIndication(int indicationType, CrssNotification crssNotification) {
        this.mMtkRil.processIndication(indicationType);
        MtkSuppCrssNotification notification = new MtkSuppCrssNotification();
        notification.code = crssNotification.code;
        notification.type = crssNotification.type;
        notification.alphaid = crssNotification.alphaid;
        notification.number = crssNotification.number;
        notification.cli_validity = crssNotification.cli_validity;
        this.mMtkRil.unsljLogRet(3025, null);
        if (this.mMtkRil.mCallRelatedSuppSvcRegistrant != null) {
            this.mMtkRil.mCallRelatedSuppSvcRegistrant.notifyRegistrant(new AsyncResult((Object) null, notification, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void eccNumIndication(int indicationType, String eccListWithCard, String eccListNoCard) {
    }

    private int getSubId(int phoneId) {
        int[] subIds = SubscriptionManager.getSubId(phoneId);
        if (subIds == null || subIds.length <= 0) {
            return -1;
        }
        return subIds[0];
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void responseCsNetworkStateChangeInd(int indicationType, ArrayList<String> state) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.riljLog("[UNSL]< UNSOL_RESPONSE_CS_NETWORK_STATE_CHANGED");
        if (this.mMtkRil.mCsNetworkStateRegistrants.size() != 0) {
            this.mMtkRil.mCsNetworkStateRegistrants.notifyRegistrants(new AsyncResult((Object) null, state.toArray(new String[state.size()]), (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void responsePsNetworkStateChangeInd(int indicationType, ArrayList<Integer> state) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.riljLog("[UNSL]< UNSOL_RESPONSE_PS_NETWORK_STATE_CHANGED");
        int[] response = new int[state.size()];
        for (int i = 0; i < state.size(); i++) {
            response[i] = state.get(i).intValue();
        }
        if (this.mMtkRil.mPsNetworkStateRegistrants.size() != 0) {
            this.mMtkRil.mPsNetworkStateRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void responseNetworkEventInd(int indicationType, ArrayList<Integer> event) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = new int[event.size()];
        for (int i = 0; i < event.size(); i++) {
            response[i] = event.get(i).intValue();
        }
        this.mMtkRil.unsljLogRet(3018, response);
        if (this.mMtkRil.mNetworkEventRegistrants.size() != 0) {
            this.mMtkRil.mNetworkEventRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void networkRejectCauseInd(int indicationType, ArrayList<Integer> event) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = new int[event.size()];
        for (int i = 0; i < event.size(); i++) {
            response[i] = event.get(i).intValue();
        }
        this.mMtkRil.unsljLogRet(3109, response);
        if (this.mMtkRil.mNetworkRejectRegistrants.size() != 0) {
            this.mMtkRil.mNetworkRejectRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void responseModulationInfoInd(int indicationType, ArrayList<Integer> data) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            response[i] = data.get(i).intValue();
        }
        this.mMtkRil.unsljLogRet(3019, response);
        if (this.mMtkRil.mModulationRegistrants.size() != 0) {
            this.mMtkRil.mModulationRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void responseInvalidSimInd(int indicationType, ArrayList<String> state) {
        this.mMtkRil.processIndication(indicationType);
        String[] ret = (String[]) state.toArray(new String[state.size()]);
        this.mMtkRil.unsljLogRet(3016, ret);
        if (this.mMtkRil.mInvalidSimInfoRegistrant.size() != 0) {
            this.mMtkRil.mInvalidSimInfoRegistrant.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void responseFemtocellInfo(int indicationType, ArrayList<String> info) {
        this.mMtkRil.processIndication(indicationType);
        String[] response = (String[]) info.toArray(new String[info.size()]);
        this.mMtkRil.unsljLogRet(3029, response);
        if (this.mMtkRil.mFemtoCellInfoRegistrants.size() != 0) {
            this.mMtkRil.mFemtoCellInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void currentSignalStrengthWithWcdmaEcioInd(int indicationType, SignalStrengthWithWcdmaEcio signalStrength) {
        this.mMtkRil.processIndication(indicationType);
        SignalStrength ss = new SignalStrength();
        this.mMtkRil.unsljLogRet(3097, ss);
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.riljLog("currentSignalStrengthWithWcdmaEcioInd SignalStrength=" + ss);
        if (this.mMtkRil.mSignalStrengthWithWcdmaEcioRegistrants.size() != 0) {
            this.mMtkRil.mSignalStrengthWithWcdmaEcioRegistrants.notifyRegistrants(new AsyncResult((Object) null, ss, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void responseLteNetworkInfo(int indicationType, int info) {
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.riljLog("[UNSL]< RIL_UNSOL_LTE_NETWORK_INFO " + info);
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onMccMncChanged(int indicationType, String mccmnc) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.unsljLogRet(3096, mccmnc);
        if (this.mMtkRil.mMccMncRegistrants.size() != 0) {
            this.mMtkRil.mMccMncRegistrants.notifyRegistrants(new AsyncResult((Object) null, mccmnc, (Throwable) null));
        }
    }

    @Override // com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onVirtualSimOn(int indicationType, int simInserted) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3005);
        }
        if (this.mMtkRil.mVirtualSimOn != null) {
            this.mMtkRil.mVirtualSimOn.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(simInserted), (Throwable) null));
        }
    }

    @Override // com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onVirtualSimOff(int indicationType, int simInserted) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3006);
        }
        if (this.mMtkRil.mVirtualSimOff != null) {
            this.mMtkRil.mVirtualSimOn.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(simInserted), (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onVirtualSimStatusChanged(int indicationType, int simInserted) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3129);
        }
        if (simInserted == 0 && this.mMtkRil.mVirtualSimOff != null) {
            this.mMtkRil.mVirtualSimOff.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(simInserted), (Throwable) null));
        } else if (simInserted == 1 && this.mMtkRil.mVirtualSimOn != null) {
            this.mMtkRil.mVirtualSimOn.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(simInserted), (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onImeiLock(int indicationType) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3007);
        }
        if (this.mMtkRil.mImeiLockRegistrant != null) {
            this.mMtkRil.mImeiLockRegistrant.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onImsiRefreshDone(int indicationType) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3008);
        }
        if (this.mMtkRil.mImsiRefreshDoneRegistrant != null) {
            this.mMtkRil.mImsiRefreshDoneRegistrant.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onCardDetectedInd(int indicationType) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3125);
        }
        if (this.mMtkRil.mCardDetectedIndRegistrant.size() != 0) {
            this.mMtkRil.mCardDetectedIndRegistrant.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            return;
        }
        if (ENG) {
            this.mMtkRil.riljLog("Cache card detected event");
        }
        this.mMtkRil.mIsCardDetected = true;
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void newEtwsInd(int indicationType, EtwsNotification etws) {
        this.mMtkRil.processIndication(indicationType);
        MtkEtwsNotification response = new MtkEtwsNotification();
        response.messageId = etws.messageId;
        response.serialNumber = etws.serialNumber;
        response.warningType = etws.warningType;
        response.plmnId = etws.plmnId;
        response.securityInfo = etws.securityInfo;
        if (ENG) {
            this.mMtkRil.unsljLogRet(3010, response);
        }
        if (this.mMtkRil.mEtwsNotificationRegistrant != null) {
            this.mMtkRil.mEtwsNotificationRegistrant.notifyRegistrant(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void meSmsStorageFullInd(int indicationType) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3011);
        }
        if (this.mMtkRil.mMeSmsFullRegistrant != null) {
            this.mMtkRil.mMeSmsFullRegistrant.notifyRegistrant();
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void smsReadyInd(int indicationType) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3012);
        }
        if (this.mMtkRil.mSmsReadyRegistrants.size() != 0) {
            this.mMtkRil.mSmsReadyRegistrants.notifyRegistrants();
            return;
        }
        if (ENG) {
            this.mMtkRil.riljLog("Cache sms ready event");
        }
        this.mMtkRil.mIsSmsReady = true;
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void dataAllowedNotification(int indicationType, int isAllowed) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = {isAllowed};
        this.mMtkRil.unsljLogMore(3014, isAllowed == 1 ? "true" : "false");
        if (this.mMtkRil.mDataAllowedRegistrants != null) {
            this.mMtkRil.mDataAllowedRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onPseudoCellInfoInd(int indicationType, ArrayList<Integer> info) {
        PseudoCellInfo cellInfo;
        try {
            this.mMtkRil.processIndication(indicationType);
            if (ENG) {
                this.mMtkRil.unsljLog(3017);
            }
            int[] response = new int[info.size()];
            for (int i = 0; i < info.size(); i++) {
                response[i] = info.get(i).intValue();
            }
            boolean enable = true;
            String propStr = SystemProperties.get(String.format("persist.vendor.radio.apc.mode%d", this.mMtkRil.mInstanceId), "0");
            int index = propStr.indexOf("=");
            if (index != -1) {
                String[] settings = propStr.substring(index + 1).split(",");
                int mode = Integer.parseInt(settings[0]);
                if (Integer.parseInt(settings[1]) != 1) {
                    enable = false;
                }
                cellInfo = new PseudoCellInfo(mode, enable, Integer.parseInt(settings[2]), response);
            } else {
                cellInfo = new PseudoCellInfo(0, false, 0, response);
            }
            if (this.mMtkRil.mPseudoCellInfoRegistrants != null) {
                this.mMtkRil.mPseudoCellInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, cellInfo, (Throwable) null));
            }
            Intent intent = new Intent("com.mediatek.phone.ACTION_APC_INFO_NOTIFY");
            intent.putExtra("phoneId", this.mMtkRil.mInstanceId);
            intent.putExtra("info", (Parcelable) cellInfo);
            this.mMtkRil.mMtkContext.sendBroadcast(intent);
            this.mMtkRil.riljLog("Broadcast for APC info:cellInfo=" + cellInfo.toString());
            Intent fakeBSIntent = new Intent("oppo.intent.action.FAKE_BS_BLOCKED");
            fakeBSIntent.setPackage("com.coloros.blacklistapp");
            fakeBSIntent.putExtra("arfcn", cellInfo.getArfcn(0));
            this.mMtkRil.mMtkContext.sendBroadcast(fakeBSIntent, "oppo.permission.OPPO_COMPONENT_SAFE");
            this.mMtkRil.riljLog("fake BS blocked, arfcn:" + cellInfo.getArfcn(0));
        } catch (NumberFormatException e) {
            this.mMtkRil.riljLog("Broadcast for APC info:cellInfo=" + e.toString());
        } catch (Exception e2) {
            this.mMtkRil.riljLog("Broadcast for APC info:cellInfo=" + e2.toString());
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void eMBMSSessionStatusIndication(int indicationType, int status) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = {status};
        this.mMtkRil.unsljLogRet(3054, null);
        if (this.mMtkRil.mEmbmsSessionStatusNotificationRegistrant.size() > 0) {
            this.mMtkRil.riljLog("Notify mEmbmsSessionStatusNotificationRegistrant");
            this.mMtkRil.mEmbmsSessionStatusNotificationRegistrant.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        } else {
            this.mMtkRil.riljLog("No mEmbmsSessionStatusNotificationRegistrant exist");
        }
        Intent intent = new Intent("com.mediatek.intent.action.EMBMS_SESSION_STATUS_CHANGED");
        intent.putExtra("isActived", status);
        this.mMtkRil.mMtkContext.sendBroadcast(intent);
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onSimHotSwapInd(int indicationType, int event, String info) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            MtkRIL mtkRIL = this.mMtkRil;
            mtkRIL.riljLog("onSimHotSwapInd event: " + event + " info: " + info);
        }
        if (event != 0) {
            if (event != 1) {
                if (event != 2) {
                    if (event != 3) {
                        if (event != 4) {
                            if (event != 6) {
                                this.mMtkRil.riljLog("onSimHotSwapInd Invalid event!");
                            } else if (this.mMtkRil.mSimCommonSlotNoChanged != null) {
                                this.mMtkRil.mSimCommonSlotNoChanged.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
                            }
                        } else if (this.mMtkRil.mSimTrayPlugIn != null) {
                            this.mMtkRil.mSimTrayPlugIn.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
                        }
                    } else if (this.mMtkRil.mSimMissing != null) {
                        this.mMtkRil.mSimMissing.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
                    }
                } else if (this.mMtkRil.mSimRecovery != null) {
                    this.mMtkRil.mSimRecovery.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
                }
            } else if (this.mMtkRil.mSimPlugOut != null) {
                this.mMtkRil.mSimPlugOut.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        } else if (this.mMtkRil.mSimPlugIn != null) {
            this.mMtkRil.mSimPlugIn.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onSimPowerChangedInd(int indicationType, ArrayList<Integer> info) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3124);
        }
        int[] response = new int[info.size()];
        for (int i = 0; i < info.size(); i++) {
            response[i] = info.get(i).intValue();
        }
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.mSimPowerInfo = response;
        if (mtkRIL.mSimPowerChanged.size() != 0) {
            this.mMtkRil.mSimPowerChanged.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void smlSlotLockInfoChangedInd(int indicationType, ArrayList<Integer> info) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3115);
        }
        int[] response = new int[info.size()];
        for (int i = 0; i < info.size(); i++) {
            response[i] = info.get(i).intValue();
        }
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.mSmlSlotLockInfo = response;
        if (mtkRIL.mSmlSlotLockInfoChanged.size() != 0) {
            this.mMtkRil.mSmlSlotLockInfoChanged.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onRsuSimLockEvent(int indicationType, int eventId) {
        this.mMtkRil.processIndication(indicationType);
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.riljLog("[RSU-SIMLOCK] onRsuSimLockEvent eventId " + eventId);
        if (ENG) {
            this.mMtkRil.unsljLog(3128);
        }
        int[] response = {eventId};
        if (this.mMtkRil.mRsuSimlockRegistrants != null) {
            this.mMtkRil.mRsuSimlockRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void eMBMSAtInfoIndication(int indicationType, String info) {
        this.mMtkRil.processIndication(indicationType);
        String response = new String(info);
        this.mMtkRil.unsljLogRet(3055, response);
        if (this.mMtkRil.mEmbmsAtInfoNotificationRegistrant.size() > 0) {
            this.mMtkRil.riljLog("Notify mEmbmsAtInfoNotificationRegistrant");
            this.mMtkRil.mEmbmsAtInfoNotificationRegistrant.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
            return;
        }
        this.mMtkRil.riljLog("No mEmbmsAtInfoNotificationRegistrant exist");
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void plmnChangedIndication(int indicationType, ArrayList<String> plmns) {
        this.mMtkRil.processIndication(indicationType);
        String[] response = new String[plmns.size()];
        for (int i = 0; i < plmns.size(); i++) {
            response[i] = plmns.get(i);
        }
        this.mMtkRil.unsljLogRet(3000, response);
        synchronized (this.mMtkRil.mWPMonitor) {
            if (this.mMtkRil.mPlmnChangeNotificationRegistrant.size() > 0) {
                this.mMtkRil.riljLog("ECOPS,notify mPlmnChangeNotificationRegistrant");
                this.mMtkRil.mPlmnChangeNotificationRegistrant.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
            } else {
                this.mMtkRil.mEcopsReturnValue = response;
            }
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void registrationSuspendedIndication(int indicationType, ArrayList<Integer> sessionIds) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = new int[sessionIds.size()];
        for (int i = 0; i < sessionIds.size(); i++) {
            response[i] = sessionIds.get(i).intValue();
        }
        this.mMtkRil.unsljLogRet(3001, response);
        synchronized (this.mMtkRil.mWPMonitor) {
            if (this.mMtkRil.mRegistrationSuspendedRegistrant != null) {
                this.mMtkRil.riljLog("EMSR, notify mRegistrationSuspendedRegistrant");
                this.mMtkRil.mRegistrationSuspendedRegistrant.notifyRegistrant(new AsyncResult((Object) null, response, (Throwable) null));
            } else {
                this.mMtkRil.mEmsrReturnValue = response;
            }
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void gmssRatChangedIndication(int indicationType, ArrayList<Integer> gmsss) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = new int[gmsss.size()];
        for (int i = 0; i < gmsss.size(); i++) {
            response[i] = gmsss.get(i).intValue();
        }
        this.mMtkRil.unsljLogRet(3003, response);
        int[] rat = response;
        if (this.mMtkRil.mGmssRatChangedRegistrant != null) {
            this.mMtkRil.mGmssRatChangedRegistrant.notifyRegistrants(new AsyncResult((Object) null, rat, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void worldModeChangedIndication(int indicationType, ArrayList<Integer> modes) {
        boolean retvalue;
        this.mMtkRil.processIndication(indicationType);
        int[] response = new int[modes.size()];
        for (int i = 0; i < modes.size(); i++) {
            response[i] = modes.get(i).intValue();
        }
        this.mMtkRil.unsljLogRet(3022, response);
        int state = response[0];
        if (state == 2) {
            retvalue = WorldMode.resetSwitchingState(state);
            state = 1;
        } else if (state == 0) {
            retvalue = WorldMode.updateSwitchingState(true);
        } else {
            retvalue = WorldMode.updateSwitchingState(false);
        }
        if (retvalue) {
            Intent intent = new Intent(WorldMode.ACTION_WORLD_MODE_CHANGED);
            intent.putExtra(WorldMode.EXTRA_WORLD_MODE_CHANGE_STATE, Integer.valueOf(state));
            this.mMtkRil.mMtkContext.sendBroadcast(intent);
            MtkRIL mtkRIL = this.mMtkRil;
            mtkRIL.riljLog("Broadcast for WorldModeChanged: state=" + state);
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void resetAttachApnInd(int indicationType) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3020);
        }
        if (this.mMtkRil.mResetAttachApnRegistrants != null) {
            this.mMtkRil.mResetAttachApnRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void mdChangedApnInd(int indicationType, int apnClassType) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3021);
        }
        if (this.mMtkRil.mAttachApnChangedRegistrants != null) {
            this.mMtkRil.mAttachApnChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(apnClassType), (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void esnMeidChangeInd(int indicationType, String esnMeid) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3023);
        }
        if (this.mMtkRil.mCDMACardEsnMeidRegistrant != null) {
            this.mMtkRil.mCDMACardEsnMeidRegistrant.notifyRegistrant(new AsyncResult((Object) null, esnMeid, (Throwable) null));
            return;
        }
        if (ENG) {
            this.mMtkRil.riljLog("Cache esnMeidChangeInd");
        }
        this.mMtkRil.mEspOrMeid = esnMeid;
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void phbReadyNotification(int indicationType, int isPhbReady) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = {isPhbReady};
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.unsljLogMore(3028, "phbReadyNotification: " + isPhbReady);
        if (this.mMtkRil.mPhbReadyRegistrants != null) {
            this.mMtkRil.mPhbReadyRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void bipProactiveCommand(int indicationType, String cmd) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3057);
        }
        if (this.mMtkRil.mBipProCmdRegistrant != null) {
            this.mMtkRil.mBipProCmdRegistrant.notifyRegistrants(new AsyncResult((Object) null, cmd, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void triggerOtaSP(int indicationType) {
        this.mMtkRil.invokeOemRilRequestStrings(new String[]{"AT+CDV=*22899", "", "DESTRILD:C2K"}, null);
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void pcoDataAfterAttached(int indicationType, PcoDataAttachedInfo pco) {
        this.mMtkRil.processIndication(indicationType);
        PcoDataAfterAttached response = new PcoDataAfterAttached(pco.cid, pco.apnName, pco.bearerProto, pco.pcoId, RIL.arrayListToPrimitiveArray(pco.contents));
        this.mMtkRil.unsljLogRet(3053, response);
        this.mMtkRil.mPcoDataAfterAttachedRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onStkMenuReset(int indicationType) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3071);
        }
        if (this.mMtkRil.mStkSetupMenuResetRegistrant != null) {
            this.mMtkRil.mStkSetupMenuResetRegistrant.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onLteAccessStratumStateChanged(int indicationType, ArrayList<Integer> state) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = new int[state.size()];
        for (int i = 0; i < state.size(); i++) {
            response[i] = state.get(i).intValue();
        }
        this.mMtkRil.unsljLogRet(3062, response);
        if (this.mMtkRil.mLteAccessStratumStateRegistrants != null) {
            this.mMtkRil.mLteAccessStratumStateRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void networkInfoInd(int indicationType, ArrayList<String> networkinfo) {
        this.mMtkRil.processIndication(indicationType);
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.unsljLogMore(3030, "networkInfo: " + networkinfo);
        String[] ret = (String[]) networkinfo.toArray(new String[networkinfo.size()]);
        if (this.mMtkRil.mNetworkInfoRegistrant.size() != 0) {
            this.mMtkRil.mNetworkInfoRegistrant.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
        OppoTelephonyFactory.getInstance().getFeature(IOppoNetworkManager.DEFAULT, new Object[0]).oppoProcessUnsolOemKeyLogErrMsg(this.mMtkRil.mMtkContext, this.mMtkRil.mInstanceId.intValue(), ret);
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onMdDataRetryCountReset(int indicationType) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3059);
        }
        if (this.mMtkRil.mMdDataRetryCountResetRegistrants != null) {
            this.mMtkRil.mMdDataRetryCountResetRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onRemoveRestrictEutran(int indicationType) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3060);
        }
        if (this.mMtkRil.mRemoveRestrictEutranRegistrants != null) {
            this.mMtkRil.mRemoveRestrictEutranRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void confSRVCC(int indicationType, ArrayList<Integer> callIds) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.unsljLog(3072);
        int[] response = new int[callIds.size()];
        for (int i = 0; i < callIds.size(); i++) {
            response[i] = callIds.get(i).intValue();
        }
        this.mMtkRil.mEconfSrvccRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onVsimEventIndication(int indicationType, VsimOperationEvent event) {
        this.mMtkRil.processIndication(indicationType);
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.unsljLogRet(3074, "len=" + new Integer(event.dataLength));
        int length = event.dataLength > 0 ? (event.dataLength / 2) + 4 : 0;
        ExternalSimManager.VsimEvent indicationEvent = new ExternalSimManager.VsimEvent(event.transactionId, event.eventId, length, 1 << this.mMtkRil.mInstanceId.intValue());
        if (length > 0) {
            indicationEvent.putInt(event.dataLength / 2);
            indicationEvent.putBytes(IccUtils.hexStringToBytes(event.data));
        }
        if (ENG) {
            this.mMtkRil.unsljLogRet(3074, indicationEvent.toString());
        }
        if (this.mMtkRil.mVsimIndicationRegistrants != null) {
            this.mMtkRil.mVsimIndicationRegistrants.notifyRegistrants(new AsyncResult((Object) null, indicationEvent, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void dedicatedBearerActivationInd(int indicationType, DedicateDataCall ddcResult) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.unsljLog(3082);
        MtkDedicateDataCallResponse ret = this.mMtkRil.convertDedicatedDataCallResult(ddcResult);
        this.mMtkRil.riljLog(ret.toString());
        if (this.mMtkRil.mDedicatedBearerActivedRegistrants != null) {
            this.mMtkRil.mDedicatedBearerActivedRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void dedicatedBearerModificationInd(int indicationType, DedicateDataCall ddcResult) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.unsljLog(3083);
        MtkDedicateDataCallResponse ret = this.mMtkRil.convertDedicatedDataCallResult(ddcResult);
        this.mMtkRil.riljLog(ret.toString());
        if (this.mMtkRil.mDedicatedBearerModifiedRegistrants != null) {
            this.mMtkRil.mDedicatedBearerModifiedRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void dedicatedBearerDeactivationInd(int indicationType, int cid) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.unsljLog(3084);
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.riljLog("dedicatedBearerDeactivationInd, cid: " + cid);
        if (this.mMtkRil.mDedicatedBearerDeactivatedRegistrants != null) {
            this.mMtkRil.mDedicatedBearerDeactivatedRegistrants.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(cid), (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void oemHookRaw(int indicationType, ArrayList<Byte> data) {
        this.mMtkRil.processIndication(indicationType);
        byte[] response = RIL.arrayListToPrimitiveArray(data);
        this.mMtkRil.unsljLogvRet(1028, IccUtils.bytesToHexString(response));
        if (this.mMtkRil.mUnsolOemHookRegistrant != null) {
            this.mMtkRil.mUnsolOemHookRegistrant.notifyRegistrant(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onTxPowerIndication(int indicationType, ArrayList<Integer> txPower) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = new int[txPower.size()];
        for (int i = 0; i < txPower.size(); i++) {
            response[i] = txPower.get(i).intValue();
        }
        if (this.mMtkRil.mTxPowerRegistrant != null) {
            this.mMtkRil.mTxPowerRegistrant.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onTxPowerStatusIndication(int indicationType, ArrayList<Integer> txPower) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = new int[txPower.size()];
        for (int i = 0; i < txPower.size(); i++) {
            response[i] = txPower.get(i).intValue();
        }
        if (this.mMtkRil.mTxPowerStatusRegistrant != null) {
            this.mMtkRil.mTxPowerStatusRegistrant.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void dsbpStateChanged(int indicationType, int dsbpState) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3114);
        }
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.riljLog("dsbpStateChanged state: " + dsbpState);
        if (this.mMtkRil.mDsbpStateRegistrant != null) {
            this.mMtkRil.mDsbpStateRegistrant.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(dsbpState), (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onDsdaChangedInd(int indicationType, int mode) {
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3131);
        }
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.riljLog("onDsdaChangedInd: mode=" + mode);
        if (this.mMtkRil.mDsdaStateRegistrant != null) {
            this.mMtkRil.mDsdaStateRegistrant.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(mode), (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void qualifiedNetworkTypesChangedInd(int indicationType, ArrayList<Integer> data) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            response[i] = data.get(i).intValue();
        }
        this.mMtkRil.unsljLogRet(3130, response);
        if (this.mMtkRil.mQualifiedNetworkTypesRegistrant != null) {
            this.mMtkRil.mQualifiedNetworkTypesRegistrant.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onCellularQualityChangedInd(int indicationType, ArrayList<Integer> indStgs) {
        int CellularQualityType;
        this.mMtkRil.processIndication(indicationType);
        if (ENG) {
            this.mMtkRil.unsljLog(3132);
        }
        int[] data = new int[indStgs.size()];
        for (int i = 0; i < indStgs.size(); i++) {
            data[i] = indStgs.get(i).intValue();
        }
        int i2 = data[0];
        if (i2 == 0) {
            CellularQualityType = 0;
        } else if (i2 == 1) {
            CellularQualityType = 5;
        } else if (i2 == 2) {
            CellularQualityType = 2;
        } else if (i2 == 3) {
            CellularQualityType = 6;
        } else if (i2 != 4) {
            CellularQualityType = 0;
        } else {
            CellularQualityType = 7;
        }
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.riljLog("RIL_UNSOL_IWLAN_CELLULAR_QUALITY_CHANGED_IND type:" + CellularQualityType + " , value = " + data[1]);
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void mobileDataUsageInd(int indicationType, ArrayList<Integer> data) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            response[i] = data.get(i).intValue();
        }
        this.mMtkRil.unsljLogRet(3133, response);
        if (this.mMtkRil.mMobileDataUsageRegistrants != null) {
            this.mMtkRil.mMobileDataUsageRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void smlDeviceLockInfoChangedInd(int indicationType, ArrayList<Integer> info) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.unsljLog(3201);
        this.mMtkRil.riljLog("smlDeviceLockInfoChangedInd");
        int[] response = new int[info.size()];
        for (int i = 0; i < info.size(); i++) {
            response[i] = info.get(i).intValue();
        }
        MtkRIL ril = this.mMtkRil;
        ril.mSmlDeviceLockInfo = response;
        if (ril.mSmlDeviceLockInfoChanged.size() != 0) {
            ril.mSmlDeviceLockInfoChanged.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void smlEncryptedSerialIdInd(int indicationType, ArrayList<String> data) {
        this.mMtkRil.processIndication(indicationType);
        this.mMtkRil.unsljLog(3202);
        MtkRIL mtkRIL = this.mMtkRil;
        mtkRIL.riljLog("smlEncryptedSerialIdInd, data size: " + data.size());
        if (((String[]) data.toArray(new String[data.size()])).length == 2) {
            String serialId = Build.getSerial();
            MtkRIL mtkRIL2 = this.mMtkRil;
            mtkRIL2.riljLog("smlEncryptedSerialIdInd, the serialNo : " + serialId);
            if (serialId != null && serialId.length() > 10) {
                byte[] serialNo = serialId.substring(0, 10).getBytes();
                MtkRIL mtkRIL3 = this.mMtkRil;
                mtkRIL3.riljLog("smlEncryptedSerialIdInd, the serialNo length is : " + serialNo.length);
                OppoEngineerManager.saveEngineerData(1000065, serialNo, serialNo.length);
            } else if (!"unknown".equals(serialId)) {
                byte[] serialNo2 = serialId.getBytes();
                OppoEngineerManager.saveEngineerData(1000065, serialNo2, serialNo2.length);
            }
        }
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication, com.mediatek.internal.telephony.MtkRadioExIndicationBase
    public void onNwLimitInd(int indicationType, ArrayList<Integer> state) {
        this.mMtkRil.processIndication(indicationType);
        int[] response = new int[state.size()];
        for (int i = 0; i < state.size(); i++) {
            response[i] = state.get(i).intValue();
        }
        this.mMtkRil.unsljLogRet(3134, response);
        if (this.mMtkRil.mNwLimitRegistrants != null) {
            this.mMtkRil.mNwLimitRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
        }
    }
}
