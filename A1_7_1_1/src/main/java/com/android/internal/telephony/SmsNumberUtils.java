package com.android.internal.telephony;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.HbpcdLookup.MccIdd;
import com.android.internal.telephony.HbpcdLookup.MccLookup;
import java.util.ArrayList;
import java.util.HashMap;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SmsNumberUtils {
    private static int[] ALL_COUNTRY_CODES = null;
    private static final int CDMA_HOME_NETWORK = 1;
    private static final int CDMA_ROAMING_NETWORK = 2;
    private static final boolean DBG = false;
    private static final int GSM_UMTS_NETWORK = 0;
    private static HashMap<String, ArrayList<String>> IDDS_MAPS = null;
    private static int MAX_COUNTRY_CODES_LENGTH = 0;
    private static final int MIN_COUNTRY_AREA_LOCAL_LENGTH = 10;
    private static final int NANP_CC = 1;
    private static final String NANP_IDD = "011";
    private static final int NANP_LONG_LENGTH = 11;
    private static final int NANP_MEDIUM_LENGTH = 10;
    private static final String NANP_NDD = "1";
    private static final int NANP_SHORT_LENGTH = 7;
    private static final int NP_CC_AREA_LOCAL = 104;
    private static final int NP_HOMEIDD_CC_AREA_LOCAL = 101;
    private static final int NP_INTERNATIONAL_BEGIN = 100;
    private static final int NP_LOCALIDD_CC_AREA_LOCAL = 103;
    private static final int NP_NANP_AREA_LOCAL = 2;
    private static final int NP_NANP_BEGIN = 1;
    private static final int NP_NANP_LOCAL = 1;
    private static final int NP_NANP_LOCALIDD_CC_AREA_LOCAL = 5;
    private static final int NP_NANP_NBPCD_CC_AREA_LOCAL = 4;
    private static final int NP_NANP_NBPCD_HOMEIDD_CC_AREA_LOCAL = 6;
    private static final int NP_NANP_NDD_AREA_LOCAL = 3;
    private static final int NP_NBPCD_CC_AREA_LOCAL = 102;
    private static final int NP_NBPCD_HOMEIDD_CC_AREA_LOCAL = 100;
    private static final int NP_NONE = 0;
    private static final String PLUS_SIGN = "+";
    private static final String TAG = "SmsNumberUtils";

    private static class NumberEntry {
        public String IDD;
        public int countryCode;
        public String number;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SmsNumberUtils.NumberEntry.<init>(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public NumberEntry(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SmsNumberUtils.NumberEntry.<init>(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SmsNumberUtils.NumberEntry.<init>(java.lang.String):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.SmsNumberUtils.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.SmsNumberUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SmsNumberUtils.<clinit>():void");
    }

    private static String formatNumber(Context context, String number, String activeMcc, int networkType) {
        if (number == null) {
            throw new IllegalArgumentException("number is null");
        } else if (activeMcc == null || activeMcc.trim().length() == 0) {
            throw new IllegalArgumentException("activeMcc is null or empty!");
        } else {
            String networkPortionNumber = PhoneNumberUtils.extractNetworkPortion(number);
            if (networkPortionNumber == null || networkPortionNumber.length() == 0) {
                throw new IllegalArgumentException("Number is invalid!");
            }
            NumberEntry numberEntry = new NumberEntry(networkPortionNumber);
            ArrayList<String> allIDDs = getAllIDDs(context, activeMcc);
            int nanpState = checkNANP(numberEntry, allIDDs);
            if (DBG) {
                Rlog.d(TAG, "NANP type: " + getNumberPlanType(nanpState));
            }
            if (nanpState == 1 || nanpState == 2 || nanpState == 3) {
                return networkPortionNumber;
            }
            if (nanpState != 4) {
                if (nanpState == 5) {
                    if (networkType == 1) {
                        return networkPortionNumber;
                    }
                    if (networkType == 0) {
                        return PLUS_SIGN + networkPortionNumber.substring(numberEntry.IDD != null ? numberEntry.IDD.length() : 0);
                    } else if (networkType == 2) {
                        return networkPortionNumber.substring(numberEntry.IDD != null ? numberEntry.IDD.length() : 0);
                    }
                }
                int internationalState = checkInternationalNumberPlan(context, numberEntry, allIDDs, NANP_IDD);
                if (DBG) {
                    Rlog.d(TAG, "International type: " + getNumberPlanType(internationalState));
                }
                String returnNumber = null;
                switch (internationalState) {
                    case 100:
                        if (networkType == 0) {
                            returnNumber = networkPortionNumber.substring(1);
                            break;
                        }
                        break;
                    case 101:
                        returnNumber = networkPortionNumber;
                        break;
                    case 102:
                        returnNumber = NANP_IDD + networkPortionNumber.substring(1);
                        break;
                    case 103:
                        if (networkType == 0 || networkType == 2) {
                            returnNumber = NANP_IDD + networkPortionNumber.substring(numberEntry.IDD != null ? numberEntry.IDD.length() : 0);
                            break;
                        }
                    case 104:
                        int countryCode = numberEntry.countryCode;
                        if (!(inExceptionListForNpCcAreaLocal(numberEntry) || networkPortionNumber.length() < 11 || countryCode == 1)) {
                            returnNumber = NANP_IDD + networkPortionNumber;
                            break;
                        }
                    default:
                        if (networkPortionNumber.startsWith(PLUS_SIGN) && (networkType == 1 || networkType == 2)) {
                            if (!networkPortionNumber.startsWith("+011")) {
                                returnNumber = NANP_IDD + networkPortionNumber.substring(1);
                                break;
                            }
                            returnNumber = networkPortionNumber.substring(1);
                            break;
                        }
                }
                if (returnNumber == null) {
                    returnNumber = networkPortionNumber;
                }
                return returnNumber;
            } else if (networkType == 1 || networkType == 2) {
                return networkPortionNumber.substring(1);
            } else {
                return networkPortionNumber;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0069  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static ArrayList<String> getAllIDDs(Context context, String mcc) {
        ArrayList<String> allIDDs = (ArrayList) IDDS_MAPS.get(mcc);
        if (allIDDs != null) {
            return allIDDs;
        }
        allIDDs = new ArrayList();
        String[] projection = new String[2];
        projection[0] = MccIdd.IDD;
        projection[1] = "MCC";
        String where = null;
        String[] strArr = null;
        if (mcc != null) {
            where = "MCC=?";
            strArr = new String[1];
            strArr[0] = mcc;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MccIdd.CONTENT_URI, projection, where, strArr, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String idd = cursor.getString(0);
                    if (!allIDDs.contains(idd)) {
                        allIDDs.add(idd);
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            Rlog.e(TAG, "Can't access HbpcdLookup database", e);
            IDDS_MAPS.put(mcc, allIDDs);
            if (DBG) {
            }
            return allIDDs;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        IDDS_MAPS.put(mcc, allIDDs);
        if (DBG) {
            Rlog.d(TAG, "MCC = " + mcc + ", all IDDs = " + allIDDs);
        }
        return allIDDs;
    }

    private static int checkNANP(NumberEntry numberEntry, ArrayList<String> allIDDs) {
        boolean isNANP = false;
        String number = numberEntry.number;
        if (number.length() == 7) {
            char firstChar = number.charAt(0);
            if (firstChar >= '2' && firstChar <= '9') {
                isNANP = true;
                for (int i = 1; i < 7; i++) {
                    if (!PhoneNumberUtils.isISODigit(number.charAt(i))) {
                        isNANP = false;
                        break;
                    }
                }
            }
            if (isNANP) {
                return 1;
            }
        } else if (number.length() == 10) {
            if (isNANP(number)) {
                return 2;
            }
        } else if (number.length() == 11) {
            if (isNANP(number)) {
                return 3;
            }
        } else if (number.startsWith(PLUS_SIGN)) {
            number = number.substring(1);
            if (number.length() == 11) {
                if (isNANP(number)) {
                    return 4;
                }
            } else if (number.startsWith(NANP_IDD) && number.length() == 14 && isNANP(number.substring(3))) {
                return 6;
            }
        } else {
            for (String idd : allIDDs) {
                if (number.startsWith(idd)) {
                    String number2 = number.substring(idd.length());
                    if (number2 != null && number2.startsWith(String.valueOf(1)) && isNANP(number2)) {
                        numberEntry.IDD = idd;
                        return 5;
                    }
                }
            }
        }
        return 0;
    }

    private static boolean isNANP(String number) {
        if (number.length() != 10 && (number.length() != 11 || !number.startsWith("1"))) {
            return false;
        }
        if (number.length() == 11) {
            number = number.substring(1);
        }
        return PhoneNumberUtils.isNanp(number);
    }

    private static int checkInternationalNumberPlan(Context context, NumberEntry numberEntry, ArrayList<String> allIDDs, String homeIDD) {
        String number = numberEntry.number;
        int countryCode;
        if (number.startsWith(PLUS_SIGN)) {
            String numberNoNBPCD = number.substring(1);
            if (numberNoNBPCD.startsWith(homeIDD)) {
                countryCode = getCountryCode(context, numberNoNBPCD.substring(homeIDD.length()));
                if (countryCode > 0) {
                    numberEntry.countryCode = countryCode;
                    return 100;
                }
            }
            countryCode = getCountryCode(context, numberNoNBPCD);
            if (countryCode > 0) {
                numberEntry.countryCode = countryCode;
                return 102;
            }
        } else if (number.startsWith(homeIDD)) {
            countryCode = getCountryCode(context, number.substring(homeIDD.length()));
            if (countryCode > 0) {
                numberEntry.countryCode = countryCode;
                return 101;
            }
        } else {
            for (String exitCode : allIDDs) {
                if (number.startsWith(exitCode)) {
                    countryCode = getCountryCode(context, number.substring(exitCode.length()));
                    if (countryCode > 0) {
                        numberEntry.countryCode = countryCode;
                        numberEntry.IDD = exitCode;
                        return 103;
                    }
                }
            }
            if (!number.startsWith("0")) {
                countryCode = getCountryCode(context, number);
                if (countryCode > 0) {
                    numberEntry.countryCode = countryCode;
                    return 104;
                }
            }
        }
        return 0;
    }

    private static int getCountryCode(Context context, String number) {
        if (number.length() >= 10) {
            int[] allCCs = getAllCountryCodes(context);
            if (allCCs == null) {
                return -1;
            }
            int i;
            int[] ccArray = new int[MAX_COUNTRY_CODES_LENGTH];
            for (i = 0; i < MAX_COUNTRY_CODES_LENGTH; i++) {
                ccArray[i] = Integer.parseInt(number.substring(0, i + 1));
            }
            for (int tempCC : allCCs) {
                for (int j = 0; j < MAX_COUNTRY_CODES_LENGTH; j++) {
                    if (tempCC == ccArray[j]) {
                        if (DBG) {
                            Rlog.d(TAG, "Country code = " + tempCC);
                        }
                        return tempCC;
                    }
                }
            }
        }
        return -1;
    }

    private static int[] getAllCountryCodes(Context context) {
        if (ALL_COUNTRY_CODES != null) {
            return ALL_COUNTRY_CODES;
        }
        Cursor cursor = null;
        try {
            String[] projection = new String[1];
            projection[0] = MccLookup.COUNTRY_CODE;
            cursor = context.getContentResolver().query(MccLookup.CONTENT_URI, projection, null, null, null);
            if (cursor.getCount() > 0) {
                ALL_COUNTRY_CODES = new int[cursor.getCount()];
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (!cursor.moveToNext()) {
                        break;
                    }
                    int countryCode = cursor.getInt(0);
                    i = i2 + 1;
                    ALL_COUNTRY_CODES[i2] = countryCode;
                    int length = String.valueOf(countryCode).trim().length();
                    if (length > MAX_COUNTRY_CODES_LENGTH) {
                        MAX_COUNTRY_CODES_LENGTH = length;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            Rlog.e(TAG, "Can't access HbpcdLookup database", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ALL_COUNTRY_CODES;
    }

    private static boolean inExceptionListForNpCcAreaLocal(NumberEntry numberEntry) {
        int countryCode = numberEntry.countryCode;
        if (numberEntry.number.length() != 12) {
            return false;
        }
        if (countryCode == 7 || countryCode == 20 || countryCode == 65 || countryCode == 90) {
            return true;
        }
        return false;
    }

    private static String getNumberPlanType(int state) {
        String numberPlanType = "Number Plan type (" + state + "): ";
        if (state == 1) {
            return "NP_NANP_LOCAL";
        }
        if (state == 2) {
            return "NP_NANP_AREA_LOCAL";
        }
        if (state == 3) {
            return "NP_NANP_NDD_AREA_LOCAL";
        }
        if (state == 4) {
            return "NP_NANP_NBPCD_CC_AREA_LOCAL";
        }
        if (state == 5) {
            return "NP_NANP_LOCALIDD_CC_AREA_LOCAL";
        }
        if (state == 6) {
            return "NP_NANP_NBPCD_HOMEIDD_CC_AREA_LOCAL";
        }
        if (state == 100) {
            return "NP_NBPCD_HOMEIDD_CC_AREA_LOCAL";
        }
        if (state == 101) {
            return "NP_HOMEIDD_CC_AREA_LOCAL";
        }
        if (state == 102) {
            return "NP_NBPCD_CC_AREA_LOCAL";
        }
        if (state == 103) {
            return "NP_LOCALIDD_CC_AREA_LOCAL";
        }
        if (state == 104) {
            return "NP_CC_AREA_LOCAL";
        }
        return "Unknown type";
    }

    public static String filterDestAddr(Phone phone, String destAddr) {
        if (DBG) {
            Rlog.d(TAG, "enter filterDestAddr. destAddr=\"" + destAddr + "\"");
        }
        if (destAddr == null || !PhoneNumberUtils.isGlobalPhoneNumber(destAddr)) {
            Rlog.w(TAG, "destAddr" + destAddr + " is not a global phone number! Nothing changed.");
            return destAddr;
        }
        String networkOperator = TelephonyManager.from(phone.getContext()).getNetworkOperator(phone.getSubId());
        String result = null;
        if (needToConvert(phone)) {
            int networkType = getNetworkType(phone);
            if (!(networkType == -1 || TextUtils.isEmpty(networkOperator))) {
                String networkMcc = networkOperator.substring(0, 3);
                if (networkMcc != null && networkMcc.trim().length() > 0) {
                    result = formatNumber(phone.getContext(), destAddr, networkMcc, networkType);
                }
            }
        }
        if (DBG) {
            String str;
            Rlog.d(TAG, "destAddr is " + (result != null ? "formatted." : "not formatted."));
            String str2 = TAG;
            StringBuilder append = new StringBuilder().append("leave filterDestAddr, new destAddr=\"");
            if (result != null) {
                str = result;
            } else {
                str = destAddr;
            }
            Rlog.d(str2, append.append(str).append("\"").toString());
        }
        if (result == null) {
            result = destAddr;
        }
        return result;
    }

    private static int getNetworkType(Phone phone) {
        int phoneType = phone.getPhoneType();
        if (phoneType == 1) {
            return 0;
        }
        if (phoneType == 2) {
            if (isInternationalRoaming(phone)) {
                return 2;
            }
            return 1;
        } else if (!DBG) {
            return -1;
        } else {
            Rlog.w(TAG, "warning! unknown mPhoneType value=" + phoneType);
            return -1;
        }
    }

    private static boolean isInternationalRoaming(Phone phone) {
        boolean internationalRoaming;
        boolean z = false;
        String operatorIsoCountry = TelephonyManager.from(phone.getContext()).getNetworkCountryIsoForPhone(phone.getPhoneId());
        String simIsoCountry = TelephonyManager.from(phone.getContext()).getSimCountryIsoForPhone(phone.getPhoneId());
        if (TextUtils.isEmpty(operatorIsoCountry) || TextUtils.isEmpty(simIsoCountry)) {
            internationalRoaming = false;
        } else {
            if (!simIsoCountry.equals(operatorIsoCountry)) {
                z = true;
            }
            internationalRoaming = z;
        }
        if (!internationalRoaming) {
            return internationalRoaming;
        }
        if ("us".equals(simIsoCountry)) {
            return !"vi".equals(operatorIsoCountry);
        } else {
            if ("vi".equals(simIsoCountry)) {
                return !"us".equals(operatorIsoCountry);
            } else {
                return internationalRoaming;
            }
        }
    }

    private static boolean needToConvert(Phone phone) {
        boolean z = false;
        String[] listArray = phone.getContext().getResources().getStringArray(17236041);
        if (listArray == null || listArray.length <= 0) {
            return false;
        }
        for (int i = 0; i < listArray.length; i++) {
            if (!TextUtils.isEmpty(listArray[i])) {
                String[] needToConvertArray = listArray[i].split(";");
                if (needToConvertArray != null && needToConvertArray.length > 0) {
                    if (needToConvertArray.length == 1) {
                        z = "true".equalsIgnoreCase(needToConvertArray[0]);
                    } else if (needToConvertArray.length == 2 && !TextUtils.isEmpty(needToConvertArray[1]) && compareGid1(phone, needToConvertArray[1])) {
                        return "true".equalsIgnoreCase(needToConvertArray[0]);
                    }
                }
            }
        }
        return z;
    }

    private static boolean compareGid1(Phone phone, String serviceGid1) {
        int i = 0;
        String gid1 = phone.getGroupIdLevel1();
        boolean ret = true;
        if (TextUtils.isEmpty(serviceGid1)) {
            if (DBG) {
                Rlog.d(TAG, "compareGid1 serviceGid is empty, return " + true);
            }
            return true;
        }
        int gid_length = serviceGid1.length();
        if (gid1 != null && gid1.length() >= gid_length) {
            i = gid1.substring(0, gid_length).equalsIgnoreCase(serviceGid1);
        }
        if (i == 0) {
            if (DBG) {
                Rlog.d(TAG, " gid1 " + gid1 + " serviceGid1 " + serviceGid1);
            }
            ret = false;
        }
        if (DBG) {
            Rlog.d(TAG, "compareGid1 is " + (ret ? "Same" : "Different"));
        }
        return ret;
    }
}
