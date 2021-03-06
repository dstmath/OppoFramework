package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.net.NetworkConfig;
import android.net.NetworkRequest;
import java.util.HashMap;

public class DcRequest implements Comparable<DcRequest> {
    private static final String LOG_TAG = "DcRequest";
    protected static final HashMap<Integer, Integer> sApnPriorityMap = new HashMap<>();
    public final int apnType = ApnContext.getApnTypeFromNetworkRequest(this.networkRequest);
    public final NetworkRequest networkRequest;
    public final int priority = priorityForApnType(this.apnType);

    public DcRequest(NetworkRequest nr, Context context) {
        initApnPriorities(context);
        this.networkRequest = nr;
    }

    public String toString() {
        return this.networkRequest.toString() + ", priority=" + this.priority + ", apnType=" + this.apnType;
    }

    public int hashCode() {
        return this.networkRequest.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof DcRequest) {
            return this.networkRequest.equals(((DcRequest) o).networkRequest);
        }
        return false;
    }

    public int compareTo(DcRequest o) {
        return o.priority - this.priority;
    }

    /* access modifiers changed from: protected */
    public void initApnPriorities(Context context) {
        synchronized (sApnPriorityMap) {
            if (sApnPriorityMap.isEmpty()) {
                for (String networkConfigString : context.getResources().getStringArray(17236107)) {
                    NetworkConfig networkConfig = new NetworkConfig(networkConfigString);
                    sApnPriorityMap.put(Integer.valueOf(ApnContext.getApnTypeFromNetworkType(networkConfig.type)), Integer.valueOf(networkConfig.priority));
                }
            }
        }
    }

    private int priorityForApnType(int apnType2) {
        Integer priority2 = sApnPriorityMap.get(Integer.valueOf(apnType2));
        if (priority2 != null) {
            return priority2.intValue();
        }
        return 0;
    }
}
