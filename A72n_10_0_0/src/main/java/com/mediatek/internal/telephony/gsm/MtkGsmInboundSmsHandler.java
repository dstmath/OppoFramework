package com.mediatek.internal.telephony.gsm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.gsm.SmsMessage;
import com.mediatek.internal.telephony.MtkInboundSmsTracker;
import com.mediatek.internal.telephony.MtkWapPushOverSms;
import com.mediatek.internal.telephony.util.MtkSmsCommonUtil;
import mediatek.telephony.MtkSmsMessage;

public class MtkGsmInboundSmsHandler extends GsmInboundSmsHandler {
    private static final int RESULT_SMS_ACCEPT_BY_PPL = 1;
    private static final int RESULT_SMS_REJECT_BY_PPL = 0;
    private String mTag = "MtkGsmInboundSmsHandler";

    public MtkGsmInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone) {
        super(context, storageMonitor, phone);
        this.mTag = "MtkGsmInboundSmsHandler-" + phone.getPhoneId();
        log("created InboundSmsHandler from MtkGsmInboundSmsHandler");
    }

    public static MtkGsmInboundSmsHandler makeInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone) {
        MtkGsmInboundSmsHandler handler = new MtkGsmInboundSmsHandler(context, storageMonitor, phone);
        handler.start();
        return handler;
    }

    /* access modifiers changed from: protected */
    public int dispatchMessageRadioSpecific(SmsMessageBase smsb) {
        SmsMessage smsMessage = (SmsMessage) smsb;
        return MtkGsmInboundSmsHandler.super.dispatchMessageRadioSpecific(smsb);
    }

    private void handleAutoRegMessage(byte[] pdu) {
        ProxyController.getInstance().getDeviceRegisterController().handleAutoRegMessage(this.mPhone.getSubId(), "3gpp", pdu);
    }

    /* access modifiers changed from: protected */
    public int addTrackerToRawTableAndSendMessage(InboundSmsTracker tracker, boolean deDup) {
        ((MtkInboundSmsTracker) tracker).setSubId(this.mPhone.getSubId());
        return MtkGsmInboundSmsHandler.super.addTrackerToRawTableAndSendMessage(tracker, deDup);
    }

    public void dispatchIntent(Intent intent, String permission, int appOp, Bundle opts, BroadcastReceiver resultReceiver, UserHandle user) {
        intent.putExtra("rTime", System.currentTimeMillis());
        MtkGsmInboundSmsHandler.super.dispatchIntent(intent, permission, appOp, opts, resultReceiver, user);
    }

    /* access modifiers changed from: protected */
    public void deleteFromRawTable(String deleteWhere, String[] deleteWhereArgs, int deleteType) {
        if (deleteType == 1) {
            Uri uri = sRawUriPermanentDelete;
        } else {
            Uri uri2 = sRawUri;
        }
        if (deleteWhere == null && deleteWhereArgs == null) {
            loge("No rows need be deleted from raw table!");
        } else {
            MtkGsmInboundSmsHandler.super.deleteFromRawTable(deleteWhere, deleteWhereArgs, deleteType);
        }
    }

    private int checkPplPermission(byte[][] pdus, String format) {
        if (((!is3gpp2() || format.compareTo("3gpp2") != 0) && (is3gpp2() || format.compareTo("3gpp") != 0)) || MtkSmsCommonUtil.phonePrivacyLockCheck(pdus, format, this.mContext, this.mPhone.getSubId()) == 0) {
            return 1;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public String[] onModifyQueryWhereArgs(String[] whereArgs) {
        return new String[]{whereArgs[0], whereArgs[1], whereArgs[2], Integer.toString(this.mPhone.getSubId())};
    }

    /* access modifiers changed from: protected */
    public boolean onCheckIfStopProcessMessagePart(byte[][] pdus, String format, InboundSmsTracker tracker) {
        if (checkPplPermission(pdus, format) == 1) {
            return false;
        }
        log("The message was blocked by Ppl! don't prompt to user");
        deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 1);
        return true;
    }

    /* access modifiers changed from: protected */
    public android.telephony.SmsMessage onCreateSmsMessage(byte[] pdu, String format) {
        return MtkSmsMessage.createFromPdu(pdu, "3gpp");
    }

    /* access modifiers changed from: protected */
    public int onDispatchWapPdu(byte[][] smsPdus, byte[] pdu, BroadcastReceiver receiver, String address) {
        if (!MtkSmsCommonUtil.isWapPushSupport()) {
            return MtkGsmInboundSmsHandler.super.onDispatchWapPdu(smsPdus, pdu, receiver, address);
        }
        log("dispatch wap push pdu with addr & sc addr");
        Bundle bundle = new Bundle();
        MtkSmsMessage sms = MtkSmsMessage.createFromPdu(smsPdus[0], "3gpp");
        if (sms != null) {
            bundle.putString("address", sms.getOriginatingAddress());
            String sca = sms.getServiceCenterAddress();
            if (sca == null) {
                sca = "";
            }
            bundle.putString("service_center", sca);
        }
        return ((MtkWapPushOverSms) this.mWapPush).dispatchWapPdu(pdu, receiver, this, bundle);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        OppoRlog.Rlog.d(this.mTag, s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        OppoRlog.Rlog.e(this.mTag, s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s, Throwable e) {
        OppoRlog.Rlog.e(this.mTag, s, e);
    }
}
