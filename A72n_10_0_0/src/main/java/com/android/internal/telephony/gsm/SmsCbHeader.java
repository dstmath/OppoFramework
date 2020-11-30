package com.android.internal.telephony.gsm;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.telephony.SmsCbCmasInfo;
import android.telephony.SmsCbEtwsInfo;
import com.android.internal.midi.MidiConstants;
import java.util.Arrays;

public class SmsCbHeader {
    protected static final int FORMAT_ETWS_PRIMARY = 3;
    protected static final int FORMAT_GSM = 1;
    protected static final int FORMAT_UMTS = 2;
    protected static final int MESSAGE_TYPE_CBS_MESSAGE = 1;
    public static final int PDU_HEADER_LENGTH = 6;
    protected static final int PDU_LENGTH_ETWS = 56;
    protected static final int PDU_LENGTH_GSM = 88;
    protected SmsCbCmasInfo mCmasInfo;
    protected int mDataCodingScheme;
    protected SmsCbEtwsInfo mEtwsInfo;
    protected int mFormat;
    protected int mGeographicalScope;
    protected int mMessageIdentifier;
    protected int mNrOfPages;
    protected int mPageIndex;
    protected int mSerialNumber;

    public SmsCbHeader(byte[] pdu) throws IllegalArgumentException {
        byte[] warningSecurityInfo;
        if (pdu == null || pdu.length < 6) {
            throw new IllegalArgumentException("Illegal PDU");
        }
        if (pdu.length <= 88) {
            this.mGeographicalScope = (pdu[0] & 192) >>> 6;
            this.mSerialNumber = ((pdu[0] & 255) << 8) | (pdu[1] & 255);
            this.mMessageIdentifier = ((pdu[2] & 255) << 8) | (pdu[3] & 255);
            if (!isEtwsMessage() || pdu.length > 56) {
                this.mFormat = 1;
                this.mDataCodingScheme = pdu[4] & 255;
                int pageIndex = (pdu[5] & 240) >>> 4;
                int nrOfPages = pdu[5] & 15;
                if (pageIndex == 0 || nrOfPages == 0 || pageIndex > nrOfPages) {
                    pageIndex = 1;
                    nrOfPages = 1;
                }
                this.mPageIndex = pageIndex;
                this.mNrOfPages = nrOfPages;
            } else {
                this.mFormat = 3;
                this.mDataCodingScheme = -1;
                this.mPageIndex = -1;
                this.mNrOfPages = -1;
                boolean emergencyUserAlert = (pdu[4] & 1) != 0;
                boolean activatePopup = (pdu[5] & 128) != 0;
                int warningType = (pdu[4] & MidiConstants.STATUS_ACTIVE_SENSING) >>> 1;
                if (pdu.length > 6) {
                    warningSecurityInfo = Arrays.copyOfRange(pdu, 6, pdu.length);
                } else {
                    warningSecurityInfo = null;
                }
                this.mEtwsInfo = new SmsCbEtwsInfo(warningType, emergencyUserAlert, activatePopup, true, warningSecurityInfo);
                this.mCmasInfo = null;
                return;
            }
        } else {
            this.mFormat = 2;
            byte b = pdu[0];
            if (b == 1) {
                this.mMessageIdentifier = ((pdu[1] & 255) << 8) | (pdu[2] & 255);
                this.mGeographicalScope = (pdu[3] & 192) >>> 6;
                this.mSerialNumber = ((pdu[3] & 255) << 8) | (pdu[4] & 255);
                this.mDataCodingScheme = pdu[5] & 255;
                this.mPageIndex = 1;
                this.mNrOfPages = 1;
            } else {
                throw new IllegalArgumentException("Unsupported message type " + ((int) b));
            }
        }
        if (isEtwsMessage()) {
            this.mEtwsInfo = new SmsCbEtwsInfo(getEtwsWarningType(), isEtwsEmergencyUserAlert(), isEtwsPopupAlert(), false, null);
            this.mCmasInfo = null;
        } else if (isCmasMessage()) {
            int messageClass = getCmasMessageClass();
            int severity = getCmasSeverity();
            int urgency = getCmasUrgency();
            int certainty = getCmasCertainty();
            this.mEtwsInfo = null;
            this.mCmasInfo = makeSmsCbCmasInfo(messageClass, -1, -1, severity, urgency, certainty);
        } else {
            this.mEtwsInfo = null;
            this.mCmasInfo = null;
        }
    }

