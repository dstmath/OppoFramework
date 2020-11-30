package com.mediatek.internal.telephony.gsm;

import android.os.Build;
import android.telephony.Rlog;
import android.telephony.SmsCbEtwsInfo;
import android.util.Xml;
import com.android.internal.telephony.gsm.SmsCbHeader;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import mediatek.telephony.MtkSmsCbCmasInfo;
import mediatek.telephony.MtkTelephony;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class MtkSmsCbHeader extends SmsCbHeader {
    private static final boolean ENG = "eng".equals(Build.TYPE);
    private static final String LOG_TAG = "MtkSmsCbHeader";
    private static final String SPECIAL_PWS_CHANNEL_PATH = "/vendor/etc/special_pws_channel.xml";
    private static final Object mListLock = new Object();
    private static HashMap<String, String> mSpecialChannelList = null;
    private boolean mIsEtwsPrimary = false;
    protected String mPlmn;

    public MtkSmsCbHeader(byte[] pdu, String plmn, boolean isEtwsPrimary) throws IllegalArgumentException {
        byte[] warningSecurityInfo;
        if (pdu == null || pdu.length < 6) {
            throw new IllegalArgumentException("Illegal PDU");
        }
        this.mPlmn = plmn;
        this.mIsEtwsPrimary = isEtwsPrimary;
        log("Create header!" + pdu.length);
        if (pdu.length <= 88) {
            this.mGeographicalScope = (pdu[0] & 192) >>> 6;
            this.mSerialNumber = ((pdu[0] & 255) << 8) | (pdu[1] & 255);
            this.mMessageIdentifier = ((pdu[2] & 255) << 8) | (pdu[3] & 255);
            if (!isEtwsMessage() || pdu.length > 56 || !this.mIsEtwsPrimary) {
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
                int warningType = (pdu[4] & 254) >>> 1;
                if (pdu.length > 6) {
                    warningSecurityInfo = Arrays.copyOfRange(pdu, 6, pdu.length);
                } else {
                    warningSecurityInfo = null;
                }
                this.mEtwsInfo = new SmsCbEtwsInfo(warningType, emergencyUserAlert, activatePopup, true, warningSecurityInfo);
                if (ENG) {
                    log("Create primary ETWS Info!");
                }
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
            this.mEtwsInfo = new SmsCbEtwsInfo(getEtwsWarningType(), isEtwsEmergencyUserAlert(), isEtwsPopupAlert(), false, (byte[]) null);
            if (ENG) {
                log("Create non-primary ETWS Info!");
            }
            this.mCmasInfo = null;
        } else if (isCmasMessage()) {
            int messageClass = getCmasMessageClass();
            int severity = getCmasSeverity();
            int urgency = getCmasUrgency();
            int certainty = getCmasCertainty();
            this.mEtwsInfo = null;
            this.mCmasInfo = new MtkSmsCbCmasInfo(messageClass, -1, -1, severity, urgency, certainty, 0);
        } else {
            this.mEtwsInfo = null;
            this.mCmasInfo = null;
        }
        log("pdu length= " + pdu.length + ", " + this);
    }

    public boolean isWHAMMessage() {
        if (this.mMessageIdentifier == 4400) {
            return true;
        }
        return false;
    }

    public boolean isEmergencyMessage() {
        if ((this.mMessageIdentifier < 4352 || this.mMessageIdentifier > 6399) && !checkNationalEmergencyChannels()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isCmasMessage() {
        if ((this.mMessageIdentifier < 4370 || this.mMessageIdentifier > 4399) && !checkNationalEmergencyChannels()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public int getCmasMessageClass() {
        int i = this.mMessageIdentifier;
        if (i == 911 || i == 919 || i == 921) {
            return 0;
        }
        switch (i) {
            case MtkSmsCbConstants.MESSAGE_ID_CMAS_PUBLIC_SAFETY_ALERT /* 4396 */:
            case MtkSmsCbConstants.MESSAGE_ID_CMAS_PUBLIC_SAFETY_ALERT_LANGUAGE /* 4397 */:
                return 7;
            case MtkSmsCbConstants.MESSAGE_ID_CMAS_WEA_TEST /* 4398 */:
            case MtkSmsCbConstants.MESSAGE_ID_CMAS_WEA_TEST_LANGUAGE /* 4399 */:
                return 8;
            default:
                return MtkSmsCbHeader.super.getCmasMessageClass();
        }
    }

    public String toString() {
        return "MtkSmsCbHeader{GS=" + this.mGeographicalScope + ", serialNumber=0x" + Integer.toHexString(this.mSerialNumber) + ", messageIdentifier=0x" + Integer.toHexString(this.mMessageIdentifier) + ", DCS=0x" + Integer.toHexString(this.mDataCodingScheme) + ", page " + this.mPageIndex + " of " + this.mNrOfPages + ", isEtwsPrimary=" + this.mIsEtwsPrimary + ", plmn " + this.mPlmn + '}';
    }

    private static void loadSpecialChannelList() {
        synchronized (mListLock) {
            if (mSpecialChannelList == null) {
                log("load special_pws_channel.xml...");
                mSpecialChannelList = new HashMap<>();
                File dbFile = new File(SPECIAL_PWS_CHANNEL_PATH);
                try {
                    FileReader dbReader = new FileReader(dbFile);
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(dbReader);
                        XmlUtils.beginDocument(parser, "SpecialPwsChannel");
                        while (true) {
                            XmlUtils.nextElement(parser);
                            if (!"SpecialPwsChannel".equals(parser.getName())) {
                                break;
                            }
                            mSpecialChannelList.put(parser.getAttributeValue(null, MtkTelephony.Carriers.MCC), parser.getAttributeValue(null, "channels"));
                        }
                        dbReader.close();
                        log("Special channels list size=" + mSpecialChannelList.size());
                    } catch (XmlPullParserException e) {
                        loge("Exception in parser " + e);
                    } catch (IOException e2) {
                        loge("Exception in parser " + e2);
                    }
                } catch (FileNotFoundException e3) {
                    Rlog.w(LOG_TAG, "Can not open " + dbFile.getAbsolutePath());
                }
            } else {
                log("Special PWS channel list is already loaded");
            }
        }
    }

    private boolean checkNationalEmergencyChannels() {
        String[] values;
        loadSpecialChannelList();
        if (mSpecialChannelList != null) {
            String str = this.mPlmn;
            String mcc = "";
            if (str != null && str.length() >= 3) {
                mcc = this.mPlmn.substring(0, 3);
            }
            String channels = mSpecialChannelList.get(mcc);
            log("checkNationalEmergencyChannels, mPlmn " + this.mPlmn + ",mcc " + mcc + ", channels list " + channels + ", header's channel " + this.mMessageIdentifier);
            if (channels != null && channels.length() > 0) {
                for (String str2 : channels.split(",")) {
                    if (str2.equals(Integer.toString(this.mMessageIdentifier))) {
                        return true;
                    }
                }
            }
        } else {
            log("checkNationalEmergencyChannels, mSpecialChannelList is null!");
        }
        return false;
    }

    private static void log(String msg) {
        if (ENG) {
            Rlog.d(LOG_TAG, msg);
        }
    }

    private static void loge(String msg) {
        if (ENG) {
            Rlog.e(LOG_TAG, msg);
        }
    }
}
