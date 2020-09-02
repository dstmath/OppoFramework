package com.mediatek.internal.telephony.cdma;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.UserHandle;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.cdma.CdmaInboundSmsHandler;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.cdma.SmsMessage;
import com.mediatek.internal.telephony.MtkInboundSmsTracker;
import com.mediatek.internal.telephony.ppl.IPplSmsFilter;
import com.mediatek.internal.telephony.util.MtkSmsCommonUtil;

public class MtkCdmaInboundSmsHandler extends CdmaInboundSmsHandler {
    private static final boolean ENG = "eng".equals(Build.TYPE);
    private static final int RESULT_SMS_ACCEPT_BY_PPL = 1;
    private static final int RESULT_SMS_REJECT_BY_PPL = 0;
    private static final int TELESERVICE_REG_SMS_CT = 65005;
    private static final boolean VDBG = false;
    private static final int WAKE_LOCK_TIMEOUT = 500;
    private String mTag = "MtkCdmaInboundSmsHandler";

    public MtkCdmaInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone, CdmaSMSDispatcher smsDispatcher) {
        super(context, storageMonitor, phone, smsDispatcher);
        this.mTag = "MtkCdmaInboundSmsHandler-" + phone.getPhoneId();
    }

    public void dispatchIntent(Intent intent, String permission, int appOp, Bundle opts, BroadcastReceiver resultReceiver, UserHandle user) {
        intent.putExtra("rTime", System.currentTimeMillis());
        MtkCdmaInboundSmsHandler.super.dispatchIntent(intent, permission, appOp, opts, resultReceiver, user);
    }

    /* access modifiers changed from: protected */
    public int addTrackerToRawTableAndSendMessage(InboundSmsTracker tracker, boolean deDup) {
        ((MtkInboundSmsTracker) tracker).setSubId(this.mPhone.getSubId());
        return MtkCdmaInboundSmsHandler.super.addTrackerToRawTableAndSendMessage(tracker, deDup);
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
            MtkCdmaInboundSmsHandler.super.deleteFromRawTable(deleteWhere, deleteWhereArgs, deleteType);
        }
    }

    /* access modifiers changed from: protected */
    public int dispatchMessageRadioSpecific(SmsMessageBase smsb) {
        SmsMessage sms = MtkSmsMessage.newMtkSmsMessage((SmsMessage) smsb);
        int ret = MtkCdmaInboundSmsHandler.super.dispatchMessageRadioSpecific(sms);
        if (ret != 4 || sms.getTeleService() != TELESERVICE_REG_SMS_CT || sms.getPdu() == null) {
            return ret;
        }
        handleAutoRegMessage(sms.getPdu());
        return 1;
    }

    private void handleAutoRegMessage(byte[] pdu) {
        if (pdu != null) {
            try {
                if (!(this.mContext == null || this.mPhone == null)) {
                    int subId = this.mPhone.getSubId();
                    log("send cdma reg message for subId = " + subId);
                    Intent intent = new Intent("android.telephony.sms.CDMA_REG_SMS_ACTION");
                    intent.putExtra("pdu", pdu);
                    intent.putExtra(IPplSmsFilter.KEY_FORMAT, "3gpp2");
                    intent.putExtra("subscription", subId);
                    PowerManager.WakeLock wakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "MtkCdmaInboundSmsHandlerOem");
                    wakeLock.setReferenceCounted(true);
                    wakeLock.acquire(500);
                    this.mContext.sendBroadcast(intent);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        ProxyController.getInstance().getDeviceRegisterController().handleAutoRegMessage(pdu);
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
