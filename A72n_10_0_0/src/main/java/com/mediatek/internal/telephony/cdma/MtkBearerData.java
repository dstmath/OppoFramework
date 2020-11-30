package com.mediatek.internal.telephony.cdma;

import android.telephony.Rlog;
import android.text.format.Time;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.util.BitwiseInputStream;
import com.android.internal.util.BitwiseOutputStream;
import mediatek.telephony.MtkServiceState;
import mediatek.telephony.MtkSmsCbCmasInfo;

public class MtkBearerData extends BearerData {
    private static final String LOG_TAG = "MtkBearerData";
    private static final byte SUBPARAM_MESSAGE_CENTER_TIME_STAMP = 3;
    private static final byte SUBPARAM_USER_DATA = 1;
    private static final byte UNENCODABLE_7_BIT_CHAR = 32;

    public static BearerData decode(byte[] smsData, int serviceCategory) {
        BearerData bData = BearerData.decode(smsData, serviceCategory);
        if (!(bData == null || bData.userData == null || !BearerData.isCmasAlertCategory(serviceCategory))) {
            try {
                BearerData temp = reGetUserData(smsData, serviceCategory);
                UserData backup = bData.userData;
                bData.userData = temp.userData;
                decodeCmasUserData(bData, serviceCategory);
                bData.userData = backup;
            } catch (BitwiseInputStream.AccessException ex) {
                Rlog.e(LOG_TAG, "BearerData decode failed: " + ex);
            } catch (BearerData.CodingException ex2) {
                Rlog.e(LOG_TAG, "BearerData decode failed: " + ex2);
                ex2.printStackTrace();
            }
        }
        return bData;
    }

    public static byte[] encode(BearerData bData) {
        byte[] main = BearerData.encode(bData);
        if (main == null) {
            return null;
        }
        byte[] append = encodeMsgCenterTimeStamp(bData);
        byte[] result = new byte[(main.length + append.length)];
        System.arraycopy(main, 0, result, 0, main.length);
        System.arraycopy(append, 0, result, main.length, append.length);
        return result;
    }

    public static GsmAlphabet.TextEncodingDetails calcTextEncodingDetails(CharSequence msg, boolean force7BitEncoding, int encodingType) {
        int septets = BearerData.countAsciiSeptets(msg, force7BitEncoding);
        if (encodingType == 3) {
            Rlog.d(LOG_TAG, "16bit in cdma");
            septets = -1;
        }
        if (septets == -1 || septets > 160) {
            Rlog.d(LOG_TAG, "gsm can understand the control character, but cdma ignore it(<0x20)");
            GsmAlphabet.TextEncodingDetails ted = BearerData.calcTextEncodingDetails(msg, force7BitEncoding, true);
            if (ted.msgCount != 1 || ted.codeUnitSize != 1) {
                return ted;
            }
            ted.codeUnitCount = msg.length();
            int octets = ted.codeUnitCount * 2;
            if (octets > 140) {
                ted.msgCount = (octets + MtkServiceState.RIL_RADIO_TECHNOLOGY_DC_DPA) / MtkServiceState.RIL_RADIO_TECHNOLOGY_DC_UPA;
                ted.codeUnitsRemaining = ((ted.msgCount * MtkServiceState.RIL_RADIO_TECHNOLOGY_DC_UPA) - octets) / 2;
            } else {
                ted.msgCount = 1;
                ted.codeUnitsRemaining = (140 - octets) / 2;
            }
            ted.codeUnitSize = 3;
            return ted;
        }
        GsmAlphabet.TextEncodingDetails ted2 = new GsmAlphabet.TextEncodingDetails();
        ted2.msgCount = 1;
        ted2.codeUnitCount = septets;
        ted2.codeUnitsRemaining = 160 - septets;
        ted2.codeUnitSize = 1;
        return ted2;
    }

    private static byte cdmaIntToBcdByte(int value) {
        int value2 = value % 100;
        return (byte) (((value2 / 10) << 4) | (value2 % 10));
    }

    private static void encodeMsgCenterTimeStamp(BearerData bData, BitwiseOutputStream outStream) throws BitwiseOutputStream.AccessException {
        outStream.write(8, 6);
        int year = bData.msgCenterTimeStamp.year - 2000;
        if (year < 0) {
            year = bData.msgCenterTimeStamp.year - 1900;
        }
        outStream.write(8, cdmaIntToBcdByte(year));
        outStream.write(8, cdmaIntToBcdByte(bData.msgCenterTimeStamp.month + 1));
        outStream.write(8, cdmaIntToBcdByte(bData.msgCenterTimeStamp.monthDay));
        outStream.write(8, cdmaIntToBcdByte(bData.msgCenterTimeStamp.hour));
        outStream.write(8, cdmaIntToBcdByte(bData.msgCenterTimeStamp.minute));
        outStream.write(8, cdmaIntToBcdByte(bData.msgCenterTimeStamp.second));
    }

