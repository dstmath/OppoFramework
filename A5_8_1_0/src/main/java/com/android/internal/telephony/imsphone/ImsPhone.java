package com.android.internal.telephony.imsphone;

import android.app.ActivityManager;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UssdResponse;
import android.text.TextUtils;
import com.android.ims.ImsCall;
import com.android.ims.ImsCallForwardInfo;
import com.android.ims.ImsCallProfile;
import com.android.ims.ImsEcbmStateListener;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.ims.ImsReasonInfo;
import com.android.ims.ImsSsInfo;
import com.android.internal.telephony.Call.SrvccState;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.SettingsObserver;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.util.NotificationChannelController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.codeaurora.ims.QtiCallConstants;
import org.codeaurora.ims.utils.QtiImsExtUtils;

public class ImsPhone extends ImsPhoneBase {
    static final int CANCEL_ECM_TIMER = 1;
    private static final boolean DBG = true;
    private static final int DEFAULT_ECM_EXIT_TIMER_VALUE = 300000;
    private static final int EVENT_CARRIER_CONFIG_CHANGED = 1047;
    private static final int EVENT_DEFAULT_DATA_SUBSCRIPTION_CHANGED = 1045;
    private static final int EVENT_DEFAULT_PHONE_DATA_STATE_CHANGED = 52;
    private static final int EVENT_GET_CALL_BARRING_DONE = 47;
    private static final int EVENT_GET_CALL_WAITING_DONE = 49;
    private static final int EVENT_GET_CLIR_DONE = 51;
    private static final int EVENT_NETWORK_MODE_CHANGED = 1046;
    private static final int EVENT_SERVICE_STATE_CHANGED = 53;
    private static final int EVENT_SET_CALL_BARRING_DONE = 46;
    private static final int EVENT_SET_CALL_WAITING_DONE = 48;
    private static final int EVENT_SET_CLIR_DONE = 50;
    private static final int EVENT_VOICE_CALL_ENDED = 54;
    private static final String LOG_TAG = "ImsPhone";
    static final int RESTART_ECM_TIMER = 0;
    private static final boolean VDBG = false;
    ImsPhoneCallTracker mCT;
    private Uri[] mCurrentSubscriberUris;
    Phone mDefaultPhone;
    private Registrant mEcmExitRespRegistrant;
    private Runnable mExitEcmRunnable;
    ImsExternalCallTracker mExternalCallTracker;
    private ImsEcbmStateListener mImsEcbmStateListener;
    private boolean mImsRegistered;
    private String mLastDialString;
    private ArrayList<ImsPhoneMmiCode> mPendingMMIs;
    private final BroadcastReceiver mReceiver;
    private BroadcastReceiver mResultReceiver;
    private boolean mRoaming;
    private BroadcastReceiver mRttReceiver;
    private ServiceState mSS;
    SettingsObserver mSettingObserver;
    private final RegistrantList mSilentRedialRegistrants;
    private RegistrantList mSsnRegistrants;
    private WakeLock mWakeLock;

    private static class Cf {
        final boolean mIsCfu;
        final Message mOnComplete;
        final int mServiceClass;
        final String mSetCfNumber;

        Cf(String cfNumber, boolean isCfu, Message onComplete, int serviceClass) {
            this.mSetCfNumber = cfNumber;
            this.mIsCfu = isCfu;
            this.mOnComplete = onComplete;
            this.mServiceClass = serviceClass;
        }
    }

    protected void setCurrentSubscriberUris(Uri[] currentSubscriberUris) {
        this.mCurrentSubscriberUris = currentSubscriberUris;
    }

    public Uri[] getCurrentSubscriberUris() {
        return this.mCurrentSubscriberUris;
    }

    public ImsPhone(Context context, PhoneNotifier notifier, Phone defaultPhone) {
        this(context, notifier, defaultPhone, false);
    }

