package com.mediatek.android.mms.pdu;

import android.util.Log;
import com.google.android.mms.pdu.CharacterSets;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class MtkCharacterSets extends CharacterSets {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int BIG5_HKSCS = 2101;
    public static final int BOCU_1 = 1020;
    public static final int CESU_8 = 1016;
    public static final int CP864 = 2051;
    public static final int EUC_JP = 18;
    public static final int EUC_KR = 38;
    public static final int GB18030 = 114;
    public static final int GBK = 113;
    public static final int GB_2312 = 2025;
    public static final int HZ_GB_2312 = 2085;
    public static final int ISO_2022_CN = 104;
    public static final int ISO_2022_CN_EXT = 105;
    public static final int ISO_2022_JP = 39;
    public static final int ISO_2022_KR = 37;
    public static final int ISO_8859_10 = 13;
    public static final int ISO_8859_13 = 109;
    public static final int ISO_8859_14 = 110;
    public static final int ISO_8859_15 = 111;
    public static final int ISO_8859_16 = 112;
    public static final int KOI8_R = 2084;
    public static final int KOI8_U = 2088;
    private static final String LOG_TAG = "CharacterSets";
    public static final int MACINTOSH = 2027;
    private static final int[] MIBENUM_NUMBERS_EXTENDS = {BIG5_HKSCS, BOCU_1, CESU_8, CP864, 18, 38, GB18030, GBK, HZ_GB_2312, GB_2312, ISO_2022_CN, ISO_2022_CN_EXT, 39, 37, 13, 109, 110, 111, ISO_8859_16, KOI8_R, KOI8_U, MACINTOSH, SCSU, TIS_620, UTF_16BE, UTF_16LE, UTF_32, UTF_32BE, UTF_32LE, UTF_7, WINDOWS_1250, WINDOWS_1251, WINDOWS_1252, WINDOWS_1253, WINDOWS_1254, WINDOWS_1255, WINDOWS_1256, WINDOWS_1257, WINDOWS_1258};
    protected static final HashMap<Integer, String> MIBENUM_TO_NAME_MAP_EXTENDS = new HashMap<>();
    public static final String MIMENAME_BIG5_HKSCS = "Big5-HKSCS";
    public static final String MIMENAME_BOCU_1 = "BOCU-1";
    public static final String MIMENAME_CESU_8 = "CESU-8";
    public static final String MIMENAME_CP864 = "cp864";
    public static final String MIMENAME_EUC_JP = "EUC-JP";
    public static final String MIMENAME_EUC_KR = "EUC-KR";
    public static final String MIMENAME_GB18030 = "GB18030";
    public static final String MIMENAME_GBK = "GBK";
    public static final String MIMENAME_GB_2312 = "GB2312";
    public static final String MIMENAME_HZ_GB_2312 = "HZ-GB-2312";
    public static final String MIMENAME_ISO_2022_CN = "ISO-2022-CN";
    public static final String MIMENAME_ISO_2022_CN_EXT = "ISO-2022-CN-EXT";
    public static final String MIMENAME_ISO_2022_JP = "ISO-2022-JP";
    public static final String MIMENAME_ISO_2022_KR = "ISO-2022-KR";
    public static final String MIMENAME_ISO_8859_10 = "ISO-8859-10";
    public static final String MIMENAME_ISO_8859_13 = "ISO-8859-13";
    public static final String MIMENAME_ISO_8859_14 = "ISO-8859-14";
    public static final String MIMENAME_ISO_8859_15 = "ISO-8859-15";
    public static final String MIMENAME_ISO_8859_16 = "ISO-8859-16";
    public static final String MIMENAME_KOI8_R = "KOI8-R";
    public static final String MIMENAME_KOI8_U = "KOI8-U";
    public static final String MIMENAME_MACINTOSH = "macintosh";
    public static final String MIMENAME_SCSU = "SCSU";
    public static final String MIMENAME_TIS_620 = "TIS-620";
    public static final String MIMENAME_UTF_16BE = "UTF-16BE";
    public static final String MIMENAME_UTF_16LE = "UTF-16LE";
    public static final String MIMENAME_UTF_32 = "UTF-32";
    public static final String MIMENAME_UTF_32BE = "UTF-32BE";
    public static final String MIMENAME_UTF_32LE = "UTF-32LE";
    public static final String MIMENAME_UTF_7 = "UTF-7";
    public static final String MIMENAME_WINDOWS_1250 = "windows-1250";
    public static final String MIMENAME_WINDOWS_1251 = "windows-1251";
    public static final String MIMENAME_WINDOWS_1252 = "windows-1252";
    public static final String MIMENAME_WINDOWS_1253 = "windows-1253";
    public static final String MIMENAME_WINDOWS_1254 = "windows-1254";
    public static final String MIMENAME_WINDOWS_1255 = "windows-1255";
    public static final String MIMENAME_WINDOWS_1256 = "windows-1256";
    public static final String MIMENAME_WINDOWS_1257 = "windows-1257";
    public static final String MIMENAME_WINDOWS_1258 = "windows-1258";
    private static final String[] MIME_NAMES_EXTENDS = {MIMENAME_BIG5_HKSCS, MIMENAME_BOCU_1, MIMENAME_CESU_8, MIMENAME_CP864, MIMENAME_EUC_JP, MIMENAME_EUC_KR, MIMENAME_GB18030, MIMENAME_GBK, MIMENAME_HZ_GB_2312, MIMENAME_GB_2312, MIMENAME_ISO_2022_CN, MIMENAME_ISO_2022_CN_EXT, MIMENAME_ISO_2022_JP, MIMENAME_ISO_2022_KR, MIMENAME_ISO_8859_10, MIMENAME_ISO_8859_13, MIMENAME_ISO_8859_14, MIMENAME_ISO_8859_15, MIMENAME_ISO_8859_16, MIMENAME_KOI8_R, MIMENAME_KOI8_U, MIMENAME_MACINTOSH, MIMENAME_SCSU, MIMENAME_TIS_620, MIMENAME_UTF_16BE, MIMENAME_UTF_16LE, MIMENAME_UTF_32, MIMENAME_UTF_32BE, MIMENAME_UTF_32LE, MIMENAME_UTF_7, MIMENAME_WINDOWS_1250, MIMENAME_WINDOWS_1251, MIMENAME_WINDOWS_1252, MIMENAME_WINDOWS_1253, MIMENAME_WINDOWS_1254, MIMENAME_WINDOWS_1255, MIMENAME_WINDOWS_1256, MIMENAME_WINDOWS_1257, MIMENAME_WINDOWS_1258};
    protected static final HashMap<String, Integer> NAME_TO_MIBENUM_MAP_EXTENDS = new HashMap<>();
    public static final int SCSU = 1011;
    public static final int TIS_620 = 2259;
    public static final int UTF_16BE = 1013;
    public static final int UTF_16LE = 1014;
    public static final int UTF_32 = 1017;
    public static final int UTF_32BE = 1018;
    public static final int UTF_32LE = 1019;
    public static final int UTF_7 = 1012;
    public static final int WINDOWS_1250 = 2250;
    public static final int WINDOWS_1251 = 2251;
    public static final int WINDOWS_1252 = 2252;
    public static final int WINDOWS_1253 = 2253;
    public static final int WINDOWS_1254 = 2254;
    public static final int WINDOWS_1255 = 2255;
    public static final int WINDOWS_1256 = 2256;
    public static final int WINDOWS_1257 = 2257;
    public static final int WINDOWS_1258 = 2258;

    static {
        int len = MIBENUM_NUMBERS_EXTENDS.length - 1;
        for (int i = 0; i <= len; i++) {
            MIBENUM_TO_NAME_MAP_EXTENDS.put(Integer.valueOf(MIBENUM_NUMBERS_EXTENDS[i]), MIME_NAMES_EXTENDS[i]);
            NAME_TO_MIBENUM_MAP_EXTENDS.put(MIME_NAMES_EXTENDS[i], Integer.valueOf(MIBENUM_NUMBERS_EXTENDS[i]));
        }
    }

    private MtkCharacterSets() {
    }

    public static String getMimeName(int mibEnumValue) {
        String name = (String) MIBENUM_TO_NAME_MAP.get(Integer.valueOf(mibEnumValue));
        if (name == null && (name = MIBENUM_TO_NAME_MAP_EXTENDS.get(Integer.valueOf(mibEnumValue))) == null) {
            return "utf-8";
        }
        return name;
    }

    public static int getMibEnumValue(String mimeName) throws UnsupportedEncodingException {
        if (mimeName == null) {
            return -1;
        }
        String mimeName2 = mimeName.toLowerCase();
        Integer mibEnumValue = (Integer) CharacterSets.NAME_TO_MIBENUM_MAP.get(mimeName2);
        if (mibEnumValue != null) {
            return mibEnumValue.intValue();
        }
        Log.i(LOG_TAG, "getMibEnumValue failed, mimeName is: " + mimeName2);
        throw new UnsupportedEncodingException();
    }
}
