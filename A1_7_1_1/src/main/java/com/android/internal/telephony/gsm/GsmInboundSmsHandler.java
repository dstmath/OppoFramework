package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.SmsMessage;
import com.android.internal.telephony.InboundSmsHandler;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SmsConstants.MessageClass;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.VisualVoicemailSmsFilter;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.common.MPlugin;
import com.mediatek.common.sms.IDupSmsFilterExt;

public class GsmInboundSmsHandler extends InboundSmsHandler {
    private final UsimDataDownloadHandler mDataDownloadHandler;
    private IDupSmsFilterExt mDupSmsFilterExt = null;

    private GsmInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone) {
        super("GsmInboundSmsHandler", context, storageMonitor, phone, GsmCellBroadcastHandler.makeGsmCellBroadcastHandler(context, phone));
        phone.mCi.setOnNewGsmSms(getHandler(), 1, null);
        this.mDataDownloadHandler = new UsimDataDownloadHandler(phone.mCi);
        try {
            if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                this.mDupSmsFilterExt = (IDupSmsFilterExt) MPlugin.createInstance(IDupSmsFilterExt.class.getName(), context);
                if (this.mDupSmsFilterExt != null) {
                    this.mDupSmsFilterExt.setPhoneId(this.mPhone.getPhoneId());
                    log("initial IDupSmsFilterExt done, actual class name is " + this.mDupSmsFilterExt.getClass().getName());
                    return;
                }
                log("FAIL! intial IDupSmsFilterExt");
            }
        } catch (Exception e) {
            log("GsmInboundSmsHandler--error");
            e.printStackTrace();
        }
    }

    protected void onQuitting() {
        this.mPhone.mCi.unSetOnNewGsmSms(getHandler());
        this.mCellBroadcastHandler.dispose();
        log("unregistered for 3GPP SMS");
        super.onQuitting();
    }

    public static GsmInboundSmsHandler makeInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone) {
        GsmInboundSmsHandler handler = new GsmInboundSmsHandler(context, storageMonitor, phone);
        handler.start();
        return handler;
    }

    protected boolean is3gpp2() {
        return false;
    }

    protected int dispatchMessageRadioSpecific(SmsMessageBase smsb) {
        boolean z = false;
        SmsMessage sms = (SmsMessage) smsb;
        try {
            if (!(SystemProperties.get("ro.mtk_bsp_package").equals("1") || this.mDupSmsFilterExt == null || !this.mDupSmsFilterExt.containDupSms(sms.getPdu()))) {
                log("discard dup sms");
                return 1;
            }
        } catch (Exception e) {
            log("dispatchMessageRadioSpecific--error");
            e.printStackTrace();
        }
        if (sms.isTypeZero()) {
            int destPort = -1;
            SmsHeader smsHeader = sms.getUserDataHeader();
            if (!(smsHeader == null || smsHeader.portAddrs == null)) {
                destPort = smsHeader.portAddrs.destPort;
            }
            VisualVoicemailSmsFilter.filter(this.mContext, new byte[][]{sms.getPdu()}, SmsMessage.FORMAT_3GPP, destPort, this.mPhone.getSubId());
            log("Received short message type 0, Don't display or store it. Send Ack");
            return 1;
        } else if (sms.isUsimDataDownload()) {
            return this.mDataDownloadHandler.handleUsimDataDownload(this.mPhone.getUsimServiceTable(), sms);
        } else {
            boolean handled = false;
            StringBuilder append;
            if (sms.isMWISetMessage()) {
                updateMessageWaitingIndicator(sms.getNumOfVoicemails());
                handled = sms.isMwiDontStore();
                append = new StringBuilder().append("Received voice mail indicator set SMS shouldStore=");
                if (!handled) {
                    z = true;
                }
                log(append.append(z).toString());
            } else if (sms.isMWIClearMessage()) {
                updateMessageWaitingIndicator(0);
                handled = sms.isMwiDontStore();
                append = new StringBuilder().append("Received voice mail indicator clear SMS shouldStore=");
                if (!handled) {
                    z = true;
                }
                log(append.append(z).toString());
            }
            if (handled) {
                return 1;
            }
            if (this.mStorageMonitor.isStorageAvailable() || sms.getMessageClass() == MessageClass.CLASS_0) {
                return dispatchNormalMessage(smsb);
            }
            return 3;
        }
    }

    private void updateMessageWaitingIndicator(int voicemailCount) {
        if (voicemailCount < 0) {
            voicemailCount = -1;
        } else if (voicemailCount > 255) {
            voicemailCount = 255;
        }
        this.mPhone.setVoiceMessageCount(voicemailCount);
        IccRecords records = UiccController.getInstance().getIccRecords(this.mPhone.getPhoneId(), 1);
        if (records != null) {
            log("updateMessageWaitingIndicator: updating SIM Records");
            records.setVoiceMessageWaiting(1, voicemailCount);
            return;
        }
        log("updateMessageWaitingIndicator: SIM Records not found");
    }

    protected void acknowledgeLastIncomingSms(boolean success, int result, Message response) {
        this.mPhone.mCi.acknowledgeLastIncomingGsmSms(success, resultToCause(result), response);
    }

    protected void onUpdatePhoneObject(Phone phone) {
        super.onUpdatePhoneObject(phone);
        log("onUpdatePhoneObject: dispose of old CellBroadcastHandler and make a new one");
        this.mCellBroadcastHandler.dispose();
        this.mCellBroadcastHandler = GsmCellBroadcastHandler.makeGsmCellBroadcastHandler(this.mContext, phone);
    }

    private static int resultToCause(int rc) {
        switch (rc) {
            case -1:
            case 1:
                return 0;
            case 3:
                return 211;
            default:
                return 255;
        }
    }
}
