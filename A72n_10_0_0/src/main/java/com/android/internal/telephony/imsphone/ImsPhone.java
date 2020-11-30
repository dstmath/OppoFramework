package com.android.internal.telephony.imsphone;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.LinkProperties;
import android.net.NetworkStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.telecom.Connection;
import android.telephony.CallQuality;
import android.telephony.CarrierConfigManager;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.NetworkScanRequest;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.UssdResponse;
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsSsInfo;
import android.text.TextUtils;
import com.android.ims.ImsEcbmStateListener;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.IOppoCallManager;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.dataconnection.TransportManager;
import com.android.internal.telephony.emergency.EmergencyNumberTracker;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.util.NotificationChannelController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ImsPhone extends ImsPhoneBase {
    public static final int CANCEL_ECM_TIMER = 1;
    protected static final boolean DBG = true;
    protected static final int DEFAULT_ECM_EXIT_TIMER_VALUE = 300000;
    protected static final int EVENT_DEFAULT_PHONE_DATA_STATE_CHANGED = 59;
    protected static final int EVENT_GET_CALL_BARRING_DONE = 54;
    protected static final int EVENT_GET_CALL_WAITING_DONE = 56;
    protected static final int EVENT_GET_CLIR_DONE = 58;
    @VisibleForTesting
    public static final int EVENT_SERVICE_STATE_CHANGED = 60;
    protected static final int EVENT_SET_CALL_BARRING_DONE = 53;
    protected static final int EVENT_SET_CALL_WAITING_DONE = 55;
    protected static final int EVENT_SET_CLIR_DONE = 57;
    protected static final int EVENT_VOICE_CALL_ENDED = 61;
    private static final String LOG_TAG = "ImsPhone";
    public static final int RESTART_ECM_TIMER = 0;
    private static final boolean VDBG = false;
    @UnsupportedAppUsage
    public ImsPhoneCallTracker mCT;
    private Uri[] mCurrentSubscriberUris;
    public Phone mDefaultPhone;
    protected Registrant mEcmExitRespRegistrant;
    protected Runnable mExitEcmRunnable;
    protected ImsExternalCallTracker mExternalCallTracker;
    private ImsEcbmStateListener mImsEcbmStateListener;
    protected boolean mImsRegistered;
    private String mLastDialString;
    @UnsupportedAppUsage
    protected ArrayList<ImsPhoneMmiCode> mPendingMMIs;
    protected BroadcastReceiver mResultReceiver;
    protected boolean mRoaming;
    @UnsupportedAppUsage
    protected ServiceState mSS;
    private final RegistrantList mSilentRedialRegistrants;
    private RegistrantList mSsnRegistrants;
    protected PowerManager.WakeLock mWakeLock;

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void activateCellBroadcastSms(int i, Message message) {
        super.activateCellBroadcastSms(i, message);
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ boolean disableDataConnectivity() {
        return super.disableDataConnectivity();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void disableLocationUpdates() {
        super.disableLocationUpdates();
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ boolean enableDataConnectivity() {
        return super.enableDataConnectivity();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void enableLocationUpdates() {
        super.enableLocationUpdates();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void getAvailableNetworks(Message message) {
        super.getAvailableNetworks(message);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ boolean getCallForwardingIndicator() {
        return super.getCallForwardingIndicator();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void getCellBroadcastSmsConfig(Message message) {
        super.getCellBroadcastSmsConfig(message);
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ List getCurrentDataConnectionList() {
        return super.getCurrentDataConnectionList();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ PhoneInternalInterface.DataActivityState getDataActivityState() {
        return super.getDataActivityState();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ PhoneConstants.DataState getDataConnectionState() {
        return super.getDataConnectionState();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ boolean getDataRoamingEnabled() {
        return super.getDataRoamingEnabled();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ String getDeviceId() {
        return super.getDeviceId();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ String getDeviceSvn() {
        return super.getDeviceSvn();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ String getEsn() {
        return super.getEsn();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ String getGroupIdLevel1() {
        return super.getGroupIdLevel1();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ String getGroupIdLevel2() {
        return super.getGroupIdLevel2();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ IccCard getIccCard() {
        return super.getIccCard();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ IccFileHandler getIccFileHandler() {
        return super.getIccFileHandler();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager() {
        return super.getIccPhoneBookInterfaceManager();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ boolean getIccRecordsLoaded() {
        return super.getIccRecordsLoaded();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ String getIccSerialNumber() {
        return super.getIccSerialNumber();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ String getImei() {
        return super.getImei();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ String getLine1AlphaTag() {
        return super.getLine1AlphaTag();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ String getLine1Number() {
        return super.getLine1Number();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ LinkProperties getLinkProperties(String str) {
        return super.getLinkProperties(str);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ String getMeid() {
        return super.getMeid();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ boolean getMessageWaitingIndicator() {
        return super.getMessageWaitingIndicator();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ int getPhoneType() {
        return super.getPhoneType();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ SignalStrength getSignalStrength() {
        return super.getSignalStrength();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ String getSubscriberId() {
        return super.getSubscriberId();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ String getVoiceMailAlphaTag() {
        return super.getVoiceMailAlphaTag();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ String getVoiceMailNumber() {
        return super.getVoiceMailNumber();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ boolean handlePinMmi(String str) {
        return super.handlePinMmi(str);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ boolean isDataAllowed(int i) {
        return super.isDataAllowed(i);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ boolean isUserDataEnabled() {
        return super.isUserDataEnabled();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void migrateFrom(Phone phone) {
        super.migrateFrom(phone);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ boolean needsOtaServiceProvisioning() {
        return super.needsOtaServiceProvisioning();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void notifyCallForwardingIndicator() {
        super.notifyCallForwardingIndicator();
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void notifyDisconnect(Connection connection) {
        super.notifyDisconnect(connection);
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void notifyImsReason(ImsReasonInfo imsReasonInfo) {
        super.notifyImsReason(imsReasonInfo);
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void notifyPhoneStateChanged() {
        super.notifyPhoneStateChanged();
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void notifyPreciseCallStateChanged() {
        super.notifyPreciseCallStateChanged();
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void notifySuppServiceFailed(PhoneInternalInterface.SuppService suppService) {
        super.notifySuppServiceFailed(suppService);
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void onCallQualityChanged(CallQuality callQuality, int i) {
        super.onCallQualityChanged(callQuality, i);
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void onTtyModeReceived(int i) {
        super.onTtyModeReceived(i);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void registerForOnHoldTone(Handler handler, int i, Object obj) {
        super.registerForOnHoldTone(handler, i, obj);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void registerForRingbackTone(Handler handler, int i, Object obj) {
        super.registerForRingbackTone(handler, i, obj);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void registerForTtyModeReceived(Handler handler, int i, Object obj) {
        super.registerForTtyModeReceived(handler, i, obj);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void selectNetworkManually(OperatorInfo operatorInfo, boolean z, Message message) {
        super.selectNetworkManually(operatorInfo, z, message);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void setCellBroadcastSmsConfig(int[] iArr, Message message) {
        super.setCellBroadcastSmsConfig(iArr, message);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void setDataRoamingEnabled(boolean z) {
        super.setDataRoamingEnabled(z);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ boolean setLine1Number(String str, String str2, Message message) {
        return super.setLine1Number(str, str2, message);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void setNetworkSelectionModeAutomatic(Message message) {
        super.setNetworkSelectionModeAutomatic(message);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void setVoiceMailNumber(String str, String str2, Message message) {
        super.setVoiceMailNumber(str, str2, message);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void startNetworkScan(NetworkScanRequest networkScanRequest, Message message) {
        super.startNetworkScan(networkScanRequest, message);
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    @VisibleForTesting
    public /* bridge */ /* synthetic */ void startOnHoldTone(Connection connection) {
        super.startOnHoldTone(connection);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void startRingbackTone() {
        super.startRingbackTone();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void stopNetworkScan(Message message) {
        super.stopNetworkScan(message);
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void stopOnHoldTone(Connection connection) {
        super.stopOnHoldTone(connection);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void stopRingbackTone() {
        super.stopRingbackTone();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void unregisterForOnHoldTone(Handler handler) {
        super.unregisterForOnHoldTone(handler);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void unregisterForRingbackTone(Handler handler) {
        super.unregisterForRingbackTone(handler);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void unregisterForTtyModeReceived(Handler handler) {
        super.unregisterForTtyModeReceived(handler);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public /* bridge */ /* synthetic */ void updateServiceLocation() {
        super.updateServiceLocation();
    }

    public static class ImsDialArgs extends PhoneInternalInterface.DialArgs {
        public final int clirMode;
        public final Connection.RttTextStream rttTextStream;

        public static class Builder extends PhoneInternalInterface.DialArgs.Builder<Builder> {
            private int mClirMode = 0;
            private Connection.RttTextStream mRttTextStream;

            public static Builder from(PhoneInternalInterface.DialArgs dialArgs) {
                return (Builder) ((Builder) ((Builder) new Builder().setUusInfo(dialArgs.uusInfo)).setVideoState(dialArgs.videoState)).setIntentExtras(dialArgs.intentExtras);
            }

            public static Builder from(ImsDialArgs dialArgs) {
                return ((Builder) ((Builder) ((Builder) new Builder().setUusInfo(dialArgs.uusInfo)).setVideoState(dialArgs.videoState)).setIntentExtras(dialArgs.intentExtras)).setRttTextStream(dialArgs.rttTextStream).setClirMode(dialArgs.clirMode);
            }

            public Builder setRttTextStream(Connection.RttTextStream s) {
                this.mRttTextStream = s;
                return this;
            }

            public Builder setClirMode(int clirMode) {
                this.mClirMode = clirMode;
                return this;
            }

            @Override // com.android.internal.telephony.PhoneInternalInterface.DialArgs.Builder
            public ImsDialArgs build() {
                return new ImsDialArgs(this);
            }
        }

        private ImsDialArgs(Builder b) {
            super(b);
            this.rttTextStream = b.mRttTextStream;
            this.clirMode = b.mClirMode;
        }
    }

    public void setCurrentSubscriberUris(Uri[] currentSubscriberUris) {
        this.mCurrentSubscriberUris = currentSubscriberUris;
    }

    @Override // com.android.internal.telephony.Phone
    public Uri[] getCurrentSubscriberUris() {
        return this.mCurrentSubscriberUris;
    }

    @Override // com.android.internal.telephony.Phone
    public EmergencyNumberTracker getEmergencyNumberTracker() {
        return this.mDefaultPhone.getEmergencyNumberTracker();
    }

    @Override // com.android.internal.telephony.Phone
    public ServiceStateTracker getServiceStateTracker() {
        return this.mDefaultPhone.getServiceStateTracker();
    }

    /* access modifiers changed from: protected */
    public static class Cf {
        public final boolean mIsCfu;
        public final Message mOnComplete;
        public final int mServiceClass;
        public final String mSetCfNumber;

        @UnsupportedAppUsage
        public Cf(String cfNumber, boolean isCfu, Message onComplete, int serviceClass) {
            this.mSetCfNumber = cfNumber;
            this.mIsCfu = isCfu;
            this.mOnComplete = onComplete;
            this.mServiceClass = serviceClass;
        }
    }

    public ImsPhone(Context context, PhoneNotifier notifier, Phone defaultPhone) {
        this(context, notifier, defaultPhone, false);
    }

    @VisibleForTesting
    public ImsPhone(Context context, PhoneNotifier notifier, Phone defaultPhone, boolean unitTestMode) {
        super(LOG_TAG, context, notifier, unitTestMode);
        this.mPendingMMIs = new ArrayList<>();
        this.mSS = new ServiceState();
        this.mSilentRedialRegistrants = new RegistrantList();
        this.mImsRegistered = false;
        this.mRoaming = false;
        this.mSsnRegistrants = new RegistrantList();
        this.mExitEcmRunnable = new Runnable() {
            /* class com.android.internal.telephony.imsphone.ImsPhone.AnonymousClass1 */

            public void run() {
                ImsPhone.this.exitEmergencyCallbackMode();
            }
        };
        this.mImsEcbmStateListener = new ImsEcbmStateListener() {
            /* class com.android.internal.telephony.imsphone.ImsPhone.AnonymousClass2 */

            public void onECBMEntered() {
                ImsPhone.this.logd("onECBMEntered");
                ImsPhone.this.handleEnterEmergencyCallbackMode();
            }

            public void onECBMExited() {
                ImsPhone.this.logd("onECBMExited");
                ImsPhone.this.handleExitEmergencyCallbackMode();
            }
        };
        this.mResultReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.imsphone.ImsPhone.AnonymousClass3 */

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
                    ((NotificationManager) ImsPhone.this.mContext.getSystemService("notification")).notify("wifi_calling", 1, new Notification.Builder(ImsPhone.this.mContext).setSmallIcon(17301642).setContentTitle(title).setContentText(messageNotification).setAutoCancel(true).setContentIntent(PendingIntent.getActivity(ImsPhone.this.mContext, 0, resultIntent, 134217728)).setStyle(new Notification.BigTextStyle().bigText(messageNotification)).setChannelId(NotificationChannelController.CHANNEL_ID_WFC).build());
                }
            }
        };
        this.mDefaultPhone = defaultPhone;
        this.mExternalCallTracker = TelephonyComponentFactory.getInstance().inject(ImsExternalCallTracker.class.getName()).makeImsExternalCallTracker(this);
        this.mCT = TelephonyComponentFactory.getInstance().inject(ImsPhoneCallTracker.class.getName()).makeImsPhoneCallTracker(this);
        this.mCT.registerPhoneStateListener(this.mExternalCallTracker);
        this.mExternalCallTracker.setCallPuller(this.mCT);
        this.mSS.setStateOff();
        this.mPhoneId = this.mDefaultPhone.getPhoneId();
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, LOG_TAG);
        this.mWakeLock.setReferenceCounted(false);
        if (!(this.mDefaultPhone.getServiceStateTracker() == null || this.mDefaultPhone.getTransportManager() == null)) {
            for (int transport : this.mDefaultPhone.getTransportManager().getAvailableTransports()) {
                this.mDefaultPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(transport, this, 59, null);
            }
        }
        setServiceState(1);
        this.mDefaultPhone.registerForServiceStateChanged(this, 60, null);
        this.mImsPhoneEx = (IOppoImsPhone) OppoTelephonyFactory.getInstance().getFeature(IOppoImsPhone.DEFAULT, this);
    }

    protected ImsPhone(String name, Context context, PhoneNotifier notifier, boolean unitTestMode) {
        super(name, context, notifier, unitTestMode);
        this.mPendingMMIs = new ArrayList<>();
        this.mSS = new ServiceState();
        this.mSilentRedialRegistrants = new RegistrantList();
        this.mImsRegistered = false;
        this.mRoaming = false;
        this.mSsnRegistrants = new RegistrantList();
        this.mExitEcmRunnable = new Runnable() {
            /* class com.android.internal.telephony.imsphone.ImsPhone.AnonymousClass1 */

            public void run() {
                ImsPhone.this.exitEmergencyCallbackMode();
            }
        };
        this.mImsEcbmStateListener = new ImsEcbmStateListener() {
            /* class com.android.internal.telephony.imsphone.ImsPhone.AnonymousClass2 */

            public void onECBMEntered() {
                ImsPhone.this.logd("onECBMEntered");
                ImsPhone.this.handleEnterEmergencyCallbackMode();
            }

            public void onECBMExited() {
                ImsPhone.this.logd("onECBMExited");
                ImsPhone.this.handleExitEmergencyCallbackMode();
            }
        };
        this.mResultReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.imsphone.ImsPhone.AnonymousClass3 */

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
                    ((NotificationManager) ImsPhone.this.mContext.getSystemService("notification")).notify("wifi_calling", 1, new Notification.Builder(ImsPhone.this.mContext).setSmallIcon(17301642).setContentTitle(title).setContentText(messageNotification).setAutoCancel(true).setContentIntent(PendingIntent.getActivity(ImsPhone.this.mContext, 0, resultIntent, 134217728)).setStyle(new Notification.BigTextStyle().bigText(messageNotification)).setChannelId(NotificationChannelController.CHANNEL_ID_WFC).build());
                }
            }
        };
    }

    @Override // com.android.internal.telephony.imsphone.AbstractImsPhone, com.android.internal.telephony.Phone
    public void dispose() {
        logd("dispose");
        super.dispose();
        this.mPendingMMIs.clear();
        this.mExternalCallTracker.tearDown();
        this.mCT.unregisterPhoneStateListener(this.mExternalCallTracker);
        this.mCT.unregisterForVoiceCallEnded(this);
        this.mCT.dispose();
        Phone phone = this.mDefaultPhone;
        if (!(phone == null || phone.getServiceStateTracker() == null)) {
            for (int transport : this.mDefaultPhone.getTransportManager().getAvailableTransports()) {
                this.mDefaultPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(transport, this);
            }
            this.mDefaultPhone.unregisterForServiceStateChanged(this);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    @UnsupportedAppUsage
    public ServiceState getServiceState() {
        return this.mSS;
    }

    @UnsupportedAppUsage
    @VisibleForTesting
    public void setServiceState(int state) {
        boolean isVoiceRegStateChanged;
        synchronized (this) {
            isVoiceRegStateChanged = this.mSS.getVoiceRegState() != state;
            this.mSS.setVoiceRegState(state);
        }
        updateDataServiceState();
        if (isVoiceRegStateChanged && this.mDefaultPhone.getServiceStateTracker() != null) {
            this.mDefaultPhone.getServiceStateTracker().onImsServiceStateChanged();
        }
    }

    @Override // com.android.internal.telephony.Phone
    public CallTracker getCallTracker() {
        return this.mCT;
    }

    public ImsExternalCallTracker getExternalCallTracker() {
        return this.mExternalCallTracker;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public List<? extends ImsPhoneMmiCode> getPendingMmiCodes() {
        return this.mPendingMMIs;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void acceptCall(int videoState) throws CallStateException {
        this.mCT.acceptCall(videoState);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void rejectCall() throws CallStateException {
        this.mCT.rejectCall();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void switchHoldingAndActive() throws CallStateException {
        throw new UnsupportedOperationException("Use hold() and unhold() instead.");
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean canConference() {
        return this.mCT.canConference();
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    public boolean canDial() {
        try {
            this.mCT.checkForDialIssues();
            return true;
        } catch (CallStateException e) {
            return false;
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void conference() {
        this.mCT.conference();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void clearDisconnected() {
        this.mCT.clearDisconnected();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean canTransfer() {
        return this.mCT.canTransfer();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void explicitCallTransfer() {
        this.mCT.explicitCallTransfer();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    @UnsupportedAppUsage
    public ImsPhoneCall getForegroundCall() {
        return this.mCT.mForegroundCall;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    @UnsupportedAppUsage
    public ImsPhoneCall getBackgroundCall() {
        return this.mCT.mBackgroundCall;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    @UnsupportedAppUsage
    public ImsPhoneCall getRingingCall() {
        return this.mCT.mRingingCall;
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isImsAvailable() {
        return this.mCT.isImsServiceReady();
    }

    public void holdActiveCall() throws CallStateException {
        this.mCT.holdActiveCall();
    }

    public void unholdHeldCall() throws CallStateException {
        this.mCT.unholdHeldCall();
    }

    private boolean handleCallDeflectionIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        if (getRingingCall().getState() != Call.State.IDLE) {
            logd("MmiCode 0: rejectCall");
            try {
                this.mCT.rejectCall();
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "reject failed", e);
                notifySuppServiceFailed(PhoneInternalInterface.SuppService.REJECT);
            }
        } else if (getBackgroundCall().getState() != Call.State.IDLE) {
            logd("MmiCode 0: hangupWaitingOrBackground");
            try {
                this.mCT.hangup(getBackgroundCall());
            } catch (CallStateException e2) {
                Rlog.d(LOG_TAG, "hangup failed", e2);
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void sendUssdResponse(String ussdRequest, CharSequence message, int returnCode, ResultReceiver wrappedCallback) {
        UssdResponse response = new UssdResponse(ussdRequest, message);
        Bundle returnData = new Bundle();
        returnData.putParcelable("USSD_RESPONSE", response);
        wrappedCallback.send(returnCode, returnData);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean handleUssdRequest(String ussdRequest, ResultReceiver wrappedCallback) throws CallStateException {
        if (this.mPendingMMIs.size() > 0) {
            logi("handleUssdRequest: queue full: " + Rlog.pii(LOG_TAG, ussdRequest));
            sendUssdResponse(ussdRequest, null, -1, wrappedCallback);
            return true;
        }
        try {
            dialInternal(ussdRequest, new ImsDialArgs.Builder().build(), wrappedCallback);
        } catch (CallStateException cse) {
            if (!Phone.CS_FALLBACK.equals(cse.getMessage())) {
                Rlog.w(LOG_TAG, "Could not execute USSD " + cse);
                sendUssdResponse(ussdRequest, null, -1, wrappedCallback);
            } else {
                throw cse;
            }
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
                logd("not support 1X SEND");
                notifySuppServiceFailed(PhoneInternalInterface.SuppService.HANGUP);
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "hangup failed", e);
                notifySuppServiceFailed(PhoneInternalInterface.SuppService.HANGUP);
            }
        } else if (call.getState() != Call.State.IDLE) {
            logd("MmiCode 1: hangup foreground");
            this.mCT.hangup(call);
        } else {
            logd("MmiCode 1: holdActiveCallForWaitingCall_p");
            this.mCT.holdActiveCallForWaitingCall_p();
        }
        return true;
    }

    private boolean handleCallHoldIncallSupplementaryService(String dialString) {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        if (len > 1) {
            logd("separate not supported");
            notifySuppServiceFailed(PhoneInternalInterface.SuppService.SEPARATE);
        } else {
            try {
                if (getRingingCall().getState() != Call.State.IDLE) {
                    logd("MmiCode 2: accept ringing call");
                    this.mCT.acceptCall(0);
                } else if (getBackgroundCall().getState() == Call.State.HOLDING) {
                    if (getForegroundCall().getState() != Call.State.IDLE) {
                        logd("MmiCode 2: switch holding and active");
                        this.mCT.holdActiveCall();
                    } else {
                        logd("MmiCode 2: unhold held call");
                        this.mCT.unholdHeldCall();
                    }
                } else if (getForegroundCall().getState() != Call.State.IDLE) {
                    logd("MmiCode 2: hold active call");
                    this.mCT.holdActiveCall();
                }
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "switch failed", e);
                notifySuppServiceFailed(PhoneInternalInterface.SuppService.SWITCH);
            }
        }
        return true;
    }

    private boolean handleMultipartyIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        logd("MmiCode 3: merge calls");
        conference();
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleEctIncallSupplementaryService(String dialString) {
        if (dialString.length() != 1) {
            return false;
        }
        logd("MmiCode 4: not support explicit call transfer");
        notifySuppServiceFailed(PhoneInternalInterface.SuppService.TRANSFER);
        return true;
    }

    private boolean handleCcbsIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        logi("MmiCode 5: CCBS not supported!");
        notifySuppServiceFailed(PhoneInternalInterface.SuppService.UNKNOWN);
        return true;
    }

    public void notifySuppSvcNotification(SuppServiceNotification suppSvc) {
        logd("notifySuppSvcNotification: suppSvc = " + suppSvc);
        this.mSsnRegistrants.notifyRegistrants(new AsyncResult((Object) null, suppSvc, (Throwable) null));
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    @UnsupportedAppUsage
    public boolean handleInCallMmiCommands(String dialString) {
        if (!isInCall() || TextUtils.isEmpty(dialString)) {
            return false;
        }
        switch (dialString.charAt(0)) {
            case '0':
                return handleCallDeflectionIncallSupplementaryService(dialString);
            case '1':
                return handleCallWaitingIncallSupplementaryService(dialString);
            case '2':
                return handleCallHoldIncallSupplementaryService(dialString);
            case '3':
                return handleMultipartyIncallSupplementaryService(dialString);
            case '4':
                return handleEctIncallSupplementaryService(dialString);
            case '5':
                return handleCcbsIncallSupplementaryService(dialString);
            default:
                return false;
        }
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneBase
    public boolean isInCall() {
        return getForegroundCall().getState().isAlive() || getBackgroundCall().getState().isAlive() || getRingingCall().getState().isAlive();
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isInEcm() {
        return this.mDefaultPhone.isInEcm();
    }

    @Override // com.android.internal.telephony.Phone
    public void setIsInEcm(boolean isInEcm) {
        this.mDefaultPhone.setIsInEcm(isInEcm);
    }

    public void notifyNewRingingConnection(com.android.internal.telephony.Connection c) {
        this.mDefaultPhone.notifyNewRingingConnectionP(c);
    }

    @UnsupportedAppUsage
    public void notifyUnknownConnection(com.android.internal.telephony.Connection c) {
        this.mDefaultPhone.notifyUnknownConnectionP(c);
    }

    @Override // com.android.internal.telephony.Phone
    public void notifyForVideoCapabilityChanged(boolean isVideoCapable) {
        this.mIsVideoCapable = isVideoCapable;
        this.mDefaultPhone.notifyForVideoCapabilityChanged(isVideoCapable);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public void setRadioPower(boolean on) {
        this.mDefaultPhone.setRadioPower(on);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public com.android.internal.telephony.Connection dial(String dialString, PhoneInternalInterface.DialArgs dialArgs) throws CallStateException {
        return dialInternal(dialString, dialArgs, null);
    }

    /* access modifiers changed from: protected */
    public com.android.internal.telephony.Connection dialInternal(String dialString, PhoneInternalInterface.DialArgs dialArgs, ResultReceiver wrappedCallback) throws CallStateException {
        ImsDialArgs.Builder imsDialArgsBuilder;
        String newDialString = PhoneNumberUtils.stripSeparators(dialString);
        if (handleInCallMmiCommands(newDialString)) {
            return null;
        }
        if (!(dialArgs instanceof ImsDialArgs)) {
            imsDialArgsBuilder = ImsDialArgs.Builder.from(dialArgs);
        } else {
            imsDialArgsBuilder = ImsDialArgs.Builder.from((ImsDialArgs) dialArgs);
        }
        imsDialArgsBuilder.setClirMode(this.mCT.getClirMode());
        if (this.mDefaultPhone.getPhoneType() == 2) {
            return this.mCT.dial(dialString, imsDialArgsBuilder.build());
        }
        ImsPhoneMmiCode mmi = ImsPhoneMmiCode.newFromDialString(PhoneNumberUtils.extractNetworkPortionAlt(newDialString), this, wrappedCallback);
        logd("dialInternal: dialing w/ mmi '" + mmi + "'...");
        if (mmi == null) {
            return this.mCT.dial(dialString, imsDialArgsBuilder.build());
        }
        if (mmi.isTemporaryModeCLIR()) {
            imsDialArgsBuilder.setClirMode(mmi.getCLIRMode());
            return this.mCT.dial(mmi.getDialingNumber(), imsDialArgsBuilder.build());
        } else if (mmi.isSupportedOverImsPhone()) {
            this.mPendingMMIs.add(mmi);
            this.mMmiRegistrants.notifyRegistrants(new AsyncResult((Object) null, mmi, (Throwable) null));
            try {
                mmi.processCode();
            } catch (CallStateException cse) {
                if (Phone.CS_FALLBACK.equals(cse.getMessage())) {
                    logi("dialInternal: fallback to GSM required.");
                    this.mPendingMMIs.remove(mmi);
                    throw cse;
                }
            }
            return null;
        } else {
            logi("dialInternal: USSD not supported by IMS; fallback to CS.");
            throw new CallStateException(Phone.CS_FALLBACK);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void sendDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            loge("sendDtmf called with invalid character '" + c + "'");
        } else if (this.mCT.getState() == PhoneConstants.State.OFFHOOK) {
            this.mCT.sendDtmf(c, null);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void startDtmf(char c) {
        if (PhoneNumberUtils.is12Key(c) || (c >= 'A' && c <= 'D')) {
            this.mCT.startDtmf(c);
            return;
        }
        loge("startDtmf called with invalid character '" + c + "'");
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void stopDtmf() {
        this.mCT.stopDtmf();
    }

    public void notifyIncomingRing() {
        logd("notifyIncomingRing");
        sendMessage(obtainMessage(14, new AsyncResult((Object) null, (Object) null, (Throwable) null)));
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setMute(boolean muted) {
        this.mCT.setMute(muted);
    }

    @Override // com.android.internal.telephony.Phone
    public void setTTYMode(int ttyMode, Message onComplete) {
        this.mCT.setTtyMode(ttyMode);
    }

    @Override // com.android.internal.telephony.Phone
    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        this.mCT.setUiTTYMode(uiTtyMode, onComplete);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean getMute() {
        return this.mCT.getMute();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.imsphone.ImsPhoneBase
    @UnsupportedAppUsage
    public PhoneConstants.State getState() {
        return this.mCT.getState();
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean isValidCommandInterfaceCFReason(int commandInterfaceCFReason) {
        if (commandInterfaceCFReason == 0 || commandInterfaceCFReason == 1 || commandInterfaceCFReason == 2 || commandInterfaceCFReason == 3 || commandInterfaceCFReason == 4 || commandInterfaceCFReason == 5) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean isValidCommandInterfaceCFAction(int commandInterfaceCFAction) {
        if (commandInterfaceCFAction == 0 || commandInterfaceCFAction == 1 || commandInterfaceCFAction == 3 || commandInterfaceCFAction == 4) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean isCfEnable(int action) {
        return action == 1 || action == 3;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public int getConditionFromCFReason(int reason) {
        if (reason == 0) {
            return 0;
        }
        if (reason == 1) {
            return 1;
        }
        if (reason == 2) {
            return 2;
        }
        if (reason == 3) {
            return 3;
        }
        if (reason != 4) {
            return reason != 5 ? -1 : 5;
        }
        return 4;
    }

    /* access modifiers changed from: protected */
    public int getCFReasonFromCondition(int condition) {
        if (condition == 0) {
            return 0;
        }
        if (condition == 1) {
            return 1;
        }
        if (condition == 2) {
            return 2;
        }
        if (condition == 3) {
            return 3;
        }
        if (condition != 4) {
            return condition != 5 ? 3 : 5;
        }
        return 4;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public int getActionFromCFAction(int action) {
        if (action == 0) {
            return 0;
        }
        if (action == 1) {
            return 1;
        }
        if (action == 3) {
            return 3;
        }
        if (action != 4) {
            return -1;
        }
        return 4;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public void getOutgoingCallerIdDisplay(Message onComplete) {
        logd("getCLIR");
        try {
            this.mCT.getUtInterface().queryCLIR(obtainMessage(58, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public void setOutgoingCallerIdDisplay(int clirMode, Message onComplete) {
        logd("setCLIR action= " + clirMode);
        try {
            this.mCT.getUtInterface().updateCLIR(clirMode, obtainMessage(57, clirMode, 0, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    @UnsupportedAppUsage
    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        logd("getCallForwardingOption reason=" + commandInterfaceCFReason);
        if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            logd("requesting call forwarding query.");
            try {
                this.mCT.getUtInterface().queryCallForward(getConditionFromCFReason(commandInterfaceCFReason), (String) null, obtainMessage(13, onComplete));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
        setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, 1, timerSeconds, onComplete);
    }

    @UnsupportedAppUsage
    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int serviceClass, int timerSeconds, Message onComplete) {
        logd("setCallForwardingOption action=" + commandInterfaceCFAction + ", reason=" + commandInterfaceCFReason + " serviceClass=" + serviceClass);
        String dialingNumber2 = super.handlePreCheckCFDialingNumber(dialingNumber);
        if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            try {
                this.mCT.getUtInterface().updateCallForward(getActionFromCFAction(commandInterfaceCFAction), getConditionFromCFReason(commandInterfaceCFReason), dialingNumber2, serviceClass, timerSeconds, obtainMessage(12, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new Cf(dialingNumber2, commandInterfaceCFReason == 0, onComplete, serviceClass)));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    @UnsupportedAppUsage
    public void getCallWaiting(Message onComplete) {
        logd("getCallWaiting");
        try {
            this.mCT.getUtInterface().queryCallWaiting(obtainMessage(56, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    @UnsupportedAppUsage
    public void setCallWaiting(boolean enable, Message onComplete) {
        int serviceClass = 1;
        PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId());
        if (b != null) {
            serviceClass = b.getInt("call_waiting_service_class_int", 1);
        }
        setCallWaiting(enable, serviceClass, onComplete);
    }

    public void setCallWaiting(boolean enable, int serviceClass, Message onComplete) {
        logd("setCallWaiting enable=" + enable);
        try {
            this.mCT.getUtInterface().updateCallWaiting(enable, serviceClass, obtainMessage(55, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    /* access modifiers changed from: protected */
    public int getCBTypeFromFacility(String facility) {
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
        getCallBarring(facility, onComplete, 0);
    }

    public void getCallBarring(String facility, Message onComplete, int serviceClass) {
        getCallBarring(facility, PhoneConfigurationManager.SSSS, onComplete, serviceClass);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public void getCallBarring(String facility, String password, Message onComplete, int serviceClass) {
        logd("getCallBarring facility=" + facility + ", serviceClass = " + serviceClass);
        try {
            this.mCT.getUtInterface().queryCallBarring(getCBTypeFromFacility(facility), obtainMessage(54, onComplete), serviceClass);
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete) {
        setCallBarring(facility, lockState, password, onComplete, 0);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete, int serviceClass) {
        int action;
        logd("setCallBarring facility=" + facility + ", lockState=" + lockState + ", serviceClass = " + serviceClass);
        Message resp = obtainMessage(53, onComplete);
        if (lockState) {
            action = 1;
        } else {
            action = 0;
        }
        try {
            this.mCT.getUtInterface().updateCallBarring(getCBTypeFromFacility(facility), action, resp, (String[]) null, serviceClass);
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public void sendUssdResponse(String ussdMessge) {
        logd("sendUssdResponse");
        ImsPhoneMmiCode mmi = ImsPhoneMmiCode.newFromUssdUserInput(ussdMessge, this);
        this.mPendingMMIs.add(mmi);
        this.mMmiRegistrants.notifyRegistrants(new AsyncResult((Object) null, mmi, (Throwable) null));
        mmi.sendUssd(ussdMessge);
    }

    public void sendUSSD(String ussdString, Message response) {
        this.mCT.sendUSSD(ussdString, response);
    }

    @Override // com.android.internal.telephony.Phone
    public void cancelUSSD(Message msg) {
        this.mCT.cancelUSSD(msg);
    }

    @UnsupportedAppUsage
    public void sendErrorResponse(Message onComplete) {
        logd("sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
            onComplete.sendToTarget();
        }
    }

    @UnsupportedAppUsage
    @VisibleForTesting
    public void sendErrorResponse(Message onComplete, Throwable e) {
        logd("sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, (Object) null, getCommandException(e));
            onComplete.sendToTarget();
        }
    }

    /* access modifiers changed from: protected */
    public CommandException getCommandException(int code, String errorString) {
        logd("getCommandException code= " + code + ", errorString= " + errorString);
        CommandException.Error error = CommandException.Error.GENERIC_FAILURE;
        if (code == 241) {
            error = CommandException.Error.FDN_CHECK_FAILURE;
        } else if (code == 801) {
            error = CommandException.Error.REQUEST_NOT_SUPPORTED;
        } else if (code != 802) {
            switch (code) {
                case 821:
                    error = CommandException.Error.PASSWORD_INCORRECT;
                    break;
                case 822:
                    error = CommandException.Error.SS_MODIFIED_TO_DIAL;
                    break;
                case 823:
                    error = CommandException.Error.SS_MODIFIED_TO_USSD;
                    break;
                case 824:
                    error = CommandException.Error.SS_MODIFIED_TO_SS;
                    break;
                case 825:
                    error = CommandException.Error.SS_MODIFIED_TO_DIAL_VIDEO;
                    break;
            }
        } else {
            error = CommandException.Error.RADIO_NOT_AVAILABLE;
        }
        return new CommandException(error, errorString);
    }

    /* access modifiers changed from: protected */
    public CommandException getCommandException(Throwable e) {
        if (e instanceof ImsException) {
            return getCommandException(((ImsException) e).getCode(), e.getMessage());
        }
        logd("getCommandException generic failure");
        return new CommandException(CommandException.Error.GENERIC_FAILURE);
    }

    /* access modifiers changed from: protected */
    public void onNetworkInitiatedUssd(ImsPhoneMmiCode mmi) {
        logd("onNetworkInitiatedUssd");
        this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult((Object) null, mmi, (Throwable) null));
    }

    /* access modifiers changed from: protected */
    public void onIncomingUSSD(int ussdMode, String ussdMessage) {
        logd("onIncomingUSSD ussdMode=" + ussdMode);
        boolean isUssdError = false;
        boolean isUssdRequest = ussdMode == 1;
        if (!(ussdMode == 0 || ussdMode == 1)) {
            isUssdError = true;
        }
        ImsPhoneMmiCode found = null;
        int i = 0;
        int s = this.mPendingMMIs.size();
        while (true) {
            if (i >= s) {
                break;
            } else if (this.mPendingMMIs.get(i).isPendingUSSD()) {
                found = this.mPendingMMIs.get(i);
                break;
            } else {
                i++;
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

    @UnsupportedAppUsage
    public void onMMIDone(ImsPhoneMmiCode mmi) {
        logd("onMMIDone: mmi=" + mmi);
        if (this.mPendingMMIs.remove(mmi) || mmi.isUssdRequest() || mmi.isSsInfo()) {
            ResultReceiver receiverCallback = mmi.getUssdCallbackReceiver();
            if (receiverCallback != null) {
                sendUssdResponse(mmi.getDialString(), mmi.getMessage(), mmi.getState() == MmiCode.State.COMPLETE ? 100 : -1, receiverCallback);
                return;
            }
            logv("onMMIDone: notifyRegistrants");
            this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult((Object) null, mmi, (Throwable) null));
        }
    }

    @Override // com.android.internal.telephony.Phone
    public ArrayList<com.android.internal.telephony.Connection> getHandoverConnection() {
        ArrayList<com.android.internal.telephony.Connection> connList = new ArrayList<>();
        connList.addAll(getForegroundCall().mConnections);
        connList.addAll(getBackgroundCall().mConnections);
        connList.addAll(getRingingCall().mConnections);
        if (connList.size() > 0) {
            return connList;
        }
        return null;
    }

    @Override // com.android.internal.telephony.Phone
    public void notifySrvccState(Call.SrvccState state) {
        this.mCT.notifySrvccState(state);
    }

    public void initiateSilentRedial() {
        this.mSilentRedialRegistrants.notifyRegistrants(new AsyncResult((Object) null, this.mLastDialString, (Throwable) null));
    }

    @Override // com.android.internal.telephony.Phone
    public void registerForSilentRedial(Handler h, int what, Object obj) {
        this.mSilentRedialRegistrants.addUnique(h, what, obj);
    }

    @Override // com.android.internal.telephony.Phone
    public void unregisterForSilentRedial(Handler h) {
        this.mSilentRedialRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public void registerForSuppServiceNotification(Handler h, int what, Object obj) {
        this.mSsnRegistrants.addUnique(h, what, obj);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.imsphone.ImsPhoneBase
    public void unregisterForSuppServiceNotification(Handler h) {
        this.mSsnRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.Phone
    public int getSubId() {
        return this.mDefaultPhone.getSubId();
    }

    @Override // com.android.internal.telephony.Phone
    public int getPhoneId() {
        return this.mDefaultPhone.getPhoneId();
    }

    /* access modifiers changed from: protected */
    public CallForwardInfo getCallForwardInfo(ImsCallForwardInfo info) {
        CallForwardInfo cfInfo = new CallForwardInfo();
        cfInfo.status = info.getStatus();
        cfInfo.reason = getCFReasonFromCondition(info.getCondition());
        cfInfo.serviceClass = 1;
        cfInfo.toa = info.getToA();
        cfInfo.number = info.getNumber();
        cfInfo.timeSeconds = info.getTimeSeconds();
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
                if (infos[i].getCondition() == 0 && r != null) {
                    setVoiceCallForwardingFlag(r, 1, infos[i].getStatus() == 1, infos[i].getNumber());
                }
                cfInfos[i] = getCallForwardInfo(infos[i]);
            }
        } else if (r != null) {
            setVoiceCallForwardingFlag(r, 1, false, null);
        }
        return cfInfos;
    }

    /* access modifiers changed from: protected */
    public int[] handleCbQueryResult(ImsSsInfo[] infos) {
        int[] cbInfos = {0};
        if (infos[0].getStatus() == 1) {
            cbInfos[0] = 1;
        }
        return cbInfos;
    }

    /* access modifiers changed from: protected */
    public int[] handleCwQueryResult(ImsSsInfo[] infos) {
        int[] cwInfos = new int[2];
        cwInfos[0] = 0;
        if (infos[0].getStatus() == 1) {
            cwInfos[0] = 1;
            cwInfos[1] = 1;
        }
        return cwInfos;
    }

    /* access modifiers changed from: protected */
    public void sendResponse(Message onComplete, Object result, Throwable e) {
        if (onComplete != null) {
            CommandException ex = null;
            if (e != null) {
                ex = getCommandException(e);
            }
            AsyncResult.forMessage(onComplete, result, ex);
            onComplete.sendToTarget();
        }
    }

    /* access modifiers changed from: protected */
    public void updateDataServiceState() {
        if (!(this.mSS == null || this.mDefaultPhone.getServiceStateTracker() == null || this.mDefaultPhone.getServiceStateTracker().mSS == null)) {
            ServiceState ss = this.mDefaultPhone.getServiceStateTracker().mSS;
            this.mSS.setDataRegState(ss.getDataRegState());
            for (NetworkRegistrationInfo nri : ss.getNetworkRegistrationInfoListForDomain(2)) {
                this.mSS.addNetworkRegistrationInfo(nri);
            }
            logd("updateDataServiceState: defSs = " + ss + " imsSs = " + this.mSS);
        }
    }

    /* JADX INFO: Multiple debug info for r1v19 java.lang.Object: [D('sst' com.android.internal.telephony.ServiceStateTracker), D('ar' android.os.AsyncResult)] */
    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // com.android.internal.telephony.Phone
    public void handleMessage(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        logd("handleMessage what=" + msg.what);
        int i = msg.what;
        if (i == 12) {
            IccRecords r = this.mDefaultPhone.getIccRecords();
            Cf cf = (Cf) ar.userObj;
            if (cf.mIsCfu && ar.exception == null && r != null) {
                setVoiceCallForwardingFlag(r, 1, msg.arg1 == 1, cf.mSetCfNumber);
            }
            sendResponse(cf.mOnComplete, null, ar.exception);
        } else if (i != 13) {
            switch (i) {
                case 53:
                case 55:
                    break;
                case 54:
                case 56:
                    int[] ssInfos = null;
                    if (ar.exception == null) {
                        if (msg.what == 54) {
                            ssInfos = handleCbQueryResult((ImsSsInfo[]) ar.result);
                        } else if (msg.what == 56) {
                            ssInfos = handleCwQueryResult((ImsSsInfo[]) ar.result);
                        }
                    }
                    sendResponse((Message) ar.userObj, ssInfos, ar.exception);
                    return;
                case 57:
                    if (ar.exception == null) {
                        saveClirSetting(msg.arg1);
                        break;
                    }
                    break;
                case 58:
                    Bundle ssInfo = (Bundle) ar.result;
                    int[] clirInfo = null;
                    if (ssInfo != null) {
                        clirInfo = ssInfo.getIntArray(ImsPhoneMmiCode.UT_BUNDLE_KEY_CLIR);
                    }
                    sendResponse((Message) ar.userObj, clirInfo, ar.exception);
                    return;
                case 59:
                    logd("EVENT_DEFAULT_PHONE_DATA_STATE_CHANGED");
                    updateDataServiceState();
                    return;
                case 60:
                    updateRoamingState((ServiceState) ((AsyncResult) msg.obj).result);
                    return;
                case 61:
                    logd("Voice call ended. Handle pending updateRoamingState.");
                    this.mCT.unregisterForVoiceCallEnded(this);
                    ServiceStateTracker sst = getDefaultPhone().getServiceStateTracker();
                    if (sst != null) {
                        updateRoamingState(sst.mSS);
                        return;
                    }
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
            sendResponse((Message) ar.userObj, null, ar.exception);
        } else {
            CallForwardInfo[] cfInfos = null;
            if (ar.exception == null) {
                cfInfos = handleCfQueryResult((ImsCallForwardInfo[]) ar.result);
            }
            sendResponse((Message) ar.userObj, cfInfos, ar.exception);
        }
    }

    @VisibleForTesting
    public ImsEcbmStateListener getImsEcbmStateListener() {
        return this.mImsEcbmStateListener;
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isInEmergencyCall() {
        return this.mCT.isInEmergencyCall();
    }

    /* access modifiers changed from: protected */
    public void sendEmergencyCallbackModeChange() {
        Intent intent = new Intent("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        intent.putExtra("phoneinECMState", isInEcm());
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, getPhoneId());
        ActivityManager.broadcastStickyIntent(intent, -1);
        logd("sendEmergencyCallbackModeChange: isInEcm=" + isInEcm());
    }

    @Override // com.android.internal.telephony.Phone
    public void exitEmergencyCallbackMode() {
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        logd("exitEmergencyCallbackMode()");
        try {
            this.mCT.getEcbmInterface().exitEmergencyCallbackMode();
        } catch (ImsException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void handleEnterEmergencyCallbackMode() {
        logd("handleEnterEmergencyCallbackMode,mIsPhoneInEcmState= " + isInEcm());
        if (!isInEcm()) {
            setIsInEcm(true);
            sendEmergencyCallbackModeChange();
            ((GsmCdmaPhone) this.mDefaultPhone).notifyEmergencyCallRegistrants(true);
            postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", 300000));
            this.mWakeLock.acquire();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.Phone
    @UnsupportedAppUsage
    public void handleExitEmergencyCallbackMode() {
        logd("handleExitEmergencyCallbackMode: mIsPhoneInEcmState = " + isInEcm());
        if (isInEcm()) {
            setIsInEcm(false);
        }
        removeCallbacks(this.mExitEcmRunnable);
        Registrant registrant = this.mEcmExitRespRegistrant;
        if (registrant != null) {
            registrant.notifyResult(Boolean.TRUE);
        }
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        sendEmergencyCallbackModeChange();
        ((GsmCdmaPhone) this.mDefaultPhone).notifyEmergencyCallRegistrants(false);
    }

    /* access modifiers changed from: package-private */
    public void handleTimerInEmergencyCallbackMode(int action) {
        if (action == 0) {
            postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", 300000));
            ((GsmCdmaPhone) this.mDefaultPhone).notifyEcbmTimerReset(Boolean.FALSE);
        } else if (action != 1) {
            loge("handleTimerInEmergencyCallbackMode, unsupported action " + action);
        } else {
            removeCallbacks(this.mExitEcmRunnable);
            ((GsmCdmaPhone) this.mDefaultPhone).notifyEcbmTimerReset(Boolean.TRUE);
        }
    }

    @Override // com.android.internal.telephony.Phone
    @UnsupportedAppUsage
    public void setOnEcbModeExitResponse(Handler h, int what, Object obj) {
        this.mEcmExitRespRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.Phone
    public void unsetOnEcbModeExitResponse(Handler h) {
        this.mEcmExitRespRegistrant.clear();
    }

    public void onFeatureCapabilityChanged() {
        this.mDefaultPhone.getServiceStateTracker().onImsCapabilityChanged();
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isImsCapabilityAvailable(int capability, int regTech) {
        return this.mCT.isImsCapabilityAvailable(capability, regTech);
    }

    @Override // com.android.internal.telephony.Phone
    @UnsupportedAppUsage
    public boolean isVolteEnabled() {
        return this.mCT.isVolteEnabled();
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isWifiCallingEnabled() {
        return this.mCT.isVowifiEnabled();
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isVideoEnabled() {
        return this.mCT.isVideoCallEnabled();
    }

    @Override // com.android.internal.telephony.Phone
    public int getImsRegistrationTech() {
        return this.mCT.getImsRegistrationTech();
    }

    @Override // com.android.internal.telephony.Phone
    public Phone getDefaultPhone() {
        return this.mDefaultPhone;
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isImsRegistered() {
        return this.mImsRegistered;
    }

    @UnsupportedAppUsage
    public void setImsRegistered(boolean value) {
        this.mImsRegistered = value;
    }

    @Override // com.android.internal.telephony.Phone
    public void callEndCleanupHandOverCallIfAny() {
        this.mCT.callEndCleanupHandOverCallIfAny();
    }

    public void processDisconnectReason(ImsReasonInfo imsReasonInfo) {
        if (imsReasonInfo.mCode == 1000 && imsReasonInfo.mExtraMessage != null && ImsManager.getInstance(this.mContext, this.mPhoneId).isWfcEnabledByUser()) {
            processWfcDisconnectForNotification(imsReasonInfo);
        }
    }

    private void processWfcDisconnectForNotification(ImsReasonInfo imsReasonInfo) {
        CarrierConfigManager configManager;
        String messageNotification;
        String messageNotification2;
        CarrierConfigManager configManager2 = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configManager2 == null) {
            loge("processDisconnectReason: CarrierConfigManager is not ready");
            return;
        }
        PersistableBundle pb = configManager2.getConfigForSubId(getSubId());
        if (pb == null) {
            loge("processDisconnectReason: no config for subId " + getSubId());
            return;
        }
        String[] wfcOperatorErrorCodes = pb.getStringArray("wfc_operator_error_codes_string_array");
        if (wfcOperatorErrorCodes != null) {
            String[] wfcOperatorErrorAlertMessages = this.mContext.getResources().getStringArray(17236131);
            String[] wfcOperatorErrorNotificationMessages = this.mContext.getResources().getStringArray(17236132);
            int i = 0;
            while (i < wfcOperatorErrorCodes.length) {
                String[] codes = wfcOperatorErrorCodes[i].split("\\|");
                if (codes.length != 2) {
                    loge("Invalid carrier config: " + wfcOperatorErrorCodes[i]);
                    configManager = configManager2;
                } else if (!imsReasonInfo.mExtraMessage.startsWith(codes[0])) {
                    configManager = configManager2;
                } else {
                    int codeStringLength = codes[0].length();
                    if (!Character.isLetterOrDigit(codes[0].charAt(codeStringLength - 1)) || imsReasonInfo.mExtraMessage.length() <= codeStringLength || !Character.isLetterOrDigit(imsReasonInfo.mExtraMessage.charAt(codeStringLength))) {
                        CharSequence title = this.mContext.getText(17041224);
                        int idx = -1;
                        try {
                            idx = Integer.parseInt(codes[1]);
                        } catch (NumberFormatException e) {
                            Rlog.d(LOG_TAG, e.toString());
                        } catch (Exception e2) {
                            Rlog.d(LOG_TAG, e2.toString());
                        }
                        if (idx < 0 || idx >= wfcOperatorErrorAlertMessages.length) {
                            configManager = configManager2;
                        } else if (idx >= wfcOperatorErrorNotificationMessages.length) {
                            configManager = configManager2;
                        } else {
                            String messageAlert = imsReasonInfo.mExtraMessage;
                            String messageNotification3 = imsReasonInfo.mExtraMessage;
                            if (!wfcOperatorErrorAlertMessages[idx].isEmpty()) {
                                messageNotification = messageNotification3;
                                messageAlert = String.format(wfcOperatorErrorAlertMessages[idx], imsReasonInfo.mExtraMessage);
                            } else {
                                messageNotification = messageNotification3;
                            }
                            if (!wfcOperatorErrorNotificationMessages[idx].isEmpty()) {
                                messageNotification2 = String.format(wfcOperatorErrorNotificationMessages[idx], imsReasonInfo.mExtraMessage);
                            } else {
                                messageNotification2 = messageNotification;
                            }
                            Intent intent = new Intent("com.android.ims.REGISTRATION_ERROR");
                            intent.putExtra(Phone.EXTRA_KEY_ALERT_TITLE, title);
                            intent.putExtra(Phone.EXTRA_KEY_ALERT_MESSAGE, messageAlert);
                            intent.putExtra(Phone.EXTRA_KEY_NOTIFICATION_MESSAGE, messageNotification2);
                            this.mContext.sendOrderedBroadcast(intent, null, this.mResultReceiver, null, -1, null, null);
                            return;
                        }
                        loge("Invalid index: " + wfcOperatorErrorCodes[i]);
                    } else {
                        configManager = configManager2;
                    }
                }
                i++;
                configManager2 = configManager;
            }
        }
    }

    @Override // com.android.internal.telephony.Phone
    @UnsupportedAppUsage
    public boolean isUtEnabled() {
        if (((IOppoCallManager) OppoTelephonyFactory.getInstance().getFeature(IOppoCallManager.DEFAULT, new Object[0])).isCtcCardCtaTest(this.mContext, this)) {
            return false;
        }
        return this.mCT.isUtEnabled();
    }

    @Override // com.android.internal.telephony.Phone
    public void sendEmergencyCallStateChange(boolean callActive) {
        this.mDefaultPhone.sendEmergencyCallStateChange(callActive);
    }

    @Override // com.android.internal.telephony.Phone
    public void setBroadcastEmergencyCallStateChanges(boolean broadcast) {
        this.mDefaultPhone.setBroadcastEmergencyCallStateChanges(broadcast);
    }

    @VisibleForTesting
    public PowerManager.WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    @Override // com.android.internal.telephony.Phone
    public NetworkStats getVtDataUsage(boolean perUidStats) {
        return this.mCT.getVtDataUsage(perUidStats);
    }

    /* access modifiers changed from: protected */
    public void updateRoamingState(ServiceState ss) {
        if (ss == null) {
            loge("updateRoamingState: null ServiceState!");
            return;
        }
        boolean newRoamingState = ss.getRoaming();
        if (this.mRoaming != newRoamingState) {
            if (!(ss.getVoiceRegState() == 0 || ss.getDataRegState() == 0)) {
                logi("updateRoamingState: we are OUT_OF_SERVICE, ignoring roaming change.");
            } else if (isCsNotInServiceAndPsWwanReportingWlan(ss)) {
                logi("updateRoamingState: IWLAN masking roaming, ignore roaming change.");
            } else if (this.mCT.getState() == PhoneConstants.State.IDLE) {
                logd("updateRoamingState now: " + newRoamingState);
                this.mRoaming = newRoamingState;
                ImsManager imsManager = ImsManager.getInstance(this.mContext, this.mPhoneId);
                imsManager.setWfcMode(imsManager.getWfcMode(newRoamingState), newRoamingState);
            } else {
                logd("updateRoamingState postponed: " + newRoamingState);
                this.mCT.registerForVoiceCallEnded(this, 61, null);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCsNotInServiceAndPsWwanReportingWlan(ServiceState ss) {
        TransportManager tm = this.mDefaultPhone.getTransportManager();
        if (tm == null || !tm.isInLegacyMode()) {
            return false;
        }
        NetworkRegistrationInfo csInfo = ss.getNetworkRegistrationInfo(1, 1);
        NetworkRegistrationInfo psInfo = ss.getNetworkRegistrationInfo(2, 1);
        if (psInfo == null || csInfo == null || csInfo.isInService() || psInfo.getAccessNetworkTechnology() != 18) {
            return false;
        }
        return true;
    }

    @Override // com.android.internal.telephony.Phone
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

    /* access modifiers changed from: protected */
    public void logi(String s) {
        Rlog.i(LOG_TAG, "[" + this.mPhoneId + "] " + s);
    }

    private void logv(String s) {
        Rlog.v(LOG_TAG, "[" + this.mPhoneId + "] " + s);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        Rlog.d(LOG_TAG, "[" + this.mPhoneId + "] " + s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e(LOG_TAG, "[" + this.mPhoneId + "] " + s);
    }
}
