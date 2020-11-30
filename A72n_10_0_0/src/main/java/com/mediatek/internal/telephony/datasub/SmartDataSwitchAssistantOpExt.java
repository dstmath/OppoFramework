package com.mediatek.internal.telephony.datasub;

import android.content.Context;
import android.content.Intent;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import com.mediatek.internal.telephony.dataconnection.MtkDcHelper;

public class SmartDataSwitchAssistantOpExt implements ISmartDataSwitchAssistantOpExt {
    private static boolean DBG = true;
    private static String LOG_TAG = "SmartDataSwitchOpExt";
    private static Context mContext = null;
    private static SmartDataSwitchAssistant mSmartData = null;
    protected int mVoiceNetworkType = 0;

    public SmartDataSwitchAssistantOpExt(Context context) {
        mContext = context;
    }

    @Override // com.mediatek.internal.telephony.datasub.ISmartDataSwitchAssistantOpExt
    public void init(SmartDataSwitchAssistant smartDataSwitchAssistant) {
        logd("init()");
        mSmartData = smartDataSwitchAssistant;
    }

    @Override // com.mediatek.internal.telephony.datasub.ISmartDataSwitchAssistantOpExt
    public void onCallStarted() {
        mSmartData.regServiceStateChangedEvent();
        mSmartData.regSrvccEvent();
    }

    @Override // com.mediatek.internal.telephony.datasub.ISmartDataSwitchAssistantOpExt
    public void onCallEnded() {
        mSmartData.unregServiceStateChangedEvent();
        mSmartData.unregSrvccEvent();
    }

    @Override // com.mediatek.internal.telephony.datasub.ISmartDataSwitchAssistantOpExt
    public void onSubChanged() {
    }

    @Override // com.mediatek.internal.telephony.datasub.ISmartDataSwitchAssistantOpExt
    public void onTemporaryDataSettingsChanged() {
    }

    @Override // com.mediatek.internal.telephony.datasub.ISmartDataSwitchAssistantOpExt
    public void onSrvccStateChanged() {
    }

    @Override // com.mediatek.internal.telephony.datasub.ISmartDataSwitchAssistantOpExt
    public boolean onServiceStateChanged(int phoneId) {
        if (phoneId == mSmartData.getInCallPhoneId() && isNetworkTypeChanged(mSmartData.getVoiceNetworkType(phoneId))) {
            return true;
        }
        return false;
    }

    @Override // com.mediatek.internal.telephony.datasub.ISmartDataSwitchAssistantOpExt
    public void onHandoverToWifi() {
    }

    @Override // com.mediatek.internal.telephony.datasub.ISmartDataSwitchAssistantOpExt
    public void onHandoverToCellular() {
    }

    @Override // com.mediatek.internal.telephony.datasub.ISmartDataSwitchAssistantOpExt
    public boolean preCheckByCallStateExt(Intent intent, boolean result) {
        return result;
    }

    @Override // com.mediatek.internal.telephony.datasub.ISmartDataSwitchAssistantOpExt
    public boolean isNeedSwitchCallType(int callType) {
        return false;
    }

    @Override // com.mediatek.internal.telephony.datasub.ISmartDataSwitchAssistantOpExt
    public boolean isSmartDataSwtichAllowed() {
        return true;
    }

    @Override // com.mediatek.internal.telephony.datasub.ISmartDataSwitchAssistantOpExt
    public boolean checkIsSwitchAvailable(int phoneId) {
        if (!mSmartData.getTemporaryDataSettings()) {
            logd("checkIsSwitchAvailable() settings is off, not passed");
            return false;
        }
        MtkDcHelper dcHelper = MtkDcHelper.getInstance();
        if (dcHelper == null || dcHelper.getDsdaMode() != 1) {
            int nwType = mSmartData.getVoiceNetworkType(phoneId);
            boolean isWifiCalling = mSmartData.isWifcCalling(phoneId);
            boolean isVoLteCalling = mSmartData.isVoLteCalling(phoneId);
            logd("checkIsSwitchAvailable() nwType=" + nwType + ", isCdma=" + ServiceState.isCdma(nwType) + ", isWifcCalling=" + isWifiCalling + ", isVoLteCalling=" + isVoLteCalling);
            if (ServiceState.isCdma(nwType) || nwType == 16 || isWifiCalling || !isVoLteCalling) {
                logd("checkIsSwitchAvailable(): not passed");
                return false;
            }
            logd("checkIsSwitchAvailable(): passed");
            return true;
        }
        logd("checkIsSwitchAvailable(): Dsda mode, not passed");
        return false;
    }

    private boolean isNetworkTypeChanged(int newVoiceNwType) {
        logd("isNetworkTypeChanged: mVoiceNetworkType=" + this.mVoiceNetworkType + " newVoiceNwType=" + newVoiceNwType);
        if (this.mVoiceNetworkType == newVoiceNwType) {
            return false;
        }
        this.mVoiceNetworkType = newVoiceNwType;
        return true;
    }

    protected static void logv(String s) {
        if (DBG) {
            Rlog.v(LOG_TAG, s);
        }
    }

    protected static void logd(String s) {
        if (DBG) {
            Rlog.d(LOG_TAG, s);
        }
    }

    protected static void loge(String s) {
        if (DBG) {
            Rlog.e(LOG_TAG, s);
        }
    }

    protected static void logi(String s) {
        if (DBG) {
            Rlog.i(LOG_TAG, s);
        }
    }
}
