package com.oppo.internal.telephony;

import android.content.Context;
import android.net.LinkProperties;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.data.ApnSetting;
import android.telephony.data.DataCallResponse;
import android.text.TextUtils;
import com.android.internal.telephony.IOppoDataManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.oppo.internal.telephony.dataconnection.OppoDataCommonUtils;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseUtils;
import com.oppo.internal.telephony.utils.OppoPhoneUtil;
import com.oppo.internal.telephony.utils.OppoPolicyController;
import java.util.List;

public class OppoDataManagerImpl implements IOppoDataManager {
    private static OppoDataManagerImpl sInstance = null;

    public static OppoDataManagerImpl getInstance() {
        OppoDataManagerImpl oppoDataManagerImpl;
        OppoDataManagerImpl oppoDataManagerImpl2 = sInstance;
        if (oppoDataManagerImpl2 != null) {
            return oppoDataManagerImpl2;
        }
        synchronized (OppoDataManagerImpl.class) {
            if (sInstance == null) {
                sInstance = new OppoDataManagerImpl();
            }
            oppoDataManagerImpl = sInstance;
        }
        return oppoDataManagerImpl;
    }

    public boolean handleDataBlockControl(Phone phone, DataConnection.ConnectionParams cp, ApnContext apnContext, DataConnection dc, int event) {
        if (OppoPolicyController.isPoliceVersion(phone)) {
            boolean isDataAllow = OppoPolicyController.isDataAllow(phone);
            String apntype = "";
            if (!(cp == null || apnContext == null)) {
                apntype = apnContext.getApnType();
            }
            boolean isSpecialApn = !TextUtils.isEmpty(apntype) && ("ims".equals(apntype) || "emergency".equals(apntype));
            Rlog.d(NetworkDiagnoseUtils.INFO_APCONFIG_DATA, "onConnect:isDataAllow=" + isDataAllow + " apntype=" + apntype + "  isSpecialApn=" + isSpecialApn);
            if (!isDataAllow && !isSpecialApn) {
                DataCallResponse response = new DataCallResponse(65535, -1, 0, 0, 0, "", (List) null, (List) null, (List) null, (List) null, 0);
                Message msg = dc.obtainMessage(event, cp);
                msg.getData().putParcelable("data_call_response", response);
                dc.sendMessage(msg);
                return true;
            }
        }
        return false;
    }

    public boolean handleDataBlockControl(Phone phone, boolean enabled) {
        if (phone == null || !OppoPolicyController.isPoliceVersion(phone) || OppoPolicyController.canSwitchByUser(phone) || enabled == OppoPolicyController.isDataAllow(phone)) {
            return false;
        }
        Rlog.d(NetworkDiagnoseUtils.INFO_APCONFIG_DATA, "---data-enable-return---");
        return true;
    }

    public boolean isDataAllowByPolicy(Phone phone) {
        try {
            if (OppoPolicyController.isPoliceVersion(phone)) {
                boolean isDataAllow = OppoPolicyController.isDataAllow(phone);
                Rlog.d(NetworkDiagnoseUtils.INFO_APCONFIG_DATA, "---isDataAllowByPolicy isDataAllow == " + isDataAllow);
                if (!isDataAllow) {
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean getWlanAssistantEnable(Context context) {
        return OppoPhoneUtil.getWlanAssistantEnable(context);
    }

    public boolean oemCheckSetMtu(ApnSetting apn, LinkProperties lp, Phone phone) {
        return OppoDataCommonUtils.oemCheckSetMtu(apn, lp, phone);
    }
}
