package com.android.internal.telephony;

import android.content.Context;
import android.content.pm.PackageInfo;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.telephony.dataconnection.DcTracker;

public interface IOppoSmsManager extends IOppoCommonFeature {
    public static final IOppoSmsManager DEFAULT = new IOppoSmsManager() {
        /* class com.android.internal.telephony.IOppoSmsManager.AnonymousClass1 */
    };
    public static final String TAG = "IOppoSmsManager";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoSmsManager;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoSmsManager getDefault() {
        return DEFAULT;
    }

    default boolean oemIsMtSmsBlock(Context context, String number) {
        return false;
    }

    default boolean isSmsBlockByPolicy(Phone phone) {
        return false;
    }

    default boolean oemShouldWriteMessageWhenSafeDialogShow(Context context, PackageInfo packageInfo) {
        return false;
    }

    default String oemGetPackageNameViaProcessId(Context context, String callingPackage) {
        return PhoneConfigurationManager.SSSS;
    }

    default boolean oemAllowMmsWhenDataDisableInRoamingForDcTracker(ApnContext apnContext) {
        return false;
    }

    default boolean oemAllowMmsWhenDataDisableInRoamingForDataConnection(DataConnection.ConnectionParams cp, boolean isModemRoaming, boolean allowRoaming) {
        return false;
    }

    default boolean oemAllowDataWhenDataswitchOffOrDataroamingOffWhenMms(DcTracker dc, Phone phone) {
        return false;
    }

    default boolean oemCheck(String appName, Context contex) {
        return false;
    }
}
