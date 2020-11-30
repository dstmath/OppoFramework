package com.mediatek.ims.internal;

import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSession;
import android.telephony.ims.ImsReasonInfo;
import android.util.Log;
import com.android.ims.internal.IImsCallSession;
import com.mediatek.ims.internal.IMtkImsCallSessionListener;
import java.util.Objects;

public class MtkImsCallSession extends ImsCallSession {
    private static final String TAG = "MtkImsCallSession";
    private final IMtkImsCallSession miMtkSession;

    public MtkImsCallSession(IImsCallSession iSession, IMtkImsCallSession iMtkSession) {
        this.miMtkSession = iMtkSession;
        this.miSession = iSession;
        if (iMtkSession == null || iSession == null) {
            this.mClosed = true;
            return;
        }
        try {
            this.miMtkSession.setListener(new IMtkImsCallSessionListenerProxy());
        } catch (RemoteException e) {
        }
        try {
            this.miSession.setListener(new ImsCallSession.IImsCallSessionListenerProxy(this));
        } catch (RemoteException e2) {
        }
    }

    public synchronized void close() {
        if (!this.mClosed) {
            try {
                this.miMtkSession.close();
                this.mClosed = true;
            } catch (RemoteException e) {
            }
        }
    }

    public boolean isIncomingCallMultiparty() {
        if (this.mClosed) {
            return false;
        }
        try {
            return this.miMtkSession.isIncomingCallMultiparty();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void approveEccRedial(boolean isAprroved) {
        if (!this.mClosed) {
            try {
                this.miMtkSession.approveEccRedial(isAprroved);
            } catch (RemoteException e) {
            }
        }
    }

    public void explicitCallTransfer() {
        if (!this.mClosed) {
            try {
                this.miMtkSession.explicitCallTransfer();
            } catch (RemoteException e) {
                Log.e(TAG, "explicitCallTransfer: RemoteException!");
            }
        }
    }

    public void unattendedCallTransfer(String number, int type) {
        if (!this.mClosed) {
            try {
                this.miMtkSession.unattendedCallTransfer(number, type);
            } catch (RemoteException e) {
                Log.e(TAG, "explicitCallTransfer: RemoteException!");
            }
        }
    }

    public void deviceSwitch(String number, String deviceId) {
        if (!this.mClosed) {
            try {
                this.miMtkSession.deviceSwitch(number, deviceId);
            } catch (RemoteException e) {
                Log.e(TAG, "deviceSwitch: RemoteException!");
            }
        }
    }

    public void cancelDeviceSwitch() {
        if (!this.mClosed) {
            try {
                this.miMtkSession.cancelDeviceSwitch();
            } catch (RemoteException e) {
                Log.e(TAG, "cancelDeviceSwitch: RemoteException!");
            }
        }
    }

    public void setImsCallMode(int mode) {
        if (!this.mClosed) {
            try {
                this.miMtkSession.setImsCallMode(mode);
            } catch (RemoteException e) {
                Log.e(TAG, "setImsCallMode: RemoteException!");
            }
        }
    }

    public void removeLastParticipant() {
        if (!this.mClosed) {
            try {
                this.miMtkSession.removeLastParticipant();
            } catch (RemoteException e) {
                Log.e(TAG, "removeLastParticipant: RemoteException!");
            }
        }
    }

    public String getHeaderCallId() {
        if (this.mClosed) {
            return null;
        }
        try {
            return this.miMtkSession.getHeaderCallId();
        } catch (RemoteException e) {
            Log.e(TAG, "getCallId: RemoteException!");
            return null;
        }
    }

    public class IMtkImsCallSessionListenerProxy extends IMtkImsCallSessionListener.Stub {
        public IMtkImsCallSessionListenerProxy() {
        }

        public void callSessionTransferred(IMtkImsCallSession session) {
            if (MtkImsCallSession.this.mListener != null) {
                MtkImsCallSession.this.mListener.callSessionTransferred(MtkImsCallSession.this);
            }
        }

        public void callSessionTransferFailed(IMtkImsCallSession session, ImsReasonInfo reasonInfo) {
            if (MtkImsCallSession.this.mListener != null) {
                MtkImsCallSession.this.mListener.callSessionTransferFailed(MtkImsCallSession.this, reasonInfo);
            }
        }

        public void callSessionTextCapabilityChanged(IMtkImsCallSession session, int localCapability, int remoteCapability, int localTextStatus, int realRemoteCapability) {
            if (MtkImsCallSession.this.mListener != null) {
                MtkImsCallSession.this.mListener.callSessionTextCapabilityChanged(MtkImsCallSession.this, localCapability, remoteCapability, localTextStatus, realRemoteCapability);
            }
        }

        public void callSessionRttEventReceived(IMtkImsCallSession session, int event) {
            if (MtkImsCallSession.this.mListener != null) {
                MtkImsCallSession.this.mListener.callSessionRttEventReceived(MtkImsCallSession.this, event);
            }
        }

        public void callSessionDeviceSwitched(IMtkImsCallSession session) {
            if (MtkImsCallSession.this.mListener != null) {
                MtkImsCallSession.this.mListener.callSessionDeviceSwitched(MtkImsCallSession.this);
            }
        }

        public void callSessionDeviceSwitchFailed(IMtkImsCallSession session, ImsReasonInfo reasonInfo) {
            if (MtkImsCallSession.this.mListener != null) {
                MtkImsCallSession.this.mListener.callSessionDeviceSwitchFailed(MtkImsCallSession.this, reasonInfo);
            }
        }

        public void callSessionRinging(IMtkImsCallSession session, ImsCallProfile profile) {
            if (MtkImsCallSession.this.mListener != null) {
                MtkImsCallSession.this.mListener.callSessionRinging(MtkImsCallSession.this, profile);
            }
        }

        public void callSessionBusy(IMtkImsCallSession session) {
            if (MtkImsCallSession.this.mListener != null) {
                MtkImsCallSession.this.mListener.callSessionBusy(MtkImsCallSession.this);
            }
        }

        public void callSessionCalling(IMtkImsCallSession session) {
            if (MtkImsCallSession.this.mListener != null) {
                MtkImsCallSession.this.mListener.callSessionCalling(MtkImsCallSession.this);
            }
        }

        public void callSessionMergeStarted(IMtkImsCallSession session, IMtkImsCallSession newSession, ImsCallProfile profile) {
            Log.d(MtkImsCallSession.TAG, "callSessionMergeStarted");
        }

        public void callSessionMergeComplete(IMtkImsCallSession newSession) {
            if (MtkImsCallSession.this.mListener == null) {
                return;
            }
            if (newSession != null) {
                ImsCallSession validActiveSession = MtkImsCallSession.this;
                try {
                    if (!Objects.equals(MtkImsCallSession.this.miSession.getCallId(), newSession.getCallId())) {
                        validActiveSession = new MtkImsCallSession(newSession.getIImsCallSession(), newSession);
                    }
                } catch (RemoteException e) {
                    Log.e(MtkImsCallSession.TAG, "callSessionMergeComplete: exception for getCallId!");
                }
                MtkImsCallSession.this.mListener.callSessionMergeComplete(validActiveSession);
                return;
            }
            MtkImsCallSession.this.mListener.callSessionMergeComplete((ImsCallSession) null);
        }

        public void callSessionRedialEcc(IMtkImsCallSession session, boolean isNeedUserConfirm) {
            Log.d(MtkImsCallSession.TAG, "callSessionRedialEcc: isNeedUserConfirm = " + isNeedUserConfirm);
            if (MtkImsCallSession.this.mListener != null) {
                MtkImsCallSession.this.mListener.callSessionRedialEcc(MtkImsCallSession.this, isNeedUserConfirm);
            }
        }
    }
}
