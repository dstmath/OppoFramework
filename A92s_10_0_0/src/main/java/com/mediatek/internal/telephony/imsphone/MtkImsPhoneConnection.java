package com.mediatek.internal.telephony.imsphone;

import android.net.Uri;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telecom.ConferenceParticipant;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.text.TextUtils;
import com.android.ims.ImsCall;
import com.android.ims.ImsException;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.imsphone.ImsPhoneCall;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.imsphone.ImsPhoneConnection;
import com.mediatek.ims.MtkImsCall;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MtkImsPhoneConnection extends ImsPhoneConnection {
    private static final String EXTRA_IMS_GWSD = "ims_gwsd";
    private static final String LOG_TAG = "MtkImsPhoneConnection";
    public static final int SUPPORTS_VT_RINGTONE = 64;
    private int mCallIdBeforeDisconnected;
    private ArrayList<String> mConfDialStrings;
    private List<ConferenceParticipant> mConferenceParticipants;
    private boolean mIsIncomingCallDuringRttEmcGuard;
    private boolean mIsIncomingCallGwsd;
    private boolean mIsRttVideoSwitchSupported;
    private String mVendorCause;
    public boolean mWasMultiparty;
    public boolean mWasPreMultipartyHost;

    public MtkImsPhoneConnection(Phone phone, ImsCall imsCall, ImsPhoneCallTracker ct, ImsPhoneCall parent, boolean isUnknown) {
        super(phone, imsCall, ct, parent, isUnknown);
        this.mConfDialStrings = null;
        this.mConferenceParticipants = null;
        this.mCallIdBeforeDisconnected = -1;
        this.mVendorCause = null;
        boolean z = false;
        this.mWasMultiparty = false;
        this.mWasPreMultipartyHost = false;
        this.mIsRttVideoSwitchSupported = true;
        this.mIsIncomingCallGwsd = false;
        this.mIsIncomingCallDuringRttEmcGuard = false;
        if (!(imsCall == null || imsCall.getCallProfile() == null)) {
            this.mIsIncomingCallGwsd = imsCall.getCallProfile().getCallExtraInt(EXTRA_IMS_GWSD) == 1 ? true : z;
        }
        fetchIsRttVideoSwitchSupported(phone);
    }

    public MtkImsPhoneConnection(Phone phone, String dialString, ImsPhoneCallTracker ct, ImsPhoneCall parent, boolean isEmergency) {
        super(phone, dialString, ct, parent, isEmergency);
        this.mConfDialStrings = null;
        this.mConferenceParticipants = null;
        this.mCallIdBeforeDisconnected = -1;
        this.mVendorCause = null;
        this.mWasMultiparty = false;
        this.mWasPreMultipartyHost = false;
        this.mIsRttVideoSwitchSupported = true;
        this.mIsIncomingCallGwsd = false;
        this.mIsIncomingCallDuringRttEmcGuard = false;
        if (PhoneNumberUtils.isUriNumber(dialString)) {
            this.mAddress = dialString;
            this.mPostDialString = "";
        }
        fetchIsRttVideoSwitchSupported(phone);
    }

    public synchronized boolean isIncomingCallGwsd() {
        Rlog.d(LOG_TAG, "isIncomingCallGwsd: " + this.mIsIncomingCallGwsd);
        return this.mIsIncomingCallGwsd;
    }

    public String getVendorDisconnectCause() {
        return this.mVendorCause;
    }

    public void hangup() throws CallStateException {
        if (this.mOwner != null && (this.mOwner instanceof MtkImsPhoneCallTracker)) {
            this.mOwner.logDebugMessagesWithOpFormat("CC", "Hangup", this, "MtkImsphoneConnection.hangup");
        }
        MtkImsPhoneConnection.super.hangup();
    }

    public boolean onDisconnect() {
        if (!this.mDisconnected) {
            this.mCallIdBeforeDisconnected = getCallId();
        }
        return MtkImsPhoneConnection.super.onDisconnect();
    }

    public void onDisconnectConferenceParticipant(Uri endpoint) {
        if (this.mOwner != null && (this.mOwner instanceof MtkImsPhoneCallTracker)) {
            StringBuilder sb = new StringBuilder();
            sb.append(" remove: ");
            sb.append(MtkImsPhoneCallTracker.sensitiveEncode("" + endpoint));
            this.mOwner.logDebugMessagesWithOpFormat("CC", "RemoveMember", this, sb.toString());
        }
        MtkImsPhoneConnection.super.onDisconnectConferenceParticipant(endpoint);
    }

    public boolean updateAddressDisplay(ImsCall imsCall) {
        boolean changed = MtkImsPhoneConnection.super.updateAddressDisplay(imsCall);
        if (changed) {
            setConnectionAddressDisplay();
        }
        return changed;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(MtkImsPhoneConnection.super.toString());
        sb.append(" state:" + getState());
        sb.append(" mParent:");
        sb.append(getParentCallName());
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public int getCallId() {
        try {
            ImsCall call = getImsCall();
            if (call != null) {
                if (call.getCallSession() != null) {
                    String callId = call.getCallSession().getCallId();
                    if (callId != null) {
                        return Integer.parseInt(callId);
                    }
                    Rlog.d(LOG_TAG, "Abnormal! Call Id = null");
                    return -1;
                }
            }
            return -1;
        } catch (NumberFormatException e) {
            Rlog.d(LOG_TAG, e.toString());
            return -1;
        } catch (Exception e2) {
            Rlog.d(LOG_TAG, e2.toString());
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public int getCallIdBeforeDisconnected() {
        return this.mCallIdBeforeDisconnected;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<String> getConfDialStrings() {
        return this.mConfDialStrings;
    }

    public String getConferenceParticipantAddress(int index) {
        List<ConferenceParticipant> list = this.mConferenceParticipants;
        if (list == null) {
            Rlog.d(LOG_TAG, "getConferenceParticipantAddress(): no XML information");
            return "";
        } else if (index < 0 || index + 1 >= list.size()) {
            Rlog.d(LOG_TAG, "getConferenceParticipantAddress(): invalid index");
            return "";
        } else {
            ConferenceParticipant participant = this.mConferenceParticipants.get(index + 1);
            if (participant == null) {
                Rlog.d(LOG_TAG, "getConferenceParticipantAddress(): empty participant info");
                return "";
            }
            Uri userEntity = participant.getHandle();
            StringBuilder sb = new StringBuilder();
            sb.append("getConferenceParticipantAddress(): ret=");
            sb.append(MtkImsPhoneCallTracker.sensitiveEncode("" + userEntity));
            Rlog.d(LOG_TAG, sb.toString());
            return userEntity.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public String getParentCallName() {
        if (this.mOwner == null) {
            return "Unknown";
        }
        if (this.mParent == this.mOwner.mForegroundCall) {
            return "Foreground Call";
        }
        if (this.mParent == this.mOwner.mBackgroundCall) {
            return "Background Call";
        }
        if (this.mParent == this.mOwner.mRingingCall) {
            return "Ringing Call";
        }
        if (this.mParent == this.mOwner.mHandoverCall) {
            return "Handover Call";
        }
        return "Abnormal";
    }

    public boolean isConfHostBeforeHandover() {
        return this.mWasPreMultipartyHost;
    }

    public boolean isMultipartyBeforeHandover() {
        return this.mWasMultiparty;
    }

    public synchronized boolean isIncomingCallMultiparty() {
        return this.mImsCall != null && (this.mImsCall instanceof MtkImsCall) && this.mImsCall.isIncomingCallMultiparty();
    }

    public void inviteConferenceParticipants(List<String> numbers) {
        StringBuilder sb = new StringBuilder();
        for (String number : numbers) {
            sb.append(number);
            sb.append(", ");
        }
        if (this.mOwner != null && (this.mOwner instanceof MtkImsPhoneCallTracker)) {
            this.mOwner.logDebugMessagesWithOpFormat("CC", "AddMember", this, " invite with " + MtkImsPhoneCallTracker.sensitiveEncode(sb.toString()));
        }
        ImsCall imsCall = getImsCall();
        if (imsCall != null) {
            ArrayList<String> list = new ArrayList<>();
            for (String str : numbers) {
                if (PhoneNumberUtils.isUriNumber(str) || isTestSim()) {
                    list.add(str);
                } else {
                    list.add(PhoneNumberUtils.extractNetworkPortionAlt(str));
                }
            }
            String[] participants = (String[]) list.toArray(new String[list.size()]);
            try {
                imsCall.inviteParticipants(participants);
            } catch (ImsException e) {
                Rlog.e(LOG_TAG, "inviteConferenceParticipants: no call session and fail to invite participants " + MtkImsPhoneCallTracker.sensitiveEncode(Arrays.toString(participants)));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setConfDialStrings(ArrayList<String> dialStrings) {
        this.mConfDialStrings = dialStrings;
    }

    /* access modifiers changed from: package-private */
    public void setVendorDisconnectCause(String cause) {
        this.mVendorCause = cause;
    }

    public void updateConferenceParticipants(List<ConferenceParticipant> conferenceParticipants) {
        this.mConferenceParticipants = conferenceParticipants;
        MtkImsPhoneConnection.super.updateConferenceParticipants(conferenceParticipants);
    }

    public static abstract class MtkListenerBase extends Connection.ListenerBase {
        public void onConferenceParticipantsInvited(boolean isSuccess) {
        }

        public void onConferenceConnectionsConfigured(ArrayList<Connection> arrayList) {
        }

        public void onDeviceSwitched(boolean isSuccess) {
        }

        public void onAddressDisplayChanged() {
        }

        public void onTextCapabilityChanged(int localCapability, int remoteCapability, int localTextStatus, int realRemoteTextCapability) {
        }

        public void onRedialEcc(boolean isNeedUserConfirm) {
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyConferenceParticipantsInvited(boolean isSuccess) {
        for (Connection.Listener l : this.mListeners) {
            if (l instanceof MtkListenerBase) {
                ((MtkListenerBase) l).onConferenceParticipantsInvited(isSuccess);
            }
        }
    }

    public void notifyConferenceConnectionsConfigured(ArrayList<Connection> radioConnections) {
        for (Connection.Listener l : this.mListeners) {
            if (l instanceof MtkListenerBase) {
                ((MtkListenerBase) l).onConferenceConnectionsConfigured(radioConnections);
            }
        }
    }

    public void notifyDeviceSwitched(boolean isSuccess) {
        for (Connection.Listener l : this.mListeners) {
            if (l instanceof MtkListenerBase) {
                ((MtkListenerBase) l).onDeviceSwitched(isSuccess);
            }
        }
    }

    public void notifyRedialEcc(boolean isNeedUserConfirm) {
        for (Connection.Listener l : this.mListeners) {
            if (l instanceof MtkListenerBase) {
                ((MtkListenerBase) l).onRedialEcc(isNeedUserConfirm);
            }
        }
    }

    private void setConnectionAddressDisplay() {
        for (Connection.Listener l : this.mListeners) {
            if (l instanceof MtkListenerBase) {
                ((MtkListenerBase) l).onAddressDisplayChanged();
            }
        }
    }

    /* access modifiers changed from: protected */
    public int applyVideoRingtoneCapabilities(ImsCallProfile remoteProfile, int capabilities) {
        if (remoteProfile.mMediaProfile.mVideoDirection != 1) {
            return removeCapability(capabilities, 64);
        }
        Rlog.d(LOG_TAG, "Set video ringtone capability");
        return addCapability(capabilities, 64);
    }

    /* access modifiers changed from: protected */
    public boolean skipSwitchingCallToForeground() {
        if (this.mParent == this.mOwner.mHandoverCall) {
            return true;
        }
        Rlog.d(LOG_TAG, "update() - Switch Connection to foreground call:" + this);
        return false;
    }

    /* access modifiers changed from: protected */
    public void switchCallToBackgroundIfNecessary() {
        if (this.mParent == this.mOwner.mForegroundCall) {
            Rlog.d(LOG_TAG, "update() - Switch Connection to background call:" + this);
            this.mParent.detach(this);
            this.mParent = this.mOwner.mBackgroundCall;
            this.mParent.attach(this);
        }
    }

    /* access modifiers changed from: protected */
    public int calNumberPresentation(ImsCallProfile callProfile) {
        int nump = ImsCallProfile.OIRToPresentation(callProfile.getCallExtraInt("oir"));
        if (!this.mIsIncoming) {
            return 1;
        }
        return nump;
    }

    /* access modifiers changed from: protected */
    public boolean needUpdateAddress(String address) {
        if (equalsBaseDialString(this.mAddress, address)) {
            return false;
        }
        Rlog.d(LOG_TAG, "update address = " + MtkImsPhoneCallTracker.sensitiveEncode(address) + " isMpty = " + isMultiparty());
        if (!TextUtils.isEmpty(address)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean allowedUpdateMOAddress() {
        return true;
    }

    public void setIncomingCallDuringRttEmcGuard(boolean isDuringRttGuard) {
        this.mIsIncomingCallDuringRttEmcGuard = isDuringRttGuard;
        Rlog.d(LOG_TAG, "setIncomingCallDuringRttEmcGuard: " + this.mIsIncomingCallDuringRttEmcGuard);
    }

    public boolean isIncomingCallDuringRttEmcGuard() {
        Rlog.d(LOG_TAG, "isIncomingCallDuringRttEmcGuard: " + this.mIsIncomingCallDuringRttEmcGuard);
        return this.mIsIncomingCallDuringRttEmcGuard;
    }

    public boolean updateMediaCapabilities(ImsCall imsCall) {
        boolean changed = MtkImsPhoneConnection.super.updateMediaCapabilities(imsCall);
        if (changed) {
            Rlog.d(LOG_TAG, "updateMediaCapabilities capabilities = " + getConnectionCapabilities());
        }
        return changed;
    }

    /* access modifiers changed from: protected */
    public int applyLocalCallCapabilities(ImsCallProfile localProfile, int capabilities) {
        int capabilities2 = MtkImsPhoneConnection.super.applyLocalCallCapabilities(localProfile, capabilities);
        boolean isRttActive = isRttEnabledForCall();
        Rlog.d(LOG_TAG, "applyLocalCallCapabilities: isRttEnabledForCall=" + isRttActive + " mIsRttVideoSwitchSupported=" + this.mIsRttVideoSwitchSupported);
        if (!isRttActive || this.mIsRttVideoSwitchSupported) {
            return capabilities2;
        }
        return removeCapability(capabilities2, 4);
    }

    private void fetchIsRttVideoSwitchSupported(Phone phone) {
        PersistableBundle b = ((CarrierConfigManager) phone.getContext().getSystemService("carrier_config")).getConfigForSubId(phone.getSubId());
        if (b != null) {
            this.mIsRttVideoSwitchSupported = b.getBoolean("rtt_supported_for_vt_bool");
        }
    }

    private boolean isTestSim() {
        boolean isTestSim = SystemProperties.get("vendor.gsm.sim.ril.testsim").equals("1") || SystemProperties.get("vendor.gsm.sim.ril.testsim.2").equals("1") || SystemProperties.get("vendor.gsm.sim.ril.testsim.3").equals("1") || SystemProperties.get("vendor.gsm.sim.ril.testsim.4").equals("1");
        Rlog.d(LOG_TAG, "isTestSim: " + isTestSim);
        return isTestSim;
    }
}
