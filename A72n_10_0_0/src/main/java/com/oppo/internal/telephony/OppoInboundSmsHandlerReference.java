package com.oppo.internal.telephony;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.IOppoInboundSmsHandler;
import com.android.internal.telephony.InboundSmsHandler;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.WapPushOverSms;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.telephony.util.ReflectionHelper;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseUtils;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

public class OppoInboundSmsHandlerReference implements IOppoInboundSmsHandler {
    private static final int CMATUO_REG_MT_SMS_CDMA = 0;
    private static final int CMATUO_REG_MT_SMS_IMS = 1;
    private static final int CMATUO_REG_MT_SMS_NORMAL = 2;
    private static final String CT_AUTO_IMS_REG_INTENT = "android.intent.action.RECEIVE_SMS_REG_ACK";
    public static final String CT_AUTO_IMS_REG_PACKAGE = "com.oppo.ctautoregist";
    private static final String CT_MSG_ADDRESS = "10659401";
    private static boolean DBG = true;
    private static final int GTS_MAX_TEST_TIME = 10;
    private static final String GTS_SMS_CLASS = "com.google.android.gts.telephony.imsservice.SmsReceiverQPlus";
    private static final String GTS_SMS_PACKAGE = "com.google.android.gts.telephony";
    private static final String HEALTHCHECK_INTENT = "oppo.intent.action.REMOTE_DIAGNOSIS_START";
    private static final String HEALTHCHECK_MSG_ADDRESS_END_DEBUG = "0115";
    private static final String HEALTHCHECK_MSG_ADDRESS_END_RELEASE = "3247";
    private static final String HEALTHCHECK_MSG_ADDRESS_HEAD = "1069";
    private static final int HEALTHCHECK_MT_SMS = 0;
    public static final String HEALTHCHECK_PACKAGE = "com.coloros.healthcheck";
    private static final String HEALTHCHECK_PERMISSION = "android.permission.BROADCAST_SMS";
    private static final int MT_SMS_CHECK_CARD_SUBID_TIME_MAX_TIME_FBE = 20;
    private static final int MT_SMS_CHECK_CARD_SUBID_TIME_MAX_TIME_NON_FBE = 5;
    private static final int MT_SMS_NORMAL = 1;
    private static final String OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE = "com.android.mms";
    private static final int OEM_SMS_MMS_SHOWDIALOG = 1;
    private static String TAG = "OppoInboundSmsHandlerReference";
    private static int mGtsTestTimer = 0;
    private boolean isRelease = false;
    private Object mOppoUsageManager = null;
    private InboundSmsHandler mRef;
    private Handler mUiHandler = null;

    public OppoInboundSmsHandlerReference(InboundSmsHandler ref) {
        this.mRef = ref;
        if (!(ref == null || ref.getPhone() == null)) {
            TAG = "OppoInboundSmsHandlerReference[" + ref.getPhone().getPhoneId() + "]";
        }
        DBG = OppoRlog.Rlog.isDebugSwitchOpen();
    }

    public void oemInitUIHandler(Context context, Context phoneContext) {
        if (context == null) {
            if (DBG) {
                OppoRlog.Rlog.d(TAG, "context == null");
            }
        } else if (phoneContext == null) {
            if (DBG) {
                OppoRlog.Rlog.d(TAG, "phoneContext == null");
            }
        } else if (context == phoneContext) {
            this.mUiHandler = new UIHandler(context, Looper.getMainLooper());
        } else if (DBG) {
            String str = TAG;
            OppoRlog.Rlog.d(str, "context=" + context + " phoneContext=" + phoneContext);
        }
    }

    private class UIHandler extends Handler {
        private Context mContext;
        private Looper mLooper;

        public UIHandler(Context context, Looper looper) {
            super(looper);
            this.mContext = context;
            this.mLooper = looper;
        }