    private static byte[] encodeMsgCenterTimeStamp(BearerData bData) {
        try {
            BitwiseOutputStream outStream = new BitwiseOutputStream(200);
            if (bData.msgCenterTimeStamp != null) {
                outStream.write(8, 3);
                encodeMsgCenterTimeStamp(bData, outStream);
                return outStream.toByteArray();
            }
        } catch (BitwiseOutputStream.AccessException ex) {
            Rlog.e(LOG_TAG, "BearerData encode failed: " + ex);
        }
        return new byte[0];
    }

    public static void decodeCmasUserData(BearerData bData, int serviceCategory) throws BitwiseInputStream.AccessException, BearerData.CodingException {
        BitwiseInputStream inStream = new BitwiseInputStream(bData.userData.payload);
        int protocolVersion = inStream.read(8);
        if (protocolVersion == 0) {
            long expiration = 0;
            while (inStream.available() >= 16) {
                int recordType = inStream.read(8);
                int recordLen = inStream.read(8);
                if (recordType != 2) {
                    Rlog.w(LOG_TAG, "skipping CMAS record type " + recordType);
                    inStream.skip(recordLen * 8);
                } else {
                    int identifier = (inStream.read(8) << 8) | inStream.read(8);
                    inStream.read(8);
                    expiration = getCmasExpireTime(inStream.readByteArray(48));
                    inStream.read(8);
                }
            }
            bData.cmasWarningInfo = new MtkSmsCbCmasInfo(bData.cmasWarningInfo.getMessageClass(), bData.cmasWarningInfo.getCategory(), bData.cmasWarningInfo.getResponseType(), bData.cmasWarningInfo.getSeverity(), bData.cmasWarningInfo.getUrgency(), bData.cmasWarningInfo.getCertainty(), expiration);
            Rlog.w(LOG_TAG, "MtkSmsCbCmasInfo " + bData.cmasWarningInfo);
            return;
        }
        throw new BearerData.CodingException("unsupported CMAE_protocol_version " + protocolVersion);
    }

    private static BearerData reGetUserData(byte[] smsData, int serviceCategory) throws BitwiseInputStream.AccessException {
        BitwiseInputStream inStream = new BitwiseInputStream(smsData);
        BearerData bData = new BearerData();
        while (inStream.available() > 0) {
            int subparamId = inStream.read(8);
            if (subparamId != 1) {
                decodeReserved(bData, inStream, subparamId);
            } else {
                decodeUserData(bData, inStream);
            }
        }
        return bData;
    }

    private static boolean decodeUserData(BearerData bData, BitwiseInputStream inStream) throws BitwiseInputStream.AccessException {
        int paramBits = inStream.read(8) * 8;
        bData.userData = new UserData();
        bData.userData.msgEncoding = inStream.read(5);
        bData.userData.msgEncodingSet = true;
        bData.userData.msgType = 0;
        int consumedBits = 5;
        if (bData.userData.msgEncoding == 1 || bData.userData.msgEncoding == 10) {
            bData.userData.msgType = inStream.read(8);
            consumedBits = 5 + 8;
        }
        bData.userData.numFields = inStream.read(8);
        bData.userData.payload = inStream.readByteArray(paramBits - (consumedBits + 8));
        return true;
    }

    private static boolean decodeReserved(BearerData bData, BitwiseInputStream inStream, int subparamId) throws BitwiseInputStream.AccessException {
        int paramBits = inStream.read(8) * 8;
        if (paramBits > inStream.available()) {
            return false;
        }
        inStream.skip(paramBits);
        return true;
    }

    private static long getCmasExpireTime(byte[] data) {
        Time ts = new Time("UTC");
        byte b = data[0];
        if (b > 99 || b < 0) {
            return 0;
        }
        ts.year = b >= 96 ? b + 1900 : b + 2000;
        byte b2 = data[1];
        if (b2 < 1 || b2 > 12) {
            return 0;
        }
        ts.month = b2 - 1;
        byte b3 = data[2];
        if (b3 < 1 || b3 > 31) {
            return 0;
        }
        ts.monthDay = b3;
        byte b4 = data[3];
        if (b4 < 0 || b4 > 23) {
            return 0;
        }
        ts.hour = b4;
        byte b5 = data[4];
        if (b5 < 0 || b5 > 59) {
            return 0;
        }
        ts.minute = b5;
        byte b6 = data[5];
        if (b6 < 0 || b6 > 59) {
            return 0;
        }
        ts.second = b6;
        return ts.toMillis(true);
    }
}
