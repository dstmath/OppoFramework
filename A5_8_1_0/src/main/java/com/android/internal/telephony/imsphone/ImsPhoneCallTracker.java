package com.android.internal.telephony.imsphone;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.radio.V1_0.LastCallFailCause;
import android.hardware.radio.V1_0.RadioError;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest.Builder;
import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.telecom.ConferenceParticipant;
import android.telecom.Connection.VideoProvider;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.VoLteServiceState;
import android.telephony.ims.ImsServiceProxy.INotifyStatusChanged;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import android.util.SparseIntArray;
import com.android.ims.ImsCall;
import com.android.ims.ImsCall.Listener;
import com.android.ims.ImsCallProfile;
import com.android.ims.ImsConfigListener.Stub;
import com.android.ims.ImsConnectionStateListener;
import com.android.ims.ImsEcbm;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.ims.ImsMultiEndpoint;
import com.android.ims.ImsReasonInfo;
import com.android.ims.ImsStreamMediaProfile;
import com.android.ims.ImsSuppServiceNotification;
import com.android.ims.ImsUtInterface;
import com.android.ims.internal.IImsVideoCallProvider;
import com.android.ims.internal.ImsCallSession;
import com.android.ims.internal.ImsVideoCallProviderWrapper;
import com.android.internal.os.SomeArgs;
import com.android.internal.telephony.Call.SrvccState;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.GsmCdmaCallTracker;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.SpnOverride;
import com.google.android.mms.pdu.CharacterSets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import org.codeaurora.ims.QtiCarrierConfigs;
import org.codeaurora.ims.utils.QtiImsExtUtils;

