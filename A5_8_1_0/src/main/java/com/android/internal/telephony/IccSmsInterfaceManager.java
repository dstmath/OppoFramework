package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.UserManager;
import android.provider.oppo.CallLog.Calls;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.oem.SmsCbConfigInfo;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.HexDump;
import com.google.android.mms.pdu.PduHeaders;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IccSmsInterfaceManager {
    static final boolean DBG = true;
    private static final int EVENT_GET_BROADCAST_ACTIVATION_DONE = 108;
    private static final int EVENT_GET_BROADCAST_CONFIG_DONE = 107;
    private static final int EVENT_LOAD_DONE = 1;
    private static final int EVENT_REMOVE_BROADCAST_MSG_DONE = 109;
    protected static final int EVENT_SET_BROADCAST_ACTIVATION_DONE = 3;
    protected static final int EVENT_SET_BROADCAST_CONFIG_DONE = 4;
    private static final int EVENT_UPDATE_DONE = 2;
    static final String LOG_TAG = "IccSmsInterfaceManager";
    private static final int SMS_CB_CODE_SCHEME_MAX = 255;
    private static final int SMS_CB_CODE_SCHEME_MIN = 0;
    protected final AppOpsManager mAppOps;
    private CdmaBroadcastRangeManager mCdmaBroadcastRangeManager = new CdmaBroadcastRangeManager();
    private CellBroadcastRangeManager mCellBroadcastRangeManager = new CellBroadcastRangeManager();
    protected final Context mContext;
    protected SMSDispatcher mDispatcher;
    protected Handler mHandler = new Handler() {
        /* JADX WARNING: Missing block: B:8:0x0021, code:
            monitor-exit(r7);
     */
        /* JADX WARNING: Missing block: B:20:0x0054, code:
            monitor-exit(r6);
     */
        /* JADX WARNING: Missing block: B:95:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:96:?, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            boolean z = true;
            AsyncResult ar;
            Object obj;
            Object obj2;
            IccSmsInterfaceManager iccSmsInterfaceManager;
            switch (msg.what) {
                case 1:
                    ar = (AsyncResult) msg.obj;
                    obj = IccSmsInterfaceManager.this.mLock;
                    synchronized (obj) {
                        if (ar.exception == null) {
                            IccSmsInterfaceManager.this.mSms = IccSmsInterfaceManager.this.buildValidRawData((ArrayList) ar.result);
                            IccSmsInterfaceManager.this.markMessagesAsRead((ArrayList) ar.result);
                        } else {
                            if (Rlog.isLoggable("SMS", 3)) {
                                IccSmsInterfaceManager.this.log("Cannot load Sms records");
                            }
                            IccSmsInterfaceManager.this.mSms = null;
                        }
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    }
                case 2:
                    ar = msg.obj;
                    obj2 = IccSmsInterfaceManager.this.mLock;
                    synchronized (obj2) {
                        iccSmsInterfaceManager = IccSmsInterfaceManager.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        iccSmsInterfaceManager.mSuccess = z;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    }
                case 3:
                case 4:
                    ar = (AsyncResult) msg.obj;
                    obj2 = IccSmsInterfaceManager.this.mLock;
                    synchronized (obj2) {
                        iccSmsInterfaceManager = IccSmsInterfaceManager.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        iccSmsInterfaceManager.mSuccess = z;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    }
                case IccSmsInterfaceManager.EVENT_GET_BROADCAST_CONFIG_DONE /*107*/:
                    ar = (AsyncResult) msg.obj;
                    obj = IccSmsInterfaceManager.this.mLock;
                    synchronized (obj) {
                        if (ar.exception == null) {
                            ArrayList<SmsBroadcastConfigInfo> mList = ar.result;
                            if (mList.size() != 0) {
                                IccSmsInterfaceManager.this.mSmsCBConfig = new SmsBroadcastConfigInfo[mList.size()];
                                mList.toArray(IccSmsInterfaceManager.this.mSmsCBConfig);
                                if (IccSmsInterfaceManager.this.mSmsCBConfig != null) {
                                    IccSmsInterfaceManager.this.log("config size=" + IccSmsInterfaceManager.this.mSmsCBConfig.length);
                                    for (int index = 0; index < IccSmsInterfaceManager.this.mSmsCBConfig.length; index++) {
                                        IccSmsInterfaceManager.this.log("mSmsCBConfig[" + index + "] = " + "Channel id: " + IccSmsInterfaceManager.this.mSmsCBConfig[index].getFromServiceId() + "-" + IccSmsInterfaceManager.this.mSmsCBConfig[index].getToServiceId() + ", " + "Language: " + IccSmsInterfaceManager.this.mSmsCBConfig[index].getFromCodeScheme() + "-" + IccSmsInterfaceManager.this.mSmsCBConfig[index].getToCodeScheme() + ", " + "Selected: " + IccSmsInterfaceManager.this.mSmsCBConfig[index].isSelected());
                                    }
                                }
                            }
                        } else {
                            IccSmsInterfaceManager.this.log("Cannot Get CB configs");
                        }
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    }
                case IccSmsInterfaceManager.EVENT_GET_BROADCAST_ACTIVATION_DONE /*108*/:
                    ar = (AsyncResult) msg.obj;
                    obj = IccSmsInterfaceManager.this.mLock;
                    synchronized (obj) {
                        if (ar.exception == null) {
                            ArrayList<SmsBroadcastConfigInfo> list = ar.result;
                            if (list.size() == 0) {
                                IccSmsInterfaceManager.this.mSuccess = false;
                            } else {
                                SmsBroadcastConfigInfo cbConfig = (SmsBroadcastConfigInfo) list.get(0);
                                IccSmsInterfaceManager.this.log("cbConfig: " + cbConfig.toString());
                                if (cbConfig.getFromCodeScheme() == -1 && cbConfig.getToCodeScheme() == -1 && cbConfig.getFromServiceId() == -1 && cbConfig.getToServiceId() == -1 && !cbConfig.isSelected()) {
                                    IccSmsInterfaceManager.this.mSuccess = false;
                                } else {
                                    IccSmsInterfaceManager.this.mSuccess = true;
                                }
                            }
                        }
                        IccSmsInterfaceManager.this.log("queryCbActivation: " + IccSmsInterfaceManager.this.mSuccess);
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    }
                case IccSmsInterfaceManager.EVENT_REMOVE_BROADCAST_MSG_DONE /*109*/:
                    ar = (AsyncResult) msg.obj;
                    obj2 = IccSmsInterfaceManager.this.mLock;
                    synchronized (obj2) {
                        iccSmsInterfaceManager = IccSmsInterfaceManager.this;
                        if (ar.exception != null) {
                            z = false;
                        }
                        iccSmsInterfaceManager.mSuccess = z;
                        IccSmsInterfaceManager.this.mLock.notifyAll();
                        break;
                    }
                default:
                    return;
            }
        }
    };
    protected final Object mLock = new Object();
    protected Phone mPhone;
    private List<SmsRawData> mSms;
    private SmsBroadcastConfigInfo[] mSmsCBConfig = null;
    protected boolean mSuccess;
    private final UserManager mUserManager;

    class CdmaBroadcastRangeManager extends IntRangeManager {
        private ArrayList<CdmaSmsBroadcastConfigInfo> mConfigList = new ArrayList();

        CdmaBroadcastRangeManager() {
        }

        protected void startUpdate() {
            this.mConfigList.clear();
        }

        protected void addRange(int startId, int endId, boolean selected) {
            this.mConfigList.add(new CdmaSmsBroadcastConfigInfo(startId, endId, 1, selected));
        }

        protected boolean finishUpdate() {
            if (this.mConfigList.isEmpty()) {
                return true;
            }
            return IccSmsInterfaceManager.this.setCdmaBroadcastConfig((CdmaSmsBroadcastConfigInfo[]) this.mConfigList.toArray(new CdmaSmsBroadcastConfigInfo[this.mConfigList.size()]));
        }
    }

    class CellBroadcastRangeManager extends IntRangeManager {
        private ArrayList<SmsBroadcastConfigInfo> mConfigList = new ArrayList();

        CellBroadcastRangeManager() {
        }

        protected void startUpdate() {
            this.mConfigList.clear();
        }

        protected void addRange(int startId, int endId, boolean selected) {
            this.mConfigList.add(new SmsBroadcastConfigInfo(startId, endId, 0, 255, selected));
        }

        protected boolean finishUpdate() {
            if (this.mConfigList.isEmpty()) {
                return true;
            }
            return IccSmsInterfaceManager.this.setCellBroadcastConfig((SmsBroadcastConfigInfo[]) this.mConfigList.toArray(new SmsBroadcastConfigInfo[this.mConfigList.size()]));
        }
    }

    protected IccSmsInterfaceManager(Phone phone) {
        this.mPhone = phone;
        this.mContext = phone.getContext();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mDispatcher = new ImsSMSDispatcher(phone, phone.mSmsStorageMonitor, phone.mSmsUsageMonitor);
    }

    protected void markMessagesAsRead(ArrayList<byte[]> messages) {
        if (messages != null) {
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh == null) {
                if (Rlog.isLoggable("SMS", 3)) {
                    log("markMessagesAsRead - aborting, no icc card present.");
                }
                return;
            }
            int count = messages.size();
            for (int i = 0; i < count; i++) {
                byte[] ba = (byte[]) messages.get(i);
                if (ba[0] == (byte) 3) {
                    int n = ba.length;
                    byte[] nba = new byte[(n - 1)];
                    System.arraycopy(ba, 1, nba, 0, n - 1);
                    fh.updateEFLinearFixed(IccConstants.EF_SMS, i + 1, makeSmsRecordData(1, nba), null, null);
                    if (Rlog.isLoggable("SMS", 3)) {
                        log("SMS " + (i + 1) + " marked as read");
                    }
                }
            }
        }
    }

    protected void updatePhoneObject(Phone phone) {
        this.mPhone = phone;
        this.mDispatcher.updatePhoneObject(phone);
    }

    protected void enforceReceiveAndSend(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.RECEIVE_SMS", message);
        this.mContext.enforceCallingOrSelfPermission("android.permission.SEND_SMS", message);
    }

    public boolean updateMessageOnIccEf(String callingPackage, int index, int status, byte[] pdu) {
        log("updateMessageOnIccEf: index=" + index + " status=" + status + " ==> " + "(" + Arrays.toString(pdu) + ")");
        enforceReceiveAndSend("Updating message on Icc");
        if (this.mAppOps.noteOp(22, Binder.getCallingUid(), callingPackage) != 0) {
            return false;
        }
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHandler.obtainMessage(2);
            if (status != 0) {
                IccFileHandler fh = this.mPhone.getIccFileHandler();
                if (fh == null) {
                    response.recycle();
                    boolean z = this.mSuccess;
                    return z;
                }
                fh.updateEFLinearFixed(IccConstants.EF_SMS, index, makeSmsRecordData(status, pdu), null, response);
            } else if (1 == this.mPhone.getPhoneType()) {
                this.mPhone.mCi.deleteSmsOnSim(index, response);
            } else {
                this.mPhone.mCi.deleteSmsOnRuim(index, response);
            }
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to update by index");
            }
        }
        return this.mSuccess;
    }

    public boolean copyMessageToIccEf(String callingPackage, int status, byte[] pdu, byte[] smsc) {
        log("copyMessageToIccEf: status=" + status + " ==> " + "pdu=(" + Arrays.toString(pdu) + "), smsc=(" + Arrays.toString(smsc) + ")");
        enforceReceiveAndSend("Copying message to Icc");
        if (this.mAppOps.noteOp(22, Binder.getCallingUid(), callingPackage) != 0) {
            return false;
        }
        synchronized (this.mLock) {
            this.mSuccess = false;
            Message response = this.mHandler.obtainMessage(2);
            if (1 == this.mPhone.getPhoneType()) {
                this.mPhone.mCi.writeSmsToSim(status, IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), response);
            } else {
                this.mPhone.mCi.writeSmsToRuim(status, IccUtils.bytesToHexString(pdu), response);
            }
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to update by index");
            }
        }
        return this.mSuccess;
    }

    public List<SmsRawData> getAllMessagesFromIccEf(String callingPackage) {
        log("getAllMessagesFromEF");
        this.mContext.enforceCallingOrSelfPermission("android.permission.RECEIVE_SMS", "Reading messages from Icc");
        if (this.mAppOps.noteOp(21, Binder.getCallingUid(), callingPackage) != 0) {
            return new ArrayList();
        }
        synchronized (this.mLock) {
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh == null) {
                Rlog.e(LOG_TAG, "Cannot load Sms records. No icc card?");
                this.mSms = null;
                List<SmsRawData> list = this.mSms;
                return list;
            }
            fh.loadEFLinearFixedAll(IccConstants.EF_SMS, this.mHandler.obtainMessage(1));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to load from the Icc");
            }
        }
        return this.mSms;
    }

    public void sendDataWithSelfPermissions(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.SEND_SMS", "Sending SMS message");
        sendDataInternal(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
    }

    public void sendData(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        sendDataInternal(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
    }

    private void sendDataInternal(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendData: destAddr=" + destAddr + " scAddr=" + scAddr + " destPort=" + destPort + " data='" + HexDump.toHexString(data) + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            this.mDispatcher.sendData(filterDestAddress(destAddr), scAddr, destPort, data, sentIntent, deliveryIntent, callingPackage);
        }
    }

    public void sendText(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        sendTextInternal(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp);
    }

    public void sendTextWithSelfPermissions(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        this.mPhone.getContext().enforceCallingOrSelfPermission("android.permission.SEND_SMS", "Sending SMS message");
        sendTextInternal(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessage);
    }

    private void sendTextInternal(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendText: destAddr=" + destAddr + " scAddr=" + scAddr + " text='" + text + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            if (!persistMessageForNonDefaultSmsApp) {
                enforcePrivilegedAppPermissions();
            }
            this.mDispatcher.sendText(filterDestAddress(destAddr), scAddr, text, sentIntent, deliveryIntent, null, callingPackage, persistMessageForNonDefaultSmsApp, -1, false, -1);
        }
    }

    public void sendTextWithOptions(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean isExpectMore, int validityPeriod) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendText: destAddr=" + destAddr + " scAddr=" + scAddr + " text='" + text + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent + "validityPeriod" + validityPeriod);
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            this.mDispatcher.sendText(destAddr, scAddr, text, sentIntent, deliveryIntent, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, isExpectMore, validityPeriod);
        }
    }

    public void sendTextWithOptionsOem(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean isExpectMore, int validityPeriod, int encodingType) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendText: destAddr=" + destAddr + " scAddr=" + scAddr + " text='" + text + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent + "validityPeriod" + validityPeriod);
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            this.mDispatcher.sendTextOem(destAddr, scAddr, text, sentIntent, deliveryIntent, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, isExpectMore, validityPeriod, encodingType);
        }
    }

    public void injectSmsPdu(byte[] pdu, String format, PendingIntent receivedIntent) {
        enforcePrivilegedAppPermissions();
        if (Rlog.isLoggable("SMS", 2)) {
            log("pdu: " + pdu + "\n format=" + format + "\n receivedIntent=" + receivedIntent);
        }
        this.mDispatcher.injectSmsPdu(pdu, format, receivedIntent);
    }

    public void sendMultipartText(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) {
        int i;
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (!persistMessageForNonDefaultSmsApp) {
            enforcePrivilegedAppPermissions();
        }
        if (Rlog.isLoggable("SMS", 2)) {
            i = 0;
            for (String part : parts) {
                int i2 = i + 1;
                log("sendMultipartText: destAddr=" + destAddr + ", srAddr=" + scAddr + ", part[" + i + "]=" + part);
                i = i2;
            }
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            destAddr = filterDestAddress(destAddr);
            if (parts.size() <= 1 || parts.size() >= 10 || (SmsMessage.hasEmsSupport() ^ 1) == 0) {
                this.mDispatcher.sendMultipartText(destAddr, scAddr, (ArrayList) parts, (ArrayList) sentIntents, (ArrayList) deliveryIntents, null, callingPackage, persistMessageForNonDefaultSmsApp, -1, false, -1);
                return;
            }
            i = 0;
            while (i < parts.size()) {
                String singlePart = (String) parts.get(i);
                if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                    singlePart = String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart;
                } else {
                    singlePart = singlePart.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
                }
                PendingIntent pendingIntent = null;
                if (sentIntents != null && sentIntents.size() > i) {
                    pendingIntent = (PendingIntent) sentIntents.get(i);
                }
                PendingIntent pendingIntent2 = null;
                if (deliveryIntents != null && deliveryIntents.size() > i) {
                    pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
                }
                this.mDispatcher.sendText(destAddr, scAddr, singlePart, pendingIntent, pendingIntent2, null, callingPackage, persistMessageForNonDefaultSmsApp, -1, false, -1);
                i++;
            }
        }
    }

    public void sendMultipartTextWithOptions(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp, int priority, boolean isExpectMore, int validityPeriod) {
        int i;
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (Rlog.isLoggable("SMS", 2)) {
            i = 0;
            for (String part : parts) {
                int i2 = i + 1;
                log("sendMultipartTextWithOptions: destAddr=" + destAddr + ", srAddr=" + scAddr + ", part[" + i + "]=" + part);
                i = i2;
            }
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            destAddr = filterDestAddress(destAddr);
            if (parts.size() <= 1 || parts.size() >= 10 || (SmsMessage.hasEmsSupport() ^ 1) == 0) {
                this.mDispatcher.sendMultipartText(destAddr, scAddr, (ArrayList) parts, (ArrayList) sentIntents, (ArrayList) deliveryIntents, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, isExpectMore, validityPeriod);
                return;
            }
            i = 0;
            while (i < parts.size()) {
                String singlePart = (String) parts.get(i);
                if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                    singlePart = String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart;
                } else {
                    singlePart = singlePart.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
                }
                PendingIntent pendingIntent = null;
                if (sentIntents != null && sentIntents.size() > i) {
                    pendingIntent = (PendingIntent) sentIntents.get(i);
                }
                PendingIntent pendingIntent2 = null;
                if (deliveryIntents != null && deliveryIntents.size() > i) {
                    pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
                }
                this.mDispatcher.sendText(destAddr, scAddr, singlePart, pendingIntent, pendingIntent2, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, isExpectMore, validityPeriod);
                i++;
            }
        }
    }

    public void sendMultipartTextWithOptionsOem(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp, int priority, boolean isExpectMore, int validityPeriod, int encodingType) {
        int i;
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (Rlog.isLoggable("SMS", 2)) {
            i = 0;
            for (String part : parts) {
                int i2 = i + 1;
                log("sendMultipartTextWithOptions: destAddr=" + destAddr + ", srAddr=" + scAddr + ", part[" + i + "]=" + part);
                i = i2;
            }
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            destAddr = filterDestAddress(destAddr);
            if (parts.size() <= 1 || parts.size() >= 10 || (SmsMessage.hasEmsSupport() ^ 1) == 0) {
                this.mDispatcher.sendMultipartTextOem(destAddr, scAddr, (ArrayList) parts, (ArrayList) sentIntents, (ArrayList) deliveryIntents, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, isExpectMore, validityPeriod, encodingType);
                return;
            }
            i = 0;
            while (i < parts.size()) {
                String singlePart = (String) parts.get(i);
                if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                    singlePart = String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart;
                } else {
                    singlePart = singlePart.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
                }
                PendingIntent pendingIntent = null;
                if (sentIntents != null && sentIntents.size() > i) {
                    pendingIntent = (PendingIntent) sentIntents.get(i);
                }
                PendingIntent pendingIntent2 = null;
                if (deliveryIntents != null && deliveryIntents.size() > i) {
                    pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
                }
                this.mDispatcher.sendTextOem(destAddr, scAddr, singlePart, pendingIntent, pendingIntent2, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, isExpectMore, validityPeriod, encodingType);
                i++;
            }
        }
    }

    public int getPremiumSmsPermission(String packageName) {
        return this.mDispatcher.getPremiumSmsPermission(packageName);
    }

    public void setPremiumSmsPermission(String packageName, int permission) {
        this.mDispatcher.setPremiumSmsPermission(packageName, permission);
    }

    protected ArrayList<SmsRawData> buildValidRawData(ArrayList<byte[]> messages) {
        int count = messages.size();
        ArrayList<SmsRawData> ret = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            if (((byte[]) messages.get(i))[0] == (byte) 0) {
                ret.add(null);
            } else {
                ret.add(new SmsRawData((byte[]) messages.get(i)));
            }
        }
        return ret;
    }

    protected byte[] makeSmsRecordData(int status, byte[] pdu) {
        byte[] data;
        if (1 == this.mPhone.getPhoneType()) {
            data = new byte[PduHeaders.ADDITIONAL_HEADERS];
        } else {
            data = new byte[255];
        }
        data[0] = (byte) (status & 7);
        System.arraycopy(pdu, 0, data, 1, pdu.length);
        for (int j = pdu.length + 1; j < data.length; j++) {
            data[j] = (byte) -1;
        }
        return data;
    }

    public boolean enableCellBroadcast(int messageIdentifier, int ranType) {
        return enableCellBroadcastRange(messageIdentifier, messageIdentifier, ranType);
    }

    public boolean disableCellBroadcast(int messageIdentifier, int ranType) {
        return disableCellBroadcastRange(messageIdentifier, messageIdentifier, ranType);
    }

    public boolean enableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (ranType == 0) {
            return enableGsmBroadcastRange(startMessageId, endMessageId);
        }
        if (ranType == 1) {
            return enableCdmaBroadcastRange(startMessageId, endMessageId);
        }
        throw new IllegalArgumentException("Not a supportted RAN Type");
    }

    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (ranType == 0) {
            return disableGsmBroadcastRange(startMessageId, endMessageId);
        }
        if (ranType == 1) {
            return disableCdmaBroadcastRange(startMessageId, endMessageId);
        }
        throw new IllegalArgumentException("Not a supportted RAN Type");
    }

    public synchronized boolean enableGsmBroadcastRange(int startMessageId, int endMessageId) {
        Context context = this.mPhone.getContext();
        context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling cell broadcast SMS");
        String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (this.mCellBroadcastRangeManager.enableRange(startMessageId, endMessageId, client)) {
            log("Added GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            setCellBroadcastActivation(this.mCellBroadcastRangeManager.isEmpty() ^ 1);
            return true;
        }
        log("Failed to add GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
        return false;
    }

    public synchronized boolean disableGsmBroadcastRange(int startMessageId, int endMessageId) {
        Context context = this.mPhone.getContext();
        context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Disabling cell broadcast SMS");
        String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (this.mCellBroadcastRangeManager.disableRange(startMessageId, endMessageId, client)) {
            log("Removed GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            setCellBroadcastActivation(this.mCellBroadcastRangeManager.isEmpty() ^ 1);
            return true;
        }
        log("Failed to remove GSM cell broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
        return false;
    }

    public synchronized boolean enableCdmaBroadcastRange(int startMessageId, int endMessageId) {
        Context context = this.mPhone.getContext();
        context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Enabling cdma broadcast SMS");
        String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (this.mCdmaBroadcastRangeManager.enableRange(startMessageId, endMessageId, client)) {
            log("Added cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            setCdmaBroadcastActivation(this.mCdmaBroadcastRangeManager.isEmpty() ^ 1);
            return true;
        }
        log("Failed to add cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
        return false;
    }

    public synchronized boolean disableCdmaBroadcastRange(int startMessageId, int endMessageId) {
        Context context = this.mPhone.getContext();
        context.enforceCallingPermission("android.permission.RECEIVE_SMS", "Disabling cell broadcast SMS");
        String client = context.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (this.mCdmaBroadcastRangeManager.disableRange(startMessageId, endMessageId, client)) {
            log("Removed cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
            setCdmaBroadcastActivation(this.mCdmaBroadcastRangeManager.isEmpty() ^ 1);
            return true;
        }
        log("Failed to remove cdma broadcast subscription for MID range " + startMessageId + " to " + endMessageId + " from client " + client);
        return false;
    }

    private boolean setCellBroadcastConfig(SmsBroadcastConfigInfo[] configs) {
        log("Calling setGsmBroadcastConfig with " + configs.length + " configurations");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(4);
            this.mSuccess = false;
            this.mPhone.mCi.setGsmBroadcastConfig(configs, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set cell broadcast config");
            }
        }
        return this.mSuccess;
    }

    private boolean setCellBroadcastActivation(boolean activate) {
        log("Calling setCellBroadcastActivation(" + activate + ')');
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(3);
            this.mSuccess = false;
            this.mPhone.mCi.setGsmBroadcastActivation(activate, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set cell broadcast activation");
            }
        }
        return this.mSuccess;
    }

    private boolean setCdmaBroadcastConfig(CdmaSmsBroadcastConfigInfo[] configs) {
        log("Calling setCdmaBroadcastConfig with " + configs.length + " configurations");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(4);
            this.mSuccess = false;
            this.mPhone.mCi.setCdmaBroadcastConfig(configs, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set cdma broadcast config");
            }
        }
        return this.mSuccess;
    }

    private boolean setCdmaBroadcastActivation(boolean activate) {
        log("Calling setCdmaBroadcastActivation(" + activate + ")");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(3);
            this.mSuccess = false;
            this.mPhone.mCi.setCdmaBroadcastActivation(activate, response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set cdma broadcast activation");
            }
        }
        return this.mSuccess;
    }

    protected void log(String msg) {
        Log.d(LOG_TAG, "[IccSmsInterfaceManager] " + msg);
    }

    public boolean isImsSmsSupported() {
        return this.mDispatcher.isIms();
    }

    public String getImsSmsFormat() {
        return this.mDispatcher.getImsSmsFormat();
    }

    public void sendStoredText(String callingPkg, Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (Rlog.isLoggable("SMS", 2)) {
            log("sendStoredText: scAddr=" + scAddress + " messageUri=" + messageUri + " sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPkg) == 0) {
            ContentResolver resolver = this.mPhone.getContext().getContentResolver();
            if (isFailedOrDraft(resolver, messageUri)) {
                String[] textAndAddress = loadTextAndAddress(resolver, messageUri);
                if (textAndAddress == null) {
                    Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredText: can not load text");
                    returnUnspecifiedFailure(sentIntent);
                    return;
                }
                textAndAddress[1] = filterDestAddress(textAndAddress[1]);
                this.mDispatcher.sendText(textAndAddress[1], scAddress, textAndAddress[0], sentIntent, deliveryIntent, messageUri, callingPkg, true, -1, false, -1);
                return;
            }
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredText: not FAILED or DRAFT message");
            returnUnspecifiedFailure(sentIntent);
        }
    }

    public void sendStoredMultipartText(String callingPkg, Uri messageUri, String scAddress, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPkg) == 0) {
            ContentResolver resolver = this.mPhone.getContext().getContentResolver();
            if (isFailedOrDraft(resolver, messageUri)) {
                String[] textAndAddress = loadTextAndAddress(resolver, messageUri);
                if (textAndAddress == null) {
                    Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: can not load text");
                    returnUnspecifiedFailure((List) sentIntents);
                    return;
                }
                ArrayList<String> parts = SmsManager.getDefault().divideMessage(textAndAddress[0]);
                if (parts == null || parts.size() < 1) {
                    Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: can not divide text");
                    returnUnspecifiedFailure((List) sentIntents);
                    return;
                }
                textAndAddress[1] = filterDestAddress(textAndAddress[1]);
                if (parts.size() <= 1 || parts.size() >= 10 || (SmsMessage.hasEmsSupport() ^ 1) == 0) {
                    this.mDispatcher.sendMultipartText(textAndAddress[1], scAddress, parts, (ArrayList) sentIntents, (ArrayList) deliveryIntents, messageUri, callingPkg, true, -1, false, -1);
                    return;
                }
                int i = 0;
                while (i < parts.size()) {
                    String singlePart = (String) parts.get(i);
                    if (SmsMessage.shouldAppendPageNumberAsPrefix()) {
                        singlePart = String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart;
                    } else {
                        singlePart = singlePart.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
                    }
                    PendingIntent pendingIntent = null;
                    if (sentIntents != null && sentIntents.size() > i) {
                        pendingIntent = (PendingIntent) sentIntents.get(i);
                    }
                    PendingIntent pendingIntent2 = null;
                    if (deliveryIntents != null && deliveryIntents.size() > i) {
                        pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
                    }
                    this.mDispatcher.sendText(textAndAddress[1], scAddress, singlePart, pendingIntent, pendingIntent2, messageUri, callingPkg, true, -1, false, -1);
                    i++;
                }
                return;
            }
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]sendStoredMultipartText: not FAILED or DRAFT message");
            returnUnspecifiedFailure((List) sentIntents);
        }
    }

    private boolean isFailedOrDraft(ContentResolver resolver, Uri messageUri) {
        long identity = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            cursor = resolver.query(messageUri, new String[]{Calls.TYPE}, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return false;
            }
            int type = cursor.getInt(0);
            boolean z = type != 3 ? type == 5 : true;
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
            return z;
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, "[IccSmsInterfaceManager]isFailedOrDraft: query message type failed", e);
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private String[] loadTextAndAddress(ContentResolver resolver, Uri messageUri) {
        long identity = Binder.clearCallingIdentity();
        Cursor cursor = null;
        String[] strArr;
        try {
            cursor = resolver.query(messageUri, new String[]{"body", "address"}, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return null;
            }
            strArr = new String[]{cursor.getString(0), cursor.getString(1)};
            return strArr;
        } catch (SQLiteException e) {
            strArr = LOG_TAG;
            Log.e(strArr, "[IccSmsInterfaceManager]loadText: query message text failed", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void returnUnspecifiedFailure(PendingIntent pi) {
        if (pi != null) {
            try {
                pi.send(1);
            } catch (CanceledException e) {
            }
        }
    }

    private void returnUnspecifiedFailure(List<PendingIntent> pis) {
        if (pis != null) {
            for (PendingIntent pi : pis) {
                returnUnspecifiedFailure(pi);
            }
        }
    }

    private void enforceCarrierPrivilege() {
        UiccController controller = UiccController.getInstance();
        if (controller == null || controller.getUiccCard(this.mPhone.getPhoneId()) == null) {
            throw new SecurityException("No Carrier Privilege: No UICC");
        } else if (controller.getUiccCard(this.mPhone.getPhoneId()).getCarrierPrivilegeStatusForCurrentTransaction(this.mContext.getPackageManager()) != 1) {
            throw new SecurityException("No Carrier Privilege.");
        }
    }

    private void enforcePrivilegedAppPermissions() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            int callingUid = Binder.getCallingUid();
            String carrierImsPackage = CarrierSmsUtils.getCarrierImsPackageForIntent(this.mContext, this.mPhone, new Intent("android.service.carrier.CarrierMessagingService"));
            if (carrierImsPackage != null) {
                try {
                    if (callingUid == this.mContext.getPackageManager().getPackageUid(carrierImsPackage, 0)) {
                        return;
                    }
                } catch (NameNotFoundException e) {
                    if (Rlog.isLoggable("SMS", 3)) {
                        log("Cannot find configured carrier ims package");
                    }
                }
            }
            enforceCarrierPrivilege();
        }
    }

    private String filterDestAddress(String destAddr) {
        String result = SmsNumberUtils.filterDestAddr(this.mPhone, destAddr);
        return result != null ? result : destAddr;
    }

    public int getSmsCapacityOnIcc() {
        int numberOnIcc = -1;
        IccRecords ir = this.mPhone.getIccRecords();
        if (ir != null) {
            numberOnIcc = ir.getSmsCapacityOnIcc();
        } else {
            log("getSmsCapacityOnIcc - aborting, no icc card present.");
        }
        log("getSmsCapacityOnIcc().numberOnIcc = " + numberOnIcc);
        return numberOnIcc;
    }

    public boolean activateCellBroadcastSms(boolean activate) {
        log("activateCellBroadcastSms activate : " + activate);
        return setCellBroadcastActivation(activate);
    }

    private SmsCbConfigInfo Convert2SmsCbConfigInfo(SmsBroadcastConfigInfo info) {
        return new SmsCbConfigInfo(info.getFromServiceId(), info.getToServiceId(), info.getFromCodeScheme(), info.getToCodeScheme(), info.isSelected());
    }

    private SmsBroadcastConfigInfo Convert2SmsBroadcastConfigInfo(SmsCbConfigInfo info) {
        return new SmsBroadcastConfigInfo(info.mFromServiceId, info.mToServiceId, info.mFromCodeScheme, info.mToCodeScheme, info.mSelected);
    }

    public SmsCbConfigInfo[] getCellBroadcastSmsConfig() {
        log("getCellBroadcastSmsConfig");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(EVENT_GET_BROADCAST_CONFIG_DONE);
            this.mSmsCBConfig = null;
            this.mPhone.mCi.getGsmBroadcastConfig(response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get CB config");
            }
        }
        if (this.mSmsCBConfig != null) {
            log("config length = " + this.mSmsCBConfig.length);
            if (this.mSmsCBConfig.length != 0) {
                SmsCbConfigInfo[] result = new SmsCbConfigInfo[this.mSmsCBConfig.length];
                for (int i = 0; i < this.mSmsCBConfig.length; i++) {
                    result[i] = Convert2SmsCbConfigInfo(this.mSmsCBConfig[i]);
                }
                return result;
            }
        }
        return null;
    }

    public boolean setCellBroadcastSmsConfig(SmsCbConfigInfo[] channels, SmsCbConfigInfo[] languages) {
        log("setCellBroadcastSmsConfig");
        if (channels == null && languages == null) {
            return true;
        }
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(4);
            this.mSuccess = false;
            ArrayList<SmsBroadcastConfigInfo> chid_list = new ArrayList();
            if (channels != null) {
                for (SmsCbConfigInfo Convert2SmsBroadcastConfigInfo : channels) {
                    chid_list.add(Convert2SmsBroadcastConfigInfo(Convert2SmsBroadcastConfigInfo));
                }
            }
            ArrayList<SmsBroadcastConfigInfo> lang_list = new ArrayList();
            if (languages != null) {
                for (SmsCbConfigInfo Convert2SmsBroadcastConfigInfo2 : languages) {
                    lang_list.add(Convert2SmsBroadcastConfigInfo(Convert2SmsBroadcastConfigInfo2));
                }
            }
            chid_list.addAll(lang_list);
            this.mPhone.mCi.setGsmBroadcastConfig((SmsBroadcastConfigInfo[]) chid_list.toArray(new SmsBroadcastConfigInfo[1]), response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set CB config");
            }
        }
        return this.mSuccess;
    }

    public boolean queryCellBroadcastSmsActivation() {
        log("queryCellBroadcastSmsActivation");
        synchronized (this.mLock) {
            Message response = this.mHandler.obtainMessage(EVENT_GET_BROADCAST_ACTIVATION_DONE);
            this.mSuccess = false;
            this.mPhone.mCi.getGsmBroadcastConfig(response);
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get CB activation");
            }
        }
        return this.mSuccess;
    }
}
