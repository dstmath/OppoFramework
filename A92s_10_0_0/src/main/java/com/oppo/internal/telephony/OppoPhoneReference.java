package com.oppo.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.AbstractCallTracker;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.IOppoPhone;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.AbstractDcTracker;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.uicc.AbstractBaseRecords;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UsimServiceTable;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.explock.RegionLockConstant;
import com.oppo.internal.telephony.explock.util.ExpLockHelper;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseUtils;
import com.oppo.internal.telephony.rf.OemMTKSarConfigParser;
import com.oppo.internal.telephony.utils.OppoManagerHelper;
import com.oppo.internal.telephony.utils.OppoPhoneUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class OppoPhoneReference implements IOppoPhone {
    public static final String CF_ENABLED_VIDEO = "video_cf_enabled";
    protected static final int CMD_OPPO_GET_BATTERY_COVER_STATUS = 12;
    protected static final int CMD_OPPO_REG_FREQ_HOP_IND = 22;
    protected static final int CMD_OPPO_SET_SAR_RF_STATE = 5;
    protected static final int CMD_OPPO_SET_SIM_CARD_TYPE = 14;
    protected static final int CMD_OPPO_SET_TUNER_LOGIC = 13;
    protected static final boolean DBG = OemConstant.SWITCH_LOG;
    protected static final int EVENT_RADIO_AVAILABLE = 1;
    private static final int GET_IMEI_INTERVAL = 3000;
    protected static boolean HybridVolteType = false;
    private static final int MAX_GET_IMEI_RETRY = 3;
    protected static String[] OEM_IMEI = new String[2];
    private static final String OEM_ISAUTO_ANSWER = "oem_is_auto_answer";
    protected static String OEM_MEID = null;
    protected static final int OPPO_EVENT_GET_MDM_VERSION_DONE = 1;
    protected static final int OPPO_EVENT_SET_ARFCN_DONE = 2;
    protected static final int OPPO_EVENT_SET_ECC_LIST_DONE = 3;
    public static final String PROPERTY_QCOM_FREQHOP = "persist.sys.qcom_freqhop";
    protected static int mSarState = 0;
    private final String DEEP_SLEEP_URI = "oppoguaedelf_deep_sleep_status";
    /* access modifiers changed from: private */
    public String LOG_TAG = "OppoPhone";
    private int mCatchImeiFail = 0;
    protected final Context mContext;
    protected int mDeepSleepStatus = 0;
    private final Uri mDeepSleepUri = Settings.System.getUriFor("oppoguaedelf_deep_sleep_status");
    private Handler mHandler = new Handler() {
        /* class com.oppo.internal.telephony.OppoPhoneReference.AnonymousClass1 */

        public void handleMessage(Message msg) {
            int i = msg.what;
        }
    };
    private boolean mIsPendingSRVCC = false;
    private boolean mManualSearching = false;
    private String mMdmVersion;
    private OppoRIL mOppoRIL;
    private Phone mPhone;
    private ContentResolver mResolver;
    private SettingObserver mSettingObserver;
    private final RegistrantList mShutDownRegistrants = new RegistrantList();
    private Call.SrvccState mSrvccState = Call.SrvccState.NONE;

    private class SettingObserver extends ContentObserver {
        public SettingObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            OppoPhoneReference oppoPhoneReference = OppoPhoneReference.this;
            oppoPhoneReference.mDeepSleepStatus = Settings.System.getInt(oppoPhoneReference.mContext.getContentResolver(), "oppoguaedelf_deep_sleep_status", 0);
            String access$000 = OppoPhoneReference.this.LOG_TAG;
            Rlog.d(access$000, "mDeepSleepStatus=" + OppoPhoneReference.this.mDeepSleepStatus);
        }
    }

    public int getDeepSleepStatus() {
        return this.mDeepSleepStatus;
    }

    public OppoPhoneReference(Phone phone) {
        this.mPhone = phone;
        this.LOG_TAG += "/" + this.mPhone.getPhoneId();
        this.mContext = this.mPhone.getContext();
        initOnce(phone);
    }

    private void initOnce(Phone phone) {
        OppoTelephonyController oppoTelephony = OppoTelephonyController.getInstance(this.mContext);
        oppoTelephony.initByPhone(this.mPhone);
        this.mOppoRIL = oppoTelephony.getOppoRIL(this.mPhone.getPhoneId());
        this.mSettingObserver = new SettingObserver();
        this.mContext.getContentResolver().registerContentObserver(this.mDeepSleepUri, true, this.mSettingObserver);
    }

    public boolean getOemAutoAnswer() {
        boolean defaultVal = OemTelephonyUtils.isNwLabTest();
        boolean isAuto = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(OEM_ISAUTO_ANSWER, !defaultVal);
        Rlog.d("oem", "getOemAutoAnswer isAuto:" + isAuto + ", defaultVal " + defaultVal);
        return isAuto;
    }

    public void setOemAutoAnswer(boolean isAuto) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putBoolean(OEM_ISAUTO_ANSWER, isAuto);
        editor.apply();
    }

    private void sendResponse(int error, Message response) {
        AsyncResult.forMessage(response, (Object) null, CommandException.fromRilErrno(error));
        response.sendToTarget();
    }

    public void getBandMode(Message response) {
        this.mOppoRIL.getBandMode(response);
    }

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, Message onComplete) {
        this.mPhone.mCi.changeBarringPassword(facility, oldPwd, newPwd, onComplete);
    }

    public String getOEMImei(String mImei) {
        String imei = OEM_IMEI[this.mPhone.getPhoneId()];
        if (!TextUtils.isEmpty(imei)) {
            return imei;
        }
        return mImei;
    }

    public void getImeiAgainIfFailure() {
        int i = this.mCatchImeiFail;
        if (i < 3) {
            int delays = (i * 3000) + 1;
            Phone phone = this.mPhone;
            phone.sendMessageDelayed(phone.obtainMessage(1), (long) delays);
            this.mCatchImeiFail++;
            Rlog.d(this.LOG_TAG, "GET_DEVICE_IDENTITY[" + this.mCatchImeiFail + "]:Fail," + delays + "s get again,");
        }
    }

    public void saveImeiAndMeid(String mMeid, String mImei) {
        int phoneId = this.mPhone.getPhoneId();
        if (!TextUtils.isEmpty(mImei) && !"00000000".equals(mImei) && TextUtils.isEmpty(OEM_IMEI[phoneId])) {
            OEM_IMEI[phoneId] = mImei;
        }
        if (!TextUtils.isEmpty(mMeid) && mMeid.toUpperCase().startsWith("A") && TextUtils.isEmpty(OEM_MEID)) {
            OEM_MEID = mMeid;
        }
        if (DBG) {
            Rlog.d(this.LOG_TAG + phoneId + " " + this.mPhone.getPhoneId(), "saveImeiAndMeid00:" + mMeid + ";" + mImei + ";" + OEM_MEID + ";" + OEM_IMEI[phoneId]);
        }
    }

    public void registerForLteCAState(Handler h, int what, Object obj) {
        this.mOppoRIL.registerForLteCAState(h, what, obj);
    }

    public void unregisterForLteCAState(Handler h) {
        this.mOppoRIL.unregisterForLteCAState(h);
    }

    public int oppoGetSarRfStateV2() {
        return mSarState;
    }

    public boolean getManualSearchingStatus() {
        String str = this.LOG_TAG;
        Rlog.d(str, " mPhoneId:  " + this.mPhone.getPhoneId() + "  getManualSearchingStatus:  " + this.mManualSearching);
        return this.mManualSearching;
    }

    public void setManualSearchingStatus(boolean status) {
        String str = this.LOG_TAG;
        Rlog.d(str, " mPhoneId:  " + this.mPhone.getPhoneId() + "  setManualSearchingStatus:  " + status);
        this.mManualSearching = status;
    }

    public boolean isSRVCC() {
        return this.mSrvccState == Call.SrvccState.STARTED;
    }

    public void clearSRVCC() {
        this.mSrvccState = Call.SrvccState.NONE;
    }

    public void setPeningSRVCC(boolean bl) {
        this.mIsPendingSRVCC = bl;
    }

    public void setSRVCCState(Call.SrvccState srvccState) {
        this.mSrvccState = srvccState;
    }

    public boolean getPendingSRVCC() {
        return this.mIsPendingSRVCC;
    }

    public boolean getHybridVolteType() {
        String str = this.LOG_TAG;
        Rlog.d(str, "getHybridVolteType: enable = " + HybridVolteType);
        return HybridVolteType;
    }

    public void setHybridVolteType(boolean enable) {
        String str = this.LOG_TAG;
        Rlog.d(str, "setHybridVolteType: enable = " + enable);
        HybridVolteType = enable;
    }

    public void registerForShutDownChanged(Handler h, int what, Object obj) {
        this.mShutDownRegistrants.add(h, what, obj);
    }

    public void unregisterForShutDownChanged(Handler h) {
        this.mShutDownRegistrants.remove(h);
    }

    public void notifyForShutDownChanged() {
        this.mShutDownRegistrants.notifyRegistrants();
    }

    public boolean getVowifiRegStatus() {
        ImsPhoneCallTracker cT;
        Phone imsPhone = this.mPhone.getImsPhone();
        if (imsPhone == null || (cT = imsPhone.getCallTracker()) == null) {
            return false;
        }
        return ((AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, cT)).getVowifiRegStatus();
    }

    public void oemMigrate(RegistrantList to, RegistrantList from) {
        from.removeCleared();
        int n = from.size();
        for (int i = 0; i < n; i++) {
            Registrant r = (Registrant) from.get(i);
            Message msg = r.messageForRegistrant();
            if (msg == null) {
                Rlog.d(this.LOG_TAG, "msg is null");
            } else if (msg.obj != CallManager.getInstance().getRegistrantIdentifier()) {
                boolean issame = false;
                int j = 0;
                int k = to.size();
                while (true) {
                    if (j >= k) {
                        break;
                    } else if (((Registrant) to.get(j)) == r) {
                        issame = true;
                        break;
                    } else {
                        j++;
                    }
                }
                String str = this.LOG_TAG;
                Rlog.d(str, "leon oemMigrate:" + issame);
                if (!issame) {
                    to.add(r);
                }
            }
        }
    }

    public void unregister() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingObserver);
        this.mDeepSleepStatus = 0;
    }

    public void updateSrvccState(Call.SrvccState srvccState) {
        this.mSrvccState = srvccState;
        if (this.mIsPendingSRVCC && this.mSrvccState == Call.SrvccState.COMPLETED) {
            Message msg = this.mPhone.getCallTracker().obtainMessage();
            msg.what = 3;
            this.mPhone.getCallTracker().sendMessageDelayed(msg, (long) 250);
        }
        this.mIsPendingSRVCC = false;
    }

    public void keyLogSrvcc(int state) {
        if (state == 1) {
            OppoManagerHelper.writeLogToPartition(this.mContext, "zz_oppo_critical_log_260", " SRVCC handover completed event ", OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_COMPLETED);
        } else if (state == 2) {
            OppoManagerHelper.writeLogToPartition(this.mContext, "zz_oppo_critical_log_261", " SRVCC handover failed event  ", OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_FAILED);
        } else if (state == 3) {
            OppoManagerHelper.writeLogToPartition(this.mContext, "zz_oppo_critical_log_262", " SRVCC handover cancel event  ", OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_IMS_SRVCC_CANCEL);
        }
    }

    public void startMobileDataHongbaoPolicy(int time1, int time2, String value1, String value2) {
        DcTracker dcTracker = this.mPhone.getDcTracker(1);
        if (dcTracker != null) {
            ((AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, dcTracker)).startMobileDataHongbaoPolicy(time1, time2, value1, value2);
        } else {
            Rlog.d(this.LOG_TAG, "requestFakePing: mDcTracker is  NULL");
        }
    }

    public boolean is_test_card() {
        IccRecords records = this.mPhone.getIccRecords();
        if (records != null) {
            return ((AbstractBaseRecords) OemTelephonyUtils.typeCasting(AbstractBaseRecords.class, records)).is_test_card();
        }
        return false;
    }

    public String[] getLteCdmaImsi(int phoneid) {
        String[] ImsiList = {"", ""};
        SIMRecords newSimRecords = null;
        RuimRecords newRuimRecords = null;
        UiccController uiccController = UiccController.getInstance();
        if (uiccController == null) {
            Rlog.d(this.LOG_TAG, "getLteCdmaImsi mUiccController == null");
            return ImsiList;
        }
        UiccCardApplication newUiccApplication = uiccController.getUiccCardApplication(phoneid, 1);
        UiccCardApplication newUiccApplication2 = uiccController.getUiccCardApplication(phoneid, 2);
        if (newUiccApplication != null) {
            newSimRecords = newUiccApplication.getIccRecords();
        }
        if (newUiccApplication2 != null) {
            newRuimRecords = newUiccApplication2.getIccRecords();
        }
        if (!(newRuimRecords == null || newRuimRecords.getIMSI() == null)) {
            ImsiList[0] = newRuimRecords.getIMSI();
            if (DBG) {
                String str = this.LOG_TAG;
                Rlog.d(str, "getLteCdmaImsi ruim imsi = " + ImsiList[0]);
            }
        }
        if (!(newSimRecords == null || newSimRecords.getIMSI() == null)) {
            ImsiList[1] = newSimRecords.getIMSI();
            if (DBG) {
                String str2 = this.LOG_TAG;
                Rlog.d(str2, "getLteCdmaImsi sim imsi = " + ImsiList[1]);
            }
        }
        if (DBG) {
            String str3 = this.LOG_TAG;
            Rlog.d(str3, "getLteCdmaImsi ImsiList = " + ImsiList);
        }
        return ImsiList;
    }

    public void updateFreqHopEnable(boolean enable) {
        OppoTelephonyController.getInstance(this.mPhone.getContext()).updateFreqHopEnable(enable);
    }

    public void setVideoCallForwardingFlag(boolean enable) {
        PersistableBundle config;
        boolean supportVideoCf = false;
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (!(configManager == null || (config = configManager.getConfigForSubId(this.mPhone.getSubId())) == null)) {
            supportVideoCf = config.getBoolean("oppo_support_video_cf_bool");
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        int subId = this.mPhone.getSubId();
        String str = this.LOG_TAG;
        Rlog.d(str, "setVideoCallForwardingFlag InSharedPref: Storing enable = " + enable + "  video_cf_enabled:" + subId + ", supportVideoCf " + supportVideoCf);
        if (sp != null && supportVideoCf) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(CF_ENABLED_VIDEO + subId, enable);
            editor.apply();
        }
    }

    public boolean getVideoCallForwardingFlag() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        boolean enabled = false;
        int subId = this.mPhone.getSubId();
        if (sp != null) {
            enabled = sp.getBoolean(CF_ENABLED_VIDEO + subId, false);
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "getVideoCallForwardingFlag enabled = " + enabled + " for subid " + subId);
        return enabled;
    }

    public boolean matchUnLock(String imei, String password, int type) {
        return ExpLockHelper.matchUnLock(imei, password, type);
    }

    public boolean OppoCheckUsimIs4G() {
        UsimServiceTable st = this.mPhone.getUsimServiceTable();
        if (st == null) {
            return false;
        }
        return st.isAvailable(UsimServiceTable.UsimService.EPS_MOBILITY_MANAGEMENT_INFO);
    }

    public String getOperatorName() {
        BufferedReader br = null;
        String operator = "";
        try {
            br = new BufferedReader(new FileReader(new File("/proc/oppoVersion/operatorName")));
            operator = br.readLine();
            try {
                br.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            operator = "";
            if (br != null) {
                br.close();
            }
        } catch (Throwable th) {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        if (operator == null) {
            Rlog.d(this.LOG_TAG, "operator is null");
            return "";
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "operator:" + operator);
        return operator.trim();
    }

    public int getmodemType() {
        BufferedReader br = null;
        int modemType = 0;
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(new File("/proc/oppoVersion/modemType")));
            String type = br2.readLine();
            if (type != null) {
                modemType = Integer.parseInt(type.trim());
            } else {
                String str = this.LOG_TAG;
                Rlog.e(str, "modemType can not get value:" + 0);
            }
            try {
                br2.close();
            } catch (IOException e) {
            }
        } catch (NumberFormatException e2) {
            modemType = 0;
            Rlog.d(this.LOG_TAG, e2.toString());
            if (br != null) {
                br.close();
            }
        } catch (IOException e3) {
            modemType = 0;
            if (br != null) {
                br.close();
            }
        } catch (Exception e4) {
            modemType = 0;
            Rlog.d(this.LOG_TAG, e4.toString());
            if (br != null) {
                br.close();
            }
        } catch (Throwable th) {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
        String str2 = this.LOG_TAG;
        Rlog.e(str2, "modemType:" + modemType);
        return modemType;
    }

    public void oppoSetTunerLogic(int antTunerStateIndx, Message msg) {
        Rlog.d(this.LOG_TAG, "enter oppoSetTunerLogic");
        String[] Gsm1800FixUpCmd = {"AT+ETXANT=4,1,1,3", "+ETXANT:"};
        String[] Gsm1800TASOnCmd = {"AT+ETXANT=3,1,,3", "+ETXANT:"};
        String[] EnterDATCmd = {"AT+ERFIDX=0,0", "+ERFIDX:"};
        String[] ExitDATCmd = {"AT+ERFIDX=0,-1", "+ERFIDX:"};
        if (antTunerStateIndx == 0) {
            this.mPhone.invokeOemRilRequestStrings(Gsm1800FixUpCmd, (Message) null);
            this.mPhone.invokeOemRilRequestStrings(EnterDATCmd, (Message) null);
        } else if (1 == antTunerStateIndx) {
            this.mPhone.invokeOemRilRequestStrings(ExitDATCmd, (Message) null);
            this.mPhone.invokeOemRilRequestStrings(Gsm1800TASOnCmd, (Message) null);
        }
    }

    public void oppoSetSarRfStateByScene(int scene) {
        ArrayList<String[]> cmdList;
        int b_sim = 0;
        String isTestSim1 = SystemProperties.get("vendor.gsm.sim.ril.testsim", RegionLockConstant.TEST_OP_CUANDCMCC);
        String isTestSim2 = SystemProperties.get("vendor.gsm.sim.ril.testsim.1", RegionLockConstant.TEST_OP_CUANDCMCC);
        boolean isFdd = SystemProperties.get("vendor.ril.nw.worldmode.activemode", "1").equalsIgnoreCase("1");
        if (!isTestSim1.equals(RegionLockConstant.TEST_OP_CUANDCMCC) || !isTestSim2.equals(RegionLockConstant.TEST_OP_CUANDCMCC)) {
            b_sim = 1;
        }
        if (isTestSim1.equals("1") || isTestSim2.equals("1")) {
            b_sim = 2;
        }
        Rlog.d(this.LOG_TAG, "txPowerBackoff, scene:" + scene + ", SimType:" + b_sim + ", isFdd:" + isFdd);
        if (scene == -1) {
            this.mPhone.invokeOemRilRequestStrings(new String[]{"AT+ERFTX=1,0,0,0", ""}, (Message) null);
            this.mPhone.invokeOemRilRequestStrings(new String[]{"AT+ERFIDX=1,-1", ""}, (Message) null);
            return;
        }
        if (b_sim == 2) {
            cmdList = OemMTKSarConfigParser.getInstance().getFinalCmdbyScene(scene, isFdd, true);
        } else {
            cmdList = OemMTKSarConfigParser.getInstance().getFinalCmdbyScene(scene, isFdd, false);
        }
        if (cmdList != null && cmdList.size() > 0) {
            Iterator<String[]> it = cmdList.iterator();
            while (it.hasNext()) {
                this.mPhone.invokeOemRilRequestStrings(it.next(), (Message) null);
            }
        }
    }

    public void oppoSetTasForceIdx(int mode, int rat, String antenna_idx, String band) {
        String[] cmd = {"AT+ETXANT=" + mode + "," + rat + "," + antenna_idx + "," + band, "+ETXANT:"};
        String str = this.LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("oppoSetTasForceIdx, sendAtCommand: ");
        sb.append(cmd[0]);
        Rlog.d(str, sb.toString());
        this.mPhone.invokeOemRilRequestStrings(cmd, (Message) null);
    }

    public void oppoSetCaSwitch() {
        String[] cmd = new String[2];
        cmd[1] = "+ECASW:";
        String productHW = SystemProperties.get("ro.product.hw", "oppo");
        String regionMark = SystemProperties.get("ro.oppo.regionmark", "oppo");
        String str = this.LOG_TAG;
        Rlog.d(str, "oppoSetCaSwitch, regionMark: " + regionMark);
        if ("CC035".equals(productHW)) {
            if ("TW".equals(regionMark) || "SG".equals(regionMark)) {
                cmd[0] = "AT+ECASW=1";
            } else {
                cmd[0] = "AT+ECASW=0";
            }
            this.mPhone.invokeOemRilRequestStrings(cmd, (Message) null);
        }
    }

    public void oppoSetBandindicationSwitch(boolean on) {
        String[] cmdOn = new String[2];
        cmdOn[1] = "";
        String[] cmdQuery = {"AT+ECBDINFO?", ""};
        if (on) {
            cmdOn[0] = "AT+ECBDINFO=1";
        } else {
            cmdOn[0] = "AT+ECBDINFO=0";
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "oppoSetBandindicationSwitch, on: " + on);
        this.mPhone.invokeOemRilRequestStrings(cmdOn, (Message) null);
        if (on) {
            this.mPhone.invokeOemRilRequestStrings(cmdQuery, (Message) null);
        }
    }

    public String handlePreCheckCFDialingNumber(String dialingNumber) {
        String operator = SystemProperties.get("ro.oppo.operator", "US");
        String region = SystemProperties.get("persist.sys.oppo.region", "US");
        logd("operator and region : " + operator + " " + region);
        if (operator.equals("SINGTEL") && region.equals("SG") && dialingNumber != null && !dialingNumber.startsWith("+")) {
            dialingNumber = "+65" + dialingNumber;
            logd("Singtel version add +65!");
        }
        if (dialingNumber == null) {
            return dialingNumber;
        }
        if ((operator.equals("VODAFONE") || operator.equals("VODAFONE_PREPAID") || operator.equals("VODAFONE_POSTPAID")) && region.equals("AU") && dialingNumber.startsWith("0")) {
            dialingNumber = "+61" + dialingNumber.substring(1, dialingNumber.length());
            logd("AU VDF version force to use +61 international format!");
        }
        if (!OppoPhoneUtil.isIdeaCard(this.mPhone) || dialingNumber.startsWith("+")) {
            return dialingNumber;
        }
        String dialingNumber2 = "+91" + dialingNumber;
        logd("IN force to use +91 international format!");
        return dialingNumber2;
    }

    public int specifyServiceClassForOperator(int serviceClass) {
        String[][] serviceCodeForOp = {new String[]{"50501", "voice"}, new String[]{"50511", "voice"}, new String[]{"50571", "voice"}};
        int serviceCode = -1;
        String mccMnc = TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "gsm.sim.operator.numeric", "");
        if (mccMnc != null) {
            int i = 0;
            while (true) {
                if (i >= serviceCodeForOp.length) {
                    break;
                } else if (!mccMnc.equals(serviceCodeForOp[i][0])) {
                    i++;
                } else if (serviceCodeForOp[i][1].equals("voice")) {
                    serviceCode = 1;
                } else if (serviceCodeForOp[i][1].equals("video")) {
                    serviceCode = NetworkDiagnoseUtils.RF_GSM_1800_ARFCN_BOTTOM;
                } else if (serviceCodeForOp[i][1].equals("both")) {
                    serviceCode = 0;
                }
            }
        }
        logd("specifyServiceClassForOperator(): mccMnc = " + mccMnc + " serviceCode = " + serviceCode);
        if (-1 == serviceCode) {
            return serviceClass;
        }
        logd("specifyServiceClassForOperator = -1");
        return serviceCode;
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(this.LOG_TAG, s);
        }
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e(this.LOG_TAG, s);
    }
}
