package com.android.internal.telephony;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.BroadcastOptions;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.OppoUsageManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Sms.Intents;
import android.provider.oppo.CallLog.Calls;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.EventLog;
import com.android.internal.telephony.CarrierServicesSmsFilter.CarrierServicesSmsFilterCallbackInterface;
import com.android.internal.telephony.SmsHeader.ConcatRef;
import com.android.internal.telephony.SmsHeader.PortAddrs;
import com.android.internal.telephony.regionlock.RegionLockConstant;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.telephony.util.NotificationChannelController;
import com.android.internal.util.HexDump;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.codeaurora.ims.QtiCallConstants;

public abstract class InboundSmsHandler extends StateMachine {
    private static String ACTION_OPEN_SMS_APP = "com.android.internal.telephony.OPEN_DEFAULT_SMS_APP";
    public static final int ADDRESS_COLUMN = 6;
    private static final String AUTHORITY = "com.coloros.provider.BlackListProvider";
    private static final Uri AUTHORITY_URI = Uri.parse("content://com.coloros.provider.BlackListProvider");
    private static final int COLOR_BLOCK_SMS_TYPE_BLACKLIST = 0;
    private static final int COLOR_BLOCK_SMS_TYPE_NONE = -1;
    private static final int COLOR_BLOCK_SMS_TYPE_ZHENGQI = 1;
    private static final String COLOR_DEFAULT_MMS_REGIONS = "color_default_mms_regions";
    public static final int COUNT_COLUMN = 5;
    public static final int DATE_COLUMN = 3;
    protected static final boolean DBG = true;
    private static final long DEFAULT_DUP_SMS_KEPP_PERIOD = 300000;
    public static final int DESTINATION_PORT_COLUMN = 2;
    public static final int DISPLAY_ADDRESS_COLUMN = 9;
    private static final int EVENT_BROADCAST_COMPLETE = 3;
    public static final int EVENT_BROADCAST_SMS = 2;
    public static final int EVENT_INJECT_SMS = 8;
    public static final int EVENT_NEW_SMS = 1;
    private static final int EVENT_RELEASE_WAKELOCK = 5;
    private static final int EVENT_RETURN_TO_IDLE = 4;
    public static final int EVENT_START_ACCEPTING_SMS = 6;
    private static final int EVENT_STATE_TIMEOUT = 10;
    private static final int EVENT_UPDATE_PHONE_OBJECT = 7;
    private static final int EVENT_UPDATE_TRACKER = 9;
    public static final int ID_COLUMN = 7;
    private static final String[] INITIAL_REGIONS = new String[]{"AU", "NZ"};
    public static final int MESSAGE_BODY_COLUMN = 8;
    private static final int NOTIFICATION_ID_NEW_MESSAGE = 1;
    private static final String NOTIFICATION_TAG = "InboundSmsHandler";
    private static final String OEM_DEFAULT_SYSTEM_SMS_DELIVER_CLASS = "com.android.mms.transaction.PrivilegedSmsReceiver";
    private static final String OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE = "com.android.mms";
    private static final int OEM_SMS_MMS_SHOWDIALOG = 1;
    private static final String OPPO_BLOCK_SMS_DELIVER_ACTION = "oppo.intent.action.OPPO_BLOCK_SMS_DELIVER_ACTION";
    public static final int PDU_COLUMN = 0;
    private static final String[] PDU_PROJECTION = new String[]{"pdu"};
    private static final String[] PDU_SEQUENCE_PORT_PROJECTION = new String[]{"pdu", "sequence", "destination_port", "display_originating_addr"};
    private static final Map<Integer, Integer> PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING = new HashMap<Integer, Integer>() {
        {
            put(Integer.valueOf(0), Integer.valueOf(0));
            put(Integer.valueOf(1), Integer.valueOf(1));
            put(Integer.valueOf(2), Integer.valueOf(2));
            put(Integer.valueOf(9), Integer.valueOf(3));
        }
    };
    public static final int REFERENCE_NUMBER_COLUMN = 4;
    public static final String SELECT_BY_ID = "_id=?";
    public static final int SEQUENCE_COLUMN = 1;
    public static final int STATE_TIMEOUT = 300000;
    private static final String TABLE_BL_LIST = "bl_list";
    private static String TAG = NOTIFICATION_TAG;
    private static final Uri URI_BLACKLIST_BLOCK_SMS_AND_CALL = Uri.withAppendedPath(URI_BLACKLIST_LIST, URI_PATH_SMS_AND_CALL_BLOCK);
    private static Uri URI_BLACKLIST_LIST = Uri.withAppendedPath(AUTHORITY_URI, TABLE_BL_LIST);
    private static final String URI_PATH_SMS_AND_CALL_BLOCK = "sms_and_call_block";
    private static final boolean VDBG = false;
    private static final int WAKELOCK_TIMEOUT = 3000;
    private static Handler mUiHandler;
    protected static final Uri sRawUri = Uri.withAppendedPath(Sms.CONTENT_URI, "raw");
    protected static final Uri sRawUriPermanentDelete = Uri.withAppendedPath(Sms.CONTENT_URI, "raw/permanentDelete");
    private final int DELETE_PERMANENTLY = 1;
    private final int MARK_DELETED = 2;
    protected boolean isLastStorageFull = false;
    protected CellBroadcastHandler mCellBroadcastHandler;
    protected final Context mContext;
    private final DefaultState mDefaultState = new DefaultState(this, null);
    private final DeliveringState mDeliveringState = new DeliveringState(this, null);
    IDeviceIdleController mDeviceIdleController;
    private final IdleState mIdleState = new IdleState(this, null);
    private OppoUsageManager mOppoUsageManager = null;
    protected Phone mPhone;
    private final ContentResolver mResolver;
    private final boolean mSmsReceiveDisabled;
    private final HashMap<Long, byte[]> mSmsStampWithPdu = new HashMap();
    private final StartupState mStartupState = new StartupState(this, null);
    protected SmsStorageMonitor mStorageMonitor;
    private UserManager mUserManager;
    private final WaitingState mWaitingState = new WaitingState(this, null);
    private final WakeLock mWakeLock;
    private int mWakeLockTimeout;
    private final WapPushOverSms mWapPush;

    private final class CarrierServicesSmsFilterCallback implements CarrierServicesSmsFilterCallbackInterface {
        private final int mDestPort;
        private final byte[][] mPdus;
        private final SmsBroadcastReceiver mSmsBroadcastReceiver;
        private final String mSmsFormat;
        private final boolean mUserUnlocked;

