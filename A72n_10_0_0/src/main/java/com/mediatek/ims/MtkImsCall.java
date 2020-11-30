package com.mediatek.ims;

import android.content.Context;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSession;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.util.Log;
import com.android.ims.ImsCall;
import com.android.ims.ImsException;
import com.android.internal.annotations.VisibleForTesting;
import com.mediatek.ims.internal.MtkImsCallSession;
import java.util.Objects;

public class MtkImsCall extends ImsCall {
    private static final String TAG = "MtkImsCall";
    private static final int UPDATE_DEVICE_SWITCH = 8;
    private static final int UPDATE_ECT = 7;
    private String mAddress = null;
    private boolean mIsConferenceMerging = false;

    public MtkImsCall(Context context, ImsCallProfile profile) {
        super(context, profile);
    }

    public static class Listener extends ImsCall.Listener {
        public void onPauInfoChanged(ImsCall imsCall) {
        }

        public void onCallTransferred(ImsCall call) {
        }

        public void onCallTransferFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onTextCapabilityChanged(ImsCall call, int localCapability, int remoteCapability, int localTextStatus, int realRemoteCapability) {
        }

        public void onRttEventReceived(ImsCall call, int event) {
        }

        public void onCallDeviceSwitched(ImsCall imsCall) {
        }

        public void onCallDeviceSwitchFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
        }

