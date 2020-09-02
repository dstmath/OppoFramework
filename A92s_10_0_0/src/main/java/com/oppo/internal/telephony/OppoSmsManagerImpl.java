package com.oppo.internal.telephony;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.telephony.Rlog;
import com.android.internal.telephony.IOppoSmsManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseUtils;
import com.oppo.internal.telephony.utils.OppoPolicyController;

public class OppoSmsManagerImpl implements IOppoSmsManager {
    private static final String[] SKIP_SEND_LIMIT_PACKAGES = {"com.google.android.apps.messaging", "com.android.mms"};
    private static OppoSmsManagerImpl sInstance = null;

    public static OppoSmsManagerImpl getInstance() {
        OppoSmsManagerImpl oppoSmsManagerImpl;
        OppoSmsManagerImpl oppoSmsManagerImpl2 = sInstance;
        if (oppoSmsManagerImpl2 != null) {
            return oppoSmsManagerImpl2;
        }
        synchronized (OppoSmsManagerImpl.class) {
            if (sInstance == null) {
                sInstance = new OppoSmsManagerImpl();
            }
            oppoSmsManagerImpl = sInstance;
        }
        return oppoSmsManagerImpl;
    }

    public boolean isSmsBlockByPolicy(Phone phone) {
        if (!OppoPolicyController.isPoliceVersion(phone)) {
            return false;
        }
        boolean isPolicyMessageReceEnable = OppoPolicyController.isSmsReceiveEnable(phone);
        Rlog.d(NetworkDiagnoseUtils.INFO_OTHER_SMS, "isPolicyMessageReceEnable=" + isPolicyMessageReceEnable);
        return !isPolicyMessageReceEnable;
    }

    public boolean oemIsMtSmsBlock(Context context, String number) {
        return OppoSmsCommonUtils.oemIsMtSmsBlock(context, number);
    }

    public boolean oemShouldWriteMessageWhenSafeDialogShow(Context context, PackageInfo packageInfo) {
        return OppoSmsCommonUtils.oemShouldWriteMessageWhenSafeDialogShow(context, packageInfo);
    }

    public String oemGetPackageNameViaProcessId(Context context, String callingPackage) {
        return OppoSmsCommonUtils.oemGetPackageNameViaProcessId(context, callingPackage);
    }

    public boolean oemAllowMmsWhenDataDisableInRoamingForDcTracker(ApnContext apnContext) {
        return OppoSmsCommonUtils.oemAllowMmsWhenDataDisableInRoamingForDcTracker(apnContext);
    }

    public boolean oemAllowMmsWhenDataDisableInRoamingForDataConnection(DataConnection.ConnectionParams cp, boolean isModemRoaming, boolean allowRoaming) {
        return OppoSmsCommonUtils.oemAllowMmsWhenDataDisableInRoamingForDataConnection(cp, isModemRoaming, allowRoaming);
    }

    public boolean oemAllowDataWhenDataswitchOffOrDataroamingOffWhenMms(DcTracker dc, Phone phone) {
        return OppoSmsCommonUtils.oemAllowDataWhenDataswitchOffOrDataroamingOffWhenMms(dc, phone);
    }

    public boolean oemCheck(String appName, Context contex) {
        if (contex == null) {
            return false;
        }
        try {
            if (SKIP_SEND_LIMIT_PACKAGES != null) {
                String[] strArr = SKIP_SEND_LIMIT_PACKAGES;
                for (String name : strArr) {
                    if (name != null && name.equals(appName)) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Rlog.d("IOppoSmsManager", "appName=" + appName);
        return false;
    }
}