public class ImsPhoneCallTracker extends CallTracker implements ImsPullCall {
    private static final int CEILING_SERVICE_RETRY_COUNT = 6;
    private static final boolean DBG = true;
    private static final int EVENT_CHECK_FOR_WIFI_HANDOVER = 25;
    private static final int EVENT_DATA_ENABLED_CHANGED = 23;
    private static final int EVENT_DIAL_PENDINGMO = 20;
    private static final int EVENT_EXIT_ECBM_BEFORE_PENDINGMO = 21;
    private static final int EVENT_GET_IMS_SERVICE = 24;
    private static final int EVENT_HANGUP_PENDINGMO = 18;
    private static final int EVENT_ON_FEATURE_CAPABILITY_CHANGED = 26;
    private static final int EVENT_OPPO_PENGDING_HANGUP = 101;
    private static final int EVENT_RESUME_BACKGROUND = 19;
    private static final int EVENT_SUPP_SERVICE_INDICATION = 27;
    private static final int EVENT_VT_DATA_USAGE_UPDATE = 22;
    private static final boolean FORCE_VERBOSE_STATE_LOGGING = false;
    private static final int HANDOVER_TO_WIFI_TIMEOUT_MS = 60000;
    private static final int IMS_RETRY_STARTING_TIMEOUT_MS = 500;
    private static final String IMS_VOLTE_ENABLE = "volte";
    private static final String IMS_VOWIFI_ENABLE = "vowifi";
    static final String LOG_TAG = "ImsPhoneCallTracker";
    static final int MAX_CONNECTIONS = 7;
    static final int MAX_CONNECTIONS_PER_CALL = 5;
    private static final SparseIntArray PRECISE_CAUSE_MAP = new SparseIntArray();
    private static final String PRO_IMS_TYPE = "gsm.ims.type";
    private static final int TIMEOUT_HANGUP_PENDINGMO = 500;
    private static final int TIME_OPPO_PENGDING_HANGUP = 500;
    private static final boolean VERBOSE_STATE_LOGGING = Rlog.isLoggable(VERBOSE_STATE_TAG, 2);
    static final String VERBOSE_STATE_TAG = "IPCTState";
    private Message mAddPartResp;
    private Object mAddParticipantLock = new Object();
    private boolean mAllowAddCallDuringVideoCall = true;
    private boolean mAllowEmergencyVideoCalls = false;
    private boolean mAllowHoldingVideoCall = true;
    public ImsPhoneCall mBackgroundCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_BACKGROUND);
    private ImsCall mCallExpectedToResume = null;
    private boolean mCarrierConfigLoaded = false;
    private int mClirMode = 0;
    private ArrayList<ImsPhoneConnection> mConnections = new ArrayList();
    private final AtomicInteger mDefaultDialerUid = new AtomicInteger(-1);
    private boolean mDesiredMute = false;
    private boolean mDropVideoCallWhenAnsweringAudioCall = false;
    public ImsPhoneCall mForegroundCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_FOREGROUND);
    public ImsPhoneCall mHandoverCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_HANDOVER);
    private boolean mHasPerformedStartOfCallHandover = false;
    private boolean mIgnoreDataEnabledChangedForVideoCalls = false;
    private boolean mIgnoreResetUtCapability = false;
    private Listener mImsCallListener = new Listener() {
        public void onCallProgressing(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallProgressing");
            ImsPhoneCallTracker.this.mPendingMO = null;
            ImsPhoneCallTracker.this.processPendingHangup("onCallProgressing");
            ImsPhoneCallTracker.this.processCallStateChange(imsCall, State.ALERTING, 0);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallProgressing(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
        }

        public void onCallStarted(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallStarted");
            if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && ImsPhoneCallTracker.this.mCallExpectedToResume != null && ImsPhoneCallTracker.this.mCallExpectedToResume == imsCall) {
                ImsPhoneCallTracker.this.log("onCallStarted: starting a call as a result of a switch.");
                ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                ImsPhoneCallTracker.this.mCallExpectedToResume = null;
            }
            ImsPhoneCallTracker.this.mPendingMO = null;
            ImsPhoneCallTracker.this.processCallStateChange(imsCall, State.ACTIVE, 0);
            if (ImsPhoneCallTracker.this.mNotifyVtHandoverToWifiFail && imsCall.isVideoCall() && (imsCall.isWifiCall() ^ 1) != 0) {
                if (ImsPhoneCallTracker.this.isWifiConnected()) {
                    ImsPhoneCallTracker.this.sendMessageDelayed(ImsPhoneCallTracker.this.obtainMessage(25, imsCall), 60000);
                } else {
                    ImsPhoneCallTracker.this.registerForConnectivityChanges();
                }
            }
            ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover = false;
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallStarted(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
        }

        public void onCallUpdated(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallUpdated");
            if (imsCall != null) {
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    ImsPhoneCallTracker.this.processCallStateChange(imsCall, conn.getCall().mState, 0, true);
                    ImsPhoneCallTracker.this.mMetrics.writeImsCallState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), conn.getCall().mState);
                }
            }
        }

        public void onCallStartFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("onCallStartFailed reasonCode=" + reasonInfo.getCode());
            if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && ImsPhoneCallTracker.this.mCallExpectedToResume != null && ImsPhoneCallTracker.this.mCallExpectedToResume == imsCall) {
                ImsPhoneCallTracker.this.log("onCallStarted: starting a call as a result of a switch.");
                ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                ImsPhoneCallTracker.this.mCallExpectedToResume = null;
            }
            ImsPhoneCallTracker.this.processPendingHangup("onCallStartFailed");
            if (ImsPhoneCallTracker.this.mPendingMO != null) {
                if (reasonInfo.getCode() == 146 && ImsPhoneCallTracker.this.mBackgroundCall.getState() == State.IDLE && ImsPhoneCallTracker.this.mRingingCall.getState() == State.IDLE) {
                    ImsPhoneCallTracker.this.mForegroundCall.detach(ImsPhoneCallTracker.this.mPendingMO);
                    ImsPhoneCallTracker.this.removeConnection(ImsPhoneCallTracker.this.mPendingMO);
                    ImsPhoneCallTracker.this.mPendingMO.finalize();
                    ImsPhoneCallTracker.this.mPendingMO = null;
                    ImsPhoneCallTracker.this.mPhone.initiateSilentRedial();
                    return;
                }
                State callState;
                ImsPhoneCallTracker.this.mPendingMO = null;
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    callState = conn.getState();
                } else {
                    callState = State.DIALING;
                }
                int cause = ImsPhoneCallTracker.this.getDisconnectCauseFromReasonInfo(reasonInfo, callState);
                if (conn != null) {
                    conn.setPreciseDisconnectCause(ImsPhoneCallTracker.this.getPreciseDisconnectCauseFromReasonInfo(reasonInfo));
                }
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, State.DISCONNECTED, cause);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallStartFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
            }
        }

        public void onCallTerminated(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            State callState;
            ImsPhoneCallTracker.this.log("onCallTerminated reasonCode=" + reasonInfo.getCode());
            if (ImsPhoneCallTracker.this.mPhone.mDefaultPhone.getServiceStateTracker() != null) {
                ImsPhoneCallTracker.this.mPhone.mDefaultPhone.getServiceStateTracker().oppoResetOosDelayState();
            }
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                callState = conn.getState();
            } else {
                callState = State.ACTIVE;
            }
            int cause = ImsPhoneCallTracker.this.getDisconnectCauseFromReasonInfo(reasonInfo, callState);
            ImsPhoneCallTracker.this.log("cause = " + cause + " conn = " + conn);
            if (conn != null) {
                VideoProvider videoProvider = conn.getVideoProvider();
                if (videoProvider instanceof ImsVideoCallProviderWrapper) {
                    ((ImsVideoCallProviderWrapper) videoProvider).removeImsVideoProviderCallback(conn);
                }
            }
            if (ImsPhoneCallTracker.this.mOnHoldToneId == System.identityHashCode(conn)) {
                if (conn != null && ImsPhoneCallTracker.this.mOnHoldToneStarted) {
                    ImsPhoneCallTracker.this.mPhone.stopOnHoldTone(conn);
                }
                ImsPhoneCallTracker.this.mOnHoldToneStarted = false;
                ImsPhoneCallTracker.this.mOnHoldToneId = -1;
            }
            if (conn != null) {
                if (conn.isPulledCall() && ((reasonInfo.getCode() == CharacterSets.UTF_16 || reasonInfo.getCode() == 336 || reasonInfo.getCode() == 332) && ImsPhoneCallTracker.this.mPhone != null && ImsPhoneCallTracker.this.mPhone.getExternalCallTracker() != null)) {
                    ImsPhoneCallTracker.this.log("Call pull failed.");
                    conn.onCallPullFailed(ImsPhoneCallTracker.this.mPhone.getExternalCallTracker().getConnectionById(conn.getPulledDialogId()));
                    cause = 0;
                } else if (conn.isIncoming() && conn.getConnectTime() == 0 && cause != 52) {
                    cause = cause == 3 ? 16 : 1;
                }
            }
            if (cause == 2 && conn != null && conn.getImsCall().isMerged()) {
                cause = 45;
            }
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallTerminated(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
            if (conn != null) {
                conn.setPreciseDisconnectCause(ImsPhoneCallTracker.this.getPreciseDisconnectCauseFromReasonInfo(reasonInfo));
            }
            ImsPhoneCallTracker.this.processCallStateChange(imsCall, State.DISCONNECTED, cause);
            if (ImsPhoneCallTracker.this.mForegroundCall.getState() != State.ACTIVE) {
                if (ImsPhoneCallTracker.this.mRingingCall.getState().isRinging()) {
                    ImsPhoneCallTracker.this.mPendingMO = null;
                } else if (ImsPhoneCallTracker.this.mPendingMO != null) {
                    ImsPhoneCallTracker.this.sendEmptyMessage(20);
                }
            }
            if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls) {
                String str;
                ImsPhoneCallTracker.this.log("onCallTerminated: Call terminated in the midst of Switching Fg and Bg calls.");
                if (imsCall == ImsPhoneCallTracker.this.mCallExpectedToResume) {
                    ImsPhoneCallTracker.this.log("onCallTerminated: switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                    ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                }
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                StringBuilder append = new StringBuilder().append("onCallTerminated: foreground call in state ").append(ImsPhoneCallTracker.this.mForegroundCall.getState()).append(" and ringing call in state ");
                if (ImsPhoneCallTracker.this.mRingingCall == null) {
                    str = "null";
                } else {
                    str = ImsPhoneCallTracker.this.mRingingCall.getState().toString();
                }
                imsPhoneCallTracker.log(append.append(str).toString());
                if (ImsPhoneCallTracker.this.mForegroundCall.getState() == State.HOLDING || ImsPhoneCallTracker.this.mRingingCall.getState() == State.WAITING) {
                    ImsPhoneCallTracker.this.sendEmptyMessage(19);
                    ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                }
            }
            if (ImsPhoneCallTracker.this.mShouldUpdateImsConfigOnDisconnect) {
                ImsPhoneCallTracker.this.mImsManager.updateImsServiceConfigForSlot(true);
                ImsPhoneCallTracker.this.mShouldUpdateImsConfigOnDisconnect = false;
            }
        }

        public void onCallHeld(ImsCall imsCall) {
            if (ImsPhoneCallTracker.this.mForegroundCall.getImsCall() == imsCall) {
                ImsPhoneCallTracker.this.log("onCallHeld (fg) " + imsCall);
            } else if (ImsPhoneCallTracker.this.mBackgroundCall.getImsCall() == imsCall) {
                ImsPhoneCallTracker.this.log("onCallHeld (bg) " + imsCall);
            }
            synchronized (ImsPhoneCallTracker.this.mSyncHold) {
                State oldState = ImsPhoneCallTracker.this.mBackgroundCall.getState();
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, State.HOLDING, 0);
                if (oldState == State.ACTIVE) {
                    if (ImsPhoneCallTracker.this.mForegroundCall.getState() == State.HOLDING || ImsPhoneCallTracker.this.mRingingCall.getState() == State.WAITING) {
                        ImsPhoneCallTracker.this.sendEmptyMessage(19);
                    } else {
                        if (ImsPhoneCallTracker.this.mPendingMO != null) {
                            ImsPhoneCallTracker.this.dialPendingMO();
                        }
                        if (!(ImsPhoneCallTracker.this.mPendingMO != null || ImsPhoneCallTracker.this.mForegroundCall == null || ImsPhoneCallTracker.this.mForegroundCall.getState() != State.DISCONNECTED || ImsPhoneCallTracker.this.mRingingCall == null || (ImsPhoneCallTracker.this.mRingingCall.getState().isAlive() ^ 1) == 0)) {
                            ImsPhoneCallTracker.this.log("onCallHeld (bg) fg call is disconnected already, resume myself");
                            ImsPhoneCallTracker.this.sendEmptyMessage(19);
                        }
                        ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    }
                } else if (oldState == State.IDLE && ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && ImsPhoneCallTracker.this.mForegroundCall.getState() == State.HOLDING) {
                    ImsPhoneCallTracker.this.sendEmptyMessage(19);
                    ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                }
            }
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHeld(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
        }

        public void onCallHoldFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("onCallHoldFailed reasonCode=" + reasonInfo.getCode());
            synchronized (ImsPhoneCallTracker.this.mSyncHold) {
                State bgState = ImsPhoneCallTracker.this.mBackgroundCall.getState();
                if (reasonInfo.getCode() == 148) {
                    if (ImsPhoneCallTracker.this.mPendingMO != null) {
                        ImsPhoneCallTracker.this.dialPendingMO();
                    }
                } else if (bgState == State.ACTIVE) {
                    ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                    if (ImsPhoneCallTracker.this.mPendingMO != null) {
                        ImsPhoneCallTracker.this.mPendingMO.setDisconnectCause(36);
                        ImsPhoneCallTracker.this.sendEmptyMessageDelayed(18, 500);
                    }
                    if (imsCall != ImsPhoneCallTracker.this.mCallExpectedToResume) {
                        ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                    }
                }
                ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.HOLD);
            }
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHoldFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
        }

        public void onCallResumed(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallResumed");
            if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls) {
                if (imsCall != ImsPhoneCallTracker.this.mCallExpectedToResume) {
                    ImsPhoneCallTracker.this.log("onCallResumed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                    ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                } else {
                    ImsPhoneCallTracker.this.log("onCallResumed : expected call resumed.");
                }
                ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                ImsPhoneCallTracker.this.mCallExpectedToResume = null;
            }
            ImsPhoneCallTracker.this.processCallStateChange(imsCall, State.ACTIVE, 0);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
        }

        public void onCallResumeFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls) {
                if (imsCall == ImsPhoneCallTracker.this.mCallExpectedToResume) {
                    ImsPhoneCallTracker.this.log("onCallResumeFailed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                    ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                    if (ImsPhoneCallTracker.this.mForegroundCall.getState() == State.HOLDING) {
                        ImsPhoneCallTracker.this.sendEmptyMessage(19);
                    }
                }
                ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
            }
            ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.RESUME);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumeFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
        }

        public void onCallResumeReceived(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallResumeReceived");
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                if (ImsPhoneCallTracker.this.mOnHoldToneStarted) {
                    ImsPhoneCallTracker.this.mPhone.stopOnHoldTone(conn);
                    ImsPhoneCallTracker.this.mOnHoldToneStarted = false;
                }
                conn.onConnectionEvent("android.telecom.event.CALL_REMOTELY_UNHELD", null);
            }
            if (ImsPhoneCallTracker.this.mPhone.getContext().getResources().getBoolean(17957053) && ImsPhoneCallTracker.this.mSupportPauseVideo && VideoProfile.isVideo(conn.getVideoState())) {
                conn.changeToUnPausedState();
            }
            SuppServiceNotification supp = new SuppServiceNotification();
            supp.notificationType = 1;
            supp.code = 3;
            ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumeReceived(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
        }

        public void onCallHoldReceived(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallHoldReceived");
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                if (!ImsPhoneCallTracker.this.mOnHoldToneStarted && ImsPhoneCall.isLocalTone(imsCall) && conn.getState() == State.ACTIVE) {
                    ImsPhoneCallTracker.this.mPhone.startOnHoldTone(conn);
                    ImsPhoneCallTracker.this.mOnHoldToneStarted = true;
                    ImsPhoneCallTracker.this.mOnHoldToneId = System.identityHashCode(conn);
                }
                conn.onConnectionEvent("android.telecom.event.CALL_REMOTELY_HELD", null);
                if (ImsPhoneCallTracker.this.mPhone.getContext().getResources().getBoolean(17957053) && ImsPhoneCallTracker.this.mSupportPauseVideo && VideoProfile.isVideo(conn.getVideoState())) {
                    conn.changeToPausedState();
                }
            }
            SuppServiceNotification supp = new SuppServiceNotification();
            supp.notificationType = 1;
            supp.code = 2;
            ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHoldReceived(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
        }

        public void onCallSuppServiceReceived(ImsCall call, ImsSuppServiceNotification suppServiceInfo) {
            ImsPhoneCallTracker.this.log("onCallSuppServiceReceived: suppServiceInfo=" + suppServiceInfo);
            SuppServiceNotification supp = new SuppServiceNotification();
            supp.notificationType = suppServiceInfo.notificationType;
            supp.code = suppServiceInfo.code;
            supp.index = suppServiceInfo.index;
            supp.number = suppServiceInfo.number;
            supp.history = suppServiceInfo.history;
            ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
            ImsPhoneCallTracker.this.mPhone.getDefaultPhone().notifySuppService(supp);
        }

        public void onCallMerged(ImsCall call, ImsCall peerCall, boolean swapCalls) {
            ImsPhoneCall peerImsPhoneCall;
            ImsPhoneCallTracker.this.log("onCallMerged");
            ImsPhoneCall foregroundImsPhoneCall = ImsPhoneCallTracker.this.findConnection(call).getCall();
            ImsPhoneConnection peerConnection = ImsPhoneCallTracker.this.findConnection(peerCall);
            if (peerConnection == null) {
                peerImsPhoneCall = null;
            } else {
                peerImsPhoneCall = peerConnection.getCall();
            }
            if (swapCalls) {
                ImsPhoneCallTracker.this.switchAfterConferenceSuccess();
            }
            foregroundImsPhoneCall.merge(peerImsPhoneCall, State.ACTIVE);
            try {
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                ImsPhoneCallTracker.this.log("onCallMerged: ImsPhoneConnection=" + conn);
                ImsPhoneCallTracker.this.log("onCallMerged: CurrentVideoProvider=" + conn.getVideoProvider());
                ImsPhoneCallTracker.this.setVideoCallProvider(conn, call);
                ImsPhoneCallTracker.this.log("onCallMerged: CurrentVideoProvider=" + conn.getVideoProvider());
            } catch (Exception e) {
                ImsPhoneCallTracker.this.loge("onCallMerged: exception " + e);
            }
            ImsPhoneCallTracker.this.processCallStateChange(ImsPhoneCallTracker.this.mForegroundCall.getImsCall(), State.ACTIVE, 0);
            if (peerConnection != null) {
                ImsPhoneCallTracker.this.processCallStateChange(ImsPhoneCallTracker.this.mBackgroundCall.getImsCall(), State.HOLDING, 0);
                ImsPhoneCallTracker.this.log("onCallMerged: send call merge faild msg");
                ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.CONFERENCE);
            }
            if (call.isMergeRequestedByConf()) {
                ImsPhoneCallTracker.this.log("onCallMerged :: Merge requested by existing conference.");
                call.resetIsMergeRequestedByConf(false);
            } else {
                ImsPhoneCallTracker.this.log("onCallMerged :: calling onMultipartyStateChanged()");
                onMultipartyStateChanged(call, true);
            }
            ImsPhoneCallTracker.this.logState();
        }

        public void onCallMergeFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("onCallMergeFailed reasonInfo=" + reasonInfo);
            ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.CONFERENCE);
            call.resetIsMergeRequestedByConf(false);
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
            if (conn != null) {
                conn.onConferenceMergeFailed();
                conn.handleMergeComplete();
            }
        }

        public void onConferenceParticipantsStateChanged(ImsCall call, List<ConferenceParticipant> participants) {
            ImsPhoneCallTracker.this.log("onConferenceParticipantsStateChanged");
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
            if (conn != null) {
                conn.updateConferenceParticipants(participants);
            }
        }

        public void onCallSessionTtyModeReceived(ImsCall call, int mode) {
            ImsPhoneCallTracker.this.mPhone.onTtyModeReceived(mode);
        }

        public void onCallHandover(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("onCallHandover ::  srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            boolean isHandoverToWifi = (srcAccessTech == 0 || srcAccessTech == 18) ? false : targetAccessTech == 18;
            boolean isHandoverFromWifi = (srcAccessTech != 18 || targetAccessTech == 0) ? false : targetAccessTech != 18;
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                if (isHandoverToWifi && ImsPhoneCallTracker.this.mIsViLteDataMetered) {
                    conn.setVideoEnabled(true);
                }
                if (conn.getDisconnectCause() == 0) {
                    if (isHandoverToWifi) {
                        ImsPhoneCallTracker.this.removeMessages(25);
                        if (ImsPhoneCallTracker.this.mNotifyHandoverVideoFromLTEToWifi && ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover) {
                            conn.onConnectionEvent("android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_LTE_TO_WIFI", null);
                        }
                        ImsPhoneCallTracker.this.unregisterForConnectivityChanges();
                    } else if (isHandoverFromWifi && imsCall.isVideoCall()) {
                        ImsPhoneCallTracker.this.registerForConnectivityChanges();
                    }
                }
                if (isHandoverFromWifi && imsCall.isVideoCall()) {
                    if (ImsPhoneCallTracker.this.mIsViLteDataMetered) {
                        conn.setVideoEnabled(ImsPhoneCallTracker.this.mIsDataEnabled);
                    }
                    if (ImsPhoneCallTracker.this.mNotifyHandoverVideoFromWifiToLTE && ImsPhoneCallTracker.this.mIsDataEnabled) {
                        if (conn.getDisconnectCause() == 0) {
                            ImsPhoneCallTracker.this.log("onCallHandover :: notifying of WIFI to LTE handover.");
                            conn.onConnectionEvent("android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_WIFI_TO_LTE", null);
                        } else {
                            ImsPhoneCallTracker.this.log("onCallHandover :: skip notify of WIFI to LTE handover for disconnected call.");
                        }
                    }
                    if (!ImsPhoneCallTracker.this.mIsDataEnabled && ImsPhoneCallTracker.this.mIsViLteDataMetered) {
                        ImsPhoneCallTracker.this.downgradeVideoCall(1407, conn);
                    }
                }
            } else {
                ImsPhoneCallTracker.this.loge("onCallHandover :: connection null.");
            }
            if (!ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover) {
                ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover = true;
            }
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHandoverEvent(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 18, imsCall.getCallSession(), srcAccessTech, targetAccessTech, reasonInfo);
        }

        public void onCallHandoverFailed(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("onCallHandoverFailed :: srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHandoverEvent(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 19, imsCall.getCallSession(), srcAccessTech, targetAccessTech, reasonInfo);
            boolean isHandoverToWifi = srcAccessTech != 18 ? targetAccessTech == 18 : false;
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null && isHandoverToWifi) {
                ImsPhoneCallTracker.this.log("onCallHandoverFailed - handover to WIFI Failed");
                ImsPhoneCallTracker.this.removeMessages(25);
                if (imsCall.isVideoCall() && conn.getDisconnectCause() == 0) {
                    ImsPhoneCallTracker.this.registerForConnectivityChanges();
                }
                if (ImsPhoneCallTracker.this.mNotifyVtHandoverToWifiFail) {
                    conn.onHandoverToWifiFailed();
                }
            }
            if (!ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover) {
                ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover = true;
            }
        }

        public void onCallSessionMayHandover(ImsCall imsCall, int srcAccessTech, int targetAccessTech) {
            ImsPhoneCallTracker.this.log("callSessionMayHandover ::  srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech);
        }

        public void onRttModifyRequestReceived(ImsCall imsCall) {
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                conn.onRttModifyRequestReceived();
            }
        }

        public void onRttModifyResponseReceived(ImsCall imsCall, int status) {
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                conn.onRttModifyResponseReceived(status);
                if (status == 1) {
                    conn.startRttTextProcessing();
                }
            }
        }

        public void onRttMessageReceived(ImsCall imsCall, String message) {
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                conn.onRttMessageReceived(message);
            }
        }

        public void onMultipartyStateChanged(ImsCall imsCall, boolean isMultiParty) {
            ImsPhoneCallTracker.this.log("onMultipartyStateChanged to " + (isMultiParty ? "Y" : "N"));
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                conn.updateMultipartyState(isMultiParty);
            }
        }

        public void onCallInviteParticipantsRequestDelivered(ImsCall call) {
            ImsPhoneCallTracker.this.log("invite participants delivered");
            synchronized (ImsPhoneCallTracker.this.mAddParticipantLock) {
                ImsPhoneCallTracker.this.sendAddParticipantResponse(true, ImsPhoneCallTracker.this.mAddPartResp);
                ImsPhoneCallTracker.this.mAddPartResp = null;
            }
        }

        public void onCallInviteParticipantsRequestFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("invite participants failed.");
            synchronized (ImsPhoneCallTracker.this.mAddParticipantLock) {
                ImsPhoneCallTracker.this.sendAddParticipantResponse(false, ImsPhoneCallTracker.this.mAddPartResp);
                ImsPhoneCallTracker.this.mAddPartResp = null;
            }
        }

        public void onCallSessionRttMessageReceived(String rttMessage) {
            ImsPhoneCallTracker.this.log("onCallSessionRttMessageReceived");
        }

        public void onCallSessionRttModifyReceived(ImsCall call, ImsCallProfile profile) {
            ImsPhoneCallTracker.this.log("onCallSessionRttModifyReceived");
        }

        public void onCallSessionRttModifyResponseReceived(int status) {
            ImsPhoneCallTracker.this.log("onCallSessionRttModifyResponseReceived");
        }
    };
    private Stub mImsConfigListener = new Stub() {
        public void onGetFeatureResponse(int feature, int network, int value, int status) {
        }

        public void onSetFeatureResponse(int feature, int network, int value, int status) {
            ImsPhoneCallTracker.this.mMetrics.writeImsSetFeatureValue(ImsPhoneCallTracker.this.mPhone.getPhoneId(), feature, network, value, status);
        }

        public void onGetVideoQuality(int status, int quality) {
        }

        public void onSetVideoQuality(int status) {
        }
    };
    private ImsConnectionStateListener mImsConnectionStateListener = new ImsConnectionStateListener() {
        public void onImsConnected(int imsRadioTech) {
            ImsPhoneCallTracker.this.log("onImsConnected imsRadioTech=" + imsRadioTech);
            ImsPhoneCallTracker.this.mPhone.setServiceState(0);
            ImsPhoneCallTracker.this.mPhone.setImsRegistered(!ImsPhoneCallTracker.this.isVolteEnabled() ? ImsPhoneCallTracker.this.isVowifiEnabled() : true);
            ImsPhoneCallTracker.this.mPhone.notifyForVideoCapabilityChanged(ImsPhoneCallTracker.this.isVideoCallEnabled());
            ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 1, null);
        }

        public void onImsDisconnected(ImsReasonInfo imsReasonInfo) {
            ImsPhoneCallTracker.this.log("onImsDisconnected imsReasonInfo=" + imsReasonInfo);
            ImsPhoneCallTracker.this.resetImsCapabilities();
            ImsPhoneCallTracker.this.mPhone.setServiceState(1);
            ImsPhoneCallTracker.this.mPhone.setImsRegistered(false);
            ImsPhoneCallTracker.this.mPhone.processDisconnectReason(imsReasonInfo);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 3, imsReasonInfo);
        }

        public void onImsProgressing(int imsRadioTech) {
            ImsPhoneCallTracker.this.log("onImsProgressing imsRadioTech=" + imsRadioTech);
            ImsPhoneCallTracker.this.mPhone.setServiceState(1);
            ImsPhoneCallTracker.this.mPhone.setImsRegistered(false);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 2, null);
        }

        public void onImsResumed() {
            ImsPhoneCallTracker.this.log("onImsResumed");
            ImsPhoneCallTracker.this.mPhone.setServiceState(0);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 4, null);
        }

        public void onImsSuspended() {
            ImsPhoneCallTracker.this.log("onImsSuspended");
            ImsPhoneCallTracker.this.mPhone.setServiceState(1);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 5, null);
        }

        public void onFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) {
            ImsPhoneCallTracker.this.log("onFeatureCapabilityChanged");
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = serviceClass;
            args.arg1 = enabledFeatures;
            args.arg2 = disabledFeatures;
            ImsPhoneCallTracker.this.removeMessages(26);
            ImsPhoneCallTracker.this.obtainMessage(26, args).sendToTarget();
        }

        public void onVoiceMessageCountChanged(int count) {
            ImsPhoneCallTracker.this.log("onVoiceMessageCountChanged :: count=" + count);
            ImsPhoneCallTracker.this.mPhone.mDefaultPhone.setVoiceMessageCount(count);
        }

        public void registrationAssociatedUriChanged(Uri[] uris) {
            ImsPhoneCallTracker.this.log("registrationAssociatedUriChanged");
            ImsPhoneCallTracker.this.mPhone.setCurrentSubscriberUris(uris);
        }
    };
    private boolean[] mImsFeatureEnabled = new boolean[]{false, false, false, false, false, false};
    private final String[] mImsFeatureStrings = new String[]{"VoLTE", "ViLTE", "VoWiFi", "ViWiFi", "UTLTE", "UTWiFi"};
    private ImsManager mImsManager;
    private Map<Pair<Integer, String>, Integer> mImsReasonCodeMap = new ArrayMap();
    private int mImsServiceRetryCount;
    private Listener mImsUssdListener = new Listener() {
        public void onCallStarted(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("mImsUssdListener onCallStarted");
            if (imsCall == ImsPhoneCallTracker.this.mUssdSession && ImsPhoneCallTracker.this.mPendingUssd != null) {
                AsyncResult.forMessage(ImsPhoneCallTracker.this.mPendingUssd);
                ImsPhoneCallTracker.this.mPendingUssd.sendToTarget();
                ImsPhoneCallTracker.this.mPendingUssd = null;
            }
        }

        public void onCallStartFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("mImsUssdListener onCallStartFailed reasonCode=" + reasonInfo.getCode());
            onCallTerminated(imsCall, reasonInfo);
        }

        public void onCallTerminated(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("mImsUssdListener onCallTerminated reasonCode=" + reasonInfo.getCode());
            ImsPhoneCallTracker.this.removeMessages(25);
            ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover = false;
            ImsPhoneCallTracker.this.unregisterForConnectivityChanges();
            if (imsCall == ImsPhoneCallTracker.this.mUssdSession) {
                ImsPhoneCallTracker.this.mUssdSession = null;
                if (ImsPhoneCallTracker.this.mPendingUssd != null) {
                    AsyncResult.forMessage(ImsPhoneCallTracker.this.mPendingUssd, null, new CommandException(Error.GENERIC_FAILURE));
                    ImsPhoneCallTracker.this.mPendingUssd.sendToTarget();
                    ImsPhoneCallTracker.this.mPendingUssd = null;
                }
            }
            imsCall.close();
        }

        public void onCallUssdMessageReceived(ImsCall call, int mode, String ussdMessage) {
            ImsPhoneCallTracker.this.log("mImsUssdListener onCallUssdMessageReceived mode=" + mode);
            int ussdMode = -1;
            switch (mode) {
                case 0:
                    ussdMode = 0;
                    break;
                case 1:
                    ussdMode = 1;
                    break;
            }
            ImsPhoneCallTracker.this.mPhone.onIncomingUSSD(ussdMode, ussdMessage);
        }
    };
    private boolean mIsDataEnabled = false;
    private boolean mIsInEmergencyCall = false;
    private boolean mIsMonitoringConnectivity = false;
    private boolean mIsViLteDataMetered = false;
    private TelephonyMetrics mMetrics;
    private NetworkCallback mNetworkCallback = new NetworkCallback() {
        public void onAvailable(Network network) {
            Rlog.i(ImsPhoneCallTracker.LOG_TAG, "Network available: " + network);
            ImsPhoneCallTracker.this.scheduleHandoverCheck();
        }
    };
    private boolean mNotifyHandoverVideoFromLTEToWifi = false;
    private boolean mNotifyHandoverVideoFromWifiToLTE = false;
    private INotifyStatusChanged mNotifyStatusChangedCallback = new com.android.internal.telephony.imsphone.-$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w.AnonymousClass2(this);
    private boolean mNotifyVtHandoverToWifiFail = false;
    private int mOnHoldToneId = -1;
    private boolean mOnHoldToneStarted = false;
    private int mPendingCallVideoState;
    private Bundle mPendingIntentExtras;
    private ImsPhoneConnection mPendingMO;
    private Message mPendingUssd = null;
    ImsPhone mPhone;
    private PhoneNumberUtilsProxy mPhoneNumberUtilsProxy = -$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w.$INST$0;
    private List<PhoneStateListener> mPhoneStateListeners = new ArrayList();
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Removed duplicated region for block: B:53:0x0247 A:{Splitter: B:8:0x002d, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing block: B:42:0x01e1, code:
            r18.this$0.log("Exception in terminate call");
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.android.ims.IMS_INCOMING_CALL")) {
                ImsPhoneCallTracker.this.log("onReceive : incoming call intent");
                if (ImsPhoneCallTracker.this.mImsManager != null && ImsPhoneCallTracker.this.mServiceId >= 0) {
                    try {
                        if (intent.getBooleanExtra("android:ussd", false)) {
                            ImsPhoneCallTracker.this.log("onReceive : USSD");
                            ImsPhoneCallTracker.this.mUssdSession = ImsPhoneCallTracker.this.mImsManager.takeCall(ImsPhoneCallTracker.this.mServiceId, intent, ImsPhoneCallTracker.this.mImsUssdListener);
                            if (ImsPhoneCallTracker.this.mUssdSession != null) {
                                ImsPhoneCallTracker.this.mUssdSession.accept(2);
                            }
                            return;
                        }
                        ImsPhoneCall imsPhoneCall;
                        boolean isUnknown = intent.getBooleanExtra("android:isUnknown", false);
                        ImsPhoneCallTracker.this.log("onReceive : isUnknown = " + isUnknown + " fg = " + ImsPhoneCallTracker.this.mForegroundCall.getState() + " bg = " + ImsPhoneCallTracker.this.mBackgroundCall.getState());
                        ImsCall imsCall = ImsPhoneCallTracker.this.mImsManager.takeCall(ImsPhoneCallTracker.this.mServiceId, intent, ImsPhoneCallTracker.this.mImsCallListener);
                        if (ImsPhoneCallTracker.this.mPhone != null && ImsPhoneCallTracker.this.-wrap0(ImsPhoneCallTracker.this.mPhone.getDefaultPhone())) {
                            ImsPhoneCallTracker.this.sendEmptyMessageDelayed(900, 500);
                        }
                        if (!OemConstant.isCallInEnable(ImsPhoneCallTracker.this.mPhone.getDefaultPhone())) {
                            ImsPhoneCallTracker.this.log("ctmm vi block");
                            imsCall.reject(RadioError.OEM_ERROR_4);
                        }
                        Phone phone = ImsPhoneCallTracker.this.mPhone;
                        ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                        if (isUnknown) {
                            imsPhoneCall = ImsPhoneCallTracker.this.mForegroundCall;
                        } else {
                            imsPhoneCall = ImsPhoneCallTracker.this.mRingingCall;
                        }
                        ImsPhoneConnection conn = new ImsPhoneConnection(phone, imsCall, imsPhoneCallTracker, imsPhoneCall, isUnknown);
                        if (ImsPhoneCallTracker.this.mForegroundCall.hasConnections()) {
                            ImsCall activeCall = ImsPhoneCallTracker.this.mForegroundCall.getFirstConnection().getImsCall();
                            if (!(activeCall == null || imsCall == null)) {
                                conn.setActiveCallDisconnectedOnAnswer(ImsPhoneCallTracker.this.shouldDisconnectActiveCallOnAnswer(activeCall, imsCall));
                            }
                        }
                        conn.setAllowAddCallDuringVideoCall(ImsPhoneCallTracker.this.mAllowAddCallDuringVideoCall);
                        conn.setAllowHoldingVideoCall(ImsPhoneCallTracker.this.mAllowHoldingVideoCall);
                        ImsPhoneCallTracker.this.addConnection(conn);
                        ImsPhoneCallTracker.this.setVideoCallProvider(conn, imsCall);
                        TelephonyMetrics.getInstance().writeOnImsCallReceive(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getSession());
                        if (isUnknown) {
                            ImsPhoneCallTracker.this.mPhone.notifyUnknownConnection(conn);
                        } else {
                            if (!(ImsPhoneCallTracker.this.mForegroundCall.getState() == State.IDLE && ImsPhoneCallTracker.this.mBackgroundCall.getState() == State.IDLE)) {
                                conn.update(imsCall, State.WAITING);
                            }
                            ImsPhoneCallTracker.this.mPhone.notifyNewRingingConnection(conn);
                            ImsPhoneCallTracker.this.mPhone.notifyIncomingRing();
                        }
                        ((GsmCdmaCallTracker) ImsPhoneCallTracker.this.mPhone.getDefaultPhone().getCallTracker()).oemClearConn();
                        ImsPhoneCallTracker.this.updatePhoneState();
                        ImsPhoneCallTracker.this.mPhone.notifyPreciseCallStateChanged();
                    } catch (ImsException e) {
                        ImsPhoneCallTracker.this.loge("onReceive : exception " + e);
                    } catch (RemoteException e2) {
                    }
                }
            } else if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                int subId = intent.getIntExtra("subscription", -1);
                if (subId == ImsPhoneCallTracker.this.mPhone.getSubId()) {
                    ImsPhoneCallTracker.this.cacheCarrierConfiguration(subId);
                    ImsPhoneCallTracker.this.log("onReceive : Updating mAllowEmergencyVideoCalls = " + ImsPhoneCallTracker.this.mAllowEmergencyVideoCalls);
                }
            } else if ("android.telecom.action.CHANGE_DEFAULT_DIALER".equals(intent.getAction())) {
                ImsPhoneCallTracker.this.mDefaultDialerUid.set(ImsPhoneCallTracker.this.getPackageUid(context, intent.getStringExtra("android.telecom.extra.CHANGE_DEFAULT_DIALER_PACKAGE_NAME")));
            }
        }
    };
    public IRetryTimeout mRetryTimeout = new com.android.internal.telephony.imsphone.-$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w.AnonymousClass3(this);
    public ImsPhoneCall mRingingCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_RINGING);
    private int mServiceId = -1;
    private SharedPreferenceProxy mSharedPreferenceProxy = com.android.internal.telephony.imsphone.-$Lambda$tILLuSJl16qfDJK1ikBVGFm2D5w.AnonymousClass1.$INST$0;
    private boolean mShouldUpdateImsConfigOnDisconnect = false;
    private SrvccState mSrvccState = SrvccState.NONE;
    private PhoneConstants.State mState = PhoneConstants.State.IDLE;
    private boolean mSupportDowngradeVtToAudio = false;
    private boolean mSupportPauseVideo = false;
    private boolean mSwitchingFgAndBgCalls = false;
    private Object mSyncHold = new Object();
    private boolean mTreatDowngradedVideoCallsAsVideoCalls = false;
    private ImsCall mUssdSession = null;
    private ImsUtInterface mUtInterface;
    private RegistrantList mVoiceCallEndedRegistrants = new RegistrantList();
    private RegistrantList mVoiceCallStartedRegistrants = new RegistrantList();
    private final HashMap<Integer, Long> mVtDataUsageMap = new HashMap();
    private volatile NetworkStats mVtDataUsageSnapshot = null;
    private volatile NetworkStats mVtDataUsageUidSnapshot = null;
    private int pendingCallClirMode;
    private boolean pendingCallInEcm = false;

    public interface SharedPreferenceProxy {
        SharedPreferences getDefaultSharedPreferences(Context context);
    }

    public interface IRetryTimeout {
        int get();
    }

    public interface PhoneNumberUtilsProxy {
        boolean isEmergencyNumber(String str);
    }

    public interface PhoneStateListener {
        void onPhoneStateChanged(PhoneConstants.State state, PhoneConstants.State state2);
    }

    static {
        PRECISE_CAUSE_MAP.append(101, 1200);
        PRECISE_CAUSE_MAP.append(102, 1201);
        PRECISE_CAUSE_MAP.append(103, 1202);
        PRECISE_CAUSE_MAP.append(106, 1203);
        PRECISE_CAUSE_MAP.append(107, 1204);
        PRECISE_CAUSE_MAP.append(108, 16);
        PRECISE_CAUSE_MAP.append(111, 1205);
        PRECISE_CAUSE_MAP.append(112, 1206);
        PRECISE_CAUSE_MAP.append(121, 1207);
        PRECISE_CAUSE_MAP.append(122, 1208);
        PRECISE_CAUSE_MAP.append(123, 1209);
        PRECISE_CAUSE_MAP.append(124, 1210);
        PRECISE_CAUSE_MAP.append(131, 1211);
        PRECISE_CAUSE_MAP.append(132, 1212);
        PRECISE_CAUSE_MAP.append(141, 1213);
        PRECISE_CAUSE_MAP.append(143, 1214);
        PRECISE_CAUSE_MAP.append(144, 1215);
        PRECISE_CAUSE_MAP.append(145, 1216);
        PRECISE_CAUSE_MAP.append(146, 1217);
        PRECISE_CAUSE_MAP.append(147, 1218);
        PRECISE_CAUSE_MAP.append(148, 1219);
        PRECISE_CAUSE_MAP.append(149, 1220);
        PRECISE_CAUSE_MAP.append(201, 1221);
        PRECISE_CAUSE_MAP.append(202, 1222);
        PRECISE_CAUSE_MAP.append(203, 1223);
        PRECISE_CAUSE_MAP.append(241, 241);
        PRECISE_CAUSE_MAP.append(321, 1300);
        PRECISE_CAUSE_MAP.append(331, 1310);
        PRECISE_CAUSE_MAP.append(332, 1311);
        PRECISE_CAUSE_MAP.append(333, 1312);
        PRECISE_CAUSE_MAP.append(334, 1313);
        PRECISE_CAUSE_MAP.append(335, 1314);
        PRECISE_CAUSE_MAP.append(336, 1315);
        PRECISE_CAUSE_MAP.append(337, 1316);
        PRECISE_CAUSE_MAP.append(338, 1317);
        PRECISE_CAUSE_MAP.append(339, 1318);
        PRECISE_CAUSE_MAP.append(340, 1319);
        PRECISE_CAUSE_MAP.append(341, 1320);
        PRECISE_CAUSE_MAP.append(342, 1321);
        PRECISE_CAUSE_MAP.append(351, 1330);
        PRECISE_CAUSE_MAP.append(352, 1331);
        PRECISE_CAUSE_MAP.append(353, 1332);
        PRECISE_CAUSE_MAP.append(354, 1333);
        PRECISE_CAUSE_MAP.append(361, 1340);
        PRECISE_CAUSE_MAP.append(362, 1341);
        PRECISE_CAUSE_MAP.append(363, 1342);
        PRECISE_CAUSE_MAP.append(364, 1343);
        PRECISE_CAUSE_MAP.append(401, 1400);
        PRECISE_CAUSE_MAP.append(402, 1401);
        PRECISE_CAUSE_MAP.append(403, 1402);
        PRECISE_CAUSE_MAP.append(404, 1403);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_1, 1500);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_2, 1501);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_3, 1502);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_4, 1503);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_5, 1504);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_6, 1505);
        PRECISE_CAUSE_MAP.append(RadioError.OEM_ERROR_10, 1510);
        PRECISE_CAUSE_MAP.append(801, 1800);
        PRECISE_CAUSE_MAP.append(802, 1801);
        PRECISE_CAUSE_MAP.append(803, 1802);
        PRECISE_CAUSE_MAP.append(804, 1803);
        PRECISE_CAUSE_MAP.append(821, 1804);
        PRECISE_CAUSE_MAP.append(901, 1900);
        PRECISE_CAUSE_MAP.append(902, 1901);
        PRECISE_CAUSE_MAP.append(1100, ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT);
        PRECISE_CAUSE_MAP.append(1014, 2100);
        PRECISE_CAUSE_MAP.append(CharacterSets.UTF_16, 2101);
        PRECISE_CAUSE_MAP.append(1016, 2102);
        PRECISE_CAUSE_MAP.append(1201, 2300);
        PRECISE_CAUSE_MAP.append(1202, 2301);
        PRECISE_CAUSE_MAP.append(1203, 2302);
        PRECISE_CAUSE_MAP.append(1300, 2400);
        PRECISE_CAUSE_MAP.append(1400, 2500);
        PRECISE_CAUSE_MAP.append(1401, 2501);
        PRECISE_CAUSE_MAP.append(1402, 2502);
        PRECISE_CAUSE_MAP.append(1403, 2503);
        PRECISE_CAUSE_MAP.append(1404, 2504);
        PRECISE_CAUSE_MAP.append(1405, 2505);
        PRECISE_CAUSE_MAP.append(1406, 2506);
        PRECISE_CAUSE_MAP.append(1407, 2507);
        PRECISE_CAUSE_MAP.append(1500, LastCallFailCause.RADIO_OFF);
        PRECISE_CAUSE_MAP.append(1501, LastCallFailCause.NO_VALID_SIM);
        PRECISE_CAUSE_MAP.append(1502, LastCallFailCause.RADIO_INTERNAL_ERROR);
        PRECISE_CAUSE_MAP.append(1503, LastCallFailCause.NETWORK_RESP_TIMEOUT);
        PRECISE_CAUSE_MAP.append(1504, LastCallFailCause.NETWORK_REJECT);
        PRECISE_CAUSE_MAP.append(1505, LastCallFailCause.RADIO_ACCESS_FAILURE);
        PRECISE_CAUSE_MAP.append(1506, LastCallFailCause.RADIO_LINK_FAILURE);
        PRECISE_CAUSE_MAP.append(1507, 255);
        PRECISE_CAUSE_MAP.append(1508, 256);
        PRECISE_CAUSE_MAP.append(1509, LastCallFailCause.RADIO_SETUP_FAILURE);
        PRECISE_CAUSE_MAP.append(1510, LastCallFailCause.RADIO_RELEASE_NORMAL);
        PRECISE_CAUSE_MAP.append(1511, LastCallFailCause.RADIO_RELEASE_ABNORMAL);
        PRECISE_CAUSE_MAP.append(1512, LastCallFailCause.ACCESS_CLASS_BLOCKED);
        PRECISE_CAUSE_MAP.append(1513, LastCallFailCause.NETWORK_DETACH);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_1, LastCallFailCause.OEM_CAUSE_1);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_2, LastCallFailCause.OEM_CAUSE_2);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_3, LastCallFailCause.OEM_CAUSE_3);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_4, LastCallFailCause.OEM_CAUSE_4);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_5, LastCallFailCause.OEM_CAUSE_5);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_6, LastCallFailCause.OEM_CAUSE_6);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_7, LastCallFailCause.OEM_CAUSE_7);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_8, LastCallFailCause.OEM_CAUSE_8);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_9, LastCallFailCause.OEM_CAUSE_9);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_10, LastCallFailCause.OEM_CAUSE_10);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_11, LastCallFailCause.OEM_CAUSE_11);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_12, LastCallFailCause.OEM_CAUSE_12);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_13, LastCallFailCause.OEM_CAUSE_13);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_14, LastCallFailCause.OEM_CAUSE_14);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_15, LastCallFailCause.OEM_CAUSE_15);
    }

    /* renamed from: lambda$-com_android_internal_telephony_imsphone_ImsPhoneCallTracker_36628 */
    /* synthetic */ void m31x31590ae0() {
        try {
            int status = this.mImsManager.getImsServiceStatus();
            log("Status Changed: " + status);
            switch (status) {
                case 0:
                case 1:
                    stopListeningForCalls();
                    return;
                case 2:
                    startListeningForCalls();
                    return;
                default:
                    Log.w(LOG_TAG, "Unexpected State!");
                    return;
            }
        } catch (ImsException e) {
            retryGetImsService();
        }
        retryGetImsService();
    }

    /* renamed from: lambda$-com_android_internal_telephony_imsphone_ImsPhoneCallTracker_37700 */
    /* synthetic */ int m32x315982ba() {
        int timeout = (1 << this.mImsServiceRetryCount) * 500;
        if (this.mImsServiceRetryCount <= 6) {
            this.mImsServiceRetryCount++;
        }
        return timeout;
    }

    public ImsPhoneCallTracker(ImsPhone phone) {
        this.mPhone = phone;
        this.mMetrics = TelephonyMetrics.getInstance();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("com.android.ims.IMS_INCOMING_CALL");
        intentfilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        intentfilter.addAction("android.telecom.action.CHANGE_DEFAULT_DIALER");
        this.mPhone.getContext().registerReceiver(this.mReceiver, intentfilter);
        cacheCarrierConfiguration(this.mPhone.getSubId());
        this.mPhone.getDefaultPhone().registerForDataEnabledChanged(this, 23, null);
        this.mImsServiceRetryCount = 0;
        this.mDefaultDialerUid.set(getPackageUid(this.mPhone.getContext(), ((TelecomManager) this.mPhone.getContext().getSystemService("telecom")).getDefaultDialerPackage()));
        long currentTime = SystemClock.elapsedRealtime();
        this.mVtDataUsageSnapshot = new NetworkStats(currentTime, 1);
        this.mVtDataUsageUidSnapshot = new NetworkStats(currentTime, 1);
        sendEmptyMessage(24);
    }

    public void setSharedPreferenceProxy(SharedPreferenceProxy sharedPreferenceProxy) {
        this.mSharedPreferenceProxy = sharedPreferenceProxy;
    }

    public void setPhoneNumberUtilsProxy(PhoneNumberUtilsProxy phoneNumberUtilsProxy) {
        this.mPhoneNumberUtilsProxy = phoneNumberUtilsProxy;
    }

    private int getPackageUid(Context context, String pkg) {
        if (pkg == null) {
            return -1;
        }
        int uid = -1;
        try {
            uid = context.getPackageManager().getPackageUid(pkg, 0);
        } catch (NameNotFoundException e) {
            loge("Cannot find package uid. pkg = " + pkg);
        }
        return uid;
    }

    private PendingIntent createIncomingCallPendingIntent() {
        Intent intent = new Intent("com.android.ims.IMS_INCOMING_CALL");
        intent.addFlags(268435456);
        return PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
    }

    private void getImsService() throws ImsException {
        log("getImsService");
        this.mImsManager = ImsManager.getInstance(this.mPhone.getContext(), this.mPhone.getPhoneId());
        this.mImsManager.addNotifyStatusChangedCallbackIfAvailable(this.mNotifyStatusChangedCallback);
        this.mNotifyStatusChangedCallback.notifyStatusChanged();
    }

    private void startListeningForCalls() throws ImsException {
        this.mImsServiceRetryCount = 0;
        this.mServiceId = this.mImsManager.open(1, createIncomingCallPendingIntent(), this.mImsConnectionStateListener);
        this.mImsManager.setImsConfigListener(this.mImsConfigListener);
        getEcbmInterface().setEcbmStateListener(this.mPhone.getImsEcbmStateListener());
        if (this.mPhone.isInEcm()) {
            this.mPhone.exitEmergencyCallbackMode();
        }
        this.mImsManager.setUiTTYMode(this.mPhone.getContext(), Secure.getInt(this.mPhone.getContext().getContentResolver(), "preferred_tty_mode", 0), null);
        ImsMultiEndpoint multiEndpoint = getMultiEndpointInterface();
        if (multiEndpoint != null) {
            multiEndpoint.setExternalCallStateListener(this.mPhone.getExternalCallTracker().getExternalCallStateListener());
        }
        this.mUtInterface = getUtInterface();
        if (this.mUtInterface != null) {
            this.mUtInterface.setSuppServiceIndication(this, 27, null);
        }
        if (this.mCarrierConfigLoaded) {
            this.mImsManager.updateImsServiceConfigForSlot(true);
        }
    }

    private void stopListeningForCalls() {
        try {
            resetImsCapabilities();
            if (this.mImsManager != null && this.mServiceId > 0) {
                this.mImsManager.close(this.mServiceId);
                this.mServiceId = -1;
            }
        } catch (ImsException e) {
        }
    }

    public void dispose() {
        log("dispose");
        this.mRingingCall.dispose();
        this.mBackgroundCall.dispose();
        this.mForegroundCall.dispose();
        this.mHandoverCall.dispose();
        clearDisconnected();
        if (this.mUtInterface != null) {
            this.mUtInterface.unSetSuppServiceIndication(this);
        }
        this.mPhone.getContext().unregisterReceiver(this.mReceiver);
        this.mPhone.getDefaultPhone().unregisterForDataEnabledChanged(this);
        removeMessages(24);
    }

    protected void finalize() {
        log("ImsPhoneCallTracker finalized");
    }

    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        this.mVoiceCallStartedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVoiceCallStarted(Handler h) {
        this.mVoiceCallStartedRegistrants.remove(h);
    }

    public void registerForVoiceCallEnded(Handler h, int what, Object obj) {
        this.mVoiceCallEndedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVoiceCallEnded(Handler h) {
        this.mVoiceCallEndedRegistrants.remove(h);
    }

    public Connection dial(String dialString, int videoState, Bundle intentExtras) throws CallStateException {
        int oirMode;
        if (this.mSharedPreferenceProxy == null || this.mPhone.getDefaultPhone() == null) {
            loge("dial; could not get default CLIR mode.");
            oirMode = 0;
        } else {
            oirMode = this.mSharedPreferenceProxy.getDefaultSharedPreferences(this.mPhone.getContext()).getInt(Phone.CLIR_KEY + this.mPhone.getDefaultPhone().getPhoneId(), 0);
        }
        return dial(dialString, oirMode, videoState, intentExtras);
    }

    synchronized Connection dial(String dialString, int clirMode, int videoState, Bundle intentExtras) throws CallStateException {
        boolean isPhoneInEcmMode = isPhoneInEcbMode();
        boolean isEmergencyNumber = this.mPhoneNumberUtilsProxy.isEmergencyNumber(dialString);
        log("dial clirMode=" + clirMode);
        if (isEmergencyNumber) {
            clirMode = 2;
            log("dial emergency call, set clirModIe=" + 2);
        }
        clearDisconnected();
        if (this.mImsManager == null) {
            throw new CallStateException("service not available");
        } else if (canDial() && (canAddVideoCallDuringImsAudioCall(videoState) ^ 1) == 0) {
            if (isPhoneInEcmMode && isEmergencyNumber) {
                handleEcmTimer(1);
            }
            if (isEmergencyNumber && VideoProfile.isVideo(videoState) && (this.mAllowEmergencyVideoCalls ^ 1) != 0) {
                loge("dial: carrier does not support video emergency calls; downgrade to audio-only");
                videoState = 0;
            }
            boolean holdBeforeDial = false;
            boolean hangupBeforeDial = false;
            if (this.mForegroundCall.getState() == State.ACTIVE) {
                if (this.mBackgroundCall.getState() != State.IDLE) {
                    throw new CallStateException("cannot dial in current state");
                }
                holdBeforeDial = true;
                this.mPendingCallVideoState = videoState;
                this.mPendingIntentExtras = intentExtras;
                if (shouldDisconnectActiveCallOnDial(isEmergencyNumber)) {
                    holdBeforeDial = false;
                    hangupBeforeDial = true;
                    log("dial, hangingup active call");
                    this.mForegroundCall.hangup();
                } else {
                    switchWaitingOrHoldingAndActive();
                }
            }
            State fgState = State.IDLE;
            State bgState = State.IDLE;
            this.mClirMode = clirMode;
            synchronized (this.mSyncHold) {
                if (holdBeforeDial) {
                    fgState = this.mForegroundCall.getState();
                    bgState = this.mBackgroundCall.getState();
                    if (fgState == State.ACTIVE) {
                        throw new CallStateException("cannot dial in current state");
                    } else if (bgState == State.HOLDING) {
                        holdBeforeDial = false;
                    }
                }
                this.mPendingMO = new ImsPhoneConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, isEmergencyNumber, intentExtras);
                this.mPendingMO.setVideoState(videoState);
            }
            addConnection(this.mPendingMO);
            if (!(holdBeforeDial || (hangupBeforeDial ^ 1) == 0)) {
                if (!isPhoneInEcmMode || (isPhoneInEcmMode && isEmergencyNumber)) {
                    dialInternal(this.mPendingMO, clirMode, videoState, intentExtras);
                } else {
                    try {
                        getEcbmInterface().exitEmergencyCallbackMode();
                        this.mPhone.setOnEcbModeExitResponse(this, 14, null);
                        this.pendingCallClirMode = clirMode;
                        this.mPendingCallVideoState = videoState;
                        this.mPendingIntentExtras = intentExtras;
                        this.pendingCallInEcm = true;
                    } catch (ImsException e) {
                        e.printStackTrace();
                        throw new CallStateException("service not available");
                    }
                }
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
        } else {
            throw new CallStateException("cannot dial in current state");
        }
        return this.mPendingMO;
    }

    public void addParticipant(String dialString, Message onComplete) throws CallStateException {
        boolean isSuccess = false;
        if (this.mForegroundCall != null) {
            ImsCall imsCall = this.mForegroundCall.getImsCall();
            if (imsCall == null) {
                loge("addParticipant : No foreground ims call");
            } else {
                ImsCallSession imsCallSession = imsCall.getCallSession();
                if (imsCallSession != null) {
                    synchronized (this.mAddParticipantLock) {
                        this.mAddPartResp = onComplete;
                        imsCallSession.inviteParticipants(new String[]{dialString});
                        isSuccess = true;
                    }
                } else {
                    loge("addParticipant : ImsCallSession does not exist");
                }
            }
        } else {
            loge("addParticipant : Foreground call does not exist");
        }
        if (!isSuccess && onComplete != null) {
            sendAddParticipantResponse(false, onComplete);
            this.mAddPartResp = null;
        }
    }

    private void sendAddParticipantResponse(boolean success, Message onComplete) {
        loge("sendAddParticipantResponse : success = " + success);
        if (onComplete != null) {
            Throwable th = null;
            if (!success) {
                th = new Exception("Add participant failed");
            }
            AsyncResult.forMessage(onComplete, null, th);
            onComplete.sendToTarget();
        }
    }

    boolean isImsServiceReady() {
        if (this.mImsManager == null) {
            return false;
        }
        return this.mImsManager.isServiceAvailable();
    }

    private void cacheCarrierConfiguration(int subId) {
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (carrierConfigManager == null || (SubscriptionController.getInstance().isActiveSubId(subId) ^ 1) != 0) {
            loge("cacheCarrierConfiguration: No carrier config service found or not active subId = " + subId);
            this.mCarrierConfigLoaded = false;
            return;
        }
        PersistableBundle carrierConfig = carrierConfigManager.getConfigForSubId(subId);
        if (carrierConfig == null) {
            loge("cacheCarrierConfiguration: Empty carrier config.");
            this.mCarrierConfigLoaded = false;
            return;
        }
        this.mCarrierConfigLoaded = true;
        updateCarrierConfigCache(carrierConfig);
    }

    public void updateCarrierConfigCache(PersistableBundle carrierConfig) {
        this.mAllowEmergencyVideoCalls = carrierConfig.getBoolean("allow_emergency_video_calls_bool");
        this.mTreatDowngradedVideoCallsAsVideoCalls = carrierConfig.getBoolean("treat_downgraded_video_calls_as_video_calls_bool");
        this.mDropVideoCallWhenAnsweringAudioCall = carrierConfig.getBoolean("drop_video_call_when_answering_audio_call_bool");
        this.mAllowAddCallDuringVideoCall = carrierConfig.getBoolean("allow_add_call_during_video_call");
        this.mAllowHoldingVideoCall = carrierConfig.getBoolean(QtiCarrierConfigs.ALLOW_HOLD_IN_VIDEO_CALL);
        this.mNotifyVtHandoverToWifiFail = carrierConfig.getBoolean("notify_vt_handover_to_wifi_failure_bool");
        this.mSupportDowngradeVtToAudio = carrierConfig.getBoolean("support_downgrade_vt_to_audio_bool");
        this.mNotifyHandoverVideoFromWifiToLTE = carrierConfig.getBoolean("notify_handover_video_from_wifi_to_lte_bool");
        this.mNotifyHandoverVideoFromLTEToWifi = carrierConfig.getBoolean("notify_handover_video_from_lte_to_wifi_bool");
        this.mIgnoreDataEnabledChangedForVideoCalls = carrierConfig.getBoolean("ignore_data_enabled_changed_for_video_calls");
        this.mIsViLteDataMetered = carrierConfig.getBoolean("vilte_data_is_metered_bool");
        this.mSupportPauseVideo = carrierConfig.getBoolean("support_pause_ims_video_calls_bool");
        this.mIgnoreResetUtCapability = carrierConfig.getBoolean("ignore_reset_ut_capability_bool");
        String[] mappings = carrierConfig.getStringArray("ims_reasoninfo_mapping_string_array");
        if (mappings == null || mappings.length <= 0) {
            log("No carrier ImsReasonInfo mappings defined.");
            return;
        }
        for (String mapping : mappings) {
            String[] values = mapping.split(Pattern.quote("|"));
            if (values.length == 3) {
                try {
                    Integer fromCode;
                    if (values[0].equals(CharacterSets.MIMENAME_ANY_CHARSET)) {
                        fromCode = null;
                    } else {
                        fromCode = Integer.valueOf(Integer.parseInt(values[0]));
                    }
                    String message = values[1];
                    int toCode = Integer.parseInt(values[2]);
                    addReasonCodeRemapping(fromCode, message, Integer.valueOf(toCode));
                    log(new StringBuilder().append("Loaded ImsReasonInfo mapping : fromCode = ").append(fromCode).toString() == null ? "any" : fromCode + " ; message = " + message + " ; toCode = " + toCode);
                } catch (NumberFormatException e) {
                    loge("Invalid ImsReasonInfo mapping found: " + mapping);
                }
            }
        }
    }

    private void handleEcmTimer(int action) {
        this.mPhone.handleTimerInEmergencyCallbackMode(action);
        switch (action) {
            case 0:
            case 1:
                return;
            default:
                log("handleEcmTimer, unsupported action " + action);
                return;
        }
    }

    private void dialInternal(ImsPhoneConnection conn, int clirMode, int videoState, Bundle intentExtras) {
        if (conn != null) {
            boolean isConferenceUri = false;
            int isSkipSchemaParsing = 0;
            if (intentExtras != null) {
                isConferenceUri = intentExtras.getBoolean("org.codeaurora.extra.DIAL_CONFERENCE_URI", false);
                isSkipSchemaParsing = intentExtras.getBoolean("org.codeaurora.extra.SKIP_SCHEMA_PARSING", false);
            }
            if (isConferenceUri || (isSkipSchemaParsing ^ 1) == 0 || !(conn.getAddress() == null || conn.getAddress().length() == 0 || conn.getAddress().indexOf(78) >= 0)) {
                setMute(false);
                int serviceType = this.mPhoneNumberUtilsProxy.isEmergencyNumber(conn.getAddress()) ? 2 : 1;
                int callType = ImsCallProfile.getCallTypeFromVideoState(videoState);
                conn.setVideoState(videoState);
                try {
                    String[] callees = new String[]{conn.getAddress()};
                    ImsCallProfile profile = this.mImsManager.createCallProfile(this.mServiceId, serviceType, callType);
                    profile.setCallExtraInt("oir", clirMode);
                    profile.setCallExtraBoolean("isConferenceUri", isConferenceUri);
                    if (intentExtras != null) {
                        if (intentExtras.containsKey("android.telecom.extra.CALL_SUBJECT")) {
                            intentExtras.putString("DisplayText", cleanseInstantLetteringMessage(intentExtras.getString("android.telecom.extra.CALL_SUBJECT")));
                        }
                        if (intentExtras.containsKey("CallPull")) {
                            profile.mCallExtras.putBoolean("CallPull", intentExtras.getBoolean("CallPull"));
                            int dialogId = intentExtras.getInt(ImsExternalCallTracker.EXTRA_IMS_EXTERNAL_CALL_ID);
                            conn.setIsPulledCall(true);
                            conn.setPulledDialogId(dialogId);
                        }
                        profile.mCallExtras.putBundle("OemCallExtras", intentExtras);
                    }
                    ImsCall imsCall = this.mImsManager.makeCall(this.mServiceId, setRttModeBasedOnOperator(profile), callees, this.mImsCallListener);
                    conn.setImsCall(imsCall);
                    this.mMetrics.writeOnImsCallStart(this.mPhone.getPhoneId(), imsCall.getSession());
                    setVideoCallProvider(conn, imsCall);
                    conn.setAllowAddCallDuringVideoCall(this.mAllowAddCallDuringVideoCall);
                    conn.setAllowHoldingVideoCall(this.mAllowHoldingVideoCall);
                } catch (ImsException e) {
                    loge("dialInternal : " + e);
                    conn.setDisconnectCause(36);
                    sendEmptyMessageDelayed(18, 500);
                    retryGetImsService();
                } catch (RemoteException e2) {
                }
                return;
            }
            conn.setDisconnectCause(7);
            sendEmptyMessageDelayed(18, 500);
        }
    }

    public void acceptCall(int videoState) throws CallStateException {
        log("acceptCall");
        if (this.mForegroundCall.getState().isAlive() && this.mBackgroundCall.getState().isAlive()) {
            throw new CallStateException("cannot accept call");
        }
        ImsStreamMediaProfile mediaProfile = new ImsStreamMediaProfile();
        if (this.mRingingCall.getState() == State.WAITING && this.mForegroundCall.getState().isAlive()) {
            setMute(false);
            boolean answeringWillDisconnect = false;
            ImsCall activeCall = this.mForegroundCall.getImsCall();
            ImsCall ringingCall = this.mRingingCall.getImsCall();
            if (this.mForegroundCall.hasConnections() && this.mRingingCall.hasConnections()) {
                answeringWillDisconnect = shouldDisconnectActiveCallOnAnswer(activeCall, ringingCall);
            }
            this.mPendingCallVideoState = videoState;
            if (answeringWillDisconnect) {
                this.mForegroundCall.hangup();
                try {
                    ringingCall.accept(ImsCallProfile.getCallTypeFromVideoState(videoState), addRttAttributeIfRequired(ringingCall, mediaProfile));
                    return;
                } catch (ImsException e) {
                    throw new CallStateException("cannot accept call");
                }
            }
            switchWaitingOrHoldingAndActive();
        } else if (this.mRingingCall.getState().isRinging()) {
            log("acceptCall: incoming...");
            setMute(false);
            try {
                ImsCall imsCall = this.mRingingCall.getImsCall();
                if (imsCall != null) {
                    imsCall.accept(ImsCallProfile.getCallTypeFromVideoState(videoState), addRttAttributeIfRequired(imsCall, mediaProfile));
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 2);
                    return;
                }
                throw new CallStateException("no valid ims call");
            } catch (ImsException e2) {
                throw new CallStateException("cannot accept call");
            }
        } else {
            throw new CallStateException("phone not ringing");
        }
    }

    public void rejectCall() throws CallStateException {
        log("rejectCall");
        if (this.mRingingCall.getState().isRinging()) {
            hangup(this.mRingingCall);
            return;
        }
        throw new CallStateException("phone not ringing");
    }

    private void switchAfterConferenceSuccess() {
        log("switchAfterConferenceSuccess fg =" + this.mForegroundCall.getState() + ", bg = " + this.mBackgroundCall.getState());
        if (this.mBackgroundCall.getState() == State.HOLDING) {
            log("switchAfterConferenceSuccess");
            this.mForegroundCall.switchWith(this.mBackgroundCall);
        }
    }

    public void switchWaitingOrHoldingAndActive() throws CallStateException {
        log("switchWaitingOrHoldingAndActive");
        if (this.mRingingCall.getState() == State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else if (this.mForegroundCall.getState() == State.ACTIVE) {
            ImsCall imsCall = this.mForegroundCall.getImsCall();
            if (imsCall == null) {
                throw new CallStateException("no ims call");
            }
            boolean switchingWithWaitingCall = (this.mBackgroundCall.getState().isAlive() || this.mRingingCall == null) ? false : this.mRingingCall.getState() == State.WAITING;
            this.mSwitchingFgAndBgCalls = true;
            if (switchingWithWaitingCall) {
                this.mCallExpectedToResume = this.mRingingCall.getImsCall();
            } else {
                this.mCallExpectedToResume = this.mBackgroundCall.getImsCall();
            }
            this.mForegroundCall.switchWith(this.mBackgroundCall);
            try {
                imsCall.hold();
                this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 5);
                if (this.mCallExpectedToResume == null) {
                    log("mCallExpectedToResume is null");
                    this.mSwitchingFgAndBgCalls = false;
                }
            } catch (ImsException e) {
                this.mForegroundCall.switchWith(this.mBackgroundCall);
                throw new CallStateException(e.getMessage());
            }
        } else if (this.mBackgroundCall.getState() == State.HOLDING) {
            resumeWaitingOrHolding();
        }
    }

    public void conference() {
        ImsCall fgImsCall = this.mForegroundCall.getImsCall();
        if (fgImsCall == null) {
            log("conference no foreground ims call");
            return;
        }
        ImsCall bgImsCall = this.mBackgroundCall.getImsCall();
        if (bgImsCall == null) {
            log("conference no background ims call");
        } else if (fgImsCall.isCallSessionMergePending()) {
            log("conference: skip; foreground call already in process of merging.");
        } else if (bgImsCall.isCallSessionMergePending()) {
            log("conference: skip; background call already in process of merging.");
        } else {
            long conferenceConnectTime;
            long foregroundConnectTime = this.mForegroundCall.getEarliestConnectTime();
            long backgroundConnectTime = this.mBackgroundCall.getEarliestConnectTime();
            if (foregroundConnectTime > 0 && backgroundConnectTime > 0) {
                conferenceConnectTime = Math.min(this.mForegroundCall.getEarliestConnectTime(), this.mBackgroundCall.getEarliestConnectTime());
                log("conference - using connect time = " + conferenceConnectTime);
            } else if (foregroundConnectTime > 0) {
                log("conference - bg call connect time is 0; using fg = " + foregroundConnectTime);
                conferenceConnectTime = foregroundConnectTime;
            } else {
                log("conference - fg call connect time is 0; using bg = " + backgroundConnectTime);
                conferenceConnectTime = backgroundConnectTime;
            }
            String foregroundId = SpnOverride.MVNO_TYPE_NONE;
            ImsPhoneConnection foregroundConnection = this.mForegroundCall.getFirstConnection();
            if (foregroundConnection != null) {
                foregroundConnection.setConferenceConnectTime(conferenceConnectTime);
                foregroundConnection.handleMergeStart();
                foregroundId = foregroundConnection.getTelecomCallId();
            }
            String backgroundId = SpnOverride.MVNO_TYPE_NONE;
            ImsPhoneConnection backgroundConnection = findConnection(bgImsCall);
            if (backgroundConnection != null) {
                backgroundConnection.handleMergeStart();
                backgroundId = backgroundConnection.getTelecomCallId();
            }
            log("conference: fgCallId=" + foregroundId + ", bgCallId=" + backgroundId);
            try {
                fgImsCall.merge(bgImsCall);
            } catch (ImsException e) {
                log("conference " + e.getMessage());
            }
        }
    }

    public void explicitCallTransfer() {
    }

    public void clearDisconnected() {
        log("clearDisconnected");
        internalClearDisconnected();
        updatePhoneState();
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public boolean canConference() {
        if (this.mForegroundCall.getState() == State.ACTIVE && this.mBackgroundCall.getState() == State.HOLDING && (this.mBackgroundCall.isFull() ^ 1) != 0) {
            return this.mForegroundCall.isFull() ^ 1;
        }
        return false;
    }

    private boolean canAddVideoCallDuringImsAudioCall(int videoState) {
        boolean z = true;
        if (this.mAllowHoldingVideoCall) {
            return true;
        }
        int isImsAudioCallActiveOrHolding;
        ImsCall call = this.mForegroundCall.getImsCall();
        if (call == null) {
            call = this.mBackgroundCall.getImsCall();
        }
        if ((this.mForegroundCall.getState() == State.ACTIVE || this.mBackgroundCall.getState() == State.HOLDING) && call != null) {
            isImsAudioCallActiveOrHolding = call.isVideoCall() ^ 1;
        } else {
            isImsAudioCallActiveOrHolding = 0;
        }
        if (isImsAudioCallActiveOrHolding != 0) {
            z = VideoProfile.isVideo(videoState) ^ 1;
        }
        return z;
    }

    public boolean canDial() {
        int ret;
        String disableCall = SystemProperties.get("ro.telephony.disable-call", "false");
        if (this.mPendingMO != null || (this.mRingingCall.isRinging() ^ 1) == 0 || (disableCall.equals("true") ^ 1) == 0) {
            ret = 0;
        } else if (this.mForegroundCall.getState().isAlive()) {
            ret = this.mBackgroundCall.getState().isAlive() ^ 1;
        } else {
            ret = 1;
        }
        return ret != 0 ? isPendingResumeCall() ^ 1 : false;
    }

    public boolean canTransfer() {
        if (this.mForegroundCall.getState() == State.ACTIVE && this.mBackgroundCall.getState() == State.HOLDING) {
            return true;
        }
        return false;
    }

    private void internalClearDisconnected() {
        this.mRingingCall.clearDisconnected();
        this.mForegroundCall.clearDisconnected();
        this.mBackgroundCall.clearDisconnected();
        this.mHandoverCall.clearDisconnected();
    }

    private void updatePhoneState() {
        Object obj;
        PhoneConstants.State oldState = this.mState;
        boolean isPendingMOIdle = this.mPendingMO != null ? this.mPendingMO.getState().isAlive() ^ 1 : true;
        if (this.mRingingCall.isRinging()) {
            this.mState = PhoneConstants.State.RINGING;
        } else if (isPendingMOIdle && (this.mForegroundCall.isIdle() ^ 1) == 0 && (this.mBackgroundCall.isIdle() ^ 1) == 0) {
            this.mState = PhoneConstants.State.IDLE;
        } else {
            this.mState = PhoneConstants.State.OFFHOOK;
        }
        if (this.mState == PhoneConstants.State.IDLE && oldState != this.mState) {
            this.mVoiceCallEndedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        } else if (oldState == PhoneConstants.State.IDLE && oldState != this.mState) {
            this.mVoiceCallStartedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
        StringBuilder append = new StringBuilder().append("updatePhoneState pendingMo = ");
        if (this.mPendingMO == null) {
            obj = "null";
        } else {
            obj = this.mPendingMO.getState();
        }
        log(append.append(obj).append(", fg= ").append(this.mForegroundCall.getState()).append("(").append(this.mForegroundCall.getConnections().size()).append("), bg= ").append(this.mBackgroundCall.getState()).append("(").append(this.mBackgroundCall.getConnections().size()).append(")").toString());
        log("updatePhoneState oldState=" + oldState + ", newState=" + this.mState);
        if (this.mState != oldState) {
            OemConstant.setOemVoocState(oldState, this.mState);
            this.mPhone.notifyPhoneStateChanged();
            this.mMetrics.writePhoneState(this.mPhone.getPhoneId(), this.mState);
            notifyPhoneStateChanged(oldState, this.mState);
        }
    }

    protected void setOemStates(PhoneConstants.State state) {
        this.mState = state;
    }

    private void handleRadioNotAvailable() {
        pollCallsWhenSafe();
    }

    private void dumpState() {
        int i;
        log("Phone State:" + this.mState);
        log("Ringing call: " + this.mRingingCall.toString());
        List l = this.mRingingCall.getConnections();
        int s = l.size();
        for (i = 0; i < s; i++) {
            log(l.get(i).toString());
        }
        log("Foreground call: " + this.mForegroundCall.toString());
        l = this.mForegroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i++) {
            log(l.get(i).toString());
        }
        log("Background call: " + this.mBackgroundCall.toString());
        l = this.mBackgroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i++) {
            log(l.get(i).toString());
        }
    }

    public void setTtyMode(int ttyMode) {
        if (this.mImsManager == null) {
            Log.w(LOG_TAG, "ImsManager is null when setting TTY mode");
            return;
        }
        try {
            this.mImsManager.setTtyMode(ttyMode);
        } catch (ImsException e) {
            loge("setTtyMode : " + e);
            retryGetImsService();
        }
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        if (this.mImsManager == null) {
            this.mPhone.sendErrorResponse(onComplete, getImsManagerIsNullException());
            return;
        }
        try {
            this.mImsManager.setUiTTYMode(this.mPhone.getContext(), uiTtyMode, onComplete);
        } catch (ImsException e) {
            loge("setUITTYMode : " + e);
            this.mPhone.sendErrorResponse(onComplete, e);
            retryGetImsService();
        }
    }

    public void setMute(boolean mute) {
        this.mDesiredMute = mute;
        this.mForegroundCall.setMute(mute);
    }

    public boolean getMute() {
        return this.mDesiredMute;
    }

    public void sendDtmf(char c, Message result) {
        log("sendDtmf");
        ImsCall imscall = this.mForegroundCall.getImsCall();
        if (imscall != null) {
            imscall.sendDtmf(c, result);
        }
    }

    public void startDtmf(char c) {
        log("startDtmf");
        ImsCall imscall = this.mForegroundCall.getImsCall();
        if (imscall != null) {
            imscall.startDtmf(c);
        } else {
            loge("startDtmf : no foreground call");
        }
    }

    public void stopDtmf() {
        log("stopDtmf");
        ImsCall imscall = this.mForegroundCall.getImsCall();
        if (imscall != null) {
            imscall.stopDtmf();
        } else {
            loge("stopDtmf : no foreground call");
        }
    }

    public void hangup(ImsPhoneConnection conn) throws CallStateException {
        log("hangup connection");
        if (conn.getOwner() != this) {
            throw new CallStateException("ImsPhoneConnection " + conn + "does not belong to ImsPhoneCallTracker " + this);
        }
        hangup(conn.getCall());
    }

    public void hangup(ImsPhoneCall call) throws CallStateException {
        log("hangup call");
        if (call.getConnections().size() == 0) {
            throw new CallStateException("no connections");
        }
        ImsCall imsCall = call.getImsCall();
        boolean rejectCall = false;
        if (call == this.mRingingCall) {
            log("(ringing) hangup incoming");
            rejectCall = true;
        } else if (call == this.mForegroundCall) {
            if (call.isDialingOrAlerting()) {
                log("(foregnd) hangup dialing or alerting...");
            } else {
                log("(foregnd) hangup foreground");
                if (this.mPhone.isSRVCC() || !(this.mPendingHangupCall != null || this.mPendingMO == null || (this.mPendingMO.getState().isAlive() ^ 1) == 0)) {
                    log("(foregnd) hangup dialing or alerting pending...");
                    this.mPendingHangupCall = call;
                    if (this.mPhone.isSRVCC()) {
                        this.mPendingHangupAddr = call.getFirstConnection().getAddress();
                    } else {
                        this.mPendingHangupAddr = this.mPendingMO.getAddress();
                    }
                    call.onHangupLocal();
                    sendEmptyMessageDelayed(101, 500);
                    return;
                }
            }
        } else if (call == this.mBackgroundCall) {
            log("(backgnd) hangup waiting or background");
        } else {
            throw new CallStateException("ImsPhoneCall " + call + "does not belong to ImsPhoneCallTracker " + this);
        }
        call.onHangupLocal();
        if (imsCall != null) {
            if (rejectCall) {
                try {
                    imsCall.reject(RadioError.OEM_ERROR_4);
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 3);
                } catch (ImsException e) {
                    throw new CallStateException(e.getMessage());
                }
            }
            imsCall.terminate(RadioError.OEM_ERROR_1);
            this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 4);
        } else if (this.mPendingMO != null && call == this.mForegroundCall) {
            this.mPendingMO.update(null, State.DISCONNECTED);
            this.mPendingMO.onDisconnect();
            removeConnection(this.mPendingMO);
            this.mPendingMO = null;
            updatePhoneState();
            removeMessages(20);
        }
        this.mPhone.notifyPreciseCallStateChanged();
    }

    void callEndCleanupHandOverCallIfAny() {
        if (this.mHandoverCall.mConnections.size() > 0) {
            log("callEndCleanupHandOverCallIfAny, mHandoverCall.mConnections=" + this.mHandoverCall.mConnections);
            this.mHandoverCall.mConnections.clear();
            this.mConnections.clear();
            this.mState = PhoneConstants.State.IDLE;
        }
    }

    void resumeWaitingOrHolding() throws CallStateException {
        log("resumeWaitingOrHolding");
        try {
            ImsCall imsCall;
            if (this.mForegroundCall.getState().isAlive()) {
                imsCall = this.mForegroundCall.getImsCall();
                if (imsCall != null) {
                    imsCall.resume();
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 6);
                }
            } else if (this.mRingingCall.getState() == State.WAITING) {
                imsCall = this.mRingingCall.getImsCall();
                if (imsCall != null) {
                    imsCall.accept(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState), addRttAttributeIfRequired(imsCall, new ImsStreamMediaProfile()));
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 2);
                }
            } else {
                imsCall = this.mBackgroundCall.getImsCall();
                if (imsCall != null) {
                    imsCall.resume();
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 6);
                }
            }
        } catch (ImsException e) {
            throw new CallStateException(e.getMessage());
        }
    }

    public void sendUSSD(String ussdString, Message response) {
        log("sendUSSD");
        try {
            if (this.mUssdSession != null) {
                this.mUssdSession.sendUssd(ussdString);
                AsyncResult.forMessage(response, null, null);
                response.sendToTarget();
            } else if (this.mImsManager == null) {
                this.mPhone.sendErrorResponse(response, getImsManagerIsNullException());
            } else {
                String[] callees = new String[]{ussdString};
                ImsCallProfile profile = this.mImsManager.createCallProfile(this.mServiceId, 1, 2);
                profile.setCallExtraInt("dialstring", 2);
                this.mUssdSession = this.mImsManager.makeCall(this.mServiceId, profile, callees, this.mImsUssdListener);
            }
        } catch (ImsException e) {
            loge("sendUSSD : " + e);
            this.mPhone.sendErrorResponse(response, e);
            retryGetImsService();
        }
    }

    public void cancelUSSD() {
        if (this.mUssdSession != null) {
            try {
                this.mUssdSession.terminate(RadioError.OEM_ERROR_1);
            } catch (ImsException e) {
            }
        }
    }

    private synchronized ImsPhoneConnection findConnection(ImsCall imsCall) {
        for (ImsPhoneConnection conn : this.mConnections) {
            if (conn.getImsCall() == imsCall) {
                return conn;
            }
        }
        return null;
    }

    private synchronized void removeConnection(ImsPhoneConnection conn) {
        this.mConnections.remove(conn);
        if (this.mIsInEmergencyCall) {
            boolean isEmergencyCallInList = false;
            for (ImsPhoneConnection imsPhoneConnection : this.mConnections) {
                if (imsPhoneConnection != null && imsPhoneConnection.isEmergency()) {
                    isEmergencyCallInList = true;
                    break;
                }
            }
            if (!isEmergencyCallInList) {
                this.mIsInEmergencyCall = false;
                this.mPhone.sendEmergencyCallStateChange(false);
            }
        }
    }

    private synchronized void addConnection(ImsPhoneConnection conn) {
        this.mConnections.add(conn);
        if (conn.isEmergency()) {
            this.mIsInEmergencyCall = true;
            this.mPhone.sendEmergencyCallStateChange(true);
        }
    }

    private void processCallStateChange(ImsCall imsCall, State state, int cause) {
        log("processCallStateChange " + imsCall + " state=" + state + " cause=" + cause);
        processCallStateChange(imsCall, state, cause, false);
    }

    private void processCallStateChange(ImsCall imsCall, State state, int cause, boolean ignoreState) {
        log("processCallStateChange state=" + state + " cause=" + cause + " ignoreState=" + ignoreState);
        if (imsCall != null) {
            ImsPhoneConnection conn = findConnection(imsCall);
            if (conn != null) {
                conn.updateMediaCapabilities(imsCall);
                if (ignoreState) {
                    conn.updateAddressDisplay(imsCall);
                    conn.updateExtras(imsCall);
                    maybeSetVideoCallProvider(conn, imsCall);
                    return;
                }
                boolean changed = conn.update(imsCall, state);
                if (state == State.DISCONNECTED) {
                    if (conn.onDisconnect(cause)) {
                        changed = true;
                    }
                    conn.getCall().detach(conn);
                    removeConnection(conn);
                }
                if (changed && conn.getCall() != this.mHandoverCall) {
                    updatePhoneState();
                    this.mPhone.notifyPreciseCallStateChanged();
                }
            }
        }
    }

    private void maybeSetVideoCallProvider(ImsPhoneConnection conn, ImsCall imsCall) {
        if (conn.getVideoProvider() == null && imsCall.getCallSession().getVideoCallProvider() != null) {
            try {
                setVideoCallProvider(conn, imsCall);
            } catch (RemoteException e) {
                loge("maybeSetVideoCallProvider: exception " + e);
            }
        }
    }

    public void addReasonCodeRemapping(Integer fromCode, String message, Integer toCode) {
        this.mImsReasonCodeMap.put(new Pair(fromCode, message), toCode);
    }

    public int maybeRemapReasonCode(ImsReasonInfo reasonInfo) {
        int code = reasonInfo.getCode();
        Pair<Integer, String> toCheck = new Pair(Integer.valueOf(code), reasonInfo.getExtraMessage());
        Pair<Integer, String> wildcardToCheck = new Pair(null, reasonInfo.getExtraMessage());
        int toCode;
        if (this.mImsReasonCodeMap.containsKey(toCheck)) {
            toCode = ((Integer) this.mImsReasonCodeMap.get(toCheck)).intValue();
            log("maybeRemapReasonCode : fromCode = " + reasonInfo.getCode() + " ; message = " + reasonInfo.getExtraMessage() + " ; toCode = " + toCode);
            return toCode;
        } else if (!this.mImsReasonCodeMap.containsKey(wildcardToCheck)) {
            return code;
        } else {
            toCode = ((Integer) this.mImsReasonCodeMap.get(wildcardToCheck)).intValue();
            log("maybeRemapReasonCode : fromCode(wildcard) = " + reasonInfo.getCode() + " ; message = " + reasonInfo.getExtraMessage() + " ; toCode = " + toCode);
            return toCode;
        }
    }

    public boolean getSwitchingFgAndBgCallsValue() {
        return this.mSwitchingFgAndBgCalls;
    }

    public void setSwitchingFgAndBgCallsValue(boolean value) {
        this.mSwitchingFgAndBgCalls = value;
    }

    public int getDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo, State callState) {
        switch (maybeRemapReasonCode(reasonInfo)) {
            case 0:
            case 339:
            case RadioError.OEM_ERROR_10 /*510*/:
            case 1014:
                return 2;
            case 106:
            case 121:
            case 122:
            case 123:
            case 124:
            case 131:
            case 132:
            case 144:
                return 18;
            case 108:
                return 45;
            case 111:
                return 17;
            case 112:
            case RadioError.OEM_ERROR_5 /*505*/:
                if (callState == State.DIALING) {
                    return 110;
                }
                return 95;
            case 143:
            case RadioError.OEM_ERROR_1 /*501*/:
                return 3;
            case 201:
            case 202:
            case 203:
            case 335:
                return 13;
            case 241:
                return 21;
            case 243:
                return 58;
            case 244:
                return 46;
            case 245:
                return 47;
            case 246:
                return 48;
            case LastCallFailCause.RADIO_OFF /*247*/:
                return 111;
            case LastCallFailCause.OUT_OF_SERVICE /*248*/:
                return 114;
            case LastCallFailCause.NO_VALID_SIM /*249*/:
                return 115;
            case LastCallFailCause.RADIO_INTERNAL_ERROR /*250*/:
                return 112;
            case LastCallFailCause.NETWORK_RESP_TIMEOUT /*251*/:
                return 113;
            case 321:
            case 331:
            case 340:
            case 361:
            case 362:
                return 12;
            case 332:
                return 12;
            case 333:
            case 352:
            case 354:
                return 9;
            case 337:
            case 341:
                return 8;
            case 338:
                return 4;
            case 363:
                return 105;
            case 364:
                return 106;
            case 1016:
                return 51;
            case 1403:
                return 53;
            case 1404:
                return 16;
            case 1405:
                return 55;
            case 1406:
                return 54;
            case 1407:
                return 59;
            case 1512:
                return 109;
            default:
                return 36;
        }
    }

    private int getPreciseDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo) {
        return PRECISE_CAUSE_MAP.get(maybeRemapReasonCode(reasonInfo), 65535);
    }

    private boolean isPhoneInEcbMode() {
        return this.mPhone.isInEcm();
    }

    private void dialPendingMO() {
        boolean isPhoneInEcmMode = isPhoneInEcbMode();
        boolean isEmergencyNumber = this.mPendingMO.isEmergency();
        if (!isPhoneInEcmMode || (isPhoneInEcmMode && isEmergencyNumber)) {
            sendEmptyMessage(20);
        } else {
            sendEmptyMessage(21);
        }
    }

    public ImsUtInterface getUtInterface() throws ImsException {
        if (this.mImsManager != null) {
            return this.mImsManager.getSupplementaryServiceConfiguration();
        }
        throw getImsManagerIsNullException();
    }

    private void transferHandoverConnections(ImsPhoneCall call) {
        if (call.mConnections != null) {
            for (Connection c : call.mConnections) {
                c.mPreHandoverState = call.mState;
                log("Connection state before handover is " + c.getStateBeforeHandover());
            }
        }
        if (this.mHandoverCall.mConnections == null) {
            this.mHandoverCall.mConnections = call.mConnections;
        } else {
            this.mHandoverCall.mConnections.addAll(call.mConnections);
        }
        if (this.mHandoverCall.mConnections != null) {
            if (call.getImsCall() != null) {
                call.getImsCall().close();
            }
            for (Connection c2 : this.mHandoverCall.mConnections) {
                ((ImsPhoneConnection) c2).changeParent(this.mHandoverCall);
                ((ImsPhoneConnection) c2).releaseWakeLock();
            }
        }
        if (call.getState().isAlive()) {
            log("Call is alive and state is " + call.mState);
            this.mHandoverCall.mState = call.mState;
        }
        call.mConnections.clear();
        call.mState = State.IDLE;
    }

    void notifySrvccState(SrvccState state) {
        log("notifySrvccState state=" + state);
        this.mSrvccState = state;
        if (this.mSrvccState == SrvccState.COMPLETED) {
            transferHandoverConnections(this.mForegroundCall);
            transferHandoverConnections(this.mBackgroundCall);
            transferHandoverConnections(this.mRingingCall);
        }
    }

    public void handleMessage(Message msg) {
        log("handleMessage what=" + msg.what);
        switch (msg.what) {
            case 14:
                if (this.pendingCallInEcm) {
                    dialInternal(this.mPendingMO, this.pendingCallClirMode, this.mPendingCallVideoState, this.mPendingIntentExtras);
                    this.mPendingIntentExtras = null;
                    this.pendingCallInEcm = false;
                }
                this.mPhone.unsetOnEcbModeExitResponse(this);
                break;
            case 18:
                if (this.mPendingMO != null) {
                    this.mPendingMO.onDisconnect();
                    removeConnection(this.mPendingMO);
                    this.mPendingMO = null;
                }
                this.mPendingIntentExtras = null;
                updatePhoneState();
                this.mPhone.notifyPreciseCallStateChanged();
                break;
            case 19:
                try {
                    resumeWaitingOrHolding();
                    break;
                } catch (CallStateException e) {
                    loge("handleMessage EVENT_RESUME_BACKGROUND exception=" + e);
                    break;
                }
            case 20:
                dialInternal(this.mPendingMO, this.mClirMode, this.mPendingCallVideoState, this.mPendingIntentExtras);
                this.mPendingIntentExtras = null;
                break;
            case 21:
                if (this.mPendingMO != null) {
                    try {
                        getEcbmInterface().exitEmergencyCallbackMode();
                        this.mPhone.setOnEcbModeExitResponse(this, 14, null);
                        this.pendingCallClirMode = this.mClirMode;
                        this.pendingCallInEcm = true;
                        break;
                    } catch (ImsException e2) {
                        e2.printStackTrace();
                        this.mPendingMO.setDisconnectCause(36);
                        sendEmptyMessageDelayed(18, 500);
                        break;
                    }
                }
                break;
            case 22:
                AsyncResult ar = msg.obj;
                ImsCall call = ar.userObj;
                Long usage = Long.valueOf(((Long) ar.result).longValue());
                log("VT data usage update. usage = " + usage + ", imsCall = " + call);
                if (usage.longValue() > 0) {
                    updateVtDataUsage(call, usage.longValue());
                    break;
                }
                break;
            case 23:
                log("VT call ignore Data Enabled Changed Event");
                break;
            case 24:
                try {
                    getImsService();
                    break;
                } catch (ImsException e22) {
                    loge("getImsService: " + e22);
                    retryGetImsService();
                    break;
                }
            case 25:
                if (msg.obj instanceof ImsCall) {
                    ImsCall imsCall = msg.obj;
                    if (imsCall == this.mForegroundCall.getImsCall()) {
                        if (!imsCall.isWifiCall()) {
                            ImsPhoneConnection conn = findConnection(imsCall);
                            if (conn != null) {
                                Rlog.i(LOG_TAG, "handoverCheck: handover failed.");
                                conn.onHandoverToWifiFailed();
                            }
                            if (imsCall.isVideoCall() && conn.getDisconnectCause() == 0) {
                                registerForConnectivityChanges();
                                break;
                            }
                        }
                    }
                    Rlog.i(LOG_TAG, "handoverCheck: no longer FG; check skipped.");
                    unregisterForConnectivityChanges();
                    return;
                }
                break;
            case 26:
                SomeArgs args = msg.obj;
                try {
                    handleFeatureCapabilityChanged(args.argi1, args.arg1, args.arg2);
                    break;
                } finally {
                    args.recycle();
                }
            case 27:
                new ImsPhoneMmiCode(this.mPhone).processImsSsData((AsyncResult) msg.obj);
                break;
            case 101:
                processPendingHangup("handler");
                break;
            case 900:
                loge("EVENT_AUTO_ANSWER:");
                try {
                    acceptCall(0);
                    break;
                } catch (Exception e3) {
                    loge("EVENT_AUTO_ANSWER: e " + e3);
                    break;
                }
        }
    }

    private void updateVtDataUsage(ImsCall call, long dataUsage) {
        long oldUsage = 0;
        if (this.mVtDataUsageMap.containsKey(Integer.valueOf(call.uniqueId))) {
            oldUsage = ((Long) this.mVtDataUsageMap.get(Integer.valueOf(call.uniqueId))).longValue();
        }
        long delta = dataUsage - oldUsage;
        this.mVtDataUsageMap.put(Integer.valueOf(call.uniqueId), Long.valueOf(dataUsage));
        log("updateVtDataUsage: call=" + call + ", delta=" + delta);
        long currentTime = SystemClock.elapsedRealtime();
        int isRoaming = this.mPhone.getServiceState().getDataRoaming() ? 1 : 0;
        NetworkStats networkStats = new NetworkStats(currentTime, 1);
        networkStats.combineAllValues(this.mVtDataUsageSnapshot);
        networkStats.combineValues(new Entry("vt_data0", -1, 1, 0, 1, isRoaming, delta / 2, 0, delta / 2, 0, 0));
        this.mVtDataUsageSnapshot = networkStats;
        networkStats = new NetworkStats(currentTime, 1);
        networkStats.combineAllValues(this.mVtDataUsageUidSnapshot);
        if (this.mDefaultDialerUid.get() == -1) {
            this.mDefaultDialerUid.set(getPackageUid(this.mPhone.getContext(), ((TelecomManager) this.mPhone.getContext().getSystemService("telecom")).getDefaultDialerPackage()));
        }
        networkStats.combineValues(new Entry("vt_data0", this.mDefaultDialerUid.get(), 1, 0, 1, isRoaming, delta / 2, 0, delta / 2, 0, 0));
        this.mVtDataUsageUidSnapshot = networkStats;
    }

    protected void log(String msg) {
        Rlog.d(LOG_TAG, "[ImsPhoneCallTracker] " + msg);
    }

    protected void loge(String msg) {
        Rlog.e(LOG_TAG, "[ImsPhoneCallTracker] " + msg);
    }

    void logState() {
        if (VERBOSE_STATE_LOGGING) {
            StringBuilder sb = new StringBuilder();
            sb.append("Current IMS PhoneCall State:\n");
            sb.append(" Foreground: ");
            sb.append(this.mForegroundCall);
            sb.append("\n");
            sb.append(" Background: ");
            sb.append(this.mBackgroundCall);
            sb.append("\n");
            sb.append(" Ringing: ");
            sb.append(this.mRingingCall);
            sb.append("\n");
            sb.append(" Handover: ");
            sb.append(this.mHandoverCall);
            sb.append("\n");
            Rlog.v(LOG_TAG, sb.toString());
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("ImsPhoneCallTracker extends:");
        super.dump(fd, pw, args);
        pw.println(" mVoiceCallEndedRegistrants=" + this.mVoiceCallEndedRegistrants);
        pw.println(" mVoiceCallStartedRegistrants=" + this.mVoiceCallStartedRegistrants);
        pw.println(" mRingingCall=" + this.mRingingCall);
        pw.println(" mForegroundCall=" + this.mForegroundCall);
        pw.println(" mBackgroundCall=" + this.mBackgroundCall);
        pw.println(" mHandoverCall=" + this.mHandoverCall);
        pw.println(" mPendingMO=" + this.mPendingMO);
        pw.println(" mPhone=" + this.mPhone);
        pw.println(" mDesiredMute=" + this.mDesiredMute);
        pw.println(" mState=" + this.mState);
        for (i = 0; i < this.mImsFeatureEnabled.length; i++) {
            pw.println(" " + this.mImsFeatureStrings[i] + ": " + (this.mImsFeatureEnabled[i] ? "enabled" : "disabled"));
        }
        pw.println(" mDefaultDialerUid=" + this.mDefaultDialerUid.get());
        pw.println(" mVtDataUsageSnapshot=" + this.mVtDataUsageSnapshot);
        pw.println(" mVtDataUsageUidSnapshot=" + this.mVtDataUsageUidSnapshot);
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
        try {
            if (this.mImsManager != null) {
                this.mImsManager.dump(fd, pw, args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mConnections != null && this.mConnections.size() > 0) {
            pw.println("mConnections:");
            for (i = 0; i < this.mConnections.size(); i++) {
                pw.println("  [" + i + "]: " + this.mConnections.get(i));
            }
        }
    }

    protected void handlePollCalls(AsyncResult ar) {
    }

    ImsEcbm getEcbmInterface() throws ImsException {
        if (this.mImsManager != null) {
            return this.mImsManager.getEcbmInterface(this.mServiceId);
        }
        throw getImsManagerIsNullException();
    }

    ImsMultiEndpoint getMultiEndpointInterface() throws ImsException {
        if (this.mImsManager == null) {
            throw getImsManagerIsNullException();
        }
        try {
            return this.mImsManager.getMultiEndpointInterface(this.mServiceId);
        } catch (ImsException e) {
            if (e.getCode() == 902) {
                return null;
            }
            throw e;
        }
    }

    public boolean isInEmergencyCall() {
        return this.mIsInEmergencyCall;
    }

    public boolean isVolteEnabled() {
        return this.mImsFeatureEnabled[0];
    }

    public boolean isVowifiEnabled() {
        return this.mImsFeatureEnabled[2];
    }

    public boolean isVideoCallEnabled() {
        if (this.mImsFeatureEnabled[1]) {
            return true;
        }
        return this.mImsFeatureEnabled[3];
    }

    public PhoneConstants.State getState() {
        return this.mState;
    }

    private void retryGetImsService() {
        if (!this.mImsManager.isServiceAvailable()) {
            this.mImsManager = null;
            loge("getImsService: Retrying getting ImsService...");
            removeMessages(24);
            sendEmptyMessageDelayed(24, (long) this.mRetryTimeout.get());
        }
    }

    private void setVideoCallProvider(ImsPhoneConnection conn, ImsCall imsCall) throws RemoteException {
        IImsVideoCallProvider imsVideoCallProvider = imsCall.getCallSession().getVideoCallProvider();
        if (imsVideoCallProvider != null) {
            boolean useVideoPauseWorkaround = this.mPhone.getContext().getResources().getBoolean(17957053);
            ImsVideoCallProviderWrapper imsVideoCallProviderWrapper = new ImsVideoCallProviderWrapper(imsVideoCallProvider);
            if (useVideoPauseWorkaround) {
                imsVideoCallProviderWrapper.setUseVideoPauseWorkaround(useVideoPauseWorkaround);
            }
            conn.setVideoProvider(imsVideoCallProviderWrapper);
            imsVideoCallProviderWrapper.registerForDataUsageUpdate(this, 22, imsCall);
            imsVideoCallProviderWrapper.addImsVideoProviderCallback(conn);
        }
    }

    public boolean isUtEnabled() {
        if (this.mImsFeatureEnabled[4]) {
            return true;
        }
        return this.mImsFeatureEnabled[5];
    }

    private String cleanseInstantLetteringMessage(String callSubject) {
        if (TextUtils.isEmpty(callSubject)) {
            return callSubject;
        }
        CarrierConfigManager configMgr = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configMgr == null) {
            return callSubject;
        }
        PersistableBundle carrierConfig = configMgr.getConfigForSubId(this.mPhone.getSubId());
        if (carrierConfig == null) {
            return callSubject;
        }
        String invalidCharacters = carrierConfig.getString("carrier_instant_lettering_invalid_chars_string");
        if (!TextUtils.isEmpty(invalidCharacters)) {
            callSubject = callSubject.replaceAll(invalidCharacters, SpnOverride.MVNO_TYPE_NONE);
        }
        String escapedCharacters = carrierConfig.getString("carrier_instant_lettering_escaped_chars_string");
        if (!TextUtils.isEmpty(escapedCharacters)) {
            callSubject = escapeChars(escapedCharacters, callSubject);
        }
        return callSubject;
    }

    private String escapeChars(String toEscape, String source) {
        StringBuilder escaped = new StringBuilder();
        for (char c : source.toCharArray()) {
            if (toEscape.contains(Character.toString(c))) {
                escaped.append("\\");
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    public void pullExternalCall(String number, int videoState, int dialogId) {
        Bundle extras = new Bundle();
        extras.putBoolean("CallPull", true);
        extras.putInt(ImsExternalCallTracker.EXTRA_IMS_EXTERNAL_CALL_ID, dialogId);
        try {
            this.mPhone.notifyUnknownConnection(dial(number, videoState, extras));
        } catch (CallStateException e) {
            loge("pullExternalCall failed - " + e);
        }
    }

    private ImsException getImsManagerIsNullException() {
        return new ImsException("no ims manager", 102);
    }

    private boolean shouldDisconnectActiveCallOnDial(boolean isEmergencyNumber) {
        if (this.mAllowHoldingVideoCall) {
            return false;
        }
        boolean isActiveVideoCall = false;
        if (this.mForegroundCall.getState() == State.ACTIVE) {
            ImsCall activeImsCall = this.mForegroundCall.getImsCall();
            if (activeImsCall != null) {
                isActiveVideoCall = activeImsCall.isVideoCall();
            }
        }
        if (!isActiveVideoCall) {
            isEmergencyNumber = false;
        }
        return isEmergencyNumber;
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean shouldDisconnectActiveCallOnAnswer(ImsCall activeCall, ImsCall incomingCall) {
        boolean z = false;
        if (activeCall == null || incomingCall == null || !this.mDropVideoCallWhenAnsweringAudioCall) {
            return false;
        }
        boolean isVoWifiEnabled;
        boolean isActiveCallVideo = !activeCall.isVideoCall() ? this.mTreatDowngradedVideoCallsAsVideoCalls ? activeCall.wasVideoCall() : false : true;
        boolean isActiveCallOnWifi = activeCall.isWifiCall();
        if (this.mImsManager.isWfcEnabledByPlatformForSlot()) {
            isVoWifiEnabled = this.mImsManager.isWfcEnabledByUserForSlot();
        } else {
            isVoWifiEnabled = false;
        }
        boolean isIncomingCallAudio = incomingCall.isVideoCall() ^ 1;
        log("shouldDisconnectActiveCallOnAnswer : isActiveCallVideo=" + isActiveCallVideo + " isActiveCallOnWifi=" + isActiveCallOnWifi + " isIncomingCallAudio=" + isIncomingCallAudio + " isVowifiEnabled=" + isVoWifiEnabled);
        if (isActiveCallVideo && isActiveCallOnWifi && isIncomingCallAudio) {
            z = isVoWifiEnabled ^ 1;
        }
        return z;
    }

    public NetworkStats getVtDataUsage(boolean perUidStats) {
        if (this.mState != PhoneConstants.State.IDLE) {
            for (ImsPhoneConnection conn : this.mConnections) {
                VideoProvider videoProvider = conn.getVideoProvider();
                if (videoProvider != null) {
                    videoProvider.onRequestConnectionDataUsage();
                }
            }
        }
        return perUidStats ? this.mVtDataUsageUidSnapshot : this.mVtDataUsageSnapshot;
    }

    public void registerPhoneStateListener(PhoneStateListener listener) {
        this.mPhoneStateListeners.add(listener);
    }

    public void unregisterPhoneStateListener(PhoneStateListener listener) {
        this.mPhoneStateListeners.remove(listener);
    }

    private void notifyPhoneStateChanged(PhoneConstants.State oldState, PhoneConstants.State newState) {
        for (PhoneStateListener listener : this.mPhoneStateListeners) {
            listener.onPhoneStateChanged(oldState, newState);
        }
    }

    private void modifyVideoCall(ImsCall imsCall, int newVideoState) {
        ImsPhoneConnection conn = findConnection(imsCall);
        if (conn != null) {
            int oldVideoState = conn.getVideoState();
            if (conn.getVideoProvider() != null) {
                conn.getVideoProvider().onSendSessionModifyRequest(new VideoProfile(oldVideoState), new VideoProfile(newVideoState));
            }
        }
    }

    public boolean isViLteDataMetered() {
        return this.mIsViLteDataMetered;
    }

    private void onDataEnabledChanged(boolean enabled, int reason) {
        log("onDataEnabledChanged: enabled=" + enabled + ", reason=" + reason);
        ImsManager.getInstance(this.mPhone.getContext(), this.mPhone.getPhoneId()).setDataEnabled(enabled);
        this.mIsDataEnabled = enabled;
        if (this.mIsViLteDataMetered) {
            int reasonCode;
            for (ImsPhoneConnection conn : this.mConnections) {
                ImsCall imsCall = conn.getImsCall();
                boolean isVideoEnabled = !enabled ? imsCall != null ? imsCall.isWifiCall() : false : true;
                conn.setVideoEnabled(isVideoEnabled);
            }
            if (reason == 3) {
                reasonCode = 1405;
            } else if (reason == 2) {
                reasonCode = 1406;
            } else {
                reasonCode = 1406;
            }
            maybeNotifyDataDisabled(enabled, reasonCode);
            handleDataEnabledChange(enabled, reasonCode);
            if (!(this.mShouldUpdateImsConfigOnDisconnect || this.mImsManager == null || reason == 0 || !this.mCarrierConfigLoaded)) {
                this.mImsManager.updateImsServiceConfigForSlot(true);
            }
            return;
        }
        log("Ignore data " + (enabled ? "enabled" : "disabled") + " - carrier policy " + "indicates that data is not metered for ViLTE calls.");
    }

    private void maybeNotifyDataDisabled(boolean enabled, int reasonCode) {
        if (!enabled) {
            for (ImsPhoneConnection conn : this.mConnections) {
                ImsCall imsCall = conn.getImsCall();
                if (imsCall != null && imsCall.isVideoCall() && (imsCall.isWifiCall() ^ 1) != 0 && conn.hasCapabilities(3)) {
                    if (reasonCode == 1406) {
                        conn.onConnectionEvent("android.telephony.event.EVENT_DOWNGRADE_DATA_DISABLED", null);
                    } else if (reasonCode == 1405) {
                        conn.onConnectionEvent("android.telephony.event.EVENT_DOWNGRADE_DATA_LIMIT_REACHED", null);
                    }
                }
            }
        }
    }

    private void handleDataEnabledChange(boolean enabled, int reasonCode) {
        if (!enabled) {
            for (ImsPhoneConnection conn : this.mConnections) {
                ImsCall imsCall = conn.getImsCall();
                if (!(imsCall == null || !imsCall.isVideoCall() || (imsCall.isWifiCall() ^ 1) == 0)) {
                    log("handleDataEnabledChange - downgrading " + conn);
                    downgradeVideoCall(reasonCode, conn);
                }
            }
        } else if (this.mSupportPauseVideo) {
            for (ImsPhoneConnection conn2 : this.mConnections) {
                log("handleDataEnabledChange - resuming " + conn2);
                if (VideoProfile.isPaused(conn2.getVideoState()) && conn2.wasVideoPausedFromSource(2)) {
                    conn2.resumeVideo(2);
                }
            }
            this.mShouldUpdateImsConfigOnDisconnect = false;
        }
    }

    private void downgradeVideoCall(int reasonCode, ImsPhoneConnection conn) {
        ImsCall imsCall = conn.getImsCall();
        if (imsCall == null) {
            return;
        }
        if (conn.hasCapabilities(3)) {
            modifyVideoCall(imsCall, 0);
        } else if (!this.mSupportPauseVideo || reasonCode == 1407) {
            try {
                imsCall.terminate(RadioError.OEM_ERROR_1, reasonCode);
            } catch (ImsException e) {
                loge("Couldn't terminate call " + imsCall);
            }
        } else {
            this.mShouldUpdateImsConfigOnDisconnect = true;
            conn.pauseVideo(2);
        }
    }

    private void resetImsCapabilities() {
        Object obj;
        log("Resetting Capabilities...");
        boolean tmpIsVideoCallEnabled = isVideoCallEnabled();
        boolean tmpIsRegistered = !isVolteEnabled() ? isVowifiEnabled() : true;
        StringBuilder append = new StringBuilder().append("Resetting Capabilities... for phoneId : ");
        if (this.mPhone == null) {
            obj = "null";
        } else {
            obj = Integer.valueOf(this.mPhone.getPhoneId());
        }
        log(append.append(obj).toString());
        if (tmpIsRegistered && this.mPhone != null) {
            this.mPhone.notifyVoLteServiceStateChanged(new VoLteServiceState(5));
            SystemProperties.set(PRO_IMS_TYPE + this.mPhone.getPhoneId(), SpnOverride.MVNO_TYPE_NONE);
        }
        int i = 0;
        while (i < this.mImsFeatureEnabled.length) {
            if (!(this.mIgnoreResetUtCapability && (i == 4 || i == 5))) {
                this.mImsFeatureEnabled[i] = false;
            }
            i++;
        }
        if (tmpIsVideoCallEnabled != isVideoCallEnabled()) {
            this.mPhone.notifyForVideoCapabilityChanged(isVideoCallEnabled());
        }
    }

    private boolean isWifiConnected() {
        boolean z = true;
        ConnectivityManager cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnected()) {
                if (ni.getType() != 1) {
                    z = false;
                }
                return z;
            }
        }
        return false;
    }

    private void registerForConnectivityChanges() {
        if (!this.mIsMonitoringConnectivity && (this.mNotifyVtHandoverToWifiFail ^ 1) == 0) {
            ConnectivityManager cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
            if (cm != null) {
                Rlog.i(LOG_TAG, "registerForConnectivityChanges");
                NetworkCapabilities capabilities = new NetworkCapabilities();
                capabilities.addTransportType(1);
                Builder builder = new Builder();
                builder.setCapabilities(capabilities);
                cm.registerNetworkCallback(builder.build(), this.mNetworkCallback);
                this.mIsMonitoringConnectivity = true;
            }
        }
    }

    private void unregisterForConnectivityChanges() {
        if (this.mIsMonitoringConnectivity && (this.mNotifyVtHandoverToWifiFail ^ 1) == 0) {
            ConnectivityManager cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
            if (cm != null) {
                Rlog.i(LOG_TAG, "unregisterForConnectivityChanges");
                cm.unregisterNetworkCallback(this.mNetworkCallback);
                this.mIsMonitoringConnectivity = false;
            }
        }
    }

    /* JADX WARNING: Missing block: B:3:0x0014, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void scheduleHandoverCheck() {
        ImsCall fgCall = this.mForegroundCall.getImsCall();
        ImsPhoneConnection conn = this.mForegroundCall.getFirstConnection();
        if (this.mNotifyVtHandoverToWifiFail && fgCall != null && (fgCall.isVideoCall() ^ 1) == 0 && conn != null && conn.getDisconnectCause() == 0 && !hasMessages(25)) {
            Rlog.i(LOG_TAG, "scheduleHandoverCheck: schedule");
            sendMessageDelayed(obtainMessage(25, fgCall), 60000);
        }
    }

    public boolean isCarrierDowngradeOfVtCallSupported() {
        return this.mSupportDowngradeVtToAudio;
    }

    public void setDataEnabled(boolean isDataEnabled) {
        this.mIsDataEnabled = isDataEnabled;
    }

    private void handleFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) {
        if (serviceClass == 1) {
            int status;
            boolean tmpIsVideoCallEnabled = isVideoCallEnabled();
            StringBuilder sb = new StringBuilder(120);
            sb.append("handleFeatureCapabilityChanged: ");
            boolean tmpIsVolteEnabled = isVolteEnabled();
            boolean tmpIsVowifiEnabled = isVowifiEnabled();
            log("handleFeatureCapabilityChanged: tmpIsVolteEnabled = " + tmpIsVolteEnabled + ", tmpIsVowifiEnabled = " + tmpIsVowifiEnabled);
            if (this.mPhone != null) {
                log("handleFeatureCapabilityChanged: for phoneId: " + this.mPhone.getPhoneId());
                TelephonyManager tm = TelephonyManager.from(this.mPhone.getContext());
                if (tm != null && tm.getSimState(this.mPhone.getPhoneId()) == 1) {
                    log("handleFeatureCapabilityChanged: sim already absent, reset and return");
                    resetImsCapabilities();
                    return;
                }
            }
            int i = 0;
            while (i <= 5 && i < enabledFeatures.length) {
                if (enabledFeatures[i] == i) {
                    sb.append(this.mImsFeatureStrings[i]);
                    sb.append(":true ");
                    this.mImsFeatureEnabled[i] = true;
                } else if (enabledFeatures[i] == -1) {
                    sb.append(this.mImsFeatureStrings[i]);
                    sb.append(":false ");
                    this.mImsFeatureEnabled[i] = false;
                } else {
                    loge("handleFeatureCapabilityChanged(" + i + ", " + this.mImsFeatureStrings[i] + "): unexpectedValue=" + enabledFeatures[i]);
                }
                i++;
            }
            boolean[] zArr = this.mImsFeatureEnabled;
            zArr[2] = zArr[2] & isWifiConnected();
            boolean isVideoEnabled = isVideoCallEnabled();
            boolean isVideoEnabledStatechanged = tmpIsVideoCallEnabled != isVideoEnabled;
            sb.append(" isVideoEnabledStateChanged=");
            sb.append(isVideoEnabledStatechanged);
            if (isVideoEnabledStatechanged) {
                log("handleFeatureCapabilityChanged - notifyForVideoCapabilityChanged=" + isVideoEnabled);
                this.mPhone.notifyForVideoCapabilityChanged(isVideoEnabled);
            }
            log(sb.toString());
            log("handleFeatureCapabilityChanged: isVolteEnabled=" + isVolteEnabled() + ", isVideoCallEnabled=" + isVideoCallEnabled() + ", isVowifiEnabled=" + isVowifiEnabled() + ", isUtEnabled=" + isUtEnabled());
            this.mPhone.onFeatureCapabilityChanged();
            this.mPhone.setImsRegistered(!isVolteEnabled() ? isVowifiEnabled() : true);
            String registerImsType = SpnOverride.MVNO_TYPE_NONE;
            if (isVolteEnabled()) {
                registerImsType = IMS_VOLTE_ENABLE;
            } else if (isVowifiEnabled()) {
                registerImsType = IMS_VOWIFI_ENABLE;
            }
            SystemProperties.set(PRO_IMS_TYPE + this.mPhone.getPhoneId(), registerImsType);
            if (!isVolteEnabled() ? isVowifiEnabled() : true) {
                status = 4;
            } else {
                status = 5;
            }
            if (!(tmpIsVolteEnabled == isVolteEnabled() && tmpIsVowifiEnabled == isVowifiEnabled())) {
                this.mPhone.notifyVoLteServiceStateChanged(new VoLteServiceState(status));
            }
            log("handleFeatureCapabilityChanged: registerImsType = " + registerImsType + ",phoneId = " + this.mPhone.getPhoneId() + ", status " + status);
            this.mMetrics.writeOnImsCapabilities(this.mPhone.getPhoneId(), this.mImsFeatureEnabled);
        }
    }

    private ImsCallProfile setRttModeBasedOnOperator(ImsCallProfile profile) {
        if (!this.mPhone.canProcessRttReqest()) {
            return profile;
        }
        int mode = QtiImsExtUtils.getRttOperatingMode(this.mPhone.getContext());
        log("RTT: setRttModeBasedOnOperator mode = " + mode);
        if ((QtiImsExtUtils.isRttSupportedOnVtCalls(this.mPhone.getPhoneId(), this.mPhone.getContext()) || !profile.isVideoCall()) && QtiImsExtUtils.isRttSupported(this.mPhone.getPhoneId(), this.mPhone.getContext())) {
            profile.mMediaProfile.setRttMode(mode);
        }
        return profile;
    }

    private ImsStreamMediaProfile addRttAttributeIfRequired(ImsCall call, ImsStreamMediaProfile mediaProfile) {
        if (!this.mPhone.canProcessRttReqest()) {
            return mediaProfile;
        }
        ImsCallProfile profile = call.getCallProfile();
        if (profile.mMediaProfile != null && profile.mMediaProfile.isRttCall() && this.mPhone.isRttVtCallAllowed(call)) {
            log("RTT: addRttAttributeIfRequired = " + profile.mMediaProfile.isRttCall());
            mediaProfile.setRttMode(1);
        }
        return mediaProfile;
    }

    private boolean isPendingResumeCall() {
        boolean isfgResume = false;
        boolean isbgResume = false;
        if (this.mForegroundCall.getImsCall() != null) {
            isfgResume = this.mForegroundCall.getImsCall().isPendingResume();
        }
        if (this.mBackgroundCall.getImsCall() != null) {
            isbgResume = this.mBackgroundCall.getImsCall().isPendingResume();
        }
        log("isfgResume = " + isfgResume + " , isbgResume = " + isbgResume);
        return !isfgResume ? isbgResume : true;
    }

    private synchronized void processPendingHangup(String msg) {
        if (this.mPendingHangupCall != null) {
            log("processPendingHangup. for " + msg);
            removeMessages(101);
            try {
                ImsCall imsCall = this.mPendingHangupCall.getImsCall();
                if (imsCall != null) {
                    imsCall.terminate(RadioError.OEM_ERROR_1);
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 4);
                }
            } catch (Exception ex) {
                log("processPendingHangup. ex:" + ex.getMessage());
            }
            this.mPendingHangupCall = null;
            this.mPendingHangupAddr = null;
        }
        return;
    }
}