        CarrierServicesSmsFilterCallback(byte[][] pdus, int destPort, String smsFormat, SmsBroadcastReceiver smsBroadcastReceiver, boolean userUnlocked) {
            this.mPdus = pdus;
            this.mDestPort = destPort;
            this.mSmsFormat = smsFormat;
            this.mSmsBroadcastReceiver = smsBroadcastReceiver;
            this.mUserUnlocked = userUnlocked;
        }

        public void onFilterComplete(int result) {
            InboundSmsHandler.this.logv("onFilterComplete: result is " + result);
            if ((result & 1) != 0) {
                InboundSmsHandler.this.dropSms(this.mSmsBroadcastReceiver);
            } else if (VisualVoicemailSmsFilter.filter(InboundSmsHandler.this.mContext, this.mPdus, this.mSmsFormat, this.mDestPort, InboundSmsHandler.this.mPhone.getSubId())) {
                InboundSmsHandler.this.log("Visual voicemail SMS dropped");
                InboundSmsHandler.this.dropSms(this.mSmsBroadcastReceiver);
            } else if (this.mUserUnlocked) {
                InboundSmsHandler.this.dispatchSmsDeliveryIntent(this.mPdus, this.mSmsFormat, this.mDestPort, this.mSmsBroadcastReceiver);
            } else {
                if (!InboundSmsHandler.this.isSkipNotifyFlagSet(result)) {
                    InboundSmsHandler.this.showNewMessageNotification();
                }
                InboundSmsHandler.this.sendMessage(3);
            }
        }
    }

    private class DefaultState extends State {
        /* synthetic */ DefaultState(InboundSmsHandler this$0, DefaultState -this1) {
            this();
        }

