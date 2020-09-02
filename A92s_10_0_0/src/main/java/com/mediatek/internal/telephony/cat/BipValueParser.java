package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.ComprehensionTlv;
import com.android.internal.telephony.cat.ResultCode;
import com.android.internal.telephony.cat.ResultException;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import java.net.UnknownHostException;

abstract class BipValueParser {
    BipValueParser() {
    }

    static BearerDesc retrieveBearerDesc(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int length = ctlv.getLength();
        int valueIndex2 = valueIndex + 1;
        try {
            int bearerType = rawValue[valueIndex] & 255;
            MtkCatLog.d("CAT", "retrieveBearerDesc: bearerType:" + bearerType + ", length: " + length);
            if (2 == bearerType) {
                GPRSBearerDesc gprsbearerDesc = new GPRSBearerDesc();
                int valueIndex3 = valueIndex2 + 1;
                try {
                    gprsbearerDesc.precedence = rawValue[valueIndex2] & PplMessageManager.Type.INVALID;
                    int valueIndex4 = valueIndex3 + 1;
                    gprsbearerDesc.delay = rawValue[valueIndex3] & PplMessageManager.Type.INVALID;
                    int valueIndex5 = valueIndex4 + 1;
                    gprsbearerDesc.reliability = rawValue[valueIndex4] & PplMessageManager.Type.INVALID;
                    int valueIndex6 = valueIndex5 + 1;
                    gprsbearerDesc.peak = rawValue[valueIndex5] & PplMessageManager.Type.INVALID;
                    int valueIndex7 = valueIndex6 + 1;
                    gprsbearerDesc.mean = rawValue[valueIndex6] & PplMessageManager.Type.INVALID;
                    int i = valueIndex7 + 1;
                    gprsbearerDesc.pdpType = rawValue[valueIndex7] & PplMessageManager.Type.INVALID;
                    return gprsbearerDesc;
                } catch (IndexOutOfBoundsException e) {
                    MtkCatLog.d("CAT", "retrieveBearerDesc: out of bounds");
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                }
            } else if (9 == bearerType) {
                UTranBearerDesc uTranbearerDesc = new UTranBearerDesc();
                int valueIndex8 = valueIndex2 + 1;
                uTranbearerDesc.trafficClass = rawValue[valueIndex2] & PplMessageManager.Type.INVALID;
                int valueIndex9 = valueIndex8 + 1;
                uTranbearerDesc.maxBitRateUL_High = rawValue[valueIndex8] & PplMessageManager.Type.INVALID;
                int valueIndex10 = valueIndex9 + 1;
                uTranbearerDesc.maxBitRateUL_Low = rawValue[valueIndex9] & PplMessageManager.Type.INVALID;
                int valueIndex11 = valueIndex10 + 1;
                uTranbearerDesc.maxBitRateDL_High = rawValue[valueIndex10] & PplMessageManager.Type.INVALID;
                int valueIndex12 = valueIndex11 + 1;
                uTranbearerDesc.maxBitRateDL_Low = rawValue[valueIndex11] & PplMessageManager.Type.INVALID;
                int valueIndex13 = valueIndex12 + 1;
                uTranbearerDesc.guarBitRateUL_High = rawValue[valueIndex12] & PplMessageManager.Type.INVALID;
                int valueIndex14 = valueIndex13 + 1;
                uTranbearerDesc.guarBitRateUL_Low = rawValue[valueIndex13] & PplMessageManager.Type.INVALID;
                int valueIndex15 = valueIndex14 + 1;
                uTranbearerDesc.guarBitRateDL_High = rawValue[valueIndex14] & PplMessageManager.Type.INVALID;
                int valueIndex16 = valueIndex15 + 1;
                uTranbearerDesc.guarBitRateDL_Low = rawValue[valueIndex15] & PplMessageManager.Type.INVALID;
                int valueIndex17 = valueIndex16 + 1;
                uTranbearerDesc.deliveryOrder = rawValue[valueIndex16] & PplMessageManager.Type.INVALID;
                int valueIndex18 = valueIndex17 + 1;
                uTranbearerDesc.maxSduSize = rawValue[valueIndex17] & PplMessageManager.Type.INVALID;
                int valueIndex19 = valueIndex18 + 1;
                uTranbearerDesc.sduErrorRatio = rawValue[valueIndex18] & PplMessageManager.Type.INVALID;
                int valueIndex20 = valueIndex19 + 1;
                uTranbearerDesc.residualBitErrorRadio = rawValue[valueIndex19] & PplMessageManager.Type.INVALID;
                int valueIndex21 = valueIndex20 + 1;
                uTranbearerDesc.deliveryOfErroneousSdus = rawValue[valueIndex20] & PplMessageManager.Type.INVALID;
                int valueIndex22 = valueIndex21 + 1;
                uTranbearerDesc.transferDelay = rawValue[valueIndex21] & PplMessageManager.Type.INVALID;
                int valueIndex23 = valueIndex22 + 1;
                uTranbearerDesc.trafficHandlingPriority = rawValue[valueIndex22] & PplMessageManager.Type.INVALID;
                int i2 = valueIndex23 + 1;
                uTranbearerDesc.pdpType = rawValue[valueIndex23] & PplMessageManager.Type.INVALID;
                return uTranbearerDesc;
            } else if (11 == bearerType) {
                EUTranBearerDesc euTranbearerDesc = new EUTranBearerDesc();
                int valueIndex24 = valueIndex2 + 1;
                euTranbearerDesc.QCI = rawValue[valueIndex2] & PplMessageManager.Type.INVALID;
                int valueIndex25 = valueIndex24 + 1;
                euTranbearerDesc.maxBitRateU = rawValue[valueIndex24] & PplMessageManager.Type.INVALID;
                int valueIndex26 = valueIndex25 + 1;
                euTranbearerDesc.maxBitRateD = rawValue[valueIndex25] & PplMessageManager.Type.INVALID;
                int valueIndex27 = valueIndex26 + 1;
                euTranbearerDesc.guarBitRateU = rawValue[valueIndex26] & PplMessageManager.Type.INVALID;
                int valueIndex28 = valueIndex27 + 1;
                euTranbearerDesc.guarBitRateD = rawValue[valueIndex27] & PplMessageManager.Type.INVALID;
                int valueIndex29 = valueIndex28 + 1;
                euTranbearerDesc.maxBitRateUEx = rawValue[valueIndex28] & PplMessageManager.Type.INVALID;
                int valueIndex30 = valueIndex29 + 1;
                euTranbearerDesc.maxBitRateDEx = rawValue[valueIndex29] & PplMessageManager.Type.INVALID;
                int valueIndex31 = valueIndex30 + 1;
                euTranbearerDesc.guarBitRateUEx = rawValue[valueIndex30] & PplMessageManager.Type.INVALID;
                int valueIndex32 = valueIndex31 + 1;
                euTranbearerDesc.guarBitRateDEx = rawValue[valueIndex31] & PplMessageManager.Type.INVALID;
                int i3 = valueIndex32 + 1;
                euTranbearerDesc.pdnType = rawValue[valueIndex32] & PplMessageManager.Type.INVALID;
                return euTranbearerDesc;
            } else if (3 == bearerType) {
                return new DefaultBearerDesc();
            } else {
                if (1 == bearerType) {
                    MtkCatLog.d("CAT", "retrieveBearerDesc: unsupport CSD");
                    throw new ResultException(ResultCode.BEYOND_TERMINAL_CAPABILITY);
                }
                MtkCatLog.d("CAT", "retrieveBearerDesc: un-understood bearer type");
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        } catch (IndexOutOfBoundsException e2) {
            MtkCatLog.d("CAT", "retrieveBearerDesc: out of bounds");
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static int retrieveBufferSize(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        try {
            return ((rawValue[valueIndex] & PplMessageManager.Type.INVALID) << 8) + (rawValue[valueIndex + 1] & PplMessageManager.Type.INVALID);
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.d("CAT", "retrieveBufferSize: out of bounds");
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    /* JADX INFO: Multiple debug info for r4v1 byte: [D('len' int), D('valueIndex' int)] */
    /* JADX INFO: Multiple debug info for r10v8 'valueIndex'  int: [D('tmp_string' java.lang.String), D('valueIndex' int)] */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.String.<init>(byte[], int, int):void}
     arg types: [byte[], int, byte]
     candidates:
      ClspMth{java.lang.String.<init>(int[], int, int):void}
      ClspMth{java.lang.String.<init>(char[], int, int):void}
      ClspMth{java.lang.String.<init>(byte[], int, int):void} */
    static String retrieveNetworkAccessName(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        String networkAccessName = null;
        try {
            int totalLen = ctlv.getLength();
            new String(rawValue, valueIndex, totalLen);
            String stkNetworkIdentifier = null;
            String stkOperatorIdentifier = null;
            if (totalLen > 0) {
                int valueIndex2 = valueIndex + 1;
                try {
                    byte b = rawValue[valueIndex];
                    if (totalLen > b) {
                        stkNetworkIdentifier = new String(rawValue, valueIndex2, (int) b);
                        valueIndex2 += b;
                    }
                    MtkCatLog.d("CAT", "totalLen:" + totalLen + ";" + valueIndex2 + ";" + ((int) b));
                    while (totalLen > b + 1) {
                        totalLen -= b + 1;
                        int valueIndex3 = valueIndex2 + 1;
                        try {
                            b = rawValue[valueIndex2];
                            MtkCatLog.d("CAT", "next len: " + ((int) b));
                            if (totalLen > b) {
                                String tmp_string = new String(rawValue, valueIndex3, (int) b);
                                if (stkOperatorIdentifier == null) {
                                    stkOperatorIdentifier = tmp_string;
                                } else {
                                    stkOperatorIdentifier = stkOperatorIdentifier + "." + tmp_string;
                                }
                            }
                            valueIndex2 = valueIndex3 + b;
                            MtkCatLog.d("CAT", "totalLen:" + totalLen + ";" + valueIndex2 + ";" + ((int) b));
                        } catch (IndexOutOfBoundsException e) {
                            MtkCatLog.d("CAT", "retrieveNetworkAccessName: out of bounds");
                            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                        }
                    }
                    if (stkNetworkIdentifier != null && stkOperatorIdentifier != null) {
                        networkAccessName = stkNetworkIdentifier + "." + stkOperatorIdentifier;
                    } else if (stkNetworkIdentifier != null) {
                        networkAccessName = stkNetworkIdentifier;
                    }
                    MtkCatLog.d("CAT", "nw:" + stkNetworkIdentifier + ";" + stkOperatorIdentifier);
                } catch (IndexOutOfBoundsException e2) {
                    MtkCatLog.d("CAT", "retrieveNetworkAccessName: out of bounds");
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                }
            }
            return networkAccessName;
        } catch (IndexOutOfBoundsException e3) {
            MtkCatLog.d("CAT", "retrieveNetworkAccessName: out of bounds");
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    /* JADX INFO: Multiple debug info for r1v2 byte: [D('protocolType' int), D('valueIndex' int)] */
    static TransportProtocol retrieveTransportProtocol(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int valueIndex2 = valueIndex + 1;
        try {
            return new TransportProtocol(rawValue[valueIndex], ((rawValue[valueIndex2] & PplMessageManager.Type.INVALID) << 8) + (rawValue[valueIndex2 + 1] & PplMessageManager.Type.INVALID));
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.d("CAT", "retrieveTransportProtocol: out of bounds");
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static OtherAddress retrieveOtherAddress(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int valueIndex2 = valueIndex + 1;
        try {
            byte b = rawValue[valueIndex];
            if (33 == b) {
                return new OtherAddress(b, rawValue, valueIndex2);
            }
            if (87 == b) {
                return new OtherAddress(b, rawValue, valueIndex2);
            }
            return null;
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.d("CAT", "retrieveOtherAddress: out of bounds");
            return null;
        } catch (UnknownHostException e2) {
            MtkCatLog.d("CAT", "retrieveOtherAddress: unknown host");
            return null;
        }
    }

    static int retrieveChannelDataLength(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        MtkCatLog.d("CAT", "valueIndex:" + valueIndex);
        try {
            return rawValue[valueIndex] & 255;
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.d("CAT", "retrieveTransportProtocol: out of bounds");
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static byte[] retrieveChannelData(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        try {
            byte[] channelData = new byte[ctlv.getLength()];
            System.arraycopy(rawValue, valueIndex, channelData, 0, channelData.length);
            return channelData;
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.d("CAT", "retrieveChannelData: out of bounds");
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
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
}
