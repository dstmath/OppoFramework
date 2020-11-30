package com.android.internal.telephony;

import android.os.Handler;
import android.os.Message;
import android.os.RegistrantList;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;

public interface IOppoPhone extends IOppoCommonFeature {
    public static final IOppoPhone DEFAULT = new IOppoPhone() {
        /* class com.android.internal.telephony.IOppoPhone.AnonymousClass1 */
    };
    public static final String TAG = "IOppoPhone";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoPhone;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoPhone getDefault() {
        return DEFAULT;
    }

    default boolean getOemAutoAnswer() {
        return false;
    }

    default void setOemAutoAnswer(boolean isAuto) {
    }

    default boolean isSRVCC() {
        return false;
    }

    default void clearSRVCC() {
    }

    default void setPeningSRVCC(boolean bl) {
    }

    default boolean getHybridVolteType() {
        return false;
    }

    default void setHybridVolteType(boolean enable) {
    }

    default void registerForShutDownChanged(Handler h, int what, Object obj) {
    }

    default void unregisterForShutDownChanged(Handler h) {
    }

    default void notifyForShutDownChanged() {
    }

    default boolean getVowifiRegStatus() {
        return false;
    }

    default void oemMigrate(RegistrantList to, RegistrantList from) {
    }

    default void unregister() {
    }

    default void updateSrvccState(Call.SrvccState srvccState) {
    }

    default void keyLogSrvcc(int state) {
    }

    default void startMobileDataHongbaoPolicy(int time1, int time2, String value1, String value2) {
    }

    default void updateFreqHopEnable(boolean enable) {
    }

    default void setSRVCCState(Call.SrvccState srvccState) {
    }

    default boolean getPendingSRVCC() {
        return false;
    }

    default boolean is_test_card() {
        return false;
    }

    default String[] getLteCdmaImsi(int phoneid) {
        return null;
    }

    default boolean matchUnLock(String imei, String password, int type) {
        return false;
    }

    default void setVideoCallForwardingFlag(boolean enable) {
    }

    default boolean getVideoCallForwardingFlag() {
        return false;
    }

    default boolean OppoCheckUsimIs4G() {
        return false;
    }

    default String getOperatorName() {
        return PhoneConfigurationManager.SSSS;
    }

    default int getmodemType() {
        return 0;
    }

    default void oppoSetTunerLogic(int antTunerStateIndx, Message msg) {
    }

    default void oppoSetSarRfStateByScene(int scene) {
    }

    default void oppoSetTasForceIdx(int mode, int rat, String antenna_idx, String band) {
    }

    default void oppoSetCaSwitch() {
    }

    default void oppoSetBandindicationSwitch(boolean on) {
    }

    default String handlePreCheckCFDialingNumber(String dialingNumber) {
        return PhoneConfigurationManager.SSSS;
    }

    default int specifyServiceClassForOperator(int serviceClass) {
        return 0;
    }

    default void handleCustomizedMMICodes(String code) {
    }
}
