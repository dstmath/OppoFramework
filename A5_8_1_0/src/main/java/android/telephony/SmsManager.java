package android.telephony;

import android.app.ActivityThread;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.BaseBundle;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SeempLog;
import com.android.internal.telephony.IMms;
import com.android.internal.telephony.ISms;
import com.android.internal.telephony.ISms.Stub;
import com.android.internal.telephony.SmsConstants;
import com.android.internal.telephony.SmsRawData;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.oem.SmsCbConfigInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class SmsManager {
    public static final String APK_LABEL_NAME = "apk_label_name";
    public static final String APK_PACKAGE_NAME = "apk_package_name";
    public static final int CDMA_SMS_RECORD_LENGTH = 255;
    public static final int CELL_BROADCAST_RAN_TYPE_CDMA = 1;
    public static final int CELL_BROADCAST_RAN_TYPE_GSM = 0;
    public static final String DEFAULT_PACKAGE = "com.test.send.message;com.bankmandiri.mandirionline;bri.delivery.brimobile";
    private static final int DEFAULT_SUBSCRIPTION_ID = -1002;
    private static String DIALOG_TYPE_KEY = "dialog_type";
    public static final String EXTRA_MMS_DATA = "android.telephony.extra.MMS_DATA";
    public static final String EXTRA_MMS_HTTP_STATUS = "android.telephony.extra.MMS_HTTP_STATUS";
    public static final String MESSAGE_STATUS_READ = "read";
    public static final String MESSAGE_STATUS_SEEN = "seen";
    public static final String MESSENGER = "messenger";
    public static final String MMS_CONFIG_ALIAS_ENABLED = "aliasEnabled";
    public static final String MMS_CONFIG_ALIAS_MAX_CHARS = "aliasMaxChars";
    public static final String MMS_CONFIG_ALIAS_MIN_CHARS = "aliasMinChars";
    public static final String MMS_CONFIG_ALLOW_ATTACH_AUDIO = "allowAttachAudio";
    public static final String MMS_CONFIG_APPEND_TRANSACTION_ID = "enabledTransID";
    public static final String MMS_CONFIG_CLOSE_CONNECTION = "mmsCloseConnection";
    public static final String MMS_CONFIG_EMAIL_GATEWAY_NUMBER = "emailGatewayNumber";
    public static final String MMS_CONFIG_GROUP_MMS_ENABLED = "enableGroupMms";
    public static final String MMS_CONFIG_HTTP_PARAMS = "httpParams";
    public static final String MMS_CONFIG_HTTP_SOCKET_TIMEOUT = "httpSocketTimeout";
    public static final String MMS_CONFIG_MAX_IMAGE_HEIGHT = "maxImageHeight";
    public static final String MMS_CONFIG_MAX_IMAGE_WIDTH = "maxImageWidth";
    public static final String MMS_CONFIG_MAX_MESSAGE_SIZE = "maxMessageSize";
    public static final String MMS_CONFIG_MESSAGE_TEXT_MAX_SIZE = "maxMessageTextSize";
    public static final String MMS_CONFIG_MMS_DELIVERY_REPORT_ENABLED = "enableMMSDeliveryReports";
    public static final String MMS_CONFIG_MMS_ENABLED = "enabledMMS";
    public static final String MMS_CONFIG_MMS_READ_REPORT_ENABLED = "enableMMSReadReports";
    public static final String MMS_CONFIG_MULTIPART_SMS_ENABLED = "enableMultipartSMS";
    public static final String MMS_CONFIG_NAI_SUFFIX = "naiSuffix";
    public static final String MMS_CONFIG_NOTIFY_WAP_MMSC_ENABLED = "enabledNotifyWapMMSC";
    public static final String MMS_CONFIG_RECIPIENT_LIMIT = "recipientLimit";
    public static final String MMS_CONFIG_SEND_MULTIPART_SMS_AS_SEPARATE_MESSAGES = "sendMultipartSmsAsSeparateMessages";
    public static final String MMS_CONFIG_SHOW_CELL_BROADCAST_APP_LINKS = "config_cellBroadcastAppLinks";
    public static final String MMS_CONFIG_SMS_DELIVERY_REPORT_ENABLED = "enableSMSDeliveryReports";
    public static final String MMS_CONFIG_SMS_TO_MMS_TEXT_LENGTH_THRESHOLD = "smsToMmsTextLengthThreshold";
    public static final String MMS_CONFIG_SMS_TO_MMS_TEXT_THRESHOLD = "smsToMmsTextThreshold";
    public static final String MMS_CONFIG_SUBJECT_MAX_LENGTH = "maxSubjectLength";
    public static final String MMS_CONFIG_SUPPORT_HTTP_CHARSET_HEADER = "supportHttpCharsetHeader";
    public static final String MMS_CONFIG_SUPPORT_MMS_CONTENT_DISPOSITION = "supportMmsContentDisposition";
    public static final String MMS_CONFIG_UA_PROF_TAG_NAME = "uaProfTagName";
    public static final String MMS_CONFIG_UA_PROF_URL = "uaProfUrl";
    public static final String MMS_CONFIG_USER_AGENT = "userAgent";
    public static final int MMS_ERROR_CONFIGURATION_ERROR = 7;
    public static final int MMS_ERROR_HTTP_FAILURE = 4;
    public static final int MMS_ERROR_INVALID_APN = 2;
    public static final int MMS_ERROR_IO_ERROR = 5;
    public static final int MMS_ERROR_NO_DATA_NETWORK = 8;
    public static final int MMS_ERROR_RETRY = 6;
    public static final int MMS_ERROR_UNABLE_CONNECT_MMS = 3;
    public static final int MMS_ERROR_UNSPECIFIED = 1;
    public static final int MSG_CLICK_SEND_ITEM = 1;
    public static final int MSG_DISSMISS_SEND = 2;
    private static final String PHONE_PACKAGE_NAME = "com.android.phone";
    public static final int RESULT_ERROR_FDN_CHECK_FAILURE = 6;
    public static final int RESULT_ERROR_GENERIC_FAILURE = 1;
    public static final int RESULT_ERROR_LIMIT_EXCEEDED = 5;
    public static final int RESULT_ERROR_NO_SERVICE = 4;
    public static final int RESULT_ERROR_NULL_PDU = 3;
    public static final int RESULT_ERROR_RADIO_OFF = 2;
    public static final int RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED = 8;
    public static final int RESULT_ERROR_SHORT_CODE_NOT_ALLOWED = 7;
    public static final String ROMUPDATE_SEND_MESSAGE_PKG = "romupdate_send_message_pkg";
    public static final String SIM_NAME_ONE = "same_name_one";
    public static final String SIM_NAME_TWO = "same_name_two";
    private static final int SMS_PICK = 2;
    public static final int SMS_RECORD_LENGTH = 176;
    public static final int SMS_TYPE_INCOMING = 0;
    public static final int SMS_TYPE_OUTGOING = 1;
    public static final int STATUS_ON_ICC_FREE = 0;
    public static final int STATUS_ON_ICC_READ = 1;
    public static final int STATUS_ON_ICC_SENT = 5;
    public static final int STATUS_ON_ICC_UNREAD = 3;
    public static final int STATUS_ON_ICC_UNSENT = 7;
    public static final String SUBSCRIPTION_ID_ONE = "subscription_id_one";
    public static final String SUBSCRIPTION_ID_TWO = "subscription_id_two";
    private static final String TAG = "SmsManager";
    private static final SmsManager sInstance = new SmsManager(DEFAULT_SUBSCRIPTION_ID);
    private static final Object sLockObject = new Object();
    private static final Map<Integer, SmsManager> sSubInstances = new ArrayMap();
    private int mSubId;

    public void sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        SeempLog.record_str(75, destinationAddress);
        sendTextMessageInternal(destinationAddress, scAddress, text, sentIntent, deliveryIntent, true);
    }

    private void sendTextMessageInternal(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (TextUtils.isEmpty(text)) {
            throw new IllegalArgumentException("Invalid message body");
        } else {
            final List<SubscriptionInfo> operatorSubInfoList = new ArrayList();
            final Context context = ActivityThread.currentApplication().getApplicationContext();
            if (isNeedDisplayPickSimCardDialog(context, operatorSubInfoList) && operatorSubInfoList.size() == 2) {
                String delayDestinationAddress = destinationAddress;
                String delayScAddress = scAddress;
                String delayText = text;
                PendingIntent delaySentIntent = sentIntent;
                PendingIntent delayDeliveryIntent = deliveryIntent;
                boolean delayPersistMessage = persistMessage;
                final String str = destinationAddress;
                final String str2 = scAddress;
                final String str3 = text;
                final PendingIntent pendingIntent = sentIntent;
                final PendingIntent pendingIntent2 = deliveryIntent;
                final boolean z = persistMessage;
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Looper.prepare();
                            final Looper looper = Looper.myLooper();
                            final String str = str;
                            final String str2 = str2;
                            final String str3 = str3;
                            final PendingIntent pendingIntent = pendingIntent;
                            final PendingIntent pendingIntent2 = pendingIntent2;
                            final boolean z = z;
                            SmsManager.this.startPickSimCardActivity(context, new Handler() {
                                public void handleMessage(Message msg) {
                                    try {
                                        Log.d(SmsManager.TAG, "receive msg.what = " + msg.what + ", msg.arg1 =" + msg.arg1);
                                        switch (msg.what) {
                                            case 1:
                                                try {
                                                    SmsManager.getISmsServiceOrThrow().sendTextForSubscriber(msg.arg1, ActivityThread.currentPackageName(), str, str2, str3, pendingIntent, pendingIntent2, z);
                                                    break;
                                                } catch (RemoteException e) {
                                                    break;
                                                }
                                        }
                                        looper.quit();
                                    } catch (Exception e2) {
                                    }
                                }
                            }, operatorSubInfoList);
                            Looper.loop();
                        } catch (Exception e) {
                            try {
                                SmsManager.getISmsServiceOrThrow().sendTextForSubscriber(SmsManager.this.getSubscriptionId(), ActivityThread.currentPackageName(), str, str2, str3, pendingIntent, pendingIntent2, z);
                            } catch (RemoteException e2) {
                            }
                        }
                    }
                }).start();
                return;
            }
            try {
                getISmsServiceOrThrow().sendTextForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, text, sentIntent, deliveryIntent, persistMessage);
            } catch (RemoteException e) {
            }
        }
    }

    public void sendTextMessageWithoutPersisting(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        sendTextMessageInternal(destinationAddress, scAddress, text, sentIntent, deliveryIntent, false);
    }

    public void sendTextMessageWithSelfPermissions(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) {
        SeempLog.record_str(75, destinationAddress);
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (TextUtils.isEmpty(text)) {
            throw new IllegalArgumentException("Invalid message body");
        } else {
            try {
                getISmsServiceOrThrow().sendTextForSubscriberWithSelfPermissions(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, text, sentIntent, deliveryIntent, persistMessage);
            } catch (RemoteException e) {
            }
        }
    }

    public void sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, int priority, boolean isExpectMore, int validityPeriod) {
        sendTextMessageInternal(destinationAddress, scAddress, text, sentIntent, deliveryIntent, true, priority, isExpectMore, validityPeriod);
    }

    private void sendTextMessageInternal(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage, int priority, boolean isExpectMore, int validityPeriod) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (TextUtils.isEmpty(text)) {
            throw new IllegalArgumentException("Invalid message body");
        } else {
            try {
                ISms iccISms = getISmsServiceOrThrow();
                if (iccISms != null) {
                    iccISms.sendTextForSubscriberWithOptions(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, text, sentIntent, deliveryIntent, persistMessage, priority, isExpectMore, validityPeriod);
                }
            } catch (RemoteException e) {
            }
        }
    }

    private void sendTextMessageInternalOem(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage, int priority, boolean isExpectMore, int validityPeriod, int encodingType) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (TextUtils.isEmpty(text)) {
            throw new IllegalArgumentException("Invalid message body");
        } else {
            try {
                ISms iccISms = getISmsServiceOrThrow();
                if (iccISms != null) {
                    iccISms.sendTextForSubscriberWithOptionsOem(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, text, sentIntent, deliveryIntent, persistMessage, priority, isExpectMore, validityPeriod, encodingType);
                }
            } catch (RemoteException e) {
            }
        }
    }

    private void sendTextMessageInternalOem(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage, int priority, boolean isExpectMore, int validityPeriod) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (TextUtils.isEmpty(text)) {
            throw new IllegalArgumentException("Invalid message body");
        } else {
            try {
                ISms iccISms = getISmsServiceOrThrow();
                if (iccISms != null) {
                    iccISms.sendTextForSubscriberWithOptions(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, text, sentIntent, deliveryIntent, persistMessage, priority, isExpectMore, validityPeriod);
                }
            } catch (RemoteException e) {
            }
        }
    }

    public void sendTextMessageWithoutPersisting(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, int priority, boolean isExpectMore, int validityPeriod) {
        sendTextMessageInternal(destinationAddress, scAddress, text, sentIntent, deliveryIntent, false, priority, isExpectMore, validityPeriod);
    }

    public void injectSmsPdu(byte[] pdu, String format, PendingIntent receivedIntent) {
        if (format.equals("3gpp") || (format.equals("3gpp2") ^ 1) == 0) {
            try {
                ISms iccISms = Stub.asInterface(ServiceManager.getService("isms"));
                if (iccISms != null) {
                    iccISms.injectSmsPduForSubscriber(getSubscriptionId(), pdu, format, receivedIntent);
                    return;
                }
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid pdu format. format must be either 3gpp or 3gpp2");
    }

    public ArrayList<String> divideMessage(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null");
        }
        try {
            int subid = getSubscriptionId();
            ArrayList<String> ret = SmsMessage.oemFragmentText(text, subid);
            if (ret != null) {
                Rlog.d("sms", "divideMessage---mSubId=" + this.mSubId + " subid=" + subid + " ret.size()=" + ret.size());
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return SmsMessage.fragmentText(text);
        }
    }

    public ArrayList<String> divideMessageOem(String text, int encodingType) {
        if (encodingType != 1 && encodingType != 3) {
            return divideMessage(text);
        }
        if (text == null) {
            throw new IllegalArgumentException("text is null");
        }
        try {
            int subid = getSubscriptionId();
            ArrayList<String> ret = SmsMessage.oemFragmentText(text, subid, encodingType);
            if (ret != null) {
                Rlog.d("sms", "divideMessage---mSubId=" + this.mSubId + " subid=" + subid + " ret.size()=" + ret.size() + " encodingType=" + encodingType);
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return SmsMessage.fragmentText(text);
        }
    }

    public void sendMultipartTextMessage(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        sendMultipartTextMessageInternal(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, true);
    }

    private void sendMultipartTextMessageInternal(String destinationAddress, String scAddress, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessage) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (parts == null || parts.size() < 1) {
            throw new IllegalArgumentException("Invalid message body");
        } else if (parts.size() > 1) {
            final List<SubscriptionInfo> operatorSubInfoList = new ArrayList();
            final Context context = ActivityThread.currentApplication().getApplicationContext();
            if (isNeedDisplayPickSimCardDialog(context, operatorSubInfoList) && operatorSubInfoList.size() == 2) {
                String delayDestinationAddress = destinationAddress;
                String delayScAddress = scAddress;
                List<String> delayParts = parts;
                List<PendingIntent> delaySentIntents = sentIntents;
                List<PendingIntent> delayDeliveryIntents = deliveryIntents;
                boolean delayPersistMessage = persistMessage;
                final String str = destinationAddress;
                final String str2 = scAddress;
                final List<String> list = parts;
                final List<PendingIntent> list2 = sentIntents;
                final List<PendingIntent> list3 = deliveryIntents;
                final boolean z = persistMessage;
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Looper.prepare();
                            final Looper looper = Looper.myLooper();
                            final String str = str;
                            final String str2 = str2;
                            final List list = list;
                            final List list2 = list2;
                            final List list3 = list3;
                            final boolean z = z;
                            SmsManager.this.startPickSimCardActivity(context, new Handler() {
                                public void handleMessage(Message msg) {
                                    try {
                                        Log.d(SmsManager.TAG, "receive msg.what = " + msg.what + ", msg.arg1 =" + msg.arg1);
                                        switch (msg.what) {
                                            case 1:
                                                try {
                                                    SmsManager.getISmsServiceOrThrow().sendMultipartTextForSubscriber(msg.arg1, ActivityThread.currentPackageName(), str, str2, list, list2, list3, z);
                                                    break;
                                                } catch (RemoteException e) {
                                                    break;
                                                }
                                        }
                                        looper.quit();
                                    } catch (Exception e2) {
                                    }
                                }
                            }, operatorSubInfoList);
                            Looper.loop();
                        } catch (Exception e) {
                            try {
                                SmsManager.getISmsServiceOrThrow().sendMultipartTextForSubscriber(SmsManager.this.getSubscriptionId(), ActivityThread.currentPackageName(), str, str2, list, list2, list3, z);
                            } catch (RemoteException e2) {
                            }
                        }
                    }
                }).start();
                return;
            }
            try {
                getISmsServiceOrThrow().sendMultipartTextForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, parts, sentIntents, deliveryIntents, persistMessage);
            } catch (RemoteException e) {
            }
        } else {
            PendingIntent pendingIntent = null;
            PendingIntent pendingIntent2 = null;
            if (sentIntents != null && sentIntents.size() > 0) {
                pendingIntent = (PendingIntent) sentIntents.get(0);
            }
            if (deliveryIntents != null && deliveryIntents.size() > 0) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(0);
            }
            sendTextMessage(destinationAddress, scAddress, (String) parts.get(0), pendingIntent, pendingIntent2);
        }
    }

    public void sendMultipartTextMessageWithoutPersisting(String destinationAddress, String scAddress, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) {
        sendMultipartTextMessageInternal(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, false);
    }

    public void sendMultipartTextMessage(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, int priority, boolean isExpectMore, int validityPeriod) {
        sendMultipartTextMessageInternal(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, true);
    }

    public void sendMultipartTextMessageOem(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, int priority, boolean isExpectMore, int validityPeriod, int encodingType) {
        Rlog.d(TAG, "encodingType=" + encodingType);
        if (encodingType == 1 || encodingType == 3) {
            sendMultipartTextMessageInternalOem(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, true, priority, isExpectMore, validityPeriod, encodingType);
        } else {
            sendMultipartTextMessageInternal(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, true);
        }
    }

    private void sendMultipartTextMessageInternal(String destinationAddress, String scAddress, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessage, int priority, boolean isExpectMore, int validityPeriod) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (parts == null || parts.size() < 1) {
            throw new IllegalArgumentException("Invalid message body");
        } else if (parts.size() > 1) {
            try {
                ISms iccISms = getISmsServiceOrThrow();
                if (iccISms != null) {
                    iccISms.sendMultipartTextForSubscriberWithOptions(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, parts, sentIntents, deliveryIntents, persistMessage, priority, isExpectMore, validityPeriod);
                }
            } catch (RemoteException e) {
            }
        } else {
            PendingIntent pendingIntent = null;
            PendingIntent pendingIntent2 = null;
            if (sentIntents != null && sentIntents.size() > 0) {
                pendingIntent = (PendingIntent) sentIntents.get(0);
            }
            if (deliveryIntents != null && deliveryIntents.size() > 0) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(0);
            }
            sendTextMessageInternal(destinationAddress, scAddress, (String) parts.get(0), pendingIntent, pendingIntent2, persistMessage, priority, isExpectMore, validityPeriod);
        }
    }

    private void sendMultipartTextMessageInternalOem(String destinationAddress, String scAddress, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessage, int priority, boolean isExpectMore, int validityPeriod, int encodingType) {
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (parts == null || parts.size() < 1) {
            throw new IllegalArgumentException("Invalid message body");
        } else if (parts.size() > 1) {
            try {
                ISms iccISms = getISmsServiceOrThrow();
                if (iccISms != null) {
                    iccISms.sendMultipartTextForSubscriberWithOptionsOem(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, parts, sentIntents, deliveryIntents, persistMessage, priority, isExpectMore, validityPeriod, encodingType);
                }
            } catch (RemoteException e) {
            }
        } else {
            PendingIntent pendingIntent = null;
            PendingIntent pendingIntent2 = null;
            if (sentIntents != null && sentIntents.size() > 0) {
                pendingIntent = (PendingIntent) sentIntents.get(0);
            }
            if (deliveryIntents != null && deliveryIntents.size() > 0) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(0);
            }
            sendTextMessageInternalOem(destinationAddress, scAddress, (String) parts.get(0), pendingIntent, pendingIntent2, persistMessage, priority, isExpectMore, validityPeriod, encodingType);
        }
    }

    public void sendMultipartTextMessageWithoutPersisting(String destinationAddress, String scAddress, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, int priority, boolean isExpectMore, int validityPeriod) {
        sendMultipartTextMessageInternal(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, false, priority, isExpectMore, validityPeriod);
    }

    public void sendDataMessage(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        SeempLog.record_str(73, destinationAddress);
        if (TextUtils.isEmpty(destinationAddress)) {
            throw new IllegalArgumentException("Invalid destinationAddress");
        } else if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Invalid message data");
        } else {
            final List<SubscriptionInfo> operatorSubInfoList = new ArrayList();
            final Context context = ActivityThread.currentApplication().getApplicationContext();
            if (isNeedDisplayPickSimCardDialog(context, operatorSubInfoList) && operatorSubInfoList.size() == 2) {
                String delayDestinationAddress = destinationAddress;
                String delayScAddress = scAddress;
                short delayDestinationPort = destinationPort;
                byte[] delayData = data;
                PendingIntent delaySentIntent = sentIntent;
                PendingIntent delayDeliveryIntent = deliveryIntent;
                final String str = destinationAddress;
                final String str2 = scAddress;
                final short s = destinationPort;
                final byte[] bArr = data;
                final PendingIntent pendingIntent = sentIntent;
                final PendingIntent pendingIntent2 = deliveryIntent;
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Looper.prepare();
                            final Looper looper = Looper.myLooper();
                            final String str = str;
                            final String str2 = str2;
                            final short s = s;
                            final byte[] bArr = bArr;
                            final PendingIntent pendingIntent = pendingIntent;
                            final PendingIntent pendingIntent2 = pendingIntent2;
                            SmsManager.this.startPickSimCardActivity(context, new Handler() {
                                public void handleMessage(Message msg) {
                                    try {
                                        Log.d(SmsManager.TAG, "receive msg.what = " + msg.what + ", msg.arg1 =" + msg.arg1);
                                        switch (msg.what) {
                                            case 1:
                                                try {
                                                    SmsManager.getISmsServiceOrThrow().sendDataForSubscriber(msg.arg1, ActivityThread.currentPackageName(), str, str2, s & 65535, bArr, pendingIntent, pendingIntent2);
                                                    break;
                                                } catch (RemoteException e) {
                                                    break;
                                                }
                                        }
                                        looper.quit();
                                    } catch (Exception e2) {
                                    }
                                }
                            }, operatorSubInfoList);
                            Looper.loop();
                        } catch (Exception e) {
                            try {
                                SmsManager.getISmsServiceOrThrow().sendDataForSubscriber(SmsManager.this.getSubscriptionId(), ActivityThread.currentPackageName(), str, str2, s & 65535, bArr, pendingIntent, pendingIntent2);
                            } catch (RemoteException e2) {
                            }
                        }
                    }
                }).start();
                return;
            }
            try {
                getISmsServiceOrThrow().sendDataForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), destinationAddress, scAddress, destinationPort & 65535, data, sentIntent, deliveryIntent);
            } catch (RemoteException e) {
            }
        }
    }

    public void sendDataMessageWithSelfPermissions(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        SeempLog.record_str(73, destinationAddress);
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

    public static SmsManager getDefault() {
        return sInstance;
    }

    public static SmsManager getSmsManagerForSubscriptionId(int subId) {
        SmsManager smsManager;
        synchronized (sLockObject) {
            smsManager = (SmsManager) sSubInstances.get(Integer.valueOf(subId));
            if (smsManager == null) {
                smsManager = new SmsManager(subId);
                sSubInstances.put(Integer.valueOf(subId), smsManager);
            }
        }
        return smsManager;
    }

    private SmsManager(int subId) {
        this.mSubId = subId;
    }

    public int getSubscriptionId() {
        int subId = this.mSubId == DEFAULT_SUBSCRIPTION_ID ? getDefaultSmsSubscriptionId() : this.mSubId;
        boolean isSmsSimPickActivityNeeded = false;
        Context context = ActivityThread.currentApplication().getApplicationContext();
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                isSmsSimPickActivityNeeded = iccISms.isSmsSimPickActivityNeeded(subId);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Exception in getSubscriptionId");
        }
        if (isSmsSimPickActivityNeeded) {
            Log.d(TAG, "getSubscriptionId isSmsSimPickActivityNeeded is true");
            Intent intent = new Intent();
            intent.setClassName("com.qualcomm.qti.simsettings", "com.qualcomm.qti.simsettings.SimDialogActivity");
            intent.addFlags(268435456);
            intent.putExtra(DIALOG_TYPE_KEY, 2);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e2) {
                Log.e(TAG, "Unable to launch Settings application.");
            }
        }
        return subId;
    }

    private static ISms getISmsServiceOrThrow() {
        ISms iccISms = getISmsService();
        if (iccISms != null) {
            return iccISms;
        }
        throw new UnsupportedOperationException("Sms is not supported");
    }

    private static ISms getISmsService() {
        return Stub.asInterface(ServiceManager.getService("isms"));
    }

    public boolean copyMessageToIcc(byte[] smsc, byte[] pdu, int status) {
        SeempLog.record(79);
        if (pdu == null) {
            throw new IllegalArgumentException("pdu is NULL");
        }
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.copyMessageToIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), status, pdu, smsc);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean deleteMessageFromIcc(int messageIndex) {
        SeempLog.record(80);
        byte[] pdu = new byte[175];
        Arrays.fill(pdu, (byte) -1);
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.updateMessageOnIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), messageIndex, 0, pdu);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean updateMessageOnIcc(int messageIndex, int newStatus, byte[] pdu) {
        SeempLog.record(81);
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.updateMessageOnIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName(), messageIndex, newStatus, pdu);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public ArrayList<SmsMessage> getAllMessagesFromIcc() {
        List<SmsRawData> records = null;
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                records = iccISms.getAllMessagesFromIccEfForSubscriber(getSubscriptionId(), ActivityThread.currentPackageName());
            }
        } catch (RemoteException e) {
        }
        return createMessageListFromRawRecords(records);
    }

    public boolean enableCellBroadcast(int messageIdentifier, int ranType) {
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.enableCellBroadcastForSubscriber(getSubscriptionId(), messageIdentifier, ranType);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean disableCellBroadcast(int messageIdentifier, int ranType) {
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.disableCellBroadcastForSubscriber(getSubscriptionId(), messageIdentifier, ranType);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean enableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (endMessageId < startMessageId) {
            throw new IllegalArgumentException("endMessageId < startMessageId");
        }
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.enableCellBroadcastRangeForSubscriber(getSubscriptionId(), startMessageId, endMessageId, ranType);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId, int ranType) {
        if (endMessageId < startMessageId) {
            throw new IllegalArgumentException("endMessageId < startMessageId");
        }
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.disableCellBroadcastRangeForSubscriber(getSubscriptionId(), startMessageId, endMessageId, ranType);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    private ArrayList<SmsMessage> createMessageListFromRawRecords(List<SmsRawData> records) {
        ArrayList<SmsMessage> messages = new ArrayList();
        if (records != null) {
            int count = records.size();
            for (int i = 0; i < count; i++) {
                SmsRawData data = (SmsRawData) records.get(i);
                if (data != null) {
                    SmsMessage sms = SmsMessage.createFromEfRecord(i + 1, data.getBytes(), getSubscriptionId());
                    if (sms != null) {
                        messages.add(sms);
                    }
                }
            }
        }
        return messages;
    }

    public boolean isImsSmsSupported() {
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.isImsSmsSupportedForSubscriber(getSubscriptionId());
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public String getImsSmsFormat() {
        String format = SmsConstants.FORMAT_UNKNOWN;
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.getImsSmsFormatForSubscriber(getSubscriptionId());
            }
            return format;
        } catch (RemoteException e) {
            return format;
        }
    }

    public static int getDefaultSmsSubscriptionId() {
        try {
            return Stub.asInterface(ServiceManager.getService("isms")).getPreferredSmsSubscription();
        } catch (RemoteException e) {
            return -1;
        } catch (NullPointerException e2) {
            return -1;
        }
    }

    public boolean isSMSPromptEnabled() {
        try {
            return Stub.asInterface(ServiceManager.getService("isms")).isSMSPromptEnabled();
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public int getSmsCapacityOnIcc() {
        try {
            ISms iccISms = getISmsService();
            if (iccISms != null) {
                return iccISms.getSmsCapacityOnIccForSubscriber(getSubscriptionId());
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public void sendMultimediaMessage(Context context, Uri contentUri, String locationUrl, Bundle configOverrides, PendingIntent sentIntent) {
        if (contentUri == null) {
            throw new IllegalArgumentException("Uri contentUri null");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                iMms.sendMessage(getSubscriptionId(), ActivityThread.currentPackageName(), contentUri, locationUrl, configOverrides, sentIntent);
            }
        } catch (RemoteException e) {
        }
    }

    public void downloadMultimediaMessage(Context context, String locationUrl, Uri contentUri, Bundle configOverrides, PendingIntent downloadedIntent) {
        if (TextUtils.isEmpty(locationUrl)) {
            throw new IllegalArgumentException("Empty MMS location URL");
        } else if (contentUri == null) {
            throw new IllegalArgumentException("Uri contentUri null");
        } else {
            try {
                IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
                if (iMms != null) {
                    iMms.downloadMessage(getSubscriptionId(), ActivityThread.currentPackageName(), locationUrl, contentUri, configOverrides, downloadedIntent);
                }
            } catch (RemoteException e) {
            }
        }
    }

    public Uri importTextMessage(String address, int type, String text, long timestampMillis, boolean seen, boolean read) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.importTextMessage(ActivityThread.currentPackageName(), address, type, text, timestampMillis, seen, read);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public Uri importMultimediaMessage(Uri contentUri, String messageId, long timestampSecs, boolean seen, boolean read) {
        if (contentUri == null) {
            throw new IllegalArgumentException("Uri contentUri null");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.importMultimediaMessage(ActivityThread.currentPackageName(), contentUri, messageId, timestampSecs, seen, read);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public boolean deleteStoredMessage(Uri messageUri) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.deleteStoredMessage(ActivityThread.currentPackageName(), messageUri);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public boolean deleteStoredConversation(long conversationId) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.deleteStoredConversation(ActivityThread.currentPackageName(), conversationId);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public boolean updateStoredMessageStatus(Uri messageUri, ContentValues statusValues) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.updateStoredMessageStatus(ActivityThread.currentPackageName(), messageUri, statusValues);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public boolean archiveStoredConversation(long conversationId, boolean archived) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.archiveStoredConversation(ActivityThread.currentPackageName(), conversationId, archived);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public Uri addTextMessageDraft(String address, String text) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.addTextMessageDraft(ActivityThread.currentPackageName(), address, text);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public Uri addMultimediaMessageDraft(Uri contentUri) {
        if (contentUri == null) {
            throw new IllegalArgumentException("Uri contentUri null");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.addMultimediaMessageDraft(ActivityThread.currentPackageName(), contentUri);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public void sendStoredTextMessage(Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        try {
            getISmsServiceOrThrow().sendStoredText(getSubscriptionId(), ActivityThread.currentPackageName(), messageUri, scAddress, sentIntent, deliveryIntent);
        } catch (RemoteException e) {
        }
    }

    public void sendStoredMultipartTextMessage(Uri messageUri, String scAddress, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        try {
            getISmsServiceOrThrow().sendStoredMultipartText(getSubscriptionId(), ActivityThread.currentPackageName(), messageUri, scAddress, sentIntents, deliveryIntents);
        } catch (RemoteException e) {
        }
    }

    public void sendStoredMultimediaMessage(Uri messageUri, Bundle configOverrides, PendingIntent sentIntent) {
        if (messageUri == null) {
            throw new IllegalArgumentException("Empty message URI");
        }
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                iMms.sendStoredMessage(getSubscriptionId(), ActivityThread.currentPackageName(), messageUri, configOverrides, sentIntent);
            }
        } catch (RemoteException e) {
        }
    }

    public void setAutoPersisting(boolean enabled) {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                iMms.setAutoPersisting(ActivityThread.currentPackageName(), enabled);
            }
        } catch (RemoteException e) {
        }
    }

    public boolean getAutoPersisting() {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.getAutoPersisting();
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public Bundle getCarrierConfigValues() {
        try {
            IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
            if (iMms != null) {
                return iMms.getCarrierConfigValues(getSubscriptionId());
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public String createAppSpecificSmsToken(PendingIntent intent) {
        try {
            return getISmsServiceOrThrow().createAppSpecificSmsToken(getSubscriptionId(), ActivityThread.currentPackageName(), intent);
        } catch (RemoteException ex) {
            ex.rethrowFromSystemServer();
            return null;
        }
    }

    public static Bundle getMmsConfig(BaseBundle config) {
        Bundle filtered = new Bundle();
        filtered.putBoolean("enabledTransID", config.getBoolean("enabledTransID"));
        filtered.putBoolean("enabledMMS", config.getBoolean("enabledMMS"));
        filtered.putBoolean("enableGroupMms", config.getBoolean("enableGroupMms"));
        filtered.putBoolean("enabledNotifyWapMMSC", config.getBoolean("enabledNotifyWapMMSC"));
        filtered.putBoolean("aliasEnabled", config.getBoolean("aliasEnabled"));
        filtered.putBoolean("allowAttachAudio", config.getBoolean("allowAttachAudio"));
        filtered.putBoolean("enableMultipartSMS", config.getBoolean("enableMultipartSMS"));
        filtered.putBoolean("enableSMSDeliveryReports", config.getBoolean("enableSMSDeliveryReports"));
        filtered.putBoolean("supportMmsContentDisposition", config.getBoolean("supportMmsContentDisposition"));
        filtered.putBoolean("sendMultipartSmsAsSeparateMessages", config.getBoolean("sendMultipartSmsAsSeparateMessages"));
        filtered.putBoolean("enableMMSReadReports", config.getBoolean("enableMMSReadReports"));
        filtered.putBoolean("enableMMSDeliveryReports", config.getBoolean("enableMMSDeliveryReports"));
        filtered.putBoolean("mmsCloseConnection", config.getBoolean("mmsCloseConnection"));
        filtered.putInt("maxMessageSize", config.getInt("maxMessageSize"));
        filtered.putInt("maxImageWidth", config.getInt("maxImageWidth"));
        filtered.putInt("maxImageHeight", config.getInt("maxImageHeight"));
        filtered.putInt("recipientLimit", config.getInt("recipientLimit"));
        filtered.putInt("aliasMinChars", config.getInt("aliasMinChars"));
        filtered.putInt("aliasMaxChars", config.getInt("aliasMaxChars"));
        filtered.putInt("smsToMmsTextThreshold", config.getInt("smsToMmsTextThreshold"));
        filtered.putInt("smsToMmsTextLengthThreshold", config.getInt("smsToMmsTextLengthThreshold"));
        filtered.putInt("maxMessageTextSize", config.getInt("maxMessageTextSize"));
        filtered.putInt("maxSubjectLength", config.getInt("maxSubjectLength"));
        filtered.putInt("httpSocketTimeout", config.getInt("httpSocketTimeout"));
        filtered.putString("uaProfTagName", config.getString("uaProfTagName"));
        filtered.putString("userAgent", config.getString("userAgent"));
        filtered.putString("uaProfUrl", config.getString("uaProfUrl"));
        filtered.putString("httpParams", config.getString("httpParams"));
        filtered.putString("emailGatewayNumber", config.getString("emailGatewayNumber"));
        filtered.putString("naiSuffix", config.getString("naiSuffix"));
        filtered.putBoolean("config_cellBroadcastAppLinks", config.getBoolean("config_cellBroadcastAppLinks"));
        filtered.putBoolean("supportHttpCharsetHeader", config.getBoolean("supportHttpCharsetHeader"));
        return filtered;
    }

    public static SmsManager getSmsManagerForSubscriber(int subId) {
        return getSmsManagerForSubscriptionId(subId);
    }

    public static SmsManager oppogetSmsManagerForSubscriber(int subId) {
        return getSmsManagerForSubscriptionId(subId);
    }

    private SmsBroadcastConfigInfo Convert2SmsBroadcastConfigInfo(SmsCbConfigInfo info) {
        return new SmsBroadcastConfigInfo(info.mFromServiceId, info.mToServiceId, info.mFromCodeScheme, info.mToCodeScheme, info.mSelected);
    }

    private SmsCbConfigInfo Convert2SmsCbConfigInfo(SmsBroadcastConfigInfo info) {
        return new SmsCbConfigInfo(info.getFromServiceId(), info.getToServiceId(), info.getFromCodeScheme(), info.getToCodeScheme(), info.isSelected());
    }

    public SmsBroadcastConfigInfo[] getCellBroadcastSmsConfig() {
        Rlog.d(TAG, "getCellBroadcastSmsConfig");
        Rlog.d(TAG, "subId=" + getSubscriptionId());
        SmsCbConfigInfo[] configs = null;
        try {
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                configs = iccISms.getCellBroadcastSmsConfigForSubscriber(getSubscriptionId());
            } else {
                Rlog.d(TAG, "fail to get sms service");
            }
        } catch (RemoteException e) {
            Rlog.d(TAG, "RemoteException");
        }
        if (configs != null) {
            Rlog.d(TAG, "config length = " + configs.length);
            if (configs.length != 0) {
                SmsBroadcastConfigInfo[] result = new SmsBroadcastConfigInfo[configs.length];
                for (int i = 0; i < configs.length; i++) {
                    result[i] = Convert2SmsBroadcastConfigInfo(configs[i]);
                }
                return result;
            }
        }
        return null;
    }

    public boolean setCellBroadcastSmsConfig(SmsBroadcastConfigInfo[] channels, SmsBroadcastConfigInfo[] languages) {
        Rlog.d(TAG, "setCellBroadcastSmsConfig");
        Rlog.d(TAG, "subId=" + getSubscriptionId());
        if (channels != null) {
            Rlog.d(TAG, "channel size=" + channels.length);
        } else {
            Rlog.d(TAG, "channel size=0");
        }
        if (languages != null) {
            Rlog.d(TAG, "language size=" + languages.length);
        } else {
            Rlog.d(TAG, "language size=0");
        }
        try {
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                int i;
                SmsCbConfigInfo[] smsCbConfigInfoArr = null;
                SmsCbConfigInfo[] languageInfos = null;
                if (!(channels == null || channels.length == 0)) {
                    smsCbConfigInfoArr = new SmsCbConfigInfo[channels.length];
                    for (i = 0; i < channels.length; i++) {
                        smsCbConfigInfoArr[i] = Convert2SmsCbConfigInfo(channels[i]);
                    }
                }
                if (languages != null) {
                    if (languages.length != 0) {
                        languageInfos = new SmsCbConfigInfo[languages.length];
                        for (i = 0; i < languages.length; i++) {
                            languageInfos[i] = Convert2SmsCbConfigInfo(languages[i]);
                        }
                    }
                }
                return iccISms.setCellBroadcastSmsConfigForSubscriber(getSubscriptionId(), smsCbConfigInfoArr, languageInfos);
            }
            Rlog.d(TAG, "fail to get sms service");
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "setCellBroadcastSmsConfig, RemoteException!");
            return false;
        }
    }

    public boolean queryCellBroadcastSmsActivation() {
        Rlog.d(TAG, "queryCellBroadcastSmsActivation");
        Rlog.d(TAG, "subId=" + getSubscriptionId());
        try {
            ISms iccISms = getISmsServiceOrThrow();
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
            ISms iccISms = getISmsServiceOrThrow();
            if (iccISms != null) {
                return iccISms.activateCellBroadcastSmsForSubscriber(getSubscriptionId(), activate);
            }
            Rlog.d(TAG, "fail to get sms service");
            return false;
        } catch (RemoteException e) {
            Rlog.d(TAG, "fail to activate CB");
            return false;
        }
    }

    private boolean isNeedDisplayPickSimCardDialog(Context context, List<SubscriptionInfo> operatorSubInfoList) {
        boolean isNeedDisplayDialog = false;
        try {
            Log.d(TAG, "isNeedDisplayPickSimCardDialog: mSubId = " + this.mSubId + ", pkgName = " + context.getPackageName());
            if (isWhiteListPackageName(context)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                List<SubscriptionInfo> subInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
                if (subInfoList != null) {
                    int subInfoLength = subInfoList.size();
                    int canSendMessageCardCount = 0;
                    int softSimSlotId = ColorOSTelephonyManager.getDefault(context).colorGetSoftSimCardSlotId();
                    for (int i = 0; i < subInfoLength; i++) {
                        SubscriptionInfo sir = (SubscriptionInfo) subInfoList.get(i);
                        if (softSimSlotId != sir.getSimSlotIndex() && SubscriptionManager.getSubState(sir.getSubscriptionId()) == 1) {
                            operatorSubInfoList.add(sir);
                            canSendMessageCardCount++;
                        }
                    }
                    if (canSendMessageCardCount == 2) {
                        isNeedDisplayDialog = true;
                    }
                }
            } else {
                isNeedDisplayDialog = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "isNeedDisplayPickSimCardDialog: isNeedDisplayDialog = " + isNeedDisplayDialog);
        return isNeedDisplayDialog;
    }

    private boolean isWhiteListPackageName(Context context) {
        boolean isWhiteListPackageName = false;
        String allPkg = Global.getString(context.getContentResolver(), ROMUPDATE_SEND_MESSAGE_PKG);
        Log.d(TAG, "isWhiteListPackageName: allPkg = " + allPkg);
        if (TextUtils.isEmpty(allPkg)) {
            allPkg = DEFAULT_PACKAGE;
        }
        String[] splitAllPkg = allPkg.split(";");
        if (splitAllPkg != null && splitAllPkg.length > 0) {
            for (String trim : splitAllPkg) {
                if (trim.trim().equals(context.getPackageName())) {
                    isWhiteListPackageName = true;
                    break;
                }
            }
        }
        Log.d(TAG, "isWhiteListPackageName: isWhiteListPackageName = " + isWhiteListPackageName);
        return isWhiteListPackageName;
    }

    private String getAppLabel(Context context) {
        String pkg = "";
        try {
            pkg = context.getPackageName();
            Log.d(TAG, "isWhiteListPackageName: pkg = " + pkg);
            String rt = context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(pkg, 0)).toString();
            if (TextUtils.isEmpty(rt)) {
                return pkg;
            }
            return rt;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPickSimCardActivity(Context context, Handler handler, List<SubscriptionInfo> operatorSubInfoList) {
        Intent intent = new Intent();
        intent.setClassName("com.coloros.simsettings", "com.coloros.simsettings.SendSmsPickSimCardAlertDialog");
        intent.addFlags(268435456);
        intent.putExtra(APK_LABEL_NAME, getAppLabel(context));
        intent.putExtra(APK_PACKAGE_NAME, context.getPackageName());
        intent.putExtra(SIM_NAME_ONE, ((SubscriptionInfo) operatorSubInfoList.get(0)).getDisplayName());
        intent.putExtra(SIM_NAME_TWO, ((SubscriptionInfo) operatorSubInfoList.get(1)).getDisplayName());
        intent.putExtra(SUBSCRIPTION_ID_ONE, ((SubscriptionInfo) operatorSubInfoList.get(0)).getSubscriptionId());
        intent.putExtra(SUBSCRIPTION_ID_TWO, ((SubscriptionInfo) operatorSubInfoList.get(1)).getSubscriptionId());
        intent.putExtra(MESSENGER, new Messenger(handler));
        context.startActivity(intent);
    }
}