    public ImsPhone(Context context, PhoneNotifier notifier, Phone defaultPhone, boolean unitTestMode) {
        super(LOG_TAG, context, notifier, unitTestMode);
        this.mPendingMMIs = new ArrayList();
        this.mSS = new ServiceState();
        this.mSilentRedialRegistrants = new RegistrantList();
        this.mImsRegistered = false;
        this.mRoaming = false;
        this.mSsnRegistrants = new RegistrantList();
        this.mExitEcmRunnable = new Runnable() {
            public void run() {
                ImsPhone.this.exitEmergencyCallbackMode();
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction().equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                    ImsPhone.this.sendEmptyMessage(ImsPhone.EVENT_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
                } else if (intent != null && intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    ImsPhone.this.sendEmptyMessage(ImsPhone.EVENT_CARRIER_CONFIG_CHANGED);
                }
            }
        };
        this.mSettingObserver = null;
        this.mImsEcbmStateListener = new ImsEcbmStateListener() {
            public void onECBMEntered() {
                Rlog.d(ImsPhone.LOG_TAG, "onECBMEntered");
                ImsPhone.this.handleEnterEmergencyCallbackMode();
            }

            public void onECBMExited() {
                Rlog.d(ImsPhone.LOG_TAG, "onECBMExited");
                ImsPhone.this.handleExitEmergencyCallbackMode();
            }
        };
        this.mResultReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (getResultCode() == -1) {
                    CharSequence title = intent.getCharSequenceExtra(Phone.EXTRA_KEY_ALERT_TITLE);
                    CharSequence messageAlert = intent.getCharSequenceExtra(Phone.EXTRA_KEY_ALERT_MESSAGE);
                    CharSequence messageNotification = intent.getCharSequenceExtra(Phone.EXTRA_KEY_NOTIFICATION_MESSAGE);
                    Intent resultIntent = new Intent("android.intent.action.MAIN");
                    resultIntent.setClassName("com.android.settings", "com.android.settings.Settings$WifiCallingSettingsActivity");
                    resultIntent.putExtra(Phone.EXTRA_KEY_ALERT_SHOW, true);
                    resultIntent.putExtra(Phone.EXTRA_KEY_ALERT_TITLE, title);
                    resultIntent.putExtra(Phone.EXTRA_KEY_ALERT_MESSAGE, messageAlert);
                    String notificationTag = "wifi_calling";
                    ((NotificationManager) ImsPhone.this.mContext.getSystemService("notification")).notify("wifi_calling", 1, new Builder(ImsPhone.this.mContext).setSmallIcon(17301642).setContentTitle(title).setContentText(messageNotification).setAutoCancel(true).setContentIntent(PendingIntent.getActivity(ImsPhone.this.mContext, 0, resultIntent, 134217728)).setStyle(new BigTextStyle().bigText(messageNotification)).setChannelId(NotificationChannelController.CHANNEL_ID_WFC).build());
                }
            }
        };
        this.mRttReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (QtiCallConstants.ACTION_SEND_RTT_TEXT.equals(intent.getAction())) {
                    Rlog.d(ImsPhone.LOG_TAG, "RTT: Received ACTION_SEND_RTT_TEXT");
                    ImsPhone.this.sendRttMessage(intent.getStringExtra(QtiCallConstants.RTT_TEXT_VALUE));
                } else if (QtiCallConstants.ACTION_RTT_OPERATION.equals(intent.getAction())) {
                    Rlog.d(ImsPhone.LOG_TAG, "RTT: Received ACTION_RTT_OPERATION");
                    ImsPhone.this.checkIfModifyRequestOrResponse(intent.getIntExtra(QtiCallConstants.RTT_OPERATION_TYPE, 0));
                } else {
                    Rlog.d(ImsPhone.LOG_TAG, "RTT: unknown intent");
                }
            }
        };
        this.mDefaultPhone = defaultPhone;
        this.mExternalCallTracker = TelephonyComponentFactory.getInstance().makeImsExternalCallTracker(this);
        this.mCT = TelephonyComponentFactory.getInstance().makeImsPhoneCallTracker(this);
        this.mCT.registerPhoneStateListener(this.mExternalCallTracker);
        this.mExternalCallTracker.setCallPuller(this.mCT);
        this.mSS.setStateOff();
        this.mPhoneId = this.mDefaultPhone.getPhoneId();
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, LOG_TAG);
        this.mWakeLock.setReferenceCounted(false);
        if (this.mDefaultPhone.getServiceStateTracker() != null) {
            this.mDefaultPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(this, 52, null);
        }
        setServiceState(1);
        this.mDefaultPhone.registerForServiceStateChanged(this, 53, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(QtiCallConstants.ACTION_SEND_RTT_TEXT);
        filter.addAction(QtiCallConstants.ACTION_RTT_OPERATION);
        this.mDefaultPhone.getContext().registerReceiver(this.mRttReceiver, filter);
        this.mSettingObserver = new SettingsObserver(this.mContext, this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
    }

    public void dispose() {
        Rlog.d(LOG_TAG, "dispose");
        this.mPendingMMIs.clear();
        this.mExternalCallTracker.tearDown();
        this.mCT.unregisterPhoneStateListener(this.mExternalCallTracker);
        this.mCT.unregisterForVoiceCallEnded(this);
        this.mCT.dispose();
        if (!(this.mDefaultPhone == null || this.mDefaultPhone.getServiceStateTracker() == null)) {
            this.mDefaultPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(this);
            this.mDefaultPhone.unregisterForServiceStateChanged(this);
            this.mDefaultPhone.getContext().unregisterReceiver(this.mRttReceiver);
        }
        if (this.mSettingObserver != null) {
            this.mSettingObserver.unobserve();
        }
    }

    public ServiceState getServiceState() {
        return this.mSS;
    }

    public void setServiceState(int state) {
        boolean isVoiceRegStateChanged;
        synchronized (this) {
            isVoiceRegStateChanged = this.mSS.getVoiceRegState() != state;
            this.mSS.setVoiceRegState(state);
        }
        updateDataServiceState();
        if (isVoiceRegStateChanged) {
            this.mNotifier.notifyServiceState(this.mDefaultPhone);
        }
    }

    public CallTracker getCallTracker() {
        return this.mCT;
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String number) {
        IccRecords r = getIccRecords();
        if (r != null) {
            setVoiceCallForwardingFlag(r, line, enable, number);
        }
    }

    public ImsExternalCallTracker getExternalCallTracker() {
        return this.mExternalCallTracker;
    }

    public List<? extends ImsPhoneMmiCode> getPendingMmiCodes() {
        return this.mPendingMMIs;
    }

    public void acceptCall(int videoState) throws CallStateException {
        this.mCT.acceptCall(videoState);
    }

    public void rejectCall() throws CallStateException {
        this.mCT.rejectCall();
    }

    public void switchHoldingAndActive() throws CallStateException {
        this.mCT.switchWaitingOrHoldingAndActive();
    }

    public boolean canConference() {
        return this.mCT.canConference();
    }

    public boolean canDial() {
        return this.mCT.canDial();
    }

    public void conference() {
        this.mCT.conference();
    }

    public void clearDisconnected() {
        this.mCT.clearDisconnected();
    }

    public boolean canTransfer() {
        return this.mCT.canTransfer();
    }

    public void explicitCallTransfer() {
        this.mCT.explicitCallTransfer();
    }

    public ImsPhoneCall getForegroundCall() {
        return this.mCT.mForegroundCall;
    }

    public ImsPhoneCall getBackgroundCall() {
        return this.mCT.mBackgroundCall;
    }

    public ImsPhoneCall getRingingCall() {
        return this.mCT.mRingingCall;
    }

    public boolean isImsAvailable() {
        return this.mCT.isImsServiceReady();
    }

    private boolean handleCallDeflectionIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        if (getRingingCall().getState() != State.IDLE) {
            Rlog.d(LOG_TAG, "MmiCode 0: rejectCall");
            try {
                this.mCT.rejectCall();
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "reject failed", e);
                notifySuppServiceFailed(SuppService.REJECT);
            }
        } else if (getBackgroundCall().getState() != State.IDLE) {
            Rlog.d(LOG_TAG, "MmiCode 0: hangupWaitingOrBackground");
            try {
                this.mCT.hangup(getBackgroundCall());
            } catch (CallStateException e2) {
                Rlog.d(LOG_TAG, "hangup failed", e2);
            }
        }
        return true;
    }

    private void sendUssdResponse(String ussdRequest, CharSequence message, int returnCode, ResultReceiver wrappedCallback) {
        UssdResponse response = new UssdResponse(ussdRequest, message);
        Bundle returnData = new Bundle();
        returnData.putParcelable("USSD_RESPONSE", response);
        wrappedCallback.send(returnCode, returnData);
    }

    public boolean handleUssdRequest(String ussdRequest, ResultReceiver wrappedCallback) throws CallStateException {
        if (this.mPendingMMIs.size() > 0) {
            Rlog.i(LOG_TAG, "handleUssdRequest: queue full: " + Rlog.pii(LOG_TAG, ussdRequest));
            sendUssdResponse(ussdRequest, null, -1, wrappedCallback);
            return true;
        }
        try {
            dialInternal(ussdRequest, 0, null, wrappedCallback);
        } catch (CallStateException cse) {
            if (Phone.CS_FALLBACK.equals(cse.getMessage())) {
                throw cse;
            }
            Rlog.w(LOG_TAG, "Could not execute USSD " + cse);
            sendUssdResponse(ussdRequest, null, -1, wrappedCallback);
        } catch (Exception e) {
            Rlog.w(LOG_TAG, "Could not execute USSD " + e);
            sendUssdResponse(ussdRequest, null, -1, wrappedCallback);
            return false;
        }
        return true;
    }

    private boolean handleCallWaitingIncallSupplementaryService(String dialString) {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        ImsPhoneCall call = getForegroundCall();
        if (len > 1) {
            try {
                Rlog.d(LOG_TAG, "not support 1X SEND");
                notifySuppServiceFailed(SuppService.HANGUP);
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "hangup failed", e);
                notifySuppServiceFailed(SuppService.HANGUP);
            }
        } else if (call.getState() != State.IDLE) {
            Rlog.d(LOG_TAG, "MmiCode 1: hangup foreground");
            this.mCT.hangup(call);
        } else {
            Rlog.d(LOG_TAG, "MmiCode 1: switchWaitingOrHoldingAndActive");
            this.mCT.switchWaitingOrHoldingAndActive();
        }
        return true;
    }

    private boolean handleCallHoldIncallSupplementaryService(String dialString) {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        if (len > 1) {
            Rlog.d(LOG_TAG, "separate not supported");
            notifySuppServiceFailed(SuppService.SEPARATE);
        } else {
            try {
                if (getRingingCall().getState() != State.IDLE) {
                    Rlog.d(LOG_TAG, "MmiCode 2: accept ringing call");
                    this.mCT.acceptCall(2);
                } else {
                    Rlog.d(LOG_TAG, "MmiCode 2: switchWaitingOrHoldingAndActive");
                    this.mCT.switchWaitingOrHoldingAndActive();
                }
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "switch failed", e);
                notifySuppServiceFailed(SuppService.SWITCH);
            }
        }
        return true;
    }

    private boolean handleMultipartyIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        Rlog.d(LOG_TAG, "MmiCode 3: merge calls");
        conference();
        return true;
    }

    private boolean handleEctIncallSupplementaryService(String dialString) {
        if (dialString.length() != 1) {
            return false;
        }
        Rlog.d(LOG_TAG, "MmiCode 4: not support explicit call transfer");
        notifySuppServiceFailed(SuppService.TRANSFER);
        return true;
    }

    private boolean handleCcbsIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        Rlog.i(LOG_TAG, "MmiCode 5: CCBS not supported!");
        notifySuppServiceFailed(SuppService.UNKNOWN);
        return true;
    }

    public void notifySuppSvcNotification(SuppServiceNotification suppSvc) {
        Rlog.d(LOG_TAG, "notifySuppSvcNotification: suppSvc = " + suppSvc);
        this.mSsnRegistrants.notifyRegistrants(new AsyncResult(null, suppSvc, null));
    }

    public boolean handleInCallMmiCommands(String dialString) {
        if (!isInCall() || TextUtils.isEmpty(dialString)) {
            return false;
        }
        boolean result = false;
        switch (dialString.charAt(0)) {
            case '0':
                result = handleCallDeflectionIncallSupplementaryService(dialString);
                break;
            case '1':
                result = handleCallWaitingIncallSupplementaryService(dialString);
                break;
            case '2':
                result = handleCallHoldIncallSupplementaryService(dialString);
                break;
            case '3':
                result = handleMultipartyIncallSupplementaryService(dialString);
                break;
            case '4':
                result = handleEctIncallSupplementaryService(dialString);
                break;
            case '5':
                result = handleCcbsIncallSupplementaryService(dialString);
                break;
        }
        return result;
    }

    boolean isInCall() {
        State foregroundCallState = getForegroundCall().getState();
        State backgroundCallState = getBackgroundCall().getState();
        State ringingCallState = getRingingCall().getState();
        if (foregroundCallState.isAlive() || backgroundCallState.isAlive()) {
            return true;
        }
        return ringingCallState.isAlive();
    }

    public boolean isInActiveCall() {
        State foregroundCallState = getForegroundCall().getState();
        State backgroundCallState = getBackgroundCall().getState();
        State ringingCallState = getRingingCall().getState();
        if (foregroundCallState.isActive() || backgroundCallState.isActive()) {
            return true;
        }
        return ringingCallState.isActive();
    }

    public boolean isInEcm() {
        return this.mDefaultPhone.isInEcm();
    }

    public void setIsInEcm(boolean isInEcm) {
        this.mDefaultPhone.setIsInEcm(isInEcm);
    }

    public void notifyNewRingingConnection(Connection c) {
        this.mDefaultPhone.notifyNewRingingConnectionP(c);
    }

    void notifyUnknownConnection(Connection c) {
        this.mDefaultPhone.notifyUnknownConnectionP(c);
    }

    public void notifyForVideoCapabilityChanged(boolean isVideoCapable) {
        this.mIsVideoCapable = isVideoCapable;
        this.mDefaultPhone.notifyForVideoCapabilityChanged(isVideoCapable);
    }

    public Connection dial(String dialString, int videoState) throws CallStateException {
        return dialInternal(dialString, videoState, null, null);
    }

    public Connection dial(String dialString, UUSInfo uusInfo, int videoState, Bundle intentExtras) throws CallStateException {
        return dialInternal(dialString, videoState, intentExtras, null);
    }

    protected Connection dialInternal(String dialString, int videoState, Bundle intentExtras) throws CallStateException {
        return dialInternal(dialString, videoState, intentExtras, null);
    }

    private Connection dialInternal(String dialString, int videoState, Bundle intentExtras, ResultReceiver wrappedCallback) throws CallStateException {
        boolean isConferenceUri = false;
        int isSkipSchemaParsing = 0;
        if (intentExtras != null) {
            isConferenceUri = intentExtras.getBoolean("org.codeaurora.extra.DIAL_CONFERENCE_URI", false);
            isSkipSchemaParsing = intentExtras.getBoolean("org.codeaurora.extra.SKIP_SCHEMA_PARSING", false);
        }
        String newDialString = dialString;
        if (!(isConferenceUri || (isSkipSchemaParsing ^ 1) == 0)) {
            newDialString = PhoneNumberUtils.stripSeparators(dialString);
        }
        if (handleInCallMmiCommands(newDialString)) {
            return null;
        }
        if (this.mDefaultPhone.getPhoneType() == 2) {
            return this.mCT.dial(dialString, videoState, intentExtras);
        }
        ImsPhoneMmiCode mmi = ImsPhoneMmiCode.newFromDialString(PhoneNumberUtils.extractNetworkPortionAlt(newDialString), this, wrappedCallback);
        Rlog.d(LOG_TAG, "dialInternal: dialing w/ mmi '" + mmi + "'...");
        if (mmi == null) {
            return this.mCT.dial(dialString, videoState, intentExtras);
        }
        if (mmi.isTemporaryModeCLIR()) {
            return this.mCT.dial(mmi.getDialingNumber(), mmi.getCLIRMode(), videoState, intentExtras);
        }
        if (mmi.isSupportedOverImsPhone()) {
            this.mPendingMMIs.add(mmi);
            this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
            try {
                mmi.processCode();
            } catch (CallStateException cse) {
                if (Phone.CS_FALLBACK.equals(cse.getMessage())) {
                    Rlog.i(LOG_TAG, "dialInternal: fallback to GSM required.");
                    this.mPendingMMIs.remove(mmi);
                    throw cse;
                }
            }
            return null;
        }
        Rlog.i(LOG_TAG, "dialInternal: USSD not supported by IMS; fallback to CS.");
        throw new CallStateException(Phone.CS_FALLBACK);
    }

    public void addParticipant(String dialString) throws CallStateException {
        addParticipant(dialString, null);
    }

    public void addParticipant(String dialString, Message onComplete) throws CallStateException {
        this.mCT.addParticipant(dialString, onComplete);
    }

    public void sendDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            Rlog.e(LOG_TAG, "sendDtmf called with invalid character '" + c + "'");
        } else if (this.mCT.getState() == PhoneConstants.State.OFFHOOK) {
            this.mCT.sendDtmf(c, null);
        }
    }

    public void startDtmf(char c) {
        Object obj = 1;
        if (!PhoneNumberUtils.is12Key(c) && (c < 'A' || c > 'D')) {
            obj = null;
        }
        if (obj == null) {
            Rlog.e(LOG_TAG, "startDtmf called with invalid character '" + c + "'");
        } else {
            this.mCT.startDtmf(c);
        }
    }

    public void stopDtmf() {
        this.mCT.stopDtmf();
    }

    public void notifyIncomingRing() {
        Rlog.d(LOG_TAG, "notifyIncomingRing");
        sendMessage(obtainMessage(14, new AsyncResult(null, null, null)));
    }

    public void setMute(boolean muted) {
        this.mCT.setMute(muted);
    }

    public void setTTYMode(int ttyMode, Message onComplete) {
        this.mCT.setTtyMode(ttyMode);
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        this.mCT.setUiTTYMode(uiTtyMode, onComplete);
    }

    public boolean getMute() {
        return this.mCT.getMute();
    }

    public PhoneConstants.State getState() {
        return this.mCT.getState();
    }

    private boolean isValidCommandInterfaceCFReason(int commandInterfaceCFReason) {
        switch (commandInterfaceCFReason) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return true;
            default:
                return false;
        }
    }

    private boolean isValidCommandInterfaceCFAction(int commandInterfaceCFAction) {
        switch (commandInterfaceCFAction) {
            case 0:
            case 1:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private boolean isCfEnable(int action) {
        return action == 1 || action == 3;
    }

    private int getConditionFromCFReason(int reason) {
        switch (reason) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            default:
                return -1;
        }
    }

    private int getCFReasonFromCondition(int condition) {
        switch (condition) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            default:
                return 3;
        }
    }

    private int getActionFromCFAction(int action) {
        switch (action) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 3:
                return 3;
            case 4:
                return 4;
            default:
                return -1;
        }
    }

    public void getOutgoingCallerIdDisplay(Message onComplete) {
        Rlog.d(LOG_TAG, "getCLIR");
        try {
            this.mCT.getUtInterface().queryCLIR(obtainMessage(51, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setOutgoingCallerIdDisplay(int clirMode, Message onComplete) {
        Rlog.d(LOG_TAG, "setCLIR action= " + clirMode);
        try {
            this.mCT.getUtInterface().updateCLIR(clirMode, obtainMessage(50, clirMode, 0, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        getCallForwardingOption(commandInterfaceCFReason, 1, onComplete);
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, int commandInterfaceServiceClass, Message onComplete) {
        Rlog.d(LOG_TAG, "getCallForwardingOption reason=" + commandInterfaceCFReason + "serviceclass =" + commandInterfaceServiceClass);
        if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            Rlog.d(LOG_TAG, "requesting call forwarding query.");
            Message resp = obtainMessage(13, onComplete);
            String operator = SystemProperties.get("ro.oppo.operator", "US");
            String region = SystemProperties.get("persist.sys.oppo.region", "US");
            Rlog.d(LOG_TAG, "operator and region : " + operator + " " + region);
            if ((((operator.equals("TELSTRA") || operator.equals("VODAFONE")) && region.equals("AU")) || (OemConstant.isJioCard(this.mDefaultPhone) && region.equals("IN"))) && commandInterfaceServiceClass == 0) {
                commandInterfaceServiceClass = 1;
                Rlog.d(LOG_TAG, "telstra and vodafone default serviceClass set to VOICE!");
            }
            try {
                this.mCT.getUtInterface().queryCallForward(getConditionFromCFReason(commandInterfaceCFReason), null, commandInterfaceServiceClass, resp);
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
        setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, 1, timerSeconds, onComplete);
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int serviceClass, int timerSeconds, Message onComplete) {
        int i = 1;
        Rlog.d(LOG_TAG, "setCallForwardingOption action=" + commandInterfaceCFAction + ", reason=" + commandInterfaceCFReason + " serviceClass=" + serviceClass);
        if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            Cf cf = new Cf(dialingNumber, commandInterfaceCFReason == 0, onComplete, serviceClass);
            if (!isCfEnable(commandInterfaceCFAction)) {
                i = 0;
            }
            try {
                this.mCT.getUtInterface().updateCallForward(getActionFromCFAction(commandInterfaceCFAction), getConditionFromCFReason(commandInterfaceCFReason), dialingNumber, serviceClass, timerSeconds, obtainMessage(12, i, 0, cf));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    public void getCallWaiting(Message onComplete) {
        Rlog.d(LOG_TAG, "getCallWaiting");
        try {
            this.mCT.getUtInterface().queryCallWaiting(obtainMessage(49, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setCallWaiting(boolean enable, Message onComplete) {
        setCallWaiting(enable, 1, onComplete);
    }

    public void setCallWaiting(boolean enable, int serviceClass, Message onComplete) {
        Rlog.d(LOG_TAG, "setCallWaiting enable=" + enable);
        try {
            this.mCT.getUtInterface().updateCallWaiting(enable, serviceClass, obtainMessage(48, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    private int getCBTypeFromFacility(String facility) {
        if (CommandsInterface.CB_FACILITY_BAOC.equals(facility)) {
            return 2;
        }
        if (CommandsInterface.CB_FACILITY_BAOIC.equals(facility)) {
            return 3;
        }
        if (CommandsInterface.CB_FACILITY_BAOICxH.equals(facility)) {
            return 4;
        }
        if (CommandsInterface.CB_FACILITY_BAIC.equals(facility)) {
            return 1;
        }
        if (CommandsInterface.CB_FACILITY_BAICr.equals(facility)) {
            return 5;
        }
        if (CommandsInterface.CB_FACILITY_BA_ALL.equals(facility)) {
            return 7;
        }
        if (CommandsInterface.CB_FACILITY_BA_MO.equals(facility)) {
            return 8;
        }
        if (CommandsInterface.CB_FACILITY_BA_MT.equals(facility)) {
            return 9;
        }
        return 0;
    }

    public void getCallBarring(String facility, Message onComplete) {
        getCallBarring(facility, 0, onComplete);
    }

    public void getCallBarring(String facility, int serviceClass, Message onComplete) {
        Rlog.d(LOG_TAG, "getCallBarring facility=" + facility + "serviceClass = " + serviceClass);
        try {
            this.mCT.getUtInterface().queryCallBarring(getCBTypeFromFacility(facility), serviceClass, obtainMessage(47, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete) {
        setCallBarring(facility, lockState, 0, password, onComplete);
    }

    public void setCallBarring(String facility, boolean lockState, int serviceClass, String password, Message onComplete) {
        int action;
        Rlog.d(LOG_TAG, "setCallBarring facility=" + facility + ", lockState=" + lockState + "serviceClass = " + serviceClass);
        Message resp = obtainMessage(46, onComplete);
        if (lockState) {
            action = 1;
        } else {
            action = 0;
        }
        try {
            this.mCT.getUtInterface().updateCallBarring(getCBTypeFromFacility(facility), action, serviceClass, resp, null);
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void sendUssdResponse(String ussdMessge) {
        Rlog.d(LOG_TAG, "sendUssdResponse");
        ImsPhoneMmiCode mmi = ImsPhoneMmiCode.newFromUssdUserInput(ussdMessge, this);
        this.mPendingMMIs.add(mmi);
        this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
        mmi.sendUssd(ussdMessge);
    }

    public void sendUSSD(String ussdString, Message response) {
        this.mCT.sendUSSD(ussdString, response);
    }

    public void cancelUSSD() {
        this.mCT.cancelUSSD();
    }

    private void sendErrorResponse(Message onComplete) {
        Rlog.d(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, new CommandException(Error.GENERIC_FAILURE));
            onComplete.sendToTarget();
        }
    }

    public void sendErrorResponse(Message onComplete, Throwable e) {
        Rlog.d(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, getCommandException(e));
            onComplete.sendToTarget();
        }
    }

    private CommandException getCommandException(int code, String errorString) {
        Rlog.d(LOG_TAG, "getCommandException code= " + code + ", errorString= " + errorString);
        Error error = Error.GENERIC_FAILURE;
        switch (code) {
            case 241:
                error = Error.FDN_CHECK_FAILURE;
                break;
            case 801:
                error = Error.REQUEST_NOT_SUPPORTED;
                break;
            case 802:
                error = Error.RADIO_NOT_AVAILABLE;
                break;
            case 821:
                error = Error.PASSWORD_INCORRECT;
                break;
            case 822:
                error = Error.SS_MODIFIED_TO_DIAL;
                break;
            case 823:
                error = Error.SS_MODIFIED_TO_USSD;
                break;
            case 824:
                error = Error.SS_MODIFIED_TO_SS;
                break;
            case 825:
                error = Error.SS_MODIFIED_TO_DIAL_VIDEO;
                break;
        }
        return new CommandException(error, errorString);
    }

    private CommandException getCommandException(Throwable e) {
        if (e instanceof ImsException) {
            return getCommandException(((ImsException) e).getCode(), e.getMessage());
        }
        Rlog.d(LOG_TAG, "getCommandException generic failure");
        return new CommandException(Error.GENERIC_FAILURE);
    }

    private void onNetworkInitiatedUssd(ImsPhoneMmiCode mmi) {
        Rlog.d(LOG_TAG, "onNetworkInitiatedUssd");
        this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
    }

    void onIncomingUSSD(int ussdMode, String ussdMessage) {
        Rlog.d(LOG_TAG, "onIncomingUSSD ussdMode=" + ussdMode);
        boolean isUssdRequest = ussdMode == 1;
        boolean isUssdError = ussdMode != 0 ? ussdMode != 1 : false;
        ImsPhoneMmiCode found = null;
        int s = this.mPendingMMIs.size();
        for (int i = 0; i < s; i++) {
            if (((ImsPhoneMmiCode) this.mPendingMMIs.get(i)).isPendingUSSD()) {
                found = (ImsPhoneMmiCode) this.mPendingMMIs.get(i);
                break;
            }
        }
        if (found != null) {
            if (isUssdError) {
                found.onUssdFinishedError();
            } else {
                found.onUssdFinished(ussdMessage, isUssdRequest);
            }
        } else if (!isUssdError && ussdMessage != null) {
            onNetworkInitiatedUssd(ImsPhoneMmiCode.newNetworkInitiatedUssd(ussdMessage, isUssdRequest, this));
        }
    }

    public void onMMIDone(ImsPhoneMmiCode mmi) {
        Rlog.d(LOG_TAG, "onMMIDone: mmi=" + mmi);
        if (this.mPendingMMIs.remove(mmi) || mmi.isUssdRequest() || mmi.isSsInfo()) {
            ResultReceiver receiverCallback = mmi.getUssdCallbackReceiver();
            if (receiverCallback != null) {
                sendUssdResponse(mmi.getDialString(), mmi.getMessage(), mmi.getState() == MmiCode.State.COMPLETE ? 100 : -1, receiverCallback);
                return;
            }
            Rlog.v(LOG_TAG, "onMMIDone: notifyRegistrants");
            this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
        }
    }

    public ArrayList<Connection> getHandoverConnection() {
        ArrayList<Connection> connList = new ArrayList();
        connList.addAll(getForegroundCall().mConnections);
        connList.addAll(getBackgroundCall().mConnections);
        connList.addAll(getRingingCall().mConnections);
        if (connList.size() > 0) {
            return connList;
        }
        return null;
    }

    public void notifySrvccState(SrvccState state) {
        this.mCT.notifySrvccState(state);
    }

    void initiateSilentRedial() {
        AsyncResult ar = new AsyncResult(null, this.mLastDialString, null);
        if (ar != null) {
            this.mSilentRedialRegistrants.notifyRegistrants(ar);
        }
    }

    public void registerForSilentRedial(Handler h, int what, Object obj) {
        this.mSilentRedialRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSilentRedial(Handler h) {
        this.mSilentRedialRegistrants.remove(h);
    }

    public void registerForSuppServiceNotification(Handler h, int what, Object obj) {
        this.mSsnRegistrants.addUnique(h, what, obj);
        if (this.mSsnRegistrants.size() == 1) {
            this.mDefaultPhone.mCi.setSuppServiceNotifications(true, null);
        }
    }

    public void unregisterForSuppServiceNotification(Handler h) {
        this.mSsnRegistrants.remove(h);
        if (this.mSsnRegistrants.size() == 0) {
            this.mDefaultPhone.mCi.setSuppServiceNotifications(false, null);
        }
    }

    public int getSubId() {
        return this.mDefaultPhone.getSubId();
    }

    public int getPhoneId() {
        return this.mDefaultPhone.getPhoneId();
    }

    public IccRecords getIccRecords() {
        return this.mDefaultPhone.getIccRecords();
    }

    private CallForwardInfo getCallForwardInfo(ImsCallForwardInfo info) {
        CallForwardInfo cfInfo = new CallForwardInfo();
        cfInfo.status = info.mStatus;
        cfInfo.reason = getCFReasonFromCondition(info.mCondition);
        if (info.mServiceClass == 80) {
            cfInfo.serviceClass = info.mServiceClass;
        } else {
            cfInfo.serviceClass = 1;
        }
        cfInfo.toa = info.mToA;
        cfInfo.number = info.mNumber;
        cfInfo.timeSeconds = info.mTimeSeconds;
        return cfInfo;
    }

    public CallForwardInfo[] handleCfQueryResult(ImsCallForwardInfo[] infos) {
        CallForwardInfo[] cfInfos = null;
        if (!(infos == null || infos.length == 0)) {
            cfInfos = new CallForwardInfo[infos.length];
        }
        IccRecords r = this.mDefaultPhone.getIccRecords();
        if (infos != null && infos.length != 0) {
            int s = infos.length;
            for (int i = 0; i < s; i++) {
                if (infos[i].mCondition == 0) {
                    if (infos[i].mServiceClass == 80) {
                        boolean z;
                        if (infos[i].mStatus == 1) {
                            z = true;
                        } else {
                            z = false;
                        }
                        setVideoCallForwardingPreference(z);
                    } else if (r != null) {
                        setVoiceCallForwardingFlag(r, 1, infos[i].mStatus == 1, infos[i].mNumber);
                    }
                    notifyCallForwardingIndicator();
                }
                cfInfos[i] = getCallForwardInfo(infos[i]);
            }
        } else if (r != null) {
            setVoiceCallForwardingFlag(r, 1, false, null);
        }
        return cfInfos;
    }

    private int[] handleCbQueryResult(ImsSsInfo[] infos) {
        int[] cbInfos = new int[]{0};
        if (infos[0].mStatus == 1) {
            cbInfos[0] = 1;
        }
        return cbInfos;
    }

    private int[] handleCwQueryResult(ImsSsInfo[] infos) {
        int[] cwInfos = new int[2];
        cwInfos[0] = 0;
        if (infos[0].mStatus == 1) {
            cwInfos[0] = 1;
            cwInfos[1] = 1;
        }
        return cwInfos;
    }

    private void sendResponse(Message onComplete, Object result, Throwable e) {
        if (onComplete != null) {
            CommandException ex = null;
            if (e != null) {
                ex = getCommandException(e);
            }
            AsyncResult.forMessage(onComplete, result, ex);
            onComplete.sendToTarget();
        }
    }

    private void updateDataServiceState() {
        if (this.mSS != null && this.mDefaultPhone.getServiceStateTracker() != null && this.mDefaultPhone.getServiceStateTracker().mSS != null) {
            ServiceState ss = this.mDefaultPhone.getServiceStateTracker().mSS;
            this.mSS.setDataRegState(ss.getDataRegState());
            this.mSS.setRilDataRadioTechnology(ss.getRilDataRadioTechnology());
            Rlog.d(LOG_TAG, "updateDataServiceState: defSs = " + ss + " imsSs = " + this.mSS);
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar = msg.obj;
        Rlog.d(LOG_TAG, "handleMessage what=" + msg.what);
        switch (msg.what) {
            case 12:
                IccRecords r = this.mDefaultPhone.getIccRecords();
                Cf cf = ar.userObj;
                if (cf.mIsCfu && ar.exception == null && r != null && cf.mServiceClass == 1) {
                    setVoiceCallForwardingFlag(r, 1, msg.arg1 == 1, cf.mSetCfNumber);
                }
                sendResponse(cf.mOnComplete, null, ar.exception);
                return;
            case 13:
                Object cfInfos = null;
                if (ar.exception == null) {
                    cfInfos = handleCfQueryResult((ImsCallForwardInfo[]) ar.result);
                }
                sendResponse((Message) ar.userObj, cfInfos, ar.exception);
                return;
            case 46:
            case 48:
                break;
            case 47:
            case 49:
                Object ssInfos = null;
                if (ar.exception == null) {
                    if (msg.what == 47) {
                        ssInfos = handleCbQueryResult((ImsSsInfo[]) ar.result);
                    } else if (msg.what == 49) {
                        ssInfos = handleCwQueryResult((ImsSsInfo[]) ar.result);
                    }
                }
                sendResponse((Message) ar.userObj, ssInfos, ar.exception);
                return;
            case 50:
                if (ar.exception == null) {
                    saveClirSetting(msg.arg1);
                    break;
                }
                break;
            case 51:
                Bundle ssInfo = ar.result;
                Object clirInfo = null;
                if (ssInfo != null) {
                    clirInfo = ssInfo.getIntArray(ImsPhoneMmiCode.UT_BUNDLE_KEY_CLIR);
                }
                sendResponse((Message) ar.userObj, clirInfo, ar.exception);
                return;
            case 52:
                Rlog.d(LOG_TAG, "EVENT_DEFAULT_PHONE_DATA_STATE_CHANGED");
                updateDataServiceState();
                return;
            case 53:
                ServiceState newServiceState = msg.obj.result;
                if (this.mRoaming == newServiceState.getRoaming()) {
                    return;
                }
                if (newServiceState.getVoiceRegState() == 0 || newServiceState.getDataRegState() == 0) {
                    Rlog.d(LOG_TAG, "Roaming state changed - " + this.mRoaming);
                    updateRoamingState(newServiceState.getRoaming());
                    return;
                }
                return;
            case 54:
                Rlog.d(LOG_TAG, "Voice call ended. Handle pending updateRoamingState.");
                this.mCT.unregisterForVoiceCallEnded(this);
                boolean newRoaming = getCurrentRoaming();
                if (this.mRoaming != newRoaming) {
                    updateRoamingState(newRoaming);
                    return;
                }
                return;
            case EVENT_DEFAULT_DATA_SUBSCRIPTION_CHANGED /*1045*/:
                handleDdsChange();
                return;
            case EVENT_NETWORK_MODE_CHANGED /*1046*/:
                handleNetworkModeChange();
                return;
            case EVENT_CARRIER_CONFIG_CHANGED /*1047*/:
                if (this.mSettingObserver != null) {
                    this.mSettingObserver.unobserve();
                    this.mSettingObserver.observe(Global.getUriFor("preferred_network_mode" + getSubId()), EVENT_NETWORK_MODE_CHANGED);
                    return;
                }
                return;
            default:
                super.handleMessage(msg);
                return;
        }
        sendResponse((Message) ar.userObj, null, ar.exception);
    }

    public ImsEcbmStateListener getImsEcbmStateListener() {
        return this.mImsEcbmStateListener;
    }

    public boolean isInEmergencyCall() {
        return this.mCT.isInEmergencyCall();
    }

    private void sendEmergencyCallbackModeChange() {
        Intent intent = new Intent("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        intent.putExtra("phoneinECMState", isInEcm());
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, getPhoneId());
        ActivityManager.broadcastStickyIntent(intent, -1);
        Rlog.d(LOG_TAG, "sendEmergencyCallbackModeChange: isInEcm=" + isInEcm());
    }

    public void exitEmergencyCallbackMode() {
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        Rlog.d(LOG_TAG, "exitEmergencyCallbackMode()");
        try {
            this.mCT.getEcbmInterface().exitEmergencyCallbackMode();
        } catch (ImsException e) {
            e.printStackTrace();
        }
    }

    private void handleEnterEmergencyCallbackMode() {
        Rlog.d(LOG_TAG, "handleEnterEmergencyCallbackMode,mIsPhoneInEcmState= " + isInEcm());
        if (!isInEcm()) {
            setIsInEcm(true);
            sendEmergencyCallbackModeChange();
            postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", RIL.DEFAULT_DUP_SMS_KEPP_PERIOD));
            this.mWakeLock.acquire();
        }
    }

    protected void handleExitEmergencyCallbackMode() {
        Rlog.d(LOG_TAG, "handleExitEmergencyCallbackMode: mIsPhoneInEcmState = " + isInEcm());
        if (isInEcm()) {
            setIsInEcm(false);
        }
        removeCallbacks(this.mExitEcmRunnable);
        if (this.mEcmExitRespRegistrant != null) {
            this.mEcmExitRespRegistrant.notifyResult(Boolean.TRUE);
        }
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        sendEmergencyCallbackModeChange();
        ((GsmCdmaPhone) this.mDefaultPhone).notifyEmergencyCallRegistrants(false);
    }

    void handleTimerInEmergencyCallbackMode(int action) {
        switch (action) {
            case 0:
                postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", RIL.DEFAULT_DUP_SMS_KEPP_PERIOD));
                ((GsmCdmaPhone) this.mDefaultPhone).notifyEcbmTimerReset(Boolean.FALSE);
                return;
            case 1:
                removeCallbacks(this.mExitEcmRunnable);
                ((GsmCdmaPhone) this.mDefaultPhone).notifyEcbmTimerReset(Boolean.TRUE);
                return;
            default:
                Rlog.e(LOG_TAG, "handleTimerInEmergencyCallbackMode, unsupported action " + action);
                return;
        }
    }

    public void setOnEcbModeExitResponse(Handler h, int what, Object obj) {
        this.mEcmExitRespRegistrant = new Registrant(h, what, obj);
    }

    public void unsetOnEcbModeExitResponse(Handler h) {
        this.mEcmExitRespRegistrant.clear();
    }

    public void onFeatureCapabilityChanged() {
        this.mDefaultPhone.getServiceStateTracker().onImsCapabilityChanged();
    }

    public boolean isVolteEnabled() {
        return this.mCT.isVolteEnabled();
    }

    public boolean isWifiCallingEnabled() {
        return this.mCT.isVowifiEnabled();
    }

    public boolean isVideoEnabled() {
        return this.mCT.isVideoCallEnabled();
    }

    public Phone getDefaultPhone() {
        return this.mDefaultPhone;
    }

    public boolean isImsRegistered() {
        Rlog.d(LOG_TAG, "imsphone isImsRegistered =" + this.mImsRegistered);
        return this.mImsRegistered;
    }

    public void setImsRegistered(boolean value) {
        this.mImsRegistered = value;
    }

    public void callEndCleanupHandOverCallIfAny() {
        this.mCT.callEndCleanupHandOverCallIfAny();
    }

    public void processDisconnectReason(ImsReasonInfo imsReasonInfo) {
        if (imsReasonInfo.mCode == 1000 && imsReasonInfo.mExtraMessage != null && ImsManager.isWfcEnabledByUser(this.mContext)) {
            processWfcDisconnectForNotification(imsReasonInfo);
        }
    }

    private void processWfcDisconnectForNotification(ImsReasonInfo imsReasonInfo) {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configManager == null) {
            Rlog.e(LOG_TAG, "processDisconnectReason: CarrierConfigManager is not ready");
            return;
        }
        PersistableBundle pb = configManager.getConfigForSubId(getSubId());
        if (pb == null) {
            Rlog.e(LOG_TAG, "processDisconnectReason: no config for subId " + getSubId());
            return;
        }
        String[] wfcOperatorErrorCodes = pb.getStringArray("wfc_operator_error_codes_string_array");
        if (wfcOperatorErrorCodes != null) {
            String[] wfcOperatorErrorAlertMessages = this.mContext.getResources().getStringArray(17236076);
            String[] wfcOperatorErrorNotificationMessages = this.mContext.getResources().getStringArray(17236077);
            for (int i = 0; i < wfcOperatorErrorCodes.length; i++) {
                String[] codes = wfcOperatorErrorCodes[i].split("\\|");
                if (codes.length != 2) {
                    Rlog.e(LOG_TAG, "Invalid carrier config: " + wfcOperatorErrorCodes[i]);
                } else if (imsReasonInfo.mExtraMessage.startsWith(codes[0])) {
                    int codeStringLength = codes[0].length();
                    if (!Character.isLetterOrDigit(codes[0].charAt(codeStringLength - 1)) || imsReasonInfo.mExtraMessage.length() <= codeStringLength || !Character.isLetterOrDigit(imsReasonInfo.mExtraMessage.charAt(codeStringLength))) {
                        CharSequence title = this.mContext.getText(17041073);
                        int idx = Integer.parseInt(codes[1]);
                        if (idx < 0 || idx >= wfcOperatorErrorAlertMessages.length || idx >= wfcOperatorErrorNotificationMessages.length) {
                            Rlog.e(LOG_TAG, "Invalid index: " + wfcOperatorErrorCodes[i]);
                        } else {
                            String messageAlert = imsReasonInfo.mExtraMessage;
                            String messageNotification = imsReasonInfo.mExtraMessage;
                            if (!wfcOperatorErrorAlertMessages[idx].isEmpty()) {
                                messageAlert = String.format(wfcOperatorErrorAlertMessages[idx], new Object[]{imsReasonInfo.mExtraMessage});
                            }
                            if (!wfcOperatorErrorNotificationMessages[idx].isEmpty()) {
                                messageNotification = String.format(wfcOperatorErrorNotificationMessages[idx], new Object[]{imsReasonInfo.mExtraMessage});
                            }
                            Intent intent = new Intent("com.android.ims.REGISTRATION_ERROR");
                            intent.putExtra(Phone.EXTRA_KEY_ALERT_TITLE, title);
                            intent.putExtra(Phone.EXTRA_KEY_ALERT_MESSAGE, messageAlert);
                            intent.putExtra(Phone.EXTRA_KEY_NOTIFICATION_MESSAGE, messageNotification);
                            this.mContext.sendOrderedBroadcast(intent, null, this.mResultReceiver, null, -1, null, null);
                        }
                    }
                } else {
                    continue;
                }
            }
        }
    }

    public boolean isUtEnabled() {
        boolean z = false;
        if (OemConstant.isCTA(this.mContext) && OemConstant.isCtCard(this.mDefaultPhone)) {
            return false;
        }
        int imsFeatureState = 0;
        try {
            imsFeatureState = ImsManager.getInstance(this.mContext, this.mPhoneId).getImsServiceStatus();
        } catch (ImsException ex) {
            Rlog.d(LOG_TAG, "Exception when trying to get ImsServiceStatus: " + ex);
        }
        if (this.mCT.isUtEnabled() && imsFeatureState == 2) {
            z = true;
        }
        return z;
    }

    public void sendEmergencyCallStateChange(boolean callActive) {
        this.mDefaultPhone.sendEmergencyCallStateChange(callActive);
    }

    public void setBroadcastEmergencyCallStateChanges(boolean broadcast) {
        this.mDefaultPhone.setBroadcastEmergencyCallStateChanges(broadcast);
    }

    public void notifyCallForwardingIndicator() {
        this.mDefaultPhone.notifyCallForwardingIndicator();
    }

    public WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    public NetworkStats getVtDataUsage(boolean perUidStats) {
        return this.mCT.getVtDataUsage(perUidStats);
    }

    private void updateRoamingState(boolean newRoaming) {
        if (this.mCT.getState() == PhoneConstants.State.IDLE) {
            Rlog.d(LOG_TAG, "updateRoamingState now: " + newRoaming);
            this.mRoaming = newRoaming;
            ImsManager imsMgr = ImsManager.getInstance(this.mContext, this.mPhoneId);
            imsMgr.setWfcModeForSlot(imsMgr.getWfcModeForSlot(newRoaming), newRoaming);
            return;
        }
        Rlog.d(LOG_TAG, "updateRoamingState postponed: " + newRoaming);
        this.mCT.registerForVoiceCallEnded(this, 54, null);
    }

    private boolean getCurrentRoaming() {
        return ((TelephonyManager) this.mContext.getSystemService("phone")).isNetworkRoaming();
    }

    public void sendRttMessage(String data) {
        if (!canProcessRttReqest() || (isFgCallActive() ^ 1) != 0) {
            return;
        }
        if (TextUtils.isEmpty(data)) {
            Rlog.d(LOG_TAG, "RTT: Text null");
            return;
        }
        ImsCall imsCall = getForegroundCall().getImsCall();
        if (imsCall == null) {
            Rlog.d(LOG_TAG, "RTT: imsCall null");
        } else if (isRttVtCallAllowed(imsCall)) {
            Rlog.d(LOG_TAG, "RTT: sendRttMessage");
            imsCall.sendRttMessage(data);
        } else {
            Rlog.d(LOG_TAG, "RTT: InCorrect mode");
        }
    }

    public void sendRttModifyRequest(ImsCallProfile to) {
        Rlog.d(LOG_TAG, "RTT: sendRttModifyRequest");
        ImsCall imsCall = getForegroundCall().getImsCall();
        if (imsCall == null) {
            Rlog.d(LOG_TAG, "RTT: imsCall null");
            return;
        }
        try {
            imsCall.sendRttModifyRequest(to);
        } catch (ImsException e) {
            Rlog.e(LOG_TAG, "RTT: sendRttModifyRequest exception = " + e);
        }
    }

    public void sendRttModifyResponse(int response) {
        ImsCall imsCall = getForegroundCall().getImsCall();
        if (imsCall == null) {
            Rlog.d(LOG_TAG, "RTT: imsCall null");
        } else if (isRttVtCallAllowed(imsCall)) {
            Rlog.d(LOG_TAG, "RTT: sendRttModifyResponse");
            imsCall.sendRttModifyResponse(mapRequestToResponse(response));
        } else {
            Rlog.d(LOG_TAG, "RTT: Not allowed for VT");
        }
    }

    private void checkIfModifyRequestOrResponse(int data) {
        if (canProcessRttReqest() && (isFgCallActive() ^ 1) == 0) {
            Rlog.d(LOG_TAG, "RTT: checkIfModifyRequestOrResponse data =  " + data);
            switch (data) {
                case 1:
                    packRttModifyRequestToProfile(1);
                    break;
                case 2:
                case 3:
                    sendRttModifyResponse(data);
                    break;
                case 4:
                    packRttModifyRequestToProfile(0);
                    break;
            }
        }
    }

    private void packRttModifyRequestToProfile(int data) {
        if (canSendRttModifyRequest()) {
            ImsCallProfile fromProfile = getForegroundCall().getImsCall().getCallProfile();
            ImsCallProfile toProfile = new ImsCallProfile(fromProfile.mServiceType, fromProfile.mCallType);
            toProfile.mMediaProfile.setRttMode(data);
            Rlog.d(LOG_TAG, "RTT: packRttModifyRequestToProfile");
            sendRttModifyRequest(toProfile);
            return;
        }
        Rlog.d(LOG_TAG, "RTT: cannot send rtt modify request");
    }

    private boolean canSendRttModifyRequest() {
        if (getForegroundCall().getImsCall() != null) {
            return true;
        }
        Rlog.d(LOG_TAG, "RTT: imsCall null");
        return false;
    }

    private boolean mapRequestToResponse(int response) {
        switch (response) {
            case 2:
                return true;
            case 3:
                return false;
            default:
                return false;
        }
    }

    private boolean isInFullRttMode() {
        int mode = QtiImsExtUtils.getRttOperatingMode(this.mContext);
        Rlog.d(LOG_TAG, "RTT: isInFullRttMode mode = " + mode);
        if (mode == 1) {
            return true;
        }
        return false;
    }

    public boolean isRttVtCallAllowed(ImsCall call) {
        Rlog.d(LOG_TAG, "RTT: isRttVtCallAllowed mode = " + QtiImsExtUtils.getRttOperatingMode(this.mContext));
        if (!call.getCallProfile().isVideoCall() || (QtiImsExtUtils.isRttSupportedOnVtCalls(this.mPhoneId, this.mContext) ^ 1) == 0) {
            return true;
        }
        return false;
    }

    public boolean canProcessRttReqest() {
        boolean isRttOn;
        if (QtiImsExtUtils.isRttSupported(this.mPhoneId, this.mContext)) {
            isRttOn = QtiImsExtUtils.isRttOn(this.mContext);
        } else {
            isRttOn = false;
        }
        if (isRttOn) {
            Rlog.d(LOG_TAG, "RTT: canProcessRttReqest rtt supported = " + QtiImsExtUtils.isRttSupported(this.mPhoneId, this.mContext) + ", is Rtt on = " + QtiImsExtUtils.isRttOn(this.mContext) + ", Rtt mode = " + QtiImsExtUtils.getRttMode(this.mContext));
            return true;
        }
        Rlog.d(LOG_TAG, "RTT: canProcessRttReqest RTT is not supported/off");
        return false;
    }

    public boolean isFgCallActive() {
        if (State.ACTIVE == getForegroundCall().getState()) {
            return true;
        }
        Rlog.d(LOG_TAG, "RTT: isFgCallActive fg call not active");
        return false;
    }

    private void handleDdsChange() {
        Rlog.d(LOG_TAG, "handleDdsChange");
        ImsManager imsmanager = ImsManager.getInstance(this.mContext, this.mPhoneId);
        if (imsmanager != null && imsmanager.isWfcEnabledByPlatformForSlot()) {
            imsmanager.updateImsServiceConfigForSlot(true);
        }
    }

    private void handleNetworkModeChange() {
        ImsManager imsmanager = ImsManager.getInstance(this.mContext, this.mPhoneId);
        if (this.mDefaultPhone != null && imsmanager != null) {
            int subId = this.mDefaultPhone.getSubId();
            Rlog.d(LOG_TAG, "handleNetworkModeChange");
            if (subId != SubscriptionManager.getDefaultDataSubId() && imsmanager.isWfcEnabledByPlatformForSlot()) {
                imsmanager.updateImsServiceConfigForSlot(true);
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ImsPhone extends:");
        super.dump(fd, pw, args);
        pw.flush();
        pw.println("ImsPhone:");
        pw.println("  mDefaultPhone = " + this.mDefaultPhone);
        pw.println("  mPendingMMIs = " + this.mPendingMMIs);
        pw.println("  mPostDialHandler = " + this.mPostDialHandler);
        pw.println("  mSS = " + this.mSS);
        pw.println("  mWakeLock = " + this.mWakeLock);
        pw.println("  mIsPhoneInEcmState = " + isInEcm());
        pw.println("  mEcmExitRespRegistrant = " + this.mEcmExitRespRegistrant);
        pw.println("  mSilentRedialRegistrants = " + this.mSilentRedialRegistrants);
        pw.println("  mImsRegistered = " + this.mImsRegistered);
        pw.println("  mRoaming = " + this.mRoaming);
        pw.println("  mSsnRegistrants = " + this.mSsnRegistrants);
        pw.flush();
    }
}