    public int getGeographicalScope() {
        return this.mGeographicalScope;
    }

    public int getSerialNumber() {
        return this.mSerialNumber;
    }

    public int getServiceCategory() {
        return this.mMessageIdentifier;
    }

    public int getDataCodingScheme() {
        return this.mDataCodingScheme;
    }

    public int getPageIndex() {
        return this.mPageIndex;
    }

    public int getNumberOfPages() {
        return this.mNrOfPages;
    }

    public SmsCbEtwsInfo getEtwsInfo() {
        return this.mEtwsInfo;
    }

    public SmsCbCmasInfo getCmasInfo() {
        return this.mCmasInfo;
    }

    /* access modifiers changed from: protected */
    public boolean isEmergencyMessage() {
        int i = this.mMessageIdentifier;
        return i >= 4352 && i <= 6399;
    }

    /* access modifiers changed from: protected */
    public boolean isEtwsMessage() {
        return (this.mMessageIdentifier & SmsCbConstants.MESSAGE_ID_ETWS_TYPE_MASK) == 4352;
    }

    public boolean isEtwsPrimaryNotification() {
        return this.mFormat == 3;
    }

    public boolean isUmtsFormat() {
        return this.mFormat == 2;
    }

    /* access modifiers changed from: protected */
    public boolean isCmasMessage() {
        int i = this.mMessageIdentifier;
        return i >= 4370 && i <= 4399;
    }

    /* access modifiers changed from: protected */
    public boolean isEtwsPopupAlert() {
        return (this.mSerialNumber & 4096) != 0;
    }

    /* access modifiers changed from: protected */
    public boolean isEtwsEmergencyUserAlert() {
        return (this.mSerialNumber & 8192) != 0;
    }

    /* access modifiers changed from: protected */
    public int getEtwsWarningType() {
        return this.mMessageIdentifier - 4352;
    }

    /* access modifiers changed from: protected */
    public int getCmasMessageClass() {
        switch (this.mMessageIdentifier) {
            case 4370:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL_LANGUAGE /* 4383 */:
                return 0;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED /* 4371 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY /* 4372 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED_LANGUAGE /* 4384 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY_LANGUAGE /* 4385 */:
                return 1;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED /* 4373 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY /* 4374 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED /* 4375 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY /* 4376 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED /* 4377 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY /* 4378 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED_LANGUAGE /* 4386 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY_LANGUAGE /* 4387 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED_LANGUAGE /* 4388 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY_LANGUAGE /* 4389 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED_LANGUAGE /* 4390 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY_LANGUAGE /* 4391 */:
                return 2;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_CHILD_ABDUCTION_EMERGENCY /* 4379 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_CHILD_ABDUCTION_EMERGENCY_LANGUAGE /* 4392 */:
                return 3;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_REQUIRED_MONTHLY_TEST /* 4380 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_REQUIRED_MONTHLY_TEST_LANGUAGE /* 4393 */:
                return 4;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXERCISE /* 4381 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXERCISE_LANGUAGE /* 4394 */:
                return 5;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_OPERATOR_DEFINED_USE /* 4382 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_OPERATOR_DEFINED_USE_LANGUAGE /* 4395 */:
                return 6;
            default:
                return -1;
        }
    }

