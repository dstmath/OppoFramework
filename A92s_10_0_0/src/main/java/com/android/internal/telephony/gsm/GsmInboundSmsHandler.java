package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.Message;
import com.android.internal.telephony.InboundSmsHandler;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SmsConstants;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.VisualVoicemailSmsFilter;

public class GsmInboundSmsHandler extends InboundSmsHandler {
    protected final UsimDataDownloadHandler mDataDownloadHandler;

    protected GsmInboundSmsHandler(Context context, SmsStorageMonitor storageMonitor, Phone phone) {
        super("GsmInboundSmsHandler", context, storageMonitor, phone, TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName()).makeGsmCellBroadcastHandler(context, phone));
        phone.mCi.setOnNewGsmSms(getHandler(), 1, null);
        this.mDataDownloadHandler = new UsimDataDownloadHandler(phone.mCi, phone.getPhoneId());
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.InboundSmsHandler
    public void onQuitting() {
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

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.InboundSmsHandler
    public boolean is3gpp2() {
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.InboundSmsHandler
    public int dispatchMessageRadioSpecific(SmsMessageBase smsb) {
        SmsMessage sms = (SmsMessage) smsb;
        if (sms.isTypeZero()) {
            int destPort = -1;
            SmsHeader smsHeader = sms.getUserDataHeader();
            if (!(smsHeader == null || smsHeader.portAddrs == null)) {
                destPort = smsHeader.portAddrs.destPort;
            }
            VisualVoicemailSmsFilter.filter(this.mContext, new byte[][]{sms.getPdu()}, "3gpp", destPort, this.mPhone.getSubId());
            log("Received short message type 0, Don't display or store it. Send Ack");
            addSmsTypeZeroToMetrics();
            return 1;
        } else if (sms.isUsimDataDownload()) {
            return this.mDataDownloadHandler.handleUsimDataDownload(this.mPhone.getUsimServiceTable(), sms);
        } else {
            boolean handled = false;
            if (sms.isMWISetMessage()) {
                updateMessageWaitingIndicator(sms.getNumOfVoicemails());
                handled = sms.isMwiDontStore();
                StringBuilder sb = new StringBuilder();
                sb.append("Received voice mail indicator set SMS shouldStore=");
                sb.append(!handled);
                log(sb.toString());
            } else if (sms.isMWIClearMessage()) {
                updateMessageWaitingIndicator(0);
                handled = sms.isMwiDontStore();
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Received voice mail indicator clear SMS shouldStore=");
                sb2.append(!handled);
                log(sb2.toString());
            }
            if (handled) {
                addVoicemailSmsToMetrics();
                return 1;
            } else if (this.mStorageMonitor.isStorageAvailable() || sms.getMessageClass() == SmsConstants.MessageClass.CLASS_0) {
                return dispatchNormalMessage(smsb);
            } else {
                return 3;
            }
        }
    }

    private void updateMessageWaitingIndicator(int voicemailCount) {
        if (voicemailCount < 0) {
            voicemailCount = -1;
        } else if (voicemailCount > 255) {
            voicemailCount = 255;
        }
        this.mPhone.setVoiceMessageCount(voicemailCount);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.InboundSmsHandler
    public void acknowledgeLastIncomingSms(boolean success, int result, Message response) {
        this.mPhone.mCi.acknowledgeLastIncomingGsmSms(success, resultToCause(result), response);
    }

    private static int resultToCause(int rc) {
        if (rc == -1 || rc == 1) {
            return 0;
        }
        if (rc != 3) {
            return 255;
        }
        return 211;
    }

    private void addSmsTypeZeroToMetrics() {
        this.mMetrics.writeIncomingSmsTypeZero(this.mPhone.getPhoneId(), "3gpp");
    }

    private void addVoicemailSmsToMetrics() {
        this.mMetrics.writeIncomingVoiceMailSms(this.mPhone.getPhoneId(), "3gpp");
    }
}
