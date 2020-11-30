package com.mediatek.internal.telephony.cat;

import android.content.res.Resources;
import com.android.internal.telephony.cat.ComprehensionTlv;
import com.android.internal.telephony.cat.Item;
import com.android.internal.telephony.cat.ResultCode;
import com.android.internal.telephony.cat.ResultException;
import com.android.internal.telephony.uicc.IccUtils;
import com.mediatek.internal.telephony.ppl.PplMessageManager;

/* access modifiers changed from: package-private */
public abstract class MtkValueParser {
    MtkValueParser() {
    }

    static Item retrieveItem(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int length = ctlv.getLength();
        if (length == 0) {
            return null;
        }
        try {
            return new Item(rawValue[valueIndex] & PplMessageManager.Type.INVALID, IccUtils.adnStringFieldToString(rawValue, valueIndex + 1, removeInvalidCharInItemTextString(rawValue, valueIndex, length - 1)));
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.d("ValueParser", "retrieveItem fail");
            return null;
        }
    }

    static int removeInvalidCharInItemTextString(byte[] rawValue, int valueIndex, int textLen) {
        Boolean isucs2 = false;
        int len = textLen;
        if ((textLen >= 1 && rawValue[valueIndex + 1] == Byte.MIN_VALUE) || ((textLen >= 3 && rawValue[valueIndex + 1] == -127) || (textLen >= 4 && rawValue[valueIndex + 1] == -126))) {
            isucs2 = true;
        }
        if (!isucs2.booleanValue() && textLen > 0) {
            int i = textLen;
            while (i > 0 && rawValue[valueIndex + i] == -16) {
                len--;
                i--;
            }
        }
        return len;
    }

    static String retrieveAlphaId(ComprehensionTlv ctlv) throws ResultException {
        boolean noAlphaUsrCnf;
        if (ctlv != null) {
            byte[] rawValue = ctlv.getRawValue();
            int valueIndex = ctlv.getValueIndex();
            int length = ctlv.getLength();
            if (length != 0) {
                try {
                    return IccUtils.adnStringFieldToString(rawValue, valueIndex, length);
                } catch (IndexOutOfBoundsException e) {
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                }
            } else {
                MtkCatLog.d("ValueParser", "Alpha Id length=" + length);
                return "";
            }
        } else {
            try {
                noAlphaUsrCnf = Resources.getSystem().getBoolean(17891527);
            } catch (Resources.NotFoundException e2) {
                noAlphaUsrCnf = false;
            }
            if (noAlphaUsrCnf) {
                return null;
            }
            return "Default Message";
        }
    }

    static byte[] retrieveNextActionIndicator(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int length = ctlv.getLength();
        byte[] nai = new byte[length];
        int index = 0;
        while (index < length) {
            int index2 = index + 1;
            int valueIndex2 = valueIndex + 1;
            try {
                nai[index] = rawValue[valueIndex];
                index = index2;
                valueIndex = valueIndex2;
            } catch (IndexOutOfBoundsException e) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
        return nai;
    }

    static int retrieveTarget(ComprehensionTlv ctlv) throws ResultException {
        try {
            return ctlv.getRawValue()[ctlv.getValueIndex()] & PplMessageManager.Type.INVALID;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }
}
