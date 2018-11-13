package com.android.internal.telephony.cdma;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.ImsSMSDispatcher;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SMSDispatcher.SmsTracker;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsHeader.ConcatRef;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.SmsUsageMonitor;
import com.android.internal.telephony.cdma.SmsMessage.SubmitPdu;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.uicc.IccUtils;
import com.mediatek.internal.telephony.cdma.CdmaOmhSmsUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class CdmaSMSDispatcher extends SMSDispatcher {
    private static final boolean ENG = false;
    private static final int EVENT_CDMA_CARD_INITIAL_ESN_OR_MEID = 200;
    private static final String TAG = "CdmaMtSms";
    private static final boolean VDBG = false;
    private String mPendingEsnOrMeid;
    private final BroadcastReceiver mReceiver;

    /* renamed from: com.android.internal.telephony.cdma.CdmaSMSDispatcher$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ CdmaSMSDispatcher this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cdma.CdmaSMSDispatcher.1.<init>(com.android.internal.telephony.cdma.CdmaSMSDispatcher):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1(com.android.internal.telephony.cdma.CdmaSMSDispatcher r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cdma.CdmaSMSDispatcher.1.<init>(com.android.internal.telephony.cdma.CdmaSMSDispatcher):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.CdmaSMSDispatcher.1.<init>(com.android.internal.telephony.cdma.CdmaSMSDispatcher):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cdma.CdmaSMSDispatcher.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cdma.CdmaSMSDispatcher.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.CdmaSMSDispatcher.1.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cdma.CdmaSMSDispatcher.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cdma.CdmaSMSDispatcher.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.CdmaSMSDispatcher.<clinit>():void");
    }

    public CdmaSMSDispatcher(Phone phone, SmsUsageMonitor usageMonitor, ImsSMSDispatcher imsSMSDispatcher) {
        super(phone, usageMonitor, imsSMSDispatcher);
        this.mPendingEsnOrMeid = null;
        this.mReceiver = new AnonymousClass1(this);
        Rlog.d(TAG, "CdmaSMSDispatcher created");
        this.mCi.setCDMACardInitalEsnMeid(this, 200, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    public String getFormat() {
        return SmsMessage.FORMAT_3GPP2;
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

    public void sendData(String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (CdmaOmhSmsUtils.isOmhCard(this.mPhone.getSubId())) {
            CdmaOmhSmsUtils.getNextMessageId(this.mPhone.getSubId());
        }
        SubmitPdu pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, destPort, data, deliveryIntent != null);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, destPort, data, pdu), sentIntent, deliveryIntent, getFormat(), null, false, null, false, true);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender dataSmsSender = new DataSmsSender(this, tracker);
                dataSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(this, dataSmsSender));
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

    public void sendText(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage) {
        if (CdmaOmhSmsUtils.isOmhCard(this.mPhone.getSubId())) {
            CdmaOmhSmsUtils.getNextMessageId(this.mPhone.getSubId());
        }
        SubmitPduBase pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, text, deliveryIntent != null, null);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, text, pdu), sentIntent, deliveryIntent, getFormat(), messageUri, false, text, true, persistMessage);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender textSmsSender = new TextSmsSender(this, tracker);
                textSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(this, textSmsSender));
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

    protected SmsTracker getNewSubmitPduTracker(String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int encoding, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText) {
        UserData uData = new UserData();
        uData.payloadStr = message;
        uData.userDataHeader = smsHeader;
        if (encoding == 1) {
            uData.msgEncoding = get7bitEncodingType();
        } else {
            uData.msgEncoding = 4;
        }
        uData.msgEncodingSet = true;
        if (CdmaOmhSmsUtils.isOmhCard(this.mPhone.getSubId())) {
            CdmaOmhSmsUtils.getNextMessageId(this.mPhone.getSubId());
        }
        if (deliveryIntent == null) {
            lastPart = false;
        }
        return getSmsTracker(getSmsTrackerMap(destinationAddress, scAddress, message, SmsMessage.getSubmitPdu(destinationAddress, uData, lastPart)), sentIntent, deliveryIntent, getFormat(), unsentPartCount, anyPartFailed, messageUri, smsHeader, false, fullMessageText, true, true);
    }

    protected void sendSubmitPdu(SmsTracker tracker) {
        if (Boolean.parseBoolean(TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "ril.cdma.inecmmode", "false"))) {
            tracker.onFailed(this.mContext, 4, 0);
        } else {
            sendRawPdu(tracker);
        }
    }

    public void sendSms(SmsTracker tracker) {
        if (addToTrackerQueue(tracker)) {
            Rlog.d(TAG, "sendSms:  isIms()=" + isIms() + " mRetryCount=" + tracker.mRetryCount + " mImsRetry=" + tracker.mImsRetry + " mMessageRef=" + tracker.mMessageRef + " SS=" + this.mPhone.getServiceState().getState());
            sendSmsByPstn(tracker);
        }
    }

    protected void sendSmsByPstn(SmsTracker tracker) {
        try {
            boolean isPolicyMessageSendEnable = OemConstant.isSmsSendEnable(this.mPhone);
            Rlog.d("sms", "isPolicyMessageSendEnable=" + isPolicyMessageSendEnable);
            if (!(this.mContext == null || isPolicyMessageSendEnable)) {
                Rlog.d(TAG, "cdma--sendSmsByPstn, stop tracker.");
                tracker.onFailed(this.mContext, 1, 0);
                sendMessageDelayed(obtainMessage(Phone.OEM_PRODUCT_17373, tracker), 10);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0) {
            Message reply = obtainMessage(2, tracker);
            byte[] pdu = (byte[]) tracker.getData().get("pdu");
            int currentDataNetwork = this.mPhone.getServiceState().getDataNetworkType();
            boolean imsSmsDisabled = ((currentDataNetwork == 14 || (ServiceState.isLte(currentDataNetwork) && !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed())) && this.mPhone.getServiceState().getVoiceNetworkType() == 7) ? ((GsmCdmaPhone) this.mPhone).mCT.mState != State.IDLE : false;
            if ((tracker.mImsRetry != 0 || isIms()) && !imsSmsDisabled) {
                this.mCi.sendImsCdmaSms(pdu, tracker.mImsRetry, tracker.mMessageRef, reply);
                tracker.mImsRetry++;
            } else {
                this.mCi.sendCdmaSms(pdu, reply);
            }
            return;
        }
        if (isSimAbsent()) {
            tracker.onFailed(this.mContext, 1, 0);
        } else {
            tracker.onFailed(this.mContext, SMSDispatcher.getNotInServiceError(ss), 0);
        }
        sendMessageDelayed(obtainMessage(Phone.OEM_PRODUCT_17373, tracker), 10);
    }

    private boolean addToTrackerQueue(SmsTracker tracker) {
        boolean isReadySend = false;
        synchronized (this.mSTrackersQueue) {
            if (this.mSTrackersQueue.isEmpty() || this.mSTrackersQueue.get(0) != tracker) {
                Rlog.d(TAG, "Add tracker into the list: " + tracker);
                this.mSTrackersQueue.add(tracker);
            }
            if (this.mSTrackersQueue.get(0) == tracker) {
                isReadySend = true;
            }
        }
        if (!isReadySend) {
            Rlog.d(TAG, "There is another tracker in-queue and is sending");
        }
        return isReadySend;
    }

    protected void sendData(String destAddr, String scAddr, int destPort, int originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        Rlog.d(TAG, "CdmaSMSDispatcher, implemented for interfaces needed. sendData");
        if (CdmaOmhSmsUtils.isOmhCard(this.mPhone.getSubId())) {
            CdmaOmhSmsUtils.getNextMessageId(this.mPhone.getSubId());
        }
        SubmitPdu pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, destPort, originalPort, data, deliveryIntent != null ? true : null);
        if (pdu == null) {
            Rlog.d(TAG, "sendData error: invalid paramters, pdu == null.");
            return;
        }
        SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, destPort, data, pdu), sentIntent, deliveryIntent, getFormat(), null, false, null, false, true);
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage != null) {
            Rlog.d(TAG, "Found carrier package. w/op");
            SmsSender dataSmsSender = new DataSmsSender(this, tracker);
            dataSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(this, dataSmsSender));
        } else {
            Rlog.v(TAG, "No carrier package. w/op");
            sendRawPdu(tracker);
        }
    }

    public int copyTextMessageToIccCard(String scAddress, String address, List<String> text, int status, long timestamp) {
        Rlog.d(TAG, "CDMASMSDispatcher: copy text message to icc card");
        this.mSuccess = true;
        int msgCount = text.size();
        Rlog.d(TAG, "[copyText storage available");
        if (status == 1 || status == 3) {
            Rlog.d(TAG, "[copyText to encode deliver pdu");
        } else if (status == 5 || status == 7) {
            Rlog.d(TAG, "[copyText to encode submit pdu");
        } else {
            Rlog.d(TAG, "[copyText invalid status, default is deliver pdu");
            return 1;
        }
        Rlog.d(TAG, "[copyText msgCount " + msgCount);
        if (msgCount > 1) {
            Rlog.d(TAG, "[copyText multi-part message");
        } else if (msgCount == 1) {
            Rlog.d(TAG, "[copyText single-part message");
        } else {
            Rlog.d(TAG, "[copyText invalid message count");
            return 1;
        }
        int i = 0;
        while (i < msgCount) {
            if (this.mSuccess) {
                SubmitPdu pdu = SmsMessage.createEfPdu(address, (String) text.get(i), timestamp);
                if (pdu == null) {
                    return 1;
                }
                Rlog.d(TAG, "[copyText write submit pdu into UIM");
                this.mCi.writeSmsToRuim(status, IccUtils.bytesToHexString(pdu.encodedMessage), obtainMessage(106));
                synchronized (this.mLock) {
                    try {
                        Rlog.d(TAG, "[copyText wait until the message be wrote in UIM");
                        this.mLock.wait();
                    } catch (InterruptedException e) {
                        Rlog.d(TAG, "[copyText interrupted while trying to copy text message into UIM");
                        return 1;
                    }
                }
                Rlog.d(TAG, "[copyText thread is waked up");
                i++;
            } else {
                Rlog.d(TAG, "[copyText Exception happened when copy message");
                return 1;
            }
        }
        if (this.mSuccess) {
            Rlog.d(TAG, "[copyText all messages have been copied into UIM");
            return 0;
        }
        Rlog.d(TAG, "[copyText copy failed");
        return 1;
    }

    protected void sendTextWithEncodingType(String destAddr, String scAddr, String text, int encodingType, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage) {
        Rlog.d(TAG, "CdmaSMSDispatcher, implemented for interfaces needed. sendTextWithEncodingType");
        int encoding = encodingType;
        Rlog.d(TAG, "want to use encoding = " + encodingType);
        if (encodingType < 0 || encodingType > 10) {
            Rlog.w(TAG, "unavalid encoding = " + encodingType);
            Rlog.w(TAG, "to use the unkown default.");
            encoding = 0;
        }
        if (encoding == 0) {
            Rlog.d(TAG, "unkown encoding, to find one best.");
            TextEncodingDetails details = SmsMessage.calculateLength((CharSequence) text, false, encoding);
            if (encoding != details.codeUnitSize && (encoding == 0 || encoding == 1)) {
                encoding = details.codeUnitSize;
            }
        }
        UserData uData = new UserData();
        uData.payloadStr = text;
        if (encoding == 1) {
            uData.msgEncoding = 2;
        } else if (encoding == 2) {
            uData.msgEncoding = 0;
        } else {
            uData.msgEncoding = 4;
        }
        uData.msgEncodingSet = true;
        if (CdmaOmhSmsUtils.isOmhCard(this.mPhone.getSubId())) {
            CdmaOmhSmsUtils.getNextMessageId(this.mPhone.getSubId());
        }
        SubmitPduBase submitPdu = SmsMessage.getSubmitPdu(destAddr, uData, deliveryIntent != null);
        if (submitPdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, text, submitPdu), sentIntent, deliveryIntent, getFormat(), messageUri, false, text, true, true);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "sendTextWithEncodingType: Found carrier package.");
                SmsSender textSmsSender = new TextSmsSender(this, tracker);
                textSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(this, textSmsSender));
                return;
            }
            Rlog.v(TAG, "sendTextWithEncodingType: No carrier package.");
            sendSubmitPdu(tracker);
            return;
        }
        Rlog.d(TAG, "sendTextWithEncodingType: submitPdu is null");
        if (sentIntent != null) {
            try {
                sentIntent.send(3);
            } catch (CanceledException e) {
                Rlog.e(TAG, "failed to send back RESULT_ERROR_NULL_PDU");
            }
        }
    }

    protected void sendMultipartTextWithEncodingType(String destAddr, String scAddr, ArrayList<String> parts, int encodingType, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage) {
        int i;
        String fullMessageText = getMultipartMessageText(parts);
        Rlog.d(TAG, "CdmaSMSDispatcher, implemented by for interfaces needed. sendMultipartTextWithEncodingType");
        int refNumber = SMSDispatcher.getNextConcatenatedRef() & 255;
        int msgCount = parts.size();
        int encoding = encodingType;
        Rlog.d(TAG, "want to use encoding = " + encodingType);
        if (encodingType < 0 || encodingType > 10) {
            Rlog.w(TAG, "unavalid encoding = " + encodingType);
            Rlog.w(TAG, "to use the unkown default.");
            encoding = 0;
        }
        TextEncodingDetails[] encodingForParts = new TextEncodingDetails[msgCount];
        TextEncodingDetails details;
        if (encoding == 0) {
            Rlog.d(TAG, "unkown encoding, to find one best.");
            for (i = 0; i < msgCount; i++) {
                details = calculateLength((CharSequence) parts.get(i), false);
                if (encoding != details.codeUnitSize && (encoding == 0 || encoding == 1)) {
                    encoding = details.codeUnitSize;
                }
                encodingForParts[i] = details;
            }
        } else {
            Rlog.d(TAG, "APP want use specified encoding type.");
            for (i = 0; i < msgCount; i++) {
                details = SmsMessage.calculateLength((CharSequence) parts.get(i), false, encoding);
                details.codeUnitSize = encoding;
                encodingForParts[i] = details;
            }
        }
        Object trackers = new SmsTracker[msgCount];
        AtomicInteger unsentPartCount = new AtomicInteger(msgCount);
        AtomicBoolean anyPartFailed = new AtomicBoolean(false);
        Rlog.d(TAG, "now to send one by one, msgCount = " + msgCount);
        i = 0;
        while (i < msgCount) {
            ConcatRef concatRef = new ConcatRef();
            concatRef.refNumber = refNumber;
            concatRef.seqNumber = i + 1;
            concatRef.msgCount = msgCount;
            concatRef.isEightBits = true;
            SmsHeader smsHeader = new SmsHeader();
            smsHeader.concatRef = concatRef;
            PendingIntent pendingIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                pendingIntent = (PendingIntent) sentIntents.get(i);
            }
            PendingIntent pendingIntent2 = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
            }
            Rlog.d(TAG, "to send the " + i + " part");
            trackers[i] = getNewSubmitPduTracker(destAddr, scAddr, (String) parts.get(i), smsHeader, encodingForParts[i].codeUnitSize, pendingIntent, pendingIntent2, i == msgCount + -1, unsentPartCount, anyPartFailed, messageUri, fullMessageText);
            i++;
        }
        if (parts == null || trackers == null || trackers.length == 0 || trackers[0] == null) {
            Rlog.e(TAG, "sendMultipartTextWithEncodingType: Cannot send multipart text. parts=" + parts + " trackers=" + trackers);
            return;
        }
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage != null) {
            Rlog.d(TAG, "sendMultipartTextWithEncodingType: Found carrier package.");
            MultipartSmsSender multipartSmsSender = new MultipartSmsSender(this, parts, trackers);
            multipartSmsSender.sendSmsByCarrierApp(carrierPackage, new MultipartSmsSenderCallback(this, multipartSmsSender));
        } else {
            Rlog.v(TAG, "sendMultipartTextWithEncodingType: No carrier package.");
            for (SmsTracker tracker : trackers) {
                if (tracker != null) {
                    sendSubmitPdu(tracker);
                } else {
                    Rlog.e(TAG, "sendMultipartTextWithEncodingType: Null tracker.");
                }
            }
        }
    }

    public void sendTextWithExtraParams(String destAddr, String scAddr, String text, Bundle extraParams, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage) {
        int validityPeriod;
        int priority;
        int encoding;
        Rlog.d(TAG, "CdmaSMSDispatcher, implemented by for interfaces needed. sendTextWithExtraParams");
        if (extraParams == null) {
            Rlog.d(TAG, "extraParams == null, will encoding with no extra feature.");
            validityPeriod = -1;
            priority = -1;
            encoding = 0;
        } else {
            validityPeriod = extraParams.getInt(SmsManager.EXTRA_PARAMS_VALIDITY_PERIOD, -1);
            if (validityPeriod > 244 && validityPeriod <= 255) {
                validityPeriod = 244;
            }
            priority = extraParams.getInt("priority", -1);
            encoding = extraParams.getInt(SmsManager.EXTRA_PARAMS_ENCODING_TYPE, 0);
        }
        Rlog.d(TAG, "validityPeriod is " + validityPeriod);
        Rlog.d(TAG, "priority is " + priority);
        Rlog.d(TAG, "want to use encoding = " + encoding);
        if (encoding < 0 || encoding > 10) {
            Rlog.w(TAG, "unavalid encoding = " + encoding);
            Rlog.w(TAG, "to use the unkown default.");
            encoding = 0;
        }
        if (encoding == 0) {
            Rlog.d(TAG, "unkown encoding, to find one best.");
            TextEncodingDetails details = calculateLength(text, false);
            if (encoding != details.codeUnitSize && (encoding == 0 || encoding == 1)) {
                encoding = details.codeUnitSize;
            }
        }
        if (CdmaOmhSmsUtils.isOmhCard(this.mPhone.getSubId())) {
            CdmaOmhSmsUtils.getNextMessageId(this.mPhone.getSubId());
        }
        SubmitPduBase submitPdu = SmsMessage.getSubmitPdu(scAddr, destAddr, text, deliveryIntent != null, null, encoding, validityPeriod, priority);
        if (submitPdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, text, submitPdu), sentIntent, deliveryIntent, getFormat(), messageUri, false, text, true, true);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "sendTextWithExtraParams: Found carrier package.");
                SmsSender textSmsSender = new TextSmsSender(this, tracker);
                textSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(this, textSmsSender));
                return;
            }
            Rlog.v(TAG, "sendTextWithExtraParams: No carrier package.");
            sendSubmitPdu(tracker);
            return;
        }
        Rlog.d(TAG, "sendTextWithExtraParams: submitPdu is null");
        if (sentIntent != null) {
            try {
                sentIntent.send(3);
            } catch (CanceledException e) {
                Rlog.e(TAG, "failed to send back RESULT_ERROR_NULL_PDU");
            }
        }
    }

    public void sendMultipartTextWithExtraParams(String destAddr, String scAddr, ArrayList<String> parts, Bundle extraParams, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage) {
        int validityPeriod;
        int priority;
        int encoding;
        int i;
        String fullMessageText = getMultipartMessageText(parts);
        Rlog.d(TAG, "CdmaSMSDispatcher, implemented by for interfaces needed. sendMultipartTextWithExtraParams");
        if (extraParams == null) {
            Rlog.d(TAG, "extraParams == null, will encoding with no extra feature.");
            validityPeriod = -1;
            priority = -1;
            encoding = 0;
        } else {
            validityPeriod = extraParams.getInt(SmsManager.EXTRA_PARAMS_VALIDITY_PERIOD, -1);
            priority = extraParams.getInt("priority", -1);
            encoding = extraParams.getInt(SmsManager.EXTRA_PARAMS_ENCODING_TYPE, 0);
        }
        Rlog.d(TAG, "validityPeriod is " + validityPeriod);
        Rlog.d(TAG, "priority is " + priority);
        Rlog.d(TAG, "want to use encoding = " + encoding);
        int refNumber = SMSDispatcher.getNextConcatenatedRef() & 255;
        int msgCount = parts.size();
        if (encoding < 0 || encoding > 10) {
            Rlog.w(TAG, "unavalid encoding = " + encoding);
            Rlog.w(TAG, "to use the unkown default.");
            encoding = 0;
        }
        TextEncodingDetails[] encodingForParts = new TextEncodingDetails[msgCount];
        TextEncodingDetails details;
        if (encoding == 0) {
            Rlog.d(TAG, "unkown encoding, to find one best.");
            for (i = 0; i < msgCount; i++) {
                details = calculateLength((CharSequence) parts.get(i), false);
                if (encoding != details.codeUnitSize && (encoding == 0 || encoding == 1)) {
                    encoding = details.codeUnitSize;
                }
                encodingForParts[i] = details;
            }
        } else {
            Rlog.d(TAG, "APP want use specified encoding type.");
            for (i = 0; i < msgCount; i++) {
                details = SmsMessage.calculateLength((CharSequence) parts.get(i), false, encoding);
                details.codeUnitSize = encoding;
                encodingForParts[i] = details;
            }
        }
        Object trackers = new SmsTracker[msgCount];
        AtomicInteger unsentPartCount = new AtomicInteger(msgCount);
        AtomicBoolean anyPartFailed = new AtomicBoolean(false);
        Rlog.d(TAG, "now to send one by one, msgCount = " + msgCount);
        i = 0;
        while (i < msgCount) {
            ConcatRef concatRef = new ConcatRef();
            concatRef.refNumber = refNumber;
            concatRef.seqNumber = i + 1;
            concatRef.msgCount = msgCount;
            concatRef.isEightBits = true;
            SmsHeader smsHeader = new SmsHeader();
            smsHeader.concatRef = concatRef;
            PendingIntent pendingIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                pendingIntent = (PendingIntent) sentIntents.get(i);
            }
            PendingIntent pendingIntent2 = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
            }
            trackers[i] = getNewSubmitPduTracker(destAddr, scAddr, (String) parts.get(i), smsHeader, encodingForParts[i].codeUnitSize, pendingIntent, pendingIntent2, i == msgCount + -1, unsentPartCount, anyPartFailed, messageUri, fullMessageText, validityPeriod, priority);
            i++;
        }
        if (parts == null || trackers == null || trackers.length == 0 || trackers[0] == null) {
            Rlog.e(TAG, "sendMultipartTextWithExtraParams: Cannot send multipart text. parts=" + parts + " trackers=" + trackers);
            return;
        }
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage != null) {
            Rlog.d(TAG, "sendMultipartTextWithExtraParams: Found carrier package.");
            MultipartSmsSender multipartSmsSender = new MultipartSmsSender(this, parts, trackers);
            multipartSmsSender.sendSmsByCarrierApp(carrierPackage, new MultipartSmsSenderCallback(this, multipartSmsSender));
        } else {
            Rlog.v(TAG, "sendMultipartTextWithExtraParams: No carrier package.");
            for (SmsTracker tracker : trackers) {
                if (tracker != null) {
                    sendSubmitPdu(tracker);
                } else {
                    Rlog.e(TAG, "sendMultipartTextWithExtraParams: Null tracker.");
                }
            }
        }
    }

    protected SmsTracker getNewSubmitPduTracker(String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int encoding, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText, int validityPeriod, int priority) {
        if (CdmaOmhSmsUtils.isOmhCard(this.mPhone.getSubId())) {
            CdmaOmhSmsUtils.getNextMessageId(this.mPhone.getSubId());
        }
        SubmitPduBase submitPdu = SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, deliveryIntent != null ? lastPart : false, smsHeader, encoding, validityPeriod, priority);
        if (submitPdu != null) {
            return getSmsTracker(getSmsTrackerMap(destinationAddress, scAddress, message, submitPdu), sentIntent, deliveryIntent, getFormat(), unsentPartCount, anyPartFailed, messageUri, smsHeader, false, fullMessageText, true, true);
        }
        Rlog.e(TAG, "CDMASMSDispatcher.getNewSubmitPduTracker(), returned null, B");
        return null;
    }

    boolean isSimAbsent() {
        IccCardConstants.State state;
        IccCard card = PhoneFactory.getPhone(this.mPhone.getPhoneId()).getIccCard();
        if (card == null) {
            state = IccCardConstants.State.UNKNOWN;
        } else {
            state = card.getState();
        }
        boolean ret = state != IccCardConstants.State.ABSENT ? state == IccCardConstants.State.NOT_READY : true;
        Rlog.d(TAG, "isSimAbsent state = " + state + " ret=" + ret);
        return ret;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 200:
                AsyncResult ar = msg.obj;
                if (!(ar == null || ar.exception != null || ar.result == null)) {
                    try {
                        String data = ar.result;
                        if (SystemProperties.get("sys.boot_completed").equals("1")) {
                            Intent intent = new Intent("android.telephony.sms.CDMA_CARD_ESN_OR_MEID");
                            String[] temp = data.split(",");
                            if (temp.length >= 1) {
                                intent.putExtra("esn_or_meid", temp[0].trim());
                            }
                            if (temp.length >= 2) {
                                intent.putExtra("esn_or_meid2", temp[1].trim());
                            }
                            if (ENG) {
                                Rlog.d(TAG, "Broadcast ESN/MEID = " + data);
                            }
                            Rlog.d(TAG, "Broadcast ESN/MEID = " + data);
                            WakeLock wakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "CDMASMSDispatcher");
                            wakeLock.setReferenceCounted(true);
                            wakeLock.acquire(500);
                            this.mContext.sendBroadcast(intent);
                            break;
                        }
                        this.mPendingEsnOrMeid = data;
                        Rlog.d(TAG, "Cache ESN/MEID " + this.mPendingEsnOrMeid);
                        return;
                    } catch (ClassCastException e) {
                        break;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        break;
                    }
                }
            default:
                super.handleMessage(msg);
                break;
        }
    }

    int get7bitEncodingType() {
        int type;
        PersistableBundle b = ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId());
        if (b != null) {
            type = b.getInt("cdma_sms_7bit_encoding_type_int");
        } else {
            type = 9;
        }
        Rlog.d(TAG, "get7bitEncodingType = " + type);
        return type;
    }
}
