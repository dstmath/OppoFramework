package com.mediatek.internal.telephony.cdma.pluscode;

public interface IPlusCodeUtils {
    public static final String PROPERTY_ICC_CDMA_OPERATOR_MCC = "cdma.icc.operator.mcc";
    public static final String PROPERTY_NITZ_TIME_ZONE_ID = "cdma.operator.nitztimezoneid";
    public static final String PROPERTY_OPERATOR_MCC = "cdma.operator.mcc";
    public static final String PROPERTY_OPERATOR_SID = "cdma.operator.sid";
    public static final String PROPERTY_TIME_LTMOFFSET = "cdma.operator.ltmoffset";

    boolean canFormatPlusCodeForSms();

    boolean canFormatPlusToIddNdd();

    String checkMccBySidLtmOff(String str);

    String removeIddNddAddPlusCode(String str);

    String removeIddNddAddPlusCodeForSms(String str);

    String replacePlusCodeForSms(String str);

    String replacePlusCodeWithIddNdd(String str);
}
