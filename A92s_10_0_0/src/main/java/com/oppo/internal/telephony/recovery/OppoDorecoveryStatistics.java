package com.oppo.internal.telephony.recovery;

import android.content.Context;
import android.location.Location;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.JsonWriter;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.OppoRIL;
import com.oppo.internal.telephony.utils.OppoManagerHelper;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class OppoDorecoveryStatistics {
    private static final String DATA_EVENT_ID = "050401";
    private static final String LOG_TAG = "oppo-dorecovery";
    private static final String TAG = "OppoDorecoveryStatistics";
    private static final Object mLock = new Object();
    private static String mRecoveryKpiLog = "";
    private static ArrayList<String> mResultList = new ArrayList<>();

    private static String getIssuFromType(int issueType) {
        if (issueType == 131) {
            return OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_DORECOVERY_RESULT;
        }
        if (issueType != 132) {
            return "";
        }
        return OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_DORECOVERY_KPI;
    }

    private static void saveLog(Context context, int issueType, String log) {
        String issue = getIssuFromType(issueType);
        try {
            String log_string = OemTelephonyUtils.getOemRes(context, "zz_oppo_critical_log_" + issueType, "");
            if (log_string.equals("")) {
                Rlog.e(TAG, "Can not get resource of identifier zz_oppo_critical_log_" + issueType);
                return;
            }
            String[] log_array = log_string.split(",");
            int writeSize = OppoManagerHelper.writeLogToPartition(Integer.parseInt(log_array[0]), log, issue, log_array[1]);
            HashMap<String, String> record = new HashMap<>();
            record.put(issue, log);
            OppoManagerHelper.onStamp(DATA_EVENT_ID, record);
            OppoRlog.Rlog.d(TAG, "writeLogToPartition logTag:" + issueType + ",log:" + log + ",size:" + writeSize);
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "saveLog exception:" + e.getMessage());
        }
    }

    public static void uploadLog(Context context) {
        Rlog.d(TAG, "uploadLog");
        synchronized (mLock) {
            Iterator<String> it = mResultList.iterator();
            while (it.hasNext()) {
                saveLog(context, OppoRIL.SYS_OEM_NW_DIAG_CAUSE_DATA_DORECOVERY_RESULT, it.next());
            }
            mResultList.clear();
            if (!TextUtils.isEmpty(mRecoveryKpiLog)) {
                saveLog(context, OppoRIL.SYS_OEM_NW_DIAG_CAUSE_DATA_DORECOVERY_KPI, mRecoveryKpiLog);
            }
            mRecoveryKpiLog = "";
        }
    }

    public static void eventRecoveryResult(PdpRecoveryStatisticsResult result) {
        String log = result.toJson();
        OppoRlog.Rlog.d(TAG, "eventRecoveryResult : " + result);
        synchronized (mLock) {
            mResultList.add(log);
        }
    }

    public static void eventRecoveryKPI(PdpRecoveryStatisticsKpi kpi) {
        String log = kpi.toJson();
        OppoRlog.Rlog.d(TAG, "eventRecoveryKPI :" + log);
        synchronized (mLock) {
            mRecoveryKpiLog = log;
        }
    }

    public static class PdpRecoveryStatisticsResult {
        private int mAfterCid = 0;
        private int mAfterPic = 0;
        private int mAfterRealPfNetworkType = 0;
        private int mAfterRsrp = 0;
        private int mAfterTac = 0;
        private int mAfterUserPfNetworkType = 0;
        private int mBeforeCid = 0;
        private int mBeforePic = 0;
        private int mBeforeRealPfNetworkType = 0;
        private int mBeforeRsrp = 0;
        private int mBeforeTac = 0;
        private int mBeforeUserPfNetworkType = 0;
        private String mCurStep = "";
        private long mCurTime = 0;
        private String mFailReason = "";
        private boolean mIs5g = false;
        private boolean mIsSmart5gEnable = false;
        private double mLatitude = 0.0d;
        private double mLongitude = 0.0d;
        private int mNrState = 0;
        private String mReason = "";
        private boolean mSucc = false;
        private long mTimeCost = 0;
        private int mVersion = 1;

        public PdpRecoveryStatisticsResult(boolean mIs5g2, String mReason2, int mBeforeRsrp2, int mBeforeCid2, int mBeforeTac2, int mBeforePic2, int mBeforeUserPfNetworkType2, int mBeforeRealPfNetworkType2, int mAfterRsrp2, int mAfterCid2, int mAfterTac2, int mAfterPic2, int mAfterUserPfNetworkType2, int mAfterRealPfNetworkType2, String mCurStep2, String mFailReason2, boolean mSucc2, long mTimeCost2, Location location, boolean mIsSmart5gEnable2, int mNrState2) {
            this.mIs5g = mIs5g2;
            this.mReason = mReason2;
            this.mBeforeRsrp = mBeforeRsrp2;
            this.mBeforeCid = mBeforeCid2;
            this.mBeforeTac = mBeforeTac2;
            this.mBeforePic = mBeforePic2;
            this.mBeforeUserPfNetworkType = mBeforeUserPfNetworkType2;
            this.mBeforeRealPfNetworkType = mBeforeRealPfNetworkType2;
            this.mAfterRsrp = mAfterRsrp2;
            this.mAfterCid = mAfterCid2;
            this.mAfterTac = mAfterTac2;
            this.mAfterPic = mAfterPic2;
            this.mAfterUserPfNetworkType = mAfterUserPfNetworkType2;
            this.mAfterRealPfNetworkType = mAfterRealPfNetworkType2;
            this.mCurStep = mCurStep2;
            this.mFailReason = mFailReason2;
            this.mSucc = mSucc2;
            this.mTimeCost = mTimeCost2;
            this.mCurTime = System.currentTimeMillis();
            if (location != null) {
                this.mLatitude = location.getLatitude();
                this.mLongitude = location.getLongitude();
            }
            this.mIsSmart5gEnable = mIsSmart5gEnable2;
            this.mNrState = mNrState2;
        }

        public String toString() {
            return "PdpRecoveryStatisticsResult{mVersion=" + this.mVersion + ", mIs5g=" + this.mIs5g + ", mReason='" + this.mReason + '\'' + ", mBeforeRsrp=" + this.mBeforeRsrp + ", mBeforeCid=" + this.mBeforeCid + ", mBeforeTac=" + this.mBeforeTac + ", mBeforePic=" + this.mBeforePic + ", mBeforeUserPfNetworkType=" + this.mBeforeUserPfNetworkType + ", mBeforeRealPfNetworkType=" + this.mBeforeRealPfNetworkType + ", mAfterRsrp=" + this.mAfterRsrp + ", mAfterCid=" + this.mAfterCid + ", mAfterTac=" + this.mAfterTac + ", mAfterPic=" + this.mAfterPic + ", mAfterUserPfNetworkType=" + this.mAfterUserPfNetworkType + ", mAfterRealPfNetworkType=" + this.mAfterRealPfNetworkType + ", mCurStep='" + this.mCurStep + '\'' + ", mFailReason='" + this.mFailReason + '\'' + ", mSucc=" + this.mSucc + ", mTimeCost=" + this.mTimeCost + ", mCurTime=" + this.mCurTime + ", mLatitude=" + this.mLatitude + ", mLongitude=" + this.mLongitude + ", mIsSmart5gEnable=" + this.mIsSmart5gEnable + ", mNrState=" + this.mNrState + '}';
        }

        public String toJson() {
            StringWriter sw = new StringWriter();
            JsonWriter jw = new JsonWriter(sw);
            try {
                jw.beginObject();
                jw.name("mVersion").value((long) this.mVersion);
                jw.name("mIs5g").value(this.mIs5g);
                jw.name("mReason").value(this.mReason);
                jw.name("mBeforeRsrp").value((long) this.mBeforeRsrp);
                jw.name("mBeforeCid").value((long) this.mBeforeCid);
                jw.name("mBeforeTac").value((long) this.mBeforeTac);
                jw.name("mBeforePic").value((long) this.mBeforePic);
                jw.name("mBeforeUserPfNetworkType").value((long) this.mBeforeUserPfNetworkType);
                jw.name("mBeforeRealPfNetworkType").value((long) this.mBeforeRealPfNetworkType);
                jw.name("mAfterRsrp").value((long) this.mAfterRsrp);
                jw.name("mAfterCid").value((long) this.mAfterCid);
                jw.name("mAfterTac").value((long) this.mAfterTac);
                jw.name("mAfterPic").value((long) this.mAfterPic);
                jw.name("mAfterUserPfNetworkType").value((long) this.mAfterUserPfNetworkType);
                jw.name("mAfterRealPfNetworkType").value((long) this.mAfterRealPfNetworkType);
                jw.name("mCurStep").value(this.mCurStep);
                jw.name("mFailReason").value(this.mFailReason);
                jw.name("mSucc").value(this.mSucc);
                jw.name("mTimeCost").value(this.mTimeCost);
                jw.name("mCurTime").value(this.mCurTime);
                jw.name("mLatitude").value(this.mLatitude);
                jw.name("mLongitude").value(this.mLongitude);
                jw.name("mIsSmart5gEnable").value(this.mIsSmart5gEnable);
                jw.name("mNrState").value((long) this.mNrState);
                jw.endObject();
                String stringWriter = sw.toString();
                try {
                    jw.close();
                    sw.close();
                } catch (Exception eclose) {
                    eclose.printStackTrace();
                    Rlog.e(OppoDorecoveryStatistics.TAG, "eclose failed!" + eclose.getMessage());
                }
                return stringWriter;
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.d(OppoDorecoveryStatistics.TAG, "gen PdpRecoveryStatisticsResult json string failed!" + e.getMessage());
                try {
                    jw.close();
                    sw.close();
                } catch (Exception eclose2) {
                    eclose2.printStackTrace();
                    Rlog.e(OppoDorecoveryStatistics.TAG, "eclose failed!" + eclose2.getMessage());
                }
                return "";
            } catch (Throwable th) {
                try {
                    jw.close();
                    sw.close();
                } catch (Exception eclose3) {
                    eclose3.printStackTrace();
                    Rlog.e(OppoDorecoveryStatistics.TAG, "eclose failed!" + eclose3.getMessage());
                }
                throw th;
            }
        }
    }

    public static class PdpRecoveryStatisticsKpi {
        private int mCheck5GCount = 0;
        private int mCheckCount = 0;
        private int mDnsCheckCount = 0;
        private int mEnter5GCount = 0;
        private int mEnterCount = 0;
        private int mRecover5GCount = 0;
        private int mRecoverCount = 0;
        private int mRx0Count12 = 0;
        private int mRx0Count16 = 0;
        private int mRx0Count20 = 0;
        private int mRx0Count4 = 0;
        private int mRx0Count8 = 0;
        private int mTxrxCheckCount = 0;
        private int mVersion = 1;

        public PdpRecoveryStatisticsKpi(int mCheckCount2, int mDnsCheckCount2, int mTxrxCheckCount2, int mEnterCount2, int mRecoverCount2, int mCheck5GCount2, int mEnter5GCount2, int mRecover5GCount2, int mRx0Count42, int mRx0Count82, int mRx0Count122, int mRx0Count162, int mRx0Count202) {
            this.mCheckCount = mCheckCount2;
            this.mDnsCheckCount = mDnsCheckCount2;
            this.mTxrxCheckCount = mTxrxCheckCount2;
            this.mEnterCount = mEnterCount2;
            this.mRecoverCount = mRecoverCount2;
            this.mCheck5GCount = mCheck5GCount2;
            this.mEnter5GCount = mEnter5GCount2;
            this.mRecover5GCount = mRecover5GCount2;
            this.mRx0Count4 = mRx0Count42;
            this.mRx0Count8 = mRx0Count82;
            this.mRx0Count12 = mRx0Count122;
            this.mRx0Count16 = mRx0Count162;
            this.mRx0Count20 = mRx0Count202;
        }

        public String toString() {
            return "PdpRecoveryStatisticsKpi [mVersion=" + this.mVersion + ", mCheckCount=" + this.mCheckCount + ", mDnsCheckCount=" + this.mDnsCheckCount + ", mTxrxCheckCount=" + this.mTxrxCheckCount + ", mEnterCount=" + this.mEnterCount + ", mRecoverCount=" + this.mRecoverCount + ", mCheck5GCount=" + this.mCheck5GCount + ", mEnter5GCount=" + this.mEnter5GCount + ", mRecover5GCount=" + this.mRecover5GCount + ", mRx0Count4=" + this.mRx0Count4 + ", mRx0Count8=" + this.mRx0Count8 + ", mRx0Count12=" + this.mRx0Count12 + ", mRx0Count16=" + this.mRx0Count16 + ", mRx0Count20=" + this.mRx0Count20 + "]";
        }

        public String toJson() {
            StringWriter sw = new StringWriter();
            JsonWriter jw = new JsonWriter(sw);
            try {
                jw.beginObject();
                jw.name("mVersion").value((long) this.mVersion);
                jw.name("mCheckCount").value((long) this.mCheckCount);
                jw.name("mDnsCheckCount").value((long) this.mDnsCheckCount);
                jw.name("mTxrxCheckCount").value((long) this.mTxrxCheckCount);
                jw.name("mEnterCount").value((long) this.mEnterCount);
                jw.name("mRecoverCount").value((long) this.mRecoverCount);
                jw.name("mCheck5GCount").value((long) this.mCheck5GCount);
                jw.name("mEnter5GCount").value((long) this.mEnter5GCount);
                jw.name("mRecover5GCount").value((long) this.mRecover5GCount);
                jw.name("mRx0Count4").value((long) this.mRx0Count4);
                jw.name("mRx0Count8").value((long) this.mRx0Count8);
                jw.name("mRx0Count12").value((long) this.mRx0Count12);
                jw.name("mRx0Count16").value((long) this.mRx0Count16);
                jw.name("mRx0Count20").value((long) this.mRx0Count20);
                jw.endObject();
                String stringWriter = sw.toString();
                try {
                    jw.close();
                    sw.close();
                } catch (Exception eclose) {
                    eclose.printStackTrace();
                    Rlog.e(OppoDorecoveryStatistics.TAG, "eclose failed!" + eclose.getMessage());
                }
                return stringWriter;
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.d(OppoDorecoveryStatistics.TAG, "gen PdpRecoveryStatisticsKpi json string failed!" + e.getMessage());
                try {
                    jw.close();
                    sw.close();
                } catch (Exception eclose2) {
                    eclose2.printStackTrace();
                    Rlog.e(OppoDorecoveryStatistics.TAG, "eclose failed!" + eclose2.getMessage());
                }
                return "";
            } catch (Throwable th) {
                try {
                    jw.close();
                    sw.close();
                } catch (Exception eclose3) {
                    eclose3.printStackTrace();
                    Rlog.e(OppoDorecoveryStatistics.TAG, "eclose failed!" + eclose3.getMessage());
                }
                throw th;
            }
        }
    }
}
