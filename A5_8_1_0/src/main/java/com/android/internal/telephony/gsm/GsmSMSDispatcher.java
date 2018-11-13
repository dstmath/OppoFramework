package com.android.internal.telephony.gsm;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.ImsSMSDispatcher;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SMSDispatcher.SmsTracker;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.SmsUsageMonitor;
import com.android.internal.telephony.gsm.SmsMessage.SubmitPdu;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.NotificationChannelController;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class GsmSMSDispatcher extends SMSDispatcher {
    private static final int EVENT_NEW_SMS_STATUS_REPORT = 100;
    private static final String TAG = "GsmSMSDispatcher";
    private static final boolean VDBG = false;
    private GsmInboundSmsHandler mGsmInboundSmsHandler;
    private AtomicReference<IccRecords> mIccRecords = new AtomicReference();
    private AtomicReference<UiccCardApplication> mUiccApplication = new AtomicReference();
    protected UiccController mUiccController = null;

    public GsmSMSDispatcher(Phone phone, SmsUsageMonitor usageMonitor, ImsSMSDispatcher imsSMSDispatcher, GsmInboundSmsHandler gsmInboundSmsHandler) {
        super(phone, usageMonitor, imsSMSDispatcher);
        this.mCi.setOnSmsStatus(this, 100, null);
        this.mGsmInboundSmsHandler = gsmInboundSmsHandler;
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 15, null);
        Rlog.d(TAG, "GsmSMSDispatcher created");
    }

    public void dispose() {
        super.dispose();
        this.mCi.unSetOnSmsStatus(this);
        this.mUiccController.unregisterForIccChanged(this);
    }

    protected String getFormat() {
        return "3gpp";
    }

    public void handleMessage(Message msg) {
        try {
            switch (msg.what) {
                case 14:
                    this.mGsmInboundSmsHandler.sendMessage(1, msg.obj);
                    return;
                case 15:
                    onUpdateIccAvailability();
                    return;
                case 100:
                    handleStatusReport((AsyncResult) msg.obj);
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        e.printStackTrace();
    }

    private void handleStatusReport(AsyncResult ar) {
        byte[] pdu = ar.result;
        SmsMessage sms = SmsMessage.newFromCDS(pdu);
        if (sms != null) {
            int tpStatus = sms.getStatus();
            int messageRef = sms.mMessageRef;
            int i = 0;
            int count = this.deliveryPendingList.size();
            while (i < count) {
                SmsTracker tracker = (SmsTracker) this.deliveryPendingList.get(i);
                if (tracker.mMessageRef == messageRef) {
                    if (tpStatus >= 64 || tpStatus < 32) {
                        this.deliveryPendingList.remove(i);
                        tracker.updateSentMessageStatus(this.mContext, tpStatus);
                    }
                    PendingIntent intent = tracker.mDeliveryIntent;
                    Intent fillIn = new Intent();
                    fillIn.putExtra("pdu", pdu);
                    fillIn.putExtra("format", getFormat());
                    try {
                        intent.send(this.mContext, -1, fillIn);
                    } catch (CanceledException e) {
                    }
                } else {
                    i++;
                }
            }
        }
        this.mCi.acknowledgeLastIncomingGsmSms(true, 1, null);
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
            sendRawPdu(tracker);
            return;
        }
        Rlog.e(TAG, "GsmSMSDispatcher.sendData(): getSubmitPdu() returned null");
        if (sentIntent != null && this.mContext != null) {
            try {
                Intent fillIn = new Intent();
                fillIn.putExtra("errorCode", 1);
                sentIntent.send(this.mContext, 1, fillIn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendText(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean isExpectMore, int validityPeriod) {
        SubmitPduBase pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, text, deliveryIntent != null, validityPeriod);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, text, pdu), sentIntent, deliveryIntent, getFormat(), messageUri, false, text, true, persistMessage, validityPeriod, callingPkg);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender textSmsSender = new TextSmsSender(tracker);
                textSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(textSmsSender));
                return;
            }
            Rlog.v(TAG, "No carrier package.");
            sendRawPdu(tracker);
            return;
        }
        Rlog.e(TAG, "GsmSMSDispatcher.sendText(): getSubmitPdu() returned null");
        if (sentIntent != null && this.mContext != null) {
            try {
                Intent fillIn = new Intent();
                fillIn.putExtra("errorCode", 1);
                sentIntent.send(this.mContext, 1, fillIn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendTextOem(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean isExpectMore, int validityPeriod, int encodingType) {
        Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "oem1-gsm,pri=" + priority + " isE=" + isExpectMore + " vP=" + validityPeriod + " eT=" + encodingType);
        int encoding = encodingType;
        TextEncodingDetails details = SmsMessage.calculateLength(text, false);
        if (encodingType != details.codeUnitSize && (encodingType == 0 || encodingType == 1)) {
            Rlog.d(TAG, "[enc conflict between details[" + details.codeUnitSize + "] and encoding " + encodingType);
            details.codeUnitSize = encodingType;
        }
        SubmitPduBase pdu = SmsMessage.getSubmitPduOem(scAddr, destAddr, text, deliveryIntent != null, validityPeriod, encodingType, details.languageTable, details.languageShiftTable);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, text, pdu), sentIntent, deliveryIntent, getFormat(), messageUri, false, text, true, persistMessage, validityPeriod, callingPkg);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender textSmsSender = new TextSmsSender(tracker);
                textSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(textSmsSender));
                return;
            }
            Rlog.v(TAG, "No carrier package.");
            sendRawPdu(tracker);
            return;
        }
        Rlog.e(TAG, "GsmSMSDispatcher.sendText(): getSubmitPdu() returned null");
        if (sentIntent != null && this.mContext != null) {
            try {
                Intent fillIn = new Intent();
                fillIn.putExtra("errorCode", 1);
                sentIntent.send(this.mContext, 1, fillIn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void injectSmsPdu(byte[] pdu, String format, PendingIntent receivedIntent) {
        throw new IllegalStateException("This method must be called only on ImsSMSDispatcher");
    }

    protected TextEncodingDetails calculateLength(CharSequence messageBody, boolean use7bitOnly) {
        return SmsMessage.calculateLength(messageBody, use7bitOnly);
    }

    protected SmsTracker getNewSubmitPduTracker(String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int encoding, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText, int priority, boolean isExpectMore, int validityPeriod, String callingPackage) {
        SubmitPduBase pdu = SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, deliveryIntent != null, SmsHeader.toByteArray(smsHeader), encoding, smsHeader.languageTable, smsHeader.languageShiftTable, validityPeriod);
        if (pdu != null) {
            return getSmsTracker(getSmsTrackerMap(destinationAddress, scAddress, message, pdu), sentIntent, deliveryIntent, getFormat(), unsentPartCount, anyPartFailed, messageUri, smsHeader, lastPart ? isExpectMore : true, fullMessageText, true, false, validityPeriod, callingPackage);
        }
        Rlog.e(TAG, "GsmSMSDispatcher.sendNewSubmitPdu(): getSubmitPdu() returned null");
        return null;
    }

    protected void sendSubmitPdu(SmsTracker tracker) {
        sendRawPdu(tracker);
    }

    protected void sendSms(SmsTracker tracker) {
        byte[] pdu = (byte[]) tracker.getData().get("pdu");
        if (tracker.mRetryCount > 0) {
            Rlog.d(TAG, "sendSms:  mRetryCount=" + tracker.mRetryCount + " mMessageRef=" + tracker.mMessageRef + " SS=" + this.mPhone.getServiceState().getState());
            if ((pdu[0] & 1) == 1) {
                pdu[0] = (byte) (pdu[0] | 4);
                pdu[1] = (byte) tracker.mMessageRef;
            }
        }
        Rlog.d(TAG, "sendSms:  isIms()=" + isIms() + " mRetryCount=" + tracker.mRetryCount + " mImsRetry=" + tracker.mImsRetry + " mMessageRef=" + tracker.mMessageRef + " SS=" + this.mPhone.getServiceState().getState());
        sendSmsByPstn(tracker);
    }

    protected void sendSmsByPstn(SmsTracker tracker) {
        try {
            if (OemConstant.isPoliceVersion(this.mPhone) || OemConstant.isDeviceLockVersion()) {
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
                Rlog.d(TAG, "in gsm: 2-cdma--crossmap, stop tracker.");
                tracker.onFailed(this.mContext, 1, 0);
                return;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0) {
            HashMap<String, Object> map = tracker.getData();
            byte[] smsc = (byte[]) map.get("smsc");
            byte[] pdu = (byte[]) map.get("pdu");
            Message reply = obtainMessage(2, tracker);
            if (tracker.mImsRetry != 0 || (isIms() ^ 1) == 0) {
                if (isRetryAlwaysOverIMS()) {
                    this.mCi.sendImsGsmSms(IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), 0, tracker.mMessageRef, reply);
                } else {
                    this.mCi.sendImsGsmSms(IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), tracker.mImsRetry, tracker.mMessageRef, reply);
                }
                tracker.mImsRetry++;
            } else {
                if (tracker.mRetryCount > 0 && (pdu[0] & 1) == 1) {
                    pdu[0] = (byte) (pdu[0] | 4);
                    pdu[1] = (byte) tracker.mMessageRef;
                }
                if (tracker.mRetryCount == 0 && tracker.mExpectMore) {
                    this.mCi.sendSMSExpectMore(IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), reply);
                } else {
                    this.mCi.sendSMS(IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), reply);
                }
            }
            return;
        }
        tracker.onFailed(this.mContext, SMSDispatcher.getNotInServiceError(ss), 0);
    }

    protected UiccCardApplication getUiccCardApplication() {
        Rlog.d(TAG, "GsmSMSDispatcher: subId = " + this.mPhone.getSubId() + " slotId = " + this.mPhone.getPhoneId());
        return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 1);
    }

    private void onUpdateIccAvailability() {
        if (this.mUiccController != null) {
            UiccCardApplication newUiccApplication = getUiccCardApplication();
            UiccCardApplication app = (UiccCardApplication) this.mUiccApplication.get();
            if (app != newUiccApplication) {
                if (app != null) {
                    Rlog.d(TAG, "Removing stale icc objects.");
                    if (this.mIccRecords.get() != null) {
                        ((IccRecords) this.mIccRecords.get()).unregisterForNewSms(this);
                    }
                    this.mIccRecords.set(null);
                    this.mUiccApplication.set(null);
                }
                if (newUiccApplication != null) {
                    Rlog.d(TAG, "New Uicc application found");
                    this.mUiccApplication.set(newUiccApplication);
                    this.mIccRecords.set(newUiccApplication.getIccRecords());
                    if (this.mIccRecords.get() != null) {
                        ((IccRecords) this.mIccRecords.get()).registerForNewSms(this, 14, null);
                    }
                }
            }
        }
    }
}
