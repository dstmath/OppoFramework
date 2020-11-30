package com.android.internal.telephony.util;

import android.content.Context;
import android.os.SystemProperties;
import android.telecom.Log;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.ArrayList;
import java.util.List;

public class OppoVdfLocaleUpdateUtils {
    private static final String ALTICE_OPE_VERSION = "ALTICE";
    private static final String DE_COUNTRY_CODE = "de";
    private static final String[] DE_VDF_IMSI_EXCLUSION = {"262021915688022-262021915689721", "262021916728922-262021916753921", "262021916844422-262021916869421", "262021505517152-262021505518151", "262021605721605-262021605722104", "262021913520022-262021914085021", "262021914583022-262021914843021", "262021704060033-262021704075032", "262021705178075-262021705185574", "262021706006477-262021706046476"};
    private static final String[] DE_VDF_NUMERIC = {"26202", "26209"};
    private static final String[] DE_VDF_SPN = new String[0];
    private static final String[] DT_NUMERIC = {"26201"};
    private static final String EMPTY_VERSION = "";
    private static final String ES_COUNTRY_CODE = "es";
    private static final String[] ES_VDF_NUMERIC = {"21401"};
    private static final String[] ES_VDF_SPN = new String[0];
    private static final String EU_REGION_VERSION = "EUEX";
    private static final String GB_COUNTRY_CODE = "gb";
    private static final String[] GB_VDF_NUMERIC = {"23415", "23491"};
    private static final String[] GB_VDF_SPN = new String[0];
    private static final String IT_COUNTRY_CODE = "it";
    private static final String[] IT_VDF_NUMERIC = {"22210"};
    private static final String[] IT_VDF_SPN = new String[0];
    private static final String KEY_OPERATOR = "ro.oppo.operator";
    private static final String KEY_OPE_EXTRA = "operator";
    private static final String KEY_REGION = "ro.oppo.regionmark";
    private static final String KEY_REG_EXTRA = "region";
    private static final int MCC_LENGTH = 3;
    private static final String PT_COUNTRY_CODE = "pt";
    private static final String[] PT_VDF_NUMERIC = {"26801"};
    private static final String[] PT_VDF_SPN = new String[0];
    private static final String REGION_DE = "DE";
    private static final String RO_COUNTRY_CODE = "ro";
    private static final String[] RO_VDF_NUMERIC = {"22601"};
    private static final String[] RO_VDF_SPN = {"Vodafone RO"};
    private static final String TAG = "OppoVdfLocaleUpdateUtils";
    private static final String VDF_VERSION_EEA = "VODAFONE_EEA";
    private static final String VDF_VERSION_NONEEA = "VODAFONE_NONEEA";
    public static final String VODAFONE = "VODAFONE";
    private static boolean mIsVDFSim1Loaded = false;
    private static boolean mIsVDFSim2Loaded = false;
    private Context mContext;
    private TelephonyManager mTelephonyManager;
    private String mVdfMccmnc = "";

    public String getVdfMccmncIfExist(Context context, int slotId) {
        Log.i(TAG, " getVdfMccmncifExist  -  entry  :  slotId = " + slotId, new Object[0]);
        this.mVdfMccmnc = "";
        ProcessSetVdfMccmnc(context, slotId);
        return this.mVdfMccmnc;
    }

    private void ProcessSetVdfMccmnc(Context context, int slotId) {
        Log.i(TAG, " ProcessSetVdfMccmnc  -  entry  :  slotId = " + slotId, new Object[0]);
        if (context == null) {
            Log.i(TAG, "context is null!", new Object[0]);
            return;
        }
        this.mContext = context;
        if (!OppoLocaleChangedUtils.isVdfNeedUpdateLocale(context)) {
            Log.i(TAG, " Provisioned , not need updateLocale ", new Object[0]);
            return;
        }
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        List<SubscriptionInfo> sil = getActualSubInfoList();
        if (sil == null || sil.size() <= 0) {
            Log.i(TAG, "no actual sim card, return", new Object[0]);
            return;
        }
        Log.i(TAG, "actual sil size = " + sil.size() + " state = " + this.mTelephonyManager.getSimState(slotId), new Object[0]);
        String opeVersion = SystemProperties.get(KEY_OPERATOR, "");
        String regionVersion = SystemProperties.get(KEY_REGION, "");
        Log.d(TAG, "opeVersion = " + opeVersion + " regionVersion = " + regionVersion, new Object[0]);
        String upperCase = opeVersion.toUpperCase();
        char c = 65535;
        int hashCode = upperCase.hashCode();
        if (hashCode != -1563094019) {
            if (hashCode == 1423184408 && upperCase.equals(VDF_VERSION_EEA)) {
                c = 0;
            }
        } else if (upperCase.equals(VDF_VERSION_NONEEA)) {
            c = 1;
        }
        if (c != 0) {
            if (c != 1) {
                Log.d(TAG, "version not match", new Object[0]);
            } else if (TextUtils.isEmpty(regionVersion)) {
                handleVDFCase(slotId);
            }
        } else if (EU_REGION_VERSION.equalsIgnoreCase(regionVersion)) {
            handleVDFCase(slotId);
        }
    }

