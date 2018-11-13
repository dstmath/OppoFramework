package com.android.internal.telephony;

import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.CellInfo;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoLteServiceState;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.ITelephonyRegistry.Stub;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneInternalInterface.DataActivityState;
import com.android.internal.telephony.uicc.SpnOverride;
import java.util.List;

public class DefaultPhoneNotifier implements PhoneNotifier {
    /* renamed from: -com-android-internal-telephony-Call$StateSwitchesValues */
    private static final /* synthetic */ int[] f6-com-android-internal-telephony-Call$StateSwitchesValues = null;
    /* renamed from: -com-android-internal-telephony-PhoneInternalInterface$DataActivityStateSwitchesValues */
    private static final /* synthetic */ int[] f7xb8401fb4 = null;
    private static final boolean DBG = false;
    private static final String LOG_TAG = "DefaultPhoneNotifier";
    protected ITelephonyRegistry mRegistry = Stub.asInterface(ServiceManager.getService("telephony.registry"));

    /* renamed from: -getcom-android-internal-telephony-Call$StateSwitchesValues */
    private static /* synthetic */ int[] m7-getcom-android-internal-telephony-Call$StateSwitchesValues() {
        if (f6-com-android-internal-telephony-Call$StateSwitchesValues != null) {
            return f6-com-android-internal-telephony-Call$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ACTIVE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.ALERTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DIALING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.DISCONNECTED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.HOLDING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.IDLE.ordinal()] = 13;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.INCOMING.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.WAITING.ordinal()] = 8;
        } catch (NoSuchFieldError e9) {
        }
        f6-com-android-internal-telephony-Call$StateSwitchesValues = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-PhoneInternalInterface$DataActivityStateSwitchesValues */
    private static /* synthetic */ int[] m8x2c473d58() {
        if (f7xb8401fb4 != null) {
            return f7xb8401fb4;
        }
        int[] iArr = new int[DataActivityState.values().length];
        try {
            iArr[DataActivityState.DATAIN.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DataActivityState.DATAINANDOUT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DataActivityState.DATAOUT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DataActivityState.DORMANT.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[DataActivityState.NONE.ordinal()] = 13;
        } catch (NoSuchFieldError e5) {
        }
        f7xb8401fb4 = iArr;
        return iArr;
    }

    public void notifyPhoneState(Phone sender) {
        if (sender.getState() == PhoneConstants.State.RINGING) {
            Rlog.d(LOG_TAG, "ringing state notify move to oppo/phone/Telecom/PhoneStateBroadcaster,so this return!");
            return;
        }
        Call ringingCall = sender.getRingingCall();
        int subId = sender.getSubId();
        int phoneId = sender.getPhoneId();
        String incomingNumber = SpnOverride.MVNO_TYPE_NONE;
        if (!(ringingCall == null || ringingCall.getEarliestConnection() == null)) {
            incomingNumber = ringingCall.getEarliestConnection().getAddress();
        }
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyCallStateForPhoneId(phoneId, subId, PhoneConstantConversions.convertCallState(sender.getState()), incomingNumber);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyServiceState(Phone sender) {
        ServiceState ss = sender.getServiceState();
        if (!(sender == null || sender.getDefaultPhone() == null || !sender.isWifiCallingEnabled())) {
            ServiceStateTracker sst = sender.getDefaultPhone().getServiceStateTracker();
            if (sst != null) {
                ss = sst.mSS;
                Rlog.d(LOG_TAG, "notifyServiceState: use unmerged servicestate when vowifi registered");
            }
        }
        int phoneId = sender.getPhoneId();
        int subId = sender.getSubId();
        Rlog.d(LOG_TAG, "nofityServiceState: mRegistry=" + this.mRegistry + " ss=" + ss + " sender=" + sender + " phondId=" + phoneId + " subId=" + subId);
        if (ss == null) {
            ss = new ServiceState();
            ss.setStateOutOfService();
        }
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyServiceStateForPhoneId(phoneId, subId, ss);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifySignalStrength(Phone sender) {
        int phoneId = sender.getPhoneId();
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifySignalStrengthForPhoneId(phoneId, subId, sender.getSignalStrength());
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyMessageWaitingChanged(Phone sender) {
        int phoneId = sender.getPhoneId();
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyMessageWaitingChangedForPhoneId(phoneId, subId, sender.getMessageWaitingIndicator());
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyCallForwardingChanged(Phone sender) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                Rlog.d(LOG_TAG, "notifyCallForwardingChanged: subId=" + subId + ", isCFActive=" + sender.getCallForwardingIndicator());
                this.mRegistry.notifyCallForwardingChangedForSubscriber(subId, sender.getCallForwardingIndicator());
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyDataActivity(Phone sender) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyDataActivityForSubscriber(subId, convertDataActivityState(sender.getDataActivityState()));
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyDataConnection(Phone sender, String reason, String apnType, DataState state) {
        doNotifyDataConnection(sender, reason, apnType, state);
    }

    private void doNotifyDataConnection(Phone sender, String reason, String apnType, DataState state) {
        int subId = sender.getSubId();
        long dds = (long) SubscriptionManager.getDefaultDataSubscriptionId();
        TelephonyManager telephony = TelephonyManager.getDefault();
        LinkProperties linkProperties = null;
        NetworkCapabilities networkCapabilities = null;
        boolean roaming = false;
        if (state == DataState.CONNECTED) {
            linkProperties = sender.getLinkProperties(apnType);
            networkCapabilities = sender.getNetworkCapabilities(apnType);
        }
        ServiceState ss = sender.getServiceState();
        if (ss != null) {
            roaming = ss.getDataRoaming();
        }
        try {
            if (this.mRegistry != null) {
                int dataNetworkType;
                ITelephonyRegistry iTelephonyRegistry = this.mRegistry;
                int convertDataState = PhoneConstantConversions.convertDataState(state);
                boolean isDataAllowed = sender.isDataAllowed();
                String activeApnHost = sender.getActiveApnHost(apnType);
                if (telephony != null) {
                    dataNetworkType = telephony.getDataNetworkType(subId);
                } else {
                    dataNetworkType = 0;
                }
                iTelephonyRegistry.notifyDataConnectionForSubscriber(subId, convertDataState, isDataAllowed, reason, activeApnHost, apnType, linkProperties, networkCapabilities, dataNetworkType, roaming);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyDataConnectionFailed(Phone sender, String reason, String apnType) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyDataConnectionFailedForSubscriber(subId, reason, apnType);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyCellLocation(Phone sender) {
        int subId = sender.getSubId();
        Bundle data = new Bundle();
        sender.getCellLocation().fillInNotifierBundle(data);
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyCellLocationForSubscriber(subId, data);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyCellInfo(Phone sender, List<CellInfo> cellInfo) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyCellInfoForSubscriber(subId, cellInfo);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyOtaspChanged(Phone sender, int otaspMode) {
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyOtaspChanged(otaspMode);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyPreciseCallState(Phone sender) {
        Call ringingCall = sender.getRingingCall();
        Call foregroundCall = sender.getForegroundCall();
        Call backgroundCall = sender.getBackgroundCall();
        if (ringingCall != null && foregroundCall != null && backgroundCall != null) {
            try {
                this.mRegistry.notifyPreciseCallState(convertPreciseCallState(ringingCall.getState()), convertPreciseCallState(foregroundCall.getState()), convertPreciseCallState(backgroundCall.getState()));
            } catch (RemoteException e) {
            }
        }
    }

    public void notifyDisconnectCause(int cause, int preciseCause) {
        try {
            this.mRegistry.notifyDisconnectCause(cause, preciseCause);
        } catch (RemoteException e) {
        }
    }

    public void notifyPreciseDataConnectionFailed(Phone sender, String reason, String apnType, String apn, String failCause) {
        try {
            this.mRegistry.notifyPreciseDataConnectionFailed(reason, apnType, apn, failCause);
        } catch (RemoteException e) {
        }
    }

    public void notifyVoLteServiceStateChanged(Phone sender, VoLteServiceState lteState) {
        if (sender != null) {
            try {
                if (this.mRegistry != null) {
                    int phoneId = sender.getPhoneId();
                    int subId = sender.getSubId();
                    Rlog.d(LOG_TAG, "notifyVoLteServiceStateChanged: mRegistry=" + this.mRegistry + " lteState=" + lteState + " sender=" + sender + " phondId=" + phoneId + " subId=" + subId);
                    this.mRegistry.notifyVoLteServiceStateChanged(phoneId, subId, lteState);
                }
            } catch (RemoteException e) {
            }
        }
    }

    public void notifyDataActivationStateChanged(Phone sender, int activationState) {
        try {
            this.mRegistry.notifySimActivationStateChangedForPhoneId(sender.getPhoneId(), sender.getSubId(), 1, activationState);
        } catch (RemoteException e) {
        }
    }

    public void notifyVoiceActivationStateChanged(Phone sender, int activationState) {
        try {
            this.mRegistry.notifySimActivationStateChangedForPhoneId(sender.getPhoneId(), sender.getSubId(), 0, activationState);
        } catch (RemoteException e) {
        }
    }

    public void notifyOemHookRawEventForSubscriber(int subId, byte[] rawData) {
        try {
            this.mRegistry.notifyOemHookRawEventForSubscriber(subId, rawData);
        } catch (RemoteException e) {
        }
    }

    public static int convertDataActivityState(DataActivityState state) {
        switch (m8x2c473d58()[state.ordinal()]) {
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
                return 2;
            case 4:
                return 4;
            default:
                return 0;
        }
    }

    public static int convertPreciseCallState(State state) {
        switch (m7-getcom-android-internal-telephony-Call$StateSwitchesValues()[state.ordinal()]) {
            case 1:
                return 1;
            case 2:
                return 4;
            case 3:
                return 3;
            case 4:
                return 7;
            case 5:
                return 8;
            case 6:
                return 2;
            case 7:
                return 5;
            case 8:
                return 6;
            default:
                return 0;
        }
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
