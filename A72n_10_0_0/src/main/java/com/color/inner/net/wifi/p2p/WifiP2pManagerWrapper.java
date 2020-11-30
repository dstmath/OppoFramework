package com.color.inner.net.wifi.p2p;

import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class WifiP2pManagerWrapper {
    private static final String TAG = "WifiP2pManagerWrapper";

    public interface ActionListenerWrapper {
        void onFailure(int i);

        void onSuccess();
    }

    public interface PersistentGroupInfoListenerWrapper {
        void onPersistentGroupInfoAvailable(WifiP2pGroupListWrapper wifiP2pGroupListWrapper);
    }

    private WifiP2pManagerWrapper() {
    }

    public static void requestPersistentGroupInfo(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel c, final PersistentGroupInfoListenerWrapper listener) {
        WifiP2pManager.PersistentGroupInfoListener persistentGroupInfoListener = null;
        if (listener != null) {
            try {
                persistentGroupInfoListener = new WifiP2pManager.PersistentGroupInfoListener() {
                    /* class com.color.inner.net.wifi.p2p.WifiP2pManagerWrapper.AnonymousClass1 */

                    public void onPersistentGroupInfoAvailable(WifiP2pGroupList groups) {
                        WifiP2pGroupListWrapper wrapper = new WifiP2pGroupListWrapper();
                        wrapper.setWifiP2pGroupList(groups);
                        PersistentGroupInfoListenerWrapper.this.onPersistentGroupInfoAvailable(wrapper);
                    }
                };
            } catch (Throwable e) {
                Log.e(TAG, e.toString());
                return;
            }
        }
        wifiP2pManager.requestPersistentGroupInfo(c, persistentGroupInfoListener);
    }

    public static void deletePersistentGroup(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel c, int netId, final ActionListenerWrapper listener) {
        WifiP2pManager.ActionListener actionListener = null;
        if (listener != null) {
            try {
                actionListener = new WifiP2pManager.ActionListener() {
                    /* class com.color.inner.net.wifi.p2p.WifiP2pManagerWrapper.AnonymousClass2 */

                    public void onSuccess() {
                        ActionListenerWrapper.this.onSuccess();
                    }

                    public void onFailure(int reason) {
                        ActionListenerWrapper.this.onFailure(reason);
                    }
                };
            } catch (Throwable e) {
                Log.e(TAG, e.toString());
                return;
            }
        }
        wifiP2pManager.deletePersistentGroup(c, netId, actionListener);
    }
}
