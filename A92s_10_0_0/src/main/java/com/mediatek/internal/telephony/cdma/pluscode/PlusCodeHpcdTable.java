package com.mediatek.internal.telephony.cdma.pluscode;

import android.telephony.Rlog;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class PlusCodeHpcdTable {
    private static final boolean DBG = true;
    static final String LOG_TAG = "PlusCodeHpcdTable";
    private static final MccIddNddSid[] MccIddNddSidMap = TelephonyPlusCode.MCC_IDD_NDD_SID_MAP;
    private static final MccSidLtmOff[] MccSidLtmOffMap = TelephonyPlusCode.MCC_SID_LTM_OFF_MAP;
    static final int PARAM_FOR_OFFSET = 2;
    static final Object sInstSync = new Object();
    private static PlusCodeHpcdTable sInstance;

    public static PlusCodeHpcdTable getInstance() {
        synchronized (sInstSync) {
            if (sInstance == null) {
                sInstance = new PlusCodeHpcdTable();
            }
        }
        return sInstance;
    }

    private PlusCodeHpcdTable() {
    }

    public static MccIddNddSid getCcFromTableByMcc(String sMcc) {
        Rlog.d(LOG_TAG, " getCcFromTableByMcc mcc = " + sMcc);
        if (sMcc == null || sMcc.length() == 0) {
            Rlog.d(LOG_TAG, "[getCcFromTableByMcc] please check the param ");
            return null;
        }
        try {
            int mcc = Integer.parseInt(sMcc);
            int size = MccIddNddSidMap.length;
            Rlog.d(LOG_TAG, " getCcFromTableByMcc size = " + size);
            int find = -1;
            int i = 0;
            while (true) {
                if (i >= size) {
                    break;
                } else if (MccIddNddSidMap[i].getMcc() == mcc) {
                    find = i;
                    break;
                } else {
                    i++;
                }
            }
            Rlog.d(LOG_TAG, " getCcFromTableByMcc find = " + find);
            if (find <= -1 || find >= size) {
                Rlog.d(LOG_TAG, "can't find one that match the Mcc");
                return null;
            }
            MccIddNddSid mccIddNddSid = MccIddNddSidMap[find];
            Rlog.d(LOG_TAG, "Now find Mcc = " + mccIddNddSid.mMcc + ", Mcc = " + mccIddNddSid.mCc + ", SidMin = " + Rlog.pii(LOG_TAG, Integer.valueOf(mccIddNddSid.mSidMin)) + ", SidMax = " + Rlog.pii(LOG_TAG, Integer.valueOf(mccIddNddSid.mSidMax)) + ", Idd = " + mccIddNddSid.mIdd + ", Ndd = " + mccIddNddSid.mNdd);
            return mccIddNddSid;
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    public static ArrayList<String> getMccFromConflictTableBySid(String sSid) {
        Rlog.d(LOG_TAG, " [getMccFromConflictTableBySid] sid = " + Rlog.pii(LOG_TAG, sSid));
        if (sSid == null || sSid.length() == 0 || sSid.length() > 5) {
            Rlog.d(LOG_TAG, "[getMccFromConflictTableBySid] please check the param ");
            return null;
        }
        try {
            int sid = Integer.parseInt(sSid);
            if (sid < 0) {
                return null;
            }
            ArrayList<String> mccArrays = new ArrayList<>();
            int mccSidMapSize = MccSidLtmOffMap.length;
            Rlog.d(LOG_TAG, " [getMccFromConflictTableBySid] mccSidMapSize = " + mccSidMapSize);
            for (int i = 0; i < mccSidMapSize; i++) {
                MccSidLtmOff mccSidLtmOff = MccSidLtmOffMap[i];
                if (mccSidLtmOff != null && mccSidLtmOff.mSid == sid) {
                    mccArrays.add(Integer.toString(mccSidLtmOff.mMcc));
                    Rlog.d(LOG_TAG, "mccSidLtmOff  Mcc = " + mccSidLtmOff.mMcc + ", Sid = " + Rlog.pii(LOG_TAG, Integer.valueOf(mccSidLtmOff.mSid)) + ", LtmOffMin = " + mccSidLtmOff.mLtmOffMin + ", LtmOffMax = " + mccSidLtmOff.mLtmOffMax);
                }
            }
            return mccArrays;
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    public static MccIddNddSid getCcFromMINSTableBySid(String sSid) {
        Rlog.d(LOG_TAG, " [getCcFromMINSTableBySid] sid = " + Rlog.pii(LOG_TAG, sSid));
        if (sSid == null || sSid.length() == 0 || sSid.length() > 5) {
            Rlog.d(LOG_TAG, "[getCcFromMINSTableBySid] please check the param ");
            return null;
        }
        try {
            int sid = Integer.parseInt(sSid);
            if (sid < 0) {
                return null;
            }
            MccIddNddSid findMccIddNddSid = null;
            int size = MccIddNddSidMap.length;
            int i = 0;
            while (true) {
                if (i >= size) {
                    break;
                }
                MccIddNddSid mccIddNddSid = MccIddNddSidMap[i];
                if (sid <= mccIddNddSid.mSidMax && sid >= mccIddNddSid.mSidMin) {
                    findMccIddNddSid = mccIddNddSid;
                    break;
                }
                i++;
            }
            Rlog.d(LOG_TAG, " getCcFromMINSTableBySid findMccIddNddSid = " + Rlog.pii(LOG_TAG, findMccIddNddSid));
            return findMccIddNddSid;
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    public String getCcFromMINSTableByLTM(List<String> mccArray, String ltmOff) {
        String findMcc;
        MccSidLtmOff[] mccSidLtmOffArr;
        Rlog.d(LOG_TAG, " getCcFromMINSTableByLTM sLtm_off = " + ltmOff);
        if (ltmOff == null || ltmOff.length() == 0 || mccArray == null || mccArray.size() == 0) {
            Rlog.d(LOG_TAG, "[getCcFromMINSTableByLTM] please check the param ");
            return null;
        }
        try {
            int ltmoff = Integer.parseInt(ltmOff);
            Rlog.d(LOG_TAG, "[getCcFromMINSTableByLTM]  ltm_off =  " + ltmoff);
            int findOutMccSize = mccArray.size();
            if (findOutMccSize <= 1 || (mccSidLtmOffArr = MccSidLtmOffMap) == null) {
                findMcc = mccArray.get(0);
            } else {
                int mccSidMapSize = mccSidLtmOffArr.length;
                Rlog.d(LOG_TAG, " Conflict FindOutMccSize = " + findOutMccSize);
                int i = 0;
                findMcc = null;
                while (i < findOutMccSize) {
                    try {
                        int mcc = Integer.parseInt(mccArray.get(i));
                        Rlog.d(LOG_TAG, " Conflict mcc = " + mcc + ",index = " + i);
                        int j = 0;
                        while (true) {
                            if (j >= mccSidMapSize) {
                                break;
                            }
                            MccSidLtmOff mccSidLtmOff = MccSidLtmOffMap[j];
                            if (mccSidLtmOff.mMcc == mcc) {
                                int max = mccSidLtmOff.mLtmOffMax * PARAM_FOR_OFFSET;
                                int min = mccSidLtmOff.mLtmOffMin * PARAM_FOR_OFFSET;
                                Rlog.d(LOG_TAG, "mccSidLtmOff LtmOffMin = " + mccSidLtmOff.mLtmOffMin + ", LtmOffMax = " + mccSidLtmOff.mLtmOffMax);
                                if (ltmoff <= max && ltmoff >= min) {
                                    findMcc = mccArray.get(i);
                                    break;
                                }
                            }
                            j++;
                        }
                        i++;
                    } catch (NumberFormatException e) {
                        Rlog.e(LOG_TAG, Log.getStackTraceString(e));
                        return null;
                    }
                }
            }
            Rlog.d(LOG_TAG, "find one that match the ltm_off mcc = " + findMcc);
            return findMcc;
        } catch (NumberFormatException e2) {
            Rlog.e(LOG_TAG, Log.getStackTraceString(e2));
            return null;
        }
    }

    public static String getMccFromConflictTableBySidLtmOff(String sSid, String sLtmOff) {
        Rlog.d(LOG_TAG, " [getMccFromConflictTableBySidLtmOff] sSid = " + Rlog.pii(LOG_TAG, sSid) + ", sLtm_off = " + sLtmOff);
        if (sSid == null || sSid.length() == 0 || sSid.length() > 5 || sLtmOff == null || sLtmOff.length() == 0) {
            Rlog.d(LOG_TAG, "[getMccFromConflictTableBySidLtmOff] please check the param ");
            return null;
        }
        try {
            int sid = Integer.parseInt(sSid);
            if (sid < 0) {
                return null;
            }
            try {
                int ltmoff = Integer.parseInt(sLtmOff);
                Rlog.d(LOG_TAG, " [getMccFromConflictTableBySidLtmOff] sid = " + Rlog.pii(LOG_TAG, Integer.valueOf(sid)));
                int mccSidMapSize = MccSidLtmOffMap.length;
                Rlog.d(LOG_TAG, " [getMccFromConflictTableBySidLtmOff] mccSidMapSize = " + mccSidMapSize);
                int i = 0;
                while (i < mccSidMapSize) {
                    MccSidLtmOff mccSidLtmOff = MccSidLtmOffMap[i];
                    int max = mccSidLtmOff.mLtmOffMax * PARAM_FOR_OFFSET;
                    int min = mccSidLtmOff.mLtmOffMin * PARAM_FOR_OFFSET;
                    Rlog.d(LOG_TAG, "[getMccFromConflictTableBySidLtmOff] mccSidLtmOff.Sid = " + Rlog.pii(LOG_TAG, Integer.valueOf(mccSidLtmOff.mSid)) + ", sid = " + Rlog.pii(LOG_TAG, Integer.valueOf(sid)) + ", ltm_off = " + ltmoff + ", max = " + max + ", min = " + min);
                    if (mccSidLtmOff.mSid != sid || ltmoff > max || ltmoff < min) {
                        i++;
                    } else {
                        String mccStr = Integer.toString(mccSidLtmOff.mMcc);
                        Rlog.d(LOG_TAG, "[getMccFromConflictTableBySidLtmOff] Mcc = " + mccStr);
                        return mccStr;
                    }
                }
                return null;
            } catch (NumberFormatException e) {
                Rlog.e(LOG_TAG, Log.getStackTraceString(e));
                return null;
            }
        } catch (NumberFormatException e2) {
            Rlog.e(LOG_TAG, Log.getStackTraceString(e2));
            return null;
        }
    }

    public static String getMccFromMINSTableBySid(String sSid) {
        Rlog.d(LOG_TAG, " [getMccFromMINSTableBySid] sid = " + Rlog.pii(LOG_TAG, sSid));
        if (sSid == null || sSid.length() == 0 || sSid.length() > 5) {
            Rlog.d(LOG_TAG, "[getMccFromMINSTableBySid] please check the param ");
            return null;
        }
        try {
            int sid = Integer.parseInt(sSid);
            if (sid < 0) {
                return null;
            }
            int size = MccIddNddSidMap.length;
            Rlog.d(LOG_TAG, " [getMccFromMINSTableBySid] size = " + size);
            int i = 0;
            while (i < size) {
                MccIddNddSid mccIddNddSid = MccIddNddSidMap[i];
                Rlog.d(LOG_TAG, " [getMccFromMINSTableBySid] sid = " + Rlog.pii(LOG_TAG, Integer.valueOf(sid)) + ", mccIddNddSid.SidMin = " + Rlog.pii(LOG_TAG, Integer.valueOf(mccIddNddSid.mSidMin)) + ", mccIddNddSid.SidMax = " + Rlog.pii(LOG_TAG, Integer.valueOf(mccIddNddSid.mSidMax)));
                if (sid < mccIddNddSid.mSidMin || sid > mccIddNddSid.mSidMax) {
                    i++;
                } else {
                    String mccStr = Integer.toString(mccIddNddSid.mMcc);
                    Rlog.d(LOG_TAG, "[queryMccFromConflictTableBySid] Mcc = " + mccStr);
                    return mccStr;
                }
            }
            return null;
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    public static String getMccMncFromSidMccMncListBySid(String sSid) {
        Rlog.d(LOG_TAG, " [getMccMncFromSidMccMncListBySid] sid = " + Rlog.pii(LOG_TAG, sSid));
        if (sSid == null || sSid.length() == 0 || sSid.length() > 5) {
            Rlog.d(LOG_TAG, "[getMccMncFromSidMccMncListBySid] please check the param ");
            return null;
        }
        try {
            int sid = Integer.parseInt(sSid);
            if (sid < 0) {
                return null;
            }
            List<SidMccMnc> mSidMccMncList = TelephonyPlusCode.getSidMccMncList();
            int left = 0;
            int right = mSidMccMncList.size() - 1;
            int mccMnc = 0;
            while (true) {
                if (left > right) {
                    break;
                }
                int mid = (left + right) / PARAM_FOR_OFFSET;
                SidMccMnc mSidMccMnc = mSidMccMncList.get(mid);
                if (sid >= mSidMccMnc.mSid) {
                    if (sid <= mSidMccMnc.mSid) {
                        mccMnc = mSidMccMnc.mMccMnc;
                        break;
                    }
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
            if (mccMnc == 0) {
                return null;
            }
            String mccMncStr = Integer.toString(mccMnc);
            Rlog.d(LOG_TAG, "[getMccMncFromSidMccMncListBySid] MccMncStr = " + mccMncStr);
            return mccMncStr;
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, Log.getStackTraceString(e));
            return null;
        }
    }
}
