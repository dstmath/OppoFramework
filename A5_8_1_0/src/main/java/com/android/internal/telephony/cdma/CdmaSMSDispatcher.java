package com.android.internal.telephony.cdma;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Message;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.ImsSMSDispatcher;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SMSDispatcher.SmsTracker;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.SmsUsageMonitor;
import com.android.internal.telephony.cdma.SmsMessage.SubmitPdu;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.util.NotificationChannelController;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CdmaSMSDispatcher extends SMSDispatcher {
    private static final String TAG = "CdmaSMSDispatcher";
    private static final boolean VDBG = false;

    public CdmaSMSDispatcher(Phone phone, SmsUsageMonitor usageMonitor, ImsSMSDispatcher imsSMSDispatcher) {
        super(phone, usageMonitor, imsSMSDispatcher);
        Rlog.d(TAG, "CdmaSMSDispatcher created");
    }

    public String getFormat() {
        return "3gpp2";
    }

    public void sendStatusReportMessage(SmsMessage sms) {
        sendMessage(obtainMessage(10, sms));
    }

    protected void handleStatusReport(Object o) {
        if (o instanceof SmsMessage) {
            handleCdmaStatusReport((SmsMessage) o);
        } else {
            Rlog.e(TAG, "handleStatusReport() called for object type " + o.getClass().getName());
        }
    }

    private void handleCdmaStatusReport(SmsMessage sms) {
        int count = this.deliveryPendingList.size();
        for (int i = 0; i < count; i++) {
            SmsTracker tracker = (SmsTracker) this.deliveryPendingList.get(i);
            if (tracker.mMessageRef == sms.mMessageRef) {
                this.deliveryPendingList.remove(i);
                tracker.updateSentMessageStatus(this.mContext, 0);
                PendingIntent intent = tracker.mDeliveryIntent;
                Intent fillIn = new Intent();
                fillIn.putExtra("pdu", sms.getPdu());
                fillIn.putExtra("format", getFormat());
                try {
                    intent.send(this.mContext, -1, fillIn);
                    return;
                } catch (CanceledException e) {
                    return;
                }
            }
        }
    }

    protected void sendData(String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, String callingPackage) {
        SubmitPdu pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, destPort, data, deliveryIntent != null);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, destPort, data, pdu), sentIntent, deliveryIntent, getFormat(), null, false, null, false, true, callingPackage);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender dataSmsSender = new DataSmsSender(tracker);
                dataSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(dataSmsSender));
                return;
            }
            Rlog.v(TAG, "No carrier package.");
            sendSubmitPdu(tracker);
            return;
        }
        Rlog.e(TAG, "CdmaSMSDispatcher.sendData(): getSubmitPdu() returned null");
        if (sentIntent != null) {
            try {
                sentIntent.send(1);
            } catch (CanceledException e) {
                Rlog.e(TAG, "Intent has been canceled!");
            }
        }
    }

    public void sendText(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean isExpectMore, int validityPeriod) {
        SubmitPduBase pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, text, deliveryIntent != null, null, priority);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, text, pdu), sentIntent, deliveryIntent, getFormat(), messageUri, isExpectMore, text, true, persistMessage, validityPeriod, callingPkg);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender textSmsSender = new TextSmsSender(tracker);
                textSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(textSmsSender));
                return;
            }
            Rlog.v(TAG, "No carrier package.");
            sendSubmitPdu(tracker);
            return;
        }
        Rlog.e(TAG, "CdmaSMSDispatcher.sendText(): getSubmitPdu() returned null");
        if (sentIntent != null) {
            try {
                sentIntent.send(1);
            } catch (CanceledException e) {
                Rlog.e(TAG, "Intent has been canceled!");
            }
        }
    }

    public void sendTextOem(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean isExpectMore, int validityPeriod, int encodingType) {
        Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "oem1-cdma,pri=" + priority + " isE=" + isExpectMore + " vP=" + validityPeriod + " eT=" + encodingType);
        SubmitPduBase pdu = SmsMessage.getSubmitPduOem(scAddr, destAddr, text, deliveryIntent != null, null, priority, encodingType);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, text, pdu), sentIntent, deliveryIntent, getFormat(), messageUri, isExpectMore, text, true, persistMessage, validityPeriod, callingPkg);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender textSmsSender = new TextSmsSender(tracker);
                textSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(textSmsSender));
                return;
            }
            Rlog.v(TAG, "No carrier package.");
            sendSubmitPdu(tracker);
            return;
        }
        Rlog.e(TAG, "CdmaSMSDispatcher.sendText(): getSubmitPdu() returned null");
        if (sentIntent != null) {
            try {
                sentIntent.send(1);
            } catch (CanceledException e) {
                Rlog.e(TAG, "Intent has been canceled!");
            }
        }
    }

    protected void injectSmsPdu(byte[] pdu, String format, PendingIntent receivedIntent) {
        throw new IllegalStateException("This method must be called only on ImsSMSDispatcher");
    }

    protected TextEncodingDetails calculateLength(CharSequence messageBody, boolean use7bitOnly) {
        return SmsMessage.calculateLength(messageBody, use7bitOnly, false);
    }

    protected SmsTracker getNewSubmitPduTracker(String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int encoding, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText, int priority, boolean isExpectMore, int validityPeriod, String callingPackage) {
        UserData uData = new UserData();
        uData.payloadStr = message;
        uData.userDataHeader = smsHeader;
        if (encoding == 1) {
            uData.msgEncoding = isAscii7bitSupportedForLongMessage() ? 2 : 9;
            Rlog.d(TAG, "Message ecoding for proper 7 bit: " + uData.msgEncoding);
        } else {
            uData.msgEncoding = 4;
        }
        uData.msgEncodingSet = true;
        return getSmsTracker(getSmsTrackerMap(destinationAddress, scAddress, message, SmsMessage.getSubmitPdu(destinationAddress, uData, deliveryIntent != null ? lastPart : false, priority)), sentIntent, deliveryIntent, getFormat(), unsentPartCount, anyPartFailed, messageUri, smsHeader, lastPart ? isExpectMore : true, fullMessageText, true, true, validityPeriod, callingPackage);
    }

    private boolean isAscii7bitSupportedForLongMessage() {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        PersistableBundle pb = null;
        long ident = Binder.clearCallingIdentity();
        try {
            pb = configManager.getConfigForSubId(this.mPhone.getSubId());
            if (pb != null) {
                return pb.getBoolean("ascii_7_bit_support_for_long_message");
            }
            return false;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    protected void sendSubmitPdu(SmsTracker tracker) {
        if (this.mPhone.isInEcm()) {
            tracker.onFailed(this.mContext, 4, 0);
        } else {
            sendRawPdu(tracker);
        }
    }

    public void sendSms(SmsTracker tracker) {
        Rlog.d(TAG, "sendSms:  isIms()=" + isIms() + " mRetryCount=" + tracker.mRetryCount + " mImsRetry=" + tracker.mImsRetry + " mMessageRef=" + tracker.mMessageRef + " SS=" + this.mPhone.getServiceState().getState());
        sendSmsByPstn(tracker);
    }

    protected void sendSmsByPstn(SmsTracker tracker) {
        try {
            if (OemConstant.isPoliceVersion(this.mPhone)) {
                boolean isPolicyMessageSendEnable = OemConstant.isSmsSendEnable(this.mPhone);
                Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "isPolicyMessageSendEnable=" + isPolicyMessageSendEnable);
                if (!(this.mContext == null || (isPolicyMessageSendEnable ^ 1) == 0)) {
                    Rlog.d(TAG, "cdma--sendSmsByPstn, stop tracker.");
                    tracker.onFailed(this.mContext, 1, 0);
                    return;
                }
            }
            boolean isSmsReg = false;
            if (!(tracker == null || tracker.mAppInfo == null || tracker.mAppInfo.applicationInfo == null)) {
                isSmsReg = "com.oppo.ctautoregist".equals(tracker.mAppInfo.applicationInfo.packageName);
            }
            if (isSmsReg) {
                Rlog.d(TAG, "sms reg stop");
                tracker.onFailed(this.mContext, 1, 0);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (!isIms() && isNeedStopSms(tracker)) {
                Rlog.d(TAG, "int cdma: 2-cdma--crossmap, stop tracker.");
                tracker.onFailed(this.mContext, 1, 0);
                return;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0) {
            Message reply = obtainMessage(2, tracker);
            byte[] pdu = (byte[]) tracker.getData().get("pdu");
            int currentDataNetwork = this.mPhone.getServiceState().getDataNetworkType();
            boolean imsSmsDisabled = ((currentDataNetwork == 14 || (ServiceState.isLte(currentDataNetwork) && (this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed() ^ 1) != 0)) && this.mPhone.getServiceState().getVoiceNetworkType() == 7) ? ((GsmCdmaPhone) this.mPhone).mCT.mState != State.IDLE : false;
            if ((tracker.mImsRetry != 0 || (isIms() ^ 1) == 0) && !imsSmsDisabled) {
                this.mCi.sendImsCdmaSms(pdu, tracker.mImsRetry, tracker.mMessageRef, reply);
                tracker.mImsRetry++;
            } else {
                this.mCi.sendCdmaSms(pdu, reply);
            }
            return;
        }
        tracker.onFailed(this.mContext, SMSDispatcher.getNotInServiceError(ss), 0);
    }
}
