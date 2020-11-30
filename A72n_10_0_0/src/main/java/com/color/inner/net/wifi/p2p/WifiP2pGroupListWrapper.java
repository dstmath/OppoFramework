package com.color.inner.net.wifi.p2p;

import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.util.Log;
import java.util.Collection;

public class WifiP2pGroupListWrapper {
    private static final String TAG = "WifiP2pGroupListWrapper";
    private WifiP2pGroupList mWifiP2pGroupList;

    /* access modifiers changed from: package-private */
    public void setWifiP2pGroupList(WifiP2pGroupList wifiP2pGroupList) {
        this.mWifiP2pGroupList = wifiP2pGroupList;
    }

    public Collection<WifiP2pGroup> getGroupList() {
        try {
            if (this.mWifiP2pGroupList == null) {
                return null;
            }
            return this.mWifiP2pGroupList.getGroupList();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
