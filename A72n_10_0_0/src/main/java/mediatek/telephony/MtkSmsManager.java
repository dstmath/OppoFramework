package mediatek.telephony;

import android.app.ActivityThread;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.internal.telephony.ISms;
import com.android.internal.telephony.SmsRawData;
import com.mediatek.internal.telephony.IMtkSms;
import com.mediatek.internal.telephony.MtkIccSmsStorageStatus;
import com.mediatek.internal.telephony.MtkPhoneConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MtkSmsManager {
    private static final int DEFAULT_SUBSCRIPTION_ID = -1002;
    private static String DIALOG_TYPE_KEY = "dialog_type";
    public static final byte ERROR_CODE_GENERIC_ERROR = 1;
    public static final byte ERROR_CODE_NO_ERROR = 0;
    public static final byte ERROR_CODE_NO_SUPPORT_SC_ADDR = 2;
    public static final String EXTRA_PARAMS_ENCODING_TYPE = "encoding_type";
    public static final String EXTRA_PARAMS_VALIDITY_PERIOD = "validity_period";
    public static final String GET_SC_ADDRESS_KEY_ADDRESS = "scAddress";
    public static final String GET_SC_ADDRESS_KEY_RESULT = "errorCode";
    public static final int RESULT_ERROR_INVALID_ADDRESS = 8;
    public static final int RESULT_ERROR_SIM_MEM_FULL = 7;
    public static final int RESULT_ERROR_SUCCESS = 0;
    private static final int SMS_PICK = 2;
    private static final String TAG = "MtkSmsManager";
    public static final int VALIDITY_PERIOD_MAX_DURATION = 255;
    public static final int VALIDITY_PERIOD_NO_DURATION = -1;
    public static final int VALIDITY_PERIOD_ONE_DAY = 167;
    public static final int VALIDITY_PERIOD_ONE_HOUR = 11;
    public static final int VALIDITY_PERIOD_SIX_HOURS = 71;
    public static final int VALIDITY_PERIOD_TWELVE_HOURS = 143;
    private static final MtkSmsManager sInstance = new MtkSmsManager(DEFAULT_SUBSCRIPTION_ID);
    private static final Object sLockObject = new Object();
    private static final Map<Integer, MtkSmsManager> sSubInstances = new ArrayMap();
    private int mSubId;

    public static MtkSmsManager getDefault() {
        return sInstance;
    }

    public static MtkSmsManager getSmsManagerForSubscriptionId(int subId) {
        MtkSmsManager smsManager;
        synchronized (sLockObject) {
            smsManager = sSubInstances.get(Integer.valueOf(subId));
            if (smsManager == null) {
                smsManager = new MtkSmsManager(subId);
                sSubInstances.put(Integer.valueOf(subId), smsManager);
            }
        }
        return smsManager;
    }

    private MtkSmsManager(int subId) {
        this.mSubId = subId;
    }

    public void sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        sendTextMessageInternal(destinationAddress, scAddress, text, sentIntent, deliveryIntent, true);
    }

    private void sendTextMessageInternal(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForCarrierApp) {
        if (!TextUtils.isEmpty(destinationAddress)) {
            try {
                getISmsService().sendTextForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, text, sentIntent, deliveryIntent, persistMessageForCarrierApp);
            } catch (RemoteException e) {
                Rlog.d(TAG, "sendTextMessage, RemoteException!");
            }
        } else {
            throw new IllegalArgumentException("Invalid destinationAddress");
        }
    }

    public void sendTextMessageWithoutPersisting(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        sendTextMessageInternal(destinationAddress, scAddress, text, sentIntent, deliveryIntent, false);
    }

    public ArrayList<MtkSmsMessage> getAllMessagesFromIcc() {
        Rlog.d(TAG, "getAllMessagesFromIcc");
        List<SmsRawData> records = null;
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                records = iccISms.getAllMessagesFromIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName());
            }
        } catch (RemoteException e) {
            Rlog.d(TAG, "getAllMessagesFromIcc, RemoteException!");
        }
        return createMessageListFromRawRecords(records);
    }

    private ArrayList<MtkSmsMessage> createMessageListFromRawRecords(List<SmsRawData> records) {
        ArrayList<MtkSmsMessage> messages = new ArrayList<>();
        Rlog.d(TAG, "createMessageListFromRawRecords");
        if (records != null) {
            int count = records.size();
            for (int i = 0; i < count; i++) {
                SmsRawData data = records.get(i);
                if (data != null) {
                    String phoneType = 2 == TelephonyManager.from(ActivityThread.currentApplication().getApplicationContext()).getCurrentPhoneType(this.mSubId) ? "3gpp2" : "3gpp";
                    Rlog.d(TAG, "phoneType: " + phoneType);
                    MtkSmsMessage sms = MtkSmsMessage.createFromEfRecord(i + 1, data.getBytes(), phoneType);
                    if (sms != null) {
                        messages.add(sms);
                    }
                }
            }
            Rlog.d(TAG, "actual sms count is " + count);
        } else {
            Rlog.d(TAG, "fail to parse SIM sms, records is null");
        }
        return messages;
    }

    public ArrayList<MtkSmsMessage> getAllMessagesFromIccEfByMode(int mode) {
        Rlog.d(TAG, "getAllMessagesFromIcc, mode=" + mode);
        List<SmsRawData> records = null;
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                records = iccISms.getAllMessagesFromIccEfByModeForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), mode);
            }
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException!");
        }
        int sz = 0;
        if (records != null) {
            sz = records.size();
        }
        for (int i = 0; i < sz; i++) {
            SmsRawData record = records.get(i);
            if (record != null) {
                byte[] data = record.getBytes();
                int index = i + 1;
                if ((data[0] & 255) == 3) {
                    Rlog.d(TAG, "index[" + index + "] is STATUS_ON_ICC_READ");
                    if (SmsManager.getSmsManagerForSubscriptionId(this.mSubId).updateMessageOnIcc(index, 1, data)) {
                        Rlog.d(TAG, "update index[" + index + "] to STATUS_ON_ICC_READ");
                    } else {
                        Rlog.d(TAG, "fail to update message status");
                    }
                }
            }
        }
        return createMessageListFromRawRecordsByMode(getSubscriptionId(), records, mode);
    }

    public int copyTextMessageToIccCard(String scAddress, String address, List<String> text, int status, long timestamp) {
        Rlog.d(TAG, "copyTextMessageToIccCard");
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.copyTextMessageToIccCardForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), scAddress, address, text, status, timestamp);
            }
            return 1;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException!");
            return 1;
        }
    }

    public void sendDataMessage(String destinationAddress, String scAddress, short destinationPort, short originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        Rlog.d(TAG, "sendDataMessage");
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (isValidParameters(destinationAddress, "send_data", sentIntent)) {
            if (data == null || data.length == 0) {
                throw new IllegalArgumentException("Invalid message data");
            }
            try {
                IMtkSms iccISms = getIMtkSmsServiceOrThrow();
                if (iccISms != null) {
                    iccISms.sendDataWithOriginalPortForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, destinationPort & 65535, originalPort & 65535, data, sentIntent, deliveryIntent);
                }
            } catch (RemoteException e) {
                Rlog.d(TAG, "RemoteException!");
            }
        }
    }

    public void sendTextMessageWithEncodingType(String destAddr, String scAddr, String text, int encodingType, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        Rlog.d(TAG, "sendTextMessageWithEncodingType, encoding=" + encodingType);
        if (TextUtils.isEmpty(destAddr)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (!isValidParameters(destAddr, text, sentIntent)) {
            Rlog.d(TAG, "the parameters are invalid");
        } else {
            try {
                IMtkSms iccISms = getIMtkSmsServiceOrThrow();
                if (iccISms != null) {
                    iccISms.sendTextWithEncodingTypeForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destAddr, scAddr, text, encodingType, sentIntent, deliveryIntent, true);
                }
            } catch (RemoteException e) {
                Rlog.d(TAG, "RemoteException");
            }
        }
    }

    public void sendMultipartTextMessageWithEncodingType(String destAddr, String scAddr, ArrayList<String> parts, int encodingType, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        PendingIntent deliveryIntent;
        Rlog.d(TAG, "sendMultipartTextMessageWithEncodingType, encoding=" + encodingType);
        if (TextUtils.isEmpty(destAddr)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (!isValidParameters(destAddr, parts, sentIntents)) {
            Rlog.d(TAG, "invalid parameters for multipart message");
        } else if (parts == null || parts.size() <= 1) {
            PendingIntent sentIntent = null;
            if (sentIntents != null && sentIntents.size() > 0) {
                sentIntent = sentIntents.get(0);
            }
            Rlog.d(TAG, "get sentIntent: " + sentIntent);
            if (deliveryIntents == null || deliveryIntents.size() <= 0) {
                deliveryIntent = null;
            } else {
                deliveryIntent = deliveryIntents.get(0);
            }
            Rlog.d(TAG, "send single message");
            if (parts != null) {
                Rlog.d(TAG, "parts.size = " + parts.size());
            }
            String text = (parts == null || parts.size() == 0) ? "" : parts.get(0);
            Rlog.d(TAG, "pass encoding type " + encodingType);
            sendTextMessageWithEncodingType(destAddr, scAddr, text, encodingType, sentIntent, deliveryIntent);
        } else {
            try {
                IMtkSms iccISms = getIMtkSmsServiceOrThrow();
                if (iccISms != null) {
                    iccISms.sendMultipartTextWithEncodingTypeForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destAddr, scAddr, parts, encodingType, sentIntents, deliveryIntents, true);
                }
            } catch (RemoteException e) {
                Rlog.d(TAG, "RemoteException");
            }
        }
    }

    public ArrayList<String> divideMessage(String text, int encodingType) {
        Rlog.d(TAG, "divideMessage, encoding = " + encodingType);
        ArrayList<String> ret = MtkSmsMessage.fragmentText(text, encodingType);
        Rlog.d(TAG, "divideMessage: size = " + ret.size());
        return ret;
    }

    public MtkSimSmsInsertStatus insertTextMessageToIccCard(String scAddress, String address, List<String> text, int status, long timestamp) {
        String str;
        Rlog.d(TAG, "insertTextMessageToIccCard");
        MtkSimSmsInsertStatus ret = null;
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                ret = iccISms.insertTextMessageToIccCardForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), scAddress, address, text, status, timestamp);
            }
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
        }
        if (ret != null) {
            str = "insert Text " + ret.indexInIcc;
        } else {
            str = "insert Text null";
        }
        Rlog.d(TAG, str);
        return ret;
    }

    public MtkSimSmsInsertStatus insertRawMessageToIccCard(int status, byte[] pdu, byte[] smsc) {
        String str;
        Rlog.d(TAG, "insertRawMessageToIccCard");
        MtkSimSmsInsertStatus ret = null;
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                ret = iccISms.insertRawMessageToIccCardForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), status, pdu, smsc);
            }
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
        }
        if (ret != null) {
            str = "insert Raw " + ret.indexInIcc;
        } else {
            str = "insert Raw null";
        }
        Rlog.d(TAG, str);
        return ret;
    }

    public void sendTextMessageWithExtraParams(String destAddr, String scAddr, String text, Bundle extraParams, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        Rlog.d(TAG, "sendTextMessageWithExtraParams");
        if (TextUtils.isEmpty(destAddr)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (isValidParameters(destAddr, text, sentIntent)) {
            if (extraParams == null) {
                Rlog.d(TAG, "bundle is null");
                return;
            }
            try {
                IMtkSms iccISms = getIMtkSmsServiceOrThrow();
                if (iccISms != null) {
                    iccISms.sendTextWithExtraParamsForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destAddr, scAddr, text, extraParams, sentIntent, deliveryIntent, true);
                }
            } catch (RemoteException e) {
                Rlog.d(TAG, "RemoteException");
            }
        }
    }

    public void sendMultipartTextMessageWithExtraParams(String destAddr, String scAddr, ArrayList<String> parts, Bundle extraParams, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        PendingIntent deliveryIntent;
        Rlog.d(TAG, "sendMultipartTextMessageWithExtraParams");
        if (TextUtils.isEmpty(destAddr)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (isValidParameters(destAddr, parts, sentIntents)) {
            if (extraParams == null) {
                Rlog.d(TAG, "bundle is null");
            } else if (parts == null || parts.size() <= 1) {
                PendingIntent sentIntent = null;
                if (sentIntents != null && sentIntents.size() > 0) {
                    sentIntent = sentIntents.get(0);
                }
                if (deliveryIntents == null || deliveryIntents.size() <= 0) {
                    deliveryIntent = null;
                } else {
                    deliveryIntent = deliveryIntents.get(0);
                }
                sendTextMessageWithExtraParams(destAddr, scAddr, (parts == null || parts.size() == 0) ? "" : parts.get(0), extraParams, sentIntent, deliveryIntent);
            } else {
                try {
                    IMtkSms iccISms = getIMtkSmsServiceOrThrow();
                    if (iccISms != null) {
                        iccISms.sendMultipartTextWithExtraParamsForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destAddr, scAddr, parts, extraParams, sentIntents, deliveryIntents, true);
                    }
                } catch (RemoteException e) {
                    Rlog.d(TAG, "RemoteException");
                }
            }
        }
    }

    public MtkSmsParameters getSmsParameters() {
        Rlog.d(TAG, "getSmsParameters");
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.getSmsParametersForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName());
            }
            return null;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            Rlog.d(TAG, "fail to get MtkSmsParameters");
            return null;
        }
    }

    public boolean setSmsParameters(MtkSmsParameters params) {
        Rlog.d(TAG, "setSmsParameters");
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.setSmsParametersForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), params);
            }
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            return false;
        }
    }

    public int copySmsToIcc(byte[] smsc, byte[] pdu, int status) {
        int[] index;
        Rlog.d(TAG, "copySmsToIcc");
        MtkSimSmsInsertStatus smsStatus = insertRawMessageToIccCard(status, pdu, smsc);
        if (smsStatus == null || (index = smsStatus.getIndex()) == null || index.length <= 0) {
            return -1;
        }
        return index[0];
    }

    public boolean updateSmsOnSimReadStatus(int index, boolean read) {
        Rlog.d(TAG, "updateSmsOnSimReadStatus");
        SmsRawData record = null;
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                record = iccISms.getMessageFromIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), index);
            }
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
        }
        if (record != null) {
            byte[] rawData = record.getBytes();
            int status = rawData[0] & 255;
            Rlog.d(TAG, "sms status is " + status);
            char c = 3;
            if (status != 3 && status != 1) {
                Rlog.d(TAG, "non-delivery sms " + status);
                return false;
            } else if ((status != 3 || read) && !(status == 1 && read)) {
                Rlog.d(TAG, "update sms status as " + read);
                if (read) {
                    c = 1;
                }
                return SmsManager.getSmsManagerForSubscriptionId(this.mSubId).updateMessageOnIcc(index, 1, rawData);
            } else {
                Rlog.d(TAG, "no need to update status");
                return true;
            }
        } else {
            Rlog.d(TAG, "record is null");
            return false;
        }
    }

    public void setSmsMemoryStatus(boolean status) {
        Rlog.d(TAG, "setSmsMemoryStatus");
        try {
            IMtkSms iccISms = getIMtkSmsServiceOrThrow();
            if (iccISms != null) {
                iccISms.setSmsMemoryStatusForSubscriber(getSubscriptionId(), status);
            }
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
        }
    }

    public MtkIccSmsStorageStatus getSmsSimMemoryStatus() {
        Rlog.d(TAG, "getSmsSimMemoryStatus");
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.getSmsSimMemoryStatusForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName());
            }
            return null;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            return null;
        }
    }

    private static boolean isValidParameters(String destinationAddress, String text, PendingIntent sentIntent) {
        ArrayList<PendingIntent> sentIntents = new ArrayList<>();
        ArrayList<String> parts = new ArrayList<>();
        sentIntents.add(sentIntent);
        parts.add(text);
        return isValidParameters(destinationAddress, parts, sentIntents);
    }

    private static boolean isValidParameters(String destinationAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents) {
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
            Rlog.d(TAG, "Invalid destinationAddress");
            return false;
        } else if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (parts.size() >= 1) {
            return true;
        } else {
            throw new IllegalArgumentException("Invalid message body");
        }
    }

    private static boolean isValidSmsDestinationAddress(String da) {
        String encodeAddress = PhoneNumberUtils.extractNetworkPortion(da);
        if (encodeAddress == null) {
            return true;
        }
        return true ^ encodeAddress.isEmpty();
    }

    private static ArrayList<MtkSmsMessage> createMessageListFromRawRecordsByMode(int subId, List<SmsRawData> records, int mode) {
        MtkSmsMessage singleSms;
        Rlog.d(TAG, "createMessageListFromRawRecordsByMode");
        ArrayList<MtkSmsMessage> msg = null;
        if (records != null) {
            int count = records.size();
            msg = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                SmsRawData data = records.get(i);
                if (!(data == null || (singleSms = createFromEfRecordByMode(subId, i + 1, data.getBytes(), mode)) == null)) {
                    msg.add(singleSms);
                }
            }
            Rlog.d(TAG, "actual sms count is " + msg.size());
        } else {
            Rlog.d(TAG, "fail to parse SIM sms, records is null");
        }
        return msg;
    }

    private static MtkSmsMessage createFromEfRecordByMode(int subId, int index, byte[] data, int mode) {
        MtkSmsMessage sms = null;
        if (mode != 2) {
            sms = MtkSmsMessage.createFromEfRecord(index, data, "3gpp");
        }
        if (sms != null) {
            sms.setSubId(subId);
        }
        return sms;
    }

    public int getSubscriptionId() {
        int subId = this.mSubId;
        if (subId == DEFAULT_SUBSCRIPTION_ID) {
            subId = SmsManager.getDefaultSmsSubscriptionId();
        }
        Context context = ActivityThread.currentApplication().getApplicationContext();
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                iccISms.isSmsSimPickActivityNeeded(subId);
            }
        } catch (RemoteException e) {
            Rlog.e(TAG, "Exception in getSubscriptionId");
        }
        if (0 != 0) {
            Rlog.d(TAG, "getSubscriptionId isSmsSimPickActivityNeeded is true");
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.sim.SimDialogActivity");
            intent.addFlags(268435456);
            intent.putExtra(DIALOG_TYPE_KEY, 2);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e2) {
                Rlog.e(TAG, "Unable to launch Settings application.");
            }
        }
        return subId;
    }

    private static IMtkSms getIMtkSmsServiceOrThrow() {
        IMtkSms iccISms = getIMtkSmsService();
        if (iccISms != null) {
            return iccISms;
        }
        throw new UnsupportedOperationException("SmsEx is not supported");
    }

    private static IMtkSms getIMtkSmsService() {
        return IMtkSms.Stub.asInterface(ServiceManager.getService("imtksms"));
    }

    private static ISms getISmsServiceOrThrow() {
        ISms iccISms = getISmsService();
        if (iccISms != null) {
            return iccISms;
        }
        throw new UnsupportedOperationException("Sms is not supported");
    }

    private static ISms getISmsService() {
        return ISms.Stub.asInterface(ServiceManager.getService("isms"));
    }

    public boolean queryCellBroadcastSmsActivation() {
        Rlog.d(TAG, "queryCellBroadcastSmsActivation");
        Rlog.d(TAG, "subId=" + getSubscriptionId());
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.queryCellBroadcastSmsActivationForSubscriber(getSubscriptionId());
            }
            Rlog.d(TAG, "fail to get sms service");
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException!");
            return false;
        }
    }

    public boolean activateCellBroadcastSms(boolean activate) {
        Rlog.d(TAG, "activateCellBroadcastSms activate : " + activate + ", sub = " + getSubscriptionId());
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.activateCellBroadcastSmsForSubscriber(getSubscriptionId(), activate);
            }
            Rlog.d(TAG, "fail to get sms service, maybe phone is initializing");
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "fail to activate CB");
            return false;
        }
    }

    public boolean removeCellBroadcastMsg(int channelId, int serialId) {
        Rlog.d(TAG, "RemoveCellBroadcastMsg, subId=" + getSubscriptionId());
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.removeCellBroadcastMsgForSubscriber(getSubscriptionId(), channelId, serialId);
            }
            Rlog.d(TAG, "fail to get sms service");
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoveCellBroadcastMsg, RemoteException!");
            return false;
        }
    }

    public String getCellBroadcastRanges() {
        Rlog.d(TAG, "getCellBroadcastRanges, subId=" + getSubscriptionId());
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.getCellBroadcastRangesForSubscriber(getSubscriptionId());
            }
            Rlog.d(TAG, "fail to get sms service");
            return "";
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            return "";
        }
    }

    public boolean setCellBroadcastLang(String lang) {
        Rlog.d(TAG, "setCellBroadcastLang, subId=" + getSubscriptionId());
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.setCellBroadcastLangsForSubscriber(getSubscriptionId(), lang);
            }
            Rlog.d(TAG, "fail to get sms service");
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            return false;
        }
    }

    public String getCellBroadcastLang() {
        Rlog.d(TAG, "getCellBroadcastLang, subId=" + getSubscriptionId());
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.getCellBroadcastLangsForSubscriber(getSubscriptionId());
            }
            Rlog.d(TAG, "fail to get sms service");
            return "";
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            return "";
        }
    }

    public boolean setEtwsConfig(int mode) {
        Rlog.d(TAG, "setEtwsConfig, mode=" + mode);
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.setEtwsConfigForSubscriber(getSubscriptionId(), mode);
            }
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            return false;
        }
    }

    public String getScAddress() {
        Rlog.d(TAG, "getScAddress");
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.getScAddressForSubscriber(getSubscriptionId());
            }
            return null;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            return null;
        }
    }

    public Bundle getScAddressWithErroCode() {
        Rlog.d(TAG, "getScAddressWithErroCode");
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.getScAddressWithErrorCodeForSubscriber(getSubscriptionId());
            }
            return null;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            return null;
        }
    }

    public boolean setScAddress(String address) {
        Rlog.d(TAG, "setScAddress");
        try {
            IMtkSms iccISms = getIMtkSmsService();
            if (iccISms != null) {
                return iccISms.setScAddressForSubscriber(getSubscriptionId(), address);
            }
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
            return false;
        }
    }

    public boolean isImsSmsSupported() {
        try {
            ISms iccISms = getISmsService();
            if (iccISms == null) {
                return false;
            }
            boolean boSupported = iccISms.isImsSmsSupportedForSubscriber(getSubscriptionId());
            Rlog.d(TAG, "isImsSmsSupported " + boSupported);
            return boSupported;
        } catch (RemoteException e) {
            return false;
        }
    }

    public String getImsSmsFormat() {
        try {
            ISms iccISms = getISmsService();
            if (iccISms == null) {
                return MtkPhoneConstants.LTE_ACCESS_STRATUM_STATE_UNKNOWN;
            }
            String format = iccISms.getImsSmsFormatForSubscriber(getSubscriptionId());
            Rlog.d(TAG, "getImsSmsFormat " + format);
            return format;
        } catch (RemoteException e) {
            return MtkPhoneConstants.LTE_ACCESS_STRATUM_STATE_UNKNOWN;
        }
    }

    public ArrayList<String> divideMessage(String text) {
        if (text != null) {
            return MtkSmsMessage.fragmentText(text);
        }
        throw new IllegalArgumentException("text is null");
    }

    public void sendDataMessage(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Invalid message data");
        } else {
            try {
                getISmsServiceOrThrow().sendDataForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, destinationPort & 65535, data, sentIntent, deliveryIntent);
            } catch (RemoteException e) {
            }
        }
    }

    public void sendDataMessageWithSelfPermissions(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Invalid message data");
        } else {
            try {
                getISmsServiceOrThrow().sendDataForSubscriberWithSelfPermissions(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, destinationPort & 65535, data, sentIntent, deliveryIntent);
            } catch (RemoteException e) {
            }
        }
    }

    public static boolean checkSimPickActivityNeeded(boolean needed) {
        return false;
    }
}
