package com.mediatek.internal.telephony.phb;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.uicc.CsimFileHandler;
import com.android.internal.telephony.uicc.IccFileHandler;
import java.util.ArrayList;

public class CsimPhbUtil extends Handler {
    private static final String LOG_TAG = "CsimPhbUtil";
    private static final int MAX_NAME_LENGTH = 14;
    private static final int MAX_NUMBER_LENGTH = 20;
    private static final int MAX_SIM_CNT = 4;
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"vendor.gsm.ril.fulluicctype", "vendor.gsm.ril.fulluicctype.2", "vendor.gsm.ril.fulluicctype.3", "vendor.gsm.ril.fulluicctype.4"};
    private static int[] sAdnRecordSize = {-1, -1, -1, -1};

    public static void getPhbRecordInfo(Message response) {
        int[] iArr = sAdnRecordSize;
        iArr[2] = 20;
        iArr[3] = 14;
        Rlog.d(LOG_TAG, "[getPhbRecordInfo] sAdnRecordSize[] {" + sAdnRecordSize[0] + ", " + sAdnRecordSize[1] + ", " + sAdnRecordSize[2] + ", " + sAdnRecordSize[3] + "}");
        if (response != null) {
            AsyncResult.forMessage(response).result = sAdnRecordSize;
            response.sendToTarget();
        }
    }

    public static void clearAdnRecordSize() {
        Rlog.d(LOG_TAG, "[clearAdnRecordSize]");
        if (sAdnRecordSize != null) {
            int i = 0;
            while (true) {
                int[] iArr = sAdnRecordSize;
                if (i < iArr.length) {
                    iArr[i] = -1;
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    public static boolean updatePhbStorageInfo(int update) {
        int[] iArr = sAdnRecordSize;
        int used = iArr[0];
        int total = iArr[1];
        Rlog.d(LOG_TAG, "[updatePhbStorageInfo] used: " + used + ", total: " + total + ", update: " + update);
        if (used > -1) {
            setPhbRecordStorageInfo(total, used + update);
            return true;
        }
        Rlog.d(LOG_TAG, "[updatePhbStorageInfo] Storage info is not ready!");
        return false;
    }

    public static void initPhbStorage(ArrayList<MtkAdnRecord> adnList) {
        if (adnList != null) {
            int totalSize = adnList.size();
            int usedRecord = 0;
            for (int i = 0; i < totalSize; i++) {
                if (!adnList.get(i).isEmpty()) {
                    usedRecord++;
                }
            }
            Rlog.d(LOG_TAG, "[initPhbStorage] Current total: " + sAdnRecordSize[1] + ", used:" + sAdnRecordSize[0] + ", update total: " + totalSize + ", used: " + usedRecord);
            int[] iArr = sAdnRecordSize;
            if (iArr[1] > -1) {
                setPhbRecordStorageInfo(iArr[1] + totalSize, iArr[0] + usedRecord);
                return;
            }
            setPhbRecordStorageInfo(totalSize, usedRecord);
        }
    }

    private static void setPhbRecordStorageInfo(int totalSize, int usedRecord) {
        int[] iArr = sAdnRecordSize;
        iArr[0] = usedRecord;
        iArr[1] = totalSize;
        Rlog.d(LOG_TAG, "[setPhbRecordStorageInfo] usedRecord: " + usedRecord + ", totalSize: " + totalSize);
    }

    public static boolean hasModemPhbEnhanceCapability(IccFileHandler fileHandler) {
        if (SystemProperties.get("ro.vendor.mtk_ril_mode").equals("c6m_1rild") || fileHandler == null || !(fileHandler instanceof CsimFileHandler)) {
            return true;
        }
        for (int i = 0; i < 4; i++) {
            String cardType = SystemProperties.get(PROPERTY_RIL_FULL_UICC_TYPE[i]);
            if (cardType.indexOf("CSIM") >= 0 && cardType.indexOf("USIM") >= 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUsingGsmPhbReady(IccFileHandler fileHandler) {
        if (SystemProperties.get("ro.vendor.mtk_ril_mode").equals("c6m_1rild")) {
            return true;
        }
        if (fileHandler == null || !(fileHandler instanceof CsimFileHandler)) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            String cardType = SystemProperties.get(PROPERTY_RIL_FULL_UICC_TYPE[i]);
            if (cardType.indexOf("CSIM") >= 0 && cardType.indexOf("USIM") >= 0) {
                return true;
            }
        }
        return false;
    }
}
