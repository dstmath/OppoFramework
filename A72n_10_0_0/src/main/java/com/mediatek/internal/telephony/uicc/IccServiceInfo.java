package com.mediatek.internal.telephony.uicc;

public class IccServiceInfo {

    public enum IccServiceStatus {
        NOT_EXIST_IN_SIM,
        NOT_EXIST_IN_USIM,
        ACTIVATED,
        INACTIVATED,
        UNKNOWN
    }

    public enum IccService {
        CHV1_DISABLE_FUNCTION,
        SPN,
        PNN,
        OPL,
        MWIS,
        CFIS,
        SPDI,
        EPLMN,
        SMSP,
        FDN,
        PLMNsel,
        OPLMNwACT,
        UNSUPPORTED_SERVICE;

        public int getIndex() {
            switch (this) {
                case CHV1_DISABLE_FUNCTION:
                    return 0;
                case SPN:
                    return 1;
                case PNN:
                    return 2;
                case OPL:
                    return 3;
                case MWIS:
                    return 4;
                case CFIS:
                    return 5;
                case SPDI:
                    return 6;
                case EPLMN:
                    return 7;
                case SMSP:
                    return 8;
                case FDN:
                    return 9;
                case PLMNsel:
                    return 10;
                case OPLMNwACT:
                    return 11;
                case UNSUPPORTED_SERVICE:
                    return 10;
                default:
                    return -1;
            }
        }
    }
}