        private DefaultState() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 7:
                    try {
                        InboundSmsHandler.this.onUpdatePhoneObject((Phone) msg.obj);
                        break;
                    } catch (Exception e) {
                        InboundSmsHandler.this.loge("EVENT_UPDATE_PHONE_OBJECT--exception");
                        break;
                    }
                default:
                    if (msg != null) {
                        InboundSmsHandler.this.loge("processMessage: unhandled message type " + msg.what);
                        break;
                    }
                    break;
            }
            return true;
        }
    }

    private class DeliveringState extends State {
        /* synthetic */ DeliveringState(InboundSmsHandler this$0, DeliveringState -this1) {
            this();
        }

        private DeliveringState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Delivering state");
        }

        public void exit() {
            InboundSmsHandler.this.log("leaving Delivering state");
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("DeliveringState.processMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                    try {
                        InboundSmsHandler.this.handleNewSms((AsyncResult) msg.obj);
                        InboundSmsHandler.this.sendMessage(4);
                    } catch (Exception e) {
                        e.printStackTrace();
                        InboundSmsHandler.this.log("EVENT_NEW_SMS--exception");
                    }
                    return true;
                case 2:
                    boolean processResult;
                    InboundSmsTracker inboundSmsTracker = msg.obj;
                    try {
                        processResult = InboundSmsHandler.this.processMessagePart(inboundSmsTracker);
                    } catch (Exception e2) {
                        processResult = false;
                        e2.printStackTrace();
                    }
                    if (processResult) {
                        InboundSmsHandler.this.sendMessage(9, inboundSmsTracker);
                        InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mWaitingState);
                    } else {
                        InboundSmsHandler.this.log("No broadcast sent on processing EVENT_BROADCAST_SMS in Delivering state. Return to Idle state");
                        InboundSmsHandler.this.sendMessage(4);
                    }
                    return true;
                case 4:
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mIdleState);
                    return true;
                case 5:
                    InboundSmsHandler.this.mWakeLock.release();
                    if (!InboundSmsHandler.this.mWakeLock.isHeld()) {
                        InboundSmsHandler.this.loge("mWakeLock released while delivering/broadcasting!");
                    }
                    return true;
                case 8:
                    try {
                        InboundSmsHandler.this.handleInjectSms((AsyncResult) msg.obj);
                        InboundSmsHandler.this.sendMessage(4);
                    } catch (Exception e22) {
                        e22.printStackTrace();
                        InboundSmsHandler.this.log("EVENT_INJECT_SMS--exception");
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    private class IdleState extends State {
        /* synthetic */ IdleState(InboundSmsHandler this$0, IdleState -this1) {
            this();
        }

        private IdleState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Idle state");
            InboundSmsHandler.this.sendMessageDelayed(5, (long) InboundSmsHandler.this.getWakeLockTimeout());
        }

        public void exit() {
            InboundSmsHandler.this.mWakeLock.acquire();
            InboundSmsHandler.this.log("acquired wakelock, leaving Idle state");
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("IdleState.processMessage:" + msg.what);
            InboundSmsHandler.this.log("Idle state processing message type " + msg.what);
            switch (msg.what) {
                case 1:
                case 2:
                case 8:
                    InboundSmsHandler.this.deferMessage(msg);
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mDeliveringState);
                    return true;
                case 4:
                    return true;
                case 5:
                    InboundSmsHandler.this.mWakeLock.release();
                    if (InboundSmsHandler.this.mWakeLock.isHeld()) {
                        InboundSmsHandler.this.log("mWakeLock is still held after release");
                    } else {
                        InboundSmsHandler.this.log("mWakeLock released");
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    private static class NewMessageNotificationActionReceiver extends BroadcastReceiver {
        /* synthetic */ NewMessageNotificationActionReceiver(NewMessageNotificationActionReceiver -this0) {
            this();
        }

        private NewMessageNotificationActionReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (InboundSmsHandler.ACTION_OPEN_SMS_APP.equals(intent.getAction())) {
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage(Sms.getDefaultSmsPackage(context)));
            }
        }
    }

    private final class SmsBroadcastReceiver extends BroadcastReceiver {
        private long mBroadcastTimeNano = System.nanoTime();
        private final String mDeleteWhere;
        private final String[] mDeleteWhereArgs;

        SmsBroadcastReceiver(InboundSmsTracker tracker) {
            this.mDeleteWhere = tracker.getDeleteWhere();
            this.mDeleteWhereArgs = tracker.getDeleteWhereArgs();
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
                Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "onReceive--SmsBroadcastReceiver, intent.getAction=" + action);
                if (!(InboundSmsHandler.this.mOppoUsageManager == null || action == null || (!action.equals("android.provider.Telephony.SMS_DELIVER") && !action.equals("android.provider.Telephony.WAP_PUSH_DELIVER")))) {
                    Rlog.d(InboundSmsHandler.NOTIFICATION_TAG, "accumulate the count of the received sms");
                    InboundSmsHandler.this.mOppoUsageManager.accumulateHistoryCountOfReceivedMsg(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (action.equals("android.provider.Telephony.SMS_DELIVER")) {
                intent.setAction("android.provider.Telephony.SMS_RECEIVED");
                intent.addFlags(QtiCallConstants.CAPABILITY_SUPPORTS_DOWNGRADE_TO_VOICE_REMOTE);
                intent.setComponent(null);
                Intent intent2 = intent;
                InboundSmsHandler.this.dispatchIntent(intent2, "android.permission.RECEIVE_SMS", 16, InboundSmsHandler.this.handleSmsWhitelisting(null), this, UserHandle.ALL);
            } else if (action.equals("android.provider.Telephony.WAP_PUSH_DELIVER")) {
                intent.setAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
                intent.setComponent(null);
                intent.addFlags(QtiCallConstants.CAPABILITY_SUPPORTS_DOWNGRADE_TO_VOICE_REMOTE);
                Bundle options = null;
                try {
                    long duration = InboundSmsHandler.this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(InboundSmsHandler.this.mContext.getPackageName(), 0, "mms-broadcast");
                    BroadcastOptions bopts = BroadcastOptions.makeBasic();
                    bopts.setTemporaryAppWhitelistDuration(duration);
                    options = bopts.toBundle();
                } catch (RemoteException e2) {
                }
                String mimeType = intent.getType();
                InboundSmsHandler.this.dispatchIntent(intent, WapPushOverSms.getPermissionForType(mimeType), WapPushOverSms.getAppOpsPermissionForIntent(mimeType), options, this, UserHandle.SYSTEM);
            } else {
                if (!("android.intent.action.DATA_SMS_RECEIVED".equals(action) || ("android.provider.Telephony.SMS_RECEIVED".equals(action) ^ 1) == 0 || ("android.intent.action.DATA_SMS_RECEIVED".equals(action) ^ 1) == 0 || ("android.provider.Telephony.WAP_PUSH_RECEIVED".equals(action) ^ 1) == 0)) {
                    InboundSmsHandler.this.loge("unexpected BroadcastReceiver action: " + action);
                }
                int rc = getResultCode();
                if (rc == -1 || rc == 1) {
                    InboundSmsHandler.this.log("successful broadcast, deleting from raw table.");
                } else {
                    InboundSmsHandler.this.loge("a broadcast receiver set the result code to " + rc + ", deleting from raw table anyway!");
                }
                InboundSmsHandler.this.deleteFromRawTable(this.mDeleteWhere, this.mDeleteWhereArgs, 2);
                InboundSmsHandler.this.sendMessage(3);
                int durationMillis = (int) ((System.nanoTime() - this.mBroadcastTimeNano) / 1000000);
                if (durationMillis >= RegionLockConstant.EVENT_NETWORK_LOCK_STATUS) {
                    InboundSmsHandler.this.loge("Slow ordered broadcast completion time: " + durationMillis + " ms");
                } else {
                    InboundSmsHandler.this.log("ordered broadcast completed in: " + durationMillis + " ms");
                }
            }
        }
    }

    private class StartupState extends State {
        /* synthetic */ StartupState(InboundSmsHandler this$0, StartupState -this1) {
            this();
        }

        private StartupState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Startup state");
            InboundSmsHandler.this.setWakeLockTimeout(0);
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("StartupState.processMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                case 2:
                case 8:
                    InboundSmsHandler.this.deferMessage(msg);
                    return true;
                case 6:
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mIdleState);
                    return true;
                default:
                    return false;
            }
        }
    }

    private static class UIHandler extends Handler {
        private Context context;

        public UIHandler(Context context) {
            this.context = context;
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                Rlog.v(NotificationChannelController.CHANNEL_ID_SMS, "msg == null");
                return;
            }
            switch (msg.what) {
                case 1:
                    try {
                        if (Looper.myLooper() == Looper.getMainLooper() && this.context != null) {
                            AlertDialog d = new Builder(this.context, 201523207).setMessage(201590148).setTitle(201590150).setPositiveButton(201590149, null).create();
                            d.getWindow().setType(2003);
                            d.setCanceledOnTouchOutside(false);
                            d.setCancelable(false);
                            d.show();
                            break;
                        }
                        Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "context=" + this.context);
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    break;
                default:
                    Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "error, msg" + msg);
                    break;
            }
        }
    }

    private class WaitingState extends State {
        private InboundSmsTracker mTracker;

        /* synthetic */ WaitingState(InboundSmsHandler this$0, WaitingState -this1) {
            this();
        }

        private WaitingState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Waiting state");
            this.mTracker = null;
            InboundSmsHandler.this.sendMessageDelayed(10, 300000);
        }

        public void exit() {
            InboundSmsHandler.this.log("exiting Waiting state");
            InboundSmsHandler.this.setWakeLockTimeout(InboundSmsHandler.WAKELOCK_TIMEOUT);
            InboundSmsHandler.this.removeMessages(10);
            InboundSmsHandler.this.removeMessages(9);
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("WaitingState.processMessage:" + msg.what);
            switch (msg.what) {
                case 2:
                    InboundSmsHandler.this.deferMessage(msg);
                    return true;
                case 3:
                    InboundSmsHandler.this.sendMessage(4);
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mDeliveringState);
                    return true;
                case 4:
                    return true;
                case 9:
                    this.mTracker = (InboundSmsTracker) msg.obj;
                    return true;
                case 10:
                    if (this.mTracker != null) {
                        InboundSmsHandler.this.log("WaitingState.processMessage: EVENT_STATE_TIMEOUT; dropping message");
                        InboundSmsHandler.this.dropSms(new SmsBroadcastReceiver(this.mTracker));
                    } else {
                        InboundSmsHandler.this.log("WaitingState.processMessage: EVENT_STATE_TIMEOUT; mTracker is null - sending EVENT_BROADCAST_COMPLETE");
                        InboundSmsHandler.this.sendMessage(3);
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    protected abstract void acknowledgeLastIncomingSms(boolean z, int i, Message message);

    protected abstract int dispatchMessageRadioSpecific(SmsMessageBase smsMessageBase);

    protected abstract boolean is3gpp2();

    protected InboundSmsHandler(String name, Context context, SmsStorageMonitor storageMonitor, Phone phone, CellBroadcastHandler cellBroadcastHandler) {
        super(name);
        this.mContext = context;
        this.mStorageMonitor = storageMonitor;
        this.mPhone = phone;
        this.mCellBroadcastHandler = cellBroadcastHandler;
        this.mResolver = context.getContentResolver();
        this.mWapPush = new WapPushOverSms(context);
        this.mSmsReceiveDisabled = TelephonyManager.from(this.mContext).getSmsReceiveCapableForPhone(this.mPhone.getPhoneId(), this.mContext.getResources().getBoolean(17957018)) ^ 1;
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, name);
        this.mWakeLock.acquire();
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mDeviceIdleController = TelephonyComponentFactory.getInstance().getIDeviceIdleController();
        addState(this.mDefaultState);
        addState(this.mStartupState, this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mDeliveringState, this.mDefaultState);
        addState(this.mWaitingState, this.mDeliveringState);
        setInitialState(this.mStartupState);
        log("created InboundSmsHandler");
        this.mOppoUsageManager = OppoUsageManager.getOppoUsageManager();
        initUIHandler(this.mContext, this.mPhone == null ? null : this.mPhone.getContext());
    }

    public void dispose() {
        quit();
    }

    public void updatePhoneObject(Phone phone) {
        sendMessage(7, phone);
    }

    protected void onQuitting() {
        this.mWapPush.dispose();
        while (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    private void handleNewSms(AsyncResult ar) {
        if (ar.exception != null) {
            loge("Exception processing incoming SMS: " + ar.exception);
            return;
        }
        int result;
        try {
            result = dispatchMessage(ar.result.mWrappedSmsMessage);
        } catch (RuntimeException ex) {
            loge("Exception dispatching message", ex);
            result = 2;
        }
        if (result != -1) {
            notifyAndAcknowledgeLastIncomingSms(result == 1, result, null);
        }
    }

    private void handleInjectSms(AsyncResult ar) {
        int result;
        PendingIntent pendingIntent = null;
        try {
            pendingIntent = (PendingIntent) ar.userObj;
            SmsMessage sms = ar.result;
            if (sms == null) {
                result = 2;
            } else {
                result = dispatchMessage(sms.mWrappedSmsMessage);
            }
        } catch (RuntimeException ex) {
            loge("Exception dispatching message", ex);
            result = 2;
        }
        if (pendingIntent != null) {
            try {
                pendingIntent.send(result);
            } catch (CanceledException e) {
            }
        }
    }

    private int dispatchMessage(SmsMessageBase smsb) {
        if (smsb == null) {
            loge("dispatchSmsMessage: message is null");
            return 2;
        } else if (this.mSmsReceiveDisabled) {
            log("Received short message on device which doesn't support receiving SMS. Ignored.");
            return 1;
        } else {
            boolean onlyCore = false;
            try {
                onlyCore = Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
            } catch (RemoteException e) {
            }
            if (!onlyCore) {
                return dispatchMessageRadioSpecific(smsb);
            }
            log("Received a short message in encrypted state. Rejecting.");
            return 2;
        }
    }

    protected void onUpdatePhoneObject(Phone phone) {
        this.mPhone = phone;
        this.mStorageMonitor = this.mPhone.mSmsStorageMonitor;
        log("onUpdatePhoneObject: phone=" + this.mPhone.getClass().getSimpleName());
    }

    private void notifyAndAcknowledgeLastIncomingSms(boolean success, int result, Message response) {
        if (!success) {
            Intent intent = new Intent("android.provider.Telephony.SMS_REJECTED");
            intent.putExtra("result", result);
            intent.addFlags(QtiCallConstants.CAPABILITY_SUPPORTS_DOWNGRADE_TO_VOICE_REMOTE);
            this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
        }
        acknowledgeLastIncomingSms(success, result, response);
    }

    protected int dispatchNormalMessage(SmsMessageBase sms) {
        InboundSmsTracker tracker;
        SmsHeader smsHeader = sms.getUserDataHeader();
        if (smsHeader == null || smsHeader.concatRef == null) {
            int destPort = -1;
            if (!(smsHeader == null || smsHeader.portAddrs == null)) {
                destPort = smsHeader.portAddrs.destPort;
                log("destination port: " + destPort);
            }
            tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), destPort, is3gpp2(), false, sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), sms.getMessageBody());
        } else {
            ConcatRef concatRef = smsHeader.concatRef;
            PortAddrs portAddrs = smsHeader.portAddrs;
            tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), portAddrs != null ? portAddrs.destPort : -1, is3gpp2(), sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), concatRef.refNumber, concatRef.seqNumber, concatRef.msgCount, false, sms.getMessageBody());
        }
        return addTrackerToRawTableAndSendMessage(tracker, tracker.getDestPort() == -1);
    }

    protected int addTrackerToRawTableAndSendMessage(InboundSmsTracker tracker, boolean deDup) {
        switch (addTrackerToRawTable(tracker, deDup)) {
            case 1:
                sendMessage(2, tracker);
                return 1;
            case 5:
                return 1;
            default:
                return 2;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:56:0x01c6 A:{Splitter: B:39:0x011d, ExcHandler: android.database.SQLException (r20_0 'e' java.lang.Throwable), PHI: r18 } */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:56:0x01c6, code:
            r20 = move-exception;
     */
    /* JADX WARNING: Missing block: B:58:?, code:
            loge("Can't access multipart SMS database", r20);
     */
    /* JADX WARNING: Missing block: B:60:0x01d2, code:
            if (r18 != null) goto L_0x01d4;
     */
    /* JADX WARNING: Missing block: B:61:0x01d4, code:
            r18.close();
     */
    /* JADX WARNING: Missing block: B:62:0x01d7, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:84:0x028a, code:
            r22 = move-exception;
     */
    /* JADX WARNING: Missing block: B:86:?, code:
            loge("Can't access multipart SMS database2", r22);
     */
    /* JADX WARNING: Missing block: B:88:0x0296, code:
            if (r18 != null) goto L_0x0298;
     */
    /* JADX WARNING: Missing block: B:89:0x0298, code:
            r18.close();
     */
    /* JADX WARNING: Missing block: B:90:0x029b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean processMessagePart(InboundSmsTracker tracker) {
        Cursor cursor;
        int messageCount = tracker.getMessageCount();
        int destPort = tracker.getDestPort();
        boolean block = false;
        int blockOem = -1;
        try {
            loge("isDupCheckRequired: " + tracker.isDupCheckRequired());
            if (tracker.isDupCheckRequired()) {
                boolean processMsg = true;
                cursor = this.mResolver.query(sRawUri, null, tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), null);
                if (!(cursor == null || (cursor.moveToNext() ^ 1) == 0)) {
                    processMsg = false;
                }
                loge("processMessagePart procedd: " + processMsg);
                if (cursor != null) {
                    cursor.close();
                }
                if (!processMsg) {
                    return false;
                }
                loge("processMessagePart: END of duplicate checking");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Object[] objArr;
        if (messageCount <= 0) {
            objArr = new Object[3];
            objArr[0] = "72298611";
            objArr[1] = Integer.valueOf(-1);
            objArr[2] = String.format("processMessagePart: invalid messageCount = %d", new Object[]{Integer.valueOf(messageCount)});
            EventLog.writeEvent(1397638484, objArr);
            return false;
        }
        byte[][] pdus;
        if (messageCount == 1) {
            pdus = new byte[][]{tracker.getPdu()};
            block = BlockChecker.isBlocked(this.mContext, tracker.getDisplayAddress());
            if (!(block || tracker == null || this.mContext == null)) {
                blockOem = getBlockSmsTypeOem(this.mContext, destPort, tracker.getDisplayAddress());
            }
        } else {
            cursor = null;
            try {
                String address = tracker.getAddress();
                String refNumber = Integer.toString(tracker.getReferenceNumber());
                String count = Integer.toString(tracker.getMessageCount());
                cursor = this.mResolver.query(sRawUri, PDU_SEQUENCE_PORT_PROJECTION, tracker.getQueryForSegments(), new String[]{address, refNumber, count}, null);
                if (cursor.getCount() < messageCount) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return false;
                }
                pdus = new byte[messageCount][];
                while (cursor.moveToNext()) {
                    int index = cursor.getInt(((Integer) PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(1))).intValue()) - tracker.getIndexOffset();
                    if (index >= pdus.length || index < 0) {
                        objArr = new Object[3];
                        objArr[0] = "72298611";
                        objArr[1] = Integer.valueOf(-1);
                        objArr[2] = String.format("processMessagePart: invalid seqNumber = %d, messageCount = %d", new Object[]{Integer.valueOf(tracker.getIndexOffset() + index), Integer.valueOf(messageCount)});
                        EventLog.writeEvent(1397638484, objArr);
                    } else {
                        pdus[index] = HexDump.hexStringToByteArray(cursor.getString(((Integer) PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(0))).intValue()));
                        if (index == 0) {
                            if ((cursor.isNull(((Integer) PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(2))).intValue()) ^ 1) != 0) {
                                int port = InboundSmsTracker.getRealDestPort(cursor.getInt(((Integer) PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(2))).intValue()));
                                if (port != -1) {
                                    destPort = port;
                                }
                            }
                        }
                        if (block) {
                            continue;
                        } else {
                            block = BlockChecker.isBlocked(this.mContext, cursor.getString(((Integer) PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(9))).intValue()));
                            if (!(block || tracker == null)) {
                                if (this.mContext != null) {
                                    blockOem = getBlockSmsTypeOem(this.mContext, destPort, cursor.getString(((Integer) PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(9))).intValue()));
                                }
                            }
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
            } catch (Throwable e) {
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        List<byte[]> pduList = Arrays.asList(pdus);
        if (pduList.size() == 0 || pduList.contains(null)) {
            loge("processMessagePart: returning false due to " + (pduList.size() == 0 ? "pduList.size() == 0" : "pduList.contains(null)"));
            return false;
        }
        SmsBroadcastReceiver resultReceiver = new SmsBroadcastReceiver(tracker);
        if (!this.mUserManager.isUserUnlocked()) {
            return processMessagePartWithUserLocked(tracker, pdus, destPort, resultReceiver);
        }
        if (destPort == 2948) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (byte[] pdu : pdus) {
                byte[] pdu2;
                if (!tracker.is3gpp2()) {
                    SmsMessage msg = SmsMessage.createFromPdu(pdu2, "3gpp");
                    if (msg != null) {
                        pdu2 = msg.getUserData();
                    } else {
                        loge("processMessagePart: SmsMessage.createFromPdu returned null");
                        return false;
                    }
                }
                output.write(pdu2, 0, pdu2.length);
            }
            log("dispatch wap push pdu with addr & sc addr");
            Bundle bundle = new Bundle();
            try {
                if (tracker.is3gpp2WapPdu()) {
                    bundle.putString("address", tracker.getAddress());
                    bundle.putString("service_center", SpnOverride.MVNO_TYPE_NONE);
                } else {
                    SmsMessage sms = SmsMessage.createFromPdu(pdus[0], tracker.getFormat());
                    if (sms != null) {
                        bundle.putString("address", sms.getOriginatingAddress());
                        String sca = sms.getServiceCenterAddress();
                        if (sca == null) {
                            sca = SpnOverride.MVNO_TYPE_NONE;
                        }
                        bundle.putString("service_center", sca);
                    }
                }
            } catch (Exception e2) {
                bundle.putString("address", SpnOverride.MVNO_TYPE_NONE);
                bundle.putString("service_center", SpnOverride.MVNO_TYPE_NONE);
            }
            int result = this.mWapPush.dispatchWapPdu(output.toByteArray(), resultReceiver, this, bundle);
            log("dispatchWapPdu() returned " + result);
            if (result == -1) {
                return true;
            }
            deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
            return false;
        } else if (blockOem == 1) {
            deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 1);
            return false;
        } else {
            if (!filterSms(pdus, destPort, tracker, resultReceiver, true)) {
                oemSetDefaultSms(this.mContext, this.mPhone == null ? null : this.mPhone.getContext());
                if (block || blockOem == 0) {
                    dispatchSmsDeliveryIntentOem(pdus, tracker.getFormat(), destPort, resultReceiver);
                } else {
                    dispatchSmsDeliveryIntent(pdus, tracker.getFormat(), destPort, resultReceiver);
                }
            }
            return true;
        }
    }

    private boolean processMessagePartWithUserLocked(InboundSmsTracker tracker, byte[][] pdus, int destPort, SmsBroadcastReceiver resultReceiver) {
        log("Credential-encrypted storage not available. Port: " + destPort);
        if (destPort == 2948 && this.mWapPush.isWapPushForMms(pdus[0], this)) {
            showNewMessageNotification();
            return false;
        } else if (destPort != -1) {
            return false;
        } else {
            if (filterSms(pdus, destPort, tracker, resultReceiver, false)) {
                return true;
            }
            showNewMessageNotification();
            return false;
        }
    }

    private void showNewMessageNotification() {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            log("Show new message notification.");
            ((NotificationManager) this.mContext.getSystemService("notification")).notify(NOTIFICATION_TAG, 1, new Notification.Builder(this.mContext).setSmallIcon(17301646).setAutoCancel(true).setVisibility(1).setDefaults(-1).setContentTitle(this.mContext.getString(17040355)).setContentText(this.mContext.getString(17040354)).setContentIntent(PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_OPEN_SMS_APP), 1073741824)).setChannelId(NotificationChannelController.CHANNEL_ID_SMS).build());
        }
    }

    static void cancelNewMessageNotification(Context context) {
        ((NotificationManager) context.getSystemService("notification")).cancel(NOTIFICATION_TAG, 1);
    }

    private boolean filterSms(byte[][] pdus, int destPort, InboundSmsTracker tracker, SmsBroadcastReceiver resultReceiver, boolean userUnlocked) {
        if (new CarrierServicesSmsFilter(this.mContext, this.mPhone, pdus, destPort, tracker.getFormat(), new CarrierServicesSmsFilterCallback(pdus, destPort, tracker.getFormat(), resultReceiver, userUnlocked), getName()).filter()) {
            return true;
        }
        if (!VisualVoicemailSmsFilter.filter(this.mContext, pdus, tracker.getFormat(), destPort, this.mPhone.getSubId())) {
            return false;
        }
        log("Visual voicemail SMS dropped");
        dropSms(resultReceiver);
        return true;
    }

    public void dispatchIntent(Intent intent, String permission, int appOp, Bundle opts, BroadcastReceiver resultReceiver, UserHandle user) {
        if (intent == null || intent.getAction() == null || !intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            intent.addFlags(134217728);
        } else {
            intent.removeFlags(134217728);
        }
        String action = intent.getAction();
        if ("android.provider.Telephony.SMS_DELIVER".equals(action) || "android.provider.Telephony.SMS_RECEIVED".equals(action) || "android.provider.Telephony.WAP_PUSH_DELIVER".equals(action) || "android.provider.Telephony.WAP_PUSH_RECEIVED".equals(action)) {
            intent.addFlags(268435456);
        }
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        if (user.equals(UserHandle.ALL)) {
            int[] users = null;
            try {
                users = ActivityManager.getService().getRunningUserIds();
            } catch (RemoteException e) {
            }
            if (users == null) {
                users = new int[]{user.getIdentifier()};
            }
            for (int i = users.length - 1; i >= 0; i--) {
                UserHandle targetUser = new UserHandle(users[i]);
                if (users[i] != 0) {
                    if (!this.mUserManager.hasUserRestriction("no_sms", targetUser)) {
                        UserInfo info = this.mUserManager.getUserInfo(users[i]);
                        if (info != null) {
                            if (info.isManagedProfile()) {
                            }
                        }
                    }
                }
                this.mContext.sendOrderedBroadcastAsUser(intent, targetUser, permission, appOp, opts, users[i] == 0 ? resultReceiver : null, getHandler(), -1, null, null);
            }
            return;
        }
        this.mContext.sendOrderedBroadcastAsUser(intent, user, permission, appOp, opts, resultReceiver, getHandler(), -1, null, null);
    }

    private void deleteFromRawTable(String deleteWhere, String[] deleteWhereArgs, int deleteType) {
        Uri uri = deleteType == 1 ? sRawUriPermanentDelete : sRawUri;
        loge("deleteWhereArgs=" + deleteWhereArgs + " deleteWhere=" + deleteWhere);
        if (deleteWhere == null && deleteWhereArgs == null) {
            loge("No rows need be deleted from raw table, sync from mtk!");
            return;
        }
        int rows = this.mResolver.delete(uri, deleteWhere, deleteWhereArgs);
        if (rows == 0) {
            loge("No rows were deleted from raw table!");
        } else {
            log("Deleted " + rows + " rows from raw table.");
        }
    }

    private Bundle handleSmsWhitelisting(ComponentName target) {
        String pkgName;
        String reason;
        if (target != null) {
            pkgName = target.getPackageName();
            reason = "sms-app";
        } else {
            pkgName = this.mContext.getPackageName();
            reason = "sms-broadcast";
        }
        try {
            long duration = this.mDeviceIdleController.addPowerSaveTempWhitelistAppForSms(pkgName, 0, reason);
            BroadcastOptions bopts = BroadcastOptions.makeBasic();
            bopts.setTemporaryAppWhitelistDuration(duration);
            return bopts.toBundle();
        } catch (RemoteException e) {
            return null;
        }
    }

    private void dispatchSmsDeliveryIntent(byte[][] pdus, String format, int destPort, SmsBroadcastReceiver resultReceiver) {
        Intent intent = new Intent();
        intent.putExtra("pdus", pdus);
        intent.putExtra("format", format);
        if (destPort == -1) {
            intent.setAction("android.provider.Telephony.SMS_DELIVER");
            ComponentName componentName = SmsApplication.getDefaultSmsApplication(this.mContext, true);
            if (componentName != null) {
                intent.setComponent(componentName);
                log("Delivering SMS to: " + componentName.getPackageName() + " " + componentName.getClassName());
            } else {
                intent.setComponent(null);
            }
            if (SmsManager.getDefault().getAutoPersisting()) {
                Uri uri = writeInboxMessage(intent);
                if (uri != null) {
                    intent.putExtra("uri", uri.toString());
                }
            }
            if (this.mPhone.getAppSmsManager().handleSmsReceivedIntent(intent)) {
                dropSms(resultReceiver);
                return;
            }
        }
        intent.setAction("android.intent.action.DATA_SMS_RECEIVED");
        intent.setData(Uri.parse("sms://localhost:" + destPort));
        intent.setComponent(null);
        intent.addFlags(QtiCallConstants.CAPABILITY_SUPPORTS_DOWNGRADE_TO_VOICE_REMOTE);
        dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, handleSmsWhitelisting(intent.getComponent()), resultReceiver, UserHandle.SYSTEM);
    }

    private boolean duplicateExists(InboundSmsTracker tracker) throws SQLException {
        String where;
        String address = tracker.getAddress();
        String refNumber = Integer.toString(tracker.getReferenceNumber());
        String count = Integer.toString(tracker.getMessageCount());
        String seqNumber = Integer.toString(tracker.getSequenceNumber());
        String date = Long.toString(tracker.getTimestamp());
        String messageBody = tracker.getMessageBody();
        if (tracker.getMessageCount() == 1) {
            where = "address=? AND reference_number=? AND count=? AND sequence=? AND date=? AND message_body=?";
        } else {
            where = tracker.getQueryForMultiPartDuplicates();
        }
        Cursor cursor = null;
        try {
            cursor = this.mResolver.query(sRawUri, PDU_PROJECTION, where, new String[]{address, refNumber, count, seqNumber, date, messageBody}, null);
            if (cursor == null || !cursor.moveToNext()) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            loge("Discarding duplicate message segment, refNumber=" + refNumber + " seqNumber=" + seqNumber + " count=" + count);
            String oldPduString = cursor.getString(0);
            byte[] pdu = tracker.getPdu();
            byte[] oldPdu = HexDump.hexStringToByteArray(oldPduString);
            if (!Arrays.equals(oldPdu, tracker.getPdu())) {
                loge("Warning: dup message segment PDU of length " + pdu.length + " is different from existing PDU of length " + oldPdu.length);
            }
            return true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int addTrackerToRawTable(InboundSmsTracker tracker, boolean deDup) {
        if (deDup) {
            try {
                if (duplicateExists(tracker)) {
                    return 5;
                }
            } catch (SQLException e) {
                loge("Can't access SMS database", e);
                return 2;
            }
        }
        logd("Skipped message de-duping logic");
        String address = tracker.getAddress();
        String refNumber = Integer.toString(tracker.getReferenceNumber());
        String count = Integer.toString(tracker.getMessageCount());
        Uri newUri = this.mResolver.insert(sRawUri, tracker.getContentValues());
        log("URI of new row -> " + newUri);
        try {
            long rowId = ContentUris.parseId(newUri);
            if (tracker.getMessageCount() == 1) {
                tracker.setDeleteWhere(SELECT_BY_ID, new String[]{Long.toString(rowId)});
            } else {
                tracker.setDeleteWhere(tracker.getQueryForSegments(), new String[]{address, refNumber, count});
            }
            return 1;
        } catch (Exception e2) {
            loge("error parsing URI for new row: " + newUri, e2);
            return 2;
        }
    }

    static boolean isCurrentFormat3gpp2() {
        return 2 == TelephonyManager.getDefault().getCurrentPhoneType();
    }

    private void dropSms(SmsBroadcastReceiver receiver) {
        deleteFromRawTable(receiver.mDeleteWhere, receiver.mDeleteWhereArgs, 2);
        sendMessage(3);
    }

    private boolean isSkipNotifyFlagSet(int callbackResult) {
        return (callbackResult & 2) > 0;
    }

    protected void log(String s) {
        Rlog.d(getName(), s);
    }

    protected void loge(String s) {
        Rlog.e(getName(), s);
    }

    protected void loge(String s, Throwable e) {
        Rlog.e(getName(), s, e);
    }

    private Uri writeInboxMessage(Intent intent) {
        try {
            SmsMessage[] messages = Intents.getMessagesFromIntent(intent);
            if (messages == null || messages.length < 1) {
                loge("Failed to parse SMS pdu");
                return null;
            }
            int i = 0;
            int length = messages.length;
            while (i < length) {
                try {
                    messages[i].getDisplayMessageBody();
                    i++;
                } catch (NullPointerException e) {
                    loge("NPE inside SmsMessage");
                    return null;
                }
            }
            ContentValues values = parseSmsMessage(messages);
            long identity = Binder.clearCallingIdentity();
            Uri insert;
            try {
                insert = this.mContext.getContentResolver().insert(Inbox.CONTENT_URI, values);
                return insert;
            } catch (Exception e2) {
                insert = "Failed to persist inbox message";
                loge(insert, e2);
                return null;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } catch (Exception e3) {
            Rlog.d(TAG, "writeInboxMessage--exception");
        }
    }

    private static ContentValues parseSmsMessage(SmsMessage[] msgs) {
        int i = 0;
        SmsMessage sms = msgs[0];
        ContentValues values = new ContentValues();
        values.put("address", sms.getDisplayOriginatingAddress());
        values.put("body", buildMessageBodyFromPdus(msgs));
        values.put("date_sent", Long.valueOf(sms.getTimestampMillis()));
        values.put(Calls.DATE, Long.valueOf(System.currentTimeMillis()));
        values.put("protocol", Integer.valueOf(sms.getProtocolIdentifier()));
        values.put("seen", Integer.valueOf(0));
        values.put("read", Integer.valueOf(0));
        String subject = sms.getPseudoSubject();
        if (!TextUtils.isEmpty(subject)) {
            values.put("subject", subject);
        }
        String str = "reply_path_present";
        if (sms.isReplyPathPresent()) {
            i = 1;
        }
        values.put(str, Integer.valueOf(i));
        values.put("service_center", sms.getServiceCenterAddress());
        return values;
    }

    private static String buildMessageBodyFromPdus(SmsMessage[] msgs) {
        int i = 0;
        if (msgs.length == 1) {
            return replaceFormFeeds(msgs[0].getDisplayMessageBody());
        }
        StringBuilder body = new StringBuilder();
        int length = msgs.length;
        while (i < length) {
            body.append(msgs[i].getDisplayMessageBody());
            i++;
        }
        return replaceFormFeeds(body.toString());
    }

    private static String replaceFormFeeds(String s) {
        return s == null ? SpnOverride.MVNO_TYPE_NONE : s.replace(12, 10);
    }

    public WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    public int getWakeLockTimeout() {
        return this.mWakeLockTimeout;
    }

    private void setWakeLockTimeout(int timeOut) {
        this.mWakeLockTimeout = timeOut;
    }

    static void registerNewMessageNotificationActionHandler(Context context) {
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction(ACTION_OPEN_SMS_APP);
        context.registerReceiver(new NewMessageNotificationActionReceiver(), userFilter);
    }

    public static boolean isInBlackLists(Context context, String number) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.withAppendedPath(URI_BLACKLIST_BLOCK_SMS_AND_CALL, number), null, "block_type=1 OR block_type=3", null, null);
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
            boolean z = cursor.getCount() > 0;
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
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
            return false;
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

    public static boolean isColorOsVersion3X() {
        String romversion = SystemProperties.get("ro.build.version.opporom");
        if (romversion == null || romversion.length() == 0) {
            return false;
        }
        return romversion.startsWith("V3.");
    }

    private void removeExpiredPdu() {
        synchronized (this.mSmsStampWithPdu) {
            long beginCheckPeriod = System.currentTimeMillis() - 300000;
            Iterator<Entry<Long, byte[]>> iter = this.mSmsStampWithPdu.entrySet().iterator();
            while (iter.hasNext()) {
                if (((Long) ((Entry) iter.next()).getKey()).longValue() < beginCheckPeriod) {
                    iter.remove();
                }
            }
        }
        log("mSmsStampWithPdu has , phoneid=" + (this.mPhone == null ? -1 : this.mPhone.getPhoneId()) + "  size=" + this.mSmsStampWithPdu.size() + " items after removeExpiredItem, isLastStorageFull=" + this.isLastStorageFull);
    }

    private boolean checkPdu(byte[] newPdu) {
        if (newPdu == null) {
            return false;
        }
        for (Entry<Long, byte[]> entry : this.mSmsStampWithPdu.entrySet()) {
            byte[] oldPdu = (byte[]) entry.getValue();
            if (newPdu != null && oldPdu != null && Arrays.equals(newPdu, oldPdu)) {
                log("find a same sms, filter it");
                return true;
            }
        }
        boolean isMemAvailable = this.mStorageMonitor == null ? false : this.mStorageMonitor.isStorageAvailable();
        synchronized (this.mSmsStampWithPdu) {
            if (isMemAvailable) {
                log("mem not full, not find a same sms, save it");
                this.mSmsStampWithPdu.put(Long.valueOf(System.currentTimeMillis()), newPdu);
            }
        }
        return false;
    }

    private boolean containDupSmsInner(byte[] newPdu) {
        try {
            boolean checkPdu;
            synchronized (this.mSmsStampWithPdu) {
                removeExpiredPdu();
                checkPdu = checkPdu(newPdu);
            }
            return checkPdu;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean containDupSms(SmsMessageBase sms) {
        return false;
    }

    private static void oemShowDialogSmsMMs(Context context, Context phoneContext) {
        Message msg = Message.obtain();
        msg.what = 1;
        if (mUiHandler == null) {
            Rlog.e(NotificationChannelController.CHANNEL_ID_SMS, "warning:oemShowDialogSmsMMs mUiHandler==null");
        } else {
            mUiHandler.sendMessage(msg);
        }
    }

    private static void oemShowDialogSmsMMs(Context context) {
        oemShowDialogSmsMMs(context, context);
    }

    private void initUIHandler(Context context, Context phoneContext) {
        if (!(context == null || phoneContext == null || context == phoneContext)) {
            Rlog.v(NotificationChannelController.CHANNEL_ID_SMS, "Context != phone.getContext()");
        }
        if (context != null) {
            mUiHandler = new UIHandler(context);
            Rlog.v(NotificationChannelController.CHANNEL_ID_SMS, "initUIHandler1");
        } else if (phoneContext != null) {
            mUiHandler = new UIHandler(phoneContext);
            Rlog.v(NotificationChannelController.CHANNEL_ID_SMS, "initUIHandler2");
        }
    }

    public static void oemSetDefaultSms(Context context, Context phoneContext) {
        if (context == null) {
            try {
                Rlog.e(NotificationChannelController.CHANNEL_ID_SMS, "oemSetDefaultSms error, context == null!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isDefaultMmsRegion(context)) {
            Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "isDefaultMmsRegion-true");
        } else {
            ComponentName lastSmsCompName = SmsApplication.getDefaultSmsApplication(context, true);
            if (lastSmsCompName == null) {
                Rlog.e(NotificationChannelController.CHANNEL_ID_SMS, "lastSmsCompName == null");
                SmsApplication.setDefaultApplication(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, context);
                oemShowDialogSmsMMs(context, phoneContext);
                return;
            }
            String lastSmsPackage = lastSmsCompName.getPackageName();
            Rlog.e(NotificationChannelController.CHANNEL_ID_SMS, "lastSmsPackage=" + lastSmsPackage);
            if (!(lastSmsPackage == null || (lastSmsPackage.equals(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE) ^ 1) == 0) || TextUtils.isEmpty(lastSmsPackage)) {
                SmsApplication.setDefaultApplication(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, context);
                oemShowDialogSmsMMs(context, phoneContext);
            }
        }
    }

    public static void oemSetDefaultWappush(Context context) {
        if (context == null) {
            try {
                Rlog.e(NotificationChannelController.CHANNEL_ID_SMS, "oemSetDefaultWappush error, context == null!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isDefaultMmsRegion(context)) {
            Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "isDefaultMmsRegion-true");
        } else {
            ComponentName lastWappushsCompName = SmsApplication.getDefaultMmsApplication(context, true);
            if (lastWappushsCompName == null) {
                Rlog.e(NotificationChannelController.CHANNEL_ID_SMS, "lastWappushsCompName == null");
                SmsApplication.setDefaultApplication(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, context);
                oemShowDialogSmsMMs(context);
                return;
            }
            String lastWappushPackage = lastWappushsCompName.getPackageName();
            Rlog.e(NotificationChannelController.CHANNEL_ID_SMS, "lastWappushPackage=" + lastWappushPackage);
            if (!(lastWappushPackage == null || (lastWappushPackage.equals(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE) ^ 1) == 0) || TextUtils.isEmpty(lastWappushPackage)) {
                SmsApplication.setDefaultApplication(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, context);
                oemShowDialogSmsMMs(context);
            }
        }
    }

    /* JADX WARNING: Missing block: B:6:0x000c, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getBlockSmsTypeOem(Context context, int destPort, String senderNumber) {
        try {
            if (!(TextUtils.isEmpty(senderNumber) || destPort == 2948 || context == null)) {
                String tmpAddress = senderNumber;
                if (senderNumber != null && senderNumber.length() == 13 && senderNumber.charAt(0) != '+' && senderNumber.startsWith("861")) {
                    senderNumber = "+" + senderNumber;
                }
                boolean isNumberBlocked = isInBlackLists(context, senderNumber);
                Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "sms isNumberBlocked=" + isNumberBlocked + " senderNumber=" + senderNumber);
                if (isNumberBlocked) {
                    return 0;
                }
                if (OemConstant.isPoliceVersion(this.mPhone) || OemConstant.isDeviceLockVersion()) {
                    boolean isPolicyMessageReceEnable = OemConstant.isSmsReceiveEnable(this.mPhone);
                    Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "isPolicyMessageReceEnable=" + isPolicyMessageReceEnable);
                    if (!isPolicyMessageReceEnable) {
                        return 1;
                    }
                }
            }
        } catch (Exception e) {
            Rlog.e(NotificationChannelController.CHANNEL_ID_SMS, "need check the reason, sms framework");
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean isDefaultMmsRegion(Context context) {
        if (context == null) {
            return false;
        }
        try {
            if (context.getPackageManager().hasSystemFeature("oppo.version.exp")) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void dispatchSmsDeliveryIntentOem(byte[][] pdus, String format, int destPort, SmsBroadcastReceiver resultReceiver) {
        Intent intent = new Intent();
        intent.putExtra("pdus", pdus);
        intent.putExtra("format", format);
        intent.putExtra("isBlacklist", true);
        if (destPort == -1) {
            intent.setAction(OPPO_BLOCK_SMS_DELIVER_ACTION);
            ComponentName componentName = new ComponentName(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, OEM_DEFAULT_SYSTEM_SMS_DELIVER_CLASS);
            if (componentName != null) {
                intent.setComponent(componentName);
                log("OEM Delivering blocked SMS to: " + componentName.getPackageName() + " " + componentName.getClassName());
            } else {
                intent.setComponent(null);
            }
            if (SmsManager.getDefault().getAutoPersisting()) {
                Uri uri = writeInboxMessage(intent);
                if (uri != null) {
                    intent.putExtra("uri", uri.toString());
                }
            }
            if (this.mPhone.getAppSmsManager().handleSmsReceivedIntent(intent)) {
                dropSms(resultReceiver);
                return;
            }
        }
        intent.setAction("android.intent.action.DATA_SMS_RECEIVED");
        intent.setData(Uri.parse("sms://localhost:" + destPort));
        intent.setComponent(null);
        intent.addFlags(QtiCallConstants.CAPABILITY_SUPPORTS_DOWNGRADE_TO_VOICE_REMOTE);
        dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, handleSmsWhitelisting(intent.getComponent()), resultReceiver, UserHandle.SYSTEM);
    }
}
