package com.android.internal.telephony;

import android.content.Context;
import android.net.LinkProperties;
import android.telephony.data.ApnSetting;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DataConnection;

public interface IOppoDataManager extends IOppoCommonFeature {
    public static final IOppoDataManager DEFAULT = new IOppoDataManager() {
        /* class com.android.internal.telephony.IOppoDataManager.AnonymousClass1 */
    };
    public static final String TAG = "IOppoDataManager";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoDataManager;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoDataManager getDefault() {
        return DEFAULT;
    }

    default boolean getWlanAssistantEnable(Context context) {
        return false;
    }

    default boolean handleDataBlockControl(Phone phone, DataConnection.ConnectionParams cp, ApnContext apnContext, DataConnection dc, int event) {
        return false;
    }

    default boolean handleDataBlockControl(Phone phone, boolean enabled) {
        return false;
    }

    default boolean isDataAllowByPolicy(Phone phone) {
        return false;
    }

    default boolean oemCheckSetMtu(ApnSetting apn, LinkProperties lp, Phone phone) {
        return false;
    }
}
