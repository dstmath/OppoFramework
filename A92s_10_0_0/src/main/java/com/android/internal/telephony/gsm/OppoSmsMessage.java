package com.android.internal.telephony.gsm;

import android.content.res.Resources;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.R;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.OemFeatureBase;
import com.android.internal.telephony.Sms7BitEncodingTranslator;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.gsm.SmsMessage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

public class OppoSmsMessage {
    private static final String LOG_TAG = "gsm-OppoSmsMessage";

    public static GsmAlphabet.TextEncodingDetails calculateLengthOem(CharSequence msgBody, boolean use7bitOnly, int encodingType) {
        CharSequence newMsgBody = null;
        if (Resources.getSystem().getBoolean(R.bool.config_sms_force_7bit_encoding)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(msgBody, false);
        }
        if (TextUtils.isEmpty(newMsgBody)) {
            newMsgBody = msgBody;
        }
        GsmAlphabet.TextEncodingDetails ted = GsmAlphabet.countGsmSeptets(newMsgBody, use7bitOnly);
        if (encodingType == 3) {
            Rlog.d(LOG_TAG, "input mode is unicode");
            ted = null;
        }
        if (ted != null) {
            return ted;
        }
        Rlog.d(LOG_TAG, "7-bit encoding fail");
        return SmsMessageBase.calcUnicodeEncodingDetails(newMsgBody);
    }

    public static String getUserDataOem8bit(byte[] mUserData, byte[] mPdu, int mCur, int byteCount) {
        if (mUserData == null || mPdu == null) {
            return null;
        }
        try {
            CharsetDecoder decoderUtf_8 = StandardCharsets.UTF_8.newDecoder();
            int len = mUserData.length;
            byte[] userDataUtf_8 = new byte[len];
            System.arraycopy(mUserData, 0, userDataUtf_8, 0, len);
            return decoderUtf_8.decode(ByteBuffer.wrap(userDataUtf_8)).toString();
        } catch (Exception e) {
            Rlog.d(LOG_TAG, "UTF_8 parse error");
            try {
                return GsmAlphabet.gsm8BitUnpackedToString(mPdu, mCur, byteCount);
            } catch (Exception e2) {
                Rlog.d(LOG_TAG, "GSM_8 parse error");
                return null;
            }
        }
    }

    public static boolean isEnable8BitMtSms() {
        try {
            String regionmark = SystemProperties.get("ro.oppo.regionmark", "");
            String operator = SystemProperties.get("ro.oppo.operator", "");
            String country = SystemProperties.get("ro.oppo.euex.country", "");
            if (!TextUtils.isEmpty(regionmark) && "EUEX".equals(regionmark) && !TextUtils.isEmpty(operator) && "ORANGE".equals(operator) && !TextUtils.isEmpty(country) && "FR".equals(country)) {
                Rlog.d(LOG_TAG, "isEnable8BitMtSms true");
                return true;
            }
        } catch (Exception e) {
        }
        Rlog.d(LOG_TAG, "isEnable8BitMtSms false");
        return false;
    }

    public static SmsMessage.SubmitPdu oemGetSubmitPdu(String scAddress, String destinationAddress, int destinationPort, byte[] data, boolean statusReportRequested) {
        boolean isCtIms = false;
        try {
            isCtIms = SystemProperties.get(OemFeatureBase.CT_AUTOREG_IMS_PROP, WifiEnterpriseConfig.ENGINE_DISABLE).equals(WifiEnterpriseConfig.ENGINE_ENABLE);
            Rlog.d(LOG_TAG, "isCtIms=" + isCtIms);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!isCtIms) {
            return null;
        }
        if (data.length > 140) {
            Rlog.e(LOG_TAG, "SMS data message may only contain 140 bytes");
            return null;
        }
        SmsMessage.SubmitPdu ret = new SmsMessage.SubmitPdu();
        ByteArrayOutputStream bo = SmsMessage.getSubmitPduHeadPublic(scAddress, destinationAddress, (byte) 1, statusReportRequested, ret);
        if (bo == null) {
            return ret;
        }
        bo.write(4);
        bo.write(data.length);
        bo.write(data, 0, data.length);
        ret.encodedMessage = bo.toByteArray();
        try {
            SystemProperties.set(OemFeatureBase.CT_AUTOREG_IMS_PROP, WifiEnterpriseConfig.ENGINE_DISABLE);
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }
        return ret;
    }
}
