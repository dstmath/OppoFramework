package com.android.internal.telephony;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.OppoUsageManager;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Telephony.Sms.Sent;
import android.provider.oppo.CallLog.Calls;
import android.service.carrier.ICarrierMessagingCallback.Stub;
import android.service.carrier.ICarrierMessagingService;
import android.telephony.CarrierConfigManager;
import android.telephony.CarrierMessagingServiceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
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
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.SmsHeader.ConcatRef;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.SmsUsageMonitor.SmsAuthorizationCallback;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.NotificationChannelController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SMSDispatcher extends Handler {
    static final boolean DBG = false;
    private static final long DEFAULT_CROSSMAP_SMS_KEPP_PERIOD = 300000;
    private static final int EVENT_CONFIRM_SEND_TO_POSSIBLE_PREMIUM_SHORT_CODE = 8;
    private static final int EVENT_CONFIRM_SEND_TO_PREMIUM_SHORT_CODE = 9;
    protected static final int EVENT_HANDLE_STATUS_REPORT = 10;
    protected static final int EVENT_ICC_CHANGED = 15;
    protected static final int EVENT_IMS_STATE_CHANGED = 12;
    protected static final int EVENT_IMS_STATE_DONE = 13;
    protected static final int EVENT_NEW_ICC_SMS = 14;
    protected static final int EVENT_RADIO_ON = 11;
    static final int EVENT_SEND_CONFIRMED_SMS = 5;
    private static final int EVENT_SEND_LIMIT_REACHED_CONFIRMATION = 4;
    private static final int EVENT_SEND_RETRY = 3;
    protected static final int EVENT_SEND_SMS_COMPLETE = 2;
    static final int EVENT_STOP_SENDING = 7;
    private static final int MAX_SEND_RETRIES = 3;
    private static final int MO_MSG_QUEUE_LIMIT = 5;
    private static final int PREMIUM_RULE_USE_BOTH = 3;
    private static final int PREMIUM_RULE_USE_NETWORK = 2;
    private static final int PREMIUM_RULE_USE_SIM = 1;
    private static final String SEND_NEXT_MSG_EXTRA = "SendNextMsg";
    private static final int SEND_RETRY_DELAY = 2000;
    private static final int SINGLE_PART_SMS = 1;
    static final String TAG = "SMSDispatcher";
    private static int sConcatenatedRef = new Random().nextInt(256);
    protected final ArrayList<SmsTracker> deliveryPendingList = new ArrayList();
    protected final CommandsInterface mCi;
    protected final Context mContext;
    private ImsSMSDispatcher mImsSMSDispatcher;
    private OppoUsageManager mOppoUsageManager = null;
    private int mPendingTrackerCount;
    protected Phone mPhone;
    private final AtomicInteger mPremiumSmsRule = new AtomicInteger(1);
    protected final ContentResolver mResolver;
    private final SettingsObserver mSettingsObserver;
    protected boolean mSmsCapable = true;
    protected boolean mSmsSendDisabled;
    protected final TelephonyManager mTelephonyManager;
    private SmsUsageMonitor mUsageMonitor;

    private final class ConfirmDialogListener implements OnClickListener, OnCancelListener, OnCheckedChangeListener {
        private static final int NEVER_ALLOW = 1;
        private static final int RATE_LIMIT = 1;
        private static final int SHORT_CODE_MSG = 0;
        private int mConfirmationType;
        private Button mNegativeButton;
        private Button mPositiveButton;
        private boolean mRememberChoice;
        private final TextView mRememberUndoInstruction;
        private final SmsTracker mTracker;

        ConfirmDialogListener(SmsTracker tracker, TextView textView, int confirmationType) {
            this.mTracker = tracker;
            this.mRememberUndoInstruction = textView;
            this.mConfirmationType = confirmationType;
        }

        void setPositiveButton(Button button) {
            this.mPositiveButton = button;
        }

        void setNegativeButton(Button button) {
            this.mNegativeButton = button;
        }

        public void onClick(DialogInterface dialog, int which) {
            int i = -1;
            int newSmsPermission = 1;
            if (which == -1) {
                Rlog.d(SMSDispatcher.TAG, "CONFIRM sending SMS");
                if (this.mTracker.mAppInfo.applicationInfo != null) {
                    i = this.mTracker.mAppInfo.applicationInfo.uid;
                }
                EventLog.writeEvent(EventLogTags.EXP_DET_SMS_SENT_BY_USER, i);
                SMSDispatcher.this.sendMessage(SMSDispatcher.this.obtainMessage(5, this.mTracker));
                if (this.mRememberChoice) {
                    newSmsPermission = 3;
                }
            } else if (which == -2) {
                Rlog.d(SMSDispatcher.TAG, "DENY sending SMS");
                if (this.mTracker.mAppInfo.applicationInfo != null) {
                    i = this.mTracker.mAppInfo.applicationInfo.uid;
                }
                EventLog.writeEvent(EventLogTags.EXP_DET_SMS_DENIED_BY_USER, i);
                Message msg = SMSDispatcher.this.obtainMessage(7, this.mTracker);
                msg.arg1 = this.mConfirmationType;
                if (this.mRememberChoice) {
                    newSmsPermission = 2;
                    msg.arg2 = 1;
                }
                SMSDispatcher.this.sendMessage(msg);
            }
            SMSDispatcher.this.setPremiumSmsPermission(this.mTracker.getAppPackageName(), newSmsPermission);
        }

        public void onCancel(DialogInterface dialog) {
            Rlog.d(SMSDispatcher.TAG, "dialog dismissed: don't send SMS");
            Message msg = SMSDispatcher.this.obtainMessage(7, this.mTracker);
            msg.arg1 = this.mConfirmationType;
            SMSDispatcher.this.sendMessage(msg);
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Rlog.d(SMSDispatcher.TAG, "remember this choice: " + isChecked);
            this.mRememberChoice = isChecked;
            if (isChecked) {
                this.mPositiveButton.setText(17040884);
                this.mNegativeButton.setText(17040887);
                if (this.mRememberUndoInstruction != null) {
                    this.mRememberUndoInstruction.setText(17040890);
                    this.mRememberUndoInstruction.setPadding(0, 0, 0, 32);
                    return;
                }
                return;
            }
            this.mPositiveButton.setText(17040883);
            this.mNegativeButton.setText(17040885);
            if (this.mRememberUndoInstruction != null) {
                this.mRememberUndoInstruction.setText(SpnOverride.MVNO_TYPE_NONE);
                this.mRememberUndoInstruction.setPadding(0, 0, 0, 0);
            }
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
            if (bindToCarrierMessagingService(SMSDispatcher.this.mContext, carrierPackageName)) {
                Rlog.d(SMSDispatcher.TAG, "bindService() for carrier messaging service succeeded");
                return;
            }
            Rlog.e(SMSDispatcher.TAG, "bindService() for carrier messaging service failed");
            this.mSenderCallback.onSendSmsComplete(1, 0);
        }
    }

    protected final class DataSmsSender extends SmsSender {
        public DataSmsSender(SmsTracker tracker) {
            super(tracker);
        }

        protected void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            HashMap<String, Object> map = this.mTracker.getData();
            byte[] data = (byte[]) map.get("data");
            int destPort = ((Integer) map.get("destPort")).intValue();
            if (data != null) {
                try {
                    carrierMessagingService.sendDataSms(data, SMSDispatcher.this.getSubId(), this.mTracker.mDestAddress, destPort, SMSDispatcher.getSendSmsFlag(this.mTracker.mDeliveryIntent), this.mSenderCallback);
                    return;
                } catch (RemoteException e) {
                    Rlog.e(SMSDispatcher.TAG, "Exception sending the SMS: " + e);
                    this.mSenderCallback.onSendSmsComplete(1, 0);
                    return;
                }
            }
            this.mSenderCallback.onSendSmsComplete(1, 0);
        }
    }

    private final class MultipartSmsSender extends CarrierMessagingServiceManager {
        private final List<String> mParts;
        private volatile MultipartSmsSenderCallback mSenderCallback;
        public final SmsTracker[] mTrackers;

        MultipartSmsSender(ArrayList<String> parts, SmsTracker[] trackers) {
            this.mParts = parts;
            this.mTrackers = trackers;
        }

        void sendSmsByCarrierApp(String carrierPackageName, MultipartSmsSenderCallback senderCallback) {
            this.mSenderCallback = senderCallback;
            if (bindToCarrierMessagingService(SMSDispatcher.this.mContext, carrierPackageName)) {
                Rlog.d(SMSDispatcher.TAG, "bindService() for carrier messaging service succeeded");
                return;
            }
            Rlog.e(SMSDispatcher.TAG, "bindService() for carrier messaging service failed");
            this.mSenderCallback.onSendMultipartSmsComplete(1, null);
        }

        protected void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            try {
                carrierMessagingService.sendMultipartTextSms(this.mParts, SMSDispatcher.this.getSubId(), this.mTrackers[0].mDestAddress, SMSDispatcher.getSendSmsFlag(this.mTrackers[0].mDeliveryIntent), this.mSenderCallback);
            } catch (RemoteException e) {
                Rlog.e(SMSDispatcher.TAG, "Exception sending the SMS: " + e);
                this.mSenderCallback.onSendMultipartSmsComplete(1, null);
            }
        }
    }

    private final class MultipartSmsSenderCallback extends Stub {
        private final MultipartSmsSender mSmsSender;

        MultipartSmsSenderCallback(MultipartSmsSender smsSender) {
            this.mSmsSender = smsSender;
        }

        public void onSendSmsComplete(int result, int messageRef) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onSendSmsComplete call with result: " + result);
        }

        public void onSendMultipartSmsComplete(int result, int[] messageRefs) {
            this.mSmsSender.disposeConnection(SMSDispatcher.this.mContext);
            if (this.mSmsSender.mTrackers == null) {
                Rlog.e(SMSDispatcher.TAG, "Unexpected onSendMultipartSmsComplete call with null trackers.");
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
            Rlog.e(SMSDispatcher.TAG, "Unexpected onFilterComplete call with result: " + result);
        }

        public void onSendMmsComplete(int result, byte[] sendConfPdu) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onSendMmsComplete call with result: " + result);
        }

        public void onDownloadMmsComplete(int result) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onDownloadMmsComplete call with result: " + result);
        }
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
            this.mPremiumSmsRule.set(Global.getInt(this.mContext.getContentResolver(), "sms_short_code_rule", 1));
        }
    }

    protected final class SmsSenderCallback extends Stub {
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
            Rlog.e(SMSDispatcher.TAG, "Unexpected onSendMultipartSmsComplete call with result: " + result);
        }

        public void onFilterComplete(int result) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onFilterComplete call with result: " + result);
        }

        public void onSendMmsComplete(int result, byte[] sendConfPdu) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onSendMmsComplete call with result: " + result);
        }

        public void onDownloadMmsComplete(int result) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onDownloadMmsComplete call with result: " + result);
        }
    }

    public static class SmsTracker {
        private AtomicBoolean mAnyPartFailed;
        public final PackageInfo mAppInfo;
        private final HashMap<String, Object> mData;
        public final PendingIntent mDeliveryIntent;
        public final String mDestAddress;
        public boolean mExpectMore;
        String mFormat;
        private String mFullMessageText;
        public int mImsRetry;
        private boolean mIsText;
        public int mMessageRef;
        public Uri mMessageUri;
        private boolean mPersistMessage;
        public int mRetryCount;
        public final PendingIntent mSentIntent;
        public final SmsHeader mSmsHeader;
        private int mSubId;
        private long mTimestamp;
        private AtomicInteger mUnsentPartCount;
        private final int mUserId;
        public int mvalidityPeriod;

        /* synthetic */ SmsTracker(HashMap data, PendingIntent sentIntent, PendingIntent deliveryIntent, PackageInfo appInfo, String destAddr, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean isExpectMore, String fullMessageText, int subId, boolean isText, boolean persistMessage, int userId, int validityPeriod, SmsTracker -this17) {
            this(data, sentIntent, deliveryIntent, appInfo, destAddr, format, unsentPartCount, anyPartFailed, messageUri, smsHeader, isExpectMore, fullMessageText, subId, isText, persistMessage, userId, validityPeriod);
        }

        private SmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, PackageInfo appInfo, String destAddr, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean isExpectMore, String fullMessageText, int subId, boolean isText, boolean persistMessage, int userId, int validityPeriod) {
            this.mTimestamp = System.currentTimeMillis();
            this.mData = data;
            this.mSentIntent = sentIntent;
            this.mDeliveryIntent = deliveryIntent;
            this.mRetryCount = 0;
            this.mAppInfo = appInfo;
            this.mDestAddress = destAddr;
            this.mFormat = format;
            this.mExpectMore = isExpectMore;
            this.mImsRetry = 0;
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
            this.mvalidityPeriod = validityPeriod;
        }

        boolean isMultipart() {
            return this.mData.containsKey("parts");
        }

        public HashMap<String, Object> getData() {
            return this.mData;
        }

        public String getAppPackageName() {
            return this.mAppInfo != null ? this.mAppInfo.packageName : null;
        }

        public void updateSentMessageStatus(Context context, int status) {
            if (this.mMessageUri != null) {
                ContentValues values = new ContentValues(1);
                values.put("status", Integer.valueOf(status));
                SqliteWrapper.update(context, context.getContentResolver(), this.mMessageUri, values, null, null);
            }
        }

        private void updateMessageState(Context context, int messageType, int errorCode) {
            if (this.mMessageUri != null) {
                ContentValues values = new ContentValues(2);
                values.put(Calls.TYPE, Integer.valueOf(messageType));
                values.put("error_code", Integer.valueOf(errorCode));
                long identity = Binder.clearCallingIdentity();
                try {
                    if (SqliteWrapper.update(context, context.getContentResolver(), this.mMessageUri, values, null, null) != 1) {
                        Rlog.e(SMSDispatcher.TAG, "Failed to move message to " + messageType);
                    }
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        private Uri persistSentMessageIfRequired(Context context, int messageType, int errorCode) {
            if (!this.mIsText || (this.mPersistMessage ^ 1) != 0 || (SmsApplication.shouldWriteMessageForPackage(this.mAppInfo.packageName, context) ^ 1) != 0) {
                return null;
            }
            Rlog.d(SMSDispatcher.TAG, "Persist SMS into " + (messageType == 5 ? "FAILED" : "SENT"));
            if (this.mDestAddress != null && "10659401".equals(this.mDestAddress)) {
                char[] header = new char[]{2};
                if (this.mFullMessageText != null && this.mFullMessageText.startsWith(new String(header))) {
                    Rlog.d(SMSDispatcher.TAG, "ctsmsreg writeSentMessage check for sms reg = true");
                    return null;
                }
            }
            if (this.mAppInfo == null || !SMSDispatcher.noNeedWriteSmsToDbWhenSendSms(context, this.mAppInfo.packageName)) {
                ContentValues values = new ContentValues();
                values.put(Calls.SUB_ID, Integer.valueOf(this.mSubId));
                values.put("address", this.mDestAddress);
                values.put("body", this.mFullMessageText);
                values.put(Calls.DATE, Long.valueOf(System.currentTimeMillis()));
                values.put("seen", Integer.valueOf(1));
                values.put("read", Integer.valueOf(1));
                Object creator = this.mAppInfo != null ? this.mAppInfo.packageName : null;
                if (!TextUtils.isEmpty(creator)) {
                    values.put("creator", creator);
                }
                if (this.mDeliveryIntent != null) {
                    values.put("status", Integer.valueOf(32));
                }
                if (errorCode != 0) {
                    values.put("error_code", Integer.valueOf(errorCode));
                }
                long identity = Binder.clearCallingIdentity();
                ContentResolver resolver = context.getContentResolver();
                try {
                    Uri uri = resolver.insert(Sent.CONTENT_URI, values);
                    if (uri != null && messageType == 5) {
                        ContentValues updateValues = new ContentValues(1);
                        updateValues.put(Calls.TYPE, Integer.valueOf(5));
                        resolver.update(uri, updateValues, null, null);
                    }
                    Binder.restoreCallingIdentity(identity);
                    return uri;
                } catch (Exception e) {
                    Rlog.e(SMSDispatcher.TAG, "writeOutboxMessage: Failed to persist outbox message", e);
                    Binder.restoreCallingIdentity(identity);
                    return null;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            }
            Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "needWriteSmsToDbWhenSendSms--no");
            return null;
        }

        private void persistOrUpdateMessage(Context context, int messageType, int errorCode) {
            if (this.mMessageUri != null) {
                updateMessageState(context, messageType, errorCode);
            } else {
                this.mMessageUri = persistSentMessageIfRequired(context, messageType, errorCode);
            }
        }

        public void onFailed(Context context, int error, int errorCode) {
            if (this.mAnyPartFailed != null) {
                this.mAnyPartFailed.set(true);
            }
            boolean isSinglePartOrLastPart = true;
            if (this.mUnsentPartCount != null) {
                isSinglePartOrLastPart = this.mUnsentPartCount.decrementAndGet() == 0;
            }
            if (isSinglePartOrLastPart) {
                persistOrUpdateMessage(context, 5, errorCode);
            }
            if (this.mSentIntent != null) {
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
                    this.mSentIntent.send(context, error, fillIn);
                } catch (CanceledException e) {
                    Rlog.e(SMSDispatcher.TAG, "Failed to send result");
                }
            }
        }

        public void onSent(Context context) {
            boolean isSinglePartOrLastPart = true;
            if (this.mUnsentPartCount != null) {
                isSinglePartOrLastPart = this.mUnsentPartCount.decrementAndGet() == 0;
            }
            if (isSinglePartOrLastPart) {
                int messageType = 2;
                if (this.mAnyPartFailed != null && this.mAnyPartFailed.get()) {
                    messageType = 5;
                }
                persistOrUpdateMessage(context, messageType, 0);
            }
            if (this.mSentIntent != null) {
                try {
                    Intent fillIn = new Intent();
                    if (this.mMessageUri != null) {
                        fillIn.putExtra("uri", this.mMessageUri.toString());
                    }
                    if (this.mUnsentPartCount != null && isSinglePartOrLastPart) {
                        fillIn.putExtra(SMSDispatcher.SEND_NEXT_MSG_EXTRA, true);
                    }
                    this.mSentIntent.send(context, -1, fillIn);
                } catch (CanceledException e) {
                    Rlog.e(SMSDispatcher.TAG, "Failed to send result");
                }
            }
        }
    }

    protected final class TextSmsSender extends SmsSender {
        public TextSmsSender(SmsTracker tracker) {
            super(tracker);
        }

        protected void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            String text = (String) this.mTracker.getData().get("text");
            if (text != null) {
                try {
                    carrierMessagingService.sendTextSms(text, SMSDispatcher.this.getSubId(), this.mTracker.mDestAddress, SMSDispatcher.getSendSmsFlag(this.mTracker.mDeliveryIntent), this.mSenderCallback);
                    return;
                } catch (RemoteException e) {
                    Rlog.e(SMSDispatcher.TAG, "Exception sending the SMS: " + e);
                    this.mSenderCallback.onSendSmsComplete(1, 0);
                    return;
                }
            }
            this.mSenderCallback.onSendSmsComplete(1, 0);
        }
    }

    protected abstract TextEncodingDetails calculateLength(CharSequence charSequence, boolean z);

    protected abstract String getFormat();

    protected abstract SmsTracker getNewSubmitPduTracker(String str, String str2, String str3, SmsHeader smsHeader, int i, PendingIntent pendingIntent, PendingIntent pendingIntent2, boolean z, AtomicInteger atomicInteger, AtomicBoolean atomicBoolean, Uri uri, String str4, int i2, boolean z2, int i3, String str5);

    protected abstract void injectSmsPdu(byte[] bArr, String str, PendingIntent pendingIntent);

    protected abstract void sendData(String str, String str2, int i, byte[] bArr, PendingIntent pendingIntent, PendingIntent pendingIntent2, String str3);

    protected abstract void sendSms(SmsTracker smsTracker);

    protected abstract void sendSmsByPstn(SmsTracker smsTracker);

    protected abstract void sendSubmitPdu(SmsTracker smsTracker);

    protected abstract void sendText(String str, String str2, String str3, PendingIntent pendingIntent, PendingIntent pendingIntent2, Uri uri, String str4, boolean z, int i, boolean z2, int i2);

    protected abstract void sendTextOem(String str, String str2, String str3, PendingIntent pendingIntent, PendingIntent pendingIntent2, Uri uri, String str4, boolean z, int i, boolean z2, int i2, int i3);

    protected static int getNextConcatenatedRef() {
        sConcatenatedRef++;
        return sConcatenatedRef;
    }

    protected SMSDispatcher(Phone phone, SmsUsageMonitor usageMonitor, ImsSMSDispatcher imsSMSDispatcher) {
        this.mPhone = phone;
        this.mImsSMSDispatcher = imsSMSDispatcher;
        this.mContext = phone.getContext();
        this.mResolver = this.mContext.getContentResolver();
        this.mCi = phone.mCi;
        this.mUsageMonitor = usageMonitor;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mSettingsObserver = new SettingsObserver(this, this.mPremiumSmsRule, this.mContext);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("sms_short_code_rule"), false, this.mSettingsObserver);
        this.mSmsCapable = this.mContext.getResources().getBoolean(17957018);
        this.mSmsSendDisabled = this.mTelephonyManager.getSmsSendCapableForPhone(this.mPhone.getPhoneId(), this.mSmsCapable) ^ 1;
        Rlog.d(TAG, "SMSDispatcher: ctor mSmsCapable=" + this.mSmsCapable + " format=" + getFormat() + " mSmsSendDisabled=" + this.mSmsSendDisabled);
        this.mOppoUsageManager = OppoUsageManager.getOppoUsageManager();
    }

    protected void updatePhoneObject(Phone phone) {
        this.mPhone = phone;
        this.mUsageMonitor = phone.mSmsUsageMonitor;
        Rlog.d(TAG, "Active phone changed to " + this.mPhone.getPhoneName());
    }

    public void dispose() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
    }

    protected void handleStatusReport(Object o) {
        Rlog.d(TAG, "handleStatusReport() called with no subclass.");
    }

    public void handleMessage(Message msg) {
        try {
            SmsTracker tracker;
            switch (msg.what) {
                case 2:
                    handleSendComplete((AsyncResult) msg.obj);
                    return;
                case 3:
                    Rlog.d(TAG, "SMS retry..");
                    sendRetrySms((SmsTracker) msg.obj);
                    return;
                case 4:
                    handleReachSentLimit((SmsTracker) msg.obj);
                    return;
                case 5:
                    tracker = msg.obj;
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
                case 7:
                    tracker = (SmsTracker) msg.obj;
                    if (msg.arg1 == 0) {
                        if (msg.arg2 == 1) {
                            tracker.onFailed(this.mContext, 8, 0);
                            Rlog.d(TAG, "SMSDispatcher: EVENT_STOP_SENDING - sending SHORT_CODE_NEVER_ALLOWED error code.");
                        } else {
                            tracker.onFailed(this.mContext, 7, 0);
                            Rlog.d(TAG, "SMSDispatcher: EVENT_STOP_SENDING - sending SHORT_CODE_NOT_ALLOWED error code.");
                        }
                    } else if (msg.arg1 == 1) {
                        tracker.onFailed(this.mContext, 5, 0);
                        Rlog.d(TAG, "SMSDispatcher: EVENT_STOP_SENDING - sending LIMIT_EXCEEDED error code.");
                    } else {
                        Rlog.e(TAG, "SMSDispatcher: EVENT_STOP_SENDING - unexpected cases.");
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
                default:
                    Rlog.e(TAG, "handleMessage() ignoring message of unexpected type " + msg.what);
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        e.printStackTrace();
    }

    private static int getSendSmsFlag(PendingIntent deliveryIntent) {
        if (deliveryIntent == null) {
            return 0;
        }
        return 1;
    }

    private void processSendSmsResponse(SmsTracker tracker, int result, int messageRef) {
        if (tracker == null) {
            Rlog.e(TAG, "processSendSmsResponse: null tracker");
            return;
        }
        SmsResponse smsResponse = new SmsResponse(messageRef, null, -1);
        switch (result) {
            case 0:
                Rlog.d(TAG, "Sending SMS by IP succeeded.");
                sendMessage(obtainMessage(2, new AsyncResult(tracker, smsResponse, null)));
                break;
            case 1:
                Rlog.d(TAG, "Sending SMS by IP failed. Retry on carrier network.");
                sendSubmitPdu(tracker);
                break;
            case 2:
                Rlog.d(TAG, "Sending SMS by IP failed.");
                sendMessage(obtainMessage(2, new AsyncResult(tracker, smsResponse, new CommandException(Error.GENERIC_FAILURE))));
                break;
            default:
                Rlog.d(TAG, "Unknown result " + result + " Retry on carrier network.");
                sendSubmitPdu(tracker);
                break;
        }
    }

    protected void handleSendComplete(AsyncResult ar) {
        SmsTracker tracker = ar.userObj;
        PendingIntent sentIntent = tracker.mSentIntent;
        if (ar.result != null) {
            tracker.mMessageRef = ((SmsResponse) ar.result).mMessageRef;
        } else {
            Rlog.d(TAG, "SmsResponse was null");
        }
        if (ar.exception == null) {
            countSendSms(tracker);
            if (tracker.mDeliveryIntent != null) {
                this.deliveryPendingList.add(tracker);
            }
            tracker.onSent(this.mContext);
        } else {
            try {
                if (!isIms() && isNeedStopSms(tracker)) {
                    Rlog.d(TAG, "handleSendComplete-cdma--crossmap, stop tracker.");
                    tracker.onFailed(this.mContext, 1, 0);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            int ss = this.mPhone.getServiceState().getState();
            if (tracker.mImsRetry > 0 && ss != 0) {
                tracker.mRetryCount = 3;
                Rlog.d(TAG, "handleSendComplete: Skipping retry:  isIms()=" + isIms() + " mRetryCount=" + tracker.mRetryCount + " mImsRetry=" + tracker.mImsRetry + " mMessageRef=" + tracker.mMessageRef + " SS= " + this.mPhone.getServiceState().getState());
            }
            if (!isIms() && ss != 0) {
                tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
            } else if (((CommandException) ar.exception).getCommandError() == Error.SMS_FAIL_RETRY && tracker.mRetryCount < 3) {
                tracker.mRetryCount++;
                sendMessageDelayed(obtainMessage(3, tracker), 2000);
            } else if (tracker == null || tracker.mImsRetry <= 0 || tracker.mRetryCount != 0) {
                int errorCode = 0;
                if (ar.result != null) {
                    errorCode = ((SmsResponse) ar.result).mErrorCode;
                }
                int error = 1;
                if (((CommandException) ar.exception).getCommandError() == Error.FDN_CHECK_FAILURE) {
                    error = 6;
                }
                tracker.onFailed(this.mContext, error, errorCode);
            } else {
                Rlog.d(TAG, "oppo ims retry...");
                tracker.mRetryCount++;
                sendMessageDelayed(obtainMessage(3, tracker), 2000);
            }
        }
    }

    protected static void handleNotInService(int ss, PendingIntent sentIntent) {
        if (sentIntent == null) {
            return;
        }
        if (ss == 3) {
            try {
                sentIntent.send(2);
                return;
            } catch (CanceledException e) {
                return;
            }
        }
        sentIntent.send(4);
    }

    protected static int getNotInServiceError(int ss) {
        if (ss == 3) {
            return 2;
        }
        return 4;
    }

    protected void sendMultipartText(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean isExpectMore, int validityPeriod) {
        int i;
        String fullMessageText = getMultipartMessageText(parts);
        int refNumber = getNextConcatenatedRef() & 255;
        int msgCount = parts.size();
        int encoding = 0;
        TextEncodingDetails[] encodingForParts = new TextEncodingDetails[msgCount];
        for (i = 0; i < msgCount; i++) {
            TextEncodingDetails details = calculateLength((CharSequence) parts.get(i), false);
            if (encoding != details.codeUnitSize && (encoding == 0 || encoding == 1)) {
                encoding = details.codeUnitSize;
            }
            encodingForParts[i] = details;
        }
        Object trackers = new SmsTracker[msgCount];
        AtomicInteger unsentPartCount = new AtomicInteger(msgCount);
        AtomicBoolean anyPartFailed = new AtomicBoolean(false);
        i = 0;
        while (i < msgCount) {
            ConcatRef concatRef = new ConcatRef();
            concatRef.refNumber = refNumber;
            concatRef.seqNumber = i + 1;
            concatRef.msgCount = msgCount;
            concatRef.isEightBits = true;
            SmsHeader smsHeader = new SmsHeader();
            smsHeader.concatRef = concatRef;
            if (encoding == 1) {
                smsHeader.languageTable = encodingForParts[i].languageTable;
                smsHeader.languageShiftTable = encodingForParts[i].languageShiftTable;
            }
            PendingIntent pendingIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                pendingIntent = (PendingIntent) sentIntents.get(i);
            }
            PendingIntent pendingIntent2 = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
            }
            trackers[i] = getNewSubmitPduTracker(destAddr, scAddr, (String) parts.get(i), smsHeader, encoding, pendingIntent, pendingIntent2, i == msgCount + -1, unsentPartCount, anyPartFailed, messageUri, fullMessageText, priority, isExpectMore, validityPeriod, callingPkg);
            trackers[i].mPersistMessage = persistMessage;
            i++;
        }
        if (parts == null || trackers == null || trackers.length == 0 || trackers[0] == null) {
            Rlog.e(TAG, "Cannot send multipart text. parts=" + parts + " trackers=" + trackers);
            return;
        }
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage != null) {
            Rlog.d(TAG, "Found carrier package.");
            MultipartSmsSender multipartSmsSender = new MultipartSmsSender(parts, trackers);
            multipartSmsSender.sendSmsByCarrierApp(carrierPackage, new MultipartSmsSenderCallback(multipartSmsSender));
        } else {
            Rlog.v(TAG, "No carrier package.");
            for (SmsTracker tracker : trackers) {
                if (tracker != null) {
                    sendSubmitPdu(tracker);
                } else {
                    Rlog.e(TAG, "Null tracker.");
                }
            }
        }
    }

    protected void sendMultipartTextOem(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean isExpectMore, int validityPeriod, int encodingType) {
        Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "oem2,pri=" + priority + " isE=" + isExpectMore + " vP=" + validityPeriod + " eT=" + encodingType);
        if (parts == null) {
            Rlog.e(TAG, "Cannot send multipart text, parts == null");
            return;
        }
        int i;
        String fullMessageText = getMultipartMessageText(parts);
        int refNumber = getNextConcatenatedRef() & 255;
        int msgCount = parts.size();
        int encoding = encodingType;
        TextEncodingDetails[] encodingForParts = new TextEncodingDetails[msgCount];
        for (i = 0; i < msgCount; i++) {
            TextEncodingDetails details = calculateLength((CharSequence) parts.get(i), false);
            if (details == null) {
                Rlog.e(TAG, "Cannot send multipart text, details == null");
                return;
            }
            if (encodingType != details.codeUnitSize && (encodingType == 0 || encodingType == 1)) {
                Rlog.d(TAG, "[enc conflict between details[" + details.codeUnitSize + "] and encoding " + encodingType);
                details.codeUnitSize = encodingType;
            }
            encodingForParts[i] = details;
        }
        Object trackers = new SmsTracker[msgCount];
        AtomicInteger unsentPartCount = new AtomicInteger(msgCount);
        AtomicBoolean anyPartFailed = new AtomicBoolean(false);
        i = 0;
        while (i < msgCount) {
            ConcatRef concatRef = new ConcatRef();
            concatRef.refNumber = refNumber;
            concatRef.seqNumber = i + 1;
            concatRef.msgCount = msgCount;
            concatRef.isEightBits = true;
            SmsHeader smsHeader = new SmsHeader();
            smsHeader.concatRef = concatRef;
            if (encodingType == 1) {
                smsHeader.languageTable = encodingForParts[i].languageTable;
                smsHeader.languageShiftTable = encodingForParts[i].languageShiftTable;
            }
            PendingIntent pendingIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                pendingIntent = (PendingIntent) sentIntents.get(i);
            }
            PendingIntent pendingIntent2 = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
            }
            trackers[i] = getNewSubmitPduTracker(destAddr, scAddr, (String) parts.get(i), smsHeader, encodingType, pendingIntent, pendingIntent2, i == msgCount + -1, unsentPartCount, anyPartFailed, messageUri, fullMessageText, priority, isExpectMore, validityPeriod, callingPkg);
            trackers[i].mPersistMessage = persistMessage;
            i++;
        }
        if (trackers == null || trackers.length == 0 || trackers[0] == null) {
            Rlog.e(TAG, "Cannot send multipart text. parts=" + parts + " trackers=" + trackers);
            return;
        }
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage != null) {
            Rlog.d(TAG, "Found carrier package.");
            MultipartSmsSender multipartSmsSender = new MultipartSmsSender(parts, trackers);
            multipartSmsSender.sendSmsByCarrierApp(carrierPackage, new MultipartSmsSenderCallback(multipartSmsSender));
        } else {
            Rlog.v(TAG, "No carrier package.");
            for (SmsTracker tracker : trackers) {
                if (tracker != null) {
                    sendSubmitPdu(tracker);
                } else {
                    Rlog.e(TAG, "Null tracker.");
                }
            }
        }
    }

    public void sendRawPdu(final SmsTracker tracker) {
        byte[] pdu = (byte[]) tracker.getData().get("pdu");
        if (this.mSmsSendDisabled) {
            Rlog.e(TAG, "Device does not support sending sms.");
            tracker.onFailed(this.mContext, 4, 0);
        } else if (pdu == null) {
            Rlog.e(TAG, "Empty PDU");
            tracker.onFailed(this.mContext, 3, 0);
        } else {
            PackageManager pm = this.mContext.getPackageManager();
            String[] packageNames = pm.getPackagesForUid(Binder.getCallingUid());
            if (packageNames == null || packageNames.length == 0) {
                Rlog.e(TAG, "Can't get calling app package name: refusing to send SMS");
                tracker.onFailed(this.mContext, 1, 0);
                return;
            }
            String packageName = getPackageNameViaProcessId(packageNames);
            if (packageName != null) {
                packageNames[0] = packageName;
                Rlog.d(TAG, "sendRawPdu and get the package name via process id: " + packageNames[0]);
            }
            try {
                PackageInfo appInfo = pm.getPackageInfoAsUser(packageNames[0], 64, tracker.mUserId);
                if (checkDestination(tracker)) {
                    if (OemConstant.getPowerCenterEnable(this.mPhone.getContext())) {
                        ServiceStateTracker.mSMSSendCount++;
                    }
                    if (!SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US") && !this.mUsageMonitor.check(appInfo.packageName, 1)) {
                        sendMessage(obtainMessage(4, tracker));
                        return;
                    } else if (this.mUsageMonitor.isSmsAuthorizationEnabled()) {
                        this.mUsageMonitor.authorizeOutgoingSms(tracker.mAppInfo, tracker.mDestAddress, tracker.mFullMessageText, new SmsAuthorizationCallback() {
                            public void onAuthorizationResult(boolean accepted) {
                                if (accepted) {
                                    SMSDispatcher.this.sendSms(tracker);
                                } else {
                                    tracker.onFailed(SMSDispatcher.this.mContext, 1, 191286);
                                }
                            }
                        }, this);
                    } else {
                        sendSms(tracker);
                    }
                }
                if (PhoneNumberUtils.isLocalEmergencyNumber(this.mContext, tracker.mDestAddress)) {
                    new AsyncEmergencyContactNotifier(this.mContext).execute(new Void[0]);
                }
            } catch (NameNotFoundException e) {
                Rlog.e(TAG, "Can't get calling app package info: refusing to send SMS");
                tracker.onFailed(this.mContext, 1, 0);
            }
        }
    }

    boolean checkDestination(SmsTracker tracker) {
        return true;
    }

    private boolean denyIfQueueLimitReached(SmsTracker tracker) {
        if (this.mPendingTrackerCount >= 5) {
            Rlog.e(TAG, "Denied because queue limit reached");
            tracker.onFailed(this.mContext, 5, 0);
            return true;
        }
        this.mPendingTrackerCount++;
        return false;
    }

    private CharSequence getAppLabel(String appPackage, int userId) {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            return pm.getApplicationInfoAsUser(appPackage, 0, userId).loadSafeLabel(pm);
        } catch (NameNotFoundException e) {
            Rlog.e(TAG, "PackageManager Name Not Found for package " + appPackage);
            return appPackage;
        }
    }

    protected void handleReachSentLimit(SmsTracker tracker) {
        if (!denyIfQueueLimitReached(tracker)) {
            CharSequence appLabel = getAppLabel(tracker.getAppPackageName(), tracker.mUserId);
            Resources r = Resources.getSystem();
            Spanned messageText = Html.fromHtml(r.getString(17040878, new Object[]{appLabel}));
            ConfirmDialogListener listener = new ConfirmDialogListener(tracker, null, 1);
            AlertDialog d = new Builder(this.mContext).setTitle(17040880).setIcon(17301642).setMessage(messageText).setPositiveButton(r.getString(17040881), listener).setNegativeButton(r.getString(17040879), listener).setOnCancelListener(listener).create();
            d.getWindow().setType(2003);
            d.show();
        }
    }

    protected void handleConfirmShortCode(boolean isPremium, SmsTracker tracker) {
        if (!denyIfQueueLimitReached(tracker)) {
            int detailsId;
            if (isPremium) {
                detailsId = 17040882;
            } else {
                detailsId = 17040888;
            }
            CharSequence appLabel = getAppLabel(tracker.getAppPackageName(), tracker.mUserId);
            Resources r = Resources.getSystem();
            Spanned messageText = Html.fromHtml(r.getString(17040886, new Object[]{appLabel, tracker.mDestAddress}));
            View layout = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(17367274, null);
            ConfirmDialogListener listener = new ConfirmDialogListener(tracker, (TextView) layout.findViewById(16909293), 0);
            ((TextView) layout.findViewById(16909288)).setText(messageText);
            ((TextView) ((ViewGroup) layout.findViewById(16909289)).findViewById(16909290)).setText(detailsId);
            ((CheckBox) layout.findViewById(16909291)).setOnCheckedChangeListener(listener);
            AlertDialog d = new Builder(this.mContext).setView(layout).setPositiveButton(r.getString(17040883), listener).setNegativeButton(r.getString(17040885), listener).setOnCancelListener(listener).create();
            d.getWindow().setType(2003);
            d.show();
            listener.setPositiveButton(d.getButton(-1));
            listener.setNegativeButton(d.getButton(-2));
        }
    }

    public int getPremiumSmsPermission(String packageName) {
        return this.mUsageMonitor.getPremiumSmsPermission(packageName);
    }

    public void setPremiumSmsPermission(String packageName, int permission) {
        this.mUsageMonitor.setPremiumSmsPermission(packageName, permission);
    }

    public void sendRetrySms(SmsTracker tracker) {
        if (this.mImsSMSDispatcher != null) {
            this.mImsSMSDispatcher.sendRetrySms(tracker);
        } else {
            Rlog.e(TAG, this.mImsSMSDispatcher + " is null. Retry failed");
        }
    }

    private void sendMultipartSms(SmsTracker tracker) {
        HashMap<String, Object> map = tracker.getData();
        String destinationAddress = (String) map.get("destination");
        String scAddress = (String) map.get("scaddress");
        ArrayList<String> parts = (ArrayList) map.get("parts");
        ArrayList<PendingIntent> sentIntents = (ArrayList) map.get("sentIntents");
        ArrayList<PendingIntent> deliveryIntents = (ArrayList) map.get("deliveryIntents");
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0) {
            sendMultipartText(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, null, null, tracker.mPersistMessage, -1, tracker.mExpectMore, tracker.mvalidityPeriod);
            return;
        }
        int i = 0;
        int count = parts.size();
        while (i < count) {
            PendingIntent pendingIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                pendingIntent = (PendingIntent) sentIntents.get(i);
            }
            handleNotInService(ss, pendingIntent);
            i++;
        }
    }

    protected SmsTracker getSmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean isExpectMore, String fullMessageText, boolean isText, boolean persistMessage, int validityPeriod, String callingPackage) {
        PackageManager pm = this.mContext.getPackageManager();
        String callerPackageName = callingPackage;
        if (callingPackage == null) {
            String[] packageNames = pm.getPackagesForUid(Binder.getCallingUid());
            String packageName = getPackageNameViaProcessId(packageNames);
            if (packageName != null) {
                packageNames[0] = packageName;
                Rlog.d(TAG, "SmsTrackerFactory and get the package name via process id: " + packageNames[0]);
            }
            callerPackageName = packageNames[0];
        }
        int userId = UserHandle.getCallingUserId();
        PackageInfo appInfo = null;
        if (callerPackageName != null) {
            try {
                appInfo = pm.getPackageInfoAsUser(callerPackageName, 64, userId);
            } catch (NameNotFoundException e) {
            }
        }
        return new SmsTracker(data, sentIntent, deliveryIntent, appInfo, PhoneNumberUtils.extractNetworkPortion((String) data.get("destAddr")), format, unsentPartCount, anyPartFailed, messageUri, smsHeader, isExpectMore, fullMessageText, getSubId(), isText, persistMessage, userId, validityPeriod, null);
    }

    protected SmsTracker getSmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, Uri messageUri, boolean isExpectMore, String fullMessageText, boolean isText, boolean persistMessage, String callingPackage) {
        return getSmsTracker(data, sentIntent, deliveryIntent, format, null, null, messageUri, null, isExpectMore, fullMessageText, isText, persistMessage, -1, callingPackage);
    }

    protected SmsTracker getSmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, Uri messageUri, boolean isExpectMore, String fullMessageText, boolean isText, boolean persistMessage, int validityPeriod, String callingPackage) {
        return getSmsTracker(data, sentIntent, deliveryIntent, format, null, null, messageUri, null, isExpectMore, fullMessageText, isText, persistMessage, validityPeriod, callingPackage);
    }

    protected HashMap<String, Object> getSmsTrackerMap(String destAddr, String scAddr, String text, SubmitPduBase pdu) {
        HashMap<String, Object> map = new HashMap();
        map.put("destAddr", destAddr);
        map.put("scAddr", scAddr);
        map.put("text", text);
        map.put("smsc", pdu.encodedScAddress);
        map.put("pdu", pdu.encodedMessage);
        return map;
    }

    protected HashMap<String, Object> getSmsTrackerMap(String destAddr, String scAddr, int destPort, byte[] data, SubmitPduBase pdu) {
        HashMap<String, Object> map = new HashMap();
        map.put("destAddr", destAddr);
        map.put("scAddr", scAddr);
        map.put("destPort", Integer.valueOf(destPort));
        map.put("data", data);
        map.put("smsc", pdu.encodedScAddress);
        map.put("pdu", pdu.encodedMessage);
        return map;
    }

    public boolean isIms() {
        if (this.mImsSMSDispatcher != null) {
            return this.mImsSMSDispatcher.isIms();
        }
        Rlog.e(TAG, this.mImsSMSDispatcher + " is null");
        return false;
    }

    public String getImsSmsFormat() {
        if (this.mImsSMSDispatcher != null) {
            return this.mImsSMSDispatcher.getImsSmsFormat();
        }
        Rlog.e(TAG, this.mImsSMSDispatcher + " is null");
        return null;
    }

    private String getMultipartMessageText(ArrayList<String> parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part != null) {
                sb.append(part);
            }
        }
        return sb.toString();
    }

    protected String getCarrierAppPackageName() {
        UiccCard card = UiccController.getInstance().getUiccCard(this.mPhone.getPhoneId());
        if (card == null) {
            return null;
        }
        List<String> carrierPackages = card.getCarrierPackageNamesForIntent(this.mContext.getPackageManager(), new Intent("android.service.carrier.CarrierMessagingService"));
        if (carrierPackages == null || carrierPackages.size() != 1) {
            return CarrierSmsUtils.getCarrierImsPackageForIntent(this.mContext, this.mPhone, new Intent("android.service.carrier.CarrierMessagingService"));
        }
        return (String) carrierPackages.get(0);
    }

    protected int getSubId() {
        return SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mPhone.getPhoneId());
    }

    private void checkCallerIsPhoneOrCarrierApp() {
        int uid = Binder.getCallingUid();
        if (UserHandle.getAppId(uid) != 1001 && uid != 0) {
            try {
                if (!UserHandle.isSameApp(this.mContext.getPackageManager().getApplicationInfo(getCarrierAppPackageName(), 0).uid, Binder.getCallingUid())) {
                    throw new SecurityException("Caller is not phone or carrier app!");
                }
            } catch (NameNotFoundException e) {
                throw new SecurityException("Caller is not phone or carrier app!");
            }
        }
    }

    protected static boolean noNeedWriteSmsToDbWhenSendSms(Context context, String packageName) {
        Cursor cursor = null;
        int count = -1;
        try {
            if (SystemProperties.get("persist.sys.sms_cmcc", "0").equals("1") || context == null || TextUtils.isEmpty(packageName)) {
                return false;
            }
            if (InboundSmsHandler.isDefaultMmsRegion(context)) {
                Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "except region");
                return false;
            }
            cursor = context.getContentResolver().query(Uri.parse("content://com.color.provider.SafeProvider/pp_permission"), null, "pkg_name=? AND send_sms=1", new String[]{packageName}, null);
            if (cursor == null) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return false;
            }
            boolean z;
            count = cursor.getCount();
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
            if (count >= 1) {
                z = true;
            } else {
                z = false;
            }
            return z;
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex22) {
                    ex22.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex222) {
                    ex222.printStackTrace();
                }
            }
        }
    }

    private String getPackageNameViaProcessId(String[] packageNames) {
        String packageName = null;
        try {
            if (packageNames.length == 1) {
                return packageNames[0];
            }
            if (packageNames.length <= 1) {
                return null;
            }
            int callingPid = Binder.getCallingPid();
            List<RunningAppProcessInfo> processList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
            if (processList == null) {
                return null;
            }
            for (RunningAppProcessInfo processInfo : processList) {
                if (callingPid == processInfo.pid) {
                    for (String pkgInProcess : processInfo.pkgList) {
                        for (String pkg : packageNames) {
                            if (pkg.equals(pkgInProcess)) {
                                packageName = pkg;
                                break;
                            }
                        }
                        if (packageName != null) {
                            return packageName;
                        }
                    }
                    return packageName;
                }
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    protected boolean isNeedStopSms(SmsTracker tracker) {
        return false;
    }

    protected void countSendSms(SmsTracker smsTracker) {
        try {
            String packageName = smsTracker.mAppInfo.applicationInfo.packageName;
            if (this.mOppoUsageManager == null || !"com.android.mms".equals(packageName)) {
                boolean shouldCount = true;
                String[] countSmsFilterPackages = new String[]{"com.android", "com.mediatek", "com.oppo", "com.qualcomm", "com.nxp", "com.nearme"};
                Rlog.d(TAG, "packageName=" + packageName);
                if (countSmsFilterPackages != null && packageName != null) {
                    for (String smsPackage : countSmsFilterPackages) {
                        if (smsPackage != null && packageName.startsWith(smsPackage)) {
                            shouldCount = false;
                            break;
                        }
                    }
                }
                Rlog.d(TAG, "shouldCount=" + shouldCount);
                if (shouldCount) {
                    Rlog.d(TAG, "accumulate the count of the send sms");
                    if (this.mOppoUsageManager != null) {
                        this.mOppoUsageManager.accumulateHistoryCountOfSendedMsg(1);
                    }
                }
                return;
            }
            Rlog.d(TAG, "com.android.sms--shouldCount, accumulate the count of the send sms");
            this.mOppoUsageManager.accumulateHistoryCountOfSendedMsg(1);
        } catch (Exception e) {
            Rlog.e(TAG, "countSendSms--exception");
        }
    }

    protected boolean isRetryAlwaysOverIMS() {
        try {
            CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
            PersistableBundle b = null;
            boolean retryAlwaysOverIMS = false;
            if (configManager != null) {
                b = configManager.getConfigForSubId(getSubId());
            }
            if (b != null) {
                retryAlwaysOverIMS = b.getBoolean("config_retry_sms_over_ims", false);
            }
            return retryAlwaysOverIMS;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
