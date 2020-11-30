package com.android.internal.telephony;

import android.os.Handler;
import android.os.Message;
import android.os.RegistrantList;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.gsm.NetworkInfoWithAcT;
import com.android.internal.telephony.gsm.SuppServiceNotification;

public abstract class AbstractPhone extends Handler {
    private static final String TAG = "Phone";
    protected IOppoPhone mReference;

    public boolean getOemAutoAnswer() {
        return this.mReference.getOemAutoAnswer();
    }

    public void setOemAutoAnswer(boolean isAuto) {
        this.mReference.setOemAutoAnswer(isAuto);
    }

    public void setEngineerModeNvProcessCmd(int processcmd) {
    }

    public void setFactoryModeModemGPIO(int status, int num, Message response) {
    }

    public void OppoGetRffeDevInfo(int rf_tech, Message response) {
    }

    public void setModemCrash(Message response) {
    }

    public String getModemVersion() {
        return PhoneConfigurationManager.SSSS;
    }

    public void oppoSetFilterArfcn(int arfcn1, int arfcn2, Message result) {
    }

    public void oppoLockGSMArfcn(int arfcn1, Message result) {
    }

    public void oppoLockLteCell(int arfcn, int pci, Message result) {
    }

    public void oppoGetEsnChangeFlag(Message result) {
    }

    public synchronized void oppoUpdatePplmnList(byte[] value, Message response) {
    }

    public void oppoUpdateFakeBsWeight(int[] values, Message response) {
    }

    public void oppoUpdateVolteFr2(int flags, int rsrp_thresh, int fr2_rsrp, int rsrp_adj, Message response) {
    }

    public void oppoCtlModemFeature(int[] values, Message response) {
    }

    public void oppoNoticeUpdateVolteFr(int flags, Message response) {
    }

    public void oppoSetSarRfStateV2(int state) {
    }

    public void oppoGetBatteryCoverStatus(Message response) {
    }

    public void oppoGetTxRxInfo(int sys_mode, Message response) {
    }

    public void oppoRffeCmd(int[] rffe_params, Message response) {
    }

    public void oppoSetSimType(int sim_sype) {
    }

    public void regFreqHopInd(int enable) {
    }

    public void oppoGetASDIVState(int rat, Message response) {
    }

    public void oemCommonReq(int cmd, byte[] data, int datalen, Message response) {
    }

    public void oppoGetRadioInfo(Message response) {
    }

    public String colorGetImei() {
        return null;
    }

    public void setPOLEntry(NetworkInfoWithAcT networkWithAct, Message onComplete) {
    }

    public void notifySuppService(SuppServiceNotification supp) {
    }

    public String getPnnName(String plmn, boolean longName) {
        return null;
    }

    public String getMvnoPattern(String type) {
        return null;
    }

    public boolean matchUnLock(String imei, String password, int type) {
        return this.mReference.matchUnLock(imei, password, type);
    }