        public void onCallRedialEcc(ImsCall imsCall, boolean isNeedUserConfirm) {
        }
    }

    public void start(ImsCallSession session, String callee) throws ImsException {
        MtkImsCall.super.start(session, callee);
        this.mAddress = callee;
    }

    public void start(ImsCallSession session, String[] participants) throws ImsException {
        MtkImsCall.super.start(session, participants);
        if (this.mCallProfile != null && this.mCallProfile.getCallExtraBoolean("conference")) {
            this.mIsConferenceHost = true;
        }
    }

    /* access modifiers changed from: protected */
    public void setTransientSessionAsPrimary(ImsCallSession transientSession) {
        synchronized (this) {
            if (this.mSession != null) {
                this.mSession.setListener((ImsCallSession.Listener) null);
            }
            this.mSession = transientSession;
            if (this.mSession != null) {
                this.mSession.setListener(createCallSessionListener());
                ImsCallProfile imsCallProfile = this.mSession.getCallProfile();
                if (imsCallProfile != null) {
                    this.mCallProfile.updateCallType(imsCallProfile);
                }
            }
        }
    }

    public boolean isIncomingCallMultiparty() {
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                if (this.mSession instanceof MtkImsCallSession) {
                    return this.mSession.isIncomingCallMultiparty();
                }
            }
            return false;
        }
    }

    public void approveEccRedial(boolean isAprroved) {
        logi("approveEccRedial :: ");
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                if (this.mSession instanceof MtkImsCallSession) {
                    this.mSession.approveEccRedial(isAprroved);
                }
            }
        }
    }

    public void explicitCallTransfer() throws ImsException {
        logi("explicitCallTransfer :: ");
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                loge("explicitCallTransfer :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                throw new ImsException("Call update is in progress", 102);
            } else if (this.mSession == null) {
                throw new ImsException("No call session", 148);
            } else if (this.mSession instanceof MtkImsCallSession) {
                this.mSession.explicitCallTransfer();
                this.mUpdateRequest = 7;
            }
        }
    }

    public void unattendedCallTransfer(String number, int type) throws ImsException {
        logi("explicitCallTransfer :: ");
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                loge("explicitCallTransfer :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                throw new ImsException("Call update is in progress", 102);
            } else if (this.mSession == null) {
                throw new ImsException("No call session", 148);
            } else if (this.mSession instanceof MtkImsCallSession) {
                this.mSession.unattendedCallTransfer(number, type);
                this.mUpdateRequest = 7;
            }
        }
    }

    public void deviceSwitch(String number, String deviceId) throws ImsException {
        logi("deviceSwitch :: ");
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                loge("deviceSwitch :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                throw new ImsException("Call update is in progress", 102);
            } else if (this.mSession == null) {
                throw new ImsException("No call session", 148);
            } else if (this.mSession instanceof MtkImsCallSession) {
                this.mSession.deviceSwitch(number, deviceId);
                this.mUpdateRequest = UPDATE_DEVICE_SWITCH;
            }
        }
    }

    public void cancelDeviceSwitch() throws ImsException {
        logi("cancelDeviceSwitch :: ");
        synchronized (this.mLockObj) {
            if (this.mSession instanceof MtkImsCallSession) {
                this.mSession.cancelDeviceSwitch();
            }
        }
    }

    /* access modifiers changed from: protected */
    public ImsCall createNewCall(ImsCallSession session, ImsCallProfile profile) {
        ImsCall call = new MtkImsCall(this.mContext, profile);
        try {
            call.attachSession(session);
            return call;
        } catch (ImsException e) {
            call.close();
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public ImsCallSession.Listener createCallSessionListener() {
        this.mImsCallSessionListenerProxy = new MtkImsCallSessionListenerProxy();
        return this.mImsCallSessionListenerProxy;
    }

    @VisibleForTesting
    public class MtkImsCallSessionListenerProxy extends ImsCall.ImsCallSessionListenerProxy {
        public MtkImsCallSessionListenerProxy() {
            super(MtkImsCall.this);
        }

        /* access modifiers changed from: protected */
        public boolean doesCallSessionExistsInMerge(ImsCallSession cs) {
            String hostId;
            String peerId;
            String callId = cs.getCallId();
            String sessionId = "";
            if (MtkImsCall.this.mMergeHost == null || MtkImsCall.this.mMergeHost.mSession == null) {
                hostId = sessionId;
            } else {
                hostId = MtkImsCall.this.mMergeHost.mSession.getCallId();
            }
            if (MtkImsCall.this.mMergePeer == null || MtkImsCall.this.mMergePeer.mSession == null) {
                peerId = sessionId;
            } else {
                peerId = MtkImsCall.this.mMergePeer.mSession.getCallId();
            }
            if (MtkImsCall.this.mSession != null) {
                sessionId = MtkImsCall.this.mSession.getCallId();
            }
            return (MtkImsCall.this.isMergeHost() && Objects.equals(peerId, callId)) || (MtkImsCall.this.isMergePeer() && Objects.equals(hostId, callId)) || Objects.equals(sessionId, callId);
        }

        public void callSessionMergeComplete(ImsCallSession newSession) {
            MtkImsCall mtkImsCall = MtkImsCall.this;
            mtkImsCall.logi("callSessionMergeComplete :: newSession =" + newSession);
            if (MtkImsCall.this.isMergeHost()) {
                if (newSession != null) {
                    MtkImsCall.this.mTransientConferenceSession = doesCallSessionExistsInMerge(newSession) ? null : newSession;
                    if (MtkImsCall.this.mTransientConferenceSession == null) {
                        MtkImsCall.this.logi("callSessionMergeComplete :: callSessionExisted.");
                    }
                }
                MtkImsCall.this.processMergeComplete();
            } else if (MtkImsCall.this.mMergeHost == null) {
                MtkImsCall.this.logd("merge host is null, terminate conf");
                if (newSession != null) {
                    newSession.terminate(102);
                }
            } else {
                MtkImsCall.this.mMergeHost.processMergeComplete();
            }
        }

        public void callSessionTransferred(ImsCallSession session) {
            ImsCall.Listener listener;
            MtkImsCall mtkImsCall = MtkImsCall.this;
            mtkImsCall.loge("callSessionTransferred :: session=" + session);
            synchronized (MtkImsCall.this) {
                MtkImsCall.this.mUpdateRequest = 0;
                listener = MtkImsCall.this.mListener;
            }
            if (listener != null && (listener instanceof Listener)) {
                try {
                    ((Listener) listener).onCallTransferred(MtkImsCall.this);
                } catch (Throwable t) {
                    MtkImsCall.this.loge("callSessionTransferred :: ", t);
                }
            }
        }

        public void callSessionTransferFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.Listener listener;
            MtkImsCall mtkImsCall = MtkImsCall.this;
            mtkImsCall.loge("callSessionTransferFailed :: session=" + session + " reasonInfo=" + reasonInfo);
            synchronized (MtkImsCall.this) {
                MtkImsCall.this.mUpdateRequest = 0;
                listener = MtkImsCall.this.mListener;
            }
            if (listener != null && (listener instanceof Listener)) {
                try {
                    ((Listener) listener).onCallTransferFailed(MtkImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    MtkImsCall.this.loge("callSessionTransferFailed :: ", t);
                }
            }
        }

        public void callSessionDeviceSwitched(ImsCallSession session) {
            ImsCall.Listener listener;
            MtkImsCall mtkImsCall = MtkImsCall.this;
            mtkImsCall.loge("callSessionTransferred :: session=" + session);
            synchronized (MtkImsCall.this) {
                MtkImsCall.this.mUpdateRequest = 0;
                listener = MtkImsCall.this.mListener;
            }
            if (listener != null && (listener instanceof Listener)) {
                try {
                    ((Listener) listener).onCallDeviceSwitched(MtkImsCall.this);
                } catch (Throwable t) {
                    MtkImsCall.this.loge("callSessionDeviceSwitched :: ", t);
                }
            }
        }

        public void callSessionDeviceSwitchFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.Listener listener;
            MtkImsCall mtkImsCall = MtkImsCall.this;
            mtkImsCall.loge("callSessionDeviceSwitchedFailed :: session=" + session + " reasonInfo=" + reasonInfo);
            synchronized (MtkImsCall.this) {
                MtkImsCall.this.mUpdateRequest = 0;
                listener = MtkImsCall.this.mListener;
            }
            if (listener != null && (listener instanceof Listener)) {
                try {
                    ((Listener) listener).onCallDeviceSwitchFailed(MtkImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    MtkImsCall.this.loge("callSessionDeviceSwitchedFailed :: ", t);
                }
            }
        }

        public void callSessionTextCapabilityChanged(ImsCallSession session, int localCapability, int remoteCapability, int localTextStatus, int realRemoteCapability) {
            ImsCall.Listener listener;
            synchronized (MtkImsCall.this) {
                listener = MtkImsCall.this.mListener;
            }
            if (listener != null && (listener instanceof Listener)) {
                try {
                    ((Listener) listener).onTextCapabilityChanged(MtkImsCall.this, localCapability, remoteCapability, localTextStatus, realRemoteCapability);
                } catch (Throwable t) {
                    MtkImsCall.this.loge("callSessionTextCapabilityChanged :: ", t);
                }
            }
        }

        public void callSessionRttEventReceived(ImsCallSession session, int event) {
            ImsCall.Listener listener;
            synchronized (MtkImsCall.this) {
                listener = MtkImsCall.this.mListener;
            }
            if (listener != null && (listener instanceof Listener)) {
                try {
                    ((Listener) listener).onRttEventReceived(MtkImsCall.this, event);
                } catch (Throwable t) {
                    MtkImsCall.this.loge("callSessionRttEventReceived :: ", t);
                }
            }
        }

        public void callSessionRedialEcc(ImsCallSession session, boolean isNeedUserConfirm) {
            ImsCall.Listener listener;
            MtkImsCall mtkImsCall = MtkImsCall.this;
            mtkImsCall.loge("callSessionRedialEcc :: session=" + session);
            synchronized (MtkImsCall.this) {
                listener = MtkImsCall.this.mListener;
            }
            if (listener != null && (listener instanceof Listener)) {
                try {
                    ((Listener) listener).onCallRedialEcc(MtkImsCall.this, isNeedUserConfirm);
                } catch (Throwable t) {
                    MtkImsCall.this.loge("callSessionRedialEcc :: ", t);
                }
            }
        }

        public void callSessionRinging(ImsCallSession session, ImsCallProfile callProfile) {
        }

        public void callSessionBusy(ImsCallSession session) {
        }

        public void callSessionCalling(ImsCallSession session) {
        }
    }

    /* access modifiers changed from: protected */
    public String updateRequestToString(int updateRequest) {
        if (updateRequest == 7) {
            return "ECT";
        }
        if (updateRequest != UPDATE_DEVICE_SWITCH) {
            return MtkImsCall.super.updateRequestToString(updateRequest);
        }
        return "DEVICE_SWITCH";
    }

    /* access modifiers changed from: protected */
    public void logi(String s) {
        Log.i(TAG, appendImsCallInfoToString(s));
    }

    private String appendImsCallInfoToString(String s) {
        return s + " MtkImsCall=" + this;
    }

    /* access modifiers changed from: protected */
    public void copyCallProfileIfNecessary(ImsStreamMediaProfile profile) {
        if (this.mCallProfile != null) {
            this.mCallProfile.mMediaProfile.copyFrom(profile);
        }
    }

    /* access modifiers changed from: protected */
    public void checkIfConferenceMerge(ImsReasonInfo reasonInfo) {
        if (!isCallSessionMergePending()) {
            return;
        }
        if (isMultiparty() || this.mTerminationRequestPending) {
            logi("this is a conference host during merging, and is disconnected..");
            processMergeFailed(reasonInfo);
        }
    }

    /* access modifiers changed from: protected */
    public void updateHoldStateIfNecessary(boolean hold) {
        this.mHold = hold;
    }

    /* access modifiers changed from: protected */
    public boolean shouldSkipResetMergePending() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void resetConferenceMergingFlag() {
        this.mIsConferenceMerging = false;
    }

    public void setTerminationRequestFlag(boolean result) {
        this.mTerminationRequestPending = result;
    }

    /* access modifiers changed from: protected */
    public void setPendingUpdateMerge() {
        this.mUpdateRequest = 4;
        if (this.mMergeHost != null) {
            this.mMergeHost.mUpdateRequest = 4;
        }
        if (this.mMergePeer != null) {
            this.mMergePeer.mUpdateRequest = 4;
        }
    }
}
