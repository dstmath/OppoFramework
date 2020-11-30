package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.BroadcastOptions;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.IDeviceIdleController;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CarrierServicesSmsFilter;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.SmsConstants;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.util.NotificationChannelController;
import com.android.internal.util.HexDump;
import com.android.internal.util.State;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class InboundSmsHandler extends AbstractInboundSmsHandler {
    private static String ACTION_OPEN_SMS_APP = "com.android.internal.telephony.OPEN_DEFAULT_SMS_APP";
    public static final int ADDRESS_COLUMN = 6;
    public static final int COUNT_COLUMN = 5;
    public static final int DATE_COLUMN = 3;
    protected static final boolean DBG = true;
    public static final int DELETED_FLAG_COLUMN = 10;
    public static final int DESTINATION_PORT_COLUMN = 2;
    public static final int DISPLAY_ADDRESS_COLUMN = 9;
    protected static final int EVENT_BROADCAST_COMPLETE = 3;
    public static final int EVENT_BROADCAST_SMS = 2;
    public static final int EVENT_INJECT_SMS = 7;
    public static final int EVENT_NEW_SMS = 1;
    private static final int EVENT_RELEASE_WAKELOCK = 5;
    protected static final int EVENT_RETURN_TO_IDLE = 4;
    public static final int EVENT_START_ACCEPTING_SMS = 6;
    public static final int EVENT_UPDATE_TRACKER = 8;
    public static final int ID_COLUMN = 7;
    public static final int MESSAGE_BODY_COLUMN = 8;
    private static final int NOTIFICATION_ID_NEW_MESSAGE = 1;
    private static final String NOTIFICATION_TAG = "InboundSmsHandler";
    public static final int PDU_COLUMN = 0;
    protected static final String[] PDU_DELETED_FLAG_PROJECTION = {"pdu", "deleted"};
    private static final Map<Integer, Integer> PDU_DELETED_FLAG_PROJECTION_INDEX_MAPPING = new HashMap<Integer, Integer>() {
        /* class com.android.internal.telephony.InboundSmsHandler.AnonymousClass1 */

        {
            put(0, 0);
            put(10, 1);
        }
    };
    protected static final String[] PDU_SEQUENCE_PORT_PROJECTION = {"pdu", "sequence", "destination_port", "display_originating_addr", "date"};
    protected static final Map<Integer, Integer> PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING = new HashMap<Integer, Integer>() {
        /* class com.android.internal.telephony.InboundSmsHandler.AnonymousClass2 */

        {
            put(0, 0);
            put(1, 1);
            put(2, 2);
            put(9, 3);
            put(3, 4);
        }
    };
    public static final int REFERENCE_NUMBER_COLUMN = 4;
    public static final String SELECT_BY_ID = "_id=?";
    public static final int SEQUENCE_COLUMN = 1;
    protected static final boolean VDBG = false;
    private static final int WAKELOCK_TIMEOUT = 3000;
    public static final Uri sRawUri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "raw");
    public static final Uri sRawUriPermanentDelete = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "raw/permanentDelete");
    protected final int DELETE_PERMANENTLY = 1;
    protected final int MARK_DELETED = 2;
    @UnsupportedAppUsage
    protected CellBroadcastHandler mCellBroadcastHandler;
    @UnsupportedAppUsage
    protected final Context mContext;
    protected DefaultState mDefaultState = new DefaultState();
    @UnsupportedAppUsage
    protected DeliveringState mDeliveringState = new DeliveringState();
    @UnsupportedAppUsage
    IDeviceIdleController mDeviceIdleController;
    @UnsupportedAppUsage
    protected IdleState mIdleState = new IdleState();
    private boolean mLastSmsWasInjected = false;
    private LocalLog mLocalLog = new LocalLog(64);
    protected TelephonyMetrics mMetrics = TelephonyMetrics.getInstance();
    @UnsupportedAppUsage
    protected Phone mPhone;
    @UnsupportedAppUsage
    protected final ContentResolver mResolver;
    private final boolean mSmsReceiveDisabled;
    protected StartupState mStartupState = new StartupState();
    public SmsStorageMonitor mStorageMonitor;
    @UnsupportedAppUsage
    protected UserManager mUserManager;
    @UnsupportedAppUsage
    protected WaitingState mWaitingState = new WaitingState();
    @UnsupportedAppUsage
    private final PowerManager.WakeLock mWakeLock;
    private int mWakeLockTimeout;
    @UnsupportedAppUsage
    protected final WapPushOverSms mWapPush;

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public abstract void acknowledgeLastIncomingSms(boolean z, int i, Message message);

    /* access modifiers changed from: protected */
    public abstract int dispatchMessageRadioSpecific(SmsMessageBase smsMessageBase);

    /* access modifiers changed from: protected */
    public abstract boolean is3gpp2();

    protected InboundSmsHandler(String name, Context context, SmsStorageMonitor storageMonitor, Phone phone, CellBroadcastHandler cellBroadcastHandler) {
        super(name, context, phone);
        this.mContext = context;
        this.mStorageMonitor = storageMonitor;
        this.mPhone = phone;
        this.mCellBroadcastHandler = cellBroadcastHandler;
        this.mResolver = context.getContentResolver();
        this.mWapPush = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName()).makeWapPushOverSms(context);
        this.mSmsReceiveDisabled = !TelephonyManager.from(this.mContext).getSmsReceiveCapableForPhone(this.mPhone.getPhoneId(), this.mContext.getResources().getBoolean(17891522));
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, name);
        this.mWakeLock.acquire();
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mDeviceIdleController = TelephonyComponentFactory.getInstance().inject(IDeviceIdleController.class.getName()).getIDeviceIdleController();
        addState(this.mDefaultState);
        addState(this.mStartupState, this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mDeliveringState, this.mDefaultState);
        addState(this.mWaitingState, this.mDeliveringState);
        setInitialState(this.mStartupState);
        log("created InboundSmsHandler");
    }

    public void dispose() {
        quit();
    }

    /* access modifiers changed from: protected */
    public void onQuitting() {
        this.mWapPush.dispose();
        while (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    @UnsupportedAppUsage
    public Phone getPhone() {
        return this.mPhone;
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            InboundSmsHandler.this.loge("processMessage: unhandled message type " + msg.what + " currState=" + InboundSmsHandler.this.getCurrentState().getName());
            return true;
        }
    }

    private class StartupState extends State {
        private StartupState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Startup state");
            InboundSmsHandler.this.setWakeLockTimeout(0);
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
            inboundSmsHandler.log("StartupState.processMessage:" + msg.what);
            int i = msg.what;
            if (!(i == 1 || i == 2)) {
                if (i == 6) {
                    InboundSmsHandler inboundSmsHandler2 = InboundSmsHandler.this;
                    inboundSmsHandler2.transitionTo(inboundSmsHandler2.mIdleState);
                    return true;
                } else if (i != 7) {
                    return false;
                }
            }
            InboundSmsHandler.this.deferMessage(msg);
            return true;
        }
    }

    private class IdleState extends State {
        private IdleState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Idle state");
            InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
            inboundSmsHandler.sendMessageDelayed(5, (long) inboundSmsHandler.getWakeLockTimeout());
        }

        public void exit() {
            InboundSmsHandler.this.mWakeLock.acquire();
            InboundSmsHandler.this.log("acquired wakelock, leaving Idle state");
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
            inboundSmsHandler.log("IdleState.processMessage:" + msg.what);
            InboundSmsHandler inboundSmsHandler2 = InboundSmsHandler.this;
            inboundSmsHandler2.log("Idle state processing message type " + msg.what);
            int i = msg.what;
            if (!(i == 1 || i == 2)) {
                if (i == 4) {
                    return true;
                }
                if (i == 5) {
                    InboundSmsHandler.this.mWakeLock.release();
                    if (InboundSmsHandler.this.mWakeLock.isHeld()) {
                        InboundSmsHandler.this.log("mWakeLock is still held after release");
                    } else {
                        InboundSmsHandler.this.log("mWakeLock released");
                    }
                    return true;
                } else if (i != 7) {
                    return false;
                }
            }
            InboundSmsHandler.this.deferMessage(msg);
            InboundSmsHandler inboundSmsHandler3 = InboundSmsHandler.this;
            inboundSmsHandler3.transitionTo(inboundSmsHandler3.mDeliveringState);
            return true;
        }
    }

    private class DeliveringState extends State {
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
            int i = msg.what;
            if (i == 1) {
                InboundSmsHandler.this.handleNewSms((AsyncResult) msg.obj);
                InboundSmsHandler.this.sendMessage(4);
                return true;
            } else if (i == 2) {
                if (InboundSmsHandler.this.processMessagePart((InboundSmsTracker) msg.obj)) {
                    InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
                    inboundSmsHandler.sendMessage(inboundSmsHandler.obtainMessage(8, msg.obj));
                    InboundSmsHandler inboundSmsHandler2 = InboundSmsHandler.this;
                    inboundSmsHandler2.transitionTo(inboundSmsHandler2.mWaitingState);
                } else {
                    InboundSmsHandler.this.log("No broadcast sent on processing EVENT_BROADCAST_SMS in Delivering state. Return to Idle state");
                    InboundSmsHandler.this.sendMessage(4);
                }
                return true;
            } else if (i == 4) {
                InboundSmsHandler inboundSmsHandler3 = InboundSmsHandler.this;
                inboundSmsHandler3.transitionTo(inboundSmsHandler3.mIdleState);
                return true;
            } else if (i == 5) {
                InboundSmsHandler.this.mWakeLock.release();
                if (!InboundSmsHandler.this.mWakeLock.isHeld()) {
                    InboundSmsHandler.this.loge("mWakeLock released while delivering/broadcasting!");
                }
                return true;
            } else if (i == 7) {
                InboundSmsHandler.this.handleInjectSms((AsyncResult) msg.obj);
                InboundSmsHandler.this.sendMessage(4);
                return true;
            } else if (i != 8) {
                String errorMsg = "Unhandled msg in delivering state, msg.what = " + msg.what;
                InboundSmsHandler.this.loge(errorMsg);
                InboundSmsHandler.this.mLocalLog.log(errorMsg);
                return false;
            } else {
                InboundSmsHandler.this.logd("process tracker message in DeliveringState " + msg.arg1);
                return true;
            }
        }
    }

    private class WaitingState extends State {
        private InboundSmsTracker mLastDeliveredSmsTracker;

        private WaitingState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Waiting state");
        }

        public void exit() {
            InboundSmsHandler.this.log("exiting Waiting state");
            InboundSmsHandler.this.setWakeLockTimeout(InboundSmsHandler.WAKELOCK_TIMEOUT);
            InboundSmsHandler.this.mPhone.getIccSmsInterfaceManager().mDispatchersController.sendEmptyMessage(17);
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("WaitingState.processMessage:" + msg.what);
            int i = msg.what;
            if (i == 2) {
                if (this.mLastDeliveredSmsTracker != null) {
                    String str = "Defer sms broadcast due to undelivered sms,  messageCount = " + this.mLastDeliveredSmsTracker.getMessageCount() + " destPort = " + this.mLastDeliveredSmsTracker.getDestPort() + " timestamp = " + this.mLastDeliveredSmsTracker.getTimestamp() + " currentTimestamp = " + System.currentTimeMillis();
                    InboundSmsHandler.this.logd(str);
                    InboundSmsHandler.this.mLocalLog.log(str);
                }
                InboundSmsHandler.this.deferMessage(msg);
                return true;
            } else if (i == 3) {
                this.mLastDeliveredSmsTracker = null;
                InboundSmsHandler.this.sendMessage(4);
                InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
                inboundSmsHandler.transitionTo(inboundSmsHandler.mDeliveringState);
                return true;
            } else if (i == 4) {
                return true;
            } else {
                if (i != 8) {
                    return false;
                }
                this.mLastDeliveredSmsTracker = (InboundSmsTracker) msg.obj;
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void handleNewSms(AsyncResult ar) {
        int result;
        try {
            if (ar.exception != null) {
                loge("Exception processing incoming SMS: " + ar.exception);
                return;
            }
            boolean handled = false;
            try {
                this.mLastSmsWasInjected = false;
                result = dispatchMessage(((SmsMessage) ar.result).mWrappedSmsMessage);
            } catch (RuntimeException ex) {
                loge("Exception dispatching message", ex);
                result = 2;
            }
            if (result != -1) {
                if (result == 1) {
                    handled = true;
                }
                notifyAndAcknowledgeLastIncomingSms(handled, result, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void handleInjectSms(AsyncResult ar) {
        int result;
        SmsDispatchersController.SmsInjectionCallback callback = null;
        try {
            callback = (SmsDispatchersController.SmsInjectionCallback) ar.userObj;
            SmsMessage sms = (SmsMessage) ar.result;
            if (sms == null) {
                result = 2;
            } else {
                this.mLastSmsWasInjected = true;
                result = dispatchMessage(sms.mWrappedSmsMessage);
            }
        } catch (RuntimeException ex) {
            try {
                loge("Exception dispatching message", ex);
                result = 2;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        if (callback != null) {
            callback.onSmsInjectedResult(result);
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
                onlyCore = IPackageManager.Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
            } catch (RemoteException e) {
            }
            if (onlyCore) {
                log("Received a short message in encrypted state. Rejecting.");
                return 2;
            }
            int result = dispatchMessageRadioSpecific(smsb);
            if (result != 1) {
                this.mMetrics.writeIncomingSmsError(this.mPhone.getPhoneId(), this.mLastSmsWasInjected, result);
            }
            return result;
        }
    }

    private void notifyAndAcknowledgeLastIncomingSms(boolean success, int result, Message response) {
        if (!success) {
            Intent intent = new Intent("android.provider.Telephony.SMS_REJECTED");
            intent.putExtra("result", result);
            intent.addFlags(16777216);
            this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
        }
        acknowledgeLastIncomingSms(success, result, response);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public int dispatchNormalMessage(SmsMessageBase sms) {
        InboundSmsTracker tracker;
        SmsHeader smsHeader = sms.getUserDataHeader();
        if (smsHeader == null || smsHeader.concatRef == null) {
            int destPort = -1;
            if (!(smsHeader == null || smsHeader.portAddrs == null)) {
                destPort = smsHeader.portAddrs.destPort;
                log("destination port: " + destPort);
            }
            tracker = TelephonyComponentFactory.getInstance().inject(InboundSmsTracker.class.getName()).makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), destPort, is3gpp2(), false, sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), sms.getMessageBody(), sms.getMessageClass() == SmsConstants.MessageClass.CLASS_0);
        } else {
            SmsHeader.ConcatRef concatRef = smsHeader.concatRef;
            SmsHeader.PortAddrs portAddrs = smsHeader.portAddrs;
            tracker = TelephonyComponentFactory.getInstance().inject(InboundSmsTracker.class.getName()).makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), portAddrs != null ? portAddrs.destPort : -1, is3gpp2(), sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), concatRef.refNumber, concatRef.seqNumber, concatRef.msgCount, false, sms.getMessageBody(), sms.getMessageClass() == SmsConstants.MessageClass.CLASS_0);
        }
        return addTrackerToRawTableAndSendMessage(tracker, tracker.getDestPort() == -1);
    }

    /* access modifiers changed from: protected */
    public int addTrackerToRawTableAndSendMessage(InboundSmsTracker tracker, boolean deDup) {
        int addTrackerToRawTable = addTrackerToRawTable(tracker, deDup);
        if (addTrackerToRawTable != 1) {
            return addTrackerToRawTable != 5 ? 2 : 1;
        }
        sendMessage(2, tracker);
        return 1;
    }

    /* JADX INFO: Multiple debug info for r6v2 com.android.internal.telephony.InboundSmsHandler$SmsBroadcastReceiver: [D('resultReceiver' com.android.internal.telephony.InboundSmsHandler$SmsBroadcastReceiver), D('pduList' java.util.List<byte[]>)] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x0397  */
    /* JADX WARNING: Removed duplicated region for block: B:134:0x039f  */
    /* JADX WARNING: Removed duplicated region for block: B:147:? A[RETURN, SYNTHETIC] */
    @UnsupportedAppUsage
    public boolean processMessagePart(InboundSmsTracker tracker) {
        long[] timestamps;
        byte[][] pdus;
        boolean block;
        int destPort;
        int messageCount;
        List<byte[]> pduList;
        SQLException e;
        SQLException e2;
        int port;
        int i = 1;
        try {
            int messageCount2 = tracker.getMessageCount();
            int destPort2 = tracker.getDestPort();
            boolean block2 = false;
            String address = tracker.getAddress();
            int i2 = 0;
            if (messageCount2 <= 0) {
                loge("processMessagePart: returning false due to invalid message count " + messageCount2);
                return false;
            }
            if (messageCount2 == 1) {
                byte[][] pdus2 = {tracker.getPdu()};
                long[] timestamps2 = {tracker.getTimestamp()};
                destPort = destPort2;
                block = OppoRlog.BlockChecker.isBlocked(this.mContext, tracker.getDisplayAddress(), null);
                pdus = pdus2;
                timestamps = timestamps2;
            } else {
                Cursor cursor = null;
                try {
                    cursor = this.mResolver.query(sRawUri, PDU_SEQUENCE_PORT_PROJECTION, tracker.getQueryForSegments(), onModifyQueryWhereArgs(new String[]{address, Integer.toString(tracker.getReferenceNumber()), Integer.toString(tracker.getMessageCount())}), null);
                    if (cursor.getCount() < messageCount2) {
                        cursor.close();
                        return false;
                    }
                    byte[][] pdus3 = new byte[messageCount2][];
                    timestamps = new long[messageCount2];
                    while (cursor.moveToNext()) {
                        try {
                            int index = cursor.getInt(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(i)).intValue()) - tracker.getIndexOffset();
                            if (index < pdus3.length) {
                                if (index >= 0) {
                                    pdus3[index] = HexDump.hexStringToByteArray(cursor.getString(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(i2)).intValue()));
                                    if (index == 0 && !cursor.isNull(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(2).intValue()) && (port = InboundSmsTracker.getRealDestPort(cursor.getInt(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(2).intValue()))) != -1) {
                                        destPort2 = port;
                                    }
                                    timestamps[index] = cursor.getLong(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(3).intValue());
                                    if (!block2) {
                                        block2 = OppoRlog.BlockChecker.isBlocked(this.mContext, cursor.getString(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(9).intValue()), null);
                                    }
                                    i2 = 0;
                                    i = 1;
                                }
                            }
                            loge(String.format("processMessagePart: invalid seqNumber = %d, messageCount = %d", Integer.valueOf(tracker.getIndexOffset() + index), Integer.valueOf(messageCount2)));
                            i2 = 0;
                            i = 1;
                        } catch (SQLException e3) {
                            e2 = e3;
                            try {
                                loge("Can't access multipart SMS database", e2);
                                if (cursor != null) {
                                }
                            } catch (Throwable th) {
                                e = th;
                            }
                        } catch (Throwable th2) {
                            e = th2;
                            if (cursor != null) {
                            }
                            throw e;
                        }
                    }
                    cursor.close();
                    destPort = destPort2;
                    block = block2;
                    pdus = pdus3;
                } catch (SQLException e4) {
                    e2 = e4;
                    loge("Can't access multipart SMS database", e2);
                    if (cursor != null) {
                        return false;
                    }
                    cursor.close();
                    return false;
                } catch (Throwable th3) {
                    e = th3;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw e;
                }
            }
            String format = !tracker.is3gpp2() ? "3gpp" : "3gpp2";
            if (destPort != 2948) {
                messageCount = 2948;
                this.mMetrics.writeIncomingSmsSession(this.mPhone.getPhoneId(), this.mLastSmsWasInjected, format, timestamps, block);
            } else {
                messageCount = 2948;
            }
            List<byte[]> pduList2 = Arrays.asList(pdus);
            if (pduList2.size() == 0) {
                pduList = pduList2;
            } else if (pduList2.contains(null)) {
                pduList = pduList2;
            } else {
                SmsBroadcastReceiver resultReceiver = new SmsBroadcastReceiver(tracker);
                if (onCheckIfStopProcessMessagePart(pdus, tracker.getFormat(), tracker)) {
                    return false;
                }
                if (this.mUserManager.isUserUnlocked()) {
                    SmsBroadcastReceiver resultReceiver2 = resultReceiver;
                    if (tracker.isClass0() && this.mPhone.getServiceState().getOperatorNumeric().equals("26201")) {
                        log("sms class is 0");
                        deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 1);
                        return false;
                    } else if (destPort == messageCount || !oemDealWithCtImsSms(pdus, format, this.mContext)) {
                        if (destPort != messageCount && oemDealWithHealthcheckSms(pdus, format, this.mContext)) {
                            log("Healthcheck sms");
                        }
                        IOppoSmsManager manager = (IOppoSmsManager) OppoTelephonyFactory.getInstance().getFeature(IOppoSmsManager.DEFAULT, new Object[0]);
                        if (manager != null && manager.isSmsBlockByPolicy(this.mPhone)) {
                            deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 1);
                            return false;
                        } else if (destPort == messageCount) {
                            ByteArrayOutputStream output = new ByteArrayOutputStream();
                            int length = pdus.length;
                            int i3 = 0;
                            while (i3 < length) {
                                byte[] pdu = pdus[i3];
                                if (!tracker.is3gpp2()) {
                                    SmsMessage msg = onCreateSmsMessage(pdu, "3gpp");
                                    if (msg != null) {
                                        pdu = msg.getUserData();
                                    } else {
                                        loge("processMessagePart: SmsMessage.createFromPdu returned null");
                                        this.mMetrics.writeIncomingWapPush(this.mPhone.getPhoneId(), this.mLastSmsWasInjected, format, timestamps, false);
                                        return false;
                                    }
                                }
                                output.write(pdu, 0, pdu.length);
                                i3++;
                                resultReceiver2 = resultReceiver2;
                            }
                            int result = onDispatchWapPdu(pdus, output.toByteArray(), resultReceiver2, address);
                            log("dispatchWapPdu() returned " + result);
                            if (result == -1 || result == 1) {
                                this.mMetrics.writeIncomingWapPush(this.mPhone.getPhoneId(), this.mLastSmsWasInjected, format, timestamps, true);
                            } else {
                                this.mMetrics.writeIncomingWapPush(this.mPhone.getPhoneId(), this.mLastSmsWasInjected, format, timestamps, false);
                            }
                            if (result == -1) {
                                return true;
                            }
                            deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
                            return false;
                        } else if (block) {
                            romDispatchSmsDeliveryIntent(pdus, tracker.getFormat(), destPort, resultReceiver2, tracker.isClass0(), block);
                            return true;
                        } else {
                            boolean filterInvoked = filterSms(pdus, destPort, tracker, resultReceiver2, true);
                            log("filterInvoked=" + filterInvoked);
                            if (filterInvoked) {
                                return true;
                            }
                            dispatchSmsDeliveryIntent(pdus, tracker.getFormat(), destPort, resultReceiver2, tracker.isClass0());
                            return true;
                        }
                    } else {
                        log("mt ct ims auto sms");
                        deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 1);
                        return false;
                    }
                } else if (romProcessMessagePartWithUserLocked(block, this.mWapPush, pdus, destPort, this)) {
                    return false;
                } else {
                    return processMessagePartWithUserLocked(tracker, pdus, destPort, resultReceiver);
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("processMessagePart: returning false due to ");
            sb.append(pduList.size() == 0 ? "pduList.size() == 0" : "pduList.contains(null)");
            String errorMsg = sb.toString();
            loge(errorMsg);
            this.mLocalLog.log(errorMsg);
            return false;
        } catch (Exception e5) {
            e5.printStackTrace();
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean processMessagePartWithUserLocked(InboundSmsTracker tracker, byte[][] pdus, int destPort, SmsBroadcastReceiver resultReceiver) {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void showNewMessageNotification() {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            log("Show new message notification.");
            ((NotificationManager) this.mContext.getSystemService("notification")).notify(NOTIFICATION_TAG, 1, new Notification.Builder(this.mContext).setSmallIcon(17301646).setAutoCancel(true).setVisibility(1).setDefaults(-1).setContentTitle(this.mContext.getString(17040469)).setContentText(this.mContext.getString(17040468)).setContentIntent(PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_OPEN_SMS_APP), 1140850688)).setChannelId(NotificationChannelController.CHANNEL_ID_SMS).build());
        }
    }

    static void cancelNewMessageNotification(Context context) {
        ((NotificationManager) context.getSystemService("notification")).cancel(NOTIFICATION_TAG, 1);
    }

    /* access modifiers changed from: protected */
    public boolean filterSms(byte[][] pdus, int destPort, InboundSmsTracker tracker, SmsBroadcastReceiver resultReceiver, boolean userUnlocked) {
        try {
            if (new CarrierServicesSmsFilter(this.mContext, this.mPhone, pdus, destPort, tracker.getFormat(), new CarrierServicesSmsFilterCallback(pdus, destPort, tracker.getFormat(), resultReceiver, userUnlocked, tracker.isClass0()), getName(), this.mLocalLog).oemFilter(tracker.getAddress())) {
                return true;
            }
            if (!VisualVoicemailSmsFilter.filter(this.mContext, pdus, tracker.getFormat(), destPort, this.mPhone.getSubId())) {
                return false;
            }
            log("Visual voicemail SMS dropped");
            dropSms(resultReceiver);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @UnsupportedAppUsage
    public void dispatchIntent(Intent intent, String permission, int appOp, Bundle opts, BroadcastReceiver resultReceiver, UserHandle user) {
        int[] users;
        intent.addFlags(134217728);
        oemRemoveAbortFlag(intent, user);
        String action = intent.getAction();
        if ("android.provider.Telephony.SMS_DELIVER".equals(action) || "android.provider.Telephony.SMS_RECEIVED".equals(action) || "android.provider.Telephony.WAP_PUSH_DELIVER".equals(action) || "android.provider.Telephony.WAP_PUSH_RECEIVED".equals(action)) {
            intent.addFlags(268435456);
        }
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        if (user.equals(UserHandle.ALL)) {
            int[] users2 = null;
            try {
                users2 = ActivityManager.getService().getRunningUserIds();
            } catch (RemoteException e) {
            }
            int[] users3 = users2 == null ? new int[]{user.getIdentifier()} : users2;
            int i = users3.length - 1;
            while (i >= 0) {
                UserHandle targetUser = new UserHandle(users3[i]);
                if (users3[i] != 0) {
                    if (this.mUserManager.hasUserRestriction("no_sms", targetUser)) {
                        log("hasUserRestriction," + users3[i]);
                        users = users3;
                    } else {
                        UserInfo info = this.mUserManager.getUserInfo(users3[i]);
                        if (info == null || info.isManagedProfile()) {
                            log("unknown user," + users3[i]);
                            users = users3;
                        }
                    }
                    i--;
                    users3 = users;
                }
                log("s1");
                users = users3;
                this.mContext.sendOrderedBroadcastAsUser(intent, targetUser, permission, appOp, opts, users3[i] == 0 ? resultReceiver : null, getHandler(), -1, null, null);
                i--;
                users3 = users;
            }
            return;
        }
        log("send to," + user);
        this.mContext.sendOrderedBroadcastAsUser(intent, user, permission, appOp, opts, resultReceiver, getHandler(), -1, null, null);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void deleteFromRawTable(String deleteWhere, String[] deleteWhereArgs, int deleteType) {
        int rows = this.mResolver.delete(deleteType == 1 ? sRawUriPermanentDelete : sRawUri, deleteWhere, deleteWhereArgs);
        if (rows == 0) {
            loge("No rows were deleted from raw table!");
            return;
        }
        log("Deleted " + rows + " rows from raw table.");
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public Bundle handleSmsWhitelisting(ComponentName target, boolean bgActivityStartAllowed) {
        String reason;
        String pkgName;
        if (target != null) {
            pkgName = target.getPackageName();
            reason = "sms-app";
        } else {
            pkgName = this.mContext.getPackageName();
            reason = "sms-broadcast";
        }
        BroadcastOptions bopts = null;
        Bundle bundle = null;
        if (bgActivityStartAllowed) {
            bopts = BroadcastOptions.makeBasic();
            bopts.setBackgroundActivityStartsAllowed(true);
            bundle = bopts.toBundle();
        }
        try {
            long duration = this.mDeviceIdleController.addPowerSaveTempWhitelistAppForSms(pkgName, 0, reason);
            if (bopts == null) {
                bopts = BroadcastOptions.makeBasic();
            }
            bopts.setTemporaryAppWhitelistDuration(duration);
            return bopts.toBundle();
        } catch (RemoteException e) {
            loge("handleSmsWhitelisting exception");
            return bundle;
        } catch (Exception ex) {
            ex.printStackTrace();
            return bundle;
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchSmsDeliveryIntent(byte[][] pdus, String format, int destPort, SmsBroadcastReceiver resultReceiver, boolean isClass0) {
        romDispatchSmsDeliveryIntent(pdus, format, destPort, resultReceiver, isClass0, false);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r10v0, resolved type: byte[][] */
    /* JADX WARN: Multi-variable type inference failed */
    public void romDispatchSmsDeliveryIntent(byte[][] pdus, String format, int destPort, SmsBroadcastReceiver resultReceiver, boolean isClass0, boolean block) {
        Uri uri;
        oemSetDefaultSms(this.mContext);
        Intent intent = new Intent();
        intent.putExtra("pdus", (Serializable) pdus);
        intent.putExtra("format", format);
        if (destPort == -1) {
            intent.setAction("android.provider.Telephony.SMS_DELIVER");
            ComponentName componentName = SmsApplication.getDefaultSmsApplication(this.mContext, true);
            ComponentName romComponentName = romDealWithMtSms(block, intent, componentName);
            if (romComponentName != null) {
                componentName = romComponentName;
            }
            if (componentName != null) {
                intent.setComponent(componentName);
                log("Delivering SMS to: " + componentName.getPackageName() + " " + componentName.getClassName());
            } else {
                intent.setComponent(null);
            }
            if (SmsManager.getDefault().getAutoPersisting() && (uri = writeInboxMessage(intent)) != null) {
                intent.putExtra("uri", uri.toString());
            }
            if (this.mPhone.getAppSmsManager().handleSmsReceivedIntent(intent)) {
                dropSms(resultReceiver);
                return;
            }
        } else {
            intent.setAction("android.intent.action.DATA_SMS_RECEIVED");
            intent.setData(Uri.parse("sms://localhost:" + destPort));
            intent.setComponent(null);
            intent.addFlags(16777216);
        }
        dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, handleSmsWhitelisting(intent.getComponent(), isClass0), resultReceiver, UserHandle.SYSTEM);
    }

    private boolean checkAndHandleDuplicate(InboundSmsTracker tracker) throws SQLException {
        if (tracker == null || tracker.getMessageBody() != null) {
            Pair<String, String[]> exactMatchQuery = tracker.getExactMatchDupDetectQuery();
            Cursor cursor = null;
            try {
                cursor = this.mResolver.query(sRawUri, PDU_DELETED_FLAG_PROJECTION, (String) exactMatchQuery.first, (String[]) exactMatchQuery.second, null);
                if (cursor != null && cursor.moveToNext()) {
                    if (cursor.getCount() != 1) {
                        loge("Exact match query returned " + cursor.getCount() + " rows");
                    }
                    if (cursor.getInt(PDU_DELETED_FLAG_PROJECTION_INDEX_MAPPING.get(10).intValue()) == 1) {
                        loge("Discarding duplicate message segment: " + tracker);
                        logDupPduMismatch(cursor, tracker);
                        cursor.close();
                        return true;
                    } else if (tracker.getMessageCount() == 1) {
                        deleteFromRawTable((String) exactMatchQuery.first, (String[]) exactMatchQuery.second, 1);
                        loge("Replacing duplicate message: " + tracker);
                        logDupPduMismatch(cursor, tracker);
                    }
                }
                if (tracker.getMessageCount() > 1) {
                    Pair<String, String[]> inexactMatchQuery = tracker.getInexactMatchDupDetectQuery();
                    Cursor cursor2 = null;
                    try {
                        cursor2 = this.mResolver.query(sRawUri, PDU_DELETED_FLAG_PROJECTION, (String) inexactMatchQuery.first, (String[]) inexactMatchQuery.second, null);
                        if (cursor2 != null && cursor2.moveToNext()) {
                            if (cursor2.getCount() != 1) {
                                loge("Inexact match query returned " + cursor2.getCount() + " rows");
                            }
                            deleteFromRawTable((String) inexactMatchQuery.first, (String[]) inexactMatchQuery.second, 1);
                            loge("Replacing duplicate message segment: " + tracker);
                            logDupPduMismatch(cursor2, tracker);
                        }
                    } finally {
                        if (cursor2 != null) {
                            cursor2.close();
                        }
                    }
                }
                return false;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            loge("checkAndHandleDuplicate, body null");
            return false;
        }
    }

    private void logDupPduMismatch(Cursor cursor, InboundSmsTracker tracker) {
        String oldPduString = cursor.getString(PDU_DELETED_FLAG_PROJECTION_INDEX_MAPPING.get(0).intValue());
        byte[] pdu = tracker.getPdu();
        byte[] oldPdu = HexDump.hexStringToByteArray(oldPduString);
        if (!Arrays.equals(oldPdu, tracker.getPdu())) {
            loge("Warning: dup message PDU of length " + pdu.length + " is different from existing PDU of length " + oldPdu.length);
        }
    }

    /* access modifiers changed from: protected */
    public int addTrackerToRawTable(InboundSmsTracker tracker, boolean deDup) {
        if (deDup) {
            try {
                if (checkAndHandleDuplicate(tracker)) {
                    return 5;
                }
            } catch (SQLException e) {
                loge("Can't access SMS database", e);
                return 2;
            } catch (Exception ex) {
                loge("allow 8bit sms", ex);
            }
        } else {
            logd("Skipped message de-duping logic");
        }
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

    public static boolean isCurrentFormat3gpp2() {
        return 2 == TelephonyManager.getDefault().getCurrentPhoneType();
    }

    /* access modifiers changed from: protected */
    public final class SmsBroadcastReceiver extends BroadcastReceiver {
        private long mBroadcastTimeNano = System.nanoTime();
        @UnsupportedAppUsage
        private final String mDeleteWhere;
        @UnsupportedAppUsage
        private final String[] mDeleteWhereArgs;

        public SmsBroadcastReceiver(InboundSmsTracker tracker) {
            this.mDeleteWhere = tracker.getDeleteWhere();
            this.mDeleteWhereArgs = tracker.getDeleteWhereArgs();
        }

        public void onReceive(Context context, Intent intent) {
            InboundSmsHandler.this.oemMtSmsCount(intent);
            String action = intent.getAction();
            if (action.equals("android.provider.Telephony.SMS_DELIVER")) {
                intent.setAction("android.provider.Telephony.SMS_RECEIVED");
                intent.addFlags(16777216);
                intent.setComponent(null);
                InboundSmsHandler.this.dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, InboundSmsHandler.this.handleSmsWhitelisting(null, false), this, UserHandle.ALL);
            } else if (action.equals("android.provider.Telephony.WAP_PUSH_DELIVER")) {
                intent.setAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
                intent.setComponent(null);
                intent.addFlags(16777216);
                Bundle options = null;
                try {
                    long duration = InboundSmsHandler.this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(InboundSmsHandler.this.mContext.getPackageName(), 0, "mms-broadcast");
                    BroadcastOptions bopts = BroadcastOptions.makeBasic();
                    bopts.setTemporaryAppWhitelistDuration(duration);
                    options = bopts.toBundle();
                } catch (RemoteException e) {
                    InboundSmsHandler.this.loge("onReceive exception");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                String mimeType = intent.getType();
                InboundSmsHandler.this.dispatchIntent(intent, WapPushOverSms.getPermissionForType(mimeType), WapPushOverSms.getAppOpsPermissionForIntent(mimeType), options, this, UserHandle.SYSTEM);
            } else {
                if (!"android.intent.action.DATA_SMS_RECEIVED".equals(action) && !"android.provider.Telephony.SMS_RECEIVED".equals(action) && !"android.intent.action.DATA_SMS_RECEIVED".equals(action) && !"android.provider.Telephony.WAP_PUSH_RECEIVED".equals(action)) {
                    InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
                    inboundSmsHandler.loge("unexpected BroadcastReceiver action: " + action);
                }
                int rc = getResultCode();
                if (rc == -1 || rc == 1) {
                    InboundSmsHandler.this.log("successful broadcast, deleting from raw table.");
                } else {
                    InboundSmsHandler inboundSmsHandler2 = InboundSmsHandler.this;
                    inboundSmsHandler2.loge("a broadcast receiver set the result code to " + rc + ", deleting from raw table anyway!");
                }
                InboundSmsHandler.this.deleteFromRawTable(this.mDeleteWhere, this.mDeleteWhereArgs, 2);
                InboundSmsHandler.this.sendMessage(3);
                int durationMillis = (int) ((System.nanoTime() - this.mBroadcastTimeNano) / 1000000);
                if (durationMillis >= 5000) {
                    InboundSmsHandler inboundSmsHandler3 = InboundSmsHandler.this;
                    inboundSmsHandler3.loge("Slow ordered broadcast completion time: " + durationMillis + " ms");
                    return;
                }
                InboundSmsHandler inboundSmsHandler4 = InboundSmsHandler.this;
                inboundSmsHandler4.log("ordered broadcast completed in: " + durationMillis + " ms");
            }
        }
    }

    public final class CarrierServicesSmsFilterCallback implements CarrierServicesSmsFilter.CarrierServicesSmsFilterCallbackInterface {
        private final int mDestPort;
        private final boolean mIsClass0;
        private final byte[][] mPdus;
        private final SmsBroadcastReceiver mSmsBroadcastReceiver;
        private final String mSmsFormat;
        private final boolean mUserUnlocked;

        public CarrierServicesSmsFilterCallback(byte[][] pdus, int destPort, String smsFormat, SmsBroadcastReceiver smsBroadcastReceiver, boolean userUnlocked, boolean isClass0) {
            this.mPdus = pdus;
            this.mDestPort = destPort;
            this.mSmsFormat = smsFormat;
            this.mSmsBroadcastReceiver = smsBroadcastReceiver;
            this.mUserUnlocked = userUnlocked;
            this.mIsClass0 = isClass0;
        }

        @Override // com.android.internal.telephony.CarrierServicesSmsFilter.CarrierServicesSmsFilterCallbackInterface
        public void onFilterComplete(int result) {
            InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
            inboundSmsHandler.logv("onFilterComplete: result is " + result);
            InboundSmsHandler inboundSmsHandler2 = InboundSmsHandler.this;
            inboundSmsHandler2.logv("mUserUnlocked=" + this.mUserUnlocked);
            if ((result & 1) != 0) {
                InboundSmsHandler.this.dropSms(this.mSmsBroadcastReceiver);
            } else if (VisualVoicemailSmsFilter.filter(InboundSmsHandler.this.mContext, this.mPdus, this.mSmsFormat, this.mDestPort, InboundSmsHandler.this.mPhone.getSubId())) {
                InboundSmsHandler.this.log("Visual voicemail SMS dropped");
                InboundSmsHandler.this.dropSms(this.mSmsBroadcastReceiver);
            } else if (this.mUserUnlocked) {
                InboundSmsHandler.this.dispatchSmsDeliveryIntent(this.mPdus, this.mSmsFormat, this.mDestPort, this.mSmsBroadcastReceiver, this.mIsClass0);
            } else {
                if (!InboundSmsHandler.this.isSkipNotifyFlagSet(result)) {
                    InboundSmsHandler.this.showNewMessageNotification();
                }
                InboundSmsHandler.this.sendMessage(3);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dropSms(SmsBroadcastReceiver receiver) {
        loge("dropSms");
        deleteFromRawTable(receiver.mDeleteWhere, receiver.mDeleteWhereArgs, 2);
        sendMessage(3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private boolean isSkipNotifyFlagSet(int callbackResult) {
        return (callbackResult & 2) > 0;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void log(String s) {
        OppoRlog.Rlog.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void loge(String s) {
        OppoRlog.Rlog.e(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s, Throwable e) {
        OppoRlog.Rlog.e(getName(), s, e);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public Uri writeInboxMessage(Intent intent) {
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (messages == null || messages.length < 1) {
            loge("Failed to parse SMS pdu");
            return null;
        }
        for (SmsMessage sms : messages) {
            if (sms == null) {
                loge("Can’t write null SmsMessage");
                return null;
            }
        }
        ContentValues values = parseSmsMessage(messages);
        long identity = Binder.clearCallingIdentity();
        try {
            return this.mContext.getContentResolver().insert(Telephony.Sms.Inbox.CONTENT_URI, values);
        } catch (Exception e) {
            loge("Failed to persist inbox message", e);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private static ContentValues parseSmsMessage(SmsMessage[] msgs) {
        SmsMessage sms = msgs[0];
        ContentValues values = new ContentValues();
        values.put("address", sms.getDisplayOriginatingAddress());
        values.put("body", buildMessageBodyFromPdus(msgs));
        values.put("date_sent", Long.valueOf(sms.getTimestampMillis()));
        values.put("date", Long.valueOf(System.currentTimeMillis()));
        values.put("protocol", Integer.valueOf(sms.getProtocolIdentifier()));
        values.put("seen", (Integer) 0);
        values.put("read", (Integer) 0);
        String subject = sms.getPseudoSubject();
        if (!TextUtils.isEmpty(subject)) {
            values.put("subject", subject);
        }
        values.put("reply_path_present", Integer.valueOf(sms.isReplyPathPresent() ? 1 : 0));
        values.put("service_center", sms.getServiceCenterAddress());
        return values;
    }

    private static String buildMessageBodyFromPdus(SmsMessage[] msgs) {
        if (msgs.length == 1) {
            return replaceFormFeeds(msgs[0].getDisplayMessageBody());
        }
        StringBuilder body = new StringBuilder();
        for (SmsMessage msg : msgs) {
            body.append(msg.getDisplayMessageBody());
        }
        return replaceFormFeeds(body.toString());
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        CellBroadcastHandler cellBroadcastHandler = this.mCellBroadcastHandler;
        if (cellBroadcastHandler != null) {
            cellBroadcastHandler.dump(fd, pw, args);
        }
        this.mLocalLog.dump(fd, pw, args);
    }

    private static String replaceFormFeeds(String s) {
        return s == null ? PhoneConfigurationManager.SSSS : s.replace('\f', '\n');
    }

    @VisibleForTesting
    public PowerManager.WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    @VisibleForTesting
    public int getWakeLockTimeout() {
        return this.mWakeLockTimeout;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setWakeLockTimeout(int timeOut) {
        this.mWakeLockTimeout = timeOut;
    }

    private static class NewMessageNotificationActionReceiver extends BroadcastReceiver {
        private NewMessageNotificationActionReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (InboundSmsHandler.ACTION_OPEN_SMS_APP.equals(intent.getAction()) && ((UserManager) context.getSystemService("user")).isUserUnlocked()) {
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage(Telephony.Sms.getDefaultSmsPackage(context)));
            }
        }
    }

    static void registerNewMessageNotificationActionHandler(Context context) {
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction(ACTION_OPEN_SMS_APP);
        context.registerReceiver(new NewMessageNotificationActionReceiver(), userFilter);
    }

    /* access modifiers changed from: protected */
    public String[] onModifyQueryWhereArgs(String[] whereArgs) {
        return whereArgs;
    }

    /* access modifiers changed from: protected */
    public boolean onCheckIfStopProcessMessagePart(byte[][] pdus, String format, InboundSmsTracker tracker) {
        return false;
    }

    /* access modifiers changed from: protected */
    public SmsMessage onCreateSmsMessage(byte[] pdu, String format) {
        return SmsMessage.createFromPdu(pdu, "3gpp");
    }

    /* access modifiers changed from: protected */
    public int onDispatchWapPdu(byte[][] smsPdus, byte[] pdu, BroadcastReceiver receiver, String address) {
        return this.mWapPush.dispatchWapPdu(pdu, receiver, this, address);
    }
}
