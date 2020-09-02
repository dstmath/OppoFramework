package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Telephony;
import android.service.carrier.ICarrierMessagingCallback;
import android.service.carrier.ICarrierMessagingService;
import android.telephony.CarrierConfigManager;
import android.telephony.CarrierMessagingServiceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.EventLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.cdma.SmsMessage;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SMSDispatcher extends AbstractSMSDispatcher {
    static final boolean DBG = false;
    protected static final int EVENT_CONFIRM_SEND_TO_POSSIBLE_PREMIUM_SHORT_CODE = 8;
    protected static final int EVENT_CONFIRM_SEND_TO_PREMIUM_SHORT_CODE = 9;
    protected static final int EVENT_GET_IMS_SERVICE = 16;
    protected static final int EVENT_HANDLE_STATUS_REPORT = 10;
    protected static final int EVENT_ICC_CHANGED = 15;
    protected static final int EVENT_NEW_ICC_SMS = 14;
    static final int EVENT_SEND_CONFIRMED_SMS = 5;
    protected static final int EVENT_SEND_LIMIT_REACHED_CONFIRMATION = 4;
    private static final int EVENT_SEND_RETRY = 3;
    protected static final int EVENT_SEND_SMS_COMPLETE = 2;
    protected static final int EVENT_STOP_SENDING = 7;
    protected static final String MAP_KEY_DATA = "data";
    protected static final String MAP_KEY_DEST_ADDR = "destAddr";
    protected static final String MAP_KEY_DEST_PORT = "destPort";
    protected static final String MAP_KEY_PDU = "pdu";
    protected static final String MAP_KEY_SC_ADDR = "scAddr";
    protected static final String MAP_KEY_SMSC = "smsc";
    protected static final String MAP_KEY_TEXT = "text";
    protected static final int MAX_SEND_RETRIES = 3;
    public static final int MO_MSG_QUEUE_LIMIT = 5;
    protected static final int PREMIUM_RULE_USE_BOTH = 3;
    protected static final int PREMIUM_RULE_USE_NETWORK = 2;
    protected static final int PREMIUM_RULE_USE_SIM = 1;
    private static final String SEND_NEXT_MSG_EXTRA = "SendNextMsg";
    private static final int SEND_RETRY_DELAY = 2000;
    protected static final int SINGLE_PART_SMS = 1;
    static final String TAG = "SMSDispatcher";
    private static int sConcatenatedRef = new Random().nextInt(256);
    @UnsupportedAppUsage
    protected final ArrayList<SmsTracker> deliveryPendingList = new ArrayList<>();
    @UnsupportedAppUsage
    protected final CommandsInterface mCi;
    @UnsupportedAppUsage
    protected final Context mContext;
    private int mPendingTrackerCount;
    @UnsupportedAppUsage
    protected Phone mPhone;
    protected final AtomicInteger mPremiumSmsRule = new AtomicInteger(1);
    @UnsupportedAppUsage
    protected final ContentResolver mResolver;
    private final SettingsObserver mSettingsObserver;
    protected boolean mSmsCapable = true;
    protected SmsDispatchersController mSmsDispatchersController;
    protected boolean mSmsSendDisabled;
    @UnsupportedAppUsage
    protected final TelephonyManager mTelephonyManager;

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public abstract GsmAlphabet.TextEncodingDetails calculateLength(CharSequence charSequence, boolean z);

    /* access modifiers changed from: protected */
    public abstract String getFormat();

    /* access modifiers changed from: protected */
    public abstract SmsMessageBase.SubmitPduBase getSubmitPdu(String str, String str2, int i, byte[] bArr, boolean z);

    /* access modifiers changed from: protected */
    public abstract SmsMessageBase.SubmitPduBase getSubmitPdu(String str, String str2, String str3, boolean z, SmsHeader smsHeader, int i, int i2);

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public abstract void sendSms(SmsTracker smsTracker);

    /* access modifiers changed from: protected */
    public abstract boolean shouldBlockSmsForEcbm();

    @UnsupportedAppUsage
    protected static int getNextConcatenatedRef() {
        sConcatenatedRef++;
        return sConcatenatedRef;
    }

    protected SMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        super(phone);
        this.mPhone = phone;
        this.mSmsDispatchersController = smsDispatchersController;
        this.mContext = phone.getContext();
        this.mResolver = this.mContext.getContentResolver();
        this.mCi = phone.mCi;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mSettingsObserver = new SettingsObserver(this, this.mPremiumSmsRule, this.mContext);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("sms_short_code_rule"), false, this.mSettingsObserver);
        this.mSmsCapable = this.mContext.getResources().getBoolean(17891522);
        this.mSmsSendDisabled = !this.mTelephonyManager.getSmsSendCapableForPhone(this.mPhone.getPhoneId(), this.mSmsCapable);
        OppoRlog.Rlog.d(TAG, "SMSDispatcher: ctor mSmsCapable=" + this.mSmsCapable + " format=" + getFormat() + " mSmsSendDisabled=" + this.mSmsSendDisabled);
    }

    private static class SettingsObserver extends ContentObserver {
        private final Context mContext;
        private final AtomicInteger mPremiumSmsRule;

        SettingsObserver(Handler handler, AtomicInteger premiumSmsRule, Context context) {
            super(handler);
            this.mPremiumSmsRule = premiumSmsRule;
            this.mContext = context;
            onChange(false);
        }

        public void onChange(boolean selfChange) {
            this.mPremiumSmsRule.set(Settings.Global.getInt(this.mContext.getContentResolver(), "sms_short_code_rule", 1));
        }
    }

    @UnsupportedAppUsage
    public void dispose() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
    }

    /* access modifiers changed from: protected */
    public void handleStatusReport(Object o) {
        OppoRlog.Rlog.d(TAG, "handleStatusReport() called with no subclass.");
    }

    public void handleMessage(Message msg) {
        try {
            switch (msg.what) {
                case 2:
                    handleSendComplete((AsyncResult) msg.obj);
                    return;
                case 3:
                    OppoRlog.Rlog.d(TAG, "SMS retry..");
                    sendRetrySms((SmsTracker) msg.obj);
                    return;
                case 4:
                    handleReachSentLimit((SmsTracker) msg.obj);
                    return;
                case 5:
                    SmsTracker tracker = (SmsTracker) msg.obj;
                    if (tracker.isMultipart()) {
                        sendMultipartSms(tracker);
                    } else {
                        if (this.mPendingTrackerCount > 1) {
                            tracker.mExpectMore = true;
                        } else {
                            tracker.mExpectMore = false;
                        }
                        sendSms(tracker);
                    }
                    this.mPendingTrackerCount--;
                    return;
                case 6:
                default:
                    OppoRlog.Rlog.e(TAG, "handleMessage() ignoring message of unexpected type " + msg.what);
                    return;
                case 7:
                    SmsTracker tracker2 = (SmsTracker) msg.obj;
                    if (msg.arg1 == 0) {
                        if (msg.arg2 == 1) {
                            tracker2.onFailed(this.mContext, 8, 0);
                            OppoRlog.Rlog.d(TAG, "SMSDispatcher: EVENT_STOP_SENDING - sending SHORT_CODE_NEVER_ALLOWED error code.");
                        } else {
                            tracker2.onFailed(this.mContext, 7, 0);
                            OppoRlog.Rlog.d(TAG, "SMSDispatcher: EVENT_STOP_SENDING - sending SHORT_CODE_NOT_ALLOWED error code.");
                        }
                    } else if (msg.arg1 == 1) {
                        tracker2.onFailed(this.mContext, 5, 0);
                        OppoRlog.Rlog.d(TAG, "SMSDispatcher: EVENT_STOP_SENDING - sending LIMIT_EXCEEDED error code.");
                    } else {
                        OppoRlog.Rlog.e(TAG, "SMSDispatcher: EVENT_STOP_SENDING - unexpected cases.");
                    }
                    this.mPendingTrackerCount--;
                    return;
                case 8:
                    handleConfirmShortCode(false, (SmsTracker) msg.obj);
                    return;
                case 9:
                    handleConfirmShortCode(true, (SmsTracker) msg.obj);
                    return;
                case 10:
                    handleStatusReport(msg.obj);
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract class SmsSender extends CarrierMessagingServiceManager {
        protected volatile SmsSenderCallback mSenderCallback;
        protected final SmsTracker mTracker;

        protected SmsSender(SmsTracker tracker) {
            this.mTracker = tracker;
        }

        public void sendSmsByCarrierApp(String carrierPackageName, SmsSenderCallback senderCallback) {
            this.mSenderCallback = senderCallback;
            if (!bindToCarrierMessagingService(SMSDispatcher.this.mContext, carrierPackageName)) {
                OppoRlog.Rlog.e(SMSDispatcher.TAG, "bindService() for carrier messaging service failed");
                this.mSenderCallback.onSendSmsComplete(1, 0);
                return;
            }
            OppoRlog.Rlog.d(SMSDispatcher.TAG, "bindService() for carrier messaging service succeeded");
        }
    }

    /* access modifiers changed from: private */
    public static int getSendSmsFlag(PendingIntent deliveryIntent) {
        if (deliveryIntent == null) {
            return 0;
        }
        return 1;
    }

    protected final class TextSmsSender extends SmsSender {
        public TextSmsSender(SmsTracker tracker) {
            super(tracker);
        }

        /* access modifiers changed from: protected */
        @Override // android.telephony.CarrierMessagingServiceManager
        public void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            String text = (String) this.mTracker.getData().get(SMSDispatcher.MAP_KEY_TEXT);
            if (text != null) {
                try {
                    carrierMessagingService.sendTextSms(text, SMSDispatcher.this.getSubId(), this.mTracker.mDestAddress, SMSDispatcher.getSendSmsFlag(this.mTracker.mDeliveryIntent), this.mSenderCallback);
                } catch (RemoteException e) {
                    OppoRlog.Rlog.e(SMSDispatcher.TAG, "Exception sending the SMS: " + e);
                    this.mSenderCallback.onSendSmsComplete(1, 0);
                }
            } else {
                this.mSenderCallback.onSendSmsComplete(1, 0);
            }
        }
    }

    protected final class DataSmsSender extends SmsSender {
        public DataSmsSender(SmsTracker tracker) {
            super(tracker);
        }

        /* access modifiers changed from: protected */
        @Override // android.telephony.CarrierMessagingServiceManager
        public void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            HashMap<String, Object> map = this.mTracker.getData();
            byte[] data = (byte[]) map.get(SMSDispatcher.MAP_KEY_DATA);
            int destPort = ((Integer) map.get(SMSDispatcher.MAP_KEY_DEST_PORT)).intValue();
            if (data != null) {
                try {
                    carrierMessagingService.sendDataSms(data, SMSDispatcher.this.getSubId(), this.mTracker.mDestAddress, destPort, SMSDispatcher.getSendSmsFlag(this.mTracker.mDeliveryIntent), this.mSenderCallback);
                } catch (RemoteException e) {
                    OppoRlog.Rlog.e(SMSDispatcher.TAG, "Exception sending the SMS: " + e);
                    this.mSenderCallback.onSendSmsComplete(1, 0);
                }
            } else {
                this.mSenderCallback.onSendSmsComplete(1, 0);
            }
        }
    }

    protected final class SmsSenderCallback extends ICarrierMessagingCallback.Stub {
        private final SmsSender mSmsSender;

        public SmsSenderCallback(SmsSender smsSender) {
            this.mSmsSender = smsSender;
        }

        public void onSendSmsComplete(int result, int messageRef) {
            SMSDispatcher.this.checkCallerIsPhoneOrCarrierApp();
            long identity = Binder.clearCallingIdentity();
            try {
                this.mSmsSender.disposeConnection(SMSDispatcher.this.mContext);
                SMSDispatcher.this.processSendSmsResponse(this.mSmsSender.mTracker, result, messageRef);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void onSendMultipartSmsComplete(int result, int[] messageRefs) {
            OppoRlog.Rlog.e(SMSDispatcher.TAG, "Unexpected onSendMultipartSmsComplete call with result: " + result);
        }

        public void onFilterComplete(int result) {
            OppoRlog.Rlog.e(SMSDispatcher.TAG, "Unexpected onFilterComplete call with result: " + result);
        }

        public void onSendMmsComplete(int result, byte[] sendConfPdu) {
            OppoRlog.Rlog.e(SMSDispatcher.TAG, "Unexpected onSendMmsComplete call with result: " + result);
        }

        public void onDownloadMmsComplete(int result) {
            OppoRlog.Rlog.e(SMSDispatcher.TAG, "Unexpected onDownloadMmsComplete call with result: " + result);
        }
    }

    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public void processSendSmsResponse(SmsTracker tracker, int result, int messageRef) {
        if (tracker == null) {
            OppoRlog.Rlog.e(TAG, "processSendSmsResponse: null tracker");
            return;
        }
        SmsResponse smsResponse = new SmsResponse(messageRef, null, -1);
        if (result == 0) {
            OppoRlog.Rlog.d(TAG, "Sending SMS by IP succeeded.");
            sendMessage(obtainMessage(2, new AsyncResult(tracker, smsResponse, (Throwable) null)));
        } else if (result == 1) {
            OppoRlog.Rlog.d(TAG, "Sending SMS by IP failed. Retry on carrier network.");
            sendSubmitPdu(tracker);
        } else if (result != 2) {
            OppoRlog.Rlog.d(TAG, "Unknown result " + result + " Retry on carrier network.");
            sendSubmitPdu(tracker);
        } else {
            OppoRlog.Rlog.d(TAG, "Sending SMS by IP failed.");
            sendMessage(obtainMessage(2, new AsyncResult(tracker, smsResponse, new CommandException(CommandException.Error.GENERIC_FAILURE))));
        }
    }

    protected final class MultipartSmsSender extends CarrierMessagingServiceManager {
        private final List<String> mParts;
        private volatile MultipartSmsSenderCallback mSenderCallback;
        public final SmsTracker[] mTrackers;

        public MultipartSmsSender(ArrayList<String> parts, SmsTracker[] trackers) {
            this.mParts = parts;
            this.mTrackers = trackers;
        }

        @UnsupportedAppUsage
        public void sendSmsByCarrierApp(String carrierPackageName, MultipartSmsSenderCallback senderCallback) {
            this.mSenderCallback = senderCallback;
            if (!bindToCarrierMessagingService(SMSDispatcher.this.mContext, carrierPackageName)) {
                OppoRlog.Rlog.e(SMSDispatcher.TAG, "bindService() for carrier messaging service failed");
                this.mSenderCallback.onSendMultipartSmsComplete(1, null);
                return;
            }
            OppoRlog.Rlog.d(SMSDispatcher.TAG, "bindService() for carrier messaging service succeeded");
        }

        /* access modifiers changed from: protected */
        @Override // android.telephony.CarrierMessagingServiceManager
        public void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            try {
                carrierMessagingService.sendMultipartTextSms(this.mParts, SMSDispatcher.this.getSubId(), this.mTrackers[0].mDestAddress, SMSDispatcher.getSendSmsFlag(this.mTrackers[0].mDeliveryIntent), this.mSenderCallback);
            } catch (RemoteException e) {
                OppoRlog.Rlog.e(SMSDispatcher.TAG, "Exception sending the SMS: " + e);
                this.mSenderCallback.onSendMultipartSmsComplete(1, null);
            }
        }
    }

    protected final class MultipartSmsSenderCallback extends ICarrierMessagingCallback.Stub {
        private final MultipartSmsSender mSmsSender;

        public MultipartSmsSenderCallback(MultipartSmsSender smsSender) {
            this.mSmsSender = smsSender;
        }

        public void onSendSmsComplete(int result, int messageRef) {
            OppoRlog.Rlog.e(SMSDispatcher.TAG, "Unexpected onSendSmsComplete call with result: " + result);
        }

        public void onSendMultipartSmsComplete(int result, int[] messageRefs) {
            this.mSmsSender.disposeConnection(SMSDispatcher.this.mContext);
            if (this.mSmsSender.mTrackers == null) {
                OppoRlog.Rlog.e(SMSDispatcher.TAG, "Unexpected onSendMultipartSmsComplete call with null trackers.");
                return;
            }
            SMSDispatcher.this.checkCallerIsPhoneOrCarrierApp();
            long identity = Binder.clearCallingIdentity();
            int i = 0;
            while (i < this.mSmsSender.mTrackers.length) {
                try {
                    int messageRef = 0;
                    if (messageRefs != null && messageRefs.length > i) {
                        messageRef = messageRefs[i];
                    }
                    SMSDispatcher.this.processSendSmsResponse(this.mSmsSender.mTrackers[i], result, messageRef);
                    i++;
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void onFilterComplete(int result) {
            OppoRlog.Rlog.e(SMSDispatcher.TAG, "Unexpected onFilterComplete call with result: " + result);
        }

        public void onSendMmsComplete(int result, byte[] sendConfPdu) {
            OppoRlog.Rlog.e(SMSDispatcher.TAG, "Unexpected onSendMmsComplete call with result: " + result);
        }

        public void onDownloadMmsComplete(int result) {
            OppoRlog.Rlog.e(SMSDispatcher.TAG, "Unexpected onDownloadMmsComplete call with result: " + result);
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void sendSubmitPdu(SmsTracker tracker) {
        if (shouldBlockSmsForEcbm()) {
            OppoRlog.Rlog.d(TAG, "Block SMS in Emergency Callback mode");
            tracker.onFailed(this.mContext, 4, 0);
            return;
        }
        sendRawPdu(tracker);
    }

    /* access modifiers changed from: protected */
    public void handleSendComplete(AsyncResult ar) {
        SmsTracker tracker = (SmsTracker) ar.userObj;
        PendingIntent pendingIntent = tracker.mSentIntent;
        if (ar.result != null) {
            tracker.mMessageRef = ((SmsResponse) ar.result).mMessageRef;
        } else {
            OppoRlog.Rlog.d(TAG, "SmsResponse was null");
        }
        if (ar.exception == null) {
            if (tracker.mDeliveryIntent != null) {
                this.deliveryPendingList.add(tracker);
            }
            tracker.onSent(this.mContext);
            this.mPhone.notifySmsSent(tracker.mDestAddress);
            oemMoSmsCount(tracker);
            return;
        }
        int ss = this.mPhone.getServiceState().getState();
        if (tracker.mImsRetry > 0 && ss != 0) {
            tracker.mRetryCount = 3;
            OppoRlog.Rlog.d(TAG, "handleSendComplete: Skipping retry:  isIms()=" + isIms() + " mRetryCount=" + tracker.mRetryCount + " mImsRetry=" + tracker.mImsRetry + " mMessageRef=" + tracker.mMessageRef + " SS= " + this.mPhone.getServiceState().getState());
        }
        if (!isIms() && ss != 0) {
            tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
        } else if (((CommandException) ar.exception).getCommandError() != CommandException.Error.SMS_FAIL_RETRY || tracker.mRetryCount >= 3) {
            int errorCode = 0;
            if (ar.result != null) {
                errorCode = ((SmsResponse) ar.result).mErrorCode;
            }
            int error = 1;
            if (((CommandException) ar.exception).getCommandError() == CommandException.Error.FDN_CHECK_FAILURE) {
                error = 6;
            }
            tracker.onFailed(this.mContext, error, errorCode);
        } else {
            tracker.mRetryCount++;
            sendMessageDelayed(obtainMessage(3, tracker), 2000);
        }
    }

    protected static void handleNotInService(int ss, PendingIntent sentIntent) {
        if (sentIntent == null) {
            return;
        }
        if (ss == 3) {
            try {
                sentIntent.send(2);
            } catch (PendingIntent.CanceledException e) {
                OppoRlog.Rlog.e(TAG, "Failed to send result");
            }
        } else {
            sentIntent.send(4);
        }
    }

    protected static int getNotInServiceError(int ss) {
        if (ss == 3) {
            return 2;
        }
        return 4;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void sendData(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean isForVvm) {
        SmsMessageBase.SubmitPduBase pdu = onSendData(destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(callingPackage, getSmsTrackerMap(destAddr, scAddr, destPort, data, pdu), sentIntent, deliveryIntent, getFormat(), null, false, null, false, true, isForVvm);
            if (!sendSmsByCarrierApp(true, tracker)) {
                sendSubmitPdu(tracker);
            }
            return;
        }
        OppoRlog.Rlog.e(TAG, "SMSDispatcher.sendData(): getSubmitPdu() returned null");
        triggerSentIntentForFailure(sentIntent);
    }

    @Override // com.android.internal.telephony.AbstractSMSDispatcher
    public void sendText(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, boolean isForVvm) {
        OppoRlog.Rlog.d(TAG, "sendText");
        SmsMessageBase.SubmitPduBase pdu = onSendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(callingPkg, getSmsTrackerMap(destAddr, scAddr, text, pdu), sentIntent, deliveryIntent, getFormat(), messageUri, expectMore, text, true, persistMessage, priority, validityPeriod, isForVvm);
            if (!sendSmsByCarrierApp(false, tracker)) {
                sendSubmitPdu(tracker);
            }
            return;
        }
        OppoRlog.Rlog.e(TAG, "SmsDispatcher.sendText(): getSubmitPdu() returned null");
        triggerSentIntentForFailure(sentIntent);
    }

    private void triggerSentIntentForFailure(PendingIntent sentIntent) {
        if (sentIntent != null) {
            try {
                sentIntent.send(1);
            } catch (PendingIntent.CanceledException e) {
                OppoRlog.Rlog.e(TAG, "Intent has been canceled!");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void triggerSentIntentForFailure(List<PendingIntent> sentIntents) {
        if (sentIntents != null) {
            for (PendingIntent sentIntent : sentIntents) {
                triggerSentIntentForFailure(sentIntent);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean sendSmsByCarrierApp(boolean isDataSms, SmsTracker tracker) {
        SmsSender smsSender;
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage == null) {
            return false;
        }
        OppoRlog.Rlog.d(TAG, "Found carrier package.");
        if (isDataSms) {
            smsSender = new DataSmsSender(tracker);
        } else {
            smsSender = new TextSmsSender(tracker);
        }
        smsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(smsSender));
        return true;
    }

    /* JADX DEBUG: Additional 1 move instruction added to help type inference */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r7v4 */
    /* JADX WARN: Type inference failed for: r7v5, types: [int] */
    @Override // com.android.internal.telephony.AbstractSMSDispatcher
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PROTECTED)
    public void sendMultipartText(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        PendingIntent sentIntent;
        PendingIntent deliveryIntent;
        ArrayList<PendingIntent> arrayList = sentIntents;
        String fullMessageText = getMultipartMessageText(parts);
        int refNumber = getNextConcatenatedRef() & 255;
        int msgCount = parts.size();
        if (msgCount < 1) {
            triggerSentIntentForFailure(arrayList);
            return;
        }
        GsmAlphabet.TextEncodingDetails[] encodingForParts = new GsmAlphabet.TextEncodingDetails[msgCount];
        int msgCount2 = msgCount;
        int refNumber2 = refNumber;
        int encoding = onSendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingForParts);
        SmsTracker[] trackers = new SmsTracker[msgCount2];
        AtomicInteger unsentPartCount = new AtomicInteger(msgCount2);
        boolean z = false;
        AtomicBoolean anyPartFailed = new AtomicBoolean(false);
        int i = 0;
        while (i < msgCount2) {
            SmsHeader.ConcatRef concatRef = new SmsHeader.ConcatRef();
            concatRef.refNumber = refNumber2;
            concatRef.seqNumber = i + 1;
            concatRef.msgCount = msgCount2;
            concatRef.isEightBits = true;
            SmsHeader smsHeader = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName()).makeSmsHeader();
            smsHeader.concatRef = concatRef;
            if (encoding == 1) {
                smsHeader.languageTable = encodingForParts[i].languageTable;
                smsHeader.languageShiftTable = encodingForParts[i].languageShiftTable;
            }
            if (arrayList == null || sentIntents.size() <= i) {
                sentIntent = null;
            } else {
                sentIntent = arrayList.get(i);
            }
            if (deliveryIntents == null || deliveryIntents.size() <= i) {
                deliveryIntent = null;
            } else {
                deliveryIntent = deliveryIntents.get(i);
            }
            trackers[i] = getNewSubmitPduTracker(callingPkg, destAddr, scAddr, parts.get(i), smsHeader, encoding, sentIntent, deliveryIntent, i == msgCount2 + -1 ? true : z, unsentPartCount, anyPartFailed, messageUri, fullMessageText, priority, expectMore, validityPeriod);
            if (trackers[i] == null) {
                triggerSentIntentForFailure(sentIntents);
                return;
            }
            trackers[i].mPersistMessage = persistMessage;
            i++;
            trackers = trackers;
            arrayList = sentIntents;
            refNumber2 = refNumber2;
            encoding = encoding;
            z = z;
            msgCount2 = msgCount2;
        }
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage != null) {
            OppoRlog.Rlog.d(TAG, "Found carrier package.");
            MultipartSmsSender smsSender = new MultipartSmsSender(parts, trackers);
            smsSender.sendSmsByCarrierApp(carrierPackage, new MultipartSmsSenderCallback(smsSender));
            return;
        }
        OppoRlog.Rlog.v(TAG, "No carrier package.");
        int length = trackers.length;
        for (int i2 = z; i2 < length; i2++) {
            sendSubmitPdu(trackers[i2]);
        }
    }

    /* access modifiers changed from: protected */
    public SmsTracker getNewSubmitPduTracker(String callingPackage, String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int encoding, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText, int priority, boolean expectMore, int validityPeriod) {
        if (isCdmaMo()) {
            UserData uData = new UserData();
            uData.payloadStr = message;
            uData.userDataHeader = smsHeader;
            if (encoding == 1) {
                uData.msgEncoding = isAscii7bitSupportedForLongMessage() ? 2 : 9;
                OppoRlog.Rlog.d(TAG, "Message encoding for proper 7 bit: " + uData.msgEncoding);
            } else {
                uData.msgEncoding = 4;
            }
            uData.msgEncodingSet = true;
            SmsMessageBase.SubmitPduBase submitPdu = SmsMessage.getSubmitPdu(destinationAddress, uData, deliveryIntent != null && lastPart, priority);
            if (submitPdu != null) {
                return getSmsTracker(callingPackage, getSmsTrackerMap(destinationAddress, scAddress, message, submitPdu), sentIntent, deliveryIntent, getFormat(), unsentPartCount, anyPartFailed, messageUri, smsHeader, !lastPart || expectMore, fullMessageText, true, true, priority, validityPeriod, false);
            }
            OppoRlog.Rlog.e(TAG, "CdmaSMSDispatcher.getNewSubmitPduTracker(): getSubmitPdu() returned null");
            return null;
        }
        SmsMessageBase.SubmitPduBase pdu = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, deliveryIntent != null, SmsHeader.toByteArray(smsHeader), encoding, smsHeader.languageTable, smsHeader.languageShiftTable, validityPeriod);
        if (pdu != null) {
            return getSmsTracker(callingPackage, getSmsTrackerMap(destinationAddress, scAddress, message, pdu), sentIntent, deliveryIntent, getFormat(), unsentPartCount, anyPartFailed, messageUri, smsHeader, !lastPart || expectMore, fullMessageText, true, false, priority, validityPeriod, false);
        }
        OppoRlog.Rlog.e(TAG, "GsmSMSDispatcher.getNewSubmitPduTracker(): getSubmitPdu() returned null");
        return null;
    }

    @VisibleForTesting
    public void sendRawPdu(SmsTracker tracker) {
        byte[] pdu = (byte[]) tracker.getData().get(MAP_KEY_PDU);
        if (this.mSmsSendDisabled) {
            OppoRlog.Rlog.e(TAG, "Device does not support sending sms.");
            tracker.onFailed(this.mContext, 4, 0);
        } else if (pdu == null) {
            OppoRlog.Rlog.e(TAG, "Empty PDU");
            tracker.onFailed(this.mContext, 3, 0);
        } else {
            String packageName = tracker.getAppPackageName();
            PackageManager pm = this.mContext.getPackageManager();
            if (packageName == null) {
                String[] packageNames = pm.getPackagesForUid(Binder.getCallingUid());
                if (packageNames == null || packageNames.length == 0) {
                    OppoRlog.Rlog.e(TAG, "Can't get calling app package name: refusing to send SMS");
                    tracker.onFailed(this.mContext, 1, 0);
                    return;
                }
                packageName = packageNames[0];
            }
            try {
                PackageInfo appInfo = pm.getPackageInfoAsUser(packageName, 64, tracker.mUserId);
                if (checkDestination(tracker)) {
                    Phone phone = this.mPhone;
                    if (!(phone == null || phone.getServiceStateTracker() == null)) {
                        this.mPhone.getServiceStateTracker().oppoAddSmsSendCount();
                    }
                    if (!this.mSmsDispatchersController.getUsageMonitor().check(appInfo.packageName, 1)) {
                        sendMessage(obtainMessage(4, tracker));
                        return;
                    }
                    sendSms(tracker);
                }
                if (PhoneNumberUtils.isLocalEmergencyNumber(this.mContext, tracker.mDestAddress)) {
                    new AsyncEmergencyContactNotifier(this.mContext).execute(new Void[0]);
                }
            } catch (PackageManager.NameNotFoundException e) {
                OppoRlog.Rlog.e(TAG, "Can't get calling app package info: refusing to send SMS");
                tracker.onFailed(this.mContext, 1, 0);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkDestination(SmsTracker tracker) {
        return true;
    }

    private boolean denyIfQueueLimitReached(SmsTracker tracker) {
        int i = this.mPendingTrackerCount;
        if (i >= 5) {
            OppoRlog.Rlog.e(TAG, "Denied because queue limit reached");
            tracker.onFailed(this.mContext, 5, 0);
            return true;
        }
        this.mPendingTrackerCount = i + 1;
        return false;
    }

    private CharSequence getAppLabel(String appPackage, int userId) {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            return pm.getApplicationInfoAsUser(appPackage, 0, userId).loadSafeLabel(pm, 500.0f, 5);
        } catch (PackageManager.NameNotFoundException e) {
            OppoRlog.Rlog.e(TAG, "PackageManager Name Not Found for package " + appPackage);
            return appPackage;
        }
    }

    /* access modifiers changed from: protected */
    public void handleReachSentLimit(SmsTracker tracker) {
        if (!denyIfQueueLimitReached(tracker)) {
            CharSequence appLabel = getAppLabel(tracker.getAppPackageName(), tracker.mUserId);
            Resources r = Resources.getSystem();
            Spanned messageText = Html.fromHtml(r.getString(17041042, appLabel));
            ConfirmDialogListener listener = new ConfirmDialogListener(tracker, null, 1);
            AlertDialog d = new AlertDialog.Builder(this.mContext).setTitle(17041044).setIcon(17301642).setMessage(messageText).setPositiveButton(r.getString(17041045), listener).setNegativeButton(r.getString(17041043), listener).setOnCancelListener(listener).create();
            d.getWindow().setType(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_MIP_FA_MOBILE_NODE_AUTHENTICATION_FAILURE);
            d.show();
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void handleConfirmShortCode(boolean isPremium, SmsTracker tracker) {
        int detailsId;
        if (!denyIfQueueLimitReached(tracker)) {
            if (isPremium) {
                detailsId = 17041047;
            } else {
                detailsId = 17041053;
            }
            CharSequence appLabel = getAppLabel(tracker.getAppPackageName(), tracker.mUserId);
            Resources r = Resources.getSystem();
            Spanned messageText = Html.fromHtml(r.getString(17041051, appLabel, tracker.mDestAddress));
            View layout = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(17367301, (ViewGroup) null);
            ConfirmDialogListener listener = new ConfirmDialogListener(tracker, (TextView) layout.findViewById(16909380), 0);
            ((TextView) layout.findViewById(16909375)).setText(messageText);
            ((TextView) ((ViewGroup) layout.findViewById(16909376)).findViewById(16909377)).setText(detailsId);
            ((CheckBox) layout.findViewById(16909378)).setOnCheckedChangeListener(listener);
            AlertDialog d = new AlertDialog.Builder(this.mContext).setView(layout).setPositiveButton(r.getString(17041048), listener).setNegativeButton(r.getString(17041050), listener).setOnCancelListener(listener).create();
            d.getWindow().setType(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_MIP_FA_MOBILE_NODE_AUTHENTICATION_FAILURE);
            d.show();
            listener.setPositiveButton(d.getButton(-1));
            listener.setNegativeButton(d.getButton(-2));
        }
    }

    public void sendRetrySms(SmsTracker tracker) {
        SmsDispatchersController smsDispatchersController = this.mSmsDispatchersController;
        if (smsDispatchersController != null) {
            smsDispatchersController.sendRetrySms(tracker);
            return;
        }
        OppoRlog.Rlog.e(TAG, this.mSmsDispatchersController + " is null. Retry failed");
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void sendMultipartSms(SmsTracker tracker) {
        HashMap<String, Object> map = tracker.getData();
        String destinationAddress = (String) map.get("destination");
        String scAddress = (String) map.get("scaddress");
        ArrayList<String> parts = (ArrayList) map.get("parts");
        ArrayList<PendingIntent> sentIntents = (ArrayList) map.get("sentIntents");
        ArrayList<PendingIntent> deliveryIntents = (ArrayList) map.get("deliveryIntents");
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0) {
            sendMultipartText(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, null, null, tracker.mPersistMessage, tracker.mPriority, tracker.mExpectMore, tracker.mValidityPeriod);
            return;
        }
        int count = parts.size();
        for (int i = 0; i < count; i++) {
            PendingIntent sentIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                sentIntent = sentIntents.get(i);
            }
            handleNotInService(ss, sentIntent);
        }
    }

    public static class SmsTracker {
        protected static String MSG_REF_NUM = "msg_ref_num";
        protected static String PDU_SIZE = "pdu_size";
        private AtomicBoolean mAnyPartFailed;
        @UnsupportedAppUsage
        public final PackageInfo mAppInfo;
        @UnsupportedAppUsage
        public final HashMap<String, Object> mData;
        @UnsupportedAppUsage
        public final PendingIntent mDeliveryIntent;
        @UnsupportedAppUsage
        public final String mDestAddress;
        public boolean mExpectMore;
        public String mFormat;
        public String mFullMessageText;
        public int mImsRetry;
        public final boolean mIsForVvm;
        private boolean mIsText;
        @UnsupportedAppUsage
        public int mMessageRef;
        @UnsupportedAppUsage
        public Uri mMessageUri;
        public int mOemStayImsRetryCount = 0;
        @UnsupportedAppUsage
        public boolean mPersistMessage;
        public int mPriority;
        public int mRetryCount;
        @UnsupportedAppUsage
        public final PendingIntent mSentIntent;
        public final SmsHeader mSmsHeader;
        public int mSubId;
        @UnsupportedAppUsage
        private long mTimestamp = System.currentTimeMillis();
        /* access modifiers changed from: private */
        public AtomicInteger mUnsentPartCount;
        public final int mUserId;
        public boolean mUsesImsServiceForIms;
        public int mValidityPeriod;

        public SmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, PackageInfo appInfo, String destAddr, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean expectMore, String fullMessageText, int subId, boolean isText, boolean persistMessage, int userId, int priority, int validityPeriod, boolean isForVvm) {
            this.mData = data;
            this.mSentIntent = sentIntent;
            this.mDeliveryIntent = deliveryIntent;
            this.mRetryCount = 0;
            this.mAppInfo = appInfo;
            this.mDestAddress = destAddr;
            this.mFormat = format;
            this.mExpectMore = expectMore;
            this.mImsRetry = 0;
            this.mUsesImsServiceForIms = false;
            this.mMessageRef = 0;
            this.mUnsentPartCount = unsentPartCount;
            this.mAnyPartFailed = anyPartFailed;
            this.mMessageUri = messageUri;
            this.mSmsHeader = smsHeader;
            this.mFullMessageText = fullMessageText;
            this.mSubId = subId;
            this.mIsText = isText;
            this.mPersistMessage = persistMessage;
            this.mUserId = userId;
            this.mPriority = priority;
            this.mValidityPeriod = validityPeriod;
            this.mIsForVvm = isForVvm;
        }

        /* access modifiers changed from: package-private */
        @UnsupportedAppUsage
        public boolean isMultipart() {
            return this.mData.containsKey("parts");
        }

        public HashMap<String, Object> getData() {
            return this.mData;
        }

        public String getAppPackageName() {
            PackageInfo packageInfo = this.mAppInfo;
            if (packageInfo != null) {
                return packageInfo.packageName;
            }
            return null;
        }

        @UnsupportedAppUsage
        public void updateSentMessageStatus(Context context, int status) {
            if (this.mMessageUri != null) {
                ContentValues values = new ContentValues(1);
                values.put("status", Integer.valueOf(status));
                SqliteWrapper.update(context, context.getContentResolver(), this.mMessageUri, values, (String) null, (String[]) null);
            }
        }

        private void updateMessageState(Context context, int messageType, int errorCode) {
            if (this.mMessageUri != null) {
                ContentValues values = new ContentValues(2);
                values.put("type", Integer.valueOf(messageType));
                values.put("error_code", Integer.valueOf(errorCode));
                long identity = Binder.clearCallingIdentity();
                try {
                    if (SqliteWrapper.update(context, context.getContentResolver(), this.mMessageUri, values, (String) null, (String[]) null) != 1) {
                        OppoRlog.Rlog.e(SMSDispatcher.TAG, "Failed to move message to " + messageType);
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Integer):void}
         arg types: [java.lang.String, int]
         candidates:
          ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Byte):void}
          ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Float):void}
          ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.String):void}
          ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Long):void}
          ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Boolean):void}
          ClspMth{android.content.ContentValues.put(java.lang.String, byte[]):void}
          ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Double):void}
          ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Short):void}
          ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Integer):void} */
        private Uri persistSentMessageIfRequired(Context context, int messageType, int errorCode) {
            if (this.mIsText && this.mPersistMessage) {
                PackageInfo packageInfo = this.mAppInfo;
                if (SmsApplication.shouldWriteMessageForPackage(packageInfo != null ? packageInfo.packageName : null, context)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Persist SMS into ");
                    sb.append(messageType == 5 ? "FAILED" : "SENT");
                    OppoRlog.Rlog.d(SMSDispatcher.TAG, sb.toString());
                    ContentValues values = new ContentValues();
                    values.put("sub_id", Integer.valueOf(this.mSubId));
                    values.put("address", this.mDestAddress);
                    values.put("body", this.mFullMessageText);
                    values.put("date", Long.valueOf(System.currentTimeMillis()));
                    values.put("seen", (Integer) 1);
                    values.put("read", (Integer) 1);
                    PackageInfo packageInfo2 = this.mAppInfo;
                    String creator = packageInfo2 != null ? packageInfo2.packageName : null;
                    if (!TextUtils.isEmpty(creator)) {
                        values.put("creator", creator);
                    }
                    if (this.mDeliveryIntent != null) {
                        values.put("status", (Integer) 32);
                    }
                    if (errorCode != 0) {
                        values.put("error_code", Integer.valueOf(errorCode));
                    }
                    long identity = Binder.clearCallingIdentity();
                    ContentResolver resolver = context.getContentResolver();
                    try {
                        Uri uri = resolver.insert(Telephony.Sms.Sent.CONTENT_URI, values);
                        if (uri != null && messageType == 5) {
                            ContentValues updateValues = new ContentValues(1);
                            updateValues.put("type", (Integer) 5);
                            resolver.update(uri, updateValues, null, null);
                        }
                        return uri;
                    } catch (Exception e) {
                        OppoRlog.Rlog.e(SMSDispatcher.TAG, "writeOutboxMessage: Failed to persist outbox message", e);
                        return null;
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
            return null;
        }

        /* access modifiers changed from: private */
        public void persistOrUpdateMessage(Context context, int messageType, int errorCode) {
            if (this.mMessageUri != null) {
                updateMessageState(context, messageType, errorCode);
            } else {
                this.mMessageUri = persistSentMessageIfRequired(context, messageType, errorCode);
            }
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
         arg types: [java.lang.String, int]
         candidates:
          ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
        @UnsupportedAppUsage
        public void onFailed(Context context, int error, int errorCode) {
            AtomicBoolean atomicBoolean = this.mAnyPartFailed;
            if (atomicBoolean != null) {
                atomicBoolean.set(true);
            }
            boolean isSinglePartOrLastPart = true;
            AtomicInteger atomicInteger = this.mUnsentPartCount;
            if (atomicInteger != null) {
                isSinglePartOrLastPart = atomicInteger.decrementAndGet() == 0;
            }
            if (isSinglePartOrLastPart) {
                new AsyncPersistOrUpdateTask(context, 5, errorCode, error, true).execute(new Void[0]);
            } else if (this.mSentIntent != null) {
                try {
                    Intent fillIn = new Intent();
                    if (this.mMessageUri != null) {
                        fillIn.putExtra("uri", this.mMessageUri.toString());
                    }
                    if (errorCode != 0) {
                        fillIn.putExtra("errorCode", errorCode);
                    }
                    if (this.mUnsentPartCount != null && isSinglePartOrLastPart) {
                        fillIn.putExtra(SMSDispatcher.SEND_NEXT_MSG_EXTRA, true);
                    }
                    putPduSize(fillIn);
                    this.mSentIntent.send(context, error, fillIn);
                } catch (PendingIntent.CanceledException e) {
                    OppoRlog.Rlog.e(SMSDispatcher.TAG, "Failed to send result");
                }
            }
        }

        private void putPduSize(Intent fillIn) {
            int szPdu = 0;
            int smscLength = 0;
            int pduLength = 0;
            HashMap<String, Object> hashMap = this.mData;
            if (hashMap != null) {
                if (hashMap.get(SMSDispatcher.MAP_KEY_SMSC) != null) {
                    smscLength = ((byte[]) this.mData.get(SMSDispatcher.MAP_KEY_SMSC)).length;
                }
                if (this.mData.get(SMSDispatcher.MAP_KEY_PDU) != null) {
                    pduLength = ((byte[]) this.mData.get(SMSDispatcher.MAP_KEY_PDU)).length;
                }
                szPdu = smscLength + pduLength;
            }
            fillIn.putExtra(PDU_SIZE, szPdu);
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
         arg types: [java.lang.String, int]
         candidates:
          ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
          ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
        @UnsupportedAppUsage
        public void onSent(Context context) {
            boolean isSinglePartOrLastPart = true;
            AtomicInteger atomicInteger = this.mUnsentPartCount;
            if (atomicInteger != null) {
                isSinglePartOrLastPart = atomicInteger.decrementAndGet() == 0;
            }
            if (isSinglePartOrLastPart) {
                int messageType = 2;
                AtomicBoolean atomicBoolean = this.mAnyPartFailed;
                if (atomicBoolean != null && atomicBoolean.get()) {
                    messageType = 5;
                }
                new AsyncPersistOrUpdateTask(context, messageType, 0, 0, false).execute(new Void[0]);
            } else if (this.mSentIntent != null) {
                try {
                    Intent fillIn = new Intent();
                    if (this.mMessageUri != null) {
                        fillIn.putExtra("uri", this.mMessageUri.toString());
                    }
                    if (this.mUnsentPartCount != null && isSinglePartOrLastPart) {
                        fillIn.putExtra(SMSDispatcher.SEND_NEXT_MSG_EXTRA, true);
                    }
                    putPduSize(fillIn);
                    fillIn.putExtra(MSG_REF_NUM, this.mMessageRef);
                    OppoRlog.Rlog.d(SMSDispatcher.TAG, "message reference number : " + this.mMessageRef);
                    this.mSentIntent.send(context, -1, fillIn);
                } catch (PendingIntent.CanceledException e) {
                    OppoRlog.Rlog.e(SMSDispatcher.TAG, "Failed to send result");
                }
            }
        }

        class AsyncPersistOrUpdateTask extends AsyncTask<Void, Void, Void> {
            private final Context mContext;
            private int mError;
            private int mErrorCode;
            private boolean mFail;
            private int mMessageType;

            public AsyncPersistOrUpdateTask(Context context, int messageType, int errorCode, int error, boolean fail) {
                this.mContext = context;
                this.mMessageType = messageType;
                this.mErrorCode = errorCode;
                this.mError = error;
                this.mFail = fail;
            }

            /* access modifiers changed from: protected */
            public Void doInBackground(Void... params) {
                SmsTracker.this.persistOrUpdateMessage(this.mContext, this.mMessageType, this.mErrorCode);
                return null;
            }

            /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
             method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
             arg types: [java.lang.String, int]
             candidates:
              ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
            /* access modifiers changed from: protected */
            public void onPostExecute(Void result) {
                if (SmsTracker.this.mSentIntent != null) {
                    try {
                        Intent fillIn = new Intent();
                        if (SmsTracker.this.mMessageUri != null) {
                            fillIn.putExtra("uri", SmsTracker.this.mMessageUri.toString());
                        }
                        if (this.mFail && this.mErrorCode != 0) {
                            fillIn.putExtra("errorCode", this.mErrorCode);
                        }
                        if (SmsTracker.this.mUnsentPartCount != null) {
                            fillIn.putExtra(SMSDispatcher.SEND_NEXT_MSG_EXTRA, true);
                        }
                        int szPdu = 0;
                        int smscLength = 0;
                        int pduLength = 0;
                        if (SmsTracker.this.mData != null) {
                            if (SmsTracker.this.mData.get(SMSDispatcher.MAP_KEY_SMSC) != null) {
                                smscLength = ((byte[]) SmsTracker.this.mData.get(SMSDispatcher.MAP_KEY_SMSC)).length;
                            }
                            if (SmsTracker.this.mData.get(SMSDispatcher.MAP_KEY_PDU) != null) {
                                pduLength = ((byte[]) SmsTracker.this.mData.get(SMSDispatcher.MAP_KEY_PDU)).length;
                            }
                            szPdu = smscLength + pduLength;
                        }
                        fillIn.putExtra(SmsTracker.PDU_SIZE, szPdu);
                        if (!this.mFail) {
                            fillIn.putExtra(SmsTracker.MSG_REF_NUM, SmsTracker.this.mMessageRef);
                            OppoRlog.Rlog.d(SMSDispatcher.TAG, "message reference number : " + SmsTracker.this.mMessageRef);
                            SmsTracker.this.mSentIntent.send(this.mContext, -1, fillIn);
                            return;
                        }
                        SmsTracker.this.mSentIntent.send(this.mContext, this.mError, fillIn);
                    } catch (PendingIntent.CanceledException e) {
                        OppoRlog.Rlog.e(SMSDispatcher.TAG, "Failed to send result");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public SmsTracker getSmsTracker(String callingPackage, HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean expectMore, String fullMessageText, boolean isText, boolean persistMessage, int priority, int validityPeriod, boolean isForVvm) {
        PackageInfo appInfo;
        PackageManager pm = this.mContext.getPackageManager();
        int userId = UserHandle.getCallingUserId();
        try {
            appInfo = pm.getPackageInfoAsUser(callingPackage, 64, userId);
        } catch (PackageManager.NameNotFoundException e) {
            appInfo = null;
        }
        return new SmsTracker(data, sentIntent, deliveryIntent, appInfo, PhoneNumberUtils.extractNetworkPortion((String) data.get(MAP_KEY_DEST_ADDR)), format, unsentPartCount, anyPartFailed, messageUri, smsHeader, expectMore, fullMessageText, getSubId(), isText, persistMessage, userId, priority, validityPeriod, isForVvm);
    }

    /* access modifiers changed from: protected */
    public SmsTracker getSmsTracker(String callingPackage, HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, Uri messageUri, boolean expectMore, String fullMessageText, boolean isText, boolean persistMessage, boolean isForVvm) {
        return getSmsTracker(callingPackage, data, sentIntent, deliveryIntent, format, null, null, messageUri, null, expectMore, fullMessageText, isText, persistMessage, -1, -1, isForVvm);
    }

    /* access modifiers changed from: protected */
    public SmsTracker getSmsTracker(String callingPackage, HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, Uri messageUri, boolean expectMore, String fullMessageText, boolean isText, boolean persistMessage, int priority, int validityPeriod, boolean isForVvm) {
        return getSmsTracker(callingPackage, data, sentIntent, deliveryIntent, format, null, null, messageUri, null, expectMore, fullMessageText, isText, persistMessage, priority, validityPeriod, isForVvm);
    }

    /* access modifiers changed from: protected */
    public HashMap<String, Object> getSmsTrackerMap(String destAddr, String scAddr, String text, SmsMessageBase.SubmitPduBase pdu) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(MAP_KEY_DEST_ADDR, destAddr);
        map.put(MAP_KEY_SC_ADDR, scAddr);
        map.put(MAP_KEY_TEXT, text);
        map.put(MAP_KEY_SMSC, pdu.encodedScAddress);
        map.put(MAP_KEY_PDU, pdu.encodedMessage);
        return map;
    }

    /* access modifiers changed from: protected */
    public HashMap<String, Object> getSmsTrackerMap(String destAddr, String scAddr, int destPort, byte[] data, SmsMessageBase.SubmitPduBase pdu) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(MAP_KEY_DEST_ADDR, destAddr);
        map.put(MAP_KEY_SC_ADDR, scAddr);
        map.put(MAP_KEY_DEST_PORT, Integer.valueOf(destPort));
        map.put(MAP_KEY_DATA, data);
        map.put(MAP_KEY_SMSC, pdu.encodedScAddress);
        map.put(MAP_KEY_PDU, pdu.encodedMessage);
        return map;
    }

    private final class ConfirmDialogListener implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener, CompoundButton.OnCheckedChangeListener {
        private static final int NEVER_ALLOW = 1;
        private static final int RATE_LIMIT = 1;
        private static final int SHORT_CODE_MSG = 0;
        private int mConfirmationType;
        @UnsupportedAppUsage
        private Button mNegativeButton;
        @UnsupportedAppUsage
        private Button mPositiveButton;
        private boolean mRememberChoice;
        @UnsupportedAppUsage
        private final TextView mRememberUndoInstruction;
        private final SmsTracker mTracker;

        ConfirmDialogListener(SmsTracker tracker, TextView textView, int confirmationType) {
            this.mTracker = tracker;
            this.mRememberUndoInstruction = textView;
            this.mConfirmationType = confirmationType;
        }

        /* access modifiers changed from: package-private */
        public void setPositiveButton(Button button) {
            this.mPositiveButton = button;
        }

        /* access modifiers changed from: package-private */
        public void setNegativeButton(Button button) {
            this.mNegativeButton = button;
        }

        public void onClick(DialogInterface dialog, int which) {
            int newSmsPermission = 1;
            int i = -1;
            if (which == -1) {
                OppoRlog.Rlog.d(SMSDispatcher.TAG, "CONFIRM sending SMS");
                if (this.mTracker.mAppInfo.applicationInfo != null) {
                    i = this.mTracker.mAppInfo.applicationInfo.uid;
                }
                EventLog.writeEvent((int) EventLogTags.EXP_DET_SMS_SENT_BY_USER, i);
                SMSDispatcher sMSDispatcher = SMSDispatcher.this;
                sMSDispatcher.sendMessage(sMSDispatcher.obtainMessage(5, this.mTracker));
                if (this.mRememberChoice) {
                    newSmsPermission = 3;
                }
            } else if (which == -2) {
                OppoRlog.Rlog.d(SMSDispatcher.TAG, "DENY sending SMS");
                if (this.mTracker.mAppInfo.applicationInfo != null) {
                    i = this.mTracker.mAppInfo.applicationInfo.uid;
                }
                EventLog.writeEvent((int) EventLogTags.EXP_DET_SMS_DENIED_BY_USER, i);
                Message msg = SMSDispatcher.this.obtainMessage(7, this.mTracker);
                msg.arg1 = this.mConfirmationType;
                if (this.mRememberChoice) {
                    newSmsPermission = 2;
                    msg.arg2 = 1;
                }
                SMSDispatcher.this.sendMessage(msg);
            }
            SMSDispatcher.this.mSmsDispatchersController.setPremiumSmsPermission(this.mTracker.getAppPackageName(), newSmsPermission);
        }

        public void onCancel(DialogInterface dialog) {
            OppoRlog.Rlog.d(SMSDispatcher.TAG, "dialog dismissed: don't send SMS");
            Message msg = SMSDispatcher.this.obtainMessage(7, this.mTracker);
            msg.arg1 = this.mConfirmationType;
            SMSDispatcher.this.sendMessage(msg);
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            OppoRlog.Rlog.d(SMSDispatcher.TAG, "remember this choice: " + isChecked);
            this.mRememberChoice = isChecked;
            if (isChecked) {
                this.mPositiveButton.setText(17041049);
                this.mNegativeButton.setText(17041052);
                TextView textView = this.mRememberUndoInstruction;
                if (textView != null) {
                    textView.setText(17041055);
                    this.mRememberUndoInstruction.setPadding(0, 0, 0, 32);
                    return;
                }
                return;
            }
            this.mPositiveButton.setText(17041048);
            this.mNegativeButton.setText(17041050);
            TextView textView2 = this.mRememberUndoInstruction;
            if (textView2 != null) {
                textView2.setText(PhoneConfigurationManager.SSSS);
                this.mRememberUndoInstruction.setPadding(0, 0, 0, 0);
            }
        }
    }

    public boolean isIms() {
        SmsDispatchersController smsDispatchersController = this.mSmsDispatchersController;
        if (smsDispatchersController != null) {
            return smsDispatchersController.isIms();
        }
        OppoRlog.Rlog.e(TAG, "mSmsDispatchersController  is null");
        return false;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public String getMultipartMessageText(ArrayList<String> parts) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = parts.iterator();
        while (it.hasNext()) {
            String part = it.next();
            if (part != null) {
                sb.append(part);
            }
        }
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public String getCarrierAppPackageName() {
        UiccCard card = UiccController.getInstance().getUiccCard(this.mPhone.getPhoneId());
        if (card == null) {
            return null;
        }
        List<String> carrierPackages = card.getCarrierPackageNamesForIntent(this.mContext.getPackageManager(), new Intent("android.service.carrier.CarrierMessagingService"));
        if (carrierPackages == null || carrierPackages.size() != 1) {
            return CarrierSmsUtils.getCarrierImsPackageForIntent(this.mContext, this.mPhone, new Intent("android.service.carrier.CarrierMessagingService"));
        }
        return carrierPackages.get(0);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public int getSubId() {
        return SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mPhone.getPhoneId());
    }

    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public void checkCallerIsPhoneOrCarrierApp() {
        int uid = Binder.getCallingUid();
        if (UserHandle.getAppId(uid) != 1001 && uid != 0) {
            try {
                if (!UserHandle.isSameApp(this.mContext.getPackageManager().getApplicationInfo(getCarrierAppPackageName(), 0).uid, Binder.getCallingUid())) {
                    throw new SecurityException("Caller is not phone or carrier app!");
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new SecurityException("Caller is not phone or carrier app!");
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCdmaMo() {
        return this.mSmsDispatchersController.isCdmaMo();
    }

    /* access modifiers changed from: protected */
    public String getPackageNameViaProcessId(String[] packageNames) {
        if (packageNames == null || packageNames.length <= 0) {
            return null;
        }
        return packageNames[0];
    }

    /* access modifiers changed from: protected */
    public SmsMessageBase.SubmitPduBase onSendData(String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        return getSubmitPdu(scAddr, destAddr, destPort, data, deliveryIntent != null);
    }

    /* access modifiers changed from: protected */
    public SmsMessageBase.SubmitPduBase onSendText(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        return getSubmitPdu(scAddr, destAddr, text, deliveryIntent != null, null, priority, validityPeriod);
    }

    /* access modifiers changed from: protected */
    public int onSendMultipartText(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> arrayList, ArrayList<PendingIntent> arrayList2, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, GsmAlphabet.TextEncodingDetails[] encodingForParts) {
        int msgCount = parts.size();
        int encoding = 0;
        for (int i = 0; i < msgCount; i++) {
            GsmAlphabet.TextEncodingDetails details = calculateLength(parts.get(i), false);
            if (encoding != details.codeUnitSize && (encoding == 0 || encoding == 1)) {
                encoding = details.codeUnitSize;
            }
            encodingForParts[i] = details;
        }
        return encoding;
    }

    private boolean isAscii7bitSupportedForLongMessage() {
        long token = Binder.clearCallingIdentity();
        try {
            PersistableBundle pb = ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId());
            if (pb != null) {
                return pb.getBoolean("ascii_7_bit_support_for_long_message_bool");
            }
            Binder.restoreCallingIdentity(token);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}
