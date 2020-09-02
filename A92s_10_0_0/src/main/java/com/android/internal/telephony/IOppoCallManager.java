package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;

public interface IOppoCallManager extends IOppoCommonFeature {
    public static final IOppoCallManager DEFAULT = new IOppoCallManager() {
        /* class com.android.internal.telephony.IOppoCallManager.AnonymousClass1 */
    };
    public static final String TAG = "IOppoCallManager";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoCallManager;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoCallManager getDefault() {
        return DEFAULT;
    }

    default String getNetworkOperator(Phone phone) {
        return PhoneConfigurationManager.SSSS;
    }

    default String getSignalQuality(Connection c) {
        return PhoneConfigurationManager.SSSS;
    }

    default String getLoc(Connection c) {
        return PhoneConfigurationManager.SSSS;
    }

    default void writeCallRecord(Connection c, Context context) {
    }

    default String dealWithAddress(String addr, boolean isIncoming) {
        return PhoneConfigurationManager.SSSS;
    }

    default String getCurrentDateStr() {
        return PhoneConfigurationManager.SSSS;
    }

    default void updateCallRecord(Context context) {
    }

    default boolean isConferenceHostConnection(boolean isConf, String connAddr, Phone phone) {
        return false;
    }

    default boolean isCurrPhoneInCall(Phone phone) {
        return false;
    }

    default boolean isOtherPhoneInCall(Phone phone) {
        return false;
    }

    default void handleSetCFFDone(Phone phone, int serviceClass, boolean cffEnable, String dialingNum) {
    }

    default void checkVoocState(String val) {
    }

    default boolean isCtcCardCtaTest(Context mContext, Phone phone) {
        return false;
    }

    default boolean isUssiEnabled(Phone phone) {
        return false;
    }

    default boolean isRestricted(int type, int slot) {
        return false;
    }
}
