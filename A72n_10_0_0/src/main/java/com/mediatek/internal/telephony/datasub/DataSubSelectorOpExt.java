package com.mediatek.internal.telephony.datasub;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.SubscriptionController;
import com.mediatek.internal.telephony.MtkSubscriptionManager;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.util.Arrays;
import java.util.List;

public class DataSubSelectorOpExt implements IDataSubSelectorOPExt {
    private static boolean DBG = true;
    private static final int DSS_RET_CANNOT_GET_SIM_INFO = -2;
    private static final int DSS_RET_INVALID_PHONE_INDEX = -1;
    private static String LOG_TAG = "DSSExt";
    private static final String PRIMARY_SIM = "primary_sim";
    private static final int SML_CHECK_FOLLOW_OM = 3;
    private static final int SML_CHECK_FOLLOW_OM_DO_NOTHING = 4;
    private static final int SML_CHECK_SWITCH_DONE = 1;
    private static final int SML_CHECK_WAIT_VAILD_CARD_INFO = 2;
    private static final boolean USER_BUILD = TextUtils.equals(Build.TYPE, DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER);
    private static CapabilitySwitch mCapabilitySwitch = null;
    private static Context mContext = null;
    private static DataSubSelector mDataSubSelector = null;
    private static DataSubSelectorOpExt mInstance = null;
    private static ISimSwitchForDSSExt mSimSwitchForDSS = null;
    private static SubscriptionManager mSubscriptionManager = null;

    public DataSubSelectorOpExt(Context context) {
        mContext = context;
    }

    @Override // com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt
    public void init(DataSubSelector dataSubSelector, ISimSwitchForDSSExt simSwitchForDSS) {
        mDataSubSelector = dataSubSelector;
        mCapabilitySwitch = CapabilitySwitch.getInstance(mContext, dataSubSelector);
        mSimSwitchForDSS = simSwitchForDSS;
        mSubscriptionManager = (SubscriptionManager) mContext.getSystemService("telephony_subscription_service");
    }

    @Override // com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt
    public void handleSimStateChanged(Intent intent) {
        int simStatus = intent.getIntExtra("android.telephony.extra.SIM_STATE", 0);
        log("subsidylock: handleSimStateChanged: " + simStatus);
        if (simStatus == 10) {
            log("handleSimStateChanged: INTENT_VALUE_ICC_IMSI");
            mCapabilitySwitch.handleSimImsiStatus(intent);
            handleNeedWaitImsi(intent);
            handleNeedWaitUnlock(intent);
            if (RadioCapabilitySwitchUtil.isSubsidyLockForOmSupported()) {
                log("subsidylock: process capability switch in IMSI state");
                subSelectorForOp18Subsidy(intent);
            }
        } else if (simStatus == 6) {
            mCapabilitySwitch.handleSimImsiStatus(intent);
        } else if ((simStatus == 2 || simStatus == 3 || simStatus == 4) && RadioCapabilitySwitchUtil.isSubsidyLockForOmSupported()) {
            log("subsidylock: process capability switch in LOCKED state");
            subSelectorForOp18Subsidy(intent);
        }
    }

    @Override // com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt
    public void handleSubinfoRecordUpdated(Intent intent) {
        int detectedType = intent.getIntExtra("simDetectStatus", 4);
        log("handleSubinfoRecordUpdated: detectedType = " + detectedType);
        if (detectedType == 4 || !RadioCapabilitySwitchUtil.isSubsidyLockForOmSupported()) {
            subSelector(intent);
        } else {
            subSelectorForOp18Subsidy(intent);
        }
    }

    private void handleNeedWaitImsi(Intent intent) {
        if (CapabilitySwitch.isNeedWaitImsi()) {
            CapabilitySwitch.setNeedWaitImsi(Boolean.toString(false));
            subSelector(intent);
        }
        if (CapabilitySwitch.isNeedWaitImsiRoaming()) {
            CapabilitySwitch.setNeedWaitImsiRoaming(Boolean.toString(false));
        }
    }

    private void handleNeedWaitUnlock(Intent intent) {
        if (CapabilitySwitch.isNeedWaitUnlock()) {
            CapabilitySwitch.setNeedWaitUnlock("false");
            subSelector(intent);
        }
        if (CapabilitySwitch.isNeedWaitUnlockRoaming()) {
            CapabilitySwitch.setNeedWaitUnlockRoaming("false");
        }
    }

