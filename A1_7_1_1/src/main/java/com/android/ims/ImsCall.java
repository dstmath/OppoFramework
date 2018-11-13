package com.android.ims;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.telecom.ConferenceParticipant;
import android.util.Log;
import com.android.ims.internal.ICall;
import com.android.ims.internal.ImsCallSession;
import com.android.ims.internal.ImsStreamMediaSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ImsCall implements ICall {
    private static final boolean CONF_DBG = true;
    private static final boolean DBG = false;
    private static final boolean FORCE_DEBUG = true;
    private static final String TAG = "ImsCall";
    private static final int UPDATE_ECT = 7;
    private static final int UPDATE_EXTEND_TO_CONFERENCE = 5;
    private static final int UPDATE_HOLD = 1;
    private static final int UPDATE_HOLD_MERGE = 2;
    private static final int UPDATE_MERGE = 4;
    private static final int UPDATE_NONE = 0;
    private static final int UPDATE_RESUME = 3;
    private static final int UPDATE_UNSPECIFIED = 6;
    public static final int USSD_MODE_NOTIFY = 0;
    public static final int USSD_MODE_REQUEST = 1;
    private static final boolean VDBG = false;
    private static final AtomicInteger sUniqueIdGenerator = null;
    private String mAddress;
    private ImsCallProfile mCallProfile;
    private boolean mCallSessionMergePending;
    private CopyOnWriteArrayList<ConferenceParticipant> mConferenceParticipants;
    private Context mContext;
    private boolean mHold;
    private ImsCallSessionListenerProxy mImsCallSessionListenerProxy;
    private boolean mInCall;
    private boolean mIsConferenceHost;
    private boolean mIsConferenceMerging;
    private boolean mIsMerged;
    private ImsReasonInfo mLastReasonInfo;
    private Listener mListener;
    private Object mLockObj;
    private ImsStreamMediaSession mMediaSession;
    private ImsCall mMergeHost;
    private ImsCall mMergePeer;
    private boolean mMergeRequestedByConference;
    private boolean mMute;
    private int mOverrideReason;
    private ImsCallProfile mProposedCallProfile;
    private ImsCallSession mSession;
    private boolean mSessionEndDuringMerge;
    private ImsReasonInfo mSessionEndDuringMergeReasonInfo;
    private boolean mTerminationRequestPending;
    private ImsCallSession mTransientConferenceSession;
    private int mUpdateRequest;
    private boolean mWasVideoCall;
    public final int uniqueId;

    public class ImsCallSessionListenerProxy extends com.android.ims.internal.ImsCallSession.Listener {
        public void callSessionProgressing(ImsCallSession session, ImsStreamMediaProfile profile) {
            ImsCall.this.logi("callSessionProgressing :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionProgressing :: not supported for transient conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                if (ImsCall.this.mCallProfile != null) {
                    ImsCall.this.mCallProfile.mMediaProfile.copyFrom(profile);
                }
            }
            if (listener != null) {
                try {
                    listener.onCallProgressing(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionProgressing :: ", t);
                }
            }
        }

        public void callSessionStarted(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionStarted :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionStarted :: on transient session=" + session);
                return;
            }
            ImsCall.this.setCallSessionMergePending(ImsCall.DBG);
            if (!ImsCall.this.isTransientConferenceSession(session)) {
                Listener listener;
                synchronized (ImsCall.this) {
                    listener = ImsCall.this.mListener;
                    ImsCall.this.setCallProfile(profile);
                }
                if (listener != null) {
                    try {
                        listener.onCallStarted(ImsCall.this);
                    } catch (Throwable t) {
                        ImsCall.this.loge("callSessionStarted :: ", t);
                    }
                }
            }
        }

        public void callSessionStartFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionStartFailed :: session=" + session + " reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionStartFailed :: not supported for transient conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.mLastReasonInfo = reasonInfo;
            }
            if (listener != null) {
                try {
                    listener.onCallStartFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionStarted :: ", t);
                }
            }
        }

        public void callSessionTerminated(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.logi("callSessionTerminated :: session=" + session + " reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionTerminated :: on transient session=" + session);
                ImsCall.this.processMergeFailed(reasonInfo);
                return;
            }
            if (ImsCall.this.isCallSessionMergePending() && ImsCall.this.isMultiparty()) {
                ImsCall.this.logi("this is a conference host during merging, and is disconnected..");
                ImsCall.this.processMergeFailed(reasonInfo);
            }
            if (ImsCall.this.mOverrideReason != 0) {
                ImsCall.this.logi("callSessionTerminated :: overrideReasonInfo=" + ImsCall.this.mOverrideReason);
                reasonInfo = new ImsReasonInfo(ImsCall.this.mOverrideReason, reasonInfo.getExtraCode(), reasonInfo.getExtraMessage());
            }
            ImsCall.this.processCallTerminated(reasonInfo);
            ImsCall.this.setCallSessionMergePending(ImsCall.DBG);
        }

        /* JADX WARNING: Missing block: B:11:0x004f, code:
            if (r0 == null) goto L_0x0056;
     */
        /* JADX WARNING: Missing block: B:13:?, code:
            r0.onCallHeld(r5.this$0);
     */
        /* JADX WARNING: Missing block: B:18:0x005a, code:
            r1 = move-exception;
     */
        /* JADX WARNING: Missing block: B:19:0x005b, code:
            com.android.ims.ImsCall.-wrap7(r5.this$0, "callSessionHeld :: ", r1);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void callSessionHeld(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionHeld :: session=" + session + "profile=" + profile);
            synchronized (ImsCall.this) {
                ImsCall.this.setCallProfile(profile);
                if (ImsCall.this.mUpdateRequest == 2) {
                    ImsCall.this.mergeInternal();
                    return;
                }
                ImsCall.this.mUpdateRequest = 0;
                Listener listener = ImsCall.this.mListener;
                ImsCall.this.mHold = true;
            }
        }

        public void callSessionHoldFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionHoldFailed :: session" + session + "reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionHoldFailed :: not supported for transient conference session=" + session);
                return;
            }
            Listener listener;
            ImsCall.this.logi("callSessionHoldFailed :: session=" + session + ", reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this.mLockObj) {
                ImsCall.this.mHold = ImsCall.DBG;
            }
            synchronized (ImsCall.this) {
                if (ImsCall.this.mUpdateRequest == 2) {
                }
                ImsCall.this.mUpdateRequest = 0;
                listener = ImsCall.this.mListener;
                ImsCall.this.mHold = ImsCall.DBG;
            }
            if (listener != null) {
                try {
                    listener.onCallHoldFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionHoldFailed :: ", t);
                }
            }
        }

        public void callSessionHoldReceived(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionHoldReceived :: session=" + session + "profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionHoldReceived :: not supported for transient conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.setCallProfile(profile);
            }
            if (listener != null) {
                try {
                    listener.onCallHoldReceived(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionHoldReceived :: ", t);
                }
            }
        }

        public void callSessionResumed(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionResumed :: session=" + session + "profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionResumed :: not supported for transient conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.setCallProfile(profile);
                ImsCall.this.mUpdateRequest = 0;
                ImsCall.this.mHold = ImsCall.DBG;
            }
            if (listener != null) {
                try {
                    listener.onCallResumed(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionResumed :: ", t);
                }
            }
        }

        public void callSessionResumeFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionResumeFailed :: session=" + session + "reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionResumeFailed :: not supported for transient conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this.mLockObj) {
                ImsCall.this.mHold = true;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.mUpdateRequest = 0;
            }
            if (listener != null) {
                try {
                    listener.onCallResumeFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionResumeFailed :: ", t);
                }
            }
        }

        public void callSessionResumeReceived(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionResumeReceived :: session=" + session + "profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionResumeReceived :: not supported for transient conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.setCallProfile(profile);
            }
            if (listener != null) {
                try {
                    listener.onCallResumeReceived(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionResumeReceived :: ", t);
                }
            }
        }

        public void callSessionMergeStarted(ImsCallSession session, ImsCallSession newSession, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionMergeStarted :: session=" + session + " newSession=" + newSession + ", profile=" + profile);
        }

        private boolean doesCallSessionExistsInMerge(ImsCallSession cs) {
            String callId = cs.getCallId();
            if ((ImsCall.this.isMergeHost() && Objects.equals(ImsCall.this.mMergePeer.mSession.getCallId(), callId)) || (ImsCall.this.isMergePeer() && Objects.equals(ImsCall.this.mMergeHost.mSession.getCallId(), callId))) {
                return true;
            }
            return Objects.equals(ImsCall.this.mSession.getCallId(), callId);
        }

        public void callSessionMergeComplete(ImsCallSession newSession) {
            ImsCall.this.logi("callSessionMergeComplete :: newSession =" + newSession);
            if (ImsCall.this.isMergeHost()) {
                if (newSession != null) {
                    ImsCall imsCall = ImsCall.this;
                    if (doesCallSessionExistsInMerge(newSession)) {
                        newSession = null;
                    }
                    imsCall.mTransientConferenceSession = newSession;
                }
                ImsCall.this.processMergeComplete();
            } else if (ImsCall.this.mMergeHost == null) {
                ImsCall.this.logd("merge host is null, terminate conf");
                if (newSession != null) {
                    newSession.terminate(102);
                }
            } else {
                ImsCall.this.mMergeHost.processMergeComplete();
            }
        }

        public void callSessionMergeFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionMergeFailed :: session=" + session + "reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this) {
                if (ImsCall.this.isMergeHost()) {
                    ImsCall.this.processMergeFailed(reasonInfo);
                } else if (ImsCall.this.mMergeHost != null) {
                    ImsCall.this.mMergeHost.processMergeFailed(reasonInfo);
                } else {
                    ImsCall.this.loge("callSessionMergeFailed :: No merge host for this conference!");
                }
            }
        }

        public void callSessionUpdated(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionUpdated :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionUpdated :: not supported for transient conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.setCallProfile(profile);
            }
            if (listener != null) {
                try {
                    listener.onCallUpdated(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionUpdated :: ", t);
                }
            }
        }

        public void callSessionUpdateFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionUpdateFailed :: session=" + session + " reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionUpdateFailed :: not supported for transient conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.mUpdateRequest = 0;
            }
            if (listener != null) {
                try {
                    listener.onCallUpdateFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionUpdateFailed :: ", t);
                }
            }
        }

        public void callSessionUpdateReceived(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionUpdateReceived :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionUpdateReceived :: not supported for transient conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.mProposedCallProfile = profile;
                ImsCall.this.mUpdateRequest = 6;
            }
            if (listener != null) {
                try {
                    listener.onCallUpdateReceived(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionUpdateReceived :: ", t);
                }
            }
        }

        public void callSessionConferenceExtended(ImsCallSession session, ImsCallSession newSession, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionConferenceExtended :: session=" + session + " newSession=" + newSession + ", profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionConferenceExtended :: not supported for transient conference session=" + session);
                return;
            }
            ImsCall newCall = ImsCall.this.createNewCall(newSession, profile);
            if (newCall == null) {
                callSessionConferenceExtendFailed(session, new ImsReasonInfo());
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.mUpdateRequest = 0;
            }
            if (listener != null) {
                try {
                    listener.onCallConferenceExtended(ImsCall.this, newCall);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionConferenceExtended :: ", t);
                }
            }
        }

        public void callSessionConferenceExtendFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionConferenceExtendFailed :: reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionConferenceExtendFailed :: not supported for transient conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.mUpdateRequest = 0;
            }
            if (listener != null) {
                try {
                    listener.onCallConferenceExtendFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionConferenceExtendFailed :: ", t);
                }
            }
        }

        public void callSessionConferenceExtendReceived(ImsCallSession session, ImsCallSession newSession, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionConferenceExtendReceived :: newSession=" + newSession + ", profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionConferenceExtendReceived :: not supported for transient conference session" + session);
                return;
            }
            ImsCall newCall = ImsCall.this.createNewCall(newSession, profile);
            if (newCall != null) {
                Listener listener;
                synchronized (ImsCall.this) {
                    listener = ImsCall.this.mListener;
                }
                if (listener != null) {
                    try {
                        listener.onCallConferenceExtendReceived(ImsCall.this, newCall);
                    } catch (Throwable t) {
                        ImsCall.this.loge("callSessionConferenceExtendReceived :: ", t);
                    }
                }
            }
        }

        public void callSessionInviteParticipantsRequestDelivered(ImsCallSession session) {
            ImsCall.this.logi("callSessionInviteParticipantsRequestDelivered ::");
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionInviteParticipantsRequestDelivered :: not supported for conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                if (ImsCall.this.mIsConferenceMerging) {
                    ImsCall.this.mIsConferenceMerging = ImsCall.DBG;
                }
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallInviteParticipantsRequestDelivered(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionInviteParticipantsRequestDelivered :: ", t);
                }
            }
        }

        public void callSessionInviteParticipantsRequestFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionInviteParticipantsRequestFailed :: reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionInviteParticipantsRequestFailed :: not supported for conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.mIsConferenceMerging = ImsCall.DBG;
            }
            if (listener != null) {
                try {
                    listener.onCallInviteParticipantsRequestFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionInviteParticipantsRequestFailed :: ", t);
                }
            }
        }

        public void callSessionRemoveParticipantsRequestDelivered(ImsCallSession session) {
            ImsCall.this.logi("callSessionRemoveParticipantsRequestDelivered ::");
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionRemoveParticipantsRequestDelivered :: not supported for conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallRemoveParticipantsRequestDelivered(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionRemoveParticipantsRequestDelivered :: ", t);
                }
            }
        }

        public void callSessionRemoveParticipantsRequestFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionRemoveParticipantsRequestFailed :: reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionRemoveParticipantsRequestFailed :: not supported for conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallRemoveParticipantsRequestFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionRemoveParticipantsRequestFailed :: ", t);
                }
            }
        }

        public void callSessionConferenceStateUpdated(ImsCallSession session, ImsConferenceState state) {
            ImsCall.this.logi("callSessionConferenceStateUpdated :: state=" + state);
            ImsCall.this.conferenceStateUpdated(state);
        }

        public void callSessionUssdMessageReceived(ImsCallSession session, int mode, String ussdMessage) {
            ImsCall.this.logi("callSessionUssdMessageReceived :: mode=" + mode + ", ussdMessage=" + ussdMessage);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionUssdMessageReceived :: not supported for transient conference session=" + session);
                return;
            }
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallUssdMessageReceived(ImsCall.this, mode, ussdMessage);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionUssdMessageReceived :: ", t);
                }
            }
        }

        public void callSessionTtyModeReceived(ImsCallSession session, int mode) {
            Listener listener;
            ImsCall.this.logi("callSessionTtyModeReceived :: mode=" + mode);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallSessionTtyModeReceived(ImsCall.this, mode);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionTtyModeReceived :: ", t);
                }
            }
        }

        public void callSessionMultipartyStateChanged(ImsCallSession session, boolean isMultiParty) {
            Listener listener;
            if (ImsCall.VDBG) {
                String str;
                ImsCall imsCall = ImsCall.this;
                StringBuilder append = new StringBuilder().append("callSessionMultipartyStateChanged isMultiParty: ");
                if (isMultiParty) {
                    str = "Y";
                } else {
                    str = "N";
                }
                imsCall.logi(append.append(str).toString());
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onMultipartyStateChanged(ImsCall.this, isMultiParty);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionMultipartyStateChanged :: ", t);
                }
            }
        }

        public void callSessionHandover(ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall.this.logi("callSessionHandover :: session=" + session + ", srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallHandover(ImsCall.this, srcAccessTech, targetAccessTech, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionHandover :: ", t);
                }
            }
        }

        public void callSessionHandoverFailed(ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall.this.loge("callSessionHandoverFailed :: session=" + session + ", srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallHandoverFailed(ImsCall.this, srcAccessTech, targetAccessTech, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionHandoverFailed :: ", t);
                }
            }
        }

        public void callSessionSuppServiceReceived(ImsCallSession session, ImsSuppServiceNotification suppServiceInfo) {
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionSuppServiceReceived :: not supported for transient conference session=" + session);
                return;
            }
            Listener listener;
            ImsCall.this.logi("callSessionSuppServiceReceived :: session=" + session + ", suppServiceInfo" + suppServiceInfo);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallSuppServiceReceived(ImsCall.this, suppServiceInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionSuppServiceReceived :: ", t);
                }
            }
        }

        public void callSessionTransferred(ImsCallSession session) {
            Listener listener;
            ImsCall.this.loge("callSessionTransferred :: session=" + session);
            synchronized (ImsCall.this) {
                ImsCall.this.mUpdateRequest = 0;
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallTransferred(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionTransferred :: ", t);
                }
            }
        }

        public void callSessionTransferFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall.this.loge("callSessionTransferFailed :: session=" + session + " reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this) {
                ImsCall.this.mUpdateRequest = 0;
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallTransferFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionTransferFailed :: ", t);
                }
            }
        }
    }

    public static class Listener {
        public void onCallProgressing(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallStarted(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallStartFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallTerminated(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallStateChanged(call);
        }

        public void onCallHeld(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallHoldFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallHoldReceived(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallResumed(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallResumeFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallResumeReceived(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallMerged(ImsCall call, ImsCall peerCall, boolean swapCalls) {
            onCallStateChanged(call);
        }

        public void onCallMergeFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallUpdated(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallUpdateFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallUpdateReceived(ImsCall call) {
        }

        public void onCallConferenceExtended(ImsCall call, ImsCall newCall) {
            onCallStateChanged(call);
        }

        public void onCallConferenceExtendFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallConferenceExtendReceived(ImsCall call, ImsCall newCall) {
            onCallStateChanged(call);
        }

        public void onCallInviteParticipantsRequestDelivered(ImsCall call) {
        }

        public void onCallInviteParticipantsRequestFailed(ImsCall call, ImsReasonInfo reasonInfo) {
        }

        public void onCallRemoveParticipantsRequestDelivered(ImsCall call) {
        }

        public void onCallRemoveParticipantsRequestFailed(ImsCall call, ImsReasonInfo reasonInfo) {
        }

        public void onCallConferenceStateUpdated(ImsCall call, ImsConferenceState state) {
        }

        public void onConferenceParticipantsStateChanged(ImsCall call, List<ConferenceParticipant> list) {
        }

        public void onCallUssdMessageReceived(ImsCall call, int mode, String ussdMessage) {
        }

        public void onCallError(ImsCall call, ImsReasonInfo reasonInfo) {
        }

        public void onCallStateChanged(ImsCall call) {
        }

        public void onCallStateChanged(ImsCall call, int state) {
        }

        public void onCallSuppServiceReceived(ImsCall call, ImsSuppServiceNotification suppServiceInfo) {
        }

        public void onCallSessionTtyModeReceived(ImsCall call, int mode) {
        }

        public void onCallHandover(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        }

        public void onCallHandoverFailed(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        }

        public void onMultipartyStateChanged(ImsCall imsCall, boolean isMultiParty) {
        }

        public void onPauInfoChanged(ImsCall imsCall) {
        }

        public void onCallTransferred(ImsCall call) {
        }

        public void onCallTransferFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCall.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCall.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCall.<clinit>():void");
    }

    public ImsCall(Context context, ImsCallProfile profile) {
        this.mLockObj = new Object();
        this.mInCall = DBG;
        this.mHold = DBG;
        this.mMute = DBG;
        this.mUpdateRequest = 0;
        this.mListener = null;
        this.mMergePeer = null;
        this.mMergeHost = null;
        this.mMergeRequestedByConference = DBG;
        this.mSession = null;
        this.mCallProfile = null;
        this.mProposedCallProfile = null;
        this.mLastReasonInfo = null;
        this.mMediaSession = null;
        this.mTransientConferenceSession = null;
        this.mSessionEndDuringMerge = DBG;
        this.mSessionEndDuringMergeReasonInfo = null;
        this.mIsMerged = DBG;
        this.mCallSessionMergePending = DBG;
        this.mIsConferenceMerging = DBG;
        this.mAddress = null;
        this.mTerminationRequestPending = DBG;
        this.mIsConferenceHost = DBG;
        this.mWasVideoCall = DBG;
        this.mOverrideReason = 0;
        this.mContext = context;
        setCallProfile(profile);
        this.uniqueId = sUniqueIdGenerator.getAndIncrement();
    }

    public void close() {
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.close();
                this.mSession = null;
            } else {
                logi("close :: Cannot close Null call session!");
            }
            this.mCallProfile = null;
            this.mProposedCallProfile = null;
            this.mLastReasonInfo = null;
            this.mMediaSession = null;
        }
    }

    public boolean checkIfRemoteUserIsSame(String userId) {
        boolean z = true;
        if (userId == null) {
            return DBG;
        }
        String address = this.mCallProfile.getCallExtra("oi", "").replace("+", "");
        if (address == null || address.equals("")) {
            address = this.mAddress;
        }
        logi("address= " + address);
        if (address != null && !address.equals("") && (userId.equals(address) || userId.indexOf(address) != -1)) {
            return true;
        }
        String remoteSipUri = this.mCallProfile.getCallExtra("remote_uri", "");
        logi("REMOTE_SIP_URI= " + remoteSipUri);
        if (remoteSipUri == null || remoteSipUri.equals("")) {
            return DBG;
        }
        if (!userId.equals(remoteSipUri) && userId.indexOf(remoteSipUri) == -1) {
            z = DBG;
        }
        return z;
    }

    public boolean equalsTo(ICall call) {
        if (call != null && (call instanceof ImsCall)) {
            return equals(call);
        }
        return DBG;
    }

    public static boolean isSessionAlive(ImsCallSession session) {
        return session != null ? session.isAlive() : DBG;
    }

    public ImsCallProfile getCallProfile() {
        ImsCallProfile imsCallProfile;
        synchronized (this.mLockObj) {
            imsCallProfile = this.mCallProfile;
        }
        return imsCallProfile;
    }

    private void setCallProfile(ImsCallProfile profile) {
        synchronized (this.mLockObj) {
            this.mCallProfile = profile;
            trackVideoStateHistory(this.mCallProfile);
        }
    }

    public ImsCallProfile getLocalCallProfile() throws ImsException {
        ImsCallProfile localCallProfile;
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                throw new ImsException("No call session", 148);
            }
            try {
                localCallProfile = this.mSession.getLocalCallProfile();
            } catch (Throwable t) {
                loge("getLocalCallProfile :: ", t);
                ImsException imsException = new ImsException("getLocalCallProfile()", t, 0);
            }
        }
        return localCallProfile;
    }

    public ImsCallProfile getRemoteCallProfile() throws ImsException {
        ImsCallProfile remoteCallProfile;
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                throw new ImsException("No call session", 148);
            }
            try {
                remoteCallProfile = this.mSession.getRemoteCallProfile();
            } catch (Throwable t) {
                loge("getRemoteCallProfile :: ", t);
                ImsException imsException = new ImsException("getRemoteCallProfile()", t, 0);
            }
        }
        return remoteCallProfile;
    }

    public ImsCallProfile getProposedCallProfile() {
        synchronized (this.mLockObj) {
            if (isInCall()) {
                ImsCallProfile imsCallProfile = this.mProposedCallProfile;
                return imsCallProfile;
            }
            return null;
        }
    }

    public List<ConferenceParticipant> getConferenceParticipants() {
        synchronized (this.mLockObj) {
            logi("getConferenceParticipants :: mConferenceParticipants" + this.mConferenceParticipants);
            List arrayList;
            if (this.mConferenceParticipants == null) {
                return null;
            } else if (this.mConferenceParticipants.isEmpty()) {
                arrayList = new ArrayList(0);
                return arrayList;
            } else {
                arrayList = new ArrayList(this.mConferenceParticipants);
                return arrayList;
            }
        }
    }

    public int getState() {
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                return 0;
            }
            int state = this.mSession.getState();
            return state;
        }
    }

    public ImsCallSession getCallSession() {
        ImsCallSession imsCallSession;
        synchronized (this.mLockObj) {
            imsCallSession = this.mSession;
        }
        return imsCallSession;
    }

    public ImsStreamMediaSession getMediaSession() {
        ImsStreamMediaSession imsStreamMediaSession;
        synchronized (this.mLockObj) {
            imsStreamMediaSession = this.mMediaSession;
        }
        return imsStreamMediaSession;
    }

    public String getCallExtra(String name) throws ImsException {
        String property;
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                throw new ImsException("No call session", 148);
            }
            try {
                property = this.mSession.getProperty(name);
            } catch (Throwable t) {
                loge("getCallExtra :: ", t);
                ImsException imsException = new ImsException("getCallExtra()", t, 0);
            }
        }
        return property;
    }

    public ImsReasonInfo getLastReasonInfo() {
        ImsReasonInfo imsReasonInfo;
        synchronized (this.mLockObj) {
            imsReasonInfo = this.mLastReasonInfo;
        }
        return imsReasonInfo;
    }

    public boolean hasPendingUpdate() {
        boolean z = DBG;
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean isPendingHold() {
        boolean z = true;
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 1) {
                z = DBG;
            }
        }
        return z;
    }

    public boolean isInCall() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mInCall;
        }
        return z;
    }

    public boolean isMuted() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mMute;
        }
        return z;
    }

    public boolean isOnHold() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mHold;
        }
        return z;
    }

    public boolean isMultiparty() {
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                return DBG;
            }
            boolean isMultiparty = this.mSession.isMultiparty();
            return isMultiparty;
        }
    }

    public boolean isConferenceHost() {
        boolean z;
        synchronized (this.mLockObj) {
            z = isMultiparty() ? this.mIsConferenceHost : DBG;
        }
        return z;
    }

    public void setIsMerged(boolean isMerged) {
        this.mIsMerged = isMerged;
    }

    public boolean isMerged() {
        return this.mIsMerged;
    }

    public void setListener(Listener listener) {
        setListener(listener, DBG);
    }

    /* JADX WARNING: Missing block: B:8:0x0014, code:
            if (r1 == null) goto L_0x001f;
     */
    /* JADX WARNING: Missing block: B:10:?, code:
            r8.onCallError(r7, r1);
     */
    /* JADX WARNING: Missing block: B:13:0x001b, code:
            return;
     */
    /* JADX WARNING: Missing block: B:17:0x001f, code:
            if (r0 == false) goto L_0x0033;
     */
    /* JADX WARNING: Missing block: B:18:0x0021, code:
            if (r2 == false) goto L_0x002f;
     */
    /* JADX WARNING: Missing block: B:20:?, code:
            r8.onCallHeld(r7);
     */
    /* JADX WARNING: Missing block: B:21:0x0027, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:0x0028, code:
            loge("setListener() :: ", r4);
     */
    /* JADX WARNING: Missing block: B:24:?, code:
            r8.onCallStarted(r7);
     */
    /* JADX WARNING: Missing block: B:25:0x0033, code:
            switch(r3) {
                case 3: goto L_0x0037;
                case 8: goto L_0x003b;
                default: goto L_0x0036;
            };
     */
    /* JADX WARNING: Missing block: B:26:0x0037, code:
            r8.onCallProgressing(r7);
     */
    /* JADX WARNING: Missing block: B:27:0x003b, code:
            r8.onCallTerminated(r7, r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setListener(Listener listener, boolean callbackImmediately) {
        synchronized (this.mLockObj) {
            this.mListener = listener;
            if (listener == null || !callbackImmediately) {
            } else {
                boolean inCall = this.mInCall;
                boolean onHold = this.mHold;
                int state = getState();
                ImsReasonInfo lastReasonInfo = this.mLastReasonInfo;
            }
        }
    }

    public void setMute(boolean muted) throws ImsException {
        synchronized (this.mLockObj) {
            if (this.mMute != muted) {
                logi("setMute :: turning mute " + (muted ? "on" : "off"));
                this.mMute = muted;
                try {
                    this.mSession.setMute(muted);
                } catch (Throwable t) {
                    loge("setMute :: ", t);
                    throwImsException(t, 0);
                }
            }
        }
    }

    public void attachSession(ImsCallSession session) throws ImsException {
        logi("attachSession :: session=" + session);
        synchronized (this.mLockObj) {
            this.mSession = session;
            try {
                this.mSession.setListener(createCallSessionListener());
            } catch (Throwable t) {
                loge("attachSession :: ", t);
                throwImsException(t, 0);
            }
        }
        return;
    }

    public void start(ImsCallSession session, String callee) throws ImsException {
        logi("start(1) :: session=" + session);
        synchronized (this.mLockObj) {
            this.mSession = session;
            try {
                session.setListener(createCallSessionListener());
                session.start(callee, this.mCallProfile);
                this.mAddress = callee;
            } catch (Throwable t) {
                loge("start(1) :: ", t);
                ImsException imsException = new ImsException("start(1)", t, 0);
            }
        }
    }

    public void start(ImsCallSession session, String[] participants) throws ImsException {
        logi("start(n) :: session=" + session);
        synchronized (this.mLockObj) {
            this.mSession = session;
            if (this.mCallProfile != null && this.mCallProfile.getCallExtraBoolean("conference")) {
                this.mIsConferenceHost = true;
            }
            try {
                session.setListener(createCallSessionListener());
                session.start(participants, this.mCallProfile);
            } catch (Throwable t) {
                loge("start(n) :: ", t);
                ImsException imsException = new ImsException("start(n)", t, 0);
            }
        }
    }

    public void accept(int callType) throws ImsException {
        accept(callType, new ImsStreamMediaProfile());
    }

    public void accept(int callType, ImsStreamMediaProfile profile) throws ImsException {
        logi("accept :: callType=" + callType + ", profile=" + profile);
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                throw new ImsException("No call to answer", 148);
            }
            try {
                this.mSession.accept(callType, profile);
                if (this.mInCall && this.mProposedCallProfile != null) {
                    if (DBG) {
                        logi("accept :: call profile will be updated");
                    }
                    this.mCallProfile = this.mProposedCallProfile;
                    trackVideoStateHistory(this.mCallProfile);
                    this.mProposedCallProfile = null;
                }
                if (this.mInCall && this.mUpdateRequest == 6) {
                    this.mUpdateRequest = 0;
                }
            } catch (Throwable t) {
                loge("accept :: ", t);
                ImsException imsException = new ImsException("accept()", t, 0);
            }
        }
    }

    public void reject(int reason) throws ImsException {
        logi("reject :: reason=" + reason);
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.reject(reason);
            }
            if (this.mInCall && this.mProposedCallProfile != null) {
                if (DBG) {
                    logi("reject :: call profile is not updated; destroy it...");
                }
                this.mProposedCallProfile = null;
            }
            if (this.mInCall && this.mUpdateRequest == 6) {
                this.mUpdateRequest = 0;
            }
        }
    }

    public void terminate(int reason, int overrideReason) throws ImsException {
        logi("terminate :: reason=" + reason + " ; overrideReadon=" + overrideReason);
        this.mOverrideReason = overrideReason;
        terminate(reason);
    }

    public void terminate(int reason) throws ImsException {
        logi("terminate :: reason=" + reason);
        synchronized (this.mLockObj) {
            this.mHold = DBG;
            this.mInCall = DBG;
            this.mTerminationRequestPending = true;
            if (this.mSession != null) {
                this.mSession.terminate(reason);
            }
        }
    }

    public void hold() throws ImsException {
        logi("hold :: ");
        if (isOnHold()) {
            if (DBG) {
                logi("hold :: call is already on hold");
            }
            return;
        }
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                loge("hold :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                throw new ImsException("Call update is in progress", 102);
            } else if (this.mSession == null) {
                throw new ImsException("No call session", 148);
            } else {
                this.mSession.hold(createHoldMediaProfile());
                this.mHold = true;
                this.mUpdateRequest = 1;
            }
        }
    }

    public void resume() throws ImsException {
        logi("resume :: ");
        if (isOnHold()) {
            synchronized (this.mLockObj) {
                if (this.mUpdateRequest != 0) {
                    loge("resume :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                    throw new ImsException("Call update is in progress", 102);
                } else if (this.mSession == null) {
                    loge("resume :: ");
                    throw new ImsException("No call session", 148);
                } else {
                    this.mUpdateRequest = 3;
                    this.mSession.resume(createResumeMediaProfile());
                }
            }
            return;
        }
        if (DBG) {
            logi("resume :: call is not being held");
        }
    }

    private void merge() throws ImsException {
        logi("merge :: ");
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                loge("merge :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                throw new ImsException("Call update is in progress", 102);
            } else if (this.mSession == null) {
                loge("merge :: no call session");
                throw new ImsException("No call session", 148);
            } else {
                if (this.mHold || this.mContext.getResources().getBoolean(17957002)) {
                    if (!(this.mMergePeer == null || this.mMergePeer.isMultiparty())) {
                        if (!isMultiparty()) {
                            this.mUpdateRequest = 4;
                            this.mMergePeer.mUpdateRequest = 4;
                        }
                    }
                    this.mSession.merge();
                } else {
                    this.mSession.hold(createHoldMediaProfile());
                    this.mHold = true;
                    this.mUpdateRequest = 2;
                }
            }
        }
    }

    public void merge(ImsCall bgCall) throws ImsException {
        logi("merge(1) :: bgImsCall=" + bgCall);
        if (bgCall == null) {
            throw new ImsException("No background call", ImsManager.INCOMING_CALL_RESULT_CODE);
        }
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                loge("merge :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                throw new ImsException("Call update is in progress", 102);
            }
            setCallSessionMergePending(true);
            bgCall.setCallSessionMergePending(true);
            if ((isMultiparty() || bgCall.isMultiparty()) && !isMultiparty()) {
                setMergeHost(bgCall);
            } else {
                setMergePeer(bgCall);
            }
        }
        if (isMultiparty()) {
            this.mMergeRequestedByConference = true;
        } else {
            logi("merge : mMergeRequestedByConference not set");
        }
        merge();
    }

    public void update(int callType, ImsStreamMediaProfile mediaProfile) throws ImsException {
        logi("update :: callType=" + callType + ", mediaProfile=" + mediaProfile);
        if (isOnHold()) {
            if (DBG) {
                logi("update :: call is on hold");
            }
            throw new ImsException("Not in a call to update call", 102);
        }
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                if (DBG) {
                    logi("update :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                }
                throw new ImsException("Call update is in progress", 102);
            } else if (this.mSession == null) {
                loge("update :: ");
                throw new ImsException("No call session", 148);
            } else {
                this.mSession.update(callType, mediaProfile);
                this.mUpdateRequest = 6;
            }
        }
    }

    public void extendToConference(String[] participants) throws ImsException {
        logi("extendToConference ::");
        if (isOnHold()) {
            if (DBG) {
                logi("extendToConference :: call is on hold");
            }
            throw new ImsException("Not in a call to extend a call to conference", 102);
        }
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                logi("extendToConference :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                throw new ImsException("Call update is in progress", 102);
            } else if (this.mSession == null) {
                loge("extendToConference :: ");
                throw new ImsException("No call session", 148);
            } else {
                this.mSession.extendToConference(participants);
                this.mUpdateRequest = 5;
            }
        }
    }

    public void inviteParticipants(String[] participants) throws ImsException {
        logi("inviteParticipants ::");
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                loge("inviteParticipants :: ");
                throw new ImsException("No call session", 148);
            } else {
                this.mSession.inviteParticipants(participants);
            }
        }
    }

    public void removeParticipants(String[] participants) throws ImsException {
        logi("removeParticipants :: session=" + this.mSession);
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                loge("removeParticipants :: ");
                throw new ImsException("No call session", 148);
            } else {
                this.mSession.removeParticipants(participants);
            }
        }
    }

    public void sendDtmf(char c, Message result) {
        logi("sendDtmf :: code=" + c);
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.sendDtmf(c, result);
            }
        }
    }

    public void startDtmf(char c) {
        logi("startDtmf :: code=" + c);
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.startDtmf(c);
            }
        }
    }

    public void stopDtmf() {
        logi("stopDtmf :: ");
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.stopDtmf();
            }
        }
    }

    public void sendUssd(String ussdMessage) throws ImsException {
        logi("sendUssd :: ussdMessage=" + ussdMessage);
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                loge("sendUssd :: ");
                throw new ImsException("No call session", 148);
            } else {
                this.mSession.sendUssd(ussdMessage);
            }
        }
    }

    private void clear(ImsReasonInfo lastReasonInfo) {
        this.mInCall = DBG;
        this.mHold = DBG;
        this.mUpdateRequest = 0;
        this.mLastReasonInfo = lastReasonInfo;
    }

    private com.android.ims.internal.ImsCallSession.Listener createCallSessionListener() {
        this.mImsCallSessionListenerProxy = new ImsCallSessionListenerProxy();
        return this.mImsCallSessionListenerProxy;
    }

    public ImsCallSessionListenerProxy getImsCallSessionListenerProxy() {
        return this.mImsCallSessionListenerProxy;
    }

    private ImsCall createNewCall(ImsCallSession session, ImsCallProfile profile) {
        ImsCall call = new ImsCall(this.mContext, profile);
        try {
            call.attachSession(session);
            return call;
        } catch (ImsException e) {
            if (call == null) {
                return call;
            }
            call.close();
            return null;
        }
    }

    private ImsStreamMediaProfile createHoldMediaProfile() {
        ImsStreamMediaProfile mediaProfile = new ImsStreamMediaProfile();
        if (this.mCallProfile == null) {
            return mediaProfile;
        }
        mediaProfile.mAudioQuality = this.mCallProfile.mMediaProfile.mAudioQuality;
        mediaProfile.mVideoQuality = this.mCallProfile.mMediaProfile.mVideoQuality;
        mediaProfile.mAudioDirection = 2;
        if (mediaProfile.mVideoQuality != 0) {
            mediaProfile.mVideoDirection = 2;
        }
        return mediaProfile;
    }

    private ImsStreamMediaProfile createResumeMediaProfile() {
        ImsStreamMediaProfile mediaProfile = new ImsStreamMediaProfile();
        if (this.mCallProfile == null) {
            return mediaProfile;
        }
        mediaProfile.mAudioQuality = this.mCallProfile.mMediaProfile.mAudioQuality;
        mediaProfile.mVideoQuality = this.mCallProfile.mMediaProfile.mVideoQuality;
        mediaProfile.mAudioDirection = 3;
        if (mediaProfile.mVideoQuality != 0) {
            mediaProfile.mVideoDirection = 3;
        }
        return mediaProfile;
    }

    private void enforceConversationMode() {
        if (this.mInCall) {
            this.mHold = DBG;
            this.mUpdateRequest = 0;
        }
    }

    private void mergeInternal() {
        logi("mergeInternal :: ");
        this.mSession.merge();
        this.mUpdateRequest = 4;
    }

    private void notifyConferenceSessionTerminated(ImsReasonInfo reasonInfo) {
        Listener listener = this.mListener;
        clear(reasonInfo);
        if (listener != null) {
            try {
                listener.onCallTerminated(this, reasonInfo);
            } catch (Throwable t) {
                loge("notifyConferenceSessionTerminated :: ", t);
            }
        }
    }

    private void notifyConferenceStateUpdated(ImsConferenceState state) {
        if (state != null && state.mParticipants != null) {
            Set<Entry<String, Bundle>> participants = state.mParticipants.entrySet();
            if (participants != null) {
                this.mConferenceParticipants = new CopyOnWriteArrayList();
                for (Entry<String, Bundle> entry : participants) {
                    String key = (String) entry.getKey();
                    Bundle confInfo = (Bundle) entry.getValue();
                    String status = confInfo.getString("status");
                    String user = confInfo.getString("user");
                    String displayName = confInfo.getString("display-text");
                    String endpoint = confInfo.getString("endpoint");
                    logi("notifyConferenceStateUpdated :: key=" + key + ", status=" + status + ", user=" + user + ", displayName= " + displayName + ", endpoint=" + endpoint);
                    Uri handle = Uri.parse(user);
                    if (endpoint == null) {
                        endpoint = "";
                    }
                    Uri endpointUri = Uri.parse(endpoint);
                    int connectionState = ImsConferenceState.getConnectionStateForStatus(status);
                    if (connectionState != 6) {
                        this.mConferenceParticipants.add(new ConferenceParticipant(handle, displayName, endpointUri, connectionState));
                    }
                }
                if (!(this.mConferenceParticipants == null || this.mListener == null)) {
                    try {
                        this.mListener.onConferenceParticipantsStateChanged(this, this.mConferenceParticipants);
                    } catch (Throwable t) {
                        loge("notifyConferenceStateUpdated :: ", t);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:19:0x004e, code:
            if (r0 == null) goto L_0x0053;
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            r0.onCallTerminated(r4, r5);
     */
    /* JADX WARNING: Missing block: B:26:0x0057, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:27:0x0058, code:
            loge("processCallTerminated :: ", r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processCallTerminated(ImsReasonInfo reasonInfo) {
        logi("processCallTerminated :: reason=" + reasonInfo + " userInitiated = " + this.mTerminationRequestPending);
        synchronized (this) {
            if (isCallSessionMergePending() && !this.mTerminationRequestPending) {
                logi("processCallTerminated :: burying termination during ongoing merge.");
                this.mSessionEndDuringMerge = true;
                this.mSessionEndDuringMergeReasonInfo = reasonInfo;
            } else if (isMultiparty()) {
                notifyConferenceSessionTerminated(reasonInfo);
            } else {
                Listener listener = this.mListener;
                clear(reasonInfo);
            }
        }
    }

    private boolean isTransientConferenceSession(ImsCallSession session) {
        if (session == null || session == this.mSession || session != this.mTransientConferenceSession) {
            return DBG;
        }
        return true;
    }

    private void setTransientSessionAsPrimary(ImsCallSession transientSession) {
        synchronized (this) {
            if (this.mSession != null) {
                this.mSession.setListener(null);
                this.mSession.close();
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

    private void markCallAsMerged(boolean playDisconnectTone) {
        if (!isSessionAlive(this.mSession)) {
            String reasonInfo;
            logi("markCallAsMerged");
            setIsMerged(playDisconnectTone ? DBG : true);
            this.mSessionEndDuringMerge = true;
            if (playDisconnectTone) {
                reasonInfo = "Call ended by network";
            } else {
                reasonInfo = "Call ended during conference merge process.";
            }
            this.mSessionEndDuringMergeReasonInfo = new ImsReasonInfo(0, 0, reasonInfo);
        }
    }

    public boolean isMergeRequestedByConf() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mMergeRequestedByConference;
        }
        return z;
    }

    public void resetIsMergeRequestedByConf(boolean value) {
        synchronized (this.mLockObj) {
            this.mMergeRequestedByConference = value;
        }
    }

    public ImsCallSession getSession() {
        ImsCallSession imsCallSession;
        synchronized (this.mLockObj) {
            imsCallSession = this.mSession;
        }
        return imsCallSession;
    }

    /* JADX WARNING: Missing block: B:15:0x0045, code:
            if (r2 == null) goto L_0x0056;
     */
    /* JADX WARNING: Missing block: B:17:?, code:
            r2.onCallMerged(r0, r1, r3);
     */
    /* JADX WARNING: Missing block: B:57:0x0125, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:58:0x0126, code:
            loge("processMergeComplete :: ", r4);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processMergeComplete() {
        ImsCall finalHostCall;
        Listener listener;
        logi("processMergeComplete :: ");
        if (isMergeHost()) {
            boolean swapRequired = DBG;
            synchronized (this) {
                ImsCall finalPeerCall;
                if (isMultiparty()) {
                    setIsMerged(DBG);
                    if (!this.mMergeRequestedByConference) {
                        this.mHold = DBG;
                        swapRequired = true;
                    }
                    this.mMergePeer.markCallAsMerged(DBG);
                    finalHostCall = this;
                    finalPeerCall = this.mMergePeer;
                } else if (this.mTransientConferenceSession == null) {
                    loge("processMergeComplete :: No transient session!");
                    return;
                } else if (this.mMergePeer == null) {
                    loge("processMergeComplete :: No merge peer!");
                    return;
                } else {
                    ImsCallSession transientConferenceSession = this.mTransientConferenceSession;
                    this.mTransientConferenceSession = null;
                    transientConferenceSession.setListener(null);
                    if (isSessionAlive(this.mSession) && !isSessionAlive(this.mMergePeer.getCallSession())) {
                        this.mMergePeer.mHold = DBG;
                        this.mHold = true;
                        if (!(this.mConferenceParticipants == null || this.mConferenceParticipants.isEmpty())) {
                            this.mMergePeer.mConferenceParticipants = this.mConferenceParticipants;
                        }
                        finalHostCall = this.mMergePeer;
                        finalPeerCall = this;
                        swapRequired = true;
                        setIsMerged(DBG);
                        this.mMergePeer.setIsMerged(DBG);
                        logi("processMergeComplete :: transient will transfer to merge peer");
                    } else if (isSessionAlive(this.mSession) || !isSessionAlive(this.mMergePeer.getCallSession())) {
                        finalHostCall = this;
                        finalPeerCall = this.mMergePeer;
                        this.mMergePeer.markCallAsMerged(DBG);
                        swapRequired = DBG;
                        setIsMerged(DBG);
                        this.mMergePeer.setIsMerged(true);
                        logi("processMergeComplete :: transient will stay with us (I'm the host).");
                    } else {
                        finalHostCall = this;
                        finalPeerCall = this.mMergePeer;
                        swapRequired = DBG;
                        setIsMerged(DBG);
                        this.mMergePeer.setIsMerged(DBG);
                        logi("processMergeComplete :: transient will stay with the merge host");
                    }
                    logi("processMergeComplete :: call=" + finalHostCall + " is the final host");
                    finalHostCall.setTransientSessionAsPrimary(transientConferenceSession);
                }
                listener = finalHostCall.mListener;
                updateCallProfile(finalPeerCall);
                updateCallProfile(finalHostCall);
                clearMergeInfo();
                finalPeerCall.notifySessionTerminatedDuringMerge();
                finalHostCall.clearSessionTerminationFlags();
                finalHostCall.mIsConferenceHost = true;
            }
        } else {
            loge("processMergeComplete :: We are not the merge host!");
            return;
        }
        if (!(this.mConferenceParticipants == null || this.mConferenceParticipants.isEmpty())) {
            try {
                listener.onConferenceParticipantsStateChanged(finalHostCall, this.mConferenceParticipants);
            } catch (Throwable t) {
                loge("processMergeComplete :: ", t);
            }
        }
    }

    private static void updateCallProfile(ImsCall call) {
        if (call != null) {
            call.updateCallProfile();
        }
    }

    private void updateCallProfile() {
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                setCallProfile(this.mSession.getCallProfile());
            }
        }
    }

    private void notifySessionTerminatedDuringMerge() {
        Listener listener;
        boolean notifyFailure = DBG;
        ImsReasonInfo notifyFailureReasonInfo = null;
        synchronized (this) {
            listener = this.mListener;
            if (this.mSessionEndDuringMerge) {
                logi("notifySessionTerminatedDuringMerge ::reporting terminate during merge");
                notifyFailure = true;
                notifyFailureReasonInfo = this.mSessionEndDuringMergeReasonInfo;
            }
            clearSessionTerminationFlags();
        }
        if (listener != null && notifyFailure) {
            try {
                processCallTerminated(notifyFailureReasonInfo);
            } catch (Throwable t) {
                loge("notifySessionTerminatedDuringMerge :: ", t);
            }
        }
    }

    private void clearSessionTerminationFlags() {
        this.mSessionEndDuringMerge = DBG;
        this.mSessionEndDuringMergeReasonInfo = null;
    }

    /* JADX WARNING: Missing block: B:17:0x0063, code:
            if (r0 == null) goto L_0x0068;
     */
    /* JADX WARNING: Missing block: B:19:?, code:
            r0.onCallMergeFailed(r4, r5);
     */
    /* JADX WARNING: Missing block: B:26:0x0073, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:27:0x0074, code:
            loge("processMergeFailed :: ", r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processMergeFailed(ImsReasonInfo reasonInfo) {
        logi("processMergeFailed :: reason=" + reasonInfo);
        synchronized (this) {
            if (isMergeHost()) {
                if (this.mTransientConferenceSession != null) {
                    this.mTransientConferenceSession.setListener(null);
                    this.mTransientConferenceSession = null;
                }
                Listener listener = this.mListener;
                setIsMerged(DBG);
                markCallAsMerged(true);
                setCallSessionMergePending(DBG);
                notifySessionTerminatedDuringMerge();
                if (this.mMergePeer != null) {
                    this.mMergePeer.setIsMerged(DBG);
                    this.mMergePeer.markCallAsMerged(true);
                    this.mMergePeer.setCallSessionMergePending(DBG);
                    this.mMergePeer.notifySessionTerminatedDuringMerge();
                } else {
                    loge("processMergeFailed :: No merge peer!");
                }
                clearMergeInfo();
            } else {
                loge("processMergeFailed :: We are not the merge host!");
            }
        }
    }

    public void conferenceStateUpdated(ImsConferenceState state) {
        Listener listener;
        synchronized (this) {
            notifyConferenceStateUpdated(state);
            listener = this.mListener;
        }
        if (listener != null) {
            try {
                listener.onCallConferenceStateUpdated(this, state);
            } catch (Throwable t) {
                loge("callSessionConferenceStateUpdated :: ", t);
            }
        }
    }

    private String updateRequestToString(int updateRequest) {
        switch (updateRequest) {
            case 0:
                return "NONE";
            case 1:
                return "HOLD";
            case 2:
                return "HOLD_MERGE";
            case 3:
                return "RESUME";
            case 4:
                return "MERGE";
            case 5:
                return "EXTEND_TO_CONFERENCE";
            case 6:
                return "UNSPECIFIED";
            case 7:
                return "ECT";
            default:
                return "UNKNOWN";
        }
    }

    private void clearMergeInfo() {
        logi("clearMergeInfo :: clearing all merge info");
        if (this.mMergeHost != null) {
            this.mMergeHost.mMergePeer = null;
            this.mMergeHost.mUpdateRequest = 0;
            this.mMergeHost.mCallSessionMergePending = DBG;
        }
        if (this.mMergePeer != null) {
            this.mMergePeer.mMergeHost = null;
            this.mMergePeer.mUpdateRequest = 0;
            this.mMergePeer.mCallSessionMergePending = DBG;
        }
        this.mMergeHost = null;
        this.mMergePeer = null;
        this.mUpdateRequest = 0;
        this.mCallSessionMergePending = DBG;
    }

    private void setMergePeer(ImsCall mergePeer) {
        this.mMergePeer = mergePeer;
        this.mMergeHost = null;
        mergePeer.mMergeHost = this;
        mergePeer.mMergePeer = null;
    }

    public void setMergeHost(ImsCall mergeHost) {
        this.mMergeHost = mergeHost;
        this.mMergePeer = null;
        mergeHost.mMergeHost = null;
        mergeHost.mMergePeer = this;
    }

    private boolean isMerging() {
        return (this.mMergePeer == null && this.mMergeHost == null) ? DBG : true;
    }

    private boolean isMergeHost() {
        return (this.mMergePeer == null || this.mMergeHost != null) ? DBG : true;
    }

    private boolean isMergePeer() {
        return (this.mMergePeer != null || this.mMergeHost == null) ? DBG : true;
    }

    private boolean isCallSessionMergePending() {
        return this.mCallSessionMergePending;
    }

    private void setCallSessionMergePending(boolean callSessionMergePending) {
        this.mCallSessionMergePending = callSessionMergePending;
    }

    private boolean shouldProcessConferenceResult() {
        boolean areMergeTriggersDone = DBG;
        synchronized (this) {
            if (isMergeHost() || isMergePeer()) {
                if (isMergeHost()) {
                    logi("shouldProcessConferenceResult :: We are a merge host");
                    logi("shouldProcessConferenceResult :: Here is the merge peer=" + this.mMergePeer);
                    areMergeTriggersDone = !isCallSessionMergePending() ? this.mMergePeer.isCallSessionMergePending() ? DBG : true : DBG;
                    if (!isMultiparty()) {
                        areMergeTriggersDone &= isSessionAlive(this.mTransientConferenceSession);
                    }
                } else if (isMergePeer()) {
                    logi("shouldProcessConferenceResult :: We are a merge peer");
                    logi("shouldProcessConferenceResult :: Here is the merge host=" + this.mMergeHost);
                    areMergeTriggersDone = !isCallSessionMergePending() ? this.mMergeHost.isCallSessionMergePending() ? DBG : true : DBG;
                    areMergeTriggersDone = !this.mMergeHost.isMultiparty() ? areMergeTriggersDone & isSessionAlive(this.mMergeHost.mTransientConferenceSession) : isCallSessionMergePending() ? DBG : true;
                } else {
                    loge("shouldProcessConferenceResult : merge in progress but call is neither host nor peer.");
                }
                logi("shouldProcessConferenceResult :: returning:" + (areMergeTriggersDone ? ImsManager.TRUE : ImsManager.FALSE));
                return areMergeTriggersDone;
            }
            loge("shouldProcessConferenceResult :: no merge in progress");
            return DBG;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ImsCall objId:");
        sb.append(System.identityHashCode(this));
        sb.append(" onHold:");
        sb.append(isOnHold() ? "Y" : "N");
        sb.append(" mute:");
        sb.append(isMuted() ? "Y" : "N");
        if (this.mCallProfile != null) {
            sb.append(" tech:");
            sb.append(this.mCallProfile.getCallExtra("CallRadioTech"));
        }
        sb.append(" updateRequest:");
        sb.append(updateRequestToString(this.mUpdateRequest));
        sb.append(" merging:");
        sb.append(isMerging() ? "Y" : "N");
        if (isMerging()) {
            if (isMergePeer()) {
                sb.append("P");
            } else {
                sb.append("H");
            }
        }
        sb.append(" merge action pending:");
        sb.append(isCallSessionMergePending() ? "Y" : "N");
        sb.append(" merged:");
        sb.append(isMerged() ? "Y" : "N");
        sb.append(" multiParty:");
        sb.append(isMultiparty() ? "Y" : "N");
        sb.append(" confHost:");
        sb.append(isConferenceHost() ? "Y" : "N");
        sb.append(" buried term:");
        sb.append(this.mSessionEndDuringMerge ? "Y" : "N");
        sb.append(" wasVideo: ");
        sb.append(this.mWasVideoCall ? "Y" : "N");
        sb.append(" session:");
        sb.append(this.mSession);
        sb.append(" transientSession:");
        sb.append(this.mTransientConferenceSession);
        sb.append("]");
        return sb.toString();
    }

    private void throwImsException(Throwable t, int code) throws ImsException {
        if (t instanceof ImsException) {
            throw ((ImsException) t);
        }
        throw new ImsException(String.valueOf(code), t, code);
    }

    private String appendImsCallInfoToString(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append(s);
        sb.append(" ImsCall=");
        sb.append(this);
        return sb.toString();
    }

    private void trackVideoStateHistory(ImsCallProfile profile) {
        this.mWasVideoCall = !this.mWasVideoCall ? profile.isVideoCall() : true;
    }

    public boolean wasVideoCall() {
        return this.mWasVideoCall;
    }

    public boolean isVideoCall() {
        boolean isVideoCall;
        synchronized (this.mLockObj) {
            isVideoCall = this.mCallProfile != null ? this.mCallProfile.isVideoCall() : DBG;
        }
        return isVideoCall;
    }

    /* JADX WARNING: Missing block: B:20:0x002f, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isWifiCall() {
        boolean z = DBG;
        synchronized (this.mLockObj) {
            if (this.mCallProfile == null) {
                return DBG;
            }
            int radioTechnology;
            String callType = this.mCallProfile.getCallExtra("CallRadioTech");
            if (callType == null || callType.isEmpty()) {
                callType = this.mCallProfile.getCallExtra("callRadioTech");
            }
            try {
                radioTechnology = Integer.parseInt(callType);
            } catch (NumberFormatException e) {
                radioTechnology = 0;
            }
            if (radioTechnology == 18) {
                z = true;
            }
        }
    }

    private void logi(String s) {
        Log.i(TAG, appendImsCallInfoToString(s));
    }

    private void logd(String s) {
        Log.d(TAG, appendImsCallInfoToString(s));
    }

    private void logv(String s) {
        Log.v(TAG, appendImsCallInfoToString(s));
    }

    private void loge(String s) {
        Log.e(TAG, appendImsCallInfoToString(s));
    }

    private void loge(String s, Throwable t) {
        Log.e(TAG, appendImsCallInfoToString(s), t);
    }

    public boolean isIncomingCallMultiparty() {
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                return DBG;
            }
            boolean isIncomingCallMultiparty = this.mSession.isIncomingCallMultiparty();
            return isIncomingCallMultiparty;
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
            } else {
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
            } else {
                this.mSession.unattendedCallTransfer(number, type);
                this.mUpdateRequest = 7;
            }
        }
    }
}
