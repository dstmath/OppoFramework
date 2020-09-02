package com.mediatek.internal.telephony.uicc;

import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.uicc.IccUtils;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import java.nio.charset.Charset;
import java.util.Arrays;

public class MtkIccUtilsEx extends IccUtils {
    public static final int CDMA_CARD_TYPE_NOT_3GCARD = 0;
    public static final int CDMA_CARD_TYPE_RUIM_SIM = 2;
    public static final int CDMA_CARD_TYPE_UIM_ONLY = 1;
    static final String MTK_LOG_TAG = "MtkIccUtilsEx";
    protected static final String[] PROPERTY_RIL_CT3G = {"vendor.gsm.ril.ct3g", "vendor.gsm.ril.ct3g.2", "vendor.gsm.ril.ct3g.3", "vendor.gsm.ril.ct3g.4"};
    protected static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"vendor.gsm.ril.fulluicctype", "vendor.gsm.ril.fulluicctype.2", "vendor.gsm.ril.fulluicctype.3", "vendor.gsm.ril.fulluicctype.4"};
    protected static final int TAG_FULL_NETWORK_NAME = 67;
    private static final int TAG_ISIM_VALUE = 128;

    public static String parseSpnToString(int family, byte[] data) {
        if (data == null) {
            return null;
        }
        if (1 == family) {
            return IccUtils.adnStringFieldToString(data, 1, data.length - 1);
        }
        if (2 == family) {
            byte b = data[1];
            byte b2 = data[2];
            int len = 32;
            byte[] spnData = new byte[32];
            if (data.length - 3 < 32) {
                len = data.length - 3;
            }
            System.arraycopy(data, 3, spnData, 0, len);
            int numBytes = 0;
            while (numBytes < spnData.length && (spnData[numBytes] & PplMessageManager.Type.INVALID) != 255) {
                numBytes++;
            }
            if (numBytes == 0) {
                return "";
            }
            if (b != 0) {
                if (b != 2) {
                    if (b != 3) {
                        if (b == 4) {
                            return new String(spnData, 0, numBytes, "utf-16");
                        }
                        if (b != 8) {
                            if (b != 9) {
                                try {
                                    Rlog.d(MTK_LOG_TAG, "spn decode error: " + ((int) b));
                                } catch (Exception e) {
                                    Rlog.d(MTK_LOG_TAG, "spn decode error: " + e);
                                }
                            }
                        }
                    }
                    return GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7);
                }
                String spn = new String(spnData, 0, numBytes, "US-ASCII");
                if (TextUtils.isPrintableAsciiOnly(spn)) {
                    return spn;
                }
                return GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7);
            }
            return new String(spnData, 0, numBytes, "ISO-8859-1");
        }
        return null;
    }

    public static String parsePnnToString(byte[] data) {
        if (data == null) {
            return null;
        }
        SimTlv tlv = new SimTlv(data, 0, data.length);
        while (tlv.isValidObject()) {
            if (tlv.getTag() == TAG_FULL_NETWORK_NAME) {
                return networkNameToString(tlv.getData(), 0, tlv.getData().length);
            }
            tlv.nextObject();
        }
        return null;
    }

    public static String parseImpiToString(byte[] data) {
        if (data == null) {
            return null;
        }
        SimTlv tlv = new SimTlv(data, 0, data.length);
        do {
            if (tlv.isValidObject() && tlv.getTag() == 128) {
                return new String(tlv.getData(), Charset.forName("UTF-8"));
            }
        } while (tlv.nextObject());
        Rlog.d(MTK_LOG_TAG, "[ISIM] can't find TLV. record = " + IccUtils.bytesToHexString(data));
        return null;
    }

    public static int checkCdma3gCard(int slotId) {
        String[] values = null;
        int cdma3gCardType = -1;
        if (slotId >= 0) {
            String[] strArr = PROPERTY_RIL_FULL_UICC_TYPE;
            if (slotId < strArr.length) {
                String prop = SystemProperties.get(strArr[slotId]);
                if (prop != null && prop.length() > 0) {
                    values = prop.split(",");
                }
                if (values != null) {
                    if (Arrays.asList(values).contains("RUIM") && Arrays.asList(values).contains("SIM")) {
                        cdma3gCardType = 2;
                    } else if ((Arrays.asList(values).contains("USIM") || Arrays.asList(values).contains("SIM")) && (!Arrays.asList(values).contains("SIM") || !"1".equals(SystemProperties.get(PROPERTY_RIL_CT3G[slotId])))) {
                        cdma3gCardType = 0;
                    } else {
                        cdma3gCardType = 1;
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append("checkCdma3gCard slotId ");
                sb.append(slotId);
                sb.append(", prop value = ");
                sb.append(prop);
                sb.append(", size = ");
                sb.append(values != null ? values.length : 0);
                sb.append(", cdma3gCardType = ");
                sb.append(cdma3gCardType);
                Rlog.d(MTK_LOG_TAG, sb.toString());
                return cdma3gCardType;
            }
        }
        Rlog.d(MTK_LOG_TAG, "checkCdma3gCard: invalid slotId " + slotId);
        return -1;
    }

    public static String getPrintableString(String str, int length) {
        if (str == null) {
            return null;
        }
        if (str.length() <= length) {
            return str;
        }
        return str.substring(0, length) + Rlog.pii(false, str.substring(length));
    }
}
