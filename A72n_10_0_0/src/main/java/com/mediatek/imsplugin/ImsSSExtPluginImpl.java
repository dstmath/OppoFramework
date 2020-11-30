package com.mediatek.imsplugin;

import android.content.Context;
import android.telephony.ims.ImsCallForwardInfo;
import android.util.Log;
import com.android.internal.telephony.CallForwardInfo;
import com.mediatek.ims.ImsUtImpl;
import com.mediatek.ims.plugin.impl.ImsSSExtPluginBase;

public class ImsSSExtPluginImpl extends ImsSSExtPluginBase {
    private static final String TAG = "ImsSSExtPluginImpl";
    private Context mContext;

    public ImsSSExtPluginImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public ImsCallForwardInfo[] getImsCallForwardInfo(CallForwardInfo[] info) {
        ImsCallForwardInfo[] imsCfInfo = null;
        if (info != null) {
            imsCfInfo = new ImsCallForwardInfo[info.length];
            for (int i = 0; i < info.length; i++) {
                Log.d(TAG, "getImsCallForwardInfo: info[" + i + "] = " + info[i]);
                imsCfInfo[i] = new ImsCallForwardInfo();
                imsCfInfo[i].mCondition = ImsUtImpl.getConditionFromCFReason(info[i].reason);
                imsCfInfo[i].mStatus = info[i].status;
                imsCfInfo[i].mServiceClass = info[i].serviceClass;
                imsCfInfo[i].mToA = info[i].toa;
                imsCfInfo[i].mNumber = info[i].number;
                imsCfInfo[i].mTimeSeconds = info[i].timeSeconds;
            }
        }
        return imsCfInfo;
    }
}