    /* access modifiers changed from: protected */
    public int getCmasSeverity() {
        int i = this.mMessageIdentifier;
        switch (i) {
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED /* 4371 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY /* 4372 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED /* 4373 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY /* 4374 */:
                return 0;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED /* 4375 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY /* 4376 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED /* 4377 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY /* 4378 */:
                return 1;
            default:
                switch (i) {
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED_LANGUAGE /* 4384 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY_LANGUAGE /* 4385 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED_LANGUAGE /* 4386 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY_LANGUAGE /* 4387 */:
                        return 0;
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED_LANGUAGE /* 4388 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY_LANGUAGE /* 4389 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED_LANGUAGE /* 4390 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY_LANGUAGE /* 4391 */:
                        return 1;
                    default:
                        return -1;
                }
        }
    }

    /* access modifiers changed from: protected */
    public int getCmasUrgency() {
        int i = this.mMessageIdentifier;
        switch (i) {
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED /* 4371 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY /* 4372 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED /* 4375 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY /* 4376 */:
                return 0;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED /* 4373 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY /* 4374 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED /* 4377 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY /* 4378 */:
                return 1;
            default:
                switch (i) {
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED_LANGUAGE /* 4384 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY_LANGUAGE /* 4385 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED_LANGUAGE /* 4388 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY_LANGUAGE /* 4389 */:
                        return 0;
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED_LANGUAGE /* 4386 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY_LANGUAGE /* 4387 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED_LANGUAGE /* 4390 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY_LANGUAGE /* 4391 */:
                        return 1;
                    default:
                        return -1;
                }
        }
    }

    /* access modifiers changed from: protected */
    public int getCmasCertainty() {
        int i = this.mMessageIdentifier;
        switch (i) {
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED /* 4371 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED /* 4373 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED /* 4375 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED /* 4377 */:
                return 0;
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY /* 4372 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY /* 4374 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY /* 4376 */:
            case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY /* 4378 */:
                return 1;
            default:
                switch (i) {
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED_LANGUAGE /* 4384 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED_LANGUAGE /* 4386 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_OBSERVED_LANGUAGE /* 4388 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_OBSERVED_LANGUAGE /* 4390 */:
                        return 0;
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY_LANGUAGE /* 4385 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_LIKELY_LANGUAGE /* 4387 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_IMMEDIATE_LIKELY_LANGUAGE /* 4389 */:
                    case SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY_LANGUAGE /* 4391 */:
                        return 1;
                    default:
                        return -1;
                }
        }
    }

    public String toString() {
        return "SmsCbHeader{GS=" + this.mGeographicalScope + ", serialNumber=0x" + Integer.toHexString(this.mSerialNumber) + ", messageIdentifier=0x" + Integer.toHexString(this.mMessageIdentifier) + ", format=" + this.mFormat + ", DCS=0x" + Integer.toHexString(this.mDataCodingScheme) + ", page " + this.mPageIndex + " of " + this.mNrOfPages + '}';
    }

    private SmsCbCmasInfo makeSmsCbCmasInfo(int messageClass, int category, int responseType, int severity, int urgency, int certainty) {
        if (!SystemProperties.get("ro.vendor.mtk_telephony_add_on_policy", WifiEnterpriseConfig.ENGINE_DISABLE).equals(WifiEnterpriseConfig.ENGINE_DISABLE)) {
            return new SmsCbCmasInfo(messageClass, category, responseType, severity, urgency, certainty);
        }
        try {
            return (SmsCbCmasInfo) Class.forName("mediatek.telephony.MtkSmsCbCmasInfo").getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Long.TYPE).newInstance(Integer.valueOf(messageClass), Integer.valueOf(category), Integer.valueOf(responseType), Integer.valueOf(severity), Integer.valueOf(urgency), Integer.valueOf(certainty), 0L);
        } catch (Exception e) {
            return new SmsCbCmasInfo(messageClass, category, responseType, severity, urgency, certainty);
        }
    }

    public SmsCbHeader() {
    }
}
