package com.mediatek.internal.telephony.dataconnection;

import android.content.Context;
import android.net.NetworkConfig;
import android.net.NetworkRequest;
import android.os.SystemProperties;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DcRequest;

public class MtkDcRequest extends DcRequest {
    public MtkDcRequest(NetworkRequest nr, Context context) {
        super(nr, context);
    }

    /* access modifiers changed from: protected */
    public void initApnPriorities(Context context) {
        synchronized (sApnPriorityMap) {
            if (sApnPriorityMap.isEmpty()) {
                for (String networkConfigString : context.getResources().getStringArray(17236103)) {
                    NetworkConfig networkConfig = new NetworkConfig(networkConfigString);
                    int apnType = ApnContext.getApnTypeFromNetworkType(networkConfig.type);
                    if ((SystemProperties.getInt("persist.vendor.mims_support", 1) > 1) && networkConfig.type == 15) {
                        sApnPriorityMap.put(Integer.valueOf(apnType), -1);
                    } else {
                        sApnPriorityMap.put(Integer.valueOf(apnType), Integer.valueOf(networkConfig.priority));
                    }
                }
            }
        }
    }
}