    private int getHighCapabilityPhoneIdBySimType() {
        int phoneId = -1;
        int[] simOpInfo = new int[mDataSubSelector.getPhoneNum()];
        int[] simType = new int[mDataSubSelector.getPhoneNum()];
        int insertedState = 0;
        int insertedSimCount = 0;
        int tSimCount = 0;
        int wSimCount = 0;
        int cSimCount = 0;
        int op09VolteOffPhoneId = -1;
        String[] currIccId = new String[mDataSubSelector.getPhoneNum()];
        if (RadioCapabilitySwitchUtil.isPS2SupportLTE() && mDataSubSelector.getPhoneNum() == 2) {
            for (int i = 0; i < mDataSubSelector.getPhoneNum(); i++) {
                currIccId[i] = DataSubSelectorUtil.getIccidFromProp(i);
                if (currIccId[i] == null || "".equals(currIccId[i])) {
                    log("sim not ready, can not get high capability phone id");
                    return -1;
                }
                if (!DataSubConstants.NO_SIM_VALUE.equals(currIccId[i])) {
                    insertedSimCount++;
                    insertedState |= 1 << i;
                }
            }
            if (insertedSimCount == 0) {
                log("no sim card, don't switch");
                return -1;
            } else if (!RadioCapabilitySwitchUtil.getSimInfo(simOpInfo, simType, insertedState)) {
                log("cannot get sim operator info, don't switch");
                return -2;
            } else {
                for (int i2 = 0; i2 < mDataSubSelector.getPhoneNum(); i2++) {
                    if (2 == simOpInfo[i2]) {
                        tSimCount++;
                    } else if (RadioCapabilitySwitchUtil.isCdmaCard(i2, simOpInfo[i2], mContext)) {
                        cSimCount++;
                        simOpInfo[i2] = 4;
                        op09VolteOffPhoneId = i2;
                    } else if (simOpInfo[i2] != 0) {
                        wSimCount++;
                        if (simOpInfo[i2] != 4) {
                            simOpInfo[i2] = 3;
                        }
                    }
                }
                log("getHighCapabilityPhoneIdBySimType : Inserted SIM count: " + insertedSimCount + ", insertedStatus: " + insertedState + ", tSimCount: " + tSimCount + ", wSimCount: " + wSimCount + ", cSimCount: " + cSimCount + Arrays.toString(simOpInfo));
                if (RadioCapabilitySwitchUtil.isSupportSimSwitchEnhancement(1) && RadioCapabilitySwitchUtil.isTPlusWSupport() && (!(simOpInfo[0] == 2 && simOpInfo[1] == 3) && simOpInfo[0] == 3)) {
                    int i3 = simOpInfo[1];
                }
                if (RadioCapabilitySwitchUtil.isSupportSimSwitchEnhancement(2)) {
                    if (simOpInfo[0] == 2 && RadioCapabilitySwitchUtil.isCdmaCard(1, simOpInfo[1], mContext)) {
                        phoneId = 1;
                    } else if (RadioCapabilitySwitchUtil.isCdmaCard(0, simOpInfo[0], mContext) && simOpInfo[1] == 2) {
                        phoneId = 0;
                    }
                }
                if (RadioCapabilitySwitchUtil.isSupportSimSwitchEnhancement(3)) {
                    if (RadioCapabilitySwitchUtil.isCdmaCard(0, simOpInfo[0], mContext) && simOpInfo[1] == 3) {
                        phoneId = 0;
                    } else if (simOpInfo[0] == 3 && RadioCapabilitySwitchUtil.isCdmaCard(1, simOpInfo[1], mContext)) {
                        phoneId = 1;
                    }
                }
                if (simOpInfo[0] == 4 && simOpInfo[1] == 4 && wSimCount == 1 && cSimCount == 1 && op09VolteOffPhoneId != -1) {
                    phoneId = op09VolteOffPhoneId;
                }
            }
        }
        log("getHighCapabilityPhoneIdBySimType : " + phoneId);
        return phoneId;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b7, code lost:
        return;
     */
    @Override // com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt
    public void subSelector(Intent intent) {
        String[] currIccId = new String[mDataSubSelector.getPhoneNum()];
        if (MtkTelephonyManagerEx.getDefault().getSimLockPolicy() != 0) {
            int checkResult = preCheckForSimMeLock(intent);
            log("preCheckForSimMeLock result=" + checkResult);
            if (checkResult == 1 || checkResult == 4 || checkResult == 2) {
                return;
            }
        }
        int phoneId = getHighCapabilityPhoneIdBySimType();
        if (phoneId == -2) {
            CapabilitySwitch.setNeedWaitImsi(Boolean.toString(true));
        } else if (phoneId == -1) {
            String defaultIccid = "";
            int defDataPhoneId = SubscriptionManager.getPhoneId(SubscriptionController.getInstance().getDefaultDataSubId());
            if (defDataPhoneId >= 0) {
                if (defDataPhoneId >= DataSubSelectorUtil.getIccidNum()) {
                    log("phoneId out of boundary :" + defDataPhoneId);
                } else {
                    defaultIccid = DataSubSelectorUtil.getIccidFromProp(defDataPhoneId);
                }
            }
            if (!DataSubConstants.NO_SIM_VALUE.equals(defaultIccid) && !"".equals(defaultIccid)) {
                int i = 0;
                while (true) {
                    if (i >= mDataSubSelector.getPhoneNum()) {
                        break;
                    }
                    currIccId[i] = DataSubSelectorUtil.getIccidFromProp(i);
                    if (currIccId[i] == null || "".equals(currIccId[i])) {
                        log("error: iccid not found, wait for next sub ready");
                    } else if (defaultIccid.equals(currIccId[i])) {
                        phoneId = i;
                        break;
                    } else {
                        i++;
                    }
                }
            } else {
                return;
            }
        }
        if (!mCapabilitySwitch.isSimUnLocked()) {
            log("DataSubSelector for OM: do not switch because of sim locking");
            CapabilitySwitch.setNeedWaitUnlock("true");
            CapabilitySwitch.setSimStatus(intent);
            return;
        }
        log("DataSubSelector for OM: no pin lock");
        CapabilitySwitch.setNeedWaitUnlock("false");
        log("Default data phoneid = " + phoneId);
        if (phoneId >= 0) {
            mCapabilitySwitch.setCapabilityIfNeeded(phoneId);
        }
        CapabilitySwitch.resetSimStatus();
    }

    public void subSelectorForOp18Subsidy(Intent intent) {
        int detectedType;
        if (intent == null) {
            CapabilitySwitch capabilitySwitch = mCapabilitySwitch;
            detectedType = CapabilitySwitch.getSimStatus();
        } else {
            detectedType = intent.getIntExtra("simDetectStatus", 0);
        }
        log("DataSubSelector for Op18-Subsidy");
        for (int i = 0; i < mDataSubSelector.getPhoneNum(); i++) {
            int status = SubscriptionManager.getSimStateForSlotIndex(i);
            log("DataSubSelector for Op18-Subsidy: slot:" + i + ", status:" + status);
            if (status == 0 || status == 6) {
                log("DataSubSelector for Op18-Subsidy: sim state update not done, wait.");
                return;
            }
        }
        if (!mCapabilitySwitch.isSimUnLocked()) {
            log("DataSubSelector for Op18-Subsidy: do not switch because of sim locking");
            CapabilitySwitch.setNeedWaitUnlock("true");
            return;
        }
        log("DataSubSelector for Op18-Subsidy: no pin lock");
        CapabilitySwitch.setNeedWaitUnlock("false");
        List<SubscriptionInfo> subList = mSubscriptionManager.getActiveSubscriptionInfoList();
        int insertedSimCount = (subList == null || subList.isEmpty()) ? 0 : subList.size();
        int defaultSub = SubscriptionManager.getDefaultDataSubscriptionId();
        log("Default sub = " + defaultSub + ", insertedSimCount = " + insertedSimCount);
        if (insertedSimCount == 0) {
            log("C0: No SIM inserted, set data unset");
            setDefaultData(-1);
        } else if (insertedSimCount == 1) {
            int phoneId = subList.get(0).getSimSlotIndex();
            if (detectedType == 1) {
                log("C1: Single SIM + New SIM: Set Default data to phone:" + phoneId);
                if (mCapabilitySwitch.setCapability(phoneId)) {
                    setDefaultData(phoneId);
                }
                mDataSubSelector.setDataEnabled(phoneId, true);
            } else if (defaultSub == -1) {
                log("C3: Single SIM + Non Data SIM: Set Default data to phone:" + phoneId);
                if (mCapabilitySwitch.setCapability(phoneId)) {
                    setDefaultData(phoneId);
                }
                mDataSubSelector.setDataEnabled(phoneId, true);
            } else if (mSubscriptionManager.isActiveSubscriptionId(defaultSub)) {
                log("C2: Single SIM + Data SIM: Set Default data to phone:" + phoneId);
                if (mCapabilitySwitch.setCapability(phoneId)) {
                    setDefaultData(phoneId);
                }
            } else {
                log("C3: Single SIM + Non Data SIM: Set Default data to phone:" + phoneId);
                if (mCapabilitySwitch.setCapability(phoneId)) {
                    setDefaultData(phoneId);
                }
                mDataSubSelector.setDataEnabled(phoneId, true);
            }
        } else if (insertedSimCount >= 2 && !mSimSwitchForDSS.checkCapSwitch(-1)) {
            CapabilitySwitch.setNeedWaitImsi(Boolean.toString(true));
        }
    }

    private void setDefaultData(int phoneId) {
        if (RadioCapabilitySwitchUtil.isSubsidyLockForOmSupported()) {
            log("setDefaultData for subsidylock for phoneId: " + phoneId);
            mDataSubSelector.setDefaultData(phoneId);
            return;
        }
        SubscriptionController.getInstance();
        int sub = MtkSubscriptionManager.getSubIdUsingPhoneId(phoneId);
        int currSub = SubscriptionManager.getDefaultDataSubscriptionId();
        log("setDefaultData: " + sub + ", current default sub:" + currSub);
        if (sub != currSub && sub >= -1) {
            updateImsSim(mContext, sub);
        }
    }

    private void updateImsSim(Context context, int subId) {
        if (!SystemProperties.get("ro.vendor.md_auto_setup_ims").equals("1") && SystemProperties.getInt("persist.vendor.mtk_mims_support", 1) != 1) {
            log("updateImsSim, subId = " + subId);
            Settings.Global.putInt(context.getContentResolver(), PRIMARY_SIM, subId);
            mDataSubSelector.updateNetworkMode(context, subId);
        }
    }

    @Override // com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt
    public void handleBootCompleteAction() {
        log("handleBootCompleteAction");
        int simState1 = TelephonyManager.from(mContext).getSimState(0);
        int simState2 = TelephonyManager.from(mContext).getSimState(1);
        log("subsidylock: simState1 :" + simState1 + ", simState2 : " + simState2);
        if (simState1 == 1 && simState2 == 1) {
            log("subsidylock: both SIM ABSENT, Set capability and data to phoneId 0");
            mCapabilitySwitch.setCapability(0);
            setDefaultData(0);
        }
    }

    @Override // com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt
    public void handleSubsidyLockStateAction(Intent intent) {
        log("handleSubsidyLockStateAction");
        subSelectorForOp18Subsidy(intent);
    }

    private boolean hasConnectivity() {
        NetworkInfo info = ((ConnectivityManager) mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        log("DataSubselector, networkinfo: " + info);
        if (info == null || !info.isConnected()) {
            return false;
        }
        NetworkInfo.DetailedState state = info.getDetailedState();
        log("DataSubselector, DetailedState : " + state);
        if (state == NetworkInfo.DetailedState.CONNECTED) {
            return true;
        }
        return false;
    }

    @Override // com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt
    public void handleConnectivityAction() {
        log("handleConnectivityAction");
        if (hasConnectivity()) {
            log("SET CONNECTIVITY_STATUS TO 1");
            SystemProperties.set("persist.vendor.subsidylock.connectivity_status", String.valueOf(1));
            return;
        }
        log("SET CONNECTIVITY_STATUS TO 0");
        SystemProperties.set("persist.vendor.subsidylock.connectivity_status", String.valueOf(0));
    }

    @Override // com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt
    public void handleAirPlaneModeOff(Intent intent) {
        subSelector(intent);
    }

    @Override // com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt
    public void handlePlmnChanged(Intent intent) {
    }

    @Override // com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt
    public void handleDefaultDataChanged(Intent intent) {
    }

    @Override // com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt
    public void handleSimMeLock(Intent intent) {
        subSelector(intent);
    }

    private int preCheckForSimMeLock(Intent intent) {
        int simLockPolicy = MtkTelephonyManagerEx.getDefault().getSimLockPolicy();
        int[] simSlotvaildInfo = new int[mDataSubSelector.getPhoneNum()];
        int phoneId = -1;
        int tempDefaultDataPhone = -1;
        int simCount = 0;
        int unlockedSimCount = 0;
        int simValidCount = 0;
        if (simLockPolicy != 1 && simLockPolicy != 2 && simLockPolicy != 3) {
            if (simLockPolicy != 7) {
                if (simLockPolicy == 4 || simLockPolicy == 5 || simLockPolicy == 6 || simLockPolicy == 8 || simLockPolicy == 9) {
                    int i = 0;
                    while (i < mDataSubSelector.getPhoneNum()) {
                        simSlotvaildInfo[i] = MtkTelephonyManagerEx.getDefault().checkValidCard(i);
                        log("preCheckForSimMeLock() simSlotvaildInfo[" + i + "]=" + simSlotvaildInfo[i]);
                        if (simSlotvaildInfo[i] == -1) {
                            log("preCheckForSimMeLock() wait for sim vaild state update");
                            return 2;
                        }
                        if (simSlotvaildInfo[i] == 0) {
                            simValidCount++;
                        }
                        i++;
                        phoneId = phoneId;
                        simLockPolicy = simLockPolicy;
                    }
                    log("preCheckForSimMeLock() simValidCount=" + simValidCount);
                    if (simValidCount >= 1) {
                        return 3;
                    }
                    return 4;
                } else if (simLockPolicy == 255) {
                    log("Follow OM for Legacy, simLockPolicy=" + simLockPolicy);
                    return 3;
                } else {
                    log("not handled simLockPolicy=" + simLockPolicy);
                    return 4;
                }
            }
        }
        int phoneId2 = intent.getIntExtra("phone", -1);
        int subId = intent.getIntExtra("subscription", -1);
        int vaildState = intent.getIntExtra("SIM_VALID", -1);
        log("preCheckForSimMeLock() phoneId=" + phoneId2 + " subId= " + subId + " vaildState=" + vaildState);
        for (int i2 = 0; i2 < mDataSubSelector.getPhoneNum(); i2++) {
            simSlotvaildInfo[i2] = MtkTelephonyManagerEx.getDefault().checkValidCard(i2);
            log("preCheckForSimMeLock() simSlotvaildInfo[" + i2 + "]=" + simSlotvaildInfo[i2]);
            if (simSlotvaildInfo[i2] == -1) {
                log("preCheckForSimMeLock() wait for sim vaild state update");
                return 2;
            }
            if (simSlotvaildInfo[i2] != 2) {
                simCount++;
            }
        }
        if (simCount == 1) {
            for (int i3 = 0; i3 < mDataSubSelector.getPhoneNum(); i3++) {
                if (simSlotvaildInfo[i3] == 0) {
                    log("preCheckForSimMeLock() only one unlocked sim in slot" + i3);
                    return 3;
                }
            }
            log("preCheckForSimMeLock() only one locked sim in slot");
            return 4;
        }
        for (int i4 = 0; i4 < mDataSubSelector.getPhoneNum(); i4++) {
            if (simSlotvaildInfo[i4] == 0) {
                unlockedSimCount++;
                tempDefaultDataPhone = i4;
            }
        }
        if (unlockedSimCount == 1) {
            log("preCheckForSimMeLock() one unlocked SIM + n Unlocked SIM");
            CapabilitySwitch.setNeedWaitImsi(Boolean.toString(true));
            mDataSubSelector.setDefaultData(tempDefaultDataPhone);
            return 1;
        } else if (unlockedSimCount > 1) {
            log("preCheckForSimMeLock() two unlocked SIMs");
            return 3;
        } else {
            log("preCheckForSimMeLock() two locked SIMs");
            return 4;
        }
    }

    @Override // com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt
    public boolean enableAospDefaultDataUpdate() {
        return false;
    }

    @Override // com.mediatek.internal.telephony.datasub.IDataSubSelectorOPExt
    public boolean enableAospDisableDataSwitch() {
        return true;
    }

    private void log(String txt) {
        if (DBG) {
            Rlog.d(LOG_TAG, txt);
        }
    }

    private void loge(String txt) {
        if (DBG) {
            Rlog.e(LOG_TAG, txt);
        }
    }
}
