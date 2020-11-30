package com.mediatek.internal.telephony.cdma.pluscode;

public interface IPlusCodeUtils {
    public static final String PROPERTY_ICC_CDMA_OPERATOR_MCC = "vendor.cdma.icc.operator.mcc";
    public static final String PROPERTY_TIME_LTMOFFSET = "vendor.cdma.operator.ltmoffset";

    boolean canFormatPlusCodeForSms();

    boolean canFormatPlusToIddNdd();

    String checkMccBySidLtmOff(String str);

    String removeIddNddAddPlusCode(String str);

    String removeIddNddAddPlusCodeForSms(String str);

    String replacePlusCodeForSms(String str);

    String replacePlusCodeWithIddNdd(String str);
}