    private List<SubscriptionInfo> getActualSubInfoList() {
        List<SubscriptionInfo> sil = ((SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service")).getActiveSubscriptionInfoList();
        if (sil != null && sil.size() > 0) {
            return sil;
        }
        Log.i(TAG, "empty sil list", new Object[0]);
        return new ArrayList();
    }

    private void setVdfMccmnc(Context context, int phoneId) {
        this.mVdfMccmnc = ((TelephonyManager) context.getSystemService("phone")).getSimOperatorNumericForPhone(phoneId);
    }

    private void handleVDFCase(int slotId) {
        if (slotId == 0) {
            mIsVDFSim1Loaded = true;
            Log.d(TAG, "Set mIsVDFSim1Loaded : " + mIsVDFSim1Loaded, new Object[0]);
        } else if (slotId == 1) {
            mIsVDFSim2Loaded = true;
            Log.d(TAG, "Set mIsVDFSim2Loaded : " + mIsVDFSim2Loaded, new Object[0]);
        }
        List<SubscriptionInfo> sil = getActualSubInfoList();
        if (sil.size() == 1) {
            if (isVDFSim(this.mContext, sil.get(0).getSimSlotIndex())) {
                setVdfMccmnc(this.mContext, sil.get(0).getSimSlotIndex());
            }
        } else if (sil.size() == 2) {
            Log.d(TAG, "vdf case simState = " + this.mTelephonyManager.getSimState(0) + " : " + this.mTelephonyManager.getSimState(1), new Object[0]);
            if (this.mTelephonyManager.getSimState(0) != 5 || this.mTelephonyManager.getSimState(1) != 5 || TextUtils.isEmpty(this.mTelephonyManager.getSimOperatorNumericForPhone(0)) || TextUtils.isEmpty(this.mTelephonyManager.getSimOperatorNumericForPhone(1))) {
                Log.d(TAG, "VDF case not two sim ready, do nothing", new Object[0]);
                return;
            }
            Log.d(TAG, " mIsVDFSim1Loaded : " + mIsVDFSim1Loaded + "mIsVDFSim2Loaded : " + mIsVDFSim2Loaded, new Object[0]);
            if (!mIsVDFSim1Loaded || !mIsVDFSim2Loaded) {
                Log.d(TAG, "vdf two sim not all loaded", new Object[0]);
            } else if (isVDFSim(this.mContext, 0)) {
                setVdfMccmnc(this.mContext, 0);
            } else if (isVDFSim(this.mContext, 1)) {
                setVdfMccmnc(this.mContext, 1);
            }
        }
    }

    /* JADX INFO: Multiple debug info for r1v16 java.lang.String: [D('mccMncArray' java.lang.String[]), D('spnName' java.lang.String)] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00e5  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00f0  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00fc  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0152  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x015b  */
    private static boolean isVDFSim(Context context, int phoneId) {
        char c;
        String[] spnArray;
        String[] mccMncArray;
        boolean isDeVDFImsiMatch;
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            Log.i(TAG, "VDF not valid phoneId", new Object[0]);
            return false;
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService("telephony_subscription_service");
        String imsi = "";
        Phone phone = PhoneFactory.getPhone(phoneId);
        IccRecords iccRecords = null;
        if (phone != null) {
            imsi = phone.getSubscriberId();
            iccRecords = phone.getIccRecords();
        }
        String mccmnc = tm.getSimOperatorNumericForPhone(phoneId);
        String spn = iccRecords != null ? iccRecords.getServiceProviderName() : "";
        String countryCode = getCountryCodeForPhone(context, phoneId);
        boolean isSpnMatch = false;
        String[] strArr = new String[0];
        String[] mccMncArray2 = new String[0];
        int hashCode = countryCode.hashCode();
        if (hashCode != 3201) {
            if (hashCode != 3246) {
                if (hashCode != 3291) {
                    if (hashCode != 3371) {
                        if (hashCode != 3588) {
                            if (hashCode == 3645 && countryCode.equals(RO_COUNTRY_CODE)) {
                                c = 5;
                                if (c == 0) {
                                    mccMncArray = DE_VDF_NUMERIC;
                                    spnArray = DE_VDF_SPN;
                                } else if (c == 1) {
                                    mccMncArray = GB_VDF_NUMERIC;
                                    spnArray = GB_VDF_SPN;
                                } else if (c == 2) {
                                    mccMncArray = IT_VDF_NUMERIC;
                                    spnArray = IT_VDF_SPN;
                                } else if (c == 3) {
                                    mccMncArray = ES_VDF_NUMERIC;
                                    spnArray = ES_VDF_SPN;
                                } else if (c == 4) {
                                    mccMncArray = PT_VDF_NUMERIC;
                                    spnArray = PT_VDF_SPN;
                                } else if (c != 5) {
                                    Log.d(TAG, "country should not start vdf customize", new Object[0]);
                                    return false;
                                } else {
                                    mccMncArray = RO_VDF_NUMERIC;
                                    spnArray = RO_VDF_SPN;
                                }
                                if (TextUtils.isEmpty(mccmnc)) {
                                    Log.i(TAG, "get empty mccmnc", new Object[0]);
                                    return false;
                                }
                                boolean isMccMncMatch = false;
                                int i = 0;
                                for (int length = mccMncArray.length; i < length; length = length) {
                                    if (TextUtils.equals(mccMncArray[i], mccmnc)) {
                                        isMccMncMatch = true;
                                    }
                                    i++;
                                }
                                if (!isMccMncMatch) {
                                    Log.i(TAG, "mccmnc not match VDF", new Object[0]);
                                    return false;
                                }
                                if (spnArray != null) {
                                    if (spnArray.length != 0) {
                                        int length2 = spnArray.length;
                                        int i2 = 0;
                                        while (i2 < length2) {
                                            if (TextUtils.equals(spnArray[i2], spn)) {
                                                isSpnMatch = true;
                                            }
                                            i2++;
                                            mccMncArray = mccMncArray;
                                        }
                                        if (isSpnMatch) {
                                            Log.i(TAG, "spn not match VDF", new Object[0]);
                                            return false;
                                        }
                                        if (DE_COUNTRY_CODE.equals(countryCode)) {
                                            boolean isDeVDFImsiMatch2 = !isImsiMatch(imsi, DE_VDF_IMSI_EXCLUSION);
                                            Log.d(TAG, "isDeVDFImsiMatch = " + isDeVDFImsiMatch2, new Object[0]);
                                            isDeVDFImsiMatch = isMccMncMatch && isSpnMatch && isDeVDFImsiMatch2;
                                        } else {
                                            isDeVDFImsiMatch = isMccMncMatch && isSpnMatch;
                                        }
                                        Log.i(TAG, "isMccMncMatch = " + isMccMncMatch + " isSpnMatch = " + isSpnMatch + " isVDF = " + isDeVDFImsiMatch, new Object[0]);
                                        return isDeVDFImsiMatch;
                                    }
                                }
                                Log.d(TAG, "carrier require empty spn", new Object[0]);
                                if (TextUtils.isEmpty(spn)) {
                                    isSpnMatch = true;
                                }
                                if (isSpnMatch) {
                                }
                            }
                        } else if (countryCode.equals(PT_COUNTRY_CODE)) {
                            c = 4;
                            if (c == 0) {
                            }
                            if (TextUtils.isEmpty(mccmnc)) {
                            }
                        }
                    } else if (countryCode.equals(IT_COUNTRY_CODE)) {
                        c = 2;
                        if (c == 0) {
                        }
                        if (TextUtils.isEmpty(mccmnc)) {
                        }
                    }
                } else if (countryCode.equals(GB_COUNTRY_CODE)) {
                    c = 1;
                    if (c == 0) {
                    }
                    if (TextUtils.isEmpty(mccmnc)) {
                    }
                }
            } else if (countryCode.equals(ES_COUNTRY_CODE)) {
                c = 3;
                if (c == 0) {
                }
                if (TextUtils.isEmpty(mccmnc)) {
                }
            }
        } else if (countryCode.equals(DE_COUNTRY_CODE)) {
            c = 0;
            if (c == 0) {
            }
            if (TextUtils.isEmpty(mccmnc)) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
        if (TextUtils.isEmpty(mccmnc)) {
        }
    }

    private static boolean isImsiMatch(String imsi, String[] matchedImsiRanges) {
        String[] points;
        if (TextUtils.isEmpty(imsi)) {
            Log.d(TAG, "imsi is empty", new Object[0]);
            return false;
        } else if (matchedImsiRanges == null || matchedImsiRanges.length == 0) {
            Log.d(TAG, "matched imsi ranges is empty", new Object[0]);
            return false;
        } else {
            try {
                Long imsiL = Long.valueOf(Long.parseLong(imsi));
                for (String imsiRange : matchedImsiRanges) {
                    if (!(imsiRange == null || (points = imsiRange.split("-")) == null || points.length != 2)) {
                        long start = Long.parseLong(points[0]);
                        long end = Long.parseLong(points[1]);
                        if (imsiL.longValue() >= start && imsiL.longValue() <= end) {
                            Log.d(TAG, "imsi match in range", new Object[0]);
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "catch exception = " + e, new Object[0]);
            }
            return false;
        }
    }

    private static String getCountryCodeForPhone(Context context, int phoneId) {
        String mccmnc = ((TelephonyManager) context.getSystemService("phone")).getSimOperatorNumericForPhone(phoneId);
        String mcc = "";
        if (mccmnc != null && mccmnc.length() > 3) {
            mcc = mccmnc.substring(0, 3);
        }
        try {
            return MccTable.countryCodeForMcc(Integer.parseInt(mcc));
        } catch (NumberFormatException e) {
            return "";
        }
    }
}