        public void handleMessage(Message msg) {
            if (msg == null || this.mLooper != Looper.getMainLooper()) {
                if (OppoInboundSmsHandlerReference.DBG) {
                    String str = OppoInboundSmsHandlerReference.TAG;
                    OppoRlog.Rlog.d(str, "msg=" + msg);
                }
            } else if (msg.what == 1) {
                try {
                    if (Looper.myLooper() == Looper.getMainLooper() && this.mContext != null) {
                        Object style = ReflectionHelper.getDeclaredField((Object) null, "oppo.R$style", "Theme_OPPO_Dialog_Alert");
                        if (style != null) {
                            AlertDialog d = new AlertDialog.Builder(this.mContext, ((Integer) style).intValue()).setMessage(OemTelephonyUtils.getOemRes(this.mContext, "color_oppo_set_default_sms_mms_toast", "")).setTitle(OemTelephonyUtils.getOemRes(this.mContext, "color_oppo_set_default_sms_mms_toast_title", "")).setPositiveButton(OemTelephonyUtils.getOemRes(this.mContext, "color_oppo_set_default_sms_mms_toast_ok", ""), (DialogInterface.OnClickListener) null).create();
                            d.getWindow().setType(2038);
                            d.setCanceledOnTouchOutside(false);
                            d.setCancelable(false);
                            d.show();
                        }
                    } else if (OppoInboundSmsHandlerReference.DBG) {
                        String str2 = OppoInboundSmsHandlerReference.TAG;
                        OppoRlog.Rlog.d(str2, "context=" + this.mContext);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (OppoInboundSmsHandlerReference.DBG) {
                String str3 = OppoInboundSmsHandlerReference.TAG;
                OppoRlog.Rlog.d(str3, "error, msg" + msg);
            }
        }
    }

    private boolean oemIsInGtsTest(ComponentName lastSmsCompName) {
        int i;
        if (lastSmsCompName == null || (i = mGtsTestTimer) >= 10) {
            return false;
        }
        mGtsTestTimer = i + 1;
        String packageName = lastSmsCompName.getPackageName();
        String className = lastSmsCompName.getClassName();
        if (packageName == null || !packageName.equals(GTS_SMS_PACKAGE) || className == null || !className.equals(GTS_SMS_CLASS)) {
            return false;
        }
        OppoRlog.Rlog.d(TAG, "gts sms received");
        return true;
    }

    public void oemSetDefaultSms(Context context) {
        if (context == null) {
            try {
                if (DBG) {
                    OppoRlog.Rlog.e(TAG, "oemSetDefaultSms, context=null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (!OppoSmsCommonUtils.isExpRegion(context)) {
            ComponentName lastSmsCompName = SmsApplication.getDefaultSmsApplication(context, true);
            if (!oemIsInGtsTest(lastSmsCompName)) {
                if (lastSmsCompName == null) {
                    if (DBG) {
                        OppoRlog.Rlog.e(TAG, "lastSmsCompName == null");
                    }
                    SmsApplication.setDefaultApplication(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, context);
                    oemShowDialogSmsMMs();
                    return;
                }
                String lastSmsPackage = lastSmsCompName.getPackageName();
                if (DBG) {
                    String str = TAG;
                    OppoRlog.Rlog.e(str, "lastSmsPackage=" + lastSmsPackage);
                }
                if ((lastSmsPackage != null && !lastSmsPackage.equals(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE)) || TextUtils.isEmpty(lastSmsPackage)) {
                    SmsApplication.setDefaultApplication(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, context);
                    oemShowDialogSmsMMs();
                }
            }
        } else if (DBG) {
            OppoRlog.Rlog.d(TAG, "isDefaultMmsRegion-true");
        }
    }

    public void oemSetDefaultWappush(Context context) {
        if (context == null) {
            try {
                if (DBG) {
                    OppoRlog.Rlog.e(TAG, "oemSetDefaultWappush, context=null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (!OppoSmsCommonUtils.isExpRegion(context)) {
            ComponentName lastWappushsCompName = SmsApplication.getDefaultMmsApplication(context, true);
            if (!oemIsInGtsTest(lastWappushsCompName)) {
                if (lastWappushsCompName == null) {
                    if (DBG) {
                        OppoRlog.Rlog.e(TAG, "lastWappushsCompName == null");
                    }
                    SmsApplication.setDefaultApplication(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, context);
                    oemShowDialogSmsMMs();
                    return;
                }
                String lastWappushPackage = lastWappushsCompName.getPackageName();
                if (DBG) {
                    String str = TAG;
                    OppoRlog.Rlog.e(str, "lastWappushPackage=" + lastWappushPackage);
                }
                if ((lastWappushPackage != null && !lastWappushPackage.equals(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE)) || TextUtils.isEmpty(lastWappushPackage)) {
                    SmsApplication.setDefaultApplication(OEM_DEFAULT_SYSTEM_SMS_MMS_PACKAGE, context);
                    oemShowDialogSmsMMs();
                }
            }
        } else if (DBG) {
            OppoRlog.Rlog.d(TAG, "isDefaultMmsRegion-true");
        }
    }

    private void oemShowDialogSmsMMs() {
        if (this.mUiHandler == null) {
            OppoRlog.Rlog.e(NetworkDiagnoseUtils.INFO_OTHER_SMS, "warning:oemShowDialogSmsMMs mUiHandler==null");
            return;
        }
        Message msg = Message.obtain();
        msg.what = 1;
        this.mUiHandler.sendMessage(msg);
    }

    public void oemSetWapPushScAddress(WapPushOverSms wapPush, byte[][] pdus, InboundSmsTracker tracker) {
        try {
            ReflectionHelper.setDeclaredField(wapPush, "com.android.internal.telephony.AbstractWapPushOverSms", "mScAddress", "");
            if (!((Boolean) ReflectionHelper.callMethod(tracker, "com.android.internal.telephony.InboundSmsTracker", "is3gpp2WapPdu", new Class[0], new Object[0])).booleanValue()) {
                String sca = SmsMessage.createFromPdu(pdus[0], tracker.getFormat()).getServiceCenterAddress();
                if (sca == null) {
                    sca = "";
                }
                ReflectionHelper.setDeclaredField(wapPush, "com.android.internal.telephony.AbstractWapPushOverSms", "mScAddress", sca);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void oemRemoveAbortFlag(Intent intent, UserHandle user) {
        try {
            String str = TAG;
            OppoRlog.Rlog.d(str, "user=" + user + ", intent=" + intent);
            if (!(intent == null || intent.getAction() == null || !intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))) {
                intent.removeFlags(134217728);
            }
            try {
                int[] users = ActivityManager.getService().getRunningUserIds();
                if (users == null && user != null) {
                    users = new int[]{user.getIdentifier()};
                }
                if (users != null) {
                    String str2 = TAG;
                    OppoRlog.Rlog.d(str2, "users.size=" + users.length);
                }
            } catch (Exception e) {
                OppoRlog.Rlog.e(TAG, "Unable to access ActivityManagerService");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void oemMtSmsCount(Intent intent) {
        try {
            String action = intent.getAction();
            String str = TAG;
            OppoRlog.Rlog.d(str, "onReceive--=" + action);
            if (this.mOppoUsageManager == null) {
                this.mOppoUsageManager = ReflectionHelper.callMethod((Object) null, "android.os.OppoUsageManager", "getOppoUsageManager", new Class[0], new Object[0]);
            }
            if (this.mOppoUsageManager != null && action != null) {
                if (action.equals("android.provider.Telephony.SMS_DELIVER") || action.equals("android.provider.Telephony.WAP_PUSH_DELIVER")) {
                    if (DBG) {
                        OppoRlog.Rlog.d(TAG, "accumulate the count of the received sms");
                    }
                    ReflectionHelper.callMethod(this.mOppoUsageManager, "android.os.OppoUsageManager", "accumulateHistoryCountOfReceivedMsg", new Class[]{Integer.TYPE}, new Object[]{1});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean oemIsProgressing() {
        try {
            String state = this.mRef.getCurrentState().getName();
            if (state != null && !state.equals("IdleState")) {
                String str = TAG;
                OppoRlog.Rlog.d(str, "oemIsProgressing, state=" + state);
            }
            if (state == null) {
                return false;
            }
            if (state.equals("WaitingState") || state.equals("DeliveringState")) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public Uri oemCheckSubIdWhenMtSms(UserManager userManager, Phone phone) {
        Uri ret = (Uri) ReflectionHelper.getDeclaredField(this.mRef, "com.android.internal.telephony.InboundSmsHandler", "sRawUriStaticFinal");
        try {
            OppoRlog.Rlog.d(TAG, "check, before");
            if (userManager == null || phone == null) {
                String str = TAG;
                OppoRlog.Rlog.e(str, "oemCheckSubIdWhenMtSms, warning!1, phone=" + phone + ", userManager=" + userManager);
            } else {
                String str2 = TAG;
                OppoRlog.Rlog.d(str2, "subId=" + phone.getSubId());
                if (!userManager.isUserUnlocked()) {
                    for (int i = 0; i < 20; i++) {
                        if (!SubscriptionController.getInstance().isActiveSubId(phone.getSubId())) {
                            Thread.sleep(1000);
                            String str3 = TAG;
                            OppoRlog.Rlog.d(str3, "sleep fbe," + i);
                        }
                    }
                } else {
                    for (int i2 = 0; i2 < 5; i2++) {
                        if (!SubscriptionController.getInstance().isActiveSubId(phone.getSubId())) {
                            Thread.sleep(1000);
                            String str4 = TAG;
                            OppoRlog.Rlog.d(str4, "sleep nonfbe," + i2);
                        }
                    }
                }
            }
            if (phone == null || !SubscriptionController.getInstance().isActiveSubId(phone.getSubId())) {
                String str5 = TAG;
                OppoRlog.Rlog.e(str5, "oemCheckSubIdWhenMtSms, warning!2, phone=" + phone);
            } else {
                Uri uri = Telephony.Sms.CONTENT_URI;
                ret = Uri.withAppendedPath(uri, "raw/" + phone.getSubId());
            }
        } catch (Exception e) {
            OppoRlog.Rlog.e(TAG, "checkSubIdWhenMoSms error");
        }
        String str6 = TAG;
        OppoRlog.Rlog.d(str6, "check, after, ret=" + ret);
        return ret;
    }

    public boolean oemIsCurrentFormat3gpp2(Context context) {
        boolean ret = true;
        try {
            if (this.mRef == null) {
                OppoRlog.Rlog.e(TAG, "mRef == null");
                return false;
            } else if (this.mRef.getPhone() == null) {
                OppoRlog.Rlog.e(TAG, "mRef.getPhone() == null");
                return false;
            } else {
                int activePhone = TelephonyManager.getDefault().getCurrentPhoneType(this.mRef.getPhone().getSubId());
                boolean ret2 = 2 == activePhone;
                OppoRlog.Rlog.d(TAG, "1,oemIsCurrentFormat3gpp2=" + ret2 + ", activePhone=" + activePhone);
                return ret2;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
                if (tm != null) {
                    int activePhone2 = tm.getCurrentPhoneType(this.mRef.getPhone().getSubId());
                    if (2 != activePhone2) {
                        ret = false;
                    }
                    OppoRlog.Rlog.d(TAG, "2,oemIsCurrentFormat3gpp2=" + ret + ", activePhone=" + activePhone2);
                    return ret;
                }
            } catch (Exception e) {
                OppoRlog.Rlog.d(TAG, "oemIsCurrentFormat3gpp2--error");
            }
            Object ret3 = ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.InboundSmsHandler", "isCurrentFormat3gpp2", new Class[0], new Object[0]);
            if (ret3 != null) {
                return ((Boolean) ret3).booleanValue();
            }
            return false;
        }
    }

    public boolean oemDealWithCtImsSms(byte[][] pdus, String format, Context context) {
        try {
            int phoneId = this.mRef.getPhone().getPhoneId();
            FullMessage fullMessage = getFullMessage(pdus, format);
            if (fullMessage != null) {
                int result = isSmsFromCTAutoRegServer(fullMessage.firstMessage);
                String str = TAG;
                OppoRlog.Rlog.d(str, "The compare result is " + result + " phoneId=" + phoneId);
                if (result != 0) {
                    if (result == 1) {
                    }
                }
                Intent intent = new Intent(CT_AUTO_IMS_REG_INTENT);
                intent.putExtra("phone", phoneId);
                intent.setFlags(268435456);
                intent.setPackage(CT_AUTO_IMS_REG_PACKAGE);
                if (result == 0) {
                    intent.putExtra("result", false);
                } else if (result == 1) {
                    intent.putExtra("result", true);
                }
                OppoRlog.Rlog.d(TAG, "sendOrderedBroadcast");
                context.sendOrderedBroadcast(intent, "android.permission.RECEIVE_SMS");
                return true;
            }
            OppoRlog.Rlog.d(TAG, "fullMessage == null");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static FullMessage getFullMessage(byte[][] pdus, String format) {
        FullMessage result = new FullMessage();
        StringBuilder builder = new StringBuilder();
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        for (byte[] pdu : pdus) {
            SmsMessage message = SmsMessage.createFromPdu(pdu, format);
            if (message == null) {
                OppoRlog.Rlog.e(TAG, "getFullMessage, message == null");
                return null;
            }
            if (result.firstMessage == null) {
                result.firstMessage = message;
            }
            String body = message.getMessageBody();
            if (body == null && message.getUserData() != null) {
                try {
                    body = decoder.decode(ByteBuffer.wrap(message.getUserData())).toString();
                } catch (CharacterCodingException e) {
                    return null;
                }
            }
            if (body != null) {
                builder.append(body);
            }
        }
        result.fullMessageBody = builder.toString();
        return result;
    }

    private int isSmsFromCTAutoRegServer(SmsMessage message) {
        if (message == null) {
            OppoRlog.Rlog.e(TAG, "Unable to create SmsMessage from PDU, cannot determine originating number");
            return 2;
        }
        String orgAddress = message.getOriginatingAddress();
        byte[] msgData = message.getUserData();
        if (PhoneNumberUtils.compare(CT_MSG_ADDRESS, orgAddress, true) && msgData.length >= 2) {
            if ((msgData[0] == 1 && msgData[1] == 4) || (msgData[0] == 2 && msgData[1] == 4)) {
                OppoRlog.Rlog.d(TAG, "find ct mt sms, cdma discard");
                return 0;
            } else if (msgData[0] == 3 && msgData[1] == 4) {
                OppoRlog.Rlog.d(TAG, "find ct mt sms, ims to autoreg");
                return 1;
            } else {
                OppoRlog.Rlog.d(TAG, "need care about it");
            }
        }
        return 2;
    }

    /* access modifiers changed from: private */
    public static class FullMessage {
        public SmsMessage firstMessage;
        public String fullMessageBody;

        private FullMessage() {
        }
    }

    private String isSmsMatchHealtheck(String messagebody) {
        try {
            String str = messagebody.trim();
            String smshealthcheck = "";
            if (str != null && !"".equals(str)) {
                for (int i = 0; i < str.length(); i++) {
                    if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
                        smshealthcheck = smshealthcheck + str.charAt(i);
                    }
                }
            }
            OppoRlog.Rlog.d(TAG, "string msg " + smshealthcheck);
            return smshealthcheck;
        } catch (Exception ex) {
            ex.printStackTrace();
            return " ";
        }
    }

    private int isSmsFromHealtheck(FullMessage message) {
        if (message == null) {
            OppoRlog.Rlog.e(TAG, "Unable to create SmsMessage from PDU, cannot determine originating number");
            return 1;
        }
        String orgAddress = message.firstMessage.getOriginatingAddress();
        if (orgAddress.length() < 8) {
            OppoRlog.Rlog.d(TAG, "address is small that 8");
            return 1;
        }
        if (orgAddress.contains("+")) {
            orgAddress = orgAddress.substring(3, orgAddress.length());
        }
        String str = TAG;
        OppoRlog.Rlog.d(str, "origin headaddress is" + orgAddress.substring(0, 4) + "origin endaddress is" + orgAddress.substring(orgAddress.length() - 4, orgAddress.length()));
        String healthchecknumber = isSmsMatchHealtheck(message.fullMessageBody);
        String str2 = TAG;
        OppoRlog.Rlog.d(str2, "string healthchecknumber " + healthchecknumber);
        if (!PhoneNumberUtils.compare(HEALTHCHECK_MSG_ADDRESS_HEAD, orgAddress.substring(0, 4), true)) {
            return 1;
        }
        if (PhoneNumberUtils.compare(HEALTHCHECK_MSG_ADDRESS_END_DEBUG, orgAddress.substring(orgAddress.length() - 4, orgAddress.length()), true) && healthchecknumber.length() > 5) {
            OppoRlog.Rlog.d(TAG, "find healtheckAddress sms");
            this.isRelease = false;
            return 0;
        } else if ((!PhoneNumberUtils.compare(HEALTHCHECK_MSG_ADDRESS_END_RELEASE, orgAddress.substring(orgAddress.length() - 5, orgAddress.length() - 1), true) && !PhoneNumberUtils.compare(HEALTHCHECK_MSG_ADDRESS_END_RELEASE, orgAddress.substring(orgAddress.length() - 4, orgAddress.length()), true)) || healthchecknumber.length() <= 5) {
            return 1;
        } else {
            OppoRlog.Rlog.d(TAG, "find healtheckAddress sms");
            this.isRelease = true;
            return 0;
        }
    }

    public boolean oemDealWithHealthcheckSms(byte[][] pdus, String format, Context context) {
        try {
            OppoRlog.Rlog.d(TAG, "oemDealWithHealthcheckSms start");
            int phoneId = this.mRef.getPhone().getPhoneId();
            FullMessage fullMessage = getFullMessage(pdus, format);
            if (fullMessage != null) {
                int result = isSmsFromHealtheck(fullMessage);
                String healthchecknumber = isSmsMatchHealtheck(fullMessage.fullMessageBody);
                String str = TAG;
                OppoRlog.Rlog.d(str, "The compare result is " + result + " phoneId=" + phoneId);
                if (result != 0) {
                    return false;
                }
                Intent intent = new Intent(HEALTHCHECK_INTENT);
                intent.putExtra("verify_code", healthchecknumber);
                intent.setFlags(268435456);
                intent.setPackage(HEALTHCHECK_PACKAGE);
                intent.putExtra("start_type", 1);
                intent.putExtra("is_relaese", this.isRelease);
                OppoRlog.Rlog.d(TAG, "sendOrderedBroadcast");
                context.sendOrderedBroadcast(intent, HEALTHCHECK_PERMISSION);
                return true;
            }
            OppoRlog.Rlog.d(TAG, "fullMessage == null");
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
