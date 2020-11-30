package com.mediatek.internal.telephony;

import android.app.ActivityThread;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.IccSmsInterfaceManager;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SmsNumberUtils;
import com.android.internal.telephony.SmsRawData;
import com.android.internal.telephony.cdma.SmsMessage;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsMessage;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.util.HexDump;
import com.mediatek.internal.telephony.gsm.MtkSmsMessage;
import com.mediatek.internal.telephony.uicc.MtkSIMFileHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mediatek.telephony.MtkSimSmsInsertStatus;
import mediatek.telephony.MtkSmsParameters;

public class MtkIccSmsInterfaceManager extends IccSmsInterfaceManager {
    private static final int CB_ACTIVATION_OFF = 0;
    private static final int CB_ACTIVATION_ON = 1;
    private static final int CB_ACTIVATION_UNKNOWN = -1;
    static final boolean DBG = true;
    private static final int EVENT_GET_BROADCAST_ACTIVATION_DONE = 106;
    private static final int EVENT_GET_BROADCAST_CONFIG_CHANNEL_DONE = 108;
    private static final int EVENT_GET_BROADCAST_CONFIG_LANGUAGE_DONE = 110;
    private static final int EVENT_GET_SMSC_ADDRESS_BUNDLE_DONE = 113;
    private static final int EVENT_GET_SMSC_ADDRESS_DONE = 112;
    private static final int EVENT_GET_SMS_PARAMS = 103;
    private static final int EVENT_GET_SMS_SIM_MEM_STATUS_DONE = 101;
    private static final int EVENT_INSERT_TEXT_MESSAGE_TO_ICC_DONE = 102;
    private static final int EVENT_LOAD_ONE_RECORD_DONE = 105;
    private static final int EVENT_MTK_LOAD_DONE = 115;
    private static final int EVENT_MTK_UPDATE_DONE = 116;
    private static final int EVENT_REMOVE_BROADCAST_MSG_DONE = 107;
    private static final int EVENT_SET_BROADCAST_CONFIG_LANGUAGE_DONE = 109;
    private static final int EVENT_SET_ETWS_CONFIG_DONE = 111;
    private static final int EVENT_SET_SMSC_ADDRESS_DONE = 114;
    private static final int EVENT_SET_SMS_PARAMS = 104;
    private static final int EVENT_SIM_SMS_DELETE_DONE = 100;
    private static final int EVENT_SMS_WIPE_DONE = 117;
    private static final String INDEXT_SPLITOR = ",";
    static final String LOG_TAG = "MtkIccSmsInterfaceManager";
    private static int sConcatenatedRef = 456;
    private int mCurrentCellBroadcastActivation = -1;
    private boolean mInsertMessageSuccess;
    private boolean mInserted;
    protected Handler mMtkHandler = new Handler() {
        /* class com.mediatek.internal.telephony.MtkIccSmsInterfaceManager.AnonymousClass1 */

        public void handleMessage(Message msg) {
            boolean z = false;
            boolean z2 = false;
            boolean z3 = false;
            switch (msg.what) {
                case 101:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                        if (ar.exception == null) {
                            MtkIccSmsInterfaceManager.this.mMtkSuccess = true;
                            if (MtkIccSmsInterfaceManager.this.mSimMemStatus == null) {
                                MtkIccSmsInterfaceManager.this.mSimMemStatus = new MtkIccSmsStorageStatus();
                            }
                            MtkIccSmsStorageStatus tmpStatus = (MtkIccSmsStorageStatus) ar.result;
                            MtkIccSmsInterfaceManager.this.mSimMemStatus.mUsed = tmpStatus.mUsed;
                            MtkIccSmsInterfaceManager.this.mSimMemStatus.mTotal = tmpStatus.mTotal;
                        } else {
                            MtkIccSmsInterfaceManager.this.log("Cannot Get Sms SIM Memory Status from SIM");
                        }
                        MtkIccSmsInterfaceManager.this.mMtkLock.notifyAll();
                    }
                    return;
                case 102:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mSimInsertLock) {
                        MtkIccSmsInterfaceManager.this.mInsertMessageSuccess = ar2.exception == null;
                        if (MtkIccSmsInterfaceManager.this.mInsertMessageSuccess) {
                            try {
                                int index = ((int[]) ar2.result)[0];
                                StringBuilder sb = new StringBuilder();
                                MtkSimSmsInsertStatus mtkSimSmsInsertStatus = MtkIccSmsInterfaceManager.this.smsInsertRet;
                                sb.append(mtkSimSmsInsertStatus.indexInIcc);
                                sb.append(index);
                                sb.append(MtkIccSmsInterfaceManager.INDEXT_SPLITOR);
                                mtkSimSmsInsertStatus.indexInIcc = sb.toString();
                                MtkIccSmsInterfaceManager.this.log("insertText save one pdu in index " + index);
                            } catch (ClassCastException e) {
                                e.printStackTrace();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            MtkIccSmsInterfaceManager.this.log("insertText fail to insert sms into ICC");
                            StringBuilder sb2 = new StringBuilder();
                            MtkSimSmsInsertStatus mtkSimSmsInsertStatus2 = MtkIccSmsInterfaceManager.this.smsInsertRet;
                            sb2.append(mtkSimSmsInsertStatus2.indexInIcc);
                            sb2.append("-1,");
                            mtkSimSmsInsertStatus2.indexInIcc = sb2.toString();
                        }
                        MtkIccSmsInterfaceManager.this.mInserted = true;
                        MtkIccSmsInterfaceManager.this.mSimInsertLock.notifyAll();
                    }
                    return;
                case MtkIccSmsInterfaceManager.EVENT_GET_SMS_PARAMS /* 103 */:
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                        if (ar3.exception == null) {
                            try {
                                MtkIccSmsInterfaceManager.this.mSmsParams = (MtkSmsParameters) ar3.result;
                            } catch (ClassCastException e2) {
                                MtkIccSmsInterfaceManager.this.log("[EFsmsp fail to get sms params ClassCastException");
                                e2.printStackTrace();
                            } catch (Exception ex2) {
                                MtkIccSmsInterfaceManager.this.log("[EFsmsp fail to get sms params Exception");
                                ex2.printStackTrace();
                            }
                        } else {
                            MtkIccSmsInterfaceManager.this.log("[EFsmsp fail to get sms params");
                            MtkIccSmsInterfaceManager.this.mSmsParams = null;
                        }
                        MtkIccSmsInterfaceManager.this.mMtkLock.notifyAll();
                    }
                    return;
                case 104:
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                        if (ar4.exception == null) {
                            MtkIccSmsInterfaceManager.this.mSmsParamsSuccess = true;
                        } else {
                            MtkIccSmsInterfaceManager.this.log("[EFsmsp fail to set sms params");
                            MtkIccSmsInterfaceManager.this.mSmsParamsSuccess = false;
                        }
                        MtkIccSmsInterfaceManager.this.mMtkLock.notifyAll();
                    }
                    return;
                case 105:
                    AsyncResult ar5 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                        if (ar5.exception == null) {
                            try {
                                byte[] rawData = (byte[]) ar5.result;
                                if (rawData[0] == 0) {
                                    MtkIccSmsInterfaceManager.this.log("sms raw data status is FREE");
                                    MtkIccSmsInterfaceManager.this.mSmsRawData = null;
                                } else {
                                    MtkIccSmsInterfaceManager.this.mSmsRawData = new SmsRawData(rawData);
                                }
                            } catch (ClassCastException e3) {
                                MtkIccSmsInterfaceManager.this.log("fail to get sms raw data ClassCastException");
                                e3.printStackTrace();
                                MtkIccSmsInterfaceManager.this.mSmsRawData = null;
                            }
                        } else {
                            MtkIccSmsInterfaceManager.this.log("fail to get sms raw data rild");
                            MtkIccSmsInterfaceManager.this.mSmsRawData = null;
                        }
                        MtkIccSmsInterfaceManager.this.mMtkLock.notifyAll();
                    }
                    return;
                case MtkIccSmsInterfaceManager.EVENT_GET_BROADCAST_ACTIVATION_DONE /* 106 */:
                    AsyncResult ar6 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                        if (ar6.exception == null) {
                            MtkIccSmsInterfaceManager mtkIccSmsInterfaceManager = MtkIccSmsInterfaceManager.this;
                            if (((int[]) ar6.result)[0] == 1) {
                                z = true;
                            }
                            mtkIccSmsInterfaceManager.mMtkSuccess = z;
                        }
                        MtkIccSmsInterfaceManager.this.log("queryCbActivation: " + MtkIccSmsInterfaceManager.this.mMtkSuccess);
                        MtkIccSmsInterfaceManager.this.mMtkLock.notifyAll();
                    }
                    return;
                case MtkIccSmsInterfaceManager.EVENT_REMOVE_BROADCAST_MSG_DONE /* 107 */:
                    AsyncResult ar7 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                        MtkIccSmsInterfaceManager mtkIccSmsInterfaceManager2 = MtkIccSmsInterfaceManager.this;
                        if (ar7.exception == null) {
                            z3 = true;
                        }
                        mtkIccSmsInterfaceManager2.mMtkSuccess = z3;
                        MtkIccSmsInterfaceManager.this.mMtkLock.notifyAll();
                    }
                    return;
                case MtkIccSmsInterfaceManager.EVENT_GET_BROADCAST_CONFIG_CHANNEL_DONE /* 108 */:
                    AsyncResult ar8 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                        if (ar8.exception == null) {
                            ArrayList<SmsBroadcastConfigInfo> mList = (ArrayList) ar8.result;
                            for (int i = 0; i < mList.size(); i++) {
                                SmsBroadcastConfigInfo cbConfig = mList.get(i);
                                if (cbConfig.getFromServiceId() == cbConfig.getToServiceId()) {
                                    MtkIccSmsInterfaceManager.this.mSmsCbChannelConfig = MtkIccSmsInterfaceManager.this.mSmsCbChannelConfig + cbConfig.getFromServiceId();
                                } else {
                                    MtkIccSmsInterfaceManager.this.mSmsCbChannelConfig = MtkIccSmsInterfaceManager.this.mSmsCbChannelConfig + cbConfig.getFromServiceId() + "-" + cbConfig.getToServiceId();
                                }
                                if (i + 1 != mList.size()) {
                                    MtkIccSmsInterfaceManager.this.mSmsCbChannelConfig = MtkIccSmsInterfaceManager.this.mSmsCbChannelConfig + MtkIccSmsInterfaceManager.INDEXT_SPLITOR;
                                }
                            }
                            MtkIccSmsInterfaceManager.this.log("Channel configuration " + MtkIccSmsInterfaceManager.this.mSmsCbChannelConfig);
                        } else {
                            MtkIccSmsInterfaceManager.this.log("Cannot Get CB configs");
                        }
                        MtkIccSmsInterfaceManager.this.mMtkLock.notifyAll();
                    }
                    return;
                case 109:
                case 111:
                case 114:
                    AsyncResult ar9 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                        MtkIccSmsInterfaceManager mtkIccSmsInterfaceManager3 = MtkIccSmsInterfaceManager.this;
                        if (ar9.exception == null) {
                            z2 = true;
                        }
                        mtkIccSmsInterfaceManager3.mMtkSuccess = z2;
                        MtkIccSmsInterfaceManager.this.mMtkLock.notifyAll();
                    }
                    return;
                case 110:
                    AsyncResult ar10 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                        if (ar10.exception == null) {
                            MtkIccSmsInterfaceManager.this.mSmsCbLanguageConfig = (String) ar10.result;
                            MtkIccSmsInterfaceManager.this.mSmsCbLanguageConfig = MtkIccSmsInterfaceManager.this.mSmsCbLanguageConfig != null ? MtkIccSmsInterfaceManager.this.mSmsCbLanguageConfig : "";
                            MtkIccSmsInterfaceManager.this.log("Language configuration " + MtkIccSmsInterfaceManager.this.mSmsCbLanguageConfig);
                        } else {
                            MtkIccSmsInterfaceManager.this.log("Cannot Get CB configs");
                        }
                        MtkIccSmsInterfaceManager.this.mMtkLock.notifyAll();
                    }
                    return;
                case 112:
                    AsyncResult ar11 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                        if (ar11.exception == null) {
                            MtkIccSmsInterfaceManager.this.mSmscAddress = (String) ar11.result;
                        } else {
                            MtkIccSmsInterfaceManager.this.log("Cannot Get SMSC address");
                        }
                        MtkIccSmsInterfaceManager.this.mMtkLock.notifyAll();
                    }
                    return;
                case 113:
                    AsyncResult ar12 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                        MtkIccSmsInterfaceManager.this.mSmscAddressBundle.clear();
                        if (ar12.exception == null) {
                            MtkIccSmsInterfaceManager.this.mSmscAddressBundle.putByte("errorCode", (byte) 0);
                            MtkIccSmsInterfaceManager.this.mSmscAddressBundle.putCharSequence("scAddress", (String) ar12.result);
                        } else {
                            MtkIccSmsInterfaceManager.this.log("Cannot Get SMSC address");
                            byte error = 1;
                            if ((ar12.exception instanceof CommandException) && ar12.exception.getCommandError() == CommandException.Error.REQUEST_NOT_SUPPORTED) {
                                error = 2;
                            }
                            MtkIccSmsInterfaceManager.this.log("Fail to get sc address, error = " + ((int) error));
                            MtkIccSmsInterfaceManager.this.mSmscAddressBundle.putByte("errorCode", error);
                            MtkIccSmsInterfaceManager.this.mSmscAddressBundle.putCharSequence("scAddress", "");
                        }
                        MtkIccSmsInterfaceManager.this.mMtkLock.notifyAll();
                    }
                    return;
                case MtkIccSmsInterfaceManager.EVENT_MTK_LOAD_DONE /* 115 */:
                    AsyncResult ar13 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLoadLock) {
                        if (ar13.exception == null) {
                            MtkIccSmsInterfaceManager.this.mSms = MtkIccSmsInterfaceManager.this.buildValidRawData((ArrayList) ar13.result);
                            MtkIccSmsInterfaceManager.this.markMessagesAsRead((ArrayList) ar13.result);
                        } else {
                            if (OppoRlog.Rlog.isLoggable("SMS", 3)) {
                                MtkIccSmsInterfaceManager.this.log("Cannot load Sms records");
                            }
                            MtkIccSmsInterfaceManager.this.mSms = null;
                        }
                        MtkIccSmsInterfaceManager.this.mMtkLoadLock.notifyAll();
                    }
                    return;
                case MtkIccSmsInterfaceManager.EVENT_MTK_UPDATE_DONE /* 116 */:
                    AsyncResult ar14 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                        MtkIccSmsInterfaceManager.this.mMtkSuccess = ar14.exception == null;
                        if (MtkIccSmsInterfaceManager.this.mMtkSuccess) {
                            try {
                                int index2 = ((int[]) ar14.result)[0];
                                StringBuilder sb3 = new StringBuilder();
                                MtkSimSmsInsertStatus mtkSimSmsInsertStatus3 = MtkIccSmsInterfaceManager.this.smsInsertRet2;
                                sb3.append(mtkSimSmsInsertStatus3.indexInIcc);
                                sb3.append(index2);
                                sb3.append(MtkIccSmsInterfaceManager.INDEXT_SPLITOR);
                                mtkSimSmsInsertStatus3.indexInIcc = sb3.toString();
                                MtkIccSmsInterfaceManager.this.log("[insertRaw save one pdu in index " + index2);
                            } catch (ClassCastException e4) {
                                e4.printStackTrace();
                            } catch (Exception ex3) {
                                ex3.printStackTrace();
                            }
                        } else {
                            MtkIccSmsInterfaceManager.this.log("[insertRaw fail to insert raw into ICC");
                            StringBuilder sb4 = new StringBuilder();
                            MtkSimSmsInsertStatus mtkSimSmsInsertStatus4 = MtkIccSmsInterfaceManager.this.smsInsertRet2;
                            sb4.append(mtkSimSmsInsertStatus4.indexInIcc);
                            sb4.append("-1,");
                            mtkSimSmsInsertStatus4.indexInIcc = sb4.toString();
                        }
                        MtkIccSmsInterfaceManager.this.mMtkLock.notifyAll();
                    }
                    if (ar14.exception != null) {
                        CommandException e5 = ar14.exception;
                        MtkIccSmsInterfaceManager.this.log("Cannot update SMS " + e5.getCommandError());
                        if (e5.getCommandError() == CommandException.Error.SIM_FULL) {
                            MtkIccSmsInterfaceManager.this.mDispatchersController.handleIccFull();
                            return;
                        }
                        return;
                    }
                    return;
                case MtkIccSmsInterfaceManager.EVENT_SMS_WIPE_DONE /* 117 */:
                    AsyncResult ar15 = (AsyncResult) msg.obj;
                    synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                        if (ar15.exception == null) {
                            try {
                                int index3 = ((int[]) ar15.result)[0];
                                StringBuilder sb5 = new StringBuilder();
                                MtkSimSmsInsertStatus mtkSimSmsInsertStatus5 = MtkIccSmsInterfaceManager.this.smsInsertRet2;
                                sb5.append(mtkSimSmsInsertStatus5.indexInIcc);
                                sb5.append(index3);
                                sb5.append(MtkIccSmsInterfaceManager.INDEXT_SPLITOR);
                                mtkSimSmsInsertStatus5.indexInIcc = sb5.toString();
                                MtkIccSmsInterfaceManager.this.log("[insertRaw save one pdu in index " + index3);
                            } catch (ClassCastException e6) {
                                e6.printStackTrace();
                            } catch (Exception ex4) {
                                ex4.printStackTrace();
                            }
                        } else {
                            MtkIccSmsInterfaceManager.this.log("[insertRaw fail to insert raw into ICC");
                            StringBuilder sb6 = new StringBuilder();
                            MtkSimSmsInsertStatus mtkSimSmsInsertStatus6 = MtkIccSmsInterfaceManager.this.smsInsertRet2;
                            sb6.append(mtkSimSmsInsertStatus6.indexInIcc);
                            sb6.append("-1,");
                            mtkSimSmsInsertStatus6.indexInIcc = sb6.toString();
                        }
                        MtkIccSmsInterfaceManager.this.mSmsWipedRsp = true;
                        MtkIccSmsInterfaceManager.this.mMtkLock.notifyAll();
                    }
                    if (ar15.exception != null) {
                        CommandException e7 = ar15.exception;
                        MtkIccSmsInterfaceManager.this.log("Cannot update SMS " + e7.getCommandError());
                        if (e7.getCommandError() == CommandException.Error.SIM_FULL) {
                            MtkIccSmsInterfaceManager.this.mDispatchersController.handleIccFull();
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    protected final Object mMtkLoadLock = new Object();
    protected final Object mMtkLock = new Object();
    protected boolean mMtkSuccess;
    private final Object mSimInsertLock = new Object();
    private MtkIccSmsStorageStatus mSimMemStatus;
    private List<SmsRawData> mSms;
    private SmsBroadcastConfigInfo[] mSmsCBConfig = null;
    private String mSmsCbChannelConfig = "";
    private String mSmsCbLanguageConfig = "";
    private MtkSmsParameters mSmsParams = null;
    private boolean mSmsParamsSuccess = false;
    private SmsRawData mSmsRawData = null;
    private BroadcastReceiver mSmsWipeReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.MtkIccSmsInterfaceManager.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            MtkIccSmsInterfaceManager.this.log("Receive intent");
            if (intent.getAction().equals("com.mediatek.dm.LAWMO_WIPE")) {
                MtkIccSmsInterfaceManager.this.log("Receive wipe intent");
                new Thread() {
                    /* class com.mediatek.internal.telephony.MtkIccSmsInterfaceManager.AnonymousClass2.AnonymousClass1 */

                    public void run() {
                        synchronized (MtkIccSmsInterfaceManager.this.mMtkLock) {
                            MtkIccSmsInterfaceManager mtkIccSmsInterfaceManager = MtkIccSmsInterfaceManager.this;
                            mtkIccSmsInterfaceManager.log("Delete message on sub " + MtkIccSmsInterfaceManager.this.mPhone.getSubId());
                            MtkIccSmsInterfaceManager.this.mSmsWipedRsp = false;
                            MtkIccSmsInterfaceManager.this.mPhone.mCi.deleteSmsOnSim(-1, MtkIccSmsInterfaceManager.this.mMtkHandler.obtainMessage(MtkIccSmsInterfaceManager.EVENT_SMS_WIPE_DONE));
                            while (!MtkIccSmsInterfaceManager.this.mSmsWipedRsp) {
                                try {
                                    MtkIccSmsInterfaceManager.this.mMtkLock.wait();
                                } catch (InterruptedException e) {
                                    MtkIccSmsInterfaceManager.this.log("insertRaw interrupted while trying to update by index");
                                }
                            }
                        }
                    }
                }.start();
            }
        }
    };
    protected boolean mSmsWipedRsp;
    private String mSmscAddress = "";
    private Bundle mSmscAddressBundle = new Bundle();
    private MtkSimSmsInsertStatus smsInsertRet = new MtkSimSmsInsertStatus(0, "");
    private MtkSimSmsInsertStatus smsInsertRet2 = new MtkSimSmsInsertStatus(0, "");

    protected MtkIccSmsInterfaceManager(Phone phone) {
        super(phone);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.mediatek.dm.LAWMO_WIPE");
        this.mContext.registerReceiver(this.mSmsWipeReceiver, filter);
    }

    /* access modifiers changed from: protected */
    public void sendTextInternal(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, boolean isForVvm) {
        if (isValidParameters(destAddr, text, sentIntent)) {
            if (!checkTddDataOnlyPermission(sentIntent)) {
                log("TDD data only and w/o permission!");
                return;
            }
            ActivityThread.currentApplication().getApplicationContext();
            MtkIccSmsInterfaceManager.super.sendTextInternal(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod, isForVvm);
        }
    }

    /* access modifiers changed from: protected */
    public void sendDataInternal(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean isForVvm) {
        log("sendDataMessage");
        if (isValidParameters(destAddr, "send_data", sentIntent)) {
            if (!checkTddDataOnlyPermission(sentIntent)) {
                log("TDD data only and w/o permission!");
                return;
            }
            ActivityThread.currentApplication().getApplicationContext();
            MtkIccSmsInterfaceManager.super.sendDataInternal(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent, isForVvm);
        }
    }

    public void sendStoredText(String callingPkg, Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        ActivityThread.currentApplication().getApplicationContext();
        if (!checkTddDataOnlyPermission(sentIntent)) {
            log("TDD data only and w/o permission!");
        } else {
            MtkIccSmsInterfaceManager.super.sendStoredText(callingPkg, messageUri, scAddress, sentIntent, deliveryIntent);
        }
    }

    public void sendStoredMultipartText(String callingPkg, Uri messageUri, String scAddress, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) {
        ActivityThread.currentApplication().getApplicationContext();
        if (!checkTddDataOnlyPermission(sentIntents)) {
            log("TDD data only and w/o permission!");
        } else {
            MtkIccSmsInterfaceManager.super.sendStoredMultipartText(callingPkg, messageUri, scAddress, sentIntents, deliveryIntents);
        }
    }

    public List<SmsRawData> getAllMessagesFromIccEf(String callingPackage) {
        log("getAllMessagesFromEF " + callingPackage);
        this.mContext.enforceCallingOrSelfPermission("android.permission.RECEIVE_SMS", "Reading messages from Icc");
        if (this.mAppOps.noteOp(21, Binder.getCallingUid(), callingPackage) != 0) {
            return new ArrayList();
        }
        synchronized (this.mMtkLoadLock) {
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh == null) {
                OppoRlog.Rlog.e(LOG_TAG, "Cannot load Sms records. No icc card?");
                this.mSms = null;
                return this.mSms;
            }
            fh.loadEFLinearFixedAll(28476, this.mMtkHandler.obtainMessage(EVENT_MTK_LOAD_DONE));
            try {
                this.mMtkLoadLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to load from the Icc");
            }
            return this.mSms;
        }
    }

    /* access modifiers changed from: protected */
    public ArrayList<SmsRawData> buildValidRawData(ArrayList<byte[]> messages) {
        int count = messages.size();
        ArrayList<SmsRawData> ret = new ArrayList<>(count);
        int validSmsCount = 0;
        for (int i = 0; i < count; i++) {
            if (messages.get(i)[0] == 0) {
                ret.add(null);
            } else {
                validSmsCount++;
                ret.add(new SmsRawData(messages.get(i)));
            }
        }
        log("validSmsCount = " + validSmsCount);
        return ret;
    }

    /* access modifiers changed from: protected */
    public byte[] makeSmsRecordData(int status, byte[] pdu) {
        byte[] data;
        if (1 == this.mPhone.getPhoneType()) {
            data = new byte[176];
        } else {
            data = new byte[255];
        }
        data[0] = (byte) (status & 7);
        log("ISIM-makeSmsRecordData: pdu size = " + pdu.length);
        if (pdu.length == 176) {
            log("ISIM-makeSmsRecordData: sim pdu");
            try {
                System.arraycopy(pdu, 1, data, 1, pdu.length - 1);
            } catch (ArrayIndexOutOfBoundsException e) {
                log("ISIM-makeSmsRecordData: out of bounds, sim pdu");
            }
        } else {
            log("ISIM-makeSmsRecordData: normal pdu");
            try {
                System.arraycopy(pdu, 0, data, 1, pdu.length);
            } catch (ArrayIndexOutOfBoundsException e2) {
                log("ISIM-makeSmsRecordData: out of bounds, normal pdu");
            }
        }
        for (int j = pdu.length + 1; j < data.length; j++) {
            data[j] = -1;
        }
        return data;
    }

    /* access modifiers changed from: protected */
    public void log(String msg) {
        OppoRlog.Rlog.d(LOG_TAG, msg);
    }

    private void loge(String msg) {
        OppoRlog.Rlog.e(LOG_TAG, msg);
    }

    public void sendDataWithOriginalPort(String callingPackage, String destAddr, String scAddr, int destPort, int originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean checkPermission) {
        OppoRlog.Rlog.d(LOG_TAG, "Enter IccSmsInterfaceManager.sendDataWithOriginalPort");
        if (checkPermission) {
            this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
            if (OppoRlog.Rlog.isLoggable("SMS", 2)) {
                log("sendData: data='" + HexDump.toHexString(data) + "' sentIntent=" + sentIntent + " deliveryIntent=" + deliveryIntent);
            }
            if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) != 0) {
                return;
            }
        }
        this.mDispatchersController.sendData(callingPackage, destAddr, scAddr, destPort, originalPort, data, sentIntent, deliveryIntent);
    }

    public void sendMultipartText(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        log("sendMultipartTextMessage");
        if (isValidParameters(destAddr, parts, sentIntents)) {
            if (!checkTddDataOnlyPermission(sentIntents)) {
                log("TDD data only and w/o permission!");
                return;
            }
            ActivityThread.currentApplication().getApplicationContext();
            MtkIccSmsInterfaceManager.super.sendMultipartText(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp);
        }
    }

    public void sendMultipartData(String callingPackage, String destAddr, String scAddr, int destPort, List<SmsRawData> data, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (OppoRlog.Rlog.isLoggable("SMS", 2)) {
            Iterator<SmsRawData> it = data.iterator();
            while (it.hasNext()) {
                log("sendMultipartData:data='" + HexDump.toHexString(it.next().getBytes()));
            }
        }
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            this.mDispatchersController.sendMultipartData(callingPackage, destAddr, scAddr, destPort, (ArrayList) data, (ArrayList) sentIntents, (ArrayList) deliveryIntents);
        }
    }

    public void setSmsMemoryStatus(boolean status) {
        log("setSmsMemoryStatus: set storage status -> " + status);
        this.mDispatchersController.setSmsMemoryStatus(status);
    }

    public boolean isSmsReady() {
        boolean isReady = this.mDispatchersController.isSmsReady();
        log("isSmsReady: " + isReady);
        return isReady;
    }

    public void sendTextWithEncodingType(String callingPackage, String destAddr, String scAddr, String text, int encodingType, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        sendTextWithOptions(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp, -1, false, -1, encodingType);
    }

    public void sendMultipartTextWithEncodingType(String callingPackage, String destAddr, String scAddr, List<String> parts, int encodingType, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) {
        sendMultipartTextWithOptions(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp, -1, false, -1, encodingType);
    }

    public void sendTextWithExtraParams(String callingPackage, String destAddr, String scAddr, String text, Bundle extraParams, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            sendTextInternal(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp, -1, false, extraParams.getInt("validity_period", -1), false);
        }
    }

    public void sendMultipartTextWithExtraParams(String callingPackage, String destAddr, String scAddr, List<String> parts, Bundle extraParams, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            if (!checkTddDataOnlyPermission(sentIntents)) {
                log("TDD data only and w/o permission!");
                return;
            }
            String destAddr2 = filterDestAddress(destAddr);
            int validityPeriod = extraParams.getInt("validity_period", -1);
            if (parts.size() <= 1 || parts.size() >= 10 || SmsMessage.hasEmsSupport()) {
                sendMultipartTextWithOptions(callingPackage, destAddr2, scAddr, parts, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp, -1, false, validityPeriod);
                return;
            }
            for (int i = 0; i < parts.size(); i++) {
                String singlePart = parts.get(i);
                String singlePart2 = SmsMessage.shouldAppendPageNumberAsPrefix() ? String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart : singlePart.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
                PendingIntent singleSentIntent = (sentIntents == null || sentIntents.size() <= i) ? null : sentIntents.get(i);
                PendingIntent singleDeliveryIntent = null;
                if (deliveryIntents != null && deliveryIntents.size() > i) {
                    singleDeliveryIntent = deliveryIntents.get(i);
                }
                sendTextWithOptions(callingPackage, destAddr2, scAddr, singlePart2, singleSentIntent, singleDeliveryIntent, persistMessageForNonDefaultSmsApp, -1, false, validityPeriod);
            }
        }
    }

    public SmsRawData getMessageFromIccEf(String callingPackage, int index) {
        log("getMessageFromIccEf");
        this.mPhone.getContext().enforceCallingPermission("android.permission.RECEIVE_SMS", "Reading messages from SIM");
        if (this.mAppOps.noteOp(21, Binder.getCallingUid(), callingPackage) != 0) {
            return null;
        }
        this.mSmsRawData = null;
        synchronized (this.mMtkLock) {
            if (this.mPhone.getIccFileHandler() != null) {
                this.mPhone.getIccFileHandler().loadEFLinearFixed(28476, index, this.mMtkHandler.obtainMessage(105));
                try {
                    this.mMtkLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to load from the SIM");
                }
            }
        }
        return this.mSmsRawData;
    }

    public List<SmsRawData> getAllMessagesFromIccEfByMode(String callingPackage, int mode) {
        log("getAllMessagesFromIccEfByMode, mode=" + mode);
        if (mode < 1 || mode > 2) {
            log("getAllMessagesFromIccEfByMode wrong mode=" + mode);
            return null;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.RECEIVE_SMS", "Reading messages from Icc");
        if (this.mAppOps.noteOp(21, Binder.getCallingUid(), callingPackage) != 0) {
            return new ArrayList();
        }
        synchronized (this.mMtkLoadLock) {
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh == null) {
                OppoRlog.Rlog.e(LOG_TAG, "Cannot load Sms records. No icc card?");
                if (this.mSms == null) {
                    return null;
                }
                this.mSms.clear();
                return this.mSms;
            }
            Message response = this.mMtkHandler.obtainMessage(EVENT_MTK_LOAD_DONE);
            if (1 == this.mPhone.getPhoneType()) {
                OppoRlog.Rlog.e(LOG_TAG, "getAllMessagesFromIccEfByMode. In the case of GSM phone");
                ((MtkSIMFileHandler) fh).loadEFLinearFixedAll(28476, mode, response);
                try {
                    this.mMtkLoadLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to load from the SIM");
                }
            }
            return this.mSms;
        }
    }

    public MtkSmsParameters getSmsParameters(String callingPackage) {
        InterruptedException e;
        log("getSmsParameters");
        enforceReceiveAndSend("Get SMS parametner on SIM");
        if (this.mAppOps.noteOp(21, Binder.getCallingUid(), callingPackage) != 0) {
            return null;
        }
        synchronized (this.mMtkLock) {
            this.mPhone.mCi.getSmsParameters(this.mMtkHandler.obtainMessage(EVENT_GET_SMS_PARAMS));
            try {
                this.mMtkLock.wait();
            } catch (InterruptedException e2) {
                log("interrupted while trying to get sms params");
            }
            e = this.mSmsParams;
        }
        return e;
    }

    public boolean setSmsParameters(String callingPackage, MtkSmsParameters params) {
        log("setSmsParameters");
        enforceReceiveAndSend("Set SMS parametner on SIM");
        if (this.mAppOps.noteOp(22, Binder.getCallingUid(), callingPackage) != 0) {
            return false;
        }
        this.mSmsParamsSuccess = false;
        synchronized (this.mMtkLock) {
            this.mPhone.mCi.setSmsParameters(params, this.mMtkHandler.obtainMessage(104));
            try {
                this.mMtkLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get sms params");
            }
        }
        return this.mSmsParamsSuccess;
    }

    public int copyTextMessageToIccCard(String callingPkg, String scAddress, String address, List<String> text, int status, long timestamp) {
        log("copyTextMessageToIccCard, message count: " + text.size() + " status: " + status);
        enforceReceiveAndSend("Copying message to USIM/SIM");
        if (this.mAppOps.noteOp(22, Binder.getCallingUid(), callingPkg) != 0) {
            return 1;
        }
        MtkIccSmsStorageStatus memStatus = getSmsSimMemoryStatus(callingPkg);
        if (memStatus == null) {
            log("Fail to get SIM memory status");
            return 1;
        } else if (memStatus.getUnused() >= text.size()) {
            return this.mDispatchersController.copyTextMessageToIccCard(scAddress, address, text, status, timestamp);
        } else {
            log("SIM memory is not enough");
            return 7;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:63:0x0187  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x01a1  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01a7  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x020f  */
    public MtkSimSmsInsertStatus insertTextMessageToIccCard(String callingPackage, String scAddress, String address, List<String> text, int status, long timestamp) {
        String scAddress2;
        boolean isDeliverPdu;
        int encoding_detail;
        int lockingShiftId;
        int lockingShiftId2;
        int msgCount;
        byte[] smsHeader;
        GsmAlphabet.TextEncodingDetails[] details;
        InterruptedException e;
        log("insertTextMessageToIccCard");
        enforceReceiveAndSend("insertText insert message into SIM");
        int i = 1;
        if (this.mAppOps.noteOp(22, Binder.getCallingUid(), callingPackage) != 0) {
            MtkSimSmsInsertStatus mtkSimSmsInsertStatus = this.smsInsertRet;
            mtkSimSmsInsertStatus.insertStatus = 1;
            return mtkSimSmsInsertStatus;
        }
        int msgCount2 = text.size();
        log("insertText msgCount=" + msgCount2 + ", status=" + status);
        this.smsInsertRet.indexInIcc = "";
        MtkIccSmsStorageStatus memStatus = getSmsSimMemoryStatus(callingPackage);
        if (memStatus != null) {
            int unused = memStatus.getUnused();
            if (unused < msgCount2) {
                log("insertText SIM mem is not enough [" + unused + "/" + msgCount2 + "]");
                MtkSimSmsInsertStatus mtkSimSmsInsertStatus2 = this.smsInsertRet;
                mtkSimSmsInsertStatus2.insertStatus = 7;
                return mtkSimSmsInsertStatus2;
            }
            if (!checkPhoneNumberInternal(scAddress)) {
                log("insertText invalid sc address");
                scAddress2 = null;
            } else {
                scAddress2 = scAddress;
            }
            if (!checkPhoneNumberInternal(address)) {
                log("insertText invalid address");
                MtkSimSmsInsertStatus mtkSimSmsInsertStatus3 = this.smsInsertRet;
                mtkSimSmsInsertStatus3.insertStatus = 8;
                return mtkSimSmsInsertStatus3;
            }
            if (status == 1 || status == 3) {
                log("insertText to encode delivery pdu");
                isDeliverPdu = true;
            } else if (status == 5 || status == 7) {
                log("insertText to encode submit pdu");
                isDeliverPdu = false;
            } else {
                log("insertText invalid status " + status);
                MtkSimSmsInsertStatus mtkSimSmsInsertStatus4 = this.smsInsertRet;
                mtkSimSmsInsertStatus4.insertStatus = 1;
                return mtkSimSmsInsertStatus4;
            }
            log("insertText params check pass");
            if (2 == this.mPhone.getPhoneType()) {
                return writeTextMessageToRuim(address, text, status, timestamp);
            }
            GsmAlphabet.TextEncodingDetails[] details2 = new GsmAlphabet.TextEncodingDetails[msgCount2];
            int encoding = 0;
            for (int i2 = 0; i2 < msgCount2; i2++) {
                details2[i2] = com.android.internal.telephony.gsm.SmsMessage.calculateLength(text.get(i2), false);
                if (encoding != details2[i2].codeUnitSize && (encoding == 0 || encoding == 1)) {
                    encoding = details2[i2].codeUnitSize;
                }
            }
            log("insertText create & insert pdu start...");
            int i3 = 0;
            while (i3 < msgCount2) {
                if (this.mInsertMessageSuccess || i3 <= 0) {
                    int language = details2[i3].shiftLangId;
                    if (encoding == i) {
                        if (details2[i3].languageTable <= 0 || details2[i3].languageShiftTable <= 0) {
                            if (details2[i3].languageShiftTable > 0) {
                                lockingShiftId = details2[i3].languageShiftTable;
                                encoding_detail = 12;
                                lockingShiftId2 = -1;
                            } else if (details2[i3].languageTable > 0) {
                                lockingShiftId = -1;
                                encoding_detail = 11;
                                lockingShiftId2 = details2[i3].languageTable;
                            }
                            if (msgCount2 > i) {
                                log("insertText create pdu header for concat-message");
                                msgCount = msgCount2;
                                smsHeader = MtkSmsHeader.getSubmitPduHeaderWithLang(-1, getNextConcatRef() & 255, i3 + 1, msgCount2, lockingShiftId2, lockingShiftId);
                            } else {
                                msgCount = msgCount2;
                                smsHeader = null;
                            }
                            if (isDeliverPdu) {
                                MtkSmsMessage.DeliverPdu pdu = MtkSmsMessage.getDeliverPduWithLang(scAddress2, address, text.get(i3), smsHeader, timestamp, encoding_detail, language);
                                if (pdu != null) {
                                    synchronized (this.mSimInsertLock) {
                                        try {
                                            details = details2;
                                            try {
                                                this.mPhone.mCi.writeSmsToSim(status, IccUtils.bytesToHexString(pdu.encodedScAddress), IccUtils.bytesToHexString(pdu.encodedMessage), this.mMtkHandler.obtainMessage(102));
                                                try {
                                                    log("insertText wait until the pdu be wrote into the SIM");
                                                    this.mSimInsertLock.wait();
                                                } catch (InterruptedException e2) {
                                                    log("insertText fail to insert pdu");
                                                    this.smsInsertRet.insertStatus = 1;
                                                    return this.smsInsertRet;
                                                }
                                            } catch (Throwable th) {
                                                e = th;
                                                throw e;
                                            }
                                        } catch (Throwable th2) {
                                            e = th2;
                                            throw e;
                                        }
                                    }
                                } else {
                                    log("insertText fail to create deliver pdu");
                                    MtkSimSmsInsertStatus mtkSimSmsInsertStatus5 = this.smsInsertRet;
                                    mtkSimSmsInsertStatus5.insertStatus = 1;
                                    return mtkSimSmsInsertStatus5;
                                }
                            } else {
                                details = details2;
                                SmsMessage.SubmitPdu pdu2 = MtkSmsMessage.getSubmitPduWithLang(scAddress2, address, text.get(i3), false, smsHeader, encoding_detail, language, -1);
                                if (pdu2 != null) {
                                    synchronized (this.mSimInsertLock) {
                                        this.mPhone.mCi.writeSmsToSim(status, IccUtils.bytesToHexString(pdu2.encodedScAddress), IccUtils.bytesToHexString(pdu2.encodedMessage), this.mMtkHandler.obtainMessage(102));
                                        try {
                                            log("insertText wait until the pdu be wrote into the SIM");
                                            this.mSimInsertLock.wait();
                                        } catch (InterruptedException e3) {
                                            log("insertText fail to insert pdu");
                                            this.smsInsertRet.insertStatus = 1;
                                            return this.smsInsertRet;
                                        }
                                    }
                                } else {
                                    log("insertText fail to create submit pdu");
                                    MtkSimSmsInsertStatus mtkSimSmsInsertStatus6 = this.smsInsertRet;
                                    mtkSimSmsInsertStatus6.insertStatus = 1;
                                    return mtkSimSmsInsertStatus6;
                                }
                            }
                            i3++;
                            msgCount2 = msgCount;
                            details2 = details;
                            i = 1;
                        } else {
                            int singleShiftId = details2[i3].languageTable;
                            lockingShiftId = details2[i3].languageShiftTable;
                            encoding_detail = 13;
                            lockingShiftId2 = singleShiftId;
                            if (msgCount2 > i) {
                            }
                            if (isDeliverPdu) {
                            }
                            i3++;
                            msgCount2 = msgCount;
                            details2 = details;
                            i = 1;
                        }
                    }
                    lockingShiftId = -1;
                    encoding_detail = encoding;
                    lockingShiftId2 = -1;
                    if (msgCount2 > i) {
                    }
                    if (isDeliverPdu) {
                    }
                    i3++;
                    msgCount2 = msgCount;
                    details2 = details;
                    i = 1;
                } else {
                    log("insertText last message insert fail");
                    MtkSimSmsInsertStatus mtkSimSmsInsertStatus7 = this.smsInsertRet;
                    mtkSimSmsInsertStatus7.insertStatus = i;
                    return mtkSimSmsInsertStatus7;
                }
            }
            log("insertText create & insert pdu end");
            if (this.mInsertMessageSuccess == i) {
                log("insertText all messages inserted");
                MtkSimSmsInsertStatus mtkSimSmsInsertStatus8 = this.smsInsertRet;
                mtkSimSmsInsertStatus8.insertStatus = i;
                return mtkSimSmsInsertStatus8;
            }
            log("insertText pdu insert fail");
            MtkSimSmsInsertStatus mtkSimSmsInsertStatus9 = this.smsInsertRet;
            mtkSimSmsInsertStatus9.insertStatus = i;
            return mtkSimSmsInsertStatus9;
        }
        log("insertText fail to get SIM mem status");
        MtkSimSmsInsertStatus mtkSimSmsInsertStatus10 = this.smsInsertRet;
        mtkSimSmsInsertStatus10.insertStatus = 1;
        return mtkSimSmsInsertStatus10;
    }

    public MtkSimSmsInsertStatus insertRawMessageToIccCard(String callingPackage, int status, byte[] pdu, byte[] smsc) {
        log("insertRawMessageToIccCard");
        enforceReceiveAndSend("insertRaw insert message into SIM");
        if (this.mAppOps.noteOp(22, Binder.getCallingUid(), callingPackage) != 0) {
            MtkSimSmsInsertStatus mtkSimSmsInsertStatus = this.smsInsertRet2;
            mtkSimSmsInsertStatus.insertStatus = 1;
            return mtkSimSmsInsertStatus;
        }
        synchronized (this.mMtkLock) {
            this.mMtkSuccess = false;
            this.smsInsertRet2.insertStatus = 1;
            this.smsInsertRet2.indexInIcc = "";
            Message response = this.mMtkHandler.obtainMessage(EVENT_MTK_UPDATE_DONE);
            if (2 != this.mPhone.getPhoneType()) {
                this.mPhone.mCi.writeSmsToSim(status, IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), response);
            } else {
                this.mPhone.mCi.writeSmsToRuim(status, IccUtils.bytesToHexString(pdu), response);
            }
            try {
                this.mMtkLock.wait();
            } catch (InterruptedException e) {
                log("insertRaw interrupted while trying to update by index");
            }
        }
        if (this.mMtkSuccess) {
            log("insertRaw message inserted");
            MtkSimSmsInsertStatus mtkSimSmsInsertStatus2 = this.smsInsertRet2;
            mtkSimSmsInsertStatus2.insertStatus = 0;
            return mtkSimSmsInsertStatus2;
        }
        log("insertRaw pdu insert fail");
        MtkSimSmsInsertStatus mtkSimSmsInsertStatus3 = this.smsInsertRet2;
        mtkSimSmsInsertStatus3.insertStatus = 1;
        return mtkSimSmsInsertStatus3;
    }

    public MtkIccSmsStorageStatus getSmsSimMemoryStatus(String callingPackage) {
        log("getSmsSimMemoryStatus");
        enforceReceiveAndSend("Get SMS SIM Card Memory Status from RUIM");
        if (this.mAppOps.noteOp(21, Binder.getCallingUid(), callingPackage) != 0) {
            return null;
        }
        synchronized (this.mMtkLock) {
            this.mMtkSuccess = false;
            Message response = this.mMtkHandler.obtainMessage(101);
            MtkRIL ci = this.mPhone.mCi;
            if (this.mPhone.getPhoneType() == 2) {
                ci.getSmsRuimMemoryStatus(response);
            } else {
                ci.getSmsSimMemoryStatus(response);
            }
            try {
                this.mMtkLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get SMS SIM Card Memory Status from SIM");
            }
        }
        if (this.mMtkSuccess) {
            return this.mSimMemStatus;
        }
        return null;
    }

    private static int getNextConcatRef() {
        int i = sConcatenatedRef;
        sConcatenatedRef = i + 1;
        return i;
    }

    private static boolean checkPhoneNumberCharacter(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '+' || c == '#' || c == 'N' || c == ' ' || c == '-';
    }

    private static boolean checkPhoneNumberInternal(String number) {
        if (number == null) {
            return true;
        }
        int n = number.length();
        for (int i = 0; i < n; i++) {
            if (!checkPhoneNumberCharacter(number.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public MtkSimSmsInsertStatus writeTextMessageToRuim(String address, List<String> text, int status, long timestamp) {
        MtkSimSmsInsertStatus insertRet = new MtkSimSmsInsertStatus(0, "");
        this.mMtkSuccess = true;
        for (int i = 0; i < text.size(); i++) {
            if (!this.mMtkSuccess) {
                log("[copyText Exception happened when copy message");
                insertRet.insertStatus = 1;
                return insertRet;
            }
            SmsMessage.SubmitPdu pdu = com.mediatek.internal.telephony.cdma.MtkSmsMessage.createEfPdu(address, text.get(i), timestamp);
            if (pdu != null) {
                synchronized (this.mSimInsertLock) {
                    this.mPhone.mCi.writeSmsToRuim(status, IccUtils.bytesToHexString(pdu.encodedMessage), this.mMtkHandler.obtainMessage(102));
                    this.mInserted = false;
                    try {
                        this.mSimInsertLock.wait();
                    } catch (InterruptedException e) {
                        log("InterruptedException " + e);
                        insertRet.insertStatus = 1;
                        return insertRet;
                    }
                }
            } else {
                log("writeTextMessageToRuim: pdu == null");
                insertRet.insertStatus = 1;
                return insertRet;
            }
        }
        log("writeTextMessageToRuim: done");
        insertRet.insertStatus = 0;
        return insertRet;
    }

    private String filterDestAddress(String destAddr) {
        String result = SmsNumberUtils.filterDestAddr(this.mPhone, destAddr);
        return result != null ? result : destAddr;
    }

    private static boolean isValidParameters(String destinationAddress, String text, PendingIntent sentIntent) {
        List<PendingIntent> sentIntents = new ArrayList<>();
        List<String> parts = new ArrayList<>();
        sentIntents.add(sentIntent);
        parts.add(text);
        return isValidParameters(destinationAddress, parts, sentIntents);
    }

    private static boolean isValidParameters(String destinationAddress, List<String> parts, List<PendingIntent> sentIntents) {
        if (parts == null || parts.size() == 0) {
            return true;
        }
        if (!isValidSmsDestinationAddress(destinationAddress)) {
            for (int i = 0; i < sentIntents.size(); i++) {
                PendingIntent sentIntent = sentIntents.get(i);
                if (sentIntent != null) {
                    try {
                        sentIntent.send(1);
                    } catch (PendingIntent.CanceledException e) {
                    }
                }
            }
            OppoRlog.Rlog.d("IccSmsInterfaceManagerEx", "Invalid destinationAddress: " + destinationAddress);
            return false;
        } else if (TextUtils.isEmpty(destinationAddress)) {
            OppoRlog.Rlog.e("IccSmsInterfaceManagerEx", "Invalid destinationAddress");
            return false;
        } else if (parts.size() >= 1) {
            return true;
        } else {
            OppoRlog.Rlog.e("IccSmsInterfaceManagerEx", "Invalid message body");
            return false;
        }
    }

    private static boolean isValidSmsDestinationAddress(String da) {
        String encodeAddress = PhoneNumberUtils.extractNetworkPortion(da);
        if (encodeAddress == null) {
            return true;
        }
        return true ^ encodeAddress.isEmpty();
    }

    public boolean activateCellBroadcastSms(boolean activate) {
        log("activateCellBroadcastSms activate : " + activate);
        return setCellBroadcastActivation(activate);
    }

    public boolean queryCellBroadcastSmsActivation() {
        log("queryCellBroadcastSmsActivation");
        synchronized (this.mMtkLock) {
            Message response = this.mMtkHandler.obtainMessage(EVENT_GET_BROADCAST_ACTIVATION_DONE);
            this.mMtkSuccess = false;
            this.mPhone.mCi.getGsmBroadcastActivation(response);
            try {
                this.mMtkLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get CB activation");
            }
        }
        return this.mMtkSuccess;
    }

    public String getCellBroadcastRanges() {
        log("getCellBroadcastChannels");
        synchronized (this.mMtkLock) {
            Message response = this.mMtkHandler.obtainMessage(EVENT_GET_BROADCAST_CONFIG_CHANNEL_DONE);
            this.mSmsCbChannelConfig = "";
            this.mPhone.mCi.getGsmBroadcastConfig(response);
            try {
                this.mMtkLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get CB config");
            }
        }
        return this.mSmsCbChannelConfig;
    }

    public boolean setCellBroadcastLangs(String lang) {
        log("setCellBroadcastLangs");
        synchronized (this.mMtkLock) {
            Message response = this.mMtkHandler.obtainMessage(109);
            this.mMtkSuccess = false;
            this.mPhone.mCi.setGsmBroadcastLangs(lang, response);
            try {
                this.mMtkLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get CB config");
            }
        }
        return this.mMtkSuccess;
    }

    public String getCellBroadcastLangs() {
        String str;
        log("getCellBroadcastLangs");
        synchronized (this.mMtkLock) {
            Message response = this.mMtkHandler.obtainMessage(110);
            this.mSmsCbLanguageConfig = "";
            this.mPhone.mCi.getGsmBroadcastLangs(response);
            try {
                this.mMtkLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get CB config");
            }
            str = this.mSmsCbLanguageConfig;
        }
        return str;
    }

    public boolean removeCellBroadcastMsg(int channelId, int serialId) {
        log("removeCellBroadcastMsg(" + channelId + " , " + serialId + ")");
        synchronized (this.mMtkLock) {
            Message response = this.mMtkHandler.obtainMessage(EVENT_REMOVE_BROADCAST_MSG_DONE);
            this.mMtkSuccess = false;
            this.mPhone.mCi.removeCellBroadcastMsg(channelId, serialId, response);
            try {
                this.mMtkLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to remove CB msg");
            }
        }
        return this.mMtkSuccess;
    }

    public boolean setEtwsConfig(int mode) {
        log("Calling setEtwsConfig(" + mode + ')');
        synchronized (this.mMtkLock) {
            Message response = this.mMtkHandler.obtainMessage(111);
            this.mMtkSuccess = false;
            this.mPhone.mCi.setEtws(mode, response);
            try {
                this.mMtkLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set ETWS config");
            }
        }
        return this.mMtkSuccess;
    }

    public String getScAddress() {
        log("getScAddress");
        synchronized (this.mMtkLock) {
            this.mPhone.getSmscAddress(this.mMtkHandler.obtainMessage(112));
            try {
                this.mMtkLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get SMSC address");
            }
        }
        log("getScAddress: exit");
        return this.mSmscAddress;
    }

    public Bundle getScAddressWithErrorCode() {
        log("getScAddressWithErrorCode");
        synchronized (this.mMtkLock) {
            this.mPhone.getSmscAddress(this.mMtkHandler.obtainMessage(113));
            try {
                this.mMtkLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to get SMSC address and error code");
            }
        }
        log("getScAddressWithErrorCode error code done");
        return this.mSmscAddressBundle;
    }

    public boolean setScAddress(String address) {
        log("setScAddressUsingSubId");
        synchronized (this.mMtkLock) {
            Message response = this.mMtkHandler.obtainMessage(114);
            this.mMtkSuccess = false;
            this.mPhone.setSmscAddress(address, response);
            try {
                this.mMtkLock.wait();
            } catch (InterruptedException e) {
                log("interrupted while trying to set SMSC address");
            }
        }
        log("setScAddressUsingSubId result " + this.mMtkSuccess);
        return this.mMtkSuccess;
    }

    private boolean checkTddDataOnlyPermission(PendingIntent sentIntent) {
        if (new MtkLteDataOnlyController(this.mContext).checkPermission(this.mPhone.getSubId())) {
            return true;
        }
        log("checkTddDataOnlyPermission, w/o permission, sentIntent = " + sentIntent);
        if (sentIntent == null) {
            log("checkTddDataOnlyPermission, can not notify APP");
            return false;
        }
        try {
            sentIntent.send(1);
            return false;
        } catch (PendingIntent.CanceledException e) {
            loge("checkTddDataOnlyPermission, CanceledException happened when send sms fail with sentIntent");
            return false;
        }
    }

    private boolean checkTddDataOnlyPermission(List<PendingIntent> sentIntents) {
        if (new MtkLteDataOnlyController(this.mContext).checkPermission(this.mPhone.getSubId())) {
            return true;
        }
        log("checkTddDataOnlyPermission, w/o permission, sentIntents = " + sentIntents);
        if (sentIntents == null) {
            log("checkTddDataOnlyPermission, can not notify APP");
            return false;
        }
        try {
            int size = sentIntents.size();
            for (int i = 0; i < size; i++) {
                PendingIntent si = sentIntents.get(i);
                if (si == null) {
                    log("checkTddDataOnlyPermission, can not notify APP for i = " + i);
                } else {
                    si.send(1);
                }
            }
            return false;
        } catch (PendingIntent.CanceledException e) {
            loge("checkTddDataOnlyPermission, CanceledException happened when send sms fail with sentIntent");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean setCellBroadcastActivation(boolean activate) {
        log("Calling proprietary setCellBroadcastActivation(" + activate + ')');
        if (this.mCurrentCellBroadcastActivation != activate) {
            MtkIccSmsInterfaceManager.super.setCellBroadcastActivation(activate);
        } else {
            this.mSuccess = true;
        }
        if (this.mSuccess && this.mCurrentCellBroadcastActivation != activate) {
            this.mCurrentCellBroadcastActivation = activate ? 1 : 0;
            log("mCurrentCellBroadcastActivation change to " + this.mCurrentCellBroadcastActivation);
        }
        return this.mSuccess;
    }

    public void sendTextWithOptions(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            if (!checkTddDataOnlyPermission(sentIntent)) {
                log("TDD data only and w/o permission!");
            } else {
                this.mDispatchersController.sendTextWithEncodingType(destAddr, scAddr, text, encodingType, sentIntent, deliveryIntent, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
            }
        }
    }

    public void sendMultipartTextWithOptions(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        this.mPhone.getContext().enforceCallingPermission("android.permission.SEND_SMS", "Sending SMS message");
        if (this.mAppOps.noteOp(20, Binder.getCallingUid(), callingPackage) == 0) {
            if (!checkTddDataOnlyPermission(sentIntents)) {
                log("TDD data only and w/o permission!");
                return;
            }
            String destAddr2 = filterDestAddress(destAddr);
            if (parts.size() <= 1 || parts.size() >= 10 || android.telephony.SmsMessage.hasEmsSupport()) {
                this.mDispatchersController.sendMultipartTextWithEncodingType(destAddr2, scAddr, (ArrayList) parts, encodingType, (ArrayList) sentIntents, (ArrayList) deliveryIntents, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
                return;
            }
            for (int i = 0; i < parts.size(); i++) {
                String singlePart = parts.get(i);
                String singlePart2 = android.telephony.SmsMessage.shouldAppendPageNumberAsPrefix() ? String.valueOf(i + 1) + '/' + parts.size() + ' ' + singlePart : singlePart.concat(' ' + String.valueOf(i + 1) + '/' + parts.size());
                PendingIntent singleSentIntent = (sentIntents == null || sentIntents.size() <= i) ? null : sentIntents.get(i);
                PendingIntent singleDeliveryIntent = null;
                if (deliveryIntents != null && deliveryIntents.size() > i) {
                    singleDeliveryIntent = deliveryIntents.get(i);
                }
                ((MtkSmsDispatchersController) this.mDispatchersController).sendTextWithEncodingType(destAddr2, scAddr, singlePart2, encodingType, singleSentIntent, singleDeliveryIntent, null, callingPackage, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
            }
        }
    }
}
