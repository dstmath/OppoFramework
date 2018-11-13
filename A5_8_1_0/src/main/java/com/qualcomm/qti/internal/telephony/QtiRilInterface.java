package com.qualcomm.qti.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.Log;
import com.qualcomm.qcrilhook.IQcRilHook;
import com.qualcomm.qcrilhook.OemHookCallback;
import com.qualcomm.qcrilhook.QcRilHook;
import com.qualcomm.qcrilhook.QcRilHookCallback;
import com.qualcomm.qti.internal.telephony.QtiUiccCardProvisioner.UiccProvisionStatus;
import com.qualcomm.qti.internal.telephony.dataconnection.QtiApnProfileOmh;
import com.qualcomm.qti.internal.telephony.dataconnection.QtiApnSetting;
import com.qualcomm.qti.internal.telephony.uicccontact.QtiSimPhoneBookAdnRecord;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import org.codeaurora.internal.IDepersoResCallback;

public class QtiRilInterface implements BaseRilInterface {
    private static final int BYTE_SIZE = 1;
    private static final int INT_SIZE = 4;
    private static final String LOG_TAG = "QtiRilInterface";
    private static final char NULL_TERMINATOR = '\u0000';
    private static final int NULL_TERMINATOR_LENGTH = 1;
    private static final int SHORT_SIZE = 2;
    private static boolean mIsServiceReady = false;
    private static QtiRilInterface sInstance = null;
    private final String ACTION_ADN_INIT_DONE = "qualcomm.intent.action.ACTION_ADN_INIT_DONE";
    private final String ACTION_ADN_RECORDS_IND = "qualcomm.intent.action.ACTION_ADN_RECORDS_IND";
    private String OMH_FAKE_QCRIL_HOOK_RESPONSE = "persist.test.omh.fakeprofile";
    private RegistrantList mAdnInitDoneRegistrantList;
    private RegistrantList mAdnRecordsInfoRegistrantList;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            QtiRilInterface.this.logd("Received " + intent.getAction());
            if ("qualcomm.intent.action.ACTION_ADN_INIT_DONE".equals(intent.getAction())) {
                QtiRilInterface.this.mAdnInitDoneRegistrantList.notifyRegistrants(new AsyncResult(null, null, null));
            } else if ("qualcomm.intent.action.ACTION_ADN_RECORDS_IND".equals(intent.getAction())) {
                QtiRilInterface.this.mAdnRecordsInfoRegistrantList.notifyRegistrants(new AsyncResult(null, QtiRilInterface.this.responseAdnRecords(intent.getByteArrayExtra("adn_records")), null));
            }
        }
    };
    private QcRilHook mQcRilHook;
    private QcRilHookCallback mQcrilHookCb = new QcRilHookCallback() {
        public void onQcRilHookReady() {
            QtiRilInterface.mIsServiceReady = true;
            QtiRilInterface.this.logd("Service ready, notifying registrants");
            QtiRilInterface.this.mServiceReadyRegistrantList.notifyRegistrants(new AsyncResult(null, Boolean.valueOf(QtiRilInterface.mIsServiceReady), null));
        }

        public synchronized void onQcRilHookDisconnected() {
            QtiRilInterface.mIsServiceReady = false;
            QtiRilInterface.this.logd("Service disconnected, notifying registrants");
            QtiRilInterface.this.mServiceReadyRegistrantList.notifyRegistrants(new AsyncResult(null, Boolean.valueOf(QtiRilInterface.mIsServiceReady), null));
            QtiRilInterface.sInstance = null;
        }
    };
    private RegistrantList mServiceReadyRegistrantList;

    protected class AdnOemHookCallback extends OemHookCallback {
        Message mAppMessage;
        int mRspLength;

        public AdnOemHookCallback(Message msg, int length) {
            super(msg);
            this.mAppMessage = msg;
            this.mRspLength = length;
        }

        public void onOemHookResponse(byte[] response, int phoneId) throws RemoteException {
            if (response != null) {
                QtiRilInterface.this.logd("AdnOemHookCallback: onOemHookResponse = " + response.toString());
                AsyncResult.forMessage(this.mAppMessage, QtiRilInterface.this.parseInts(response, this.mRspLength), null);
            } else {
                AsyncResult.forMessage(this.mAppMessage, null, new Exception("QCRIL_EVT_HOOK_GET_ADN_RECORD failed"));
            }
            this.mAppMessage.sendToTarget();
        }

        public void onOemHookException(int phoneId) throws RemoteException {
            QtiRilInterface.this.logd("AdnOemHookCallback: onOemHookException");
            AsyncResult.forMessage(this.mAppMessage, null, new Exception("com.android.internal.telephony.CommandException: MODEM_ERR"));
            this.mAppMessage.sendToTarget();
        }
    }

    private class DepersoCallback extends OemHookCallback {
        int ERROR = 1;
        int SUCCESS = 0;
        IDepersoResCallback depersoCallBack;

        public DepersoCallback(IDepersoResCallback callback, Message msg) {
            super(msg);
            this.depersoCallBack = callback;
        }

        public void onOemHookResponse(byte[] response, int phoneId) throws RemoteException {
            if (response != null) {
                QtiRilInterface.this.logd("DepersoResult SUCCESS");
                this.depersoCallBack.onDepersoResult(this.SUCCESS, phoneId);
                return;
            }
            QtiRilInterface.this.logd("DepersoResult ERROR");
            this.depersoCallBack.onDepersoResult(this.ERROR, phoneId);
        }

        public void onOemHookException(int phoneId) throws RemoteException {
            QtiRilInterface.this.logd("DepersoResult ERROR");
            this.depersoCallBack.onDepersoResult(this.ERROR, phoneId);
        }
    }

    private class OmhCallProfileCallback extends OemHookCallback {
        Message mAppMessage;
        int mModemApnType;

        public OmhCallProfileCallback(int modemApnType, Message msg) {
            super(msg);
            this.mAppMessage = msg;
            this.mModemApnType = modemApnType;
        }

        public void onOemHookResponse(byte[] response, int phoneId) throws RemoteException {
            ArrayList<QtiApnSetting> profiles = new ArrayList();
            if (SystemProperties.getBoolean(QtiRilInterface.this.OMH_FAKE_QCRIL_HOOK_RESPONSE, false)) {
                QtiRilInterface.this.logi("Getting fake omh profiles");
                AsyncResult.forMessage(this.mAppMessage, QtiRilInterface.this.getFakeOmhProfiles(this.mModemApnType), null);
            } else if (response != null) {
                Log.d(QtiRilInterface.LOG_TAG, "getOmhCallProfile: onOemHookResponse = " + response.toString());
                AsyncResult.forMessage(this.mAppMessage, QtiRilInterface.this.parseOmhProfiles(response), null);
            } else {
                AsyncResult.forMessage(this.mAppMessage, profiles, new Exception("QCRIL_EVT_HOOK_GET_OMH_CALL_PROFILE failed"));
            }
            this.mAppMessage.sendToTarget();
        }
    }

    public static synchronized QtiRilInterface getInstance(Context context) {
        QtiRilInterface qtiRilInterface;
        synchronized (QtiRilInterface.class) {
            if (sInstance == null) {
                sInstance = new QtiRilInterface(context);
            } else {
                Log.d(LOG_TAG, "instance = " + sInstance);
            }
            qtiRilInterface = sInstance;
        }
        return qtiRilInterface;
    }

    private QtiRilInterface(Context context) {
        logd(" in constructor ");
        this.mServiceReadyRegistrantList = new RegistrantList();
        this.mAdnInitDoneRegistrantList = new RegistrantList();
        this.mAdnRecordsInfoRegistrantList = new RegistrantList();
        try {
            this.mQcRilHook = new QcRilHook(context, this.mQcrilHookCb);
        } catch (SecurityException se) {
            loge("SecurityException during QcRilHook init: " + se);
            mIsServiceReady = false;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("qualcomm.intent.action.ACTION_ADN_INIT_DONE");
        intentFilter.addAction("qualcomm.intent.action.ACTION_ADN_RECORDS_IND");
        context.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    public UiccProvisionStatus getUiccProvisionPreference(int phoneId) {
        UiccProvisionStatus provStatus = new UiccProvisionStatus();
        AsyncResult ar = this.mQcRilHook.sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_GET_UICC_PROVISION_PREFERENCE, new byte[0], phoneId);
        if (ar.exception == null && ar.result != null) {
            ByteBuffer byteBuf = ByteBuffer.wrap((byte[]) ar.result);
            byteBuf.order(ByteOrder.nativeOrder());
            logd("Data received: " + byteBuf.toString());
            provStatus.setUserPreference(byteBuf.getInt());
            provStatus.setCurrentState(byteBuf.getInt());
        }
        logi("get pref, phoneId " + phoneId + " " + provStatus + " exception " + ar.exception);
        return provStatus;
    }

    private ArrayList<QtiApnSetting> parseOmhProfiles(byte[] buffer) {
        ArrayList<QtiApnSetting> profilesList = new ArrayList();
        ByteBuffer byteBuf = ByteBuffer.wrap(buffer);
        if (byteBuf != null) {
            byteBuf.order(ByteOrder.nativeOrder());
            logi("Data received: " + byteBuf.toString());
            int nProfiles = byteBuf.getInt();
            for (int i = 0; i < nProfiles; i++) {
                int profileId = byteBuf.getInt();
                int priority = byteBuf.getInt();
                QtiApnProfileOmh profile = new QtiApnProfileOmh(profileId, priority);
                logi("getOmhCallProfile " + profileId + ":" + priority);
                profilesList.add(profile);
            }
        }
        return profilesList;
    }

    private ArrayList<QtiApnSetting> getFakeOmhProfiles(int profileId) {
        int[] prioritySortedProfiles = new int[]{2, 32, 64, 1};
        ArrayList<QtiApnSetting> profilesList = new ArrayList();
        for (int i = 0; i < prioritySortedProfiles.length; i++) {
            if (prioritySortedProfiles[i] == profileId) {
                QtiApnProfileOmh profile = new QtiApnProfileOmh(prioritySortedProfiles[i], i);
                logi("profile(id=" + profileId + ") =" + profile);
                profilesList.add(profile);
                break;
            }
        }
        return profilesList;
    }

    public void getOmhCallProfile(int modemApnType, Message callbackMsg, int phoneId) {
        logi("getOmhCallProfile()");
        byte[] requestData = new byte[4];
        QcRilHook qcRilHook = this.mQcRilHook;
        QcRilHook.createBufferWithNativeByteOrder(requestData).putInt(modemApnType);
        this.mQcRilHook.sendQcRilHookMsgAsync(IQcRilHook.QCRIL_EVT_HOOK_GET_OMH_CALL_PROFILE, requestData, new OmhCallProfileCallback(modemApnType, callbackMsg), phoneId);
    }

    public void supplyIccDepersonalization(String netpin, String type, IDepersoResCallback callback, int phoneId) {
        int i;
        logd("supplyDepersonalization: netpin = " + netpin + " type = " + type + "phoneId = " + phoneId);
        Message msg = Message.obtain();
        int length = type.length() + 1;
        if (netpin == null) {
            i = 1;
        } else {
            i = netpin.length() + 1;
        }
        byte[] payload = new byte[(length + i)];
        QcRilHook qcRilHook = this.mQcRilHook;
        ByteBuffer buf = QcRilHook.createBufferWithNativeByteOrder(payload);
        buf.put(type.getBytes());
        buf.put((byte) 0);
        if (netpin != null) {
            buf.put(netpin.getBytes());
        }
        buf.put((byte) 0);
        this.mQcRilHook.sendQcRilHookMsgAsync(IQcRilHook.QCRIL_EVT_HOOK_ENTER_DEPERSONALIZATION_CODE, payload, new DepersoCallback(callback, msg), phoneId);
    }

    public String getUiccIccId(int phoneId) {
        String str = null;
        byte[] requestData = new byte[4];
        QcRilHook qcRilHook = this.mQcRilHook;
        QcRilHook.createBufferWithNativeByteOrder(requestData).putInt(phoneId);
        AsyncResult ar = this.mQcRilHook.sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_GET_UICC_ICCID, requestData, phoneId);
        if (ar.exception == null && ar.result != null) {
            str = new String((byte[]) ar.result);
        }
        logi("getUiccIccId iccId[" + phoneId + "] = " + str + " exception: " + ar.exception);
        return str;
    }

    public int getMaxDataAllowed() {
        int maxData = 0;
        AsyncResult ar = this.mQcRilHook.sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_MAX_DATA_ALLOWED);
        if (ar.exception == null && ar.result != null) {
            maxData = ByteBuffer.wrap(ar.result).get();
        }
        logi("getMaxDataAllowed maxData = " + maxData + " exception: " + ar.exception);
        return maxData;
    }

    public boolean getLpluslSupportStatus() {
        boolean status = false;
        AsyncResult ar = this.mQcRilHook.sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_REQ_HOOK_GET_L_PLUS_L_FEATURE_SUPPORT_STATUS_REQ);
        if (ar.exception == null && ar.result != null) {
            status = (ByteBuffer.wrap((byte[]) ar.result).get() & 1) == 1;
        }
        logi("getLpluslSupportStatus: " + status + " exception: " + ar.exception);
        return status;
    }

    public void sendPhoneStatus(int isReady, int phoneId) {
        byte[] requestData = new byte[1];
        QcRilHook qcRilHook = this.mQcRilHook;
        QcRilHook.createBufferWithNativeByteOrder(requestData).put((byte) isReady);
        AsyncResult ar = this.mQcRilHook.sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_SET_ATEL_UI_STATUS, requestData, phoneId);
    }

    public boolean setUiccProvisionPreference(int userPref, int phoneId) {
        boolean retval = false;
        byte[] requestData = new byte[8];
        QcRilHook qcRilHook = this.mQcRilHook;
        ByteBuffer reqBuffer = QcRilHook.createBufferWithNativeByteOrder(requestData);
        reqBuffer.putInt(userPref);
        reqBuffer.putInt(phoneId);
        AsyncResult ar = this.mQcRilHook.sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_SET_UICC_PROVISION_PREFERENCE, requestData, phoneId);
        if (ar.exception == null) {
            retval = true;
        }
        logi("set provision userPref " + userPref + " phoneId " + phoneId + " exception: " + ar.exception);
        return retval;
    }

    private int[] parseInts(byte[] buffer, int numInts) {
        ByteBuffer byteBuf = ByteBuffer.wrap(buffer);
        byteBuf.order(ByteOrder.nativeOrder());
        logi("numInts: " + numInts);
        int[] response = new int[numInts];
        for (int i = 0; i < numInts; i++) {
            response[i] = byteBuf.getInt();
            logi("response[i]: " + response[i]);
        }
        return response;
    }

    public void getAdnRecord(Message callbackMsg, int phoneId) {
        byte[] requestData = new byte[4];
        QcRilHook qcRilHook = this.mQcRilHook;
        QcRilHook.createBufferWithNativeByteOrder(requestData).putInt(phoneId);
        this.mQcRilHook.sendQcRilHookMsgAsync(IQcRilHook.QCRIL_EVT_HOOK_GET_ADN_RECORD, requestData, new AdnOemHookCallback(callbackMsg, 10), phoneId);
        logi("getAdnRecord()");
    }

    public void updateAdnRecord(QtiSimPhoneBookAdnRecord adnRecordInfo, Message callbackMsg, int phoneId) {
        int i;
        int j;
        int numEmails = adnRecordInfo.getNumEmails();
        int numAdNumbers = adnRecordInfo.getNumAdNumbers();
        String name = adnRecordInfo.getAlphaTag();
        String number = adnRecordInfo.getNumber();
        int dataSize = ((TextUtils.isEmpty(name) ? 0 : name.getBytes().length + 1) + 10) + (TextUtils.isEmpty(number) ? 0 : number.getBytes().length + 1);
        for (i = 0; i < numEmails; i++) {
            dataSize = (dataSize + 2) + (adnRecordInfo.getEmails()[i].getBytes().length + 1);
        }
        for (j = 0; j < numAdNumbers; j++) {
            dataSize = (dataSize + 2) + (adnRecordInfo.getAdNumbers()[j].getBytes().length + 1);
        }
        byte[] requestData = new byte[dataSize];
        QcRilHook qcRilHook = this.mQcRilHook;
        ByteBuffer reqBuffer = QcRilHook.createBufferWithNativeByteOrder(requestData);
        AdnOemHookCallback oemHookCb = new AdnOemHookCallback(callbackMsg, 1);
        reqBuffer.putShort((short) adnRecordInfo.getRecordIndex());
        if (TextUtils.isEmpty(name)) {
            reqBuffer.putShort((short) 0);
        } else {
            reqBuffer.putShort((short) name.getBytes().length);
            try {
                reqBuffer.put(name.getBytes("UTF-8"));
                reqBuffer.put((byte) 0);
            } catch (UnsupportedEncodingException e) {
                loge("Unsupport UTF-8 to parse name");
                return;
            }
        }
        if (TextUtils.isEmpty(number)) {
            reqBuffer.putShort((short) 0);
        } else {
            reqBuffer.putShort((short) number.getBytes().length);
            try {
                reqBuffer.put(QtiSimPhoneBookAdnRecord.ConvertToRecordNumber(number).getBytes("UTF-8"));
                reqBuffer.put((byte) 0);
            } catch (UnsupportedEncodingException e2) {
                loge("Unsupport UTF-8 to parse number");
                return;
            }
        }
        reqBuffer.putShort((short) numEmails);
        i = 0;
        while (i < numEmails) {
            reqBuffer.putShort((short) adnRecordInfo.getEmails()[i].getBytes().length);
            try {
                reqBuffer.put(adnRecordInfo.getEmails()[i].getBytes("UTF-8"));
                reqBuffer.put((byte) 0);
                i++;
            } catch (UnsupportedEncodingException e3) {
                loge("Unsupport UTF-8 to parse email");
                return;
            }
        }
        reqBuffer.putShort((short) numAdNumbers);
        j = 0;
        while (j < numAdNumbers) {
            reqBuffer.putShort((short) adnRecordInfo.getAdNumbers()[j].getBytes().length);
            try {
                reqBuffer.put(QtiSimPhoneBookAdnRecord.ConvertToRecordNumber(adnRecordInfo.getAdNumbers()[j]).getBytes("UTF-8"));
                reqBuffer.put((byte) 0);
                j++;
            } catch (UnsupportedEncodingException e4) {
                loge("Unsupport UTF-8 to parse anr");
                return;
            }
        }
        this.mQcRilHook.sendQcRilHookMsgAsync(IQcRilHook.QCRIL_EVT_HOOK_UPDATE_ADN_RECORD, requestData, oemHookCb, phoneId);
        logi("updateAdnRecord() with " + adnRecordInfo.toString());
    }

    public boolean isServiceReady() {
        return mIsServiceReady;
    }

    public void registerForUnsol(Handler handler, int event, Object obj) {
        QcRilHook qcRilHook = this.mQcRilHook;
        QcRilHook.register(handler, event, obj);
    }

    public void unRegisterForUnsol(Handler handler) {
        QcRilHook qcRilHook = this.mQcRilHook;
        QcRilHook.unregister(handler);
    }

    public void registerForServiceReadyEvent(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mServiceReadyRegistrantList.add(r);
        if (isServiceReady()) {
            r.notifyRegistrant(new AsyncResult(null, Boolean.valueOf(mIsServiceReady), null));
        }
    }

    public void registerForAdnInitDone(Handler h, int what, Object obj) {
        this.mAdnInitDoneRegistrantList.add(new Registrant(h, what, obj));
    }

    public void registerForAdnRecordsInfo(Handler h, int what, Object obj) {
        this.mAdnRecordsInfoRegistrantList.add(new Registrant(h, what, obj));
    }

    public void qcRilSendDDSInfo(int ddsPhoneId, int reason, int rilId) {
        this.mQcRilHook.qcRilSendDDSInfo(ddsPhoneId, reason, rilId);
    }

    public void unRegisterForServiceReadyEvent(Handler h) {
        this.mServiceReadyRegistrantList.remove(h);
    }

    public void unregisterForAdnInitDone(Handler h) {
        this.mAdnInitDoneRegistrantList.remove(h);
    }

    public void unregisterForAdnRecordsInfo(Handler h) {
        this.mAdnRecordsInfoRegistrantList.remove(h);
    }

    public boolean setLocalCallHold(int phoneId, boolean enable) {
        return this.mQcRilHook.setLocalCallHold(phoneId, enable);
    }

    private Object responseAdnRecords(byte[] data) {
        ByteBuffer byteBuf = ByteBuffer.wrap(data);
        byteBuf.order(ByteOrder.nativeOrder());
        int numRecords = byteBuf.getShort();
        QtiSimPhoneBookAdnRecord[] AdnRecordsInfoGroup = new QtiSimPhoneBookAdnRecord[numRecords];
        for (int i = 0; i < numRecords; i++) {
            AdnRecordsInfoGroup[i] = new QtiSimPhoneBookAdnRecord();
            AdnRecordsInfoGroup[i].mRecordIndex = byteBuf.getShort();
            int nameLength = byteBuf.getShort();
            if (nameLength > 0) {
                byte[] alphaTag = new byte[nameLength];
                byteBuf.get(alphaTag);
                try {
                    AdnRecordsInfoGroup[i].mAlphaTag = new String(alphaTag, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    loge("Unsupport UTF-8 to parse name");
                }
            }
            int numberLength = byteBuf.getShort();
            if (numberLength > 0) {
                byte[] number = new byte[numberLength];
                byteBuf.get(number);
                try {
                    AdnRecordsInfoGroup[i].mNumber = QtiSimPhoneBookAdnRecord.ConvertToPhoneNumber(new String(number, "UTF-8"));
                } catch (UnsupportedEncodingException e2) {
                    loge("Unsupport UTF-8 to parse number");
                }
            }
            int numEmails = byteBuf.getShort();
            if (numEmails > 0) {
                AdnRecordsInfoGroup[i].mEmailCount = numEmails;
                AdnRecordsInfoGroup[i].mEmails = new String[numEmails];
                int j = 0;
                while (j < numEmails) {
                    byte[] email = new byte[byteBuf.getShort()];
                    byteBuf.get(email);
                    try {
                        AdnRecordsInfoGroup[i].mEmails[j] = new String(email, "UTF-8");
                        j++;
                    } catch (UnsupportedEncodingException e3) {
                        loge("Unsupport UTF-8 to parse email");
                    }
                }
            }
            int numAnrs = byteBuf.getShort();
            if (numAnrs > 0) {
                AdnRecordsInfoGroup[i].mAdNumCount = numAnrs;
                AdnRecordsInfoGroup[i].mAdNumbers = new String[numAnrs];
                int k = 0;
                while (k < numAnrs) {
                    byte[] anr = new byte[byteBuf.getShort()];
                    byteBuf.get(anr);
                    try {
                        AdnRecordsInfoGroup[i].mAdNumbers[k] = QtiSimPhoneBookAdnRecord.ConvertToPhoneNumber(new String(anr, "UTF-8"));
                        k++;
                    } catch (UnsupportedEncodingException e4) {
                        loge("Unsupport UTF-8 to parse anr");
                    }
                }
            }
        }
        logd(Arrays.toString(AdnRecordsInfoGroup));
        return AdnRecordsInfoGroup;
    }

    private void logd(String string) {
        Rlog.d(LOG_TAG, string);
    }

    private void logi(String string) {
        Rlog.i(LOG_TAG, string);
    }

    private void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }
}