    public void getBandMode(Message response) {
    }

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, Message onComplete) {
    }

    /* access modifiers changed from: protected */
    public String getOEMImei(String mImei) {
        return mImei;
    }

    /* access modifiers changed from: protected */
    public void getImeiAgainIfFailure() {
    }

    /* access modifiers changed from: protected */
    public void saveImeiAndMeid(String mMeid, String mImei) {
    }

    public void registerForLteCAState(Handler h, int what, Object obj) {
    }

    public void unregisterForLteCAState(Handler h) {
    }

    public void OppoSetTddLte(int status, Message response) {
    }

    public int oppoGetSarRfStateV2() {
        return 0;
    }

    public void oppoSetSarRfStateV3(int state) {
    }

    public boolean getManualSearchingStatus() {
        return false;
    }

    public void setManualSearchingStatus(boolean status) {
    }

    public boolean isSRVCC() {
        return this.mReference.isSRVCC();
    }

    public void clearSRVCC() {
        this.mReference.clearSRVCC();
    }

    public void setPeningSRVCC(boolean bl) {
        this.mReference.setPeningSRVCC(bl);
    }

    public boolean getHybridVolteType() {
        return this.mReference.getHybridVolteType();
    }

    public void setHybridVolteType(boolean enable) {
        this.mReference.setHybridVolteType(enable);
    }

    public void registerForShutDownChanged(Handler h, int what, Object obj) {
        this.mReference.registerForShutDownChanged(h, what, obj);
    }

    public void unregisterForShutDownChanged(Handler h) {
        this.mReference.unregisterForShutDownChanged(h);
    }

    public void notifyForShutDownChanged() {
        this.mReference.notifyForShutDownChanged();
    }

    public boolean getVowifiRegStatus() {
        return this.mReference.getVowifiRegStatus();
    }

    public void oemMigrate(RegistrantList to, RegistrantList from) {
        this.mReference.oemMigrate(to, from);
    }

    public void unregister() {
        this.mReference.unregister();
    }

    public void updateSrvccState(Call.SrvccState srvccState) {
        this.mReference.updateSrvccState(srvccState);
    }

    public void keyLogSrvcc(int state) {
        this.mReference.keyLogSrvcc(state);
    }

    public void startMobileDataHongbaoPolicy(int time1, int time2, String value1, String value2) {
        this.mReference.startMobileDataHongbaoPolicy(time1, time2, value1, value2);
    }

    public void updateFreqHopEnable(boolean enable) {
        this.mReference.updateFreqHopEnable(enable);
    }

    public void setSRVCCState(Call.SrvccState srvccState) {
        this.mReference.setSRVCCState(srvccState);
    }

    public boolean getPendingSRVCC() {
        return this.mReference.getPendingSRVCC();
    }

    public void getPreferedOperatorList(Message onComplete) {
    }

    public boolean is_test_card() {
        return this.mReference.is_test_card();
    }

    public String[] getLteCdmaImsi(int phoneid) {
        return this.mReference.getLteCdmaImsi(phoneid);
    }

    public String colorGetIccCardType() {
        return null;
    }

    public void oppoSetSarRfState(int state) {
    }

    public void updateLteWifiCoexist(boolean enabled) {
    }

    public void setVideoCallForwardingFlag(boolean enable) {
        this.mReference.setVideoCallForwardingFlag(enable);
    }

    public boolean getVideoCallForwardingFlag() {
        return this.mReference.getVideoCallForwardingFlag();
    }

    public boolean OppoCheckUsimIs4G() {
        return this.mReference.OppoCheckUsimIs4G();
    }

    public String getOperatorName() {
        return this.mReference.getOperatorName();
    }

    public int getmodemType() {
        return this.mReference.getmodemType();
    }

    public void oppoSetTunerLogic(int antTunerStateIndx, Message msg) {
        this.mReference.oppoSetTunerLogic(antTunerStateIndx, msg);
    }

    public void oppoSetSarRfStateByScene(int scene) {
        this.mReference.oppoSetSarRfStateByScene(scene);
    }

    public void oppoSetTasForceIdx(int mode, int rat, String antenna_idx, String band) {
        this.mReference.oppoSetTasForceIdx(mode, rat, antenna_idx, band);
    }

    public void oppoSetCaSwitch() {
        this.mReference.oppoSetCaSwitch();
    }

    public void oppoSetBandindicationSwitch(boolean on) {
        this.mReference.oppoSetBandindicationSwitch(on);
    }

    public String getRilRequestToString(int request) {
        return RIL.requestToString(request);
    }

    public String getRilResponseToString(int request) {
        return RIL.responseToString(request);
    }

    public String handlePreCheckCFDialingNumber(String dialingNumber) {
        return this.mReference.handlePreCheckCFDialingNumber(dialingNumber);
    }

    public int specifyServiceClassForOperator(int serviceClass) {
        return this.mReference.specifyServiceClassForOperator(serviceClass);
    }

    public void oppoSimlockReq(String[] AtCmd, Message response) {
    }

    public void registerForSimlockState(Handler h, int what, Object obj) {
    }

    public void unregisterForSimlockState(Handler h) {
    }

    public DeviceStateMonitor getDeviceStateMonitor() {
        return null;
    }

    public void handleCustomizedMMICodes(String code) {
        IOppoPhone iOppoPhone = this.mReference;
        if (iOppoPhone != null) {
            iOppoPhone.handleCustomizedMMICodes(code);
        }
    }
}
