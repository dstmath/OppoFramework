package com.mediatek.internal.telephony.cdma;

import android.content.res.Resources;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.SmsAddress;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.cdma.SmsMessage;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.cdma.sms.SmsEnvelope;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.util.HexDump;
import com.mediatek.internal.telephony.MtkPhoneNumberUtils;
import com.mediatek.internal.telephony.cdma.pluscode.IPlusCodeUtils;
import com.mediatek.internal.telephony.cdma.pluscode.PlusCodeProcessor;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MtkSmsMessage extends SmsMessage {
    private static final String LOGGABLE_TAG = "CDMA:SMS";
    private static final String LOG_TAG = "MtkCdmaSmsMessage";
    private static final int RETURN_ACK = 1;
    private static IPlusCodeUtils sPlusCodeUtils = PlusCodeProcessor.getPlusCodeUtils();

    public static SmsMessage createFromEfRecord(int index, byte[] data) {
        try {
            SmsMessage msg = new MtkSmsMessage();
            msg.mIndexOnIcc = index;
            if ((data[0] & 1) == 0) {
                Rlog.w(LOG_TAG, "SMS parsing failed: Trying to parse a free record");
                return null;
            }
            msg.mStatusOnIcc = data[0] & 7;
            int size = data[1] & 255;
            byte[] pdu = new byte[size];
            System.arraycopy(data, 2, pdu, 0, size);
            msg.parsePduFromEfRecord(pdu);
            return msg;
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "SMS PDU parsing failed: ", ex);
            return null;
        }
    }

    public String getOriginatingAddress() {
        replaceIddNddWithPluscode(this.mOriginatingAddress);
        return MtkSmsMessage.super.getOriginatingAddress();
    }

    /* access modifiers changed from: protected */
    public BearerData onDecodeBroadcastSms() {
        return MtkBearerData.decode(this.mEnvelope.bearerData, this.mEnvelope.serviceCategory);
    }

    public static SmsMessage.SubmitPdu getSubmitPdu(String scAddr, String destAddr, int destPort, int originalPort, byte[] data, boolean statusReportRequested) {
        SmsHeader.PortAddrs portAddrs = new SmsHeader.PortAddrs();
        portAddrs.destPort = destPort;
        portAddrs.origPort = originalPort;
        portAddrs.areEightBits = false;
        SmsHeader smsHeader = new SmsHeader();
        smsHeader.portAddrs = portAddrs;
        UserData uData = new UserData();
        if (originalPort == 0) {
            uData.userDataHeader = null;
            Rlog.d(LOG_TAG, "getSubmitPdu(with dest&original port), clear the header.");
        } else {
            uData.userDataHeader = smsHeader;
        }
        uData.msgEncoding = 0;
        uData.msgEncodingSet = true;
        uData.payload = data;
        return privateGetSubmitPdu(destAddr, statusReportRequested, uData);
    }

    public static SmsMessage.SubmitPdu getSubmitPdu(String destAddr, UserData userData, boolean statusReportRequested) {
        return privateGetPdu(destAddr, statusReportRequested, userData, 0, -1, -1);
    }

    public static SmsMessage.SubmitPdu createEfPdu(String destinationAddress, String message, long timeStamp) {
        if (destinationAddress == null || message == null) {
            return null;
        }
        UserData uData = new UserData();
        uData.payloadStr = message;
        uData.userDataHeader = null;
        if (timeStamp > 0) {
            Rlog.d(LOG_TAG, "createEfPdu, input timeStamp = " + timeStamp + ", out scTimeMillis = " + timeStamp);
        } else {
            Rlog.d(LOG_TAG, "createEfPdu, input timeStamp = " + timeStamp + ", dont assign time zone to this invalid value");
        }
        return privateGetPdu(destinationAddress, false, uData, timeStamp, -1, -1);
    }

    private static SmsMessage.SubmitPdu privateGetPdu(String destAddrStr, boolean statusReportRequested, UserData userData, long timeStamp, int validityPeriod, int priority) {
        CdmaSmsAddress destAddr = CdmaSmsAddress.parse(MtkPhoneNumberUtils.cdmaCheckAndProcessPlusCodeForSms(destAddrStr));
        if (destAddr == null) {
            return null;
        }
        if (destAddr.numberOfDigits > 36) {
            Rlog.d(LOG_TAG, "number of digit exceeds the SMS_ADDRESS_MAX");
            return null;
        }
        BearerData bearerData = new BearerData();
        bearerData.messageType = 2;
        bearerData.messageId = getNextMessageId();
        bearerData.deliveryAckReq = statusReportRequested;
        bearerData.userAckReq = false;
        bearerData.readAckReq = false;
        bearerData.reportReq = false;
        bearerData.userData = userData;
        if (timeStamp > 0) {
            bearerData.msgCenterTimeStamp = new BearerData.TimeStamp();
            bearerData.msgCenterTimeStamp.set(timeStamp);
        }
        if (validityPeriod >= 0) {
            bearerData.validityPeriodRelativeSet = true;
            bearerData.validityPeriodRelative = validityPeriod;
        } else {
            bearerData.validityPeriodRelativeSet = false;
        }
        if (priority >= 0) {
            bearerData.priorityIndicatorSet = true;
            bearerData.priority = priority;
        } else {
            bearerData.priorityIndicatorSet = false;
        }
        byte[] encodedBearerData = MtkBearerData.encode(bearerData);
        if (Rlog.isLoggable(LOGGABLE_TAG, 2)) {
            Rlog.d(LOG_TAG, "MO (encoded) BearerData = " + bearerData);
            if (encodedBearerData != null) {
                Rlog.d(LOG_TAG, "MO raw BearerData = '" + HexDump.toHexString(encodedBearerData) + "'");
            }
        }
        if (encodedBearerData == null) {
            return null;
        }
        int teleservice = bearerData.hasUserDataHeader ? 4101 : 4098;
        SmsEnvelope envelope = new SmsEnvelope();
        envelope.messageType = 0;
        envelope.teleService = teleservice;
        envelope.destAddress = destAddr;
        envelope.bearerReply = 1;
        envelope.bearerData = encodedBearerData;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(envelope.teleService);
            dos.writeInt(0);
            dos.writeInt(0);
            dos.write(destAddr.digitMode);
            dos.write(destAddr.numberMode);
            dos.write(destAddr.ton);
            dos.write(destAddr.numberPlan);
            dos.write(destAddr.numberOfDigits);
            dos.write(destAddr.origBytes, 0, destAddr.origBytes.length);
            dos.write(0);
            dos.write(0);
            dos.write(0);
            dos.write(encodedBearerData.length);
            dos.write(encodedBearerData, 0, encodedBearerData.length);
            dos.close();
            SmsMessage.SubmitPdu pdu = new SmsMessage.SubmitPdu();
            pdu.encodedMessage = baos.toByteArray();
            pdu.encodedScAddress = null;
            return pdu;
        } catch (IOException ex) {
            Rlog.e(LOG_TAG, "creating SubmitPdu failed: " + ex);
            return null;
        }
    }

    public static SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String destAddrStr, String message, boolean statusReportRequested, SmsHeader smsHeader, int encodingtype, int validityPeriod, int priority, boolean use7BitAscii) {
        int validityPeriod2 = validityPeriod;
        if (destAddrStr != null) {
            if (message != null) {
                if (destAddrStr.isEmpty()) {
                    Log.e(LOG_TAG, "getSubmitPdu, destination address is empty. do nothing.");
                    return null;
                } else if (message.isEmpty()) {
                    Log.e(LOG_TAG, "getSubmitPdu, message text is empty. do nothing.");
                    return null;
                } else {
                    if (validityPeriod2 > 244 && validityPeriod2 <= 255) {
                        validityPeriod2 = 244;
                    }
                    UserData uData = new UserData();
                    uData.payloadStr = message;
                    uData.userDataHeader = smsHeader;
                    int i = 2;
                    if (encodingtype == 1) {
                        if (!use7BitAscii) {
                            i = 9;
                        }
                        uData.msgEncoding = i;
                    } else if (encodingtype == 2) {
                        uData.msgEncoding = 0;
                    } else {
                        uData.msgEncoding = 4;
                    }
                    uData.msgEncodingSet = true;
                    return privateGetPdu(destAddrStr, statusReportRequested, uData, 0, validityPeriod2, priority);
                }
            }
        }
        Log.e(LOG_TAG, "getSubmitPdu, null sms text or destination address. do nothing.");
        return null;
    }

    public static GsmAlphabet.TextEncodingDetails calculateLength(CharSequence messageBody, boolean use7bitOnly, int encodingType) {
        if (Resources.getSystem().getBoolean(17891524)) {
            Rlog.d(LOG_TAG, "here use BearerData.calcTextEncodingDetails, but divide in parent class will use Sms7BitEncodingTranslator.translate(messageBody) returned string instead again in this case, Caution!!");
            Rlog.d(LOG_TAG, "search calculateLengthCDMA for help!", new Throwable());
        }
        return MtkBearerData.calcTextEncodingDetails(messageBody, use7bitOnly, encodingType);
    }

    public static MtkSmsMessage newMtkSmsMessage(SmsMessage sms) {
        if (sms == null) {
            return null;
        }
        MtkSmsMessage mtkSms = new MtkSmsMessage();
        mtkSms.mScAddress = sms.getServiceCenterAddress();
        mtkSms.mOriginatingAddress = sms.mOriginatingAddress;
        mtkSms.mMessageBody = sms.getMessageBody();
        mtkSms.mPseudoSubject = sms.getPseudoSubject();
        mtkSms.mEmailFrom = sms.getEmailFrom();
        mtkSms.mEmailBody = sms.getEmailBody();
        mtkSms.mIsEmail = sms.isEmail();
        mtkSms.mScTimeMillis = sms.getTimestampMillis();
        mtkSms.mPdu = sms.getPdu();
        mtkSms.mUserData = sms.getUserData();
        mtkSms.mUserDataHeader = sms.getUserDataHeader();
        mtkSms.mIsMwi = false;
        mtkSms.mMwiSense = false;
        mtkSms.mMwiDontStore = false;
        mtkSms.mStatusOnIcc = sms.getStatusOnIcc();
        mtkSms.mIndexOnIcc = sms.getIndexOnIcc();
        mtkSms.mMessageRef = sms.mMessageRef;
        mtkSms.status = sms.getStatus() >> 16;
        mtkSms.mEnvelope = sms.mEnvelope;
        mtkSms.mBearerData = sms.mBearerData;
        return mtkSms;
    }

    private static String handlePlusCodeInternal(int ton, String number) {
        String ret = sPlusCodeUtils.removeIddNddAddPlusCodeForSms(number);
        if (TextUtils.isEmpty(ret)) {
            return null;
        }
        if (ton == 1 && number.charAt(0) != '+') {
            ret = "+" + ret;
        }
        Rlog.d(LOG_TAG, "handlePlusCodeInternal, after handled, the address = " + Rlog.pii(LOG_TAG, ret));
        return ret;
    }

    private static void replaceIddNddWithPluscode(SmsAddress addr) {
        String orignalStr = new String(addr.origBytes);
        String number = handlePlusCodeInternal(addr.ton, orignalStr);
        if (!TextUtils.isEmpty(number) && (!number.equals(orignalStr))) {
            addr.origBytes = number.getBytes();
            addr.address = number;
        }
    }

    public String getDestinationAddress() {
        return getOriginatingAddress();
    }

    public void parseSms() {
        if (this.mEnvelope.teleService == 262144) {
            this.mBearerData = new BearerData();
            if (this.mEnvelope.bearerData != null) {
                this.mBearerData.numberOfMessages = this.mEnvelope.bearerData[0] & 255;
            }
            Rlog.d(LOG_TAG, "parseSms: get MWI " + Integer.toString(this.mBearerData.numberOfMessages));
            return;
        }
        this.mBearerData = BearerData.decode(this.mEnvelope.bearerData);
        if (Rlog.isLoggable(LOGGABLE_TAG, 2)) {
            Rlog.d(LOG_TAG, "MT raw BearerData = '" + HexDump.toHexString(this.mEnvelope.bearerData) + "'");
            StringBuilder sb = new StringBuilder();
            sb.append("MT (decoded) BearerData = ");
            sb.append(this.mBearerData);
            Rlog.d(LOG_TAG, sb.toString());
        }
        if (this.mBearerData != null) {
            this.mMessageRef = this.mBearerData.messageId;
            if (this.mBearerData.userData != null) {
                this.mUserData = this.mBearerData.userData.payload;
                this.mUserDataHeader = this.mBearerData.userData.userDataHeader;
                this.mMessageBody = this.mBearerData.userData.payloadStr;
            }
            if (this.mOriginatingAddress != null) {
                decodeSmsDisplayAddress(this.mOriginatingAddress);
            }
            if (this.mRecipientAddress != null) {
                decodeSmsDisplayAddress(this.mRecipientAddress);
            }
            if (this.mBearerData.msgCenterTimeStamp != null) {
                this.mScTimeMillis = this.mBearerData.msgCenterTimeStamp.toMillis(true);
            }
            if (this.mBearerData.messageType == 4) {
                if (!this.mBearerData.messageStatusSet) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("DELIVERY_ACK message without msgStatus (");
                    sb2.append(this.mUserData == null ? "also missing" : "does have");
                    sb2.append(" userData).");
                    Rlog.d(LOG_TAG, sb2.toString());
                    this.status = 0;
                } else {
                    this.status = this.mBearerData.errorClass << 8;
                    this.status |= this.mBearerData.messageStatus;
                }
            } else if (!(this.mBearerData.messageType == 1 || this.mBearerData.messageType == 2)) {
                throw new RuntimeException("Unsupported message type: " + this.mBearerData.messageType);
            }
            if (this.mMessageBody != null) {
                parseMessageBody();
                return;
            }
            return;
        }
        throw new RuntimeException("Unsupported message: BearerData decode failed.");
    }
}
