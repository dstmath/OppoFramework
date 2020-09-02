package com.android.internal.telephony;

import android.util.TimestampedValue;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;

public interface IOppoNewNitzStateMachine extends IOppoCommonFeature {
    public static final IOppoNewNitzStateMachine DEFAULT = new IOppoNewNitzStateMachine() {
        /* class com.android.internal.telephony.IOppoNewNitzStateMachine.AnonymousClass1 */
    };
    public static final String TAG = "IOppoNewNitzStateMachine";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoNewNitzStateMachine;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoNewNitzStateMachine getDefault() {
        return DEFAULT;
    }

    default void OppoisUseNtptime(long currenttimeinmillis, long millisSinceNitzReceived, long ReferenceTimeMillis, TimestampedValue<Long> timestampedValue) {
    }

    default void OppoSetUseNtpTime(String name, String value) {
    }

    default String OppoGetTimeZonesWithCapitalCity(String iso) {
        return PhoneConfigurationManager.SSSS;
    }

    default void OppoRecordNitzTimeZone(int settingType, String zoneId) {
    }
}
